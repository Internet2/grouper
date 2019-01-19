package edu.internet2.middleware.grouperMessagingRabbitmq;

import com.rabbitmq.client.Connection;

public interface RabbitMQConnectionFactory {
  
  Connection getConnection(String messagingSystemName);
  
  void closeConnection(String messagingSystemName);

}
