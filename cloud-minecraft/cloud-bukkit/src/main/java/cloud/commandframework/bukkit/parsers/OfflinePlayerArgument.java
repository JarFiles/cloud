//
// MIT License
//
// Copyright (c) 2020 Alexander Söderberg & Contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package cloud.commandframework.bukkit.parsers;

import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

/**
 * Argument type that parses into {@link OfflinePlayer}. This is not thread safe. This
 * may also result in a blocking request to get the UUID for the offline player.
 * <p>
 * Avoid using this type if possible.
 *
 * @param <C> Command sender type
 */
@SuppressWarnings("unused")
public final class OfflinePlayerArgument<C> extends CommandArgument<C, OfflinePlayer> {

    private OfflinePlayerArgument(final boolean required,
                                  final @NonNull String name,
                                  final @NonNull String defaultValue,
                                  final @Nullable BiFunction<@NonNull CommandContext<C>, @NonNull String,
                                          @NonNull List<@NonNull String>> suggestionsProvider) {
        super(required, name, new OfflinePlayerParser<>(), defaultValue, OfflinePlayer.class, suggestionsProvider);
    }

    /**
     * Create a new builder
     *
     * @param name Name of the component
     * @param <C>  Command sender type
     * @return Created builder
     */
    public static <C> @NonNull Builder<C> newBuilder(final @NonNull String name) {
        return new Builder<>(name);
    }

    /**
     * Create a new required command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, OfflinePlayer> of(final @NonNull String name) {
        return OfflinePlayerArgument.<C>newBuilder(name).asRequired().build();
    }

    /**
     * Create a new optional command component
     *
     * @param name Component name
     * @param <C>  Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, OfflinePlayer> optional(final @NonNull String name) {
        return OfflinePlayerArgument.<C>newBuilder(name).asOptional().build();
    }

    /**
     * Create a new required command component with a default value
     *
     * @param name          Component name
     * @param defaultPlayer Default player
     * @param <C>           Command sender type
     * @return Created component
     */
    public static <C> @NonNull CommandArgument<C, OfflinePlayer> optional(final @NonNull String name,
                                                                          final @NonNull String defaultPlayer) {
        return OfflinePlayerArgument.<C>newBuilder(name).asOptionalWithDefault(defaultPlayer).build();
    }


    public static final class Builder<C> extends CommandArgument.Builder<C, OfflinePlayer> {

        protected Builder(final @NonNull String name) {
            super(OfflinePlayer.class, name);
        }

        /**
         * Builder a new boolean component
         *
         * @return Constructed component
         */
        @Override
        public @NonNull OfflinePlayerArgument<C> build() {
            return new OfflinePlayerArgument<>(this.isRequired(), this.getName(), this.getDefaultValue(),
                                               this.getSuggestionsProvider());
        }

    }


    public static final class OfflinePlayerParser<C> implements ArgumentParser<C, OfflinePlayer> {

        @Override
        public @NonNull ArgumentParseResult<OfflinePlayer> parse(final @NonNull CommandContext<C> commandContext,
                                                        final @NonNull Queue<String> inputQueue) {
            final String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NullPointerException("No input was provided"));
            }
            inputQueue.remove();

            //noinspection deprecation
            OfflinePlayer player = Bukkit.getOfflinePlayer(input);

            if (player == null || (!player.hasPlayedBefore() && !player.isOnline())) {
                return ArgumentParseResult.failure(new OfflinePlayerParseException(input));
            }

            return ArgumentParseResult.success(player);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(final @NonNull CommandContext<C> commandContext,
                                                          final @NonNull String input) {
            List<String> output = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                output.add(player.getName());
            }

            return output;
        }
    }


    /**
     * OfflinePlayer parse exception
     */
    public static final class OfflinePlayerParseException extends IllegalArgumentException {

        private final String input;

        /**
         * Construct a new OfflinePlayer parse exception
         *
         * @param input String input
         */
        public OfflinePlayerParseException(final @NonNull String input) {
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NonNull String getInput() {
            return input;
        }

        @Override
        public String getMessage() {
            return String.format("No player found for input '%s'.", input);
        }
    }
}
