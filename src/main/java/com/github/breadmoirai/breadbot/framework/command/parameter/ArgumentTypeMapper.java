package com.github.breadmoirai.breadbot.framework.command.parameter;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * This class provided behavior to map a {@link com.github.breadmoirai.breadbot.framework.command.parameter.CommandArgument} to a specified Type.
 * @param <T> the type to map to.
 */
@FunctionalInterface
public interface ArgumentTypeMapper<T> extends BiFunction<CommandArgument, Integer, Optional<T>> {

    ArgumentTypeMapper<Void> VOID_MAPPER = (arg, flags) -> Optional.empty();

    static <R> ArgumentTypeMapper<R> getEmptyMapper(Class<R> type) {
        return (arg, flags) -> Optional.empty();
    }

    /**
     * Maps / Parses the {@link com.github.breadmoirai.breadbot.framework.command.parameter.CommandArgument} to this given type.
     * This method should never return a null value.
     *
     * @param arg The {@link com.github.breadmoirai.breadbot.framework.command.parameter.CommandArgument} to be mapped.
     * @param flags the flags. See Class Constants*
     *
     * @return {@code !null} value. Must be wrapped in an {@link java.util.Optional}
     */
    @NotNull
    Optional<T> map(CommandArgument arg, int flags);

    /**
     * functional version of this obj
     *
     * @param commandArgument
     * @param integer
     * @return
     */
    @Override
    default Optional<T> apply(CommandArgument commandArgument, Integer integer) {
        return map(commandArgument, integer);
    }
}