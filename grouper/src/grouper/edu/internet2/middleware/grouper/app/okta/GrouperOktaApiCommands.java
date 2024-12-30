package edu.internet2.middleware.grouper.app.okta;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.duo.GrouperDuoLog;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperHttpThrottlingCallback;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.morphString.Morph;

public class GrouperOktaApiCommands {
  
  /**
   * cache of config key to encrypted bearer token
   */
  private static ExpirableCache<String, String> configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, String>();
  private static ExpirableCache<String, String> configKeyToExpiresOnAndSettingsToken = new ExpirableCache<String, String>();

  
  static class OktaRsaKeyProvider implements RSAKeyProvider {
    
    private RSAPrivateKey privateKey;
    private String publicKeyId;
    
    OktaRsaKeyProvider(PrivateKey privateKey, String publicKeyId) {
     this.privateKey = (RSAPrivateKey)privateKey; 
     this.publicKeyId = publicKeyId;
    }
    
    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      throw new RuntimeException("not implemented");
    }
    
    @Override
    public String getPrivateKeyId() {
      return this.publicKeyId;
    }
    
    @Override
    public RSAPrivateKey getPrivateKey() {
      return privateKey;
    }
      
  }
  
  /**
   * get access token from okta
   * @param debugMap
   * @param configId
   * @param scope
   * @return token in the first index and its expiry in the second index
   */
  private static Object[] generateAccessToken(Map<String, Object> debugMap, String configId) {
    
    long startedNanos = System.nanoTime();
    
    try {
      
      String clientId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.oktaConnector." + configId + ".clientId");
      String tenantDomain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.oktaConnector." + configId + ".tenantDomain");
      String privateKeyString = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.oktaConnector." + configId + ".privateKey");
      String publicKeyId = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.oktaConnector." + configId + ".publicKeyId");
      PrivateKey privateKey;
      try {
        
        privateKeyString = privateKeyString.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        // Decode the Base64 encoded key
        byte[] keyBytes = java.util.Base64.getDecoder().decode(privateKeyString);
        
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        privateKey = kf.generatePrivate(keySpec);
        
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("Could not reconstruct the private key, the given algorithm could not be found.", e);
      } catch (InvalidKeySpecException e) {
        throw new RuntimeException("Could not reconstruct the private key", e);
      } catch (Exception e) {
        throw new RuntimeException("Could not construct private key from key contents", e);
      }
      
      Algorithm algorithm = Algorithm.RSA256(new OktaRsaKeyProvider(privateKey, publicKeyId));
      
      long now = System.currentTimeMillis();
      
      String tokenUrl = tenantDomain + (tenantDomain.endsWith("/") ? "" : "/") +  "oauth2/v1/token";
      
      Builder jwtBuilder = JWT.create()
          .withIssuer(clientId)
          .withSubject(clientId)
          .withAudience(tokenUrl)
          .withIssuedAt(new Date())
          .withJWTId(UUID.randomUUID().toString())
          .withExpiresAt(new Date(now + 3600 * 1000L));
        
      String signedJwt = jwtBuilder.sign(algorithm);
      
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      grouperHttpClient.assignUrl(tokenUrl);
      grouperHttpClient.assignDoNotLogResponseBody(true);
      grouperHttpClient.assignDoNotLogRequestBody(true);
      
      String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.oktaConnector." + configId + ".proxyUrl");
      String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.oktaConnector." + configId + ".proxyType");
      
      grouperHttpClient.assignProxyUrl(proxyUrl);
      grouperHttpClient.assignProxyType(proxyType);
      
//      String privateKeyFilePath = GrouperConfig.retrieveConfig().propertyValueString("grouper.oktaConnector." + configId + ".serviceAccountPKCS12FilePath");
//
//      String privateKeyString = GrouperConfig.retrieveConfig().propertyValueString("grouper.oktaConnector." + configId + ".serviceAccountPrivateKeyPEM");
//      
//      PrivateKey privateKey = null;
//      
//      if (StringUtils.isNotBlank(privateKeyFilePath)) {
//        try {
//          KeyStore keyStore = KeyStore.getInstance("PKCS12");
//          keyStore.load(new FileInputStream(privateKeyFilePath), "notasecret".toCharArray());
//          privateKey = (PrivateKey) keyStore.getKey("privatekey", "notasecret".toCharArray());
//        } catch (Exception e) {
//          throw new RuntimeException("Could not construct private key from p12 file", e);
//        }
//      } else if (StringUtils.isNotBlank(privateKeyString)) {
//        
//        try {
//          
//          privateKeyString = privateKeyString.replaceAll("\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
//          PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString));
//          
//          KeyFactory kf = KeyFactory.getInstance("RSA");
//          privateKey = kf.generatePrivate(keySpec);
//          
//        } catch (NoSuchAlgorithmException e) {
//          throw new RuntimeException("Could not reconstruct the private key, the given algorithm could not be found.", e);
//        } catch (InvalidKeySpecException e) {
//          throw new RuntimeException("Could not reconstruct the private key", e);
//        }
//        
//      } else {
//        throw new RuntimeException("Supply privateKeyFilePath or privateKeyFileString");
//      }
      
      grouperHttpClient.addBodyParameter("client_assertion", signedJwt);
      grouperHttpClient.addBodyParameter("scope", "okta.users.manage okta.groups.manage");
      grouperHttpClient.addBodyParameter("grant_type", "client_credentials");
      grouperHttpClient.addBodyParameter("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer");

      grouperHttpClient.assignDoNotLogParameters("client_assertion");
      
      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + tokenUrl + "'", e);
      }
  
      if (code != 200) {
        throw new RuntimeException("Cant get access token from '" + tokenUrl + "' " + code + ", " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      int expiresInSeconds = GrouperUtil.jsonJacksonGetInteger(jsonObject, "expires_in");
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonObject, "access_token");
      return new Object[] {accessToken, expiresInSeconds};
      
    } catch (RuntimeException re) {
      
      if (debugMap != null) {
        debugMap.put("oktaTokenError", GrouperUtil.getFullStackTrace(re));
      }
      throw re;
  
    } finally {
      if (debugMap != null) {
        debugMap.put("oktaTokenTookMillis", (System.nanoTime()-startedNanos)/1000000);
      }
    }
  }
  
  /**
   * get bearer token for okta config id
   * @param configId
   * @return the bearer token
   */
  public static String retrieveBearerTokenForOktaConfigId(Map<String, Object> debugMap, String configId) {
    
    String encryptedBearerToken = configKeyToExpiresOnAndBearerToken.get(configId);
  
    if (StringUtils.isNotBlank(encryptedBearerToken)) {
      if (debugMap != null) {
        debugMap.put("oktaCachedAccessToken", true);
      }
      return Morph.decrypt(encryptedBearerToken);
    }
    
    Object[] accessTokenAndExpiry = generateAccessToken(debugMap, configId);
    
    String accessToken = GrouperUtil.toStringSafe(accessTokenAndExpiry[0]);
    int expiresInSeconds = (Integer) accessTokenAndExpiry[1] - 5; // subtracting 5 just in case if there are network delays
    int timeToLive = expiresInSeconds/60;
    configKeyToExpiresOnAndBearerToken.put(configId, Morph.encrypt(accessToken), timeToLive - 5);
    return accessToken;
  }
  
  public static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId, String urlSuffix) {

    int[] returnCode = new int[] { -1 };
    JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
        GrouperUtil.toSet(200, 404), returnCode, null);
    
    if (returnCode[0] == 404) {
      return null;
    }
    
    return jsonNode;
  }

  public static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    grouperHttpCall.assignDebugMap(debugMap);
    
    String tenantDomain = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.oktaConnector." + configId + ".tenantDomain");
    
    String bearerToken = retrieveBearerTokenForOktaConfigId(debugMap, configId);
    
    String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.oktaConnector." + configId + ".proxyUrl");
    String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.oktaConnector." + configId + ".proxyType");
    
    grouperHttpCall.assignProxyUrl(proxyUrl);
    grouperHttpCall.assignProxyType(proxyType);
    
    String url = "";
    if (urlSuffix.startsWith("https://")) { // for pagination, we are passing the full url instead of url suffix
      url = urlSuffix;
    } else {      
      url = tenantDomain + (tenantDomain.endsWith("/") ? "" : "/") +  urlSuffix;
    }
    
    debugMap.put("url", url);

    grouperHttpCall.assignUrl(url);
    grouperHttpCall.assignGrouperHttpMethod(httpMethodName);
    
    grouperHttpCall.addHeader("Content-Type", "application/json");
    grouperHttpCall.addHeader("Authorization", "Bearer " + bearerToken);
    grouperHttpCall.assignBody(body);
    
    grouperHttpCall.setRetryForThrottlingOrNetworkIssuesSleepMillis(120*1000L); // 2mins
    
    grouperHttpCall.setThrottlingCallback(new GrouperHttpThrottlingCallback() {
      
      @Override
      public boolean setupThrottlingCallback(GrouperHttpClient httpClient) {
        boolean isThrottle = httpClient.getResponseCode() == 403 
            || httpClient.getResponseCode() == 429 || httpClient.getResponseCode() == 503;
        if (isThrottle) {                
          GrouperUtil.mapAddValue(debugMap, "throttleCount", 1);
        }
        return isThrottle;
      }
    });
    
    grouperHttpCall.executeRequest();
    
    int code = -1;
    String json = null;

    Map<String,String> prevNextHeader = new HashMap<String, String>();
    try {
      code = grouperHttpCall.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpCall.getResponseBody();
      Map<String,String> responseHeaders = grouperHttpCall.getResponseHeaders();
      if (responseHeaders.containsKey("link")) {
        prevNextHeader = parseLinkHeader(responseHeaders.get("link"));
      }
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException(
          "Invalid return code '" + code + "', expecting: " + GrouperUtil.setToString(allowedReturnCodes)
              + ". '" + debugMap.get("url") + "' " + json);
    }

    if (StringUtils.isBlank(json)) {
      return null;
    }

    try {
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      JsonNode dataNode = GrouperUtil.jsonJacksonNode(json);
      resultNode.set("data", dataNode);
      if (prevNextHeader.containsKey("prev") || prevNextHeader.containsKey("next")) {
        ObjectNode paginationNode = GrouperUtil.jsonJacksonNode();
        resultNode.set("pagination", paginationNode);
        if (prevNextHeader.containsKey("prev")) {      
          GrouperUtil.jsonJacksonAssignString(paginationNode, "prev", prevNextHeader.get("prev"));
        }
        if (prevNextHeader.containsKey("next")) { 
          GrouperUtil.jsonJacksonAssignString(paginationNode, "next", prevNextHeader.get("next"));
        }
        
      }
      return resultNode;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }

  }
  
  private static Map<String, String> parseLinkHeader(String header) {
    
    Map<String, String> links = new HashMap<>();
    
    if (StringUtils.isBlank(header)) {
      return links;
    }

    // Split the header into individual links
    String[] parts = header.split(", ");
    for (String part : parts) {
        // Match the URL and rel value
        String[] sections = part.split("; ");
        if (sections.length == 2) {
            String url = sections[0].trim();
            String rel = sections[1].trim();

            // Remove the angle brackets from the URL
            if (url.startsWith("<") && url.endsWith(">")) {
                url = url.substring(1, url.length() - 1);
            }

            // Extract the rel value
            if (rel.startsWith("rel=\"") && rel.endsWith("\"")) {
                rel = rel.substring(5, rel.length() - 1);
            }

            // Add to the map
            links.put(rel, url);
        }
    }

    return links;
  }

  /**
   * create a group
   * @param grouperOktaGroup
   * @return the result
   */
  public static GrouperOktaGroup createOktaGroup(String configId,
      GrouperOktaGroup grouperOktaGroup, Set<String> fieldsToInsert) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createOktaGroup");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperOktaGroup.toJsonGroupOnly(fieldsToInsert);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/v1/groups", GrouperUtil.toSet(200), 
          new int[] { -1 }, jsonStringToSend);
      
      GrouperOktaGroup grouperOktaGroupResult = null;
      if (jsonNode != null && jsonNode.has("data")) {        
        grouperOktaGroupResult = GrouperOktaGroup.fromJson(jsonNode.get("data"));
      }

      return grouperOktaGroupResult;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }
  
  //https://www.baeldung.com/java-generate-secure-password
  private static String generateRandomPassword() {
    String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
    String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
    String numbers = RandomStringUtils.randomNumeric(2);
    String specialChar = RandomStringUtils.random(2, 33, 47, false, false);
    String totalChars = RandomStringUtils.randomAlphanumeric(2);
    String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
      .concat(numbers)
      .concat(specialChar)
      .concat(totalChars);
    List<Character> pwdChars = combinedChars.chars()
      .mapToObj(c -> (char) c)
      .collect(Collectors.toList());
    Collections.shuffle(pwdChars);
    String password = pwdChars.stream()
      .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
      .toString();
    return password;
  }
  
  /**
   * create a user
   * @param configId
   * @param grouperOktaUser
   * @return
   */
  public static GrouperOktaUser createOktaUser(String configId, GrouperOktaUser grouperOktaUser) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createOktaUser");

    long startTime = System.nanoTime();

    try {
      
      JsonNode jsonToSend = grouperOktaUser.toJson(null);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/v1/users",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);
      
      GrouperOktaUser grouperOktaUserResult = null;
      if (jsonNode != null && jsonNode.has("data")) {
        grouperOktaUserResult = GrouperOktaUser.fromJson(jsonNode.get("data"));
      }
      
      return grouperOktaUserResult;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
    
    
  }

  /**
   * create a membership
   *
   * @param configId
   * @param groupId
   * @param userId
   */
  public static void createOktaMembership(String configId,
      String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createOktaMembership");

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();
      objectNode.put("id", userId);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);

      //api/v1/groups/00gmvgcs9mpZKSfAX697/users/00umxoh7cgDm3zD9v697
      String urlSuffix = "api/v1/groups/"+groupId+"/users/"+userId;

      executeMethod(debugMap, "PUT", configId, urlSuffix, GrouperUtil.toSet(204), 
          new int[] { -1 }, jsonStringToSend);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }

  public static void deleteOktaUser(String configId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteOktaUser");
    
    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
      
      //api/v1/users/00umxoh7cgDm3zD9v697
      int[] returnCode = new int[1];
      //first delete marks the user as deactivated and the second one actually deletes it
      executeMethod(debugMap, "DELETE", configId, "api/v1/users/"+userId,
          GrouperUtil.toSet(200, 204, 404), returnCode, null);
      
      if (returnCode[0] == 200 || returnCode[0] == 204) {
        executeMethod(debugMap, "DELETE", configId, "api/v1/users/"+userId,
            GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * update a group except the managers and owners of the group
   * @param grouperOktaGroup
   * @return the result
   */
  public static GrouperOktaGroup updateOktaGroup(String configId,
      GrouperOktaGroup grouperOktaGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateOktaGroup");

    long startTime = System.nanoTime();

    try {

      String id = grouperOktaGroup.getId();
      
      JsonNode jsonToSend = grouperOktaGroup.toJsonGroupOnly(fieldsToUpdate);
      
      GrouperOktaGroup updatedOktaGroup = null;
      
      if (jsonToSend.size() > 0) {
        //api/v1/groups/00gmxp8w6iYQLHtEN697
        String urlSuffix = "api/v1/groups/"+id;
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

        JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);

        updatedOktaGroup = GrouperOktaGroup.fromJson(jsonNode);
      }
      
      return updatedOktaGroup;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }
  
  /**
   * update a user
   * @param grouperOktaUser
   * @return the result
   */
  public static GrouperOktaUser updateOktaUser(String configId,
      GrouperOktaUser grouperOktaUser, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateOktaUser");

    long startTime = System.nanoTime();

    try {

      String id = grouperOktaUser.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperOktaUser);
      }

      if (fieldsToUpdate.contains("id")) {
        throw new RuntimeException("Cant update the id field: " + grouperOktaUser + ", " + GrouperUtil.setToString(fieldsToUpdate));
      }
      
      JsonNode jsonToSend = grouperOktaUser.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, "api/v1/users/"+id,
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend);

      GrouperOktaUser grouperOktaUserResult = GrouperOktaUser.fromJson(jsonNode);

      return grouperOktaUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }

  public static void deleteOktaGroup(String configId,String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteOktaGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "api/v1/groups/"+groupId,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }
  }

  public static List<GrouperOktaGroup> retrieveOktaGroups(String configId, String fieldToSearchFor,
      String fieldValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaGroups");

    long startTime = System.nanoTime();

    try {

      List<GrouperOktaGroup> results = new ArrayList<GrouperOktaGroup>();

      String nextPageUrl = null;
      String previousPageUrl = null;
      boolean firstRequest = true;
      
      String urlSuffixConstant = "api/v1/groups";
      
      if (StringUtils.isNotBlank(fieldToSearchFor)) {
        
        String filterValue = fieldToSearchFor + " eq "+ "\"" + fieldValue + "\"";
        String urlEncodedFilter = GrouperUtil.escapeUrlEncode(filterValue);
        urlSuffixConstant = urlSuffixConstant + "?search="+urlEncodedFilter;
      }
      
      int maxCalls = 10000;
      int numberOfCalls = 0;
      while (StringUtils.isNotBlank(nextPageUrl) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + results.size()
             + ", numberOfCalls: " + numberOfCalls);
        }
        
        firstRequest = false;
        
        String urlSuffix = nextPageUrl != null ? nextPageUrl : urlSuffixConstant;
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
        numberOfCalls++;
        
        ArrayNode groupsArray = (ArrayNode) jsonNode.get("data");
        
        JsonNode paginationNode = jsonNode.get("pagination");
        if (paginationNode != null && paginationNode.get("next") != null) {
          nextPageUrl = paginationNode.get("next").asText();
        } else {
          nextPageUrl = null;
        }
        
        if (groupsArray == null || groupsArray.size() == 0) {
          break;
        }

        for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
          JsonNode groupNode = groupsArray.get(i);
          GrouperOktaGroup grouperOktaGroup = GrouperOktaGroup.fromJson(groupNode);
          
          results.add(grouperOktaGroup);
        }
        
        if (StringUtils.isNotBlank(previousPageUrl) && StringUtils.isNotBlank(nextPageUrl) && StringUtils.equals(previousPageUrl, nextPageUrl)) {
          break;
        }
        previousPageUrl = nextPageUrl;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }

  public static List<GrouperOktaUser> retrieveOktaUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaUsers");

    long startTime = System.nanoTime();

    try {

      List<GrouperOktaUser> results = new ArrayList<GrouperOktaUser>();
      
      String nextPageUrl = null;
      boolean firstRequest = true;
      
      String urlSuffixConstant = "api/v1/users";
      
      int maxCalls = 1000000;
      int numberOfCalls = 0;
      String previousPageToken = null;
      while (StringUtils.isNotBlank(nextPageUrl) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + results.size()
             + ", numberOfCalls: " + numberOfCalls);
        }

        firstRequest = false;
        String urlSuffix = nextPageUrl != null ? nextPageUrl : urlSuffixConstant;
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
        numberOfCalls++;
        
        ArrayNode usersArray = (ArrayNode) jsonNode.get("data");
        
        JsonNode paginationNode = jsonNode.get("pagination");
        if (paginationNode != null && paginationNode.get("next") != null) {
          nextPageUrl = paginationNode.get("next").asText();
        } else {
          nextPageUrl = null;
        }
        
        if (usersArray == null || usersArray.size() == 0) {
          break;
        }

        for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
          JsonNode userNode = usersArray.get(i);
          GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromJson(userNode);
          results.add(grouperOktaUser);
        }
        
        if (StringUtils.isNotBlank(previousPageToken) && StringUtils.isNotBlank(nextPageUrl) && StringUtils.equals(previousPageToken, nextPageUrl)) {
          break;
        }
        previousPageToken = nextPageUrl;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }

  /**
   * @param configId
   * @param id of the user
   * @return okta user
   */
  public static GrouperOktaUser retrieveOktaUser(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaUser");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "api/v1/users/"+id;
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
      
      if (jsonNode == null || jsonNode.get("data") == null) {
        return null;
      }
      
      GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromJson(jsonNode.get("data"));
      return grouperOktaUser;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }
  

  /**
   * return user ids in the group
   * @param configId
   * @param groupId
   * @return user ids
   */
  public static Set<String> retrieveOktaGroupMembers(String configId, String groupId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaGroupMembers");

    long startTime = System.nanoTime();

    try {

      Set<String> memberIds = new HashSet<String>();
      
      String nextPageUrl = null;
      boolean firstRequest = true;
      
      //api/v1/groups/00gmvgcs9mpZKSfAX697/users
      String urlSuffixConstant = "api/v1/groups/"+groupId+"/skinny_users";

      int maxCalls = 10000000;
      int numberOfCalls = 0;
      String previousPageUrl = null;
      while (StringUtils.isNotBlank(nextPageUrl) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + memberIds.size()
             + ", numberOfCalls: " + numberOfCalls);
        }
        
        firstRequest = false;
        String urlSuffix = nextPageUrl != null ? nextPageUrl : urlSuffixConstant;
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
        numberOfCalls++;
        
        ArrayNode membersArray = (ArrayNode)jsonNode.get("data");
        
        if (membersArray == null || membersArray.size() == 0) {
          break;
        }
        
        JsonNode paginationNode = jsonNode.get("pagination");
        if (paginationNode != null && paginationNode.get("next") != null) {
          nextPageUrl = paginationNode.get("next").asText();
        } else {
          nextPageUrl = null;
        }

        for (int i = 0; i < (membersArray == null ? 0 : membersArray.size()); i++) {
          JsonNode memberNode = membersArray.get(i);
          
          String memberId = GrouperUtil.jsonJacksonGetString(memberNode, "id");
          
          memberIds.add(memberId);
        }
        
        if (StringUtils.isNotBlank(previousPageUrl) && StringUtils.isNotBlank(nextPageUrl) && StringUtils.equals(previousPageUrl, nextPageUrl)) {
          break;
        }
        previousPageUrl = nextPageUrl;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(memberIds));

      return memberIds;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }
  }
  
  /**
   * @param configId
   * @param id is the group id
   * @return the okta group
   */
  public static GrouperOktaGroup retrieveOktaGroup(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveOktaGroup");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "api/v1/groups/"+id;
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);
      
      if (jsonNode == null || jsonNode.get("data") == null) {
        return null;
      }
      
      GrouperOktaGroup grouperOktaGroup = GrouperOktaGroup.fromJson(jsonNode.get("data"));
      
      return grouperOktaGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }

  }



  /**
   * delete membership
   * @param grouperOktaGroup
   * @return the result
   */
  public static void deleteOktaMembership(String configId, String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteOktaMembership");

    long startTime = System.nanoTime();

    try {
     // api/v1/groups/00gmvgcs9mpZKSfAX697/users/00umxoh7cgDm3zD9v697
      executeMethod(debugMap, "DELETE", configId, "api/v1/groups/"+groupId+"/users/"+userId,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null);
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperOktaLog.oktaLog(debugMap, startTime);
    }
  }

}
