package io.github.gaming32.filetransferapi.impl;

import io.github.gaming32.filetransferapi.api.PacketSender;
import io.github.gaming32.filetransferapi.api.StreamType;
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
    private final StreamType streamType;
    private boolean closed;

    public TransferInputStream(long transferId, PacketSender packetSender, DoubleEndedStream in, StreamType streamType) {
        this.transferId = transferId;
        this.packetSender = packetSender;
        this.in = in;
        this.streamType = streamType;
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("TransferInputStream is closed.");
        }
    }

    public DoubleEndedStream getIn() {
        return in;
    }

    private void preRead() throws IOException {
        ensureOpen();
        if (streamType == StreamType.STREAM) {
            if (in.readWouldBlock()) {
                new TransferRequestBlockPacket(transferId).sendPacket(packetSender);
            }
        } else if (streamType == StreamType.DOWNLOAD) {
            while (!in.isWriteClosed()) {
                in.awaitMaybeReadyUninterruptibly();
            }
        }
    }

    @Override
    public int read() throws IOException {
        preRead();
        return in.inputStream().read();
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        preRead();
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
                    Long.toUnsignedString(transferId, 16), this, shouldBeSelf
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
