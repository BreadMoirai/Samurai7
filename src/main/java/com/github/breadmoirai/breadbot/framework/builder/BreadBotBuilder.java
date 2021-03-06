/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.builder;

import com.github.breadmoirai.breadbot.framework.BreadBot;
import com.github.breadmoirai.breadbot.framework.CommandPlugin;
import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandResultHandler;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandHandleImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertiesManagerImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandResultManagerImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.builder.CommandHandleBuilderFactoryImpl;
import com.github.breadmoirai.breadbot.framework.command.internal.builder.CommandHandleBuilderInternal;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import com.github.breadmoirai.breadbot.framework.event.CommandEventFactory;
import com.github.breadmoirai.breadbot.framework.inject.BreadInjector;
import com.github.breadmoirai.breadbot.framework.inject.InjectionBuilder;
import com.github.breadmoirai.breadbot.framework.inject.InjectionBuilderImpl;
import com.github.breadmoirai.breadbot.framework.internal.BreadBotImpl;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.builder.CommandParameterTypeManagerImpl;
import com.github.breadmoirai.breadbot.plugins.prefix.PrefixPlugin;
import com.github.breadmoirai.breadbot.plugins.prefix.UnmodifiablePrefixPlugin;
import com.github.breadmoirai.breadbot.plugins.waiter.EventWaiterPlugin;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.internal.utils.Checks;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BreadBotBuilder implements
                             CommandPluginBuilder<BreadBotBuilder>,
                             CommandHandleBuilderFactory<BreadBotBuilder>,
                             CommandParameterManagerBuilder<BreadBotBuilder>,
                             CommandResultManagerBuilder<BreadBotBuilder>,
                             CommandPropertiesManager<BreadBotBuilder>,
                             InjectionBuilder<BreadBotBuilder> {

    private final List<CommandPlugin> plugins;
    private final CommandPropertiesManagerImpl commandProperties;
    private final CommandParameterTypeManagerImpl argumentTypes;
    private final CommandHandleBuilderFactoryImpl factory;
    private final List<CommandHandleBuilderInternal> commandBuilders;
    private final List<Command> commands;
    private final CommandResultManagerImpl resultManager;
    private InjectionBuilderImpl injector;
    private boolean injectionEnabled;
    private Predicate<Message> preProcessPredicate;
    private CommandEventFactory commandEventFactory;
    private boolean shouldEvaluateCommandOnMessageUpdate = false;

    public BreadBotBuilder() {
        plugins = new ArrayList<>();
        commandProperties = new CommandPropertiesManagerImpl();
        argumentTypes = new CommandParameterTypeManagerImpl();
        factory = new CommandHandleBuilderFactoryImpl(this);
        commandBuilders = new ArrayList<>();
        commands = new ArrayList<>();
        resultManager = new CommandResultManagerImpl();
        injector = new InjectionBuilderImpl();
        injectionEnabled = false;
    }

    @Override
    public BreadBotBuilder addPlugin(Collection<CommandPlugin> plugins) {
        Checks.noneNull(plugins, "plugins");
        for (CommandPlugin module : plugins) {
            module.initialize(this);
        }
        this.plugins.addAll(plugins);
        return this;
    }

    @Override
    public BreadBotBuilder addPlugin(CommandPlugin plugin) {
        Checks.notNull(plugin, "plugin");
        this.plugins.add(plugin);
        plugin.initialize(this);
        return this;
    }

    @Override
    public boolean hasPlugin(Class<? extends CommandPlugin> pluginClass) {
        return pluginClass != null && plugins.stream().map(Object::getClass).anyMatch(pluginClass::isAssignableFrom);
    }

    @Override
    public <T extends CommandPlugin> T getPlugin(Class<T> pluginClass) {
        return pluginClass == null ? null : plugins.stream()
                .filter(module -> pluginClass.isAssignableFrom(module.getClass()))
                .map(pluginClass::cast)
                .findAny()
                .orElse(null);
    }

    public BreadBotBuilder addCommand(Command command) {
        commands.add(command);
        return this;
    }

    @Override
    public CommandHandleBuilder createCommand(Consumer<CommandEvent> onCommand) {
        Checks.notNull(onCommand, "onCommand");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(onCommand);
        commandBuilders.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Class<?> commandClass) {
        Checks.notNull(commandClass, "commandClass");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandClass);
        commandBuilders.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Object commandObject) {
        Checks.notNull(commandObject, "commandObject");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandObject);
        commandBuilders.add(commandHandle);
        return commandHandle;
    }

    @Override
    public CommandHandleBuilder createCommand(Supplier<?> commandSupplier) {
        Checks.notNull(commandSupplier, "commandSupplier");
        CommandHandleBuilderInternal commandHandle = factory.createCommand(commandSupplier);
        commandBuilders.add(commandHandle);
        return commandHandle;
    }

    @Override
    public List<CommandHandleBuilder> createCommands(String packageName) {
        Checks.notNull(packageName, "packageName");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(packageName);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Class<?> commandClass) {
        Checks.notNull(commandClass, "commandClass");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandClass);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Object commandObject) {
        Checks.notNull(commandObject, "commandObject");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandObject);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommands(Supplier<?> commandSupplier) {
        Checks.notNull(commandSupplier, "commandSupplier");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommands(commandSupplier,
                                                                                   commandSupplier.get());
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromClasses(Collection<Class<?>> commandClasses) {
        Checks.noneNull(commandClasses, "commandClasses");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromClasses(commandClasses);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromObjects(Collection<?> commandObjects) {
        Checks.noneNull(commandObjects, "commandObjects");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromObjects(commandObjects);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public List<CommandHandleBuilder> createCommandsFromSuppliers(Collection<Supplier<?>> commandSupplier) {
        Checks.noneNull(commandSupplier, "commandSuppliers");
        List<CommandHandleBuilderInternal> commandHandles = factory.createCommandsFromSuppliers(commandSupplier);
        commandBuilders.addAll(commandHandles);
        return Collections.unmodifiableList(commandHandles);
    }

    @Override
    public BreadBotBuilder clearCommandModifiers(Class<?> propertyType) {
        commandProperties.clearCommandModifiers(propertyType);
        return this;
    }

    @Override
    public BreadBotBuilder clearParameterModifiers(Class<?> parameterType) {
        commandProperties.clearParameterModifiers(parameterType);
        return this;
    }

    @Override
    public <T> BreadBotBuilder bindCommandModifier(Class<T> propertyType,
                                                   BiConsumer<T, CommandHandleBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        commandProperties.bindCommandModifier(propertyType, configurator);
        for (CommandHandleBuilder commandBuilder : commandBuilders) {
            applyCommandModifierRecursive(propertyType, configurator, commandBuilder);
        }
        return this;
    }

    private <T> void applyCommandModifierRecursive(Class<T> propertyType,
                                                   BiConsumer<T, CommandHandleBuilder> configurator,
                                                   CommandHandleBuilder commandBuilder) {
        if (commandBuilder.hasProperty(propertyType)) {
            final T property = commandBuilder.getProperty(propertyType);
            configurator.accept(property, commandBuilder);
        }
        for (CommandHandleBuilder c : commandBuilder.getChildren()) {
            applyCommandModifierRecursive(propertyType, configurator, c);
        }
    }

    @Override
    public <T> BreadBotBuilder bindParameterModifier(Class<T> propertyType,
                                                     BiConsumer<T, CommandParameterBuilder> configurator) {
        Checks.notNull(configurator, "configurator");
        commandProperties.bindParameterModifier(propertyType, configurator);
        for (CommandHandleBuilder c : commandBuilders) {
            applyParameterModifierRecursive(propertyType, configurator, c);
        }
        return this;
    }

    private <T> void applyParameterModifierRecursive(Class<T> propertyType,
                                                     BiConsumer<T, CommandParameterBuilder> configurator,
                                                     CommandHandleBuilder commandBuilder) {
        for (CommandParameterBuilder parameterBuilder : commandBuilder.getParameters()) {
            if (parameterBuilder.hasProperty(propertyType)) {
                final T t = parameterBuilder.getProperty(propertyType);
                configurator.accept(t, parameterBuilder);
            }
        }
        for (CommandHandleBuilder commandHandleBuilder : commandBuilder.getChildren()) {
            applyParameterModifierRecursive(propertyType, configurator, commandHandleBuilder);
        }
    }

    @Override
    public void applyModifiers(CommandHandleBuilder builder) {
        commandProperties.applyModifiers(builder);
    }

    @Override
    public <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        return commandProperties.getCommandModifier(propertyType);
    }

    @Override
    public <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        commandProperties.applyCommandModifier(propertyType, builder);
    }

    @Override
    public void applyModifiers(CommandParameterBuilder builder) {
        commandProperties.applyModifiers(builder);
    }

    @Override
    public <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        return commandProperties.getParameterModifier(propertyType);
    }

    @Override
    public <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        commandProperties.applyParameterModifier(propertyType, builder);
    }

    @Override
    public List<String> getPreprocessorPriorityList() {
        return commandProperties.getPreprocessorPriorityList();
    }

    @Override
    public BreadBotBuilder setPreprocessorPriority(String... identifiers) {
        commandProperties.setPreprocessorPriority(identifiers);
        return this;
    }

    @Override
    public BreadBotBuilder setPreprocessorPriority(List<String> identifierList) {
        commandProperties.setPreprocessorPriority(identifierList);
        return this;
    }

    @Override
    public Comparator<CommandPreprocessor> getPriorityComparator() {
        return commandProperties.getPriorityComparator();
    }

    @Override
    public <T> BreadBotBuilder bindTypeParser(Class<T> type, TypeParser<T> parser) {
        argumentTypes.bindTypeParser(type, parser);
        return this;
    }

    @Override
    public <T> TypeParser<T> getTypeParser(Class<T> type) {
        return argumentTypes.getTypeParser(type);
    }

    @Override
    public BreadBotBuilder clearTypeModifiers(Class<?> parameterType) {
        argumentTypes.clearTypeModifiers(parameterType);
        return this;
    }

    @Override
    public BreadBotBuilder bindTypeModifier(Class<?> parameterType, Consumer<CommandParameterBuilder> modifier) {
        argumentTypes.bindTypeModifier(parameterType, modifier);
        for (CommandHandleBuilderInternal commandBuilder : commandBuilders) {
            applyTypeModifierRecursive(commandBuilder, parameterType, modifier);
        }
        return this;
    }

    private void applyTypeModifierRecursive(CommandHandleBuilder commandBuilder, Class<?> parameterType,
                                            Consumer<CommandParameterBuilder> modifier) {
        for (CommandParameterBuilder commandParameterBuilder : commandBuilder.getParameters()) {
            if (commandParameterBuilder.getDeclaringParameter().getType() == parameterType) {
                modifier.accept(commandParameterBuilder);
            }
        }
        for (CommandHandleBuilder subCommand : commandBuilder.getChildren()) {
            applyTypeModifierRecursive(subCommand, parameterType, modifier);
        }
    }

    @Override
    public void applyTypeModifiers(CommandParameterBuilder parameterBuilder) {
        argumentTypes.applyTypeModifiers(parameterBuilder);
    }

    @Override
    public <T> BreadBotBuilder bindResultHandler(Class<T> resultType, CommandResultHandler<T> handler) {
        resultManager.bindResultHandler(resultType, handler);
        return this;
    }

    @Override
    public <T> CommandResultHandler<? super T> getResultHandler(Class<T> resultType) {
        return resultManager.getResultHandler(resultType);
    }

    /**
     * Sets a predicate to be used on each message before processing it. This will override any existing predicates.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     * @return this
     */
    public BreadBotBuilder setPreProcessPredicate(Predicate<Message> predicate) {
        preProcessPredicate = predicate;
        return this;
    }

    /**
     * Appends a predicate to be used on each message before processing it.
     *
     * @param predicate a predicate which returns {@code true} if a message should be processed as a command.
     * @return this
     */
    public BreadBotBuilder addPreProcessPredicate(Predicate<Message> predicate) {
        if (preProcessPredicate == null) {
            preProcessPredicate = predicate;
        } else {
            preProcessPredicate = preProcessPredicate.and(predicate);
        }
        return this;
    }

    /**
     * This will allow messages to be re-evaluated on message edit.
     * This will also evaluate commands that are unpinned.
     * Will ignore messages that are pinned.
     *
     * @param shouldEvaluateCommandOnMessageUpdate By default this is {@code false}.
     * @return this
     */
    public BreadBotBuilder setEvaluateCommandOnMessageUpdate(boolean shouldEvaluateCommandOnMessageUpdate) {
        this.shouldEvaluateCommandOnMessageUpdate = shouldEvaluateCommandOnMessageUpdate;
        return this;
    }

    @Override
    public BreadBotBuilder self() {
        return this;
    }

    public BreadBotBuilder enableInjection() {
        injectionEnabled = true;
        return this;
    }

    @Override
    public <V> BreadBotBuilder bindInjection(V fieldValue) {
        injector.bindInjection(fieldValue);
        return this;
    }

    @Override
    public <V> BreadBotBuilder bindInjection(Class<V> fieldType, V fieldValue) {
        injector.bindInjection(fieldType, fieldValue);
        return this;
    }

    /**
     * Builds the BreadBotClient with the provided EventManager.
     * If an {@link PrefixPlugin} has not been provided, a {@link UnmodifiablePrefixPlugin new
     * UnmodifiablePrefixPlugin("!")} is provided.
     *
     * @return a new BreadBotClient. This must be added to JDA with
     * {@link JDABuilder#addEventListeners(Object...)}
     */
    public BreadBot build() {
        if (!hasPlugin(PrefixPlugin.class)) plugins.add(new UnmodifiablePrefixPlugin("!"));
        if (!hasPlugin(EventWaiterPlugin.class))
            plugins.add(new EventWaiterPlugin());
        if (commandEventFactory == null)
            commandEventFactory = new CommandEventFactory(getPlugin(PrefixPlugin.class));
        Map<Type, CommandPlugin> typeMap = createPluginTypeMap(plugins);
        final List<CommandHandleImpl> build;
        final BreadInjector breadInjector;
        if (injectionEnabled) {
            typeMap.forEach((type, commandPlugin) -> {
                injector.bindInjectionUnchecked(type, commandPlugin);
            });
            breadInjector = injector.build();
            for (CommandHandleBuilderInternal commandBuilder : commandBuilders) {
                commandBuilder.setInjector(breadInjector);
            }
        }
        build = commandBuilders.stream().map(o -> o.build(null)).collect(Collectors.toList());
        commands.addAll(build);
        commandEventFactory.setPreprocessor(preProcessPredicate);
        final BreadBotImpl breadBotClient = new BreadBotImpl(plugins, typeMap, commands, resultManager,
                                                             argumentTypes, commandEventFactory,
                                                             shouldEvaluateCommandOnMessageUpdate);
        breadBotClient.propagateReadyEvent();
        return breadBotClient;
    }

    private Map<Type, CommandPlugin> createPluginTypeMap(List<CommandPlugin> modules) {
        final HashMap<Type, CommandPlugin> typeMap = new HashMap<>(modules.size());
        for (CommandPlugin module : modules) {
            Class<?> moduleClass = module.getClass();
            do {
                typeMap.put(moduleClass, module);
                for (Class<?> inter : moduleClass.getInterfaces()) {
                    final List<Class<?>> interfaceList = getInterfaceHierarchy(inter, CommandPlugin.class);
                    if (interfaceList != null) {
                        for (Class<?> interfaceClass : interfaceList)
                            typeMap.put(interfaceClass, module);
                    }
                }
            } while (CommandPlugin.class.isAssignableFrom(moduleClass = moduleClass.getSuperclass()));
        }
        return typeMap;
    }

    private List<Class<?>> getInterfaceHierarchy(Class<?> from, Class<?> toSuper) {
        if (!from.isInterface())
            return null;
        if (from == toSuper)
            return new ArrayList<>();
        final Class<?>[] interfaces = from.getInterfaces();
        if (interfaces.length == 0)
            return null;
        final List<Class<?>> interfaceList = getInterfaceHierarchy(interfaces[0], toSuper);
        if (interfaceList != null)
            interfaceList.add(0, from);
        return interfaceList;
    }

}