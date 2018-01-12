/*
 *        Copyright 2017-2018 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.breadbot.framework.command.internal;

import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorFunction;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessorPredicate;
import com.github.breadmoirai.breadbot.framework.command.CommandPropertiesManager;
import com.github.breadmoirai.breadbot.framework.defaults.DefaultCommandProperties;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class CommandPropertiesManagerImpl implements CommandPropertiesManager {

    private List<String> preprocessorPriorityList = Collections.emptyList();

    private static Map<Package, CommandPropertyMapImpl> packageMap = new HashMap<>();

    private final Map<Class<?>, BiConsumer<?, CommandHandleBuilder>> commandPropertyMap = new HashMap<>();
    private final Map<Class<?>, BiConsumer<?, CommandParameterBuilder>> parameterPropertyMap = new HashMap<>();

    public CommandPropertiesManagerImpl() {
        putCommandModifier(null, (o, b) -> {
        });
        putParameterModifier(null, (o, b) -> {
        });
        new DefaultCommandProperties().initialize(this);
    }

    @Override
    public <T> void putCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        commandPropertyMap.put(propertyType, configurator);
    }

    @Override
    public <T> void appendCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator) {
        @SuppressWarnings("unchecked") BiConsumer<Object, CommandHandleBuilder> c1 = (BiConsumer<Object, CommandHandleBuilder>) commandPropertyMap.get(propertyType);
        if (c1 == null) {
            putCommandModifier(propertyType, configurator);
            return;
        }
        @SuppressWarnings("unchecked") BiConsumer<Object, CommandHandleBuilder> c2 = (BiConsumer<Object, CommandHandleBuilder>) configurator;
        commandPropertyMap.put(propertyType, (o, builder) -> {
            c1.accept(o, builder);
            c2.accept(o, builder);
        });
    }

    @Override
    public <T> void putParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        parameterPropertyMap.put(propertyType, configurator);
    }

    @Override
    public <T> void appendParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator) {
        @SuppressWarnings("unchecked") BiConsumer<Object, CommandParameterBuilder> c1 = (BiConsumer<Object, CommandParameterBuilder>) parameterPropertyMap.get(propertyType);
        if (c1 == null) {
            putParameterModifier(propertyType, configurator);
            return;
        }
        @SuppressWarnings("unchecked") BiConsumer<Object, CommandParameterBuilder> c2 = (BiConsumer<Object, CommandParameterBuilder>) configurator;
        parameterPropertyMap.put(propertyType, (o, builder) -> {
            c1.accept(o, builder);
            c2.accept(o, builder);
        });
    }

    @Override
    public void applyModifiers(CommandHandleBuilder builder) {
        for (Class<?> aClass : commandPropertyMap.keySet())
            if (aClass != null) if (builder.hasProperty(aClass))
                applyCommandModifier(aClass, builder);
        applyCommandModifier(null, builder);
    }

    @Override
    public <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType) {
        BiConsumer<?, CommandHandleBuilder> biConsumer = commandPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandHandleBuilder> consumer = (BiConsumer<T, CommandHandleBuilder>) biConsumer;
        return consumer;
    }

    @Override
    public <T> void applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder) {
        BiConsumer<T, CommandHandleBuilder> commandModifier = getCommandModifier(propertyType);
        T property = builder.getProperty(propertyType);
        commandModifier.accept(property, builder);
    }

    @Override
    public void applyModifiers(CommandParameterBuilder builder) {
        for (Class<?> aClass : parameterPropertyMap.keySet())
            if (aClass != null && builder.hasProperty(aClass))
                applyParameterModifier(aClass, builder);
        applyParameterModifier(null, builder);
    }

    @Override
    public <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType) {
        BiConsumer<?, CommandParameterBuilder> biConsumer = parameterPropertyMap.get(propertyType);
        @SuppressWarnings("unchecked") BiConsumer<T, CommandParameterBuilder> consumer = (BiConsumer<T, CommandParameterBuilder>) biConsumer;
        return consumer;
    }

    @Override
    public <T> void applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder) {
        BiConsumer<T, CommandParameterBuilder> commandModifier = getParameterModifier(propertyType);
        T property = builder.getProperty(propertyType);
        commandModifier.accept(property, builder);
    }


    private <T> void associatePreprocessor(Class<T> propertyType, Function<T, CommandPreprocessor> factory) {
        appendCommandModifier(propertyType, (t, commandHandleBuilder) -> commandHandleBuilder.addPreprocessor(factory.apply(t)));
    }

    @Override
    public <T> void associatePreprocessorFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorFunction> factory) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, factory.apply(o)));
    }

    @Override
    public <T> void associatePreprocessorPredicateFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorPredicate> factory) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, factory.apply(o)));
    }

    @Override
    public void associatePreprocessor(String identifier, Class<?> propertyType, CommandPreprocessorFunction function) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, function));
    }

    @Override
    public void associatePreprocessorPredicate(String identifier, Class<?> propertyType, CommandPreprocessorPredicate predicate) {
        associatePreprocessor(propertyType, o -> new CommandPreprocessor(identifier, predicate));
    }

    @Override
    public List<String> getPreprocessorPriorityList() {
        return preprocessorPriorityList;
    }


    @Override
    public void setPreprocessorPriority(String... identifiers) {
        preprocessorPriorityList = Arrays.asList(identifiers);
    }

    @Override
    public void setPreprocessorPriority(List<String> identifierList) {
        preprocessorPriorityList = identifierList;
    }

    private static CommandPropertyMapImpl createPropertiesForPackage(Package p) {
        final String name = p.getName();
        final int i = name.lastIndexOf('.');
        final CommandPropertyMapImpl map;
        if (i != -1) {
            final String parentPackageName = name.substring(0, i);
            final Package aPackage = Package.getPackage(parentPackageName);
            if (aPackage != null) {
                map = new CommandPropertyMapImpl(getPP(aPackage), p.getAnnotations());
            } else {
                map = new CommandPropertyMapImpl(null, p.getAnnotations());
            }
        } else {
            map = new CommandPropertyMapImpl(null, p.getAnnotations());
        }
        return map;
    }

    public static CommandPropertyMapImpl getPP(Package p) {
        if (p == null) return null;
        return packageMap.computeIfAbsent(p, CommandPropertiesManagerImpl::createPropertiesForPackage);
    }

    private int getPriority(String identifier, List<String> list) {
        final int i = list.indexOf(identifier);
        if (i != -1) return i;
        final int j = list.indexOf(null);
        if (j != -1) return j;
        else return list.size();
    }

    @Override
    public Comparator<CommandPreprocessor> getPriorityComparator() {
        return new PriorityComparator(preprocessorPriorityList);
    }

    public Comparator<CommandPreprocessor> getPreprocessorComparator(String... identifier) {
        return new PriorityComparator(Arrays.asList(identifier));
    }

    public class PriorityComparator implements Comparator<CommandPreprocessor> {

        private final List<String> identifierList;

        public PriorityComparator(List<String> identifierList) {
            this.identifierList = identifierList;
        }

        @Override
        public int compare(CommandPreprocessor o1, CommandPreprocessor o2) {
            return getPriority(o1.getIdentifier(), identifierList) - getPriority(o2.getIdentifier(), identifierList);
        }
    }


}