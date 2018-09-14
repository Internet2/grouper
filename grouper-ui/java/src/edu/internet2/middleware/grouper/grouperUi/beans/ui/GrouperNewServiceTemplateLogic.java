/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * @author vsachdeva
 *
 */
public class GrouperNewServiceTemplateLogic extends GrouperTemplateLogicBase {
  
  private static final Map<Stem, List<ServiceAction>> serviceActions = new LinkedHashMap<Stem, List<ServiceAction>>();
  
  /**
  Do you want a "apps:wiki" folder created?
    
    Do you want a "apps:wiki:service" folder created?
    
      Do you want a "apps:wiki:service:policy" folder created?
      Do you want a "apps:wiki:service:reference" folder created? (extension is "ref", displayExtension is "reference")
      Do you want a "apps:wiki:service:attribute" folder created?
    
    
    Do you want a "apps:wiki:security" folder created?
      
      Do you want a "apps:wiki:security:wiki_admins" group created?
        Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Groups on the "apps:wiki" folder?
        Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Folders on the "apps:wiki" folder?
        Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Attributes on the "apps:wiki" folder?
      
      Do you want a "apps:wiki:security:wiki_readers" group created?
        Do you want "apps:wiki:security:wiki_readers" to have inherited READ privileges on Groups on the "apps:wiki" folder?
      
      Do you want a "apps:wiki:security:wiki_updaters" group created?
        Do you want "apps:wiki:security:wiki_updaters" to have inherited UPDATE privileges on Groups on the "apps:wiki:service" folder?
        Do you want "apps:wiki:security:wiki_updaters" to be a member of "apps:wiki:security:wiki_readers"?
   */
  @Override
  public List<ServiceAction> getServiceActions() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Stem stem = StemFinder.findByUuid(grouperSession, this.getStemId(), true);
    
    if (!serviceActions.containsKey(stem)) {
      
      List<ServiceAction> serviceActionsForStem = new ArrayList<ServiceAction>();
      
      List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
    
      // Do you want a "apps:wiki" folder created
      args.add(new ServiceActionArgument("stemName", stem.getName()+":wiki"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":wiki"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseFolderDescription")));
      ServiceAction rootServiceAction = createNewServiceAction(true, 0, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, null);
      serviceActionsForStem.add(rootServiceAction);
      
      //Do you want a "apps:wiki:service" folder created
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":wiki:service"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":wiki:service"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseFolderDescription")));
      ServiceAction levelOneServiceAction_One = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_One);
      
      //Do you want a "apps:wiki:service:policy" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":wiki:service:policy"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":wiki:service:policy"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseFolderDescription")));
      ServiceAction levelTwoServiceAction_One = createNewServiceAction(true, 2, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_One);
      
      //Do you want a "apps:wiki:service:reference" folder created? (extension is "ref", displayExtension is "reference")
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":wiki:service:reference"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":wiki:service:reference"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseFolderDescription")));
      ServiceAction levelTwoServiceAction_Two = createNewServiceAction(true, 2, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_Two);
      
      //Do you want a "apps:wiki:service:attribute" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":wiki:service:attribute"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":wiki:service:attribute"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseFolderDescription")));
      ServiceAction levelTwoServiceAction_Three = createNewServiceAction(true, 2, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_Three);
      
      //Do you want a "apps:wiki:security" folder created
      args = new ArrayList<ServiceActionArgument>();
      rootServiceAction.setServiceActionType(ServiceActionType.stem);
      args.add(new ServiceActionArgument("stemName", stem.getName()+":wiki:security"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":wiki:security"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseFolderDescription")));
      ServiceAction levelOneServiceAction_Two = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_Two);
      
      //Do you want a "apps:wiki:security:wiki_admins" group created
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":wiki:security:wiki_admins"));
      args.add(new ServiceActionArgument("groupDisplayName", stem.getDisplayName()+":wiki:security:wiki_admins"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseGroupDescription")));
      ServiceAction levelTwoServiceAction_Four = createNewServiceAction(true, 2, "stemServiceBaseGroupCreationConfirmation", 
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Four);
      
      //Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Groups on the "apps:wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":wiki:security:wiki_admins"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":wiki"));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "admin"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_One = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_One);
      
      //Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Folders on the "apps:wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":wiki:security:wiki_admins"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":wiki"));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "stemAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Folders"));
      ServiceAction levelThreeServiceAction_Two = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_Two);
      
      //Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Attributes on the "apps:wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":wiki:security:wiki_admins"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":wiki"));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "attrAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Attributes"));
      ServiceAction levelThreeServiceAction_Three = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation",
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_Three);
      
      //Do you want a "apps:wiki:security:wiki_readers" group created
      args = new ArrayList<ServiceActionArgument>();
      rootServiceAction = new ServiceAction();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":wiki:security:wiki_readers"));
      args.add(new ServiceActionArgument("groupDisplayName", stem.getDisplayName()+":wiki:security:wiki_readers"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseGroupDescription")));
      ServiceAction levelTwoServiceAction_Five = createNewServiceAction(true, 2, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Five);
      
      //Do you want "apps:wiki:security:wiki_readers" to have inherited READ privileges on Groups on the "apps:wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":wiki:security:wiki_readers"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":wiki"));
      args.add(new ServiceActionArgument("privilegeType", "READ"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "read"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_Four = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Two);
      serviceActionsForStem.add(levelThreeServiceAction_Four);
      
      //Do you want a "apps:wiki:security:wiki_updaters" group created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":wiki:security:wiki_updaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stem.getDisplayName()+":wiki:security:wiki_updaters"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseGroupDescription")));
      ServiceAction levelTwoServiceAction_Six = createNewServiceAction(true, 2, "stemServiceBaseGroupCreationConfirmation", 
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Six);
      
      //Do you want "apps:wiki:security:wiki_updaters" to have inherited UPDATE privileges on Groups on the "apps:wiki:service" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":wiki:security:wiki_updaters"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":wiki:service"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_Five = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Five);
      serviceActionsForStem.add(levelThreeServiceAction_Five);
      
      //Do you want "apps:wiki:security:wiki_updaters" to be a member of "apps:wiki:security:wiki_readers"?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupNameMembership", stem.getName()+":wiki:security:wiki_updaters"));
      args.add(new ServiceActionArgument("groupNameMembershipOf", stem.getDisplayName()+":wiki:security:wiki_readers"));
      ServiceAction levelThreeServiceAction_Six = createNewServiceAction(true, 3, "stemServiceBaseMemberAdditionConfirmation", 
          ServiceActionType.membership, args, levelTwoServiceAction_Five);
      serviceActionsForStem.add(levelThreeServiceAction_Six);
      
      serviceActions.put(stem, serviceActionsForStem);
      
    }
    
    return serviceActions.get(stem);
    
  }
  
  private ServiceAction createNewServiceAction(boolean defaulChecked, int indentLevel, 
      String externalizedKey, ServiceActionType type, List<ServiceActionArgument> args,
      ServiceAction parentServiceAction) {
  
    ServiceAction serviceAction = new ServiceAction();
    serviceAction.setService(this);
    serviceAction.setDefaultChecked(defaulChecked);
    serviceAction.setIndentLevel(indentLevel);
    serviceAction.setExternalizedKey(externalizedKey);
    serviceAction.setServiceActionType(type);
    serviceAction.getArgs().addAll(args);
    serviceAction.setParentServiceAction(parentServiceAction);
    serviceAction.setId(GrouperUuid.getUuid());
    
    return serviceAction;
  }
  
  
  public String getSelectLabelKey() {
    return "stemTemplateTypeServiceLabel";
  }
  
  

}
