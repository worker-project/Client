package com.mojang.blaze3d;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

public class Blaze3D
{
    public static void process(RenderPipeline pPipeline, float pUnknown)
    {
        ConcurrentLinkedQueue<RenderCall> concurrentlinkedqueue = pPipeline.getRecordingQueue();
    }

    public static void render(RenderPipeline pPipeline, float pUnknown)
    {
        ConcurrentLinkedQueue<RenderCall> concurrentlinkedqueue = pPipeline.getProcessedQueue();
    }

    public static void youJustLostTheGame()
    {
        MemoryUtil.memSet(0L, 0, 1L);
    }

    public static double getTime()
    {
        return GLFW.glfwGetTime();
    }
}
