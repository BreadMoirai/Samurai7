package com.github.breadmoirai.bot.framework.core.command;

import com.github.breadmoirai.bot.framework.core.command.Key;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

public class CommandMethod {

    private final Class<?> commandClass;
    private final Method method;
    private MethodHandle handle;

    public CommandMethod(Class<?> commandClass, Method method) throws IllegalAccessException {
        this.commandClass = commandClass;
        this.method = method;
        handle = MethodHandles.publicLookup().unreflect(method);
    }

    public MethodHandle getHandle() {
        return handle;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return method.isAnnotationPresent(annotation);
    }

    public Stream<String> getKeys() {
        final Key classKey = commandClass.getAnnotation(Key.class);
        final Key methodKey = method.getAnnotation(Key.class);
        if (classKey != null && methodKey != null) {
            return Arrays.stream(classKey.value())
                    .flatMap(s -> Arrays.stream(methodKey.value())
                            .map(ss -> s + " " + ss)
                            .map(String::toLowerCase));
        } else if (classKey != null) {
            return Arrays.stream(classKey.value()).map(String::toLowerCase);
        } else if (methodKey != null) {
            return Arrays.stream(methodKey.value()).map(String::toLowerCase);
        } else {
            return Stream.empty();
        }
    }
}
