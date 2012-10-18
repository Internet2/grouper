/**
 * @author mchyzer $Id$
 */
package edu.upenn.isc.activemq;

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

/**
 * my broker
 */
public class KerberosAuthenticationBroker extends BrokerFilter {

  private final CopyOnWriteArrayList<SecurityContext> securityContexts = new CopyOnWriteArrayList<SecurityContext>();

  /**
   * @param next1
   */
  public KerberosAuthenticationBroker(Broker next1) {
    super(next1);

  }

  public void addConnection(ConnectionContext context, ConnectionInfo info)
      throws Exception {

    SecurityContext s = context.getSecurityContext();
    if (s == null) {

      String username = info.getUserName();
      
      String password = info.getPassword();

      System.out.println("User: " + username + ", pass: " + password);
      
      s = new SecurityContext(username) {
        
        @Override
        public Set<Principal> getPrincipals() {
          Set<Principal> groups = new HashSet<Principal>();
          groups.add(new GroupPrincipal("anonymous"));
          return groups;
        }
      };
      
//          throw new SecurityException(
//                          "User name [" + info.getUserName()
//                              + "] or password is invalid.");

      context.setSecurityContext(s);
      securityContexts.add(s);
    }
    try {
      super.addConnection(context, info);
    } catch (Exception e) {
      securityContexts.remove(s);
      context.setSecurityContext(null);
      throw e;
    }
  }

  public void removeConnection(ConnectionContext context, ConnectionInfo info,
      Throwable error)
      throws Exception {
    super.removeConnection(context, info, error);
    if (securityContexts.remove(context.getSecurityContext())) {
      context.setSecurityContext(null);
    }
  }

}
