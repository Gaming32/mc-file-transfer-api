package io.github.gaming32.filetransferapi.impl.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record TransferBlockPacket(long transferId, byte[] blockData, int off, int len) implements FileTransferPacket {
    public TransferBlockPacket {
        if (transferId == 0) {
            throw new IllegalArgumentException("Transfer ID cannot be 0.");
        }
    }

    public TransferBlockPacket(long transferId, byte[] blockData) {
        this(transferId, blockData, 0, blockData.length);
    }

    public TransferBlockPacket(PacketByteBuf buf) {
        this(buf.readLong(), readRemaining(buf));
    }

    private static byte[] readRemaining(PacketByteBuf buf) {
        final byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);
        return result;
    }

    @Override
    public @NotNull Identifier getChannel() {
        return FileTransferPackets.TRANSFER_BLOCK;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(transferId);
        buf.writeBytes(blockData, off, len);
    }
}
