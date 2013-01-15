/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces;

import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.folders.AsasApiFolderDeleteParam;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.folders.AsasApiFolderDeleteResult;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.folders.AsasApiFolderSaveParam;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.folders.AsasApiFolderSaveResult;
import edu.internet2.middleware.authzStandardApiServer.interfaces.entity.AsasApiEntityLookup;


/**
 * implement this interface to provide logic for the authz standard api server folder operations
 * @author mchyzer
 *
 */
public interface AsasApiFolderInterface {

  /**
   * save a folder, e.g. a POST (insert) or PUT (update) on /folders/name:some:name
   * @param asasApiFolderSaveParam
   * @return the result
   */
  public AsasApiFolderSaveResult save(AsasApiEntityLookup authenticatedSubject, 
      AsasApiFolderSaveParam asasApiFolderSaveParam);

  /**
   * delete a folder, e.g. a DELETE on /folders/name:some:name
   * @param asasApiFolderDeleteParam
   * @return the result
   */
  public AsasApiFolderDeleteResult delete(AsasApiEntityLookup authenticatedSubject, 
      AsasApiFolderDeleteParam asasApiFolderDeleteParam);
  
}
