package edu.internet2.middleware.authzStandardApiClient.testSuite;

import edu.internet2.middleware.authzStandardApiClient.api.AsacApiFolderDelete;
import edu.internet2.middleware.authzStandardApiClient.api.AsacApiFolderSave;
import edu.internet2.middleware.authzStandardApiClient.contentType.AsacRestContentType;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacFolder;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacFolderLookup;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacFolderSaveResponse;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacSaveMode;
import edu.internet2.middleware.authzStandardApiClient.exceptions.StandardApiClientWsException;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientConfig;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;

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
    String theName = rootFolder + ":test" + asacRestContentType.name();
    
    //first delete the folder
    AsacApiFolderDelete asacApiFolderDelete = new AsacApiFolderDelete()
      .assignFolderLookup(new AsacFolderLookup(theName, null)).assignRecursive(true);
    
    asacApiFolderDelete.setContentType(asacRestContentType);
    
    asacApiFolderDelete.execute();
    
    asacFolderLookup.setName(theName);
    asacApiFolderSave.assignFolderLookup(asacFolderLookup);
    asacApiFolderSave.setContentType(asacRestContentType);
    
    asacApiFolderSave
      .assignIndent(this.getResults().isIndent());
    
    AsacFolderSaveResponse asacFolderSaveResponse = asacApiFolderSave.execute();
    
    //201 created, 200 updated?
    executeTestsForHttp(201, asacRestContentType, "PUT");
    
    executeTestsForServiceMeta(asacFolderSaveResponse);
    
    executeTestsForResponseMeta(asacFolderSaveResponse, 201);
    
    executeTestsForMeta(asacFolderSaveResponse, "FOLDER_CREATED", "folderSaveResponse", 
        "/" + StandardApiClientUtils.version() + "/folders/name:" + theName + "." + asacRestContentType.name(), true);

    assertNotNull("folderSaveResponse", asacFolderSaveResponse.getFolder());

    asacApiFolderDelete = new AsacApiFolderDelete()
      .assignFolderLookup(new AsacFolderLookup(theName, null)).assignRecursive(true);
    
    asacApiFolderDelete.setContentType(asacRestContentType);
    
    asacApiFolderDelete.execute();
  }
  
  /**
   * 
   */
  @Override
  public String getName() {
    return "folderSave";
  }

  /**
   * 
   * @param asacRestContentType
   */
  private void helperTestPutFolderCreateParents(AsacRestContentType asacRestContentType) {
  
    AsacApiFolderSave asacApiFolderSave = new AsacApiFolderSave();
    AsacFolderLookup asacFolderLookup = new AsacFolderLookup();
  
    String rootFolder = StandardApiClientConfig.retrieveConfig().unitTestRootFolder();
    String theName = rootFolder + ":test:a:b:c" + asacRestContentType.name();
    
    //first delete the folder
    AsacApiFolderDelete asacApiFolderDelete = new AsacApiFolderDelete()
      .assignFolderLookup(new AsacFolderLookup(rootFolder + ":test", null)).assignRecursive(true);
    
    asacApiFolderDelete.setContentType(asacRestContentType);
    
    asacApiFolderDelete.execute();
    
    asacFolderLookup.setName(theName);
    asacApiFolderSave.assignFolderLookup(asacFolderLookup);
    asacApiFolderSave.setContentType(asacRestContentType);
    
    asacApiFolderSave
      .assignIndent(this.getResults().isIndent());
    
    AsacFolderSaveResponse asacFolderSaveResponse = null;

    try {
      
      //this should throw an exception
      asacFolderSaveResponse = asacApiFolderSave.execute();
      assertTrue("should throw exception on update when not exist", false);

    } catch (StandardApiClientWsException sacwe) {

      //this is good
      asacFolderSaveResponse = (AsacFolderSaveResponse)sacwe.getResultObject();

    }
    
    //this should be a failure
    //409
    executeTestsForHttp(409, asacRestContentType, "PUT");
    
    executeTestsForServiceMeta(asacFolderSaveResponse);
    
    executeTestsForResponseMeta(asacFolderSaveResponse, 409);
    
    executeTestsForMeta(asacFolderSaveResponse, "FOLDER_NOT_EXIST", "folderSaveResponse", 
        "/" + StandardApiClientUtils.version() + "/folders/name:" + theName + "." + asacRestContentType.name(), false);
  
    assertNull("folderSaveResponse", asacFolderSaveResponse.getFolder());
    
    //now create and autocreate parents
    asacFolderSaveResponse = asacApiFolderSave.assignCreateParentFoldersIfNotExist(true).execute();

    //201 created, 200 updated?
    executeTestsForHttp(201, asacRestContentType, "PUT");
    
    executeTestsForServiceMeta(asacFolderSaveResponse);
    
    executeTestsForResponseMeta(asacFolderSaveResponse, 201);
    
    executeTestsForMeta(asacFolderSaveResponse, "FOLDER_CREATED", "folderSaveResponse", 
        "/" + StandardApiClientUtils.version() + "/folders/name:" + theName + "." 
            + asacRestContentType.name() + "?createParentFoldersIfNotExist=true", true);

    assertNotNull("folderSaveResponse", asacFolderSaveResponse.getFolder());

    //delete at end
    asacApiFolderDelete = new AsacApiFolderDelete()
      .assignFolderLookup(new AsacFolderLookup(rootFolder + ":test", null)).assignRecursive(true);

    asacApiFolderDelete.setContentType(asacRestContentType);
    
    asacApiFolderDelete.execute();

  }

  /**
   * test put folder json
   */
  public void testPutFolderCreateParentsJson() {
    
    helperTestPutFolderCreateParents(AsacRestContentType.json);
  
  }

  /**
   * test put folder xml
   */
  public void testPutFolderCreateParentsXml() {
    
    helperTestPutFolderCreateParents(AsacRestContentType.xml);
  
  }

  /**
   * test insert folder
   * @param asacRestContentType
   */
  private void helperTestPostFolder(AsacRestContentType asacRestContentType) {
  
    AsacApiFolderSave asacApiFolderSave = new AsacApiFolderSave();
    AsacFolderLookup asacFolderLookup = new AsacFolderLookup();
  
    String rootFolder = StandardApiClientConfig.retrieveConfig().unitTestRootFolder();
    String theName = rootFolder + ":a" + asacRestContentType.name();
    
    //first delete the folder
    AsacApiFolderDelete asacApiFolderDelete = new AsacApiFolderDelete()
      .assignFolderLookup(new AsacFolderLookup(theName, null)).assignRecursive(true);
    
    asacApiFolderDelete.setContentType(asacRestContentType);
    
    asacApiFolderDelete.execute();
    
    asacFolderLookup.setName(theName);
    asacApiFolderSave.assignFolderLookup(asacFolderLookup);
    asacApiFolderSave.setContentType(asacRestContentType);
    
    asacApiFolderSave
      .assignIndent(this.getResults().isIndent());
    
    //this makes it a post
    asacApiFolderSave.assignSaveMode(AsacSaveMode.INSERT);
    
    AsacFolderSaveResponse asacFolderSaveResponse = asacApiFolderSave.execute();

    //this should be a create
    //201
    executeTestsForHttp(201, asacRestContentType, "POST");
    
    executeTestsForServiceMeta(asacFolderSaveResponse);

    executeTestsForResponseMeta(asacFolderSaveResponse, 201);

    executeTestsForMeta(asacFolderSaveResponse, "FOLDER_CREATED", "folderSaveResponse", 
        "/" + StandardApiClientUtils.version() + "/folders/name:" + theName + "." + asacRestContentType.name(), true);

    assertNotNull("folderSaveResponse", asacFolderSaveResponse.getFolder());

    //try again and it is already there
    try {
      
      //this should throw an exception
      asacFolderSaveResponse = asacApiFolderSave.execute();
      assertTrue("should throw exception on update when not exist", false);

    } catch (StandardApiClientWsException sacwe) {

      //this is good
      asacFolderSaveResponse = (AsacFolderSaveResponse)sacwe.getResultObject();

    }
    
    //409
    executeTestsForHttp(409, asacRestContentType, "POST");
    
    executeTestsForServiceMeta(asacFolderSaveResponse);
    
    executeTestsForResponseMeta(asacFolderSaveResponse, 409);
    
    executeTestsForMeta(asacFolderSaveResponse, "FOLDER_EXISTS", "folderSaveResponse", 
        "/" + StandardApiClientUtils.version() + "/folders/name:" + theName + "." 
            + asacRestContentType.name(), false);
  
    assertNull("folderSaveResponse", asacFolderSaveResponse.getFolder());
  
    //delete at end
    asacApiFolderDelete = new AsacApiFolderDelete()
      .assignFolderLookup(new AsacFolderLookup(theName, null)).assignRecursive(true);
  
    asacApiFolderDelete.setContentType(asacRestContentType);
    
    asacApiFolderDelete.execute();
  
  }

  /**
   * test put folder json
   */
  public void testPostFolderJson() {
    
    helperTestPostFolder(AsacRestContentType.json);
  
  }

  /**
   * test put folder xml
   */
  public void testPostFolderXml() {
    
    helperTestPostFolder(AsacRestContentType.xml);
  
  }

  /**
   * test insert folder
   * @param asacRestContentType
   */
  private void helperTestPutFolderUpdate(AsacRestContentType asacRestContentType) {
  
    AsacApiFolderSave asacApiFolderSave = new AsacApiFolderSave();
    AsacFolderLookup asacFolderLookup = new AsacFolderLookup();
  
    String rootFolder = StandardApiClientConfig.retrieveConfig().unitTestRootFolder();
    String theName = rootFolder + ":a" + asacRestContentType.name();
    
    //first delete the folder
    AsacApiFolderDelete asacApiFolderDelete = new AsacApiFolderDelete()
      .assignFolderLookup(new AsacFolderLookup(theName, null)).assignRecursive(true);
    
    asacApiFolderDelete.setContentType(asacRestContentType);
    
    asacApiFolderDelete.execute();
    
    asacFolderLookup.setName(theName);
    asacApiFolderSave.assignFolderLookup(asacFolderLookup);
    asacApiFolderSave.setContentType(asacRestContentType);
    
    asacApiFolderSave
      .assignIndent(this.getResults().isIndent());
    
    //this makes it a put with update
    asacApiFolderSave.assignSaveMode(AsacSaveMode.UPDATE);

    AsacFolderSaveResponse asacFolderSaveResponse = null;

    try {
      
      //this should throw an exception
      asacFolderSaveResponse = asacApiFolderSave.execute();
      assertTrue("should throw exception on update when not exist", false);

    } catch (StandardApiClientWsException sacwe) {

      //this is good
      asacFolderSaveResponse = (AsacFolderSaveResponse)sacwe.getResultObject();

    }
      
    //this should be an error
    //409
    executeTestsForHttp(409, asacRestContentType, "PUT");
    
    executeTestsForServiceMeta(asacFolderSaveResponse);
    
    executeTestsForResponseMeta(asacFolderSaveResponse, 409);
    
    executeTestsForMeta(asacFolderSaveResponse, "FOLDER_NOT_EXIST", "folderSaveResponse", 
        "/" + StandardApiClientUtils.version() + "/folders/name:" + theName + "." + asacRestContentType.name() + "?saveMode=update", false);
  
    assertNull("folderSaveResponse", asacFolderSaveResponse.getFolder());

    //lets insert it
    asacApiFolderSave.assignSaveMode(AsacSaveMode.INSERT);
    asacFolderSaveResponse = asacApiFolderSave.execute();
    executeTestsForHttp(201, asacRestContentType, "POST");
    executeTestsForServiceMeta(asacFolderSaveResponse);
    executeTestsForResponseMeta(asacFolderSaveResponse, 201);
    
    
    //try again and it is already there
    asacApiFolderSave.assignSaveMode(AsacSaveMode.UPDATE);
    AsacFolder asacFolder = new AsacFolder();
    asacFolder.setDescription("description");
    asacApiFolderSave.assignFolder(asacFolder);
    asacFolderSaveResponse = asacApiFolderSave.execute();
    
    //200
    executeTestsForHttp(200, asacRestContentType, "PUT");
    
    executeTestsForServiceMeta(asacFolderSaveResponse);
    
    executeTestsForResponseMeta(asacFolderSaveResponse, 200);
    
    executeTestsForMeta(asacFolderSaveResponse, "FOLDER_UPDATED", "folderSaveResponse", 
        "/" + StandardApiClientUtils.version() + "/folders/name:" + theName + "." 
            + asacRestContentType.name() + "?saveMode=update", true);
  
    assertNotNull("folderSaveResponse", asacFolderSaveResponse.getFolder());
  
    asacFolder = asacFolderSaveResponse.getFolder();
    assertEquals("folder description set correctly", "description", asacFolder.getDescription());
    
    //delete at end
    asacApiFolderDelete = new AsacApiFolderDelete()
      .assignFolderLookup(new AsacFolderLookup(theName, null)).assignRecursive(true);
  
    asacApiFolderDelete.setContentType(asacRestContentType);
    
    asacApiFolderDelete.execute();
  
  }

  /**
   * test put folder update json
   */
  public void testPutFolderUpdateJson() {
    
    helperTestPutFolderUpdate(AsacRestContentType.json);
  
  }

  /**
   * test put folder update xml
   */
  public void testPutFolderUpdateXml() {
    
    helperTestPutFolderUpdate(AsacRestContentType.xml);
  
  }
    
  
}
