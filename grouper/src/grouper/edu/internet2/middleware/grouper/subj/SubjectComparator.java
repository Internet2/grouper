/**
 * @author mchyzer
 * $Id: SubjectComparator.java,v 1.1 2009-12-18 05:58:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;

import java.util.Comparator;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * compare two subjects by sourceId, then by subjectId
 */
public class SubjectComparator implements Comparator<Subject> {

  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Subject o1, Subject o2) {
    if (o1 == o2) {
      return 0;
    }
    //lets by null safe here
    if (o1 == null) {
      return -1;
    }
    if (o2 == null) {
      return 1;
    }
    int compare = GrouperUtil.compare(o1.getSourceId(), o2.getSourceId());
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(o1.getId(), o2.getId());
  }

}
