package edu.internet2.middleware.grouper.app.google;

import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.duo.GrouperDuoLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperHttpThrottlingCallback;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.morphString.Morph;

public class GrouperGoogleApiCommands {
  
  /**
   * cache of config key to encrypted bearer token
   */
  private static ExpirableCache<String, String> configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, String>();
  private static ExpirableCache<String, String> configKeyToExpiresOnAndSettingsToken = new ExpirableCache<String, String>();

  
  static class GoogleRsaKeyProvider implements RSAKeyProvider {
    
    private RSAPrivateKey privateKey;
    
    GoogleRsaKeyProvider(PrivateKey privateKey) {
     this.privateKey = (RSAPrivateKey)privateKey; 
    }
    
    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      throw new RuntimeException("not implemented");
    }
    
    @Override
    public String getPrivateKeyId() {
      return "privateKeyId";
    }
    
    @Override
    public RSAPrivateKey getPrivateKey() {
      return privateKey;
    }
      
  }
  
  /**
   * get access token from google
   * @param debugMap
   * @param configId
   * @param scope
   * @return token in the first index and its expiry in the second index
   */
  private static Object[] generateAccessToken(Map<String, Object> debugMap, String configId, String scope) {
    
    long startedNanos = System.nanoTime();
    
    try {
      // we need to get another one
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      
      final String url = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".tokenUrl", "https://oauth2.googleapis.com/token");
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignDoNotLogResponseBody(true);
      grouperHttpClient.assignDoNotLogRequestBody(true);
      
      String proxyUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".proxyUrl");
      String proxyType = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".proxyType");
      
      grouperHttpClient.assignProxyUrl(proxyUrl);
      grouperHttpClient.assignProxyType(proxyType);
      
      String signedJwt = null;

      String privateKeyFilePath = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountPKCS12FilePath");

      String privateKeyString = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountPrivateKeyPEM");
      
      String serviceAccountEmail = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountEmail");
      
      String serviceImpersonationUser = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceImpersonationUser");
      
      PrivateKey privateKey = null;
      
      if (StringUtils.isNotBlank(privateKeyFilePath)) {
        try {
          KeyStore keyStore = KeyStore.getInstance("PKCS12");
          keyStore.load(new FileInputStream(privateKeyFilePath), "notasecret".toCharArray());
          privateKey = (PrivateKey) keyStore.getKey("privatekey", "notasecret".toCharArray());
        } catch (Exception e) {
          throw new RuntimeException("Could not construct private key from p12 file", e);
        }
      } else if (StringUtils.isNotBlank(privateKeyString)) {
        
        try {
          
          privateKeyString = privateKeyString.replaceAll("\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
          PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString));
          
          KeyFactory kf = KeyFactory.getInstance("RSA");
          privateKey = kf.generatePrivate(keySpec);
          
        } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException("Could not reconstruct the private key, the given algorithm could not be found.", e);
        } catch (InvalidKeySpecException e) {
          throw new RuntimeException("Could not reconstruct the private key", e);
        }
        
      } else {
        throw new RuntimeException("Supply privateKeyFilePath or privateKeyFileString");
      }
      
      Algorithm algorithm = Algorithm.RSA256(new GoogleRsaKeyProvider(privateKey));
      
      long now = System.currentTimeMillis();

      signedJwt = JWT.create()
          .withKeyId("privateKeyId")
          .withIssuer(serviceAccountEmail)
          .withSubject(serviceImpersonationUser)
          .withAudience("https://oauth2.googleapis.com/token")
          .withClaim("scope", scope)
          .withIssuedAt(new Date(now))
          .withExpiresAt(new Date(now + 3600 * 1000L))
          .sign(algorithm);
      
      grouperHttpClient.addBodyParameter("assertion", signedJwt);
      grouperHttpClient.addBodyParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

      grouperHttpClient.assignDoNotLogParameters("assertion");
      
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
      int expiresInSeconds = GrouperUtil.jsonJacksonGetInteger(jsonObject, "expires_in");
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonObject, "access_token");
      return new Object[] {accessToken, expiresInSeconds};
      
    } catch (RuntimeException re) {
      
      if (debugMap != null) {
        debugMap.put("googleTokenError", GrouperUtil.getFullStackTrace(re));
      }
      throw re;
  
    } finally {
      if (debugMap != null) {
        debugMap.put("googleTokenTookMillis", (System.nanoTime()-startedNanos)/1000000);
      }
    }
  }
  
  /**
   * get bearer token for google config id
   * @param configId
   * @return the bearer token
   */
  private static String retrieveBearerTokenForGoogleConfigId(Map<String, Object> debugMap, String configId) {
    
    String encryptedBearerToken = configKeyToExpiresOnAndBearerToken.get(configId);
  
    if (StringUtils.isNotBlank(encryptedBearerToken)) {
      if (debugMap != null) {
        debugMap.put("googleCachedAccessToken", true);
      }
      return Morph.decrypt(encryptedBearerToken);
    }
    
    Object[] accessTokenAndExpiry = generateAccessToken(debugMap, configId, 
        "https://www.googleapis.com/auth/admin.directory.user https://www.googleapis.com/auth/admin.directory.group https://www.googleapis.com/auth/admin.directory.group.member");
    
    String accessToken = GrouperUtil.toStringSafe(accessTokenAndExpiry[0]);
    int expiresInSeconds = (Integer) accessTokenAndExpiry[1] - 5; // subtracting 5 just in case if there are network delays
    int timeToLive = expiresInSeconds/60;
    configKeyToExpiresOnAndBearerToken.put(configId, Morph.encrypt(accessToken), timeToLive - 5);
    return accessToken;
  }
  
  /**
   * get bearer token for google settings config id
   * @param configId
   * @return the bearer token
   */
  public static String retrieveBearerTokenForGoogleSettingsConfigId(Map<String, Object> debugMap, String configId) {
    
    String encryptedBearerToken = configKeyToExpiresOnAndSettingsToken.get(configId);
    
    if (StringUtils.isNotBlank(encryptedBearerToken)) {
      if (debugMap != null) {
        debugMap.put("googleCachedAccessTokenForSettings", true);
      }
      return Morph.decrypt(encryptedBearerToken);
    }
  
    Object[] accessTokenAndExpiry = generateAccessToken(debugMap, configId, "https://www.googleapis.com/auth/apps.groups.settings");
    
    String accessToken = GrouperUtil.toStringSafe(accessTokenAndExpiry[0]);
    int expiresInSeconds = (Integer) accessTokenAndExpiry[1] - 5; // subtracting 5 just in case if there are network delays
    int timeToLive = expiresInSeconds/60;
    configKeyToExpiresOnAndSettingsToken.put(configId, Morph.encrypt(accessToken), timeToLive);
    return accessToken;
  }
  
  public static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId, String urlSuffix, boolean useSettingsBearerToken) {

    int[] returnCode = new int[] { -1 };
    JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
        GrouperUtil.toSet(200, 404), returnCode, null, useSettingsBearerToken);
    
    if (returnCode[0] == 404) {
      return null;
    }
    
    return jsonNode;
  }

  public static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body, boolean useSettingsBearerToken) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    grouperHttpCall.assignDebugMap(debugMap);
    
    String bearerToken = null;
    String url =  null;
    if (useSettingsBearerToken) {
      String settingsApiBaseUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".groupSettingsApiBaseUrl", "https://www.googleapis.com/groups/v1/groups");
      if (settingsApiBaseUrl.endsWith("/")) {
        settingsApiBaseUrl = settingsApiBaseUrl.substring(0, settingsApiBaseUrl.length() - 1);
      }
      url = settingsApiBaseUrl + urlSuffix;
      bearerToken = retrieveBearerTokenForGoogleSettingsConfigId(debugMap, configId);
    } else {
      String directoryApiBaseUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".directoryApiBaseUrl", "https://admin.googleapis.com/admin/directory/v1");
      if (directoryApiBaseUrl.endsWith("/")) {
        directoryApiBaseUrl = directoryApiBaseUrl.substring(0, directoryApiBaseUrl.length() - 1);
      }
      url = directoryApiBaseUrl + urlSuffix;
      bearerToken = retrieveBearerTokenForGoogleConfigId(debugMap, configId);
    }
    
    String proxyUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".proxyUrl");
    String proxyType = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".proxyType");
    
    grouperHttpCall.assignProxyUrl(proxyUrl);
    grouperHttpCall.assignProxyType(proxyType);
    
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

    try {
      code = grouperHttpCall.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpCall.getResponseBody();
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
      JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
      return rootNode;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }

  }

  /**
   * create a group
   * @param grouperGoogleGroup
   * @return the result
   */
  public static GrouperGoogleGroup createGoogleGroup(String configId,
      GrouperGoogleGroup grouperGoogleGroup, Set<String> fieldsToInsert) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createGoogleGroup");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperGoogleGroup.toJsonGroupOnly(fieldsToInsert);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
      
      //String url = "https://admin.googleapis.com/admin/directory/v1/groups";
      
//      grouper.googleConnector.myGoogle.directoryApiBaseUrl
      
      

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/groups", GrouperUtil.toSet(200), 
          new int[] { -1 }, jsonStringToSend, false);

      GrouperGoogleGroup grouperGoogleGroupResult = GrouperGoogleGroup.fromJson(jsonNode);
      
      //now save group settings
      //url = "https://www.googleapis.com/groups/v1/groups/"+grouperGoogleGroupResult.getEmail();
      
      jsonToSend = grouperGoogleGroup.toJsonGroupSettings(fieldsToInsert);
      
      if (jsonToSend.size() > 0) {
        jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
        JsonNode groupSettingsNode = executeMethod(debugMap, "PATCH", configId, "/"+grouperGoogleGroupResult.getEmail(), GrouperUtil.toSet(200), new int[] { -1 },
            jsonStringToSend, true);
        grouperGoogleGroupResult.populateGroupSettings(groupSettingsNode);
      }
      
      Set<String> managers = GrouperUtil.nonNull(grouperGoogleGroup.getManagers());
      Set<String> owners = GrouperUtil.nonNull(grouperGoogleGroup.getOwners());
      
      managers.removeAll(owners);
      
      for (String manager: managers) {
        createGoogleRoleMembership(configId, grouperGoogleGroupResult.getId(), manager, "MANAGER");
      }
      
      if (grouperGoogleGroup.getManagers() != null) {        
        grouperGoogleGroupResult.setManagers(new HashSet<>(grouperGoogleGroup.getManagers()));
      }
      
      for (String owner: owners) {
        createGoogleRoleMembership(configId, grouperGoogleGroupResult.getId(), owner, "OWNER");
      }
      
      if (grouperGoogleGroup.getOwners() != null) {        
        grouperGoogleGroupResult.setOwners(new HashSet<>(grouperGoogleGroup.getOwners()));
      }

      return grouperGoogleGroupResult;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
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
   * @param grouperGoogleUser
   * @return
   */
  public static GrouperGoogleUser createGoogleUser(String configId, GrouperGoogleUser grouperGoogleUser) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createGoogleUser");

    long startTime = System.nanoTime();

    try {
      
      String password = generateRandomPassword();
      grouperGoogleUser.setPassword(password);
      
      JsonNode jsonToSend = grouperGoogleUser.toJson(null);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

//      String url = "https://admin.googleapis.com/admin/directory/v1/users";
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/users",
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, false);
      
      GrouperGoogleUser grouperGoogleUserResult = GrouperGoogleUser.fromJson(jsonNode);

      return grouperGoogleUserResult;
      
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
  public static void createGoogleMembership(String configId,
      String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createGoogleMembership");

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();
      objectNode.put("id", userId);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);

      //String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members";
      String urlSuffix = "/groups/"+groupId+"/members";

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200, 409), 
          new int[] { -1 }, jsonStringToSend, false);
      
      if (jsonNode == null) {
        throw new RuntimeException("error creating google membership for groupId "+groupId+" userId "+userId);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }
  
  /**
   * create a membership
   * @param grouperGoogleGroup
   * @return the result
   */
  public static void createGoogleRoleMembership(String configId,
      String groupId, String userId, String role) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createGoogleMembership");
    
    GrouperUtil.assertion(StringUtils.equals(role, "OWNER") || StringUtils.equals(role, "MANAGER"), "invalid role value: "+role);

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();
      objectNode.put("id", userId);
      objectNode.put("role", role);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);

      //String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members";
      String urlSuffix = "/groups/"+groupId+"/members";

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200, 409), 
          returnCode, jsonStringToSend, false);
      
      if (jsonNode == null) {
        throw new RuntimeException("error creating google membership for groupId "+groupId+" id "+userId + " role "+role);
      }
      
      if (returnCode[0] == 409) { // 409 means user already exists in the group.
        String existingRole = retrieveGoogleMembershipRole(configId, groupId, userId);
        if ("MEMBER".equals(existingRole)) {
          // we need to update to MANAGER or OWNER
          updateGoogleRoleMembership(configId, groupId, userId, role);
        } else if ("MANAGER".equals(existingRole) && "OWNER".equals(role)) {
          // we need to update to OWNER
          updateGoogleRoleMembership(configId, groupId, userId, role);
        }
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }
  
  /**
   * create a membership
   * @param grouperGoogleGroup
   * @return the result
   */
  public static void updateGoogleRoleMembership(String configId,
      String groupId, String userId, String role) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateGoogleRoleMembership");
    
    GrouperUtil.assertion(StringUtils.equals(role, "OWNER") || StringUtils.equals(role, "MANAGER") || StringUtils.equals(role, "MEMBER"), "invalid role value: "+role);

    long startTime = System.nanoTime();

    try {

      ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();
      objectNode.put("id", userId);
      objectNode.put("role", role);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);

      //String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members/memberId";
      String urlSuffix = "/groups/"+groupId+"/members/"+userId;

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, urlSuffix, GrouperUtil.toSet(200), 
          returnCode, jsonStringToSend, false);
      
      if (jsonNode == null) {
        throw new RuntimeException("error updating google membership for groupId "+groupId+" id "+userId + " role "+role);
      }
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }

  public static void deleteGoogleUser(String configId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteGoogleUser");
    
    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
      
//      String url = "https://admin.googleapis.com/admin/directory/v1/users/"+userId;
    
      executeMethod(debugMap, "DELETE", configId, "/users/"+userId,
          GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * update a group except the managers and owners of the group
   * @param grouperGoogleGroup
   * @return the result
   */
  public static GrouperGoogleGroup updateGoogleGroup(String configId,
      GrouperGoogleGroup grouperGoogleGroup, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateGoogleGroup");

    long startTime = System.nanoTime();

    try {

      String id = grouperGoogleGroup.getId();
      
      JsonNode jsonToSend = grouperGoogleGroup.toJsonGroupOnly(fieldsToUpdate);
      
      GrouperGoogleGroup updatedGoogleGroup = null;
      
      if (jsonToSend.size() > 0) {
//        String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+id;
        String urlSuffix = "/groups/"+id;
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

        JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, false);

        updatedGoogleGroup = GrouperGoogleGroup.fromJson(jsonNode);
      }
      
      jsonToSend = grouperGoogleGroup.toJsonGroupSettings(fieldsToUpdate);
      if (jsonToSend.size() > 0) {
        
        if (updatedGoogleGroup == null) {
          // we need to get the group email because settings API only works with group email
          updatedGoogleGroup = retrieveGoogleGroup(configId, id, false, false);
        }
        // update group settings
//        String settingsUrl = "https://www.googleapis.com/groups/v1/groups/"+updatedGoogleGroup.getEmail();
        String settingsUrlSuffix = "/"+updatedGoogleGroup.getEmail();
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
        JsonNode groupSettingsNode = executeMethod(debugMap, "PATCH", configId, settingsUrlSuffix, GrouperUtil.toSet(200), new int[] { -1 }, 
            jsonStringToSend, true);
        updatedGoogleGroup.populateGroupSettings(groupSettingsNode);
      }

      return updatedGoogleGroup;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }
  
  /**
   * update a user
   * @param grouperGoogleUser
   * @return the result
   */
  public static GrouperGoogleUser updateGoogleUser(String configId,
      GrouperGoogleUser grouperGoogleUser, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateGoogleUser");

    long startTime = System.nanoTime();

    try {

      String id = grouperGoogleUser.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperGoogleUser);
      }

      if (fieldsToUpdate.contains("id")) {
        throw new RuntimeException("Cant update the id field: " + grouperGoogleUser + ", " + GrouperUtil.setToString(fieldsToUpdate));
      }
      
//      String url = "https://admin.googleapis.com/admin/directory/v1/users/"+id;
      
      JsonNode jsonToSend = grouperGoogleUser.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, "/users/"+id,
          GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, false);

      GrouperGoogleUser grouperGoogleUserResult = GrouperGoogleUser.fromJson(jsonNode);

      return grouperGoogleUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }

  public static void deleteGoogleGroup(String configId,String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteGoogleGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("id is null");
      }
    
//      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId;
      
      executeMethod(debugMap, "DELETE", configId, "/groups/"+groupId,
          GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }
  }


  public static List<GrouperGoogleGroup> retrieveGoogleGroups(String configId, String fieldToSearchFor,
      String fieldValue, boolean lookupManagers, boolean lookupOwners) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleGroups");

    long startTime = System.nanoTime();

    try {

      List<GrouperGoogleGroup> results = new ArrayList<GrouperGoogleGroup>();
      
      String domain = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".groupDomain");
      
      if (StringUtils.isBlank(domain)) {
    	  domain = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.googleConnector." + configId + ".domain");
      }
      

      String nextPageToken = null;
      boolean firstRequest = true;
      
      
      int pageSize = GrouperConfig.retrieveConfig().propertyValueInt("grouper.googleConnector." + configId + ".pageSizeGroup", 200);
      
//      String url = "https://admin.googleapis.com/admin/directory/v1/groups?domain="+domain+"&maxResults=200&fields=nextPageToken,groups(id,email,name,description)";
      String urlSuffixConstant = "/groups?domain="+domain+"&maxResults="+pageSize+"&fields=nextPageToken,groups(id,email,name,description)";
      if (StringUtils.isNotBlank(fieldToSearchFor) && StringUtils.isNotBlank(fieldValue)) {
        String urlEncoded = GrouperUtil.escapeUrlEncode(fieldToSearchFor+"='"+fieldValue+"'");
        urlSuffixConstant = urlSuffixConstant +  "&query="+urlEncoded;
      }
      
      int maxCalls = Math.max(10000000/pageSize, 1);
      int numberOfCalls = 0;
      String previousPageToken = null;
      while (StringUtils.isNotBlank(nextPageToken) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + results.size()
             + ", itemsPerPage: " + pageSize + ", numberOfCalls: " + numberOfCalls);
        }
        
        firstRequest = false;
        
        String urlSuffix = urlSuffixConstant;
        if (StringUtils.isNotBlank(nextPageToken)) {
          urlSuffix = urlSuffix + "&pageToken="+nextPageToken;
        }
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, false);
        numberOfCalls++;
        
        ArrayNode groupsArray = (ArrayNode) jsonNode.get("groups");
        
        JsonNode nextPageTokenNode = jsonNode.get("nextPageToken");
        if (nextPageTokenNode != null && nextPageTokenNode.asText() != null) {
          nextPageToken = nextPageTokenNode.asText();
        }
        
        if (groupsArray == null || groupsArray.size() == 0) {
          break;
        }

        for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
          JsonNode groupNode = groupsArray.get(i);
          GrouperGoogleGroup grouperGoogleGroup = GrouperGoogleGroup.fromJson(groupNode);
          
          // for each group retrieve settings
          //String settingsUrl = "https://www.googleapis.com/groups/v1/groups/"+grouperGoogleGroup.getEmail()+"?alt=json";
          String settingsUrlSuffix = "/"+grouperGoogleGroup.getEmail()+"?alt=json";
          JsonNode groupSettingsNode = executeGetMethod(debugMap, configId, settingsUrlSuffix, true);
          grouperGoogleGroup.populateGroupSettings(groupSettingsNode);
          
          // retrieve roles of the members of the group (manager, owner)
          // https://admin.googleapis.com/admin/directory/v1/groups/{groupKey}/members
          if (lookupManagers) {
           Set<String> managerEmails = retrieveGoogleGroupMembersIds(configId, grouperGoogleGroup.getId(), "MANAGER");
           grouperGoogleGroup.setManagers(managerEmails);
          }
          if (lookupOwners) {
            Set<String> ownerEmails = retrieveGoogleGroupMembersIds(configId, grouperGoogleGroup.getId(), "OWNER");
            grouperGoogleGroup.setOwners(ownerEmails);
           }
          
          results.add(grouperGoogleGroup);
        }
        
        if (StringUtils.isNotBlank(previousPageToken) && StringUtils.isNotBlank(nextPageToken) && StringUtils.equals(previousPageToken, nextPageToken)) {
          break;
        }
        previousPageToken = nextPageToken;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }

  public static List<GrouperGoogleUser> retrieveGoogleUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleUsers");

    long startTime = System.nanoTime();

    try {

      List<GrouperGoogleUser> results = new ArrayList<GrouperGoogleUser>();
      
      String domain = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.googleConnector." + configId + ".domain");

      String nextPageToken = null;
      boolean firstRequest = true;
      
      int pageSize = GrouperConfig.retrieveConfig().propertyValueInt("grouper.googleConnector." + configId + ".pageSizeUser", 500);

//      String url = "https://admin.googleapis.com/admin/directory/v1/users?domain="+domain+"&maxResults=500&fields=nextPageToken,users(id,primaryEmail,name)";
      
      String urlSuffixConstant = "/users?domain="+domain+"&maxResults="+pageSize+"&fields=nextPageToken,users(id,primaryEmail,name,orgUnitPath)";
      
      int maxCalls = Math.max(10000000/pageSize, 1);
      int numberOfCalls = 0;
      String previousPageToken = null;
      while (StringUtils.isNotBlank(nextPageToken) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + results.size()
             + ", itemsPerPage: " + pageSize + ", numberOfCalls: " + numberOfCalls);
        }

        firstRequest = false;
        String urlSuffix = urlSuffixConstant;
        if (StringUtils.isNotBlank(nextPageToken)) {
          urlSuffix = urlSuffix + "&pageToken="+nextPageToken;
        }
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, false);
        
        ArrayNode usersArray = (ArrayNode) jsonNode.get("users");
        
        if (usersArray == null || usersArray.size() == 0) {
          break;
        }

        JsonNode nextPageTokenNode = jsonNode.get("nextPageToken");
        if (nextPageTokenNode != null && nextPageTokenNode.asText() != null) {
          nextPageToken = nextPageTokenNode.asText();
        } else {
          nextPageToken = null;
        }

        for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
          JsonNode userNode = usersArray.get(i);
          GrouperGoogleUser grouperGoogleUser = GrouperGoogleUser.fromJson(userNode);
          results.add(grouperGoogleUser);
        }
        
        if (StringUtils.isNotBlank(previousPageToken) && StringUtils.isNotBlank(nextPageToken) && StringUtils.equals(previousPageToken, nextPageToken)) {
          break;
        }
        previousPageToken = nextPageToken;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }

  /**
   * @param configId
   * @param id of the user
   * @return google user
   */
  public static GrouperGoogleUser retrieveGoogleUser(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleUser");

    long startTime = System.nanoTime();

    try {

//      String url = "https://admin.googleapis.com/admin/directory/v1/users/"+id+"?fields=id,primaryEmail,name";
      String urlSuffix = "/users/"+id+"?fields=id,primaryEmail,name,orgUnitPath";
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, false);
      
      /**
        {
          "id": "117982484919189471202",
          "primaryEmail": "liz@viveksachdeva.com",
          "name": {
              "givenName": "Elizabeth",
              "familyName": "Smith",
              "fullName": "Elizabeth Smith"
          }
        }
       */

      if (jsonNode == null) {
        return null;
      }
      
      GrouperGoogleUser grouperGoogleUser = GrouperGoogleUser.fromJson(jsonNode);
      return grouperGoogleUser;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param id of the user
   * @return google user
   */
  public static String retrieveGoogleMembershipRole(String configId, String groupId, String memberId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleUser");

    long startTime = System.nanoTime();

    try {

      // GET https://admin.googleapis.com/admin/directory/v1/groups/groupKey/members/memberKey
      String urlSuffix = "/groups/"+groupId+"/members/"+memberId;
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, false);
      
      /**
        {
         "kind": "directory#member",
         "id": "group member's unique ID",
         "email": "liz@example.com",
         "role": "MANAGER",
         "type": "GROUP"
        }
       */

      if (jsonNode == null) {
        return null;
      }
      
      return GrouperUtil.jsonJacksonGetString(jsonNode, "role");
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }

  /**
   * return user ids in the group
   * @param configId
   * @param groupId
   * @return user ids
   */
  public static Set<String> retrieveGoogleGroupMembers(String configId, String groupId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleGroupMembers");

    long startTime = System.nanoTime();

    try {

      Set<String> memberIds = new HashSet<String>();
      
      String nextPageToken = null;
      boolean firstRequest = true;
      
      int pageSize = GrouperConfig.retrieveConfig().propertyValueInt("grouper.googleConnector." + configId + ".pageSizeMembership", 500);

//      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members?maxResults=200&fields=nextPageToken,members(id)";
      //String urlSuffixConstant = "/groups/"+groupId+"/members?maxResults="+pageSize+"&roles=MEMBER&fields=nextPageToken,members(id)";
      String urlSuffixConstant = "/groups/"+groupId+"/members?maxResults="+pageSize+"&fields=nextPageToken,members(id)";

      
      int maxCalls = Math.max(10000000/pageSize, 1);
      int numberOfCalls = 0;
      String previousPageToken = null;
      while (StringUtils.isNotBlank(nextPageToken) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + memberIds.size()
             + ", itemsPerPage: " + pageSize + ", numberOfCalls: " + numberOfCalls);
        }
        
        firstRequest = false;
        String urlSuffix = urlSuffixConstant;
        if (StringUtils.isNotBlank(nextPageToken)) {
          urlSuffix = urlSuffix + "&pageToken="+nextPageToken;
        }
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, false);
        
        ArrayNode membersArray = (ArrayNode) jsonNode.get("members");
        
        if (membersArray == null || membersArray.size() == 0) {
          break;
        }

        JsonNode nextPageTokenNode = jsonNode.get("nextPageToken");
        if (nextPageTokenNode != null && nextPageTokenNode.asText() != null) {
          nextPageToken = nextPageTokenNode.asText();
        }

        for (int i = 0; i < (membersArray == null ? 0 : membersArray.size()); i++) {
          JsonNode memberNode = membersArray.get(i);
          
          String memberId = GrouperUtil.jsonJacksonGetString(memberNode, "id");
          
          memberIds.add(memberId);
        }
        
        if (StringUtils.isNotBlank(previousPageToken) && StringUtils.isNotBlank(nextPageToken) && StringUtils.equals(previousPageToken, nextPageToken)) {
          break;
        }
        previousPageToken = nextPageToken;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(memberIds));

      return memberIds;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }
  }
  
  
  /**
   * return user ids in the group
   * @param configId
   * @param groupId
   * @return user ids
   */
  public static Set<String> retrieveGoogleGroupMembersIds(String configId, String groupId, String role)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleGroupMembers");
    
    GrouperUtil.assertion(StringUtils.equals(role, "OWNER") || StringUtils.equals(role, "MANAGER"), "invalid role value: "+role);

    long startTime = System.nanoTime();

    try {

      Set<String> memberIds = new HashSet<String>();
      
      String nextPageToken = null;
      boolean firstRequest = true;
      
      int pageSize = GrouperConfig.retrieveConfig().propertyValueInt("grouper.googleConnector." + configId + ".pageSizeMembership", 500);

//      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members?maxResults=200&fields=nextPageToken,members(id)";
      String urlSuffixConstant = "/groups/"+groupId+"/members?maxResults="+pageSize+"&roles="+role+"&fields=nextPageToken,members(id)";
      
      
      int maxCalls = Math.max(10000000/pageSize, 1);
      int numberOfCalls = 0;
      String previousPageToken = null;
      while (StringUtils.isNotBlank(nextPageToken) || firstRequest) {
        
        if (maxCalls-- < 0) {
          throw new RuntimeException("Endless loop detected! total results so far: " + memberIds.size()
             + ", itemsPerPage: " + pageSize + ", numberOfCalls: " + numberOfCalls);
        }
        
        firstRequest = false;
        String urlSuffix = urlSuffixConstant;
        if (StringUtils.isNotBlank(nextPageToken)) {
          urlSuffix = urlSuffix + "&pageToken="+nextPageToken;
        }
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, false);
        
        ArrayNode membersArray = (ArrayNode) jsonNode.get("members");
        
        if (membersArray == null || membersArray.size() == 0) {
          break;
        }

        JsonNode nextPageTokenNode = jsonNode.get("nextPageToken");
        if (nextPageTokenNode != null && nextPageTokenNode.asText() != null) {
          nextPageToken = nextPageTokenNode.asText();
        }

        for (int i = 0; i < (membersArray == null ? 0 : membersArray.size()); i++) {
          JsonNode memberNode = membersArray.get(i);
          
          String memberId = GrouperUtil.jsonJacksonGetString(memberNode, "id");
          
          memberIds.add(memberId);
        }
        
        if (StringUtils.isNotBlank(previousPageToken) && StringUtils.isNotBlank(nextPageToken) && StringUtils.equals(previousPageToken, nextPageToken)) {
          break;
        }
        previousPageToken = nextPageToken;
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(memberIds));

      return memberIds;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }
    
    
  }

  /**
   * @param configId
   * @param id is the group id
   * @return the google group
   */
  public static GrouperGoogleGroup retrieveGoogleGroup(String configId, 
      String id, boolean lookupManagers, boolean lookupOwners) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleGroup");

    long startTime = System.nanoTime();

    try {

//      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+id+"?fields=id,email,name,description";
      
      String urlSuffix = "/groups/"+id+"?fields=id,email,name,description";
      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix, false);
      
      /**
       {
          "id": "02fk6b3p14s9iie",
          "email": "test-group@viveksachdeva.com",
          "name": "test-group",
          "description": "test group for grouper"
        }
       */

      if (jsonNode == null) {
        return null;
      }
      
      GrouperGoogleGroup grouperGoogleGroup = GrouperGoogleGroup.fromJson(jsonNode);
      
      // retrieve settings now
      // url = "https://www.googleapis.com/groups/v1/groups/"+grouperGoogleGroup.getEmail()+"?alt=json";
      urlSuffix = "/"+grouperGoogleGroup.getEmail()+"?alt=json";
      JsonNode groupSettingsNode = executeGetMethod(debugMap, configId, urlSuffix, true);
      grouperGoogleGroup.populateGroupSettings(groupSettingsNode);
      
      // retrieve roles of the members of the group (manager, owner)
      // https://admin.googleapis.com/admin/directory/v1/groups/{groupKey}/members
      if (lookupManagers) {
       Set<String> managerEmails = retrieveGoogleGroupMembersIds(configId, id, "MANAGER");
       grouperGoogleGroup.setManagers(managerEmails);
      }
      if (lookupOwners) {
        Set<String> ownerEmails = retrieveGoogleGroupMembersIds(configId, id, "OWNER");
        grouperGoogleGroup.setOwners(ownerEmails);
      }
      
      return grouperGoogleGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }

  }



  /**
   * delete membership
   * @param grouperGoogleGroup
   * @return the result
   */
  public static void deleteGoogleMembership(String configId, String groupId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteGoogleMembership");

    long startTime = System.nanoTime();

    try {
      
      String existingRole = retrieveGoogleMembershipRole(configId, groupId, userId);
      if (StringUtils.isNotBlank(existingRole) && (existingRole.equals("MANAGER") || existingRole.equals("OWNER"))) {
       //ignore it because user is already manager or owner
      } else {
      // String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members/"+userId;
      executeMethod(debugMap, "DELETE", configId, "/groups/"+groupId+"/members/"+userId,
          GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null, false);
      }
  
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }
  }
  
  /**
   * delete membership role
   * @param configId
   * @param groupId
   * @param userId
   * @param role
   * @return
   */
  public static void deleteGoogleRoleMembership(String configId, String groupId, String userId, String role, boolean addBackManager, boolean addBackMember) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteGoogleMembership");

    long startTime = System.nanoTime();
    
    try {
      String existingRole = retrieveGoogleMembershipRole(configId, groupId, userId);
      if (StringUtils.isBlank(existingRole)) {
        
        if (addBackManager) {
          createGoogleRoleMembership(configId, groupId, userId, "MANAGER");
        }
        if (!addBackManager && addBackMember && StringUtils.isNotBlank(userId)) {
          createGoogleMembership(configId, groupId, userId);
        }
        
      } else {
        if (role.equals("MANAGER") && addBackManager) {
          return;
        } 
        
        if (addBackManager) {
          updateGoogleRoleMembership(configId, groupId, userId, "MANAGER");
          return;
        }
        
        if (!addBackManager && addBackMember) {
          updateGoogleRoleMembership(configId, groupId, userId, "MEMBER");
          return;
        } 
        
        executeMethod(debugMap, "DELETE", configId, "/groups/"+groupId+"/members/"+userId,
            GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null, false);

      }
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }
  
  }

}
