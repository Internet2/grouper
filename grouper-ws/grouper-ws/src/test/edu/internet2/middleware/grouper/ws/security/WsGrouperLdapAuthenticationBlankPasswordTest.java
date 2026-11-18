/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.ws.security;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;

/**
 * WS LDAP authentication (WsGrouperLdapAuthentication) must refuse a blank user or password and
 * a user name with the four colon source separator before anything goes to the directory, and
 * must escape the user name as an RDN value in the loginDnPrefix / loginDnSuffix mode (issue
 * wsLdapAuthnEmptyPasswordLogin).
 *
 * <p>No directory is needed: the LDAP config points at a local socket that counts connections and
 * closes them.  A refused login must not open a connection; a normal user and password must (the
 * control, which proves the counting works), and fails since nothing answers.  On releases with
 * ldaptive 1.x (2.6, 4.x, 5.0 to 5.5) a directory that allows unauthenticated binds answers an
 * empty password bind with success, which is the login this guards against.  Fails before the
 * fix: escapeDnAttributeValue does not exist, and the blank password opens a connection.</p>
 */
public class WsGrouperLdapAuthenticationBlankPasswordTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsGrouperLdapAuthenticationBlankPasswordTest("testRefusedBeforeDirectory"));
  }

  /**
   * @param name
   */
  public WsGrouperLdapAuthenticationBlankPasswordTest(String name) {
    super(name);
  }

  /** ws config keys this test overrides */
  private static final String[] WS_KEYS = new String[] {"ws.authn.ldap.grouperLoaderLdapConfigId",
      "ws.authn.ldap.loginDnPrefix", "ws.authn.ldap.loginDnSuffix", "ws.authn.ldap.findUserFilter",
      "ws.authn.ldap.findUserBase", "ws.authn.ldap.cacheResults"};

  /** ldap config id, unique per run so no pool from another run is reused */
  private String ldapConfigId = null;

  /** fake directory */
  private ServerSocket serverSocket = null;

  /** connections the fake directory accepted */
  private final AtomicInteger connections = new AtomicInteger();

  /**
   * remove overrides, stop the socket
   */
  @Override
  protected void tearDown() {
    for (String key : WS_KEYS) {
      GrouperWsConfig.retrieveConfig().propertiesOverrideMap().remove(key);
    }
    if (this.ldapConfigId != null) {
      for (String suffix : new String[] {"url", "user", "pass"}) {
        GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("ldap." + this.ldapConfigId + "." + suffix);
      }
    }
    if (this.serverSocket != null) {
      try {
        this.serverSocket.close();
      } catch (Exception e) {
        // ignore
      }
    }
    super.tearDown();
  }

  /**
   * RFC 4514 escaping of the user name in a DN
   */
  public void testEscapeDnAttributeValue() {
    assertEquals("jsmith", WsGrouperLdapAuthentication.escapeDnAttributeValue("jsmith"));
    assertEquals("jsmith\\,ou\\=admins", WsGrouperLdapAuthentication.escapeDnAttributeValue("jsmith,ou=admins"));
    assertEquals("a\\+cn\\=b", WsGrouperLdapAuthentication.escapeDnAttributeValue("a+cn=b"));
    assertEquals("\\\"q\\\"\\;\\<\\>\\\\", WsGrouperLdapAuthentication.escapeDnAttributeValue("\"q\";<>\\"));
    assertEquals("\\#x", WsGrouperLdapAuthentication.escapeDnAttributeValue("#x"));
    assertEquals("x#", WsGrouperLdapAuthentication.escapeDnAttributeValue("x#"));
    assertEquals("\\ a b\\ ", WsGrouperLdapAuthentication.escapeDnAttributeValue(" a b "));
    assertEquals("a\\00b", WsGrouperLdapAuthentication.escapeDnAttributeValue("a" + (char)0 + "b"));
    assertEquals("g:isa::::GrouperSystem", WsGrouperLdapAuthentication.escapeDnAttributeValue("g:isa::::GrouperSystem"));
    assertNull(WsGrouperLdapAuthentication.escapeDnAttributeValue(null));
  }

  /**
   * blank user or password, or the source separator, never reach the directory; a normal login does
   * @throws Exception
   */
  public void testRefusedBeforeDirectory() throws Exception {

    // fake directory: count and close every connection
    this.serverSocket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
    final ServerSocket theServerSocket = this.serverSocket;
    Thread acceptThread = new Thread(new Runnable() {

      public void run() {
        while (!theServerSocket.isClosed()) {
          try {
            Socket socket = theServerSocket.accept();
            WsGrouperLdapAuthenticationBlankPasswordTest.this.connections.incrementAndGet();
            socket.close();
          } catch (Exception e) {
            // closed
          }
        }
      }
    });
    acceptThread.setDaemon(true);
    acceptThread.start();

    this.ldapConfigId = "secTest177_" + System.currentTimeMillis();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap." + this.ldapConfigId + ".url",
        "ldap://127.0.0.1:" + this.serverSocket.getLocalPort());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap." + this.ldapConfigId + ".user", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap." + this.ldapConfigId + ".pass", "secret");

    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.authn.ldap.grouperLoaderLdapConfigId", this.ldapConfigId);
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.authn.ldap.loginDnPrefix", "uid=");
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.authn.ldap.loginDnSuffix", ",ou=People,dc=example,dc=edu");
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.authn.ldap.findUserFilter", "");
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.authn.ldap.findUserBase", "");
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put("ws.authn.ldap.cacheResults", "false");

    // refused, and nothing goes to the directory
    assertFalse("empty password", WsGrouperLdapAuthentication.authenticateLdap("jsmith", ""));
    assertFalse("blank password", WsGrouperLdapAuthentication.authenticateLdap("jsmith", "  "));
    assertFalse("null password", WsGrouperLdapAuthentication.authenticateLdap("jsmith", null));
    assertFalse("empty user", WsGrouperLdapAuthentication.authenticateLdap("", "secret"));
    assertFalse("root by source separator, empty password",
        WsGrouperLdapAuthentication.authenticateLdap("g:isa::::GrouperSystem", ""));
    assertFalse("root by source separator, with a password",
        WsGrouperLdapAuthentication.authenticateLdap("g:isa::::GrouperSystem", "secret"));

    WsGrouperLdapAuthentication wsGrouperLdapAuthentication = new WsGrouperLdapAuthentication();
    assertNull("Basic g:isa::::GrouperSystem with an empty password",
        wsGrouperLdapAuthentication.retrieveLoggedInSubjectId(requestWithBasicAuth("g:isa::::GrouperSystem", "")));
    assertNull("Basic jsmith with an empty password",
        wsGrouperLdapAuthentication.retrieveLoggedInSubjectId(requestWithBasicAuth("jsmith", "")));

    assertEquals("refused logins must not contact the directory", 0, this.connections.get());

    // control: a normal user and password goes to the directory (and fails, nothing answers)
    assertFalse("fake directory does not answer", WsGrouperLdapAuthentication.authenticateLdap("jsmith", "wrongPassword"));
    for (int i = 0; i < 50 && this.connections.get() == 0; i++) {
      Thread.sleep(100);
    }
    assertTrue("a normal login contacts the directory (control)", this.connections.get() > 0);
  }

  /**
   * a stand-in servlet request with only an Authorization header
   * @param user
   * @param pass
   * @return the request
   */
  private static HttpServletRequest requestWithBasicAuth(String user, String pass) {
    final String header = "Basic " + java.util.Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());
    return (HttpServletRequest)Proxy.newProxyInstance(WsGrouperLdapAuthenticationBlankPasswordTest.class.getClassLoader(),
        new Class[] {HttpServletRequest.class}, new InvocationHandler() {

          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getHeader".equals(method.getName()) && args != null && "Authorization".equalsIgnoreCase((String)args[0])) {
              return header;
            }
            if ("toString".equals(method.getName())) {
              return "stand-in request";
            }
            if (method.getReturnType() == boolean.class) {
              return false;
            }
            if (method.getReturnType() == int.class) {
              return 0;
            }
            if (method.getReturnType() == long.class) {
              return 0L;
            }
            return null;
          }
        });
  }

}
