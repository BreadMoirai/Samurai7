package com.github.breadmoirai.bot.framework.arg;

import com.github.breadmoirai.bot.framework.arg.impl.ArgumentParameterImpl;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import javafx.beans.value.ChangeListener;

import java.util.function.BiConsumer;
import java.util.function.ObjDoubleConsumer;

public class CommandParameterBuilder {
    private Class<?> intendedType;
    private int flags = 0;
    private int[] indexes = null;
    private Class<?> type;
    private ArgumentMapper<?> mapper;
    private boolean mustBePresent = false;
    private BiConsumer<CommandEvent, CommandParameter> onParamNotFound = null;

    /**
     * Sets the actual type of the parameter. This also determines the search criteria for this parameter. If this is not set, it will be the same as the base type.
     * The argument passed should be of one of the following types
     * <ul>
     *     <li>Base Type</li>
     *         <p> - if the index is not set, it will be the first argument that matches the BaseType.
     *     <li>{@link com.github.breadmoirai.bot.framework.arg.CommandArgument CommandArgument.class}</li>
     *         <p> - if the index is not set, it will be the first argument that matches the BaseType.
     *         <p> - if the argument passed to this parameter is {@code not-null},
     *     it is guaranteed that <code>{@link com.github.breadmoirai.bot.framework.arg.CommandArgument arg}.{@link com.github.breadmoirai.bot.framework.arg.CommandArgument#getAsType(Class) getAsType}(baseType).{@link java.util.Optional#isPresent() isPresent()}</code> returns {@code true}.
     *     <li>List.class</li>
     * </ul>
     *
     * @param intendedType
     *
     * @return
     */
    public CommandParameterBuilder setIntendedType(Class<?> intendedType) {
        this.intendedType = intendedType;
        return this;
        
    }

    /**
     * Sets the flags to be passed to the {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper}
     *
     * @param flags
     */
    public CommandParameterBuilder setFlags(int flags) {
        this.flags = flags;
        return this;
    }

    /**
     * Setting this value indicates that this {@link com.github.breadmoirai.bot.framework.arg.CommandParameter} should only map the {@link com.github.breadmoirai.bot.framework.arg.CommandArgument} at the specified index.
     * If multiple indexes are passed, this will combine the specified indexes into a single argument and attempt to map from that.
     *
     * @param indexes index of the argument beginning at 0.
     */
    public CommandParameterBuilder setIndex(int[] indexes) {
        this.indexes = indexes;
        return this;
    }

    /**
     * Sets the intended base type of the method. \\todo make a wiki
     *
     * @param type the Class of the argument.
     */
    public <T> CommandParameterBuilder setBaseType(Class<T> type) {
        return setBaseType(type, ArgumentTypes.getMapper(type));
    }

    /**
     * Sets the {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper} to be used in mapping the {@link CommandParameter}.
     * If an {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper} is registered in {@link com.github.breadmoirai.bot.framework.arg.ArgumentTypes}, it will not be used.
     * The provided {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper} will not be registered with {@link com.github.breadmoirai.bot.framework.arg.ArgumentTypes}.
     * It is generally recommended to prefer using different {@link com.github.breadmoirai.bot.framework.arg.ArgumentFlags flags} on custom types to indicate that the {@link com.github.breadmoirai.bot.framework.arg.CommandArgument} should be mapped differently.
     *
     * @param mapper a public class that implements {@link com.github.breadmoirai.bot.framework.arg.ArgumentMapper} and contains a no-args public constructor.
     */
    public <T> CommandParameterBuilder setBaseType(Class<T> type, ArgumentMapper<T> mapper) {
        this.type = type;
        this.mapper = mapper;
        return this;
    }

    /**
     * @param mustBePresent {@code true} if the argument must be present. Otherwise an error message will be sent to the user with the default error or
     */
    public CommandParameterBuilder setOptional(boolean mustBePresent) {
        this.mustBePresent = mustBePresent;
        return this;
    }

    public CommandParameterBuilder setOnParamNotFound(BiConsumer<CommandEvent, CommandParameter> onParamNotFound) {
        this.onParamNotFound = onParamNotFound;
        return this;
    }

    public ArgumentParameterImpl createCommandArgumentParameter() {
        if (mapper == null) mapper = ArgumentTypes.getMapper(type);
        return new ArgumentParameterImpl(type, intendedType, flags, indexes, mapper, mustBePresent, onParamNotFound);
    }





}
