package edu.internet2.middleware.grouper.app.dataProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 */
public class GrouperDataProviderSync {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(GrouperDataProviderSync.class);
  
  private String configId;
  
  private GrouperDataProviderLogic grouperDataProviderLogic;
  
  private GrouperDataEngine grouperDataEngine;
  
  /**
   * debug map for this provisioner
   */
  private Map<String, Object> debugMap = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
  
  /**
   * loader log
   */
  private Hib3GrouperLoaderLog hib3GrouperLoaderLog;
  
  
  /**
   * loader log
   * @return log
   */
  public Hib3GrouperLoaderLog getHib3GrouperLoaderLog() {
    return this.hib3GrouperLoaderLog;
  }

  /**
   * loader log
   * @param hib3GrouperLoaderLog1
   */
  public void setHib3GrouperLoaderLog(Hib3GrouperLoaderLog hib3GrouperLoaderLog1) {
    this.hib3GrouperLoaderLog = hib3GrouperLoaderLog1;
  }
  
  /**
   * factory method to get a data provider sync by config id
   * @param configId
   * @return the data provider sync
   */
  public static GrouperDataProviderSync retrieveDataProviderSync(String configId) {
    
    // TODO
    String className = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperDataProvider." + configId + ".class", "edu.internet2.middleware.grouper.app.dataProvider.GrouperDataProviderSync");
    @SuppressWarnings("unchecked")
    Class<GrouperDataProviderSync> theClass = GrouperUtil.forName(className);
    GrouperDataProviderSync dataProviderSync = GrouperUtil.newInstance(theClass);
    dataProviderSync.setConfigId(configId);
    return dataProviderSync;
  }
  
  private Set<GrouperDataProviderQuery> grouperDataProviderQueries = null;
  
  public Set<GrouperDataProviderQuery> retrieveGrouperDataProviderQueries() {
    if (this.grouperDataProviderQueries == null) {
      this.grouperDataProviderQueries = new LinkedHashSet<GrouperDataProviderQuery>();
      
      GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
      Set<String> configIdsInConfig = GrouperUtil.nonNull(grouperConfig.propertyConfigIds(GrouperDataEngine.dataProviderQueryPattern));
      
      for (String queryConfigId : configIdsInConfig) {
        String providerConfigId = grouperConfig.propertyValueStringRequired("grouperDataProviderQuery." + queryConfigId + ".providerConfigId");

        if (!providerConfigId.equals(configId)) {
          continue;
        }
        
        String providerQueryType = grouperConfig.propertyValueStringRequired("grouperDataProviderQuery." + queryConfigId + ".providerQueryType");
        
        GrouperDataProviderQuery grouperDataProviderQuery;
        
        if (providerQueryType.equals("ldap")) {
          grouperDataProviderQuery = new GrouperLdapDataProviderQuery();
        } else if (providerQueryType.equals("sql")) {
          grouperDataProviderQuery = new GrouperSqlDataProviderQuery();
        } else {
          String className = grouperConfig.propertyValueStringRequired("grouperDataProviderQuery." + queryConfigId + ".class");
          @SuppressWarnings("unchecked")
          Class<GrouperDataProviderQuery> theClass = GrouperUtil.forName(className);
          grouperDataProviderQuery = GrouperUtil.newInstance(theClass);
        }
        
        grouperDataProviderQuery.setGrouperDataProviderSync(this);
        grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig().configureGenericSettings(queryConfigId, grouperConfig);
        grouperDataProviderQuery.retrieveGrouperDataProviderQueryConfig().configureSpecificSettings();        
        this.grouperDataProviderQueries.add(grouperDataProviderQuery);
      }
    }
    return this.grouperDataProviderQueries;
  }

  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }
  
  /**
   * return the class of the data provider logic
   */
  protected Class<? extends GrouperDataProviderLogic> grouperDataProviderLogicClass() {
    return GrouperDataProviderLogic.class;
  }
  
  /**
   * return the instance of the data provider logic
   * @return the logic
   */
  public GrouperDataProviderLogic retrieveGrouperDataProviderLogic() {
    if (this.grouperDataProviderLogic == null) {
      Class<? extends GrouperDataProviderLogic> grouperDataProviderLogicClass = this.grouperDataProviderLogicClass();
      this.grouperDataProviderLogic = GrouperUtil.newInstance(grouperDataProviderLogicClass);
      this.grouperDataProviderLogic.setGrouperDataProviderSync(this);
    }

    return this.grouperDataProviderLogic;
  }
  
  public void runSync(GrouperDataProviderSyncType grouperDataProviderSyncType) {
    grouperDataProviderSyncType.sync(this);
  }
  
  public Map<String, Object> getDebugMap() {
    return debugMap;
  }
  
  public void setDebugMap(Map<String, Object> debugMap) {
    this.debugMap = debugMap;
  }

  
  public GrouperDataEngine getGrouperDataEngine() {
    return grouperDataEngine;
  }

  
  public void setGrouperDataEngine(GrouperDataEngine grouperDataEngine) {
    this.grouperDataEngine = grouperDataEngine;
  }
}
