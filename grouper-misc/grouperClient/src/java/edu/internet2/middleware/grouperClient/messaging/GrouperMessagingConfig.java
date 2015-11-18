/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;


/**
 * configs in grouper.client.properties
 * # name of a messaging system.  note, "myAwsMessagingSystem" can be arbitrary
 * # grouper.messaging.system.myAwsMessagingSystem.name = aws
 * 
 * # class that implements edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem
 * # grouper.messaging.system.myAwsMessagingSystem.class = 
 *
 */
public class GrouperMessagingConfig {

  /**
   * 
   */
  public GrouperMessagingConfig() {
  }

  /**
   * name of grouper message system configured in grouper.client.properties
   */
  private String name;

  /**
   * theClass of the grouper messaging config.  if null there is a problem
   */
  private Class<GrouperMessagingSystem> theClass;

  
  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  
  /**
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  
  /**
   * @return the theClass
   */
  public Class<GrouperMessagingSystem> getTheClass() {
    return this.theClass;
  }

  
  /**
   * @param theClass1 the theClass to set
   */
  public void setTheClass(Class<GrouperMessagingSystem> theClass1) {
    this.theClass = theClass1;
  }
 
  
  
}

