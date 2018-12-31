/**
 * 
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
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
    TestRunner.run(new GrouperDbConfigTest("testDatabaseOverride"));

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
    grouperConfigHibernate.setConfigValue("value3.in.database");
    grouperConfigHibernate.saveOrUpdate();
    
    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property4.in.base.and.override.and.database");
    grouperConfigHibernate.setConfigValue("value4.in.database");
    grouperConfigHibernate.saveOrUpdate();

    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property5.in.override.and.database");
    grouperConfigHibernate.setConfigValue("value5.in.database");
    grouperConfigHibernate.saveOrUpdate();

    grouperConfigHibernate = new GrouperConfigHibernate();
    grouperConfigHibernate.setConfigEncrypted(false);
    grouperConfigHibernate.setConfigFileHierarchy(ConfigFileHierarchy.INSTITUTION);
    grouperConfigHibernate.setConfigFileNameDb(GrouperDbConfigTestConfig.retrieveConfig().getMainConfigFileName());
    grouperConfigHibernate.setConfigKey("property7.in.database");
    grouperConfigHibernate.setConfigValue("value7.in.database");
    grouperConfigHibernate.saveOrUpdate();

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

    int databaseConfigCount = ConfigPropertiesCascadeBase.databaseConfigRefreshCount;
    
    assertEquals("value1.in.base", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property1.in.base.only"));
    
    assertEquals(databaseConfigCount +1, ConfigPropertiesCascadeBase.databaseConfigRefreshCount);
    
    assertEquals("value2.in.override", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property2.in.base.and.override"));
    assertEquals("value3.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property3.in.base.and.database"));
    assertEquals("value4.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property4.in.base.and.override.and.database"));
    assertEquals("value5.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property5.in.override.and.database"));
    assertEquals("value6.in.override", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property6.in.override"));
    assertEquals("value7.in.database", GrouperDbConfigTestConfig.retrieveConfig().propertyValueString("property7.in.database"));

    assertEquals(databaseConfigCount +1, ConfigPropertiesCascadeBase.databaseConfigRefreshCount);
  }
  
}
