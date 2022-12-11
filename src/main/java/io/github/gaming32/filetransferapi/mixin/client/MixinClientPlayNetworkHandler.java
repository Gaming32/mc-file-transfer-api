package io.github.gaming32.filetransferapi.mixin.client;

import io.github.gaming32.filetransferapi.api.PacketSender;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler implements PacketSender {
    @Shadow public abstract void sendPacket(Packet<?> packet);

    @Override
    public void sendPacket(Identifier channel, PacketByteBuf buf) {
        sendPacket(ClientPlayNetworking.createC2SPacket(channel, buf));
    }

    @Override
    public boolean isClientbound() {
        return false;
    }
}
