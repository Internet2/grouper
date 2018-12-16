/**
 * 
 */
package edu.internet2.middleware.grouper.app.grouperTypes;

import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.GROUPER_OBJECT_TYPE_NAME;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.grouperTypes.GrouperObjectTypesSettings.objectTypesStemName;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

public class GrouperObjectTypesJobTest extends GrouperTest {
  

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }
  
  
  public void testUpdateMetadata() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test1").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stemTest2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2").save();
    Stem stemTest11a = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1a").save();
    Stem stemTest22a = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:test2a").save();
    Group groupDirectAssigned = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:test2a:group0").save();
    
    saveObjectTypeAttributeMetadata(stem0, true);
    saveObjectTypeAttributeMetadata(stem1, false);
    
    //When
    GrouperObjectTypesJob.updateMetadataOnDirectStemsChildren();
    GrouperObjectTypesJob.updateMetadataOnIndirectGrouperObjects();
    
    //Then - All the children of stem0 should have metadata coming from parent
    GrouperObjectTypesAttributeValue grouperObjectTypesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stemTest1, "ref");
    assertEquals(stem0.getUuid(), grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    assertFalse(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    grouperObjectTypesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stemTest2, "ref");
    assertEquals(stem0.getUuid(), grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    assertFalse(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    grouperObjectTypesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stemTest11a, "ref");
    assertEquals(stem0.getUuid(), grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    assertFalse(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    grouperObjectTypesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stemTest22a, "ref");
    assertEquals(stem0.getUuid(), grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    assertFalse(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    grouperObjectTypesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(groupDirectAssigned, "ref");
    assertEquals(stem0.getUuid(), grouperObjectTypesAttributeValue.getObjectTypeOwnerStemId());
    assertFalse(grouperObjectTypesAttributeValue.isDirectAssignment());
    
    //stem1 shouldn't have anything assigned
    grouperObjectTypesAttributeValue = GrouperObjectTypesConfiguration.getGrouperObjectTypesAttributeValue(stem1, "ref");
    assertNull(grouperObjectTypesAttributeValue);
  }
  
  private static void saveObjectTypeAttributeMetadata(Stem stem, boolean isDirect) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(isDirect));
    
    attributeDefName = AttributeDefNameFinder.findByName(objectTypesStemName()+":"+GROUPER_OBJECT_TYPE_NAME, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "ref");
    
    attributeAssign.saveOrUpdate();
    
  }

}
