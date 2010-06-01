/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import java.util.List;


/**
 * implement this to handle events from xmpp
 */
public interface GrouperClientXmppHandler {

  /**
   * handle an incremental event from xmpp
   * @param grouperClientXmppJob 
   * @param groupName
   * @param groupExtension
   * @param subjectAttributeNames
   * @param newSubjectList
   * @param previousSubjectList
   * @param changeSubject
   * @param action
   */
  public void handleIncremental(GrouperClientXmppJob grouperClientXmppJob, String groupName, String groupExtension, 
      List<GrouperClientXmppSubject> newSubjectList, 
      List<GrouperClientXmppSubject> previousSubjectList, GrouperClientXmppSubject changeSubject, String action);

  
  /**
   * handle a full refresh event from xmpp
   * @param grouperClientXmppJob
   * @param groupName
   * @param groupExtension
   * @param subjectAttributeNames
   * @param newSubjectList
   */
  public void handleAll(GrouperClientXmppJob grouperClientXmppJob, String groupName, String groupExtension, 
      List<GrouperClientXmppSubject> newSubjectList);

}
