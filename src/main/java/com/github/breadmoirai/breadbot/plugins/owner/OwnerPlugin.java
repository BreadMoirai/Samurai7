package com.github.breadmoirai.breadbot.plugins.owner;

import com.github.breadmoirai.breadbot.framework.BreadBotPlugin;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import net.dv8tion.jda.core.entities.Member;

public abstract class OwnerPlugin implements BreadBotPlugin {

    public abstract boolean isOwner(Member member);

    @Override
    public final void initialize(BreadBotClientBuilder builder) {
        builder.associatePreprocessorPredicate("owner", Owner.class, event -> isOwner(event.getMember()));
    }


}
