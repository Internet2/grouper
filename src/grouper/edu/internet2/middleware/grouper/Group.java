/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteAlreadyDeletedException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.exception.UnableToPerformAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.internal.util.U;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.misc.Owner;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.AddCompositeMemberValidator;
import edu.internet2.middleware.grouper.validator.CanOptinValidator;
import edu.internet2.middleware.grouper.validator.CanOptoutValidator;
import edu.internet2.middleware.grouper.validator.CompositeValidator;
import edu.internet2.middleware.grouper.validator.FieldTypeValidator;
import edu.internet2.middleware.grouper.validator.GetGroupAttributeValidator;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.NamingValidator;
import edu.internet2.middleware.grouper.validator.NotNullOrEmptyValidator;
import edu.internet2.middleware.grouper.validator.NotNullValidator;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/** 
 * A group within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.207 2008-10-27 19:26:15 shilen Exp $
 */
@SuppressWarnings("serial")
public class Group extends GrouperAPI implements Owner, Hib3GrouperVersioned {

  /** name of the groups table in the db */
  public static final String TABLE_GROUPER_GROUPS = "grouper_groups";
  
  /** uuid col in db */
  public static final String COLUMN_UUID = "uuid";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_UUID = "old_uuid";
  
  /**
   * if this is a composite group, get the composite object for this group
   * @return the composite group
   * @throws CompositeNotFoundException if composite not found
   */
  public Composite getComposite() throws CompositeNotFoundException {
    return CompositeFinder.findAsOwner(this);
  }
  
  /**
   * <pre>
   * create or update a group.  Note this will not rename a group at this time (might in future)
   * 
   * This is a static method since setters to Group objects persist to the DB
   * 
   * Steps:
   * 
   * 1. Find the group by groupNameToEdit
   * 2. Internally set all the fields of the stem (no need to reset if already the same)
   * 3. Store the group (insert or update) if needed
   * 4. Return the group object
   * 
   * This runs in a tx so that if part of it fails the whole thing fails, and potentially the outer
   * transaction too
   * </pre>
   * @param GROUPER_SESSION to act as
   * @param groupNameToEdit is the name of the group to edit (or null if insert)
   * @param description new description for group
   * @param displayExtension display friendly name for this group only
   * (parent stems are not specified)
   * @param name this is required, and is the full name of the group
   * including the names of parent stems.  e.g. stem1:stem2:stem3
   * the parent stem must exist unless createParentStemsIfNotExist.  
   * Can rename a stem extension, but not the parent stem name (move)
   * @param uuid of the group.  If a group exists with this uuid, then it will
   * be updated, if not, then it will be created if createIfNotExist is true
   * @param saveMode to constrain if insert only or update only, if null defaults to INSERT_OR_UPDATE
   * @param createParentStemsIfNotExist true if the stems should be created if they dont exist, false
   * for StemNotFoundException if not exist.  Note, the display extension on created stems
   * will equal the extension
   * @return the stem that was updated or created
   * @throws StemNotFoundException 
   * @throws InsufficientPrivilegeException 
   * @throws StemAddException 
   * @throws GroupModifyException 
   * @throws GroupNotFoundException
   * @throws GroupAddException
   */
  public static Group saveGroup(final GrouperSession GROUPER_SESSION, final String groupNameToEdit,
      final String uuid, final String name, final String displayExtension, final String description, 
      SaveMode saveMode, final boolean createParentStemsIfNotExist) 
        throws StemNotFoundException, InsufficientPrivilegeException, StemAddException, 
        GroupModifyException, GroupNotFoundException, GroupAddException {
  
    //validate
    //get the stem name
    if (!StringUtils.contains(name, ":")) {
      throw new RuntimeException("Group name must exist and must contain at least one stem name (separated by colons)");
    }

    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    final SaveMode SAVE_MODE = saveMode;
    try {
      //do this in a transaction
      Group group = (Group)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {

          return (Group)GrouperSession.callbackGrouperSession(GROUPER_SESSION, new GrouperSessionHandler() {

              public Object callback(GrouperSession grouperSession)
                  throws GrouperSessionException {
                
                try {
                  String groupNameForError = GrouperUtil.defaultIfBlank(groupNameToEdit, name);
                  
                  int lastColonIndex = name.lastIndexOf(':');
                  boolean topLevelGroup = lastColonIndex < 0;
          
                  //empty is root stem
                  String parentStemNameNew = GrouperUtil.parentStemNameFromName(name);
                  String extensionNew = GrouperUtil.extensionFromName(name);
                  
                  String theDisplayExtension = StringUtils.isBlank(displayExtension) ? extensionNew : displayExtension;
                  
                  //lets find the stem
                  Stem parentStem = null;
                  
                  try {
                    parentStem = topLevelGroup ? StemFinder.findRootStem(grouperSession) 
                        : StemFinder.findByName(grouperSession, parentStemNameNew);
                  } catch (StemNotFoundException snfe) {
                    
                    //see if we should fix this problem
                    if (createParentStemsIfNotExist) {
                      
                      //at this point the stem should be there (and is equal to currentStem), 
                      //just to be sure, query again
                      parentStem = Stem._createStemAndParentStemsIfNotExist(grouperSession, parentStemNameNew);
                    } else {
                      throw new GrouperSessionException(new StemNotFoundException("Cant find stem: '" + parentStemNameNew 
                          + "' (from update on stem name: '" + groupNameForError + "')"));
                    }
                  }
                  
                  Group theGroup = null;
                  //see if update
                  boolean isUpdate = SAVE_MODE.isUpdate(groupNameToEdit);
          
                  if (isUpdate) {
                    String parentStemNameLookup = GrouperUtil.parentStemNameFromName(groupNameToEdit);
                    if (!StringUtils.equals(parentStemNameLookup, parentStemNameNew)) {
                      throw new GrouperSessionException(new GroupModifyException("Can't move a group.  Existing parentStem: '"
                          + parentStemNameLookup + "', new stem: '" + parentStemNameNew + "'"));
                  }    
                  try {
                      theGroup = GroupFinder.findByName(grouperSession, groupNameToEdit);
                      
                      //while we are here, make sure uuid's match if passed in
                      if (!StringUtils.isBlank(uuid) && !StringUtils.equals(uuid, theGroup.getUuid())) {
                        throw new RuntimeException("UUID group changes are not supported: new: " + uuid + ", old: " 
                            + theGroup.getUuid() + ", " + groupNameForError);
                      }
                      
                    } catch (GroupNotFoundException gnfe) {
                      if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                        isUpdate = false;
                      } else {
                          throw new GrouperSessionException(gnfe);
                      }
                    }
                  }
                  
                  //if inserting
                  if (!isUpdate) {
                    if (StringUtils.isBlank(uuid)) {
                      try {
                        //if no uuid
                        theGroup = parentStem.addChildGroup(extensionNew, theDisplayExtension);
                      } catch (GroupAddException gae) {
                        //here for debugging
                        throw new GrouperSessionException(gae);
                      }
                    } else {
                      //if uuid
                      theGroup = parentStem.internal_addChildGroup(extensionNew, theDisplayExtension, uuid);
                    }
                  } else {
                    //check if different so it doesnt make unneeded queries
                    if (!StringUtils.equals(theGroup.getExtension(), extensionNew)) {
                      theGroup.setExtension(extensionNew);
                      theGroup.store();
                    }
                    if (!StringUtils.equals(theGroup.getDisplayExtension(), theDisplayExtension)) {
                      theGroup.setDisplayExtension(theDisplayExtension);
                      theGroup.store();
                    }
                  }
                  
                  //now compare and put all attributes (then store if needed)
                  if (!StringUtils.equals(theGroup.getDescription(), description)) {
                    //null throws exception... hmmm
                    if (!StringUtils.isBlank(description)) {
                      theGroup.setDescription(description);
                      theGroup.store();
                    }
                  }
                  
                  return theGroup;
                  //wrap checked exceptions inside unchecked, and rethrow outside
                } catch (StemNotFoundException snfe) {
                  throw new RuntimeException(snfe);
                } catch (InsufficientPrivilegeException ipe) {
                  throw new RuntimeException(ipe);
                } catch (StemAddException sae) {
                  throw new RuntimeException(sae);
                } catch (GroupModifyException gme) {
                  throw new RuntimeException(gme);
                } catch (GroupAddException gae) {
                  throw new RuntimeException(gae);
                }
              }
              
            });
            
        }
      });
      return group;
    } catch (RuntimeException re) {
      
      Throwable throwable = re.getCause();
      if (throwable instanceof StemNotFoundException) {
        throw (StemNotFoundException)throwable;
      }
      if (throwable instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)throwable;
      }
      if (throwable instanceof StemAddException) {
        throw (StemAddException)throwable;
      }
      if (throwable instanceof GroupModifyException) {
        throw (GroupModifyException)throwable;
      }
      if (throwable instanceof GroupNotFoundException) {
        throw (GroupNotFoundException)throwable;
      }
      if (throwable instanceof GroupAddException) {
        throw (GroupAddException)throwable;
      }
      //must just be runtime
      throw re;
    }
    
  }
  
  /** */
  private static final  EventLog                  EVENT_LOG            = new EventLog();
  /** */
  private static final  String                    KEY_CREATOR   = "creator";  // for state caching 
  /** */
  private static final  String                    KEY_MODIFIER  = "modifier"; // for state caching
  /** */
  private static final  String                    KEY_SUBJECT   = "subject";  // for state caching
  
  /** */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Member cachedMember  = null;

  /** */
  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private               HashMap<String, Subject>  subjectCache  = new HashMap<String, Subject>();
  // TODO 20070531 review lazy-loading to improve consistency + performance
  
  // PRIVATE INSTANCE VARIABLES //
  private Map<String, String>       attributes;
  private long      createTime      = 0; // default to the epoch
  private String    creatorUUID;
  
  private String    modifierUUID;
  private long      modifyTime      = 0; // default to the epoch
  private String    parentUUID;

  @GrouperIgnoreDbVersion 
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Set<GroupType>       types;
  
  private String    uuid;
  /** constant for prefix of field diffs for attributes: attribute__ */
  public static final String ATTRIBUTE_PREFIX = "attribute__";
  
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributes */
  public static final String FIELD_ATTRIBUTES = "attributes";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorUUID */
  public static final String FIELD_CREATOR_UUID = "creatorUUID";

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: modifierUUID */
  public static final String FIELD_MODIFIER_UUID = "modifierUUID";

  /** constant for field name for: modifyTime */
  public static final String FIELD_MODIFY_TIME = "modifyTime";

  /** constant for field name for: parentUUID */
  public static final String FIELD_PARENT_UUID = "parentUUID";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTES, FIELD_CREATE_TIME, FIELD_CREATOR_UUID, 
      FIELD_MODIFIER_UUID, FIELD_MODIFY_TIME, FIELD_PARENT_UUID, 
      FIELD_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTES, FIELD_CREATE_TIME, FIELD_CREATOR_UUID, 
      FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_MODIFIER_UUID, 
      FIELD_MODIFY_TIME, FIELD_PARENT_UUID, FIELD_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  
  /** known built-in attributes */
  private static final  Set<String> ALLOWED_ATTRS = GrouperUtil.toSet(
    GrouperConfig.ATTR_DISPLAY_NAME, GrouperConfig.ATTR_DESCRIPTION, GrouperConfig.ATTR_DISPLAY_EXTENSION,
    GrouperConfig.ATTR_EXTENSION, GrouperConfig.ATTR_NAME);

  
  // PUBLIC CLASS METHODS //
  
  /**
   * Retrieve default members {@link Field}.
   * <pre class="eg">
   * Field members = Group.getDefaultList();
   * </pre>
   * @return  The "members" {@link Field}
   * @throws  GrouperRuntimeException
   */
  public static Field getDefaultList() 
    throws  GrouperRuntimeException
  {
    try {
      return FieldFinder.find(GrouperConfig.LIST);
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public static Field getDefaultList()

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Group.class);


  // PUBLIC INSTANCE METHODS //

  /**
   * Add a composite membership to this group.
   * <pre class="eg">
   * try {
   *   g.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add composite membership
   * } 
   * </pre>
   * @param   type  Add membership of this {@link CompositeType}.
   * @param   left  {@link Group} that is left factor of of composite membership.
   * @param   right {@link Group} that is right factor of composite membership.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @since   1.0
   */
  public void addCompositeMember(CompositeType type, Group left, Group right)
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    try {
      StopWatch sw  = new StopWatch();
      sw.start();

      PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), this, 
          GrouperSession.staticGrouperSession().getSubject(), Group.getDefaultList().getWritePriv() );

      Composite     c   = new Composite();
      c.setCreateTime( new Date().getTime() );
      c.setCreatorUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
      c.setFactorOwnerUuid( this.getUuid() );
      c.setLeftFactorUuid( left.getUuid() );
      c.setRightFactorUuid( right.getUuid() );
      c.setTypeDb( type.toString() );
      c.setUuid( GrouperUuid.getUuid() );
      CompositeValidator vComp = CompositeValidator.validate(c);
      if (vComp.isInvalid()) {
        throw new MemberAddException( vComp.getErrorMessage() );
      }

      AddCompositeMemberValidator vAdd = AddCompositeMemberValidator.validate(this);
      if (vAdd.isInvalid()) {
        throw new MemberAddException( vAdd.getErrorMessage() );
      }

      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addComposite( GrouperSession.staticGrouperSession(), this, c );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      EVENT_LOG.groupAddComposite( GrouperSession.staticGrouperSession(), c, mof, sw );
      sw.stop();
    }
    catch (GrouperDAOException eDAO) {
      throw new MemberAddException( eDAO.getMessage(), eDAO );
    }
    catch (SchemaException eS) {
      throw new MemberAddException(eS);
    }
  } 

  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add subject
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public void addMember(Subject subj) 
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    this.addMember(subj, true);
  } // public void addMember(subj)

  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add subject
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @return false if it already existed, true if it didnt already exist
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public boolean addMember(Subject subj, boolean exceptionIfAlreadyMember) 
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    try {
      Field defaultList = getDefaultList();
      return this.addMember(subj, defaultList, exceptionIfAlreadyMember);
    }
    catch (SchemaException eS) {
      throw new MemberAddException(eS.getMessage(), eS);
    }
  } // public void addMember(subj)

  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj, f);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add member
   * } 
   * catch (SchemaException eS) {
   *   // Invalid Field
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param   f     Add subject to this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @throws  SchemaException
   */
  public void addMember(Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    this.addMember(subj, f, true);
  } // public void addMember(subj, f)

  /**
   * Add a subject to this group as immediate member.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.addMember(subj, f);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add member
   * } 
   * catch (SchemaException eS) {
   *   // Invalid Field
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param   f     Add subject to this {@link Field}.
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean addMember(Subject subj, Field f, boolean exceptionIfAlreadyMember)
    throws  InsufficientPrivilegeException,
            MemberAddException, SchemaException {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !FieldType.LIST.equals( f.getType() ) ) {
      throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    if ( !this.canWriteField(f) ) { 
      GrouperValidator v = CanOptinValidator.validate(this, subj, f);
      if (v.isInvalid()) {
        throw new InsufficientPrivilegeException();
      }
    }
    if ( ( Group.getDefaultList().equals(f) ) && ( this.hasComposite() ) ) {
      throw new MemberAddException(E.GROUP_AMTC);
    }
    boolean doesntExist = true;
    try {
      Membership.internal_addImmediateMembership( GrouperSession.staticGrouperSession(), this, subj, f );
    } catch (MemberAddAlreadyExistsException maaee) {
      if (exceptionIfAlreadyMember) {
        throw maaee;
      }
      doesntExist = false;
    }
    if (doesntExist) {
      EVENT_LOG.groupAddMember(GrouperSession.staticGrouperSession(), this.getName(), subj, f, sw);
    }
    sw.stop();
    return doesntExist;
  } // public void addMember(subj, f)

  /**
   * Add an additional group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.addType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to add type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot add system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @throws  GroupModifyException if unable to add type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to add a system group type.
   */
  public void addType(GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( this.hasType(type ) ) {
      throw new GroupModifyException(E.GROUP_HAS_TYPE);
    }
    if ( type.isSystemType() ) {
      throw new SchemaException("cannot edit system group types");
    }
    if ( !PrivilegeHelper.canAdmin( GrouperSession.staticGrouperSession(), this, 
        GrouperSession.staticGrouperSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
    }
    try {
      Set types = this.getTypesDb();
      types.add( type );

      this.internal_setModified();

      GrouperDAOFactory.getFactory().getGroup().addType( this, type);
      sw.stop();
      EventLog.info(
          GrouperSession.staticGrouperSession(),
        M.GROUP_ADDTYPE + Quote.single(this.getName()) + " type=" + Quote.single( type.getName() ),
        sw
      );
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUP_TYPEADD + type + ": " + eDAO.getMessage();
      LOG.error(msg);
      throw new GroupModifyException(msg, eDAO); 
    }
  } 

  /**
   * Check whether the {@link Subject} that loaded this {@link Group} can
   * read the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canReadField(f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   f   Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can read {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canReadField(Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.canReadField(GrouperSession.staticGrouperSession().getSubject(), f);
  } // public boolean canReadField(f)

  /**
   * Check whether the specified {@link Subject} can read the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canReadField(subj, f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   subj  Check privileges for this {@link Subject}.
   * @param   f     Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can read {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Subject} or {@link Field}
   * @throws  SchemaException if invalid {@link Field} or {@link Subject}.
   * @since   1.2.0
   */
  public boolean canReadField(Subject subj, Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    GrouperValidator v = NotNullValidator.validate(subj);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "subject: " + v.getErrorMessage() );
    }
    v = NotNullValidator.validate(f);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "field: " + v.getErrorMessage() );
    }
    v = FieldTypeValidator.validate(f);
    if (v.isInvalid()) {
      throw new SchemaException( v.getErrorMessage() );
    }
    if ( !this.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.INVALID_GROUP_TYPE + f.getGroupType().toString());
    }
    try {
      PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), this, subj, f.getReadPriv() );
      return true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false;
    }
  } // public boolean canReadField(subj, f)

  /**
   * Check whether the {@link Subject} that loaded this {@link Group} can
   * write the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canWriteField(f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   f   Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can write {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canWriteField(Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.canWriteField(GrouperSession.staticGrouperSession().getSubject(), f);
  } // public boolean canWriteField(f)

  /**
   * Check whether the specified {@link Subject} can write the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canWriteField(subj, f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   subj  Check privileges for this {@link Subject}.
   * @param   f     Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can write {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Subject} or {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canWriteField(Subject subj, Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.internal_canWriteField(subj, f);
  } // public boolean canWriteField(subj, f)

  /**
   * Delete this group from the Groups Registry.
   * <pre class="eg">
   * try {
   *   g.delete();
   * }
   * catch (GroupDeleteException e0) {
   *   // Unable to delete group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this group
   * }
   * </pre>
   * @throws  GroupDeleteException
   * @throws  InsufficientPrivilegeException
   */
  public void delete() 
    throws  GroupDeleteException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    GrouperSession.validate( GrouperSession.staticGrouperSession() );
    if ( !PrivilegeHelper.canAdmin( GrouperSession.staticGrouperSession(), this, 
        GrouperSession.staticGrouperSession().getSubject() ) )
    {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
    }
    try {
      // Revoke all access privs
      this._revokeAllAccessPrivs();
      // ... And delete composite mship if it exists
      if (this.hasComposite()) {
        this.deleteCompositeMember();
      }
      // ... And delete all memberships - as root
      Set deletes = new LinkedHashSet(
        Membership.internal_deleteAllFieldType( GrouperSession.staticGrouperSession().internal_getRootSession(), this, FieldType.LIST )
      );
      //deletes.add(this);            // ... And add the group last for good luck    
      String name = this.getName(); // Preserve name for logging
      GrouperDAOFactory.getFactory().getGroup().delete( this, deletes );
      sw.stop();
      EventLog.info(GrouperSession.staticGrouperSession(), M.GROUP_DEL + Quote.single(name), sw);
    }
    catch (GrouperDAOException eDAO) {
      throw new GroupDeleteException( eDAO.getMessage(), eDAO );
    }
    catch (MemberDeleteException eMD) {
      throw new GroupDeleteException(eMD.getMessage(), eMD);
    }
    catch (RevokePrivilegeException eRP) {
      throw new GroupDeleteException(eRP.getMessage(), eRP);
    }
    catch (SchemaException eS) {
      throw new GroupDeleteException(eS.getMessage(), eS);
    }
  } // public void delete()

  /**
   * Delete a group attribute.
   * <pre class="eg">
   * try {
   *   g.deleteAttribute(attribute);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this attribute
   * }
   * </pre>
   * @param   attr  Delete this attribute.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void deleteAttribute(String attr) 
    throws  AttributeNotFoundException,
            GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      StopWatch sw = new StopWatch();
      sw.start();
      NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(attr);
      if (v.isInvalid()) {
        throw new AttributeNotFoundException(E.INVALID_ATTR_NAME + attr);
      }
      Field f = FieldFinder.find(attr);
      if (f.getRequired()) {
        throw new GroupModifyException( E.GROUP_DRA + f.getName() );
      }
      if ( !this.canWriteField(f) ) {
        throw new InsufficientPrivilegeException();
      }

      Map attrs = this.getAttributesDb();
      if (attrs.containsKey(attr)) {
        String val = (String) attrs.get(attr); // for logging
        attrs.remove(attr);
        this.setAttributes(attrs);
        this.internal_setModified();
        GrouperDAOFactory.getFactory().getGroup().update( this );
        sw.stop();
        EVENT_LOG.groupDelAttr(GrouperSession.staticGrouperSession(), this.getName(), attr, val, sw);
      }
      else {
        throw new AttributeNotFoundException();
      }
    }
    catch (GrouperDAOException eDAO) {
      throw new GroupModifyException( eDAO.getMessage(), eDAO );
    }
    catch (InsufficientPrivilegeException eIP) {
      throw eIP;
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS.getMessage(), eS);
    }
  } // public void deleteAttribute(attr)
  
  /**
   * Delete a {@link Composite} membership from this group.
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <pre class="eg">
   * try {
   *   g.deleteCompositeMember();
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete members
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete composite membership
   * } 
   * </pre>
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @since   1.0
   */
  public void deleteCompositeMember()
    throws  InsufficientPrivilegeException,
            MemberDeleteException
  {
    try {
      StopWatch sw  = new StopWatch();
      sw.start();
      if ( !this.canWriteField( GrouperSession.staticGrouperSession().getSubject(), Group.getDefaultList() ) ) {
        throw new InsufficientPrivilegeException();
      }
      if ( !this.hasComposite() ) {
        throw new MemberDeleteException(E.GROUP_DCFC); 
      }
      Composite c   = GrouperDAOFactory.getFactory().getComposite().findAsOwner( this ) ;
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.deleteComposite( GrouperSession.staticGrouperSession(), this, c );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      EVENT_LOG.groupDelComposite( GrouperSession.staticGrouperSession(), c, mof, sw );
      sw.stop();
    }
    catch (CompositeNotFoundException eCNF) {
      throw new MemberDeleteException(eCNF);
    }
    catch (GrouperDAOException eDAO) {
      throw new MemberDeleteException( eDAO.getMessage(), eDAO );
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS);
    }
  } // public void deleteCompositeMember()

  
  
  /** 
   * Delete a member from this group, and member must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(member);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   member  Delete this {@link Member}
   * @param exceptionIfAlreadyDeleted throw exception if already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @return false if it was already deleted, true if it wasnt already deleted
   */
  public boolean deleteMember(Member member, boolean exceptionIfAlreadyDeleted)
    throws  InsufficientPrivilegeException, MemberDeleteException {
    try {
      return this.deleteMember(member, getDefaultList(), exceptionIfAlreadyDeleted);
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage(), eS);
    }

  }
  
  /** 
   * Delete a member from this group, and member must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(member);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   member  Delete this {@link Member}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public void deleteMember(Member member)
    throws  InsufficientPrivilegeException, MemberDeleteException {
    deleteMember(member, true);
  }

  /** 
   * Delete a member from this group, and member must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   member  Delete this {@link Member}.
   * @param   f     Delete subject from this {@link Field}.
   * @return false if it was already deleted, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public boolean  deleteMember(Member member, Field f, boolean exceptionIfAlreadyDeleted) 
      throws  InsufficientPrivilegeException, MemberDeleteException, SchemaException {
    
    Subject lazySubject = new LazySubject(member);
    
    return deleteMember(lazySubject, f, exceptionIfAlreadyDeleted);
    
  }

  /** 
   * Delete a member from this group, and member must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   member  Delete this {@link Member}.
   * @param   f     Delete subject from this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public void deleteMember(Member member, Field f) 
      throws  InsufficientPrivilegeException, MemberDeleteException, SchemaException {
    deleteMember(member, f, true);
  }

  
  
  /** 
   * Delete a subject from this group, and subject must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(subj);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public void deleteMember(Subject subj)
    throws  InsufficientPrivilegeException,
            MemberDeleteException {
    deleteMember(subj, true);
  }
  
  /** 
   * Delete a subject from this group, and subject must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(subj);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}
   * @return false if it was already deleted, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public boolean deleteMember(Subject subj, boolean exceptionIfAlreadyDeleted)
    throws  InsufficientPrivilegeException,
            MemberDeleteException
  {
    try {
      return this.deleteMember(subj, getDefaultList(), exceptionIfAlreadyDeleted);
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage(), eS);
    }
  }

  /** 
   * Delete a subject from this group, and subject must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}.
   * @param   f     Delete subject from this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public void deleteMember(Subject subj, Field f) 
    throws  InsufficientPrivilegeException, 
            MemberDeleteException,
            SchemaException {
    deleteMember(subj, f, true);
  }
  
  /** 
   * Delete a subject from this group, and subject must be immediate
   * member.  Will not delete the effective membership.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}.
   * @param   f     Delete subject from this {@link Field}.
   * @param exceptionIfAlreadyDeleted true if an exception should be thrown
   * if the member is already deleted
   * @return false if it was already deleted, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public boolean  deleteMember(Subject subj, Field f, boolean exceptionIfAlreadyDeleted) 
    throws  InsufficientPrivilegeException, 
            MemberDeleteException,
            SchemaException
  { boolean notAlreadyDeleted = true;
    StopWatch sw  = new StopWatch();
    sw.start();
    if ( !FieldType.LIST.equals( f.getType() ) ) {
      throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    if ( !this.canWriteField(f) ) {
      GrouperValidator v = CanOptoutValidator.validate(this, subj, f);
      if (v.isInvalid()) {
        throw new InsufficientPrivilegeException();
      }
    }
    if ( (f.equals( Group.getDefaultList() ) ) && ( this.hasComposite() ) ) {
      throw new MemberDeleteException(E.GROUP_DMFC);
    }
    DefaultMemberOf  theMof = null;
    try {
      theMof = Membership.internal_delImmediateMembership( GrouperSession.staticGrouperSession(), this, subj, f );
      final DefaultMemberOf  mof = theMof; 
      
      try {
        GrouperTransaction.callbackGrouperTransaction(
            GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {
  
              public Object callback(GrouperTransaction grouperTransaction)
                  throws GrouperDAOException {
                GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
                    MembershipHooks.METHOD_MEMBERSHIP_PRE_REMOVE_MEMBER,
                    HooksMembershipChangeBean.class, mof, DefaultMemberOf.class, 
                    VetoTypeGrouper.MEMBERSHIP_PRE_REMOVE_MEMBER);
                
                GrouperDAOFactory.getFactory().getMembership().update(mof);
  
                GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
                    MembershipHooks.METHOD_MEMBERSHIP_POST_COMMIT_REMOVE_MEMBER, HooksMembershipChangeBean.class, 
                    mof, DefaultMemberOf.class);
  
                GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
                    MembershipHooks.METHOD_MEMBERSHIP_POST_REMOVE_MEMBER,
                    HooksMembershipChangeBean.class, mof, DefaultMemberOf.class, 
                    VetoTypeGrouper.MEMBERSHIP_POST_REMOVE_MEMBER);
                return null;
              }
              
            });
  
      }
      catch (GrouperDAOException eDAO) {
        throw new MemberDeleteException( eDAO.getMessage(), eDAO );
      }
    } catch (MemberDeleteAlreadyDeletedException mdade) {
      if (exceptionIfAlreadyDeleted) {
        throw mdade;
      }
      notAlreadyDeleted = false;
    }
    sw.stop();
    if (notAlreadyDeleted) {
      EVENT_LOG.groupDelMember(GrouperSession.staticGrouperSession(), this.getName(), subj, f, sw);
      EVENT_LOG.delEffMembers(GrouperSession.staticGrouperSession(), this, subj, f, theMof.getEffectiveDeletes());
      EVENT_LOG.addEffMembers(GrouperSession.staticGrouperSession(), this, subj, f, theMof.getEffectiveSaves());
    }
    return notAlreadyDeleted;
  } // public void deleteMember(subj, f)

  /**
   * Delete a group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.deleteType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to delete type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot delete system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @throws  GroupModifyException if unable to delete type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to delete a system group type.
   */
  public void deleteType(GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    String msg = E.GROUP_TYPEDEL + type + ": "; 
    try {
      if ( !this.hasType(type) ) {
        throw new GroupModifyException("does not have type");
      }
      if ( type.isSystemType() ) {
        throw new SchemaException("cannot edit system group types");
      }
      if ( !PrivilegeHelper.canAdmin( GrouperSession.staticGrouperSession(), this, GrouperSession.staticGrouperSession().getSubject() ) ) {
        throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
      }

      Set types = this.getTypesDb();
      types.remove( type );

      this.internal_setModified();

      GrouperDAOFactory.getFactory().getGroup().deleteType( this, type );
      sw.stop();
      EventLog.info(
        GrouperSession.staticGrouperSession(),
        M.GROUP_DELTYPE + Quote.single(this.getName()) + " type=" + Quote.single( type.getName() ),
        sw
      );
    }
    catch (GrouperDAOException eDAO) {
      msg += eDAO.getMessage();
      LOG.error(msg);
      throw new GroupModifyException(msg, eDAO);
    }
  } 

  /**
   * Get subjects with the ADMIN privilege on this group.
   * <pre class="eg">
   * Set admins = g.getAdmins();
   * </pre>
   * @return  Set of subjects with ADMIN
   * @throws  GrouperRuntimeException
   */
  public Set getAdmins() 
    throws  GrouperRuntimeException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.ADMIN);
  }

  /**
   * get the value of an attribute, if not there return null
   * @param attributeName
   * @return the attribute value
   */
  public String getAttributeOrNull(String attributeName) {
    try {
      return this.getAttribute(attributeName);
    } catch (AttributeNotFoundException anfe) {
      return null;
    }
  }
  
  /**
   * Get attribute value.
   * <pre class="eg">
   * try {
   *   String value = g.getAttribute(attribute);
   * }
   * catch (AttributeNotFoundException e) {
   *   // Group doesn't have attribute
   * }
   * </pre>
   * @param   attr  Get value of this attribute.
   * @return  Attribute value.
   * @throws  AttributeNotFoundException
   */
  public String getAttribute(String attr) 
    throws  AttributeNotFoundException
  {
    String emptyString = GrouperConfig.EMPTY_STRING;

    // check to see if attribute exists in Map returned by getAttributes()
    Map attrs = this.getAttributesDb();
    if (attrs.containsKey(attr)) {
      String val = (String) attrs.get(attr);
      if (val == null) {
        return emptyString;
      } 
      return val;
    }

    // if one of common attributes, allow
    if (ALLOWED_ATTRS.contains(attr)) {
      return emptyString;
    }
    
    // Group does not have attribute.  If attribute is not valid for Group,
    // throw AttributeNotFoundException.  Otherwise, return an empty string.
    
    try {
      Field f = FieldFinder.find(attr); 
      if ( !FieldType.ATTRIBUTE.equals( f.getType() ) ) {
        throw new AttributeNotFoundException( E.FIELD_INVALID_TYPE + f.getType() );
      }
      GrouperValidator v = GetGroupAttributeValidator.validate(this, f);
      if (v.isInvalid()) {
        throw new AttributeNotFoundException( v.getErrorMessage() );
      }
      return emptyString;
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException( eS.getMessage() );
    }
  } // public String getAttribute(attr)

  /**
   * Get all attributes and values.
   * <pre class="eg">
   * Map attributes = g.getAttributes();
   * </pre>
   * @return  A map of attributes and values.
   */
  public Map<String, String> getAttributes() {
    Map       filtered  = new HashMap();
    Map.Entry kv;
    Iterator  it        = this.getAttributesDb().entrySet().iterator();
    while (it.hasNext()) {
      kv = (Map.Entry) it.next();
      if ( this._canReadField( (String) kv.getKey() ) ) {
        filtered.put( (String) kv.getKey(), (String) kv.getValue() );
      }
    }
    return filtered;
  } // public Map getAttributes()

  /**
   * Get {@link Composite} {@link Member}s of this group.
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <pre class="eg">
   * Set members = g.getCompositeMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @since   1.0
   */
  public Set getCompositeMembers() {
    return MembershipFinder.internal_findMembersByType(
      GrouperSession.staticGrouperSession(), this, Group.getDefaultList(), Membership.COMPOSITE
    );
  } // public Set getCompositeMembers()

  /**
   * Get {@link Composite} {@link Membership}s of this group.
   * 
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set mships = g.getCompositeMembers();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @since   1.0
   */
  public Set getCompositeMemberships() {
    return MembershipFinder.internal_findAllByOwnerAndFieldAndType(
      GrouperSession.staticGrouperSession(), this, Group.getDefaultList(), Membership.COMPOSITE
    );
  } // public Set getCompositeMemberships()

  /**
   * Get subject that created this group.
   * <pre class="eg">
   * // Get creator of this group.
   * try {
   *   Subject creator = g.getCreateSubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that created this group.
   * @throws  SubjectNotFoundException
   */
  public Subject getCreateSubject() 
    throws SubjectNotFoundException
  {
    if ( this.subjectCache.containsKey(KEY_CREATOR) ) {
      return this.subjectCache.get(KEY_CREATOR);
    }
    try {
      // when called from "GrouperSubject" there is no attached session
      Member _m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getCreatorUuid() );
      this.subjectCache.put( 
        KEY_CREATOR, SubjectFinder.findById( _m.getSubjectId(), _m.getSubjectTypeId(), _m.getSubjectSourceId() ) 
      );
      return this.subjectCache.get(KEY_CREATOR);
    }
    catch (MemberNotFoundException eMNF) {
      throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
    }
    catch (SourceUnavailableException eSU) {
      throw new SubjectNotFoundException( eSU.getMessage(), eSU );
    }
    catch (SubjectNotUniqueException eSNU) {
      throw new SubjectNotFoundException( eSNU.getMessage(), eSNU );
    }
  } // public Subject getCreateSubject() 
  
  /**
   * Get creation time for this group.
   * <pre class="eg">
   * // Get create time.
   * Date created = g.getCreateTime();
   * </pre>
   * @return  {@link Date} that this group was created.
   */
  public Date getCreateTime() {
    return new Date(this.getCreateTimeLong());
  } // public Date getCreateTime()

  /**
   * Get group description.
   * <pre class="eg">
   * String description = g.getDescription();
   * </pre>
   * @return  Group's <i>description</i> or an empty string if no value set.
   */
  public String getDescription() {
    try {
      return (String) this.getAttribute(GrouperConfig.ATTR_DESCRIPTION);
    }
    catch (AttributeNotFoundException eANF) {
      return GrouperConfig.EMPTY_STRING; // Lack of a description is acceptable
    }
  } 

  /**
   * Get group displayExtension.
   * <pre class="eg">
   * String displayExtn = g.getDisplayExtension();
   * </pre>
   * @return  Gruop displayExtension.
   * @throws  GrouperRuntimeException
   */
  public String getDisplayExtension() 
    throws  GrouperRuntimeException
  {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = (String) this.getAttributesDb().get(GrouperConfig.ATTR_DISPLAY_EXTENSION);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      LOG.fatal(E.GROUP_NODE);
      throw new GrouperRuntimeException(E.GROUP_NODE);
    }
    return val;
  } // public String getDisplayExtension()

  /**
   * Get group displayName.
   * <pre class="eg">
   * String displayName = g.getDisplayName();
   * </pre>
   * @return  Group displayName.
   * @throws  GrouperRuntimeException
   */
  public String getDisplayName() 
    throws  GrouperRuntimeException
  {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = (String) this.getAttributesDb().get(GrouperConfig.ATTR_DISPLAY_NAME);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      LOG.fatal(E.GROUP_NODN);
      throw new GrouperRuntimeException(E.GROUP_NODN);
    }
    return val;
  } // public String getDisplayName()

  /**
   * Get effective members of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getEffectiveMembers() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getEffectiveMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getEffectiveMembership()


  /**
   * Get effective members of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set getEffectiveMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findMembersByType(GrouperSession.staticGrouperSession(), this, f, Membership.EFFECTIVE);
  }  // public Set getEffectiveMembers(f)

  /**
   * Get effective memberships of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set effectives = g.getEffectiveMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getEffectiveMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getEffectiveMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getEffectiveMembership()

  /**
   * Get effective memberships of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set memberships = g.getEffectiveMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getEffectiveMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findAllByOwnerAndFieldAndType(
      GrouperSession.staticGrouperSession(), this, f, Membership.EFFECTIVE
    );
  } // public Set getEffectiveMemberships(f)

  /**
   * Get group extension.
   * <pre class="eg">
   * String extension = g.getExtension();
   * </pre>
   * @return  Group extension.
   * @throws  GrouperRuntimeException
   */
  public String getExtension() {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = (String) this.getAttributesDb().get(GrouperConfig.ATTR_EXTENSION);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      LOG.error( E.GROUP_NOE);
      throw new GrouperRuntimeException(E.GROUP_NOE);
    }
    return val;
  } // public String getExtension()
 
  /**
   * Get immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getImmediateMembers() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getImmediateMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getImmediateMembers()

  /**
   * Get immediate members of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set getImmediateMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findMembersByType(
      GrouperSession.staticGrouperSession(), this, f, Membership.IMMEDIATE
    );
  } // public Set getImmediateMembers(f)

  /**
   * Get immediate memberships of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getImmediateMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getImmediateMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getImmediateMemberships()

  /**
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getImmediateMemberships(Field f) 
    throws  SchemaException
  {
    GrouperSession.validate(GrouperSession.staticGrouperSession());
    return MembershipFinder.internal_findAllByOwnerAndFieldAndType(
      GrouperSession.staticGrouperSession(), this, f, Membership.IMMEDIATE
    );
  } // public Set getImmediateMemberships(f)

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Member> getMembers() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getMembers()

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set<Member> getMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.findMembers(this, f);
  } 

  /**
   * Get memberships of this group.
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set<Membership> getMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getMemberships()

  /**
   * Get memberships of this group.
   * 
   * A membership is the object which represents a join of member
   * and group.  Has metadata like type and creator,
   * and, if an effective membership, the parent membership
   * 
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set<Membership> getMemberships(Field f) 
    throws  SchemaException
  {
    return new LinkedHashSet<Membership>( 
      PrivilegeHelper.canViewMemberships( 
        GrouperSession.staticGrouperSession(), GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField( this.getUuid(), f )
      )
    );
  } // public Set getMemberships(f)

  /**
   * Get subject that last modified this group.
   * <pre class="eg">
   * try {
   *   Subject modifier = g.getModifySubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that last modified this group.
   * @throws  SubjectNotFoundException
   */
  public Subject getModifySubject() 
    throws SubjectNotFoundException
  {
    if ( this.subjectCache.containsKey(KEY_MODIFIER) ) {
      return this.subjectCache.get(KEY_MODIFIER);
    }
    if ( this.getModifierUuid() == null ) {
      throw new SubjectNotFoundException("group has not been modified");
    }
    try {
      // when called from "GrouperSubject" there is no attached session
      Member _m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getModifierUuid() );
      this.subjectCache.put(
        KEY_MODIFIER, SubjectFinder.findById( _m.getSubjectId(), _m.getSubjectTypeId(), _m.getSubjectSourceId() )
      );
      return this.subjectCache.get(KEY_MODIFIER);
    }
    catch (MemberNotFoundException eMNF) {
      throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
    }
    catch (SourceUnavailableException eSU) {
      throw new SubjectNotFoundException( eSU.getMessage(), eSU );
    }
    catch (SubjectNotUniqueException eSNU) {
      throw new SubjectNotFoundException( eSNU.getMessage(), eSNU );
    }  
  } // public Subject getModifySubject()
  
  /**
   * Get last modified time for this group.
   * <pre class="eg">
   * Date modified = g.getModifyTime();
   * </pre>
   * @return  {@link Date} that this group was last modified.
   */
  public Date getModifyTime() {
    return new Date( this.getModifyTimeLong() );
  }

  /**
   * Get group name.
   * <pre class="eg">
   * String name = g.getName();
   * </pre>
   * @return  Group name.
   * @throws  GrouperRuntimeException
   */
  public String getName() 
    throws  GrouperRuntimeException
  {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = (String) this.getAttributesDb().get(GrouperConfig.ATTR_NAME);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      LOG.error( E.GROUP_NON);
      throw new GrouperRuntimeException(E.GROUP_NON);
    }
    return val;
  } // public String getName()

  /**
   * Get subjects with the OPTIN privilege on this group.
   * <pre class="eg">
   * Set optins = g.getOptins();
   * </pre>
   * @return  Set of subjects with OPTIN
   * @throws  GrouperRuntimeException
   */
  public Set getOptins() 
    throws  GrouperRuntimeException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.OPTIN);
  } 

  /**
   * Get subjects with the OPTOUT privilege on this group.
   * <pre class="eg">
   * Set admins = g.getOptouts();
   * </pre>
   * @return  Set of subjects with OPTOUT
   * @throws  GrouperRuntimeException
   */
  public Set getOptouts() 
    throws  GrouperRuntimeException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.OPTOUT);
  } 

  /**
   * Get parent stem.
   * <pre class="eg">
   * Stem parent = g.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   * @throws IllegalStateException 
   */
  public Stem getParentStem() 
    throws  IllegalStateException
  {
    String uuid = this.getParentUuid();
    if (uuid == null) {
      throw new IllegalStateException("group has no parent stem");
    }
    try {
      Stem parent = GrouperDAOFactory.getFactory().getStem().findByUuid(uuid) ;
      return parent;
    }
    catch (StemNotFoundException eShouldNeverHappen) {
      throw new IllegalStateException( 
        "this should never happen: group has no parent stem: " + eShouldNeverHappen.getMessage(), 
        eShouldNeverHappen 
      );
    }
  } // public Stem getParentStem()

  /**
   * Get privileges that the specified subject has on this group.
   * <pre class="eg">
   * Set privs = g.getPrivs(subj);
   * </pre>
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link AccessPrivilege} objects.
   */
  public Set<AccessPrivilege> getPrivs(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().getPrivileges(this, subj);
  } 


  /**
   * Get subjects with the READ privilege on this group.
   * <pre class="eg">
   * Set readers = g.getReaders();
   * </pre>
   * @return  Set of subjects with READ
   * @throws  GrouperRuntimeException
   */
  public Set getReaders() 
    throws  GrouperRuntimeException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.READ);
  } 

  /**
   * Get removable group types for this group.
   * <pre class="eg">
   * Set types = g.getRemovableTypes();
   * </pre>
   * @return  Set of removable group types.
   * @since   1.0
   */
  public Set getRemovableTypes() {
    Set types = new LinkedHashSet();
    // Must have ADMIN to remove types.
    if (PrivilegeHelper.canAdmin(GrouperSession.staticGrouperSession(), this, GrouperSession.staticGrouperSession().getSubject())) {
      GroupType t;
      Iterator  iter  = this.getTypes().iterator();
      while (iter.hasNext()) {
        t = (GroupType) iter.next();
        if ( t.getIsAssignable() ) {
          types.add(t);
        }
      }
    }
    return types;
  } // public Set getRemovableTypes()

  /**
   * Get group types for this group (only non internal ones).
   * <pre class="eg">
   * Set types = g.getTypes();
   * </pre>
   * @return  Set of group types.
   */
  public Set getTypes() {

    Set       newTypes = new LinkedHashSet();
    Iterator  it    = this.getTypesDb().iterator();
    while (it.hasNext()) {
      GroupType groupType = (GroupType) it.next();
      if ( !groupType.getIsInternal() ) {
        newTypes.add(groupType);
      }
    }
    return newTypes;
  } // public Set getTypes()

  /**
   * get types in db
   * @return types
   */
  public Set<GroupType> getTypesDb() {
    if (this.types == null) {
      this.types = GrouperDAOFactory.getFactory().getGroup()._findAllTypesByGroup( this.getUuid() );
    }
    return this.types;
  }
  
  /**
   * Get subjects with the UPDATE privilege on this group.
   * <pre class="eg">
   * Set updaters = g.getUpdaters();
   * </pre>
   * @return  Set of subjects with UPDATE
   * @throws  GrouperRuntimeException
   */
  public Set getUpdaters() 
    throws  GrouperRuntimeException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.UPDATE);
  } 

  /**
   * Get subjects with the VIEW privilege on this group.
   * <pre class="eg">
   * Set viewers = g.getViewers();
   * </pre>
   * @return  Set of subjects with VIEW
   * @throws  GrouperRuntimeException
   */
  public Set getViewers() 
    throws  GrouperRuntimeException
  {
    return GrouperSession.staticGrouperSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.VIEW);
  } 

  /**
   * Grant privilege to a subject on this group.  This
   * will throw an exception if the privilege already exists
   * <pre class="eg">
   * try {
   *   g.grantPriv(subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(Subject subj, Privilege priv)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException {
    
    grantPriv(subj, priv, true);
    
  }
  
  /**
   * Grant privilege to a subject on this group.
   * <pre class="eg">
   * try {
   *   g.grantPriv(subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @param exceptionIfAlreadyMember if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @return false if it already existed, true if it didnt already exist
   */
  public boolean grantPriv(Subject subj, Privilege priv, boolean exceptionIfAlreadyMember)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException {
    StopWatch sw = new StopWatch();
    sw.start();
    boolean assignedPrivilege = false;
    try {
      GrouperSession.staticGrouperSession().getAccessResolver().grantPrivilege(this, subj, priv);
      assignedPrivilege = true;
    } catch (UnableToPerformAlreadyExistsException eUTP) {
      if (exceptionIfAlreadyMember) {
        throw new GrantPrivilegeAlreadyExistsException(eUTP.getMessage(), eUTP);
      }
    } catch (UnableToPerformException eUTP) {
      throw new GrantPrivilegeException( eUTP.getMessage(), eUTP );
    }
    sw.stop();
    if (assignedPrivilege) {
      EVENT_LOG.groupGrantPriv(GrouperSession.staticGrouperSession(), this.getName(), subj, priv, sw);
    }
    return assignedPrivilege;
  } 
  
  /**
   * Check whether the subject has ADMIN on this group.
   * <pre class="eg">
   * if (g.hasAdmin(subj)) {
   *   // Has ADMIN
   * }
   * else {
   *   // Does not have ADMIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ADMIN.
   */
  public boolean hasAdmin(Subject subj) {
    AccessResolver accessResolver = GrouperSession.staticGrouperSession().getAccessResolver();
    return accessResolver.hasPrivilege(this, subj, AccessPrivilege.ADMIN);
  } 

  /**
   * Does this {@link Group} have a {@link Composite} membership.
   * <pre class="eg">
   * if (g.hasComposite()) {
   *   // this group has a composite membership
   * }
   * </pre>
   * @return  Boolean true if group has a composite membership.
   */
  public boolean hasComposite() {
    try {
      GrouperDAOFactory.getFactory().getComposite().findAsOwner( this );
      return true;
    }
    catch (CompositeNotFoundException eCNF) {
      return false;
    }
  } // public boolean hasComposite()

  /**
   * Check whether the subject is an effective member of this group.
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasEffectiveMember(subj)) {
   *   // Subject is an effective member of this group
   * }
   * else {
   *   // Subject is not an effective member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperRuntimeException
   */
  public boolean hasEffectiveMember(Subject subj) 
    throws  GrouperRuntimeException
  {
    try {
      return this.hasEffectiveMember(subj, getDefaultList());
    } 
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public boolean hasEffectiveMember(Subject subj)

  /**
   * Check whether the subject is an effective member of this group.  
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a
   * composite group are effective members (since the composite
   * group has two groups and a set operator and no other immediate
   * members).  Note that a member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * 'group within a group' can be nested to any level so long as it does 
   * not become circular.  A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasEffectiveMember(subj, f)) {
   *   // Subject is an effective member of this group
   * }
   * else {
   *   // Subject is not an effective member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group. 
   * @throws  SchemaException
   */
  public boolean hasEffectiveMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj);
      rv = m.isEffectiveMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      LOG.error( E.GROUP_HEM + eMNF.getMessage());
    }
    return rv;
  } // public boolean hasEffectiveMember(subj, f)

  /**
   * Check whether the subject is an immediate member of this group.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasImmediateMember(subj)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperRuntimeException
   */
  public boolean hasImmediateMember(Subject subj) 
    throws  GrouperRuntimeException
  {
    try {
      return this.hasImmediateMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public boolean hasImmediateMember(subj)

  /**
   * Check whether the subject is an immediate member of this group.
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <pre class="eg">
   * if (g.hasImmediateMember(subj, f)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasImmediateMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj);
      rv = m.isImmediateMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      LOG.error( E.GROUP_HIM + eMNF.getMessage());
    }
    return rv;
  } // public boolean hasImmediateMember(subj, f)

  /**
   * Check whether the subject is a member of this group.
   * 
   * All immediate subjects, and effective members are members.  
   * No duplicates will be returned (e.g. if immediate and effective).
   * 
   * <pre class="eg">
   * if (g.hasMember(subj)) {
   *   // Subject is a member of this group
   * }
   * else {
   *   // Subject is not a member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperRuntimeException
   */
  public boolean hasMember(Subject subj) 
    throws  GrouperRuntimeException
  {
    try {
      return this.hasMember(subj, getDefaultList());
    }
    catch (SchemaException eShouldNeverHappen) {
      // If we don't have "members" we have serious issues
      String msg = "this should never happen: default group list not found: " + eShouldNeverHappen.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen);
    }
  } // public boolean hasMember(subj)

  /**
   * Check whether the subject is a member of this list on this group.
   * 
   * All immediate subjects, and effective members are members.  
   * No duplicates will be returned (e.g. if immediate and effective).
   * 
   * <pre class="eg">
   * if (g.hasMember(subj, f)) {
   *   // Subject is a member of this group
   * }
   * else {
   *   // Subject is not a member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Is subject a member of this list {@link Field}.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subj);
      rv = m.isMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      LOG.error( E.GROUP_HM + eMNF.getMessage());
    }
    return rv;
  } // public boolean hasMember(subj, f)

  /**
   * Check whether the subject has OPTIN on this group.
   * <pre class="eg">
   * if (g.hasOptin(subj)) {
   *   // Has OPTIN
   * }
   * else {
   *   // Does not have OPTIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has OPTIN.
   */
  public boolean hasOptin(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.OPTIN);
  }

  /**
   * Check whether the subject has OPTOUT on this group.
   * <pre class="eg">
   * if (g.hasOptout(subj)) {
   *   // has OPTOUT
   * }
   * else {
   *   // Does not have OPTOUT
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has OPTOUT.
   */
  public boolean hasOptout(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.OPTOUT);
  } 

  /**
   * Check whether the subject has READ on this group.
   * <pre class="eg">
   * if (g.hasRead(subj)) {
   *   // Has READ
   * }
   * else {
   *   // Does not have READ
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has READ.
   */
  public boolean hasRead(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.READ);
  } 

  /**
   * Check whether group has the specified type.
   * <pre class="eg">
   * GroupType custom = GroupTypeFinder.find("custom type");
   * if (g.hasType(custom)) {
   *   // Group has type
   * }
   * </pre>
   * @param   type  The {@link GroupType} to check.
   * @return if has type
   */
  public boolean hasType(GroupType type) {
    return this.getTypesDb().contains( type );
  } // public boolean hasType(type)

  /**
   * Check whether the subject has UPDATE on this group.
   * <pre class="eg">
   * if (g.hasUpdate(subj)) {
   *   // Has UPDATE
   * }
   * else {
   *   // Does not have UPDATE
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has UPDATE.
   */
  public boolean hasUpdate(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.UPDATE);
  } 

  /**
   * Check whether the subject has VIEW on this group.
   * <pre class="eg">
   * if (g.hasView(subj)) {
   *   // Has VIEW
   * }
   * else {
   *   // Does not have VIEW
   * }
   * </pre>
   * @param   subj  Check this member.
   * @return  Boolean true if subject has VIEW.
   */
  public boolean hasView(Subject subj) {
    return GrouperSession.staticGrouperSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.VIEW);
  } 

  /**
   * Is this {@link Group} a factor in a {@link Composite} membership.
   * <pre class="eg">
   * if (g.isComposite()) {
   *   // this group is a factor in one-or-more composite memberships.
   * }
   * </pre>
   * @return  Boolean true if group is a factor in a composite membership.
   */
  public boolean isComposite() {
    if ( GrouperDAOFactory.getFactory().getComposite().findAsFactor( this ).size() > 0 ) {
      return true;
    }
    return false;
  } // public boolean isComposite()

  /**
   * Revoke all privileges of the specified type on this group.
   * <pre class="eg">
   * try {
   *   g.revokePriv(AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to modify group
   * }
   * </pre>
   * @param   priv  Revoke all instances of this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( Privilege.isNaming(priv) ) {
      throw new SchemaException("attempt to use naming privilege");
    }
    try {
      GrouperSession.staticGrouperSession().getAccessResolver().revokePrivilege(this, priv);
    }
    catch (UnableToPerformException eUTP) {
      throw new RevokePrivilegeException( eUTP.getMessage(), eUTP );
    }
    sw.stop();
    EVENT_LOG.groupRevokePriv(GrouperSession.staticGrouperSession(), this.getName(), priv, sw);
  } 

  /**
   * Revoke a privilege from the specified subject.
   * <pre class="eg">
   * try {
   *   g.revokePriv(subj, AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Subject subj, Privilege priv) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    revokePriv(subj, priv, true);
  }
  
  /**
   * Revoke a privilege from the specified subject.
   * <pre class="eg">
   * try {
   *   g.revokePriv(subj, AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.
   * @param exceptionIfAlreadyRevoked if false, and subject is already a member,
   * then dont throw a MemberAddException if the member is already in the group
   * @return false if it was already revoked, true if it wasnt already deleted
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public boolean revokePriv(Subject subj, Privilege priv, 
      boolean exceptionIfAlreadyRevoked) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, SchemaException {
    boolean wasntAlreadyRevoked = true;
    StopWatch sw = new StopWatch();
    sw.start();
    if ( Privilege.isNaming(priv) ) {
      throw new SchemaException("attempt to use naming privilege");
    }
    try {
      GrouperSession.staticGrouperSession().getAccessResolver().revokePrivilege(this, subj, priv);
    } catch (UnableToPerformAlreadyExistsException eUTP) {
      if (exceptionIfAlreadyRevoked) {
        throw new RevokePrivilegeAlreadyRevokedException( eUTP.getMessage(), eUTP );
      }
      wasntAlreadyRevoked = false;
    } catch (UnableToPerformException eUTP) {
      throw new RevokePrivilegeException( eUTP.getMessage(), eUTP );
    }
    sw.stop();
    if (wasntAlreadyRevoked) {
      EVENT_LOG.groupRevokePriv(GrouperSession.staticGrouperSession(), this.getName(), subj, priv, sw);
    }
    return wasntAlreadyRevoked;
  } 

  /**
   * Set an attribute value.
   * Note, you have to call store() at some point to 
   * make the kick off the sql
   * <pre class="eg">
   * try {
   *   g.attribute(attribute, value);
   * } 
   * catch (AttributeNotFoundException e0) {
   *   // Attribute doesn't exist
   * }
   * catch (GroupModifyException e1) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e2) {
   *   // Not privileged to modify this attribute
   * }
   * </pre>
   * @param   attr  Set this attribute.
   * @param   value Set to this value.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setAttribute(String attr, String value) 
    throws  AttributeNotFoundException, 
            GroupModifyException, 
            InsufficientPrivilegeException
  {
    this.setAttributeHelper(attr, value);

  }
  
  /**
   * store this object to the DB.  If
   * grouper.setters.dont.cause.queries is false, then this is a no-op
   */
  public void store() {
    if (GrouperConfig.getPropertyBoolean(GrouperConfig.PROP_SETTERS_DONT_CAUSE_QUERIES, false)) {
      GrouperDAOFactory.getFactory().getGroup().update( this );
    }
  }
  
  /**
   * store this object to the DB
   */
  private void storeIfConfiguredToStore() {
    if (!GrouperConfig.getPropertyBoolean(GrouperConfig.PROP_SETTERS_DONT_CAUSE_QUERIES, false)) {
      GrouperDAOFactory.getFactory().getGroup().update( this );
    }
  }
  
  /**
   * Set an attribute value.
   * <pre class="eg">
   * try {
   *   g.attribute(attribute, value);
   * } 
   * catch (AttributeNotFoundException e0) {
   *   // Attribute doesn't exist
   * }
   * catch (GroupModifyException e1) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e2) {
   *   // Not privileged to modify this attribute
   * }
   * </pre>
   * @param   attr  Set this attribute.
   * @param   value Set to this value.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  private void setAttributeHelper(String attr, String value) 
    throws  AttributeNotFoundException, 
            GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      StopWatch sw = new StopWatch();
      sw.start();
      Field f = FieldFinder.find(attr);
      if ( !FieldType.ATTRIBUTE.equals( f.getType() ) ) {
        throw new AttributeNotFoundException( E.FIELD_INVALID_TYPE + f.getType() );
      }

      // TODO 20070531 split and test
      GrouperValidator v = NotNullOrEmptyValidator.validate(attr);
      if (v.isInvalid()) {
        throw new AttributeNotFoundException(E.INVALID_ATTR_NAME + attr);
      }
      v = NotNullOrEmptyValidator.validate(value);
      if (v.isInvalid()) {
        throw new GroupModifyException(E.INVALID_ATTR_VALUE + value);
      }
      if (
            attr.equals(GrouperConfig.ATTR_DISPLAY_EXTENSION)
        ||  attr.equals(GrouperConfig.ATTR_DISPLAY_NAME)
        ||  attr.equals(GrouperConfig.ATTR_EXTENSION)
      )
      {
        v = NamingValidator.validate(value);
        if (v.isInvalid()) {
          throw new GroupModifyException( v.getErrorMessage() );
        }
      }
      if ( !this.canWriteField( FieldFinder.find(attr) ) ) {
        throw new InsufficientPrivilegeException();
      }

      Map attrs = this.getAttributesDb();
      attrs.put(attr, value);
      if      ( attr.equals(GrouperConfig.ATTR_EXTENSION) )   {
        attrs.put( GrouperConfig.ATTR_NAME, U.constructName( this.getParentStem().getName(), value ) );
      }
      else if ( attr.equals(GrouperConfig.ATTR_DISPLAY_EXTENSION) )  {
        attrs.put( GrouperConfig.ATTR_DISPLAY_NAME, U.constructName( this.getParentStem().getDisplayName(), value ) );
      }
      this.setAttributes(attrs);
      this.internal_setModified();
      this.storeIfConfiguredToStore();
      sw.stop();
      EVENT_LOG.groupSetAttr(GrouperSession.staticGrouperSession(), this.getName(), attr, value, sw);
    }
    catch (GrouperDAOException eDAO) {
      throw new GroupModifyException( eDAO.getMessage(), eDAO );
    }
    catch (InsufficientPrivilegeException eIP) {
      throw eIP;
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS.getMessage(), eS);
    }
  } // public void setAttribute(attr, value)

  /**
   * Set group description.  
   * Note, you have to call store() at some point to 
   * make the kick off the sql
   * <pre class="eg">
   * try {
   *   g.setDescription(value);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to modify description
   * }
   * </pre>
   * @param   value   Set description to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setDescription(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttributeHelper(GrouperConfig.ATTR_DESCRIPTION, value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set description: " + eANF.getMessage(), eANF
      );
    }
  } // public void setDescription(value)
 
  /**
   * Set group <i>extension</i>.
   * Note, you have to call store() at some point to 
   * make the kick off the sql
   * <pre class="eg">
   * try {
   *   g.setExtension(value);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to modify group
   * }
   * </pre>
   * @param   value   Set <i>extension</i> to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setExtension(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttributeHelper(GrouperConfig.ATTR_EXTENSION, value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set extension: " + eANF.getMessage(), eANF
      );
    }
  } // public void setExtension(value)

  /**
   * Set group displayExtension.
   * Note, you have to call store() at some point to 
   * make the kick off the sql
   * <pre class="eg">
   * try {
   *   g.setDisplayExtension(value);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to modify displayExtension
   * }
   * </pre>
   * @param   value   Set displayExtension to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setDisplayExtension(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttributeHelper(GrouperConfig.ATTR_DISPLAY_EXTENSION, value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set displayExtension: " + eANF.getMessage(), eANF
      );
    }
  } // public void setDisplayExtension(value)

  /**
   * Convert this group to a {@link Member} object.
   * <p/>
   * <pre class="eg">
   * Member m = g.toMember();
   * </pre>
   * @return  {@link Group} as a {@link Member}
   * @throws  GrouperRuntimeException
   */
  public Member toMember() 
    throws  GrouperRuntimeException
  {
    if ( this.cachedMember != null ) {
      return this.cachedMember;
    }
    try {
      GrouperSession.validate( GrouperSession.staticGrouperSession() );
      Member m = GrouperDAOFactory.getFactory().getMember().findBySubject( this.toSubject() );
      GrouperSession.staticGrouperSession();
      this.cachedMember = m;
      return this.cachedMember;
    }  
    catch (MemberNotFoundException eMNF) {
      // If we can't convert a group to a member we have major issues
      // and should probably just give up
      String msg = E.GROUP_G2M + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eMNF);
    }
  } // public Member toMember()

  /**
   * Convert this group to a {@link Subject} object.
   * <p/>
   * <pre class="eg">
   * Subject subj = g.toSubject();
   * </pre>
   * @return  {@link Group} as a {@link Subject}
   * @throws  GrouperRuntimeException
   */
  public Subject toSubject() 
    throws  GrouperRuntimeException
  {
    if ( this.subjectCache.containsKey(KEY_SUBJECT) ) {
      return this.subjectCache.get(KEY_SUBJECT);
    }
    try {
      this.subjectCache.put(
        KEY_SUBJECT, SubjectFinder.findById( this.getUuid(), "group", SubjectFinder.internal_getGSA().getId() )
      );
      return this.subjectCache.get(KEY_SUBJECT);
    }
    catch (SourceUnavailableException eShouldNeverHappen0)  {
      String msg = E.GROUP_G2S + eShouldNeverHappen0.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen0);
    }
    catch (SubjectNotFoundException eShouldNeverHappen1)    {
      String msg = E.GROUP_G2S + eShouldNeverHappen1.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen1);
    }
    catch (SubjectNotUniqueException eShouldNeverHappen2)   {
      String msg = E.GROUP_G2S + eShouldNeverHappen2.getMessage();
      LOG.fatal(msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen2);
    }
  } // public Subject toSubject()

  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( GrouperConfig.ATTR_NAME, (String) this.getAttributesDb().get(GrouperConfig.ATTR_NAME) )
      .append( "uuid", this.getUuid() )
      .toString();
  } // public String toString()


  /**
   * TODO 20070531 make into some flavor of validator
   * @param subj 
   * @param f 
   * @return  boolean
   * @throws IllegalArgumentException 
   * @throws SchemaException 
   * @since   1.2.1
   */
  public boolean internal_canWriteField(Subject subj, Field f)
    throws  IllegalArgumentException,
            SchemaException
  {
    GrouperValidator v = NotNullValidator.validate(subj);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "subject: " + v.getErrorMessage() );
    } 
    v = NotNullValidator.validate(f);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "field: " + v.getErrorMessage() );
    }
    v = FieldTypeValidator.validate(f);
    if (v.isInvalid()) {
      throw new SchemaException( v.getErrorMessage() );
    }
    if ( !this.hasType( f.getGroupType() ) ) {
      throw new SchemaException( E.INVALID_GROUP_TYPE + f.getGroupType().toString() );
    }
    try {
      PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), this, subj, f.getWritePriv() );
      return true;
    }
    catch (InsufficientPrivilegeException eIP) {
      //eIP.printStackTrace();
      return false;
    }
  }

  // @since   1.2.0 
  // i dislike this method
  /**
   * 
   */
  public void internal_setModified() {
    this.setModifierUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
    this.setModifyTimeLong( new Date().getTime() );
  } // protected void internal_setModified()


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  /**
   * @param name 
   * @return if can read field
   */
  private boolean _canReadField(String name) {
    boolean rv = false;
    try {
      PrivilegeHelper.dispatch( GrouperSession.staticGrouperSession(), this, GrouperSession.staticGrouperSession().getSubject(), FieldFinder.find(name).getReadPriv() );
      rv = true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false ;
    }
    catch (SchemaException eS) {
      return false;
    }
    return rv;
  } // private boolean _canReadField(name)

  /**
   * 
   * @throws InsufficientPrivilegeException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   */
  private void _revokeAllAccessPrivs() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException {

    try {
      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            Group.this.revokePriv(AccessPrivilege.ADMIN);
            Group.this.revokePriv(AccessPrivilege.OPTIN);
            Group.this.revokePriv(AccessPrivilege.OPTOUT);
            Group.this.revokePriv(AccessPrivilege.READ);
            Group.this.revokePriv(AccessPrivilege.UPDATE);
            Group.this.revokePriv(AccessPrivilege.VIEW);
          } catch (InsufficientPrivilegeException ipe) {
            throw new GrouperSessionException(ipe);
          } catch (SchemaException ipe) {
            throw new GrouperSessionException(ipe);
          } catch (RevokePrivilegeException ipe) {
            throw new GrouperSessionException(ipe);
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)gse.getCause();
      }
      if (gse.getCause() instanceof SchemaException) {
        throw (SchemaException)gse.getCause();
      }
      if (gse.getCause() instanceof RevokePrivilegeException) {
        throw (RevokePrivilegeException)gse.getCause();
      }
      throw gse;
    }


  } // private void _revokeAllAccessPrivs()

  
  /**
   * note, these are massaged so that name, extension, etc look like normal fields.
   * access with fieldValue()
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    return this.dbVersionDifferentFields(true);
  }

  /**
   * @param renameCommonAttributes is true if these are massaged so that name, extension, etc look like normal fields.
   * access with fieldValue()
   * @return the set of different fields
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  public Set<String> dbVersionDifferentFields(boolean renameCommonAttributes) {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, ATTRIBUTE_PREFIX);
    if (renameCommonAttributes) {
      if (result.contains("attribute__name")) {
        result.remove("attribute__name");
        result.add("name");
      }
      if (result.contains("attribute__displayName")) {
        result.remove("attribute__displayName");
        result.add("displayName");
      }
      if (result.contains("attribute__extension")) {
        result.remove("attribute__extension");
        result.add("extension");
      }
      if (result.contains("attribute__displayExtension")) {
        result.remove("attribute__displayExtension");
        result.add("displayExtension");
      }
      if (result.contains("attribute__description")) {
        result.remove("attribute__description");
        result.add("description");
      }
    }
    return result;
  }

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public Group clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * @since   1.2.0
   */  
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Group)) {
      return false;
    }
    return new EqualsBuilder()
      .append( this.getName(), ( (Group) other ).getName() )
      .isEquals();
  } // public boolean equals(other)

  /**
   * This is a direct access to the attributes (going around security checking etc)
   * @since   1.2.0
   */
  public Map<String, String> getAttributesDb() {
    if (this.attributes == null) {
      this.attributes = GrouperDAOFactory.getFactory().getGroup().findAllAttributesByGroup( this.getUuid() );
    }
    return this.attributes;
  }

  /**
   * @since   1.2.0
   */
  public long getCreateTimeLong() {
    return this.createTime;
  }

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public Group dbVersion() {
    return (Group)this.dbVersion;
  }

  /**
   * @since   1.2.0
   */
  public String getModifierUuid() {
    return this.modifierUUID;
  }

  /**
   * @since   1.2.0
   */
  public long getModifyTimeLong() {
    return this.modifyTime;
  }

  /**
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getName() )
      .toHashCode();
  } // public int hashCode()

  @Override
  public boolean onDelete(Session hs) 
    throws  CallbackException {
    GrouperDAOFactory.getFactory().getGroup().putInExistsCache( this.getUuid(), false );
    return Lifecycle.NO_VETO;
  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    GrouperDAOFactory.getFactory().getGroup()._updateAttributes(hibernateSession, false, this);
    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_COMMIT_INSERT, HooksGroupBean.class, 
        this, Group.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_INSERT, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_POST_INSERT, true, false);

  }

  /**
   * @see edu.internet2.middleware.grouper.hibernate.HibGrouperLifecycle#onPostUpdate(HibernateSession)
   */
  public void onPostUpdate(HibernateSession hibernateSession) {
    GrouperDAOFactory.getFactory().getGroup()._updateAttributes(hibernateSession, true, this);
    
    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_COMMIT_UPDATE, HooksGroupBean.class, 
        this, Group.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_UPDATE, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_POST_UPDATE, true, false);


  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_COMMIT_DELETE, HooksGroupBean.class, 
        this, Group.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_POST_DELETE, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_POST_DELETE, false, true);

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_PRE_INSERT, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_PRE_INSERT, false, false);
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onSave(org.hibernate.Session)
   */
  public boolean onSave(Session hs) throws  CallbackException {
    GrouperDAOFactory.getFactory().getGroup().putInExistsCache( this.getUuid(), true );
    return Lifecycle.NO_VETO;
  }

  /**
   * 
   * @param attributes
   */
  public void setAttributes(Map attributes) {
    this.attributes = attributes;

  }

  /**
   * @since   1.2.0
   */
  public void setCreateTimeLong(long createTime) {
    this.createTime = createTime;

  }

  /**
   * @since   1.2.0
   */
  public void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  
  }

  /**
   * @since   1.2.0
   */
  public void setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;

  }

  /**
   * @since   1.2.0
   */
  public void setModifyTimeLong(long modifyTime) {
    this.modifyTime = modifyTime;

  }

  /**
   * @since   1.2.0
   */
  public void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;

  }

  /**
   * @since   1.2.0
   */
  public void setTypes(Set types) {
    this.types = types;

  }

  /**
   * @since   1.2.0
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;

  }

  /**
   * @since   1.2.0
   */
  public String toStringDb() {
    return new ToStringBuilder(this)
      .append( "attributes",   this.getAttributesDb()   )
      .append( "createTime",   this.getCreateTimeLong()   )
      .append( "creatorUuid",  this.getCreatorUuid()  )
      .append( "modifierUuid", this.getModifierUuid() )
      .append( "modifyTime",   this.getModifyTime()   )
      .append( "ownerUuid",    this.getUuid()         )
      .append( "parentUuid",   this.getParentUuid()   )
      .append( "types",        this.getTypesDb()        )
      .toString();
  } // public String toString()

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_PRE_DELETE, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_PRE_DELETE, false, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#fieldValue(java.lang.String)
   */
  @Override
  public Object fieldValue(String fieldName) {
    return this.fieldValue(fieldName, true);
  }
  
  /**
   * @param fieldName 
   * @param renameCommonAttributes 
   * @return  the value
   * @see edu.internet2.middleware.grouper.GrouperAPI#fieldValue(java.lang.String)
   */
  public Object fieldValue(String fieldName, boolean renameCommonAttributes) {
    if (renameCommonAttributes) {
      //make the commons ones a little more friendly
      if (StringUtils.equals("attribute__name", fieldName) 
          || StringUtils.equals("name", fieldName)) {
        return this.attributes.get("name");
      }
      if (StringUtils.equals("attribute__extension", fieldName) 
          || StringUtils.equals("extension", fieldName)) {
        return this.attributes.get("extension");
      }
      if (StringUtils.equals("attribute__displayName", fieldName) 
          || StringUtils.equals("displayName", fieldName)) {
        return this.attributes.get("displayName");
      }
      if (StringUtils.equals("attribute__displayExtension", fieldName) 
          || StringUtils.equals("displayExtension", fieldName)) {
        return this.attributes.get("displayExtension");
      }
      if (StringUtils.equals("attribute__description", fieldName) 
          || StringUtils.equals("description", fieldName)) {
        return this.attributes.get("description");
      }
    }
    
    if (fieldName.startsWith(Group.ATTRIBUTE_PREFIX)) {
      fieldName = fieldName.substring(Group.ATTRIBUTE_PREFIX.length(), fieldName.length());
      //take from attributes including common ones
      return this.getAttributesDb().get(fieldName);
      
    }
    
    return super.fieldValue(fieldName);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP, 
        GroupHooks.METHOD_GROUP_PRE_UPDATE, HooksGroupBean.class, 
        this, Group.class, VetoTypeGrouper.GROUP_PRE_UPDATE, false, false);

  }

} // public class Group extends GrouperAPI implements Owner

