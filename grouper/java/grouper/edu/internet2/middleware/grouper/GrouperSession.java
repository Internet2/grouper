/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

import  edu.internet2.middleware.subject.*;
import  java.io.Serializable;
import  java.util.Date;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;
import  org.apache.commons.lang.builder.ToStringBuilder;

/** 
 * Session for interacting with the Grouper API.
 * <p />
 * @author  blair christensen.
 * @version $Id: GrouperSession.java,v 1.1.2.5 2005-10-18 17:53:29 blair Exp $
 *     
*/
public class GrouperSession implements Serializable {

  // Properties
  private String  id;
  private Date    start_time;
  private String  uuid;
  private Integer version;
  private Member  member_id;

  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public GrouperSession() { 
    // Nothing
  }

  // Public class methods

  /**
   * Start a session for interacting with the Grouper API.
   * <pre class="eg">
   * // Start a Grouper API session.
   * GrouperSession s = GrouperSession.startSession(subject);
   * </pre>
   * @param   subject   Start session as this {@link Subject}.
   * @return  A Grouper API session.
   */
  public static GrouperSession startSession(Subject subject) {
    // DESIGN What exception?
    throw new RuntimeException("Not implemented");
  }

  // Public instance methods

  /**
   * Begin a Groups Registry transaction.
   * <p>
   * By default, the Grouper API will update the Groups Registry for
   * each API call that modifies a registry object.  This method allows
   * one to disable the default behavior and combine a number of
   * operations into a single update.
   * </p>
   * <pre class="eg">
   * // Begin a transaction.
   * s.beginTransaction();
   * </pre> 
   */ 
  public void beginTransaction() {
    // DESIGN What exception?
    throw new RuntimeException("Not implemented");
  }

  /**
   * Commit a Groups Registry transaction.
   * <pre class="eg">
   * // Commit all updates within a registry transaction.
   * s.commit();
   * </pre>
   */
  public void commit() {
    // DESIGN What exception?
    throw new RuntimeException("Not implemented");
  }
 
  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof GrouperSession) ) return false;
    GrouperSession castOther = (GrouperSession) other;
    return new EqualsBuilder()
           .append(this.getUuid(), castOther.getUuid())
           .append(this.getMember_id(), castOther.getMember_id())
           .isEquals();
  }

  /**
   * Get the {@link Member} associated with this API session.
   * <pre class="eg">
   * // Get this session's Subject as a Member object.
   * Member m = s.getMember(); 
   * </pre>
   * @return  A {@link Member} object.
   */
  public Member getMember() {
    // DESIGN What exception?
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get the {@link Subject} associated with this API session.
   * <pre class="eg">
   * // Get this session's Subject.
   * Subject subj = s.getSubject(); 
   * </pre>
   * @return  A {@link Subject} object.
   */
  public Subject  getSubject() {
    // DESIGN What exception?
    throw new RuntimeException("Not implemented");
  }

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getUuid())
           .append(getMember_id())
           .toHashCode();
  }

  /**
   * Rollback a Groups Registry transaction.
   * <pre class="eg">
   * // Roll back changes performed during this transaction.
   * s.rollback();
   * </pre>
   */
  public void rollback() {
    // DESIGN What exception?
    throw new RuntimeException("Not implemented");
  }

  /**
   * Stop this API session.
   * <pre class="eg">
   * // Stop the session.
   * s.stopSession();
   * </pre>
   */
  public void stopSession() {
    throw new RuntimeException("Not implemented");
  }

  public String toString() {
    return new ToStringBuilder(this)
      .append("uuid", getUuid())
      .append("member_id", getMember_id())
      .toString();
  }

  // Hibernate Accessors
  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  private Date getStart_time() {
    return this.start_time;
  }

  private void setStart_time(Date start_time) {
    this.start_time = start_time;
  }

  private String getUuid() {
    return this.uuid;
  }

  private void setUuid(String uuid) {
    this.uuid = uuid;
  }

  private Integer getVersion() {
    return this.version;
  }

  private void setVersion(Integer version) {
    this.version = version;
  }

  private Member getMember_id() {
    return this.member_id;
  }

  private void setMember_id(Member member_id) {
    this.member_id = member_id;
  }

}

