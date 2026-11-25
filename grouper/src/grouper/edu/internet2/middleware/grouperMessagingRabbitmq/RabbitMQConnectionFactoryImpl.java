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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

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
          
          GrouperMessagingConfig grouperMessagingConfig = GrouperMessagingConfig.retrieveGrouperMessagingConfigNonNull(messagingSystemName);

          
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
              // check that the broker certificate names the host also when a trust store is configured
              // (default true; set false only for a broker whose certificate does not name the host)
              boolean hostnameVerification = GrouperUtil.booleanValue(grouperMessagingConfig.propertyValueString(
                  GrouperClientConfig.retrieveConfig(), "hostnameVerification"), true);
              RabbitMQConnectionFactoryImpl.configureTls(factory, messagingSystemName, tlsVersion, pathToTrustStore,
                  trustPassphrase, trustAllCertificates, hostnameVerification);
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
            // a refused broker certificate is a configuration problem: say which setting to look at
            throw new RuntimeException("Error occurred while connecting to rabbitmq host: "+host+" for "+messagingSystemName
                + tlsFailureHint(e, messagingSystemName), e);
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
   * if connecting failed in the TLS handshake (the broker certificate was refused), a hint naming the
   * settings to check, else an empty string.  The JDK message (e.g. "No subject alternative DNS name
   * matching ... found", or "PKIX path building failed") is included.
   * @param throwable what connecting threw
   * @param messagingSystemName
   * @return the hint, starting with ".  ", or empty
   */
  static String tlsFailureHint(Throwable throwable, String messagingSystemName) {
    for (Throwable current = throwable; current != null; current = current.getCause()) {
      if (current instanceof javax.net.ssl.SSLException) {
        String prefix = "grouper.messaging.system." + messagingSystemName + ".";
        return ".  The TLS handshake with the broker failed: " + current.getMessage()
            + ".  If the broker certificate does not name this host, use a certificate that does, or set " + prefix
            + "hostnameVerification=false (only applies with a trust store).  If it is not trusted, set " + prefix
            + "pathToTrustStore and " + prefix + "trustPassphrase to a trust store with the broker certificate or its CA";
      }
      if (current.getCause() == current) {
        break;
      }
    }
    return "";
  }

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
    // no host name check with a configured trust store, as before the hostnameVerification setting
    return configureTls(factory, messagingSystemName, tlsVersion, pathToTrustStore, trustPassphrase,
        trustAllCertificates, false);
  }

  /**
   * Set up TLS on a RabbitMQ connection factory, see the other configureTls.  With a configured trust
   * store, hostnameVerificationWithTrustStore decides whether the broker certificate must also
   * name the host (grouper.messaging.system.&lt;name&gt;.hostnameVerification, default true).  Without
   * a trust store the host name is always checked, and with trustAllCertificates never.
   * @param factory connection factory to configure
   * @param messagingSystemName for log messages
   * @param tlsVersion e.g. TLSv1.2, or "default" for the best one the JVM and broker support
   * @param pathToTrustStore path to a JKS trust store, or blank
   * @param trustPassphrase trust store passphrase, or blank
   * @param trustAllCertificates true to accept any certificate when no trust store is configured
   * @param hostnameVerificationWithTrustStore true to check the host name when a trust store is configured
   * @return true if host name verification was enabled
   * @throws Exception if the SSL context cannot be built
   */
  public static boolean configureTls(ConnectionFactory factory, String messagingSystemName, String tlsVersion,
      String pathToTrustStore, String trustPassphrase, boolean trustAllCertificates,
      boolean hostnameVerificationWithTrustStore) throws Exception {

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
      // SECURITY (GRP-7580): a trusted CA can sign certificates for other hosts, so also check that the
      // certificate names the broker host, unless the site turned it off for a certificate that does not
      verifyHostname = hostnameVerificationWithTrustStore;
      if (!hostnameVerificationWithTrustStore) {
        GrouperUtil.getLog(RabbitMQConnectionFactoryImpl.class).warn("RabbitMQ messaging system '" + messagingSystemName
            + "' uses TLS with hostnameVerification=false: the broker certificate is checked against the trust store "
            + "but not against the host name.");
      }

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

    // only the socket factory (blocking IO, which Grouper uses), not useSslProtocol(SSLContext) or
    // setSslContextFactory: in current amqp-client both of those also turn on host name verification
    // by themselves (and it cannot be turned off again), which would ignore hostnameVerification=false
    // and trustAllCertificates.  Host name verification is turned on below exactly when decided above
    factory.setSocketFactory(c.getSocketFactory());
    if (verifyHostname) {
      factory.enableHostnameVerification();
    }
    return verifyHostname;
  }
}
