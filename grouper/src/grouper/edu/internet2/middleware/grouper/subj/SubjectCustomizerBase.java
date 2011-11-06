package edu.internet2.middleware.grouper.subj;

import java.util.Collection;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.subject.Subject;

/**
 * extend this to do a subject customizer
 * @author mchyzer
 *
 */
public abstract class SubjectCustomizerBase implements SubjectCustomizer {

  /**
   * @see SubjectCustomizer#decorateSubjects(GrouperSession, Collection, Collection)
   */
  public void decorateSubjects(GrouperSession grouperSession,
      Collection<Subject> subjects, Collection<String> attributeNamesRequested) {
    
  }

  /**
   * @see SubjectCustomizer#filterSubjects(GrouperSession, Collection)
   */
  public void filterSubjects(GrouperSession grouperSession, Collection<Subject> subjects) {
    
  }

  
  
}
