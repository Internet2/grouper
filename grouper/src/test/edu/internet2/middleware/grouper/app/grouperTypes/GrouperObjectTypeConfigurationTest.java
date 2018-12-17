package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

public class GrouperObjectTypeConfigurationTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }
  
  public void testGetGrouperObjectTypesAttributeValue() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("objectTypes.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveObjectTypeAttributeMetadata(stem0, true, "ref");
    
    //When
    GrouperObjectTypesAttributeValue attributeValue1 = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stem0, "ref");
    
    //Then
    assertTrue(attributeValue1.isDirectAssignment());
    assertEquals("ref", attributeValue1.getObjectTypeName());
    
  }
  
  public void testGetGrouperObjectTypesAttributeValues() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("objectTypes.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveObjectTypeAttributeMetadata(stem0, true, "ref");
    saveObjectTypeAttributeMetadata(stem0, true, "basis");
    
    //When
    List<GrouperObjectTypesAttributeValue> attributeValues = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValues(stem0);
    
    //Then
    assertEquals(attributeValues.size(), 2);
    String objectTypeName1 = attributeValues.get(0).getObjectTypeName();
    String objectTypeName2 = attributeValues.get(1).getObjectTypeName();
    
    List<String> validNames = Arrays.asList("ref", "basis");
    
    assertTrue(validNames.contains(objectTypeName1));
    assertTrue(validNames.contains(objectTypeName2));
    
  }
  
  public void testSaveOrUpdateTypeAttributes() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("objectTypes.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    GrouperObjectTypesAttributeValue attributeValue = new GrouperObjectTypesAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setObjectTypeName("ref");
    
    //When
    GrouperObjectTypesConfiguration.saveOrUpdateTypeAttributes(attributeValue, stem0);
    
    //Then
    GrouperObjectTypesAttributeValue attributeValue1 = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stemTest1, "ref");
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ref", attributeValue1.getObjectTypeName());
  }
  
  public void testCopyConfigFromParent() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("objectTypes.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    saveObjectTypeAttributeMetadata(stem0, true, "ref");
    
    //When
    GrouperObjectTypesConfiguration.copyConfigFromParent(stemTest1);
    
    //Then
    GrouperObjectTypesAttributeValue attributeValue1 = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stemTest1, "ref");
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ref", attributeValue1.getObjectTypeName());
    
  }
  
  
  private static void saveObjectTypeAttributeMetadata(Stem stem, boolean isDirect, String objectTypeName) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(isDirect));
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), objectTypeName);
    
    attributeAssign.saveOrUpdate();
    
  }

}
