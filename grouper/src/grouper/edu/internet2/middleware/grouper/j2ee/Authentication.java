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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.authentication.GrouperPassword;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.EncryptionType;
import edu.internet2.middleware.grouper.authentication.GrouperPasswordRecentlyUsed;
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
  
  public static void main(String[] args) {
    System.out.println("indexOfFirst a:b:c:sddfgdfgdfgdfg: " + colonIndexOf("a:b:c:sddfgdfgdfgdfg", true));
    System.out.println("indexOfLast a:b:c:sddfgdfgdfgdfg: " + colonIndexOf("a:b:c:sddfgdfgdfgdfg", false));
    System.out.println("unescapeTrue a&#x3a;b&#x3a;c: " + unescapeColons("a&#x3a;b&#x3a;c", true));
    System.out.println("unescapeFalse a&#x3a;b&#x3a;c: " + unescapeColons("a&#x3a;b&#x3a;c", false));
    
  }
  
  private static ExpirableCache<MultiKey, GrouperPassword> grouperPasswordCache = new ExpirableCache<MultiKey, GrouperPassword>(1);
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Authentication.class);
  
  /**
   * 
   * @param credentials
   * @return the index of the right colon
   */
  public static int colonIndexOf(String credentials) {
    //  # when splitting basic auth header (which doesnt handle colons well), split on first or last colon
    //  # first colon means usernames dont have colons, but passwords do. 
    //  # splitting on last colon means usernames have colons (e.g. local entities), but passwords dont
    //  # note you can also escape colons
    //  # {valueType: "boolean", defaultValue : "false"}
    //  grouper.authentication.splitBasicAuthOnFirstColon = false 
    boolean splitBasicAuthOnFirstColon = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.authentication.splitBasicAuthOnFirstColon", false);
    return colonIndexOf(credentials, splitBasicAuthOnFirstColon);
  }
  /**
   * 
   * @param credentials
   * @return the index of the right colon
   */
  private static int colonIndexOf(String credentials, boolean splitBasicAuthOnFirstColon) {
    if (splitBasicAuthOnFirstColon) {
      return credentials.indexOf(":");
    }
    return credentials.lastIndexOf(":");

  }

  /**
   * if configured to unescape colons, unescape colons
   * @param userOrPass
   * @return the unescaped user or pass
   */
  public static String unescapeColons(String userOrPass) {
    //  # You can escape colons in usernames and passwords, and they will be unescaped.  escape with &#x3a;
    //  # set this to false to not unescape colons
    //  # {valueType: "boolean", defaultValue : "false"}
    //  grouper.authentication.basicAuthUnescapeColon = true 
    boolean basicAuthUnescapeColon = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.authentication.basicAuthUnescapeColon", true);
    return unescapeColons(userOrPass, basicAuthUnescapeColon);
  }

  /**
   * if configured to unescape colons, unescape colons
   * @param userOrPass
   * @return the unescaped user or pass
   */
  private static String unescapeColons(String userOrPass, boolean basicAuthUnescapeColon) {
    if (userOrPass == null) {
      return userOrPass;
    }
    

    if (basicAuthUnescapeColon) {
      return StringUtils.replace(userOrPass, "&#x3a;", ":");
    }
    return userOrPass;
  }
  
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
          int p = colonIndexOf(credentials);
          if (p != -1) {
            String user = credentials.substring(0, p).trim();
            user = unescapeColons(user);
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
  
  public static final String retrievePassword(final String authHeader) {
    
    if (StringUtils.isBlank(authHeader)) {
      return null;
    }
    
    try {
      StringTokenizer st = new StringTokenizer(authHeader);
      
      if (st.hasMoreTokens()) {
        String basic = st.nextToken();
        if (basic.equalsIgnoreCase("Basic")) {
          
          String credentials = new String(Base64.getDecoder().decode(st.nextToken()), "UTF-8");
          int p = colonIndexOf(credentials);
          if (p != -1) {
            String password = credentials.substring(p + 1).trim();
            password = unescapeColons(password);
            return password;
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
  
  public boolean authenticate(final String authHeader, GrouperPassword.Application application, String requesterIpAddress) {
    
    if (StringUtils.isBlank(authHeader)) {
      return false;
    }
    
    long attemptMillis = System.currentTimeMillis();
    
    ExpirableCache<MultiKey, Boolean> authenticationCache = authenticationCache(application);
    
    GrouperPasswordRecentlyUsed grouperPasswordRecentlyUsed = new GrouperPasswordRecentlyUsed();
    grouperPasswordRecentlyUsed.setAttemptMillis(attemptMillis);
    grouperPasswordRecentlyUsed.setIpAddress(requesterIpAddress);
    
    try {
      StringTokenizer st = new StringTokenizer(authHeader);
      if (st.hasMoreTokens()) {
        String basic = st.nextToken();

        if (basic.equalsIgnoreCase("Basic")) {
          String credentials = new String(Base64.getDecoder().decode(st.nextToken()), "UTF-8");
          // last index of since the local entity might contain a colon
          int p = colonIndexOf(credentials);
          if (p != -1) {
            String user = credentials.substring(0, p).trim();
            String password = credentials.substring(p + 1).trim();
            
            user = unescapeColons(user);
            password = unescapeColons(password);

            MultiKey cacheKey = null;
            
            if (authenticationCache != null) {
              
              cacheKey = new MultiKey(application, user, Morph.encrypt(password));
              Boolean result = authenticationCache.get(cacheKey);
              if (result != null && result) {
                
                MultiKey multiKey = new MultiKey(user, application.name());
                GrouperPassword grouperPassword = grouperPasswordCache.get(multiKey);
                if (grouperPassword != null) {
                  grouperPasswordRecentlyUsed.setGrouperPasswordId(grouperPassword.getId());
                  grouperPasswordRecentlyUsed.setStatus('S');
                } else {
                  grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(user, application.name());
                  if (grouperPassword == null) {
                    return false;
                  }
                  
                  grouperPasswordCache.put(multiKey, grouperPassword);
                }
                
                return true;
              }
              
            }
            
            GrouperPassword grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(user, application.name());
                
            boolean correctPassword = false;
            
            if (grouperPassword != null) {
              String generatedHash = grouperPassword.getEncryptionType().generateHash(grouperPassword.getTheSalt()+password);
              
              String encryptedPassword = Morph.encrypt(generatedHash);
                  
              correctPassword = StringUtils.equals(encryptedPassword, grouperPassword.getThePassword());
              
              if (correctPassword) {
                grouperPasswordRecentlyUsed.setGrouperPasswordId(grouperPassword.getId());
                grouperPasswordRecentlyUsed.setStatus('S');
              } else {
                grouperPasswordRecentlyUsed.setGrouperPasswordId(grouperPassword.getId());
                grouperPasswordRecentlyUsed.setStatus('F');
              }
              
            } else {
              String configKey = "grouperPasswordConfigOverride_" + application.name() + "_" + user+ "_pass";
              String configPassword = GrouperHibernateConfig.retrieveConfig().propertyValueString(configKey);
              configPassword = Morph.decryptIfFile(configPassword);

              // if its encrypted, decrypt it
              try {
                configPassword = Morph.decrypt(configPassword);
              } catch (Exception e) {
                // ignore
              }
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
      grouperPasswordRecentlyUsed.setStatus('E');
      return false;
    } finally {
      if (StringUtils.isNotBlank(grouperPasswordRecentlyUsed.getGrouperPasswordId())) {
        GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed().saveOrUpdate(grouperPasswordRecentlyUsed);
      }
    }
    
    return false;
    
  }
  
  public void assignUserPassword(GrouperPasswordSave grouperPasswordSave) {
    grouperPasswordSave.save();
  }

  
}
