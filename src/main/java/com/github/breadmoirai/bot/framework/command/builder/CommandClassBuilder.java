package com.github.breadmoirai.bot.framework.command.builder;

import com.github.breadmoirai.bot.framework.command.*;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessor;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessorFunction;
import com.github.breadmoirai.bot.framework.command.preprocessor.CommandPreprocessorPredicate;
import com.github.breadmoirai.bot.framework.command.property.CommandPackageProperties;
import com.github.breadmoirai.bot.framework.command.property.CommandPropertyMap;
import com.github.breadmoirai.bot.framework.error.CommandInitializationException;
import com.github.breadmoirai.bot.framework.error.NoSuchCommandException;
import com.github.breadmoirai.bot.framework.event.CommandEvent;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CommandClassBuilder extends CommandHandleBuilder {

    private List<CommandHandleBuilder> handleBuilders;
    private boolean isPersistent = false;
    private final Class<?> commandClass;
    private Object obj;

    public CommandClassBuilder(Class<?> commandClass) {
        super(commandClass.getSimpleName(), CommandPackageProperties.getPropertiesForPackage(commandClass.getPackage()), commandClass.getAnnotations());

    }

    public CommandClassBuilder(Object commandObj) {
        this(commandObj.getClass());
        obj = commandObj;
    }

    public Class<?> getCommandClass() {
        return commandClass;
    }

    /**
     * Sets the keys of this command. When the keys is set to {@code null}, if the provided class/object has multiple methods/classes, each one will be registered with their own keys.
     *
     * @param keys a var-arg of String. no spaces plz.
     * @return this obj
     */
    @Override
    public CommandClassBuilder setKeys(String... keys) {
        super.setKeys(keys);
        return this;
    }

    /**
     * This determines whether each execution of this command will use the same obj or instantiate a new obj for each instance.
     * By default this value is set to {@code false}
     *
     * @param isPersistent a boolean
     * @return this obj
     */
    public CommandClassBuilder setPersistent(boolean isPersistent) {
        this.isPersistent = isPersistent;
        return this;
    }

    /**
     * This configures the method with the specified name using the {@link java.util.function.Consumer} provided.
     *
     * @param methodName the name of the method. case-sensitive
     * @param consumer   a consumer that modifies the {@link CommandMethodBuilder}
     * @return this obj
     */
    public CommandClassBuilder configureCommandMethod(String methodName, Consumer<CommandMethodBuilder> consumer) {
        handleBuilders.stream()
                .filter(handleBuilder -> handleBuilder.getName().equals(methodName))
                .filter(obj -> obj instanceof CommandMethodBuilder)
                .map(obj -> ((CommandMethodBuilder) obj))
                .findFirst()
                .orElseThrow(() -> new NoSuchCommandException(methodName))
                .configure(consumer);
        return this;
    }

    /**
     * This configures an inner class with the specified name using the {@link java.util.function.Consumer} provided.
     *
     * @param className the name of the method. case-sensitive
     * @param consumer  a consumer that modifies the {@link InnerCommandBuilder}
     * @return this obj
     */
    public CommandClassBuilder configureCommandClass(String className, Consumer<InnerCommandBuilder> consumer) {
        handleBuilders.stream()
                .filter(handleBuilder -> handleBuilder.getName().equals(className))
                .filter(obj -> obj instanceof InnerCommandBuilder)
                .map(obj -> ((InnerCommandBuilder) obj))
                .findFirst()
                .orElseThrow(() -> new NoSuchCommandException(className))
                .configure(consumer);
        return this;
    }


    @Override
    public String[] getKeys() {
        return super.getKeys() == null ? handleBuilders.stream().map(CommandHandleBuilder::getKeys).flatMap(Arrays::stream).toArray(String[]::new) : super.getKeys();
    }

    @Override
    public CommandClassBuilder putProperty(Object property) {
        super.putProperty(property);
        return this;
    }

    @Override
    public <T> CommandClassBuilder putProperty(Class<? super T> type, T property) {
        super.putProperty(type, property);
        return this;
    }

    @Override
    public CommandClassBuilder setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public CommandHandle build() {
        final HashMap<String, CommandHandle> handleMap = new HashMap<>();
        for (CommandHandleBuilder handleBuilder : handleBuilders) {
            final CommandHandle handle = handleBuilder.build();
            final String[] keys = handle.getKeys();
            for (String key : keys) {
                handleMap.put(key, handle);
            }
        }
        final CommandPropertyMap propertyMap = getPropertyBuilder().build();
        final String[] keys = super.getKeys();
        final List<CommandPreprocessor> preprocessorList = getPreprocessorList();
        final Supplier<Object> supplier;
        if (isPersistent) {
            if (obj != null) {
                supplier = () -> obj;
            } else {
                try {
                    final Object o = commandClass.newInstance();
                    supplier = () -> o;
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new CommandInitializationException("Could not instantiate Command object " + commandClass.getName(), e);
                }
            }
        } else {
            try {
                final MethodHandle constructor = MethodHandles.publicLookup().findConstructor(commandClass, MethodType.methodType(void.class));
                supplier = () -> {
                    try {
                        return constructor.invoke();
                    } catch (Throwable throwable) {
                        LoggerFactory.getLogger("Command").error("Failed to instantiate CommandObject " + commandClass.getName(), throwable);
                        return null;
                    }
                };
            } catch (NoSuchMethodException e) {
                throw new CommandInitializationException("Class " + commandClass.getName() + " is missing a no-arg public constructor.");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return new CommandImpl(getName(), keys, supplier, handleMap, propertyMap, preprocessorList);
    }

    public CommandClassBuilder configure(Consumer<CommandClassBuilder> configurator) {
        configurator.accept(this);
        return this;
    }

    @Override
    public CommandClassBuilder addPreprocessorFunction(String identifier, CommandPreprocessorFunction function) {
        super.addPreprocessorFunction(identifier, function);
        return this;
    }

    @Override
    public CommandClassBuilder addPreprocessorPredicate(String identifier, CommandPreprocessorPredicate predicate) {
        super.addPreprocessorPredicate(identifier, predicate);
        return this;
    }

    @Override
    public CommandClassBuilder addPreprocessors(Iterable<CommandPreprocessor> preprocessors) {
        super.addPreprocessors(preprocessors);
        return this;
    }

    @Override
    public CommandClassBuilder sortPreprocessors() {
        super.sortPreprocessors();
        return this;
    }

    @Override
    public CommandClassBuilder addAssociatedPreprocessors() {
        super.addAssociatedPreprocessors();
        return this;
    }
}