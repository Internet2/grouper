/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDefName;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;


/**
 * index page and common functions beans
 * @author mchyzer
 *
 */
public class IndexContainer {

  /**
   * for index page, this is a short list of groups the user manages
   */
  private Set<GuiGroup> guiGroupsUserManagesAbbreviated;

  /**
   * for index page, this is a short list of groups the user manages, lazy load if null
   * @return the list of groups
   */
  public Set<GuiGroup> getGuiGroupsUserManagesAbbreviated() {
    
    if (this.guiGroupsUserManagesAbbreviated == null) {
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession();
      Set<Group> groups = new GroupFinder().assignSubject(grouperSession.getSubject())
          .assignPrivileges(AccessPrivilege.MANAGE_PRIVILEGES)
          .assignQueryOptions(new QueryOptions().paging(10, 1, false)).findGroups();
      
      this.guiGroupsUserManagesAbbreviated = new LinkedHashSet<GuiGroup>();
      
      for (Group group : groups) {
        this.guiGroupsUserManagesAbbreviated.add(new GuiGroup(group));
      }
      
    }
    
    return this.guiGroupsUserManagesAbbreviated;
  }

  /**
   * 
   */
  private Set<GuiAttributeDefName> guiAttributeDefNamesMyServices;

  /**
   * for index page, this is a short list of user's services, lazy load if null
   * @return the list of services
   */
  public Set<GuiAttributeDefName> getGuiAttributeDefNamesMyServices() {
    
    if (this.guiAttributeDefNamesMyServices == null) {
      
      GrouperSession grouperSessionOuter = GrouperSession.staticGrouperSession();
      final Subject subject = grouperSessionOuter.getSubject();
      
      GrouperSession.callbackGrouperSession(grouperSessionOuter.internal_getRootSession(), new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
          Set<AttributeDefName> attributeDefNames = new AttributeDefNameFinder().assignAnyRole(true)
              .assignSubject(subject).assignQueryOptions(new QueryOptions().paging(10, 1, false))
              .findAttributeNames();
              
          IndexContainer.this.guiAttributeDefNamesMyServices = new LinkedHashSet<GuiAttributeDefName>();
          
          for (AttributeDefName attributeDefName : attributeDefNames) {
            IndexContainer.this.guiAttributeDefNamesMyServices.add(new GuiAttributeDefName(attributeDefName));
          }

          return null;
        }
      });
      
      
      
    }
    
    return this.guiAttributeDefNamesMyServices;
  }

  
  
  
}
