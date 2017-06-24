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
 *
 */
package samurai7.modules.prefix;

import samurai7.core.command.Command;
import samurai7.core.command.CommandEvent;
import samurai7.core.command.Key;
import samurai7.core.response.Response;
import samurai7.modules.admin.Admin;
import samurai7.util.DiscordPatterns;

@Admin
@Key("prefix")
public class PrefixCommand extends Command<PrefixModule> {
    @Override
    public Response execute(CommandEvent event, PrefixModule module) {
        if (event.hasContent()) {
            final String content = event.getContent();
            if (DiscordPatterns.WHITE_SPACE.matcher(content).find())
                return Response.of("New prefix must not contain spaces");
            else if (content.length() > 16)
                return Response.of("New prefix must not be greater than 16 characters");
            else {
                module.changePrefix(event.getGuildId(), content);
                return Response.ofFormat("Prefix has been set to `%s`", content);
            }
        }
        return Response.ofFormat("The current prefix is `%s`", module.getPrefix(event.getGuildId()));
    }
}
