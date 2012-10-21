package edu.internet2.middleware.grouper.ws.poc;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsMembershipLookup;
import edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignmentsRequest;

/**
 * 
 * @author mchyzer
 *
 */
public class JsonPoc {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    WsRestGetAttributeAssignmentsRequest wsRestGetAttributeAssignmentsRequest = new WsRestGetAttributeAssignmentsRequest();
    wsRestGetAttributeAssignmentsRequest.setWsOwnerMembershipLookups(new WsMembershipLookup[] {
        new WsMembershipLookup("uuid1"), new WsMembershipLookup("uuid2")
    });
    
    System.out.println(GrouperUtil.indent(GrouperUtil.jsonConvertTo(wsRestGetAttributeAssignmentsRequest), false));
    
  }

}
