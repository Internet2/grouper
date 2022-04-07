package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.plugin.Pac4jConfigFactory;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import org.junit.Assert;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.config.Config;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;

import java.time.Period;
import java.util.Arrays;
import java.util.Map;

public class Pac4JConfigFactoryTest extends TestCase {
    /**
     * @param name
     */
    public Pac4JConfigFactoryTest(String name) {
        super(name);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        //TestRunner.run(GrouperBoxFullRefreshTest.class);
        TestRunner.run(new Pac4JConfigFactoryTest("testPac4JConfigFactorCAS"));
    }

    /*
        reads configuration from the `grouper-ui.properties` file in the test resources directory to verify that
        elconfig still works
     */
    public void testElConfig() {
        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof CasClient);

        CasConfiguration configuration = ((CasClient) config.getClients().getClients().get(0)).getConfiguration();
        Assert.assertEquals(configuration.getLoginUrl(), "login");
    }

    /**
     *
     */
    public void testPac4JConfigFactorCAS() {
        GrouperUiConfig.retrieveConfig().propertiesOverrideMap().clear();
        Map<String, String> properties = GrouperUiConfig.retrieveConfig().propertiesOverrideMap();
        properties.put("external.authentication.mechanism","cas");
        properties.put("external.authentication.grouperContextUrl","localhost");
        properties.put("external.authentication.callbackUrl","callback");
        properties.put("external.authentication.cas.encoding","UTF-8");
        properties.put("external.authentication.cas.loginUrl","login");
        properties.put("external.authentication.cas.prefixUrl","localhost");
        properties.put("external.authentication.cas.restUrl","rest");
        properties.put("external.authentication.cas.timeTolerance","1000");
        properties.put("external.authentication.cas.renew","true");
        properties.put("external.authentication.cas.gateway","false");
        properties.put("external.authentication.cas.acceptAnyProxy","true");
        properties.put("external.authentication.cas.postLogoutUrlParameter","logout");
        properties.put("external.authentication.cas.customParams","param1=value1,param2=value2,param3=value3");
        properties.put("external.authentication.cas.method","post");
        properties.put("external.authentication.cas.privateKeyPath","http://localhost/key");
        properties.put("external.authentication.cas.privateKeyAlgorithm","AES");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof CasClient);

        CasConfiguration configuration = ((CasClient) config.getClients().getClients().get(0)).getConfiguration();

        Assert.assertEquals(configuration.getEncoding(), properties.get("external.authentication.cas.encoding"));
        Assert.assertEquals(configuration.getLoginUrl(), properties.get("external.authentication.cas.loginUrl"));
        Assert.assertEquals(configuration.getPrefixUrl(), properties.get("external.authentication.cas.prefixUrl"));
        Assert.assertEquals(configuration.getRestUrl(), properties.get("external.authentication.cas.restUrl"));
        Assert.assertEquals(configuration.getTimeTolerance(), Integer.parseInt(properties.get("external.authentication.cas.timeTolerance")));
        Assert.assertEquals(configuration.isRenew(), Boolean.parseBoolean(properties.get("external.authentication.cas.renew")));
        Assert.assertEquals(configuration.isGateway(), Boolean.parseBoolean(properties.get("external.authentication.cas.gateway")));
        Assert.assertEquals(configuration.isAcceptAnyProxy(), Boolean.parseBoolean(properties.get("external.authentication.cas.acceptAnyProxy")));
        Assert.assertEquals(configuration.getPostLogoutUrlParameter(), properties.get("external.authentication.cas.postLogoutUrlParameter"));
        Assert.assertEquals(configuration.getMethod(), properties.get("external.authentication.cas.method"));
        Assert.assertEquals(configuration.getPrivateKeyPath(), properties.get("external.authentication.cas.privateKeyPath"));
        Assert.assertEquals(configuration.getPrivateKeyAlgorithm(), properties.get("external.authentication.cas.privateKeyAlgorithm"));
        Assert.assertEquals(configuration.getCustomParams().size(), Arrays.asList(properties.get("external.authentication.cas.customParams").split(",")).size());
    }

    /**
     *
     */
    public void testPac4JConfigFactorSAML() {
        GrouperUiConfig.retrieveConfig().propertiesOverrideMap().clear();
        Map<String, String> properties = GrouperUiConfig.retrieveConfig().propertiesOverrideMap();
        properties.put("external.authentication.mechanism","saml");
        properties.put("external.authentication.grouperContextUrl","localhost");
        properties.put("external.authentication.callbackUrl","callback");
        properties.put("external.authentication.saml.keystorePassword","changeme");
        properties.put("external.authentication.saml.privateKeyPassword","secret");
        properties.put("external.authentication.saml.certificateNameToAppend","cert");
        properties.put("external.authentication.saml.identityProviderEntityId","idPid");
        properties.put("external.authentication.saml.serviceProviderEntityId","sPEid");
        properties.put("external.authentication.saml.maximumAuthenticationLifetime","500");
        properties.put("external.authentication.saml.acceptedSkew","10");
        properties.put("external.authentication.saml.forceAuth","true");
        properties.put("external.authentication.saml.passive","false");
        properties.put("external.authentication.saml.comparisonType","close");
        properties.put("external.authentication.saml.authnRequestBindingType","urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        properties.put("external.authentication.saml.responseBindingType","urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        properties.put("external.authentication.saml.spLogoutRequestBindingType","urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        properties.put("external.authentication.saml.spLogoutResponseBindingType","urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        properties.put("external.authentication.saml.authnContextClassRefs","type1,type2,type3,type4");
        properties.put("external.authentication.saml.nameIdPolicyFormat","####");
        properties.put("external.authentication.saml.useNameQualifier","true");
        properties.put("external.authentication.saml.signMetadata","false");
        properties.put("external.authentication.saml.forceServiceProviderMetadataGeneration","true");
        properties.put("external.authentication.saml.forceKeystoreGeneration","false");
        properties.put("external.authentication.saml.authnRequestSigned","true");
        properties.put("external.authentication.saml.spLogoutRequestSigned","false");
        properties.put("external.authentication.saml.blackListedSignatureSigningAlgorithms","col1,col2,col3,col4");
        properties.put("external.authentication.saml.signatureAlgorithms","RSA,ECDSA");
        properties.put("external.authentication.saml.signatureReferenceDigestMethods","md5,sha256");
        properties.put("external.authentication.saml.signatureCanonicalizationAlgorithm","qweafsdf");
        properties.put("external.authentication.saml.wantsAssertionsSigned","true");
        properties.put("external.authentication.saml.wantsResponsesSigned","false");
        properties.put("external.authentication.saml.allSignatureValidationDisabled","true");
        properties.put("external.authentication.saml.keyStoreAlias","fred");
        properties.put("external.authentication.saml.keyStoreType","text");
        properties.put("external.authentication.saml.assertionConsumerServiceIndex","5");
        properties.put("external.authentication.saml.attributeConsumingServiceIndex","2");
        properties.put("external.authentication.saml.providerName","paul");
        properties.put("external.authentication.saml.attributeAsId","george");
        properties.put("external.authentication.saml.mappedAttributes","key1=value1,key2=value2,key3=value3");
        properties.put("external.authentication.saml.postLogoutURL","logout");
        properties.put("external.authentication.saml.certificateExpirationPeriod","P2Y3M5D");
        properties.put("external.authentication.saml.certificateSignatureAlg","SHA1WithRSA");
        properties.put("external.authentication.saml.privateKeySize","15");
        properties.put("external.authentication.saml.issuerFormat","urn:oasis:names:tc:SAML:2.0:nameid-format:entity");
        properties.put("external.authentication.saml.nameIdPolicyAllowCreate","true");
        properties.put("external.authentication.saml.supportedProtocols","urn:oasis:names:tc:SAML:2.0:protocol, urn:oasis:names:tc:SAML:1.0:protocol, urn:oasis:names:tc:SAML:1.1:protocol");
        properties.put("external.authentication.saml.normalizedCertificateName","ringo");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof  SAML2Client);

        SAML2Configuration configuration = ((SAML2Client) config.getClients().getClients().get(0)).getConfiguration();

        Assert.assertEquals(configuration.getKeystorePassword(), properties.get("external.authentication.saml.keystorePassword"));
        Assert.assertEquals(configuration.getPrivateKeyPassword(), properties.get("external.authentication.saml.privateKeyPassword"));
        Assert.assertEquals(configuration.getCertificateNameToAppend(), properties.get("external.authentication.saml.certificateNameToAppend"));
        Assert.assertEquals(configuration.getIdentityProviderEntityId(), properties.get("external.authentication.saml.identityProviderEntityId"));
        Assert.assertEquals(configuration.getServiceProviderEntityId(), properties.get("external.authentication.saml.serviceProviderEntityId"));
        Assert.assertEquals(configuration.getMaximumAuthenticationLifetime(), Integer.parseInt(properties.get("external.authentication.saml.maximumAuthenticationLifetime")));
        Assert.assertEquals(configuration.getAcceptedSkew(), Integer.parseInt(properties.get("external.authentication.saml.acceptedSkew")));
        Assert.assertEquals(configuration.isForceAuth(), Boolean.parseBoolean(properties.get("external.authentication.saml.forceAuth")));
        Assert.assertEquals(configuration.isPassive(), Boolean.parseBoolean(properties.get("external.authentication.saml.passive")));
        Assert.assertEquals(configuration.getComparisonType(), properties.get("external.authentication.saml.comparisonType"));
        Assert.assertEquals(configuration.getAuthnRequestBindingType(), properties.get("external.authentication.saml.authnRequestBindingType"));
        Assert.assertEquals(configuration.getResponseBindingType(), properties.get("external.authentication.saml.responseBindingType"));
        Assert.assertEquals(configuration.getSpLogoutRequestBindingType(), properties.get("external.authentication.saml.spLogoutRequestBindingType"));
        Assert.assertEquals(configuration.getSpLogoutResponseBindingType(), properties.get("external.authentication.saml.spLogoutResponseBindingType"));
        Assert.assertEquals(configuration.getNameIdPolicyFormat(), properties.get("external.authentication.saml.nameIdPolicyFormat"));
        Assert.assertEquals(configuration.isUseNameQualifier(), Boolean.parseBoolean(properties.get("external.authentication.saml.useNameQualifier")));
        Assert.assertEquals(configuration.isSignMetadata(), Boolean.parseBoolean(properties.get("external.authentication.saml.signMetadata")));
        Assert.assertEquals(configuration.isForceServiceProviderMetadataGeneration(), Boolean.parseBoolean(properties.get("external.authentication.saml.forceServiceProviderMetadataGeneration")));
        Assert.assertEquals(configuration.isForceKeystoreGeneration(), Boolean.parseBoolean(properties.get("external.authentication.saml.forceKeystoreGeneration")));
        Assert.assertEquals(configuration.isAuthnRequestSigned(), Boolean.parseBoolean(properties.get("external.authentication.saml.authnRequestSigned")));
        Assert.assertEquals(configuration.isSpLogoutRequestSigned(), Boolean.parseBoolean(properties.get("external.authentication.saml.spLogoutRequestSigned")));
        Assert.assertEquals(configuration.getSignatureCanonicalizationAlgorithm(), properties.get("external.authentication.saml.signatureCanonicalizationAlgorithm"));
        Assert.assertEquals(configuration.isWantsAssertionsSigned(), Boolean.parseBoolean(properties.get("external.authentication.saml.wantsAssertionsSigned")));
        Assert.assertEquals(configuration.isWantsResponsesSigned(), Boolean.parseBoolean(properties.get("external.authentication.saml.wantsResponsesSigned")));
        Assert.assertEquals(configuration.isAllSignatureValidationDisabled(), Boolean.parseBoolean(properties.get("external.authentication.saml.allSignatureValidationDisabled")));
        Assert.assertEquals(configuration.getKeyStoreAlias(), properties.get("external.authentication.saml.keyStoreAlias"));
        Assert.assertEquals(configuration.getKeyStoreType(), properties.get("external.authentication.saml.keyStoreType"));
        Assert.assertEquals(configuration.getAssertionConsumerServiceIndex(), Integer.parseInt(properties.get("external.authentication.saml.assertionConsumerServiceIndex")));
        Assert.assertEquals(configuration.getAttributeConsumingServiceIndex(), Integer.parseInt(properties.get("external.authentication.saml.attributeConsumingServiceIndex")));
        Assert.assertEquals(configuration.getProviderName(), properties.get("external.authentication.saml.providerName"));
        Assert.assertEquals(configuration.getAttributeAsId(), properties.get("external.authentication.saml.attributeAsId"));
        Assert.assertEquals(configuration.getPostLogoutURL(), properties.get("external.authentication.saml.postLogoutURL"));
        Assert.assertEquals(configuration.getCertificateExpirationPeriod(), Period.parse(properties.get("external.authentication.saml.certificateExpirationPeriod")));
        Assert.assertEquals(configuration.getCertificateSignatureAlg(), properties.get("external.authentication.saml.certificateSignatureAlg"));
        Assert.assertEquals(configuration.getPrivateKeySize(), Integer.parseInt(properties.get("external.authentication.saml.privateKeySize")));
        Assert.assertEquals(configuration.getIssuerFormat(), properties.get("external.authentication.saml.issuerFormat"));
        Assert.assertEquals(configuration.isNameIdPolicyAllowCreate(), Boolean.parseBoolean(properties.get("external.authentication.saml.nameIdPolicyAllowCreate")));
        Assert.assertEquals(configuration.getAuthnContextClassRefs().size(), Arrays.asList(properties.get("external.authentication.saml.authnContextClassRefs").split(",")).size());
        Assert.assertEquals(configuration.getBlackListedSignatureSigningAlgorithms().size(), Arrays.asList(properties.get("external.authentication.saml.blackListedSignatureSigningAlgorithms").split(",")).size());
        Assert.assertEquals(configuration.getSignatureReferenceDigestMethods().size(), Arrays.asList(properties.get("external.authentication.saml.signatureReferenceDigestMethods").split(",")).size());
        Assert.assertEquals(configuration.getMappedAttributes().size(), Arrays.asList(properties.get("external.authentication.saml.mappedAttributes").split(",")).size());
        Assert.assertEquals(configuration.getSupportedProtocols().size(), Arrays.asList(properties.get("external.authentication.saml.supportedProtocols").split(",")).size());
    }

    /**
     *
     */
    public void testPac4JConfigFactorOidc() {
        GrouperUiConfig.retrieveConfig().propertiesOverrideMap().clear();
        Map<String, String> properties = GrouperUiConfig.retrieveConfig().propertiesOverrideMap();
        properties.put("external.authentication.mechanism","oidc");
        properties.put("external.authentication.grouperContextUrl","localhost");
        properties.put("external.authentication.callbackUrl","callback");
        properties.put("external.authentication.oidc.clientId","myClientId");
        properties.put("external.authentication.oidc.secret","secret");
        properties.put("external.authentication.oidc.discoveryURI","https://localhost/oidc");
        properties.put("external.authentication.oidc.scope","PUBLIC");
        properties.put("external.authentication.oidc.customParams","key1=value1, key2=value2, key3=value3");
        properties.put("external.authentication.oidc.useNonce","true");
        properties.put("external.authentication.oidc.disablePkce","false");
        properties.put("external.authentication.oidc.maxAge","60000");
        properties.put("external.authentication.oidc.maxClockSkew","10000");
        properties.put("external.authentication.oidc.responseMode","token");
        properties.put("external.authentication.oidc.logoutUrl","logout");
        properties.put("external.authentication.oidc.connectTimeout","30000");
        properties.put("external.authentication.oidc.readTimeout","15000");
        properties.put("external.authentication.oidc.withState","false");
        properties.put("external.authentication.oidc.expireSessionWithToken","true");
        properties.put("external.authentication.oidc.tokenExpirationAdvance","5000");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof OidcClient);

        OidcConfiguration configuration = ((OidcClient) config.getClients().getClients().get(0)).getConfiguration();

        Assert.assertEquals(configuration.getClientId(), properties.get("external.authentication.oidc.clientId"));
        Assert.assertEquals(configuration.getSecret(), properties.get("external.authentication.oidc.secret"));
        Assert.assertEquals(configuration.getDiscoveryURI(), properties.get("external.authentication.oidc.discoveryURI"));
        Assert.assertEquals(configuration.getScope(), properties.get("external.authentication.oidc.scope"));
        Assert.assertEquals(configuration.isUseNonce(), Boolean.parseBoolean(properties.get("external.authentication.oidc.useNonce")));
        Assert.assertEquals(configuration.isDisablePkce(), Boolean.parseBoolean(properties.get("external.authentication.oidc.disablePkce")));
        Assert.assertEquals(configuration.getMaxAge().intValue(), Integer.parseInt(properties.get("external.authentication.oidc.maxAge")));
        Assert.assertEquals(configuration.getMaxClockSkew(), Integer.parseInt(properties.get("external.authentication.oidc.maxClockSkew")));
        Assert.assertEquals(configuration.getResponseMode(), properties.get("external.authentication.oidc.responseMode"));
        Assert.assertEquals(configuration.getLogoutUrl(), properties.get("external.authentication.oidc.logoutUrl"));
        Assert.assertEquals(configuration.getConnectTimeout(), Integer.parseInt(properties.get("external.authentication.oidc.connectTimeout")));
        Assert.assertEquals(configuration.getReadTimeout(), Integer.parseInt(properties.get("external.authentication.oidc.readTimeout")));
        Assert.assertEquals(configuration.isWithState(), Boolean.parseBoolean(properties.get("external.authentication.oidc.withState")));
        Assert.assertEquals(configuration.isExpireSessionWithToken(), Boolean.parseBoolean(properties.get("external.authentication.oidc.expireSessionWithToken")));
        Assert.assertEquals(configuration.getTokenExpirationAdvance(), Integer.parseInt(properties.get("external.authentication.oidc.tokenExpirationAdvance")));
        Assert.assertEquals(configuration.getCustomParams().size(), Arrays.asList(properties.get("external.authentication.oidc.customParams").split(",")).size());
    }

    public void testPac4jForManualProvider() {
        GrouperUiConfig.retrieveConfig().properties().clear();
        GrouperUiConfig.retrieveConfig().propertiesOverrideMap().clear();
        Map<String,String> overrides = GrouperUiConfig.retrieveConfig().propertiesOverrideMap();
        overrides.put("external.authentication.provider", "edu.internet2.middleware.grouper.authentication.plugin.config.SAML2ClientProvider");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof SAML2Client);

        Assert.assertTrue(true);
    }
}