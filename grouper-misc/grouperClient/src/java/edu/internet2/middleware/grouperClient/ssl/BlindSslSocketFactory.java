/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouperClient.ssl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * BlindSSLSocketFactoryTest
 *  Simple test to show an Active Directory (LDAP)
 *  and HTTPS connection without verifying the 
 *  server's certificate.
 *  
 *  From: http://blog.platinumsolutions.com/node/79
 *  http://blog.platinumsolutions.com/files/BlindSSLSocketFactoryTest.java.txt
 *  
 * @author Mike McKinney, Platinum Solutions, Inc.
 */
public class BlindSslSocketFactory extends SocketFactory {
  private static SocketFactory blindFactory = null;
  
  /**
   * Builds an all trusting "blind" ssl socket factory.
   */
  static {
    // create a trust manager that will purposefully fall down on the
    // job
    TrustManager[] blindTrustMan = new TrustManager[] { new X509TrustManager() {
      public X509Certificate[] getAcceptedIssuers() { return null; }
      public void checkClientTrusted(X509Certificate[] c, String a) { }
      public void checkServerTrusted(X509Certificate[] c, String a) { }
    } };

    // create our "blind" ssl socket factory with our lazy trust manager
    try {
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, blindTrustMan, new java.security.SecureRandom());
      blindFactory = sc.getSocketFactory();
    } catch (GeneralSecurityException e) {
      e.printStackTrace();
    }
  }

  /**
   * @see javax.net.SocketFactory#getDefault()
   */
  public static SocketFactory getDefault() {
    return new BlindSslSocketFactory();
  }


  /**
   * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
   */
  public Socket createSocket(String arg0, int arg1) throws IOException,
      UnknownHostException {
    return blindFactory.createSocket(arg0, arg1);
  }

  /**
   * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
   */
  public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
    return blindFactory.createSocket(arg0, arg1);
  }

  /**
   * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
   *      java.net.InetAddress, int)
   */
  public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
      throws IOException, UnknownHostException {
    return blindFactory.createSocket(arg0, arg1, arg2, arg3);
  }

  /**
   * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
   *      java.net.InetAddress, int)
   */
  public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
      int arg3) throws IOException {
    return blindFactory.createSocket(arg0, arg1, arg2, arg3);
  }

  /**
   * Our test...
   * @param args
   */
  public static void main(String[] args) {
    // do you want to validate the server's SSL cert?
    //   if you have invalid certs, true will produce errors
    boolean validateCert = false; 
    
    // ****************************************************>
    // LDAPS CONNECTION
    // ****************************************************>
    System.out.println("Testing LDAPS connection with validateCert: " + validateCert);
    Hashtable env = new Hashtable();
    
    // complete URL of Active Directory/LDAP server running SSL with invalid cert
    String url = "ldaps://<URL TO YOUR AD SERVER>:636";
    // domain is the Active Directory domain i.e. "yourdomain.com"
    String domain = "<YOUR AD DOMAIN>";
    // the sAMAccountName (i.e. jsmith)
    String login = "<LOGIN>";
    String password = "<PASSWORD>";
    
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, url);
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, login + "@" + domain);
    env.put(Context.SECURITY_CREDENTIALS, password);

    if (url.startsWith("ldaps") && !validateCert) {
      env.put("java.naming.ldap.factory.socket", BlindSslSocketFactory.class.getName());
    }

    try {
      LdapContext ctx = new InitialLdapContext(env, null);
      System.out.println("Successfull bind to " + url + "!");
    } catch (AuthenticationException e) {
      System.out.println("The credentials could not be validated!");
    } catch (NamingException e) {
      e.printStackTrace();
    }

    // ****************************************************>
    
    // ****************************************************>
    // HTTPS CONNECTION
    // ****************************************************>
    System.out.println("Testing HTTPS connection with validateCert: " + validateCert);
    // host name of server running SSL with invalid cert
    String host = "<SSL WEB HOST>";
    int port = 443;// modify this if other than default port.
    
    try {
        SocketFactory sslFactory;
      if (validateCert) {
        sslFactory = SSLSocketFactory.getDefault();
      } else {
        sslFactory = BlindSslSocketFactory.getDefault();
      }
      
        SSLSocket s = (SSLSocket) sslFactory.createSocket(host, port);
        OutputStream out = s.getOutputStream();
        
        out.write("GET / HTTP/1.0\n\r\n\r".getBytes());
        out.flush();
        System.out.println("Successfull connection to " + host + ":" + port + "!");
    } catch (IOException e) {
      e.printStackTrace();
    }
    // ****************************************************>
  }
}

