/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.github.breadmoirai.bot.framework.command.buildernew;

import com.github.breadmoirai.bot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.bot.framework.command.Command;
import com.github.breadmoirai.bot.framework.command.parameter.CommandParameterFunctionImpl;
import com.github.breadmoirai.bot.framework.command.property.CommandPackageProperties;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMapImpl;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import net.dv8tion.jda.core.utils.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandHandleBuilderFactory {

    private final BreadBotClientBuilder clientBuilder;
    private final Map<Class<?>, CommandPropertyMapImpl> propertyMaps = new HashMap<>();

    public CommandHandleBuilderFactory(BreadBotClientBuilder clientBuilder) {
        this.clientBuilder = clientBuilder;
    }

    public CommandHandleBuilder fromConsumer(Consumer<CommandEvent> onCommand) {
        return new CommandHandleBuilderImpl(onCommand,
                clientBuilder,
                o -> null,
                new CommandParameterBuilder[]{new CommandParameterBuilderSpecificImpl("This parameter of type CommandEvent is inconfigurable", () -> new CommandParameterFunctionImpl((commandArguments, commandParser) -> commandParser.getEvent()))},
                (o, objects) -> onCommand.accept(((CommandEvent) objects[0])));
    }

    public <T> CommandHandleBuilder fromClass(Class<T> commandClass, @Nullable T object) throws NoSuchMethodException, IllegalAccessException {
        final Class<?> superclass = commandClass.getSuperclass();
        final CommandFactory commandSupplier;
        if (object != null) {
            commandSupplier = nullObj -> object;
        } else if (superclass == null) {
            final MethodHandle constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class));
            commandSupplier = nullObj -> constructor.invoke();
        } else {
            final MethodHandle constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class, superclass));
            commandSupplier = o -> constructor.invoke(o);
        }

        final CommandPropertyMapImpl propertyMap = new CommandPropertyMapImpl();
        if (superclass != null)
            propertyMap.setDefaultProperties(propertyMaps.get(superclass));
        else {
            propertyMap.setDefaultProperties(CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()));
        }
        propertyMap.putAnnotations(commandClass.getAnnotations());

        List<Pair<Method, CommandPropertyMapImpl>> methods = Arrays.stream(commandClass.getMethods())
                .filter(method -> method.getParameterCount() > 0)
                .filter(method -> method.getParameterTypes()[0] == CommandEvent.class)
                .filter(method -> method.isAnnotationPresent(Command.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .map(method -> Pair.of(method, new CommandPropertyMapImpl(propertyMap, method.getAnnotations())))
                .collect(Collectors.toList());

        final Optional<Pair<Method, CommandPropertyMapImpl>> first = methods.stream()
                .filter(pair -> pair.getRight().testProperty(Command.class, command -> command.value().length == 1 && command.value()[0].length() == 0))
                .findFirst();


        final CommandParameterBuilder[] parameterBuilders;
        final InvokableCommand commandFunction;
        final CommandPropertyMapImpl methodMap;
        if (first.isPresent()) {
            final Pair<Method, CommandPropertyMapImpl> methodPair = first.get();
            methodMap = methodPair.getRight();
            final Pair<CommandParameterBuilder[], InvokableCommand> biConsumerPair = mapMethod(methodPair.getLeft(), methodPair.getRight());
            parameterBuilders = biConsumerPair.getLeft();
            commandFunction = biConsumerPair.getRight();
        } else {
            parameterBuilders = null;
            commandFunction = null;
            methodMap = propertyMap;
        }

        final CommandHandleBuilderImpl commandHandleBuilder = new CommandHandleBuilderImpl(commandClass,
                clientBuilder,
                commandSupplier,
                parameterBuilders,
                commandFunction,
                methodMap);

        List<Pair<? extends Class<?>, CommandPropertyMapImpl>> classes = Arrays.stream(commandClass.getClasses())
                .filter(aClass -> aClass.isAnnotationPresent(Command.class))
                .filter(aClass -> !Modifier.isStatic(aClass.getModifiers()))
                .map(aClass -> Pair.of(aClass, new CommandPropertyMapImpl(propertyMap, aClass.getAnnotations())))
                .collect(Collectors.toList());

        String simpleName = commandClass.getSimpleName().toLowerCase();
        commandHandleBuilder.setName(simpleName);
        if (simpleName.endsWith("command") && simpleName.length() > 7) {
            simpleName = simpleName.substring(0, simpleName.length() - 7);
        }
        commandHandleBuilder.setKeys(simpleName);
        String[] packageNames = commandClass.getPackage().getName().split("\\.");
        String packageName = packageNames[packageNames.length - 1];
        if (packageName.matches("(command|cmd)(s)?") && packageNames.length > 1) {
            packageName = packageNames[packageNames.length - 2];
        }
        commandHandleBuilder.setGroup(packageName);

        return commandHandleBuilder;
    }

    public CommandHandleBuilder fromMethod(Method method) {

    }

    private Pair<CommandParameterBuilder[], InvokableCommand> mapMethod(Method method, CommandPropertyMap map) throws IllegalAccessException {
        final CommandParameterBuilderFactory factory = new CommandParameterBuilderFactory(map, method.getName());
        final Parameter[] parameters = method.getParameters();
        final CommandParameterBuilder[] parameterBuilders = new CommandParameterBuilder[parameters.length];
        Arrays.setAll(parameterBuilders, value -> factory.builder(parameters[value]));
        final MethodHandle handle = MethodHandles.publicLookup().unreflect(method);
        final MethodHandle spread = handle.asSpreader(Object[].class, parameterBuilders.length);
        InvokableCommandHandle invokableCommandHandle = new InvokableCommandHandle(spread);
        return Pair.of(parameterBuilders, invokableCommandHandle);
    }
}