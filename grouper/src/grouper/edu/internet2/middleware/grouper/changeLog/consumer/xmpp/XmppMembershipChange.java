package edu.internet2.middleware.grouper.changeLog.consumer.xmpp;


/**
 * <pre>
 * Action: ADD_MEMBER, and REMOVE_MEMBER
 * </pre>
 * @author mchyzer
 */
public class XmppMembershipChange {

  /** either ADD_MEMBER or REMOVE_MEMBER */
  private String action;
  
  /** group name that has a membership change */
  private String groupName;
  
  /** jobName for this group */
  private String jobName;
  
  
  /**
   * jobName for this group
   * @return the jobName
   */
  public String getJobName() {
    return this.jobName;
  }

  
  /**
   * jobName for this group
   * @param jobName1 the jobName to set
   */
  public void setJobName(String jobName1) {
    this.jobName = jobName1;
  }

  /**
   * group name that has a membership change
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }
  
  /**
   * group name that has a membership change
   * @param groupName1 the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * either ADD_MEMBER or REMOVE_MEMBER
   * @return the action
   */
  public String getAction() {
    return this.action;
  }

  
  /**
   * either ADD_MEMBER or REMOVE_MEMBER
   * @param action1 the action to set
   */
  public void setAction(String action1) {
    this.action = action1;
  }

  /**
   * attributes of subjects returned, in same order as the data
   */
  private String[] subjectAttributeNames;

  /**
   * attributes of subjects returned, in same order as the data
   * @return the attributeNames
   */
  public String[] getSubjectAttributeNames() {
    return this.subjectAttributeNames;
  }

  /**
   * attributes of subjects returned, in same order as the data
   * @param attributeNamesa the attributeNames to set
   */
  public void setSubjectAttributeNames(String[] attributeNamesa) {
    this.subjectAttributeNames = attributeNamesa;
  }

  /**
   * results for each assignment sent in
   */
  private XmppSubject xmppSubject;

  /**
   * subject to add / remove
   * @return the results
   */
  public XmppSubject getXmppSubject() {
    return this.xmppSubject;
  }

  /**
   * results for each assignment sent in
   * @param results1 the results to set
   */
  public void setXmppSubject(XmppSubject results1) {
    this.xmppSubject = results1;
  }

  /**
   * empty
   */
  public XmppMembershipChange() {
    //empty
  }

}
