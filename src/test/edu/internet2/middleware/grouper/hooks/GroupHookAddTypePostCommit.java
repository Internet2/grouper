package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;

/**
 * add a type after a group insert
 */
public class GroupHookAddTypePostCommit extends edu.internet2.middleware.grouper.hooks.GroupHooks {

	/**
	 * 
	 * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
	 */
  @SuppressWarnings("unchecked")
	@Override
	public void groupPostCommitInsert(HooksContext hooksContext, HooksGroupBean postInsertBean) {

		super.groupPostInsert(hooksContext, postInsertBean);
		try {
  		Group group = postInsertBean.getGroup();
  		GroupType fubGroup = GroupTypeFinder.find("fubGroup");
      group.addType(fubGroup);
      group.setAttribute("gid", "2");
      group.store();
		} catch (Exception e) {
		  throw new RuntimeException(e.getMessage(), e);
		}
	}	

	
}
