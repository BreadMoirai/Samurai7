/*
 *        Copyright 2017 Ton Ly (BreadMoirai)
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

package com.github.breadmoirai.tests;

import com.github.breadmoirai.breadbot.framework.BreadBotClient;
import com.github.breadmoirai.breadbot.framework.builder.BreadBotClientBuilder;
import com.github.breadmoirai.breadbot.framework.command.CommandPreprocessor;
import com.github.breadmoirai.breadbot.framework.error.DuplicateCommandKeyException;
import com.github.breadmoirai.breadbot.framework.error.MissingCommandKeyException;
import com.github.breadmoirai.breadbot.framework.internal.command.CommandObjectFactory;
import com.github.breadmoirai.breadbot.framework.internal.event.CommandEventInternal;
import com.github.breadmoirai.tests.commands.BadCommand;
import com.github.breadmoirai.tests.commands.PingCommand;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;
import uk.org.lidalia.slf4jtest.TestLoggerFactoryResetRule;

import static com.github.breadmoirai.tests.MockFactory.mockCommand;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExceptionTester {

    @Rule
    public TestLoggerFactoryResetRule testLoggerFactoryResetRule = new TestLoggerFactoryResetRule();

    static {
        TestLoggerFactory.getInstance().setPrintLevel(Level.INFO);
    }

    @Test(expected = MissingCommandKeyException.class)
    public void missingKey() {
        new BreadBotClientBuilder()
                .addCommand(commandEvent -> {}, configurator -> {})
                .build();
    }

    @Test(expected = DuplicateCommandKeyException.class)
    public void duplicateKey() {
        new BreadBotClientBuilder()
                .addCommand(PingCommand.class)
                .addCommand(PingCommand::new)
                .build();
    }

    @Test(expected = RuntimeException.class)
    public void commandctorBuild() {
        BadCommand.ctorException = true;
        new BreadBotClientBuilder()
                .addCommand(BadCommand::new)
                .build();
    }

    @Test
    public void commandctorInvoke() {
        TestLogger testLogger = TestLoggerFactory.getTestLogger(CommandObjectFactory.class);
        testLogger.setEnabledLevels(Level.ERROR);

        BadCommand.ctorException = true;
        BreadBotClient bread = new BreadBotClientBuilder()
                .addCommand(BadCommand.class)
                .build();
        CommandEventInternal mock = mockCommand(bread, "!bad");
        bread.getCommandEngine().handle(mock);

        ImmutableList<LoggingEvent> logs = testLogger.getLoggingEvents();
        assertEquals(1, logs.size());
        LoggingEvent loggingEvent = logs.get(0);
        assertEquals("An error occurred while attempting to retrieve an instance of a command object", loggingEvent.getMessage());
        Optional<Throwable> cause = loggingEvent.getThrowable();
        assertTrue(cause.isPresent());
        assertEquals("Constructor Exception", cause.get().getMessage());
    }

    @Test
    public void preprocessorTest() {
        TestLogger testLogger = TestLoggerFactory.getTestLogger(CommandPreprocessor.class);
        testLogger.setEnabledLevels(Level.ERROR);

        BadCommand.preException = true;
        BreadBotClient bread = new BreadBotClientBuilder()
                .createCommand(BadCommand.class)
                .getClientBuilder().build();

        CommandEventInternal mock = mockCommand(bread, "!bad");
        bread.getCommandEngine().handle(mock);

        ImmutableList<LoggingEvent> logs = testLogger.getLoggingEvents();
        assertEquals(1, logs.size());
        LoggingEvent loggingEvent = logs.get(0);
        assertEquals("An exception was thrown while attempting to evaluate a preprocessor: thrower", loggingEvent.getMessage());
        Optional<Throwable> cause = loggingEvent.getThrowable();
        assertTrue(cause.isPresent());
        assertEquals("Preprocessor Exception", cause.get().getMessage());
    }
}