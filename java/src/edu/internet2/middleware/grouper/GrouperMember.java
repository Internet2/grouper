/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class representing a {@link Grouper} member.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.52 2004-12-06 02:10:28 blair Exp $
 */
public class GrouperMember {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String memberID;
  private String memberKey;
  private String subjectID;
  private String subjectTypeID;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperMember() {
    this._init();
  }

  /**
   * Construct a new {@link GrouperMember} object.
   * <p />
   * This should <b>only</b> be used within this class.
   */
  private GrouperMember(String subjectID, String subjectTypeID) {
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
  public static GrouperMember load(Subject subj) {
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
    // Give it a private UUID
    member.setMemberKey( GrouperBackend.uuid() );
    // Give it a public UUID
    member.setMemberID(  GrouperBackend.uuid() );

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
  public static GrouperMember load(String subjectID, String subjectTypeID) {
    Subject subj = GrouperSubject.load(subjectID, subjectTypeID);

    /*
     * If no subject is returned via the subject interface, assume that
     * the member either doesn't exist or should no longer exist.  Bail
     * out.
     */
    // TODO What if member can be found but matching subject cannot be
    //      found?  Or: should I check if the member is defined first
    //      and then fall back to subject?
    if (subj == null)  { return null; }

    return GrouperMember.load(subj);
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

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

  /**
   * Return member ID.
   *
   * @return Member ID of the {@link GrouperMember}
   */
  public String memberID() {
    return this.getMemberID();
  }

  /**
   * Return subject ID.
   *
   * @return Subject ID of the {@link GrouperMember}
   */
  public String subjectID() {
    return this.getSubjectID();
  }

  /**
   * Return a string representation of the {@link GrouperSchema}
   * object.
   * <p />
   * @return String representation of the object.
   */
  public String toString() {
    return new ToStringBuilder(this)                    .
      append("memberID"     , this.getMemberID()      ) .
      append("subjectTypeID", this.getSubjectTypeID() ) .
      append("subjectID"    , this.getSubjectID()     ) .
      toString();
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
   * PROTECTED INSTANCE METHODS
   */

  /**
   * Return member key.
   * <p />
   * FIXME Can I eventually make this private?
   *
   * @return Member key of the {@link GrouperMember}
   */
  protected String key() {
    return this.getMemberKey();
  }


  /*
   * PRIVATE INSTANCE MTHODS
   */

  /*
   * Initialize instance variables.
   */
  private void _init() {
    this.memberID       = null;
    this.memberKey      = null;
    this.subjectID      = null;
    this.subjectTypeID  = null;
  }


  /*
   * HIBERNATE
   */

  private String getMemberID() {
    return this.memberID;
  }

  private void setMemberID(String memberID) {
    this.memberID = memberID;
  }

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

