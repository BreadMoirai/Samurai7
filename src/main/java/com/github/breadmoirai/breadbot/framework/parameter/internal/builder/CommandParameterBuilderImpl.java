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

package com.github.breadmoirai.breadbot.framework.parameter.internal.builder;

import com.github.breadmoirai.breadbot.framework.CommandModule;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandHandleBuilder;
import com.github.breadmoirai.breadbot.framework.builder.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.command.internal.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.error.MissingTypeParserException;
import com.github.breadmoirai.breadbot.framework.parameter.AbsentArgumentHandler;
import com.github.breadmoirai.breadbot.framework.parameter.ArgumentParser;
import com.github.breadmoirai.breadbot.framework.parameter.CommandArgument;
import com.github.breadmoirai.breadbot.framework.parameter.CommandParameter;
import com.github.breadmoirai.breadbot.framework.parameter.TypeParser;
import com.github.breadmoirai.breadbot.framework.parameter.internal.ArgumentParserCollectionImpl;
import com.github.breadmoirai.breadbot.framework.parameter.internal.ArgumentParserImpl;
import com.github.breadmoirai.breadbot.framework.parameter.internal.CommandParameterImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class CommandParameterBuilderImpl implements CommandParameterBuilder {

    private static final List<Class<?>> COLLECTION_TYPES = Arrays.asList(List.class, Deque.class, Queue.class, Stream.class, IntStream.class, LongStream.class, DoubleStream.class);

    private final CommandHandleBuilder commandBuilder;
    private final Parameter parameter;
    private final CommandPropertyMapImpl map;
    private final BreadBotClientBuilder clientBuilder;
    private String paramName;
    private int index = 0;
    private int width = 1;
    private int limit = -1;
    private TypeParser<?> typeParser;
    private ArgumentParser argumentParser;
    private boolean mustBePresent = false;
    private boolean contiguous = false;
    private AbsentArgumentHandler absentArgumentHandler = null;
    private Predicate<CommandArgument> argumentPredicate;
    private String name;

    public CommandParameterBuilderImpl(BreadBotClientBuilder builder, CommandHandleBuilder commandBuilder, Parameter parameter, CommandPropertyMapImpl map) {
        this.commandBuilder = commandBuilder;
        this.parameter = parameter;
        this.clientBuilder = builder;
        if (parameter != null) {
            this.map = new CommandPropertyMapImpl(map, parameter.getAnnotations());
            this.paramName = parameter.getName();
            final Class<?> type = parameter.getType();
            this.typeParser = this.clientBuilder.getTypeParser(type);
            if (CommandModule.class.isAssignableFrom(type)) {
                this.argumentParser = (parameter1, list, parser) -> parser.getEvent().getClient().getModule(type);
            } else {
                this.argumentParser = new ArgumentParserImpl();
            }
            builder.applyTypeModifiers(this);
        } else {
            this.map = null;
        }

        builder.applyPropertyModifiers(this);
    }

    public Class<?> getGenericType() {
        return getActualTypeParameter(getDeclaringParameter());
    }

    private static Class<?> getActualTypeParameter(Parameter parameter) {
        final ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        final Type type = parameterizedType.getActualTypeArguments()[0];
        return ((Class<?>) type);
    }

    @Override
    public CommandParameterBuilder setName(String paramName) {
        this.paramName = paramName;
        return this;
    }

    @Override
    public CommandParameterBuilderImpl setIndex(int index) {
        this.index = index;
        return this;
    }

    @Override
    public CommandParameterBuilderImpl setWidth(int width) {
        this.width = width;
        return this;
    }

    @Override
    public <T> CommandParameterBuilder setTypeParser(TypeParser<T> parser) {
        this.typeParser = parser;
        return this;
    }

    @Override
    public CommandParameterBuilder setParser(ArgumentParser parser) {
        this.argumentParser = parser;
        return this;
    }


    @Override
    public CommandParameterBuilder setRequired(boolean mustBePresent) {
        this.mustBePresent = mustBePresent;
        return this;
    }

    @Override
    public CommandParameterBuilder setOnAbsentArgument(AbsentArgumentHandler onParamNotFound) {
        this.absentArgumentHandler = onParamNotFound;
        return this;
    }

    @Override
    public CommandParameterBuilder setContiguous(boolean isContiguous) {
        this.contiguous = isContiguous;
        return this;
    }

    @Override
    public CommandParameterBuilder addArgumentPredicate(Predicate<CommandArgument> argumentPredicate) {
        if (this.argumentPredicate == null) {
            this.argumentPredicate = argumentPredicate;
        } else {
            this.argumentPredicate = this.argumentPredicate.and(argumentPredicate);
        }
        return this;
    }

    public TypeParser<?> getTypeParser() {
        return typeParser;
    }

    public ArgumentParser getParser() {
        return argumentParser;
    }

    @Override
    public CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    @Override
    public BreadBotClientBuilder getClientBuilder() {
        return clientBuilder;
    }

    @Override
    public CommandHandleBuilder getCommandBuilder() {
        return commandBuilder;
    }

    @Override
    public Parameter getDeclaringParameter() {
        return parameter;
    }

    @Override
    public Method getDeclaringMethod() {
        return getCommandBuilder().getDeclaringMethod();
    }

    @Override
    public <T> T getProperty(Class<T> propertyType) {
        return map != null ? map.getProperty(propertyType) : null;
    }

    @Override
    public boolean hasProperty(Class<?> propertyType) {
        return map != null && map.hasProperty(propertyType);
    }

    @Override
    public CommandParameter build() {
        if (typeParser == null && (argumentParser.getClass() == ArgumentParserImpl.class ||
                argumentParser.getClass() == ArgumentParserCollectionImpl.class)) {
            throw new MissingTypeParserException(this);
        }
        if (argumentPredicate != null && typeParser != null) {
            final TypeParser<?> typeParser = this.typeParser;
            final Predicate<CommandArgument> predicate = this.argumentPredicate;
            this.typeParser = arg -> predicate.test(arg) ? typeParser.parse(arg) : null;
        }
        return new CommandParameterImpl(paramName, parameter, index, width, limit, contiguous, typeParser, argumentParser, mustBePresent, absentArgumentHandler);
    }

    @Override
    public CommandParameterBuilderImpl setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public String getName() {
        return name;
    }

}