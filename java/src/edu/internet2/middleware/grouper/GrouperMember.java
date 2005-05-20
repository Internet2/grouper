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


import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class modeling a {@link Grouper} list value member.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.85 2005-05-20 15:31:22 blair Exp $
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
    // Nothing
  }

  /* 
   * Create a member with assigned id and type.
   */
  protected GrouperMember(String subjectID, String subjectTypeID) {
    // Give it a public UUID
    this.setMemberID(  new GrouperUUID().toString() );
    // Give it a private UUID
    this.setMemberKey( new GrouperUUID().toString() );
    this.subjectID      = subjectID;
    this.subjectTypeID  = subjectTypeID;
  }

  /*
   * Create member with assigned session, id and type.
   */
  private GrouperMember(GrouperSession s, String subjectID, String subjectTypeID) {
    this(subjectID, subjectTypeID);
    this.s = s;
  }


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Retrieve a {@link GrouperMember} object.
   * <p />
   * @param   s     Load {@link Subject} within this {@link GrouperSession} 
   *  session.
   * @param   subj  {@link Subject} to load as {@link GrouperMember}.
   * @return {@link GrouperMember} object.
   */
  public static GrouperMember load(GrouperSession s, Subject subj) { 
    GrouperMember m = GrouperMember.load(subj);
    if (m != null) { // TODO Bah
      m.s = s;
    }
    return m;
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
    // TODO This displeases me
    String        qry = "GrouperMember.by.id";
    GrouperMember m   = new GrouperMember();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, memberID);
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          m = (GrouperMember) vals.get(0);
          m.s = s;
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                   "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    } 
    if (m == null) {
      throw new RuntimeException("Error loading member " + memberID);
    }
    return m;
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
    GrouperSession.validate(s);
    GrouperMember m     = null;
    Subject       subj  = SubjectFactory.load(subjectID, subjectTypeID);
    if (subj != null) {
      m = GrouperMember.load(subj);
      if (m != null) { // TODO Bah
        m.s = s;
      }
    }
    return m;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Check whether this member belongs to a specific group.
   * <p />
   * @param   g   Check membership in this {@link Group}
   * @return  boolean true if it is a member
   */
  public boolean isMember(Group g) {
    return this.isMember(g, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Check whether this member belongs to a specific group.
   * <p />
   * @param   g     Check membership in this {@link Group}
   * @param   list  Check membership in this list.
   * @return  boolean true if it is a member
   */
  public boolean isMember(Group g, String list) {
    String  qry = "GrouperList.as.key.by.group.and.member.and.list";
    boolean rv  = false;
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key()); 
      q.setString(1, this.key());
      q.setString(2, list);
      try {
        if (q.list().size() == 1) {
          rv = true;
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return rv;
  }

  /**
   * Retrieve group memberships of the default list type for this member.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals() {
    return this.listVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve group memberships of the specified type for this member.
   * <p />
   * @param   list    Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals(String list) {
    String  qry = "GrouperList.by.member.and.list";
    return this.queryListVals(qry, list);
  }

  /**
   * Retrieve effective group memberships of the default list type for
   * this member.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals() {
    return this.listEffVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve effective group memberships of the specified type for
   * this member.
   * <p />
   * @param   list    Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals(String list) {
    String  qry = "GrouperList.by.member.and.list.and.is.eff";
    return this.queryListVals(qry, list);
  }

  /**
   * Retrieve immediate group memberships of the default list type for
   * this member.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals() {
    return this.listImmVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve immediate group memberships of the specified type for
   * this member.
   * <p />
   * @param   list    Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals(String list) {
    String  qry = "GrouperList.by.member.and.list.and.is.imm";
    return this.queryListVals(qry, list);
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
   * Retrieve {@link Group} object for this * {@link GrouperMember}.
   * </p>
   * @return {@link Group} object
   */
  public Group toGroup() {
    GrouperSession.validate(this.s);
    Group g = Group._loadByID(this.s, this.getSubjectID());
    if (g == null) {
      throw new RuntimeException("Error converting member to group");
    }
    return g;
  }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    String subjID = this.getSubjectID();
    if (this.getSubjectTypeID().equals("group")) {
      Group g = Group._loadByID(this.s, subjID);
      subjID = g.name();
    }
    return new ToStringBuilder(this)                    .
      append("memberID"     , this.getMemberID()      ) .
      append("subjectID"    , subjID                  ) .
      append("subjectTypeID", this.getSubjectTypeID() ) .
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
   * PROTECTED CLASS METHODS
   */

  /*
   * Create and save a new {@link GrouperMember} object.
   */
  protected static GrouperMember create(
                                   GrouperSession s, String subjectID, 
                                   String subjectTypeID
                                 ) 
  {
    GrouperMember m = new GrouperMember(s, subjectID, subjectTypeID);
    m.save(s.dbSess());
    return m;
  }

  /*
   * Load, and if necessary create, a GrouperMember
   */
  protected static GrouperMember load(Subject subj) {
    // Attempt to load an already existing member
    GrouperMember m = loadByIdAndType(Grouper.dbSess(), subj);
    if (m == null) {
      /*
       * If the subject is valid but a matching member object does not
       * exist, create a new one.
       */ 
      m = new GrouperMember(
                subj.getId(), subj.getSubjectType().getId()
              );
      // Save the new member object
      m.save(Grouper.dbSess());
      Grouper.log().memberAdd(m, subj);
    } 
    return m;
  }

  /*
   * Load {@link GrouperMember} by key.
   */
  protected static GrouperMember loadByKey(GrouperSession s, String key) {
    GrouperSession.validate(s);
    GrouperMember m = new GrouperMember();
    try {
      m = (GrouperMember) s.dbSess().session().get(
                            GrouperMember.class, key
                          );
      m.s = s; // TODO Argh!
    } catch (HibernateException e) {
      throw new RuntimeException("Error loading member: " + e);
    }
    return m;
  }

  /*
   * Save a {@link GrouperMember} to the groups registry.
   */
  protected void save(DbSess dbSess) {
    try {
      dbSess.txStart();
      dbSess.session().save(this);
      dbSess.txCommit();
    } catch (HibernateException e) {
      dbSess.txRollback();
      throw new RuntimeException("Error saving member: " + e);
    }
  }

  /*
   * Convert memberKey into {@link Subject}.
   */
  protected static Subject toSubject(GrouperSession s, String key) {
    Subject subj = null; // TODO All of this damn reliance upon null
    if (key != null) {
      GrouperMember m = GrouperMember.loadByKey(s, key);
      if (m != null) {
        subj = SubjectFactory.load(m.subjectID(), m.typeID());
      }
    }
    return subj;
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
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
   * Retrieve group memberships for this member for <b>all</b> list
   * types.
   */
  protected List listValsAll() {
    String  qry   = "GrouperList.by.member";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.key());
      try {
        vals.addAll( GrouperList.load(this.s, q.list()) );
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }

  /*
   * Load member by id and type.
   */
  private static GrouperMember loadByIdAndType(DbSess dbSess, Subject subj) {
    GrouperMember m     = null;
    String        qry   = "GrouperMember.by.subjectid.and.typeid";
    List          vals  = new ArrayList();
    try {
      Query q = dbSess.session().getNamedQuery(qry);
      q.setString(0, subj.getId());
      q.setString(1, subj.getSubjectType().getId());
      try {
        vals = q.list();
        if (vals.size() == 1) {
          m = (GrouperMember) vals.get(0);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return m;
  }


  /*
   * PRIVATE INSTANCE MTHODS
   */

  /*
   * Perform a list query against the groups registry.
   */
  private List queryListVals(String qry, String list) {
    List vals = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.key());
      q.setString(1, list);
      try {
        vals.addAll( GrouperList.load(this.s, q.list()) );
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
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

