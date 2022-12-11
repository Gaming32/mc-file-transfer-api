package io.github.gaming32.filetransferapi.impl;

import io.github.gaming32.filetransferapi.impl.packets.FileTransferPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FileTransferModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
            FileTransferPackets.REQUEST_FILE,
            (server, handler, buf, responseSender) -> FileTransferMod.requestFile(buf, handler)
        );

        ClientPlayNetworking.registerGlobalReceiver(
            FileTransferPackets.TRANSFER_BLOCK,
            (server, handler, buf, responseSender) -> FileTransferMod.transferBlock(buf)
        );

        ClientPlayNetworking.registerGlobalReceiver(
            FileTransferPackets.TRANSFER_CANCEL,
            (server, handler, buf, responseSender) -> FileTransferMod.transferCancel(buf)
        );

        ClientPlayNetworking.registerGlobalReceiver(
            FileTransferPackets.TRANSFER_REQUEST_BLOCK,
            (server, handler, buf, responseSender) -> FileTransferMod.transferRequestBlock(buf)
        );
    }
}
