/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.ldap.ldaptive;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import org.junit.Assert;
import org.junit.Test;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.SearchConnectionValidator;
import org.ldaptive.handler.CaseChangeEntryHandler;
import org.ldaptive.handler.DnAttributeEntryHandler;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.sasl.Mechanism;

/**
 * Unit test for {@link LdaptiveConfiguration}.
 */
public class LdaptiveConfigurationTest {

  /**
   * Store configuration properties in {@link GrouperLoaderConfig}.
   *
   * @param propertyData to parse into {@link Properties}
   *
   * @throws Exception if an error occurs reading property data
   */
  private static void overrideProperties(String propertyData) throws Exception {
    Properties testProps = new Properties();
    testProps.load(new BufferedReader(new StringReader(propertyData)));
    Enumeration<Object> e = testProps.keys();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      GrouperLoaderConfig.retrieveConfig().propertiesThreadLocalOverrideMap().put(key, testProps.getProperty(key));
    }
  }

  @Test
  public void useStartTLSFalse() throws Exception {
    String PROPERTY_DATA = "ldap.tls-false.url = ldap://localhost:10389\n"
        + "ldap.tls-false.tls = false\n"
        + "ldap.tls-false.sizeLimit = 10\n"
        + "ldap.tls-false.timeLimit = PT5S\n"
        + "ldap.tls-false.connectTimeout = PT3S\n"
        + "ldap.tls-false.responseTimeout = PT3S\n"
        + "ldap.tls-false.minPoolSize = 0\n"
        + "ldap.tls-false.maxPoolSize = 5\n"
        + "ldap.tls-false.blockWaitTime = PT1S\n"
        + "ldap.tls-false.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    PooledConnectionFactory factory = LdaptiveConfiguration.createPooledConnectionFactory("tls-false");
    defaultAssertions(factory);
    Assert.assertFalse(factory.getConnectionConfig().getUseStartTLS());
    Assert.assertNull(factory.getConnectionConfig().getSslConfig());
    Assert.assertNull(factory.getConnectionConfig().getConnectionInitializers());
    SearchConnectionValidator validator = (SearchConnectionValidator) factory.getValidator();
    Assert.assertEquals(Duration.ofMinutes(30), validator.getValidatePeriod());
  }

  @Test
  public void useStartTLSTrue() throws Exception {
    String PROPERTY_DATA = "ldap.tls-true.url = ldap://localhost:10389\n"
        + "ldap.tls-true.tls = true\n"
        + "ldap.tls-true.sizeLimit = 10\n"
        + "ldap.tls-true.timeLimit = PT5S\n"
        + "ldap.tls-true.connectTimeout = PT3S\n"
        + "ldap.tls-true.responseTimeout = PT3S\n"
        + "ldap.tls-true.minPoolSize = 0\n"
        + "ldap.tls-true.maxPoolSize = 5\n"
        + "ldap.tls-true.blockWaitTime = PT1S\n"
        + "ldap.tls-true.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    PooledConnectionFactory factory = LdaptiveConfiguration.createPooledConnectionFactory("tls-true");
    defaultAssertions(factory);
    Assert.assertTrue(factory.getConnectionConfig().getUseStartTLS());
    Assert.assertNull(factory.getConnectionConfig().getSslConfig());
    Assert.assertNull(factory.getConnectionConfig().getConnectionInitializers());
    SearchConnectionValidator validator = (SearchConnectionValidator) factory.getValidator();
    Assert.assertEquals(Duration.ofMinutes(30), validator.getValidatePeriod());
  }

  @Test
  public void bindCredentials() throws Exception {
    String PROPERTY_DATA = "ldap.bind-creds.url = ldap://localhost:10389\n"
        + "ldap.bind-creds.tls = true\n"
        + "ldap.bind-creds.user = cn=admin,ou=people,dc=internet2,dc=edu\n"
        + "ldap.bind-creds.pass = mysecret\n"
        + "ldap.bind-creds.sizeLimit = 10\n"
        + "ldap.bind-creds.timeLimit = PT5S\n"
        + "ldap.bind-creds.connectTimeout = PT3S\n"
        + "ldap.bind-creds.responseTimeout = PT3S\n"
        + "ldap.bind-creds.minPoolSize = 0\n"
        + "ldap.bind-creds.maxPoolSize = 5\n"
        + "ldap.bind-creds.blockWaitTime = PT1S\n"
        + "ldap.bind-creds.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    PooledConnectionFactory factory = LdaptiveConfiguration.createPooledConnectionFactory("bind-creds");
    defaultAssertions(factory);
    Assert.assertTrue(factory.getConnectionConfig().getUseStartTLS());
    Assert.assertNull(factory.getConnectionConfig().getSslConfig());
    BindConnectionInitializer initializer = (BindConnectionInitializer) factory.getConnectionConfig().getConnectionInitializers()[0];
    Assert.assertEquals("cn=admin,ou=people,dc=internet2,dc=edu", initializer.getBindDn());
    Assert.assertEquals("mysecret", initializer.getBindCredential().getString());
    Assert.assertNull(initializer.getBindSaslConfig());
    SearchConnectionValidator validator = (SearchConnectionValidator) factory.getValidator();
    Assert.assertEquals(Duration.ofMinutes(30), validator.getValidatePeriod());
  }

  @Test
  public void credentialConfig() throws Exception {
    String PROPERTY_DATA = "ldap.cred-config.url = ldap://localhost:10389\n"
        + "ldap.cred-config.tls = true\n"
        + "ldap.cred-config.pemCaFile = /tmp/cafile\n"
        + "ldap.cred-config.pemCertFile = /tmp/certfile\n"
        + "ldap.cred-config.pemKeyFile = /tmp/keyfile\n"
        + "ldap.cred-config.sizeLimit = 10\n"
        + "ldap.cred-config.timeLimit = PT5S\n"
        + "ldap.cred-config.connectTimeout = PT3S\n"
        + "ldap.cred-config.responseTimeout = PT3S\n"
        + "ldap.cred-config.minPoolSize = 0\n"
        + "ldap.cred-config.maxPoolSize = 5\n"
        + "ldap.cred-config.blockWaitTime = PT1S\n"
        + "ldap.cred-config.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    PooledConnectionFactory factory = LdaptiveConfiguration.createPooledConnectionFactory("cred-config");
    defaultAssertions(factory);
    Assert.assertTrue(factory.getConnectionConfig().getUseStartTLS());
    Assert.assertNotNull(factory.getConnectionConfig().getSslConfig());
    LdapPEMCredentialConfig credentialConfig = (LdapPEMCredentialConfig) factory.getConnectionConfig().getSslConfig().getCredentialConfig();
    Assert.assertEquals(LdapPEMCredentialConfig.class, credentialConfig.getClass());
    Assert.assertEquals("/tmp/cafile", credentialConfig.getCaFile());
    Assert.assertEquals("/tmp/certfile", credentialConfig.getCertFile());
    Assert.assertEquals("/tmp/keyfile", credentialConfig.getKeyFile());
    Assert.assertNull(factory.getConnectionConfig().getConnectionInitializers());
    SearchConnectionValidator validator = (SearchConnectionValidator) factory.getValidator();
    Assert.assertEquals(Duration.ofMinutes(30), validator.getValidatePeriod());
  }

  @Test
  public void saslConfig() throws Exception {
    String PROPERTY_DATA = "ldap.sasl-config.url = ldap://localhost:10389\n"
        + "ldap.sasl-config.tls = false\n"
        + "ldap.sasl-config.saslMechanism = DIGEST_MD5\n"
        + "ldap.sasl-config.saslRealm = myrealm\n"
        + "ldap.sasl-config.bindDn = admin@internet2.edu\n"
        + "ldap.sasl-config.bindCredential = password\n"
        + "ldap.sasl-config.sizeLimit = 10\n"
        + "ldap.sasl-config.timeLimit = PT5S\n"
        + "ldap.sasl-config.connectTimeout = PT3S\n"
        + "ldap.sasl-config.responseTimeout = PT3S\n"
        + "ldap.sasl-config.minPoolSize = 0\n"
        + "ldap.sasl-config.maxPoolSize = 5\n"
        + "ldap.sasl-config.blockWaitTime = PT1S\n"
        + "ldap.sasl-config.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    PooledConnectionFactory factory = LdaptiveConfiguration.createPooledConnectionFactory("sasl-config");
    defaultAssertions(factory);
    Assert.assertFalse(factory.getConnectionConfig().getUseStartTLS());
    Assert.assertNull(factory.getConnectionConfig().getSslConfig());
    BindConnectionInitializer initializer = (BindConnectionInitializer) factory.getConnectionConfig().getConnectionInitializers()[0];
    Assert.assertEquals("admin@internet2.edu", initializer.getBindDn());
    Assert.assertEquals("password", initializer.getBindCredential().getString());
    Assert.assertEquals(Mechanism.DIGEST_MD5, initializer.getBindSaslConfig().getMechanism());
    Assert.assertEquals("myrealm", initializer.getBindSaslConfig().getRealm());
    SearchConnectionValidator validator = (SearchConnectionValidator) factory.getValidator();
    Assert.assertEquals(Duration.ofMinutes(30), validator.getValidatePeriod());
  }

  @Test
  public void compareValidator() throws Exception {
    String PROPERTY_DATA = "ldap.compare-validator.url = ldap://localhost:10389\n"
        + "ldap.compare-validator.tls = false\n"
        + "ldap.compare-validator.validator = CompareLdapValidator\n"
        + "ldap.compare-validator.validatorCompareDn = ou=people,dc=internet2,dc=edu\n"
        + "ldap.compare-validator.validatorCompareAttribute = objectclass\n"
        + "ldap.compare-validator.validatorCompareValue = organizationalunit\n"
        + "ldap.compare-validator.validateTimerPeriod = 900000\n"
        + "ldap.compare-validator.sizeLimit = 10\n"
        + "ldap.compare-validator.timeLimit = PT5S\n"
        + "ldap.compare-validator.connectTimeout = PT3S\n"
        + "ldap.compare-validator.responseTimeout = PT3S\n"
        + "ldap.compare-validator.minPoolSize = 0\n"
        + "ldap.compare-validator.maxPoolSize = 5\n"
        + "ldap.compare-validator.blockWaitTime = PT1S\n"
        + "ldap.compare-validator.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    PooledConnectionFactory factory = LdaptiveConfiguration.createPooledConnectionFactory("compare-validator");
    defaultAssertions(factory);
    Assert.assertFalse(factory.getConnectionConfig().getUseStartTLS());
    Assert.assertNull(factory.getConnectionConfig().getSslConfig());
    Assert.assertNull(factory.getConnectionConfig().getConnectionInitializers());
    LdaptiveConnectionValidator validator = (LdaptiveConnectionValidator) factory.getValidator();
    Assert.assertEquals("ou=people,dc=internet2,dc=edu", validator.getCompareRequest().getDn());
    Assert.assertEquals("objectclass", validator.getCompareRequest().getName());
    Assert.assertEquals("organizationalunit", validator.getCompareRequest().getValue());
    Assert.assertEquals(Duration.ofMinutes(15), validator.getValidatePeriod());
  }

  @Test
  public void searchValidator() throws Exception {
    String PROPERTY_DATA = "ldap.search-validator.url = ldap://localhost:10389\n"
        + "ldap.search-validator.tls = false\n"
        + "ldap.search-validator.validateTimerPeriod = 900000\n"
        + "ldap.search-validator.sizeLimit = 10\n"
        + "ldap.search-validator.timeLimit = PT5S\n"
        + "ldap.search-validator.connectTimeout = PT3S\n"
        + "ldap.search-validator.responseTimeout = PT3S\n"
        + "ldap.search-validator.minPoolSize = 0\n"
        + "ldap.search-validator.maxPoolSize = 5\n"
        + "ldap.search-validator.blockWaitTime = PT1S\n"
        + "ldap.search-validator.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    PooledConnectionFactory factory = LdaptiveConfiguration.createPooledConnectionFactory("search-validator");
    defaultAssertions(factory);
    Assert.assertFalse(factory.getConnectionConfig().getUseStartTLS());
    Assert.assertNull(factory.getConnectionConfig().getSslConfig());
    Assert.assertNull(factory.getConnectionConfig().getConnectionInitializers());
    SearchConnectionValidator validator = (SearchConnectionValidator) factory.getValidator();
    Assert.assertEquals(Duration.ofMinutes(15), validator.getValidatePeriod());
  }

  @Test
  public void handlersADFalse() throws Exception {
    String PROPERTY_DATA = "ldap.handlers-ad-false.url = ldap://localhost:10389\n"
        + "ldap.handlers-ad-false.tls = false\n"
        + "ldap.handlers-ad-false.isActiveDirectory = false\n"
        + "ldap.handlers-ad-false.searchResultHandlers = edu.internet2.middleware.grouper.ldap.handler.RangeSearchResultHandler, edu.vt.middleware.ldap.handler.EntryDnSearchResultHandler, org.ldaptive.handler.CaseChangeEntryHandler\n"
        + "ldap.handlers-ad-false.sizeLimit = 10\n"
        + "ldap.handlers-ad-false.timeLimit = PT5S\n"
        + "ldap.handlers-ad-false.connectTimeout = PT3S\n"
        + "ldap.handlers-ad-false.responseTimeout = PT3S\n"
        + "ldap.handlers-ad-false.minPoolSize = 0\n"
        + "ldap.handlers-ad-false.maxPoolSize = 5\n"
        + "ldap.handlers-ad-false.blockWaitTime = PT1S\n"
        + "ldap.handlers-ad-false.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    List<LdapEntryHandler> compare1 = List.of(new DnAttributeEntryHandler(), new CaseChangeEntryHandler());
    LdapEntryHandler[] entryHandlers = LdaptiveConfiguration.getConfig("handlers-ad-false").getLdapEntryHandlers();
    Assert.assertNotNull(entryHandlers);
    Assert.assertEquals(2, entryHandlers.length);
    Assert.assertTrue(Arrays.asList(entryHandlers).containsAll(compare1) && compare1.containsAll(Arrays.asList(entryHandlers)));

    List<SearchResultHandler> compare2 = List.of(new GrouperRangeEntryHandler());
    SearchResultHandler[] resultHandlers = LdaptiveConfiguration.getConfig("handlers-ad-false").getSearchResultHandlers();
    Assert.assertNotNull(resultHandlers);
    Assert.assertEquals(1, resultHandlers.length);
    Assert.assertTrue(Arrays.asList(resultHandlers).containsAll(compare2) && compare2.containsAll(Arrays.asList(resultHandlers)));
  }

  @Test
  public void handlersADTrue() throws Exception {
    String PROPERTY_DATA = "ldap.handlers-ad-true.url = ldap://localhost:10389\n"
        + "ldap.handlers-ad-true.tls = false\n"
        + "ldap.handlers-ad-true.isActiveDirectory = true\n"
        + "ldap.handlers-ad-true.sizeLimit = 10\n"
        + "ldap.handlers-ad-true.timeLimit = PT5S\n"
        + "ldap.handlers-ad-true.connectTimeout = PT3S\n"
        + "ldap.handlers-ad-true.responseTimeout = PT3S\n"
        + "ldap.handlers-ad-true.minPoolSize = 0\n"
        + "ldap.handlers-ad-true.maxPoolSize = 5\n"
        + "ldap.handlers-ad-true.blockWaitTime = PT1S\n"
        + "ldap.handlers-ad-true.pruneTimerPeriod = 420000";
    overrideProperties(PROPERTY_DATA);

    LdapEntryHandler[] entryHandlers = LdaptiveConfiguration.getConfig("handlers-ad-true").getLdapEntryHandlers();
    Assert.assertNull(entryHandlers);

    List<SearchResultHandler> compare = List.of(new GrouperRangeEntryHandler());
    SearchResultHandler[] resultHandlers = LdaptiveConfiguration.getConfig("handlers-ad-true").getSearchResultHandlers();
    Assert.assertNotNull(resultHandlers);
    Assert.assertEquals(1, resultHandlers.length);
    Assert.assertTrue(Arrays.asList(resultHandlers).containsAll(compare) && compare.containsAll(Arrays.asList(resultHandlers)));
  }

  private void defaultAssertions(PooledConnectionFactory factory) {
    Assert.assertEquals("ldap://localhost:10389", factory.getConnectionConfig().getLdapUrl());
    Assert.assertEquals(Duration.ofSeconds(3), factory.getConnectionConfig().getConnectTimeout());
    Assert.assertEquals(Duration.ofSeconds(3), factory.getConnectionConfig().getResponseTimeout());
    Assert.assertEquals(0, factory.getMinPoolSize());
    Assert.assertEquals(5, factory.getMaxPoolSize());
    Assert.assertTrue(factory.isValidatePeriodically());
    Assert.assertEquals(Duration.ofMinutes(7), factory.getPruneStrategy().getPrunePeriod());
    Assert.assertEquals(Duration.ofSeconds(1), factory.getBlockWaitTime());
  }
}
