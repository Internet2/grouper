/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.subj;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.subject.LazySource;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/** 
 * {@link Subject} from a {@link Membership} - getMember().getSubject()
 * only called if necessary i.e. the UI pages results and so it is often not
 * necessary to instantiate all the Subjects (and Members) 
 * <p/>
 * @author  Gary Brown.
 * @version $Id: LazySubject.java,v 1.11 2009-10-31 16:27:12 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class LazySubject implements Subject {

  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    try {
      Member theMember = this.getMember();
      return "'" + theMember.getSubjectId() + "'/'"
          + theMember.getSubjectTypeId() + "'/'" + theMember.getSubjectSourceId() + "'";
    } catch (Exception e) {

      return "LazySubject with member uuid: " + this.member.getUuid();

    }
  }

  /** membership if built from membership */
  private Membership membership;

  /** member if built from it or already retrieved it */
  private Member member;

  /** subject if it has lazily retrieved it already */
  private Subject subject;

  /**
   * 
   */
  private SubjectType subjectType = new LazySubjectType();

  /**
   * 
   */
  private Source subjectSource = null;

  /**
   * 
   */
  boolean unresolvable = false;

  /**
   * 
   * @param ms
   */
  public LazySubject(Membership ms) {
    this.membership = ms;
    try {
      this.member = ms.getMember();
    } catch (MemberNotFoundException e) {
      throw new GrouperException(e);
    }
  }

  /**
   * 
   * @param member
   */
  public LazySubject(Member member) {
    this.member = member;
  }

  /**
  * @see edu.internet2.middleware.subject.Subject#getAttributes()
  */
  public Map getAttributes() {
    try {
      return getSubject().getAttributes();
    } catch (RuntimeException re) {
      unresolvable = true;
      throw re;
    }
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
   */
  public String getAttributeValue(String name) {
    return getSubject().getAttributeValue(name);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
   */
  public Set getAttributeValues(String name) {
    return getSubject().getAttributeValues(name);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getDescription()
   */
  public String getDescription() {
    try {
      return getSubject().getDescription();
    } catch (Exception e) {
      throw new GrouperException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getId()
   */
  public String getId() {
    try {
      return member.getSubjectId();
    } catch (Exception e) {
      throw new GrouperException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getName()
   */
  public String getName() {
    try {
      return getSubject().getName();
    } catch (Exception e) {
      throw new GrouperException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getSource()
   */
  public Source getSource() {
    if (this.subjectSource == null) {
      this.subjectSource = new LazySource(this.member.getSubjectSourceId());
    }
    return subjectSource;
  }

  /** get the source id 
   * @return the soruce id */
  public String getSourceId() {
    return this.member.getSubjectSourceId();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getType()
   */
  public SubjectType getType() {
    return subjectType;
  }

  /**
   * 
   * @return the subject
   * @throws SubjectNotFoundException
   */
  @SuppressWarnings("serial")
  private Subject getSubject() throws SubjectNotFoundException {
    if (subject == null) {
      final String[] error = new String[1];
      try {
        this.subject = SubjectFinder.findByIdAndSource(
            this.member.getSubjectId(), this.member.getSubjectSourceId(), true
            );
        return subject;
      } catch (SubjectNotFoundException snfe) {
        //dont change this unless you change the UI code too
        error[0] = this.member.getSubjectId() + " entity not found";
      } catch (SourceUnavailableException eSU) {
        error[0] = this.member.getSubjectId() + " source unavailable "
            + this.member.getSubjectSourceId();
      } catch (SubjectNotUniqueException eSNU) {
        error[0] = this.member.getSubjectId() + " entity not unique";
      }
      //there was an error, note, dont return an error for every attribute...
      this.subject = new SubjectImpl(LazySubject.this.member.getSubjectId(), error[0],
          error[0],
          this.getTypeName(), this.getSourceId(), SubjectImpl.toAttributeMap("error", error[0]));
    }
    return subject;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return SubjectImpl.equalsStatic(this, obj);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return SubjectImpl.hashcodeStatic(this);
  }

  /**
   * 
   * @return member
   */
  private Member getMember() {
    return this.member;
  }

  /**
   * 
   * @return membership
   */
  public Membership getMembership() {
    return this.membership;
  }

  /**
   * Circumvent the need to instantiate an actual Subject just to get the type
   * @since 1.3.1
   */
  @SuppressWarnings("serial")
  class LazySubjectType extends SubjectType {

    /**
     */
    LazySubjectType() {
    }

    /**
     * 
     * @see edu.internet2.middleware.subject.SubjectType#getName()
     */
    public String getName() {
      return member.getSubjectTypeId();
    }
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getTypeName()
   */
  public String getTypeName() {
    return this.getType().getName();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String)
   */
  public String getAttributeValueOrCommaSeparated(String attributeName) {
    return SubjectImpl.attributeValueOrCommaSeparated(this, attributeName);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String)
   */
  public String getAttributeValueSingleValued(String attributeName) {
    return SubjectImpl.attributeValueOrCommaSeparated(this, attributeName);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String, boolean)
   */
  public String getAttributeValue(String attributeName, boolean excludeInternalAttributes) {
    return getSubject().getAttributeValue(attributeName, excludeInternalAttributes);
  }
  
  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String, boolean)
   */
  public Set<String> getAttributeValues(String attributeName, boolean excludeInternalAttributes) {
    return getSubject().getAttributeValues(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String, boolean)
   */
  public String getAttributeValueOrCommaSeparated(String attributeName, boolean excludeInternalAttributes) {
    return SubjectImpl.attributeValueOrCommaSeparated(this, attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String, boolean)
   */
  public String getAttributeValueSingleValued(String attributeName, boolean excludeInternalAttributes) {
    return getSubject().getAttributeValueSingleValued(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes(boolean)
   */
  public Map<String, Set<String>> getAttributes(boolean excludeInternalAttributes) {
    try {
      return getSubject().getAttributes(excludeInternalAttributes);
    } catch (RuntimeException re) {
      unresolvable = true;
      throw re;
    }
  }
}
