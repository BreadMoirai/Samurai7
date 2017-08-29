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
package com.github.breadmoirai.bot.framework;

import com.github.breadmoirai.bot.framework.impl.CommandEngineBuilder;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import org.json.JSONObject;

public interface IModule {

    default String getName() {
        return this.getClass().getSimpleName();
    }

    void init(CommandEngineBuilder engineBuilder, SamuraiClient client);

    default void onHelpEvent(CommandEvent event) {
        event.reply("This is not the help you are looking for");
    }

    default boolean isJSONConfigurable() {
        return false;
    }

    default void addJSONConfig(long guildId, JSONObject jsonObject) {
    }

    default boolean loadJSONConfig(long guildId, JSONObject jsonObject) {
        return false;
    }

}
