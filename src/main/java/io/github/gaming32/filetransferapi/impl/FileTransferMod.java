package io.github.gaming32.filetransferapi.impl;

import io.github.gaming32.filetransferapi.api.FileTransferApi;
import io.github.gaming32.filetransferapi.api.PacketSender;
import io.github.gaming32.filetransferapi.api.StreamType;
import io.github.gaming32.filetransferapi.api.TransferConfig;
import io.github.gaming32.filetransferapi.impl.packets.*;
import io.github.gaming32.filetransferapi.util.DoubleEndedStream;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.client.ClientPlayNetworkAddon;
import net.fabricmc.fabric.impl.networking.server.ServerPlayNetworkAddon;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

public class FileTransferMod implements ModInitializer {
    public static final String MOD_ID = "file-transfer-api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final SecureRandom TRANSFER_ID_RANDOM = new SecureRandom();
    private static final LongSet USED_TRANSFER_IDS = new LongOpenHashSet();
    public static final Long2ObjectMap<TransferInputStream> ACTIVE_DOWNLOADS = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectMap<TransferOutputStream> ACTIVE_UPLOADS = new Long2ObjectOpenHashMap<>();

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(
            FileTransferPackets.REQUEST_FILE,
            (server, player, handler, buf, responseSender) -> requestFile(buf, handler)
        );

        ServerPlayNetworking.registerGlobalReceiver(
            FileTransferPackets.TRANSFER_BLOCK,
            (server, player, handler, buf, responseSender) -> transferBlock(buf)
        );

        ServerPlayNetworking.registerGlobalReceiver(
            FileTransferPackets.TRANSFER_CANCEL,
            (server, player, handler, buf, responseSender) -> transferCancel(buf)
        );

        ServerPlayNetworking.registerGlobalReceiver(
            FileTransferPackets.TRANSFER_REQUEST_BLOCK,
            (server, player, handler, buf, responseSender) -> transferRequestBlock(buf)
        );
    }

    static void requestFile(PacketByteBuf buf, PacketSender sender) {
        final RequestFilePacket packet = new RequestFilePacket(buf);
        final var requestHandler = FileTransferApi.getDownloadRequestHandlers().get(packet.file());
        if (requestHandler == null) return;
        final TransferOutputStream out = new TransferOutputStream(packet.transferId(), sender, packet.config().maxBlockSize(), packet.config().streamType() == StreamType.STREAM);
        ACTIVE_UPLOADS.put(packet.transferId(), out);
        requestHandler.accept(new BufferedOutputStream(out, packet.config().maxBlockSize()), packet.config());
    }

    static void transferBlock(PacketByteBuf buf) {
        final TransferBlockPacket packet = new TransferBlockPacket(buf);
        final TransferInputStream stream = ACTIVE_DOWNLOADS.get(packet.transferId());
        if (stream == null) {
            LOGGER.warn("Received file data for closed transfer {}.", Long.toUnsignedString(packet.transferId(), 16));
            return;
        }
        try {
            stream.getIn().write(packet.blockData(), packet.off(), packet.len());
        } catch (IOException e) {
            LOGGER.error("Exception receiving block.", e);
        }
    }

    static void transferCancel(PacketByteBuf buf) {
        final TransferCancelPacket packet = new TransferCancelPacket(buf);
        final TransferInputStream in = ACTIVE_DOWNLOADS.get(packet.transferId());
        if (in != null) {
            in.getIn().closeWrite();
        }
        final TransferOutputStream out = ACTIVE_UPLOADS.get(packet.transferId());
        if (out != null) {
            out.close();
        }
        if (in != null && out != null) {
            LOGGER.warn("Transfer {} was both an upload and a download.", Long.toUnsignedString(packet.transferId(), 16));
        }
    }

    static void transferRequestBlock(PacketByteBuf buf) {
        final TransferRequestBlockPacket packet = new TransferRequestBlockPacket(buf);
        final TransferOutputStream out = ACTIVE_UPLOADS.get(packet.transferId());
        if (out == null) {
            LOGGER.warn("A block was requested from non-existent transfer {}.", Long.toUnsignedString(packet.transferId(), 16));
            return;
        }
        out.notifyAcceptWrite();
    }

    public static long generateTransferId() {
        long id;
        do {
            id = TRANSFER_ID_RANDOM.nextLong();
        } while (id == 0 || !USED_TRANSFER_IDS.add(id));
        return id;
    }

    public static TransferInputStream downloadFile(PacketSender packetSender, Identifier file, TransferConfig config) {
        final long transferId = generateTransferId();
        new RequestFilePacket(transferId, file, config).sendPacket(packetSender);
        final DoubleEndedStream stream = new DoubleEndedStream(config.maxBlockSize());
        final TransferInputStream result = new TransferInputStream(transferId, packetSender, stream, config.streamType());
        ACTIVE_DOWNLOADS.put(transferId, result);
        return result;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static PacketSender convertFapiPacketSender(net.fabricmc.fabric.api.networking.v1.PacketSender packetSender) {
        if (!(packetSender instanceof ServerPlayNetworkAddon) && !(packetSender instanceof ClientPlayNetworkAddon)) {
            throw new IllegalArgumentException(
                "Fabric API PacketSender must be either ServerPlayNetworkAddon or ClientPlayNetworkAddon, but was" +
                    packetSender.getClass().getSimpleName()
            );
        }
        return new PacketSender() {
            @Override
            public void sendPacket(Identifier channel, PacketByteBuf buf) {
                packetSender.sendPacket(channel, buf);
            }

            @Override
            public boolean isClientbound() {
                return packetSender instanceof ServerPlayNetworkAddon;
            }
        };
    }
}
