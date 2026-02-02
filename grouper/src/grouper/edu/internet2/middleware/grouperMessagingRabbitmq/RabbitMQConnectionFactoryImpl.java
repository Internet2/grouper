package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.logging.Log;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.TrustEverythingTrustManager;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import org.apache.commons.lang3.StringUtils;

public enum RabbitMQConnectionFactoryImpl implements RabbitMQConnectionFactory {
  
  INSTANCE {
    
    private Map<String, Connection> messagingSystemNameConnection = new HashMap<String, Connection>();
    private final Log LOG = GrouperUtil.getLog(RabbitMQConnectionFactoryImpl.class);
    
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

            if (StringUtils.isNotEmpty(tlsVersion)) {
              if ("default".equals(tlsVersion)) {
                tlsVersion = ConnectionFactory.computeDefaultTlsProtocol(SSLContext.getDefault().getSupportedSSLParameters().getProtocols());
              }
              SSLContext c = SSLContext.getInstance(tlsVersion);
              if (StringUtils.isNotEmpty(pathToTrustStore) && StringUtils.isNotEmpty(trustPassphrase)) {
                KeyStore tks = KeyStore.getInstance("JKS");
                tks.load(new FileInputStream(pathToTrustStore), trustPassphrase.toCharArray());
                TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                tmf.init(tks);
                c.init(null, tmf.getTrustManagers(), null);
              } else {
                c.init(null, new TrustManager[]{new TrustEverythingTrustManager()}, null);
              }
              factory.useSslProtocol(c);
            }
            
            if (LOG.isDebugEnabled()) {
              Map<String, Object> debugMap = new LinkedHashMap<>();
              debugMap.put("host", factory.getHost());
              debugMap.put("virtualHost", factory.getVirtualHost());
              debugMap.put("username", factory.getUsername());
              debugMap.put("port", Integer.toString(factory.getPort()));
              debugMap.put("isSSL", factory.isSSL());
              LOG.debug(GrouperUtil.mapToString(debugMap));
            }

            factory.setAutomaticRecoveryEnabled(true);
            connection = factory.newConnection("grouper messagingSystem: " + messagingSystemName);
            messagingSystemNameConnection.put(messagingSystemName, connection);
            
          } catch (Exception e) {
            throw new RuntimeException("Error occurred while connecting to rabbitmq host: "+host+" for "+messagingSystemName, e);
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
  }
}
