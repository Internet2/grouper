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
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class modeling a {@link Grouper} list value.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperList.java,v 1.47 2005-03-21 20:41:59 blair Exp $
 */
public class GrouperList implements Serializable {

  /*
   * PRIVATE INSTANCE VARIABLES
   */

  // Transient
  private transient List            elements = new ArrayList();
  private transient GrouperGroup    g;
  private transient GrouperMember   m;
  private transient GrouperSession  s;
  private transient GrouperGroup    via;

  // And persistent
  private String chainKey;
  private String groupField;
  private String groupKey;
  private String listKey;
  private String memberKey;
  private String viaKey;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperList() {
    // Nothing
  }

  /*
   * Create a new {@link GrouperList} object.
   * TODO Refactor
   */
  protected GrouperList(GrouperGroup g, GrouperMember m, String list) {
    // TODO See if it exists?
    // TODO Load chain?
    if (this.getListKey() == null) {
      this.setListKey( new GrouperUUID().toString() );
    }
    if (g == null) {
      throw new RuntimeException("GrouperList: null group");
    }
    if (m == null) {
      throw new RuntimeException("GrouperList: null member");
    }
    if (list == null) {
      throw new RuntimeException("GrouperList: null list");
    }
    this.g          = g;
    this.groupKey   = g.key();
    this.m          = m;
    this.memberKey  = m.key();
    this.groupField = list;
    this.via        = null;
    GrouperList.validate(this);
  }

  /*
   * Create a new {@link GrouperList} object.
   * TODO Refactor
   */
  protected GrouperList(
              GrouperSession s, GrouperGroup g, GrouperMember m, 
              String list, List chain
            ) 
  {
    // TODO See if it exists?
    // TODO Load chain?
    if (this.getListKey() == null) {
      this.setListKey( new GrouperUUID().toString() );
    }
    if (g == null) {
      throw new RuntimeException("GrouperList: null group");
    }
    if (m == null) {
      throw new RuntimeException("GrouperList: null member");
    }
    if (list == null) {
      throw new RuntimeException("GrouperList: null list");
    }
    this.g          = g;
    this.groupKey   = g.key();
    this.m          = m;
    this.memberKey  = m.key();
    this.groupField = list;
    this.elements = chain;
    if (chain.size() > 0) {
      MemberVia   mv = (MemberVia) chain.get(0);
      GrouperList gl = (GrouperList) mv.toList(s);
      this.via    = gl.group();
      this.viaKey = gl.group().key();
    }
    GrouperList.validate(this);
  }


  /*
   * PROTECTED CLASS METHODS
   */

  protected void load(GrouperSession s) {
    // TODO Load chain?
    GrouperSession.validate(s);
    if (this.g == null) {
      if (this.groupKey == null) {
        throw new RuntimeException("Unable to load group as key is null");
      }
      this.g = GrouperGroup.loadByKey(s, this.groupKey);
    }
    if (this.m == null) {
      if (this.memberKey == null) {
        throw new RuntimeException("Unable to load member as key is null");
      }
      this.m = GrouperMember.loadByKey(s, this.memberKey);
    }
    if (this.via == null) {
      if (this.viaKey != null) {
        this.via = GrouperGroup.loadByKey(s, this.viaKey);
      }
    }
    GrouperList.validate(this);
  }

  /*
   * Save a {@link GrouperList} object to the groups registry.
   */
  protected static void save(GrouperSession s, GrouperList gl) {
    if (gl.getListKey() == null) {
      gl.setListKey( new GrouperUUID().toString() );
    }
    try {
      s.dbSess().session().save(gl);
    } catch (HibernateException e) {
      throw new RuntimeException("Error saving list value: " + e);
    }
  }

  /*
   * Basic validation of {@link GrouperList} object.
   */
  protected static void validate(GrouperList gl) {
    if (gl == null) {
      throw new RuntimeException("list is null");
    }
    if (gl.group() == null) {
      throw new RuntimeException("list group is null");
    }
    if (gl.member() == null) {
      throw new RuntimeException("list member is null");
    }
    if (gl.groupField() == null) {
      throw new RuntimeException("list field is null");
    }
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Retrieve this list's via chain.
   * <p />
   * @return  List of {@link MemberVia} objects.
   */
  public List chain() {
    Iterator iter = this.elements.iterator();
    while (iter.hasNext()) {
      MemberVia el = (MemberVia) iter.next();
    }
    return this.elements;
  }

  /**
   * Returns the {@link GrouperGroup} object referenced by this 
   * {@link GrouperList} object.
   * <p />
   *
   * @return  A {@link GrouperGroup} object.
   */
  public GrouperGroup group() {
    return this.g;
  }

  /**
   * Return the group field for this list value.
   * <p />
   *
   * @return  Field name.
   */
  public String groupField() {
    return this.getGroupField();
  }

  /**
   * Returns the {@link GrouperMember} object referenced by this 
   * {@link GrouperList} object.
   * <p />
   *
   * @return  A {@link GrouperMember} object.
   */
  public GrouperMember member() {
    return this.m;
  }

  /**
   * If this object represents an effective membership, returning the
   * {@link GrouperGroup} object that caused the effective membership.
   * <p />
   * @return  A {@link GrouperGroup} object.
   */
  public GrouperGroup via() {
    return this.via;
  }


  /**
   * Compares the specified object with this list value for equality.
   * <p />
   * @param o Object to be compared for equality with this list value.
   * @return  True if the specified object is equal to this list value.
   */
  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }

  /**
   * Returns the hash code value for this list value.
   * <p />
   * @return  The hash code value for this list value.
   */
  public int hashCode() {
     return HashCodeBuilder.reflectionHashCode(this);
   }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    // TODO Add more information.
    return new ToStringBuilder(this).toString();
  }


  /*
   * PROTECTED INSTANCE METHODS
   */


  /*
   * Set this object's chainKey.
   */
  protected void chainKey(String key) {
    this.setChainKey(key);
  }

  /*
   * @return This object's groupKey.
   */
  protected String groupKey() {
    return this.groupKey;
  }

  /*
   * Return - and possibly assign - a listKey.
   * @return This object's listKey.
   */
  protected String key() {
    if (this.getListKey() == null) {
      this.setListKey( new GrouperUUID().toString());
    }
    return this.getListKey();
  }

  /*
   * @return This object's memberKey.
   */
  protected String memberKey() {
    return this.memberKey;
  }

  /*
   * @return This object's viaKey.
   */
  protected String viaKey() {
    return this.viaKey;
  }


  /*
   * HIBERNATE
   */

  private String getChainKey() {
    return this.chainKey;
  }

  private void setChainKey(String chainKey) {
    this.chainKey = chainKey;
  }

  private String getGroupKey() {
    return this.groupKey;
  }

  private void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  private String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

  private String getListKey() {
    return this.listKey;
  }

  private void setListKey(String listKey) {
    this.listKey = listKey;
  }

  private String getMemberKey() {
    return this.memberKey;
  }

  private void setMemberKey(String memberKey) {
    this.memberKey = memberKey;
  }

  private String getViaKey() {
    return this.viaKey;
  }

  private void setViaKey(String viaKey) {
    this.viaKey = viaKey;
  }

}

