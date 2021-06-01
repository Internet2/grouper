/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;

/**
 * @author vsachdeva
 *
 */
public class GrouperNewServiceTemplateLogic extends GrouperTemplateLogicBase {
  
  /**
  Do you want a "app:Wiki" folder created? (ID is "wiki", name is "Wiki")
    Assign "app" type to the "app:Wiki" folder?
  
    Do you want a "app:Wiki:service" folder created?
      Do you want a "app:Wiki:service:policy" folder created?
        Do you want the policy folder assigned the "policy group" type?
      Do you want a "app:Wiki:service:reference" folder created? (ID is "ref", name is "reference")
        Do you want the ref folder assigned the "ref" type?
      Do you want a "app:Wiki:service:attribute" folder created?
  
    Do you want a "app:Wiki:security" folder created?
      Do you want the security folder assigned the "grouperSecurity" type?
      Do you want a "app:Wiki:security:Wiki Admins" group created? (ID is "wikiAdmins", name is "Wiki Admins")
        Do you want "app:Wiki:security:Wiki Admins" to have inherited ADMIN privileges on Groups on the "app:Wiki" folder?
        Do you want "app:Wiki:security:Wiki Admins" to have inherited ADMIN privileges on Folders on the "app:Wiki" folder?
        Do you want "app:Wiki:security:Wiki Admins" to have inherited ADMIN privileges on Attributes on the "app:Wiki" folder?
    
      Do you want a "app:Wiki:security:Wiki Readers" group created? (ID is "wikiReaders", name is "Wiki Readers")
        Do you want "app:Wiki:security:Wiki Readers" to have inherited READ privileges on Groups on the "app:Wiki" folder?
      Do you want a "app:Wiki:security:Wiki Updaters" group created? (ID is "wikiUpdaters", name is "Wiki Updaters")
        Do you want "app:Wiki:security:Wiki Updaters" to have inherited UPDATE privileges on Groups on the "app:Wiki:service" folder?
        Do you want "app:Wiki:security:Wiki Updaters" to be a member of "app:wiki:security:Wiki Readers"?
   */
  @Override
  public List<ServiceAction> getServiceActions() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Stem stem = StemFinder.findByUuid(grouperSession, this.getStemId(), true);
    
    StemTemplateContainer templateContainer = this.getStemTemplateContainer();
    String baseStem = templateContainer.getTemplateKey();
    String baseStemFriendlyName = templateContainer.getTemplateFriendlyName();
    
    if (StringUtils.isBlank(baseStemFriendlyName)) {
      baseStemFriendlyName = baseStem;
    }
    
    String baseStemDescription = StringUtils.isBlank(templateContainer.getTemplateDescription()) ?
        TextContainer.retrieveFromRequest().getText().get("stemServiceBaseFolderDescription"): templateContainer.getTemplateDescription();
    
    String stemPrefix = "";
    String stemPrefixDisplayName = "";
    
    boolean addFirstNode = false;
    String optionalColon = "";
    
    if (StringUtils.isBlank(baseStem) && stem.isRootStem()) {
      stemPrefix = "";
      stemPrefixDisplayName = "";
      baseStem = "";
      baseStemFriendlyName = "";
    } else if (StringUtils.isBlank(baseStem) && !stem.isRootStem()) {
      stemPrefix = stem.getName();
      stemPrefixDisplayName = stem.getDisplayName();
      baseStem = "";
      baseStemFriendlyName = "";
      optionalColon = ":";
    } else if (StringUtils.isNotBlank(baseStem) && stem.isRootStem()) {
      stemPrefix = "";
      stemPrefixDisplayName = "";
      optionalColon = ":";
      addFirstNode = true;
    } else if (StringUtils.isNotBlank(baseStem) && !stem.isRootStem()) {
      stemPrefix = stem.getName()+":";
      stemPrefixDisplayName = stem.getDisplayName()+":";
      optionalColon = ":";
      addFirstNode = true;
    }

    
      List<ServiceAction> serviceActionsForStem = new ArrayList<ServiceAction>();
      
      List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
    
      //Do you want a "app:Wiki" folder created? (ID is "wiki", name is "Wiki")
      args.add(new ServiceActionArgument("stemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("stemDescription", baseStemDescription));
      ServiceAction rootServiceAction = createNewServiceAction("newAppStem", true, 0, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, null);
      
      if (addFirstNode) {
        serviceActionsForStem.add(rootServiceAction);
      }
      
      //Do you want a "app:Wiki:service" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+baseStem+optionalColon+"service"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"service"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceServiceFolderDescription")));
      ServiceAction levelOneServiceAction_One = createNewServiceAction("newAppServiceFolder", true, 1, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, 
          addFirstNode ? rootServiceAction: null);
      serviceActionsForStem.add(levelOneServiceAction_One);
      if (addFirstNode) {        
        rootServiceAction.addChildServiceAction(levelOneServiceAction_One);
      }
      
      //Do you want a "app:Wiki:service:policy" folder created?
      args = new ArrayList<ServiceActionArgument>();
      final String stemNamePolicy = stemPrefix+baseStem+optionalColon+"service:policy";
      args.add(new ServiceActionArgument("stemName", stemNamePolicy));
      final String stemDisplayNamePolicy = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"service:policy";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNamePolicy));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServicePolicyFolderDescription")));
      ServiceAction levelTwoServiceAction_One = createNewServiceAction("newAppPolicyFolder", true, 2, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_One);
      levelOneServiceAction_One.addChildServiceAction(levelTwoServiceAction_One);

//      {
//        //Assign the "policy" type to the "app:Wiki:service:policy" folder?
//        args = new ArrayList<ServiceActionArgument>();
//        args.add(new ServiceActionArgument("stemName", stemNamePolicy));
//        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNamePolicy));
//        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.POLICY));
//        ServiceAction policyTypeAction = createNewServiceAction("newAppPolicyType", true, 3, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
//        
//        serviceActionsForStem.add(policyTypeAction);
//        if (addFirstNode) {        
//          rootServiceAction.addChildServiceAction(policyTypeAction);
//        }
//      }
      
      //Do you want a "apps:wiki:service:reference" folder created? (id is "ref", name is "reference")
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameRef = stemPrefix+baseStem+optionalColon+"service:ref";
      args.add(new ServiceActionArgument("stemName", stemNameRef));
      final String stemDisplayNameRef = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"service:ref";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameRef));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceRefFolderDescription")));
      ServiceAction levelTwoServiceAction_Two = createNewServiceAction("newAppRefFolder", true, 2, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_Two);
      levelOneServiceAction_One.addChildServiceAction(levelTwoServiceAction_Two);

      {
        //Assign the "ref" type to the "app:Wiki:service:ref" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameRef));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameRef));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.REF));
        ServiceAction refTypeAction = createNewServiceAction("newAppRefType", true, 3, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(refTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(refTypeAction);
        }
      }
      
      //Do you want a "app:Wiki:service:attribute" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+baseStem+optionalColon+"service:attribute"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"service:attribute"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceAttributeFolderDescription")));
      ServiceAction levelTwoServiceAction_Three = createNewServiceAction("newAppAttributeFolder", true, 2, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, levelOneServiceAction_One);
      serviceActionsForStem.add(levelTwoServiceAction_Three);
      levelOneServiceAction_One.addChildServiceAction(levelTwoServiceAction_Three);
      
      //Do you want a "app:Wiki:security" folder created?
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameSecurity = stemPrefix+baseStem+optionalColon+"security";
      args.add(new ServiceActionArgument("stemName", stemNameSecurity));
      final String stemDisplayNameSecurity = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"security";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameSecurity));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceSecurityFolderDescription")));
      ServiceAction levelOneServiceAction_Two = createNewServiceAction("newAppSecurityFolder", true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, addFirstNode? rootServiceAction: null);
      serviceActionsForStem.add(levelOneServiceAction_Two);
      if (addFirstNode) {        
        rootServiceAction.addChildServiceAction(levelOneServiceAction_Two);
      }

      {
        //Assign the "grouperSecurity" type to the "app:Wiki:security" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameSecurity));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameSecurity));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.GROUPER_SECURITY));
        ServiceAction securityTypeAction = createNewServiceAction("newAppSecurityType", true, 2, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(securityTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(securityTypeAction);
        }
      }
      
      //Do you want a "app:Wiki:security:Wiki Admins" group created? (ID is "wikiAdmins", name is "Wiki Admins")
      args = new ArrayList<ServiceActionArgument>();
      String securityAdminsName = stemPrefix+baseStem+optionalColon+"security:"+baseStem+"Admins";
      args.add(new ServiceActionArgument("groupName", securityAdminsName));
      String securityAdminsDisplayName =  stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ") + "Admins";
      args.add(new ServiceActionArgument("groupDisplayName", securityAdminsDisplayName));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceSecurityAdminsGroupDescription")));
      ServiceAction levelTwoServiceAction_Four = createNewServiceAction("newAppAdminsGroup", true, 2, "stemServiceBaseGroupCreationConfirmation", 
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Four);
      levelOneServiceAction_Two.addChildServiceAction(levelTwoServiceAction_Four);
      
      //Do you want "app:Wiki:security:Wiki Admins" to have inherited ADMIN privileges on Groups on the "app:Wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", securityAdminsName));
      args.add(new ServiceActionArgument("groupDisplayName", securityAdminsDisplayName));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "admin"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_One = createNewServiceAction("newAppAdminPrivilege", true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_One);
      levelTwoServiceAction_Four.addChildServiceAction(levelThreeServiceAction_One);
      
      //Do you want "app:Wiki:security:Wiki Admins" to have inherited ADMIN privileges on Folders on the "app:Wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", securityAdminsName));
      args.add(new ServiceActionArgument("groupDisplayName", securityAdminsDisplayName));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "stemAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Folders"));
      ServiceAction levelThreeServiceAction_Two = createNewServiceAction("newAppAdminPrivilege2", true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_Two);
      levelTwoServiceAction_Four.addChildServiceAction(levelThreeServiceAction_Two);
      
      //Do you want "app:Wiki:security:Wiki Admins" to have inherited ADMIN privileges on Attributes on the "app:Wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", securityAdminsName));
      args.add(new ServiceActionArgument("groupDisplayName", securityAdminsDisplayName));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "attrAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Attributes"));
      ServiceAction levelThreeServiceAction_Three = createNewServiceAction("newAppAdminPrivilege3", true, 3, "stemServiceBasePrivilegeCreationConfirmation",
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Four);
      serviceActionsForStem.add(levelThreeServiceAction_Three);
      levelTwoServiceAction_Four.addChildServiceAction(levelThreeServiceAction_Three);
      
      //Do you want a "app:Wiki:security:Wiki Readers" group created? (ID is "wikiReaders", name is "Wiki Readers")
      args = new ArrayList<ServiceActionArgument>();
      String securityReadersName = stemPrefix+baseStem+optionalColon+"security:"+baseStem+"Readers";
      args.add(new ServiceActionArgument("groupName", securityReadersName));
      String securityReadersDisplayName = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ") +"Readers";
      args.add(new ServiceActionArgument("groupDisplayName", securityReadersDisplayName));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceSecurityReadersGroupDescription")));
      ServiceAction levelTwoServiceAction_Five = createNewServiceAction("newAppReadersGroup", true, 2, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Five);
      levelOneServiceAction_Two.addChildServiceAction(levelTwoServiceAction_Five);
      
      //Do you want "app:Wiki:security:Wiki Readers" to have inherited READ privileges on Groups on the "app:Wiki" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", securityReadersName));
      args.add(new ServiceActionArgument("groupDisplayName", securityReadersDisplayName));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("privilegeType", "READ"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "read"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_Four = createNewServiceAction("newAppReadersPrivilege", true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Five);
      serviceActionsForStem.add(levelThreeServiceAction_Four);
      levelTwoServiceAction_Five.addChildServiceAction(levelThreeServiceAction_Four);
      
      //Do you want a "app:Wiki:security:Wiki Updaters" group created? (ID is "wikiUpdaters", name is "Wiki Updaters")
      args = new ArrayList<ServiceActionArgument>();
      String securityUpdatersName = stemPrefix+baseStem+optionalColon+"security:"+baseStem+"Updaters";
      args.add(new ServiceActionArgument("groupName", securityUpdatersName));
      String securityUpdatersDisplayName = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Updaters";
      args.add(new ServiceActionArgument("groupDisplayName", securityUpdatersDisplayName));      
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemServiceSecurityUpdatersGroupDescription")));
      ServiceAction levelTwoServiceAction_Six = createNewServiceAction("newAppUpdatersPrivilege", true, 2, "stemServiceBaseGroupCreationConfirmation", 
          ServiceActionType.group, args, levelOneServiceAction_Two);
      serviceActionsForStem.add(levelTwoServiceAction_Six);
      levelOneServiceAction_Two.addChildServiceAction(levelTwoServiceAction_Six);
      
      //Do you want "app:Wiki:security:Wiki Updaters" to have inherited UPDATE privileges on Groups on the "app:Wiki:service" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", securityUpdatersName));
      args.add(new ServiceActionArgument("groupDisplayName", securityUpdatersDisplayName));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem+optionalColon+"service"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"service"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelThreeServiceAction_Five = createNewServiceAction("newAppUpdatersPrivilege2", true, 3, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelTwoServiceAction_Five);
      serviceActionsForStem.add(levelThreeServiceAction_Five);
      levelTwoServiceAction_Six.addChildServiceAction(levelThreeServiceAction_Five);
      
      //Do you want "app:Wiki:security:Wiki Updaters" to be a member of "app:Wiki:security:Wiki Readers"?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupNameMembership", securityUpdatersName));
      args.add(new ServiceActionArgument("groupNameMembershipDisplayName", securityUpdatersDisplayName));
      args.add(new ServiceActionArgument("groupNameMembershipOf", securityReadersName));
      args.add(new ServiceActionArgument("groupNameMembershipOfDisplayName", securityReadersDisplayName));
      ServiceAction levelThreeServiceAction_Six = createNewServiceAction("newAppReadersGroupMemberOfUpdaters", true, 3, "stemServiceBaseMemberAdditionConfirmation", 
          ServiceActionType.membership, args, levelTwoServiceAction_Five);
      serviceActionsForStem.add(levelThreeServiceAction_Six);
      levelTwoServiceAction_Six.addChildServiceAction(levelThreeServiceAction_Six);
      
    return serviceActionsForStem;
    
  }
  
  /**
   * external text property
   */
  public String getSelectLabelKey() {
    return "stemTemplateTypeServiceLabel";
  }

}
