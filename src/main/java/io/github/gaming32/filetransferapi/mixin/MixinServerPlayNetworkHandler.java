package io.github.gaming32.filetransferapi.mixin;

import io.github.gaming32.filetransferapi.api.PacketSender;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler implements PacketSender {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Override
    public void send(Packet<?> packet) {
        sendPacket(packet);
    }

    @Override
    public boolean isClientbound() {
        return true;
    }
}
