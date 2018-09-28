/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

/**
 * @author vsachdeva
 *
 */
public class GrouperNewServiceTemplateLogic extends GrouperTemplateLogicBase {
  
  /**
   * cache for service actions per stem
   */
  private static final Map<String, List<ServiceAction>> serviceActions = new LinkedHashMap<String, List<ServiceAction>>();
  
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
    
    StemTemplateContainer templateContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getStemTemplateContainer();
    String baseStem = templateContainer.getTemplateKey();
    String baseStemFriendlyName = templateContainer.getTemplateFriendlyName();
    String baseStemdescription = StringUtils.isBlank(templateContainer.getTemplateDescription()) ? 
        TextContainer.retrieveFromRequest().getText().get("stemServiceBaseFolderDescription"): templateContainer.getTemplateDescription();
    
    if (!serviceActions.containsKey(stem.getName()+":"+baseStem)) {
      
      List<ServiceAction> serviceActionsForStem = new ArrayList<ServiceAction>();
      
      List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
    
      // Do you want a "apps:wiki" folder created
      args.add(new ServiceActionArgument("stemName", stem.getName()+":"+baseStem));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":"+baseStemFriendlyName));
      args.add(new ServiceActionArgument("stemDescription", baseStemdescription));
      ServiceAction rootServiceAction = createNewServiceAction(true, 0, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, null);
      serviceActionsForStem.add(rootServiceAction);
      
      //Do you want a "apps:wiki:service" folder created
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":"+baseStem+":service"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":"+baseStemFriendlyName+":service"));
      args.add(new ServiceActionArgument("stemDescription", baseStemdescription));
      ServiceAction levelOneServiceAction_One = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_One);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_One);
      
      //Do you want a "apps:wiki:service:policy" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":"+baseStem+":service:policy"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":"+baseStemFriendlyName+":service:policy"));
      args.add(new ServiceActionArgument("stemDescription", baseStemdescription));
      ServiceAction levelTwoServiceAction_One = createNewServiceAction(true, 2, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_One);
      levelOneServiceAction_One.addChildServiceAction(levelTwoServiceAction_One);
      
      //Do you want a "apps:wiki:service:reference" folder created? (extension is "ref", displayExtension is "reference")
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":"+baseStem+":service:reference"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":"+baseStemFriendlyName+":service:reference"));
      args.add(new ServiceActionArgument("stemDescription", baseStemdescription));
      ServiceAction levelTwoServiceAction_Two = createNewServiceAction(true, 2, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_Two);
      levelOneServiceAction_One.addChildServiceAction(levelTwoServiceAction_Two);
      
      //Do you want a "apps:wiki:service:attribute" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":"+baseStem+":service:attribute"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":"+baseStemFriendlyName+":service:attribute"));
      args.add(new ServiceActionArgument("stemDescription", baseStemdescription));
      ServiceAction levelTwoServiceAction_Three = createNewServiceAction(true, 2, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_Three);
      levelOneServiceAction_One.addChildServiceAction(levelTwoServiceAction_Three);
      
      //Do you want a "apps:wiki:security" folder created
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stem.getName()+":"+baseStem+":security"));
      args.add(new ServiceActionArgument("stemDisplayName", stem.getDisplayName()+":"+baseStemFriendlyName+":security"));
      args.add(new ServiceActionArgument("stemDescription", baseStemdescription));
      ServiceAction levelOneServiceAction_Two = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_Two);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_Two);
      
      //Do you want a "apps:wiki:security:wiki_admins" group created
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":"+baseStem+":security:"+baseStem+"_admins"));
      args.add(new ServiceActionArgument("groupDisplayName", stem.getDisplayName()+":"+baseStemFriendlyName+":security:"+baseStem+"_admins"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseGroupDescription")));
      ServiceAction levelTwoServiceAction_Four = createNewServiceAction(true, 2, "stemServiceBaseGroupCreationConfirmation", 
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Four);
      levelOneServiceAction_Two.addChildServiceAction(levelTwoServiceAction_Four);
      
      //Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Groups on the "apps:wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":"+baseStem+":security:"+baseStem+"_admins"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":"+baseStem));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "admin"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_One = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_One);
      levelTwoServiceAction_Four.addChildServiceAction(levelThreeServiceAction_One);
      
      //Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Folders on the "apps:wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":"+baseStem+":security:"+baseStem+"_admins"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":"+baseStem));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "stemAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Folders"));
      ServiceAction levelThreeServiceAction_Two = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_Two);
      levelTwoServiceAction_Four.addChildServiceAction(levelThreeServiceAction_Two);
      
      //Do you want "apps:wiki:security:wiki_admins" to have inherited ADMIN privileges on Attributes on the "apps:wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":"+baseStem+":security:"+baseStem+"_admins"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":"+baseStem));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "attrAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Attributes"));
      ServiceAction levelThreeServiceAction_Three = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation",
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_Three);
      levelTwoServiceAction_Four.addChildServiceAction(levelThreeServiceAction_Three);
      
      //Do you want a "apps:wiki:security:wiki_readers" group created
      args = new ArrayList<ServiceActionArgument>();
      rootServiceAction = new ServiceAction();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":"+baseStem+":security:"+baseStem+"_readers"));
      args.add(new ServiceActionArgument("groupDisplayName", stem.getDisplayName()+":"+baseStem+":security:"+baseStem+"_readers"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseGroupDescription")));
      ServiceAction levelTwoServiceAction_Five = createNewServiceAction(true, 2, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Five);
      levelOneServiceAction_Two.addChildServiceAction(levelTwoServiceAction_Five);
      
      //Do you want "apps:wiki:security:wiki_readers" to have inherited READ privileges on Groups on the "apps:wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":"+baseStem+":security:"+baseStem+"_readers"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":"+baseStem));
      args.add(new ServiceActionArgument("privilegeType", "READ"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "read"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_Four = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Five);
      serviceActionsForStem.add(levelThreeServiceAction_Four);
      levelTwoServiceAction_Five.addChildServiceAction(levelThreeServiceAction_Four);
      
      //Do you want a "apps:wiki:security:wiki_updaters" group created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":"+baseStem+":security:"+baseStem+"_updaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stem.getDisplayName()+":"+baseStem+":security:"+baseStem+"_updaters"));      
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceBaseGroupDescription")));
      ServiceAction levelTwoServiceAction_Six = createNewServiceAction(true, 2, "stemServiceBaseGroupCreationConfirmation", 
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Six);
      levelOneServiceAction_Two.addChildServiceAction(levelTwoServiceAction_Six);
      
      //Do you want "apps:wiki:security:wiki_updaters" to have inherited UPDATE privileges on Groups on the "apps:wiki:service" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stem.getName()+":"+baseStem+":security:"+baseStem+"_updaters"));
      args.add(new ServiceActionArgument("parentStemName", stem.getDisplayName()+":"+baseStem+":service"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_Five = createNewServiceAction(true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Five);
      serviceActionsForStem.add(levelThreeServiceAction_Five);
      levelTwoServiceAction_Six.addChildServiceAction(levelThreeServiceAction_Five);
      
      //Do you want "apps:wiki:security:wiki_updaters" to be a member of "apps:wiki:security:wiki_readers"?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupNameMembership", stem.getName()+":"+baseStem+":security:"+baseStem+"_updaters"));
      args.add(new ServiceActionArgument("groupNameMembershipOf", stem.getDisplayName()+":"+baseStem+":security:"+baseStem+"_readers"));
      ServiceAction levelThreeServiceAction_Six = createNewServiceAction(true, 3, "stemServiceBaseMemberAdditionConfirmation", 
          ServiceActionType.membership, args, levelTwoServiceAction_Five);
      serviceActionsForStem.add(levelThreeServiceAction_Six);
      levelTwoServiceAction_Six.addChildServiceAction(levelThreeServiceAction_Six);
      
      serviceActions.put(stem.getName()+":"+baseStem, serviceActionsForStem);
      
    }
    
    return serviceActions.get(stem.getName()+":"+baseStem);
    
  }
  
  /**
   * create new service action
   * @param defaulChecked
   * @param indentLevel
   * @param externalizedKey
   * @param type
   * @param args
   * @param parentServiceAction
   * @return
   */
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
  
  
  /**
   * external text property
   */
  public String getSelectLabelKey() {
    return "stemTemplateTypeServiceLabel";
  }
  
  

}
