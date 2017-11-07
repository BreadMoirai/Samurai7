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
package com.github.breadmoirai.breadbot.modules.admin;

import com.github.breadmoirai.breadbot.framework.command.Command;
import com.github.breadmoirai.breadbot.framework.command.MainCommand;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import net.dv8tion.jda.core.entities.Member;

import java.util.stream.Collectors;


public class AdminCommand {

    @MainCommand
    public void admin(CommandEvent event, AdminModule module) {
        event.reply("**Administrative Members:** " + event.getGuild().getMembers().stream().filter(module::isAdmin).map(Member::getEffectiveName).collect(Collectors.joining(", ")));
    }

//    @Override
//    public void getHelp(CommandEvent event) {
//        event.reply("This command shows which users have the authority to use Administrative commands");
//    }
}
