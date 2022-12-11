package io.github.gaming32.filetransferapi.mixin.client;

import io.github.gaming32.filetransferapi.api.PacketSender;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler implements PacketSender {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Override
    public void send(Packet<?> packet) {
        sendPacket(packet);
    }

    @Override
    public boolean isClientbound() {
        return false;
    }
}
