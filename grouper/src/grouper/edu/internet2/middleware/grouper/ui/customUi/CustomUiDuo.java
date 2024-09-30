/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoApiCommands;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;


/**
 * 
 * <pre>grouper.properties
 * 
 * </pre>
 */
public class CustomUiDuo extends CustomUiUserQueryBase {

  /**
   * 
   * @param configId 
   * @param duoGroupName
   * @param subject
   * @return true if membership
   */
  public boolean hasDuoMembershipByDuoGroupName(String configId, String duoGroupName, Subject subject) {

    long startedNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(duoGroupName)) {
        throw new RuntimeException("duo group name is blank");
      }

      Map<String, Object> userAttributes = retrieveDuoUserOrFromCache(configId, subject);

      if (userAttributes == null) {
        return false;
      }

      List<Map> groups = GrouperUtil.nonNull((List)userAttributes.get("groups"));
      
      for (Map<String, Object> group : groups) {
        if (StringUtils.equals(duoGroupName, (String)group.get("name"))) {
          return true;
        }
      }
      
      return false;
    } catch (RuntimeException re) {
      
      this.debugMapPut("duoMshipError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("duoMshipTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }
    
  }

  /**
   * cche key is sourceId and subjectId
   * cache this for a minute so whenn one screen loads it does one operation
   */
  private static ExpirableCache<MultiKey, Map<String, Object>> duoUserCache = new ExpirableCache<MultiKey, Map<String, Object>>(1);
  
  /**
   * 
   * @param configId 
   * @param subject
   */
  public Map<String, Object> retrieveDuoUserOrFromCache(String configId, Subject subject) {
    
    if (subject == null) {
      throw new RuntimeException("subject is null");
    }

    MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
    Map<String, Object> duoUser = duoUserCache.get(multiKey);
    
    if (duoUser == null) {
      duoUser = retrieveDuoUser(configId, subject);
      if (duoUser == null) {
        duoUser = new HashMap<String, Object>();
      }
      duoUserCache.put(multiKey, duoUser);
    }
    
    //  {"alias1":"mchyzer-kadm","alias2":null,"alias3":null,"alias4":null,
    //  "aliases":{"alias1":"mchyzer-kadm"},
    //  "created":1695930463,"desktoptokens":[],"email":"","enable_auto_prompt":true,"firstname":"",
    //  "groups":[
    //  {"desc":"This is the early adopters group for DUO changes.","group_id":"abc123","mobile_otp_enabled":false,"name":"EarlyAdopters","push_enabled":false,"sms_enabled":false,"status":"Active","voice_enabled":false},
    //  {"desc":"Member's of the ISC IAM Program. This group will typically used for early adoption/new features in DUO for testing and piloting purposes.","group_id":"abc123","mobile_otp_enabled":false,"name":"ISC IAM Program","push_enabled":false,"sms_enabled":false,"status":"Active","voice_enabled":false}],
    //  "is_enrolled":true,"last_directory_sync":null,"last_login":1727537850,"lastname":"","lockout_reason":null,"notes":"",
    //  "phones":[
    //  {"activated":true,"capabilities":["auto","push","sms","phone","mobile_otp"],"extension":"","last_seen":"2024-09-28T21:28:48","model":"Apple iPhone SE","name":"phone 1","number":"+1123456","phone_id":"abc123","platform":"Apple iOS","postdelay":"","predelay":"","sms_passcodes_sent":true,"type":"Mobile"},
    //  {"activated":false,"capabilities":["auto","phone"],"extension":"","last_seen":"","model":"Unknown","name":"phone 2","number":"+1123456","phone_id":"abc123","platform":"Unknown","postdelay":"","predelay":"","sms_passcodes_sent":true,"type":"Landline"},
    //  {"activated":false,"capabilities":["auto","phone"],"extension":"","last_seen":"","model":"Unknown","name":"phone 3","number":"+1123456","phone_id":"abc123","platform":"Unknown","postdelay":"","predelay":"","sms_passcodes_sent":false,"type":"Landline"}],
    //  "realname":"",
    //  "status":"active", (or bypass or disabled)
    //  "tokens":[{"serial":"pennprod__10021368__hotp__0","token_id":"abc123","totp_step":null,"type":"h6"},
    //  {"serial":"pennprod__10021368__hotp__500000","token_id":"abc123","totp_step":null,"type":"h6"},
    //  {"serial":"pennprod__10021368__totp__30","token_id":"abc123","totp_step":30,"type":"t6"}],
    //  "u2ftokens":[],
    //  "user_id":"abc123","username":"mchyzer","webauthncredentials":[{"credential_name":"Touch ID","date_added":1699543237,"label":"Chrome on Mac","webauthnkey":"abc123"},
    //  {"credential_name":"iCloud Keychain","date_added":1712852003,"label":"iCloud Keychain","webauthnkey":"abc123"}]}

    return duoUser;
    
  }
  
  /**
   * 
   * @param configId 
   * @param subject
   */
  public Map<String, Object> retrieveDuoUser(String configId, Subject subject) {

    long startedNanos = System.nanoTime();

    Map<String, Object> result = new LinkedHashMap<String, Object>();
    
    result.put("userFound", false);

    
    try {
      if (subject == null) {
        throw new RuntimeException("subject is null");
      }
  
      String subjectIdValueFormat = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector." + configId + ".subjectIdValueFormat");
      
      String requireSubjectAttribute = GrouperConfig.retrieveConfig().propertyValueString("grouper.duoConnector." + configId + ".requireSubjectAttribute");
      
      if (!StringUtils.isBlank(requireSubjectAttribute)) {
        if (StringUtils.isBlank(subject.getAttributeValue(requireSubjectAttribute))) {
          return result;
        }
      }
      
      String username  = CustomUiUtil.substituteExpressionLanguage(subjectIdValueFormat, null, null, null, subject, null);
      
      if (StringUtils.isBlank(username)) {
        throw new RuntimeException("Cant find username from subject: '" + subjectIdValueFormat + "', " + SubjectUtils.subjectToString(subject));
      }
      
      JsonNode jsonNode = GrouperDuoApiCommands.retrieveDuoUserByNameJsonNode(configId, username, true);

      result.put("userFound", jsonNode != null && jsonNode.has("user_id"));

      if (jsonNode == null) {
        return result;
      }

      result = GrouperUtil.objectMapper.convertValue(jsonNode, Map.class);
      
      {
        StringBuilder summary = new StringBuilder();

        summary.append("is_enrolled: ").append(result.get("is_enrolled"))
          .append(", status: ").append(result.get("status"))
          .append(", username: ").append(result.get("username"))
          .append(", email: ").append(result.get("email"))
          .append(", notes: ").append(result.get("notes"))
          .append(", user_id: ").append(result.get("user_id"))
          .append(", lockout_reason: ").append(result.get("lockout_reason"))
          .append(", aliasesSize: ").append(GrouperUtil.length(result.get("aliases")))
          .append(", groupsSize: ").append(GrouperUtil.length(result.get("groups")))
          .append(", phonesSize: ").append(GrouperUtil.length(result.get("phones")))
          .append(", webauthnCredentialsSize: ").append(GrouperUtil.length(result.get("webauthncredentials")));
        result.put("summary", summary.toString());
      }

    } catch (RuntimeException re) {
      
      this.debugMapPut("duoUserError", GrouperUtil.getFullStackTrace(re));
      throw re;

    } finally {
      this.debugMapPut("duoUserTookMillis", (System.nanoTime()-startedNanos)/1000000);
    }
    
    return result;
  }
  
  public static void main(String[] args) throws Exception {
    GrouperStartup.startup();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        // 10287464
        Subject subject1 = SubjectFinder.findById("10021368", true);
        
        
        Map<String, Object> duoUser = new CustomUiDuo().retrieveDuoUserOrFromCache("duoAdminProdReadonly", subject1);
        
        System.out.println(GrouperUtil.mapToString(duoUser));
        
        boolean hasMembership = new CustomUiDuo().hasDuoMembershipByDuoGroupName("duoAdminProdReadonly", "EarlyAdopters", subject1);
        
        System.out.println(hasMembership);

        return null;
      }
    });

//    GrouperSession.startRootSession();
//    //  -- e814c4773cc24b34a75f7a73e2fcfbb9 -> metadata
//    //  -- 721bab64af524f068ebeaa963be4b2d9 -> metadata
//    
//    AttributeAssign attributeAssign = AttributeAssignFinder.findById("e814c4773cc24b34a75f7a73e2fcfbb9", true);
//    attributeAssign.delete();
//
//    attributeAssign = AttributeAssignFinder.findById("721bab64af524f068ebeaa963be4b2d9", true);
//    attributeAssign.delete();

    System.exit(0);

  }
  
  /**
   * 
   */
  public CustomUiDuo() {
  }

}
