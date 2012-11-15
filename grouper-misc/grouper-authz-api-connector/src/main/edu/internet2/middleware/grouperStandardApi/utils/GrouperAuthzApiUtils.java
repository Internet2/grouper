package edu.internet2.middleware.grouperStandardApi.utils;

import edu.internet2.middleware.authzStandardApiServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.authzStandardApiServer.util.ExpirableCache;
import edu.internet2.middleware.grouperStandardApi.config.GrouperAuthzApiServerConfig;
import edu.internet2.middleware.subject.Subject;


public class GrouperAuthzApiUtils {

  /**
   * successes cache
   */
  private static ExpirableCache<String, Boolean> successesCache = null;
  
  /**
   * 
   * @return
   */
  private static ExpirableCache<String, Boolean> successesCache() {
    
  }
  
  {
    int successCacheMinutes = GrouperAuthzApiServerConfig.retrieveConfig().propertyValueInt(
        "grouperAuthzApiServer.loggedInSubject.cacheSubjectSuccessesForMinutes", -1);
    if (successCacheMinutes > 0) {
      successesCache = new ExpirableCache<String, Boolean>(successCacheMinutes);
    }
  }    

  /**
   * 
   * @param authenticatedSubject
   * @return the subject or throw an exception if not found
   */
  public static Subject loggedInSubject(AsasApiEntityLookup authenticatedSubject) {
    if (authenticatedSubject == null) {
      throw new NullPointerException();
    }
    
    
    
    
  }
  
}
