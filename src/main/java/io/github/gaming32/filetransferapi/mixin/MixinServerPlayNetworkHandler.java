package io.github.gaming32.filetransferapi.mixin;

import io.github.gaming32.filetransferapi.api.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler implements PacketSender {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Override
    public void sendPacket(Identifier channel, PacketByteBuf buf) {
        sendPacket(ServerPlayNetworking.createS2CPacket(channel, buf));
    }

    @Override
    public boolean isClientbound() {
        return true;
    }
}
