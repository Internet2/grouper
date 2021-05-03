package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignStemDelegate;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class PspngToNewProvisioningAttributeConversion {
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(PspngToNewProvisioningAttributeConversion.class);

 
  public static void main(String[] args) {
    
    if (args == null || args.length == 0 || args.length > 1) {
      System.out.println("Please provide one argument that is the provisioning target name");
      return;
    }
    
    //set this and leave it...
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.GSH, false, true);

    copyProvisionToAttributesToNewProvisioningAttributes(args[0]);
    // copyProvisionToAttributesToNewProvisioningAttributes("pspng_activedirectory");
    
    System.exit(0);
    
  }


  public static void copyProvisionToAttributesToNewProvisioningAttributes(String provisioningConfigId) {
    
    // get target names configured via misc. screen 
    Set<String> validTargetNames = GrouperProvisioningSettings.getTargets(false).keySet();
    
    if (GrouperUtil.nonNull(validTargetNames).size() == 0) {
      System.out.println("No targets found. Please configure them on UI via Miscellaneous -> Provisioning screen.");
      return;
    }
    
    if (!validTargetNames.contains(provisioningConfigId)) {
      System.out.println(provisioningConfigId + " is not a valid target name. Valid target names are: "+String.join(",", validTargetNames));
      return;
    }
    
    String etc = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc");
    
    // start here
    GrouperSession grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    
    // List<Stem> = get all the pspng do provision attributes for the given target for all stems
    // List<Stem> = get all the pspng do not provision attributes for the given target for all stems
    // List<Group>  = do the same for all groups for do provision
    // List<Group>  = do the same for all groups for do not provision
    
    // convert List<Stem> to List<Stem, isProvision=true|false> for pspng
    // convert List<Group> to List<Group, isProvision=true|false> for pspng
    
    // List<Stem, isProvision=true|false> get the new provisioning attributes for the given target that are directly assigned for all stems
    // List<Group, isProvision=true|false> = get the new provisioning attributes for the given target that are directly assigned for all groups
    
    // what needs to be processed for do not provision
       // loop through pspng stems where is provision is false
         // call the saveUpdate method with doProvision = false
       // loop through pspng groups where is provision is false
        //  call the saveUpdate method with doProvision = false
    
    // what needs to be processed for do provision
    // loop through pspng stems where is provision is true
      // call the saveUpdate method with doProvision = true
    // loop through pspng groups where is provision is true
     //  call the saveUpdate method with doProvision = true
    
    // loop through all the new provisioning attributes for stem we retrieved above
    // and if there's no corresponding pspng then remove that attribute and compute from parent
    
    // loop through all the new provisioning attributes for group we retrieved above
    // and if there's no corresponding pspng then remove that attribute and compute from parent
       
    boolean createAttributeDef = false;
    if (createAttributeDef) {
      
      Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
      Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
      Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:group1").save();
      
      Stem stem2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2").save();
      Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test2:group2").save();
      
      AttributeDefSave provisionToAttributeDefSave = new AttributeDefSave(grouperSession).assignName(etc+":pspng:provision_to_def").assignCreateParentStemsIfNotExist(true).assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.type).assignMultiAssignable(true).assignMultiValued(false).assignValueType(AttributeDefValueType.string);
      AttributeDef provisionToAttributeDef = provisionToAttributeDefSave.save();
      
      provisionToAttributeDef = AttributeDefFinder.findByName(etc + ":pspng:provision_to_def", false);
      provisionToAttributeDef.getAttributeDefActionDelegate().configureActionList("assign");
      AttributeDefNameSave provisionToAttributeDefNameSave = new AttributeDefNameSave(grouperSession, provisionToAttributeDef).assignName(etc + ":pspng:provision_to").assignCreateParentStemsIfNotExist(true).assignDescription("Defines what provisioners should process a group or groups within a folder").assignDisplayName(etc + ":pspng:provision_to");  
      AttributeDefName provisionToAttributeDefName = provisionToAttributeDefNameSave.save();
      
      AttributeAssignStemDelegate attributeDelegateProvisionTo = stem.getAttributeDelegate();
      AttributeAssignResult provisionToAttributeAssignResult = attributeDelegateProvisionTo.addAttribute(provisionToAttributeDefName);
      
      String provisionToAttributeAssignId = provisionToAttributeAssignResult.getAttributeAssign().getId();
      
      AttributeAssign provisionToAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(provisionToAttributeAssignId, true, false);
      
      provisionToAttributeAssign.getValueDelegate().addValue("pspng_activedirectory");
      
      // do not provision to
      AttributeDefSave doNotrovisionToAttributeDefSave = new AttributeDefSave(grouperSession).assignName(etc+":pspng:do_not_provision_to_def").assignCreateParentStemsIfNotExist(true).assignToGroup(true).assignToStem(true).assignAttributeDefType(AttributeDefType.type).assignMultiAssignable(true).assignMultiValued(false).assignValueType(AttributeDefValueType.string);
      AttributeDef doNotrovisionToAttributeDef = doNotrovisionToAttributeDefSave.save();
      
      doNotrovisionToAttributeDef = AttributeDefFinder.findByName(etc + ":pspng:do_not_provision_to_def", false);
      doNotrovisionToAttributeDef.getAttributeDefActionDelegate().configureActionList("assign");
      AttributeDefNameSave doNotProvisionAttributeDefNameSave = new AttributeDefNameSave(grouperSession, provisionToAttributeDef).assignName(etc + ":pspng:do_not_provision_to")
          .assignCreateParentStemsIfNotExist(true)
          .assignDescription("Defines what provisioners should not process a group or groups within a folder. Since the default is already for provisioners to not provision any groups, this attribute is to override a provision_to attribute set on an ancestor folder.")
          .assignDisplayName(etc + ":pspng:do_not_provision_to");  
      AttributeDefName doNotProvisionAttributeDefName = doNotProvisionAttributeDefNameSave.save();
      
      AttributeAssignStemDelegate attributeDelegateDoNotProvisionTo = stem2.getAttributeDelegate();
      AttributeAssignResult doNotProvisionToAttributeAssignResult = attributeDelegateDoNotProvisionTo.addAttribute(doNotProvisionAttributeDefName);
      
      String doNotProvisionToAttributeAssignId = doNotProvisionToAttributeAssignResult.getAttributeAssign().getId();
      
      AttributeAssign doNotProvisionToAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(doNotProvisionToAttributeAssignId, true, false);
      
      doNotProvisionToAttributeAssign.getValueDelegate().addValue("pspng_activedirectory");
      
    }
    
    Set<Stem> provisionToStems = new StemFinder()
        .assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(etc + ":pspng:provision_to")
        .assignAttributeValue(provisioningConfigId)
        .findStems();
    
    System.out.println("Found "+provisionToStems.size() + " folder(s) that have provision_to attribute assigned with target "+provisioningConfigId);
    
    Set<Stem> doNotProvisionToStems = new StemFinder()
        .assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(etc + ":pspng:do_not_provision_to")
        .assignAttributeValue(provisioningConfigId)
        .findStems();
    
    System.out.println("Found "+doNotProvisionToStems.size() + " folder(s) that have do_not_provision_to attribute assigned with target "+provisioningConfigId);
    
    Set<Group> provisionToGroups = new GroupFinder()
            .assignAttributeCheckReadOnAttributeDef(false)
            .assignNameOfAttributeDefName(etc + ":pspng:provision_to")
            .assignAttributeValue(provisioningConfigId)
            .findGroups();
    
    System.out.println("Found "+provisionToGroups.size() + " group(s) that have provision_to attribute assigned with target "+provisioningConfigId);
    
    Set<Group> doNotProvisionToGroups = new GroupFinder()
        .assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(etc + ":pspng:do_not_provision_to")
        .assignAttributeValue(provisioningConfigId)
        .findGroups();
    
    System.out.println("Found "+doNotProvisionToGroups.size() + " group(s) that have do_not_provision_to attribute assigned with target "+provisioningConfigId);
    
    Set<Stem> stemsNewProvisioningDirect = new StemFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT)
        .addAttributeValuesOnAssignment("true")
        .assignNameOfAttributeDefName2(provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)
        .addAttributeValuesOnAssignment2(provisioningConfigId)
        .findStems();
    
    System.out.println("Found "+stemsNewProvisioningDirect.size() + " folder(s) that already have direct new provisioning attribute assigned with target "+provisioningConfigId);
    
    Set<Group> groupsNewProvisioningDirect = new GroupFinder().assignAttributeCheckReadOnAttributeDef(false)
        .assignNameOfAttributeDefName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT)
        .addAttributeValuesOnAssignment("true")
        .assignNameOfAttributeDefName2(provisioningConfigStemName()+":"+GrouperProvisioningAttributeNames.PROVISIONING_TARGET)
        .addAttributeValuesOnAssignment2(provisioningConfigId)
        .findGroups();
    
    System.out.println("Found "+groupsNewProvisioningDirect.size() + " group(s) that already have direct new provisioning attribute assigned with target "+provisioningConfigId);
    
    for (Stem doNotProvisionToStem: doNotProvisionToStems) {
      
      System.out.println("Going to assign new provsioning attributes to folder "+doNotProvisionToStem.getName() + " "
          + "with target name "+provisioningConfigId + " with provisioningDoProvision set to false");
      
      GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = new GrouperProvisioningAttributeValue();
      grouperProvisioningAttributeValue.setTargetName(provisioningConfigId);
      grouperProvisioningAttributeValue.setDirectAssignment(true);
      grouperProvisioningAttributeValue.setDoProvision(null);
      grouperProvisioningAttributeValue.setStemScopeString(Stem.Scope.SUB.name().toLowerCase());
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, doNotProvisionToStem);
      
      System.out.println("Successfully assigned "+provisioningConfigId + " to folder "+doNotProvisionToStem.getName() + ""
          + " with provisioningDoProvision set to false");
    }
    
    for (Group doNotProvisionToGroup: doNotProvisionToGroups) {
      
      System.out.println("Going to assign new provsioning attributes to group "+doNotProvisionToGroup.getName() + " "
          + "with target name "+provisioningConfigId + " with provisioningDoProvision set to false");
      
      GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = new GrouperProvisioningAttributeValue();
      grouperProvisioningAttributeValue.setTargetName(provisioningConfigId);
      grouperProvisioningAttributeValue.setDirectAssignment(true);
      grouperProvisioningAttributeValue.setDoProvision(null);
      grouperProvisioningAttributeValue.setStemScopeString(Stem.Scope.SUB.name().toLowerCase());
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, doNotProvisionToGroup);
      
      System.out.println("Successfully assigned "+provisioningConfigId + " to group "+doNotProvisionToGroup.getName() + ""
          + " with provisioningDoProvision set to false");
    }
    
    for (Stem provisionToStem: provisionToStems) {
      
      System.out.println("Going to assign new provsioning attributes to folder "+provisionToStem.getName() + " "
          + "with target name "+provisioningConfigId + " with provisioningDoProvision set to true");
      
      GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = new GrouperProvisioningAttributeValue();
      grouperProvisioningAttributeValue.setTargetName(provisioningConfigId);
      grouperProvisioningAttributeValue.setDirectAssignment(true);
      grouperProvisioningAttributeValue.setDoProvision(provisioningConfigId);
      grouperProvisioningAttributeValue.setStemScopeString(Stem.Scope.SUB.name().toLowerCase());
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, provisionToStem);
      
      System.out.println("Successfully assigned "+provisioningConfigId + " to folder "+provisionToStem.getName() + ""
          + " with provisioningDoProvision set to true");
    }
    
    for (Group provisionToGroup: provisionToGroups) {
      
      System.out.println("Going to assign new provsioning attributes to group "+provisionToGroup.getName() + " "
          + "with target name "+provisioningConfigId + " with provisioningDoProvision set to true");
      
      GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = new GrouperProvisioningAttributeValue();
      grouperProvisioningAttributeValue.setTargetName(provisioningConfigId);
      grouperProvisioningAttributeValue.setDirectAssignment(true);
      grouperProvisioningAttributeValue.setDoProvision(provisioningConfigId);
      grouperProvisioningAttributeValue.setStemScopeString(Stem.Scope.SUB.name().toLowerCase());
      
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, provisionToGroup);
      
      System.out.println("Successfully assigned "+provisioningConfigId + " to group "+provisionToGroup.getName() + ""
          + " with provisioningDoProvision set to true");
    }
    
    
    // clear out invalid assignments
    {
      Set<Stem> oldPspngStems = new HashSet<Stem>();
      oldPspngStems.addAll(provisionToStems);
      oldPspngStems.addAll(doNotProvisionToStems);
      
      stemsNewProvisioningDirect.removeAll(oldPspngStems);
      
      System.out.println("Found "+stemsNewProvisioningDirect.size() + " folders that have new provisioning attributes directly assigned"
          + " but equivalent pspng attribute not found. Going to clear the direct assignment from those folders.");
      
      for (Stem stemFromWhichAssignmentNeedsToBeRemoved: stemsNewProvisioningDirect) {
        System.out.println("Going to clear direct new provisioning attribute assignment from folder "+stemFromWhichAssignmentNeedsToBeRemoved.getName());
        GrouperProvisioningService.copyConfigFromParent(stemFromWhichAssignmentNeedsToBeRemoved, provisioningConfigId);
        System.out.println("Successfully cleared out direct new provisioning assignment from "+stemFromWhichAssignmentNeedsToBeRemoved.getName());
      }
    }
    
    {
      Set<Group> oldPspngGroups = new HashSet<Group>();
      oldPspngGroups.addAll(provisionToGroups);
      oldPspngGroups.addAll(doNotProvisionToGroups);
      
      groupsNewProvisioningDirect.removeAll(oldPspngGroups);
      
      System.out.println("Found "+groupsNewProvisioningDirect.size() + " groups that have new provisioning attributes directly assigned"
          + " but equivalent pspng attribute not found. Going to clear the direct assignment from those groups.");
      
      for (Group groupFromWhichAssignmentNeedsToBeRemoved: groupsNewProvisioningDirect) {
        System.out.println("Going to clear direct new provisioning attribute assignment from group "+groupFromWhichAssignmentNeedsToBeRemoved.getName());
        GrouperProvisioningService.copyConfigFromParent(groupFromWhichAssignmentNeedsToBeRemoved, provisioningConfigId);
        System.out.println("Successfully cleared out direct new provisioning assignment from "+groupFromWhichAssignmentNeedsToBeRemoved.getName());
      }
    }
  }
}
