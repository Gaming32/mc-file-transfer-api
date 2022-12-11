package io.github.gaming32.filetransferapi.api;

import net.minecraft.network.Packet;

public interface PacketSender {
    void send(Packet<?> packet);

    boolean isClientbound();
}
