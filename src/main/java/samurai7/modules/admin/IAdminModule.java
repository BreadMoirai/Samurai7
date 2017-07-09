/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package samurai7.modules.admin;

import net.dv8tion.jda.core.entities.Member;
import samurai7.core.IModule;
import samurai7.core.engine.CommandEngineConfiguration;

public interface IAdminModule extends IModule {

    @Override
    default void init(CommandEngineConfiguration config) {
        config.addPostProcessPredicate(command -> !command.isMarkedWith(Admin.class) || isAdmin(command.getEvent().getMember()));
        config.registerCommand(AdminCommand.class);
    }

    boolean isAdmin(Member member);
}