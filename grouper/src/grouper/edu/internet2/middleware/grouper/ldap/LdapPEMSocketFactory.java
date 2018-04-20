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
/* ========================================================================
 * Copyright (c) 2009-2011 The University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

/**
 * Build a socket from PEM ca, cert, and key files
 * @author fox
 */

package edu.internet2.middleware.grouper.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.io.IOException;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Key;

/**
 * 
 */
public class LdapPEMSocketFactory {

   private String caFilename;
   private String certFilename;
   private String keyFilename;

   private SSLSocketFactory socketFactory;
   private TrustManager[] trustManagers;
   private KeyManager[] keyManagers;

   private static Log log = LogFactory.getLog(LdapPEMSocketFactory.class);
   
   /**
    * Create a socket factory from PEM files.
    * @param caFile
    * @param certFile
    * @param keyFile
    */
   public LdapPEMSocketFactory(String caFile, String certFile, String keyFile) {
      caFilename = caFile; 
      certFilename = certFile; 
      keyFilename = keyFile; 
      initManagers();
      initSocketFactory();
   }

   /**
    * @return socket factory
    */
   public SSLSocketFactory getSocketFactory() {
       return socketFactory;
   }
      
   protected void initSocketFactory() {
       try {
           SSLContext sc = SSLContext.getInstance("TLS");
           sc.init(keyManagers, trustManagers, new java.security.SecureRandom());
           socketFactory = sc.getSocketFactory();
       } catch (Exception e) {
           log.error("ldap source initSF error: " + e);
       }
   }
   
   protected void initManagers() {

       // trust managers
       try {
           X509Certificate cert = null;
           if (caFilename!=null) cert = readCertificate(caFilename);
           KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
           ks.load(null, null);
           ks.setCertificateEntry("CACERT", cert);
           TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
           tmf.init(ks);
           trustManagers = tmf.getTrustManagers();
       } catch (Exception e) {
           log.error("ldap source cacert error: " + e);
       }

       // key managers
       if (certFilename != null && keyFilename != null) {
           char[] pw = new char[] {0};

           try {
              X509Certificate cert = readCertificate(certFilename);
              PKCS1 pkcs = new PKCS1();
              PrivateKey key = pkcs.readKey(keyFilename);
              KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
              ks.load(null, null);
              X509Certificate[] chain = new X509Certificate[1];
              chain[0] = cert;
              ks.setKeyEntry("CERT", (Key) key, pw, chain);

              KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
              kmf.init(ks, pw);
              keyManagers = kmf.getKeyManagers();
           } catch (Exception e) {
              log.error("ldap source cert/key error", e);
           }
       }

   }

    /** 
     * @param filename
     */
    protected X509Certificate readCertificate(String filename) {
        FileInputStream file;
        X509Certificate cert;
        try {
            file = new FileInputStream(filename);
        } catch (IOException e) {
            log.error("ldap source bad cert file: " + e);
            return null;
        }
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(file);
        } catch (CertificateException e) {
            log.error("ldap source bad cert: " + e);
            return null;
        }
        return cert;
    }
   
}
