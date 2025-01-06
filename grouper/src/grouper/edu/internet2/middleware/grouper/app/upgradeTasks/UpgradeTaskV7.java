package edu.internet2.middleware.grouper.app.upgradeTasks;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class UpgradeTaskV7 implements UpgradeTasksInterface {

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    
    Pattern gshTemplateFolderUuidsToShowPattern = Pattern.compile("^grouperGshTemplate\\.([^.]+)\\.folderUuidsToShow$");
    
    Map<String, String> properties = GrouperConfig.retrieveConfig().propertiesMap(gshTemplateFolderUuidsToShowPattern);
    
    if (GrouperUtil.length(properties) > 0) {
      
      for (String key : properties.keySet()) {
        
        Matcher matcher = gshTemplateFolderUuidsToShowPattern.matcher(key);
        if (matcher.matches()) {
          String configId = matcher.group(1);
          String folderUuidsToShow = properties.get("grouperGshTemplate." + configId + ".folderUuidsToShow");
          folderUuidsToShow = StringUtils.trim(folderUuidsToShow);
          
          String singularFolderUuidToShow = GrouperConfig.retrieveConfig().propertyValueString("grouperGshTemplate." + configId + ".folderUuidToShow");
          singularFolderUuidToShow = StringUtils.trim(singularFolderUuidToShow);
          
          if (!StringUtils.equals(folderUuidsToShow, singularFolderUuidToShow)) {
            new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate." + configId + ".folderUuidToShow")
            .value(folderUuidsToShow).store();
          }
          
        }
      }
      
    }
    
  }
  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("4.0.0");
  }

}
