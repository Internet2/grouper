/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.xmpp.EsbEvent;
import edu.internet2.middleware.grouperClientExt.xmpp.EsbEvents;
import edu.internet2.middleware.grouperClientExt.xmpp.GcDecodeEsbEvents;


/**
 * you can run this in the loader instead of through messaging
 */
public class DigitalMarketplaceEsbPublisher extends EsbListenerBase {

  /**
   * 
   */
  public DigitalMarketplaceEsbPublisher() {
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#dispatchEvent(java.lang.String, java.lang.String)
   */
  @Override
  public boolean dispatchEvent(String jsonString, String consumerName) {
    
    GrouperDigitalMarketplaceMessageConsumer.incrementalRefreshInProgress = true;

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    long startTimeNanos = System.nanoTime();

    debugMap.put("method", "DigitalMarketplaceEsbPublisher.dispatchEvent");

    try {
      GrouperDigitalMarketplaceFullRefresh.waitForFullRefreshToEnd();

      EsbEvents esbEvents = GcDecodeEsbEvents.decodeEsbEvents(jsonString);
      esbEvents = GcDecodeEsbEvents.unencryptEsbEvents(esbEvents);

      //  {  
      //  "encrypted":false,
      //  "esbEvent":[  
      //     {  
      //        "changeOccurred":false,
      //        "createdOnMicros":1476889916578000,
      //        "eventType":"MEMBERSHIP_DELETE",
      //        "fieldName":"members",
      //        "groupId":"89dd656be8c743e79b2ef24fde6dab36",
      //        "groupName":"box:groups:someGroup",
      //        "id":"c2641b287f964bb28b2f0ddcd05f9fd3",
      //        "membershipType":"flattened",
      //        "sequenceNumber":"618",
      //        "sourceId":"g:isa",
      //        "subjectId":"GrouperSystem"
      //     }
      //  ]
      //}
      
      //not sure why there would be no events in there
      for (EsbEvent esbEvent : GrouperClientUtils.nonNull(esbEvents.getEsbEvent(), EsbEvent.class)) {

        GrouperDigitalMarketplaceMessageConsumer.processMessage(esbEvent);

      }
      
      return true;
    } finally {

      GrouperDigitalMarketplaceMessageConsumer.incrementalRefreshInProgress = false;

      GrouperDigitalMarketplaceLog.marketplaceLog(debugMap, startTimeNanos);
    }

    
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#disconnect()
   */
  @Override
  public void disconnect() {
  }


}
