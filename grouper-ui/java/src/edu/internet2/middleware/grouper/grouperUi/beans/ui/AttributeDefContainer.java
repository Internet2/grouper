package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiAttributeDef;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;


/**
 * attribute definition container in new ui
 * @author mchyzer
 *
 */
public class AttributeDefContainer {

  /**
   * gui attribute def from url
   */
  private GuiAttributeDef guiAttributeDef;
  /**
   * if the logged in user can admin group, lazy loaded
   */
  private Boolean canAdmin;
  /**
   * if the logged in user can read group, lazy loaded
   */
  private Boolean canRead;
  /**
   * if the logged in user can update group, lazy loaded
   */
  private Boolean canUpdate;
  /**
   * if the logged in user can view group, lazy loaded
   */
  private Boolean canView;
  /**
   * if show add member on the folder privileges screen
   */
  private boolean showAddMember = false;

  /**
   * gui attribute def from url
   * @return gui attribute def
   */
  public GuiAttributeDef getGuiAttributeDef() {
    return this.guiAttributeDef;
  }

  /**
   * gui attribute def from url
   * @param guiAttributeDef1
   */
  public void setGuiAttributeDef(GuiAttributeDef guiAttributeDef1) {
    this.guiAttributeDef = guiAttributeDef1;
  }

  /**
   * if the logged in user can admin, lazy loaded
   * @return if can admin
   */
  public boolean isCanAdmin() {
    
    if (this.canAdmin == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canAdmin = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef().getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_ADMIN.getName(), false);
            }
          });
    }
    
    return this.canAdmin;
  }

  /**
   * if the logged in user can read, lazy loaded
   * @return if can read
   */
  public boolean isCanRead() {
    
    if (this.canRead == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canRead = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef().getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_READ.getName(), false);
            }
          });
    }
    
    return this.canRead;
  }

  /**
   * if the logged in user can update, lazy loaded
   * @return if can update
   */
  public boolean isCanUpdate() {
    
    if (this.canUpdate == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canUpdate = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef().getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_UPDATE.getName(), false);
            }
          });
    }
    
    return this.canUpdate;
  }

  /**
   * if the logged in user can view, lazy loaded
   * @return if can view
   */
  public boolean isCanView() {
    
    if (this.canView == null) {
      
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      
      this.canView = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
            
            @Override
            public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
              return AttributeDefContainer.this.getGuiAttributeDef().getAttributeDef().getPrivilegeDelegate().canHavePrivilege(loggedInSubject, AttributeDefPrivilege.ATTR_VIEW.getName(), false);
            }
          });
    }
    
    return this.canView;
  }

  /**
   * if show add member on the folder privileges screen
   * @return the showAddMember
   */
  public boolean isShowAddMember() {
    return this.showAddMember;
  }

  /**
   * if show add member on the folder privileges screen
   * @param showAddMember1 the showAddMember to set
   */
  public void setShowAddMember(boolean showAddMember1) {
    this.showAddMember = showAddMember1;
  }
  
  
  
}
