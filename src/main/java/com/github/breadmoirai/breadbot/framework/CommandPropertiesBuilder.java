package com.github.breadmoirai.breadbot.framework;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface CommandPropertiesBuilder<R> {

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier it is overridden.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    <T> R putCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator);

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier, the passed BiConsumer is appended to the end of it so that it is applied to the builder after the existing one has been applied.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    <T> R appendCommandModifier(Class<T> propertyType, BiConsumer<T, CommandHandleBuilder> configurator);

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier it is overridden.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    <T> R putParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator);

    /**
     * The provided {@code configurator} is used to modify commands that possess the specified property.
     * If there is an existing modifier, the passed BiConsumer is appended to the end of it so that it is applied to the builder after the existing one has been applied.
     *
     * @param propertyType the class of the property
     * @param configurator a {@link BiConsumer BiConsumer}.
     *                     The first argument is the property.
     *                     The second argument is the {@link CommandHandleBuilder CommandHandleBuilder} the property is attached to.
     * @param <T>          the propertyType
     */
    <T> R appendParameterModifier(Class<T> propertyType, BiConsumer<T, CommandParameterBuilder> configurator);

    /**
     * Applies modifiers to a CommandHandleBuilder based on whether the handle contains a property.
     *
     * @param builder the builder to modify
     */
    R applyModifiers(CommandHandleBuilder builder);

    /**
     * Retrieves a BiConsumer that is used to modify a CommandHandleBuilder.
     *
     * @param propertyType the class of the Property.
     * @param <T>          the property type.
     * @return a BiConsumer if present, otherwise {@code null}.
     * @see #putCommandModifier(Class, BiConsumer)
     * @see #appendCommandModifier(Class, BiConsumer)
     * @see #applyCommandModifier(Class, CommandHandleBuilder)
     */
    <T> BiConsumer<T, CommandHandleBuilder> getCommandModifier(Class<T> propertyType);

    /**
     * Applies a modifier that is associated with a certain {@code propertyType} to the passed {@code builder}.
     * If a modifier is not found the {@code builder} is not modified.
     *
     * @param propertyType the property class
     * @param builder      the CommandHandleBuilder to be modified
     * @param <T>          the property type
     * @see #putCommandModifier(Class, BiConsumer)
     * @see #appendCommandModifier(Class, BiConsumer)
     * @see #getCommandModifier(Class)
     */
    <T> R applyCommandModifier(Class<T> propertyType, CommandHandleBuilder builder);

    /**
     * Applies modifiers to a CommandParameterBuilder based on whether the handle contains a property.
     *
     * @param builder the builder to modify
     */
    R applyModifiers(CommandParameterBuilder builder);

    /**
     * Retrieves a BiConsumer that is used to modify a CommandParameterBuilder.
     *
     * @param propertyType the class of the Property.
     * @param <T>          the property type.
     * @return a BiConsumer if present, otherwise {@code null}.
     * @see #putParameterModifier(Class, BiConsumer)
     * @see #appendParameterModifier(Class, BiConsumer)
     * @see #applyParameterModifier(Class, CommandParameterBuilder)
     */
    <T> BiConsumer<T, CommandParameterBuilder> getParameterModifier(Class<T> propertyType);

    /**
     * Applies a modifier that is associated with a certain {@code propertyType} to the passed {@code builder}.
     * If a modifier is not found the {@code builder} is not modified.
     *
     * @param propertyType the property class
     * @param builder      the CommandHandleBuilder to be modified
     * @param <T>          the property type
     * @see #putParameterModifier(Class, BiConsumer)
     * @see #appendParameterModifier(Class, BiConsumer)
     * @see #getParameterModifier(Class)
     */
    <T> R applyParameterModifier(Class<T> propertyType, CommandParameterBuilder builder);

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the property.
     *
     * @param identifier   a name for the preprocessor
     * @param propertyType the property class
     * @param factory      a function that generates a preprocessor based upon the value of the property
     * @param <T>          the property type
     */
    <T> R associatePreprocessorFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorFunction> factory);

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the property.
     *
     * @param identifier   a name for the preprocessor
     * @param propertyType the property class
     * @param factory      a function that generates a preprocessor predicate based upon the value of the property
     * @param <T>          the property type
     */
    <T> R associatePreprocessorPredicateFactory(String identifier, Class<T> propertyType, Function<T, CommandPreprocessorPredicate> factory);
    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the property.
     *
     * @param identifier   a name for the preprocessor
     * @param propertyType the property class
     * @param function     a {@link CommandPreprocessorFunction#process CommandPreprocessorFunction}
     */
    R associatePreprocessor(String identifier, Class<?> propertyType, CommandPreprocessorFunction function);

    /**
     * Associates a preprocessor with a property. This does not replace any existing preprocessors associated with the property.
     *
     * @param identifier   a name for the preprocessor
     * @param propertyType the property class
     * @param predicate    a {@link java.util.function.Predicate Predicate}{@literal <}{@link CommandEvent CommandEvent}{@literal >} that returns {@code true} when the command should continue to execute, {@code false} otherwise
     */
    R associatePreprocessorPredicate(String identifier, Class<?> propertyType, CommandPreprocessorPredicate predicate);

    List<String> getPreprocessorPriorityList();

    R setPreprocessorPriority(String... identifiers);

    R setPreprocessorPriority(List<String> identifierList);

    Comparator<CommandPreprocessor> getPriorityComparator();
}
