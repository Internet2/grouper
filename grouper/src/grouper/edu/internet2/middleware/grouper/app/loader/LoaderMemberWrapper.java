/*
 * @author mchyzer
 * $Id: LoaderMemberWrapper.java,v 1.1.2.2 2009-08-16 19:50:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class LoaderMemberWrapper {

  /**
   * 
   */
  private String subjectId;
  
  /**
   * 
   */
  private String sourceId;
  
  /**
   * 
   */
  private Member member;

  /**
   * 
   * @return subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * 
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * 
   * @return source id
   */
  public String getSourceId() {
    return this.sourceId;
  }

  /**
   * 
   * @param sourceId1
   */
  public void setSourceId(String sourceId1) {
    this.sourceId = sourceId1;
  }

  /**
   * 
   * @return member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * 
   * @return member
   */
  public Member findOrGetMember() {
    if (this.member == null) {
      try {
        Subject subject = SubjectFinder.getSource(this.sourceId).getSubject(this.subjectId);
        this.member = MemberFinder.internal_findBySubject(subject, false);
      } catch (Exception e) {
        throw new RuntimeException("Problem with loader member wrapper: " + this.subjectId, e);
      }
    }
    return this.member;
  }

  /**
   * 
   * @return subject
   */
  public Subject findOrGetSubject() {
    try {
      if (this.member == null) {
          Subject subject = SubjectFinder.getSource(this.sourceId).getSubject(this.subjectId);
          return subject;
      }
      return this.member.getSubject();
    } catch (Exception e) {
      throw new RuntimeException("Problem with loader member wrapper: " + this.subjectId, e);
    }
  }

  /**
   * 
   * @param member1
   */
  public void setMember(Member member1) {
    this.member = member1;
  }

  /**
   * @param subjectId
   * @param sourceId
   */
  public LoaderMemberWrapper(String subjectId, String sourceId) {
    this.subjectId = subjectId;
    this.sourceId = sourceId;
  }

  /**
   * @param member
   */
  public LoaderMemberWrapper(Member member) {
    this.member = member;
    this.subjectId = member.getSubjectIdDb();
    this.sourceId = member.getSubjectSourceIdDb();
  }
  
  
  
}
