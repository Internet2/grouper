package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public abstract class GrouperExternalSystem {
  
  private String configId;
  
  private boolean enabled;
  
  public List<String> validate(boolean isAdd) {
    List<String> errors = new ArrayList<String>();
    if(StringUtils.isBlank(configId)) {
      errors.add(""); //TODO fill me
    }
    return errors;
  }
  
  public List<String> test() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  
  /**
   * 
   */
  public abstract void insertConfig();
  
  /**
   * @return true if made changes
   */
  public abstract boolean editConfig();
  
  /**
   * 
   */
  public abstract void deleteConfig();
  
  /**
   * 
   * @return
   */
  public abstract List<GrouperExternalSystem> listAllExternalSystemsOfThisType();
  
  /**
   * 
   * @return
   */
  public abstract List<GrouperExternalSystemConsumer> retrieveAllUsedBy();
  
  /**
   * 
   * @return
   */
  public abstract List<GrouperExternalSystemAttribute> retrieveAttributes();

}
