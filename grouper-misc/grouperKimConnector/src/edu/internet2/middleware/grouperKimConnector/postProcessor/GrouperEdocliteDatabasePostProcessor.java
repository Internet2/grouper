/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.postProcessor;

import javax.xml.xpath.XPath;

import org.kuali.rice.kew.edl.EDLDatabasePostProcessor;
import org.kuali.rice.kew.postprocessor.ActionTakenEvent;
import org.kuali.rice.kew.postprocessor.DocumentRouteStatusChange;
import org.kuali.rice.kew.postprocessor.ProcessDocReport;
import org.kuali.rice.kew.rule.xmlrouting.XPathHelper;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;


/**
 * provision groups and store to a database
 */
public class GrouperEdocliteDatabasePostProcessor extends EDLDatabasePostProcessor {

  /**
   * 
   */
  static final Log LOG = GrouperClientUtils.retrieveLog(GrouperEdocliteDatabasePostProcessor.class);

  /**
   * @see org.kuali.rice.kew.edl.EDLDatabasePostProcessor#doActionTaken(org.kuali.rice.kew.postprocessor.ActionTakenEvent)
   */
  @Override
  public ProcessDocReport doActionTaken(ActionTakenEvent actionTakenEvent) throws Exception {
    ProcessDocReport processDocReport = super.doActionTaken(actionTakenEvent);
    
    GrouperEdoclitePostProcessor.syncOnBehalfOf(actionTakenEvent);
    
    return processDocReport;
  }

  /**
   * when the document goes to final, provision the group
   * @see org.kuali.rice.kew.edl.EDocLitePostProcessor#doRouteStatusChange(org.kuali.rice.kew.postprocessor.DocumentRouteStatusChange)
   */
  @Override
  public ProcessDocReport doRouteStatusChange(DocumentRouteStatusChange event)
      throws Exception {
    ProcessDocReport processDocReport =  super.doRouteStatusChange(event);
    
    GrouperEdoclitePostProcessor.doRouteStatusChangeHelper(event, processDocReport);
    
    return processDocReport;
  }


}
