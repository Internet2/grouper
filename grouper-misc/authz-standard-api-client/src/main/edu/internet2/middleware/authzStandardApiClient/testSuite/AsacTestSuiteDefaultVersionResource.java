package edu.internet2.middleware.authzStandardApiClient.testSuite;

import edu.internet2.middleware.authzStandardApiClient.api.AsacApiDefaultVersionResource;
import edu.internet2.middleware.authzStandardApiClient.contentType.AsacRestContentType;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultVersionResourceContainer;

/**
 * test the default version resource
 * @author mchyzer
 */
public class AsacTestSuiteDefaultVersionResource extends AsacTestSuiteResult {

  /**
   * 
   * @param results
   */
  public AsacTestSuiteDefaultVersionResource(AsacTestSuiteResults results) {
    super(results);
  }

  /**
   * test default resource
   */
  public void testDefaultVersionResourceJson() {
    
    helperTestDefaultVersionResource(AsacRestContentType.json);

  }

  /**
   * test default resource
   */
  public void testDefaultVersionResourceXml() {
    
    helperTestDefaultVersionResource(AsacRestContentType.xml);

  }

  /**
   * 
   * @param asacRestContentType
   */
  private void helperTestDefaultVersionResource(AsacRestContentType asacRestContentType) {

    AsacApiDefaultVersionResource asacApiDefaultVersionResource = new AsacApiDefaultVersionResource();
    asacApiDefaultVersionResource.setContentType(asacRestContentType);
    AsacDefaultVersionResourceContainer asacDefaultVersionResourceContainer = asacApiDefaultVersionResource
      .assignIndent(this.getResults().isIndent()).execute();
    
    executeTestsForHttp(200, asacRestContentType);
    
    executeTestsForServiceMeta(asacDefaultVersionResourceContainer);
    
    executeTestsForResponseMeta(asacDefaultVersionResourceContainer, 200);
    
    executeTestsForMeta(asacDefaultVersionResourceContainer, "SUCCESS", "defaultVersionResourceContainer", "." + asacRestContentType.name());
  
    String serviceRootUri = asacDefaultVersionResourceContainer.getServiceMeta().getServiceRootUri();
  
    assertNotNull("asasDefaultVersionResource", asacDefaultVersionResourceContainer.getAsasDefaultVersionResource());
    assertEquals("asasDefaultVersionResource.v1Uri", serviceRootUri + "/v1." + asacRestContentType.name(), 
        asacDefaultVersionResourceContainer.getAsasDefaultVersionResource().getV1Uri());
    assertNotBlank("asasDefaultVersionResource.serverType",  
        asacDefaultVersionResourceContainer.getAsasDefaultVersionResource().getServerType());
  
  }
  
  /**
   * 
   */
  @Override
  public String getName() {
    return "defaultResource";
  }
    
  
}
