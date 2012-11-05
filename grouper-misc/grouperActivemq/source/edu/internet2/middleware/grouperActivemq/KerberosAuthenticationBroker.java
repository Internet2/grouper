/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouperActivemq;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.jaas.GroupPrincipal;
import org.apache.activemq.security.SecurityContext;

import edu.internet2.middleware.grouperActivemq.authn.GrouperActivemqKerberosAuthentication;

/**
 * my broker
 */
public class KerberosAuthenticationBroker extends BrokerFilter {

  /**
   * 
   */
  private final CopyOnWriteArrayList<SecurityContext> securityContexts = new CopyOnWriteArrayList<SecurityContext>();

  /**
   * @param next1
   */
  public KerberosAuthenticationBroker(Broker next1) {
    super(next1);

  }

  /**
   * 
   * @see org.apache.activemq.broker.BrokerFilter#addConnection(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConnectionInfo)
   */
  @Override
  public void addConnection(ConnectionContext context, ConnectionInfo info)
      throws Exception {

    SecurityContext s = context.getSecurityContext();
    if (s == null) {

      String username = info.getUserName();
      
      String password = info.getPassword();

      System.out.println("User: " + username + ", Password: " + password);
      
      boolean authenticated = false;
      
      try {
        authenticated = GrouperActivemqKerberosAuthentication.authenticateKerberos(username, password);
      } catch (Throwable e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
      if (!authenticated) {
        throw new SecurityException(
          "User name [" + username
              + "] or password is invalid.");
      }
      
      
      s = new SecurityContext(username) {
        
        @Override
        public Set<Principal> getPrincipals() {
          Set<Principal> groups = new HashSet<Principal>();
          groups.add(new GroupPrincipal("group"));
          return groups;
        }
      };
      
      context.setSecurityContext(s);
      this.securityContexts.add(s);
    }
    try {
      super.addConnection(context, info);
    } catch (Exception e) {
      this.securityContexts.remove(s);
      context.setSecurityContext(null);
      throw e;
    }
  }

  /**
   * 
   * @see org.apache.activemq.broker.BrokerFilter#removeConnection(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConnectionInfo, java.lang.Throwable)
   */
  @Override
  public void removeConnection(ConnectionContext context, ConnectionInfo info,
      Throwable error)
      throws Exception {
    super.removeConnection(context, info, error);
    if (this.securityContexts.remove(context.getSecurityContext())) {
      context.setSecurityContext(null);
    }
  }

}
