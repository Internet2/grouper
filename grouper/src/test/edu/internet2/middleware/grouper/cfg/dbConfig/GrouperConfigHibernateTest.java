/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGrouperConfigHibernate;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.RandomStringUtils;
import junit.textui.TestRunner;


/**
 *
 */
public class GrouperConfigHibernateTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperConfigHibernateTest("testSaveLessThan3000Value"));
  }
  
  /**
   * @param name
   */
  public GrouperConfigHibernateTest(String name) {
    super(name);
  }
  
  public void testSaveLessThan3000Value() {
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    GrouperConfigHibernate grouperConfigHibernate2 = null;
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    
    String randomValue = RandomStringUtils.randomAscii(2999);
    
    grouperConfigHibernate.setValueToSave(randomValue);
    grouperConfigHibernate.saveOrUpdate(true);
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals(randomValue, grouperConfigHibernate2.retrieveValue());
    assertEquals(randomValue, grouperConfigHibernate2.getConfigValueDb());
    assertEquals(2999L, grouperConfigHibernate2.getConfigValueBytes().longValue());
    
    PITGrouperConfigHibernate pitGrouperConfigHibernate = GrouperDAOFactory.getFactory().getPITConfig().findBySourceIdActive(grouperConfigHibernate.getId(), true);
    
    assertEquals(grouperConfigHibernate.getId(), pitGrouperConfigHibernate.getSourceId());
    assertEquals(grouperConfigHibernate.getConfigComment(), pitGrouperConfigHibernate.getConfigComment());
    assertEquals(grouperConfigHibernate.retrieveValue(), pitGrouperConfigHibernate.retrieveValue());
    assertEquals(grouperConfigHibernate.getConfigKey(), pitGrouperConfigHibernate.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), pitGrouperConfigHibernate.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileNameDb(), pitGrouperConfigHibernate.getConfigFileNameDb());
    assertEquals(grouperConfigHibernate.getConfigSequence(), pitGrouperConfigHibernate.getConfigSequence());
    assertEquals(grouperConfigHibernate.getConfigValueBytes(), pitGrouperConfigHibernate.getConfigValueBytes());
    assertEquals("T", pitGrouperConfigHibernate.getActiveDb());
    assertNull(pitGrouperConfigHibernate.getEndTime());
    assertNotNull(pitGrouperConfigHibernate.getStartTime());
    
    assertEquals(randomValue, GrouperConfig.retrieveConfig().propertyValueString("some.key"));
  }
  
  public void testSaveMoreThan3000Value() {
    
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    GrouperConfigHibernate grouperConfigHibernate2 = null;
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    
    String randomValue = RandomStringUtils.randomAscii(3001);
    
    grouperConfigHibernate.setValueToSave(randomValue);
    grouperConfigHibernate.saveOrUpdate(true);
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals(randomValue, grouperConfigHibernate2.retrieveValue());
    assertEquals(randomValue, grouperConfigHibernate2.getConfigValueClobDb());
    assertEquals(3001L, grouperConfigHibernate2.getConfigValueBytes().longValue());
    assertEquals(randomValue, GrouperConfig.retrieveConfig().propertyValueString("some.key"));
    
  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate#saveOrUpdate(boolean addNew)}.
   */
  public void testSaveOrUpdate() {
    
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    String id = null;
    
    GrouperConfigHibernate grouperConfigHibernate2 = null;
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.ENVIRONMENT);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    grouperConfigHibernate.setValueToSave("theValue");
    grouperConfigHibernate.saveOrUpdate(true);
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    id = grouperConfigHibernate2.getId();
    
    assertEquals(grouperConfigHibernate.getConfigComment(), grouperConfigHibernate2.getConfigComment());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), grouperConfigHibernate2.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileHierarchyDb(), grouperConfigHibernate2.getConfigFileHierarchyDb());
    assertEquals(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate2.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigSequence(), grouperConfigHibernate2.getConfigSequence());
    assertEquals(grouperConfigHibernate.retrieveValue(), grouperConfigHibernate2.retrieveValue());
    assertEquals(grouperConfigHibernate.getConfigVersionIndex(), grouperConfigHibernate2.getConfigVersionIndex());
    assertEquals(grouperConfigHibernate.getId(), grouperConfigHibernate2.getId());
    assertNotNull(grouperConfigHibernate.getId());
    assertEquals(grouperConfigHibernate.getLastUpdated(), grouperConfigHibernate2.getLastUpdated());
    assertNotNull(grouperConfigHibernate.getLastUpdated());

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, grouperConfigHibernate.getConfigKey());

    assertEquals(1, GrouperUtil.length(grouperConfigHibernates));

    grouperConfigHibernate2 = grouperConfigHibernates.iterator().next();
    
    assertEquals(grouperConfigHibernate.getConfigComment(), grouperConfigHibernate2.getConfigComment());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), grouperConfigHibernate2.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileHierarchyDb(), grouperConfigHibernate2.getConfigFileHierarchyDb());
    assertEquals(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate2.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigSequence(), grouperConfigHibernate2.getConfigSequence());
    assertEquals(grouperConfigHibernate.retrieveValue(), grouperConfigHibernate2.retrieveValue());
    assertEquals(grouperConfigHibernate.getConfigVersionIndex(), grouperConfigHibernate2.getConfigVersionIndex());
    assertEquals(grouperConfigHibernate.getId(), grouperConfigHibernate2.getId());
    assertNotNull(grouperConfigHibernate.getId());
    assertEquals(grouperConfigHibernate.getLastUpdated(), grouperConfigHibernate2.getLastUpdated());
    assertNotNull(grouperConfigHibernate.getLastUpdated());

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, null);
    
    assertTrue(GrouperUtil.length(grouperConfigHibernates) > 0);

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(null, new Timestamp(0), null);

    assertTrue(GrouperUtil.length(grouperConfigHibernates) > 0);

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, new Timestamp(0), null);

    assertTrue(GrouperUtil.length(grouperConfigHibernates) > 0);

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, new Timestamp(0), grouperConfigHibernate.getConfigKey());

    assertEquals(1, GrouperUtil.length(grouperConfigHibernates));

    GrouperUtil.sleep(1000);

    grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, new Timestamp(System.currentTimeMillis()), grouperConfigHibernate.getConfigKey());

    assertEquals(0, GrouperUtil.length(grouperConfigHibernates));

    grouperConfigHibernate.setValueToSave("theValue2");
    grouperConfigHibernate.saveOrUpdate(false);
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals(grouperConfigHibernate.getConfigComment(), grouperConfigHibernate2.getConfigComment());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), grouperConfigHibernate2.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileHierarchyDb(), grouperConfigHibernate2.getConfigFileHierarchyDb());
    assertEquals(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate2.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigSequence(), grouperConfigHibernate2.getConfigSequence());
    assertEquals(grouperConfigHibernate.retrieveValue(), grouperConfigHibernate2.retrieveValue());
    assertEquals(grouperConfigHibernate.getConfigVersionIndex(), grouperConfigHibernate2.getConfigVersionIndex());
    assertEquals(grouperConfigHibernate.getId(), grouperConfigHibernate2.getId());
    assertNotNull(grouperConfigHibernate.getId());
    assertEquals(grouperConfigHibernate.getLastUpdated(), grouperConfigHibernate2.getLastUpdated());
    assertNotNull(grouperConfigHibernate.getLastUpdated());
    
    grouperConfigHibernate.delete();
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), false);
    
    assertNull(grouperConfigHibernate2);
    
    Set<PITGrouperConfigHibernate> pitGrouperConfigHibernates = GrouperDAOFactory.getFactory().getPITConfig().findBySourceId(grouperConfigHibernate.getId(), true);
    
    assertEquals(2, GrouperUtil.length(pitGrouperConfigHibernates));
    
    List<PITGrouperConfigHibernate> pits = new ArrayList<PITGrouperConfigHibernate>(pitGrouperConfigHibernates);
    
    assertEquals(id, pits.get(0).getSourceId());
    assertEquals(id, pits.get(1).getSourceId());
    
    assertEquals("F", pits.get(0).getActiveDb());
    assertEquals("F", pits.get(1).getActiveDb());
    
  }

}
