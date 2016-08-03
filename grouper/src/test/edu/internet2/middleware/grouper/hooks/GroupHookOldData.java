/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks;

import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GroupHookOldData extends GroupHooks {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    new GroupSave(grouperSession).assignName("test:testGroup")
        .assignDescription("firstDescription")
        .assignCreateParentStemsIfNotExist(true).save();
    System.out.println("Before description update");
    new GroupSave(grouperSession).assignName("test:testGroup")
        .assignDescription("secondDescription")
        .assignCreateParentStemsIfNotExist(true).save();
  }
  
  /**
   * 
   * @param group
   * @param attributeOrField
   * @return the object
   */
  private static Object attributeOrFieldValue(Group group, String attributeOrField) {
    Field field = GrouperUtil.field(group.getClass(), attributeOrField, true, false);
    if (field != null) {
      return GrouperUtil.propertyValue(group, attributeOrField);
    }
    return group.getAttributeValue(attributeOrField, false, true);
  }
  
  /**
   * 
   */
  public GroupHookOldData() {
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostUpdate(HooksContext hooksContext, HooksGroupBean postUpdateBean) {
    
    Group group = postUpdateBean.getGroup();
    Group oldGroup = oldGroupInUpdate.get();

    //clean it up
    oldGroupInUpdate.remove();
    
    if (oldGroup != null && !StringUtils.equals(group.getId(), oldGroup.getId())) {
      oldGroup = null;
    }
    
    System.out.println("groupPostUpdate");
    for (String fieldName : GrouperUtil.nonNull(group.dbVersionDifferentFields())) {
      System.out.println("groupPostUpdate: field changed: " + fieldName + ", old value: " 
          + (oldGroup == null ? "" : attributeOrFieldValue(oldGroup, fieldName)) + ", new value: " + attributeOrFieldValue(group, fieldName));
    }
    
  }

  /**
   * store the old group
   */
  private static ThreadLocal<Group> oldGroupInUpdate = new InheritableThreadLocal<Group>();
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean postUpdateBean) {
    
    Group group = postUpdateBean.getGroup();
    Group oldGroup = group.dbVersion();
    
    //set this in thread local to be accessed by post update
    oldGroupInUpdate.set(oldGroup);
    
    for (String fieldName : GrouperUtil.nonNull(group.dbVersionDifferentFields())) {
      System.out.println("groupPreUpdate: field changed: " + fieldName + ", old value: " 
          + (oldGroup == null ? "" : attributeOrFieldValue(oldGroup, fieldName)) + ", new value: " + attributeOrFieldValue(group, fieldName));
    }
    
  }

}
