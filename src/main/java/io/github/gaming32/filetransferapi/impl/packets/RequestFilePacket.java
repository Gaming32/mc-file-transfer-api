package io.github.gaming32.filetransferapi.impl.packets;

import io.github.gaming32.filetransferapi.api.TransferConfig;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record RequestFilePacket(long transferId, Identifier file, TransferConfig config) implements FileTransferPacket {
    public RequestFilePacket(PacketByteBuf buf) {
        this(buf.readLong(), buf.readIdentifier(), new TransferConfig(buf));
    }

    @Override
    public @NotNull Identifier getChannel() {
        return FileTransferPackets.REQUEST_FILE;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeLong(transferId);
        buf.writeIdentifier(file);
        config.write(buf);
    }
}
