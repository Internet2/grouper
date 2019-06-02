/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.loadtest;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.ws.StemScope;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsStemLookup;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;


/**
 * load test the WS
 */
public class GrouperClientWsLoadTest {

  /**
   * 
   */
  public GrouperClientWsLoadTest() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    
  }

  public void loadTestToStdOut() {
    
  }

  /**
   * 
   * @return the results
   */
  public GrouperClientLoadTestResults loadTest() {
    
    GrouperClientLoadTestResults grouperClientLoadTestResults = new GrouperClientLoadTestResults();
    
    loadTestSelect(grouperClientLoadTestResults);

    return grouperClientLoadTestResults;
  }

  /**
   * 
   * @param grouperClientLoadTestResults
   */
  public void loadTestSelect(GrouperClientLoadTestResults grouperClientLoadTestResults) {

    String groupSelectFolder = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.loadtest.groupSelectFolder"); 
    if (StringUtils.isBlank(groupSelectFolder)) {
      return;
    }
    WsGetGroupsResults wsGetGroupsResults = new GcGetGroups().assignWsStemLookup(new WsStemLookup())
      .assignStemScope(StemScope.ALL_IN_SUBTREE).assignPageNumber(1).assignPageSize(1000)
      .assignSortString("name").execute();
    Map<String, String> groupUuidToNames = new LinkedHashMap<String, String>();
    for (WsGroup wsGroup : wsGetGroupsResults.getResults()[0].getWsGroups()) {
      groupUuidToNames.put(wsGroup.getUuid(), wsGroup.getName());
    }

  }

}
