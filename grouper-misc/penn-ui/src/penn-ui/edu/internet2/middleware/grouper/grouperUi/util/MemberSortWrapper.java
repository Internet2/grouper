/**
 * @author Kate
 * $Id: MemberSortWrapper.java,v 1.1 2009-08-07 07:36:02 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.util;

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
        @Override
        public String getAttributeValue(String name) {
          return null;
        }

        /**
         * 
         * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String)
         */
        @Override
        public Set getAttributeValues(String name) {
          return null;
        }

        /**
         * 
         * @see edu.internet2.middleware.subject.Subject#getAttributes()
         */
        @Override
        public Map getAttributes() {
          return null;
        }

        /**
         * 
         * @see edu.internet2.middleware.subject.Subject#getDescription()
         */
        @Override
        public String getDescription() {
          return subjectString;
        }

        @Override
        public String getId() {
          return MemberSortWrapper.this.wrappedMember.getSubjectId();
        }

        @Override
        public String getName() {
          return subjectString;
        }

        @Override
        public Source getSource() {
          try {
            return SubjectFinder.getSource(MemberSortWrapper.this.wrappedMember.getSubjectSourceId());
          } catch (SourceUnavailableException sue) {
            //not sure what to do here
          }
          return null;
        }

        @Override
        public SubjectType getType() {
          return MemberSortWrapper.this.wrappedMember.getSubjectType();
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
  @Override
  public int compareTo(Object o) {
    if (!(o instanceof MemberSortWrapper)) {
      return -1;
    }
    MemberSortWrapper memberSortWrapper = (MemberSortWrapper)o;
    return this.getWrappedSubject().compareTo(memberSortWrapper.getWrappedSubject());
  }
  

}
