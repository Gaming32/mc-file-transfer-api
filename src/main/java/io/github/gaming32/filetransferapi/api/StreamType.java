package io.github.gaming32.filetransferapi.api;

public enum StreamType {
    /**
     * Transfers the entire file before invoking the callback. This takes more memory, but ensures no slowdowns in the
     * callback.
     */
    DOWNLOAD, // TODO: implement

    /**
     * Transfers the file in the background while the callback is running, pausing the callback when it reaches a part
     * of the file that hasn't been transferred yet. This is the default.
     */
    BUFFER,

    /**
     * Calls the callback right away. When the callback reaches a part of the file that hasn't been transferred yet,
     * that part is transferred immediately. This uses low memory, but can slow down the callback quite a bit.
     */
    STREAM // TODO: implement the rest of the way
}
