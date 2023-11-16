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
package edu.internet2.middleware.grouper.ldap.ldaptive;

import java.security.GeneralSecurityException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import edu.internet2.middleware.grouper.ldap.LdapPEMSocketFactory;
import org.ldaptive.ssl.CredentialConfig;
import org.ldaptive.ssl.SSLContextInitializer;

/**
 * Provides a {@link CredentialConfig} implementation that leverages {@link LdapPEMSocketFactory}. This class exposes
 * properties to allow for configuration wiring in {@link LdaptiveConfiguration}.
 */
public class LdapPEMCredentialConfig implements CredentialConfig {

  private String caFile;
  private String certFile;
  private String keyFile;

  public String getCaFile() {
    return caFile;
  }

  public void setCaFile(final String file) {
    caFile = file;
  }

  public String getCertFile() {
    return certFile;
  }

  public void setCertFile(String file) {
    certFile = file;
  }

  public String getKeyFile() {
    return keyFile;
  }

  public void setKeyFile(String file) {
    keyFile = file;
  }

  @Override
  public SSLContextInitializer createSSLContextInitializer() throws GeneralSecurityException {
    LdapPEMSocketFactory sf = new LdapPEMSocketFactory(caFile, certFile, keyFile);
    return new SSLContextInitializer() {
      @Override
      public SSLContext initSSLContext(String protocol) throws GeneralSecurityException {
        SSLContext ctx = SSLContext.getInstance(protocol);
        ctx.init(getKeyManagers(), getTrustManagers(), null);
        return ctx;
      }

      @Override
      public TrustManager[] getTrustManagers() throws GeneralSecurityException {
        return sf.getTrustManagers();
      }

      @Override
      public void setTrustManagers(TrustManager... managers) {

      }

      @Override
      public KeyManager[] getKeyManagers() throws GeneralSecurityException {
        return sf.getKeyManagers();
      }
    };
  }
}
