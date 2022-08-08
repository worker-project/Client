package com.workerai.event;

import com.google.common.reflect.TypeToken;
import com.workerai.event.utils.InvokeEvent;
import com.workerai.event.utils.Priority;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("UnstableApiUsage")
public class EventBus {
    public static final EventBus INSTANCE = new EventBus();
    public static boolean ALLOW_PROFILE;
    private final HashMap<Class<?>, CopyOnWriteArrayList<EventSubscriber>> subscriptions = new HashMap<>();

    public void register(Object obj) {
        TypeToken<?> token = TypeToken.of(obj.getClass());

        Set superClasses = token.getTypes().rawTypes();

        for (Object temp : superClasses) {
            Class<?> clazz = (Class<?>) temp;
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getAnnotation(InvokeEvent.class) == null) {
                    continue;
                }

                if (method.getParameters()[0] == null) {
                    throw new IllegalArgumentException("Couldn't find parameter inside of " + method.getName() + "!");
                }

                Class<?> event = method.getParameters()[0].getType();
                Priority priority = method.getAnnotation(InvokeEvent.class).priority();
                method.setAccessible(true);

                if (subscriptions.containsKey(event)) {
                    subscriptions.get(event).add(new EventSubscriber(obj, method, priority));
                    subscriptions.get(event).sort(Comparator.comparingInt(a -> a.getPriority().getValue()));
                } else {
                    subscriptions.put(event, new CopyOnWriteArrayList<>());
                    subscriptions.get(event).add(new EventSubscriber(obj, method, priority));
                    subscriptions.get(event).sort(Comparator.comparingInt(a -> a.getPriority().getValue()));
                }
            }
        }
    }

    public void unregister(Object obj) {
        subscriptions.values().forEach(map -> map.removeIf(it -> it.getInstance() == obj));
    }

    public void unregister(Class<?> clazz) {
        subscriptions.values().forEach(map -> map.removeIf(it -> it.getInstance().getClass() == clazz));
    }

    public void post(Event event) {
        if (event == null) {
            return;
        }
        subscriptions.getOrDefault(event.getClass(), new CopyOnWriteArrayList<>()).forEach((sub) -> {
            try {
                sub.invoke(event);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }
}
