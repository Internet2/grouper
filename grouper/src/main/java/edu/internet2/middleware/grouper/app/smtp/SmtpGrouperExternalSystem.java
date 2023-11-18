package edu.internet2.middleware.grouper.app.smtp;

import java.util.List;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperEmail;

public class SmtpGrouperExternalSystem extends GrouperExternalSystem {

  @Override
  public List<String> test() throws UnsupportedOperationException {
    return GrouperEmail.externalSystemTest();
  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    return "mail.smtp.";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(mail)\\.(smtp)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "smtp";
  }

  @Override
  public boolean isCanAdd() {
    return false;
  }

  @Override
  public boolean isCanDelete() {
    return false;
  }
}
