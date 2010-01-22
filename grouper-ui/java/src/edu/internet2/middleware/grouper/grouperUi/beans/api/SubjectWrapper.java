/**
 * @author Kate
 * $Id: SubjectWrapper.java,v 1.1 2009-09-09 15:10:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.subject.provider.SubjectImpl;


/**
 * subject implementation around a member, if the subject is not found, handle gracefully
 */
public class SubjectWrapper extends SubjectImpl {

  /**
   * construct from member
   * @param theMember
   */
  public SubjectWrapper(Member theMember) {
    super(theMember.getSubjectId(), "Cant find subject: " + theMember.getSubjectSourceId() + ": " + theMember.getSubjectId(),
        "Cant find subject: " + theMember.getSubjectSourceId() + ": " + theMember.getSubjectId(),
        theMember.getSubjectSourceId(), null);
  }

}
