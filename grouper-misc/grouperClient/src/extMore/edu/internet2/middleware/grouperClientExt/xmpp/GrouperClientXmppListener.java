/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import java.util.List;


/**
 * listener must implement this interface to handle events
 */
public interface GrouperClientXmppListener {

  /**
   * handle an event from xmpp
   * @param groupName
   * @param groupExtension
   * @param subjectAttributeNames
   * @param newSubjectList
   * @param previousSubjectList
   * @param changeSubject
   * @param action
   */
  public void handleEvent(String groupName, String groupExtension, 
      String[] subjectAttributeNames, List<XmppSubject> newSubjectList, 
      List<XmppSubject> previousSubjectList, XmppSubject changeSubject, String action);
  
}
