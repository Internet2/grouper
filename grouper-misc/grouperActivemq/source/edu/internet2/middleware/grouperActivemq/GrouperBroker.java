/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouperActivemq;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.MessageReference;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DestinationInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.MessagePull;
import org.apache.activemq.command.ProducerInfo;
import org.apache.activemq.command.RemoveSubscriptionInfo;
import org.apache.activemq.command.Response;
import org.apache.activemq.command.SessionInfo;
import org.apache.activemq.command.TransactionId;
import org.apache.activemq.security.SecurityContext;

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
    assertAuthenticated(context);
    
    //lets make sure the user at least has some permissions...
    
    
    super.addConnection(context, info);
  }

  /**
   * make sure there is a user authenticated
   * @param context
   */
  public static void assertAuthenticated(ConnectionContext context) {
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
    assertAuthenticated(context);
    super.addSession(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#beginTransaction(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.TransactionId)
   */
  @Override
  public void beginTransaction(ConnectionContext context, TransactionId xid)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.beginTransaction(context, xid);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#commitTransaction(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.TransactionId, boolean)
   */
  @Override
  public void commitTransaction(ConnectionContext context, TransactionId xid,
      boolean onePhase) throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.commitTransaction(context, xid, onePhase);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#fastProducer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ProducerInfo, org.apache.activemq.command.ActiveMQDestination)
   */
  @Override
  public void fastProducer(ConnectionContext context, ProducerInfo producerInfo,
      ActiveMQDestination destination) {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.fastProducer(context, producerInfo, destination);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#forgetTransaction(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.TransactionId)
   */
  @Override
  public void forgetTransaction(ConnectionContext context, TransactionId transactionId)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.forgetTransaction(context, transactionId);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#getPreparedTransactions(org.apache.activemq.broker.ConnectionContext)
   */
  @Override
  public TransactionId[] getPreparedTransactions(ConnectionContext context)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    return super.getPreparedTransactions(context);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#messageConsumed(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.broker.region.MessageReference)
   */
  @Override
  public void messageConsumed(ConnectionContext context, MessageReference messageReference) {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.messageConsumed(context, messageReference);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#messageDelivered(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.broker.region.MessageReference)
   */
  @Override
  public void messageDelivered(ConnectionContext context,
      MessageReference messageReference) {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.messageDelivered(context, messageReference);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#messageDiscarded(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.broker.region.Subscription, org.apache.activemq.broker.region.MessageReference)
   */
  @Override
  public void messageDiscarded(ConnectionContext context, Subscription sub,
      MessageReference messageReference) {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.messageDiscarded(context, sub, messageReference);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#messageExpired(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.broker.region.MessageReference, org.apache.activemq.broker.region.Subscription)
   */
  @Override
  public void messageExpired(ConnectionContext context, MessageReference message,
      Subscription subscription) {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.messageExpired(context, message, subscription);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#messagePull(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.MessagePull)
   */
  @Override
  public Response messagePull(ConnectionContext context, MessagePull pull)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    return super.messagePull(context, pull);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#prepareTransaction(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.TransactionId)
   */
  @Override
  public int prepareTransaction(ConnectionContext context, TransactionId xid)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    return super.prepareTransaction(context, xid);
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
    super.removeConsumer(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeDestinationInfo(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.DestinationInfo)
   */
  @Override
  public void removeDestinationInfo(ConnectionContext context, DestinationInfo info)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
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
   * @see org.apache.activemq.broker.BrokerFilter#rollbackTransaction(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.TransactionId)
   */
  @Override
  public void rollbackTransaction(ConnectionContext context, TransactionId xid)
      throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.rollbackTransaction(context, xid);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#sendToDeadLetterQueue(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.broker.region.MessageReference, org.apache.activemq.broker.region.Subscription)
   */
  @Override
  public void sendToDeadLetterQueue(ConnectionContext context,
      MessageReference messageReference, Subscription subscription) {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.sendToDeadLetterQueue(context, messageReference, subscription);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#setAdminConnectionContext(org.apache.activemq.broker.ConnectionContext)
   */
  @Override
  public void setAdminConnectionContext(ConnectionContext adminConnectionContext) {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(adminConnectionContext);
    super.setAdminConnectionContext(adminConnectionContext);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#slowConsumer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.broker.region.Destination, org.apache.activemq.broker.region.Subscription)
   */
  @Override
  public void slowConsumer(ConnectionContext context, Destination destination,
      Subscription subs) {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.slowConsumer(context, destination, subs);
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
    assertAuthenticated(context);
    super.addDestinationInfo(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#addDestination(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ActiveMQDestination, boolean)
   */
  @Override
  public Destination addDestination(ConnectionContext context,
      ActiveMQDestination destination, boolean create) throws Exception {
    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    return super.addDestination(context, destination, create);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeDestination(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ActiveMQDestination, long)
   */
  @Override
  public void removeDestination(ConnectionContext context,
      ActiveMQDestination destination, long timeout) throws Exception {

    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    super.removeDestination(context, destination, timeout);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#addConsumer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConsumerInfo)
   */
  @Override
  public Subscription addConsumer(ConnectionContext context, ConsumerInfo info)
      throws Exception {

    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
    return super.addConsumer(context, info);
  }
  
  /**
   * @see org.apache.activemq.broker.BrokerFilter#addProducer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ProducerInfo)
   */
  @Override
  public void addProducer(ConnectionContext context, ProducerInfo info) throws Exception {

    //if you are not authenticated, then it is not allowed
    assertAuthenticated(context);
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
    assertAuthenticated(producerExchange.getConnectionContext());

    super.send(producerExchange, messageSend);
  }

  /**
   * does nothing
   * @param destination
   * @param operation
   * @param role
   */
  public void addDestinationRole(Destination destination, String operation, String role) {
  }
  
  /**
   * does nothing
   * @param destination
   * @param operation
   * @param role
   */
  public void removeDestinationRole(Destination destination, String operation, String role) {
  }
  
  /**
   * does nothing
   * @param role
   */
  public void addRole(String role) {
  }
  
  /**
   * does nothing
   * @param user
   * @param role
   */
  public void addUserRole(String user, String role) {
  }
  
  /**
   * does nothing
   * @param role
   */
  public void removeRole(String role) {
  }
  
  /**
   * does nothing
   * @param user
   * @param role
   */
  public void removeUserRole(String user, String role) {
  }

}
