package edu.internet2.middleware.grouper.ws.samples.rest.gshTemplate;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;

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
      HttpClient httpClient = new HttpClient();
      
      DefaultHttpParams.getDefaultParams().setParameter(
          HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

      //URL e.g. http://localhost:8093/grouper-ws/servicesRest/v1_3_000/...
      PostMethod method = new PostMethod(
          RestClientSettings.URL + "/" + RestClientSettings.VERSION  
            + "/gshTemplateExec");

      httpClient.getParams().setAuthenticationPreemptive(true);
      Credentials defaultcreds = new UsernamePasswordCredentials(RestClientSettings.USER, 
          RestClientSettings.PASS);

      //no keep alive so response if easier to indent for tests
      method.setRequestHeader("Connection", "close");
      
      //e.g. localhost and 8093
      httpClient.getState()
          .setCredentials(new AuthScope(RestClientSettings.HOST, RestClientSettings.PORT), defaultcreds);

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
      
      StringRequestEntity requestEntity = new StringRequestEntity(requestDocument, contentType, "UTF-8");
      method.setRequestEntity(requestEntity);
      int httpStatusCode = httpClient.executeMethod(method);

      //make sure a request came back
      Header successHeader = method.getResponseHeader("X-Grouper-success");
      String successString = successHeader == null ? null : successHeader.getValue();
      if (StringUtils.isBlank(successString)) {
        throw new RuntimeException("Web service did not even respond!");
      }
      boolean success = "T".equals(successString);
      String resultCode = method.getResponseHeader("X-Grouper-resultCode").getValue();
      
      String response = RestClientSettings.responseBodyAsString(method);

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
