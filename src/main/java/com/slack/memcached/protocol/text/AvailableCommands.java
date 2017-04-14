package com.slack.memcached.protocol.text;

import com.slack.memcached.protocol.Command;
import com.slack.memcached.protocol.CommandParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.netty.buffer.ByteBuf;


class AvailableCommands {
    private final Map<ByteBuf, CommandParser> commands = new HashMap<>();

    public AvailableCommands add(CommandParser value) {
        commands.put(value.getCommand(), value);
        return this;
    }

    public Optional<Command> tryParse(ByteBuf command, ByteBuf buffer) {
        try{
            return Optional.ofNullable(commands.getOrDefault(command, new DummyCommandParser())
                    .parse(buffer));
        } finally {
            command.release();
        }
    }
}
