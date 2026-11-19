package edu.internet2.middleware.grouper.authentication;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.Application;
import edu.internet2.middleware.grouper.authentication.GrouperPassword.EncryptionType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
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
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        GrouperStartup.startup();
        new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI).assignUsername("GrouperSystem").assignPassword("mypassword2").save();
        new GrouperPasswordSave().assignApplication(GrouperPassword.Application.UI).assignUsername("GrouperSystem").assignPassword("mypassword3").save();
        return null;
      }
    });
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
  
  
  /**
   * see if a subject can view / create / edit / delete the WS JWT keys of a local entity.
   * Wheel or root can always.  Otherwise the subject needs ADMIN on the local entity, and if
   * grouper.selfService.jwt.groupNameAllowedToManage is set, also needs to be a member of that group.
   * Lookups run as root so a subject who cannot VIEW an object gets a clean false, not a NullPointerException.
   * @param subject subject doing the work (e.g. logged in subject)
   * @param localEntitySubject subject of the local entity whose keys are being managed
   * @return true if allowed
   */
  public static boolean canAccessWsJwtKeys(final Subject subject, final Subject localEntitySubject) {

    final Subject loggedInSubject = subject;
    if (loggedInSubject == null || localEntitySubject == null) {
      return false;
    }
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }

    final String groupNameAllowedToManage = GrouperConfig.retrieveConfig().getProperty("grouper.selfService.jwt.groupNameAllowedToManage", null);

    // GRP security fix: the result of the root callback used to be ignored when the manage group was set,
    // so non-wheel admins were always denied, and a missing local entity threw a NullPointerException.
    // Both branches now run as root and return the callback result.
    Boolean result = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        if (!StringUtils.isBlank(groupNameAllowedToManage)) {
          Group groupThatIsAllowedToManageJwts = GroupFinder.findByName(grouperSession, groupNameAllowedToManage, false);
          if (groupThatIsAllowedToManageJwts == null) {
            LOG.error("Group does not exist: "+groupNameAllowedToManage);
            return false;
          }

          if (!groupThatIsAllowedToManageJwts.hasMember(loggedInSubject)) {
            return false;
          }
        }

        // localEntity that's being viewed on the screen
        Group localEntity = GroupFinder.findByUuid(grouperSession, localEntitySubject.getId(), false);

        // not found (e.g. not a group / local entity): deny
        if (localEntity == null) {
          return false;
        }

        return localEntity.hasAdmin(loggedInSubject);
      }
    });

    return result != null && result;

  }

  /**
   * find the existing password row for this save (by uuid, else by username and application)
   * @return the row or null
   */
  private GrouperPassword findExistingGrouperPassword() {
    GrouperPassword grouperPassword = null;

    if (StringUtils.isNotBlank(this.uuid)) {
      grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findById(this.uuid, false);
    }

    if (grouperPassword == null && StringUtils.isNotBlank(this.username) && this.application != null ) {
      grouperPassword = GrouperDAOFactory.getFactory().getGrouperPassword().findByUsernameApplication(this.username, this.application.name());
    }
    return grouperPassword;
  }

  /**
   * if the row is a WS JWT public key of a local entity (as opposed to a username / password)
   * @param grouperPassword
   * @return true if public key row
   */
  private static boolean isPublicKeyRow(GrouperPassword grouperPassword) {
    return grouperPassword != null && (grouperPassword.getEncryptionType() == EncryptionType.RS_2048
        || StringUtils.equals("localEntity", grouperPassword.getEntityType()));
  }

  /**
   * write an audit entry for a WS JWT key change on a local entity.  Uses the existing ENTITY_UPDATE
   * audit type (no new audit type, so no DDL or upgrade).  Never put key material in the description.
   * Call this OUTSIDE the root session callback so the audit context does not record root as the actor.
   * @param localEntity local entity group or null if not found
   * @param localEntityMemberId member id of the local entity
   * @param action e.g. Created, Updated, Deleted
   * @param grouperPassword the row
   * @param actorMemberId member id of the subject who made the change
   */
  private static void auditWsJwtKeyChange(Group localEntity, String localEntityMemberId, String action,
      GrouperPassword grouperPassword, String actorMemberId) {

    AuditEntry auditEntry = null;

    if (localEntity != null) {
      auditEntry = new AuditEntry(AuditTypeBuiltin.ENTITY_UPDATE, "id",
          localEntity.getUuid(), "name", localEntity.getName(), "parentStemId", localEntity.getParentUuid(),
          "displayName", localEntity.getDisplayName(), "description", localEntity.getDescription());
    } else {
      auditEntry = new AuditEntry(AuditTypeBuiltin.ENTITY_UPDATE, "id", localEntityMemberId);
    }

    StringBuilder description = new StringBuilder();
    description.append(action).append(" WS JWT key for local entity: ")
      .append(localEntity == null ? ("memberId " + localEntityMemberId) : localEntity.getName())
      .append(", passwordId: ").append(grouperPassword.getId());
    if (grouperPassword.getExpiresMillis() != null) {
      description.append(", expires: ").append(new java.sql.Timestamp(grouperPassword.getExpiresMillis()));
    }
    if (!StringUtils.isBlank(grouperPassword.getAllowedFromCidrs())) {
      description.append(", allowedFromCidrs: ").append(grouperPassword.getAllowedFromCidrs());
    }
    auditEntry.setDescription(StringUtils.abbreviate(description.toString(), 3500));

    // fill in context (logged in member, ip, engine, etc), then make sure the actor is recorded
    GrouperContext.assignAuditEntryFields(auditEntry);
    if (StringUtils.isBlank(auditEntry.getLoggedInMemberId()) && StringUtils.isBlank(auditEntry.getActAsMemberId())) {
      auditEntry.setActAsMemberId(actorMemberId);
    }
    auditEntry.saveOrUpdate(false);
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
    } else if (StringUtils.isBlank(thePassword) && isPublicKeyRow(findExistingGrouperPassword())) {
      // GRP security fix: a delete or a partial edit (e.g. cidrs, expiration) of an existing WS JWT key
      // has no public key assigned, but must still go through the public key path so it gets the
      // local entity access check and the audit entry
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

        // GRP security fix: what to audit, filled in by the root callback, written after it returns
        // (so the audit context records the real actor, not root).  [0] action, [1] local entity group, [2] actor member id
        final Object[] auditInfo = new Object[3];

        GrouperPassword result = (GrouperPassword)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            
            // find the existing row first so the access check covers the entity that owns it
            GrouperPassword grouperPassword = findExistingGrouperPassword();
            
            // e.g. a delete or partial edit by uuid without a member id: use the owner of the row
            if (StringUtils.isBlank(memberId) && grouperPassword != null) {
              memberId = grouperPassword.getMemberId();
            }
            
            if (StringUtils.isBlank(memberId)) {
              throw new RuntimeException("memberId cannot be null");
            }
            
            if (!runAsRoot) {
              
              Member member = MemberFinder.findByUuid(grouperSession, memberId, true);
              
              if (!canAccessWsJwtKeys(SUBJECT_IN_SESSION, member.getSubject())) {
                throw new RuntimeException("Subject '" + GrouperUtil.subjectToString(SUBJECT_IN_SESSION) 
                + "' cannot save/delete grouper public key for local entity '" + memberId + "'");
              } 
              
              // the row being changed must belong to an entity the subject can manage too (a uuid
              // of another entity's key must not ride on access to this entity)
              if (grouperPassword != null && !StringUtils.equals(memberId, grouperPassword.getMemberId())) {
                Member ownerMember = StringUtils.isBlank(grouperPassword.getMemberId()) ? null 
                    : MemberFinder.findByUuid(grouperSession, grouperPassword.getMemberId(), false);
                if (ownerMember == null || !canAccessWsJwtKeys(SUBJECT_IN_SESSION, ownerMember.getSubject())) {
                  throw new RuntimeException("Subject '" + GrouperUtil.subjectToString(SUBJECT_IN_SESSION) 
                  + "' cannot save/delete grouper public key '" + grouperPassword.getId() + "'");
                }
              }
            }
            
            // who is doing this, for the audit
            auditInfo[2] = runAsRoot ? grouperSession.getMember().getId() 
                : MemberFinder.findBySubject(grouperSession, SUBJECT_IN_SESSION, true).getId();
            
            // local entity for the audit (may be null if the member is not a group)
            Member localEntityMember = MemberFinder.findByUuid(grouperSession, memberId, false);
            if (localEntityMember != null) {
              auditInfo[1] = GroupFinder.findByUuid(grouperSession, localEntityMember.getSubjectId(), false);
            }
            
            if (saveMode == SaveMode.DELETE) {
              
              if (grouperPassword == null) {
                GrouperPasswordSave.this.saveResultType = SaveResultType.NO_CHANGE;
                return null;
              }
              
              // remove the recently used rows too (the UI used to do this itself through the DAO)
              GrouperDAOFactory.getFactory().getGrouperPasswordRecentlyUsed().deleteGrouperPasswordRecentlyUsedEntries(grouperPassword.getId());
              GrouperDAOFactory.getFactory().getGrouperPassword().delete(grouperPassword);
              saveResultType = SaveResultType.DELETE;
              auditInfo[0] = "Deleted";
              return grouperPassword;
              
            }

            if (StringUtils.isBlank(publicKey) && replaceAllSettings) {
              throw new RuntimeException("publicKey is required when replacing all settings");
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
              auditInfo[0] = "Created";
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
              
              // GRP security fix: this compare was inverted, so a changed expiration was never saved
              if ( !Objects.equals(expiresAtDb, grouperPassword.getExpiresMillis()) ) {
                
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
                  auditInfo[0] = "Updated (new public key)";
                }
                
              }
              
              if (needsSave) {
                GrouperDAOFactory.getFactory().getGrouperPassword().saveOrUpdate(grouperPassword);
                if (auditInfo[0] == null) {
                  auditInfo[0] = "Updated";
                }
              }
            }
            
            return grouperPassword;
          }
        });
        
        // audit in the same transaction, outside of the root session
        if (auditInfo[0] != null && result != null) {
          auditWsJwtKeyChange((Group)auditInfo[1], memberId, (String)auditInfo[0], result, (String)auditInfo[2]);
        }
        
        return result;
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
                throw new RuntimeException("Subject '" + GrouperUtil.subjectToString(SUBJECT_IN_SESSION) 
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
              
              // save a changed expiration (this compare was inverted, so changing only the expiration did nothing)
              if ( !Objects.equals(expiresAtDb, grouperPassword.getExpiresMillis()) ) {
                
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
