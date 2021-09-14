package edu.internet2.middleware.grouper.authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperTrustedJwtConfig {

  /**
   * cache the configs
   */
  private static ExpirableCache<String, GrouperTrustedJwtConfig> grouperTrustedJwtConfigCache = new ExpirableCache<String, GrouperTrustedJwtConfig>(1);
  
  /**
   * retrieve from config or cache
   * @param configId
   * @return the config
   */
  public static GrouperTrustedJwtConfig retrieveFromConfigOrCache(String configId) {
    
    GrouperTrustedJwtConfig grouperTrustedJwtConfig = grouperTrustedJwtConfigCache.get(configId);
    if (grouperTrustedJwtConfig == null) {
      grouperTrustedJwtConfig = retrieveFromConfig(configId);
      grouperTrustedJwtConfigCache.put(configId, grouperTrustedJwtConfig);
    }
    
    return grouperTrustedJwtConfig;
  }

  /**
   * retrieve from config or cache
   * @param configId
   * @return the config
   */
  private static GrouperTrustedJwtConfig retrieveFromConfig(String configId) {

    GrouperTrustedJwtConfig grouperTrustedJwtConfig = new GrouperTrustedJwtConfig();
  
    //  grouper.jwt.trusted.configId.numberOfKeys = 1
    //      
    //  # encrypted public key of trusted authority
    //  grouper.jwt.trusted.configId.key.0.publicKey = abc123
    //   
    //  grouper.jwt.trusted.configId.key.0.encryptionType = RS-256
    //   
    //  # optional: yyyy-mm-dd hh:mm:ss.SSS
    //  grouper.jwt.trusted.configId.key.0.expiresOn = 2021-11-01 00:00:00.000
    //   
    for (int i=0;i<10;i++) {
      String publicKey = GrouperConfig.retrieveConfig().propertyValueString("grouper.jwt.trusted." + configId + ".key." + i + ".publicKey");
      if (!StringUtils.isBlank(publicKey)) {
        
        GrouperTrustedJwtConfigKey grouperTrustedJwtConfigKey = new GrouperTrustedJwtConfigKey();
        grouperTrustedJwtConfigKey.setPublicKey(publicKey);
        
        String encryptionType = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.jwt.trusted." + configId + ".key." + i + ".encryptionType");
        grouperTrustedJwtConfigKey.setEncryptionType(encryptionType);
        
        String expiresOn = GrouperConfig.retrieveConfig().propertyValueString("grouper.jwt.trusted." + configId + ".key." + i + ".expiresOn");
        
        if (!StringUtils.isBlank(expiresOn)) {
          Date expiresOnDate = GrouperUtil.stringToDate2(expiresOn);
          grouperTrustedJwtConfigKey.setExpiresOn(expiresOnDate);
        }
        if (!grouperTrustedJwtConfigKey.isExpired()) {
          grouperTrustedJwtConfig.grouperTrustedJwtConfigKeys.add(grouperTrustedJwtConfigKey);
        }
      }
      
    }
    if (grouperTrustedJwtConfig.grouperTrustedJwtConfigKeys.size() == 0) {
      throw new RuntimeException("No valid public keys for trusted jwt configId: '" + configId + "'");
    }

    //  # JWTs only last for so long
    //  grouper.jwt.trusted.configId.expirationSeconds = 600
    grouperTrustedJwtConfig.expirationSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.jwt.trusted." + configId + ".expirationSeconds", -1);
    //   
    //  # optional, could be in claim as "subjectSourceId"
    //  grouper.jwt.trusted.configId.subjectSourceId = myPeople
    grouperTrustedJwtConfig.subjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("grouper.jwt.trusted." + configId + ".subjectSourceId");

    //   
    //  # subjectId, subjectIdentifier, or subjectIdOrIdentifier (optional)
    //  grouper.jwt.trusted.configId.subjectIdType = subjectId
    grouperTrustedJwtConfig.subjectIdType = GrouperConfig.retrieveConfig().propertyValueString("grouper.jwt.trusted." + configId + ".subjectIdType");

    //   
    //  # some claim name that has the subjectId in it.  optional, can just label claim name as "subjectId", "subjectIdentifier", or "subjectIdOrIdentifier"
    //  grouper.jwt.trusted.configId.subjectIdClaimName = pennId
    grouperTrustedJwtConfig.subjectIdClaimName = GrouperConfig.retrieveConfig().propertyValueString("grouper.jwt.trusted." + configId + ".subjectIdClaimName");
    
    return grouperTrustedJwtConfig;
  }

   /**
   * some claim name that has the subjectId in it.  optional, can just label claim name as "subjectId", "subjectIdentifier", or "subjectIdOrIdentifier"
   */
  private String subjectIdClaimName = null;
  
  /**
   * subject id claim name
   * @return claim name
   */
  public String getSubjectIdClaimName() {
    return subjectIdClaimName;
  }

  /**
   * subject id claim name
   * @param subjectIdClaimName
   */
  public void setSubjectIdClaimName(String subjectIdClaimName) {
    this.subjectIdClaimName = subjectIdClaimName;
  }

  /**
   * subjectId, subjectIdentifier, or subjectIdOrIdentifier (optional)
   */
  private String subjectIdType = null;
  
  /**
   * subjectId, subjectIdentifier, or subjectIdOrIdentifier (optional)
   * @return subject id type
   */
  public String getSubjectIdType() {
    return subjectIdType;
  }

  /**
   * subjectId, subjectIdentifier, or subjectIdOrIdentifier (optional)
   * @param subjectIdType1
   */
  public void setSubjectIdType(String subjectIdType1) {
    this.subjectIdType = subjectIdType1;
  }

  /**
   * optional, could be in claim as "subjectSourceId"
   */
  private String subjectSourceId = null;
  
  
  /**
   * optional, could be in claim as "subjectSourceId"
   * @return subject source id
   */
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  /**
   * optional, could be in claim as "subjectSourceId"
   * @param subjectSourceId1
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }

  /**
   * config keys
   */
  private List<GrouperTrustedJwtConfigKey> grouperTrustedJwtConfigKeys = new ArrayList<GrouperTrustedJwtConfigKey>();

  /**
   * config keys
   * @return
   */
  public List<GrouperTrustedJwtConfigKey> getGrouperTrustedJwtConfigKeys() {
    return grouperTrustedJwtConfigKeys;
  }

  /**
   * config keys
   * @param grouperTrustedJwtConfigKeys
   */
  public void setGrouperTrustedJwtConfigKeys(
      List<GrouperTrustedJwtConfigKey> grouperTrustedJwtConfigKeys) {
    this.grouperTrustedJwtConfigKeys = grouperTrustedJwtConfigKeys;
  }
  
  /**
   * how many seconds a jwt lasts
   */
  private int expirationSeconds = -1;
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperTrustedJwtConfig.class);

  /**
   * how many seconds a jwt lasts
   * @return
   */
  public int getExpirationSeconds() {
    return expirationSeconds;
  }

  /**
   * how many seconds a jwt lasts
   * @param expirationSeconds
   */
  public void setExpirationSeconds(int expirationSeconds) {
    this.expirationSeconds = expirationSeconds;
  }
  
  
  
}
