/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks.examples;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;


/**
 * When groups are created or saved, make sure there are no name conflicts with other object types
 */
public class UniqueObjectGroupHook extends GroupHooks {

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
    checkGroupName(preUpdateBean.getGroup().getName(), preUpdateBean.getGroup().getAlternateName());
  }

  /**
   * make sure name is not used in other object types
   * @param name
   * @param alternateName
   */
  private void checkGroupName(String name, String alternateName) {
    //dont check groups
    UniqueObjectStemHook.assertNoStemsWithThisNameExist(name);
    UniqueObjectAttributeDefHook.assertNoAttributeDefsWithThisNameExist(name);
    UniqueObjectAttributeDefNameHook.assertNoAttributeDefNamesWithThisNameExist(name);
    if (!StringUtils.isBlank(alternateName)) {
      UniqueObjectStemHook.assertNoStemsWithThisNameExist(alternateName);
      UniqueObjectAttributeDefHook.assertNoAttributeDefsWithThisNameExist(alternateName);
      UniqueObjectAttributeDefNameHook.assertNoAttributeDefNamesWithThisNameExist(alternateName);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    checkGroupName(preInsertBean.getGroup().getName(), preInsertBean.getGroup().getAlternateName());
  }

  /**
   * make sure no groups by this name exist
   * @param name
   */
  public static void assertNoGroupsWithThisNameExist(final String name) {

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        Group group = GroupFinder.findByName(grouperSession, name, false);

        if (group != null) {
          throw new HookVeto("veto.uniqueObject.group.name", "The ID is already in use by a group, please use a different ID");
        }

        return null;
      }
    });

  }
  
}
