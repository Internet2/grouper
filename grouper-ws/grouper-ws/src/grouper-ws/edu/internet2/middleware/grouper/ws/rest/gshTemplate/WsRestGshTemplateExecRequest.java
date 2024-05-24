package edu.internet2.middleware.grouper.ws.rest.gshTemplate;

import java.util.Map;

import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput;
import edu.internet2.middleware.grouper.ws.coresoap.WsParam;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRequestBean;
import edu.internet2.middleware.grouper.ws.rest.method.GrouperRestHttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel(description = "bean that will be the data from rest request for executing gsh template<br /><br /><b>gshTemplateActAsSubjectLookup</b>: if the template config has an actAsGroupUUID and if the principal user calling the webservice is in that group, then specify a user to run the template as. An external UI could run template as other users. <br />"
    + "<br /><br /><b>ownerGroupLookups</b>: group where the template is being called<br />"
    + "<br /><br /><b>ownerStemLookups</b>: stem where the template is being called<br />")
public class WsRestGshTemplateExecRequest implements WsRequestBean {
  
  private Map<String, Object> wsInput;
  
  public Map<String, Object> getWsInput() {
    return wsInput;
  }
  
  public void setWsInput(Map<String, Object> wsInput) {
    this.wsInput = wsInput;
  }

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
  
  @ApiModelProperty(value = "config id is template config that is being run", example = "myConfigId")
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }


/**
 * type of owner, group or stem
 * @return owner type
 */
  @ApiModelProperty(value = "the type of the owner", example = "stem or group")
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



  @ApiModelProperty(value = "Version of the client (i.e. that the client was coded against)", example = "v2_6_001")
  public String getClientVersion(){
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
