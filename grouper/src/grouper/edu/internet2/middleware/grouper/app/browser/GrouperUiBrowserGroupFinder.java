package edu.internet2.middleware.grouper.app.browser;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * This class is used to programmatically find a group. This is going to search by the name in the search box.
 * It will page through the results until it finds the right group. The group will be clicked on and the main group page will be ready.
 * <p>
 * Find a group by name
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserGroupFinder grouperUiBrowserGroupFinder = new GrouperUiBrowserGroupFinder(grouperPage).assignGroupToFindName("test:test25").browse();
 * </pre>
 * </blockquote>
 * </p>
 * <p>
 * Find a group by Uuid
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserGroupFinder grouperUiBrowserGroupFinder = new GrouperUiBrowserGroupFinder(grouperPage).assignGroupToFindId("a1b2c3d4").browse();
 * </pre>
 * </blockquote>
 * </p>
 * <p>
 * Find a group by group object
 * <blockquote> 
 * <pre>
 *    GrouperUiBrowserGroupFinder grouperUiBrowserGroupFinder = new GrouperUiBrowserGroupFinder(grouperPage).assignGroupToFind(group).browse();
 * </pre>
 * </blockquote>
 * </p>
 */
public class GrouperUiBrowserGroupFinder
extends GrouperUiBrowser {

  public GrouperUiBrowserGroupFinder(GrouperPage grouperPage) {
    super(grouperPage);
  }

  /**
   * Id Path in UI
   */
  private String groupToFindName;

  /**
   * Id Path in UI
   */
  public String getGroupToFindName() {
    return groupToFindName;
  }

  /**
   * Id Path in UI
   * @param groupToFindName
   * @return this object
   */
  public GrouperUiBrowserGroupFinder assignGroupToFindName(
      String groupToFindName) {

    this.groupToFindName = groupToFindName;
    return this;
  }

  /**
   * Uuid of the group
   * @param groupToFindName
   * @return this object
   */
  public GrouperUiBrowserGroupFinder assignGroupToFindId(
      String groupToFindId) {
    Group group = GroupFinder.findByUuid(groupToFindId, true);
    this.groupToFindName = group.getName();
    return this;
  }

  /**
   * Pass in a group object
   * @param groupToFindName
   * @return this object
   */
  public GrouperUiBrowserGroupFinder assignGroupToFind(
      Group group) {
    this.groupToFindName = group.getName();
    return this;
  }

  /**
   * Method used to find a group
   */
  public GrouperUiBrowserGroupFinder browse() {
    navigateToGroup(groupToFindName);
    return this;
  }
}