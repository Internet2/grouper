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
 * @version $Id: GrouperMember.java,v 1.32 2004-11-12 21:54:41 blair Exp $
 */
public class GrouperMember {

  // What we need to identify a member
  private String id;
  private String key;
  private String type;
  private String typeID;

  /**
   * Create a new {@link GrouperMember} object.
   */
  public GrouperMember() {
    this._init();
  }

  /**
   * Create a new {@link GrouperMember} object.
   *
   * @param id    Member ID
   * @param type  Member Type
   */
  public GrouperMember(String id, String type) {
    this._init();
    this.id   = id;
    this.type = type;
  }


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * TODO
   *
   * @param   id      Subject ID
   * @param   typeID  Subject Type ID
   * @return  {@link GrouperMember} object
   */
  public static GrouperMember lookup(String id, String typeID) {
    Subject subj = GrouperSubject.lookup(id, typeID);
    /*
     * If no subject is returned via the subject interface, assume that
     * the member either doesn't exist or should no longer exist.  Bail
     * out.
     */
    // TODO What if member can be found but matching subject cannot be
    //      found?  Or: should I check if the member is defined first
    //      and then fall back to subject?
    if (subj == null)  { return null; }

    // TODO Is there a reason why I am not just passing in the `id' and
    //      `typeID' passed as params to this method?
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
    member = new GrouperMember(
                                subj.getId(),
                                subj.getSubjectType().getId()
                              );
    if (member != null) {
      // Give the member a key and then store Hibernate it
      member.setMemberKey( GrouperBackend.uuid() );
    }

    return member;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Return Member ID.
   *
   * @return Member ID of the {@link GrouperMember}
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
           this.type()                + ":" +
           this.id();
  }

  /**
   * Return Member Type.
   *
   * @return Member Type of {@link GrouperMember}.
   */
  public String type() {
    return this.getMemberType();
  }

  /**
   * Return Member Type ID
   *
   * @return Member Type ID of {@link GrouperMember}.
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
    this.id     = null;
    this.key    = null;
    this.type   = null;
    this.typeID = null;
  }


  /*
   * HIBERNATE
   */

  private String getSubjectID() {
    return this.id;
  }

  private void setSubjectID(String id) {
    this.id = id;
  }

  private String getMemberKey() {
    return this.key;
  }

  private void setMemberKey(String key) {
    this.key = key;
  }

  private String getMemberType() {
    return this.type;
  }

  private void setMemberType(String type) {
    this.type = type;
  }

  private String getSubjectTypeID() {
    return this.typeID;
  }

  private void setSubjectTypeID(String typeID) {
    this.typeID = typeID;
  }

}

