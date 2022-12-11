package io.github.gaming32.filetransferapi.api;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record TransferConfig(int maxBlockSize, @NotNull StreamType streamType) {
    public static final int GOOD_FROM_SERVER_BLOCK_SIZE = 1000 * 1024;
    public static final int GOOD_FROM_CLIENT_BLOCK_SIZE = 30 * 1024;
    public static final int MAX_BLOCK_SIZE = 1024 * 1024 - 8;

    public static final TransferConfig DEFAULT_FROM_SERVER = TransferConfig.builder()
        .maxBlockSize(GOOD_FROM_SERVER_BLOCK_SIZE)
        .build();
    public static final TransferConfig DEFAULT_FROM_CLIENT = TransferConfig.builder()
        .maxBlockSize(GOOD_FROM_CLIENT_BLOCK_SIZE)
        .build();

    public TransferConfig {
        validateMaxBlockSize(maxBlockSize);
        Objects.requireNonNull(streamType);
    }

    private static int validateMaxBlockSize(int maxBlockSize) {
        if (maxBlockSize < 0) {
            throw new IllegalArgumentException("Block size cannot be less than 0.");
        }
        if (maxBlockSize > MAX_BLOCK_SIZE) {
            throw new IllegalArgumentException("Block size cannot exceed MAX_BLOCK_SIZE.");
        }
        return maxBlockSize;
    }

    @ApiStatus.Internal
    public TransferConfig(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readEnumConstant(StreamType.class));
    }

    @ApiStatus.Internal
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(maxBlockSize);
        buf.writeEnumConstant(streamType);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int maxBlockSize = GOOD_FROM_CLIENT_BLOCK_SIZE;
        private StreamType streamType = StreamType.BUFFER;

        private Builder() {
        }

        public Builder maxBlockSize(int maxBlockSize) {
            this.maxBlockSize = validateMaxBlockSize(maxBlockSize);
            return this;
        }

        public Builder streamType(@NotNull StreamType streamType) {
            this.streamType = Objects.requireNonNull(streamType);
            return this;
        }

        public TransferConfig build() {
            return new TransferConfig(maxBlockSize, streamType);
        }
    }
}
