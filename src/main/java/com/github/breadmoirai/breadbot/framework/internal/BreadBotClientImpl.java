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
package com.github.breadmoirai.breadbot.framework.internal;

import com.github.breadmoirai.breadbot.framework.*;
import com.github.breadmoirai.breadbot.framework.error.MissingCommandKeyException;
import com.github.breadmoirai.breadbot.framework.internal.command.builder.CommandHandleBuilderInternal;
import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import com.github.breadmoirai.breadbot.framework.response.CommandResponseManager;
import com.github.breadmoirai.breadbot.util.EventStringIterator;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GenericGuildMessageEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.IEventManager;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.hooks.SubscribeEvent;
import net.dv8tion.jda.core.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

public class BreadBotClientImpl implements BreadBotClient {

    private static final Logger log = LoggerFactory.getLogger(BreadBotClient.class);

    private JDA jda;

    private final CommandResultManager resultManager;
    private final ArgumentTypesManager argumentTypes;
    private final IEventManager eventManager;
    private final ICommandEventFactory eventFactory;
    private final CommandEngine commandEngine;
    private final Predicate<Message> preProcessPredicate;
    private final List<CommandModule> modules;
    private final Map<Type, CommandModule> moduleTypeMap;
    private final Map<String, CommandHandle> commandMap;
    private final CommandResponseManager responseManager;

    public BreadBotClientImpl(List<CommandModule> modules, List<CommandHandleBuilderInternal> commands, CommandPropertiesManager commandProperties, CommandResultManager resultManager, ArgumentTypesManager argumentTypes, IEventManager eventManager, ICommandEventFactory eventFactory, Predicate<Message> preProcessPredicate, CommandResponseManager responseManager) {
        this.modules = Collections.unmodifiableList(modules);
        this.resultManager = resultManager;
        this.argumentTypes = argumentTypes;
        this.eventManager = eventManager;
        this.eventFactory = eventFactory;
        this.preProcessPredicate = preProcessPredicate;
        this.responseManager = responseManager;

        HashMap<String, CommandHandle> handleMap = new HashMap<>();
        for (CommandHandleBuilderInternal command : commands) {
            CommandHandle handle = command.build();
            String[] keys = handle.getKeys();
            if (keys == null || keys.length == 0) {
                throw new MissingCommandKeyException(handle);
            }
            for (String key : keys) {
                handleMap.put(key, handle);
            }
            log.info("Command Created: " + handle);
        }
        this.commandMap = handleMap;

        final HashMap<Type, CommandModule> typeMap = new HashMap<>(modules.size());
        for (CommandModule module : modules) {
            Class<?> moduleClass = module.getClass();
            do {
                typeMap.put(moduleClass, module);
                for (Class<?> inter : moduleClass.getInterfaces()) {
                    final List<Class<?>> interfaceList = getInterfaceHierarchy(inter, CommandModule.class);
                    if (interfaceList != null) {
                        for (Class<?> interfaceClass : interfaceList)
                            typeMap.put(interfaceClass, module);
                    }
                }
            } while (CommandModule.class.isAssignableFrom(moduleClass = moduleClass.getSuperclass()));
        }

        this.moduleTypeMap = typeMap;

        commandEngine = event -> {
            CommandHandle commandHandle = handleMap.get(event.getKey().toLowerCase());
            if (commandHandle != null) {
                commandHandle.handle(event, new EventStringIterator(event));
            }
        };

        eventManager.register(new BreadBotEventListener(preProcessPredicate));

        log.debug("BreadBotClient Initialized");
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

    @Override
    public JDA getJDA() {
        return jda;
    }

    @Override
    public void setJDA(JDA jda) {
        this.jda = jda;
    }

    @Override
    public IEventManager getEventManager() {
        return eventManager;
    }

    @Override
    public ArgumentTypesManager getArgumentTypes() {
        return argumentTypes;
    }

    @Override
    public CommandResponseManager getResponseManager() {
        return responseManager;
    }

    @Override
    public CommandResultManager getResultManager() {
        return resultManager;
    }

    @Override
    public void sendResponse(CommandResponse response, MessageChannel targetChannel) {
        Checks.notNull(response, "response");
        Checks.notNull(targetChannel, "targetChannel");
        response.setClient(this);
        getResponseManager().acceptResponse(new CommandResponsePacketImpl(null, response, targetChannel));
    }

    @Override
    public Map<String, CommandHandle> getCommandMap() {
        return commandMap;
    }

    @Override
    public boolean hasModule(String moduleName) {
        return moduleName != null && modules.stream().map(CommandModule::getName).anyMatch(moduleName::equalsIgnoreCase);
    }

    @Override
    public boolean hasModule(Class<? extends CommandModule> moduleClass) {
        return moduleTypeMap.containsKey(moduleClass);
    }

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleClass The class of the Module to find
     * @return Optional containing the module if found.
     */
    @Override
    public <T extends CommandModule> T getModule(Class<T> moduleClass) {
        //noinspection unchecked
        return (T) moduleTypeMap.get(moduleClass);
    }

    /**
     * Finds and returns the first Module that is assignable to the provided {@code moduleClass}
     *
     * @param moduleName the name of the module to find. If the module does not override {@link CommandModule#getName IModule#getName} the name of the class is used.
     * @return Optional containing the module if foundd.
     */
    @Override
    public CommandModule getModule(String moduleName) {
        return moduleName == null ? null : modules.stream().filter(module -> module.getName().equalsIgnoreCase(moduleName)).findAny().orElse(null);
    }

    @Override
    public CommandModule getModule(Type moduleType) {
        return moduleTypeMap.get(moduleType);
    }

    @Override
    public List<CommandModule> getModules() {
        return modules;
    }

    @Override
    public CommandEngine getCommandEngine() {
        return commandEngine;
    }

    private class BreadBotEventListener extends ListenerAdapter {

        private final Predicate<Message> preProcessPredicate;

        BreadBotEventListener(Predicate<Message> preProcessPredicate) {
            this.preProcessPredicate = preProcessPredicate == null ? message -> true : preProcessPredicate;
        }

        @SubscribeEvent
        @Override
        public void onReady(ReadyEvent event) {
            setJDA(event.getJDA());
        }

        @SubscribeEvent
        @Override
        public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
            onGuildMessageEvent(event, event.getMessage());
        }

        @SubscribeEvent
        @Override
        public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
            if (event.getMessage().isPinned()) return;
            onGuildMessageEvent(event, event.getMessage());
        }


        private void onGuildMessageEvent(GenericGuildMessageEvent event, Message message) {
            if (preProcessPredicate.test(message)) {
                final CommandEvent commandEvent = eventFactory.createEvent(event, message, BreadBotClientImpl.this);
                if (commandEvent != null) {
                    commandEngine.handle(commandEvent);
                    eventManager.handle(commandEvent);
                }
            }
        }
    }
}