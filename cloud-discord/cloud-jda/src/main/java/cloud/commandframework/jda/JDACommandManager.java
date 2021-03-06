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
package cloud.commandframework.jda;

import cloud.commandframework.CommandManager;
import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.internal.CommandRegistrationHandler;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Function;

/**
 * Command manager for use with JDA
 *
 * @param <C> Command sender type
 */
public class JDACommandManager<C> extends CommandManager<C> {
    private final long botId;

    private final Function<@NonNull C, @NonNull String> prefixMapper;
    private final Function<@NonNull MessageReceivedEvent, @NonNull C> commandSenderMapper;
    private final Function<@NonNull C, @NonNull MessageReceivedEvent> backwardsCommandSenderMapper;

    /**
     * final
     * Construct a new JDA Command Manager
     *
     * @param jda                          JDA instance to register against
     * @param prefixMapper                 Function that maps the sender to a command prefix string
     * @param commandExecutionCoordinator  Coordination provider
     * @param commandSenderMapper          Function that maps {@link MessageReceivedEvent} to the command sender type
     * @param backwardsCommandSenderMapper Function that maps the command sender type to {@link MessageReceivedEvent}
     * @throws InterruptedException If the jda instance does not ready correctly
     */
    public JDACommandManager(final @NonNull JDA jda,
                             final @NonNull Function<@NonNull C, @NonNull String> prefixMapper,
                             final @NonNull Function<CommandTree<C>, CommandExecutionCoordinator<C>> commandExecutionCoordinator,
                             final @NonNull Function<@NonNull MessageReceivedEvent, @NonNull C> commandSenderMapper,
                             final @NonNull Function<@NonNull C, @NonNull MessageReceivedEvent> backwardsCommandSenderMapper)
            throws InterruptedException {
        super(commandExecutionCoordinator, CommandRegistrationHandler.nullCommandRegistrationHandler());
        this.prefixMapper = prefixMapper;
        this.commandSenderMapper = commandSenderMapper;
        this.backwardsCommandSenderMapper = backwardsCommandSenderMapper;
        jda.addEventListener(new JDACommandListener<>(this));
        jda.awaitReady();
        this.botId = jda.getSelfUser().getIdLong();
    }

    /**
     * Get the prefix mapper
     *
     * @return Prefix mapper
     */
    public final @NonNull Function<C, String> getPrefixMapper() {
        return prefixMapper;
    }

    /**
     * Get the command sender mapper
     *
     * @return Command sender mapper
     */
    public final @NonNull Function<@NonNull MessageReceivedEvent, @NonNull C> getCommandSenderMapper() {
        return this.commandSenderMapper;
    }

    /**
     * Get the bots discord id
     *
     * @return Bots discord id
     */
    public final long getBotId() {
        return botId;
    }

    @Override
    public final boolean hasPermission(final @NonNull C sender, final @NonNull String permission) {
        if (permission.isEmpty()) {
            return true;
        }

        MessageReceivedEvent message = backwardsCommandSenderMapper.apply(sender);
        Member member = message.getMember();
        if (member == null) {
            return false;
        }

        return member.hasPermission(Permission.valueOf(permission));
    }

    @Override
    public final @NonNull CommandMeta createDefaultCommandMeta() {
        return SimpleCommandMeta.empty();
    }
}
