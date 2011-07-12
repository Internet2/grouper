package edu.internet2.middleware.grouper.ui.poc.fileManager;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.group.TypeOfGroup;

/**
 * file manager proof of concept utils
 * @author mchyzer
 *
 */
public class PocFileManagerUtils {
  
  /** root stem */
  public static final String PSU_APPS_FILE_MANAGER_ROOT_STEM = "psu:apps:fileManager";

  /** attribute def name */
  public static final String PSU_APPS_FILE_MANAGER_PERMISSIONS_STEM = PSU_APPS_FILE_MANAGER_ROOT_STEM + ":permissions";

  /** attribute def name */
  public static final String PSU_APPS_FILE_MANAGER_PERMISSIONS_PERMISSION_DEFINITION_NAME = PSU_APPS_FILE_MANAGER_PERMISSIONS_STEM + ":fileMgrPermissionDef";

  /** user role name */
  public static final String PSU_APPS_FILE_MANAGER_ROLES_FILE_MANAGER_USER = PSU_APPS_FILE_MANAGER_ROOT_STEM + ":roles:fileManagerUser";

  /** read action */
  public static final String ACTION_READ = "read";

  /** create action */
  public static final String ACTION_CREATE = "create";
  
  /** admin action */
  public static final String ACTION_ADMIN = "admin";
  
  /**
   * init grouper if not initted about the poc file manager
   * add a root folder
   * add an attribute def
   * add actions for attribute def
   * add a role
   */
  public static void initGrouperIfNotInitted() {
    new StemSave(GrouperSession.staticGrouperSession()).assignCreateParentStemsIfNotExist(true)
      .assignName(PSU_APPS_FILE_MANAGER_ROOT_STEM).save();
    
    AttributeDef permissionsDef = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignCreateParentStemsIfNotExist(true)
      .assignName(PSU_APPS_FILE_MANAGER_PERMISSIONS_PERMISSION_DEFINITION_NAME)
      .assignAttributeDefType(AttributeDefType.perm)
      .assignToEffMembership(true).assignToGroup(true).save();
    
   // permissionsDef.getAttributeDefActionDelegate().configureActionList(ACTION_READ + "," + ACTION_CREATE);
    permissionsDef.getAttributeDefActionDelegate().configureActionList(ACTION_READ + "," + ACTION_CREATE + "," + ACTION_ADMIN);
    
    //admin should imply read and create
    AttributeAssignAction adminAction = permissionsDef.getAttributeDefActionDelegate().allowedAction(ACTION_ADMIN, true);
    AttributeAssignAction readAction = permissionsDef.getAttributeDefActionDelegate().allowedAction(ACTION_READ, true);
    AttributeAssignAction createAction = permissionsDef.getAttributeDefActionDelegate().allowedAction(ACTION_CREATE, true);
    
    adminAction.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(readAction);
    adminAction.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(createAction);
    
    new GroupSave(GrouperSession.staticGrouperSession()).assignName(PSU_APPS_FILE_MANAGER_ROLES_FILE_MANAGER_USER)
      .assignCreateParentStemsIfNotExist(true).assignTypeOfGroup(TypeOfGroup.role)
      .save();
  }
  
  
  
}
