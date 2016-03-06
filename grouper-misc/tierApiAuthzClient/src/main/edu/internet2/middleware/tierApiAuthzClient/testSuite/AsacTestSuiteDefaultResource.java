package edu.internet2.middleware.tierApiAuthzClient.testSuite;

import edu.internet2.middleware.tierApiAuthzClient.api.AsacApiDefaultResource;
import edu.internet2.middleware.tierApiAuthzClient.contentType.AsacRestContentType;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacDefaultResourceContainer;



/**
 * test the default resource
 * @author mchyzer
 *
 */
public class AsacTestSuiteDefaultResource extends AsacTestSuiteResult {

  /**
   * 
   * @param results
   */
  public AsacTestSuiteDefaultResource(AsacTestSuiteResults results) {
    super(results);
  }

  /**
   * test default resource
   */
  public void testDefaultResource() {
    
    AsacDefaultResourceContainer asacDefaultResourceContainer = new AsacApiDefaultResource()
      .assignIndent(this.getResults().isIndent()).execute();
    
    //HTTP/1.1 200 OK
    //Server: Apache-Coyote/1.1
    //Pragma: No-cache
    //Cache-Control: no-cache
    //Expires: Wed, 31 Dec 1969 19:00:00 EST
    //Content-Type: text/x-json;charset=UTF-8
    //Content-Length: 537
    //Date: Wed, 21 Nov 2012 05:17:12 GMT
    //Connection: close
    //
    //{
    //  "asasDefaultResource":{
    //    "jsonDefaultUri":"http://localhost:8090/grouperWs/authzStandardApi.json",
    //    "xmlDefaultUri":"http://localhost:8090/grouperWs/authzStandardApi.xml"
    //  },
    //  "meta":{
    //    "lastModified":"2012-11-19T18:49:09.253Z",
    //    "selfUri":"http://localhost:8090/grouperWs/authzStandardApi",
    //    "statusCode":"SUCCESS",
    //    "structureName":"defaultResourceContainer",
    //    "success":true
    //  },
    //  "responseMeta":{
    //    "httpStatusCode":200,
    //    "millis":2
    //  },
    //  "serviceMeta":{
    //    "serverVersion":"1.0",
    //    "serviceRootUri":"http://localhost:8090/grouperWs/authzStandardApi"
    //  }
    //}
    
    executeTestsForHttp(200, AsacRestContentType.json, "GET");
    
    executeTestsForServiceMeta(asacDefaultResourceContainer);
    
    executeTestsForResponseMeta(asacDefaultResourceContainer, 200);
    
    executeTestsForMeta(asacDefaultResourceContainer, "SUCCESS", "defaultResourceContainer", "", true);

    String serviceRootUri = asacDefaultResourceContainer.getServiceMeta().getServiceRootUri();
    
    assertNotNull("asasDefaultResource", asacDefaultResourceContainer.getDefaultResource());
    assertEquals("asasDefaultResource.jsonDefaultUri", serviceRootUri + ".json", 
        asacDefaultResourceContainer.getDefaultResource().getJsonDefaultUri());
    assertEquals("asasDefaultResource.xmlDefaultUri", serviceRootUri + ".xml", 
        asacDefaultResourceContainer.getDefaultResource().getXmlDefaultUri());
    
  }

  /**
   * 
   */
  @Override
  public String getName() {
    return "defaultResource";
  }

  /**
   * test default resource
   */
  public void testDefaultContentTypedResource() {
    
    AsacDefaultResourceContainer asacDefaultResourceContainer = new AsacApiDefaultResource()
      .assignIndent(this.getResults().isIndent()).execute();
    
    
    executeTestsForHttp(200, AsacRestContentType.json, "GET");
    
    executeTestsForServiceMeta(asacDefaultResourceContainer);
    
    executeTestsForResponseMeta(asacDefaultResourceContainer, 200);
    
    executeTestsForMeta(asacDefaultResourceContainer, "SUCCESS", "defaultResourceContainer", "", true);
  
    String serviceRootUri = asacDefaultResourceContainer.getServiceMeta().getServiceRootUri();
    
    assertNotNull("asasDefaultResource", asacDefaultResourceContainer.getDefaultResource());
    assertEquals("asasDefaultResource.jsonDefaultUri", serviceRootUri + ".json", 
        asacDefaultResourceContainer.getDefaultResource().getJsonDefaultUri());
    assertEquals("asasDefaultResource.xmlDefaultUri", serviceRootUri + ".xml", 
        asacDefaultResourceContainer.getDefaultResource().getXmlDefaultUri());
    
  }
    
  
}
