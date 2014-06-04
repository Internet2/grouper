/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouperActivemq;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.broker.region.Subscription;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.DestinationInfo;
import org.apache.activemq.command.Message;
import org.apache.activemq.command.ProducerInfo;


/**
 * my broker
 */
public class MyBroker extends BrokerFilter {

  /**
   * @param next1
   */
  public MyBroker(Broker next1) {
    super(next1);

  }
  
  /**
   * @see org.apache.activemq.broker.BrokerFilter#addDestinationInfo(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.DestinationInfo)
   */
  @Override
  public void addDestinationInfo(ConnectionContext context, DestinationInfo info)
      throws Exception {
    System.out.println("addDestinationInfo: ");
    super.addDestinationInfo(context, info);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#addDestination(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ActiveMQDestination, boolean)
   */
  @Override
  public Destination addDestination(ConnectionContext context,
      ActiveMQDestination destination, boolean create) throws Exception {
    System.out.println("addDestination: ");
    return super.addDestination(context, destination, create);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#removeDestination(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ActiveMQDestination, long)
   */
  @Override
  public void removeDestination(ConnectionContext context,
      ActiveMQDestination destination, long timeout) throws Exception {

    System.out.println("removeDestination: ");
    super.removeDestination(context, destination, timeout);
  }

  /**
   * @see org.apache.activemq.broker.BrokerFilter#addConsumer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ConsumerInfo)
   */
  @Override
  public Subscription addConsumer(ConnectionContext context, ConsumerInfo info)
      throws Exception {

    System.out.println("addConsumer: ");
    return super.addConsumer(context, info);
  }
  
  /**
   * @see org.apache.activemq.broker.BrokerFilter#addProducer(org.apache.activemq.broker.ConnectionContext, org.apache.activemq.command.ProducerInfo)
   */
  @Override
  public void addProducer(ConnectionContext context, ProducerInfo info) throws Exception {

    System.out.println("addProducer: ");
    super.addProducer(context, info);
  }

  /**
   * 
   * @see org.apache.activemq.broker.BrokerFilter#send(org.apache.activemq.broker.ProducerBrokerExchange, org.apache.activemq.command.Message)
   */
  @Override
  public void send(ProducerBrokerExchange producerExchange, Message messageSend)
      throws Exception {
    System.out.println("send: ");
    super.send(producerExchange, messageSend);
  }

}
