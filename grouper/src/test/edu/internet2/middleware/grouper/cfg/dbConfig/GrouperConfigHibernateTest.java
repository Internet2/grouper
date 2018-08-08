/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.sql.Timestamp;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GrouperConfigHibernateTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperConfigHibernateTest("testSaveOrUpdate"));
  }
  
  /**
   * @param name
   */
  public GrouperConfigHibernateTest(String name) {
    super(name);
  }

  /**
   * Test method for {@link edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate#saveOrUpdate()}.
   */
  public void testSaveOrUpdate() {
    
    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(ConfigFileName.GROUPER_PROPERTIES, null, "some.key");

    for (GrouperConfigHibernate grouperConfigHibernate : grouperConfigHibernates) {
      grouperConfigHibernate.delete();
    }
        
    GrouperConfigHibernate grouperConfigHibernate2 = null;
    
    // shouldnt exist
    if (grouperConfigHibernate2 != null) {
      grouperConfigHibernate2.delete();
    }
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigComment("comment");
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.ENVIRONMENT);
    grouperConfigHibernate.setConfigFileName(ConfigFileName.GROUPER_PROPERTIES);
    grouperConfigHibernate.setConfigKey("some.key");
    grouperConfigHibernate.setConfigValue("theValue");
    grouperConfigHibernate.saveOrUpdate();
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals(grouperConfigHibernate.getConfigComment(), grouperConfigHibernate2.getConfigComment());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), grouperConfigHibernate2.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileHierarchyDb(), grouperConfigHibernate2.getConfigFileHierarchyDb());
    assertEquals(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate2.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigSequence(), grouperConfigHibernate2.getConfigSequence());
    assertEquals(grouperConfigHibernate.getConfigValue(), grouperConfigHibernate2.getConfigValue());
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
    assertEquals(grouperConfigHibernate.getConfigValue(), grouperConfigHibernate2.getConfigValue());
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

    grouperConfigHibernate.setConfigValue("theValue2");
    grouperConfigHibernate.saveOrUpdate();
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), true);
    
    assertEquals(grouperConfigHibernate.getConfigComment(), grouperConfigHibernate2.getConfigComment());
    assertEquals(grouperConfigHibernate.getConfigEncryptedDb(), grouperConfigHibernate2.getConfigEncryptedDb());
    assertEquals(grouperConfigHibernate.getConfigFileHierarchyDb(), grouperConfigHibernate2.getConfigFileHierarchyDb());
    assertEquals(grouperConfigHibernate.getConfigKey(), grouperConfigHibernate2.getConfigKey());
    assertEquals(grouperConfigHibernate.getConfigSequence(), grouperConfigHibernate2.getConfigSequence());
    assertEquals(grouperConfigHibernate.getConfigValue(), grouperConfigHibernate2.getConfigValue());
    assertEquals(grouperConfigHibernate.getConfigVersionIndex(), grouperConfigHibernate2.getConfigVersionIndex());
    assertEquals(grouperConfigHibernate.getId(), grouperConfigHibernate2.getId());
    assertNotNull(grouperConfigHibernate.getId());
    assertEquals(grouperConfigHibernate.getLastUpdated(), grouperConfigHibernate2.getLastUpdated());
    assertNotNull(grouperConfigHibernate.getLastUpdated());
    
    grouperConfigHibernate.delete();
    
    grouperConfigHibernate2 = GrouperDAOFactory.getFactory().getConfig().findById(grouperConfigHibernate.getId(), false);
    
    assertNull(grouperConfigHibernate2);
    
  }

}
