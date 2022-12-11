package io.github.gaming32.filetransferapi.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class DoubleEndedStream {
    private final int blockSize;
    private final Deque<byte[]> blocks = new ConcurrentLinkedDeque<>();
    private final InputEnd inputEnd = new InputEnd();
    private final OutputEnd outputEnd = new OutputEnd();
    private final Lock lock = new ReentrantLock();
    private final Condition readyCondition = lock.newCondition();
    private byte[] readBlock, writeBlock;
    private int readPointer, writePointer;
    private boolean writeClosed;

    public DoubleEndedStream(int blockSize) {
        this.blockSize = blockSize;
        blocks.add(readBlock = writeBlock = new byte[blockSize]);
    }

    @NotNull
    @Contract(pure = true)
    public InputStream inputStream() {
        return outputEnd;
    }

    @NotNull
    @Contract(pure = true)
    public OutputStream outputStream() {
        return inputEnd;
    }

    public void write(int b) {
        lock.lock();
        try {
            if (writePointer == blockSize) {
                writePointer = 0;
                blocks.addLast(writeBlock = new byte[blockSize]);
            }
            writeBlock[writePointer++] = (byte)b;
            readyCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        lock.lock();
        try {
            while (len > 0) {
                if (blockSize == writePointer) {
                    writePointer = 0;
                    blocks.addLast(writeBlock = new byte[blockSize]);
                }
                final int toWrite = Math.min(blockSize - writePointer, len);
                System.arraycopy(b, off, writeBlock, writePointer, toWrite);
                off += toWrite;
                writePointer += toWrite;
                len -= toWrite;
            }
            readyCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    public int read() throws InterruptedException {
        lock.lock();
        try {
            while (readBlock == writeBlock && readPointer >= writePointer && !writeClosed) {
                readyCondition.await();
            }
            if (writeClosed && readBlock == writeBlock && readPointer >= writePointer) {
                return -1;
            }
            final int b = readBlock[readPointer++];
            if (readPointer == blockSize) {
                if (readBlock == writeBlock) {
                    readPointer = writePointer = 0; // Reset the block if it's full
                } else {
                    blocks.removeFirst();
                    readBlock = blocks.getFirst();
                }
            }
            return b;
        } finally {
            lock.unlock();
        }
    }

    public int read(byte @NotNull [] b, int off, int len) throws InterruptedException {
        if (len <= 0) return 0;
        lock.lock();
        try {
            int read = 0;
            while (len > 0) {
                final int toRead;
                if (readBlock == writeBlock) {
                    if (writePointer == readPointer) {
                        if (read == 0) {
                            if (writeClosed) {
                                return -1;
                            }
                            readyCondition.await();
                            if (writeClosed) {
                                return -1;
                            }
                            if (readBlock == writeBlock) {
                                if (readPointer == writePointer) {
                                    break;
                                }
                                toRead = Math.min(writePointer - readPointer, len);
                            } else {
                                toRead = Math.min(blockSize - readPointer, len);
                            }
                        } else {
                            break;
                        }
                    } else {
                        toRead = Math.min(writePointer - readPointer, len);
                    }
                } else {
                    toRead = Math.min(blockSize - readPointer, len);
                }
                System.arraycopy(readBlock, readPointer, b, off, toRead);
                off += toRead;
                read += toRead;
                len -= toRead;
                readPointer += toRead;
                if (readPointer == blockSize) {
                    if (readBlock == writeBlock) {
                        readPointer = writePointer = 0; // Reset the block if it's full
                    } else {
                        readPointer = 0;
                        blocks.removeFirst();
                        readBlock = blocks.getFirst();
                    }
                }
            }
            return read;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Calculates the minimum size of this stream. There is <i>at least</i> this amount available. For an exact size,
     * use {@link #size()}.
     */
    public int minSize() {
        lock.lock();
        try {
            if (readBlock == writeBlock) {
                return writePointer - readPointer;
            }
            return blockSize - readPointer + writePointer;
        } finally {
            lock.unlock();
        }
    }

    public boolean readWouldBlock() {
        return minSize() == 0;
    }

    public int size() {
        lock.lock();
        try {
            if (readBlock == writeBlock) {
                return writePointer - readPointer;
            }
            return blockSize - readPointer + writePointer + (blocks.size() - 2) * blockSize;
        } finally {
            lock.unlock();
        }
    }

    public void awaitMaybeReady() throws InterruptedException {
        lock.lock();
        try {
            readyCondition.await();
        } finally {
            lock.unlock();
        }
    }

    public void closeWrite() {
        lock.lock();
        try {
            writeClosed = true;
            readyCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    private final class InputEnd extends OutputStream {
        private void ensureOpen() throws IOException {
            if (writeClosed) {
                throw new IOException("DoubleEndedInputStream.outputStream() is closed.");
            }
        }

        @Override
        public void write(int b) throws IOException {
            ensureOpen();
            DoubleEndedStream.this.write(b);
        }

        @Override
        public void write(byte @NotNull [] b, int off, int len) throws IOException {
            ensureOpen();
            DoubleEndedStream.this.write(b, off, len);
        }

        @Override
        public void close() {
            writeClosed = true;
        }
    }

    private final class OutputEnd extends InputStream {
        private boolean closed;

        private void ensureOpen() throws IOException {
            if (closed) {
                throw new IOException("DoubleEndedInputStream.inputStream() is closed.");
            }
        }

        @Override
        public int read() throws IOException {
            ensureOpen();
            try {
                return DoubleEndedStream.this.read();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            ensureOpen();
            try {
                return DoubleEndedStream.this.read(b, off, len);
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        @Override
        public int available() {
            return size();
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
