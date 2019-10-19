/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouperClient.ssl;



import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.ConnectTimeoutException;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.params.HttpConnectionParams;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;


  /**
   * Apache code for SSL that doesnt fail with self-signed certs
   * 
   * @author mchyzer
   */
  public class EasySslSocketFactory implements SecureProtocolSocketFactory {

    /**
     *  
     */
    public EasySslSocketFactory() {
      super();
    }

    /**
     * <p>
     * EasySSLProtocolSocketFactory can be used to creats SSL {@link Socket}s
     * that accept self-signed certificates.
     * </p>
     * 
     * <p>
     * This socket factory SHOULD NOT be used for productive systems due to
     * security reasons, unless it is a concious decision and you are perfectly
     * aware of security implications of accepting self-signed certificates
     * </p>
     * @return SSLSocketFactory
     */
    private static SSLSocketFactory getEasySSLSocketFactory() {
      SSLContext context = null;

      try {
        context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[] { new EasyX509TrustManager(null) }, null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      return context.getSocketFactory();
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(String host, int port, InetAddress clientHost,
        int clientPort) throws IOException, UnknownHostException {
      Socket socket = getEasySSLSocketFactory().createSocket(host, port, clientHost,
          clientPort);

      return socket;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port) throws IOException,
        UnknownHostException {
      return getEasySSLSocketFactory().createSocket(host, port);
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
        throws IOException, UnknownHostException {
      return getEasySSLSocketFactory().createSocket(socket, host, port, autoClose);
    }

    /**
     * jakarta code for SSL that doesnt fail with self-signed certs
     * 
     * @author mchyzer
     */
    public static class EasyX509TrustManager implements X509TrustManager {

      /**
       * Field standardTrustManager.
       */
      private X509TrustManager standardTrustManager = null;

      /**
       * Constructor for EasyX509TrustManager.
       * @param keystore KeyStore
       * @throws NoSuchAlgorithmException
       * @throws KeyStoreException
       */
      public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException,
          KeyStoreException {
        super();

        TrustManagerFactory factory = TrustManagerFactory.getInstance("SunX509");
        factory.init(keystore);

        TrustManager[] trustmanagers = factory.getTrustManagers();

        if (trustmanagers.length == 0) {
          throw new NoSuchAlgorithmException("SunX509 trust manager not supported");
        }

        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
      }

      /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return this.standardTrustManager.getAcceptedIssuers();
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
       */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      this.standardTrustManager.checkClientTrusted(chain, authType);
      }

      /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
       */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      if ((chain != null) && (chain.length == 1)) {
        X509Certificate certificate = chain[0];

            certificate.checkValidity();
          }

      this.standardTrustManager.checkClientTrusted(chain, authType);
      }
    }

    /**
     * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int, org.apache.commons.httpclient.params.HttpConnectionParams)
     */
  public Socket createSocket(String host, int port, InetAddress clientHost,
      int clientPort, HttpConnectionParams arg4)
      throws IOException, UnknownHostException, ConnectTimeoutException {
    Socket socket = getEasySSLSocketFactory().createSocket(host, port, clientHost,
        clientPort);

      return socket;
    }
  }
