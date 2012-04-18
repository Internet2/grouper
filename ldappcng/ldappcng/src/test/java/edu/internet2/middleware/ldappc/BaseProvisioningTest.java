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
 * Copyright 2010 University Corporation for Advanced Internet Development, Inc.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.directory.shared.ldap.util.LdapURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.internet2.middleware.subject.Subject;
import edu.vt.middleware.ldap.Ldap;

public abstract class BaseProvisioningTest extends GrouperTest {

  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(BaseProvisioningTest.class);

  /** Resource location for test configuration. */
  public String confDir;

  /** Base resource location. */
  public static final String TEST_PATH = "/test/edu/internet2/middleware/ldappc";

  /** Default properties resource. */
  public final static String PROPERTIES_RESOURCE = "ldappc.test.properties";

  /** properties key toggle for testing against Active Directory */
  public static final String TEST_USE_ACTIVE_DIRECTORY = "testUseActiveDirectory";

  /** properties key toggle for testing against an embedded ApacheDS server */
  public static final String TEST_USE_EMBEDDED = "testUseEmbeddedLdap";

  /** The base under which all information will be deleted during testing. */
  protected String base;

  /** Properties. */
  protected Properties properties;

  /** Properties file. */
  protected File propertiesFile;

  /** Connection to test LDAP server. */
  protected Ldap ldap;

  /** possibly empty list of embedded servers which need to be shutdown */
  private List<EmbeddedApacheDS> embeddedADSServers = new ArrayList<EmbeddedApacheDS>();

  public BaseProvisioningTest(String name, String confDir) {
    super(name);
    this.confDir = confDir;
  }
  
  /** Print test LDAP URL once. */
  private static boolean displayed;

  public void setUp() {
    super.setUp();

    propertiesFile = GrouperUtil.fileFromResourceName(confDir + "/" + PROPERTIES_RESOURCE);
    if (propertiesFile == null || !propertiesFile.exists() || !propertiesFile.canRead()) {
      throw new LdappcException("Unable to read file '" + confDir + "/" + PROPERTIES_RESOURCE + "'");
    }

    properties = GrouperUtil.propertiesFromFile(propertiesFile, false);
    LOG.debug("props {}", properties);

    base = GrouperUtil.propertiesValue(properties, "edu.vt.middleware.ldap.base");
    LOG.debug("base {}", base);

    String ldapUrl = GrouperUtil.propertiesValue(properties, "edu.vt.middleware.ldap.ldapUrl");
    
    if (useEmbedded()) {
      try {
        int port = new LdapURL(ldapUrl).getPort();
        EmbeddedApacheDS embeddedApacheDS = new EmbeddedApacheDS(port);
        embeddedApacheDS.startup();
        embeddedADSServers.add(embeddedApacheDS);
      } catch (Exception e) {
        e.printStackTrace();
        fail("Unable to startup embedded ApacheDS server : " + e.getMessage());
      }
    } else {
      // TODO prompting fails under maven
      String user = GrouperUtil.propertiesValue(properties, "edu.vt.middleware.ldap.serviceUser");
      // GrouperUtil.promptUserAboutChanges("test ldap and destroy everything under '"
      // + base + "'", true, "ldap", ldapUrl, user);

      if (!displayed) {
        System.out.println();
        System.out.println("Testing the following LDAP server - will delete everything !");
        System.out.println("ldap : " + ldapUrl);
        System.out.println("user : " + user);
        System.out.println("base : " + base);
        System.out.println();
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        displayed = true;
      }
    }
  }

  public void tearDown() {
    super.tearDown();

    LOG.debug("tearDown");

    for (EmbeddedApacheDS embeddedADS : embeddedADSServers) {
      try {
        LOG.debug("shutdown embedded ");
        embeddedADS.shutdown();
      } catch (Exception e) {
        e.printStackTrace();
        fail("Unable to startup embedded ApacheDS server : " + e.getMessage());
      }
    }
  }

  public void loadLdif(String file) throws Exception {
    LdappcTestHelper.loadLdif(LdappcTestHelper.getFile(file), propertiesFile, ldap);
  }

  public void verifyLdif(String pathToCorrectFile, Collection<String> normalizeDnAttributes) {
    verifyLdif(pathToCorrectFile, normalizeDnAttributes, null);
  }

  public void verifyLdif(String pathToCorrectFile, Collection<String> normalizeDnAttributes, String base) {

    if (base == null) {
      base = this.base;
    }

    try {
      String correctLdif = LdappcTestHelper.readFile(LdappcTestHelper.getFile(pathToCorrectFile));
      LdappcTestHelper.verifyLdif(correctLdif, propertiesFile, normalizeDnAttributes, base, ldap, false);
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
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

  /**
   * Create a <code>Subject</code> with the given id and name. Returns the newly created
   * subject.
   * 
   * @param id
   *          the subject id
   * @param name
   *          the subject name
   * @return the
   *         <code>Subject<code> or throws <code>SubjectNotFoundException<code> if the subject is not found.
   */
  public Subject createSubject(String id, String name) {

    RegistrySubject registrySubject = new RegistrySubject();
    registrySubject.setId(id);
    registrySubject.setName(name);
    registrySubject.setTypeString("person");

    registrySubject.getAttributes().put("name", GrouperUtil.toSet("name." + id));
    registrySubject.getAttributes().put("loginid", GrouperUtil.toSet("id." + id));
    registrySubject.getAttributes().put("description", GrouperUtil.toSet("description." + id));

    GrouperDAOFactory.getFactory().getRegistrySubject().create(registrySubject);

    return SubjectFinder.findById(id, true);
  }
}
