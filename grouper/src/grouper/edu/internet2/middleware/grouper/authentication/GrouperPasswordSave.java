package edu.internet2.middleware.grouper.authentication;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.Application;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.EncryptionType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.StringUtils;
import edu.internet2.middleware.morphString.Morph;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectUtils;

/**
 * <p>Use this class to add username and password in grouper registry</p>
 * <p>Sample call to create a username password for grouper ui
 * 
 * <blockquote>
 * <pre>
 * new GrouperPasswordSave().assignUsername("GrouperSystem").assignPassword("admin123").assignApplication(GrouperPassword.Application.UI).save();
 * </pre>
 * </blockquote>
 * 
 * </p>
 * 
 * <p> Sample call to create a username password for grouper webservices
 * <blockquote>
 * <pre>
 * new GrouperPasswordSave().assignUsername("GrouperSystem").assignPassword("admin123").assignApplication(GrouperPassword.Application.WS).save();
 * </pre>
 * </blockquote>
 * </p>
 * 
 * <p> Sample call to delete a username password for grouper ui
 * <blockquote>
 * <pre>
 * new GrouperPasswordSave().assignUsername("GrouperSystem").assignApplication(GrouperPassword.Application.UI).assignSaveMode("DELETE").save();
 * </pre>
 * </blockquote>
 * </p>
 * 
 * 
 */
public class GrouperPasswordSave {
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    GrouperSession.startRootSession();
    new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI).assignUsername("GrouperSystem").assignPassword("mypassword2").save();
    new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI).assignUsername("GrouperSystem").assignPassword("mypassword3").save();
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperPasswordSave.class);
  
  /**
   * username to be assigned
   */
  private String username;
  
  /**
   * entity type to be assigned
   */
  private String entityType;
  
  private EncryptionType encryptionType;
  
  private String thePassword;
  
  private String publicKey;
  
  private Application application;
  
  private String allowedFromCidrs;
  
  private Long expiresAtDb;
  
  private String memberId;
  
  private String memberIdWhoSetPassword;
  
  /** save mode */
  private SaveMode saveMode;

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public GrouperPasswordSave assignSaveMode(SaveMode theSaveMode) {
    this.saveMode = theSaveMode;
    return this;
  }

  /**
   * assign save mode
   * @param theSaveMode
   * @return this for chaining
   */
  public GrouperPasswordSave assignSaveMode(String theSaveMode) {
    this.saveMode = SaveMode.valueOfIgnoreCase(theSaveMode);
    return this;
  }
  
  
  public GrouperPasswordSave assignMemberId(String memberId) {
    this.memberId = memberId;
    this.memberIdAssigned = true;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean memberIdAssigned = false;
  
  public GrouperPasswordSave assignPublicKey(String publicKey) {
    this.publicKey = publicKey;
    this.publicKeyAssigned = true;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean publicKeyAssigned = false;
  
  
  public static boolean canAccessWsJwtKeys(Subject subject, Subject localEntitySubject) {
    
    Subject loggedInSubject = subject;
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    
    String groupNameAllowedToManage = GrouperConfig.retrieveConfig().getProperty("grouper.selfService.jwt.groupNameAllowedToManage", null);
    
    if (StringUtils.isBlank(groupNameAllowedToManage)) {
      
      // localEntity that's being viewed on the screen
      Group localEntity = GroupFinder.findByUuid(localEntitySubject.getId(), false);
      
      if (localEntity.hasAdmin(loggedInSubject)) {
        return true;
      }
      
    } else {
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Group groupThatIsAllowedToManageJwts = GroupFinder.findByName(groupNameAllowedToManage, false);
          if (groupThatIsAllowedToManageJwts == null) {
            LOG.error("Group does not exist: "+groupNameAllowedToManage);
            return false;
          }
          
          if (groupThatIsAllowedToManageJwts.hasMember(loggedInSubject)) {
            
            Group localEntity = GroupFinder.findByUuid(localEntitySubject.getId(), false);
            
            if (localEntity.hasAdmin(loggedInSubject)) {
              return true;
            }
            
          }
          
          return false;
        }
      });
      
    }
    return false;
  
    
  }
  
  /**
   * save credentials into the database
   */
  public void save() {
    
    /**
     * 
     * have a field publicKey
     * if publicKey is there, then save without encryption
     * replaceAllSettings is there
     *  true  - already working for username, password
     *  true - public key, save to db
     *  
     *  replaceAllSettings false (username, password) - 
     *  replaceAllSettings false (public key) - 
     * 
     */
    
    if (StringUtils.isNotBlank(publicKey) && StringUtils.isNotBlank(thePassword)) {
      throw new RuntimeException("Either publicKey or password can be set, not both.");
    }
    
    if (StringUtils.isNotBlank(publicKey)) {
      savePublicKey();
    } else {
      saveUserPassword();
    }
    
  }
  
  
  /**
   * assign username to be stored
   * @param username
   * @return
   */
  public GrouperPasswordSave assignUsername(String username) {
    this.username = username;
    this.usernameAssigned = true;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean usernameAssigned = false;
  
  /**
   * assign allowed from cidrs
   * @param allowedFromCidrs
   * @return
   */
  public GrouperPasswordSave assignAllowedFromCidrs(String allowedFromCidrs) {
    this.allowedFromCidrs = allowedFromCidrs;
    this.allowedFromCidrsAssigned = true;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean allowedFromCidrsAssigned = false;
  
  /**
   * assign memberIdWhoSetPassword
   * @param memberIdWhoSetPassword
   * @return
   */
  public GrouperPasswordSave assignMemberIdWhoSetPassword(String memberIdWhoSetPassword) {
    this.memberIdWhoSetPassword = memberIdWhoSetPassword;
    this.memberIdWhoSetPasswordAssigned = true;
    return this;
  }
  
  private boolean memberIdWhoSetPasswordAssigned = false;
  
  /**
   * assign expiresAt
   * @param expiresAt
   * @return
   */
  public GrouperPasswordSave assignExpiresAt(Long expiresAt) {
    this.expiresAtDb = expiresAt;
    this.expiresAtAssigned = true;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean expiresAtAssigned = false;
  
  /**
   * assign entity type e.g. username
   * @param entityType
   * @return
   */
  public GrouperPasswordSave assignEntityType(String entityType) {
    this.entityType = entityType;
    this.entityTypeAssigned = true;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean entityTypeAssigned = false;
  
  /**
   * assign encryption type
   * @param encryptionType
   * @return
   */
  public GrouperPasswordSave assignEncryptionType(EncryptionType encryptionType) {
    this.encryptionType = encryptionType;
    this.encryptionTpyeAssigned = true;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean encryptionTpyeAssigned = false;
  
  private boolean passwordAssigned = false;
  
  /**
   * assign password to be stored
   * @param password
   * @return
   */
  public GrouperPasswordSave assignPassword(String password) {
    this.thePassword = password;
    this.passwordAssigned = true;
    return this;
  }
  
  /**
   * assign application type (ws or ui)
   * @param application
   * @return
   */
  public GrouperPasswordSave assignApplication(GrouperPassword.Application application) {
    this.application = application;
    this.applicationAssigned = true;
    return this;
  }
  
  /**
   * if this was assigned and should be changed
   */
  private boolean applicationAssigned = false;

  /** uuid */
  private String uuid;
  
  /** 
   * uuid
   * @param theUuid
   * @return uuid
   */
  public GrouperPasswordSave assignUuid(String theUuid) {
    this.uuid = theUuid;
    return this;
  }
  
  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }


  
  /**
   * @return the entityType
   */
  public String getEntityType() {
    return entityType;
  }

  
  /**
   * @return the encryptionType
   */
  public EncryptionType getEncryptionType() {
    return encryptionType;
  }


  
  /**
   * @return the thePassword
   */
  public String getThePassword() {
    return thePassword;
  }


  
  /**
   * @return the application
   */
  public Application getApplication() {
    return application;
  }
  
  /** save type after the save */
  private SaveResultType saveResultType = null;
  
  /**
   * set this to true to run as a root session
   */
  private boolean runAsRoot;
  
  /**
   * set this to true to run as a root session
   * @param runAsRoot
   * @return
   */
  public GrouperPasswordSave assignRunAsRoot(boolean runAsRoot) {
    this.runAsRoot = runAsRoot;
    return this;
  }
  
  /**
   * get the save result type after the save call
   * @return save type
   */
  public SaveResultType getSaveResultType() {
    return this.saveResultType;
  }

  
  public String getAllowedFromCidrs() {
    return allowedFromCidrs;
  }

  
  public Long getExpiresAtDb() {
    return expiresAtDb;
  }

//  
//  public String getMemberIdWhoSetPassword() {
//    return memberIdWhoSetPassword;
//  }
  
  private boolean replaceAllSettings = true;
  
  /**
   * if you want to replace all the settings for the object, send true (that's the default). If you want to update certain fields, send false.
   * @return this for chaining
   */
  public GrouperPasswordSave assignReplaceAllSettings(boolean theReplaceAllSettings) {
    
    this.replaceAllSettings = theReplaceAllSettings;
    return this;
  }
  
  
 
  
  private GrouperPassword savePublicKey() {

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    
    GrouperPassword grouperPassword = (GrouperPassword) GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
        
        grouperTransaction.setCachingEnabled(false);
        
        final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();

        return GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (!runAsRoot) {
              
              Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
              
              if (!canAccessWsJwtKeys(SUBJECT_IN_SESSION, member.getSubject())) {
                throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
                + "' cannot save/delete grouper public key for local entity '" + memberId + "'");
              } 
            }
            
            if (saveMode == SaveMode.DELETE) {
              
              GrouperPassword grouperPassword = null;
              
              if (StringUtils.isNotBlank(GrouperPasswordSave.this.uuid)) {
                grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findById(GrouperPasswordSave.this.uuid, false);
              }
              
              if (grouperPassword == null && StringUtils.isNotBlank(GrouperPasswordSave.this.username) && GrouperPasswordSave.this.application != null ) {
                grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(GrouperPasswordSave.this.username, GrouperPasswordSave.this.application.name());
              }
              
              if (grouperPassword == null) {
                GrouperPasswordSave.this.saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              
              GrouperDAOFactory.getFactory().getGrouperPassword().delete(grouperPassword);
              saveResultType = SaveResultType.DELETE;
              return grouperPassword;
              
            }

            GrouperPassword grouperPassword = null;
            
            if (StringUtils.isNotBlank(GrouperPasswordSave.this.uuid)) {
              grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findById(GrouperPasswordSave.this.uuid, false);
            }
            
            if (grouperPassword == null && StringUtils.isNotBlank(GrouperPasswordSave.this.username) && GrouperPasswordSave.this.application != null ) {
              grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(GrouperPasswordSave.this.username, GrouperPasswordSave.this.application.name());
            }
            
            if (saveMode == SaveMode.UPDATE && grouperPassword == null) {
              throw new RuntimeException("Updating grouperPassword settings but they do not exist!");
            }
            
            if (saveMode == SaveMode.INSERT && grouperPassword != null) {
              throw new RuntimeException("Inserting grouperPassword settings but they already exist!");
            }
            
            if (StringUtils.isBlank(memberIdWhoSetPassword)) {
              if (runAsRoot) {
                memberIdWhoSetPassword = grouperSession.getMember().getId();
              } else {
                memberIdWhoSetPassword = MemberFinder.findBySubject(grouperSession, SUBJECT_IN_SESSION, true).getId();
              }
            } else {
              MemberFinder.findByUuid(grouperSession, memberIdWhoSetPassword, true);
            }
            
            if (grouperPassword == null) {
              
              if (!replaceAllSettings) {
                throw new RuntimeException("You can only edit certain fields if the object exists.");
              }
              
              if (StringUtils.isBlank(username)) {
                throw new RuntimeException("username is required");
              }
              
              if (StringUtils.isBlank(memberId)) {
                throw new RuntimeException("memberId cannot be null");
              }
              
              if (StringUtils.isNotBlank(entityType) && !StringUtils.equals("localEntity", entityType)) {
                throw new RuntimeException("entityType has to be localEntity or blank for public keys");
              }
              
              if (null == application) {
                throw new RuntimeException("application is required");
              }
              
              if (null != encryptionType && encryptionType != EncryptionType.RS_2048) {
                throw new RuntimeException("encryptionType has to be RS_2048 or blank for public keys");
              }
              
              grouperPassword = new GrouperPassword();
              grouperPassword.setApplication(application);
              grouperPassword.setUsername(username);
              grouperPassword.setEncryptionType(GrouperPassword.EncryptionType.RS_2048);
              grouperPassword.setMemberId(memberId);
              grouperPassword.setEntityType("localEntity");
              grouperPassword.setThePassword(publicKey);
              grouperPassword.setHashed(false);
              grouperPassword.setTheSalt(null);
              grouperPassword.setLastEdited(Instant.now().toEpochMilli());
              grouperPassword.setAllowedFromCidrs(allowedFromCidrs);
              grouperPassword.setMemberIdWhoSetPassword(memberIdWhoSetPassword);
              grouperPassword.setExpiresMillis(expiresAtDb);
              
              GrouperDAOFactory.getFactory().getGrouperPassword().saveOrUpdate(grouperPassword);
              
              saveResultType = SaveResultType.INSERT;
              return grouperPassword;
              
            } else {
              
              boolean needsSave = false;
              saveResultType = SaveResultType.NO_CHANGE;
              
              if (application != grouperPassword.getApplication()) {
                
                if (replaceAllSettings || applicationAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setApplication(application);
                }
                
              }
              
              if (!StringUtils.equals(StringUtils.defaultString(StringUtils.trim(grouperPassword.getAllowedFromCidrs())), 
                  StringUtils.defaultString(StringUtils.trim(allowedFromCidrs)))) {
                
                if (replaceAllSettings || allowedFromCidrsAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setAllowedFromCidrs(allowedFromCidrs);
                }
                
              }
              
              if ( Objects.equals(expiresAtDb, grouperPassword.getExpiresMillis()) ) {
                
                if (replaceAllSettings || expiresAtAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setExpiresMillis(expiresAtDb);
                }
                
              }
              
              if (!StringUtils.equals(memberId, grouperPassword.getMemberId())) {
                
                if (replaceAllSettings || memberIdAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setMemberId(memberId);
                }
                
              }
              
              if (!StringUtils.equals(publicKey, grouperPassword.getThePassword())) {
                
                if (replaceAllSettings || publicKeyAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setThePassword(publicKey);
                  grouperPassword.setMemberIdWhoSetPassword(memberIdWhoSetPassword);
                }
                
              }
              
              if (needsSave) {
                GrouperDAOFactory.getFactory().getGrouperPassword().saveOrUpdate(grouperPassword);
              }
            }
            
            return grouperPassword;
          }
        });
        
      };
      
    });
    
    return grouperPassword;
    
  }
  
  private GrouperPassword saveUserPassword() {

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    
    GrouperPassword grouperPassword = (GrouperPassword) GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
        
        grouperTransaction.setCachingEnabled(false);
        
        final Subject SUBJECT_IN_SESSION = GrouperSession.staticGrouperSession().getSubject();
        
        return GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            if (!runAsRoot) {
              
              if (!PrivilegeHelper.isWheelOrRoot(SUBJECT_IN_SESSION)) {
                throw new RuntimeException("Subject '" + SubjectUtils.subjectToString(SUBJECT_IN_SESSION) 
                + "' cannot save/delete grouper password");
              }
            
            }
            
            if (saveMode == SaveMode.DELETE) {
              
              GrouperPassword grouperPassword = null;
              
              if (StringUtils.isNotBlank(GrouperPasswordSave.this.uuid)) {
                grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findById(GrouperPasswordSave.this.uuid, false);
              }
              
              if (grouperPassword == null && StringUtils.isNotBlank(GrouperPasswordSave.this.username) && GrouperPasswordSave.this.application != null ) {
                grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(GrouperPasswordSave.this.username, GrouperPasswordSave.this.application.name());
              }
              
              if (grouperPassword == null) {
                GrouperPasswordSave.this.saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              
              GrouperDAOFactory.getFactory().getGrouperPassword().delete(grouperPassword);
              saveResultType = SaveResultType.DELETE;
              return grouperPassword;
              
            }

            GrouperPassword grouperPassword = null;
            
            if (StringUtils.isNotBlank(uuid)) {
              grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findById(GrouperPasswordSave.this.uuid, false);
            }
            
            if (grouperPassword == null && StringUtils.isNotBlank(GrouperPasswordSave.this.username) && GrouperPasswordSave.this.application != null ) {
              grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(GrouperPasswordSave.this.username, GrouperPasswordSave.this.application.name());
            }
            
            if (saveMode == SaveMode.UPDATE && grouperPassword == null) {
              throw new RuntimeException("Updating grouperPassword settings but they do not exist!");
            }
            
            if (saveMode == SaveMode.INSERT && grouperPassword != null) {
              throw new RuntimeException("Inserting grouperPassword settings but they already exist!");
            }
            
            boolean splitOnFirstColon = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.authentication.splitBasicAuthOnFirstColon", false);
            if (splitOnFirstColon && StringUtils.contains(username, ":")) {
              throw new RuntimeException("username cannot contain a colon due to http basic auth and this grouper.properties setting grouper.authentication.splitBasicAuthOnFirstColon=true  "
                  + "Note, if you change that setting, you might need to adjust existing users/passes.  Note: it is recommended to use the local entity uuid as the username if you are using local entities");
            }
            if (!splitOnFirstColon && StringUtils.contains(thePassword, ":")) {
              throw new RuntimeException("password cannot contain a colon due to http basic auth and this grouper.properties setting grouper.authentication.splitBasicAuthOnFirstColon=false  "
                  + "Note, if you change that setting, you might need to adjust existing users/passes.  Note: it is recommended to use the local entity uuid as the username if you are using local entities");
            }
            
            if (StringUtils.isBlank(memberIdWhoSetPassword)) {
              if (runAsRoot) {
                memberIdWhoSetPassword = grouperSession.getMember().getId();
              } else {
                memberIdWhoSetPassword = MemberFinder.findBySubject(grouperSession, SUBJECT_IN_SESSION, true).getId();
              }
            } else {
              MemberFinder.findByUuid(grouperSession, memberIdWhoSetPassword, true);
            }
            
            if (grouperPassword == null) {
              
              if (!replaceAllSettings) {
                throw new RuntimeException("You can only edit certain fields if the object exists.");
              }
              
              if (StringUtils.isBlank(username)) {
                throw new RuntimeException("username is required");
              }
              
              if (StringUtils.isBlank(thePassword)) {
                throw new RuntimeException("password is required");
              }
              
              if (null == application) {
                throw new RuntimeException("application is required");
              }
              
              Object[] saltPasswordEncryptionType = encryptPassword(thePassword);
              
              grouperPassword = new GrouperPassword();
              grouperPassword.setApplication(application);
              grouperPassword.setUsername(username);
              grouperPassword.setEncryptionType((EncryptionType)saltPasswordEncryptionType[2]);
              grouperPassword.setMemberId(memberId);
              grouperPassword.setEntityType(entityType);
              grouperPassword.setThePassword(saltPasswordEncryptionType[1].toString());
              grouperPassword.setHashed(true);
              grouperPassword.setTheSalt(saltPasswordEncryptionType[0].toString());
              grouperPassword.setLastEdited(Instant.now().toEpochMilli());
              grouperPassword.setAllowedFromCidrs(allowedFromCidrs);
              grouperPassword.setMemberIdWhoSetPassword(memberIdWhoSetPassword);
              grouperPassword.setExpiresMillis(expiresAtDb);
              
              GrouperDAOFactory.getFactory().getGrouperPassword().saveOrUpdate(grouperPassword);
              
              saveResultType = SaveResultType.INSERT;
              return grouperPassword;
              
            } else {
              
              boolean needsSave = false;
              saveResultType = SaveResultType.NO_CHANGE;
              
              if (application != grouperPassword.getApplication()) {
                
                if (replaceAllSettings || applicationAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setApplication(application);
                }
                
              }
              
              if (!StringUtils.equals(StringUtils.defaultString(StringUtils.trim(grouperPassword.getAllowedFromCidrs())), 
                  StringUtils.defaultString(StringUtils.trim(allowedFromCidrs)))) {
                
                if (replaceAllSettings || allowedFromCidrsAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setAllowedFromCidrs(allowedFromCidrs);
                }
                
              }
              
              if ( Objects.equals(expiresAtDb, grouperPassword.getExpiresMillis()) ) {
                
                if (replaceAllSettings || expiresAtAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setExpiresMillis(expiresAtDb);
                }
                
              }
              
              if (!StringUtils.equals(memberId, grouperPassword.getMemberId())) {
                
                if (replaceAllSettings || memberIdAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  grouperPassword.setMemberId(memberId);
                }
                
              }
              
              if (StringUtils.isNotBlank(thePassword)) {
                if (replaceAllSettings || GrouperPasswordSave.this.passwordAssigned) {
                  needsSave = true;
                  if (saveResultType == SaveResultType.NO_CHANGE) {
                    saveResultType = SaveResultType.UPDATE;
                  }
                  
                  Object[] saltPasswordEncryptionType = encryptPassword(thePassword);
                  
                  grouperPassword.setEncryptionType((EncryptionType)saltPasswordEncryptionType[2]);
                  grouperPassword.setThePassword(saltPasswordEncryptionType[1].toString());
                  grouperPassword.setHashed(true);
                  grouperPassword.setTheSalt(saltPasswordEncryptionType[0].toString());
                  
                  grouperPassword.setMemberIdWhoSetPassword(memberIdWhoSetPassword);
                }
              }
              
              if (needsSave) {
                grouperPassword.setLastEdited(Instant.now().toEpochMilli());
                GrouperDAOFactory.getFactory().getGrouperPassword().saveOrUpdate(grouperPassword);
              }

            }
            
            return grouperPassword;
          }
        });
        
      };
      
    });
    
    return grouperPassword;
    
  }

  
  public String getMemberId() {
    return memberId;
  }

  
  public String getPublicKey() {
    return publicKey;
  }
  
  
  private Object[] encryptPassword(String thePassword) {
    // if its encrypted, decrypt it, otherwise just use it
    try {
      thePassword = Morph.decrypt(thePassword);
    } catch (Exception e) {
      // ignore, not encrypted
    }
    
    SecureRandom sr = new SecureRandom();
    byte[] salt = new byte[16];
    sr.nextBytes(salt);
    
    EncryptionType encryptionTypeLocal = null;
    
    if (encryptionType != null) {
      if (encryptionType != EncryptionType.SHA_256) {
        throw new RuntimeException("Only SHA_256 is allowed");
      }
      encryptionTypeLocal = encryptionType;
    } else {
      String encryptionTypeString = GrouperConfig.retrieveConfig().propertyValueString("grouper.authentication.encryptionType", null);
      if (StringUtils.isBlank(encryptionTypeString)) {
        throw new RuntimeException("grouper.authentication.encryptionType must be set to SHA-256 or RS-256");
      }
      try {        
        encryptionTypeLocal = GrouperPassword.EncryptionType.valueOf(encryptionTypeString.replace("-", "_"));
      } catch (Exception e) {
        throw new RuntimeException("grouper.authentication.encryptionType must be set to SHA-256 or RS-256");
      }
    }
    
    String hexSalt = Hex.encodeHexString(salt);
    String hashedPassword = encryptionTypeLocal.generateHash(hexSalt+thePassword);
    
    String encryptedPassword = Morph.encrypt(hashedPassword);
    return new Object[] { hexSalt, encryptedPassword, encryptionTypeLocal};
  }
  
  
  
}
