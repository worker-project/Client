package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.renderer.GameRenderer;
import net.optifine.Config;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

@DontObfuscate
public class GLX
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static String cpuInfo;

    public static String getOpenGLVersionString()
    {
        RenderSystem.assertOnRenderThread();
        return GLFW.glfwGetCurrentContext() == 0L ? "NO CONTEXT" : GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
    }

    public static int _getRefreshRate(Window pWindow)
    {
        RenderSystem.assertOnRenderThread();
        long i = GLFW.glfwGetWindowMonitor(pWindow.getWindow());

        if (i == 0L)
        {
            i = GLFW.glfwGetPrimaryMonitor();
        }

        GLFWVidMode glfwvidmode = i == 0L ? null : GLFW.glfwGetVideoMode(i);
        return glfwvidmode == null ? 0 : glfwvidmode.refreshRate();
    }

    public static String _getLWJGLVersion()
    {
        RenderSystem.assertInInitPhase();
        return Version.getVersion();
    }

    public static LongSupplier _initGlfw()
    {
        RenderSystem.assertInInitPhase();
        Window.checkGlfwError((p_69361_, p_69362_) ->
        {
            throw new IllegalStateException(String.format("GLFW error before init: [0x%X]%s", p_69361_, p_69362_));
        });
        List<String> list = Lists.newArrayList();
        GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback((p_69365_, p_69366_) ->
        {
            list.add(String.format("GLFW error during init: [0x%X]%s", p_69365_, p_69366_));
        });

        if (!GLFW.glfwInit())
        {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
        }
        else
        {
            LongSupplier longsupplier = () ->
            {
                return (long)(GLFW.glfwGetTime() * 1.0E9D);
            };

            for (String s : list)
            {
                LOGGER.error("GLFW error collected during initialization: {}", (Object)s);
            }

            RenderSystem.setErrorCallback(glfwerrorcallback);
            return longsupplier;
        }
    }

    public static void _setGlfwErrorCallback(GLFWErrorCallbackI pErrorCallback)
    {
        RenderSystem.assertInInitPhase();
        GLFWErrorCallback glfwerrorcallback = GLFW.glfwSetErrorCallback(pErrorCallback);

        if (glfwerrorcallback != null)
        {
            glfwerrorcallback.free();
        }
    }

    public static boolean _shouldClose(Window pWindow)
    {
        return GLFW.glfwWindowShouldClose(pWindow.getWindow());
    }

    public static void _init(int pDebugVerbosity, boolean pSynchronous)
    {
        RenderSystem.assertInInitPhase();
        GLCapabilities glcapabilities = GL.getCapabilities();
        GlStateManager.init(glcapabilities);

        try
        {
            CentralProcessor centralprocessor = (new SystemInfo()).getHardware().getProcessor();
            cpuInfo = String.format("%dx %s", centralprocessor.getLogicalProcessorCount(), centralprocessor.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
        }
        catch (Throwable throwable)
        {
        }

        GlDebug.enableDebugCallback(pDebugVerbosity, pSynchronous);
    }

    public static String _getCpuInfo()
    {
        return cpuInfo == null ? "<unknown>" : cpuInfo;
    }

    public static void _renderCrosshair(int p_69348_, boolean p_69349_, boolean p_69350_, boolean p_69351_)
    {
        RenderSystem.assertOnRenderThread();
        GlStateManager._disableTexture();
        GlStateManager._depthMask(false);
        GlStateManager._disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.lineWidth(4.0F);
        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        if (p_69349_)
        {
            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.vertex((double)p_69348_, 0.0D, 0.0D).color(0, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
        }

        if (p_69350_)
        {
            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(0.0D, (double)p_69348_, 0.0D).color(0, 0, 0, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        if (p_69351_)
        {
            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).color(0, 0, 0, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, (double)p_69348_).color(0, 0, 0, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
        }

        tesselator.end();
        RenderSystem.lineWidth(2.0F);
        bufferbuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        if (p_69349_)
        {
            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).color(255, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
            bufferbuilder.vertex((double)p_69348_, 0.0D, 0.0D).color(255, 0, 0, 255).normal(1.0F, 0.0F, 0.0F).endVertex();
        }

        if (p_69350_)
        {
            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).color(0, 255, 0, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(0.0D, (double)p_69348_, 0.0D).color(0, 255, 0, 255).normal(0.0F, 1.0F, 0.0F).endVertex();
        }

        if (p_69351_)
        {
            bufferbuilder.vertex(0.0D, 0.0D, 0.0D).color(127, 127, 255, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, (double)p_69348_).color(127, 127, 255, 255).normal(0.0F, 0.0F, 1.0F).endVertex();
        }

        tesselator.end();
        RenderSystem.lineWidth(1.0F);
        GlStateManager._enableCull();
        GlStateManager._depthMask(true);
        GlStateManager._enableTexture();
    }

    public static <T> T make(Supplier<T> pSupplier)
    {
        return pSupplier.get();
    }

    public static <T> T make(T pValue, Consumer<T> pConsumer)
    {
        pConsumer.accept(pValue);
        return pValue;
    }

    public static boolean isUsingFBOs()
    {
        return !Config.isAntialiasing();
    }

    public static boolean useVbo()
    {
        return true;
    }
}
