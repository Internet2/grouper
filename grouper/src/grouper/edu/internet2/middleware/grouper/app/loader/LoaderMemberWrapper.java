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
 * @author mchyzer
 * $Id: LoaderMemberWrapper.java,v 1.3 2009-08-12 04:52:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;


import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


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
  private String subjectIdentifier0;
  
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
        Subject subject = this.findOrGetSubject();
        this.member = edu.internet2.middleware.grouper.MemberFinder.internal_findBySubject(subject, null, false);
      } catch (Exception e) {
        throw new RuntimeException("Problem with loader member wrapper: " + this.subjectId, e);
      }
    }
    return this.member;
  }

  /**
   * logger 
   */
  private static final org.apache.commons.logging.Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(GrouperLoaderType.class);

  /**
   * 
   * @return subject
   */
  public Subject findOrGetSubject() {
    try {
      if (this.member == null) {
          Subject subject = null;
          try {
            subject = SubjectFinder.findByIdAndSource(this.subjectId, this.sourceId, true);
          } catch (Exception e) {
            if (e instanceof SubjectNotFoundException) {
              // dont log stack if subject not found
              LOG.info("Cant find subject: '" + this.sourceId + "', '" + this.subjectId + "'");
            } else {
              LOG.info("Cant find subject: '" + this.sourceId + "', '" + this.subjectId + "'", e);
            }
          }
          if (subject == null) {
            String subjectType = "person";
            try {
              subjectType = edu.internet2.middleware.subject.provider.SourceManager.getInstance().getSource(this.sourceId).getSubjectTypes().iterator().next().getName();
            } catch (Exception e) {
              LOG.info("Cant get subject type for source: " + this.sourceId, e);
            }
            subject = new edu.internet2.middleware.subject.provider.SubjectImpl(this.subjectId, this.subjectId, this.subjectId, subjectType, this.sourceId);
          }
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
   * @param subjectIdentifier0
   */
  public LoaderMemberWrapper(String subjectId, String sourceId, String subjectIdentifier0) {
    this.subjectId = subjectId;
    this.sourceId = sourceId;
    this.subjectIdentifier0 = subjectIdentifier0;
  }

  /**
   * @param member
   */
  public LoaderMemberWrapper(Member member) {
    this.member = member;
    this.subjectId = member.getSubjectIdDb();
    this.sourceId = member.getSubjectSourceIdDb();
    this.subjectIdentifier0 = member.getSubjectIdentifier0();
  }

  
  /**
   * @return the subjectIdentifier0
   */
  public String getSubjectIdentifier0() {
    return subjectIdentifier0;
  }

  
  /**
   * @param subjectIdentifier0 the subjectIdentifier0 to set
   */
  public void setSubjectIdentifier0(String subjectIdentifier0) {
    this.subjectIdentifier0 = subjectIdentifier0;
  }
  
}
