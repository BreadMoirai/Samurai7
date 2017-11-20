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
package com.github.breadmoirai.breadbot.framework.response.simple;

import com.github.breadmoirai.breadbot.framework.response.CommandResponse;
import com.github.breadmoirai.breadbot.framework.response.menu.reactions.MenuEmoji;
import com.github.breadmoirai.breadbot.framework.response.menu.reactions.MenuEmote;
import com.github.breadmoirai.breadbot.framework.response.menu.reactions.MenuReaction;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ReactionResponse extends CommandResponse {

    private Consumer<Void> onSuccess;
    private Consumer<Throwable> onFailure;
    private final MenuReaction reaction;

    public ReactionResponse(long messageId, String unicode) {
        this.setMessageId(messageId);
        this.reaction = new MenuEmoji(unicode, null, null);
    }

    public ReactionResponse(long messageId, Emote emote) {
        this.setMessageId(messageId);
        this.reaction = new MenuEmote(emote, null, null);
    }

    @Override
    public void onSend(Message message) {
        //hjirhh
    }


    @Override
    public void sendTo(MessageChannel channel, BiConsumer<Message, CommandResponse> onSuccess, Consumer<Throwable> onFailure) {
        reaction.addReactionTo(channel, getMessageId()).queue(null, onFailure);
    }

    private void onSend(Void nothing) {
        if (onSuccess != null) {
            onSuccess.accept(nothing);
        }
    }

    @Override
    public void onFailure(Throwable t) {
        if (onFailure == null) {
            super.onFailure(t);
        } else onFailure.accept(t);
    }

    @Override
    public Message buildMessage() {
        return null;
    }

    public ReactionResponse uponSuccess(Consumer<Void> successConsumer) {
        this.onSuccess = successConsumer;
        return this;
    }

    public ReactionResponse withSuccess(Consumer<Void> successConsumer) {
        if (this.onSuccess == null) this.onSuccess = successConsumer;
        else this.onSuccess = this.onSuccess.andThen(successConsumer);
        return this;
    }

    public ReactionResponse uponFailure(Consumer<Throwable> failureConsumer) {
        this.onFailure = failureConsumer;
        return this;
    }

    @SuppressWarnings("Duplicates")
    public ReactionResponse withFailure(Consumer<Throwable> failureConsumer) {
        if (onFailure == null) onFailure = t -> {
            super.onFailure(t);
            failureConsumer.accept(t);
        };
        else onFailure = onFailure.andThen(failureConsumer);
        return this;
    }

}
