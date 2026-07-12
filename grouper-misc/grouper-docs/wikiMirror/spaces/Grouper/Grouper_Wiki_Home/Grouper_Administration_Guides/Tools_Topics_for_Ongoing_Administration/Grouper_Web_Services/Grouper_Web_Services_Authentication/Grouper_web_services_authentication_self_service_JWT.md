---
title: "Grouper web services - authentication - self-service JWT"
space: Grouper
pageId: 28555067
version: 15
lastUpdated: 2026-07-01T05:39:02.881Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555067/Grouper+web+services+-+authentication+-+self-service+JWT
---

Out of the box, grouper-ws uses self-service JWT authentication in v2.6.4+). This is enabled by default

This authentication is built-in to Grouper and does not use tomcat or apache authentication.

If there is a JWT in the request it will use that. Otherwise it will use whatever other authentication mechanism. So this co-exists with other authn methods.

This is public / private key encryption, do not send the private key from client to server. The JWT needs to be generated new for each call

Note: if you have **grouper-ws.properties** property **ws.client.user.group.name** configured, then the local entity needs to be added to that configured group to be able to call WS.

Note: the iat must be in the jwt.

## Configure

grouper.properties

```
##################################
## ws self-service jwt
##################################

# if public private key should be enabled
# {valueType: "boolean", defaultValue: "true"}
grouper.selfService.jwt.enable =

# if you fill in a group name here, then only members of this group can manage jwt private keys on the ui
# {valueType: "string"}
grouper.selfService.jwt.groupNameAllowedToManage = 

# maximum number of seconds for which a jwt can stay valid
# {valueType: "integer"}
grouper.selfService.jwt.maxValidTimeInSeconds = 600

```

## Register

Anyone in the Grouper UI who can CREATE objects in a folder, can create a local entity. A local entity in Grouper is an object that is like a group with no members and with few privileges (e.g. you cant have UPDATE on something with no members). If someone creates a local entity (or is created privileges on it), then they have ADMIN of the local entity. Someone with ADMIN on a local entity can manage the WS JWT key.

## 

## Calling web services with self service JWT authentication

1. Save the private key and keep this safe. The algorithm used to create the private key is RSA-256 (**RS256**). It consists of PEM PKCS#1 data without the PEM header/footer. Depending on your JWT implementation method, appropriate header/footer may be required:
  
  
  ```
  -----BEGIN RSA PRIVATE KEY-----
  PRIVATE KEY DATA
  -----END RSA PRIVATE KEY-----
  
  ```
2. Generate a JWT, there are libraries in most programming languages, here is a java example
  
  
  
  
  ```
  package edu.internet2.middleware.grouper.authentication;
  
  import java.io.File;
  import java.security.KeyFactory;
  import java.security.NoSuchAlgorithmException;
  import java.security.PrivateKey;
  import java.security.interfaces.RSAPrivateKey;
  import java.security.interfaces.RSAPublicKey;
  import java.security.spec.InvalidKeySpecException;
  import java.security.spec.PKCS8EncodedKeySpec;
  import java.util.Date;
  
  import org.apache.commons.lang3.StringUtils;
  
  import com.auth0.jwt.JWT;
  import com.auth0.jwt.algorithms.Algorithm;
  import com.auth0.jwt.interfaces.RSAKeyProvider;
  
  import edu.internet2.middleware.grouper.util.GrouperUtil;
  
  public class GrouperPublicPrivateKeyJwtGenerateExample {
    
    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
      
      if (args.length != 1) {
        throw new RuntimeException("Pass in one argument: the filename of the private key");
      }
      
      File privateKeyFile = new File(args[0]);
      
      if (!privateKeyFile.exists()) {
        throw new RuntimeException("File doesnt exist: " + privateKeyFile.getAbsolutePath());
      }
      
      String privateKey = StringUtils.trim(GrouperUtil.readFileIntoString(privateKeyFile));
      
      MyTestRSAKeyProvider testRSAKeyProvider = new MyTestRSAKeyProvider(privateKey);
      Algorithm algorithm = Algorithm.RSA256(testRSAKeyProvider);
      
      String jwt = JWT.create()
          .withIssuedAt(new Date())
          .sign(algorithm);
      
      System.out.println(jwt);
    }
    
    public GrouperPublicPrivateKeyJwtGenerateExample() {
      super();
    }
  
    static class MyTestRSAKeyProvider implements RSAKeyProvider {
  
     public MyTestRSAKeyProvider(String thePrivateKey) {
       this.privateKeyString = thePrivateKey;
     }
     public String privateKeyString;
      
      @Override
      public RSAPublicKey getPublicKeyById(String keyId) {
        throw new RuntimeException("Who cares");
      }
  
      @Override
      public RSAPrivateKey getPrivateKey() {
        PrivateKey privateKey = null;
        try {
          byte[] privateKeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(privateKeyString);
          KeyFactory kf = KeyFactory.getInstance("RSA");
         
          PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
          
          privateKey = kf.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException(
              "Could not reconstruct the private key, the given algorithm could not be found.", e);
        } catch (InvalidKeySpecException e) {
          throw new RuntimeException("Could not reconstruct the private key", e);
        }
        
        if (privateKey instanceof RSAPrivateKey) {
          return (RSAPrivateKey)privateKey;
        }
        return null;
      }
  
      @Override
      public String getPrivateKeyId() {
        return privateKeyString;
      }
      
    }
  
  }
  
  
  ```
  
  
  
  An example... if this is the private key
  
  
  ```
  MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCE39LhWBeyKqwjcA61IDaPRoh6qhGn3TIOlU+JLBfpxRbvBodhaKcO7E+zxK3zYD2Tj2/fl31Iy9TRojIi7altlBfWUDLe9l9//DfkSKVgDpGeQ/LLHnZq1lpXoLZmYd5RwX1wz9yVpmh4P+EVxFtLgwYKBs4U71YQxAQDs6orVb44Vo8T03F4uB5z9Avf7WLRHKUDh/6klFq3mOA6hjoK+vrXeFDeRf7nuQcT9Dd62K6wwYJDO85xWXgC7gtsgJssY0rOD9Tnr6ZsBlT2/ihK9uldTqi+angl4X3XT77plW77MsBT1sSTlOtvPxHw1HtLoVWP3q719icapN5jGwS1AgMBAAECggEAFwWcyRR1TpZcKuuwcKt7KInk/bPXyUjm8mXmWPL91bAjUBjGfhce0tQY3yHVrdRf9uAkVw4tU96VKhk7LJgXAfhlxOkyVzVCyK8PlAsOND1x94T4XT8S72HiV0puMAEG/w1SSaiKJJenhCY1Pos98jBqbHRPYosxU0hTFb0cX9OC/GxWezLsJO5do8u7Q74Urh01750s8jEbdT6yqHpoDNwqkQWcwMgoe3t2oSqhKjwIEi0qb5ZgteC4vvA/8qMtrJK+y2jjopRL5H3WmPUKAovyoBXKAcoKH7SjNBr8udVgPwKjWovHcjyzjXKpUzyetR0ZzC4R+DZUMCVl1sCjAQKBgQDkHn+JA2hOijQSzJwnaageEmNHZECW5PsBzzRxgMikk8pLNxJmM+tg35FdgtTMHjPNSvtyI273LiD30TXNKzfNkuD/VqqaqoBhOzCbRtJI5HDyCa5VHrDfY0J4kd11d+fHOv0O+MpfkUjyvCuVikPZn6JQ7J3fS8fcvVqsy6KbQQKBgQCVHUJuTPz5IuYxtMteA8GM4pLEbpQ0M7SLbGnBXz4xIjw4M2NrUlb7SWEVEgesAHmhywS/SG5B320bZw/EFeHVHN64t3QJ5ESnrDU5eoWrsPfh/4vO+lU0h8LekSvWT2KIeNWeqV4CFJ9kqjBCNsVAzSoU0Zvj4aKXgM7IMJYQdQKBgQDf1nlIRVqtbnkoVTOIjOlVEK/wFpQ2PFt8bAhGs5qtuwMOCOz58kotlVJ7UmxiGeS0tbXDSreQvefFo7jKKHUqN8ylYDIpb9JnFgBc7QJcWiPlq9AvX90oZaqMynxmzpBU8zHq5f8WBWZyIGgX5UMsoLJR+8vxltd3Zyo5UdutwQKBgQCTURvvcxo+XM9FblO450cSFEphzMmYpTiKwindRGZiDmumLobbYbbTfhRux6hSswl34eYnwLxFUiIt/20hEWlSrboQjhYTK7T0Xnsa9UQYcrcDTP/oFYOWaUYJsyy3ByjWoWKS5MsejRdIUadp00ifk9IuTUORKLsEqNoiB3ZKfQKBgAbphiATabiDj/TSPVnMKD6GgzX/uRxVseHTGOnQRK8aRLm10JS1GkQkGSXMSdp0xyjH07BgmGtX0Znm8AxfLkqGtgdKPvyI7mOHhyuyAvBSr6lVeEYTooKf9WR6m8g+sjf9keqBlCdHngU1Uv74U7tkyJWgr+7aakHrSEtUPwew
  ```
  
  And the JWT is issued at: 2021/10/23 16:24:08.512
  
  Then the JWT is:
  
  
  ```
  eyJraWQiOiJNSUlFdmdJQkFEQU5CZ2txaGtpRzl3MEJBUUVGQUFTQ0JLZ3dnZ1NrQWdFQUFvSUJBUUNFMzlMaFdCZXlLcXdqY0E2MUlEYVBSb2g2cWhHbjNUSU9sVStKTEJmcHhSYnZCb2RoYUtjTzdFK3p4SzN6WUQyVGoyL2ZsMzFJeTlUUm9qSWk3YWx0bEJmV1VETGU5bDkvL0Rma1NLVmdEcEdlUS9MTEhuWnExbHBYb0xabVlkNVJ3WDF3ejl5VnBtaDRQK0VWeEZ0TGd3WUtCczRVNzFZUXhBUURzNm9yVmI0NFZvOFQwM0Y0dUI1ejlBdmY3V0xSSEtVRGgvNmtsRnEzbU9BNmhqb0srdnJYZUZEZVJmN251UWNUOURkNjJLNnd3WUpETzg1eFdYZ0M3Z3RzZ0pzc1kwck9EOVRucjZac0JsVDIvaWhLOXVsZFRxaSthbmdsNFgzWFQ3N3BsVzc3TXNCVDFzU1RsT3R2UHhIdzFIdExvVldQM3E3MTlpY2FwTjVqR3dTMUFnTUJBQUVDZ2dFQUZ3V2N5UlIxVHBaY0t1dXdjS3Q3S0luay9iUFh5VWptOG1YbVdQTDkxYkFqVUJqR2ZoY2UwdFFZM3lIVnJkUmY5dUFrVnc0dFU5NlZLaGs3TEpnWEFmaGx4T2t5VnpWQ3lLOFBsQXNPTkQxeDk0VDRYVDhTNzJIaVYwcHVNQUVHL3cxU1NhaUtKSmVuaENZMVBvczk4akJxYkhSUFlvc3hVMGhURmIwY1g5T0MvR3hXZXpMc0pPNWRvOHU3UTc0VXJoMDE3NTBzOGpFYmRUNnlxSHBvRE53cWtRV2N3TWdvZTN0Mm9TcWhLandJRWkwcWI1Wmd0ZUM0dnZBLzhxTXRySksreTJqam9wUkw1SDNXbVBVS0FvdnlvQlhLQWNvS0g3U2pOQnI4dWRWZ1B3S2pXb3ZIY2p5empYS3BVenlldFIwWnpDNFIrRFpVTUNWbDFzQ2pBUUtCZ1FEa0huK0pBMmhPaWpRU3pKd25hYWdlRW1OSFpFQ1c1UHNCenpSeGdNaWtrOHBMTnhKbU0rdGczNUZkZ3RUTUhqUE5TdnR5STI3M0xpRDMwVFhOS3pmTmt1RC9WcXFhcW9CaE96Q2JSdEpJNUhEeUNhNVZIckRmWTBKNGtkMTFkK2ZIT3YwTytNcGZrVWp5dkN1VmlrUFpuNkpRN0ozZlM4ZmN2VnFzeTZLYlFRS0JnUUNWSFVKdVRQejVJdVl4dE10ZUE4R000cExFYnBRME03U0xiR25CWHo0eElqdzRNMk5yVWxiN1NXRVZFZ2VzQUhtaHl3Uy9TRzVCMzIwYlp3L0VGZUhWSE42NHQzUUo1RVNuckRVNWVvV3JzUGZoLzR2TytsVTBoOExla1N2V1QyS0llTldlcVY0Q0ZKOWtxakJDTnNWQXpTb1UwWnZqNGFLWGdNN0lNSllRZFFLQmdRRGYxbmxJUlZxdGJua29WVE9Jak9sVkVLL3dGcFEyUEZ0OGJBaEdzNXF0dXdNT0NPejU4a290bFZKN1VteGlHZVMwdGJYRFNyZVF2ZWZGbzdqS0tIVXFOOHlsWURJcGI5Sm5GZ0JjN1FKY1dpUGxxOUF2WDkwb1phcU15bnhtenBCVTh6SHE1ZjhXQldaeUlHZ1g1VU1zb0xKUis4dnhsdGQzWnlvNVVkdXR3UUtCZ1FDVFVSdnZjeG8rWE05RmJsTzQ1MGNTRkVwaHpNbVlwVGlLd2luZFJHWmlEbXVtTG9iYlliYlRmaFJ1eDZoU3N3bDM0ZVlud0x4RlVpSXQvMjBoRVdsU3Jib1FqaFlUSzdUMFhuc2E5VVFZY3JjRFRQL29GWU9XYVVZSnN5eTNCeWpXb1dLUzVNc2VqUmRJVWFkcDAwaWZrOUl1VFVPUktMc0VxTm9pQjNaS2ZRS0JnQWJwaGlBVGFiaURqL1RTUFZuTUtENkdnelgvdVJ4VnNlSFRHT25RUks4YVJMbTEwSlMxR2tRa0dTWE1TZHAweHlqSDA3QmdtR3RYMFpubThBeGZMa3FHdGdkS1B2eUk3bU9IaHl1eUF2QlNyNmxWZUVZVG9vS2Y5V1I2bThnK3NqZjlrZXFCbENkSG5nVTFVdjc0VTd0a3lKV2dyKzdhYWtIclNFdFVQd2V3IiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJpYXQiOjE2MzUwMjA2NDh9.StfvFhq0XAfIBNFPAW587u8T56Id1BtdQh2zM0sDE2A9X-da8LZU3tuIxQZMTx5_abbI0cogXUuc6tJGYIVJVAM1qgH_ieu-OT1zkIMaG5Q5rb6biecuY2coqKl3Uu592D7cBcpBp0mBn2ZfxLATt08ztPVKnVIF6mN93Pq-vm59rdHtv83jE4pRZh7DJboO6XLJQpcxKtM9dtUtmsFEOCWtiCnYqjz1Nh8zU0Cl0v7-mzDIqaJ7ftYcoh31BmtLar7wXcDD5J0Lco3udl1qZU0guRHt4Bk04QVtKP1XJGbzRXBxXQgtYg_zfHKplac3Le34z17NKHuAkuRv67UHdg
  ```
  
  You can get that date like:
  
  
  ```
      String pattern = "yyyy/MM/dd HH:mm:ss.SSS";
      java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat(pattern);
      java.util.Date date = null;
      try {
        date = simpleDateFormat.parse("2021/10/23 16:24:08.512");
      } catch (java.text.ParseException pe) {
        throw new RuntimeException("error", pe);
      }
  
  --or--
      
      java.util.Date date2 = new java.util.Date(1635020648512L);
  
  Put that date in the issuedAt
  
  .withIssuedAt(new Date())
  ```
3. Now there is the prefix from the entity page, and the JWT, concatenate that together. Note, you can put an expire date in the JWT, and the Grouper server will only allow JWTs of a certain age (see config above). So make sure clocks are synchronized and that a JWT that is re-used is not expired. Also, you need to grant privileges to the local entity consistent with what access is needed. In this case the local entity was granted READ on the group - test:group1  
    
  This is the prefix of all requests for this entity (get from UI when you download the token). The "YWMyOTE.." is the base64 encoding of the local entity member ID (uuid in the grouper_members table). That part generally shouldnt matter, just get from UI.
  
  
  ```
  Bearer jwtUser_YWMyOTE5NGVkZTE4NDU1NzgxYmJhNjM0NjYwNTFlYzQ=_<jwt>
  ```
  
  
  
  
  ```
  GET http://localhost:8400/grouper-ws/servicesRest/json/v2_6_000/groups/test:group1/members
  Authorization: Bearer jwtUser_YWMyOTE5NGVkZTE4NDU1NzgxYmJhNjM0NjYwNTFlYzQ=_eyJraWQiOiJNSUlFdmdJQkFEQU5CZ2... (rest of JWT)
  
  Status Code: 200
  {
     "WsGetMembersLiteResult":{
        "resultMetadata":{
           "success":"T",
           "resultCode":"SUCCESS",
           "resultMessage":"Success for: clientVersion: 2.6.0, wsGroupLookups: Array size: 1: [0]: WsGroupLookup[pitGroups=[],groupName=test:group1]\n\n, memberFilter: All, includeSubjectDetail: false, actAsSubject: null, fieldName: null, subjectAttributeNames: null\n, paramNames: \n, params: null\n, sourceIds: null\n, pointInTimeFrom: null, pointInTimeTo: null, pageSize: null, pageNumber: null, sortString: null, ascending: null"
        },
        "wsGroup":{
           "extension":"group1",
           "displayName":"test:group1",
           "description":"description",
           "uuid":"c98d7dc1de134dc58a9da7fbf6811673",
           "enabled":"T",
           "displayExtension":"group1",
           "name":"test:group1",
           "typeOfGroup":"group",
           "idIndex":"10023"
        },
        "responseMetadata":{
           "serverVersion":"2.6.0",
           "millis":"348"
        },
        "wsSubjects":[
           {
              "sourceId":"jdbc",
              "success":"T",
              "resultCode":"SUCCESS",
              "id":"test.subject.0",
              "memberId":"a4f66cf87974454faeecdbdbf983e26d"
           }
        ]
     }
  }
  
  
  ```

## Email people whose credentials are about to expire

Add a notification daemon with this query to email people about their credential about to expire. This is postgres, could be adjusted for mysql or oracle

```
select gp.username, gm_user_to_email.subject_source as subject_to_email_source_id, gm_user_to_email.subject_id as subject_to_email_id, 
gm_user_to_email.email0 as email, to_timestamp(gp.expires_millis / 1000) as expires, gm_credential.subject_identifier0 credential_name
from grouper_password gp, grouper_members gm_user_to_email, grouper_members gm_credential
where gp.member_id_who_set_password  = gm_user_to_email.id
and gm_user_to_email.email0 is not null
and to_timestamp(gp.expires_millis  / 1000) > now()
and to_timestamp(gp.expires_millis  / 1000) < (now() + interval '14' day)
and gm_credential.id = gp.member_id ;
```
