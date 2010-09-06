/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;



/**
 * utils about emails
 */
public class GrouperEmailUtils {

  /**
   * get the subject attribute name for a source id
   * @param sourceId
   * @return the attribute name
   */
  public static String emailAttributeNameForSource(String sourceId) {
    Source source = SourceManager.getInstance().getSource(sourceId);
    return source.getInitParam("emailAttributeName");
  }
}
