package io.github.gaming32.filetransferapi.impl.packets;

import io.github.gaming32.filetransferapi.impl.FileTransferMod;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class FileTransferPackets {
    public static final Identifier REQUEST_FILE = new Identifier(FileTransferMod.MOD_ID, "request_file");
    public static final Identifier TRANSFER_CANCEL = new Identifier(FileTransferMod.MOD_ID, "transfer/cancel");
    public static final Identifier TRANSFER_REQUEST_BLOCK = new Identifier(FileTransferMod.MOD_ID, "transfer/request_block");
    public static final Identifier TRANSFER_BLOCK = new Identifier(FileTransferMod.MOD_ID, "transfer/block");

    private FileTransferPackets() {
        throw new AssertionError();
    }
}
