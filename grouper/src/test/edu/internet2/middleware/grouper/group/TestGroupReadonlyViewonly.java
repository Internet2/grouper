/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.group;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder;
import edu.internet2.middleware.grouper.misc.GrouperObjectFinder.ObjectPrivilege;


/**
 *
 */
public class TestGroupReadonlyViewonly extends GrouperTest {

  /**
   * 
   */
  public TestGroupReadonlyViewonly() {
  }

  /**
   * @param name
   */
  public TestGroupReadonlyViewonly(String name) {
    super(name);

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroupReadonlyViewonly("testReadonlyViewonlyAdmin"));

  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.readonly.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.group", "etc:sysadminViewersGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.readonly.group", "etc:sysadminReadersGroup");

  }

  /**
   * 
   */
  public void testReadonlyViewonlyAdmin() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    final Group sysadminViewersGroup = GroupFinder.findByName(grouperSession, "etc:sysadminViewersGroup", true);
    final Group sysadminReadersGroup = GroupFinder.findByName(grouperSession, "etc:sysadminReadersGroup", true);

    //subject 0 is a viewer, 1 is a reader, 2 is nothing, 3 is a member
    sysadminViewersGroup.addMember(SubjectTestHelper.SUBJ0);
    sysadminReadersGroup.addMember(SubjectTestHelper.SUBJ1);
    
    String groupName = "test:testGroup";
    Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(groupName).save();
    group.addMember(SubjectTestHelper.SUBJ3);

    String stemName = "test2";
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(stemName).save();

    //group has attribute
    String group2Name = "test:test2Group";
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(group2Name).save();
    group2.addMember(SubjectTestHelper.SUBJ3);

    //attribute
    String nameOfAttributeDef = "test:testAttributeDef";
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(nameOfAttributeDef)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignValueType(AttributeDefValueType.string).save();
    String nameOfAttributeDefName = "test:testAttributeNameDef";
    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignCreateParentStemsIfNotExist(true)
        .assignName(nameOfAttributeDefName).save();

    String name2OfAttributeDef = "test:test2AttributeDef";
    AttributeDef attributeDef2 = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(name2OfAttributeDef)
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignValueType(AttributeDefValueType.string).save();

    String name2OfAttributeDefName = "test:test2AttributeNameDef";
    AttributeDefName attributeDefName2 = new AttributeDefNameSave(grouperSession, attributeDef2).assignCreateParentStemsIfNotExist(true)
        .assignName(name2OfAttributeDefName).save();

    group2.getAttributeDelegate().assignAttribute(attributeDefName2);

    //stem has an attribute
    String name3OfAttributeDef = "test:test3AttributeDef";
    AttributeDef attributeDef3 = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName(name3OfAttributeDef)
        .assignAttributeDefType(AttributeDefType.attr).assignToStem(true).assignValueType(AttributeDefValueType.string).save();

    String name3OfAttributeDefName = "test:test3AttributeNameDef";
    AttributeDefName attributeDefName3 = new AttributeDefNameSave(grouperSession, attributeDef3).assignCreateParentStemsIfNotExist(true)
        .assignName(name3OfAttributeDefName).save();

    stem.getAttributeDelegate().assignAttribute(attributeDefName3);
    
    GrouperSession.stopQuietly(grouperSession);
    
    //############ SUBJ 0 can view, not read, can search
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    group = GroupFinder.findByName(grouperSession, groupName, false);
    
    assertNotNull(group);
    
    assertEquals(0, group.getMembers() == null ? 0 : group.getMembers().size());
      
    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(groupName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(groupName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(groupName).findGrouperObjects().size());
    
    attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
    
    assertNotNull(attributeDef);
    
    attributeDefName = AttributeDefNameFinder.findByName(nameOfAttributeDefName, false);
    
    assertNotNull(attributeDefName);
    
    assertEquals(0, group2.getAttributeDelegate().retrieveAttributes().size());

    assertEquals(0, stem.getAttributeDelegate().retrieveAttributes().size());
      
    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());

    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());

    //############ SUBJ 1 can view, and read, can search
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    group = GroupFinder.findByName(grouperSession, groupName, false);
    
    assertNotNull(group);
    
    assertEquals(1, group.getMembers().size());
      
    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(groupName).findGrouperObjects().size());
    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(groupName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(groupName).findGrouperObjects().size());

    attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
    
    assertNotNull(attributeDef);
    
    attributeDefName = AttributeDefNameFinder.findByName(nameOfAttributeDefName, false);
    
    assertNotNull(attributeDefName);
    
    assertEquals(1, group2.getAttributeDelegate().retrieveAttributes().size());
    assertEquals(1, stem.getAttributeDelegate().retrieveAttributes().size());

    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());
    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());

    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());
    assertEquals(1, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());

    //############ SUBJ 2 cant view, or read, or search
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    group = GroupFinder.findByName(grouperSession, groupName, false);
    
    assertNull(group);
    
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(groupName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(groupName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(groupName).findGrouperObjects().size());
    
    attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
    
    assertNull(attributeDef);
    
    attributeDefName = AttributeDefNameFinder.findByName(nameOfAttributeDefName, false);
    
    assertNull(attributeDefName);
    
    assertEquals(0, group2.getAttributeDelegate().retrieveAttributes().size());
    assertEquals(0, stem.getAttributeDelegate().retrieveAttributes().size());
      
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(nameOfAttributeDef).findGrouperObjects().size());

    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.view).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.read).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());
    assertEquals(0, new GrouperObjectFinder().assignSubject(grouperSession.getSubject())
        .assignObjectPrivilege(ObjectPrivilege.update).assignFilterText(nameOfAttributeDefName).findGrouperObjects().size());

    
    
  }
  

  
}
