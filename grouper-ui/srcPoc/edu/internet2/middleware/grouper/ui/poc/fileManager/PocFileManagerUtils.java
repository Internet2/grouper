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

  /*
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.0','person','my name is test.subject.0');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.1','person','my name is test.subject.1');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.2','person','my name is test.subject.2');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.3','person','my name is test.subject.3');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.4','person','my name is test.subject.4');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.5','person','my name is test.subject.5');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.6','person','my name is test.subject.6');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.7','person','my name is test.subject.7');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.8','person','my name is test.subject.8');
  insert into `subject` (`subjectId`, `subjectTypeId`, `name`) values('test.subject.9','person','my name is test.subject.9');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.0','description','description.test.subject.0','description.test.subject.0');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.0','email','test.subject.0@somewhere.someSchool.edu','test.subject.0@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.0','loginid','id.test.subject.0','id.test.subject.0');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.0','name','name.test.subject.0','name.test.subject.0');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.1','description','description.test.subject.1','description.test.subject.1');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.1','email','test.subject.1@somewhere.someSchool.edu','test.subject.1@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.1','loginid','id.test.subject.1','id.test.subject.1');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.1','name','name.test.subject.1','name.test.subject.1');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.2','description','description.test.subject.2','description.test.subject.2');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.2','email','test.subject.2@somewhere.someSchool.edu','test.subject.2@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.2','loginid','id.test.subject.2','id.test.subject.2');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.2','name','name.test.subject.2','name.test.subject.2');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.3','description','description.test.subject.3','description.test.subject.3');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.3','email','test.subject.3@somewhere.someSchool.edu','test.subject.3@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.3','loginid','id.test.subject.3','id.test.subject.3');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.3','name','name.test.subject.3','name.test.subject.3');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.4','description','description.test.subject.4','description.test.subject.4');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.4','email','test.subject.4@somewhere.someSchool.edu','test.subject.4@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.4','loginid','id.test.subject.4','id.test.subject.4');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.4','name','name.test.subject.4','name.test.subject.4');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.5','description','description.test.subject.5','description.test.subject.5');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.5','email','test.subject.5@somewhere.someSchool.edu','test.subject.5@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.5','loginid','id.test.subject.5','id.test.subject.5');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.5','name','name.test.subject.5','name.test.subject.5');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.6','description','description.test.subject.6','description.test.subject.6');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.6','email','test.subject.6@somewhere.someSchool.edu','test.subject.6@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.6','loginid','id.test.subject.6','id.test.subject.6');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.6','name','name.test.subject.6','name.test.subject.6');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.7','description','description.test.subject.7','description.test.subject.7');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.7','email','test.subject.7@somewhere.someSchool.edu','test.subject.7@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.7','loginid','id.test.subject.7','id.test.subject.7');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.7','name','name.test.subject.7','name.test.subject.7');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.8','description','description.test.subject.8','description.test.subject.8');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.8','email','test.subject.8@somewhere.someSchool.edu','test.subject.8@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.8','loginid','id.test.subject.8','id.test.subject.8');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.8','name','name.test.subject.8','name.test.subject.8');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.9','description','description.test.subject.9','description.test.subject.9');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.9','email','test.subject.9@somewhere.someSchool.edu','test.subject.9@somewhere.someschool.edu');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.9','loginid','id.test.subject.9','id.test.subject.9');
  insert into `subjectattribute` (`subjectId`, `name`, `value`, `searchValue`) values('test.subject.9','name','name.test.subject.9','name.test.subject.9');
  */
  
  /** root stem */
  public static final String PSU_APPS_FILE_MANAGER_ROOT_STEM = "psu:apps:fileManager";

  /** attribute def name */
  public static final String PSU_APPS_FILE_MANAGER_PERMISSIONS_STEM = PSU_APPS_FILE_MANAGER_ROOT_STEM + ":permissions";

  /** attribute def name */
  public static final String PSU_APPS_FILE_MANAGER_PERMISSIONS_PERMISSION_DEFINITION_NAME = PSU_APPS_FILE_MANAGER_PERMISSIONS_STEM + ":permissionDefinition";

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
