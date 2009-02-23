package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;

/**
 * add a type after a group insert
 */
public class GroupHookAddType extends
    edu.internet2.middleware.grouper.hooks.GroupHooks {

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void groupPostInsert(HooksContext hooksContext,
      HooksGroupBean postInsertBean) {

    super.groupPostInsert(hooksContext, postInsertBean);
    
    try {
      Group group = postInsertBean.getGroup();
      GroupType fubGroup = GroupTypeFinder.find("fubGroup");
      group.addType(fubGroup);
      group.setAttribute("gid", "2");
      group.store();
      
//      int sequenceNumber = HibernateSession.bySqlStatic().select(int.class, "select someSeq.nextval from dual");
//      List<Object> params = GrouperUtil.toList((Object)group.getUuid(), sequenceNumber);
//      HibernateSession.bySqlStatic().executeSql("insert into some_table (col1, col2) values (?, ?)",
//          params);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
