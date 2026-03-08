/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.authentication;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * Manages the server RSA key pair for signing and verifying OAuth JWT access tokens.
 * Key pair is stored in grouper.properties (database-backed config).
 * The private key is auto-encrypted by the config framework because the key name
 * contains "private" (see GrouperConfigHibernate.isPasswordHelper()).
 * @author mchyzer
 */
public class GrouperOAuthSigningKey {

  private static final Log LOG = GrouperUtil.getLog(GrouperOAuthSigningKey.class);

  /** config key for private key (auto-encrypted because name contains "private") */
  private static final String CONFIG_KEY_PRIVATE_KEY = "grouper.oauth.signingKey.privateKey";

  /** config key for public key */
  private static final String CONFIG_KEY_PUBLIC_KEY = "grouper.oauth.signingKey.publicKey";

  /**
   * immutable holder for the key material so all fields are read as a consistent snapshot
   */
  private static class KeyBundle {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey;
    private final Algorithm signingAlgorithm;
    private final Algorithm verificationAlgorithm;

    KeyBundle(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
      this.publicKey = publicKey;
      this.privateKey = privateKey;
      this.signingAlgorithm = Algorithm.RSA256(publicKey, privateKey);
      this.verificationAlgorithm = Algorithm.RSA256(publicKey, null);
    }
  }

  /**
   * cache so keys are re-read from config every 5 minutes,
   * allowing key changes to propagate across containers without a restart
   */
  private static ExpirableCache<Boolean, KeyBundle> keyBundleCache = new ExpirableCache<Boolean, KeyBundle>(5);

  /**
   * Retrieve the current key bundle, initializing from config or generating if needed.
   * Thread-safe.  Keys are re-read from config every 5 minutes so that key changes
   * propagate across containers without a restart.
   * @return the key bundle (never null)
   */
  private static KeyBundle retrieveKeyBundle() {
    KeyBundle keyBundle = keyBundleCache.get(Boolean.TRUE);
    if (keyBundle != null) {
      return keyBundle;
    }
    synchronized (GrouperOAuthSigningKey.class) {
      // double-check after acquiring lock
      keyBundle = keyBundleCache.get(Boolean.TRUE);
      if (keyBundle != null) {
        return keyBundle;
      }
      keyBundle = initializeKeyBundle();
      keyBundleCache.put(Boolean.TRUE, keyBundle);
      return keyBundle;
    }
  }

  /**
   * Initialize keys from config, or generate and store if not present.
   * Must be called while holding the class lock.
   * @return the initialized key bundle
   */
  private static KeyBundle initializeKeyBundle() {

    try {
      String base64PrivateKey = GrouperConfig.retrieveConfig()
          .propertyValueString(CONFIG_KEY_PRIVATE_KEY);
      String base64PublicKey = GrouperConfig.retrieveConfig()
          .propertyValueString(CONFIG_KEY_PUBLIC_KEY);

      RSAPublicKey publicKey;
      RSAPrivateKey privateKey;

      if (StringUtils.isNotBlank(base64PrivateKey) && StringUtils.isNotBlank(base64PublicKey)) {
        // load existing keys from config
        // Note: private key is auto-decrypted by config framework
        byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(base64PrivateKey);
        byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(base64PublicKey);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        LOG.info("OAuth RSA signing key pair loaded from grouper.properties config");
      } else {
        // generate new key pair
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        base64PrivateKey = java.util.Base64.getEncoder().encodeToString(privateKey.getEncoded());
        base64PublicKey = java.util.Base64.getEncoder().encodeToString(publicKey.getEncoded());

        GrouperSession grouperSession = null;
        try {
          grouperSession = GrouperSession.startRootSession();

          // save private key - auto-encrypted because key name contains "private"
          saveConfigValue(CONFIG_KEY_PRIVATE_KEY, base64PrivateKey);

          // save public key
          saveConfigValue(CONFIG_KEY_PUBLIC_KEY, base64PublicKey);

          LOG.info("OAuth RSA signing key pair generated and stored in grouper.properties config");
        } catch (Exception saveException) {
          // another container may have created it first (race condition)
          LOG.info("Race condition on OAuth signing key creation, loading from config");
          GrouperConfig.retrieveConfig().clearCachedCalculatedValues();
          base64PrivateKey = GrouperConfig.retrieveConfig()
              .propertyValueString(CONFIG_KEY_PRIVATE_KEY);
          base64PublicKey = GrouperConfig.retrieveConfig()
              .propertyValueString(CONFIG_KEY_PUBLIC_KEY);

          if (StringUtils.isBlank(base64PrivateKey) || StringUtils.isBlank(base64PublicKey)) {
            throw new RuntimeException("Failed to create or load OAuth signing key", saveException);
          }

          byte[] loadedPrivateKeyBytes = java.util.Base64.getDecoder().decode(base64PrivateKey);
          byte[] loadedPublicKeyBytes = java.util.Base64.getDecoder().decode(base64PublicKey);

          KeyFactory kf = KeyFactory.getInstance("RSA");
          privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(loadedPrivateKeyBytes));
          publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(loadedPublicKeyBytes));

          LOG.info("OAuth RSA signing key pair loaded from config after race condition");
        } finally {
          GrouperSession.stopQuietly(grouperSession);
        }
      }

      return new KeyBundle(publicKey, privateKey);

    } catch (Exception e) {
      LOG.error("Failed to initialize OAuth RSA key pair", e);
      throw new RuntimeException("Failed to initialize OAuth RSA key pair", e);
    }
  }

  /**
   * Save a config value to the grouper.properties DB config.
   * @param configKey
   * @param value
   */
  private static void saveConfigValue(String configKey, String value) {
    Set<GrouperConfigHibernate> existing = GrouperDAOFactory.getFactory().getConfig()
        .findAll(ConfigFileName.GROUPER_PROPERTIES, null, configKey);

    if (GrouperUtil.length(existing) == 0) {
      GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
      grouperConfigHibernate.setConfigEncrypted(false);
      grouperConfigHibernate.setConfigFileHierarchyDb("INSTITUTION");
      grouperConfigHibernate.setConfigFileNameDb(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName());
      grouperConfigHibernate.setConfigKey(configKey);
      grouperConfigHibernate.setValueToSave(value);
      grouperConfigHibernate.saveOrUpdate(true);
    } else {
      GrouperConfigHibernate grouperConfigHibernate = existing.iterator().next();
      grouperConfigHibernate.setValueToSave(value);
      grouperConfigHibernate.saveOrUpdate(false);
    }
  }

  /**
   * Get the public key (for JWKS endpoint, etc.)
   * @return the RSA public key
   */
  public static RSAPublicKey getPublicKey() {
    return retrieveKeyBundle().publicKey;
  }

  /**
   * force re-read of keys from config on next access
   */
  public static void initializeIfNeeded() {
    retrieveKeyBundle();
  }

  /**
   * Create a signed JWT access token
   * @param issuer the issuer URL
   * @param subjectId the Grouper subject ID
   * @param subjectSourceId the Grouper subject source ID
   * @param clientId the OAuth client ID
   * @param consentDetails JSON string with granted scopes from consent, or null
   * @return the signed JWT string
   */
  public static String createSignedJwt(String issuer, String subjectId,
      String subjectSourceId, String clientId, String consentDetails) {

    KeyBundle keyBundle = retrieveKeyBundle();

    int expirationSeconds = GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.oauth.accessToken.expirationSeconds", 3600);

    long nowMillis = System.currentTimeMillis();

    com.auth0.jwt.JWTCreator.Builder jwtBuilder = JWT.create()
        .withIssuer(issuer)
        .withSubject(subjectId)
        .withClaim("subjectSourceId", subjectSourceId)
        .withClaim("client_id", clientId)
        .withIssuedAt(new Date(nowMillis))
        .withExpiresAt(new Date(nowMillis + (long) expirationSeconds * 1000))
        .withJWTId(UUID.randomUUID().toString());

    // add granted scope claims from consent details JSON
    if (StringUtils.isNotBlank(consentDetails)) {
      try {
        com.fasterxml.jackson.databind.JsonNode consentNode =
            new com.fasterxml.jackson.databind.ObjectMapper().readTree(consentDetails);
        if (consentNode.has("readonly") && consentNode.get("readonly").asBoolean()) {
          jwtBuilder.withClaim("grouper_readonly", true);
        }
        if (consentNode.has("readwrite") && consentNode.get("readwrite").asBoolean()) {
          jwtBuilder.withClaim("grouper_readwrite", true);
        }
        if (consentNode.has("sqlReadonly") && consentNode.get("sqlReadonly").asBoolean()) {
          jwtBuilder.withClaim("grouper_sql_readonly", true);
        }
        if (consentNode.has("adminReadonly") && consentNode.get("adminReadonly").asBoolean()) {
          jwtBuilder.withClaim("grouper_admin_readonly", true);
        }
        if (consentNode.has("adminReadwrite") && consentNode.get("adminReadwrite").asBoolean()) {
          jwtBuilder.withClaim("grouper_admin_readwrite", true);
        }
      } catch (Exception e) {
        LOG.warn("Failed to parse consent details for JWT: " + e.getMessage());
      }
    }

    return jwtBuilder.sign(keyBundle.signingAlgorithm);
  }

  /**
   * Verify that the public and private keys in config form a valid pair by signing
   * and verifying a test JWT.  Call from GSH to diagnose MCP authentication issues.
   * Prints results to stdout for easy use in GSH.
   * @return true if the keys match
   */
  public static boolean verifyKeyPair() {

    KeyBundle keyBundle = retrieveKeyBundle();

    try {
      String testJwt = JWT.create()
          .withSubject("keyPairTest")
          .withIssuedAt(new Date())
          .withExpiresAt(new Date(System.currentTimeMillis() + 60000))
          .sign(keyBundle.signingAlgorithm);

      JWTVerifier verifier = JWT.require(keyBundle.verificationAlgorithm).build();
      verifier.verify(testJwt);

      System.out.println("OAuth signing key pair is valid - sign and verify succeeded");
      return true;
    } catch (Exception e) {
      System.out.println("OAuth signing key pair MISMATCH - " + e.getMessage());
      return false;
    }
  }

  /**
   * Verify and decode a JWT access token
   * @param jwt the JWT string
   * @return the decoded JWT, or null if invalid
   */
  public static DecodedJWT verifyAndDecodeJwt(String jwt) {

    KeyBundle keyBundle = retrieveKeyBundle();

    try {
      JWTVerifier verifier = JWT.require(keyBundle.verificationAlgorithm).build();
      return verifier.verify(jwt);
    } catch (JWTVerificationException e) {
      LOG.debug("OAuth JWT verification failed: " + e.getMessage());
      return null;
    }
  }
}
