package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.optifine.Config;
import net.optifine.CustomGuis;
import net.optifine.shaders.Shaders;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.slf4j.Logger;

@DontObfuscate
public class RenderSystem
{
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
    private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator();
    private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    private static boolean isReplayingQueue;
    @Nullable
    private static Thread gameThread;
    @Nullable
    private static Thread renderThread;
    private static int MAX_SUPPORTED_TEXTURE_SIZE = -1;
    private static boolean isInInit;
    private static double lastDrawTime = Double.MIN_VALUE;
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequential = new RenderSystem.AutoStorageIndexBuffer(1, 1, IntConsumer::accept);
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialQuad = new RenderSystem.AutoStorageIndexBuffer(4, 6, (p_157398_, p_157399_) ->
    {
        p_157398_.accept(p_157399_ + 0);
        p_157398_.accept(p_157399_ + 1);
        p_157398_.accept(p_157399_ + 2);
        p_157398_.accept(p_157399_ + 2);
        p_157398_.accept(p_157399_ + 3);
        p_157398_.accept(p_157399_ + 0);
    });
    private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialLines = new RenderSystem.AutoStorageIndexBuffer(4, 6, (p_157401_, p_157402_) ->
    {
        p_157401_.accept(p_157402_ + 0);
        p_157401_.accept(p_157402_ + 1);
        p_157401_.accept(p_157402_ + 2);
        p_157401_.accept(p_157402_ + 3);
        p_157401_.accept(p_157402_ + 2);
        p_157401_.accept(p_157402_ + 1);
    });
    private static Matrix3f inverseViewRotationMatrix = new Matrix3f();
    private static Matrix4f projectionMatrix = new Matrix4f();
    private static Matrix4f savedProjectionMatrix = new Matrix4f();
    private static PoseStack modelViewStack = new PoseStack();
    private static Matrix4f modelViewMatrix = new Matrix4f();
    private static Matrix4f textureMatrix = new Matrix4f();
    private static final int[] shaderTextures = new int[12];
    private static final float[] shaderColor = new float[] {1.0F, 1.0F, 1.0F, 1.0F};
    private static float shaderFogStart;
    private static float shaderFogEnd = 1.0F;
    private static final float[] shaderFogColor = new float[] {0.0F, 0.0F, 0.0F, 0.0F};
    private static FogShape shaderFogShape = FogShape.SPHERE;
    private static final Vector3f[] shaderLightDirections = new Vector3f[2];
    private static float shaderGameTime;
    private static float shaderLineWidth = 1.0F;
    private static String apiDescription = "Unknown";
    @Nullable
    private static ShaderInstance shader;
    private static boolean fogAllowed = true;
    private static boolean colorToAttribute = false;

    public static void initRenderThread()
    {
        if (renderThread == null && gameThread != Thread.currentThread())
        {
            renderThread = Thread.currentThread();
        }
        else
        {
            throw new IllegalStateException("Could not initialize render thread");
        }
    }

    public static boolean isOnRenderThread()
    {
        return Thread.currentThread() == renderThread;
    }

    public static boolean isOnRenderThreadOrInit()
    {
        return isInInit || isOnRenderThread();
    }

    public static void initGameThread(boolean pRenderOffThread)
    {
        boolean flag = renderThread == Thread.currentThread();

        if (gameThread == null && renderThread != null && flag != pRenderOffThread)
        {
            gameThread = Thread.currentThread();
        }
        else
        {
            throw new IllegalStateException("Could not initialize tick thread");
        }
    }

    public static boolean isOnGameThread()
    {
        return true;
    }

    public static void assertInInitPhase()
    {
        if (!isInInitPhase())
        {
            throw constructThreadException();
        }
    }

    public static void assertOnGameThreadOrInit()
    {
        if (!isInInit && !isOnGameThread())
        {
            throw constructThreadException();
        }
    }

    public static void assertOnRenderThreadOrInit()
    {
        if (!isInInit && !isOnRenderThread())
        {
            throw constructThreadException();
        }
    }

    public static void assertOnRenderThread()
    {
        if (!isOnRenderThread())
        {
            throw constructThreadException();
        }
    }

    public static void assertOnGameThread()
    {
        if (!isOnGameThread())
        {
            throw constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException()
    {
        return new IllegalStateException("Rendersystem called from wrong thread");
    }

    public static boolean isInInitPhase()
    {
        return true;
    }

    public static void recordRenderCall(RenderCall pRenderCall)
    {
        recordingQueue.add(pRenderCall);
    }

    public static void flipFrame(long pWindowId)
    {
        GLFW.glfwPollEvents();
        replayQueue();
        Tesselator.getInstance().getBuilder().clear();
        GLFW.glfwSwapBuffers(pWindowId);
        GLFW.glfwPollEvents();
    }

    public static void replayQueue()
    {
        isReplayingQueue = true;

        while (!recordingQueue.isEmpty())
        {
            RenderCall rendercall = recordingQueue.poll();
            rendercall.execute();
        }

        isReplayingQueue = false;
    }

    public static void limitDisplayFPS(int pFrameRateLimit)
    {
        double d0 = lastDrawTime + 1.0D / (double)pFrameRateLimit;
        double d1;

        for (d1 = GLFW.glfwGetTime(); d1 < d0; d1 = GLFW.glfwGetTime())
        {
            GLFW.glfwWaitEventsTimeout(d0 - d1);
        }

        lastDrawTime = d1;
    }

    public static void disableDepthTest()
    {
        assertOnRenderThread();
        GlStateManager._disableDepthTest();
    }

    public static void enableDepthTest()
    {
        assertOnGameThreadOrInit();
        GlStateManager._enableDepthTest();
    }

    public static void enableScissor(int pX, int pY, int pWidth, int pHeight)
    {
        assertOnGameThreadOrInit();
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(pX, pY, pWidth, pHeight);
    }

    public static void disableScissor()
    {
        assertOnGameThreadOrInit();
        GlStateManager._disableScissorTest();
    }

    public static void depthFunc(int pDepthFunc)
    {
        assertOnRenderThread();
        GlStateManager._depthFunc(pDepthFunc);
    }

    public static void depthMask(boolean pFlag)
    {
        assertOnRenderThread();
        GlStateManager._depthMask(pFlag);
    }

    public static void enableBlend()
    {
        assertOnRenderThread();
        GlStateManager._enableBlend();
    }

    public static void disableBlend()
    {
        assertOnRenderThread();
        GlStateManager._disableBlend();
    }

    public static void blendFunc(GlStateManager.SourceFactor pSourceFactor, GlStateManager.DestFactor pDestFactor)
    {
        assertOnRenderThread();
        GlStateManager._blendFunc(pSourceFactor.value, pDestFactor.value);
    }

    public static void blendFunc(int pSourceFactor, int pDestFactor)
    {
        assertOnRenderThread();
        GlStateManager._blendFunc(pSourceFactor, pDestFactor);
    }

    public static void blendFuncSeparate(GlStateManager.SourceFactor pSourceFactor, GlStateManager.DestFactor pDestFactor, GlStateManager.SourceFactor pSourceFactorAlpha, GlStateManager.DestFactor pDestFactorAlpha)
    {
        assertOnRenderThread();
        GlStateManager._blendFuncSeparate(pSourceFactor.value, pDestFactor.value, pSourceFactorAlpha.value, pDestFactorAlpha.value);
    }

    public static void blendFuncSeparate(int pSourceFactor, int pDestFactor, int pSourceFactorAlpha, int pDestFactorAlpha)
    {
        assertOnRenderThread();
        GlStateManager._blendFuncSeparate(pSourceFactor, pDestFactor, pSourceFactorAlpha, pDestFactorAlpha);
    }

    public static void blendEquation(int pMode)
    {
        assertOnRenderThread();
        GlStateManager._blendEquation(pMode);
    }

    public static void enableCull()
    {
        assertOnRenderThread();
        GlStateManager._enableCull();
    }

    public static void disableCull()
    {
        assertOnRenderThread();
        GlStateManager._disableCull();
    }

    public static void polygonMode(int pFace, int pMode)
    {
        assertOnRenderThread();
        GlStateManager._polygonMode(pFace, pMode);
    }

    public static void enablePolygonOffset()
    {
        assertOnRenderThread();
        GlStateManager._enablePolygonOffset();
    }

    public static void disablePolygonOffset()
    {
        assertOnRenderThread();
        GlStateManager._disablePolygonOffset();
    }

    public static void polygonOffset(float pFactor, float pUnits)
    {
        assertOnRenderThread();
        GlStateManager._polygonOffset(pFactor, pUnits);
    }

    public static void enableColorLogicOp()
    {
        assertOnRenderThread();
        GlStateManager._enableColorLogicOp();
    }

    public static void disableColorLogicOp()
    {
        assertOnRenderThread();
        GlStateManager._disableColorLogicOp();
    }

    public static void logicOp(GlStateManager.LogicOp pOp)
    {
        assertOnRenderThread();
        GlStateManager._logicOp(pOp.value);
    }

    public static void activeTexture(int pTexture)
    {
        assertOnRenderThread();
        GlStateManager._activeTexture(pTexture);
    }

    public static void enableTexture()
    {
        assertOnRenderThread();
        GlStateManager._enableTexture();
    }

    public static void disableTexture()
    {
        assertOnRenderThread();
        GlStateManager._disableTexture();
    }

    public static void texParameter(int pTarget, int pParameterName, int pParameter)
    {
        GlStateManager._texParameter(pTarget, pParameterName, pParameter);
    }

    public static void deleteTexture(int pTexture)
    {
        assertOnGameThreadOrInit();
        GlStateManager._deleteTexture(pTexture);
    }

    public static void bindTextureForSetup(int pTexture)
    {
        bindTexture(pTexture);
    }

    public static void bindTexture(int pTexture)
    {
        GlStateManager._bindTexture(pTexture);
    }

    public static void viewport(int pX, int pY, int pWidth, int pHeight)
    {
        assertOnGameThreadOrInit();
        GlStateManager._viewport(pX, pY, pWidth, pHeight);
    }

    public static void colorMask(boolean pRed, boolean pGreen, boolean pBlue, boolean pAlpha)
    {
        assertOnRenderThread();
        GlStateManager._colorMask(pRed, pGreen, pBlue, pAlpha);
    }

    public static void stencilFunc(int pFunc, int pRef, int pMask)
    {
        assertOnRenderThread();
        GlStateManager._stencilFunc(pFunc, pRef, pMask);
    }

    public static void stencilMask(int pMask)
    {
        assertOnRenderThread();
        GlStateManager._stencilMask(pMask);
    }

    public static void stencilOp(int pSFail, int pDpFail, int pDpPass)
    {
        assertOnRenderThread();
        GlStateManager._stencilOp(pSFail, pDpFail, pDpPass);
    }

    public static void clearDepth(double pDepth)
    {
        assertOnGameThreadOrInit();
        GlStateManager._clearDepth(pDepth);
    }

    public static void clearColor(float pRed, float pGreen, float pBlue, float pAlpha)
    {
        assertOnGameThreadOrInit();
        GlStateManager._clearColor(pRed, pGreen, pBlue, pAlpha);
    }

    public static void clearStencil(int pIndex)
    {
        assertOnRenderThread();
        GlStateManager._clearStencil(pIndex);
    }

    public static void clear(int pMask, boolean pCheckError)
    {
        assertOnGameThreadOrInit();
        GlStateManager._clear(pMask, pCheckError);
    }

    public static void setShaderFogStart(float pShaderFogStart)
    {
        assertOnRenderThread();
        _setShaderFogStart(pShaderFogStart);
    }

    private static void _setShaderFogStart(float pShaderFogStart)
    {
        shaderFogStart = pShaderFogStart;
    }

    public static float getShaderFogStart()
    {
        if (!fogAllowed)
        {
            return Float.MAX_VALUE;
        }
        else
        {
            assertOnRenderThread();
            return shaderFogStart;
        }
    }

    public static void setShaderFogEnd(float pShaderFogEnd)
    {
        assertOnRenderThread();
        _setShaderFogEnd(pShaderFogEnd);
    }

    private static void _setShaderFogEnd(float pShaderFogEnd)
    {
        shaderFogEnd = pShaderFogEnd;
    }

    public static float getShaderFogEnd()
    {
        if (!fogAllowed)
        {
            return Float.MAX_VALUE;
        }
        else
        {
            assertOnRenderThread();
            return shaderFogEnd;
        }
    }

    public static void setShaderFogColor(float pRed, float pGreen, float pBlue, float pAlpha)
    {
        assertOnRenderThread();
        _setShaderFogColor(pRed, pGreen, pBlue, pAlpha);
    }

    public static void setShaderFogColor(float pRed, float pGreen, float pBlue)
    {
        setShaderFogColor(pRed, pGreen, pBlue, 1.0F);
    }

    private static void _setShaderFogColor(float pRed, float pGreen, float pBlue, float pAlpha)
    {
        shaderFogColor[0] = pRed;
        shaderFogColor[1] = pGreen;
        shaderFogColor[2] = pBlue;
        shaderFogColor[3] = pAlpha;
    }

    public static float[] getShaderFogColor()
    {
        assertOnRenderThread();
        return shaderFogColor;
    }

    public static void setShaderFogShape(FogShape p_202161_)
    {
        assertOnRenderThread();
        _setShaderFogShape(p_202161_);
    }

    private static void _setShaderFogShape(FogShape p_202040_)
    {
        shaderFogShape = p_202040_;
    }

    public static FogShape getShaderFogShape()
    {
        assertOnRenderThread();
        return shaderFogShape;
    }

    public static void setShaderLights(Vector3f pLightingVector0, Vector3f pLightingVector1)
    {
        assertOnRenderThread();
        _setShaderLights(pLightingVector0, pLightingVector1);
    }

    public static void _setShaderLights(Vector3f pLightingVector0, Vector3f pLightingVector1)
    {
        shaderLightDirections[0] = pLightingVector0;
        shaderLightDirections[1] = pLightingVector1;
    }

    public static void setupShaderLights(ShaderInstance pInstance)
    {
        assertOnRenderThread();

        if (pInstance.LIGHT0_DIRECTION != null)
        {
            pInstance.LIGHT0_DIRECTION.set(shaderLightDirections[0]);
        }

        if (pInstance.LIGHT1_DIRECTION != null)
        {
            pInstance.LIGHT1_DIRECTION.set(shaderLightDirections[1]);
        }
    }

    public static void setShaderColor(float pRed, float pGreen, float pBlue, float pAlpha)
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                _setShaderColor(pRed, pGreen, pBlue, pAlpha);
            });
        }
        else
        {
            _setShaderColor(pRed, pGreen, pBlue, pAlpha);
        }
    }

    private static void _setShaderColor(float pRed, float pGreen, float pBlue, float pAlpha)
    {
        shaderColor[0] = pRed;
        shaderColor[1] = pGreen;
        shaderColor[2] = pBlue;
        shaderColor[3] = pAlpha;

        if (colorToAttribute)
        {
            Shaders.setDefaultAttribColor(pRed, pGreen, pBlue, pAlpha);
        }
    }

    public static float[] getShaderColor()
    {
        assertOnRenderThread();
        return shaderColor;
    }

    public static void drawElements(int pMode, int pCount, int pType)
    {
        assertOnRenderThread();
        GlStateManager._drawElements(pMode, pCount, pType, 0L);
    }

    public static void lineWidth(float pShaderLineWidth)
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                shaderLineWidth = pShaderLineWidth;
            });
        }
        else
        {
            shaderLineWidth = pShaderLineWidth;
        }
    }

    public static float getShaderLineWidth()
    {
        assertOnRenderThread();
        return shaderLineWidth;
    }

    public static void pixelStore(int pParameterName, int pParameter)
    {
        assertOnGameThreadOrInit();
        GlStateManager._pixelStore(pParameterName, pParameter);
    }

    public static void readPixels(int pX, int pY, int pWidth, int pHeight, int pFormat, int pType, ByteBuffer pPixels)
    {
        assertOnRenderThread();
        GlStateManager._readPixels(pX, pY, pWidth, pHeight, pFormat, pType, pPixels);
    }

    public static void getString(int p_69520_, Consumer<String> p_69521_)
    {
        assertOnRenderThread();
        p_69521_.accept(GlStateManager._getString(p_69520_));
    }

    public static String getBackendDescription()
    {
        assertInInitPhase();
        return String.format("LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription()
    {
        return apiDescription;
    }

    public static LongSupplier initBackendSystem()
    {
        assertInInitPhase();
        return GLX._initGlfw();
    }

    public static void initRenderer(int pDebugVerbosity, boolean pSynchronous)
    {
        assertInInitPhase();
        GLX._init(pDebugVerbosity, pSynchronous);
        apiDescription = GLX.getOpenGLVersionString();
    }

    public static void setErrorCallback(GLFWErrorCallbackI pCallback)
    {
        assertInInitPhase();
        GLX._setGlfwErrorCallback(pCallback);
    }

    public static void renderCrosshair(int p_69882_)
    {
        assertOnRenderThread();
        GLX._renderCrosshair(p_69882_, true, true, true);
    }

    public static String getCapsString()
    {
        assertOnRenderThread();
        return "Using framebuffer using OpenGL 3.2";
    }

    public static void setupDefaultState(int pX, int pY, int pWidth, int pHeight)
    {
        assertInInitPhase();
        GlStateManager._enableTexture();
        GlStateManager._clearDepth(1.0D);
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(515);
        projectionMatrix.setIdentity();
        savedProjectionMatrix.setIdentity();
        modelViewMatrix.setIdentity();
        textureMatrix.setIdentity();
        GlStateManager._viewport(pX, pY, pWidth, pHeight);
    }

    public static int maxSupportedTextureSize()
    {
        if (MAX_SUPPORTED_TEXTURE_SIZE == -1)
        {
            assertOnRenderThreadOrInit();
            int i = GlStateManager._getInteger(3379);

            for (int j = Math.max(32768, i); j >= 1024; j >>= 1)
            {
                GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, (IntBuffer)null);
                int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);

                if (k != 0)
                {
                    MAX_SUPPORTED_TEXTURE_SIZE = j;
                    return j;
                }
            }

            MAX_SUPPORTED_TEXTURE_SIZE = Math.max(i, 1024);
            LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (int)MAX_SUPPORTED_TEXTURE_SIZE);
        }

        return MAX_SUPPORTED_TEXTURE_SIZE;
    }

    public static void glBindBuffer(int pTarget, IntSupplier pBuffer)
    {
        GlStateManager._glBindBuffer(pTarget, pBuffer.getAsInt());
    }

    public static void glBindVertexArray(Supplier<Integer> pArray)
    {
        GlStateManager._glBindVertexArray(pArray.get());
    }

    public static void glBufferData(int pTarget, ByteBuffer pData, int pUsage)
    {
        assertOnRenderThreadOrInit();
        GlStateManager._glBufferData(pTarget, pData, pUsage);
    }

    public static void glDeleteBuffers(int pBuffer)
    {
        assertOnRenderThread();
        GlStateManager._glDeleteBuffers(pBuffer);
    }

    public static void glDeleteVertexArrays(int pArray)
    {
        assertOnRenderThread();
        GlStateManager._glDeleteVertexArrays(pArray);
    }

    public static void glUniform1i(int pLocation, int pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform1i(pLocation, pValue);
    }

    public static void glUniform1(int pLocation, IntBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform1(pLocation, pValue);
    }

    public static void glUniform2(int pLocation, IntBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform2(pLocation, pValue);
    }

    public static void glUniform3(int pLocation, IntBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform3(pLocation, pValue);
    }

    public static void glUniform4(int pLoctaion, IntBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform4(pLoctaion, pValue);
    }

    public static void glUniform1(int pLocation, FloatBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform1(pLocation, pValue);
    }

    public static void glUniform2(int pLocation, FloatBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform2(pLocation, pValue);
    }

    public static void glUniform3(int pLocation, FloatBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform3(pLocation, pValue);
    }

    public static void glUniform4(int pLoctaion, FloatBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniform4(pLoctaion, pValue);
    }

    public static void glUniformMatrix2(int pLocation, boolean pTranspose, FloatBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniformMatrix2(pLocation, pTranspose, pValue);
    }

    public static void glUniformMatrix3(int pLocation, boolean pTranspose, FloatBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniformMatrix3(pLocation, pTranspose, pValue);
    }

    public static void glUniformMatrix4(int pLocation, boolean pTranspose, FloatBuffer pValue)
    {
        assertOnRenderThread();
        GlStateManager._glUniformMatrix4(pLocation, pTranspose, pValue);
    }

    public static void setupOverlayColor(IntSupplier pTextureId, int pShaderTexture)
    {
        assertOnRenderThread();
        int i = pTextureId.getAsInt();
        setShaderTexture(1, i);
    }

    public static void teardownOverlayColor()
    {
        assertOnRenderThread();
        setShaderTexture(1, 0);
    }

    public static void setupLevelDiffuseLighting(Vector3f pLightingVector0, Vector3f pLightingVector1, Matrix4f pMatrix)
    {
        assertOnRenderThread();
        GlStateManager.setupLevelDiffuseLighting(pLightingVector0, pLightingVector1, pMatrix);
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f pLightingVector0, Vector3f pLightingVector1)
    {
        assertOnRenderThread();
        GlStateManager.setupGuiFlatDiffuseLighting(pLightingVector0, pLightingVector1);
    }

    public static void setupGui3DDiffuseLighting(Vector3f pLightingVector0, Vector3f pLightingVector1)
    {
        assertOnRenderThread();
        GlStateManager.setupGui3DDiffuseLighting(pLightingVector0, pLightingVector1);
    }

    public static void beginInitialization()
    {
        isInInit = true;
    }

    public static void finishInitialization()
    {
        isInInit = false;

        if (!recordingQueue.isEmpty())
        {
            replayQueue();
        }

        if (!recordingQueue.isEmpty())
        {
            throw new IllegalStateException("Recorded to render queue during initialization");
        }
    }

    public static void glGenBuffers(Consumer<Integer> pBufferIdConsumer)
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                pBufferIdConsumer.accept(GlStateManager._glGenBuffers());
            });
        }
        else
        {
            pBufferIdConsumer.accept(GlStateManager._glGenBuffers());
        }
    }

    public static void glGenVertexArrays(Consumer<Integer> pArrayObjectIdConsumer)
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                pArrayObjectIdConsumer.accept(GlStateManager._glGenVertexArrays());
            });
        }
        else
        {
            pArrayObjectIdConsumer.accept(GlStateManager._glGenVertexArrays());
        }
    }

    public static Tesselator renderThreadTesselator()
    {
        assertOnRenderThread();
        return RENDER_THREAD_TESSELATOR;
    }

    public static void defaultBlendFunc()
    {
        blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    @Deprecated
    public static void runAsFancy(Runnable pFancyRunnable)
    {
        boolean flag = Minecraft.useShaderTransparency();

        if (!flag)
        {
            pFancyRunnable.run();
        }
        else
        {
            Options options = Minecraft.getInstance().options;
            GraphicsStatus graphicsstatus = options.graphicsMode;
            options.graphicsMode = GraphicsStatus.FANCY;
            pFancyRunnable.run();
            options.graphicsMode = graphicsstatus;
        }
    }

    public static void setShader(Supplier<ShaderInstance> pShaderSupplier)
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                shader = pShaderSupplier.get();
            });
        }
        else
        {
            shader = pShaderSupplier.get();
        }
    }

    @Nullable
    public static ShaderInstance getShader()
    {
        assertOnRenderThread();
        return shader;
    }

    public static int getTextureId(int pTextureId)
    {
        return GlStateManager._getTextureId(pTextureId);
    }

    public static void setShaderTexture(int pShaderTexture, ResourceLocation pTextureId)
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                _setShaderTexture(pShaderTexture, pTextureId);
            });
        }
        else
        {
            _setShaderTexture(pShaderTexture, pTextureId);
        }
    }

    public static void _setShaderTexture(int pShaderTexture, ResourceLocation pTextureId)
    {
        if (Config.isCustomGuis())
        {
            pTextureId = CustomGuis.getTextureLocation(pTextureId);
        }

        if (pShaderTexture >= 0 && pShaderTexture < shaderTextures.length)
        {
            TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
            AbstractTexture abstracttexture = texturemanager.getTexture(pTextureId);
            shaderTextures[pShaderTexture] = abstracttexture.getId();
        }
    }

    public static void setShaderTexture(int pShaderTexture, int pTextureId)
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                _setShaderTexture(pShaderTexture, pTextureId);
            });
        }
        else
        {
            _setShaderTexture(pShaderTexture, pTextureId);
        }
    }

    public static void _setShaderTexture(int pShaderTexture, int pTextureId)
    {
        if (pShaderTexture >= 0 && pShaderTexture < shaderTextures.length)
        {
            shaderTextures[pShaderTexture] = pTextureId;
        }
    }

    public static int getShaderTexture(int pShaderTexture)
    {
        assertOnRenderThread();
        return pShaderTexture >= 0 && pShaderTexture < shaderTextures.length ? shaderTextures[pShaderTexture] : 0;
    }

    public static void setProjectionMatrix(Matrix4f pProjectionMatrix)
    {
        Matrix4f matrix4f = pProjectionMatrix.copy();

        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                projectionMatrix = matrix4f;
            });
        }
        else
        {
            projectionMatrix = matrix4f;
        }
    }

    public static void setInverseViewRotationMatrix(Matrix3f p_200919_)
    {
        Matrix3f matrix3f = p_200919_.copy();

        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                inverseViewRotationMatrix = matrix3f;
            });
        }
        else
        {
            inverseViewRotationMatrix = matrix3f;
        }
    }

    public static void setTextureMatrix(Matrix4f pTextureMatrix)
    {
        Matrix4f matrix4f = pTextureMatrix.copy();

        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                textureMatrix = matrix4f;
            });
        }
        else
        {
            textureMatrix = matrix4f;
        }
    }

    public static void resetTextureMatrix()
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                textureMatrix.setIdentity();
            });
        }
        else
        {
            textureMatrix.setIdentity();
        }
    }

    public static void applyModelViewMatrix()
    {
        Matrix4f matrix4f = modelViewStack.last().pose().copy();

        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                modelViewMatrix = matrix4f;
            });
        }
        else
        {
            modelViewMatrix = matrix4f;
        }
    }

    public static void backupProjectionMatrix()
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                _backupProjectionMatrix();
            });
        }
        else
        {
            _backupProjectionMatrix();
        }
    }

    private static void _backupProjectionMatrix()
    {
        savedProjectionMatrix = projectionMatrix;
    }

    public static void restoreProjectionMatrix()
    {
        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                _restoreProjectionMatrix();
            });
        }
        else
        {
            _restoreProjectionMatrix();
        }
    }

    private static void _restoreProjectionMatrix()
    {
        projectionMatrix = savedProjectionMatrix;
    }

    public static Matrix4f getProjectionMatrix()
    {
        assertOnRenderThread();
        return projectionMatrix;
    }

    public static Matrix3f getInverseViewRotationMatrix()
    {
        assertOnRenderThread();
        return inverseViewRotationMatrix;
    }

    public static Matrix4f getModelViewMatrix()
    {
        assertOnRenderThread();
        return modelViewMatrix;
    }

    public static PoseStack getModelViewStack()
    {
        return modelViewStack;
    }

    public static Matrix4f getTextureMatrix()
    {
        assertOnRenderThread();
        return textureMatrix;
    }

    public static RenderSystem.AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode pFormatMode, int pNeededIndexCount)
    {
        assertOnRenderThread();
        RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer;

        if (pFormatMode == VertexFormat.Mode.QUADS)
        {
            rendersystem$autostorageindexbuffer = sharedSequentialQuad;

            if (Config.isRenderRegions())
            {
                rendersystem$autostorageindexbuffer.ensureStorage(65536);
            }
        }
        else if (pFormatMode == VertexFormat.Mode.LINES)
        {
            rendersystem$autostorageindexbuffer = sharedSequentialLines;
        }
        else
        {
            rendersystem$autostorageindexbuffer = sharedSequential;
        }

        rendersystem$autostorageindexbuffer.ensureStorage(pNeededIndexCount);
        return rendersystem$autostorageindexbuffer;
    }

    public static void setShaderGameTime(long pTickTime, float p_157449_)
    {
        float f = ((float)(pTickTime % 24000L) + p_157449_) / 24000.0F;

        if (!isOnRenderThread())
        {
            recordRenderCall(() ->
            {
                shaderGameTime = f;
            });
        }
        else
        {
            shaderGameTime = f;
        }
    }

    public static float getShaderGameTime()
    {
        assertOnRenderThread();
        return shaderGameTime;
    }

    public static void setFogAllowed(boolean fogAllowed)
    {
        RenderSystem.fogAllowed = fogAllowed;

        if (Config.isShaders())
        {
            Shaders.setFogAllowed(fogAllowed);
        }
    }

    public static boolean isFogAllowed()
    {
        return fogAllowed;
    }

    public static void setColorToAttribute(boolean colorToAttribute)
    {
        if (Config.isShaders())
        {
            if (RenderSystem.colorToAttribute != colorToAttribute)
            {
                RenderSystem.colorToAttribute = colorToAttribute;

                if (colorToAttribute)
                {
                    Shaders.setDefaultAttribColor(shaderColor[0], shaderColor[1], shaderColor[2], shaderColor[3]);
                }
                else
                {
                    Shaders.setDefaultAttribColor();
                }
            }
        }
    }

    static
    {
        projectionMatrix.setIdentity();
        savedProjectionMatrix.setIdentity();
        modelViewMatrix.setIdentity();
        textureMatrix.setIdentity();
    }

    public static final class AutoStorageIndexBuffer
    {
        private final int vertexStride;
        private final int indexStride;
        private final RenderSystem.AutoStorageIndexBuffer.IndexGenerator generator;
        private int name;
        private VertexFormat.IndexType type = VertexFormat.IndexType.BYTE;
        private int indexCount;

        AutoStorageIndexBuffer(int pVertexStride, int pIndexStride, RenderSystem.AutoStorageIndexBuffer.IndexGenerator pGenerator)
        {
            this.vertexStride = pVertexStride;
            this.indexStride = pIndexStride;
            this.generator = pGenerator;
        }

        void ensureStorage(int pNeededIndexCount)
        {
            if (pNeededIndexCount > this.indexCount)
            {
                pNeededIndexCount = Mth.roundToward(pNeededIndexCount * 2, this.indexStride);
                RenderSystem.LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", this.indexCount, pNeededIndexCount);

                if (this.name == 0)
                {
                    this.name = GlStateManager._glGenBuffers();
                }

                VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(pNeededIndexCount);
                int i = Mth.roundToward(pNeededIndexCount * vertexformat$indextype.bytes, 4);
                GlStateManager._glBindBuffer(34963, this.name);
                GlStateManager._glBufferData(34963, (long)i, 35048);
                ByteBuffer bytebuffer = GlStateManager._glMapBuffer(34963, 35001);

                if (bytebuffer == null)
                {
                    throw new RuntimeException("Failed to map GL buffer");
                }

                this.type = vertexformat$indextype;
                it.unimi.dsi.fastutil.ints.IntConsumer intconsumer = this.intConsumer(bytebuffer);

                for (int j = 0; j < pNeededIndexCount; j += this.indexStride)
                {
                    this.generator.accept(intconsumer, j * this.vertexStride / this.indexStride);
                }

                GlStateManager._glUnmapBuffer(34963);
                GlStateManager._glBindBuffer(34963, 0);
                this.indexCount = pNeededIndexCount;
                BufferUploader.invalidateElementArrayBufferBinding();
            }
        }

        private it.unimi.dsi.fastutil.ints.IntConsumer intConsumer(ByteBuffer pBuffer)
        {
            switch (this.type)
            {
                case BYTE:
                    return (valueIn) ->
                    {
                        pBuffer.put((byte)valueIn);
                    };
                case SHORT:
                    return (valueIn) ->
                    {
                        pBuffer.putShort((short)valueIn);
                    };
                case INT:
                default:
                    return pBuffer::putInt;
            }
        }

        public int name()
        {
            return this.name;
        }

        public VertexFormat.IndexType type()
        {
            return this.type;
        }

        interface IndexGenerator
        {
            void accept(it.unimi.dsi.fastutil.ints.IntConsumer p_157488_, int p_157489_);
        }
    }
}
