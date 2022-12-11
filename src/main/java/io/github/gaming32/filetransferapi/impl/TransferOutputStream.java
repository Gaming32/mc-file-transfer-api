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
    private final int maxBlockSize;
    private boolean closed;

    public TransferOutputStream(long transferId, PacketSender packetSender, int maxBlockSize) {
        this.transferId = transferId;
        this.packetSender = packetSender;
        this.maxBlockSize = maxBlockSize;
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
        while (len > 0) {
            final int toWrite = Math.min(len, maxBlockSize);
            new TransferBlockPacket(transferId, b, off, toWrite).sendPacket(packetSender);
            off += toWrite;
            len -= toWrite;
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            final TransferOutputStream shouldBeSelf = FileTransferMod.ACTIVE_UPLOADS.remove(transferId);
            if (shouldBeSelf != this) {
                FileTransferMod.LOGGER.warn(
                    "File upload {} in ACTIVE_UPLOADS is a mismatched stream. Expected {}, got {}.",
                    Long.toUnsignedString(transferId, 16), this, shouldBeSelf
                );
            }
            new TransferCancelPacket(transferId).sendPacket(packetSender);
        }
    }
}
