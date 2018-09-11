package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperRemedy.GrouperRemedyLog;
import edu.internet2.middleware.grouperRemedy.GrouperRemedyUser;


/**
 * commands against the box api
 */
public class GrouperDigitalMarketplaceCommands {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

  }

//  /**
//   * @return remedy login id to user never null
//   */
//  public static Map<String, GrouperDigitalMarketplaceUser> retrieveDigitalMarketplaceUsers() {
//    
//    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
//
//    debugMap.put("method", "retrieveRemedyUsers");
//
//    long startTime = System.nanoTime();
//
//    try {
//
//      Map<String, String> paramMap = new HashMap<String, String>();
//
//      paramMap.put("fields", "values(Person%20ID,Remedy%20Login%20ID,Profile%20Status)");
//      
//      JSONObject jsonObject = executeGetMethod(debugMap, "/api/arsys/v1/entry/CTM:People", paramMap);
//      
//      Map<String, GrouperRemedyUser> results = convertRemedyUsersFromJson(jsonObject);
//      
//      debugMap.put("size", GrouperClientUtils.length(results));
//
//      return results;
//    } catch (RuntimeException re) {
//      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
//      throw re;
//    } finally {
//      GrouperRemedyLog.remedyLog(debugMap, startTime);
//    }
//
//  }

}
