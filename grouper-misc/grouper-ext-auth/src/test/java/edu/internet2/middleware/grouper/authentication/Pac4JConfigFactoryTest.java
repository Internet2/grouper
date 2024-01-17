package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.plugin.GrouperAuthentication;
import edu.internet2.middleware.grouper.authentication.plugin.Pac4jConfigFactory;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import org.pac4j.core.config.Config;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Pac4JConfigFactoryTest {
    MockedStatic<FrameworkUtil> frameworkUtilMockedStatic;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() throws Exception {
        this.frameworkUtilMockedStatic = Mockito.mockStatic(FrameworkUtil.class);

        Bundle bundle = mock(Bundle.class);
        this.frameworkUtilMockedStatic.when(() -> FrameworkUtil.getBundle(GrouperAuthentication.class)).thenReturn(bundle);

        BundleContext bundleContext = mock(BundleContext.class);
        when(bundle.getBundleContext()).thenReturn(bundleContext);


        ServiceReference<LogFactory> logFactoryServiceReference = mock(ServiceReference.class);
        when(bundleContext.getAllServiceReferences("org.apache.commons.logging.LogFactory", null)).thenReturn(new ServiceReference[]{logFactoryServiceReference});
        when(bundleContext.getService(logFactoryServiceReference)).thenReturn(LogFactory.getFactory());

        ServiceReference<ConfigPropertiesCascadeBase> UIConfigPropertiesCascadeBaseServiceReference = mock(ServiceReference.class);
        when(bundleContext.getServiceReferences(ConfigPropertiesCascadeBase.class, "(type=ui)")).thenReturn(Collections.singletonList(UIConfigPropertiesCascadeBaseServiceReference));
        when(bundleContext.getService(UIConfigPropertiesCascadeBaseServiceReference)).thenReturn(GrouperUiConfigInApi.retrieveConfig());

        ServiceReference<ConfigPropertiesCascadeBase> HibernateConfigPropertiesCascadeBaseServiceReference = mock(ServiceReference.class);
        when(bundleContext.getServiceReferences(ConfigPropertiesCascadeBase.class, "(type=hibernate)")).thenReturn(Collections.singletonList(HibernateConfigPropertiesCascadeBaseServiceReference));
        when(bundleContext.getService(HibernateConfigPropertiesCascadeBaseServiceReference)).thenReturn(GrouperHibernateConfig.retrieveConfig());
    }

    @After
    public void tearDown() {
        this.frameworkUtilMockedStatic.close();
    }

    /*
        reads configuration from the `grouper-ui.properties` file in the test resources directory to verify that
        elconfig still works
     */
    @Test
    public void testElConfig() {
        ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getConfigPropertiesCascadeBase("ui");
        grouperConfig.propertiesOverrideMap().clear();
        Map<String, String> properties = grouperConfig.propertiesOverrideMap();

        properties.put("external.authentication.provider.elConfig", "${\"cas\"}");
        properties.put("external.authentication.cas.loginUrl.elConfig", "${\"login\"}");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof CasClient);

        CasConfiguration configuration = ((CasClient) config.getClients().getClients().get(0)).getConfiguration();
        Assert.assertEquals("login", configuration.getLoginUrl());
    }

    /**
     *
     */
    @Test
    public void testPac4JConfigFactorCAS() {
        ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getConfigPropertiesCascadeBase("ui");
        grouperConfig.propertiesOverrideMap().clear();
        Map<String, String> properties = grouperConfig.propertiesOverrideMap();
        properties.put("external.authentication.provider","cas");
        properties.put("external.authentication.grouperContextUrl","localhost");
        properties.put("external.authentication.callbackUrl","http://callback");
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
        properties.put("external.authentication.cas.privateKeyPath","file:/key");
        properties.put("external.authentication.cas.privateKeyAlgorithm","AES");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof CasClient);

        CasConfiguration configuration = ((CasClient) config.getClients().getClients().get(0)).getConfiguration();

        Assert.assertEquals(properties.get("external.authentication.cas.encoding"), configuration.getEncoding());
        Assert.assertEquals(properties.get("external.authentication.cas.loginUrl"), configuration.getLoginUrl());
        Assert.assertEquals(properties.get("external.authentication.cas.prefixUrl"), configuration.getPrefixUrl());
        Assert.assertEquals(properties.get("external.authentication.cas.restUrl"), configuration.getRestUrl());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.cas.timeTolerance")), configuration.getTimeTolerance());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.cas.renew")), configuration.isRenew());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.cas.gateway")), configuration.isGateway());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.cas.acceptAnyProxy")), configuration.isAcceptAnyProxy());
        Assert.assertEquals(properties.get("external.authentication.cas.postLogoutUrlParameter"), configuration.getPostLogoutUrlParameter());
        Assert.assertEquals(properties.get("external.authentication.cas.method"), configuration.getMethod());
        Assert.assertEquals(properties.get("external.authentication.cas.privateKeyPath"), configuration.getPrivateKeyPath());
        Assert.assertEquals(properties.get("external.authentication.cas.privateKeyAlgorithm"), configuration.getPrivateKeyAlgorithm());
        Assert.assertEquals(configuration.getCustomParams().size(), Arrays.asList(properties.get("external.authentication.cas.customParams").split(",")).size());
    }

    /**
     *
     */
    @Test
    public void testPac4JConfigFactorSAML() {
        ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getConfigPropertiesCascadeBase("ui");
        grouperConfig.propertiesOverrideMap().clear();
        Map<String, String> properties = grouperConfig.propertiesOverrideMap();
        properties.put("external.authentication.provider","saml");
        properties.put("external.authentication.grouperContextUrl","localhost");
        properties.put("external.authentication.callbackUrl","http://callback");
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
        properties.put("external.authentication.saml.keystoreAlias","fred");
        properties.put("external.authentication.saml.keystoreType","text");
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

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof SAML2Client);

        SAML2Configuration configuration = ((SAML2Client) config.getClients().getClients().get(0)).getConfiguration();

        Assert.assertEquals(properties.get("external.authentication.saml.keystorePassword"), configuration.getKeystorePassword());
        Assert.assertEquals(properties.get("external.authentication.saml.privateKeyPassword"), configuration.getPrivateKeyPassword());
        Assert.assertEquals(properties.get("external.authentication.saml.certificateNameToAppend"), configuration.getCertificateNameToAppend());
        Assert.assertEquals(properties.get("external.authentication.saml.identityProviderEntityId"), configuration.getIdentityProviderEntityId());
        Assert.assertEquals(properties.get("external.authentication.saml.serviceProviderEntityId"), configuration.getServiceProviderEntityId());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.saml.maximumAuthenticationLifetime")), configuration.getMaximumAuthenticationLifetime());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.saml.acceptedSkew")), configuration.getAcceptedSkew());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.forceAuth")), configuration.isForceAuth());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.passive")), configuration.isPassive());
        Assert.assertEquals(properties.get("external.authentication.saml.comparisonType"), configuration.getComparisonType());
        Assert.assertEquals(properties.get("external.authentication.saml.authnRequestBindingType"), configuration.getAuthnRequestBindingType());
        Assert.assertEquals(properties.get("external.authentication.saml.responseBindingType"), configuration.getResponseBindingType());
        Assert.assertEquals(properties.get("external.authentication.saml.spLogoutRequestBindingType"), configuration.getSpLogoutRequestBindingType());
        Assert.assertEquals(properties.get("external.authentication.saml.spLogoutResponseBindingType"), configuration.getSpLogoutResponseBindingType());
        Assert.assertEquals(properties.get("external.authentication.saml.nameIdPolicyFormat"), configuration.getNameIdPolicyFormat());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.useNameQualifier")), configuration.isUseNameQualifier());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.signMetadata")), configuration.isSignMetadata());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.forceServiceProviderMetadataGeneration")), configuration.isForceServiceProviderMetadataGeneration());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.forceKeystoreGeneration")), configuration.isForceKeystoreGeneration());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.authnRequestSigned")), configuration.isAuthnRequestSigned());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.spLogoutRequestSigned")), configuration.isSpLogoutRequestSigned());
        Assert.assertEquals(properties.get("external.authentication.saml.signatureCanonicalizationAlgorithm"), configuration.getSignatureCanonicalizationAlgorithm());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.wantsAssertionsSigned")), configuration.isWantsAssertionsSigned());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.wantsResponsesSigned")), configuration.isWantsResponsesSigned());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.allSignatureValidationDisabled")), configuration.isAllSignatureValidationDisabled());
        Assert.assertEquals(properties.get("external.authentication.saml.keystoreAlias"), configuration.getKeyStoreAlias());
        Assert.assertEquals(properties.get("external.authentication.saml.keystoreType"), configuration.getKeyStoreType());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.saml.assertionConsumerServiceIndex")), configuration.getAssertionConsumerServiceIndex());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.saml.attributeConsumingServiceIndex")), configuration.getAttributeConsumingServiceIndex());
        Assert.assertEquals(properties.get("external.authentication.saml.providerName"), configuration.getProviderName());
        Assert.assertEquals(properties.get("external.authentication.saml.attributeAsId"), configuration.getAttributeAsId());
        Assert.assertEquals(properties.get("external.authentication.saml.postLogoutURL"), configuration.getPostLogoutURL());
        Assert.assertEquals(Period.parse(properties.get("external.authentication.saml.certificateExpirationPeriod")), configuration.getCertificateExpirationPeriod());
        Assert.assertEquals(properties.get("external.authentication.saml.certificateSignatureAlg"), configuration.getCertificateSignatureAlg());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.saml.privateKeySize")), configuration.getPrivateKeySize());
        Assert.assertEquals(properties.get("external.authentication.saml.issuerFormat"), configuration.getIssuerFormat());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.saml.nameIdPolicyAllowCreate")), configuration.isNameIdPolicyAllowCreate());
        Assert.assertEquals(configuration.getAuthnContextClassRefs().size(), Arrays.asList(properties.get("external.authentication.saml.authnContextClassRefs").split(",")).size());
        Assert.assertEquals(configuration.getBlackListedSignatureSigningAlgorithms().size(), Arrays.asList(properties.get("external.authentication.saml.blackListedSignatureSigningAlgorithms").split(",")).size());
        Assert.assertEquals(configuration.getSignatureReferenceDigestMethods().size(), Arrays.asList(properties.get("external.authentication.saml.signatureReferenceDigestMethods").split(",")).size());
        Assert.assertEquals(configuration.getMappedAttributes().size(), Arrays.asList(properties.get("external.authentication.saml.mappedAttributes").split(",")).size());
        Assert.assertEquals(configuration.getSupportedProtocols().size(), Arrays.asList(properties.get("external.authentication.saml.supportedProtocols").split(",")).size());
    }

    /**
     *
     */
    @Test
    public void testPac4JConfigFactorOidc() {
        ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getConfigPropertiesCascadeBase("ui");
        grouperConfig.propertiesOverrideMap().clear();
        Map<String, String> properties = grouperConfig.propertiesOverrideMap();
        properties.put("external.authentication.provider","oidc");
        properties.put("external.authentication.grouperContextUrl","localhost");
        properties.put("external.authentication.callbackUrl","http://callback");
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

        Assert.assertEquals(properties.get("external.authentication.oidc.clientId"), configuration.getClientId());
        Assert.assertEquals(properties.get("external.authentication.oidc.secret"), configuration.getSecret());
        Assert.assertEquals(properties.get("external.authentication.oidc.discoveryURI"), configuration.getDiscoveryURI());
        Assert.assertEquals(properties.get("external.authentication.oidc.scope"), configuration.getScope());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.oidc.useNonce")), configuration.isUseNonce());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.oidc.disablePkce")), configuration.isDisablePkce());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.oidc.maxAge")), configuration.getMaxAge().intValue());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.oidc.maxClockSkew")), configuration.getMaxClockSkew());
        Assert.assertEquals(properties.get("external.authentication.oidc.responseMode"), configuration.getResponseMode());
        Assert.assertEquals(properties.get("external.authentication.oidc.logoutUrl"), configuration.getLogoutUrl());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.oidc.connectTimeout")), configuration.getConnectTimeout());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.oidc.readTimeout")), configuration.getReadTimeout());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.oidc.withState")), configuration.isWithState());
        Assert.assertEquals(Boolean.parseBoolean(properties.get("external.authentication.oidc.expireSessionWithToken")), configuration.isExpireSessionWithToken());
        Assert.assertEquals(Integer.parseInt(properties.get("external.authentication.oidc.tokenExpirationAdvance")), configuration.getTokenExpirationAdvance());
        Assert.assertEquals(configuration.getCustomParams().size(), Arrays.asList(properties.get("external.authentication.oidc.customParams").split(",")).size());
    }

    @Test
    public void testPac4jForManualProvider() {
        ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getConfigPropertiesCascadeBase("ui");
        grouperConfig.propertiesOverrideMap().clear();
        Map<String, String> overrides = grouperConfig.propertiesOverrideMap();
        overrides.put("external.authentication.provider", "edu.internet2.middleware.grouper.authentication.plugin.config.SAML2ClientProvider");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof SAML2Client);

        Assert.assertTrue(true);
    }

    @Test
    public void testPac4jConfigMethodFind() throws IOException {
        // external.authentication.saml.identityProviderMetadataPath = file:/opt/grouper/idp-metadata.xml
        ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getConfigPropertiesCascadeBase("ui");

        grouperConfig.propertiesOverrideMap().clear();
        Map<String, String> overrides = grouperConfig.propertiesOverrideMap();
        overrides.put("external.authentication.provider","saml");
        overrides.put("external.authentication.grouperContextUrl","localhost");
        overrides.put("external.authentication.callbackUrl","http://callback");
        overrides.put("external.authentication.saml.identityProviderMetadataPath", "file:/opt/grouper/idp-metadata.xml");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        SAML2Configuration configuration = ((SAML2Client) config.getClients().getClients().get(0)).getConfiguration();

        Assert.assertTrue(configuration.getIdentityProviderMetadataResource().isFile() && ((FileSystemResource)configuration.getIdentityProviderMetadataResource()).getPath().equals("/opt/grouper/idp-metadata.xml"));
    }
    @Test
    public void testPac4jConfigEnum() throws IOException {
        // external.authentication.saml.identityProviderMetadataPath = file:/opt/grouper/idp-metadata.xml
        ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getConfigPropertiesCascadeBase("ui");

        grouperConfig.propertiesOverrideMap().clear();
        Map<String, String> overrides = grouperConfig.propertiesOverrideMap();
        overrides.put("external.authentication.provider","cas");
        overrides.put("external.authentication.cas.protocol", "CAS20");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        CasConfiguration configuration = ((CasClient) config.getClients().getClients().get(0)).getConfiguration();

        Assert.assertTrue(CasProtocol.CAS20.equals(configuration.getProtocol()));
    }

    @Test
    public void testConfigTestRename() {
        ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getConfigPropertiesCascadeBase("ui");

        grouperConfig.propertiesOverrideMap().clear();
        Map<String, String> overrides = grouperConfig.propertiesOverrideMap();
        overrides.put("external.authentication.provider","saml");
        overrides.put("external.authentication.saml.keyStoreAlias","fred");
        overrides.put("external.authentication.saml.keyStoreType","keystoretype");

        Pac4jConfigFactory pac4jConfigFactory = new Pac4jConfigFactory();
        Config config = pac4jConfigFactory.build();

        Assert.assertTrue(config.getClients().getClients().get(0) instanceof  SAML2Client);

        SAML2Configuration configuration = ((SAML2Client) config.getClients().getClients().get(0)).getConfiguration();

        Assert.assertEquals(overrides.get("external.authentication.saml.keyStoreAlias"), configuration.getKeyStoreAlias());
        Assert.assertEquals(overrides.get("external.authentication.saml.keyStoreType"), configuration.getKeyStoreType());
    }
}