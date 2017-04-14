package com.slack.memcached.protocol.text;

import com.slack.memcached.exception.BadDataChunkException;
import com.slack.memcached.protocol.Command;
import com.slack.memcached.protocol.CommandParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

/**
 * Created by jlisam on 4/10/17.
 */
public class CasCommandParser implements CommandParser {

    private static final Logger logger = LoggerFactory.getLogger(CasCommandParser.class);

    @Override
    public Command parse(ByteBuf byteBuf) throws BadDataChunkException {
        try {
            int remainingCommandLineSize = byteBuf.bytesBefore((byte) '\r');
            ByteBuf remainingCommandLine = byteBuf.readSlice(remainingCommandLineSize);

            remainingCommandLine.skipBytes(1);

            ByteBuf key = remainingCommandLine.readRetainedSlice(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1);
            ByteBuf flags = remainingCommandLine.readRetainedSlice(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1);

            // ignore expiration time
            remainingCommandLine.skipBytes(remainingCommandLine.bytesBefore((byte) ' '));
            remainingCommandLine.skipBytes(1);

            int bytesToReadBeforeSPACE = remainingCommandLine.bytesBefore((byte) ' ');
            ByteBuf payloadSize = remainingCommandLine.readSlice(bytesToReadBeforeSPACE);

            bytesToReadBeforeSPACE = remainingCommandLine.bytesBefore((byte) ' ');
            int bytesToReadBeforeCROrSPACE = Math.max(bytesToReadBeforeSPACE, remainingCommandLine.readableBytes());
            ByteBuf cas = remainingCommandLine.readSlice(bytesToReadBeforeCROrSPACE);

            // next line
            byteBuf.skipBytes(byteBuf.bytesBefore((byte) '\r') + 1);
            byteBuf.skipBytes(byteBuf.bytesBefore((byte) '\n') + 1);

            int length = Integer.parseInt(payloadSize.toString(Charset.defaultCharset()));

            int payLoadLine = byteBuf.bytesBefore((byte) '\r');

            // memcached complains when lenght provided is different than actual length of payload
            if (length != payLoadLine) {
                throw new BadDataChunkException("bad data chunk");
            }
            ByteBuf payload = byteBuf.retainedSlice(byteBuf.readerIndex(), length);

            // advance buffer reader position after payload
            byteBuf.readerIndex(byteBuf.readerIndex() + payload.readableBytes() + Command.CRLF.length);

            return new CasCommand(key, flags, Long.valueOf(cas.toString(Charset.defaultCharset()).trim()), payload);
        } catch (Exception e) {
            logger.error("Something went wrong", e);
            throw e;
        }

    }

    @Override
    public ByteBuf getCommand() {
        return CasCommand.CAS;
    }
}
