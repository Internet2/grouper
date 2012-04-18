/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.NamingException;

import org.opensaml.util.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.LdappcConfig.GroupDNStructure;
import edu.internet2.middleware.ldappc.LdappcOptions.ProvisioningMode;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.vt.middleware.ldap.Ldap;

public abstract class BaseLdappcTestCase extends GrouperTest {

  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(CRUDTest.class);

  /** resource location for test data */
  public static final String TEST_PATH = "/test/edu/internet2/middleware/ldappc/";

  /** default properties resource */
  public static final String PROPERTIES_RESOURCE = TEST_PATH + "ldappc.test.properties";

  /** default configuration resource */
  public static final String CONFIG_RESOURCE = TEST_PATH + "ldappc.test.xml";

  /** default configuration resource for Active Directory */
  public static final String CONFIG_ACTIVE_DIRECTORY_RESOURCE = TEST_PATH
      + "ldappc.ad.xml";

  /** properties key toggle for testing against an embedded ApacheDS server */
  public static final String TEST_USE_EMBEDDED = "testUseEmbeddedLdap";

  /** properties key toggle for testing against Active Directory */
  public static final String TEST_USE_ACTIVE_DIRECTORY = "testUseActiveDirectory";

  /** path to config */
  protected String pathToConfig;

  /** path to properties */
  protected String pathToProperties;

  /** properties */
  protected Properties properties;

  /** properties file */
  protected File propertiesFile;

  /** the base under which all information will be deleted during testing */
  protected String base;

  /** the grouper session */
  protected GrouperSession grouperSession;

  /** edu stem */
  protected Stem edu;

  /** root stem */
  protected Stem root;

  /** ldappc */
  protected Ldappc ldappc;

  /** possibly empty list of embedded servers which need to be shutdown */
  private List<EmbeddedApacheDS> embeddedADSServers = new ArrayList<EmbeddedApacheDS>();

  /** delete all entries from ldap context after each test */
  private boolean tearDownContext = false;

  public BaseLdappcTestCase(String name) {
    super(name);
  }

  public void setUp() {
    super.setUp();

    // load the default properties
    properties = GrouperUtil.propertiesFromResourceName(PROPERTIES_RESOURCE, false, true);

    // path to default properties
    pathToProperties = GrouperUtil.fileFromResourceName(PROPERTIES_RESOURCE)
        .getAbsolutePath();

    // properties file
    propertiesFile = new File(pathToProperties);
    if (!propertiesFile.exists() || !propertiesFile.canRead()) {
      throw new LdappcException("Unable to read file '" + pathToProperties + "'");
    }

    // path to config
    if (useActiveDirectory()) {
      pathToConfig = GrouperUtil.fileFromResourceName(CONFIG_ACTIVE_DIRECTORY_RESOURCE)
          .getAbsolutePath();
    } else {
      pathToConfig = GrouperUtil.fileFromResourceName(CONFIG_RESOURCE).getAbsolutePath();
    }

    // setup grouper
    grouperSession = SessionHelper.getRootSession();
    root = StemHelper.findRootStem(grouperSession);
    edu = StemHelper.addChildStem(root, "edu", "education");
  }
  
  /** Print test LDAP URL once. */
  private static boolean displayed;

  public void setUpLdapContext() throws Exception {

    tearDownContext = true;

    Ldap ldap;

    if (useEmbedded()) {
      EmbeddedApacheDS embeddedApacheDS = new EmbeddedApacheDS();
      embeddedApacheDS.startup();
      embeddedADSServers.add(embeddedApacheDS);

      // override properties
      // properties = embeddedApacheDS.getProperties();
      Properties embeddedApacheDSProperties = embeddedApacheDS.getProperties();
      for (Object key : embeddedApacheDSProperties.keySet()) {
        properties.setProperty(key.toString(), embeddedApacheDSProperties.get(key)
            .toString());
      }
      pathToProperties = embeddedApacheDS.getNewPropertiesFile(properties).getPath();
      propertiesFile = new File(pathToProperties);

      LOG.debug("overriding properties for the embbeded ApacheDS server '{} '{}'",
          properties, pathToProperties);

      ldap = embeddedApacheDS.getNewLdap();
    } else {
      String providerUrl = GrouperUtil.propertiesValue(properties, "edu.vt.middleware.ldap.ldapUrl");
      String user = GrouperUtil.propertiesValue(properties, "edu.vt.middleware.ldap.serviceUser");

      base = GrouperUtil.propertiesValue(properties, "edu.vt.middleware.ldap.base");

      // maven doesn't read from stdin when forkMode is not never
      // GrouperUtil.promptUserAboutChanges("test ldap and destroy everything under '"
      // + base + "'", true, "ldap", providerUrl, user);

      if (!displayed) {
        System.out.println();
        System.out.println("Testing the following LDAP server - will delete everything !");
        System.out.println("ldap : " + providerUrl);
        System.out.println("user : " + user);
        System.out.println("base : " + base);        
        System.out.println();
        Thread.sleep(1000);
        displayed = true;
      }

      ldap = new Ldap();
      ldap.loadFromProperties(new FileInputStream(propertiesFile));
    }

    base = GrouperUtil.propertiesValue(properties, "edu.vt.middleware.ldap.base");
    LOG.debug("base '{}'", base);
    if (base == null || base.equals("")) {
      throw new LdappcException("Property base is required");
    }

    LdappcTestHelper.deleteChildren(base, ldap);

    ldap.close();
  }

  /**
   * Load the ConfigManager and LdapContext.
   * 
   * @param file
   *          the ldappc config file
   * @throws Exception
   */
  public void setUpLdappc(String pathToConfig, String pathToProperties) throws Exception {

    LdappcOptions options = new LdappcOptions();
    options.setDoGroups(true);
    if (!useActiveDirectory()) {
      options.setDoMemberships(true);
    }
    options.setIsTest(true);

    File file = GrouperUtil.fileFromResourceName(TEST_PATH
        + LdappcOptions.ATTRIBUTE_RESOLVER_FILE_NAME_INTERNAL);
    options.setAttributeResolverLocation(file.getParent());

    options.setConfigManagerLocation(pathToConfig);
    options.setPropertiesFileLocation(pathToProperties);

    ConfigManager configuration = new ConfigManager(pathToConfig, pathToProperties);

    if (useEmbedded()) {
      configuration.setBundleModifications(false);
    }

    ldappc = new Ldappc(options, configuration, null);
  }

  public void setUpLdappc(String configResource) throws Exception {

    File file = GrouperUtil.fileFromResourceName(TEST_PATH + configResource);
    if (file == null) {
      throw new LdappcException("Unable to find resource '" + configResource + "'");
    }

    setUpLdappc(file.getAbsolutePath(), pathToProperties);
  }

  public void tearDown() {
    super.tearDown();

    try {
      if (ldappc != null && tearDownContext) {
        Ldap ldap = ldappc.getContext();
        LdappcTestHelper.deleteChildren(base, ldap);
        ldap.close();
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

  public File calculate(GroupDNStructure structure) throws Exception {
    return calculate(structure, false);
  }

  public File calculate(GroupDNStructure structure, boolean printFile) throws Exception {

    File file = new File(ldappc.getOptions().getOutputFileLocation());
    if (file.exists()) {
      file.delete();
    }
    file.deleteOnExit();

    ((ConfigManager) ldappc.getConfig()).setGroupDnStructure(structure);

    file = ldappc.calculate();

    if (printFile) {
      System.out.println(LdappcTestHelper.readFile(file));
    }

    return file;
  }

  public File dryRun(GroupDNStructure structure) throws Exception {
    return dryRun(structure, false);
  }

  public File dryRun(GroupDNStructure structure, boolean printFile) throws Exception {

    ldappc.getOptions().setMode(ProvisioningMode.DRYRUN);

    File file = File.createTempFile("BaseLdappcTestCaseDryRun", null);
    ldappc.getOptions().setOutputFileLocation(file.getAbsolutePath());
    file.delete();

    ((ConfigManager) ldappc.getConfig()).setGroupDnStructure(structure);

    file = ldappc.dryRun();

    if (printFile) {
      System.out.println(LdappcTestHelper.readFile(file));
      file.deleteOnExit();
    }

    return file;
  }

  public void provision(GroupDNStructure structure) throws Exception {
    provision(structure, false);
  }

  public void provision(GroupDNStructure structure, boolean printFile) throws Exception {

    ldappc.getOptions().setMode(ProvisioningMode.PROVISION);

    ((ConfigManager) ldappc.getConfig()).setGroupDnStructure(structure);

    if (printFile) {
      ((LdappcOptions) ldappc.getOptions()).setLogLdif(true);
    }

    ldappc.provision();
  }

  /**
   * Get the file with the given resource name.
   * 
   * If {@link #useActiveDirectory()} is true, then will look for files under
   * {@link #TEST_PATH}/data/ad first then {@link #TEST_PATH}/data.
   * 
   * @param resourceName
   * @return
   */
  public File getFile(String resourceName) {
    File file = null;
    if (useActiveDirectory()) {
      file = GrouperUtil.fileFromResourceName(TEST_PATH + "data/ad/" + resourceName);
    }
    if (file == null) {
      file = GrouperUtil.fileFromResourceName(TEST_PATH + "data/" + resourceName);
      if (file == null) {
        throw new RuntimeException("Unable to find file '" + resourceName + "'");
      }
    }
    return file;
  }

  public void loadLdif(String file) throws Exception {
    loadLdif(file, ldappc.getContext());
  }

  public void loadLdif(String correctFile, Ldap ldap) throws Exception {

    LdappcTestHelper.loadLdif(getFile(correctFile), propertiesFile, ldap);
  }

  /**
   * Returns true when testing against Active Directory, as configured in the properties
   * file with the key {@value #TEST_USE_ACTIVE_DIRECTORY}.
   * 
   * @return
   */
  public boolean useActiveDirectory() {
    return GrouperUtil.propertiesValueBoolean(properties, TEST_USE_ACTIVE_DIRECTORY,
        false);
  }

  /**
   * Returns true when testing against an embedded ApacheDS LDAP server, as configured in
   * the properties file using the key {@value #TEST_USE_EMBEDDED}.
   * 
   * @return
   */
  public boolean useEmbedded() {    
    return GrouperUtil.propertiesValueBoolean(properties, TEST_USE_EMBEDDED, false);
  }

  public void verifyLdif(String pathToCorrectFile) {
    try {
      ArrayList<String> normalizeDnAttributes = new ArrayList<String>();
      normalizeDnAttributes.add(ldappc.getConfig().getGroupMembersDnListAttribute());

      String correctLdif = LdappcTestHelper.readFile(getFile(pathToCorrectFile));

      LdappcTestHelper.verifyLdif(correctLdif, propertiesFile, normalizeDnAttributes,
          base, ldappc.getContext(), useActiveDirectory());
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
  }

  public void verifyLdif(String correctFile, File currentFile) throws NamingException,
      ResourceException, IOException {

    ArrayList<String> dnAttributeNames = new ArrayList<String>();
    dnAttributeNames.add(ldappc.getConfig().getGroupMembersDnListAttribute());

    LdappcTestHelper.verifyLdif(getFile(correctFile), currentFile, propertiesFile,
        dnAttributeNames, useActiveDirectory());
  }
}
