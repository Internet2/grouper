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

import  java.io.Serializable;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class representing a {@link Grouper} list value.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperList.java,v 1.21 2004-12-03 02:00:38 blair Exp $
 */
public class GrouperList implements Serializable {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String  groupKey;
  private String  groupField;
  private String  memberKey;
  private String  via;
  private String  removeAfter;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperList() {
    this._init();
  }

  /**
   * Construct a new {@link GrouperList} object.
   * <p />
   * TODO This should <b>only</b> be used within Grouper and I'd
   *      prefer to not be relying upon <i>protected</i> for that...
   */
  protected GrouperList(GrouperGroup g, GrouperMember m, String list, GrouperGroup via) {
    this._init();
    this.groupKey   = g.key();  // FIXME Validate?
    this.memberKey  = m.key();  // FIXME Validate?
    this.groupField = list;     // FIXME Validate?
    if (via != null) {
      this.via = via.key();
    } else {
      this.via = null;
    }
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * TODO Either remove or at least make protected
   */
  public String groupKey() {
    return this.getGroupKey();
  }

  /**
   * TODO MAY NOT REMAIN
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
    return GrouperBackend.member(this.memberKey());
  }

  /**
   * Returns the {@link GrouperGroup} object referenced by this 
   * {@link GrouperList} object.
   * <p />
   * TODO
   *
   * @return  A {@link GrouperGroup} object.
   */
  public GrouperGroup group() {
    return GrouperBackend.groupLoadByKey(this.groupKey());
  }

  /**
   * TODO Either remove or at least make protected
   */
  public String memberKey() {
    return this.getMemberKey();
  }

  /**
   * TODO MAY NOT REMAIN
   */
  public String via() {
    return this.getVia();
  }

  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }

  public int hashCode() {
     return HashCodeBuilder.reflectionHashCode(this);
   }

  /**
   * Return a string representation of the {@link GrouperSchema}
   * object.
   * <p />
   * TODO Do I want to add in `groupkey'?  Or perhaps, given the key,
   *      return the `groupID'?
   * TODO Do I want to add in `memberkey'?  Or perhaps, given the key,
   *      return the `memberID'?
   * TODO Do I want to add in `via'?  Or perhaps, given the via key,
   *      return the `groupID'?
   *
   * @return String representation of the object.
   */
  public String toString() {
    return new ToStringBuilder(this)        .
      append("field", this.getGroupField()) .
      toString();
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.groupKey     = null;
    this.groupField   = null;
    this.memberKey    = null;
    this.via          = null;
    this.removeAfter  = null;
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

  private String getMemberKey() {
    return this.memberKey;
  }

  private void setMemberKey(String memberKey) {
    this.memberKey = memberKey;
  }

  private String getVia() {
    return this.via;
  }

  private void setVia(String via) {
    this.via = via;
  }

  private String getRemoveAfter() {
    return this.removeAfter;
  }

  private void setRemoveAfter(String removeAfter) {
    this.removeAfter = removeAfter;
  }

}

