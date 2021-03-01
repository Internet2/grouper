package edu.internet2.middleware.grouper.ws.rest.gshTemplate;

import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;

public class WsRestGshTemplateExecRequest implements WsRequestBean {
  
  
  private String configId;
  
  private String ownerType;
  
  private WsGroupLookup ownerGroupLookup;
  
  private WsStemLookup ownerStemLookup;
  
  /**
   * if the template config has an actAsGroupUUID and if the principal user calling the webservice is in that group, then specify a user to run the template as. 
   * An external UI could run template as other users. 
   */
  private WsSubjectLookup gshTemplateActAsSubjectLookup;
  
  
  private WsGshTemplateInput[] inputs;
  
  /** field */
  private String clientVersion;
  
  /** field */
  private WsSubjectLookup actAsSubjectLookup;
  
  /** field */
  private WsParam[] params;
  
  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }



  
  public String getOwnerType() {
    return ownerType;
  }



  
  public void setOwnerType(String ownerType) {
    this.ownerType = ownerType;
  }



  
  public WsGroupLookup getOwnerGroupLookup() {
    return ownerGroupLookup;
  }



  
  public void setOwnerGroupLookup(WsGroupLookup ownerGroupLookup) {
    this.ownerGroupLookup = ownerGroupLookup;
  }



  
  public WsStemLookup getOwnerStemLookup() {
    return ownerStemLookup;
  }



  
  public void setOwnerStemLookup(WsStemLookup ownerStemLookup) {
    this.ownerStemLookup = ownerStemLookup;
  }



  
  public WsGshTemplateInput[] getInputs() {
    return inputs;
  }



  
  public void setInputs(WsGshTemplateInput[] inputs) {
    this.inputs = inputs;
  }



  
  public String getClientVersion() {
    return clientVersion;
  }



  
  public void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }



  
  public WsSubjectLookup getActAsSubjectLookup() {
    return actAsSubjectLookup;
  }



  
  public void setActAsSubjectLookup(WsSubjectLookup actAsSubjectLookup) {
    this.actAsSubjectLookup = actAsSubjectLookup;
  }



  
  public WsParam[] getParams() {
    return params;
  }



  
  public void setParams(WsParam[] params) {
    this.params = params;
  }
  

  /**
   * if the template config has an actAsGroupUUID and if the principal user calling the webservice is in that group, then specify a user to run the template as. 
   * An external UI could run template as other users.
   * @return
   */
  public WsSubjectLookup getGshTemplateActAsSubjectLookup() {
    return gshTemplateActAsSubjectLookup;
  }


  /**
   * if the template config has an actAsGroupUUID and if the principal user calling the webservice is in that group, then specify a user to run the template as. 
   * An external UI could run template as other users.
   * @param gshTemplateActAsSubjectLookup
   */
  public void setGshTemplateActAsSubjectLookup(WsSubjectLookup gshTemplateActAsSubjectLookup) {
    this.gshTemplateActAsSubjectLookup = gshTemplateActAsSubjectLookup;
  }


  @Override
  public GrouperRestHttpMethod retrieveRestHttpMethod() {
    return GrouperRestHttpMethod.PUT;
  }

}
