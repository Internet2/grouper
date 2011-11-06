/**
 * 
 */
package edu.internet2.middleware.grouper.subj;

import java.util.Collection;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;


/**
 * add the ability to decorate a list of subjects with more attributes.
 * note, while you are decorating, you can check security to see if the
 * groupersession is allowed to see those attributes
 * @author mchyzer
 *
 */
public interface SubjectCustomizer {

  /**
   * decorate subjects based on attributes requested
   * @param grouperSession
   * @param subjects
   * @param attributeNamesRequested
   */
  public void decorateSubjects(GrouperSession grouperSession, Collection<Subject> subjects, Collection<String> attributeNamesRequested);
  
  /**
   * you can edit the subjects (or replace), but you shouldnt remove them
   * @param grouperSession
   * @param subjects
   */
  public void filterSubjects(GrouperSession grouperSession, Collection<Subject> subjects);
  
}
