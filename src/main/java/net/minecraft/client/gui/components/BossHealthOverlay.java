package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import net.optifine.Config;
import net.optifine.CustomColors;
import net.optifine.reflect.Reflector;

public class BossHealthOverlay extends GuiComponent
{
    private static final ResourceLocation GUI_BARS_LOCATION = new ResourceLocation("textures/gui/bars.png");
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int OVERLAY_OFFSET = 80;
    private final Minecraft minecraft;
    final Map<UUID, LerpingBossEvent> events = Maps.newLinkedHashMap();

    public BossHealthOverlay(Minecraft pMinecraft)
    {
        this.minecraft = pMinecraft;
    }

    public void render(PoseStack pPoseStack)
    {
        if (!this.events.isEmpty())
        {
            int i = this.minecraft.getWindow().getGuiScaledWidth();
            int j = 12;

            for (LerpingBossEvent lerpingbossevent : this.events.values())
            {
                int k = i / 2 - 91;
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                boolean flag = true;
                int l = 19;

                if (Reflector.ForgeHooksClient_renderBossEventPre.exists())
                {
                    Object object = Reflector.ForgeHooksClient_renderBossEventPre.call(pPoseStack, this.minecraft.getWindow(), lerpingbossevent, k, j, 10 + 9);
                    flag = !Reflector.callBoolean(object, Reflector.Event_isCanceled);
                    l = Reflector.callInt(object, Reflector.RenderGameOverlayEvent_BossInfo_getIncrement);
                }

                if (flag)
                {
                    RenderSystem.setShaderTexture(0, GUI_BARS_LOCATION);
                    this.drawBar(pPoseStack, k, j, lerpingbossevent);
                    Component component = lerpingbossevent.getName();
                    int i1 = this.minecraft.font.width(component);
                    int j1 = i / 2 - i1 / 2;
                    int k1 = j - 9;
                    int l1 = 16777215;

                    if (Config.isCustomColors())
                    {
                        l1 = CustomColors.getBossTextColor(l1);
                    }

                    this.minecraft.font.drawShadow(pPoseStack, component, (float)j1, (float)k1, l1);
                }

                j += l;
                Reflector.ForgeHooksClient_renderBossEventPost.callVoid(pPoseStack, this.minecraft.getWindow());

                if (j >= this.minecraft.getWindow().getGuiScaledHeight() / 3)
                {
                    break;
                }
            }
        }
    }

    private void drawBar(PoseStack pPoseStack, int pX, int pY, BossEvent pBossEvent)
    {
        this.blit(pPoseStack, pX, pY, 0, pBossEvent.getColor().ordinal() * 5 * 2, 182, 5);

        if (pBossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS)
        {
            this.blit(pPoseStack, pX, pY, 0, 80 + (pBossEvent.getOverlay().ordinal() - 1) * 5 * 2, 182, 5);
        }

        int i = (int)(pBossEvent.getProgress() * 183.0F);

        if (i > 0)
        {
            this.blit(pPoseStack, pX, pY, 0, pBossEvent.getColor().ordinal() * 5 * 2 + 5, i, 5);

            if (pBossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS)
            {
                this.blit(pPoseStack, pX, pY, 0, 80 + (pBossEvent.getOverlay().ordinal() - 1) * 5 * 2 + 5, i, 5);
            }
        }
    }

    public void update(ClientboundBossEventPacket pPacket)
    {
        pPacket.dispatch(new ClientboundBossEventPacket.Handler()
        {
            public void add(UUID p_168824_, Component p_168825_, float p_168826_, BossEvent.BossBarColor p_168827_, BossEvent.BossBarOverlay p_168828_, boolean p_168829_, boolean p_168830_, boolean p_168831_)
            {
                BossHealthOverlay.this.events.put(p_168824_, new LerpingBossEvent(p_168824_, p_168825_, p_168826_, p_168827_, p_168828_, p_168829_, p_168830_, p_168831_));
            }
            public void remove(UUID p_168812_)
            {
                BossHealthOverlay.this.events.remove(p_168812_);
            }
            public void updateProgress(UUID p_168814_, float p_168815_)
            {
                BossHealthOverlay.this.events.get(p_168814_).setProgress(p_168815_);
            }
            public void updateName(UUID p_168821_, Component p_168822_)
            {
                BossHealthOverlay.this.events.get(p_168821_).setName(p_168822_);
            }
            public void updateStyle(UUID p_168817_, BossEvent.BossBarColor p_168818_, BossEvent.BossBarOverlay p_168819_)
            {
                LerpingBossEvent lerpingbossevent = BossHealthOverlay.this.events.get(p_168817_);
                lerpingbossevent.setColor(p_168818_);
                lerpingbossevent.setOverlay(p_168819_);
            }
            public void updateProperties(UUID p_168833_, boolean p_168834_, boolean p_168835_, boolean p_168836_)
            {
                LerpingBossEvent lerpingbossevent = BossHealthOverlay.this.events.get(p_168833_);
                lerpingbossevent.setDarkenScreen(p_168834_);
                lerpingbossevent.setPlayBossMusic(p_168835_);
                lerpingbossevent.setCreateWorldFog(p_168836_);
            }
        });
    }

    public void reset()
    {
        this.events.clear();
    }

    public boolean shouldPlayMusic()
    {
        if (!this.events.isEmpty())
        {
            for (BossEvent bossevent : this.events.values())
            {
                if (bossevent.shouldPlayBossMusic())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean shouldDarkenScreen()
    {
        if (!this.events.isEmpty())
        {
            for (BossEvent bossevent : this.events.values())
            {
                if (bossevent.shouldDarkenScreen())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean shouldCreateWorldFog()
    {
        if (!this.events.isEmpty())
        {
            for (BossEvent bossevent : this.events.values())
            {
                if (bossevent.shouldCreateWorldFog())
                {
                    return true;
                }
            }
        }

        return false;
    }

    public String getBossName()
    {
        if (!this.events.isEmpty())
        {
            for (BossEvent bossevent : this.events.values())
            {
                Component component = bossevent.getName();

                if (component instanceof TranslatableComponent)
                {
                    TranslatableComponent translatablecomponent = (TranslatableComponent)component;
                    return translatablecomponent.getKey();
                }
            }
        }

        return null;
    }
}
