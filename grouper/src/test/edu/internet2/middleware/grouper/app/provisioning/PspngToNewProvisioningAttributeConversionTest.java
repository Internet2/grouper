package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import junit.textui.TestRunner;

public class PspngToNewProvisioningAttributeConversionTest extends GrouperTest {

  public PspngToNewProvisioningAttributeConversionTest(String name) {
    super(name);
  }
  
  public static void main(String[] args) {
    TestRunner.run(new PspngToNewProvisioningAttributeConversionTest("testCopyProvisionToAttributesToNewProvisioningAttributes"));
  }
  
  public void testCopyProvisionToAttributesToNewProvisioningAttributes() {
    
    GrouperSession grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    
    String etc = GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc");
    
    // setup old pspng attributes first
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
    
    provisionToAttributeAssign.getValueDelegate().addValue("sqlProvTest");
    
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
    
    doNotProvisionToAttributeAssign.getValueDelegate().addValue("sqlProvTest");
    
    // now pspng is all setup
    // configure sqlProvTest as a valid target name
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisionerConfiguration.class.getName());
    
    // create two stems and a group that already has the new provisioning attribute with sqlProvTest. They should be deleted in the end.
    Stem stemAlreadyPspngDirect = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test_already_pspng").save();
    Stem stemAlreadyPspngIndirect = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test_already_pspng:test1").save();
    Group groupAlreadyPspngIndirect = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test_already_pspng:test1:group1").save();
    
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = new GrouperProvisioningAttributeValue();
    grouperProvisioningAttributeValue.setTargetName("sqlProvTest");
    grouperProvisioningAttributeValue.setDirectAssignment(true);
    grouperProvisioningAttributeValue.setDoProvision(true);
    grouperProvisioningAttributeValue.setStemScopeString(Stem.Scope.SUB.name().toLowerCase());
    
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, stemAlreadyPspngDirect);

    // stem1 should be overriden in the end with doProvision set to true
    grouperProvisioningAttributeValue.setDoProvision(false);
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(grouperProvisioningAttributeValue, stem1);
    
    
    // copy from pspng to new provisioning attributes
    PspngToNewProvisioningAttributeConversion.copyProvisionToAttributesToNewProvisioningAttributes("sqlProvTest");
    
    // assert that stems and groups have new attributes setup correctly
    GrouperProvisioningAttributeValue provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stem, "sqlProvTest");
    assertEquals(true, provisioningAttributeValue.isDirectAssignment());
    assertEquals(true, provisioningAttributeValue.isDoProvision());
    assertEquals(true, provisioningAttributeValue.isStemScopeSub());
    assertNull(provisioningAttributeValue.getOwnerStemId());
    
    provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stem1, "sqlProvTest");
    assertEquals(false, provisioningAttributeValue.isDirectAssignment());
    assertEquals(true, provisioningAttributeValue.isDoProvision());
    assertEquals(true, provisioningAttributeValue.isStemScopeSub());
    assertEquals(stem.getId(), provisioningAttributeValue.getOwnerStemId());
    
    provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(group1, "sqlProvTest");
    assertEquals(false, provisioningAttributeValue.isDirectAssignment());
    assertEquals(true, provisioningAttributeValue.isDoProvision());
    assertEquals(true, provisioningAttributeValue.isStemScopeSub());
    assertEquals(stem.getId(), provisioningAttributeValue.getOwnerStemId());
    
    provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stem2, "sqlProvTest");
    assertEquals(true, provisioningAttributeValue.isDirectAssignment());
    assertEquals(false, provisioningAttributeValue.isDoProvision());
    assertEquals(true, provisioningAttributeValue.isStemScopeSub());
    assertNull(provisioningAttributeValue.getOwnerStemId());
    
    provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(group2, "sqlProvTest");
    assertEquals(false, provisioningAttributeValue.isDirectAssignment());
    assertEquals(false, provisioningAttributeValue.isDoProvision());
    assertEquals(true, provisioningAttributeValue.isStemScopeSub());
    assertEquals(stem2.getId(), provisioningAttributeValue.getOwnerStemId());
    
    
    provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stemAlreadyPspngDirect, "sqlProvTest");
    assertNull(provisioningAttributeValue);
    
    provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(stemAlreadyPspngIndirect, "sqlProvTest");
    assertNull(provisioningAttributeValue);
    
    provisioningAttributeValue = GrouperProvisioningService.getProvisioningAttributeValue(groupAlreadyPspngIndirect, "sqlProvTest");
    assertNull(provisioningAttributeValue);
    
  }
  
}
