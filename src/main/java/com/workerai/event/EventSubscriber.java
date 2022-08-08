package com.workerai.event;

import com.google.common.base.Preconditions;
import com.workerai.event.utils.Priority;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public final class EventSubscriber {
    private static int ID = 0;
    private static final AsmClassLoader LOADER = new AsmClassLoader();

    @NotNull private final Object instance;
    @NotNull private final Method method;
    @NotNull private final Priority priority;
    private final String objName;
    private final String methodName;

    private EventHandler handler;

    public EventSubscriber(@NotNull Object instance, @NotNull Method method, @NotNull Priority priority) {
        Preconditions.checkNotNull(instance, "instance cannot be null");
        Preconditions.checkNotNull(method, "method cannot be null");
        Preconditions.checkNotNull(priority, "priority cannot be null");

        this.instance = instance;
        this.method = method;
        this.priority = priority;
        objName = this.instance.getClass().getSimpleName().replace(".", "_");
        methodName = this.method.getName();

        try {
            handler = (EventHandler) createHandler(method).getConstructor(Object.class).newInstance(instance);
        } catch (Exception e) {
          //  WorkerAI.getLogger().error("Failed to register event handler {}", method);
            e.printStackTrace();
        }
    }

    public void invoke(Object event) {
        handler.handle(event);
    }

    public String getObjName() {
        return objName;
    }

    @NotNull
    public Object getInstance() {
        return instance;
    }

    @NotNull
    public Method getMethod() {
        return method;
    }

    @NotNull
    public Priority getPriority() {
        return priority;
    }

    @NotNull
    public EventSubscriber copy(@NotNull Object instance, @NotNull Method method, @NotNull Priority priority) {
        Preconditions.checkNotNull(instance, "instance");
        Preconditions.checkNotNull(method, "method");
        Preconditions.checkNotNull(priority, "priority");

        return new EventSubscriber(instance, method, priority);
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return "EventSubscriber(instance=" + instance + ", method=" + method + ", priority=" + priority + ")";
    }

    @Override
    public int hashCode() {
        return (instance.hashCode() * 31 + method.hashCode()) * 31 + priority.hashCode();
    }

    @Override
    public boolean equals(Object subscriberIn) {
        if (this != subscriberIn) {
            if (subscriberIn instanceof EventSubscriber) {
                EventSubscriber eventSubscriber = (EventSubscriber) subscriberIn;

                return instance.equals(eventSubscriber.instance) &&
                        method.equals(eventSubscriber.method) &&
                        priority.equals(eventSubscriber.priority);
            }
            return false;
        } else {
            return true;
        }
    }

    private Class<?> createHandler(Method callback) {
        String name = objName + "$" + callback.getName() + "_" + callback.getParameters()[0].getType().getSimpleName() + "_" + (ID++);
        String eventType = Type.getInternalName(callback.getParameterTypes()[0]);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        MethodVisitor mv;
        String desc = name.replace(".", "/");
        String instanceClassName = instance.getClass().getName().replace(".", "/");

        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, desc, null, "java/lang/Object", new String[]{ Type.getInternalName(EventSubscriber.EventHandler.class) });

        cw.visitSource(".dynamic", null);
        {
            cw.visitField(ACC_PUBLIC, "instance", "Ljava/lang/Object;", null, null).visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "handle", "(Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, instanceClassName);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, eventType);
            mv.visitMethodInsn(INVOKEVIRTUAL, instanceClassName, callback.getName(), Type.getMethodDescriptor(callback), false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] handlerClassBytes = cw.toByteArray();
        return LOADER.define(name, handlerClassBytes);
    }

    public interface EventHandler {
        void handle(Object event);
    }

    private static class AsmClassLoader extends ClassLoader {
        private AsmClassLoader()
        {
            super(AsmClassLoader.class.getClassLoader());
        }

        public Class<?> define(String name, byte[] data)
        {
            return defineClass(name, data, 0, data.length);
        }
    }
}
