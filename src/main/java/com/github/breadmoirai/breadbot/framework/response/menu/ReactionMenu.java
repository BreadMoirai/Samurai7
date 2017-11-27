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
package com.github.breadmoirai.breadbot.framework.response.menu;

import com.github.breadmoirai.breadbot.framework.response.menu.reactions.MenuReaction;
import com.github.breadmoirai.breadbot.waiter.EventWaiterB;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReactionMenu extends Menu {

    private List<MenuReaction> reactions;

    ReactionMenu(List<MenuReaction> reactions) {
        this.reactions = reactions;
    }

    @Override
    void attachOptions(EmbedBuilder embedBuilder) {
        final String collect = reactions.stream().filter(MenuReaction::hasOption).map(MenuReaction::getDisplay).collect(Collectors.joining("\n"));
        if (!collect.isEmpty())
            embedBuilder.addField("", collect, false);
    }

    @Override
    void waitForEvent(MenuResponse responseMenu, EventWaiterB waiter) {
        waiter.waitForEvent(GuildMessageReactionAddEvent.class, event -> {
            if (event.getMessageIdLong() == responseMenu.getMessageId()) {
                final Optional<MenuReaction> any = reactions.stream().filter(r -> r.matches(event)).findAny();
                if (any.isPresent()) {
                    final MenuReaction r = any.get();
                    if (r.hasPredicate()) return r.apply(event, responseMenu);
                }
            }
            return false;
        });
    }

    @Override
    void addReactions(Message message) {
        reactions.forEach(r -> r.addReactionTo(message).queue());
    }

    @Override
    public List<MenuReaction> getOptions() {
        return Collections.unmodifiableList(reactions);
    }

}
