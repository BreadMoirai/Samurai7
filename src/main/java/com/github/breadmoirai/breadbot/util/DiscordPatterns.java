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

package com.github.breadmoirai.breadbot.util;

import java.util.regex.Pattern;

public class DiscordPatterns {
    public static final Pattern EMOTE_PATTERN = Pattern.compile("<(?:a)?:([^:]+):([0-9]+)>");
    public static final Pattern URL = Pattern.compile("(?:<)?((?:http(s)?://.)?(?:www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b(?:[-a-zA-Z0-9@:%_+.~#?&/=]*))(?:>)?");
    public static final Pattern FORMATTED = Pattern.compile("<[@&!#:0-9a-zA-Z/]*>");
    public static final Pattern USER_MENTION_PREFIX = Pattern.compile("<@(?:!)?([0-9]+)>");
    public static final Pattern HEX = Pattern.compile("^(0x|#)?[0-9A-Fa-f]+$");
    public static final Pattern WHITE_SPACE = Pattern.compile("\\s");
    /**
     * Pattern: {@code [\s+](?=(?:[^"]*"[^"]*")*[^"]*$)}
     * <p>This pattern splits on spaces, ignoring spaces which are enclosed by quotation marks {@code "}.
     * If there is an uneven number of quotation marks, the result is indeterministic.
     */
    public static final Pattern ARGUMENT_SPLITTER = Pattern.compile("(\\s+)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    public static final Pattern FLOAT_REGEX = Pattern.compile("[+-]?(" +
            "NaN|" +
            "Infinity|" +
            "((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)|" +
            "(\\.(\\p{Digit}+)([eE][+-]?(\\p{Digit}+))?)|" +
            "((" +
            "(0[xX](\\p{XDigit}+)(\\.)?)|" +
            "(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))" +
            ")[pP][+-]?(\\p{Digit}+)))" +
            "[fFdD]?))");
}
