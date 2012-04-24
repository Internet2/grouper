/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
