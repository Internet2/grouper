package edu.internet2.middleware.grouper.hooks;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;

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
  		GroupType fubGroup = GroupTypeFinder.find("fubGroup", true);
      group.addType(fubGroup);
      group.setAttribute("gid", "2");
      
      Set<String> dbVersionDifferentFields = group.dbVersionDifferentFields();

      //make sure dbVersion is ok
      if (dbVersionDifferentFields.size() != 1 
          || !StringUtils.equals("attribute__gid", dbVersionDifferentFields.iterator().next())) {
        throw new RuntimeException("Should have only changed gid: " + GrouperUtil.stringValue(dbVersionDifferentFields));
      }
      
      group.store();
		} catch (Exception e) {
		  throw new RuntimeException(e.getMessage(), e);
		}
	}	

	
}
