package com.streams.io;

/**
 * All Stream Input Objects extend this class
 *
 * @author isyed
 * @version 0.1
 */
public class SInput {
    boolean shutdown = false;//True, tells server to shutdown

    public boolean isShutdown() {
        return shutdown;
    }

    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
