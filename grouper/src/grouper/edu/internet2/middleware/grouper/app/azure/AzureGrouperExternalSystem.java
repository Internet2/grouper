package edu.internet2.middleware.grouper.app.azure;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;

public class AzureGrouperExternalSystem extends GrouperExternalSystem {

  /** Microsoft Graph scope (default audience for most Azure connectors). */
  public static final String SCOPE_GRAPH = "https://graph.microsoft.com/.default";

  /** Power BI service scope. Use against https://api.powerbi.com/v1.0/myorg/... */
  public static final String SCOPE_POWER_BI = "https://analysis.windows.net/powerbi/api/.default";

  /** Microsoft Fabric REST API scope. Use against https://api.fabric.microsoft.com/v1/... */
  public static final String SCOPE_FABRIC = "https://api.fabric.microsoft.com/.default";

  /** Azure Resource Manager (ARM) scope. Use against https://management.azure.com/... */
  public static final String SCOPE_AZURE_MANAGEMENT = "https://management.azure.com/.default";

  /** Azure Key Vault scope. Use against https://&lt;vault&gt;.vault.azure.net/... */
  public static final String SCOPE_KEY_VAULT = "https://vault.azure.net/.default";

  /** Azure SQL Database scope (AAD token auth for SQL connections). */
  public static final String SCOPE_AZURE_SQL = "https://database.windows.net/.default";

  /**
   * cache of (configId|scope) to expires on and encrypted bearer token. Scope is
   * included in the key so that one connector consented to multiple audiences
   * (e.g. Graph + Power BI + Fabric) gets separate cached tokens per audience.
   */
  private static ExpirableCache<String, MultiKey> configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, MultiKey>(60);

  public static void clearCache() {
    configKeyToExpiresOnAndBearerToken.clear();
  }
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.azureConnector." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.azureConnector)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myAzure";
  }

  /**
   * cache connections
   */
  private static ExpirableCache<String, GrouperAzureApiCommands> apiConnectionCache = new ExpirableCache<String, GrouperAzureApiCommands>(5);

  /**
   * Validates the Azure provisioner by trying to log in and getting an auth token
   * @return
   * @throws UnsupportedOperationException
   */
  @Override
  public List<String> test() throws UnsupportedOperationException {
    List<String> ret = new ArrayList<>();

    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    String configPrefix = "grouper.azureConnector." + this.getConfigId() + ".";

    // loginEndpoint and resourceEndpoint have sensible defaults in metadata; Java falls back too
    String loginEndpoint = config.propertyValueString(configPrefix + "loginEndpoint", "https://login.microsoftonline.com/");
    if (GrouperUtil.isBlank(loginEndpoint)) {
      ret.add("Undefined or blank property: " + configPrefix + "loginEndpoint");
    }

    String resourceEndpoint = config.propertyValueString(configPrefix + "resourceEndpoint", "https://graph.microsoft.com/v1.0/");
    if (GrouperUtil.isBlank(resourceEndpoint)) {
      ret.add("Undefined or blank property: " + configPrefix + "resourceEndpoint");
    }


    String clientIdProperty = configPrefix + "clientId";
    String clientId = config.propertyValueString(clientIdProperty);
    if (GrouperUtil.isBlank(clientId)) {
      ret.add("Undefined or blank property: " + clientIdProperty);
    }

    String authenticationType = config.propertyValueString(configPrefix + "authenticationType", "clientSecret");

    if (StringUtils.equals(authenticationType, "certificate")) {
      String privateKeyProperty = configPrefix + "privateKey";
      String privateKey = config.propertyValueString(privateKeyProperty);
      if (GrouperUtil.isBlank(privateKey)) {
        ret.add("Undefined or blank property: " + privateKeyProperty);
      }
      String thumbprintProperty = configPrefix + "certificateThumbprint";
      String thumbprint = config.propertyValueString(thumbprintProperty);
      if (GrouperUtil.isBlank(thumbprint)) {
        ret.add("Undefined or blank property: " + thumbprintProperty);
      }
    } else {
      String clientSecretProperty = configPrefix + "clientSecret";
      String clientSecret = config.propertyValueString(clientSecretProperty);
      if (GrouperUtil.isBlank(clientSecret)) {
        ret.add("Undefined or blank property: " + clientSecretProperty);
      }
    }

    String tenantIdProperty = configPrefix + "tenantId";
    String tenantId = config.propertyValueString(tenantIdProperty);
    if (GrouperUtil.isBlank(tenantId)) {
      ret.add("Undefined or blank property: " + tenantIdProperty);
    }

    String scope = config.propertyValueString(configPrefix + "scope");

    try {
      
      retrieveBearerTokenForAzureConfigId(new HashMap<String, Object>(), this.getConfigId());

    } catch (Exception e) {
      ret.add("Unable to retrieve Azure authentication token: " + GrouperUtil.escapeHtml(e.getMessage(), true));
    }

    return ret;
  }

  /**
   * Get a bearer token for an Azure config id at the default audience configured
   * on that connector (configured "resource"). Equivalent to passing null for scope.
   * @param configId
   * @return the bearer token
   */
  public static String retrieveBearerTokenForAzureConfigId(Map<String, Object> debugMap, String configId) {
    return retrieveBearerTokenForAzureConfigId(debugMap, configId, null);
  }

  /**
   * Get a bearer token for an Azure config id at a caller-supplied scope. Useful
   * when one app registration is consented for multiple resource audiences
   * (Graph, Power BI, Fabric, ...): Azure issues a separate token per audience,
   * and tokens are cached separately per (configId, scope).
   *
   * When scope is blank, falls back to the connector's configured resource:
   *   - cert path: scope = &lt;resource&gt;/.default at the v2.0 token endpoint
   *   - secret path: resource form-param at the v1.0 token endpoint (legacy behavior)
   *
   * When scope is non-blank, both paths use the v2.0 token endpoint with the
   * supplied scope form-param.
   *
   * @param debugMap optional debug map to record cache hits, errors, timings
   * @param configId azure external system config id
   * @param scope optional /.default-form scope (e.g. SCOPE_POWER_BI); when blank,
   *   falls back to the configured resource
   * @return the bearer token
   */
  public static String retrieveBearerTokenForAzureConfigId(Map<String, Object> debugMap, String configId, String scope) {

    long startedNanos = System.nanoTime();

    String cacheKey = configId + "|" + StringUtils.defaultString(scope);

    MultiKey expiresOnAndEncryptedBearerToken = configKeyToExpiresOnAndBearerToken.get(cacheKey);

    String encryptedBearerToken = null;
    if (expiresOnAndEncryptedBearerToken != null) {
      long expiresOnSeconds = (Long)expiresOnAndEncryptedBearerToken.getKey(0);
      encryptedBearerToken = (String)expiresOnAndEncryptedBearerToken.getKey(1);
      if (expiresOnSeconds * 1000 > System.currentTimeMillis()) {
        // use it
        if (debugMap != null) {
          debugMap.put("azureCachedAccessToken", true);
        }
        return Morph.decrypt(encryptedBearerToken);
      }
    }
    try {
      // we need to get another one
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignDoNotLogHeaders(AzureMockServiceHandler.doNotLogHeaders).assignDoNotLogParameters(AzureMockServiceHandler.doNotLogParameters);

      boolean logAuthenticationResponseBody = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("grouper.azureConnector." + configId + ".logAuthenticationResponseBody", false);
      if (!logAuthenticationResponseBody) {
        grouperHttpClient.assignDoNotLogResponseBody(true);
      }

      String loginEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.azureConnector." + configId + ".loginEndpoint", "https://login.microsoftonline.com/");
      String directoryId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".tenantId");

      String authenticationType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.azureConnector." + configId + ".authenticationType", "clientSecret");
      boolean useCertificate = StringUtils.equals(authenticationType, "certificate");
      boolean explicitScope = StringUtils.isNotBlank(scope);
      // cert auth and explicit-scope requests both require the v2.0 token endpoint;
      // client_secret with no explicit scope keeps using v1.0 for back-compat
      boolean useV2 = useCertificate || explicitScope;

      final String url = loginEndpoint + (loginEndpoint.endsWith("/") ? "" : "/") + directoryId + (useV2 ? "/oauth2/v2.0/token" : "/oauth2/token");
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      grouperHttpClient.assignUrl(url);

      String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.azureConnector." + configId + ".proxyUrl");
      String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.azureConnector." + configId + ".proxyType");

      grouperHttpClient.assignProxyUrl(proxyUrl);
      grouperHttpClient.assignProxyType(proxyType);

      String clientId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".clientId");
      grouperHttpClient.addBodyParameter("client_id", clientId);

      String resource = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.azureConnector." + configId + ".resource", "https://graph.microsoft.com");

      if (useCertificate) {
        String privateKeyPem = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".privateKey");
        privateKeyPem = Morph.decryptIfFile(privateKeyPem);
        String thumbprintHex = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".certificateThumbprint");

        String clientAssertion;
        try {
          PrivateKey privateKey = loadRsaPrivateKeyFromPem(privateKeyPem);
          clientAssertion = buildClientAssertionJwt(clientId, url, thumbprintHex, privateKey);
        } catch (Exception e) {
          throw new RuntimeException("Error building client_assertion JWT for azure config '" + configId + "'", e);
        }

        grouperHttpClient.addBodyParameter("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");
        grouperHttpClient.addBodyParameter("client_assertion", clientAssertion);
        grouperHttpClient.addBodyParameter("grant_type", "client_credentials");

        String effectiveScope = explicitScope ? scope : (resource + (resource.endsWith("/") ? "" : "/") + ".default");
        grouperHttpClient.addBodyParameter("scope", effectiveScope);
      } else {
        String clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".clientSecret");
        clientSecret = Morph.decryptIfFile(clientSecret);
        grouperHttpClient.addBodyParameter("client_secret", clientSecret);
        grouperHttpClient.addBodyParameter("grant_type", "client_credentials");
        if (explicitScope) {
          grouperHttpClient.addBodyParameter("scope", scope);
        } else {
          grouperHttpClient.addBodyParameter("resource", resource);
        }
      }

      int code = -1;
      String json = null;

      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());

        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }

      if (code != 200) {
        throw new RuntimeException("Cant get access token from '" + url + "' " + code + ", " + json);
      }

      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      // v1.0 returns absolute "expires_on"; v2.0 returns relative "expires_in" — handle both
      long expiresOn = GrouperUtil.jsonJacksonGetLong(jsonObject, "expires_on", -1L);
      if (expiresOn <= 0) {
        long expiresIn = GrouperUtil.jsonJacksonGetLong(jsonObject, "expires_in", -1L);
        if (expiresIn > 0) {
          expiresOn = (System.currentTimeMillis() / 1000L) + expiresIn;
        }
      }
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonObject, "access_token");

      expiresOnAndEncryptedBearerToken = new MultiKey(expiresOn, Morph.encrypt(accessToken));
      configKeyToExpiresOnAndBearerToken.put(cacheKey, expiresOnAndEncryptedBearerToken);
      return accessToken;
    } catch (RuntimeException re) {

      if (debugMap != null) {
        debugMap.put("azureTokenError", GrouperUtil.getFullStackTrace(re));
      }
      throw re;

    } finally {
      if (debugMap != null) {
        debugMap.put("azureTokenTookMillis", (System.nanoTime()-startedNanos)/1000000);
      }
    }
  }

  /**
   * Parse an RSA private key in PEM format. Accepts PKCS#8 (-----BEGIN PRIVATE KEY-----) directly,
   * and PKCS#1 (-----BEGIN RSA PRIVATE KEY-----) by wrapping into a PKCS#8 envelope.
   */
  private static PrivateKey loadRsaPrivateKeyFromPem(String pem) throws Exception {

    byte[] pkcs8Der;
    if (pem.contains("-----BEGIN PRIVATE KEY-----")) {
      String body = pem.replace("-----BEGIN PRIVATE KEY-----", "");
      body = body.replace("-----END PRIVATE KEY-----", "");
      body = body.replaceAll("\\s+", "");
      pkcs8Der = Base64.getDecoder().decode(body);
    } else if (pem.contains("-----BEGIN RSA PRIVATE KEY-----")) {
      String body = pem.replace("-----BEGIN RSA PRIVATE KEY-----", "");
      body = body.replace("-----END RSA PRIVATE KEY-----", "");
      body = body.replaceAll("\\s+", "");
      byte[] pkcs1 = Base64.getDecoder().decode(body);
      pkcs8Der = wrapPkcs1AsPkcs8(pkcs1);
    } else if (pem.contains("-----BEGIN ENCRYPTED PRIVATE KEY-----")) {
      throw new RuntimeException("Encrypted private key not supported; decrypt with: openssl pkcs8 -in src.key -out dst.key -nocrypt");
    } else {
      throw new RuntimeException("Unrecognized PEM header in private key");
    }

    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Der);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePrivate(keySpec);
  }

  /**
   * Wrap a PKCS#1 RSA private key DER blob in a PKCS#8 envelope so KeyFactory("RSA") accepts it.
   * Builds: SEQUENCE { INTEGER 0, AlgorithmIdentifier(rsaEncryption, NULL), OCTET STRING(pkcs1) }
   */
  private static byte[] wrapPkcs1AsPkcs8(byte[] pkcs1) throws Exception {

    ByteArrayOutputStream algId = new ByteArrayOutputStream();
    algId.write(0x30); algId.write(0x0D);
    algId.write(0x06); algId.write(0x09);
    algId.write(0x2A); algId.write(0x86); algId.write(0x48); algId.write(0x86);
    algId.write(0xF7); algId.write(0x0D); algId.write(0x01); algId.write(0x01); algId.write(0x01);
    algId.write(0x05); algId.write(0x00);

    ByteArrayOutputStream octetString = new ByteArrayOutputStream();
    octetString.write(0x04);
    writeDerLength(octetString, pkcs1.length);
    octetString.write(pkcs1);

    ByteArrayOutputStream inner = new ByteArrayOutputStream();
    inner.write(0x02); inner.write(0x01); inner.write(0x00);
    inner.write(algId.toByteArray());
    inner.write(octetString.toByteArray());

    ByteArrayOutputStream outer = new ByteArrayOutputStream();
    outer.write(0x30);
    writeDerLength(outer, inner.size());
    outer.write(inner.toByteArray());

    return outer.toByteArray();
  }

  private static void writeDerLength(ByteArrayOutputStream out, int len) {
    if (len < 0x80) {
      out.write(len);
    } else if (len < 0x100) {
      out.write(0x81);
      out.write(len);
    } else if (len < 0x10000) {
      out.write(0x82);
      out.write((len >> 8) & 0xFF);
      out.write(len & 0xFF);
    } else {
      out.write(0x83);
      out.write((len >> 16) & 0xFF);
      out.write((len >> 8) & 0xFF);
      out.write(len & 0xFF);
    }
  }

  /**
   * Build a JWT client_assertion for Azure AD certificate-based auth.
   * Header: { alg:RS256, typ:JWT, x5t:<base64url(sha1Bytes)> }
   * Claims: { iss, sub, aud, jti, nbf, exp }
   */
  private static String buildClientAssertionJwt(String clientId, String audience,
      String thumbprintHex, PrivateKey privateKey) throws Exception {

    byte[] thumbprintBytes = hexStringToBytes(thumbprintHex);
    String x5t = Base64.getUrlEncoder().withoutPadding().encodeToString(thumbprintBytes);

    ObjectMapper mapper = new ObjectMapper();

    ObjectNode header = mapper.createObjectNode();
    header.put("alg", "RS256");
    header.put("typ", "JWT");
    header.put("x5t", x5t);

    long nowSeconds = System.currentTimeMillis() / 1000L;

    ObjectNode claims = mapper.createObjectNode();
    claims.put("iss", clientId);
    claims.put("sub", clientId);
    claims.put("aud", audience);
    claims.put("jti", UUID.randomUUID().toString());
    claims.put("nbf", nowSeconds);
    claims.put("exp", nowSeconds + 600);

    String headerB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(mapper.writeValueAsBytes(header));
    String claimsB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(mapper.writeValueAsBytes(claims));
    String signingInput = headerB64 + "." + claimsB64;

    Signature signer = Signature.getInstance("SHA256withRSA");
    signer.initSign(privateKey);
    signer.update(signingInput.getBytes("UTF-8"));
    byte[] signatureBytes = signer.sign();

    return signingInput + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
  }

  private static byte[] hexStringToBytes(String hex) {
    String clean = hex.replaceAll("\\s+", "").replace(":", "");
    int len = clean.length();
    byte[] out = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      int hi = Character.digit(clean.charAt(i), 16);
      int lo = Character.digit(clean.charAt(i + 1), 16);
      out[i / 2] = (byte) ((hi << 4) | lo);
    }
    return out;
  }

}
