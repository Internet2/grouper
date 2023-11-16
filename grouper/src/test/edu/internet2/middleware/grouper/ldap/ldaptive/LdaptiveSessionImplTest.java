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
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldif.LDIFReader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ldap.LdapAttribute;
import edu.internet2.middleware.grouper.ldap.LdapEntry;
import edu.internet2.middleware.grouper.ldap.LdapModificationItem;
import edu.internet2.middleware.grouper.ldap.LdapModificationType;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link LdaptiveSessionImpl}.
 */
public class LdaptiveSessionImplTest {

  private static final String TEST_LDAP_PROPERTIES = """
    ldap.testLDAP.url = ldap://localhost:%PORT%
    ldap.testLDAP.tls = false
    ldap.testLDAP.sizeLimit = 10
    ldap.testLDAP.timeLimit = PT5S
    ldap.testLDAP.connectTimeout = PT3S
    ldap.testLDAP.responseTimeout = PT3S
    ldap.testLDAP.minPoolSize = 2
    ldap.testLDAP.maxPoolSize = 5
    ldap.testLDAP.blockWaitTime = PT1S
    ldap.testLDAP.pruneTimerPeriod = 420000
    """;

  private static final String TEST_LDAP_LDIF = """
    dn: dc=internet2,dc=edu
    dc: internet2
    objectClass: dcObject
    objectClass: organization
    o: Internet2
      
    dn: ou=people,dc=internet2,dc=edu
    ou: people
    description: All people in organization
    objectclass: organizationalunit
      
    dn: uid=bbacharach,ou=people,dc=internet2,dc=edu
    objectclass: inetOrgPerson
    cn: Burt Bacharach
    givenName: Burt
    sn: Bacharach
    uid: bbacharach
    uidNumber: 1001
    userPassword: p@ssw0rd1
    mail: bbacharach@internet2.edu
    mail: burt.bacharach@internet2.edu
    telephoneNumber: 8169894342

    dn: uid=dwarwick,ou=people,dc=internet2,dc=edu
    objectclass: inetOrgPerson
    cn: Dionne Warwick
    givenName: Dionne
    sn: Warwick
    uid: dwarwick
    uidNumber: 1002
    userPassword: p@ssw0rd2
    mail: dwarwick@internet2.edu
    mail: dionne.warwick@internet2.edu
    telephoneNumber: 8169894343

    dn: uid=bmanilow,ou=people,dc=internet2,dc=edu
    objectclass: inetOrgPerson
    cn: Barry Manilow
    givenName: Barry
    sn: Manilow
    uid: bmanilow
    uidNumber: 1003
    userPassword: p@ssw0rd3
    mail: bmanilow@internet2.edu
    mail: barry.manilow@internet2.edu
    telephoneNumber: 8169894344

    dn: uid=gpitney,ou=people,dc=internet2,dc=edu
    objectclass: inetOrgPerson
    cn: Gene Pitney
    givenName: Gene
    sn: Pitney
    uid: gpitney
    uidNumber: 1004
    userPassword: p@ssw0rd4
    mail: gpitney@internet2.edu
    mail: gene.pitney@internet2.edu
    telephoneNumber: 8169894345

    """;

  /** Properties configuration id used for testing. */
  private static final String SERVER_ID = "testLDAP";

  /** LDAP server to test against. */
  private static InMemoryDirectoryServer server;

  /** Ephemeral port the LDAP server is listening on. */
  private static String serverPort;

  /** Object to test. */
  private static LdaptiveSessionImpl session;

  @BeforeClass
  public static void setup() throws Exception {
    final InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=internet2,dc=edu");
    config.setSchema(null);
    final InMemoryListenerConfig listenerConfig = new InMemoryListenerConfig(
      "in-memory-ldap",
      InetAddress.getLoopbackAddress(),
      0,
      null,
      null,
      null);
    config.setListenerConfigs(listenerConfig);
    server = new InMemoryDirectoryServer(config);
    server.importFromLDIF(
      true,
      new LDIFReader(new BufferedReader(new StringReader(TEST_LDAP_LDIF))));
    try {
      server.startListening();
      serverPort = String.valueOf(server.getListenPort());
    } catch (Exception e) {
      throw new RuntimeException("Could not start in memory LDAP server", e);
    }

    overrideProperties(TEST_LDAP_PROPERTIES.replaceAll("%PORT%", serverPort));
    session = new LdaptiveSessionImpl();
  }

  @AfterClass
  public static void teardown() {
    session.refreshConnectionsIfNeeded(SERVER_ID);
    if (server != null) {
      server.shutDown(true);
    }
  }

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
  public void list() {
    List<LdapEntry> entries = session.list(
      SERVER_ID,
      "ou=people,dc=internet2,dc=edu",
      LdapSearchScope.SUBTREE_SCOPE,
      "(uid=bbacharach)",
      new String[] {"uid", "givenName"},
      10);
    Assert.assertNotNull(entries);
    Assert.assertEquals(1, entries.size());
    Assert.assertEquals("uid=bbacharach,ou=people,dc=internet2,dc=edu", entries.get(0).getDn());
    Assert.assertEquals("bbacharach", entries.get(0).getAttribute("uid").getStringValues().iterator().next());
    Assert.assertEquals("Burt", entries.get(0).getAttribute("givenName").getStringValues().iterator().next());
  }

  @Test
  public void listAttribute() {
    List<String> mail = session.list(
      String.class,
      SERVER_ID,
      "ou=people,dc=internet2,dc=edu",
      LdapSearchScope.SUBTREE_SCOPE,
      "(uid=dwarwick)",
      "mail");
    List<String> compare = List.of("dwarwick@internet2.edu", "dionne.warwick@internet2.edu");
    Assert.assertNotNull(mail);
    Assert.assertEquals(2, mail.size());
    Assert.assertTrue(mail.containsAll(compare) && compare.containsAll(mail));
  }

  @Test
  public void listInObjects() {
    Map<String, List<String>> mail = session.listInObjects(
      String.class,
      SERVER_ID,
      "ou=people,dc=internet2,dc=edu",
      LdapSearchScope.SUBTREE_SCOPE,
      "(uid=b*)",
      "mail");
    List<String> compare1 = List.of("bbacharach@internet2.edu", "burt.bacharach@internet2.edu");
    List<String> compare2 = List.of("bmanilow@internet2.edu", "barry.manilow@internet2.edu");
    Assert.assertNotNull(mail);
    Assert.assertEquals(2, mail.size());
    Assert.assertTrue(
      mail.get("uid=bbacharach,ou=people,dc=internet2,dc=edu").containsAll(compare1) &&
      compare1.containsAll(mail.get("uid=bbacharach,ou=people,dc=internet2,dc=edu")));
    Assert.assertTrue(
      mail.get("uid=bmanilow,ou=people,dc=internet2,dc=edu").containsAll(compare2) &&
      compare2.containsAll(mail.get("uid=bmanilow,ou=people,dc=internet2,dc=edu")));
  }

  @Test
  public void read() {
    List<LdapEntry> entries = session.read(
      SERVER_ID,
      "ou=people,dc=internet2,dc=edu",
      List.of("uid=bmanilow,ou=people,dc=internet2,dc=edu", "uid=gpitney,ou=people,dc=internet2,dc=edu"),
      new String[] {"givenName", "sn"});
    Assert.assertNotNull(entries);
    Assert.assertEquals(2, entries.size());
    for (LdapEntry entry: entries) {
      if (entry.getDn().equals("uid=bmanilow,ou=people,dc=internet2,dc=edu")) {
        Assert.assertEquals("Barry", entry.getAttribute("givenName").getStringValues().iterator().next());
        Assert.assertEquals("Manilow", entry.getAttribute("sn").getStringValues().iterator().next());
      } else if (entry.getDn().equals("uid=gpitney,ou=people,dc=internet2,dc=edu")) {
        Assert.assertEquals("Gene", entry.getAttribute("givenName").getStringValues().iterator().next());
        Assert.assertEquals("Pitney", entry.getAttribute("sn").getStringValues().iterator().next());
      } else {
        Assert.fail("Unexpected entry: " + entry);
      }
    }
  }

  @Test
  public void authenticate() {
    session.authenticate(
      SERVER_ID,
      "uid=bmanilow,ou=people,dc=internet2,dc=edu",
      "p@ssw0rd3");
  }

  public void create(LdapEntry entry) {
    session.create(SERVER_ID, entry);
  }

  public void move(String oldDn, String newDn) {
    session.move(SERVER_ID, oldDn, newDn);
  }

  @Test
  public void delete() {
    LdapEntry entry = new LdapEntry("uid=hmancini,ou=people,dc=internet2,dc=edu");
    entry.addAttribute(new LdapAttribute("objectclass", "inetOrgPerson"));
    entry.addAttribute(new LdapAttribute("cn", "Henry Mancini"));
    entry.addAttribute(new LdapAttribute("givenName", "Henry"));
    entry.addAttribute(new LdapAttribute("sn", "Mancini"));
    entry.addAttribute(new LdapAttribute("uid", "hmancini"));
    entry.addAttribute(new LdapAttribute("uidNumber", "1005"));
    entry.addAttribute(new LdapAttribute("mail", "hmancini@internet2.edu"));
    entry.addAttribute(new LdapAttribute("telephoneNumber", "8169894346"));
    create(entry);
    move("uid=hmancini,ou=people,dc=internet2,dc=edu", "uid=hmancini2,ou=people,dc=internet2,dc=edu");
    session.delete(SERVER_ID, "uid=hmancini2,ou=people,dc=internet2,dc=edu");
  }

  @Test
  public void internal_modifyHelper() {
    LdapEntry entry = new LdapEntry("uid=hmancini,ou=people,dc=internet2,dc=edu");
    entry.addAttribute(new LdapAttribute("objectclass", "inetOrgPerson"));
    entry.addAttribute(new LdapAttribute("cn", "Henry Mancini"));
    entry.addAttribute(new LdapAttribute("givenName", "Henry"));
    entry.addAttribute(new LdapAttribute("sn", "Mancini"));
    entry.addAttribute(new LdapAttribute("uid", "hmancini"));
    entry.addAttribute(new LdapAttribute("uidNumber", "1005"));
    entry.addAttribute(new LdapAttribute("mail", "hmancini@internet2.edu"));
    entry.addAttribute(new LdapAttribute("telephoneNumber", "8169894346"));
    create(entry);
    session.internal_modifyHelper(
      SERVER_ID,
      "uid=hmancini,ou=people,dc=internet2,dc=edu",
      List.of(
        new LdapModificationItem(LdapModificationType.ADD_ATTRIBUTE, new LdapAttribute("mail", "henry.mancini@internet2.edu")),
        new LdapModificationItem(LdapModificationType.REMOVE_ATTRIBUTE, new LdapAttribute("telephoneNumber")),
        new LdapModificationItem(LdapModificationType.REPLACE_ATTRIBUTE, new LdapAttribute("uidNumber", "1015"))
      ));

    List<String> compare = List.of("hmancini@internet2.edu", "henry.mancini@internet2.edu");
    List<LdapEntry> entries = session.list(
      SERVER_ID,
      "ou=people,dc=internet2,dc=edu",
      LdapSearchScope.SUBTREE_SCOPE,
      "(uid=hmancini)",
      new String[] {"mail", "telephoneNumber", "uidNumber"},
      10);
    Assert.assertNotNull(entries);
    Assert.assertEquals(1, entries.size());
    Assert.assertEquals("uid=hmancini,ou=people,dc=internet2,dc=edu", entries.get(0).getDn());
    Assert.assertTrue(
      entries.get(0).getAttribute("mail").getStringValues().containsAll(compare) &&
        compare.containsAll(entries.get(0).getAttribute("mail").getStringValues()));
    Assert.assertEquals("1015", entries.get(0).getAttribute("uidNumber").getStringValues().iterator().next());
    Assert.assertTrue(entries.get(0).getAttribute("telephoneNumber").getStringValues().isEmpty());
  }

  @Test
  public void testConnection() {
    session.testConnection(SERVER_ID);
  }
}
