/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.AttributeDefNameAddException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer
 *
 */
public class AttributeDefNameTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefNameTest("testXmlDifferentUpdateProperties"));
  }
  
  /**
   * 
   */
  public AttributeDefNameTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public AttributeDefNameTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** top stem */
  private Stem top;

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start( SubjectFinder.findRootSubject() );
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");
  }

  /**
   * attribute def
   */
  public void testHibernate() {
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.attr);

    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    assertNotNull(attributeDefName.getId());

    //lets retrieve by id
    AttributeDefName attributeDefName2 = GrouperDAOFactory.getFactory().getAttributeDefName().findByIdSecure(attributeDefName.getId(), true);

    assertEquals(attributeDefName.getId(), attributeDefName2.getId());
    
    //lets retrieve by name
    attributeDefName2 = GrouperDAOFactory.getFactory().getAttributeDefName().findByNameSecure("top:testName", true);
    
    assertEquals("top:testName", attributeDefName2.getName());
    assertEquals("top display name:test name", attributeDefName2.getDisplayName());
    assertEquals(attributeDefName.getId(), attributeDefName2.getId());

    //try to add another
    try {
      attributeDefName2 = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    } catch (AttributeDefNameAddException adae) {
      assertTrue(ExceptionUtils.getFullStackTrace(adae), adae.getMessage().contains("attribute def name already exists"));
    }

    attributeDefName2 = this.top.addChildAttributeDefName(attributeDef, "testName2", "test name2");
    
  }

  /**
   * make an example attribute def name for testing
   * @return an example attribute def name
   */
  public static AttributeDefName exampleAttributeDefName() {
    AttributeDefName attributeDefName = new AttributeDefName();
    attributeDefName.setAttributeDefId("attributeDefId");
    attributeDefName.setContextId("contextId");
    attributeDefName.setCreatedOnDb(5L);
    attributeDefName.setDescription("description");
    attributeDefName.setDisplayExtensionDb("displayExtension");
    attributeDefName.setDisplayNameDb("displayName");
    attributeDefName.setExtensionDb("extension");
    attributeDefName.setHibernateVersionNumber(3L);
    attributeDefName.setLastUpdatedDb(6L);
    attributeDefName.setNameDb("name");
    attributeDefName.setStemId("parentUuid");
    attributeDefName.setId("uuid");
    return attributeDefName;
  }
  
  /**
   * make an example attributeDefName for testing
   * @return an example attributeDefName
   */
  public static AttributeDefName exampleAttributeDefNameDb() {
    return exampleAttributeDefNameDb("test", "testAttributeDefName");
  }

  /**
   * make an example attributeDefName for testing
   * @param stemName 
   * @param extension 
   * @return an example attributeDefName
   */
  public static AttributeDefName exampleAttributeDefNameDb(String stemName, String extension) {
    return exampleAttributeDefNameDb(AttributeDefType.attr, stemName, extension);
  }
  
  /**
   * make an example attributeDefName for testing
   * @param attributeDefType 
   * @param stemName 
   * @param extension 
   * @return an example attributeDefName
   */
  public static AttributeDefName exampleAttributeDefNameDb(AttributeDefType attributeDefType, String stemName, String extension) {

    String name = stemName + ":" + extension;
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(name, false);

    if (attributeDefName == null) {
      
      Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit(stemName).assignName(stemName).assignCreateParentStemsIfNotExist(true)
        .assignDescription("description").save();

      String nameOfAttributeDef = name + "Def";
      
      AttributeDef attributeDef = AttributeDefFinder.findByName(nameOfAttributeDef, false);
      
      if (attributeDef == null) {
        attributeDef = stem.addChildAttributeDef(extension + "Def", attributeDefType);
        attributeDef.setAssignToGroup(true);
        attributeDef.store();
      }
      
      if (!attributeDefType.equals(attributeDef.getAttributeDefType())) {
        throw new RuntimeException("Wrong type: " + attributeDefType + ", " + attributeDef.getAttributeDefType());
      }
      
      attributeDefName = stem.addChildAttributeDefName(attributeDef, extension, extension);
    }
    return attributeDefName;
  }


  /**
   * make an example attribute def for testing
   * @return an example attribute def
   */
  public static AttributeDefName exampleRetrieveAttributeDefNameDb() {
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("test:testAttributeDefName", true);
    return attributeDefName;
  }

  /**
   * make sure update properties are detected correctly
   */
  public void testFindAll() {
    
    GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = exampleAttributeDefNameDb("test", "testAttributeDefName1");
    AttributeDefName attributeDefName2 = exampleAttributeDefNameDb("test", "testAttributeDefName2");
    
    Set<AttributeDefName> attributeDefNames = AttributeDefNameFinder.findAll("blah", null, null);
    assertEquals(0, GrouperUtil.length(attributeDefNames));
    
    attributeDefNames = AttributeDefNameFinder.findAll("blah", GrouperUtil.toSet(attributeDefName.getAttributeDefId(), attributeDefName2.getAttributeDefId()), null);
    assertEquals(0, GrouperUtil.length(attributeDefNames));

    
    attributeDefNames = AttributeDefNameFinder.findAll("TESTA%", null, null);
    
    assertEquals(2, GrouperUtil.length(attributeDefNames));
    assertTrue(attributeDefNames.contains(attributeDefName));
    assertTrue(attributeDefNames.contains(attributeDefName2));
    
    attributeDefNames = AttributeDefNameFinder.findAll("testa%", GrouperUtil.toSet(attributeDefName.getAttributeDefId(), attributeDefName2.getAttributeDefId()), null);
    
    assertEquals(2, GrouperUtil.length(attributeDefNames));
    assertTrue(attributeDefNames.contains(attributeDefName));
    assertTrue(attributeDefNames.contains(attributeDefName2));
    
    attributeDefNames = AttributeDefNameFinder.findAll("%name1", null, null);
    
    assertEquals(1, GrouperUtil.length(attributeDefNames));
    assertTrue(attributeDefNames.contains(attributeDefName));
    
    attributeDefNames = AttributeDefNameFinder.findAll("%name2", GrouperUtil.toSet(attributeDefName.getAttributeDefId(), attributeDefName2.getAttributeDefId()), null);
    
    assertEquals(1, GrouperUtil.length(attributeDefNames));
    assertTrue(attributeDefNames.contains(attributeDefName2));
    
    
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    AttributeDefName attributeDefNameOriginal = exampleAttributeDefNameDb("test", "testAttributeDefNameInsert");
    
    //do this because last membership update isnt there, only in db
    attributeDefNameOriginal =  AttributeDefNameFinder.findByName("test:testAttributeDefNameInsert", true);
    AttributeDefName attributeDefNameCopy = AttributeDefNameFinder.findByName("test:testAttributeDefNameInsert", true);
    AttributeDefName attributeDefNameCopy2 = AttributeDefNameFinder.findByName("test:testAttributeDefNameInsert", true);
    attributeDefNameCopy.delete();
    
    //lets insert the original
    attributeDefNameCopy2.xmlSaveBusinessProperties(null);
    attributeDefNameCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    attributeDefNameCopy = AttributeDefNameFinder.findByName("test:testAttributeDefNameInsert", true);
    
    assertFalse(attributeDefNameCopy == attributeDefNameOriginal);
    assertFalse(attributeDefNameCopy.xmlDifferentBusinessProperties(attributeDefNameOriginal));
    assertFalse(attributeDefNameCopy.xmlDifferentUpdateProperties(attributeDefNameOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    AttributeDefName attributeDefName = null;
    AttributeDefName exampleAttributeDefName = null;

    
    //TEST UPDATE PROPERTIES
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();
      
      attributeDefName.setContextId("abc");
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertTrue(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setContextId(exampleAttributeDefName.getContextId());
      attributeDefName.xmlSaveUpdateProperties();

      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
      
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setCreatedOnDb(99L);
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertTrue(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setCreatedOnDb(exampleAttributeDefName.getCreatedOnDb());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setHibernateVersionNumber(99L);
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertTrue(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setHibernateVersionNumber(exampleAttributeDefName.getHibernateVersionNumber());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setAttributeDefId("abc");
      
      assertTrue(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setAttributeDefId(exampleAttributeDefName.getAttributeDefId());
      attributeDefName.xmlSaveBusinessProperties(exampleAttributeDefName.clone());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setDescription("abc");
      
      assertTrue(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setDescription(exampleAttributeDefName.getDescription());
      attributeDefName.xmlSaveBusinessProperties(exampleAttributeDefName.clone());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setDisplayExtensionDb("abc");
      
      assertTrue(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setDisplayExtensionDb(exampleAttributeDefName.getDisplayExtensionDb());
      attributeDefName.xmlSaveBusinessProperties(exampleAttributeDefName.clone());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setDisplayNameDb("abc");
      
      assertTrue(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setDisplayNameDb(exampleAttributeDefName.getDisplayNameDb());
      attributeDefName.xmlSaveBusinessProperties(exampleAttributeDefName.clone());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setExtensionDb("abc");
      
      assertTrue(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setExtensionDb(exampleAttributeDefName.getExtensionDb());
      attributeDefName.xmlSaveBusinessProperties(exampleAttributeDefName.clone());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setId("abc");
      
      assertTrue(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setId(exampleAttributeDefName.getId());
      attributeDefName.xmlSaveBusinessProperties(exampleAttributeDefName.clone());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setNameDb("abc");
      
      assertTrue(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setNameDb(exampleAttributeDefName.getNameDb());
      attributeDefName.xmlSaveBusinessProperties(exampleAttributeDefName.clone());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    
    }
    
    {
      attributeDefName = exampleAttributeDefNameDb();
      exampleAttributeDefName = exampleRetrieveAttributeDefNameDb();

      attributeDefName.setStemId("abc");
      
      assertTrue(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));

      attributeDefName.setStemId(exampleAttributeDefName.getStemId());
      attributeDefName.xmlSaveBusinessProperties(exampleAttributeDefName.clone());
      attributeDefName.xmlSaveUpdateProperties();
      
      attributeDefName = exampleRetrieveAttributeDefNameDb();
      
      assertFalse(attributeDefName.xmlDifferentBusinessProperties(exampleAttributeDefName));
      assertFalse(attributeDefName.xmlDifferentUpdateProperties(exampleAttributeDefName));
    
    }

  }


  
}
