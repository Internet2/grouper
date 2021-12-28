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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.morphString.Morph;

public class GrouperGoogleApiCommands {
  
  /**
   * cache of config key to expires on and encrypted bearer token
   */
  private static ExpirableCache<String, MultiKey> configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, MultiKey>(60);
  private static ExpirableCache<String, MultiKey> configKeyToExpiresOnAndSettingsToken = new ExpirableCache<String, MultiKey>(60);

  
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
   * get bearer token for google config id
   * @param configId
   * @return the bearer token
   */
  public static String retrieveBearerTokenForGoogleConfigId(Map<String, Object> debugMap, String configId) {
    
    long startedNanos = System.nanoTime();
        
    MultiKey expiresOnAndEncryptedBearerToken = configKeyToExpiresOnAndBearerToken.get(configId);
  
    String encryptedBearerToken = null;
    if (expiresOnAndEncryptedBearerToken != null) {
      long expiresOnSeconds = (Long)expiresOnAndEncryptedBearerToken.getKey(0);
      encryptedBearerToken = (String)expiresOnAndEncryptedBearerToken.getKey(1);
      if (expiresOnSeconds * 1000 > System.currentTimeMillis()) {
        // use it
        if (debugMap != null) {
          debugMap.put("googleCachedAccessToken", true);
        }
        return Morph.decrypt(encryptedBearerToken);
      }
    }
    try {
      // we need to get another one
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      
      final String url = "https://oauth2.googleapis.com/token";
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      grouperHttpClient.assignUrl(url);
      
      String signedJwt = null;

      String privateKeyFilePath = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountPKCS12FilePath");

      String privateKeyString = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountPKCS12Pass");
      
      String serviceAccountEmail = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountEmail");
      
      String serviceImpersonationUser = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceImpersonationUser");
      
      PrivateKey privateKey = null;
      
      if (StringUtils.isNotBlank(privateKeyFilePath)) {
        try {
          KeyStore keyStore = KeyStore.getInstance("PKCS12");
          keyStore.load(new FileInputStream(privateKeyFilePath), "notasecret".toCharArray());
          privateKey = (PrivateKey) keyStore.getKey("privatekey", "notasecret".toCharArray());
        } catch (Exception e) {
          throw new RuntimeException("Could not construct private key from p12 file");
        }
      } else if (StringUtils.isNotBlank(privateKeyString)) {
        
        try {
          byte[] privateKeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyString);
          KeyFactory kf = KeyFactory.getInstance("RSA");
         
          PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
          
          privateKey = kf.generatePrivate(privateKeySpec);
          
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
          .withClaim("scope", "https://www.googleapis.com/auth/admin.directory.user https://www.googleapis.com/auth/admin.directory.group https://www.googleapis.com/auth/admin.directory.group.member")
          .withIssuedAt(new Date(now))
          .withExpiresAt(new Date(now + 3600 * 1000L))
          .sign(algorithm);
      
      grouperHttpClient.addBodyParameter("assertion", signedJwt);
      grouperHttpClient.addBodyParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
  
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
      long expiresOn = GrouperUtil.jsonJacksonGetLong(jsonObject, "expires_in", -1L);
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonObject, "access_token");
  
      expiresOnAndEncryptedBearerToken = new MultiKey(expiresOn, Morph.encrypt(accessToken));
      configKeyToExpiresOnAndBearerToken.put(configId, expiresOnAndEncryptedBearerToken);
      return accessToken;
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
   * get bearer token for google settings config id
   * @param configId
   * @return the bearer token
   */
  public static String retrieveBearerTokenForGoogleSettingsConfigId(Map<String, Object> debugMap, String configId) {
    
    long startedNanos = System.nanoTime();
        
    MultiKey expiresOnAndEncryptedBearerToken = configKeyToExpiresOnAndSettingsToken.get(configId);
  
    String encryptedBearerToken = null;
    if (expiresOnAndEncryptedBearerToken != null) {
      long expiresOnSeconds = (Long)expiresOnAndEncryptedBearerToken.getKey(0);
      encryptedBearerToken = (String)expiresOnAndEncryptedBearerToken.getKey(1);
      if (expiresOnSeconds * 1000 > System.currentTimeMillis()) {
        // use it
        if (debugMap != null) {
          debugMap.put("googleCachedAccessTokenForSettings", true);
        }
        return Morph.decrypt(encryptedBearerToken);
      }
    }
    try {
      // we need to get another one
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      
      final String url = "https://oauth2.googleapis.com/token";
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      grouperHttpClient.assignUrl(url);
      
      String signedJwt = null;

      String privateKeyFilePath = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountPKCS12FilePath");

      String privateKeyString = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountPKCS12Pass");
      
      String serviceAccountEmail = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceAccountEmail");
      
      String serviceImpersonationUser = GrouperConfig.retrieveConfig().propertyValueString("grouper.googleConnector." + configId + ".serviceImpersonationUser");
      
      PrivateKey privateKey = null;
      
      if (StringUtils.isNotBlank(privateKeyFilePath)) {
        try {
          KeyStore keyStore = KeyStore.getInstance("PKCS12");
          keyStore.load(new FileInputStream(privateKeyFilePath), "notasecret".toCharArray());
          privateKey = (PrivateKey) keyStore.getKey("privatekey", "notasecret".toCharArray());
        } catch (Exception e) {
          throw new RuntimeException("Could not construct private key from p12 file");
        }
      } else if (StringUtils.isNotBlank(privateKeyString)) {
        
        try {
          byte[] privateKeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyString);
          KeyFactory kf = KeyFactory.getInstance("RSA");
         
          PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
          
          privateKey = kf.generatePrivate(privateKeySpec);
          
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
          .withKeyId("privateKeySettingsId")
          .withIssuer(serviceAccountEmail)
          .withSubject(serviceImpersonationUser)
          .withAudience("https://oauth2.googleapis.com/token")
          .withClaim("scope", "https://www.googleapis.com/auth/apps.groups.settings")
          .withIssuedAt(new Date(now))
          .withExpiresAt(new Date(now + 3600 * 1000L))
          .sign(algorithm);
      
      grouperHttpClient.addBodyParameter("assertion", signedJwt);
      grouperHttpClient.addBodyParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
  
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
        throw new RuntimeException("Cant get access token for settings from '" + url + "' " + code + ", " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      long expiresOn = GrouperUtil.jsonJacksonGetLong(jsonObject, "expires_in", -1L);
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonObject, "access_token");
  
      expiresOnAndEncryptedBearerToken = new MultiKey(expiresOn, Morph.encrypt(accessToken));
      configKeyToExpiresOnAndSettingsToken.put(configId, expiresOnAndEncryptedBearerToken);
      return accessToken;
    } catch (RuntimeException re) {
      
      if (debugMap != null) {
        debugMap.put("googleSettingsTokenError", GrouperUtil.getFullStackTrace(re));
      }
      throw re;
  
    } finally {
      if (debugMap != null) {
        debugMap.put("googleSettingsTokenTookMillis", (System.nanoTime()-startedNanos)/1000000);
      }
    }
  }
  
  private static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId, String url, boolean useSettingsBearerToken) {

    return executeMethod(debugMap, "GET", configId, url,
        GrouperUtil.toSet(200, 404), new int[] { -1 }, null, useSettingsBearerToken);

  }

  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String url, Set<Integer> allowedReturnCodes, int[] returnCode, String body, boolean useSettingsBearerToken) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    String bearerToken = null;
    if (useSettingsBearerToken) {
      bearerToken = retrieveBearerTokenForGoogleSettingsConfigId(debugMap, configId);
    } else {
      bearerToken = retrieveBearerTokenForGoogleConfigId(debugMap, configId);
    }
    
    System.out.println(bearerToken);
    
    debugMap.put("url", url);

    grouperHttpCall.assignUrl(url);
    grouperHttpCall.assignGrouperHttpMethod(httpMethodName);
    
    grouperHttpCall.addHeader("Content-Type", "application/json");
    grouperHttpCall.addHeader("Authorization", "Bearer " + bearerToken);
    grouperHttpCall.assignBody(body);
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
      
      String url = "https://admin.googleapis.com/admin/directory/v1/groups";

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, url, GrouperUtil.toSet(200), 
          new int[] { -1 }, jsonStringToSend, false);

      GrouperGoogleGroup grouperGoogleGroupResult = GrouperGoogleGroup.fromJson(jsonNode);
      
      //now save group settings
      url = "https://www.googleapis.com/groups/v1/groups/"+grouperGoogleGroupResult.getEmail();
      
      jsonToSend = grouperGoogleGroup.toJsonGroupSettings(fieldsToInsert);
      
      if (jsonToSend.size() > 0) {
        jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
        JsonNode groupSettingsNode = executeMethod(debugMap, "PATCH", configId, url, GrouperUtil.toSet(200), new int[] { -1 },
            jsonStringToSend, true);
        grouperGoogleGroupResult.populateGroupSettings(groupSettingsNode);
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

      String url = "https://admin.googleapis.com/admin/directory/v1/users";
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, url,
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
   * @param grouperGoogleGroup
   * @return the result
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

      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members";

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, url, GrouperUtil.toSet(200), 
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
  public static void createGoogleMemberships(String configId,
      String groupId, Collection<String> userIds) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createGoogleMemberships");

    //  PATCH https://graph.microsoft.com/v1.0/groups/{group-id}
    //  Content-type: application/json
    //  Content-length: 30
    //
    //  {
    //    "members@odata.bind": [
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}",
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}",
    //      "https://graph.microsoft.com/v1.0/directoryObjects/{id}"
    //      ]
    //  }
    
    long startTime = System.nanoTime();

    try {

      int batchSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureMembershipPagingSize", 20);
      List<String> userIdsList = new ArrayList<String>(userIds);
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(userIdsList, batchSize);
      debugMap.put("numberOfBatches", numberOfBatches);
      for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
        debugMap.put("batchIndex", batchIndex);
        List<String> batchOfUserIds = GrouperUtil.batchList(userIdsList, batchSize, batchIndex);

        ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
        
        String resourceEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueString(
            "grouper.azureConnector."+configId+".resourceEndpoint");
        
        for (int i=0;i<GrouperUtil.length(batchOfUserIds);i++) {
          String userId = batchOfUserIds.get(i);
          arrayNode.add(GrouperUtil.stripLastSlashIfExists(resourceEndpoint) + "/directoryObjects/" + GrouperUtil.escapeUrlEncode(userId));
        }
        
        ObjectNode objectNode  = GrouperUtil.jsonJacksonNode();

        objectNode.set("members@odata.bind", arrayNode);
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(objectNode);
        try {
          executeMethod(debugMap, "PATCH", configId, "/groups/" + GrouperUtil.escapeUrlEncode(groupId),
              GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, false);
        } catch (Exception e) {

          debugMap.put("innerException", GrouperClientUtils.getFullStackTrace(e));

          // if this fails, try individually
          for (int i=0;i<GrouperUtil.length(batchOfUserIds);i++) {
            String userId = batchOfUserIds.get(i);
            createGoogleMembership(configId, groupId, userId);
          }
        }

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
      
      String url = "https://admin.googleapis.com/admin/directory/v1/users/"+userId;
    
      executeMethod(debugMap, "DELETE", configId, url,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * update a group
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
        String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+id;
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

        JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, url,
            GrouperUtil.toSet(200), new int[] { -1 }, jsonStringToSend, false);

        updatedGoogleGroup = GrouperGoogleGroup.fromJson(jsonNode);
      }
      
      jsonToSend = grouperGoogleGroup.toJsonGroupSettings(fieldsToUpdate);
      if (jsonToSend.size() > 0) {
        
        if (updatedGoogleGroup == null) {
          // we need to get the group email because settings API only works with group email
          updatedGoogleGroup = retrieveGoogleGroup(configId, id);
        }
        // update group settings
        String url = "https://www.googleapis.com/groups/v1/groups/"+updatedGoogleGroup.getEmail();
        
        String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
        JsonNode groupSettingsNode = executeMethod(debugMap, "PATCH", configId, url, GrouperUtil.toSet(200), new int[] { -1 }, 
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
      
      String url = "https://admin.googleapis.com/admin/directory/v1/users/"+id;
      
      JsonNode jsonToSend = grouperGoogleUser.toJson(fieldsToUpdate);
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, url,
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
    
      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId;
      
      executeMethod(debugMap, "DELETE", configId, url,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }
  }


  public static List<GrouperGoogleGroup> retrieveGoogleGroups(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleGroups");

    long startTime = System.nanoTime();

    try {

      List<GrouperGoogleGroup> results = new ArrayList<GrouperGoogleGroup>();
      
      String domain = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.googleConnector." + configId + ".domain");

      String nextPageToken = null;
      boolean firstRequest = true;

      String url = "https://admin.googleapis.com/admin/directory/v1/groups?domain="+domain+"&maxResults=200&fields=nextPageToken,groups(id,email,name,description)";
      
      while (StringUtils.isNotBlank(nextPageToken) || firstRequest) {
        
        firstRequest = false;
        
        
        if (StringUtils.isNotBlank(nextPageToken)) {
          url = url + "&pageToken="+nextPageToken;
        }
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, url, false);
        
        ArrayNode groupsArray = (ArrayNode) jsonNode.get("groups");
        
        JsonNode nextPageTokenNode = jsonNode.get("nextPageToken");
        if (nextPageTokenNode != null && nextPageTokenNode.asText() != null) {
          nextPageToken = nextPageTokenNode.asText();
        }

        for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
          JsonNode groupNode = groupsArray.get(i);
          GrouperGoogleGroup grouperGoogleGroup = GrouperGoogleGroup.fromJson(groupNode);
          
          // for each group retrieve settings
          url = "https://www.googleapis.com/groups/v1/groups/"+grouperGoogleGroup.getEmail()+"?alt=json";
          JsonNode groupSettingsNode = executeGetMethod(debugMap, configId, url, true);
          grouperGoogleGroup.populateGroupSettings(groupSettingsNode);
          
          results.add(grouperGoogleGroup);
        }
        
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

      String url = "https://admin.googleapis.com/admin/directory/v1/users?domain="+domain+"&maxResults=200&fields=nextPageToken,users(id,primaryEmail,name)";
      
      while (StringUtils.isNotBlank(nextPageToken) || firstRequest) {
        
        firstRequest = false;
        
        if (StringUtils.isNotBlank(nextPageToken)) {
          url = url + "&pageToken="+nextPageToken;
        }
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, url, false);
        
        ArrayNode usersArray = (ArrayNode) jsonNode.get("users");
        
        JsonNode nextPageTokenNode = jsonNode.get("nextPageToken");
        if (nextPageTokenNode != null && nextPageTokenNode.asText() != null) {
          nextPageToken = nextPageTokenNode.asText();
        }

        for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
          JsonNode userNode = usersArray.get(i);
          GrouperGoogleUser grouperGoogleUser = GrouperGoogleUser.fromJson(userNode);
          results.add(grouperGoogleUser);
        }
        
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

    debugMap.put("method", "retrieveGoogleGroup");

    long startTime = System.nanoTime();

    try {

      String url = "https://admin.googleapis.com/admin/directory/v1/users/"+id+"?fields=id,primaryEmail,name";
      
      JsonNode jsonNode = executeGetMethod(debugMap, configId, url, false);
      
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
   * return user ids in the group
   * @param configId
   * @param userId
   * @return group ids
   */
  public static Set<String> retrieveGoogleUserGroups(String configId, String userId)  {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleUserGroups");

    long startTime = System.nanoTime();
    
    Set<String> result = new LinkedHashSet<String>();

    try {

      String urlSuffix = "/users/" + GrouperUtil.escapeUrlEncode(userId) + "/getMemberGroups";

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, urlSuffix, GrouperUtil.toSet(200), new int[] {-1}, null, false);

      //lets get the group node

      ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
      if (value != null && value.size() > 0) {
        
        int azureGetUserGroupsMax = GrouperLoaderConfig.retrieveConfig().propertyValueInt("azureGetUserGroupsMax", 2046);
        if (value.size() == azureGetUserGroupsMax) {
          throw new RuntimeException("Too many groups! " + value.size());
        }
        
        for (int i=0;i<value.size();i++) {
          String groupId = value.get(i).asText();
          result.add(groupId);
        }
      }

      return result;
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

      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members?maxResults=200&fields=nextPageToken,members(id)";
      
      while (StringUtils.isNotBlank(nextPageToken) || firstRequest) {
        
        firstRequest = false;
        
        if (StringUtils.isNotBlank(nextPageToken)) {
          url = url + "&pageToken="+nextPageToken;
        }
        
        JsonNode jsonNode = executeGetMethod(debugMap, configId, url, false);
        
        ArrayNode membersArray = (ArrayNode) jsonNode.get("members");
        
        JsonNode nextPageTokenNode = jsonNode.get("nextPageToken");
        if (nextPageTokenNode != null && nextPageTokenNode.asText() != null) {
          nextPageToken = nextPageTokenNode.asText();
        }

        for (int i = 0; i < (membersArray == null ? 0 : membersArray.size()); i++) {
          JsonNode memberNode = membersArray.get(i);
          
          String memberId = GrouperUtil.jsonJacksonGetString(memberNode, "id");
          
          memberIds.add(memberId);
        }
        
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

  private static void retrieveGoogleGroupMembersHelper(Set<String> result, JsonNode jsonNode) {
    ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
    if (value != null && value.size() > 0) {
      for (int i=0;i<value.size();i++) {
        JsonNode membership = value.get(i);
        result.add(GrouperUtil.jsonJacksonGetString(membership, "id"));
      }
    }
  }

  /**
   * @param configId
   * @param id is the group id
   * @return the google group
   */
  public static GrouperGoogleGroup retrieveGoogleGroup(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGoogleGroup");

    long startTime = System.nanoTime();

    try {

      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+id+"?fields=id,email,name,description";
      
      JsonNode jsonNode = executeGetMethod(debugMap, configId, url, false);
      
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
      url = "https://www.googleapis.com/groups/v1/groups/"+grouperGoogleGroup.getEmail()+"?alt=json";
      JsonNode groupSettingsNode = executeGetMethod(debugMap, configId, url, true);
      grouperGoogleGroup.populateGroupSettings(groupSettingsNode);

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
  
      String url = "https://admin.googleapis.com/admin/directory/v1/groups/"+groupId+"/members/"+userId;
      executeMethod(debugMap, "DELETE", configId, url,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, false);
  
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperGoogleLog.googleLog(debugMap, startTime);
    }
  
  }

}
