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
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class modeling a {@link Grouper} list value.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperList.java,v 1.38 2005-03-15 15:33:31 blair Exp $
 */
public class GrouperList implements Serializable {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private GrouperGroup    g;
  private GrouperMember   m;
  private GrouperGroup    via;
  private String          groupKey;
  private String          groupField;
  private String          listKey;
  private String          memberKey;
  private String          pathKey;
  private GrouperSession  s;
  private String          viaKey;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperList() {
    this._init();
  }

  /* (!javadoc)
   * TODO This should <b>only</b> be used within Grouper and I'd
   *      prefer to not be relying upon <i>protected</i> for that...
   */
  protected GrouperList(GrouperGroup g, GrouperMember m, String list) {
    this._init();
    // Generate UUID
    this.setListKey( GrouperBackend.uuid() );
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
  }
  protected GrouperList(GrouperGroup g, GrouperMember m, String list, GrouperGroup via) {
    this._init();
    // Generate UUID
    this.setListKey( GrouperBackend.uuid() );
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
    this.via        = via;
    if (via != null) {
      this.viaKey   = via.key(); 
    }
  }
  // FIXME Or would I rather just lazily load the objects?
  protected GrouperList(
              GrouperSession s, String groupKey, String memberKey, 
              String list, String viaKey
            ) 
  {
    this._init();
    // FIXME Use this.load(s)?
    this.g          = GrouperGroup.loadByKey(s, groupKey);
    this.m          = GrouperBackend.member(s, memberKey);
    if (viaKey != null) {
      this.via        = GrouperGroup.loadByKey(s, viaKey);
    }
    this.groupKey   = groupKey;
    this.memberKey  = memberKey;
    this.groupField = list;
    this.viaKey     = viaKey;
  }

  protected void load(GrouperSession s) {
    // FIXME Validator!
    if (this.groupKey == null) {
      throw new RuntimeException("Unable to load group as key is null");
    }
    if (this.memberKey == null) {
      throw new RuntimeException("Unable to load member as key is null");
    }
    this.g          = GrouperGroup.loadByKey(s, this.groupKey);
    this.m          = GrouperBackend.member(s, this.memberKey);
    if (this.viaKey != null) {
      this.via        = GrouperGroup.loadByKey(s, this.viaKey);
    }
    if (this.g == null) {
      throw new RuntimeException("Unable to load group");
    }
    if (this.m == null) {
      throw new RuntimeException("Unable to load member");
    }
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

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
   * If this object represents an effective membership, returning the
   * {@link GrouperGroup} object that caused the effective membership.
   * <p />
   * @return  A {@link GrouperGroup} object.
   */
  public GrouperGroup via() {
    return this.via;
  }

  protected String groupKey() {
    return this.groupKey;
  }
  protected String memberKey() {
    return this.memberKey;
  }
  protected String viaKey() {
    return this.viaKey;
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
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.g = null;
    this.m = null;
    this.via = null;
    this.groupKey     = null;
    this.groupField   = null;
    this.memberKey    = null;
    this.viaKey       = null;
  }


  /*
   * HIBERNATE
   */

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

  private String getPathKey() {
    return this.pathKey;
  }

  private void setPathKey(String pathKey) {
    this.pathKey = pathKey;
  }

  private String getViaKey() {
    return this.viaKey;
  }

  private void setViaKey(String viaKey) {
    this.viaKey = viaKey;
  }

}

