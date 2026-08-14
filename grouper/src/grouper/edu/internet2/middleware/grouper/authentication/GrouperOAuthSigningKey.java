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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import edu.internet2.middleware.morphString.Morph;

/**
 * Manages the server RSA key pair for signing and verifying OAuth JWT access tokens.
 * Key pair is stored in grouper.properties (database-backed config).
 * The private key is stored encrypted at rest: {@link #saveConfigValue(String, String)}
 * encrypts it with {@link Morph} because its config key contains "private" (the same rule the
 * config framework applies in GrouperConfigHibernate.isPassword), and the config framework
 * decrypts it transparently on read.  The public key is stored in the clear.
 * @author mchyzer
 */
public class GrouperOAuthSigningKey {

  private static final Log LOG = GrouperUtil.getLog(GrouperOAuthSigningKey.class);

  /** config key for private key (stored encrypted because name contains "private") */
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

        if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oauth.logAuthDebug", false)) {
          LOG.warn("OAuth RSA signing key pair loaded from config, publicKey hash="
              + GrouperUtil.encryptSha(base64PublicKey).substring(0, 12));
        }
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

          // save private key - encrypted at rest because key name contains "private"
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
   * Save a config value to the grouper.properties DB config.  Password-type values (which the
   * private key is, because its config key contains "private") are stored encrypted with
   * {@link Morph#encrypt(String)} and flagged config_encrypted, exactly as the UI/GSH config
   * editor (DbConfigEngine) would store them.  The config framework decrypts these transparently
   * on read, so {@link #initializeKeyBundle()} still reads back the plain base64.
   * @param configKey
   * @param value
   */
  private static void saveConfigValue(String configKey, String value) {

    // treat the value as a password by the same rule the config framework uses: the private key's
    // config key contains "private", so isPassword is true and the value is encrypted at rest.  the
    // public key key has no password-related word, so it is stored in the clear (it is public).
    // callers always pass raw base64 key material (never our ciphertext), so encrypt unconditionally
    // when it is a password.
    boolean isPassword = GrouperConfigHibernate.isPassword(
        ConfigFileName.GROUPER_PROPERTIES, null, configKey, value, true, null);

    String valueToSave = isPassword ? Morph.encrypt(value) : value;

    Set<GrouperConfigHibernate> existing = GrouperDAOFactory.getFactory().getConfig()
        .findAll(ConfigFileName.GROUPER_PROPERTIES, null, configKey);

    if (GrouperUtil.length(existing) == 0) {
      GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
      grouperConfigHibernate.setConfigEncrypted(isPassword);
      grouperConfigHibernate.setConfigFileHierarchyDb("INSTITUTION");
      grouperConfigHibernate.setConfigFileNameDb(ConfigFileName.GROUPER_PROPERTIES.getConfigFileName());
      grouperConfigHibernate.setConfigKey(configKey);
      grouperConfigHibernate.setValueToSave(valueToSave);
      grouperConfigHibernate.saveOrUpdate(true);
    } else {
      GrouperConfigHibernate grouperConfigHibernate = existing.iterator().next();
      grouperConfigHibernate.setConfigEncrypted(isPassword);
      grouperConfigHibernate.setValueToSave(valueToSave);
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
   * Create a signed JWT access token.
   *
   * @param issuer ignored.  The issuer is read from configuration instead, because
   *   {@link #verifyAndDecodeJwt(String)} checks the claim against that same configured value,
   *   so a token stamped with anything else would fail the moment it was presented.
   * @param subjectId the Grouper subject ID
   * @param subjectSourceId the Grouper subject source ID
   * @param clientId the OAuth client ID
   * @param consentDetails JSON string with granted scopes from consent, or null
   * @return the signed JWT string
   * @deprecated use {@link #createSignedJwt(String, String, String, String)}, which does not
   *   take an issuer
   */
  @Deprecated
  public static String createSignedJwt(String issuer, String subjectId,
      String subjectSourceId, String clientId, String consentDetails) {
    return createSignedJwt(subjectId, subjectSourceId, clientId, consentDetails);
  }

  /**
   * Create a signed JWT access token
   * @param subjectId the Grouper subject ID
   * @param subjectSourceId the Grouper subject source ID
   * @param clientId the OAuth client ID
   * @param consentDetails JSON string with granted scopes from consent, or null
   * @return the signed JWT string
   */
  public static String createSignedJwt(String subjectId,
      String subjectSourceId, String clientId, String consentDetails) {

    KeyBundle keyBundle = retrieveKeyBundle();

    int expirationSeconds = GrouperConfig.retrieveConfig().propertyValueInt(
        "grouper.oauth.accessToken.expirationSeconds", 3600);

    long nowMillis = System.currentTimeMillis();

    com.auth0.jwt.JWTCreator.Builder jwtBuilder = JWT.create()
        .withSubject(subjectId)
        .withClaim("subjectSourceId", subjectSourceId)
        .withClaim("client_id", clientId)
        .withIssuedAt(new Date(nowMillis))
        .withExpiresAt(new Date(nowMillis + (long) expirationSeconds * 1000))
        .withJWTId(UUID.randomUUID().toString());

    // Say who issued this, taken from configuration rather than from the request which asked
    // for the token.  verifyAndDecodeJwt checks this claim against the same configured value,
    // so an issuer worked out from the request would make every token obtained through any
    // hostname other than the configured one fail on the very next call.
    jwtBuilder.withIssuer(GrouperOAuthStore.retrieveIssuerIdentifier());

    // bind the token to the resource it is for, so that it cannot be used against a different
    // resource which happens to trust the same signing key
    jwtBuilder.withAudience(GrouperOAuthStore.retrieveMcpResourceIdentifier());

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

        // readwrite scope restrictions (folder/group/subject lists)
        if (consentNode.has("readwriteFolders") && consentNode.get("readwriteFolders").isArray()) {
          List<String> folders = new ArrayList<String>();
          for (com.fasterxml.jackson.databind.JsonNode item : consentNode.get("readwriteFolders")) {
            folders.add(item.asText());
          }
          if (!folders.isEmpty()) {
            jwtBuilder.withClaim("grouper_readwrite_folders", folders);
          }
        }
        if (consentNode.has("readwriteGroups") && consentNode.get("readwriteGroups").isArray()) {
          List<String> groups = new ArrayList<String>();
          for (com.fasterxml.jackson.databind.JsonNode item : consentNode.get("readwriteGroups")) {
            groups.add(item.asText());
          }
          if (!groups.isEmpty()) {
            jwtBuilder.withClaim("grouper_readwrite_groups", groups);
          }
        }
        if (consentNode.has("readwriteSubjects") && consentNode.get("readwriteSubjects").isArray()) {
          List<String> subjects = new ArrayList<String>();
          for (com.fasterxml.jackson.databind.JsonNode item : consentNode.get("readwriteSubjects")) {
            subjects.add(item.asText());
          }
          if (!subjects.isEmpty()) {
            jwtBuilder.withClaim("grouper_readwrite_subjects", subjects);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed to parse consent details for JWT, "
            + "refusing to issue token without scope restrictions: " + e.getMessage(), e);
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
      com.auth0.jwt.interfaces.Verification verification =
          JWT.require(keyBundle.verificationAlgorithm);

      // a valid signature only says this Grouper minted the token.  it must also have been
      // minted by this issuer for this resource, otherwise a token issued for something else
      // which trusts the same key would be accepted here
      verification = verification.withIssuer(GrouperOAuthStore.retrieveIssuerIdentifier());

      verification = verification.withAudience(GrouperOAuthStore.retrieveMcpResourceIdentifier());

      JWTVerifier verifier = verification.build();
      return verifier.verify(jwt);
    } catch (JWTVerificationException e) {
      LOG.warn("OAuth JWT verification failed: " + e.getMessage());
      return null;
    }
  }
}
