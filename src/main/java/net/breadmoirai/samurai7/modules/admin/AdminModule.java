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
package net.breadmoirai.samurai7.modules.admin;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.breadmoirai.samurai7.core.engine.CommandEngineConfiguration;

public class AdminModule implements IAdminModule {
    @Override
    public void init(CommandEngineConfiguration config) {
        config.addPostProcessPredicate(command -> !command.isMarkedWith(Admin.class) || isAdmin(command.getEvent().getMember()));
        config.registerCommand(AdminCommand.class);
    }

    @Override
    public boolean isAdmin(Member member) {
        return member.canInteract(member.getGuild().getSelfMember()) && member.hasPermission(Permission.KICK_MEMBERS);
    }
}
