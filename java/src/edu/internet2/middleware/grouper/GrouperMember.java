/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;


/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.44 2004-11-23 19:43:26 blair Exp $
 */
public class GrouperMember {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String memberKey;
  private String subjectID;
  private String subjectTypeID;


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new {@link GrouperMember} object.
   */
  public GrouperMember() {
    this._init();
  }

  /**
   * Create a new {@link GrouperMember} object.
   *
   * @param subjectID       Subject ID
   * @param subjectTypeID   Subject Type ID
   */
  public GrouperMember(String subjectID, String subjectTypeID) {
    this._init();
    this.subjectID      = subjectID;
    this.subjectTypeID  = subjectTypeID;
  }


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Convert a {@link Subject} object to a {@link GrouperMember}
   * object.
   * <p>
   * 
   * @param   subj  A {@link Subject} object.
   * @return  A {@link GrouperMember} object.
   */
  public static GrouperMember lookup(Subject subj) {
    // Attempt to load an already existing member
    GrouperMember member = GrouperBackend.member(
                                                 subj.getId(),
                                                 subj.getSubjectType().getId()
                                                );
    /*
     * We have an already existing member.  Return the un-Hibernated
     * object.
     */
    if (member != null) { return member; }

    /*
     * If the subject is valid but a matching member object does not
     * exist, create a new one, assign a key, etc.
     */ 
    // TODO Is there a reason why I am not just passing in the
    //      `subjectID' and `subjectTypeID' passed as params to
    //      this method?
    member = new GrouperMember(
                               subj.getId(),
                               subj.getSubjectType().getId()
                              );
    // Give it a UUID
    member.setMemberKey( GrouperBackend.uuid() );

    // Hibernate and return the member
    member = GrouperBackend.memberAdd(member);
  
    return member;
  }

  /**
   * Convert a {@link Subject} id and type to a {@link GrouperMember}
   * object.
   * <p>
   * TODO Is this method needed?  Or can I just default to the version
   *      that takes a {@link Subject} object?
   *
   * @param   subjectID     Subject ID
   * @param   subjectTypeID Subject Type ID
   * @return  A {@link GrouperMember} object
   */
  public static GrouperMember lookup(String subjectID, String subjectTypeID) {
    Subject subj = GrouperSubject.lookup(subjectID, subjectTypeID);

    /*
     * If no subject is returned via the subject interface, assume that
     * the member either doesn't exist or should no longer exist.  Bail
     * out.
     */
    // TODO What if member can be found but matching subject cannot be
    //      found?  Or: should I check if the member is defined first
    //      and then fall back to subject?
    if (subj == null)  { return null; }

    return GrouperMember.lookup(subj);
  }

  /**
   * Retrieve a {@link GrouperMember} object based upon its
   * <i>memberKey</i>.
   * <p>
   * TODO This method should not remain, at least it in its current
   *      form.  At the least it should be made <i>protected</i>.
   *
   * @param   subjectID     Subject ID
   * @param   subjectTypeID Subject Type ID
   * @param   key   <i>memberKey</i> of {@link GrouperMember} object to
   * retrieve.
   * @return  A {@link GrouperMember} object
   */


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Return Subject ID.
   *
   * @return Subject ID of the {@link GrouperMember}
   */
  public String id() {
    return this.getSubjectID();
  }

  /**
   * Return Member Key.
   * <p />
   * FIXME REMOVE, REPLACE, REFACTOR
   *
   * @return Member key of the {@link GrouperMember}
   */
  public String key() {
    return this.getMemberKey();
  }

  /**
   * Return all group memberships of default list type for this 
   * {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List listVals(GrouperSession s) {
    return GrouperBackend.listVals(s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Return all group memberships of the specified type for 
   * this {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @param   list    Type of list membership to query on.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List listVals(GrouperSession s, String list) {
    return GrouperBackend.listVals(s, this, list);
  }

  /**
   * Return all effective group memberships of default list type for
   * this {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List listEffVals(GrouperSession s) {
    return GrouperBackend.listEffVals(s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Return all effective group memberships of the specified type 
   * for this {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @param   list    Type of list membership to query on.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List listEffVals(GrouperSession s, String list) {
    return GrouperBackend.listEffVals(s, this, list);
  }

  /**
   * Return all immediate group memberships of default list type for 
   * this {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List listImmVals(GrouperSession s) {
    return GrouperBackend.listImmVals(s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Return all immediate group memberships of the specified type 
   * for this {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @param   list    Type of list membership to query on.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List listImmVals(GrouperSession s, String list) {
    return GrouperBackend.listImmVals(s, this, list);
  }


  public String toString() {
    // TODO Remove key
    return this.getClass().getName()  + ":" +
           this.key()                 + ":" +
           this.typeID()              + ":" +
           this.id();
  }

  /**
   * Return Subject Type ID
   *
   * @return Subject Type ID of {@link GrouperMember}.
   */
  public String typeID() {
    return this.getSubjectTypeID();
  }


  /*
   * PRIVATE INSTANCE MTHODS
   */

  /*
   * Initialize instance variables.
   */
  private void _init() {
    this.memberKey      = null;
    this.subjectID      = null;
    this.subjectTypeID  = null;
  }


  /*
   * HIBERNATE
   */

  private String getSubjectID() {
    return this.subjectID;
  }

  private void setSubjectID(String subjectID) {
    this.subjectID = subjectID;
  }

  private String getMemberKey() {
    return this.memberKey;
  }

  private void setMemberKey(String key) {
    this.memberKey = key;
  }

  private String getSubjectTypeID() {
    return this.subjectTypeID;
  }

  private void setSubjectTypeID(String subjectTypeID) {
    this.subjectTypeID = subjectTypeID;
  }

}

