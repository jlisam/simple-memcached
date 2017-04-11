package com.slack.memcached.memory;

import io.netty.buffer.ByteBuf;

public class CachedValue {

    private ByteBuf flags;
    private ByteBuf value;
    private long cas;

    public CachedValue(ByteBuf flags, ByteBuf value) {
        this.flags = flags;
        this.value = value;
    }

    public CachedValue(ByteBuf flags, ByteBuf value, long cas) {
        this.flags = flags;
        this.value = value;
        this.cas = cas;
    }

    public ByteBuf getFlags() {
        return flags;
    }

    public ByteBuf getValue() {
        return value;
    }

    public long getCas() {
        return cas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CachedValue that = (CachedValue) o;

        if (cas != that.cas) return false;
        if (flags != null ? !flags.equals(that.flags) : that.flags != null) return false;
        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = flags != null ? flags.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (int) (cas ^ (cas >>> 32));
        return result;
    }
}
