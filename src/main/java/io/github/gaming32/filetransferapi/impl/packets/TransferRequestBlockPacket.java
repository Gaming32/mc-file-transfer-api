package io.github.gaming32.filetransferapi.impl.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record TransferRequestBlockPacket(long transferId) implements FileTransferPacket {
    public TransferRequestBlockPacket {
        if (transferId == 0) {
            throw new IllegalArgumentException("Transfer ID cannot be 0.");
        }
    }

    public TransferRequestBlockPacket(PacketByteBuf buf) {
        this(buf.readLong());
    }

    @Override
    public @NotNull Identifier getChannel() {
        return FileTransferPackets.TRANSFER_REQUEST_BLOCK;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(transferId);
    }
}
