package edu.internet2.middleware.grouper.ws.samples.rest.gshTemplate;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateExecResult;
import edu.internet2.middleware.grouper.ws.coresoap.WsGshTemplateInput;
import edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup;
import edu.internet2.middleware.grouper.ws.rest.WsRestResultProblem;
import edu.internet2.middleware.grouper.ws.rest.gshTemplate.WsRestGshTemplateExecRequest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRest;
import edu.internet2.middleware.grouper.ws.samples.types.WsSampleRestType;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

public class WsSampleGshTemplateExecRest implements WsSampleRest {
  
  
  /**
   * gsh template execute 
   * @param wsSampleRestType is the type of rest (json)
   */
  @SuppressWarnings("deprecation")
  public static void executeGshTemplate(WsSampleRestType wsSampleRestType) {

    try {
      // grouper http client
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      String url = RestClientSettings.URL + "/" + RestClientSettings.VERSION  
            + "/gshTemplateExec";

      // assign the URL and method
      grouperHttpClient.assignUrl(url).assignGrouperHttpMethod(GrouperHttpMethod.post);
      // assign user and pass
      grouperHttpClient.assignUser(RestClientSettings.USER)
          .assignPassword(RestClientSettings.PASS);

      WsRestGshTemplateExecRequest templateExecRequest = new WsRestGshTemplateExecRequest();
      
      templateExecRequest.setConfigId("testGshTemplateConfig");
      templateExecRequest.setOwnerType("stem");
      templateExecRequest.setGshTemplateActAsSubjectLookup(null);
      
      WsStemLookup ownerStemLookup = new WsStemLookup();
      ownerStemLookup.setStemName("test2");
      
      templateExecRequest.setOwnerStemLookup(ownerStemLookup);
      WsGshTemplateInput[] inputs = new WsGshTemplateInput[1];
      WsGshTemplateInput wsGshTemplateInput = new WsGshTemplateInput();
      wsGshTemplateInput.setName("gsh_input_prefix");
      wsGshTemplateInput.setValue("TEST");
      inputs[0] = wsGshTemplateInput;
      templateExecRequest.setInputs(inputs);
    
      String requestDocument = wsSampleRestType.getWsLiteRequestContentType().writeString(templateExecRequest);
      
      //make sure right content type is in request (e.g. application/xhtml+xml
      String contentType = wsSampleRestType.getWsLiteRequestContentType().getContentType();
      
      // assign body
      grouperHttpClient.assignBody(requestDocument);

      // content type
      grouperHttpClient.addHeader("Content-Type", contentType);
      
      // execute
      grouperHttpClient.executeRequest();
      

      //check if success
      String successString = grouperHttpClient.getResponseHeadersLower().get("x-grouper-success");    

      if (StringUtils.isBlank(successString)) {
        throw new RuntimeException("Web service did not even respond!");
      }
      boolean success = "T".equals(successString);

      // check result code
      String resultCode = grouperHttpClient.getResponseHeadersLower().get("x-grouper-resultcode");
      
      String response = grouperHttpClient.getResponseBody();

      Object result = wsSampleRestType
        .getWsLiteResponseContentType().parseString(response);
      
      //see if problem
      if (result instanceof WsRestResultProblem) {
        throw new RuntimeException(((WsRestResultProblem)result).getResultMetadata().getResultMessage());
      }
      
      //convert to object (from json)
      WsGshTemplateExecResult wsGshTemplateExecResult = (WsGshTemplateExecResult)result;
      
      String resultMessage = wsGshTemplateExecResult.getResultMetadata().getResultMessage();

      // see if request worked or not
      if (!success) {
        throw new RuntimeException("Bad response from web service: successString: " + successString 
            + ", resultCode: " + resultCode
            + ", " + resultMessage);
      }
      
      System.out.println("Server version: " + wsGshTemplateExecResult.getResponseMetadata().getServerVersion()
          + ", result code: " + resultCode
          + ", result message: " + resultMessage );

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  public static void main(String[] args) {
    executeGshTemplate(WsSampleRestType.json);
  }

  @Override
  public void executeSample(WsSampleRestType wsSampleRestType) {
    executeGshTemplate(wsSampleRestType);
    
  }

  @Override
  public boolean validType(WsSampleRestType wsSampleRestType) {
    if (wsSampleRestType == WsSampleRestType.json) {
      return true;
    }
    return false;
  }

}
