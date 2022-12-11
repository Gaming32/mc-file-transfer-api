package io.github.gaming32.filetransferapi.impl.packets;

import io.github.gaming32.filetransferapi.api.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface FileTransferPacket {
    @NotNull
    Identifier getChannel();

    @ApiStatus.OverrideOnly
    void write(PacketByteBuf buf);

    @ApiStatus.NonExtendable
    default void sendPacket(PacketSender sender) {
        final PacketByteBuf buf = PacketByteBufs.create();
        write(buf);
        sender.sendPacket(getChannel(), buf);
    }
}
