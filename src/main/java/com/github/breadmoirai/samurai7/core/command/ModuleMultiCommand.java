/*
 *       Copyright 2017 Ton Ly (BreadMoirai)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.github.breadmoirai.samurai7.core.command;

import com.github.breadmoirai.samurai7.core.CommandEvent;
import com.github.breadmoirai.samurai7.core.IModule;
import org.apache.commons.lang3.reflect.TypeUtils;
import com.github.breadmoirai.samurai7.core.response.Response;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;

public abstract class ModuleMultiCommand<M extends IModule> extends ModuleCommand<M> {

    private static final HashMap<Class<? extends ModuleMultiCommand>, HashMap<String, Method>> METHOD_MAP = new HashMap<>();

    @Override
    public Response execute(CommandEvent event, M module) {
        final Method method = METHOD_MAP.get(this.getClass()).get(event.getKey().toLowerCase());
        try {
            return (Response) method.invoke(this, event, module);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isMarkedWith(Class<? extends Annotation> annotation) {
        return super.isMarkedWith(annotation) || METHOD_MAP.get(this.getClass()).get(getEvent().getKey().toLowerCase()).isAnnotationPresent(annotation);
    }

    public static String[] register(Class<? extends ModuleMultiCommand> commandClass) {
        final Type moduleType = TypeUtils.getTypeArguments(commandClass.getClass(), ModuleCommand.class).get(ModuleCommand.class.getTypeParameters()[0]);
        final HashMap<String, Method> map = new HashMap<>();
        METHOD_MAP.put(commandClass, map);
        return Arrays.stream(commandClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Key.class))
                .filter(method -> Response.class.isAssignableFrom(method.getReturnType()))
                .filter(method -> method.getParameterCount() == 2)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.getParameterTypes()[1] == moduleType)
                .flatMap(method -> Arrays.stream(method.getAnnotation(Key.class).value())
                        .peek(s -> map.put(s, method)))
                .toArray(String[]::new);
    }


}