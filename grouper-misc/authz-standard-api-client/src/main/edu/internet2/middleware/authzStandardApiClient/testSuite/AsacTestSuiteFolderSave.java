package edu.internet2.middleware.authzStandardApiClient.testSuite;

import edu.internet2.middleware.authzStandardApiClient.api.AsacApiFolderDelete;
import edu.internet2.middleware.authzStandardApiClient.api.AsacApiFolderSave;
import edu.internet2.middleware.authzStandardApiClient.contentType.AsacRestContentType;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacFolderLookup;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacFolderSaveResponse;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientConfig;

/**
 * test put folder
 * @author mchyzer
 */
public class AsacTestSuiteFolderSave extends AsacTestSuiteResult {

  /**
   * 
   * @param results
   */
  public AsacTestSuiteFolderSave(AsacTestSuiteResults results) {
    super(results);
  }

  /**
   * test put folder json
   */
  public void testPutFolderJson() {
    
    helperTestPutFolderResource(AsacRestContentType.json);

  }

  /**
   * test put folder xml
   */
  public void testPutFolderXml() {
    
    helperTestPutFolderResource(AsacRestContentType.xml);

  }

  /**
   * 
   * @param asacRestContentType
   */
  private void helperTestPutFolderResource(AsacRestContentType asacRestContentType) {

    AsacApiFolderSave asacApiFolderSave = new AsacApiFolderSave();
    AsacFolderLookup asacFolderLookup = new AsacFolderLookup();

    String rootFolder = StandardApiClientConfig.retrieveConfig().unitTestRootFolder();
    String theName = rootFolder + ":test";
    
    //first delete the folder
    new AsacApiFolderDelete().assignFolderLookup(new AsacFolderLookup(theName, null)).assignRecursive(true).execute();
    
    asacFolderLookup.setName(theName + asacRestContentType.name());
    asacApiFolderSave.assignFolderLookup(asacFolderLookup);
    asacApiFolderSave.setContentType(asacRestContentType);
    
    asacApiFolderSave
      .assignIndent(this.getResults().isIndent());
    
    AsacFolderSaveResponse asacFolderSaveResponse = asacApiFolderSave.execute();
    
    //201 created, 200 updated?
    executeTestsForHttp(201, asacRestContentType);
    
    executeTestsForServiceMeta(asacFolderSaveResponse);
    
    executeTestsForResponseMeta(asacFolderSaveResponse, 201);
    
    executeTestsForMeta(asacFolderSaveResponse, "SUCCESS", "asacFolderSaveResponse", "." + asacRestContentType.name());

    assertNotNull("asacFolderSaveResponse", asacFolderSaveResponse.getFolder());

  }
  
  /**
   * 
   */
  @Override
  public String getName() {
    return "folderSave";
  }
    
  
}
