package com.github.breadmoirai.framework.event.args;

import com.github.breadmoirai.framework.util.Emoji;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class MentionArgument implements CommandArgument{

    private final JDA jda;
    private final Guild guild;
    private final TextChannel channel;
    private final String arg;

    public MentionArgument(JDA jda, Guild guild, TextChannel channel, String arg) {
        this.jda = jda;
        this.guild = guild;
        this.channel = channel;
        this.arg = arg;
    }

    @Override
    public boolean isOfType(ArgumentType type) {
        switch (type) {
            case USER:
                return isUser();
            case MEMBER:
                return isMember();
            case ROLE:
                return isRole();
            case TEXTCHANNEL:
                return isTextChannel();
            case EMOTE:
                return isEmote();
        }
        return false;
    }

    @Override
    public String getArgument() {
        return arg;
    }

    @Override
    public JDA getJDA() {
        return jda;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public TextChannel getChannel() {
        return channel;
    }

    @Override
    public boolean isMention() {
        return true;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public int parseInt() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isLong() {
        return false;
    }

    @Override
    public long parseLong() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isFloat() {
        return false;
    }

    @Override
    public float parseFloat() {
        throw new NumberFormatException(arg);
    }

    @Override
    public double parseDouble() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isRange() {
        return false;
    }

    @NotNull
    @Override
    public IntStream parseRange() {
        return IntStream.empty();
    }

    @Override
    public boolean isHex() {
        return false;
    }

    @Override
    public int parseIntFromHex() {
        throw new NumberFormatException(arg);
    }

    @Override
    public boolean isUser() {
        return false;
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public boolean isMember() {
        return false;
    }

    @Nullable
    @Override
    public Member getMember() {
        return null;
    }

    @NotNull
    @Override
    public Optional<Member> findMember() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public List<Member> searchMembers() {
        return Collections.emptyList();
    }

    @Override
    public boolean isRole() {
        return false;
    }

    @Nullable
    @Override
    public Role getRole() {
        return null;
    }

    @NotNull
    @Override
    public Optional<Role> findRole() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public List<Role> searchRoles() {
        return Collections.emptyList();
    }

    @Override
    public boolean isTextChannel() {
        return false;
    }

    @Nullable
    @Override
    public TextChannel getTextChannel() {
        return null;
    }

    @NotNull
    @Override
    public Optional<TextChannel> findTextChannel() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public List<TextChannel> searchTextChannels() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Optional<VoiceChannel> findVoiceChannel() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public List<VoiceChannel> searchVoiceChannels() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEmote() {
        return false;
    }

    @Nullable
    @Override
    public Emote getEmote() {
        return null;
    }

    @Override
    public boolean isEmoji() {
        return false;
    }

    @Nullable
    @Override
    public Emoji getEmoji() {
        return null;
    }

    @Override
    public String toString() {
        return getArgument();
    }
}