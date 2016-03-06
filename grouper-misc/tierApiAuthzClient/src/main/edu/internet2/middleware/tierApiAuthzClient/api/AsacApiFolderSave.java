/*
 * @author mchyzer
 * $Id: AsacApiDefaultResource.java,v 1.7 2009-12-13 06:33:06 mchyzer Exp $
 */
package edu.internet2.middleware.tierApiAuthzClient.api;

import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacFolder;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacFolderLookup;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacFolderSaveRequest;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacFolderSaveResponse;
import edu.internet2.middleware.tierApiAuthzClient.corebeans.AsacSaveMode;
import edu.internet2.middleware.tierApiAuthzClient.exceptions.StandardApiClientWsException;
import edu.internet2.middleware.tierApiAuthzClient.util.StandardApiClientUtils;
import edu.internet2.middleware.tierApiAuthzClient.ws.AsacRestHttpMethod;
import edu.internet2.middleware.tierApiAuthzClient.ws.StandardApiClientWs;



/**
 * class to run a folder save
 */
public class AsacApiFolderSave extends AsacApiRequestBase {

  
  
  /** true or false (null if false) */
  private Boolean createParentFoldersIfNotExist;
  
  /**
   * assign if parent folders should be created if they dont exist
   * @param theCreateParentFoldersIfNotExist
   * @return this for chaining
   */
  public AsacApiFolderSave assignCreateParentFoldersIfNotExist(Boolean theCreateParentFoldersIfNotExist) {
    this.createParentFoldersIfNotExist = theCreateParentFoldersIfNotExist;
    return this;
  }
  
  /**
   * the saved folder
   */
  private AsacFolder folder = null;
  
  /**
   * assign folder to save
   * @param asacFolder
   * @return this for chaining
   */
  public AsacApiFolderSave assignFolder(AsacFolder asacFolder) {
    this.folder = asacFolder;
    return this;
  }
  
  /**
   * lookup object (generally this is in the url)
   */
  private AsacFolderLookup folderLookup;
  
  /**
   * assign the folder lookup of the folder ot edit
   * @param folderLookup1
   * @return this for chaining
   */
  public AsacApiFolderSave assignFolderLookup(AsacFolderLookup folderLookup1) {
    this.folderLookup = folderLookup1;
    return this;
  }
  
  /** if the save should be constrained to INSERT, UPDATE, or INSERT_OR_UPDATE (default) */
  private AsacSaveMode saveMode;

  /**
   * assign save mode if insert, update, or insert_or_update
   * @param asacSaveMode
   * @return this for chaining
   */
  public AsacApiFolderSave assignSaveMode(AsacSaveMode asacSaveMode) {
    this.saveMode = asacSaveMode;
    return this;
  }
  
  /**
   * @see edu.internet2.middleware.tierApiAuthzClient.api.AsacApiRequestBase#assignIndent(boolean)
   */
  @Override
  public AsacApiFolderSave assignIndent(boolean indent1) {
    return (AsacApiFolderSave)super.assignIndent(indent1);
  }

  /**
   * validate this call
   */
  private void validate() {
    if (this.folder == null && this.folderLookup == null) {
      throw new StandardApiClientWsException("Need to pass in a lookup or a folder");
    }
    
    if (this.folderLookup != null && (StandardApiClientUtils.isBlank(this.folderLookup.getHandleName())
        != StandardApiClientUtils.isBlank(this.folderLookup.getHandleValue()))) {
      throw new StandardApiClientWsException("If you specify either handleName or " +
      		"handleValue then you need to pass both: " + this.folderLookup.getHandleName()
      		+ ", " + this.folderLookup.getHandleValue());
    }
    
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public AsacFolderSaveResponse execute() {
    this.validate();
    AsacFolderSaveResponse asacFolderSaveResponse = null;
      
    StandardApiClientWs<AsacFolderSaveResponse> standardApiClientWs = new StandardApiClientWs<AsacFolderSaveResponse>();

    //kick off the web service
    StringBuilder urlSuffix = new StringBuilder();
    
    urlSuffix.append("/" + StandardApiClientUtils.version() + "/folders/");
    
    if (this.folderLookup != null && !StandardApiClientUtils.isBlank(this.folderLookup.getId())) {
      urlSuffix.append(StandardApiClientUtils.escapeUrlEncode(
          "id:" + this.folderLookup.getId()) + "." + this.getContentType().name());
    } else if (this.folderLookup != null && !StandardApiClientUtils.isBlank(this.folderLookup.getName())) {
      urlSuffix.append(StandardApiClientUtils.escapeUrlEncode(
          "name:" + this.folderLookup.getName()) + "." + this.getContentType().name());
    } else if (this.folderLookup != null && !StandardApiClientUtils.isBlank(this.folderLookup.getHandleName())) {
      urlSuffix.append(StandardApiClientUtils.escapeUrlEncode(this.folderLookup.getHandleName())
          + StandardApiClientUtils.escapeUrlEncode(":" +this.folderLookup.getHandleValue()) + "." + this.getContentType().name());
    } else if (this.folder != null && !StandardApiClientUtils.isBlank(this.folder.getId())) {
      urlSuffix.append(StandardApiClientUtils.escapeUrlEncode(
          "id:" + this.folder.getId()) + "." + this.getContentType().name());
    } else if (this.folder != null && !StandardApiClientUtils.isBlank(this.folder.getName())) {
      urlSuffix.append(StandardApiClientUtils.escapeUrlEncode(
          "name:" + this.folder.getName()) + "." + this.getContentType().name());
    }

    AsacRestHttpMethod asacRestHttpMethod = null;
    if (this.saveMode != null) {
      switch(this.saveMode) {
        case INSERT:
          asacRestHttpMethod = AsacRestHttpMethod.POST;
          break;
        case UPDATE:
          asacRestHttpMethod = AsacRestHttpMethod.PUT;
          StandardApiClientUtils.urlEscapeAndAppend(urlSuffix, "saveMode", "update");
          break;
        case INSERT_OR_UPDATE:
          asacRestHttpMethod = AsacRestHttpMethod.PUT;
          break;
      }
    } else {
      asacRestHttpMethod = AsacRestHttpMethod.PUT;
    }

    AsacFolderSaveRequest asacFolderSaveRequest = new AsacFolderSaveRequest();
    boolean sendRequest = false;
    
    if (this.createParentFoldersIfNotExist != null) {
      StandardApiClientUtils.urlEscapeAndAppend(urlSuffix, "createParentFoldersIfNotExist", "true");
      //asacFolderSaveRequest.setCreateParentFoldersIfNotExist(this.createParentFoldersIfNotExist);
      //sendRequest = true;
    }
    
    //if (this.saveMode != null) {
    //  asacFolderSaveRequest.setSaveMode(this.saveMode.name());
    //  sendRequest = true;
    //}
    
    if (this.folder != null) {
      asacFolderSaveRequest.setFolder(this.folder);
      sendRequest = true;
    }
    
    //if not send, then it is null
    if (!sendRequest) {
      asacFolderSaveRequest = null;
    }
    
    asacFolderSaveResponse =
      standardApiClientWs.executeService(urlSuffix.toString(), asacFolderSaveRequest, "folderSave", null,
          this.getContentType(), AsacFolderSaveResponse.class, asacRestHttpMethod);
    
    return asacFolderSaveResponse;
    
  }
  
}
