package com.slack.memcached.protocol.text;

import com.slack.memcached.exception.BadDataChunkException;
import com.slack.memcached.protocol.Command;
import com.slack.memcached.protocol.CommandParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

import static com.slack.memcached.protocol.text.SetCommand.SET;

/**
 * Created by jlisam on 4/10/17.
 */
public class SetCommandParser implements CommandParser {

    private static final Logger logger = LoggerFactory.getLogger(SetCommandParser.class);
    @Override
    public Command parse(ByteBuf bufferToParse) throws BadDataChunkException {

        try {
            int remainingCommandLineSize = bufferToParse.bytesBefore((byte) '\r');
            ByteBuf remainingCommandLine = bufferToParse.readSlice(remainingCommandLineSize);

            remainingCommandLine.skipBytes(1);

            ByteBuf key = remainingCommandLine.readRetainedSlice(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1);
            ByteBuf flags = remainingCommandLine.readRetainedSlice(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1);

            // ignore expiration time
            remainingCommandLine.skipBytes(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1);

            int bytesToReadBeforeSPACE = remainingCommandLine.bytesBefore((byte) ' ');
            ByteBuf payloadSize;
            ByteBuf noReply = null;
            if (bytesToReadBeforeSPACE >= 0) {
                payloadSize = remainingCommandLine.readSlice(bytesToReadBeforeSPACE);
                bytesToReadBeforeSPACE = remainingCommandLine.bytesBefore((byte) ' ');
                int bytesToReadBeforeCROrSPACE = Math.max(bytesToReadBeforeSPACE, remainingCommandLine.readableBytes());
                noReply = remainingCommandLine.readSlice(bytesToReadBeforeCROrSPACE);

            } else {
                int bytesToReadBeforeCROrSPACE = Math.max(bytesToReadBeforeSPACE, remainingCommandLine.readableBytes());
                payloadSize = remainingCommandLine.readSlice(bytesToReadBeforeCROrSPACE);
            }

            // next line
            bufferToParse.skipBytes(bufferToParse.bytesBefore((byte) '\r') + 1);
            bufferToParse.skipBytes(bufferToParse.bytesBefore((byte) '\n') + 1);

            int length = Integer.parseInt(payloadSize.toString(Charset.defaultCharset()));

            int payLoadLine = bufferToParse.bytesBefore((byte) '\r');

            if (length != payLoadLine) {
                throw new BadDataChunkException("bad data chunk");
            }
            ByteBuf payload = bufferToParse.retainedSlice(bufferToParse.readerIndex(), length);

            // advance buffer reader position after payload
            bufferToParse.readerIndex(bufferToParse.readerIndex() + payload.readableBytes() + Command.CRLF.length);

            return new SetCommand(key, flags, payload, noReply);
        } catch (Exception e) {
            if (e instanceof BadDataChunkException) {
                throw e;
            } else {
                throw new BadDataChunkException("bad data chunk", e);
            }

        }

    }

    @Override
    public ByteBuf getCommand() {
        return SET;
    }
}
