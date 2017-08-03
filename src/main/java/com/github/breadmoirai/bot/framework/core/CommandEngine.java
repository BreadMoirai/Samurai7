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
package com.github.breadmoirai.bot.framework.core;

import com.github.breadmoirai.bot.framework.core.command.ICommand;
import com.github.breadmoirai.bot.framework.core.impl.Response;

import java.util.Optional;
import java.util.stream.Stream;

public interface CommandEngine {

    Optional<Response> execute(CommandEvent event);

    Class<? extends ICommand> getCommandClass(String key);

    default ICommand getCommand(String key) {
        try {
            final Class<? extends ICommand> commandClass = getCommandClass(key);
            if (commandClass != null) return commandClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log(e);
        }
        return null;
    }

    void log(ReflectiveOperationException e);

    Stream<Class<? extends ICommand>> getCommands();

    boolean hasCommand(String key);
}