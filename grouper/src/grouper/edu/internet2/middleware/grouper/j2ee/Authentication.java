/**
 * 
 */
package edu.internet2.middleware.grouper.j2ee;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.EncryptionType;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;

/**
 * @author vsachdeva
 *
 */
public class Authentication {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Authentication.class);
  
  
  public static final String retrieveUsername(final String authHeader) {
    
    if (StringUtils.isBlank(authHeader)) {
      return null;
    }
    
    try {
      StringTokenizer st = new StringTokenizer(authHeader);
      
      if (st.hasMoreTokens()) {
        String basic = st.nextToken();
        if (basic.equalsIgnoreCase("Basic")) {
          
          String credentials = new String(Base64.getDecoder().decode(st.nextToken()), "UTF-8");
          int p = credentials.lastIndexOf(":");
          if (p != -1) {
            String user = credentials.substring(0, p).trim();
            return user;
          }
          
        }
        
      }
    } catch (Exception e) {
      LOG.error("Error retrieving username from authHeader");
      return null;
    }
    return null;
  }
  
  /**
   * system enum, user, and encrypted password
   */
  private static Map<GrouperPassword.Application, ExpirableCache<MultiKey, Boolean>> authenticationCache = 
      new HashMap<GrouperPassword.Application, ExpirableCache<MultiKey, Boolean>>();

  /**
   * 
   * @return the cache
   */
  private static ExpirableCache<MultiKey, Boolean> authenticationCache(GrouperPassword.Application application) {

    GrouperUtil.assertion(application != null, "application cant be null");
    
    //  # if we should cache UI authns
    //  # {valueType: "boolean", required : true}
    //  grouper.authentication.UI.cache = false
    //
    //  # if we should cache WS authns
    //  # {valueType: "boolean", required : true}
    //  grouper.authentication.WS.cache = false
    //
    //  # {valueType: "integer", required: true}
    //  grouper.authentication.UI.cacheTimeMinutes = 2
    //
    //  # {valueType: "integer", required: true}
    //  grouper.authentication.WS.cacheTimeMinutes = 2
    ExpirableCache<MultiKey, Boolean> localAuthenticationCache = null;
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.authentication." + application.name() + ".cache", true)) {
      localAuthenticationCache = authenticationCache.get(application);
      if (localAuthenticationCache == null) {
        localAuthenticationCache = new ExpirableCache<MultiKey, Boolean>(GrouperConfig.retrieveConfig().propertyValueInt(
            "grouper.authentication." + application.name() + ".cacheTimeMinutes", 2));
        authenticationCache.put(application, localAuthenticationCache);
      }
    }
    return localAuthenticationCache;
  }
  
  public boolean authenticate(final String authHeader, GrouperPassword.Application application) {
    
    if (StringUtils.isBlank(authHeader)) {
      return false;
    }
    
    ExpirableCache<MultiKey, Boolean> authenticationCache = authenticationCache(application);
    
    try {
      StringTokenizer st = new StringTokenizer(authHeader);
      if (st.hasMoreTokens()) {
        String basic = st.nextToken();

        if (basic.equalsIgnoreCase("Basic")) {
          String credentials = new String(Base64.getDecoder().decode(st.nextToken()), "UTF-8");
          // last index of since the local entity might contain a colon
          int p = credentials.lastIndexOf(":");
          if (p != -1) {
            String user = credentials.substring(0, p).trim();
            String password = credentials.substring(p + 1).trim();
    

            MultiKey cacheKey = null;
            
            if (authenticationCache != null) {
              
              cacheKey = new MultiKey(application, user, Morph.encrypt(password));
              Boolean result = authenticationCache.get(cacheKey);
              if (result != null && result) {
                return true;
              }
              
            }
            
            GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(user, application.name());
                
            boolean correctPassword = false;
            
            if (grouperPassword != null) {
              String generatedHash = grouperPassword.getEncryptionType().generateHash(grouperPassword.getTheSalt()+password);
              
              String encryptedPassword = Morph.encrypt(generatedHash);
                  
              correctPassword = StringUtils.equals(encryptedPassword, grouperPassword.getThePassword());
            } else {
              String configKey = "grouperPasswordConfigOverride_" + application.name() + "_" + user+ "_pass";
              String configPassword = GrouperHibernateConfig.retrieveConfig().propertyValueString(configKey);
              configPassword = Morph.decryptIfFile(configPassword);
              correctPassword = StringUtils.equals(password, configPassword);
            }
            if (correctPassword && authenticationCache != null) {
              authenticationCache.put(cacheKey, true);
            }
            return correctPassword;

          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error authenticating", e);
      return false;
    }
    
    return false;
    
  }
  
  public void assignUserPassword(GrouperPasswordSave grouperPasswordSave) {
    
    try {
      
      if (StringUtils.isBlank(grouperPasswordSave.getUsername())) {
        throw new RuntimeException("username is required");
      }
      if (StringUtils.isBlank(grouperPasswordSave.getThePassword())) {
        throw new RuntimeException("password is required");
      }
      if (null == grouperPasswordSave.getApplication()) {
        throw new RuntimeException("application is required");
      }
      if (grouperPasswordSave.getUsername().contains(":") && grouperPasswordSave.getThePassword().contains(":")) {
        throw new RuntimeException("username and password cannot both contain a colon due to http basic auth");
      }
      
      SecureRandom sr = new SecureRandom();
      byte[] salt = new byte[16];
      sr.nextBytes(salt);
      
      String encryptionTypeString = GrouperConfig.retrieveConfig().propertyValueString("grouper.authentication.encryptionType", null);
      if (StringUtils.isBlank(encryptionTypeString)) {
        throw new RuntimeException("grouper.authentication.encryptionType must be set to SHA-256 or RS-256");
      }
      
      EncryptionType encryptionType = null;
      
      try {        
        encryptionType = GrouperPassword.EncryptionType.valueOf(encryptionTypeString.replace("-", "_"));
      } catch (Exception e) {
        throw new RuntimeException("grouper.authentication.encryptionType must be set to SHA-256 or RS-256");
      }
      
      String hexSalt = Hex.encodeHexString(salt);
      String hashedPassword = encryptionType.generateHash(hexSalt+grouperPasswordSave.getThePassword());
      
      String encryptedPassword = Morph.encrypt(hashedPassword);
      
      GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword()
          .findByUsernameApplication(grouperPasswordSave.getUsername(), grouperPasswordSave.getApplication().name());
          
      if (grouperPassword == null) {
        grouperPassword = new GrouperPassword();
        grouperPassword.setApplication(grouperPasswordSave.getApplication());
        grouperPassword.setUsername(grouperPasswordSave.getUsername());
      }
      grouperPassword.setEncryptionType(encryptionType);
      grouperPassword.setEntityType(grouperPasswordSave.getEntityType());
      grouperPassword.setThePassword(encryptedPassword);
      grouperPassword.setHashed(encryptionType == GrouperPassword.EncryptionType.SHA_256);
      grouperPassword.setTheSalt(hexSalt);
      grouperPassword.setLastEdited(Instant.now().toEpochMilli());
      
      GrouperDAOFactory.getFactory().getGrouperPassword().saveOrUpdate(grouperPassword);
      
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    }
    
  }

}
