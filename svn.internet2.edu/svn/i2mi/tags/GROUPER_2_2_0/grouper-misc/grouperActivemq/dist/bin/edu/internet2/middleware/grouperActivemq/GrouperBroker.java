/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouperActivemq;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DestinationInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.RemoveSubscriptionInfo;
import org.apache.activemq.command.SessionInfo;
import org.apache.activemq.security.SecurityContext;

import edu.internet2.middleware.grouperActivemq.permissions.GrouperActivemqPermissionAction;
import edu.internet2.middleware.grouperActivemq.permissions.GrouperActivemqPermissionsEngine;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;

/**
 * my broker
 */
public class GrouperBroker extends BrokerFilter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperBroker.class);

  /**
   * @see org.apache.activemq.broker.BrokerFilter#addConnection(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConnectionInfo)
   */
  @Override
  public void addConnection(ConnectionContext context, ConnectionInfo info)
      throws Exception {
    
    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(context);
    
    super.addConnection(context, info);
  }

  /**
   * make sure there is a user authenticated
   * @param context
   */
  static void assertAuthenticatedAndHasPermissions(ConnectionContext context) {
    
    assertAuthenticated(context);
    
    if (!context.getSecurityContext().isBrokerContext()) {
      //lets make sure the user at least has some permissions...
      String userName = context.getSecurityContext().getUserName();
      if (!GrouperActivemqPermissionsEngine.hasAnyPermission(userName)) {
        throw new SecurityException("User does not have any permissions: " + userName);
      }
    }
  }

  static {
    try {
      GrouperActivemqPermissionsEngine.startupOnce();
    } catch (RuntimeException e) {
      LOG.error("Error starting up", e);
      throw e;
    }
  }
  
  /**
   * make sure there is a user authenticated
   * @param context
   */
  static void assertAuthenticated(ConnectionContext context) {
    
    GrouperActivemqPermissionsEngine.startupOnce();

    final SecurityContext securityContext = context.getSecurityContext();
    if (securityContext == null) {
      throw new SecurityException("User is not authenticated.");
    }

  }
  

  /**
   * @see org.apache.activemq.broker.BrokerFilter#addSession(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.SessionInfo)
   */
  @Override
  public void addSession(ConnectionContext context, SessionInfo info) throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(context);
    
    super.addSession(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeConnection(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConnectionInfo, java.lang.Throwable)
   */
  @Override
  public void removeConnection(ConnectionContext context, ConnectionInfo info,
      Throwable error) throws Exception {
    //well, should be able to remove connection if there is a context problem???
    //assertAuthenticated(context);
    super.removeConnection(context, info, error);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeConsumer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConsumerInfo)
   */
  @Override
  public void removeConsumer(ConnectionContext context, ConsumerInfo info)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    //you dont need permissions to unattach consumer
    super.removeConsumer(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeDestinationInfo(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.DestinationInfo)
   */
  @Override
  public void removeDestinationInfo(ConnectionContext context, DestinationInfo info)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(context);

    assertHasPermission(context, info.getDestination(), GrouperActivemqPermissionAction.deleteDestination);

    super.removeDestinationInfo(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeProducer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ProducerInfo)
   */
  @Override
  public void removeProducer(ConnectionContext context, ProducerInfo info)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.removeProducer(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeSession(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.SessionInfo)
   */
  @Override
  public void removeSession(ConnectionContext context, SessionInfo info) throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.removeSession(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeSubscription(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.RemoveSubscriptionInfo)
   */
  @Override
  public void removeSubscription(ConnectionContext context, RemoveSubscriptionInfo info)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.removeSubscription(context, info);
  }

  /**
   * @param next1
   */
  public GrouperBroker(Broker next1) {
    super(next1);

  }
  
  /**
   * @see org.apache.activemq.broker.BrokerFilter#addDestinationInfo(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.DestinationInfo)
   */
  @Override
  public void addDestinationInfo(ConnectionContext context, DestinationInfo info)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(context);

    assertHasPermission(context, info.getDestination(), GrouperActivemqPermissionAction.createDestination);

    super.addDestinationInfo(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#addDestination(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ActiveMQDestination, boolean)
   */
  @Override
  public Destination addDestination(ConnectionContext context,
      ActiveMQDestination destination, boolean create) throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(context);
    
    Destination existing = this.getDestinationMap().get(destination);
    if (existing == null) {
      assertHasPermission(context, destination, GrouperActivemqPermissionAction.createDestination);
    }
    
    return super.addDestination(context, destination, create);
  }

  /**
   * regex for converting a destination to 
   */
  private static Pattern destinationPattern = Pattern.compile("^(topic|queue)://(.*)$");
  
  /**
   * destination is queue://something.something.something
   * @param destination
   * @return the destination: something.something.something
   */
  static String destinationStringFromDestination(String destination) {
    
    Matcher matcher = destinationPattern.matcher(destination);
    if (!matcher.matches()) {
      throw new RuntimeException("Why does destination not match pattern? " + destination);
    }
    
    return matcher.group(2);
  }
  
  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeDestination(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ActiveMQDestination, long)
   */
  @Override
  public void removeDestination(ConnectionContext context,
      ActiveMQDestination destination, long timeout) throws Exception {

    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(context);
    
    assertHasPermission(context, destination, GrouperActivemqPermissionAction.deleteDestination);
    
    super.removeDestination(context, destination, timeout);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#addConsumer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConsumerInfo)
   */
  @Override
  public Subscription addConsumer(ConnectionContext context, ConsumerInfo info)
      throws Exception {

    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(context);

    assertHasPermission(context, info.getDestination(), GrouperActivemqPermissionAction.receiveMessage);
    
    return super.addConsumer(context, info);
  }

  /**
   * make sure the user has the permission
   * @param context
   * @param destination
   * @param action
   */
  static void assertHasPermission(ConnectionContext context, ActiveMQDestination destination, GrouperActivemqPermissionAction action) {

    if (!context.getSecurityContext().isBrokerContext() && destination != null) {

      String qualifiedName = destination.getQualifiedName();
  
      //could be comma separated???
      //ActiveMQ.Advisory.TempQueue,topic://ActiveMQ.Advisory.TempTopic

      //note, destinations cant have commas
      String[] qualifiedNames = GrouperClientUtils.splitTrim(qualifiedName, ",");
      
      for (String individualQualifiedName : qualifiedNames) {
      
        individualQualifiedName = destinationStringFromDestination(individualQualifiedName);
        
        String userName = context.getSecurityContext().getUserName();
  
        if (!GrouperActivemqPermissionsEngine.hasPermission(userName, action, individualQualifiedName)) {
          throw new SecurityException("User: " + userName + " does not have permission to : " + action + " on " + individualQualifiedName);
        }
      }
    }
  }
  
  /**
   * @see org.apache.activemq.broker.BrokerFilter#addProducer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ProducerInfo)
   */
  @Override
  public void addProducer(ConnectionContext context, ProducerInfo info) throws Exception {

    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(context);
    
    assertHasPermission(context, info.getDestination(), GrouperActivemqPermissionAction.sendMessage);
    
    super.addProducer(context, info);
  }

  /**
   * 
   * @see org.apache.activemq.broker.BrokerFilter#send(org.apache.activemq.broker.ProducerBrokerExchange, org.apache.activemq.command.Message)
   */
  @Override
  public void send(ProducerBrokerExchange producerExchange, Message messageSend)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticatedAndHasPermissions(producerExchange.getConnectionContext());

    assertHasPermission(producerExchange.getConnectionContext(), messageSend.getDestination(), GrouperActivemqPermissionAction.sendMessage);
    
    super.send(producerExchange, messageSend);
  }

}
