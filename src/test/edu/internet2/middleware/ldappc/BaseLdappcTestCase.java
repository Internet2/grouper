/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapService;
import org.apache.directory.server.ldap.handlers.bind.MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.plain.PlainMechanismHandler;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.constants.SupportedSaslMechanisms;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.ldif.LdifUtils;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.mina.util.AvailablePortFinder;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.ResourceBundleUtil;
import edu.internet2.middleware.ldappc.util.SubjectCache;

public class BaseLdappcTestCase extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(CreateGroupTest.class);

  public static final String LDAPPC_BUSHY_XML = "edu/internet2/middleware/ldappc/ldappc.test.bushy.xml";

  public static final String LDAPPC_FLAT_XML = "edu/internet2/middleware/ldappc/ldappc.test.flat.xml";

  public static final String EDUMEMBER_SCHEMA = "edu/internet2/middleware/ldappc/eduMember.ldif";

  public static final String GROUPER_BASE_DN = "dc=grouper,dc=edu";

  protected GrouperSession grouperSession;

  protected Stem edu;

  protected Stem root;

  protected LdapContext ldapContext;

  private DirectoryService directoryService;

  private LdapService ldapService;

  protected ConfigManager configuration;

  public void setUp() {
    super.setUp();
    grouperSession = SessionHelper.getRootSession();
    root = StemHelper.findRootStem(grouperSession);
    edu = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * Load the ConfigManager and LdapContext.
   * 
   * @param file
   *          the ldappc config file
   * @throws Exception
   */
  public void setUpLdappc(String file) throws Exception {

    configuration = ConfigManager.load(GrouperUtil.fileFromResourceName(file)
        .getAbsolutePath());

    if (useEmbedded()) {
      this.setUpEmbeddedServer();
    } else {
      String url = ResourceBundleUtil.getString("test.provider_url");
      String user = ResourceBundleUtil.getString("test.security_principal");

      GrouperUtil.promptUserAboutChanges("test ldap", true, "ldap", url, user);

      Hashtable<String, String> env = new Hashtable<String, String>();
      env.put(Context.INITIAL_CONTEXT_FACTORY, ResourceBundleUtil
          .getString("test.initial_context_factory"));
      env.put(Context.PROVIDER_URL, url);
      env.put(Context.SECURITY_AUTHENTICATION, ResourceBundleUtil
          .getString("test.security_authentication"));
      env.put(Context.SECURITY_PRINCIPAL, user);
      env.put(Context.SECURITY_CREDENTIALS, ResourceBundleUtil
          .getString("test.security_credentials"));
      ldapContext = LdapUtil.getLdapContext(env, null);
    }
  }

  private void setUpEmbeddedServer() throws Exception {

    directoryService = new DefaultDirectoryService();
    directoryService.setWorkingDirectory(new File(ResourceBundleUtil
        .getString("testLdapWorkingDirectory")));
    directoryService.setShutdownHookEnabled(false);
    int port = AvailablePortFinder.getNextAvailable(1024);
    ldapService = new LdapService();
    ldapService.setTcpTransport(new TcpTransport(port));
    ldapService.setDirectoryService(directoryService);

    Map<String, MechanismHandler> mechanismHandlerMap = new HashMap<String, MechanismHandler>();
    mechanismHandlerMap.put(SupportedSaslMechanisms.PLAIN, new PlainMechanismHandler());
    ldapService.setSaslMechanismHandlers(mechanismHandlerMap);

    // reset working directory
    if (directoryService.getWorkingDirectory().exists()) {
      FileUtils.deleteDirectory(directoryService.getWorkingDirectory());
      if (directoryService.getWorkingDirectory().exists()) {
        fail("can't delete " + directoryService.getWorkingDirectory());
      }
    }

    directoryService.startup();

    // create partition
    Partition partition = new JdbmPartition();
    partition.setId("grouper");
    partition.setSuffix(GROUPER_BASE_DN);
    directoryService.addPartition(partition);

    ldapService.start();

    // add root entry
    ServerEntry entryFoo = directoryService.newEntry(new LdapDN(GROUPER_BASE_DN));
    entryFoo.add("objectClass", "top", "domain", "extensibleObject");
    entryFoo.add("dc", "grouper");
    directoryService.getAdminSession().add(entryFoo);

    // create ldap context
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://localhost:" + ldapService.getPort());
    env.put(Context.SECURITY_PRINCIPAL, ServerDNConstants.ADMIN_SYSTEM_DN);
    env.put(Context.SECURITY_CREDENTIALS, "secret");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");

    ldapContext = new InitialLdapContext(env, null);

    // load eduMember schema
    ldifLoad(GrouperUtil.fileFromResourceName(EDUMEMBER_SCHEMA));
  }

  public void tearDown() {
    super.tearDown();

    try {
      tearDownDIT();
      if (useEmbedded()) {
        ldapService.stop();
        directoryService.shutdown();
        if (directoryService.getWorkingDirectory().exists()) {
          FileUtils.deleteDirectory(directoryService.getWorkingDirectory());
        }
      }
    } catch (Exception e) {
      fail("An error occurred " + e.getMessage());
    }
  }

  /**
   * Destroy everything under {@link #GROUPER_BASE_DN}.
   * 
   * @throws Exception
   */
  public void tearDownDIT() throws Exception {
    List<String> toDelete = getChildDNs(GROUPER_BASE_DN);
    for (String dn : toDelete) {
      LOG.info("destroy " + dn);
      ldapContext.destroySubcontext(dn);
    }
  }

  /**
   * Return a list of child DNs under the given DN, in (reverse) order suitable for
   * deletion.
   * 
   * @param name
   *          the top level DN
   * @return
   * @throws NamingException
   */
  public List<String> getChildDNs(String name) throws NamingException {
    ArrayList<String> tree = new ArrayList<String>();

    SearchControls ctrls = new SearchControls();
    ctrls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
    ctrls.setReturningAttributes(new String[] {});

    NamingEnumeration<SearchResult> results = ldapContext.search(name, "objectclass=*",
        ctrls);

    while (results.hasMore()) {
      SearchResult result = results.next();
      tree.addAll(getChildDNs(result.getNameInNamespace()));
      tree.add(result.getNameInNamespace());
    }

    return tree;
  }

  public GrouperProvisioner getGrouperProvisioner() throws Exception {
    return getGrouperProvisioner(true, true);
  }

  public GrouperProvisioner getGrouperProvisioner(boolean doGroups, boolean doMemberships)
      throws Exception {

    InputOptions options = new InputOptions();
    options.setDoGroups(doGroups);
    options.setDoMemberships(doMemberships);
    options.setIsTest(true);
    options.setSubjectId("GrouperSystem");

    return new GrouperProvisioner(configuration, options, ldapContext, new SubjectCache());
  }

  /**
   * Return the name of the running method.
   * 
   * @param e
   * @return
   */
  public String getRunningMethodName(StackTraceElement e[]) {
    boolean doNext = false;
    for (StackTraceElement s : e) {
      if (doNext) {
        return s.getMethodName();
      }
      doNext = s.getMethodName().equals("getStackTrace");
    }
    return null;
  }

  /**
   * Return a File with a name of the form class.test.after.ldif
   * 
   * @param e
   * @return
   * @throws URISyntaxException
   */
  public File ldifAfterFile(StackTraceElement e[]) throws URISyntaxException {

    String file = getClass().getSimpleName() + "." + getRunningMethodName(e)
        + ".after.ldif";

    return new File(getClass().getResource(file).toURI());
  }

  /**
   * Return a File with a name of the form class.test.before.ldif
   * 
   * @param e
   * @return
   * @throws URISyntaxException
   */
  public File ldifBeforeFile(StackTraceElement e[]) throws URISyntaxException {
    String file = getClass().getSimpleName() + "." + getRunningMethodName(e)
        + ".before.ldif";

    return new File(getClass().getResource(file).toURI());
  }

  public void ldifLoad(File file) throws Exception {

    LdifReader ldifReader = new LdifReader(file);
    for (LdifEntry entry : ldifReader) {
      Attributes attributes = new BasicAttributes(true);
      for (EntryAttribute entryAttribute : entry.getEntry()) {
        BasicAttribute attribute = new BasicAttribute(entryAttribute.getId());
        Iterator<Value<?>> values = entryAttribute.getAll();
        while (values.hasNext()) {
          attribute.add(values.next().get());
        }
        attributes.put(attribute);
      }
      LOG.debug("creating '" + entry.getDn().toString() + " " + attributes);
      ldapContext.createSubcontext(entry.getDn().toString(), attributes);
    }
  }

  public void print() throws Exception {

    SearchControls controls = new SearchControls();
    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    NamingEnumeration<SearchResult> results = ldapContext.search(GROUPER_BASE_DN,
        "objectclass=*", controls);
    while (results.hasMore()) {
      SearchResult searchResult = results.next();
      System.out.println(LdifUtils.convertToLdif(searchResult.getAttributes(),
          new LdapDN(searchResult.getNameInNamespace())));
    }
  }

  public void provision() throws Exception {
    getGrouperProvisioner().provision();
  }

  /**
   * Returns true when testing against an embedded ApacheDS LDAP server.
   * 
   * @return
   */
  public boolean useEmbedded() {
    return ResourceBundleUtil.getString("testUseEmbeddedLdap").equalsIgnoreCase("true");
  }

  public void verify(File file) throws Exception {

    LdifReader reader = new LdifReader();

    Map<LdapDN, Entry> correctMap = buildMap(reader.parseLdifFile(file.getAbsolutePath()));

    Map<LdapDN, Entry> currentMap = buildMap(reader.parseLdif(getCurrentLdif()));

    for (LdapDN correctDN : correctMap.keySet()) {
      assertEquals("correct ", correctMap.get(correctDN), currentMap.get(correctDN));
    }

    for (LdapDN currentDN : currentMap.keySet()) {
      assertEquals("current", correctMap.get(currentDN), currentMap.get(currentDN));
    }

  }

  /**
   * Return an LDIF representation of the entire DIT.
   * 
   * @return
   * @throws NamingException
   */
  private String getCurrentLdif() throws NamingException {

    StringBuffer ldif = new StringBuffer();

    List<String> currentDns = getChildDNs(GROUPER_BASE_DN);

    for (String currentDn : currentDns) {
      ldif.append("dn: " + currentDn + "\n");
      Attributes attributes = ldapContext.getAttributes(currentDn);
      NamingEnumeration<String> ids = attributes.getIDs();
      while (ids.hasMore()) {
        String id = ids.next();
        Attribute attribute = attributes.get(id);
        NamingEnumeration<?> values = attribute.getAll();
        while (values.hasMore()) {
          String value = values.next().toString();
          ldif.append(id + ": " + value + "\n");
        }
      }
      ldif.append("\n");
    }

    return ldif.toString();
  }

  private Map<LdapDN, Entry> buildMap(List<LdifEntry> ldifEntries) throws NamingException {

    Map<LdapDN, Entry> map = new HashMap<LdapDN, Entry>();

    for (LdifEntry ldifEntry : ldifEntries) {
      Entry entry = ldifEntry.getEntry();
      if (entry.contains("objectclass", "top")) {
        entry.remove("objectclass", "top");
      }
      map.put(ldifEntry.getDn(), entry);
    }

    return map;
  }

}
