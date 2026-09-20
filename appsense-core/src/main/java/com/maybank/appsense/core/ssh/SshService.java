package com.maybank.appsense.core.ssh;

import com.maybank.appsense.core.config.SshConstant;
import com.maybank.appsense.core.exception.AppSenseException;
import com.maybank.appsense.core.exception.AuthFailedException;
import com.maybank.appsense.core.exception.ConnectionLostException;
import com.maybank.appsense.core.exception.HostKeyChangedException;
import com.maybank.appsense.core.model.CommandResultRecord;
import com.maybank.appsense.core.model.ProfileBean;
import com.maybank.appsense.core.model.SettingsBean;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.keyboard.UserAuthKeyboardInteractiveFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.keyprovider.KeyIdentityProvider;
import org.apache.sshd.core.CoreModuleProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SshService {

    private static final Logger log = LoggerFactory.getLogger(SshService.class);

    private final SettingsBean settings;
    private final HostKeyVerifierService hostKeyVerifierService;
    private SshClient client;
    private volatile ClientSession session;
    private volatile String host = "";

    public SshService(SettingsBean settings, HostKeyVerifierService hostKeyVerifierService) {
        this.settings = settings;
        this.hostKeyVerifierService = hostKeyVerifierService;
    }

    /**
     * Opens an authenticated session. Password auth is tried first with keyboard-interactive as
     * fallback. Auth failure is never retried.
     */
    public synchronized void connect(ProfileBean profile, char[] password) throws AppSenseException {
        close();
        host = profile.getHost();
        ProfileHostKeyVerifier verifier = new ProfileHostKeyVerifier(profile, hostKeyVerifierService);
        client = createClient(verifier);
        try {
            session = client.connect(profile.getUsername(), host, profile.getPort())
                    .verify(Duration.ofSeconds(SshConstant.CONNECT_TIMEOUT_SECONDS)).getSession();
            session.setUserInteraction(new PasswordUserInteraction(password));
            session.addPasswordIdentity(new String(password));
            session.auth().verify(Duration.ofSeconds(SshConstant.AUTH_TIMEOUT_SECONDS));
        } catch (SshException ex) {
            close();
            throw classify(ex, verifier, profile);
        } catch (IOException ex) {
            close();
            log.error("SSH connection to {} failed", host, ex);
            throw new ConnectionLostException(host, ex);
        }
    }

    public boolean isConnected() {
        ClientSession current = session;
        return current != null && current.isOpen();
    }

    public String getHost() {
        return host;
    }

    /** Runs a short command and returns its output and exit status. */
    public CommandResultRecord execute(String command) throws ConnectionLostException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        Duration timeout = Duration.ofSeconds(SshConstant.COMMAND_TIMEOUT_SECONDS);
        try (ChannelExec channel = requireSession().createExecChannel(command)) {
            channel.setOut(out);
            channel.setErr(err);
            channel.open().verify(timeout);
            var events = channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), timeout.toMillis());
            Integer status = channel.getExitStatus();
            if (events.contains(ClientChannelEvent.TIMEOUT) || status == null) {
                throw new ConnectionLostException(host, null);
            }
            return new CommandResultRecord(status,
                    out.toString(SshConstant.REMOTE_CHARSET), err.toString(SshConstant.REMOTE_CHARSET));
        } catch (IOException ex) {
            log.error("Remote command failed on {}", host, ex);
            throw new ConnectionLostException(host, ex);
        }
    }

    /** Starts a long-running command whose output flows into the given streams. */
    public RemoteStream openStream(String command, OutputStream out, OutputStream err)
            throws ConnectionLostException {
        try {
            ChannelExec channel = requireSession().createExecChannel(command);
            channel.setOut(out);
            channel.setErr(err);
            channel.open().verify(Duration.ofSeconds(SshConstant.COMMAND_TIMEOUT_SECONDS));
            return new RemoteStream(channel);
        } catch (IOException ex) {
            log.error("Remote stream could not be opened on {}", host, ex);
            throw new ConnectionLostException(host, ex);
        }
    }

    public synchronized void close() {
        if (session != null) {
            session.close(true);
            session = null;
        }
        if (client != null) {
            client.stop();
            client = null;
        }
    }

    private SshClient createClient(ProfileHostKeyVerifier verifier) {
        SshClient created = SshClient.setUpDefaultClient();
        int keepAlive = settings.getKeepAliveIntervalSeconds() > 0
                ? settings.getKeepAliveIntervalSeconds() : SshConstant.KEEPALIVE_INTERVAL_SECONDS;
        CoreModuleProperties.HEARTBEAT_INTERVAL.set(created, Duration.ofSeconds(keepAlive));
        CoreModuleProperties.HEARTBEAT_REPLY_WAIT.set(created, SshConstant.HEARTBEAT_REPLY_WAIT);
        CoreModuleProperties.IDLE_TIMEOUT.set(created, SshConstant.IDLE_TIMEOUT_DISABLED);
        created.setServerKeyVerifier(verifier);
        created.setKeyIdentityProvider(KeyIdentityProvider.EMPTY_KEYS_PROVIDER);
        created.setUserAuthFactories(List.of(
                UserAuthPasswordFactory.INSTANCE, UserAuthKeyboardInteractiveFactory.INSTANCE));
        created.start();
        return created;
    }

    private AppSenseException classify(SshException ex, ProfileHostKeyVerifier verifier, ProfileBean profile) {
        if (verifier.getStatus() == HostKeyStatus.CHANGED) {
            return new HostKeyChangedException(host, profile.getHostKeyFingerprint(),
                    verifier.getPresentedFingerprint());
        }
        if (ex.getDisconnectCode() == SshConstants.SSH2_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE) {
            log.error("Authentication failed for {}", host);
            return new AuthFailedException(host, ex);
        }
        log.error("SSH negotiation with {} failed", host, ex);
        return new ConnectionLostException(host, ex);
    }

    private ClientSession requireSession() throws ConnectionLostException {
        ClientSession current = session;
        if (current == null || !current.isOpen()) {
            throw new ConnectionLostException(host, null);
        }
        return current;
    }
}
