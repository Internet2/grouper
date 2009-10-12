package edu.internet2.middleware.ldappc;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.ldap.handlers.bind.MechanismHandler;
import org.apache.directory.server.ldap.handlers.bind.plain.PlainMechanismHandler;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.constants.SupportedSaslMechanisms;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.mina.util.AvailablePortFinder;
import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.exception.LdappcException;
import edu.vt.middleware.ldap.Ldap;
import edu.vt.middleware.ldap.LdapConfig;

public class EmbeddedApacheDS {

  private static final Logger LOG = GrouperUtil.getLogger(EmbeddedApacheDS.class);

  public static final String EDUMEMBER_SCHEMA = "edu/internet2/middleware/ldappc/eduMember.ldif";

  private DirectoryService directoryService;

  private LdapServer ldapServer;

  private int port = -1;

  public static final String base = "dc=grouper,dc=edu";

  public EmbeddedApacheDS() throws Exception {
  }

  public EmbeddedApacheDS(int port) throws Exception {
    this.port = port;
  }

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

    ldapServer = new LdapServer();
    ldapServer.setTransports(new TcpTransport(this.getPort()));
    ldapServer.setDirectoryService(directoryService);

    Map<String, MechanismHandler> mechanismHandlerMap = new HashMap<String, MechanismHandler>();
    mechanismHandlerMap.put(SupportedSaslMechanisms.PLAIN, new PlainMechanismHandler());
    ldapServer.setSaslMechanismHandlers(mechanismHandlerMap);

    directoryService.startup();

    // create partition
    Partition partition = new JdbmPartition();
    partition.setId("grouper");
    partition.setSuffix(base);
    directoryService.addPartition(partition);

    ldapServer.start();

    // add root entry
    ServerEntry entryFoo = directoryService.newEntry(new LdapDN(base));
    entryFoo.add("objectClass", "top", "domain", "extensibleObject");
    entryFoo.add("dc", "grouper");
    directoryService.getAdminSession().add(entryFoo);

    // load eduMember schema
    LdapContext lc = getNewLdapContext();
    LdappcTestHelper.loadLdif(GrouperUtil.fileFromResourceName(EDUMEMBER_SCHEMA), lc);
    lc.close();
  }

  public void shutdown() throws Exception {
    if (ldapServer.isStarted()) {
      ldapServer.stop();
    }
    if (directoryService.isStarted()) {
      directoryService.shutdown();
    }
    if (directoryService.getWorkingDirectory().exists()) {
      LOG.debug("deleting working directory {}", directoryService.getWorkingDirectory());
      FileUtils.deleteDirectory(directoryService.getWorkingDirectory());
    }
  }

  public Ldap getNewLdap() {
    return new Ldap(new LdapConfig("ldap://127.0.0.1:" + ldapServer.getPort(), base));
  }

  public LdapContext getNewLdapContext() throws NamingException {
    // create ldap context
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://127.0.0.1:" + ldapServer.getPort());
    env.put(Context.SECURITY_PRINCIPAL, ServerDNConstants.ADMIN_SYSTEM_DN);
    env.put(Context.SECURITY_CREDENTIALS, "secret");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");

    return new InitialLdapContext(env, null);
  }

  public int getPort() {
    if (port == -1) {
      port = AvailablePortFinder.getNextAvailable(1024);
    }

    return port;
  }
}
