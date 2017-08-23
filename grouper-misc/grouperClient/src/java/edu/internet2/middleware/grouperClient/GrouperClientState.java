/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient;



/**
 * client threadlocal state
 */
public class GrouperClientState {

  /**
   * grouper client state
   */
  private static ThreadLocal<GrouperClientState> threadLocal = new ThreadLocal<GrouperClientState>();
  
  /**
   * 
   */
  public static void removeGrouperClientState() {
    threadLocal.remove();
  }
  
  /**
   * @param createIfNecessary
   * @return the grouper client state
   */
  public static GrouperClientState retrieveGrouperClientState(boolean createIfNecessary) {
    GrouperClientState grouperClientState = threadLocal.get();
    if (createIfNecessary && grouperClientState == null) {
      grouperClientState = new GrouperClientState();
      threadLocal.set(grouperClientState);
    }
    return grouperClientState;
  }

  /**
   * assign grouper client state
   * @param grouperClientState
   */
  public static void assignGrouperClientState(GrouperClientState grouperClientState) {
    threadLocal.set(grouperClientState);
  }
  
  /** second level grouper act as source id */
  private String grouperActAsSourceId;
  
  /** second level grouper act as subject id */
  private String grouperActAsSubjectId;

  /** second level grouper act as subject identifier */
  private String grouperActAsSubjectIdentifier;

  
  /**
   * second level grouper act as source id
   * @return the grouperActAsSourceId
   */
  public String getGrouperActAsSourceId() {
    return this.grouperActAsSourceId;
  }

  
  /**
   * second level grouper act as source id
   * @param grouperActAsSourceId1 the grouperActAsSourceId to set
   */
  public void setGrouperActAsSourceId(String grouperActAsSourceId1) {
    this.grouperActAsSourceId = grouperActAsSourceId1;
  }

  
  /**
   * second level grouper act as subject id
   * @return the grouperActAsSubjectId
   */
  public String getGrouperActAsSubjectId() {
    return this.grouperActAsSubjectId;
  }

  
  /**
   * second level grouper act as subject id
   * @param grouperActAsSubjectId1 the grouperActAsSubjectId to set
   */
  public void setGrouperActAsSubjectId(String grouperActAsSubjectId1) {
    this.grouperActAsSubjectId = grouperActAsSubjectId1;
  }

  
  /**
   * second level grouper act as subject identifier
   * @return the grouperActAsSubjectIdentifier
   */
  public String getGrouperActAsSubjectIdentifier() {
    return this.grouperActAsSubjectIdentifier;
  }

  
  /**
   * second level grouper act as subject identifier
   * @param grouperActAsSubjectIdentifier1 the grouperActAsSubjectIdentifier to set
   */
  public void setGrouperActAsSubjectIdentifier(String grouperActAsSubjectIdentifier1) {
    this.grouperActAsSubjectIdentifier = grouperActAsSubjectIdentifier1;
  }

  /**
   * 
   */
  public GrouperClientState() {
  }

}
