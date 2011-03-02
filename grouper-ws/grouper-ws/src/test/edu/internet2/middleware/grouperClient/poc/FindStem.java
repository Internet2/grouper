/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.poc;

import edu.internet2.middleware.grouperClient.api.GcFindStems;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindStemsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsStem;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemQueryFilter;


/**
 *
 */
public class FindStem {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    GcFindStems gcFindStems = new GcFindStems();        
    
    WsStemQueryFilter wsStemQueryFilter = new WsStemQueryFilter();
    wsStemQueryFilter.setStemName("penn");
    wsStemQueryFilter.setStemQueryFilterType("FIND_BY_STEM_NAME_APPROXIMATE");
    
    gcFindStems.assignStemQueryFilter(wsStemQueryFilter);
    
    WsFindStemsResults wsFindStemsResults = gcFindStems.execute();
    
    WsResultMeta resultMetadata = wsFindStemsResults.getResultMetadata();
    
    if (!"T".equals(resultMetadata.getSuccess())) {
      throw new RuntimeException("Error finding stems: " + resultMetadata.getSuccess() 
          + ", " + resultMetadata.getResultCode() 
          + ", " + resultMetadata.getResultMessage());
    }
    
    WsStem[] wsStems = wsFindStemsResults.getStemResults();
    
    if (wsStems != null) {
      for (WsStem wsStem : wsStems) {
        System.out.println(wsStem.getName());
      }
    }
  }
}
