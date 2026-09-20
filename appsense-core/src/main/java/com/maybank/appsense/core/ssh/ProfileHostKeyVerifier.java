package com.maybank.appsense.core.ssh;

import com.maybank.appsense.core.model.ProfileBean;
import java.net.SocketAddress;
import java.security.PublicKey;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.KeyUtils;

final class ProfileHostKeyVerifier implements ServerKeyVerifier {

    private final ProfileBean profile;
    private final HostKeyVerifierService service;
    private volatile HostKeyStatus status = HostKeyStatus.TRUSTED;
    private volatile String presentedFingerprint;

    ProfileHostKeyVerifier(ProfileBean profile, HostKeyVerifierService service) {
        this.profile = profile;
        this.service = service;
    }

    @Override
    public boolean verifyServerKey(ClientSession session, SocketAddress remoteAddress, PublicKey serverKey) {
        presentedFingerprint = KeyUtils.getFingerPrint(serverKey);
        status = service.evaluate(profile, presentedFingerprint);
        return status != HostKeyStatus.CHANGED;
    }

    HostKeyStatus getStatus() {
        return status;
    }

    String getPresentedFingerprint() {
        return presentedFingerprint;
    }
}
