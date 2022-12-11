package io.github.gaming32.filetransferapi.api;

public enum StreamType {
    /**
     * Transfers the entire file before allowing any reads. This takes more memory, but ensures no slowdowns past the
     * first read.
     */
    DOWNLOAD,

    /**
     * Transfers the file in the background while the file is read, blocking the stream when it reaches a part of the
     * file that hasn't been transferred yet. This is the default.
     */
    BUFFER,

    /**
     * Calls the callback right away. When the callback reaches a part of the file that hasn't been transferred yet,
     * that part is transferred immediately. This uses low memory, but can slow down the callback quite a bit. Caution
     * from the uploader should be used with this method, as it will block the reading thread until transfer requests
     * come in. However, transfer requests are implemented with packets, and the callback is called on the packet
     * processing thread, so you're very likely to cause a deadlock if you don't upload from another thread.
     */
    STREAM // TODO: implement the rest of the way
}
