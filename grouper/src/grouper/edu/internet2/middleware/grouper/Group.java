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
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;

import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.internal.util.U;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/** 
 * A group within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.181.2.1 2008-06-07 16:11:55 mchyzer Exp $
 */
public class Group extends GrouperAPI implements Owner {

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
   * @param grouperSession to act as
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
  public static Group saveGroup(final GrouperSession grouperSession, final String groupNameToEdit,
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
          try {
            String groupNameForError = GrouperUtil.defaultIfBlank(groupNameToEdit, name);
            
            int lastColonIndex = name.lastIndexOf(':');
            boolean topLevelGroup = lastColonIndex < 0;
    
            //empty is root stem
            String parentStemNameNew = GrouperUtil.parentStemNameFromName(name);
            String extensionNew = GrouperUtil.extensionFromName(name);
            
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
                throw new StemNotFoundException("Cant find stem: '" + parentStemNameNew 
                    + "' (from update on stem name: '" + groupNameForError + "')");
              }
            }
            
            Group theGroup = null;
            //see if update
            boolean isUpdate = SAVE_MODE.isUpdate(groupNameToEdit);
    
            if (isUpdate) {
              String parentStemNameLookup = GrouperUtil.parentStemNameFromName(groupNameToEdit);
              if (!StringUtils.equals(parentStemNameLookup, parentStemNameNew)) {
                throw new GroupModifyException("Can't move a group.  Existing parentStem: '"
                    + parentStemNameLookup + "', new stem: '" + parentStemNameNew + "'");
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
                    throw gnfe;
                }
              }
            }
            
            //if inserting
            if (!isUpdate) {
              if (StringUtils.isBlank(uuid)) {
                try {
                  //if no uuid
                  theGroup = parentStem.addChildGroup(extensionNew, displayExtension);
                } catch (GroupAddException gae) {
                  //here for debugging
                  throw gae;
                }
              } else {
                //if uuid
                theGroup = parentStem.internal_addChildGroup(extensionNew, displayExtension, uuid);
              }
            } else {
              //check if different so it doesnt make unneeded queries
              if (!StringUtils.equals(theGroup.getExtension(), extensionNew)) {
                theGroup.setExtension(extensionNew);
                theGroup.store();
              }
              if (!StringUtils.equals(theGroup.getDisplayExtension(), displayExtension)) {
                theGroup.setDisplayExtension(displayExtension);
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
          } catch (GroupNotFoundException gnfe) {
            throw new RuntimeException(gnfe);
          } catch (GroupAddException gae) {
            throw new RuntimeException(gae);
          }
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
  private               Member                    cachedMember  = null;
  /** */
  private               HashMap<String, Subject>  subjectCache  = new HashMap<String, Subject>();
  /** known built-in attributes */
  private static final  Set<String>               ALLOWED_ATTRS = GrouperUtil.toSet(
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
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public static Field getDefaultList()

 
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

      PrivilegeHelper.dispatch( this.getSession(), this, this.getSession().getSubject(), Group.getDefaultList().getWritePriv() );

      Composite     c   = new Composite();
      CompositeDTO  _c  = new CompositeDTO()
        .setCreateTime( new Date().getTime() )
        .setCreatorUuid( this.getSession().getMember().getUuid() )
        .setFactorOwnerUuid( this._getDTO().getUuid() )
        .setLeftFactorUuid( left._getDTO().getUuid() )
        .setRightFactorUuid( right._getDTO().getUuid() )
        .setType( type.toString() )
        .setUuid( GrouperUuid.getUuid() )
        ;
      CompositeValidator vComp = CompositeValidator.validate(_c);
      if (vComp.isInvalid()) {
        throw new MemberAddException( vComp.getErrorMessage() );
      }
      c.setDTO(_c);
      c.setSession( this.getSession() );

      AddCompositeMemberValidator vAdd = AddCompositeMemberValidator.validate(this);
      if (vAdd.isInvalid()) {
        throw new MemberAddException( vAdd.getErrorMessage() );
      }

      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addComposite( this.getSession(), this, c );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      EventLog.groupAddComposite( this.getSession(), c, mof, sw );
      Composite.internal_update(this);
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
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public void addMember(Subject subj, boolean exceptionIfAlreadyMember) 
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    //CH 20080301: if not want an exception, and already a member, then exit normally
    if (!exceptionIfAlreadyMember && this.hasMember(subj)) {
      return;
    }
    try {
      this.addMember(subj, getDefaultList());
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
   */
  public void addMember(Subject subj, Field f, boolean exceptionIfAlreadyMember)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
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
    //CH 20080301: if not want an exception, and already a member, then exit normally
    if (!exceptionIfAlreadyMember && this.hasMember(subj, f)) {
      return;
    }
    Membership.internal_addImmediateMembership( this.getSession(), this, subj, f );
    EVENT_LOG.groupAddMember(this.getSession(), this.getName(), subj, f, sw);
    Composite.internal_update(this);
    sw.stop();
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
    if ( !PrivilegeHelper.canAdmin( this.getSession(), this, this.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
    }
    try {
      Set types = this._getDTO().getTypes();
      types.add( type.getDTO() );
      this._getDTO().setTypes(types);

      this.internal_setModified();

      GrouperDAOFactory.getFactory().getGroup().addType( this._getDTO(), (GroupTypeDTO) type.getDTO() );
      sw.stop();
      EventLog.info(
        this.getSession(),
        M.GROUP_ADDTYPE + Quote.single(this.getName()) + " type=" + Quote.single( type.getName() ),
        sw
      );
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUP_TYPEADD + type + ": " + eDAO.getMessage();
      ErrorLog.error(Group.class, msg);
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
    return this.canReadField(this.getSession().getSubject(), f);
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
      PrivilegeHelper.dispatch( this.getSession(), this, subj, f.getReadPriv() );
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
    return this.canWriteField(this.getSession().getSubject(), f);
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
    GrouperSession.validate( this.getSession() );
    if ( !PrivilegeHelper.canAdmin( this.getSession(), this, this.getSession().getSubject() ) )
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
        Membership.internal_deleteAllFieldType( this.getSession().internal_getRootSession(), this, FieldType.LIST )
      );
      //deletes.add(this);            // ... And add the group last for good luck    
      String name = this.getName(); // Preserve name for logging
      GrouperDAOFactory.getFactory().getGroup().delete( this._getDTO(), deletes );
      sw.stop();
      EventLog.info(this.getSession(), M.GROUP_DEL + Quote.single(name), sw);
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

      Map attrs = this._getDTO().getAttributes();
      if (attrs.containsKey(attr)) {
        String val = (String) attrs.get(attr); // for logging
        attrs.remove(attr);
        this._getDTO().setAttributes(attrs);
        this.internal_setModified();
        GrouperDAOFactory.getFactory().getGroup().update( this._getDTO() );
        sw.stop();
        EVENT_LOG.groupDelAttr(this.getSession(), this.getName(), attr, val, sw);
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
      if ( !this.canWriteField( this.getSession().getSubject(), Group.getDefaultList() ) ) {
        throw new InsufficientPrivilegeException();
      }
      if ( !this.hasComposite() ) {
        throw new MemberDeleteException(E.GROUP_DCFC); 
      }
      Composite c   = new Composite();
      c.setDTO( GrouperDAOFactory.getFactory().getComposite().findAsOwner( this._getDTO() ) );
      DefaultMemberOf  mof = new DefaultMemberOf();
      mof.deleteComposite( this.getSession(), this, c );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      EventLog.groupDelComposite( this.getSession(), c, mof, sw );
      Composite.internal_update(this);
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
            MemberDeleteException
  {
    try {
      this.deleteMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage(), eS);
    }
  } // public void deleteMember(subj)

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
            SchemaException
  {
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
    DefaultMemberOf  mof = Membership.internal_delImmediateMembership( this.getSession(), this, subj, f );
    try {
      GrouperDAOFactory.getFactory().getMembership().update(mof);
    }
    catch (GrouperDAOException eDAO) {
      throw new MemberDeleteException( eDAO.getMessage(), eDAO );
    }
    sw.stop();
    EVENT_LOG.groupDelMember(this.getSession(), this.getName(), subj, f, sw);
    EVENT_LOG.delEffMembers(this.getSession(), this, subj, f, mof.getEffectiveDeletes());
    Composite.internal_update(this);
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
      if ( !PrivilegeHelper.canAdmin( this.getSession(), this, this.getSession().getSubject() ) ) {
        throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
      }

      Set types = this._getDTO().getTypes();
      types.remove( type.getDTO() );
      this._getDTO().setTypes(types);

      this.internal_setModified();

      GrouperDAOFactory.getFactory().getGroup().deleteType( this._getDTO(), (GroupTypeDTO) type.getDTO() );
      sw.stop();
      EventLog.info(
        this.getSession(),
        M.GROUP_DELTYPE + Quote.single(this.getName()) + " type=" + Quote.single( type.getName() ),
        sw
      );
    }
    catch (GrouperDAOException eDAO) {
      msg += eDAO.getMessage();
      ErrorLog.error(Group.class, msg);
      throw new GroupModifyException(msg, eDAO);
    }
  } 

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Group)) {
      return false;
    }
    return this.getDTO().equals( ( (Group) other ).getDTO() );
  } // public boolean equals(other)

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
    return this.getSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.ADMIN);
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
    Map attrs = this._getDTO().getAttributes();
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
  public Map getAttributes() {
    Map       filtered  = new HashMap();
    Map.Entry kv;
    Iterator  it        = this._getDTO().getAttributes().entrySet().iterator();
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
      this.getSession(), this, Group.getDefaultList(), Membership.COMPOSITE
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
      this.getSession(), this, Group.getDefaultList(), Membership.COMPOSITE
    );
  } // public Set getCompositeMemberships()

  /**
   * Get (optional and questionable) create source for this group.
   * <pre class="eg">
   * // Get create source
`  * String source = g.getCreateSource();
   * </pre>
   * @return  Create source for this group.
   */
  public String getCreateSource() {
    return GrouperConfig.EMPTY_STRING;
  } // public String getCreateSource()
  
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
      MemberDTO _m = GrouperDAOFactory.getFactory().getMember().findByUuid( this._getDTO().getCreatorUuid() );
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
    return new Date(this._getDTO().getCreateTime());
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
    String val = (String) this._getDTO().getAttributes().get(GrouperConfig.ATTR_DISPLAY_EXTENSION);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      ErrorLog.fatal(Group.class, E.GROUP_NODE);
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
    String val = (String) this._getDTO().getAttributes().get(GrouperConfig.ATTR_DISPLAY_NAME);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      ErrorLog.fatal(Group.class, E.GROUP_NODN);
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
      ErrorLog.fatal(Group.class, msg);
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
    return MembershipFinder.internal_findMembersByType(this.getSession(), this, f, Membership.EFFECTIVE);
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
      ErrorLog.fatal(Group.class, msg);
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
      this.getSession(), this, f, Membership.EFFECTIVE
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
    String val = (String) this._getDTO().getAttributes().get(GrouperConfig.ATTR_EXTENSION);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      ErrorLog.error(Group.class, E.GROUP_NOE);
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
      ErrorLog.fatal(Group.class, msg);
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
      this.getSession(), this, f, Membership.IMMEDIATE
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
      ErrorLog.fatal(Group.class, msg);
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
    GrouperSession.validate(this.getSession());
    return MembershipFinder.internal_findAllByOwnerAndFieldAndType(
      this.getSession(), this, f, Membership.IMMEDIATE
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
  public Set getMembers() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
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
  public Set getMembers(Field f) 
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
  public Set getMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
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
  public Set getMemberships(Field f) 
    throws  SchemaException
  {
    return new LinkedHashSet( 
      PrivilegeHelper.canViewMemberships( 
        this.getSession(), GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField( this.getUuid(), f )
      )
    );
  } // public Set getMemberships(f)

  /**
   * Get (optional and questionable) modify source for this group.
   * <pre class="eg">
`  * String source = g.getModifySource();
   * </pre>
   * @return  Modify source for this group.
   */
  public String getModifySource() {
    return GrouperConfig.EMPTY_STRING;
  } // public String getModifySource()
  
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
    if ( this._getDTO().getModifierUuid() == null ) {
      throw new SubjectNotFoundException("group has not been modified");
    }
    try {
      // when called from "GrouperSubject" there is no attached session
      MemberDTO _m = GrouperDAOFactory.getFactory().getMember().findByUuid( this._getDTO().getModifierUuid() );
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
    return new Date( this._getDTO().getModifyTime() );
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
    String val = (String) this._getDTO().getAttributes().get(GrouperConfig.ATTR_NAME);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      ErrorLog.error(Group.class, E.GROUP_NON);
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
    return this.getSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.OPTIN);
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
    return this.getSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.OPTOUT);
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
    String uuid = this._getDTO().getParentUuid();
    if (uuid == null) {
      throw new IllegalStateException("group has no parent stem");
    }
    try {
      Stem parent = new Stem();
      parent.setDTO( GrouperDAOFactory.getFactory().getStem().findByUuid(uuid) );
      parent.setSession( this.getSession() );
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
    return this.getSession().getAccessResolver().getPrivileges(this, subj);
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
    return this.getSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.READ);
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
    if (PrivilegeHelper.canAdmin(this.s, this, this.s.getSubject())) {
      GroupType t;
      Iterator  iter  = this.getTypes().iterator();
      while (iter.hasNext()) {
        t = (GroupType) iter.next();
        if ( ( (GroupTypeDTO) t.getDTO() ).getIsAssignable() ) {
          types.add(t);
        }
      }
    }
    return types;
  } // public Set getRemovableTypes()

  /**
   * Get group types for this group.
   * <pre class="eg">
   * Set types = g.getTypes();
   * </pre>
   * @return  Set of group types.
   */
  public Set getTypes() {
    Set       types = new LinkedHashSet();
    Iterator  it    = this._getDTO().getTypes().iterator();
    while (it.hasNext()) {
      GroupTypeDTO dto = (GroupTypeDTO) it.next();
      if ( !dto.getIsInternal() ) {
        GroupType type = new GroupType();
        type.setDTO(dto);
        types.add(type);
      }
    }
    return types;
  } // public Set getTypes()

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
    return this.getSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.UPDATE);
  } 

  /**
   */
  public String getUuid() {
    return this._getDTO().getUuid();
  } // public String getUuid()

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
    return this.getSession().getAccessResolver().getSubjectsWithPrivilege(this, AccessPrivilege.VIEW);
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
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(Subject subj, Privilege priv)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    try {
      this.getSession().getAccessResolver().grantPrivilege(this, subj, priv);
    }
    catch (UnableToPerformException eUTP) {
      throw new GrantPrivilegeException( eUTP.getMessage(), eUTP );
    }
    sw.stop();
    EVENT_LOG.groupGrantPriv(this.getSession(), this.getName(), subj, priv, sw);
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
    return this.getSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.ADMIN);
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
      GrouperDAOFactory.getFactory().getComposite().findAsOwner( this._getDTO() );
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
      ErrorLog.fatal(Group.class, msg);
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
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isEffectiveMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      ErrorLog.error(Group.class, E.GROUP_HEM + eMNF.getMessage());
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
      ErrorLog.fatal(Group.class, msg);
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
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isImmediateMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      ErrorLog.error(Group.class, E.GROUP_HIM + eMNF.getMessage());
    }
    return rv;
  } // public boolean hasImmediateMember(subj, f)

  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

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
      ErrorLog.fatal(Group.class, msg);
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
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      ErrorLog.error(Group.class, E.GROUP_HM + eMNF.getMessage());
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
    return this.getSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.OPTIN);
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
    return this.getSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.OPTOUT);
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
    return this.getSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.READ);
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
    return this._getDTO().getTypes().contains( type.getDTO() );
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
    return this.getSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.UPDATE);
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
    return this.getSession().getAccessResolver().hasPrivilege(this, subj, AccessPrivilege.VIEW);
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
    if ( GrouperDAOFactory.getFactory().getComposite().findAsFactor( this._getDTO() ).size() > 0 ) {
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
      this.getSession().getAccessResolver().revokePrivilege(this, priv);
    }
    catch (UnableToPerformException eUTP) {
      throw new RevokePrivilegeException( eUTP.getMessage(), eUTP );
    }
    sw.stop();
    EVENT_LOG.groupRevokePriv(this.getSession(), this.getName(), priv, sw);
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
    StopWatch sw = new StopWatch();
    sw.start();
    if ( Privilege.isNaming(priv) ) {
      throw new SchemaException("attempt to use naming privilege");
    }
    try {
      this.getSession().getAccessResolver().revokePrivilege(this, subj, priv);
    }
    catch (UnableToPerformException eUTP) {
      throw new RevokePrivilegeException( eUTP.getMessage(), eUTP );
    }
    sw.stop();
    EVENT_LOG.groupRevokePriv(this.getSession(), this.getName(), subj, priv, sw);
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
   * store this object to the DB
   */
  public void store() {
    GrouperDAOFactory.getFactory().getGroup().update( this._getDTO() );
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

      Map attrs = this._getDTO().getAttributes();
      attrs.put(attr, value);
      if      ( attr.equals(GrouperConfig.ATTR_EXTENSION) )   {
        attrs.put( GrouperConfig.ATTR_NAME, U.constructName( this.getParentStem().getName(), value ) );
      }
      else if ( attr.equals(GrouperConfig.ATTR_DISPLAY_EXTENSION) )  {
        attrs.put( GrouperConfig.ATTR_DISPLAY_NAME, U.constructName( this.getParentStem().getDisplayName(), value ) );
      }
      this._getDTO().setAttributes(attrs);
      this.internal_setModified();
      sw.stop();
      EVENT_LOG.groupSetAttr(this.getSession(), this.getName(), attr, value, sw);
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
      GrouperSession.validate( this.getSession() );
      Member m = new Member();
      m.setDTO( GrouperDAOFactory.getFactory().getMember().findBySubject( this.toSubject() ) );
      m.setSession( this.getSession() );
      this.cachedMember = m;
      return this.cachedMember;
    }  
    catch (MemberNotFoundException eMNF) {
      // If we can't convert a group to a member we have major issues
      // and should probably just give up
      String msg = E.GROUP_G2M + eMNF.getMessage();
      ErrorLog.fatal(Group.class, msg);
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
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen0);
    }
    catch (SubjectNotFoundException eShouldNeverHappen1)    {
      String msg = E.GROUP_G2S + eShouldNeverHappen1.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen1);
    }
    catch (SubjectNotUniqueException eShouldNeverHappen2)   {
      String msg = E.GROUP_G2S + eShouldNeverHappen2.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen2);
    }
  } // public Subject toSubject()

  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( GrouperConfig.ATTR_NAME, (String) this._getDTO().getAttributes().get(GrouperConfig.ATTR_NAME) )
      .append( "uuid", this.getUuid() )
      .toString();
  } // public String toString()


  /**
   * TODO 20070531 make into some flavor of validator
   * @param subj 
   * @param f 
   * @return 
   * @throws IllegalArgumentException 
   * @throws SchemaException 
   * @since   1.2.1
   */
  protected boolean internal_canWriteField(Subject subj, Field f)
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
      PrivilegeHelper.dispatch( this.getSession(), this, subj, f.getWritePriv() );
      return true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false;
    }
  }

  // @since   1.2.0 
  // i dislike this method
  /**
   * 
   */
  protected void internal_setModified() {
    this._getDTO().setModifierUuid( this.getSession().getMember().getUuid() );
    this._getDTO().setModifyTime( new Date().getTime() );
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
      PrivilegeHelper.dispatch( this.getSession(), this, this.getSession().getSubject(), FieldFinder.find(name).getReadPriv() );
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

  // @since   1.2.0
  /**
   * @return 
   * 
   */
  private GroupDTO _getDTO() {
    return (GroupDTO) super.getDTO();
  }
  
  /**
   * 
   * @throws InsufficientPrivilegeException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   */
  private void _revokeAllAccessPrivs() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException
  {
    GrouperSession orig = this.getSession();
    this.setSession( orig.internal_getRootSession() ); // proxy as root

    this.revokePriv(AccessPrivilege.ADMIN);
    this.revokePriv(AccessPrivilege.OPTIN);
    this.revokePriv(AccessPrivilege.OPTOUT);
    this.revokePriv(AccessPrivilege.READ);
    this.revokePriv(AccessPrivilege.UPDATE);
    this.revokePriv(AccessPrivilege.VIEW);

    this.setSession(orig);
  } // private void _revokeAllAccessPrivs()

} // public class Group extends GrouperAPI implements Owner

