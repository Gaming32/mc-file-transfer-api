package io.github.gaming32.filetransferapi.testmod;

import io.github.gaming32.filetransferapi.api.FileTransferApi;
import io.github.gaming32.filetransferapi.api.TransferConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class FileTransferTestMod implements ModInitializer {
    private static final Identifier TEST_CLIENT = new Identifier("file-transfer-api-test", "test_client");
    private static final Identifier TEST_SERVER = new Identifier("file-transfer-api-test", "test_server");

    @Override
    public void onInitialize() {
        final TransferConfig config = TransferConfig.builder().maxBlockSize(5).build();

        FileTransferApi.registerDownloadRequestHandler(
            TEST_CLIENT,
            () -> new ReaderInputStream(new StringReader("Hello world from client!\n"), StandardCharsets.UTF_8)
        );

        FileTransferApi.registerDownloadRequestHandler(
            TEST_SERVER,
            () -> new ReaderInputStream(new StringReader("Hello world from server!\n"), StandardCharsets.UTF_8)
        );

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CompletableFuture.runAsync(() -> {
            try (InputStream is = FileTransferApi.downloadFile(sender, TEST_CLIENT, config)) {
                is.transferTo(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> CompletableFuture.runAsync(() -> {
            try (InputStream is = FileTransferApi.downloadFile(sender, TEST_SERVER, config)) {
                is.transferTo(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }
}
