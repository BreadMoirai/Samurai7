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
package com.github.breadmoirai.bot.framework.command;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A builder for a {@link com.github.breadmoirai.bot.framework.command.CommandPropertyMap}. This map can inherit values from another map.
 */
public class CommandPropertyMapBuilder implements Iterable<Object> {

    private CommandPropertyMap defaultProperties;
    private final Map<Class<?>, Object> properties;

    public CommandPropertyMapBuilder(CommandPropertyMap base) {
        defaultProperties = base;
        properties = new HashMap<>();
    }

    public CommandPropertyMapBuilder() {
        this(null);
    }

    public boolean containsProperty(Class<?> propertyType) {
        return properties.containsKey(propertyType);
    }

    /**
     * Retrieves the property of the passed {@link java.lang.Class}. If this obj does not contain a mapping, it will attempt to provide a value from it's defaultPropertyMap
     *
     * @param propertyType a class
     * @param <T>          the type
     * @return the type if found, otherwise {@code null}
     */
    public <T> T getProperty(Class<T> propertyType) {
        final Object obj = properties.get(propertyType);
        if (obj == null) {
            if (defaultProperties != null)
                return defaultProperties.getProperty(propertyType);
            else return null;
        }
        return propertyType.cast(obj);
    }

    public <T> CommandPropertyMapBuilder putProperty(Class<? super T> propertyType, T propertyObj) {
        properties.put(propertyType, propertyObj);
        return this;
    }

    public CommandPropertyMapBuilder putProperty(Object propertyObj) {
        properties.put(propertyObj.getClass(), propertyObj);
        return this;
    }

    public CommandPropertyMapBuilder setDefaultProperties(CommandPropertyMap defaultProperties) {
        this.defaultProperties = defaultProperties;
        return this;
    }

    /**
     * Returns a read-only unmodifiable {@link java.util.Set} view of the mappings contained in this map.
     *
     * @return a set view of the mappings contained in this map.
     */
    public Set<Map.Entry<Class<?>, Object>> entrySet() {
        return Collections.unmodifiableSet(properties.entrySet());
    }

    /**
     * Returns a read-only unmodifiable {@link java.util.Collection} view of the mappings contained in this map.
     *
     * @return a {@link java.util.Collection} view of the mappings contained in this map.
     */
    public Collection<Object> values() {
        return Collections.unmodifiableCollection(properties.values());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandPropertyMapBuilder objects = (CommandPropertyMapBuilder) o;

        if (defaultProperties != null ? !defaultProperties.equals(objects.defaultProperties) : objects.defaultProperties != null)
            return false;
        return properties != null ? properties.equals(objects.properties) : objects.properties == null;
    }

    @Override
    public int hashCode() {
        int result = defaultProperties != null ? defaultProperties.hashCode() : 0;
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    /**
     * Returns an {@link java.util.Iterator} of the property values in this map.
     *
     * @return an {@link java.util.Iterator}
     */
    @NotNull
    @Override
    public Iterator<Object> iterator() {
        return values().iterator();
    }

    public CommandPropertyMap build() {
        return new CommandPropertyMap(defaultProperties, properties);
    }
}