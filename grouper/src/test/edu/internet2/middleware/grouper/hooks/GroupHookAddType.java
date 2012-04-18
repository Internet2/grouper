/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
      GroupType fubGroup = GroupTypeFinder.find("fubGroup", true);
      group.addType(fubGroup);
      group.setAttribute("gid", "2");
      
//      int sequenceNumber = HibernateSession.bySqlStatic().select(int.class, "select someSeq.nextval from dual");
//      List<Object> params = GrouperUtil.toList((Object)group.getUuid(), sequenceNumber);
//      HibernateSession.bySqlStatic().executeSql("insert into some_table (col1, col2) values (?, ?)",
//          params);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
