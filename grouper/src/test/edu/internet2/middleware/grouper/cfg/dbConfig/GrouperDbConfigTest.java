/**
 * 
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.config.db.ConfigDatabaseLogic;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;


/**
 * @author mchyzer
 *
 */
public class GrouperDbConfigTest extends GrouperTest {

  /**
   * 
   */
  public GrouperDbConfigTest() {
  }

  /**
   * @param name
   */
  public GrouperDbConfigTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDbConfigTest("testDatabaseCacheClear"));

  }

  /**
   * 
   */
  public void testDatabaseOverride() {
    
    // delete the test config properties
    HibernateSession.bySqlStatic().executeSql(
        "delete from grouper_config where config_file_name = ?" , 
        HibUtils.listObject(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigClasspath()),
        HibUtils.listType(StringType.INSTANCE));
    
    //  ########################################
    //  ## Properties to test
    //  ########################################
    //
    //  # property1.in.base.only = 
    //
    //  property2.in.base.and.override = value2.in.override
    //
    //  # property3.in.base.and.database = 
    //
    //  property4.in.base.and.override.and.database = value4.in.override
    //
    //  property5.in.override.and.database = value5.in.override
    //
    //  property6.in.override = value6.in.override
    //
    //  # property7.in.database
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property3.in.base.and.database");
    grouperConfigHibernate.setValueToSave("value3.in.database");
    grouperConfigHibernate.saveOrUpdate(true);
    
    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property4.in.base.and.override.and.database");
    grouperConfigHibernate.setValueToSave("value4.in.database");
    grouperConfigHibernate.saveOrUpdate(true);

    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property5.in.override.and.database");
    grouperConfigHibernate.setValueToSave("value5.in.database");
    grouperConfigHibernate.saveOrUpdate(true);

    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property7.in.database");
    grouperConfigHibernate.setValueToSave("value7.in.database");
    grouperConfigHibernate.saveOrUpdate(true);

    EhcacheController.ehcacheController().flushCache();
    ConfigPropertiesCascadeBase.clearCache();
    
    //  # property1.in.base.only = 
    //
    //  property2.in.base.and.override = value2.in.override
    //
    //  # property3.in.base.and.database = 
    //
    //  property4.in.base.and.override.and.database = value4.in.override
    //
    //  property5.in.override.and.database = value5.in.override
    //
    //  property6.in.override = value6.in.override
    //
    //  # property7.in.database

    int databaseConfigCount = ConfigDatabaseLogic.databaseConfigRefreshCount;
    
    assertEquals("value1.in.base", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property1.in.base.only"));
    
    assertEquals(databaseConfigCount +1, ConfigDatabaseLogic.databaseConfigRefreshCount);
    
    assertEquals("value2.in.override", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property2.in.base.and.override"));
    assertEquals("value3.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property3.in.base.and.database"));
    assertEquals("value4.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property4.in.base.and.override.and.database"));
    assertEquals("value5.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property5.in.override.and.database"));
    assertEquals("value6.in.override", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property6.in.override"));
    assertEquals("value7.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property7.in.database"));

    assertEquals(databaseConfigCount +1, ConfigDatabaseLogic.databaseConfigRefreshCount);
  }
  
  /**
   * 
   */
  public void testDatabaseCacheClear() {
    
    // delete the test config properties
    HibernateSession.bySqlStatic().executeSql(
        "delete from grouper_config where config_file_name = ?" , 
        HibUtils.listObject(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigClasspath()),
        HibUtils.listType(StringType.INSTANCE));

    EhcacheController.ehcacheController().flushCache();
    ConfigPropertiesCascadeBase.clearCache();

    assertEquals("value1.in.base", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property1.in.base.only"));
    assertEquals("value3.in.base", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property3.in.base.and.database"));

    //  ########################################
    //  ## Properties to test
    //  ########################################
    //
    //  # property1.in.base.only = 
    //
    //  property2.in.base.and.override = value2.in.override
    //
    //  # property3.in.base.and.database = 
    //
    //  property4.in.base.and.override.and.database = value4.in.override
    //
    //  property5.in.override.and.database = value5.in.override
    //
    //  property6.in.override = value6.in.override
    //
    //  # property7.in.database
    
    GrouperConfigHibernate grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property3.in.base.and.database");
    grouperConfigHibernate.setValueToSave("value3.in.database");
    GrouperDAOFactory.getFactory().getConfig().saveOrUpdate(grouperConfigHibernate);
    
    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property4.in.base.and.override.and.database");
    grouperConfigHibernate.setValueToSave("value4.in.database");
    GrouperDAOFactory.getFactory().getConfig().saveOrUpdate(grouperConfigHibernate);

    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property5.in.override.and.database");
    grouperConfigHibernate.setValueToSave("value5.in.database");
    GrouperDAOFactory.getFactory().getConfig().saveOrUpdate(grouperConfigHibernate);

    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property7.in.database");
    grouperConfigHibernate.setValueToSave("value7.in.database");
    GrouperDAOFactory.getFactory().getConfig().saveOrUpdate(grouperConfigHibernate);

    // doesnt know about it yet
    assertEquals("value3.in.base", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property3.in.base.and.database"));

    //notify about changes
    long nowNanos = System.currentTimeMillis() * 1000000;
    
    
    int rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_instance set nanos_since_1970 = ? where cache_name = ?")
      .addBindVar(nowNanos).addBindVar("custom__" + ConfigDatabaseLogic.DATABASE_CACHE_KEY).executeSql();
  
    assertEquals(1, rowsUpdated);
    
    rowsUpdated = new GcDbAccess()
      .sql("update grouper_cache_overall set nanos_since_1970 = ? where overall_cache = 0")
      .addBindVar(nowNanos).executeSql();
  
    assertEquals(1, rowsUpdated);
  
    int databaseConfigCount = ConfigDatabaseLogic.databaseConfigRefreshCount;
    
    // still doesnt see it
    assertEquals("value3.in.base", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property3.in.base.and.database"));

    // wait a bit, then good
    GrouperUtil.sleep(6000);

    
    //  # property1.in.base.only = 
    //
    //  property2.in.base.and.override = value2.in.override
    //
    //  # property3.in.base.and.database = 
    //
    //  property4.in.base.and.override.and.database = value4.in.override
    //
    //  property5.in.override.and.database = value5.in.override
    //
    //  property6.in.override = value6.in.override
    //
    //  # property7.in.database

    assertEquals("value1.in.base", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property1.in.base.only"));
    
    assertEquals(databaseConfigCount +1, ConfigDatabaseLogic.databaseConfigRefreshCount);
    
    assertEquals("value2.in.override", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property2.in.base.and.override"));
    assertEquals("value3.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property3.in.base.and.database"));
    assertEquals("value4.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property4.in.base.and.override.and.database"));
    assertEquals("value5.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property5.in.override.and.database"));
    assertEquals("value6.in.override", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property6.in.override"));
    assertEquals("value7.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property7.in.database"));

    assertEquals(databaseConfigCount +1, ConfigDatabaseLogic.databaseConfigRefreshCount);
  }
  

}
