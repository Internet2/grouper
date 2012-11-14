package edu.internet2.middleware.authzStandardApiClient.testSuite;

import edu.internet2.middleware.authzStandardApiClient.api.AsacApiDefaultResource;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacDefaultResourceContainer;



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
    
    AsacDefaultResourceContainer asacDefaultResourceContainer = new AsacApiDefaultResource().execute();
    
    assertNotNull("meta", asacDefaultResourceContainer.getMeta());
    
  }

  /**
   * 
   */
  @Override
  public String getName() {
    return "defaultResource";
  }
    
  
}
