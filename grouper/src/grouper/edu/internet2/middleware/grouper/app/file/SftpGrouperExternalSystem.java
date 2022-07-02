package edu.internet2.middleware.grouper.app.file;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

public class SftpGrouperExternalSystem extends GrouperExternalSystem {

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperSftp.site." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperSftp\\.site)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "configId";
  }

  @Override
  public List<String> test() throws UnsupportedOperationException {
    GrouperSftp.existsFile(this.getConfigId(), "whatever");
    return null;
  }

}
