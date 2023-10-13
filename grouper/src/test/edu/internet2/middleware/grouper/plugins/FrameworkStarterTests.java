package edu.internet2.middleware.grouper.plugins;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleException;

import java.io.IOException;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FrameworkStarterTests {
    @Before
    public void setup() {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().clear();

        // common properties
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(FrameworkStarter.GROUPER_OSGI_ENABLE, "true");
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(FrameworkStarter.GROUPER_OSGI_SECURITY_ENABLE, "true");
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(BundleStarter.GROUPER_OSGI_EXCEPTION_ON_PLUGIN_LOAD_ERROR, "true");
    }

    @After
    public void tearDown() throws BundleException {
        FrameworkStarter.getInstance().stop();
    }

    @Test
    public void testSecurityBasic() {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(FrameworkStarter.GROUPER_OSGI_FRAMEWORK_TRUST_REPOSITORIES, FrameworkStarterTests.class.getResource("/plugins/test.jks").getPath());
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.osgi.plugin.test.location", FrameworkStarterTests.class.getResource("/plugins/grouper-test-plugin.jar").toString());

        FrameworkStarter.getInstance().start();
    }

    @Test
    public void testSecurityPropertyTruststore()  {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.osgi.plugin.test.location", FrameworkStarterTests.class.getResource("/plugins/grouper-test-plugin.jar").toString());
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.osgi.truststore.signing.certificate", """
                -----BEGIN CERTIFICATE-----
                MIIDczCCAlugAwIBAgIILehkAIY5xvQwDQYJKoZIhvcNAQELBQAwaDEQMA4GA1UE
                BhMHVW5rbm93bjEQMA4GA1UECBMHVW5rbm93bjEQMA4GA1UEBxMHVW5rbm93bjEQ
                MA4GA1UEChMHVW5rbm93bjEQMA4GA1UECxMHVW5rbm93bjEMMAoGA1UEAwwDSmoh
                MB4XDTIzMTAxMjAwMjE1OFoXDTMzMTAxMTAwMjE1OFowaDEQMA4GA1UEBhMHVW5r
                bm93bjEQMA4GA1UECBMHVW5rbm93bjEQMA4GA1UEBxMHVW5rbm93bjEQMA4GA1UE
                ChMHVW5rbm93bjEQMA4GA1UECxMHVW5rbm93bjEMMAoGA1UEAwwDSmohMIIBIjAN
                BgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA9Y8HkztTbh1gXW+lIB175CYiqR5U
                qPVaCe9efvLd89zzyKpO1wxdp7lbJwwHfepuFhsGPMl38aD6YXeQDRKj3m7u2m94
                I1PAiAYslRSN/3oWyv7RQwP3gn4XAegpR4kBCNJoXLTPK80oyYA62okVNtCH/dFq
                Z3k3A1O+OoeqcDBq5KzuPatViFAjEyyLCcV1g0mLQlCfzrWFw5OVp2U6q2D0qLKF
                5i2bFkIlwxcWY0tfvv0S8Lp7+JpPN/etXGByKigM4Cu9CnfLBUs0v9h4wH5wv/Zj
                /XGfViD+pemIgkR82BqPM31TeWU0ONOYapS0LS4RpZBOcLtXcwgBMIch2QIDAQAB
                oyEwHzAdBgNVHQ4EFgQU5cbJ7MBtBDej6XYd3iukheyNO5kwDQYJKoZIhvcNAQEL
                BQADggEBAMjtgjBespxleA5Po49A3kng/A1n24m0SuGDVRmtWm5OGi34e+A64GIo
                kgiux+r0HOSDHUaIfao1L84FIxIBGGSHW/hcZ8RJr1qSYvW7KnD9PfgAcghtokKx
                38fx3Q3RUDB6Gcz2vigTkJ7OzR1wkyzVvfnLEOL3kA1P49Eb/wyMKEmmLDQyij6n
                JG+T6+M4+QKiA8W9yIIAAjJFoNL0LBczsdVn6o34buwqLPhjhVoGazDbax5cXxkJ
                8TRdktqQNbBqDk+6mOto8MCJUtg7d+Run+HvtgjNsdYLI6J+6K9+51STLtXST90V
                voa3jXptx9Tl/BFoJK2sJWdoIwG3zug=
                -----END CERTIFICATE-----""");

        FrameworkStarter.getInstance().start();
    }

    @Test
    public void testSecurityUntrustedSigner() {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(FrameworkStarter.GROUPER_OSGI_FRAMEWORK_TRUST_REPOSITORIES, FrameworkStarterTests.class.getResource("/plugins/test-empty.jks").getPath());
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.osgi.plugin.test.location", FrameworkStarterTests.class.getResource("/plugins/grouper-test-plugin.jar").toString());

        Exception e = assertThrows(Exception.class, () -> FrameworkStarter.getInstance().start());
        assert(e.getCause().getCause() instanceof IOException);
    }

    @Test
    public void testSecurityCorruptJar() {
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(FrameworkStarter.GROUPER_OSGI_FRAMEWORK_TRUST_REPOSITORIES, FrameworkStarterTests.class.getResource("/plugins/test.jks").getPath());
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.osgi.plugin.test.location", FrameworkStarterTests.class.getResource("/plugins/grouper-test-plugin-corrupt.jar").toString());

        Exception e = assertThrows(GrouperPluginException.class, () -> FrameworkStarter.getInstance().start());
        assertTrue(e.getCause().getCause() instanceof IOException);
    }
}
