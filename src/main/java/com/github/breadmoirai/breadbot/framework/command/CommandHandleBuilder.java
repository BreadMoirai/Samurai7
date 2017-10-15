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
package com.github.breadmoirai.breadbot.framework.command;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.event.CommandEvent;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public interface CommandHandleBuilder {

    default CommandHandleBuilder addSubCommand(@Nullable Consumer<CommandEvent> onCommand, Consumer<CommandHandleBuilder> configurator) {
        Objects.requireNonNull(configurator);
        CommandHandleBuilder subCommand = createSubCommand(onCommand);
        configurator.accept(subCommand);
        return this;
    }

    CommandHandleBuilder createSubCommand(Consumer<CommandEvent> onCommand);

    CommandHandleBuilder setKeys(String... key);

    CommandHandleBuilder setName(String name);

    CommandHandleBuilder setGroup(String group);

    CommandHandleBuilder setDescription(String description);

    boolean containsProperty(Class<?> propertyType);

    <T> T getProperty(Class<T> propertyType);

    <T> CommandHandleBuilder putProperty(Class<? super T> type, T property);

    CommandHandleBuilder putProperty(Object property);

    default CommandHandleBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        return addPreprocessor(new CommandPreprocessor(identifier, function));
    }

    default CommandHandleBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        return addPreprocessor(new CommandPreprocessor(identifier, predicate));
    }

    default CommandHandleBuilder addPreprocessor(CommandPreprocessor preprocessor) {
        getPreprocessors().add(preprocessor);
        return this;
    }

    List<CommandPreprocessor> getPreprocessors();

    default CommandHandleBuilder sortPreprocessors(Comparator<CommandPreprocessor> comparator) {
        getPreprocessors().sort(comparator);
        return this;
    }

//    default CommandHandleBuilder addAssociatedPreprocessors() {
//        final Map<Class<?>, Function<?, CommandPreprocessor>> preprocessorFactoryMap = getClientBuilder().getPreprocessors().getPreprocessorFactoryMap();
//        final CommandPropertyMapImpl propertyMapBuilder = getPropertyMapBuilder();
//        final Set<Map.Entry<Class<?>, Object>> entries = propertyMapBuilder.entrySet();
//        for (Map.Entry<Class<?>, Object> entry : entries) {
//            final Class<?> propertyType = entry.getKey();
//            final Function<?, CommandPreprocessor> factory = preprocessorFactoryMap.get(propertyType);
//            if (factory != null) {
//                @SuppressWarnings("unchecked") final CommandPreprocessor preprocessor = ((Function<Object, CommandPreprocessor>) factory).apply(entry.getValue());
//                addPreprocessor(preprocessor);
//            }
//        }
//        return this;
//    }

    /**
     *
     */
    Class<?> getDeclaringClass();

    Method getDeclaringMethod();

    BreadBotClientBuilder getClientBuilder();

    CommandHandle build(BreadBotClient client);

}
