/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * @author Kate
 * $Id: MemberSortWrapper.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.util;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;


/**
 *
 */
public class MemberSortWrapper implements Comparable {

  /** wrapped subject */
  private SubjectSortWrapper wrappedSubject;
  
  /** wrapped member */
  private Member wrappedMember;
  
  /**
   * wrapped member
   * @param member1 
   */
  public MemberSortWrapper(Member member1) {
    this.wrappedMember = member1;
    final String subjectString = this.wrappedMember.getSubjectSourceId() + ": " + this.wrappedMember.getSubjectId(); 
    try {
      this.wrappedSubject = new SubjectSortWrapper(member1.getSubject());
    } catch (SubjectNotFoundException snfe) {
      this.wrappedSubject = new SubjectSortWrapper(new Subject() {

        /**
         * 
         * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String)
         */
        public String getAttributeValue(String name) {
          return null;
        }

        /**
         * 
         * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
         */
        public Set getAttributeValues(String name) {
          return null;
        }

        /**
         * 
         * @see edu.internet2.middleware.subject.Subject#getAttributes()
         */
        public Map getAttributes() {
          return null;
        }

        /**
         * 
         * @see edu.internet2.middleware.subject.Subject#getDescription()
         */
        public String getDescription() {
          return subjectString;
        }

        public String getId() {
          return MemberSortWrapper.this.wrappedMember.getSubjectId();
        }

        public String getName() {
          return subjectString;
        }

        public Source getSource() {
          try {
            return SubjectFinder.getSource(MemberSortWrapper.this.wrappedMember.getSubjectSourceId());
          } catch (SourceUnavailableException sue) {
            //not sure what to do here
          }
          return null;
        }

        public SubjectType getType() {
          return MemberSortWrapper.this.wrappedMember.getSubjectType();
        }

        public String getAttributeValueOrCommaSeparated(String attributeName) {
          return null;
        }

        public String getAttributeValueSingleValued(String attributeName) {
          return null;
        }

        public String getSourceId() {
          return null;
        }

        public String getTypeName() {
          return null;
        }

        /**
         * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String, boolean)
         */
        @Override
        public String getAttributeValue(String attributeName,
            boolean excludeInternalAttributes) {
          return null;
        }

        /**
         * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String, boolean)
         */
        @Override
        public String getAttributeValueOrCommaSeparated(String attributeName,
            boolean excludeInternalAttributes) {
          return null;
        }

        /**
         * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String, boolean)
         */
        @Override
        public String getAttributeValueSingleValued(String attributeName,
            boolean excludeInternalAttributes) {
          return null;
        }

        /**
         * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String, boolean)
         */
        @Override
        public Set<String> getAttributeValues(String attributeName,
            boolean excludeInternalAttributes) {
          return null;
        }

        /**
         * @see edu.internet2.middleware.subject.Subject#getAttributes(boolean)
         */
        @Override
        public Map<String, Set<String>> getAttributes(boolean excludeInternalAttributes) {
          return null;
        }
        
      });
    }
  }
  
  /**
   * return the wrapped subject
   * @return the wrapped subject
   */
  public SubjectSortWrapper getWrappedSubject() {
    return this.wrappedSubject;
  }

  
  /**
   * return the wrapped member
   * @return the wrappedMember
   */
  public Member getWrappedMember() {
    return this.wrappedMember;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    if (!(o instanceof MemberSortWrapper)) {
      return -1;
    }
    MemberSortWrapper memberSortWrapper = (MemberSortWrapper)o;
    return this.getWrappedSubject().compareTo(memberSortWrapper.getWrappedSubject());
  }
  

}
