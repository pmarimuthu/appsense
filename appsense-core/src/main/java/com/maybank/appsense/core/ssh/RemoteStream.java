package com.maybank.appsense.core.ssh;

import com.maybank.appsense.core.config.SshConstant;
import java.util.EnumSet;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;

public final class RemoteStream {

    private final ChannelExec channel;

    RemoteStream(ChannelExec channel) {
        this.channel = channel;
    }

    /** Blocks until the remote command ends or the stream is closed. */
    public void awaitClosed() {
        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), SshConstant.WAIT_FOREVER_MS);
    }

    /** Returns the remote exit status, or null when the channel ended without one. */
    public Integer getExitStatus() {
        return channel.getExitStatus();
    }

    public void close() {
        channel.close(true);
    }
}
