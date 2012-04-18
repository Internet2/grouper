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
package edu.internet2.middleware.ldappc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.ldap.handlers.bind.MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.plain.PlainMechanismHandler;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.constants.SupportedSaslMechanisms;
import org.apache.directory.shared.ldap.ldif.LdifEntry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.mina.util.AvailablePortFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;

/**
 * An embedded ApacheDS 1.5.5 Server
 * 
 */
public class EmbeddedApacheDS {

  /** logger */
  private static final Logger LOG = LoggerFactory.getLogger(EmbeddedApacheDS.class);

  /** the eduMember schema ldif */
  public static final String EDUMEMBER_SCHEMA = "/test/edu/internet2/middleware/ldappc/eduMember.ldif";

  /** the internal directory service */
  private DefaultDirectoryService directoryService;

  /** the internal ldap server */
  private LdapServer ldapServer;

  /** the port the server listens on **/
  private int port = -1;

  /** the base of the DIT */
  public static final String base = "dc=testgrouper,dc=edu";

  /**
   * Constructs an embedded ApacheDS server listening on the first available port greater
   * than 1024.
   * 
   * @throws Exception
   */
  public EmbeddedApacheDS() throws Exception {
  }

  /**
   * Constructs an embedded ApacheDS server listening on the given port.
   * 
   * @param port
   *          the port to listen on
   * @throws Exception
   */
  public EmbeddedApacheDS(int port) throws Exception {
    this.port = port;
  }

  /**
   * Start the server. Creates a temporary working directory which is deleted by calling
   * {@link #shutdown()}. Creates a "grouper" partition. Loads the eduMember schema.
   * 
   * @throws Exception
   */
  public void startup() throws Exception {
    directoryService = new DefaultDirectoryService();

    File workingDirectory = File.createTempFile("ldappcEmbeddedADS", null);
    LOG.debug("working directory is {}", workingDirectory.getAbsolutePath());
    if (!workingDirectory.delete()) {
      throw new LdappcException("Unable to delete working directory '"
          + workingDirectory.getAbsolutePath() + "'");
    }
    if (!workingDirectory.mkdirs()) {
      throw new LdappcException("Unable to make working directory '"
          + workingDirectory.getAbsolutePath() + "'");
    }
    FileUtils.forceDeleteOnExit(workingDirectory);

    directoryService.setWorkingDirectory(workingDirectory);
    
    // to fix end-of-test delay when running as application in Eclipse ?
    directoryService.setSyncPeriodMillis(0);

    // load eduMember schema
    File file = GrouperUtil.fileFromResourceName(EDUMEMBER_SCHEMA);
    if (file == null) {
      throw new RuntimeException("Unable to load eduMember schema from " + EDUMEMBER_SCHEMA);
    }
    List<LdifEntry> ldifEntries = new ArrayList<LdifEntry>();
    LdifReader ldifReader = new LdifReader(file);
    for (LdifEntry entry : ldifReader) {
      ldifEntries.add(entry);
    }
    directoryService.setTestEntries(ldifEntries);

    directoryService.startup();

    ldapServer = new LdapServer();
    ldapServer.setTransports(new TcpTransport(this.getPort()));
    ldapServer.setDirectoryService(directoryService);

    Map<String, MechanismHandler> mechanismHandlerMap = new HashMap<String, MechanismHandler>();
    mechanismHandlerMap.put(SupportedSaslMechanisms.PLAIN, new PlainMechanismHandler());
    ldapServer.setSaslMechanismHandlers(mechanismHandlerMap);

    // create partition
    Partition partition = new JdbmPartition();
    partition.setId("grouper");
    partition.setSuffix(base);
    directoryService.addPartition(partition);

    ldapServer.start();

    // add root entry
    ServerEntry entryFoo = directoryService.newEntry(new LdapDN(base));
    entryFoo.add("objectClass", "top", "domain", "extensibleObject");
    entryFoo.add("dc", "testgrouper");
    directoryService.getAdminSession().add(entryFoo);
  }

  /**
   * Shuts the server down. Deletes the temporary working directory.
   * 
   * @throws Exception
   */
  public void shutdown() throws Exception {
    if (ldapServer.isStarted()) {
      LOG.debug("stop ldap server");
      ldapServer.stop();
    }
    if (directoryService.isStarted()) {
      LOG.debug("shut down directory service");
      directoryService.shutdown();
    }
    if (directoryService.getWorkingDirectory().exists()) {
      LOG.debug("deleting working directory {}", directoryService.getWorkingDirectory());
      FileUtils.deleteDirectory(directoryService.getWorkingDirectory());
    }
  }

  /**
   * Gets a vt-ldap connection to the server.
   * 
   * @return the vt-ldap connection
   */
  public Ldap getNewLdap() {
    return new Ldap(new LdapConfig("ldap://127.0.0.1:" + ldapServer.getPort(), base));
  }

  /**
   * Gets the port the server is listening on.
   * 
   * @return the port
   */
  public int getPort() {
    if (port == -1) {
      port = AvailablePortFinder.getNextAvailable(1024);
    }

    return port;
  }

  /**
   * Gets properties of the form used by ldappc to connect to this server.
   * 
   * @return the properties
   */
  public Properties getProperties() {
    Properties properties = new Properties();
    properties.setProperty("base", base);
    properties.setProperty("edu.vt.middleware.ldap.ldapUrl", "ldap://127.0.0.1:" + getPort());
    properties.setProperty("edu.vt.middleware.ldap.authtype", "simple");
    properties.setProperty("edu.vt.middleware.ldap.serviceUser", ServerDNConstants.ADMIN_SYSTEM_DN);
    properties.setProperty("edu.vt.middleware.ldap.serviceCredential", "secret");
    properties.setProperty("edu.vt.middleware.ldap.base", base);
    properties.setProperty("testUseEmbeddedLdap", "true");
    return properties;
  }

  /**
   * Gets a temporary properties file of the form used by ldappc to connect to this
   * server. The file is deleted upon exit.
   * 
   * @return the temporary properties file
   * @throws IOException
   */
  public File getNewPropertiesFile(Properties properties) throws IOException {
    File tmpPropertiesFile = File.createTempFile("EmbeddedApacheDSProps", null);
    tmpPropertiesFile.deleteOnExit();
    properties.store(new FileOutputStream(tmpPropertiesFile), null);
    return tmpPropertiesFile;
  }
}
