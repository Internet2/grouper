/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;


/**
 *
 */
public class MyJndiSource extends GrouperJndiSourceAdapter {

  /**
   * 
   */
  public MyJndiSource() {
  }

  /**
   * @param id
   * @param name
   */
  public MyJndiSource(String id, String name) {
    super(id, name);

  }

  /**
   * 
   * @param subject
   */
  private static void initSubject(Subject subject) {
    if (subject != null) {
      if (StringUtils.isBlank(subject.getAttributeValue("somethingNew"))) {
        String newAttribute = subject.getAttributeValue("this") + "," 
          + subject.getAttributeValue("that") + ", " + subject.getAttributeValue("theOther");
        subject.getAttributes().put("somethingNew", GrouperUtil.toSet(newAttribute));
      }
    }
  }
  
  /**
   * @see edu.internet2.middleware.subject.provider.JNDISourceAdapter#getSubject(java.lang.String, boolean)
   */
  @Override
  public Subject getSubject(String id1, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    Subject subject = super.getSubject(id1, exceptionIfNull);
    initSubject(subject);
    return subject;
  }

  /**
   * @see edu.internet2.middleware.subject.provider.JNDISourceAdapter#getSubjectByIdentifier(java.lang.String, boolean)
   */
  @Override
  public Subject getSubjectByIdentifier(String id1, boolean exceptionIfNull)
      throws SubjectNotFoundException, SubjectNotUniqueException {
    Subject subject = super.getSubjectByIdentifier(id1, exceptionIfNull);
    initSubject(subject);
    return subject;
  }

  /**
   * @see edu.internet2.middleware.subject.provider.JNDISourceAdapter#search(java.lang.String)
   */
  @Override
  public Set<Subject> search(String searchValue) {
    Set<Subject> subjects = super.search(searchValue);
    for (Subject subject : GrouperUtil.nonNull(subjects)) {
      initSubject(subject);
    }
    return subjects;
  }

  
  
}
