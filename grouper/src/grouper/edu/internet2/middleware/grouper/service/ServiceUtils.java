/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.service;

import java.util.Set;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;


/**
 * utility methods for services
 */
public class ServiceUtils {

  /**
   * @param idOfAttributeDef
   * @param queryOptions 
   * @return the stems
   */
  public static Set<Stem> retrieveStemsForService(String idOfAttributeDef, QueryOptions queryOptions) {
    return new StemFinder().assignIdOfAttributeDefName(idOfAttributeDef).assignAttributeCheckReadOnAttributeDef(false)
        .assignQueryOptions(queryOptions).findStems();
  }

}
