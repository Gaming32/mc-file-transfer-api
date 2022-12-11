package io.github.gaming32.filetransferapi.impl;

import io.github.gaming32.filetransferapi.api.PacketSender;
import io.github.gaming32.filetransferapi.impl.packets.TransferBlockPacket;
import io.github.gaming32.filetransferapi.impl.packets.TransferCancelPacket;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public final class TransferOutputStream extends OutputStream {
    private final long transferId;
    private final PacketSender packetSender;
    private boolean closed;

    public TransferOutputStream(long transferId, PacketSender packetSender) {
        this.transferId = transferId;
        this.packetSender = packetSender;
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("TransferOutputStream closed.");
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] {(byte)b}, 0, 1);
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        ensureOpen();
        new TransferBlockPacket(transferId, b, off, len).sendPacket(packetSender);
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            final TransferOutputStream shouldBeSelf = FileTransferMod.ACTIVE_UPLOADS.remove(transferId);
            if (shouldBeSelf != this) {
                FileTransferMod.LOGGER.warn(
                    "File upload {} in ACTIVE_UPLOADS is a mismatched stream. Expected {}, got {}.",
                    transferId, this, shouldBeSelf
                );
            }
            new TransferCancelPacket(transferId).sendPacket(packetSender);
        }
    }
}
