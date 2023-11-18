/*******************************************************************************
 * Copyright 2015 Internet2
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
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;

/**
 * hook to prevent ldap provisioning by setting an attribute on the group.
 */

public class LDAPProvisioningHook extends GroupHooks {

 /**
  * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
  */

  @SuppressWarnings("deprecation")
  @Override
  public void groupPostInsert(HooksContext hooksContext, HooksGroupBean postInsertBean) {

    final Group group = postInsertBean.getGroup();
    String name = group.getName();

    boolean excludeMatches = false;
    int count = 0;
    while (true) {
      String property = "LDAPProvisioningHook.exclude.regex." + count;
      String regex = GrouperConfig.retrieveConfig().propertyValueString(property);
      if (regex == null) {
        break;
      }

      if (name.matches(regex)) {
        excludeMatches = true;
        break;
      }

      count++;
    }

    if (excludeMatches) {

      GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {

          GroupType groupType = GroupTypeFinder.find("LDAPProvisioning", false);
          if (groupType == null) {
            groupType = GroupType.createType(grouperSession, "LDAPProvisioning");
            groupType.addAttribute(grouperSession, "LDAPProvisioningExclude");
            groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
            groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
            groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, false);
            groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
          }

          group.addType(groupType);
          group.setAttribute("LDAPProvisioningExclude", "true");
          return null;
        }
      });
    }
  }
}
