package edu.internet2.middleware.grouperMessagingRabbitmq;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import com.rabbitmq.client.ConnectionFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests that a RabbitMQ messaging system with TLS on and no trust store configured checks the
 * broker certificate (JVM default trust store plus host name check) instead of trusting any
 * certificate, and that trust-all is only used with the explicit trustAllCertificates opt in.
 * <p>
 * No broker is needed: a local TLS server socket with a fresh self-signed certificate (made with
 * the JDK keytool) stands in for the broker, and records whether the TLS handshake completed.  The
 * AMQP protocol step after the handshake always fails (the stand-in speaks no AMQP), which does not
 * matter: what is tested is whether Grouper accepted the certificate.
 * </p>
 * Fails to compile before the fix (RabbitMQConnectionFactoryImpl.configureTls does not exist).
 * Java 8 compatible (runs on 2.6.19 too).
 */
public class RabbitMQTlsTrustTest extends TestCase {

  /**
   * run from IDE
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RabbitMQTlsTrustTest("testNoTrustStoreRejectsSelfSigned"));
  }

  /** keystore password for the throwaway keystores */
  private static final String PASS = "changeit161";

  /** temp dir with the keystores */
  private File tempDir;

  /** server keystore (self-signed cert for localhost) */
  private File serverKeystore;

  /** trust store holding only the server cert */
  private File trustStore;

  /**
   * @param name
   */
  public RabbitMQTlsTrustTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    this.tempDir = File.createTempFile("rabbitTls161_", "");
    this.tempDir.delete();
    this.tempDir.mkdirs();
    this.serverKeystore = new File(this.tempDir, "server.jks");
    this.trustStore = new File(this.tempDir, "trust.jks");
    File certFile = new File(this.tempDir, "server.cer");

    // self-signed cert for localhost, then a trust store holding just that cert
    keytool("-genkeypair", "-alias", "broker", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
        "-dname", "CN=localhost", "-ext", "SAN=dns:localhost,ip:127.0.0.1",
        "-keystore", this.serverKeystore.getAbsolutePath(), "-storetype", "JKS",
        "-storepass", PASS, "-keypass", PASS);
    keytool("-exportcert", "-alias", "broker", "-keystore", this.serverKeystore.getAbsolutePath(),
        "-storepass", PASS, "-file", certFile.getAbsolutePath());
    keytool("-importcert", "-noprompt", "-alias", "broker", "-file", certFile.getAbsolutePath(),
        "-keystore", this.trustStore.getAbsolutePath(), "-storetype", "JKS", "-storepass", PASS);
  }

  @Override
  protected void tearDown() throws Exception {
    if (this.tempDir != null) {
      File[] files = this.tempDir.listFiles();
      if (files != null) {
        for (File file : files) {
          file.delete();
        }
      }
      this.tempDir.delete();
    }
    super.tearDown();
  }

  /**
   * TLS on, no trust store, no opt in: the self-signed broker certificate must be rejected
   * (before the fix any certificate was accepted)
   * @throws Exception
   */
  public void testNoTrustStoreRejectsSelfSigned() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    boolean hostnameVerification = RabbitMQConnectionFactoryImpl.configureTls(factory, "test161", "TLSv1.2", null, null, false);
    assertTrue("host name verification should be on when the JVM default trust store is used", hostnameVerification);
    assertFalse("a self-signed broker certificate must not be accepted without a trust store or opt in",
        handshakeCompleted(factory));
  }

  /**
   * a trust store path without a passphrase is not a usable trust store config: still no trust-all
   * @throws Exception
   */
  public void testTrustStoreWithoutPassphraseDoesNotTrustAll() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    RabbitMQConnectionFactoryImpl.configureTls(factory, "test161", "TLSv1.2", this.trustStore.getAbsolutePath(), null, false);
    assertFalse("path without passphrase must not fall back to trusting any certificate", handshakeCompleted(factory));
  }

  /**
   * explicit trustAllCertificates opt in: any certificate is accepted (old behavior, now opt in)
   * @throws Exception
   */
  public void testTrustAllOptIn() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    boolean hostnameVerification = RabbitMQConnectionFactoryImpl.configureTls(factory, "test161", "TLSv1.2", null, null, true);
    assertFalse(hostnameVerification);
    assertTrue("trustAllCertificates=true should accept the self-signed certificate", handshakeCompleted(factory));
  }

  /**
   * configured trust store holding the broker cert: accepted (unchanged behavior)
   * @throws Exception
   */
  public void testConfiguredTrustStore() throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    RabbitMQConnectionFactoryImpl.configureTls(factory, "test161", "TLSv1.2", this.trustStore.getAbsolutePath(), PASS, false);
    assertTrue("the configured trust store should accept its own certificate", handshakeCompleted(factory));
  }

  /**
   * a configured trust store wins over the trust-all opt in
   * @throws Exception
   */
  public void testTrustStoreWinsOverTrustAll() throws Exception {

    // a trust store that does NOT hold the broker cert: only the server keystore of another cert
    File otherKeystore = new File(this.tempDir, "other.jks");
    keytool("-genkeypair", "-alias", "other", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
        "-dname", "CN=other", "-keystore", otherKeystore.getAbsolutePath(), "-storetype", "JKS",
        "-storepass", PASS, "-keypass", PASS);

    ConnectionFactory factory = new ConnectionFactory();
    RabbitMQConnectionFactoryImpl.configureTls(factory, "test161", "TLSv1.2", otherKeystore.getAbsolutePath(), PASS, true);
    assertFalse("with a trust store configured, trustAllCertificates must not apply", handshakeCompleted(factory));
  }

  /**
   * start a one-shot local TLS server with the self-signed cert, connect the factory to it, and
   * report whether the server saw a completed TLS handshake
   * @param factory configured factory
   * @return true if the client accepted the certificate
   * @throws Exception
   */
  private boolean handshakeCompleted(ConnectionFactory factory) throws Exception {

    KeyStore keyStore = KeyStore.getInstance("JKS");
    InputStream inputStream = new FileInputStream(this.serverKeystore);
    try {
      keyStore.load(inputStream, PASS.toCharArray());
    } finally {
      inputStream.close();
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(keyStore, PASS.toCharArray());
    SSLContext serverContext = SSLContext.getInstance("TLSv1.2");
    serverContext.init(kmf.getKeyManagers(), null, null);

    final SSLServerSocket serverSocket = (SSLServerSocket) serverContext.getServerSocketFactory()
        .createServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
    serverSocket.setEnabledProtocols(new String[] {"TLSv1.2"});
    serverSocket.setSoTimeout(20000);
    final boolean[] completed = new boolean[] {false};

    Thread serverThread = new Thread(new Runnable() {
      public void run() {
        try {
          SSLSocket socket = (SSLSocket) serverSocket.accept();
          try {
            socket.setSoTimeout(20000);
            socket.startHandshake();
            // TLS 1.2: the server handshake only completes after the client accepted the cert
            // and sent its Finished message
            completed[0] = true;
          } finally {
            socket.close();
          }
        } catch (Exception e) {
          // handshake rejected by the client, or timeout
        }
      }
    });
    serverThread.setDaemon(true);
    serverThread.start();

    try {
      factory.setHost("localhost");
      factory.setPort(serverSocket.getLocalPort());
      factory.setUsername("guest");
      factory.setPassword("guest");
      factory.setAutomaticRecoveryEnabled(false);
      factory.setConnectionTimeout(10000);
      factory.setHandshakeTimeout(10000);
      try {
        factory.newConnection().close();
      } catch (Exception e) {
        // expected: either the TLS handshake fails or the stand-in server closes without AMQP
      }
      serverThread.join(25000);
    } finally {
      closeQuietly(serverSocket);
    }
    return completed[0];
  }

  /**
   * @param serverSocket
   */
  private static void closeQuietly(ServerSocket serverSocket) {
    try {
      serverSocket.close();
    } catch (Exception e) {
      // ignore
    }
  }

  /**
   * run the JDK keytool
   * @param args
   * @throws Exception
   */
  private static void keytool(String... args) throws Exception {
    String javaHome = System.getProperty("java.home");
    File keytool = new File(javaHome, "bin" + File.separator + "keytool");
    if (!keytool.exists()) {
      // Java 8 JRE inside a JDK
      keytool = new File(new File(javaHome).getParentFile(), "bin" + File.separator + "keytool");
    }
    List<String> command = new ArrayList<String>();
    command.add(keytool.getAbsolutePath());
    command.addAll(Arrays.asList(args));
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);
    Process process = processBuilder.start();
    InputStream processOutput = process.getInputStream();
    StringBuilder output = new StringBuilder();
    byte[] buffer = new byte[4096];
    int read;
    while ((read = processOutput.read(buffer)) != -1) {
      output.append(new String(buffer, 0, read, "UTF-8"));
    }
    int exit = process.waitFor();
    assertEquals("keytool " + command + " failed: " + output, 0, exit);
  }
}
