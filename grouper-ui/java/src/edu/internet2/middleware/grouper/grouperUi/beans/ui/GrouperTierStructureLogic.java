package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;

public class GrouperTierStructureLogic extends GrouperTemplateLogicBase {
  
  /**
   * cache for service actions per stem
   */
  private static final Map<String, List<ServiceAction>> serviceActions = new LinkedHashMap<String, List<ServiceAction>>();

  @Override
  public List<ServiceAction> getServiceActions() {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Stem stem = StemFinder.findByUuid(grouperSession, this.getStemId(), true);
    
    String baseStemDescription = TextContainer.retrieveFromRequest().getText().get("stemTierBaseFolderDescription");
    
    String stemPrefix = "";
    String stemPrefixDisplayName = "";
    if (!stem.isRootStem()) {
      stemPrefix = stem.getName()+":";
      stemPrefixDisplayName = stem.getDisplayName()+":";
    }
    
    /**
     * 
      Do you want a "org:Engineering School" folder created? (ID is "engineerSchool", name is "Engineering School")
      
        Do you want a "org:Engineering School:basis" folder created? 
        Do you want a "org:Engineering School:ref" folder created?
        Do you want a "org:Engineering School:bundle" folder created?
        Do you want a "org:Engineering School:app" folder created?
        Do you want a "org:Engineering School:org?
        Do you want a "org:Engineering School:test" folder created?
        Do you want a "org:Engineering School:etc" folder created?
        
          Do you want a "org:Engineering School:etc:security" folder created?
            Do you want a "org:Engineering School:etc:security:Engineering School Admins" group created? (ID is "engineeringSchoolAdmins", name is "Engineering School Admins")
          
              Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Groups on the "org:Engineering School" folder?
              Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Folders on the "org:Engineering School" folder?
              Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Attributes on the "org:Engineering School" folder?
      
            Do you want a "org:Engineering School:etc:security:Engineering School Readers" group created? (ID is "engineeringSchoolReaders", name is "Engineering School Readers")
              Do you want "org:Engineering School:etc:security:Engineering School Readers" to have inherited READ privileges on Groups on the "org:Engineering School" folder?
            
            Do you want a "org:Engineering School:etc:security:Engineering School Updaters" group created? (ID is "engineeringSchoolUpdaters", name is "Engineering School Updaters")
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:basis" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:reference" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:bundle" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:application" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:organization" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:test" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to be a member of "org:Engineering School:etc:security:Engineering School Readers"?
     */
    
    if (!serviceActions.containsKey(stemPrefix)) {
      
      List<ServiceAction> serviceActionsForStem = new ArrayList<ServiceAction>();
      
      List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
    
      //Do you want a "org:Engineering School" folder created? (ID is "engineerSchool", name is "Engineering School")
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School"));
      args.add(new ServiceActionArgument("stemDescription", baseStemDescription));
      ServiceAction rootServiceAction = createNewServiceAction(true, 0, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, null);
      serviceActionsForStem.add(rootServiceAction);
      
      //Do you want a "org:Engineering School:basis" folder created? 
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool:basis"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School:basis"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierBasisFolderDescription")));
      ServiceAction levelOneServiceAction_One = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_One);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_One);
      
      //Do you want a "org:Engineering School:ref" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool:ref"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School:ref"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierRefFolderDescription")));
      ServiceAction levelOneServiceAction_Two = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_Two);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_Two);
      
      //Do you want a "org:Engineering School:bundle" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool:bundle"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School:bundle"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierBundleFolderDescription")));
      ServiceAction levelOneServiceAction_Three = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_Three);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_Three);
      
      //Do you want a "org:Engineering School:app" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool:app"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School:app"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierAppFolderDescription")));
      ServiceAction levelOneServiceAction_Four = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_Four);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_Four);
      
      //Do you want a "org:Engineering School:org?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool:org"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School:org"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierOrgFolderDescription")));
      ServiceAction levelOneServiceAction_Five = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_Five);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_Five);
      
      //Do you want a "org:Engineering School:test" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool:test"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School:test"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierTestFolderDescription")));
      ServiceAction levelOneServiceAction_Six = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_Six);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_Six);
      
      //Do you want a "org:Engineering School:etc" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool:etc"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School:etc"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierEtcFolderDescription")));
      ServiceAction levelOneServiceAction_Seven = createNewServiceAction(true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelOneServiceAction_Seven);
      rootServiceAction.addChildServiceAction(levelOneServiceAction_Seven);
      
      
      //Do you want a "org:Engineering School:etc:security" folder created?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("stemName", stemPrefix+"engineerSchool:etc:security"));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+"Engineering School:etc:security"));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierSecurityFolderDescription")));
      ServiceAction levelTwoServiceAction_One = createNewServiceAction(true, 2, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, rootServiceAction);
      serviceActionsForStem.add(levelTwoServiceAction_One);
      levelOneServiceAction_Seven.addChildServiceAction(levelTwoServiceAction_One);
      
      //Do you want a "org:Engineering School:etc:security:Engineering School Admins" group created? (ID is "engineeringSchoolAdmins", name is "Engineering School Admins")
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolAdmins"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Admins"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemTierSecurityAdminsGroupDescription")));
      ServiceAction levelThreeServiceAction_One = createNewServiceAction(true, 3, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelTwoServiceAction_One);
      serviceActionsForStem.add(levelThreeServiceAction_One);
      levelTwoServiceAction_One.addChildServiceAction(levelThreeServiceAction_One);
      
      //Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Groups on the "org:Engineering School" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolAdmins"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Admins"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School"));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "admin"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_One = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_One);
      serviceActionsForStem.add(levelFourServiceAction_One);
      levelThreeServiceAction_One.addChildServiceAction(levelFourServiceAction_One);
      
      //Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Folders on the "org:Engineering School" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolAdmins"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Admins"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School"));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "stemAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Folders"));
      ServiceAction levelFourServiceAction_Two = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_One);
      serviceActionsForStem.add(levelFourServiceAction_Two);
      levelThreeServiceAction_One.addChildServiceAction(levelFourServiceAction_Two);
      
      //Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Attributes on the "org:Engineering School" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolAdmins"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Admins"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School"));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "attrAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Attributes"));
      ServiceAction levelFourServiceAction_Three = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_One);
      serviceActionsForStem.add(levelFourServiceAction_Three);
      levelThreeServiceAction_One.addChildServiceAction(levelFourServiceAction_Three);
      
      //Do you want a "org:Engineering School:etc:security:Engineering School Readers" group created? (ID is "engineeringSchoolReaders", name is "Engineering School Readers")
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolReaders"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Readers"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemTierSecurityReadersGroupDescription")));
      ServiceAction levelThreeServiceAction_Two = createNewServiceAction(true, 3, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelTwoServiceAction_One);
      serviceActionsForStem.add(levelThreeServiceAction_Two);
      levelTwoServiceAction_One.addChildServiceAction(levelThreeServiceAction_Two);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Readers" to have inherited READ privileges on Groups on the "org:Engineering School" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolReaders"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Readers"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School"));
      args.add(new ServiceActionArgument("privilegeType", "READ"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "read"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Four = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Two);
      serviceActionsForStem.add(levelFourServiceAction_Four);
      levelThreeServiceAction_Two.addChildServiceAction(levelFourServiceAction_Four);
      
      //Do you want a "org:Engineering School:etc:security:Engineering School Updaters" group created? (ID is "engineeringSchoolUpdaters", name is "Engineering School Updaters")
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolUpdaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Updaters"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemTierSecurityUpdatersGroupDescription")));
      ServiceAction levelThreeServiceAction_Three = createNewServiceAction(true, 3, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelTwoServiceAction_One);
      serviceActionsForStem.add(levelThreeServiceAction_Three);
      levelTwoServiceAction_One.addChildServiceAction(levelThreeServiceAction_Three);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:basis" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolUpdaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool:basis"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School:basis"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Five = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Five);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Five);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:reference" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolUpdaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool:ref"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School:reference"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Six = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Six);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Six);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:bundle" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolUpdaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool:bundle"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School:bundle"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Seven = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Seven);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Seven);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:application" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolUpdaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool:app"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School:application"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Eight = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Eight);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Eight);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:organization" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolUpdaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool:org"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School:organization"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Nine = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Nine);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Nine);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:test" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+"engineerSchool:etc:security:engineeringSchoolUpdaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+"engineerSchool:test"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+"Engineering School:test"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Ten = createNewServiceAction(true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Ten);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Ten);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to be a member of "org:Engineering School:etc:security:Engineering School Readers"?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupNameMembership", stemPrefix+"engineerSchool:etc:security:engineeringSchoolUpdaters"));
      args.add(new ServiceActionArgument("groupNameMembershipDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Updaters"));
      args.add(new ServiceActionArgument("groupNameMembershipOf", stemPrefix+"engineerSchool:etc:security:engineeringSchoolReaders"));
      args.add(new ServiceActionArgument("groupNameMembershipOfDisplayName", stemPrefixDisplayName+"Engineering School:etc:security:Engineering School Readers"));
      ServiceAction levelFourServiceAction_Eleven = createNewServiceAction(true, 4, "stemServiceBaseMemberAdditionConfirmation", 
          ServiceActionType.membership, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Eleven);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Eleven);
      
      serviceActions.put(stemPrefix, serviceActionsForStem);
      
    }
    
    return serviceActions.get(stemPrefix);
  }

  @Override
  public String getSelectLabelKey() {
    return "stemTemplateTypeTierStructureLabel";
  }

  
  /**
   * Should user be asked for key, friendly name and description?
   * @return
   */
  @Override
  public boolean isPromptForKeyAndLabelAndDescription() {
    return false;
  }
}
