package io.github.gaming32.filetransferapi.api;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface PacketSender {
    void sendPacket(Identifier channel, PacketByteBuf buf);

    boolean isClientbound();
}
