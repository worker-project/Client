package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;

public abstract class Screen extends AbstractContainerEventHandler implements Widget
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> ALLOWED_PROTOCOLS = Sets.newHashSet("http", "https");
    private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
    private static final Component USAGE_NARRATION = new TranslatableComponent("narrator.screen.usage");
    protected final Component title;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<NarratableEntry> narratables = Lists.newArrayList();
    @Nullable
    protected Minecraft minecraft;
    protected ItemRenderer itemRenderer;
    public int width;
    public int height;
    private final List<Widget> renderables = Lists.newArrayList();
    public boolean passEvents;
    protected Font font;
    @Nullable
    private URI clickedLink;
    private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
    private static final long NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME;
    private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
    private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
    private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
    private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
    private long narrationSuppressTime = Long.MIN_VALUE;
    private long nextNarrationTime = Long.MAX_VALUE;
    @Nullable
    private NarratableEntry lastNarratable;

    protected Screen(Component pTitle)
    {
        this.title = pTitle;
    }

    public Component getTitle()
    {
        return this.title;
    }

    public Component getNarrationMessage()
    {
        return this.getTitle();
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        for (Widget widget : this.renderables)
        {
            widget.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (pKeyCode == 256 && this.shouldCloseOnEsc())
        {
            this.onClose();
            return true;
        }
        else if (pKeyCode == 258)
        {
            boolean flag = !hasShiftDown();

            if (!this.changeFocus(flag))
            {
                this.changeFocus(flag);
            }

            return false;
        }
        else
        {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    public boolean shouldCloseOnEsc()
    {
        return true;
    }

    public void onClose()
    {
        this.minecraft.setScreen((Screen)null);
    }

    protected <T extends GuiEventListener & Widget & NarratableEntry> T addRenderableWidget(T pWidget)
    {
        this.renderables.add(pWidget);
        return this.addWidget(pWidget);
    }

    protected <T extends Widget> T addRenderableOnly(T pWidget)
    {
        this.renderables.add(pWidget);
        return pWidget;
    }

    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T pListener)
    {
        this.children.add(pListener);
        this.narratables.add(pListener);
        return pListener;
    }

    protected void removeWidget(GuiEventListener p_169412_)
    {
        if (p_169412_ instanceof Widget)
        {
            this.renderables.remove((Widget)p_169412_);
        }

        if (p_169412_ instanceof NarratableEntry)
        {
            this.narratables.remove((NarratableEntry)p_169412_);
        }

        this.children.remove(p_169412_);
    }

    protected void clearWidgets()
    {
        this.renderables.clear();
        this.children.clear();
        this.narratables.clear();
    }

    protected void renderTooltip(PoseStack pPoseStack, ItemStack pTooltips, int pMouseX, int pMouseY)
    {
        this.renderTooltip(pPoseStack, this.getTooltipFromItem(pTooltips), pTooltips.getTooltipImage(), pMouseX, pMouseY);
    }

    public void renderTooltip(PoseStack pPoseStack, List<Component> pTooltips, Optional<TooltipComponent> pVisualTooltipComponent, int pMouseX, int pMouseY)
    {
        List<ClientTooltipComponent> list = pTooltips.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
        pVisualTooltipComponent.ifPresent((p_169399_) ->
        {
            list.add(1, ClientTooltipComponent.create(p_169399_));
        });
        this.renderTooltipInternal(pPoseStack, list, pMouseX, pMouseY);
    }

    public List<Component> getTooltipFromItem(ItemStack pItemStack)
    {
        return pItemStack.getTooltipLines(this.minecraft.player, this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public void renderTooltip(PoseStack pPoseStack, Component pTooltips, int pMouseX, int pMouseY)
    {
        this.renderTooltip(pPoseStack, Arrays.asList(pTooltips.getVisualOrderText()), pMouseX, pMouseY);
    }

    public void renderComponentTooltip(PoseStack pPoseStack, List<Component> pTooltips, int pMouseX, int pMouseY)
    {
        this.renderTooltip(pPoseStack, Lists.transform(pTooltips, Component::getVisualOrderText), pMouseX, pMouseY);
    }

    public void renderTooltip(PoseStack pPoseStack, List <? extends FormattedCharSequence > pTooltips, int pMouseX, int pMouseY)
    {
        this.renderTooltipInternal(pPoseStack, pTooltips.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), pMouseX, pMouseY);
    }

    private void renderTooltipInternal(PoseStack pPoseStack, List<ClientTooltipComponent> pClientTooltipComponents, int pMouseX, int pMouseY)
    {
        if (!pClientTooltipComponents.isEmpty())
        {
            int i = 0;
            int j = pClientTooltipComponents.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clienttooltipcomponent : pClientTooltipComponents)
            {
                int k = clienttooltipcomponent.getWidth(this.font);

                if (k > i)
                {
                    i = k;
                }

                j += clienttooltipcomponent.getHeight();
            }

            int j2 = pMouseX + 12;
            int k2 = pMouseY - 12;

            if (j2 + i > this.width)
            {
                j2 -= 28 + i;
            }

            if (k2 + j + 6 > this.height)
            {
                k2 = this.height - j - 6;
            }

            pPoseStack.pushPose();
            int l = -267386864;
            int i1 = 1347420415;
            int j1 = 1344798847;
            int k1 = 400;
            float f = this.itemRenderer.blitOffset;
            this.itemRenderer.blitOffset = 400.0F;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix4f = pPoseStack.last().pose();
            fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 - 4, j2 + i + 3, k2 - 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 + j + 3, j2 + i + 3, k2 + j + 4, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 - 3, j2 + i + 3, k2 + j + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferbuilder, j2 - 4, k2 - 3, j2 - 3, k2 + j + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferbuilder, j2 + i + 3, k2 - 3, j2 + i + 4, k2 + j + 3, 400, -267386864, -267386864);
            fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + j + 3 - 1, 400, 1347420415, 1344798847);
            fillGradient(matrix4f, bufferbuilder, j2 + i + 2, k2 - 3 + 1, j2 + i + 3, k2 + j + 3 - 1, 400, 1347420415, 1344798847);
            fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 - 3, j2 + i + 3, k2 - 3 + 1, 400, 1347420415, 1347420415);
            fillGradient(matrix4f, bufferbuilder, j2 - 3, k2 + j + 2, j2 + i + 3, k2 + j + 3, 400, 1344798847, 1344798847);
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            bufferbuilder.end();
            BufferUploader.end(bufferbuilder);
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            pPoseStack.translate(0.0D, 0.0D, 400.0D);
            int l1 = k2;

            for (int i2 = 0; i2 < pClientTooltipComponents.size(); ++i2)
            {
                ClientTooltipComponent clienttooltipcomponent1 = pClientTooltipComponents.get(i2);
                clienttooltipcomponent1.renderText(this.font, j2, l1, matrix4f, multibuffersource$buffersource);
                l1 += clienttooltipcomponent1.getHeight() + (i2 == 0 ? 2 : 0);
            }

            multibuffersource$buffersource.endBatch();
            pPoseStack.popPose();
            l1 = k2;

            for (int l2 = 0; l2 < pClientTooltipComponents.size(); ++l2)
            {
                ClientTooltipComponent clienttooltipcomponent2 = pClientTooltipComponents.get(l2);
                clienttooltipcomponent2.renderImage(this.font, j2, l1, pPoseStack, this.itemRenderer, 400);
                l1 += clienttooltipcomponent2.getHeight() + (l2 == 0 ? 2 : 0);
            }

            this.itemRenderer.blitOffset = f;
        }
    }

    protected void renderComponentHoverEffect(PoseStack pPoseStack, @Nullable Style pStyle, int pMouseX, int pMouseY)
    {
        if (pStyle != null && pStyle.getHoverEvent() != null)
        {
            HoverEvent hoverevent = pStyle.getHoverEvent();
            HoverEvent.ItemStackInfo hoverevent$itemstackinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ITEM);

            if (hoverevent$itemstackinfo != null)
            {
                this.renderTooltip(pPoseStack, hoverevent$itemstackinfo.getItemStack(), pMouseX, pMouseY);
            }
            else
            {
                HoverEvent.EntityTooltipInfo hoverevent$entitytooltipinfo = hoverevent.getValue(HoverEvent.Action.SHOW_ENTITY);

                if (hoverevent$entitytooltipinfo != null)
                {
                    if (this.minecraft.options.advancedItemTooltips)
                    {
                        this.renderComponentTooltip(pPoseStack, hoverevent$entitytooltipinfo.getTooltipLines(), pMouseX, pMouseY);
                    }
                }
                else
                {
                    Component component = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);

                    if (component != null)
                    {
                        this.renderTooltip(pPoseStack, this.minecraft.font.split(component, Math.max(this.width / 2, 200)), pMouseX, pMouseY);
                    }
                }
            }
        }
    }

    protected void insertText(String pText, boolean pOverwrite)
    {
    }

    public boolean handleComponentClicked(@Nullable Style pStyle)
    {
        if (pStyle == null)
        {
            return false;
        }
        else
        {
            ClickEvent clickevent = pStyle.getClickEvent();

            if (hasShiftDown())
            {
                if (pStyle.getInsertion() != null)
                {
                    this.insertText(pStyle.getInsertion(), false);
                }
            }
            else if (clickevent != null)
            {
                if (clickevent.getAction() == ClickEvent.Action.OPEN_URL)
                {
                    if (!this.minecraft.options.chatLinks)
                    {
                        return false;
                    }

                    try
                    {
                        URI uri = new URI(clickevent.getValue());
                        String s = uri.getScheme();

                        if (s == null)
                        {
                            throw new URISyntaxException(clickevent.getValue(), "Missing protocol");
                        }

                        if (!ALLOWED_PROTOCOLS.contains(s.toLowerCase(Locale.ROOT)))
                        {
                            throw new URISyntaxException(clickevent.getValue(), "Unsupported protocol: " + s.toLowerCase(Locale.ROOT));
                        }

                        if (this.minecraft.options.chatLinksPrompt)
                        {
                            this.clickedLink = uri;
                            this.minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, clickevent.getValue(), false));
                        }
                        else
                        {
                            this.openLink(uri);
                        }
                    }
                    catch (URISyntaxException urisyntaxexception)
                    {
                        LOGGER.error("Can't open url for {}", clickevent, urisyntaxexception);
                    }
                }
                else if (clickevent.getAction() == ClickEvent.Action.OPEN_FILE)
                {
                    URI uri1 = (new File(clickevent.getValue())).toURI();
                    this.openLink(uri1);
                }
                else if (clickevent.getAction() == ClickEvent.Action.SUGGEST_COMMAND)
                {
                    this.insertText(clickevent.getValue(), true);
                }
                else if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND)
                {
                    this.sendMessage(clickevent.getValue(), false);
                }
                else if (clickevent.getAction() == ClickEvent.Action.COPY_TO_CLIPBOARD)
                {
                    this.minecraft.keyboardHandler.setClipboard(clickevent.getValue());
                }
                else
                {
                    LOGGER.error("Don't know how to handle {}", (Object)clickevent);
                }

                return true;
            }

            return false;
        }
    }

    public void sendMessage(String pText)
    {
        this.sendMessage(pText, true);
    }

    public void sendMessage(String pText, boolean pAddToChat)
    {
        if (pAddToChat)
        {
            this.minecraft.gui.getChat().addRecentChat(pText);
        }

        this.minecraft.player.chat(pText);
    }

    public final void init(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        this.minecraft = pMinecraft;
        this.itemRenderer = pMinecraft.getItemRenderer();
        this.font = pMinecraft.font;
        this.width = pWidth;
        this.height = pHeight;
        this.clearWidgets();
        this.setFocused((GuiEventListener)null);
        this.init();
        this.triggerImmediateNarration(false);
        this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
    }

    public List <? extends GuiEventListener > children()
    {
        return this.children;
    }

    protected void init()
    {
    }

    public void tick()
    {
    }

    public void removed()
    {
    }

    public void renderBackground(PoseStack pPoseStack)
    {
        this.renderBackground(pPoseStack, 0);
    }

    public void renderBackground(PoseStack pPoseStack, int pVOffset)
    {
        if (this.minecraft.level != null)
        {
            this.fillGradient(pPoseStack, 0, 0, this.width, this.height, -1072689136, -804253680);
        }
        else
        {
            this.renderDirtBackground(pVOffset);
        }
    }

    public void renderDirtBackground(int pVOffset)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(0.0D, (double)this.height, 0.0D).uv(0.0F, (float)this.height / 32.0F + (float)pVOffset).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex((double)this.width, (double)this.height, 0.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F + (float)pVOffset).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex((double)this.width, 0.0D, 0.0D).uv((float)this.width / 32.0F, (float)pVOffset).color(64, 64, 64, 255).endVertex();
        bufferbuilder.vertex(0.0D, 0.0D, 0.0D).uv(0.0F, (float)pVOffset).color(64, 64, 64, 255).endVertex();
        tesselator.end();
    }

    public boolean isPauseScreen()
    {
        return true;
    }

    private void confirmLink(boolean p_96623_)
    {
        if (p_96623_)
        {
            this.openLink(this.clickedLink);
        }

        this.clickedLink = null;
        this.minecraft.setScreen(this);
    }

    private void openLink(URI pUri)
    {
        Util.getPlatform().openUri(pUri);
    }

    public static boolean hasControlDown()
    {
        if (Minecraft.ON_OSX)
        {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
        }
        else
        {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
        }
    }

    public static boolean hasShiftDown()
    {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
    }

    public static boolean hasAltDown()
    {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
    }

    public static boolean isCut(int pKeyCode)
    {
        return pKeyCode == 88 && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public static boolean isPaste(int pKeyCode)
    {
        return pKeyCode == 86 && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public static boolean isCopy(int pKeyCode)
    {
        return pKeyCode == 67 && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public static boolean isSelectAll(int pKeyCode)
    {
        return pKeyCode == 65 && hasControlDown() && !hasShiftDown() && !hasAltDown();
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        this.init(pMinecraft, pWidth, pHeight);
    }

    public static void wrapScreenError(Runnable pAction, String pErrorDesc, String pScreenName)
    {
        try
        {
            pAction.run();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, pErrorDesc);
            CrashReportCategory crashreportcategory = crashreport.addCategory("Affected screen");
            crashreportcategory.setDetail("Screen name", () ->
            {
                return pScreenName;
            });
            throw new ReportedException(crashreport);
        }
    }

    protected boolean isValidCharacterForName(String pText, char pCharTyped, int pCursorPos)
    {
        int i = pText.indexOf(58);
        int j = pText.indexOf(47);

        if (pCharTyped == ':')
        {
            return (j == -1 || pCursorPos <= j) && i == -1;
        }
        else if (pCharTyped == '/')
        {
            return pCursorPos > i;
        }
        else
        {
            return pCharTyped == '_' || pCharTyped == '-' || pCharTyped >= 'a' && pCharTyped <= 'z' || pCharTyped >= '0' && pCharTyped <= '9' || pCharTyped == '.';
        }
    }

    public boolean isMouseOver(double pMouseX, double p_96596_)
    {
        return true;
    }

    public void onFilesDrop(List<Path> pPacks)
    {
    }

    private void scheduleNarration(long p_169381_, boolean p_169382_)
    {
        this.nextNarrationTime = Util.getMillis() + p_169381_;

        if (p_169382_)
        {
            this.narrationSuppressTime = Long.MIN_VALUE;
        }
    }

    private void suppressNarration(long p_169379_)
    {
        this.narrationSuppressTime = Util.getMillis() + p_169379_;
    }

    public void afterMouseMove()
    {
        this.scheduleNarration(750L, false);
    }

    public void afterMouseAction()
    {
        this.scheduleNarration(200L, true);
    }

    public void afterKeyboardAction()
    {
        this.scheduleNarration(200L, true);
    }

    private boolean shouldRunNarration()
    {
        return NarratorChatListener.INSTANCE.isActive();
    }

    public void handleDelayedNarration()
    {
        if (this.shouldRunNarration())
        {
            long i = Util.getMillis();

            if (i > this.nextNarrationTime && i > this.narrationSuppressTime)
            {
                this.runNarration(true);
                this.nextNarrationTime = Long.MAX_VALUE;
            }
        }
    }

    protected void triggerImmediateNarration(boolean p_169408_)
    {
        if (this.shouldRunNarration())
        {
            this.runNarration(p_169408_);
        }
    }

    private void runNarration(boolean p_169410_)
    {
        this.narrationState.update(this::updateNarrationState);
        String s = this.narrationState.collectNarrationText(!p_169410_);

        if (!s.isEmpty())
        {
            NarratorChatListener.INSTANCE.sayNow(s);
        }
    }

    protected void updateNarrationState(NarrationElementOutput p_169396_)
    {
        p_169396_.add(NarratedElementType.TITLE, this.getNarrationMessage());
        p_169396_.add(NarratedElementType.USAGE, USAGE_NARRATION);
        this.updateNarratedWidget(p_169396_);
    }

    protected void updateNarratedWidget(NarrationElementOutput p_169403_)
    {
        ImmutableList<NarratableEntry> immutablelist = this.narratables.stream().filter(NarratableEntry::isActive).collect(ImmutableList.toImmutableList());
        Screen.NarratableSearchResult screen$narratablesearchresult = findNarratableWidget(immutablelist, this.lastNarratable);

        if (screen$narratablesearchresult != null)
        {
            if (screen$narratablesearchresult.priority.isTerminal())
            {
                this.lastNarratable = screen$narratablesearchresult.entry;
            }

            if (immutablelist.size() > 1)
            {
                p_169403_.add(NarratedElementType.POSITION, new TranslatableComponent("narrator.position.screen", screen$narratablesearchresult.index + 1, immutablelist.size()));

                if (screen$narratablesearchresult.priority == NarratableEntry.NarrationPriority.FOCUSED)
                {
                    p_169403_.add(NarratedElementType.USAGE, new TranslatableComponent("narration.component_list.usage"));
                }
            }

            screen$narratablesearchresult.entry.updateNarration(p_169403_.nest());
        }
    }

    @Nullable
    public static Screen.NarratableSearchResult findNarratableWidget(List <? extends NarratableEntry > p_169401_, @Nullable NarratableEntry p_169402_)
    {
        Screen.NarratableSearchResult screen$narratablesearchresult = null;
        Screen.NarratableSearchResult screen$narratablesearchresult1 = null;
        int i = 0;

        for (int j = p_169401_.size(); i < j; ++i)
        {
            NarratableEntry narratableentry = p_169401_.get(i);
            NarratableEntry.NarrationPriority narratableentry$narrationpriority = narratableentry.narrationPriority();

            if (narratableentry$narrationpriority.isTerminal())
            {
                if (narratableentry != p_169402_)
                {
                    return new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
                }

                screen$narratablesearchresult1 = new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
            }
            else if (narratableentry$narrationpriority.compareTo(screen$narratablesearchresult != null ? screen$narratablesearchresult.priority : NarratableEntry.NarrationPriority.NONE) > 0)
            {
                screen$narratablesearchresult = new Screen.NarratableSearchResult(narratableentry, i, narratableentry$narrationpriority);
            }
        }

        return screen$narratablesearchresult != null ? screen$narratablesearchresult : screen$narratablesearchresult1;
    }

    public void narrationEnabled()
    {
        this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
    }

    protected static void a(AbstractWidget... p_202377_)
    {
        for (AbstractWidget abstractwidget : p_202377_)
        {
            abstractwidget.visible = false;
        }
    }

    public static class NarratableSearchResult
    {
        public final NarratableEntry entry;
        public final int index;
        public final NarratableEntry.NarrationPriority priority;

        public NarratableSearchResult(NarratableEntry pEntry, int pIndex, NarratableEntry.NarrationPriority pPriority)
        {
            this.entry = pEntry;
            this.index = pIndex;
            this.priority = pPriority;
        }
    }
}
