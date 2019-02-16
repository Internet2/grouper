package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public enum RabbitMQConnectionFactoryImpl implements RabbitMQConnectionFactory {
  
  INSTANCE {
    
    private Map<String, Connection> messagingSystemNameConnection = new HashMap<String, Connection>();
    
    @Override
    public Connection getConnection(String messagingSystemName) {

      if (StringUtils.isBlank(messagingSystemName)) {
        throw new IllegalArgumentException("messagingSystemName is required.");
      }
      
      Connection connection =  messagingSystemNameConnection.get(messagingSystemName);
      
      synchronized(RabbitMQConnectionFactory.class) {
        if (connection != null && !connection.isOpen()) {
          connection = null;
        }
        
        if (connection == null || !connection.isOpen()) {
          
          GrouperMessagingConfig grouperMessagingConfig = GrouperClientConfig.retrieveConfig().retrieveGrouperMessagingConfigNonNull(messagingSystemName);

          
          String host = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "host");
          String virtualHost = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "virtualhost");
          String username = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "username");
          String password = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "password");
          
          String tlsVersion = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "tlsVersion");
          String pathToTrustStore = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "pathToTrustStore");
          String trustPassphrase = grouperMessagingConfig.propertyValueString(GrouperClientConfig.retrieveConfig(), "trustPassphrase");
          
          if (StringUtils.isNotBlank(password)) {
            password = GrouperClientUtils.decryptFromFileIfFileExists(password, null);
          }
          int port = grouperMessagingConfig.propertyValueInt(GrouperClientConfig.retrieveConfig(), "port", -1);
          
          try {
            ConnectionFactory factory = new ConnectionFactory();
           
            if (StringUtils.isNotEmpty(host)) {
              factory.setHost(host);
            }
            
            if (StringUtils.isNotEmpty(virtualHost)) {
              factory.setVirtualHost(virtualHost);
            }
            
            if (StringUtils.isNotEmpty(username)) {
              factory.setUsername(username);
            }
            
            if (StringUtils.isNotEmpty(password)) {
              factory.setPassword(password);
            }
            
            if (port != -1) {
              factory.setPort(port);
            }

            if (StringUtils.isNotEmpty(pathToTrustStore) && StringUtils.isNotEmpty(trustPassphrase)
                && StringUtils.isNotEmpty(tlsVersion)) {
              
              KeyStore tks = KeyStore.getInstance("JKS");
              tks.load(new FileInputStream(pathToTrustStore), trustPassphrase.toCharArray());
              TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
              tmf.init(tks);
              SSLContext c = SSLContext.getInstance(tlsVersion);
              c.init(null, tmf.getTrustManagers(), null);
              
              factory.useSslProtocol();
              
            }
            
            connection = factory.newConnection();
            messagingSystemNameConnection.put(messagingSystemName, connection);
            
          } catch (Exception e) {
            throw new RuntimeException("Error occurred while connecting to rabbitmq host: "+host+" for "+messagingSystemName);
          }
        }
      
      }
      return connection;
    
    }
    
    @Override
    public void closeConnection(String messagingSystemName) {
      
      if (StringUtils.isBlank(messagingSystemName)) {
        throw new IllegalArgumentException("messagingSystemName is required.");
      }
      Connection connection = messagingSystemNameConnection.get(messagingSystemName);
      synchronized(RabbitMQConnectionFactoryImpl.class) { 
        if (connection != null && connection.isOpen()) {
          try {
            connection.close();
            connection = null;
          } catch(IOException e) {
            throw new RuntimeException("Error occurred while closing rabbitmq connection for "+messagingSystemName);
          }
        }
      }
      
    }
  };
  


}
