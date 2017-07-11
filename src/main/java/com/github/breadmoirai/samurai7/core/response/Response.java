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
package com.github.breadmoirai.samurai7.core.response;

import com.github.breadmoirai.samurai7.core.SamuraiClient;
import com.github.breadmoirai.samurai7.core.response.simple.EditResponse;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageDeleteEvent;

import java.io.Serializable;
import java.util.function.Consumer;

public abstract class Response implements Serializable {

    private transient SamuraiClient client;

    private long authorId, messageId, channelId, guildId;

    public void send(MessageChannel channel, Consumer<Long> registerMessageId) {
        final Message message = buildMessage();
        if (message == null) return;

        Consumer<Message> onSend;
        if (registerMessageId != null)
            onSend = sent -> {
                long messageId = sent.getIdLong();
                this.setMessageId(messageId);
                registerMessageId.accept(messageId);
                this.onSend(sent);
            };
        else
            onSend = sent -> {
                this.setMessageId(sent.getIdLong());
                this.onSend(sent);
            };
        channel.sendMessage(message).queue(onSend);
    }

    public final SamuraiClient getClient() {
        return client;
    }

    public final void setClient(SamuraiClient client) {
        this.client = client;
    }

    public abstract Message buildMessage();

    public abstract void onSend(Message message);

    public final long getAuthorId() {
        return authorId;
    }

    public final void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public final long getMessageId() {
        return messageId;
    }

    public final void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public final long getChannelId() {
        return channelId;
    }

    public final void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public final long getGuildId() {
        return guildId;
    }

    public final void setGuildId(long guildId) {
        this.guildId = guildId;
    }


    protected final void submitNewResponse(Response response) {
        if (response.getChannelId() == 0) response.setChannelId(getChannelId());
        if (response.getGuildId() == 0) response.setGuildId(getGuildId());
        if (response.getAuthorId() == 0) response.setAuthorId(getAuthorId());
        response.setClient(client);
        client.submit(response);
    }

    public abstract void onDeletion(MessageDeleteEvent event);

    /**
     * This can only be used on messages sent by the bot.
     * If you want to edit a response in a different channel
     * first use {@link com.github.breadmoirai.samurai7.core.response.Response#setChannelId(long)   Response#setChannelId(long)}
     */
    public EditResponse replace(long messageId) {
        EditResponse editResponse = new EditResponse(this, messageId);
        editResponse.setClient(getClient());
        return editResponse;
    }
}
