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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.apache.commons.logging.Log;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.ldif.LdifUtils;
import org.apache.directory.shared.ldap.name.LdapDN;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.ldappc.LdappcOptions.ProvisioningMode;
import edu.internet2.middleware.ldappc.util.LdapUtil;
import edu.internet2.middleware.ldappc.util.ResourceBundleUtil;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;

public class BaseLdappcTestCase extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(CRUDTest.class);

  public static final String LDAPPC_TEST_XML = "edu/internet2/middleware/ldappc/ldappc.test.xml";

  public static final String GROUPER_BASE_DN = "dc=grouper,dc=edu";

  protected GrouperSession grouperSession;

  protected Stem edu;

  protected Stem root;

  protected Ldappc ldappc;

  protected LdapContext ldapContext;

  private List<EmbeddedApacheDS> embeddedADSServers = new ArrayList<EmbeddedApacheDS>();

  protected String className = getClass().getSimpleName();
  
  public BaseLdappcTestCase(String name) {
    super(name);
  }

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

    LdappcOptions options = new LdappcOptions();
    options.setDoGroups(true);
    options.setDoMemberships(true);
    options.setIsTest(true);
    options.setSubjectId("GrouperSystem");

    if (file == null) {
      file = LDAPPC_TEST_XML;
    }

    String configManagerLocation = GrouperUtil.fileFromResourceName(file)
        .getAbsolutePath();
    options.setConfigManagerLocation(configManagerLocation);
    ConfigManager configuration = new ConfigManager(configManagerLocation);

    ldappc = new Ldappc(options, configuration, setUpLdapContext());
  }

  public LdapContext setUpLdapContext() throws Exception {

    if (useEmbedded()) {
      EmbeddedApacheDS embeddedApacheDS = new EmbeddedApacheDS(10389);
      embeddedApacheDS.startup();
      embeddedADSServers.add(embeddedApacheDS);
      ldapContext = embeddedApacheDS.getNewLdapContext();
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

    return ldapContext;
  }

  public void tearDown() {
    super.tearDown();

    try {
      tearDownDIT();
      if (ldapContext != null) {
        ldapContext.close();
      }
      if (useEmbedded()) {
        for (EmbeddedApacheDS embeddedADS : embeddedADSServers) {
          embeddedADS.shutdown();
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

  public File calculate(GroupDNStructure structure) throws Exception {

    File file = new File(ldappc.getOptions().getOutputFileLocation());
    if (file.exists()) {
      file.delete();
    }

    ((ConfigManager) ldappc.getConfig()).setGroupDnStructure(structure);

    return ldappc.calculate();
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

  public void print(File file) {

    try {
      BufferedReader in = new BufferedReader(new FileReader(file));
      String str;
      while ((str = in.readLine()) != null) {
        System.out.println(str);
      }
      in.close();
    } catch (IOException e) {
      fail("An error occurred : " + e);
    }
  }

  public void print(Map<String, BaseAttribute> map) {

    System.out.println();

    if (map == null) {
      System.out.println(map);
    } else {
      for (String key : map.keySet()) {
        for (Object value : map.get(key).getValues()) {
          System.out.println(key + " : '" + value + "'");
        }
      }
    }
    System.out.println();
  }

  public void provision(GroupDNStructure structure) throws Exception {

    ((ConfigManager) ldappc.getConfig()).setGroupDnStructure(structure);

    ldappc.provision();
  }
  
  public File dryRun(GroupDNStructure structure) throws Exception {

    ldappc.getOptions().setMode(ProvisioningMode.DRYRUN);
        
    File file = File.createTempFile("BaseLdappcTestCaseDryRun", null);    
    ldappc.getOptions().setOutputFileLocation(file.getAbsolutePath());
    file.delete();
    
    ((ConfigManager) ldappc.getConfig()).setGroupDnStructure(structure);

    return ldappc.dryRun();
  }

  /**
   * Returns true when testing against an embedded ApacheDS LDAP server.
   * 
   * @return
   */
  public boolean useEmbedded() {
    return ResourceBundleUtil.getString("testUseEmbeddedLdap").equalsIgnoreCase("true");
  }

  public void verify(String file) throws Exception {

    File ldifFile = new File(getClass().getResource(file).toURI());

    LdifReader reader = new LdifReader();

    Map<LdapDN, Entry> correctMap = buildMap(reader.parseLdifFile(ldifFile
        .getAbsolutePath()));

    Map<LdapDN, Entry> currentMap = buildMap(reader.parseLdif(getCurrentLdif()));

    for (LdapDN correctDN : correctMap.keySet()) {
      assertEquals("correct ", correctMap.get(correctDN), currentMap.get(correctDN));
    }

    for (LdapDN currentDN : currentMap.keySet()) {
      assertEquals("current", correctMap.get(currentDN), currentMap.get(currentDN));
    }
  }

  public void verify(String file, String ldif) throws Exception {

    File ldifFile = new File(getClass().getResource(file).toURI());

    LdifReader reader = new LdifReader();

    Map<LdapDN, Entry> correctMap = buildMap(reader.parseLdifFile(ldifFile
        .getAbsolutePath()));

    Map<LdapDN, Entry> currentMap = buildMap(reader.parseLdif(ldif));

    for (LdapDN dnA : correctMap.keySet()) {
      assertEquals("correct ", correctMap.get(dnA), currentMap.get(dnA));
    }

    for (LdapDN dnB : currentMap.keySet()) {
      assertEquals("current", correctMap.get(dnB), currentMap.get(dnB));
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
      ldif.append(LdapUtil.getLdif(attributes));
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
