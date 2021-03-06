package com.sequenceiq.cloudbreak.converter;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.certificate.PkiUtil;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.domain.stack.cluster.IdBroker;
import com.sequenceiq.cloudbreak.util.PasswordUtil;

@Component
public class IdBrokerConverterUtil {

    public IdBroker generateIdBrokerSignKeys(Cluster cluster) {
        IdBroker idBroker = new IdBroker();

        KeyPair identityKey = PkiUtil.generateKeypair();
        KeyPair signKey = PkiUtil.generateKeypair();
        X509Certificate cert = PkiUtil.cert(identityKey, "signing", signKey);

        idBroker.setSignKey(PkiUtil.convert(identityKey.getPrivate()));
        idBroker.setSignPub(PkiUtil.convert(identityKey.getPublic()));
        idBroker.setSignCert(PkiUtil.convert(cert));
        idBroker.setMasterSecret(PasswordUtil.generatePassword());

        idBroker.setCluster(cluster);
        idBroker.setWorkspace(cluster.getWorkspace());
        return idBroker;
    }

}
