/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
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
 * Class modeling a {@link Grouper} list value member.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.60 2005-03-07 19:30:41 blair Exp $
 */
public class GrouperMember {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String          memberID;
  private String          memberKey;
  private GrouperSession  s;
  private String          subjectID;
  private String          subjectTypeID;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperMember() {
    this._init();
  }

  /* (!javadoc)
   * This should <b>only</b> be used within this class.
   */
  private GrouperMember(GrouperSession s, String subjectID, String subjectTypeID) {
    this._init();
    this.s              = s;
    this.subjectID      = subjectID;
    this.subjectTypeID  = subjectTypeID;
  }


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Retrieve a {@link GrouperMember} object.
   * <p />
   * This method will create a new entry in the <i>grouper_member</i>
   * table if this subject does not already have an entry.
   * 
   * @param   s     Load {@link GrouperMember} within this session.
   * @param   subj  A {@link Subject} object.
   * @return  A {@link GrouperMember} object.
   */
  public static GrouperMember load(GrouperSession s, Subject subj) {
    // Attempt to load an already existing member
    GrouperMember member = GrouperBackend.member(
                             subj.getId(), subj.getSubjectType().getId()
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
                               s, subj.getId(),
                               subj.getSubjectType().getId()
                              );
    // Give it a private UUID
    member.setMemberKey( GrouperBackend.uuid() );
    // Give it a public UUID
    member.setMemberID(  GrouperBackend.uuid() );

    // Hibernate and return the member
    member = GrouperBackend.memberAdd(member);

    Grouper.log().memberAdd(member, subj);
    return member;
  }

  /**
   * Retrieve a {@link GrouperMember} object.
   * <p />
   * This method will create a new entry in the <i>grouper_member</i>
   * table if this subject does not already have an entry.
   *
   * @param   s             Load {@link GrouperMember} within this session.
   * @param   subjectID     Subject ID
   * @param   subjectTypeID Subject Type ID
   * @return  A {@link GrouperMember} object
   */
  public static GrouperMember load(
                                GrouperSession s, String subjectID, 
                                String subjectTypeID
                              ) 
  {
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

    return GrouperMember.load(s, subj);
  }

  /**
   * Retrieve a {@link GrouperMember} object.
   * <p />
   * This method will throw a runtime exception if the specified 
   * <i>memberID</i> does not exist.
   *
   * @param   s         Load {@link GrouperMember} within this session.
   * @param   memberID  ID of member to retrieve.
   * @return  A {@link GrouperMember} object
   */
  public static GrouperMember load(GrouperSession s, String memberID) {
    // Attempt to load an already existing member
    // TODO Why am I not using the constructor?
    GrouperMember member = GrouperBackend.memberByID(memberID);
    if (member == null) {
      throw new RuntimeException(
                                 "memberID " + memberID + 
                                 " does not exist!"
                                );
    }
    member.s = s;
    return member;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Retrieve group memberships of the default list type for this member.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals() {
    return GrouperBackend.listVals(this.s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve group memberships of the specified type for this member.
   * <p />
   * @param   list    Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals(String list) {
    return GrouperBackend.listVals(this.s, this, list);
  }

  /**
   * Retrieve effective group memberships of the default list type for
   * this member.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals() {
    return GrouperBackend.listEffVals(this.s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve effective group memberships of the specified type for
   * this member.
   * <p />
   * @param   list    Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals(String list) {
    return GrouperBackend.listEffVals(this.s, this, list);
  }

  /**
   * Retrieve immediate group memberships of the default list type for
   * this member.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals() {
    return GrouperBackend.listImmVals(this.s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve immediate group memberships of the specified type for
   * this member.
   * <p />
   * @param   list    Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals(String list) {
    return GrouperBackend.listImmVals(this.s, this, list);
  }

  /**
   * Retrieve member's public GUID.
   * <p />
   * @return  Public GUID.
   */
  public String memberID() {
    return this.getMemberID();
  }

  /**
   * Retrieve member's I2MI {@link Subject} id.
   * <p />
   * @return  Subject ID of member.
   */
  public String subjectID() {
    return this.getSubjectID();
  }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    return new ToStringBuilder(this)                    .
      append("memberID"     , this.getMemberID()      ) .
      append("subjectTypeID", this.getSubjectTypeID() ) .
      append("subjectID"    , this.getSubjectID()     ) .
      toString();
  }

  /**
   * Retrieve member's I2MI {@link Subject} type.
   * <p />
   * @return  Subject TypeID of this member.
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
    this.s              = null;
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

