/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package edu.internet2.middleware.ldappcTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.apache.commons.io.FileUtils;
import org.apache.directory.server.configuration.MutableServerStartupConfiguration;
import org.apache.directory.server.core.configuration.ShutdownConfiguration;
import org.apache.directory.server.jndi.ServerContextFactory;
import org.apache.directory.shared.ldap.exception.LdapConfigurationException;
import org.apache.directory.shared.ldap.ldif.Entry;
import org.apache.directory.shared.ldap.ldif.LdifReader;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.apache.mina.util.AvailablePortFinder;

/**
 * A simple testcase for testing JNDI provider functionality.
 * 
 * Modified from org.apache.directory.server.unit.AbstractServerTest to extend TestSetup
 * instead of TestCase.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev: 502690 $
 */
public abstract class AbstractServerTestSetup extends TestSetup {

  /** the context root for the system partition */
  protected LdapContext sysRoot;

  /** the context root for the rootDSE */
  protected LdapContext rootDSE;

  /** flag whether to delete database files for each test or not */
  protected boolean doDelete = true;

  protected MutableServerStartupConfiguration configuration = new MutableServerStartupConfiguration();

  protected int port = -1;

  public AbstractServerTestSetup(Test suite) {
    super(suite);
  }

  /**
   * Get's the initial context factory for the provider's ou=system context root.
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();

    doDelete(configuration.getWorkingDirectory());
    if (port == -1) {
      port = AvailablePortFinder.getNextAvailable(1024);
    }
    configuration.setLdapPort(port);
    configuration.setShutdownHookEnabled(false);
    setContexts("uid=admin,ou=system", "secret");
  }

  /**
   * Deletes the Eve working directory.
   */
  protected void doDelete(File wkdir) throws IOException {
    if (doDelete) {
      if (wkdir.exists()) {
        FileUtils.deleteDirectory(wkdir);
      }
      if (wkdir.exists()) {
        throw new IOException("Failed to delete: " + wkdir);
      }
    }
  }

  /**
   * Sets the contexts for this base class. Values of user and password used to set the
   * respective JNDI properties. These values can be overriden by the overrides
   * properties.
   * 
   * @param user
   *          the username for authenticating as this user
   * @param passwd
   *          the password of the user
   * @throws NamingException
   *           if there is a failure of any kind
   */
  protected void setContexts(String user, String passwd) throws NamingException {
    Hashtable env = new Hashtable(configuration.toJndiEnvironment());
    env.put(Context.SECURITY_PRINCIPAL, user);
    env.put(Context.SECURITY_CREDENTIALS, passwd);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.INITIAL_CONTEXT_FACTORY, ServerContextFactory.class.getName());
    setContexts(env);
  }

  /**
   * Sets the contexts of this class taking into account the extras and overrides
   * properties.
   * 
   * @param env
   *          an environment to use while setting up the system root.
   * @throws NamingException
   *           if there is a failure of any kind
   */
  protected void setContexts(Hashtable env) throws NamingException {
    Hashtable envFinal = new Hashtable(env);
    envFinal.put(Context.PROVIDER_URL, "ou=system");
    sysRoot = new InitialLdapContext(envFinal, null);

    envFinal.put(Context.PROVIDER_URL, "");
    rootDSE = new InitialLdapContext(envFinal, null);
  }

  /**
   * Sets the system context root to null.
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
    Hashtable env = new Hashtable();
    env.put(Context.PROVIDER_URL, "ou=system");
    env.put(Context.INITIAL_CONTEXT_FACTORY,
        "org.apache.directory.server.jndi.ServerContextFactory");
    env.putAll(new ShutdownConfiguration().toJndiEnvironment());
    env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
    env.put(Context.SECURITY_CREDENTIALS, "secret");
    try {
      new InitialContext(env);
    } catch (Exception e) {
    }

    sysRoot = null;
    doDelete(configuration.getWorkingDirectory());
    configuration = new MutableServerStartupConfiguration();
  }

  /**
   * Imports the LDIF entries packaged with the Eve JNDI provider jar into the newly
   * created system partition to prime it up for operation. Note that only ou=system
   * entries will be added - entries for other partitions cannot be imported and will blow
   * chunks.
   * 
   * @throws NamingException
   *           if there are problems reading the ldif file and adding those entries to the
   *           system partition
   */
  protected void importLdif(InputStream in) throws NamingException {
    try {
      Iterator iterator = new LdifReader(in);

      while (iterator.hasNext()) {
        Entry entry = (Entry) iterator.next();

        LdapDN dn = new LdapDN(entry.getDn());

        rootDSE.createSubcontext(dn, entry.getAttributes());
      }
    } catch (Exception e) {
      String msg = "failed while trying to parse system ldif file";
      NamingException ne = new LdapConfigurationException(msg);
      ne.setRootCause(e);
      throw ne;
    }
  }
}
