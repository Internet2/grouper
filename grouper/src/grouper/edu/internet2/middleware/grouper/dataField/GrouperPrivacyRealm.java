package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperPrivacyRealm implements OptionValueDriver {


  /**
   * some required config to see what the privacy realms
   */
  private static Pattern fieldConfigIds = Pattern.compile("^grouperPrivacyRealm\\.([^.]+)\\.privacyRealmName$");
  
  
  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
        
    Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(fieldConfigIds);
    List<MultiKey> results = new ArrayList<>();
    for (String theConfigId : GrouperUtil.nonNull(configIds)) {
      results.add(new MultiKey(theConfigId, theConfigId));
    }
    return results;
  }

}
