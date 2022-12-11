package io.github.gaming32.filetransferapi.api;

import io.github.gaming32.filetransferapi.impl.FileTransferMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class FileTransferApi {
    private static final Map<Identifier, BiConsumer<OutputStream, TransferConfig>> DOWNLOAD_REQUEST_HANDLERS = new LinkedHashMap<>();

    private FileTransferApi() {
        throw new AssertionError();
    }

    public static void registerDownloadRequestHandler(
        @NotNull Identifier file,
        @NotNull Supplier<InputStream> handler
    ) {
        registerDownloadRequestHandler(file, (output, config) -> {
            final InputStream is = handler.get();
            CompletableFuture.runAsync(() -> {
                try (is; output) {
                    is.transferTo(output);
                    output.flush();
                } catch (IOException e) {
                    FileTransferMod.LOGGER.warn("IOException while transferring " + file + " (" + is + ")", e);
                }
            });
        });
    }

    public static void registerDownloadRequestHandler(
        @NotNull Identifier file,
        @NotNull BiConsumer<@NotNull OutputStream, @NotNull TransferConfig> handler
    ) {
        DOWNLOAD_REQUEST_HANDLERS.put(file, handler);
    }

    /**
     * @return Whether {@code file} was even registered in the first place.
     */
    public static boolean unregisterDownloadRequestHandler(
        @NotNull Identifier file
    ) {
        return DOWNLOAD_REQUEST_HANDLERS.remove(file) != null;
    }

    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public static Map<@NotNull Identifier, @NotNull BiConsumer<@NotNull OutputStream, @NotNull TransferConfig>> getDownloadRequestHandlers() {
        return Collections.unmodifiableMap(DOWNLOAD_REQUEST_HANDLERS);
    }

    @NotNull
    public static InputStream downloadFileFromClient(
        @NotNull ServerPlayerEntity player,
        @NotNull Identifier file,
        @NotNull TransferConfig config
    ) {
        return downloadFile(player.networkHandler, file, config);
    }

    @NotNull
    public static InputStream downloadFileFromClient(
        @NotNull ServerPlayerEntity player,
        @NotNull Identifier file
    ) {
        return downloadFile(player.networkHandler, file, TransferConfig.DEFAULT_FROM_CLIENT);
    }

    @NotNull
    @Environment(EnvType.CLIENT)
    public static InputStream downloadFileFromServer(
        @NotNull Identifier file,
        @NotNull TransferConfig config
    ) {
        return downloadFile(Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()), file, config);
    }

    @NotNull
    @Environment(EnvType.CLIENT)
    public static InputStream downloadFileFromServer(
        @NotNull Identifier file
    ) {
        return downloadFile(Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()), file, TransferConfig.DEFAULT_FROM_SERVER);
    }

    @NotNull
    public static InputStream downloadFile(
        @NotNull net.fabricmc.fabric.api.networking.v1.PacketSender packetSender,
        @NotNull Identifier file
    ) {
        return downloadFile(FileTransferMod.convertFapiPacketSender(packetSender), file);
    }

    @NotNull
    public static InputStream downloadFile(
        @NotNull PacketSender packetSender,
        @NotNull Identifier file
    ) {
        return downloadFile(
            packetSender,
            file,
            packetSender.isClientbound() ? TransferConfig.DEFAULT_FROM_CLIENT : TransferConfig.DEFAULT_FROM_SERVER
        );
    }

    @NotNull
    public static InputStream downloadFile(
        @NotNull net.fabricmc.fabric.api.networking.v1.PacketSender packetSender,
        @NotNull Identifier file,
        @NotNull TransferConfig config
    ) {
        return downloadFile(FileTransferMod.convertFapiPacketSender(packetSender), file, config);
    }

    @NotNull
    public static InputStream downloadFile(
        @NotNull PacketSender packetSender,
        @NotNull Identifier file,
        @NotNull TransferConfig config
    ) {
        return FileTransferMod.downloadFile(packetSender, file, config);
    }
}
