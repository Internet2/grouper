/*
 * @author mchyzer
 * $Id: AsacApiDefaultResource.java,v 1.7 2009-12-13 06:33:06 mchyzer Exp $
 */
package edu.internet2.middleware.authzStandardApiClient.api;

import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacFolderLookup;
import edu.internet2.middleware.authzStandardApiClient.corebeans.AsacFolderSaveResponse;
import edu.internet2.middleware.authzStandardApiClient.exceptions.StandardApiClientWsException;
import edu.internet2.middleware.authzStandardApiClient.util.StandardApiClientUtils;
import edu.internet2.middleware.authzStandardApiClient.ws.AsacRestHttpMethod;
import edu.internet2.middleware.authzStandardApiClient.ws.StandardApiClientWs;



/**
 * class to run a folder delete
 */
public class AsacApiFolderDelete extends AsacApiRequestBase {
  
  /** true or false (null if false), if all objects inside should be deleted */
  private Boolean recursive;
  
  /**
   * true or false (null if false), if all objects inside should be deleted
   * @param theRecursive
   * @return this for chaining
   */
  public AsacApiFolderDelete assignRecursive(Boolean theRecursive) {
    this.recursive = theRecursive;
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
  public AsacApiFolderDelete assignFolderLookup(AsacFolderLookup folderLookup1) {
    this.folderLookup = folderLookup1;
    return this;
  }
  
  /**
   * @see edu.internet2.middleware.authzStandardApiClient.api.AsacApiRequestBase#assignIndent(boolean)
   */
  @Override
  public AsacApiFolderDelete assignIndent(boolean indent1) {
    return (AsacApiFolderDelete)super.assignIndent(indent1);
  }

  /**
   * validate this call
   */
  private void validate() {
    if (this.folderLookup == null) {
      throw new StandardApiClientWsException("Need to pass in a lookup to a folder");
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
    AsacFolderSaveResponse asacFolderDeleteResponse = null;

    StandardApiClientWs<AsacFolderSaveResponse> standardApiClientWs = new StandardApiClientWs<AsacFolderSaveResponse>();

    //kick off the web service
    StringBuilder urlSuffix = new StringBuilder();

    if (this.folderLookup != null && !StandardApiClientUtils.isBlank(this.folderLookup.getId())) {
      urlSuffix.append("/" + StandardApiClientUtils.version() + "/folders/id:" 
            + StandardApiClientUtils.escapeUrlEncode(this.folderLookup.getId()) + "." + this.getContentType().name());
    } else if (this.folderLookup != null && !StandardApiClientUtils.isBlank(this.folderLookup.getName())) {
      urlSuffix.append("/" + StandardApiClientUtils.version() + "/folders/name:"
            + StandardApiClientUtils.escapeUrlEncode(this.folderLookup.getName()) + "." + this.getContentType().name());
    } else if (this.folderLookup != null && !StandardApiClientUtils.isBlank(this.folderLookup.getHandleName())) {
      urlSuffix.append("/" + StandardApiClientUtils.version() + "/folders/"
            + StandardApiClientUtils.escapeUrlEncode(this.folderLookup.getHandleName())
            + ":" + StandardApiClientUtils.escapeUrlEncode(this.folderLookup.getHandleValue()) + "." + this.getContentType().name());
    }

    AsacRestHttpMethod asacRestHttpMethod = AsacRestHttpMethod.DELETE;

    if (this.recursive != null && this.recursive) {
      if (urlSuffix.indexOf("?") < 0) {
        urlSuffix.append("?");
      }
      urlSuffix.append("recursive=true");
    }
    
    asacFolderDeleteResponse =
      standardApiClientWs.executeService(urlSuffix.toString(), null, "folderDelete", null,
          this.getContentType(), AsacFolderSaveResponse.class, asacRestHttpMethod);
    
    return asacFolderDeleteResponse;
    
  }
  
}
