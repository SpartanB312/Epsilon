package club.eridani.epsilon.client;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("ALL")
public class ForgeRegister {

    public static void forceRegister(Object target) {
        try {
            Field field1 = EventBus.class.getDeclaredField("listeners");
            field1.setAccessible(true);
            ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) field1.get(MinecraftForge.EVENT_BUS);


            Field field2 = EventBus.class.getDeclaredField("listenerOwners");
            field2.setAccessible(true);
            Map<Object, ModContainer> listenerOwners = (Map<Object, ModContainer>) field2.get(MinecraftForge.EVENT_BUS);

            Method register = EventBus.class.getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            register.setAccessible(true);

            if (listeners.containsKey(target)) {
                return;
            }
            ModContainer activeModContainer = Loader.instance().getMinecraftModContainer();

            listenerOwners.put(target, activeModContainer);
            boolean isStatic = target.getClass() == Class.class;
            @SuppressWarnings("unchecked")
            Set<? extends Class<?>> supers = isStatic ? Sets.newHashSet((Class<?>) target) : TypeToken.of(target.getClass()).getTypes().rawTypes();
            for (Method method : (isStatic ? (Class<?>) target : target.getClass()).getMethods()) {
                if (isStatic && !Modifier.isStatic(method.getModifiers()))
                    continue;
                else if (!isStatic && Modifier.isStatic(method.getModifiers()))
                    continue;

                for (Class<?> cls : supers) {
                    try {
                        Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        if (real.isAnnotationPresent(SubscribeEvent.class)) {
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            if (parameterTypes.length != 1) {
                                throw new IllegalArgumentException(
                                        "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                                                " arguments.  Event handler methods must require a single argument."
                                );
                            }

                            Class<?> eventType = parameterTypes[0];

                            if (!Event.class.isAssignableFrom(eventType)) {
                                throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
                            }
                            register.invoke(MinecraftForge.EVENT_BUS, eventType, target, real, activeModContainer);
                            break;
                        }
                    } catch (NoSuchMethodException e) {
                        ; // Eat the error, this is not unexpected
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
