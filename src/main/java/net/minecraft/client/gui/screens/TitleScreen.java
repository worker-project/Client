package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import com.workerai.utils.ClientUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TitleScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    private final boolean minceraftEasterEgg;
    @Nullable
    private String splash;
    private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
    private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
    private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
    private final boolean fading;
    private long fadeInStart;

    private Screen modUpdateNotification;

    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean pFading) {
        super(new TranslatableComponent("narrator.screen.title"));
        this.fading = pFading;
        this.minceraftEasterEgg = (double) (new Random()).nextFloat() < 1.0E-4D;
    }

    public static CompletableFuture<Void> preloadResources(TextureManager pTexMngr, Executor pBackgroundExecutor) {
        return CompletableFuture.allOf(pTexMngr.preload(MINECRAFT_LOGO, pBackgroundExecutor), pTexMngr.preload(MINECRAFT_EDITION, pBackgroundExecutor), pTexMngr.preload(PANORAMA_OVERLAY, pBackgroundExecutor), CUBE_MAP.preload(pTexMngr, pBackgroundExecutor));
    }

    public boolean isPauseScreen() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }
 
    protected void init() {

        if(!this.minecraft.options.disclaimerAccepted) {
            this.minecraft.setScreen(new DisclaimerScreen(this));
            return;
        }

        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int i = calendar.get(5);
            int j = calendar.get(2) + 1;

            if (i == 8 && j == 4) {
                this.splash = "Happy birthday, OptiFine!";
            }

            if (i == 14 && j == 8) {
                this.splash = "Happy birthday, sp614x!";
            }
        }

        int k = this.height / 4 + 48;
        Button button = null;


        this.createNormalMenuOptions(k);
        if (Reflector.ModListScreen_Constructor.exists()) {
            button = ReflectorForge.makeButtonMods(this, k, 24);
            this.addRenderableWidget(button);
        }


        this.addRenderableWidget(new Button(this.width / 2 - 100,  k + 24, 98, 20, new TranslatableComponent("menu.options"), (p_96787_1_) ->
        {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 2, k + 24, 98, 20, new TranslatableComponent("menu.quit"), (p_96785_1_) ->
        {
            this.minecraft.stop();
        }));

        if (Reflector.NotificationModUpdateScreen_init.exists()) {
            this.modUpdateNotification = (Screen) Reflector.call(Reflector.NotificationModUpdateScreen_init, this, button);
        }
    }

    private void createNormalMenuOptions(int pY) {
        boolean flag = this.minecraft.allowsMultiplayer();
        Button.OnTooltip button$ontooltip = flag ? Button.NO_TOOLTIP : new Button.OnTooltip() {
            private final Component text = new TranslatableComponent("title.multiplayer.disabled");

            public void onTooltip(Button p_169458_, PoseStack p_169459_, int p_169460_, int p_169461_) {
                if (!p_169458_.active) {
                    TitleScreen.this.renderTooltip(p_169459_, TitleScreen.this.minecraft.font.split(this.text, Math.max(TitleScreen.this.width / 2 - 43, 170)), p_169460_, p_169461_);
                }
            }

            public void narrateTooltip(Consumer<Component> p_169456_) {
                p_169456_.accept(this.text);
            }
        };
        (this.addRenderableWidget(new Button(this.width / 2 - 100, pY, 200, 20, new TranslatableComponent("menu.multiplayer"), (p_210871_1_) ->
        {
            Screen screen = new JoinMultiplayerScreen(this);
            this.minecraft.setScreen(screen);
        }, button$ontooltip))).active = flag;
        boolean flag1 = Reflector.ModListScreen_Constructor.exists();
    }


    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float f = this.fading ? (float) (Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
        GlStateManager._disableDepthTest();
        this.panorama.render(pPartialTick, Mth.clamp(f, 0.0F, 1.0F));
        int i = 274;
        int j = this.width / 2 - 137;
        int k = 30;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.fading ? (float) Mth.ceil(Mth.clamp(f, 0.0F, 1.0F)) : 1.0F);
        blit(pPoseStack, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        float f1 = this.fading ? Mth.clamp(f - 1.0F, 0.0F, 1.0F) : 1.0F;
        int l = Mth.ceil(f1 * 255.0F) << 24;

        if ((l & -67108864) != 0) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f1);

            if (this.minceraftEasterEgg) {
                this.blitOutlineBlack(j, 30, (p_210860_2_, p_210860_3_) ->
                {
                    this.blit(pPoseStack, p_210860_2_ + 0, p_210860_3_, 0, 0, 99, 44);
                    this.blit(pPoseStack, p_210860_2_ + 99, p_210860_3_, 129, 0, 27, 44);
                    this.blit(pPoseStack, p_210860_2_ + 99 + 26, p_210860_3_, 126, 0, 3, 44);
                    this.blit(pPoseStack, p_210860_2_ + 99 + 26 + 3, p_210860_3_, 99, 0, 26, 44);
                    this.blit(pPoseStack, p_210860_2_ + 155, p_210860_3_, 0, 45, 155, 44);
                });
            } else {
                this.blitOutlineBlack(j, 30, (p_211776_2_, p_211776_3_) ->
                {
                    this.blit(pPoseStack, p_211776_2_ + 0, p_211776_3_, 0, 0, 155, 44);
                    this.blit(pPoseStack, p_211776_2_ + 155, p_211776_3_, 0, 45, 155, 44);
                });
            }

            RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
            blit(pPoseStack, j + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);

            if (Reflector.ForgeHooksClient_renderMainMenu.exists()) {
                Reflector.callVoid(Reflector.ForgeHooksClient_renderMainMenu, this, pPoseStack, this.font, this.width, this.height, l);
            }

            if (this.splash != null) {
                pPoseStack.pushPose();
                pPoseStack.translate((double) (this.width / 2 + 90), 70.0D, 0.0D);
                pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
                float f2 = 1.8F - Mth.abs(Mth.sin((float) (Util.getMillis() % 1000L) / 1000.0F * ((float) Math.PI * 2F)) * 0.1F);
                f2 = f2 * 100.0F / (float) (this.font.width(this.splash) + 32);
                pPoseStack.scale(f2, f2, f2);
                drawCenteredString(pPoseStack, this.font, this.splash, 0, -8, 16776960 | l);
                pPoseStack.popPose();
            }

            if (Reflector.BrandingControl.exists()) {
                if (Reflector.BrandingControl_forEachLine.exists()) {
                    BiConsumer<Integer, String> biconsumer = (brdline, brd) ->
                    {
                        drawString(pPoseStack, this.font, brd, 2, this.height - (10 + brdline * (9 + 1)), 16777215 | l);
                    };
                    Reflector.call(Reflector.BrandingControl_forEachLine, true, true, biconsumer);
                }

                if (Reflector.BrandingControl_forEachAboveCopyrightLine.exists()) {
                    BiConsumer<Integer, String> biconsumer1 = (brdline, brd) ->
                    {
                        drawString(pPoseStack, this.font, brd, this.width - this.font.width(brd), this.height - (10 + (brdline + 1) * (9 + 1)), 16777215 | l);
                    };
                    Reflector.call(Reflector.BrandingControl_forEachAboveCopyrightLine, biconsumer1);
                }
            } else {
                drawString(pPoseStack, this.font, ClientUtils.NAME + " " + ClientUtils.VERSION, 2, this.height - 10, 16777215 | l);
                drawString(pPoseStack, this.font, ClientUtils.COPYRIGHT, this.width - this.font.width(ClientUtils.COPYRIGHT) - 2, this.height - 10, 16777215 | l);
            }

            for (GuiEventListener guieventlistener : this.children()) {
                if (guieventlistener instanceof AbstractWidget) {
                    ((AbstractWidget) guieventlistener).setAlpha(f1);
                }
            }

            super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }

        if (this.modUpdateNotification != null && f1 >= 1.0F) {
            this.modUpdateNotification.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    public boolean mouseClicked(double pMouseX, double p_96736_, int pMouseY) {
        return super.mouseClicked(pMouseX, p_96736_, pMouseY);
    }

}
