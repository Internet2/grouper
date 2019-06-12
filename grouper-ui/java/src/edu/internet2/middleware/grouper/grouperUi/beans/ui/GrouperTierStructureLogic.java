package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings;

public class GrouperTierStructureLogic extends GrouperTemplateLogicBase {
  
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
        TextContainer.retrieveFromRequest().getText().get("stemTierBaseFolderDescription"): templateContainer.getTemplateDescription();
    
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
    
    /**
     * 
      Do you want a "org:Engineering School" folder created? (ID is "engineerSchool", name is "Engineering School")
      
        Do you want a "org:Engineering School:basis" folder created? 
        Do you want a "org:Engineering School:ref" folder created?
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
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:application" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:organization" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:test" folder?
              Do you want "org:Engineering School:etc:security:Engineering School Updaters" to be a member of "org:Engineering School:etc:security:Engineering School Readers"?
     */
    
      List<ServiceAction> serviceActionsForStem = new ArrayList<ServiceAction>();
      
      List<ServiceActionArgument> args = new ArrayList<ServiceActionArgument>();
    
      //Do you want a "org:Engineering School" folder created? (ID is "engineerSchool", name is "Engineering School")
      args.add(new ServiceActionArgument("stemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("stemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("stemDescription", baseStemDescription));
      ServiceAction rootServiceAction = createNewServiceAction("tierBaseFolder", true, 0, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, null);
      
      if (addFirstNode) {
        serviceActionsForStem.add(rootServiceAction);
      }
      
      //Do you want a "org:Engineering School:basis" folder created? 
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameBasis = stemPrefix+baseStem+optionalColon+"basis";
      args.add(new ServiceActionArgument("stemName", stemNameBasis));
      final String stemDisplayNameBasis = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"basis";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameBasis));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierBasisFolderDescription")));
      ServiceAction levelOneServiceAction_One = createNewServiceAction("tierBasisFolder", true, 1, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args,
          addFirstNode? rootServiceAction: null);
      serviceActionsForStem.add(levelOneServiceAction_One);
      if (addFirstNode) {        
        rootServiceAction.addChildServiceAction(levelOneServiceAction_One);
      }

      {
        //Assign the "basis" type to the "org:Engineering School:basis" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameBasis));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameBasis));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.BASIS));
        ServiceAction basisTypeAction = createNewServiceAction("tierBasisType", true, 2, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(basisTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(basisTypeAction);
        }
      }
      
      //Do you want a "org:Engineering School:ref" folder created?
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameRef = stemPrefix+baseStem+optionalColon+"ref";
      args.add(new ServiceActionArgument("stemName", stemNameRef));
      final String stemDisplayNameRef = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"ref";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameRef));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierRefFolderDescription")));
      ServiceAction levelOneServiceAction_Two = createNewServiceAction("tierRefFolder", true, 1, "stemServiceBaseFolderCreationConfirmation", ServiceActionType.stem, args, 
          addFirstNode? rootServiceAction: null);
      serviceActionsForStem.add(levelOneServiceAction_Two);
      if (addFirstNode) {
        rootServiceAction.addChildServiceAction(levelOneServiceAction_Two);
      }

      {
        //Assign the "ref" type to the "org:Engineering School:ref" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameRef));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameRef));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.REF));
        ServiceAction refTypeAction = createNewServiceAction("tierRefType", true, 2, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(refTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(refTypeAction);
        }
      }

      //Do you want a "org:Engineering School:app" folder created?
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameApp = stemPrefix+baseStem+optionalColon+"app";
      args.add(new ServiceActionArgument("stemName", stemNameApp));
      final String stemDisplayNameApp = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"app";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameApp));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierAppFolderDescription")));
      ServiceAction levelOneServiceAction_Three = createNewServiceAction("tierAppFolder", true, 1, "stemServiceBaseFolderCreationConfirmation",
          ServiceActionType.stem, args, addFirstNode? rootServiceAction: null);
      serviceActionsForStem.add(levelOneServiceAction_Three);
      if (addFirstNode) {        
        rootServiceAction.addChildServiceAction(levelOneServiceAction_Three);
      }

      {
        //Assign the "app" type to the "org:Engineering School:app" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameApp));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameApp));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.APP));
        ServiceAction appTypeAction = createNewServiceAction("tierAppType", true, 2, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(appTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(appTypeAction);
        }
      }


      //Do you want a "org:Engineering School:org?
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameOrg = stemPrefix+baseStem+optionalColon+"org";
      args.add(new ServiceActionArgument("stemName", stemNameOrg));
      final String stemDisplayNameOrg = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"org";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameOrg));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierOrgFolderDescription")));
      ServiceAction levelOneServiceAction_Four = createNewServiceAction("tierOrgFolder", true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, addFirstNode? rootServiceAction: null);
      serviceActionsForStem.add(levelOneServiceAction_Four);
      if (addFirstNode) {        
        rootServiceAction.addChildServiceAction(levelOneServiceAction_Four);
      }

      {
        //Assign the "org" type to the "org:Engineering School:org" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameOrg));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameOrg));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.ORG));
        ServiceAction orgTypeAction = createNewServiceAction("tierOrgType", true, 2, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(orgTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(orgTypeAction);
        }
      }

      //Do you want a "org:Engineering School:test" folder created?
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameTest = stemPrefix+baseStem+optionalColon+"test";
      args.add(new ServiceActionArgument("stemName", stemNameTest));
      final String stemDisplayNameTest = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"test";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameTest));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierTestFolderDescription")));
      ServiceAction levelOneServiceAction_Five = createNewServiceAction("tierTestFolder", true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, addFirstNode? rootServiceAction: null);
      serviceActionsForStem.add(levelOneServiceAction_Five);
      if (addFirstNode) {        
        rootServiceAction.addChildServiceAction(levelOneServiceAction_Five);
      }

      {
        //Assign the "test" type to the "org:Engineering School:test" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameTest));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameTest));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.TEST));
        ServiceAction testTypeAction = createNewServiceAction("tierTestType", true, 2, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(testTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(testTypeAction);
        }
      }

      //Do you want a "org:Engineering School:etc" folder created?
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameEtc = stemPrefix+baseStem+optionalColon+"etc";
      args.add(new ServiceActionArgument("stemName", stemNameEtc));
      final String stemDisplayNameEtc = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameEtc));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierEtcFolderDescription")));
      ServiceAction levelOneServiceAction_Six = createNewServiceAction("tierEtcFolder", true, 1, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, addFirstNode? rootServiceAction: null);
      serviceActionsForStem.add(levelOneServiceAction_Six);
      if (addFirstNode) {        
        rootServiceAction.addChildServiceAction(levelOneServiceAction_Six);
      }

      {
        //Assign the "etc" type to the "org:Engineering School:etc" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameEtc));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameEtc));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.ETC));
        ServiceAction etcTypeAction = createNewServiceAction("tierEtcType", true, 2, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(etcTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(etcTypeAction);
        }
      }
      
      //Do you want a "org:Engineering School:etc:security" folder created?
      args = new ArrayList<ServiceActionArgument>();
      final String stemNameSecurity = stemPrefix+baseStem+optionalColon+"etc:security";
      args.add(new ServiceActionArgument("stemName", stemNameSecurity));
      final String stemDisplayNameSecurity = stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security";
      args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameSecurity));
      args.add(new ServiceActionArgument("stemDescription", TextContainer.retrieveFromRequest().getText().get("stemTierSecurityFolderDescription")));
      ServiceAction levelTwoServiceAction_One = createNewServiceAction("tierSecurityFolder", true, 2, "stemServiceBaseFolderCreationConfirmation", 
          ServiceActionType.stem, args, addFirstNode? rootServiceAction: null);
      serviceActionsForStem.add(levelTwoServiceAction_One);
      levelOneServiceAction_Six.addChildServiceAction(levelTwoServiceAction_One);

      {
        //Assign the "security" type to the "org:Engineering School:security" folder?
        args = new ArrayList<ServiceActionArgument>();
        args.add(new ServiceActionArgument("stemName", stemNameSecurity));
        args.add(new ServiceActionArgument("stemDisplayName", stemDisplayNameSecurity));
        args.add(new ServiceActionArgument("type", GrouperObjectTypesSettings.GROUPER_SECURITY));
        ServiceAction securityTypeAction = createNewServiceAction("tierSecurityType", true, 3, "stemServiceFolderTypeConfirmation", ServiceActionType.grouperType, args, null);
        
        serviceActionsForStem.add(securityTypeAction);
        if (addFirstNode) {        
          rootServiceAction.addChildServiceAction(securityTypeAction);
        }
      }
      
      //Do you want a "org:Engineering School:etc:security:Engineering School Admins" group created? (ID is "engineeringSchoolAdmins", name is "Engineering School Admins")
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Admins"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Admins"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemTierSecurityAdminsGroupDescription")));
      ServiceAction levelThreeServiceAction_One = createNewServiceAction("tierAdminsGroup", true, 3, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelTwoServiceAction_One);
      serviceActionsForStem.add(levelThreeServiceAction_One);
      levelTwoServiceAction_One.addChildServiceAction(levelThreeServiceAction_One);
      
      //Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Groups on the "org:Engineering School" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Admins"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Admins"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "admin"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_One = createNewServiceAction("tierAdminsPrivilege", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_One);
      serviceActionsForStem.add(levelFourServiceAction_One);
      levelThreeServiceAction_One.addChildServiceAction(levelFourServiceAction_One);
      
      //Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Folders on the "org:Engineering School" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Admins"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Admins"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "stemAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Folders"));
      ServiceAction levelFourServiceAction_Two = createNewServiceAction("tierAdminsPrivilege2", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_One);
      serviceActionsForStem.add(levelFourServiceAction_Two);
      levelThreeServiceAction_One.addChildServiceAction(levelFourServiceAction_Two);
      
      //Do you want "org:Engineering School:etc:security:Engineering School_admins" to have inherited ADMIN privileges on Attributes on the "org:Engineering School" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Admins"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Admins"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("privilegeType", "ADMIN"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "attrAdmin"));
      args.add(new ServiceActionArgument("templateItemType", "Attributes"));
      ServiceAction levelFourServiceAction_Three = createNewServiceAction("tierAdminsPrivilege3", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_One);
      serviceActionsForStem.add(levelFourServiceAction_Three);
      levelThreeServiceAction_One.addChildServiceAction(levelFourServiceAction_Three);
      
      //Do you want a "org:Engineering School:etc:security:Engineering School Readers" group created? (ID is "engineeringSchoolReaders", name is "Engineering School Readers")
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Readers"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Readers"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemTierSecurityReadersGroupDescription")));
      ServiceAction levelThreeServiceAction_Two = createNewServiceAction("tierReadersGroup", true, 3, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelTwoServiceAction_One);
      serviceActionsForStem.add(levelThreeServiceAction_Two);
      levelTwoServiceAction_One.addChildServiceAction(levelThreeServiceAction_Two);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Readers" to have inherited READ privileges on Groups on the "org:Engineering School" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Readers"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Readers"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName));
      args.add(new ServiceActionArgument("privilegeType", "READ"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "read"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Four = createNewServiceAction("tierReadersPrivilege", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Two);
      serviceActionsForStem.add(levelFourServiceAction_Four);
      levelThreeServiceAction_Two.addChildServiceAction(levelFourServiceAction_Four);
      
      //Do you want a "org:Engineering School:etc:security:Engineering School Updaters" group created? (ID is "engineeringSchoolUpdaters", name is "Engineering School Updaters")
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Updaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Updaters"));
      args.add(new ServiceActionArgument("groupDescription", TextContainer.retrieveFromRequest().getText().get("stemTierSecurityUpdatersGroupDescription")));
      ServiceAction levelThreeServiceAction_Three = createNewServiceAction("tierUpdatersGroup", true, 3, "stemServiceBaseGroupCreationConfirmation",
          ServiceActionType.group, args, levelTwoServiceAction_One);
      serviceActionsForStem.add(levelThreeServiceAction_Three);
      levelTwoServiceAction_One.addChildServiceAction(levelThreeServiceAction_Three);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:basis" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Updaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem+optionalColon+"basis"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"basis"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Five = createNewServiceAction("tierUpdatersPrivilege", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Five);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Five);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:reference" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Updaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem+optionalColon+"ref"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"reference"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Six = createNewServiceAction("tierUpdatersPrivilege2", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Six);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Six);
            
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:application" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Updaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem+optionalColon+"app"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"application"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Eight = createNewServiceAction("tierUpdatersPrivilege3", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Eight);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Eight);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:organization" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Updaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem+optionalColon+"org"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"organization"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Nine = createNewServiceAction("tierUpdatersPrivilege4", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Nine);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Nine);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to have inherited UPDATE privileges on Groups on the "org:Engineering School:test" folder?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupName", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Updaters"));
      args.add(new ServiceActionArgument("groupDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Updaters"));
      args.add(new ServiceActionArgument("parentStemName", stemPrefix+baseStem+optionalColon+"test"));
      args.add(new ServiceActionArgument("parentStemDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"test"));
      args.add(new ServiceActionArgument("privilegeType", "UPDATE"));
      args.add(new ServiceActionArgument("internalPrivilegeName", "update"));
      args.add(new ServiceActionArgument("templateItemType", "Groups"));
      ServiceAction levelFourServiceAction_Ten = createNewServiceAction("tierUpdatersPrivilege5", true, 4, "stemServiceBasePrivilegeCreationConfirmation", 
          ServiceActionType.inheritedPrivilege, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Ten);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Ten);
      
      //Do you want "org:Engineering School:etc:security:Engineering School Updaters" to be a member of "org:Engineering School:etc:security:Engineering School Readers"?
      args = new ArrayList<ServiceActionArgument>();
      args.add(new ServiceActionArgument("groupNameMembership", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Updaters"));
      args.add(new ServiceActionArgument("groupNameMembershipDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Updaters"));
      args.add(new ServiceActionArgument("groupNameMembershipOf", stemPrefix+baseStem+optionalColon+"etc:security:"+baseStem+"Readers"));
      args.add(new ServiceActionArgument("groupNameMembershipOfDisplayName", stemPrefixDisplayName+baseStemFriendlyName+optionalColon+"etc:security:"+baseStemFriendlyName+(StringUtils.equals(baseStem, baseStemFriendlyName) ? "" : " ")+"Readers"));
      ServiceAction levelFourServiceAction_Eleven = createNewServiceAction("tierUpdatersPrivilege6", true, 4, "stemServiceBaseMemberAdditionConfirmation", 
          ServiceActionType.membership, args, levelThreeServiceAction_Three);
      serviceActionsForStem.add(levelFourServiceAction_Eleven);
      levelThreeServiceAction_Three.addChildServiceAction(levelFourServiceAction_Eleven);
      
    return serviceActionsForStem;
  }

  @Override
  public String getSelectLabelKey() {
    return "stemTemplateTypeTierStructureLabel";
  }

}
