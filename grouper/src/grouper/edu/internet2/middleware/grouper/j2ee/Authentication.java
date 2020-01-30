/**
 * 
 */
package edu.internet2.middleware.grouper.j2ee;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.EncryptionType;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
          int p = credentials.indexOf(":");
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
  
  public boolean authenticate(final String authHeader, GrouperPassword.Application application) {
    
    if (StringUtils.isBlank(authHeader)) {
      return false;
    }
    
    try {
      StringTokenizer st = new StringTokenizer(authHeader);
      if (st.hasMoreTokens()) {
        String basic = st.nextToken();

        if (basic.equalsIgnoreCase("Basic")) {
          String credentials = new String(Base64.getDecoder().decode(st.nextToken()), "UTF-8");
          int p = credentials.indexOf(":");
          if (p != -1) {
            String user = credentials.substring(0, p).trim();
            String password = credentials.substring(p + 1).trim();
                
            GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(user, application.name());
                
            if (grouperPassword == null) {
              return false;
            }
                
            String generatedHash = grouperPassword.getEncryptionType().generateHash(grouperPassword.getTheSalt()+password);
                
            String encryptedPassword = Morph.encrypt(generatedHash);
                
            return StringUtils.equals(encryptedPassword, grouperPassword.getThePassword());

          }
        }
      }
    } catch (Exception e) {
      LOG.error("Error authenticating");
      return false;
    }
    
    return false;
    
  }
  
  public void assignUserPassword(GrouperPasswordSave grouperPasswordSave) {
    
    try {
      
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
      
      GrouperPassword grouperPassword = new GrouperPassword();
      grouperPassword.setApplication(grouperPasswordSave.getApplication());
      grouperPassword.setEncryptionType(encryptionType);
      grouperPassword.setEntityType(grouperPasswordSave.getEntityType());
      grouperPassword.setThePassword(encryptedPassword);
      grouperPassword.setHashed(encryptionType == GrouperPassword.EncryptionType.SHA_256);
      grouperPassword.setTheSalt(hexSalt);
      grouperPassword.setUsername(grouperPasswordSave.getUsername());
      grouperPassword.setLastEdited(Instant.now().toEpochMilli());
      
      GrouperDAOFactory.getFactory().getGrouperPassword().saveOrUpdate(grouperPassword);
      
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    }
    
  }

}
