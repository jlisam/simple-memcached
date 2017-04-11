package com.slack.memcached.protocol.text;

import org.junit.Test;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MemcacheDecoderTests {

    @Test
    public void decodeGetOperationShouldSucceed() {
        ByteBuf getOperation = Unpooled.copiedBuffer("get someKey\r\n".getBytes(Charset.defaultCharset()));
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new CommandDecoder());
        embeddedChannel.writeInbound(getOperation);
        embeddedChannel.finish();

        Object decodedObj = embeddedChannel.readInbound();
        System.out.println(decodedObj);
        assertTrue(decodedObj instanceof GetCommand);

        ByteBuf[] keys = ((GetCommand) decodedObj).getKeys();
        assertEquals(keys[0].toString(Charset.defaultCharset()), "someKey");
    }

    @Test
    public void decodeGetOperationWithMultipleKeysShouldSucceed() {
        ByteBuf getOperation = Unpooled.copiedBuffer("get someKey someKey2 someKey3\r\n".getBytes(Charset.defaultCharset()));
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new CommandDecoder());
        embeddedChannel.writeInbound(getOperation);
        embeddedChannel.finish();

        Object decodedObj = embeddedChannel.readInbound();
        System.out.println(decodedObj);
        assertTrue(decodedObj instanceof GetCommand);

        ByteBuf[] keys = ((GetCommand) decodedObj).getKeys();
        assertEquals(keys[0].toString(Charset.defaultCharset()), "someKey");
        assertEquals(keys[1].toString(Charset.defaultCharset()), "someKey2");
        assertEquals(keys[2].toString(Charset.defaultCharset()), "someKey3");
    }

    @Test
    public void decodeSetOperationShouldSucceed() {
        ByteBuf setOp = Unpooled.copiedBuffer(("set someKey 1 999 5\r\n" +
                "hello\r\n").getBytes(Charset.defaultCharset()));
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new CommandDecoder());
        embeddedChannel.writeInbound(setOp);
        embeddedChannel.finish();

        Object decodedObj = embeddedChannel.readInbound();
        assertTrue(decodedObj instanceof SetCommand);

        ByteBuf key = ((SetCommand) decodedObj).getKey();
        assertEquals(key.toString(Charset.defaultCharset()), "someKey");
        ByteBuf flags = ((SetCommand) decodedObj).getFlags();
        assertEquals(flags.toString(Charset.defaultCharset()), "1");
    }

    @Test(expected = Exception.class)
    public void decodeMalformedOperationShouldThrowException() {
        ByteBuf casOp = Unpooled.copiedBuffer(("cas hello 1 999 5\r\n" +
                "hello\r\n").getBytes(Charset.defaultCharset()));
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new CommandDecoder());
        embeddedChannel.writeInbound(casOp);
    }

    @Test
    public void decodeUnknownOperationShouldDoNothing() {
        ByteBuf john = Unpooled.copiedBuffer(("john hello").getBytes(Charset.defaultCharset()));
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new CommandDecoder());
        embeddedChannel.writeInbound(john);
        assertNull(embeddedChannel.readInbound());
    }
}
