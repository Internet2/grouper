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
/*
 * @author mchyzer
 * $Id: GroupAttributeNameValidationHook.java,v 1.6 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.subject.Subject;


/**
 * <pre>
 * built in hook to grouper, which is turned on when it is configured in the grouper.properties.
 * 
 * extensions in groups will be unique, and optionally you can resolve any subject to make 
 * sure the extension is not a netid or whatever
 * 
 * set that with grouper.properties:
 * 
 * hooks.group.class = edu.internet2.middleware.grouper.hooks.examples.GroupUniqueExtensionHook
 * 
 * groupUniqueExtensionHook.resolveSubjectByIdOrIdentifier = true
 * 
 * </pre>
 */
public class GroupUniqueExtensionHook extends GroupHooks {
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    Group group = preInsertBean.getGroup();
    verifyUniqueExtension(group);
  }

  /**
   * 
   * @param group
   */
  public static void verifyUniqueExtension(Group group) {
    
    //see if there is another group with the same extension
    long count = HibernateSession.byHqlStatic().createQuery("select count(g) from Group as g where g.extensionDb = :theExtension")
      .setString("theExtension", group.getExtension()).uniqueResult(long.class);
    if (count > 0) {
      throw new HookVeto("veto.group.unique.extension", "The group ID is already in use, please use a different ID");
    }
    
    //see if we are checking subjects
    if (GrouperConfig.getPropertyBoolean("groupUniqueExtensionHook.resolveSubjectByIdOrIdentifier", false)) {
      
      //resolve by id or identifier
      Subject subject = SubjectFinder.findByIdOrIdentifier(group.getExtension(), false);
      if (subject != null) {
        throw new HookVeto("veto.group.unique.extension", "The group ID is already in use, please use a different ID");
      }
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
    Group group = preUpdateBean.getGroup();
    if (group.dbVersionDifferentFields().contains(Group.FIELD_EXTENSION) || group.dbVersionDifferentFields().contains(Group.FIELD_NAME)) {
      verifyUniqueExtension(group);
    }
  }
  
}
