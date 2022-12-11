package io.github.gaming32.filetransferapi.impl.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record TransferCancelPacket(long transferId) implements FileTransferPacket {
    public TransferCancelPacket {
        if (transferId == 0) {
            throw new IllegalArgumentException("Transfer ID cannot be 0.");
        }
    }

    public TransferCancelPacket(PacketByteBuf buf) {
        this(buf.readLong());
    }

    @Override
    public @NotNull Identifier getChannel() {
        return FileTransferPackets.TRANSFER_CANCEL;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(transferId);
    }
}
