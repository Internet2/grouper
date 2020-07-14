package edu.internet2.middleware.grouper.app.loader.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class DatabaseGrouperExternalSystem extends GrouperExternalSystem {

  public static void main(String[] args) {
    GrouperStartup.startup();
    // ldap://ldap.andrew.cmu.edu:389/dc=cmu,dc=edu
    DatabaseGrouperExternalSystem databaseGrouperExternalSystem = new DatabaseGrouperExternalSystem();
    databaseGrouperExternalSystem.setConfigId("mysql");
    
    System.out.println(databaseGrouperExternalSystem.test());
  }

  
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "db." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(db)\\.([^.]+)\\.(.*)$";
  }

  @Override
  public List<String> test() throws UnsupportedOperationException {

    GrouperLoaderDb grouperLoaderDb = new GrouperLoaderDb(this.getConfigId());

    String query = GrouperLoaderConfig.retrieveConfig().propertyValueString("db.warehouse.testQuery");
    
    if (StringUtils.isBlank(query)) {
      query = "select 1";
      
      if (GrouperClientUtils.isHsql(grouperLoaderDb.getUrl())) {
        query = "SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS";
      } else if (GrouperClientUtils.isOracle(grouperLoaderDb.getUrl())) {
        query = "select 1 from dual";
      }
    }

    new GcDbAccess().connectionName(this.getConfigId()).sql(query).select(String.class);

    return new ArrayList<String>();
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "warehouse";
  }
  
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    List<MultiKey> keysAndLabels = super.retrieveKeysAndLabels();
    
    keysAndLabels.add(new MultiKey("grouper", "grouper"));
    
    Collections.sort(keysAndLabels, new Comparator<MultiKey>() {

      @Override
      public int compare(MultiKey o1, MultiKey o2) {
        return ((String)o1.getKey(0)).compareTo((String)o2.getKey(0));
      }
    });
    
    return keysAndLabels;
  }

  
}
