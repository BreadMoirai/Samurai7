package com.github.breadmoirai.breadbot.framework.internal.command.builder;

import com.github.breadmoirai.breadbot.framework.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.CommandParameterBuilder;
import com.github.breadmoirai.breadbot.framework.CommandPropertyMap;
import com.github.breadmoirai.breadbot.framework.error.MissingTypeMapperException;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandPropertyMapImpl;
import com.github.breadmoirai.breadbot.framework.internal.parameter.*;

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandParameterBuilderImpl implements CommandParameterBuilder {
    private final Parameter parameter;
    private String methodName;
    private final CommandPropertyMapImpl map;
    private String paramName;
    private final Class<?> paramType;
    private Function<Class<?>, Collector<?, ?, ?>> collectorSupplier;
    private int flags = 0, index = -1, width = 1;
    private Class<?> type;
    private ArgumentParser<?> parser;
    private boolean mustBePresent = false;
    private boolean contiguous = false;
    private MissingArgumentHandler onParamNotFound = null;
    private final BreadBotClientBuilder clientBuilder;

    public CommandParameterBuilderImpl(BreadBotClientBuilder builder, Parameter parameter, String methodName, CommandPropertyMap map) {
        this.parameter = parameter;
        this.map = new CommandPropertyMapImpl(map, parameter.getAnnotations());
        this.paramName = parameter.getName();
        this.paramType = parameter.getType();
        this.methodName = methodName;
        this.clientBuilder = builder;
        if (paramType == List.class) {
            collectorSupplier = getToList();
            setActualTypeParameter(parameter);
        } else if (paramType == Stream.class) {
            collectorSupplier = getToStream();
            setActualTypeParameter(parameter);
        } else if (paramType == Deque.class || paramType == Queue.class) {
            collectorSupplier = getToDeque();
            setActualTypeParameter(parameter);
//      } else if (paramType == Optional.class) {
//            setActualTypeParameter(parameter);
        } else

        {
            type = paramType;
        }

        parser = clientBuilder.getParser(type);
        builder.applyModifiers(this);
    }

    private void setActualTypeParameter(Parameter parameter) {
        final ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
        this.type = (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }

    private <T> Function<Class<?>, Collector<?, ?, ?>> getToList() {
        final Function<Class<T>, Collector<T, ?, List<T>>> f = tClass -> Collectors.toList();
        @SuppressWarnings("unchecked") final Function<Class<?>, Collector<?, ?, ?>> f2 = (Function<Class<?>, Collector<?, ?, ?>>) (Function<?, ?>) f;
        return f2;
    }

    private <T> Function<Class<?>, Collector<?, ?, ?>> getToStream() {
        final Function<Class<T>, Collector<T, Stream.Builder<T>, Stream<T>>> f = tClass -> Collector.of(Stream::builder, Stream.Builder::accept, (tBuilder, tBuilder2) -> {
            tBuilder2.build().forEach(tBuilder);
            return tBuilder;
        }, Stream.Builder::build);
        @SuppressWarnings("unchecked") final Function<Class<?>, Collector<?, ?, ?>> f2 = (Function<Class<?>, Collector<?, ?, ?>>) (Function<?, ?>) f;
        return f2;
    }

    private <T> Function<Class<?>, Collector<?, ?, ?>> getToDeque() {
        final Function<Class<T>, Collector<T, ?, Deque<T>>> f = tClass -> Collectors.toCollection(ArrayDeque::new);
        @SuppressWarnings("unchecked") final Function<Class<?>, Collector<?, ?, ?>> f2 = (Function<Class<?>, Collector<?, ?, ?>>) (Function<?, ?>) f;
        return f2;
    }

    CommandParameterBuilder setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    @Override
    public CommandParameterBuilder setName(String paramName) {
        this.paramName = paramName;
        return this;
    }
    //        return this;
    //        this.paramType = paramType;
    //    public CommandParameterBuilder setIntendedType(Class<?> paramType) {
    //     */
    //     * @return
    //     *
    //     * @param paramType
    //     *
    //     * </ul>
    //     *     <li>List.class</li>
    //     *     it is guaranteed that <code>{@link com.github.breadmoirai.bot.framework.command.arg.CommandArgument arg}.{@link com.github.breadmoirai.bot.framework.command.arg.CommandArgument#getAsType(Class) getAsType}(baseType).{@link java.util.Optional#isPresent() isPresent()}</code> returns {@code true}.
    //     *         <p> - if the argument passed to this parameter is {@code not-null},
    //     *         <p> - if the index is not set, it will be the first argument that matches the BaseType.
    //     *     <li>{@link com.github.breadmoirai.bot.framework.command.arg.CommandArgument CommandArgument.class}</li>
    //     *         <p> - if the index is not set, it will be the first argument that matches the BaseType.
    //     *     <li>Base Type</li>
    //     * <ul>
    //     * The argument passed should be of one of the following types
    //     * Sets the actual type of the parameter. This also determines the search criteria for this parameter. If this is not set, it will be the same as the base type.
//    /**

//    }

    @Override
    public CommandParameterBuilder setFlags(int flags) {
        this.flags = flags;
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
    public <T> CommandParameterBuilder setBaseType(Class<T> type) {
        return setBaseType(type, clientBuilder.getParser(type));
    }

    @Override
    public <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        this.type = type;
        this.parser = new ArgumentParser<>(predicate, mapper);
        return this;
    }

    @Override
    public <T> CommandParameterBuilder setParser(ArgumentTypePredicate predicate, ArgumentTypeMapper<T> mapper) {
        this.parser = new ArgumentParser<T>(predicate, mapper);
        return this;
    }

    @Override
    public CommandParameterBuilder setRequired(boolean mustBePresent) {
        this.mustBePresent = mustBePresent;
        return this;
    }

    @Override
    public CommandParameterBuilder setOnParamNotFound(MissingArgumentHandler onParamNotFound) {
        this.onParamNotFound = onParamNotFound;
        return this;
    }

    @Override
    public CommandParameterBuilder setContiguous(boolean isContiguous) {
        this.contiguous = isContiguous;
        return this;
    }

    @Override
    public ArgumentParser<?> getParser() {
        return parser;
    }

    @Override
    public CommandParameterBuilder configure(Consumer<CommandParameterBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    @Override
    public Parameter getDeclaringParameter() {
        return parameter;
    }

    @Override
    public <T> T getProperty(Class<T> propertyType) {
        return map.getProperty(propertyType);
    }

    @Override
    public boolean hasProperty(Class<?> propertyType) {
        return map.hasProperty(propertyType);
    }

    @Override
    public CommandParameter build() {
        if (parser == null) throw new MissingTypeMapperException(methodName, paramName);
        ArgumentTypeMapper<?> mapper = parser;
        if (paramType == CommandArgument.class) {
            mapper = (arg, flags1) -> parser.test(arg, flags1) ? Optional.of(arg) : Optional.empty();
        }
        final CommandParameterImpl commandParameter = new CommandParameterImpl(type, flags, index, width, mapper, mustBePresent, onParamNotFound);
        if (collectorSupplier != null) {
            @SuppressWarnings("unchecked") final Collector<Object, Object, Object> collector = (Collector<Object, Object, Object>) collectorSupplier.apply(commandParameter.getType());
            return new CommandParameterCollectionImpl(commandParameter, collector, contiguous);
        }
        return commandParameter;
    }
}