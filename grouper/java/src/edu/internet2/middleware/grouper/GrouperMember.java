/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;


/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.34 2004-11-16 02:00:47 blair Exp $
 */
public class GrouperMember {

  // What we need to identify a member
  private String memberKey;
  private String subjectID;
  private String subjectTypeID;


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
   * TODO
   *
   * @param   subjectID       Subject ID
   * @param   subjectTypeID  Subject Type ID
   * @return  {@link GrouperMember} object
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

    // TODO Is there a reason why I am not just passing in the
    //      `subjectID' and `subjectTypeID' passed as params to
    //      this method?
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

  public String toString() {
    return this.getClass().getName()  + ":" +
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

