package io.github.gaming32.filetransferapi.impl;

import io.github.gaming32.filetransferapi.api.PacketSender;
import io.github.gaming32.filetransferapi.impl.packets.TransferCancelPacket;
import io.github.gaming32.filetransferapi.impl.packets.TransferRequestBlockPacket;
import io.github.gaming32.filetransferapi.util.DoubleEndedStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public final class TransferInputStream extends InputStream {
    private final long transferId;
    private final PacketSender packetSender;
    private final DoubleEndedStream in;
    private final boolean noBuffer;
    private boolean closed;

    public TransferInputStream(long transferId, PacketSender packetSender, DoubleEndedStream in, boolean noBuffer) {
        this.transferId = transferId;
        this.packetSender = packetSender;
        this.in = in;
        this.noBuffer = noBuffer;
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("TransferInputStream is closed.");
        }
    }

    public DoubleEndedStream getIn() {
        return in;
    }

    @Override
    public int read() throws IOException {
        ensureOpen();
        if (noBuffer) {
            if (in.readWouldBlock()) {
                new TransferRequestBlockPacket(transferId).sendPacket(packetSender);
            }
        }
        return in.inputStream().read();
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        ensureOpen();
        if (noBuffer) {
            if (in.readWouldBlock()) {
                new TransferRequestBlockPacket(transferId).sendPacket(packetSender);
            }
        }
        return in.inputStream().read(b, off, len);
    }

    @Override
    public int available() throws IOException {
        ensureOpen();
        return in.size();
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            final TransferInputStream shouldBeSelf = FileTransferMod.ACTIVE_DOWNLOADS.remove(transferId);
            if (shouldBeSelf != this) {
                FileTransferMod.LOGGER.warn(
                    "File download {} in ACTIVE_DOWNLOADS is a mismatched stream. Expected {}, got {}.",
                    transferId, this, shouldBeSelf
                );
            }
            new TransferCancelPacket(transferId).sendPacket(packetSender);
            try {
                in.inputStream().close();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }
}
