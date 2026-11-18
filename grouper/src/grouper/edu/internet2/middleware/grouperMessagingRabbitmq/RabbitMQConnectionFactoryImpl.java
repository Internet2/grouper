package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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
              // explicit opt in to accept any broker certificate (only used when no trust store is configured)
              boolean trustAllCertificates = GrouperUtil.booleanValue(grouperMessagingConfig.propertyValueString(
                  GrouperClientConfig.retrieveConfig(), "trustAllCertificates"), false);
              RabbitMQConnectionFactoryImpl.configureTls(factory, messagingSystemName, tlsVersion, pathToTrustStore,
                  trustPassphrase, trustAllCertificates);
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
  };

  /**
   * Set up TLS on a RabbitMQ connection factory.  Which certificates are trusted:
   * <ul>
   * <li>pathToTrustStore and trustPassphrase set: the certificates in that JKS trust store</li>
   * <li>else trustAllCertificates true: any certificate (no protection against a man in the middle,
   * logs a warning); only for test brokers</li>
   * <li>else: the JVM default trust store (cacerts or javax.net.ssl.trustStore), and the broker
   * certificate must match the host name</li>
   * </ul>
   * Before this change a missing trust store silently meant "trust any certificate".
   * @param factory connection factory to configure
   * @param messagingSystemName for log messages
   * @param tlsVersion e.g. TLSv1.2, or "default" for the best one the JVM and broker support
   * @param pathToTrustStore path to a JKS trust store, or blank
   * @param trustPassphrase trust store passphrase, or blank
   * @param trustAllCertificates true to accept any certificate when no trust store is configured
   * @return true if host name verification was enabled
   * @throws Exception if the SSL context cannot be built
   */
  public static boolean configureTls(ConnectionFactory factory, String messagingSystemName, String tlsVersion,
      String pathToTrustStore, String trustPassphrase, boolean trustAllCertificates) throws Exception {

    if ("default".equals(tlsVersion)) {
      tlsVersion = ConnectionFactory.computeDefaultTlsProtocol(SSLContext.getDefault().getSupportedSSLParameters().getProtocols());
    }
    SSLContext c = SSLContext.getInstance(tlsVersion);
    boolean verifyHostname = false;

    if (StringUtils.isNotEmpty(pathToTrustStore) && StringUtils.isNotEmpty(trustPassphrase)) {

      // site trust store (unchanged behavior)
      KeyStore tks = KeyStore.getInstance("JKS");
      FileInputStream fileInputStream = new FileInputStream(pathToTrustStore);
      try {
        tks.load(fileInputStream, trustPassphrase.toCharArray());
      } finally {
        GrouperUtil.closeQuietly(fileInputStream);
      }
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
      tmf.init(tks);
      c.init(null, tmf.getTrustManagers(), null);

    } else if (trustAllCertificates) {

      // explicit opt in only: accept any certificate
      GrouperUtil.getLog(RabbitMQConnectionFactoryImpl.class).warn("RabbitMQ messaging system '" + messagingSystemName
          + "' uses TLS with trustAllCertificates=true: the broker certificate is not checked, so the broker password and "
          + "messages are not protected from a man in the middle.  Configure pathToTrustStore and trustPassphrase instead.");
      c.init(null, new TrustManager[]{new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) { }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) { }
        @Override
        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
      }}, null);

    } else {

      // no trust store configured: the JVM default trust managers, and check the host name, like any
      // other TLS client.  This used to trust any certificate, which let anyone on the network path
      // pose as the broker
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init((KeyStore) null);
      c.init(null, tmf.getTrustManagers(), null);
      verifyHostname = true;
    }

    factory.useSslProtocol(c);
    if (verifyHostname) {
      factory.enableHostnameVerification();
    }
    return verifyHostname;
  }
}
