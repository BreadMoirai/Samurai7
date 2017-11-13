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
package com.github.breadmoirai.breadbot.modules.owner;

import com.github.breadmoirai.breadbot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.CommandModule;
import net.dv8tion.jda.core.entities.User;

/**
 * Commands annotated with {@link com.github.breadmoirai.breadbot.modules.owner.Owner @Owner} will only activate if the id provided to the constructor matches the user who sent the command.
 */
public class OwnerModule implements CommandModule {

    private long ownerId;

    /**
     * @param ownerId the id of the owner.
     */
    public OwnerModule(long ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isOwner(User author) {
        return author.getIdLong() == ownerId;
    }

    @Override
    public void initialize(BreadBotClientBuilder builder) {
        builder.associatePreprocessorPredicate("owner", Owner.class, event -> isOwner(event.getAuthor()));
    }
}