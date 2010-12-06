package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;

import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * @author shilen
 * $Id$
 */
public class PITUtils {

  /**
   * Delete point in time records that ended before the given date.
   * @param date
   */
  public static void deleteInactiveRecords(Date date) {
    Timestamp time = new Timestamp(date.getTime());
    GrouperDAOFactory.getFactory().getPITAttributeAssignValue().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITRoleSet().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITAttributeAssignActionSet().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITAttributeAssign().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITAttributeAssignAction().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITAttributeDefNameSet().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITAttributeDefName().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITMembership().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITGroupSet().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITGroup().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITStem().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITAttributeDef().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITField().deleteInactiveRecords(time);
    GrouperDAOFactory.getFactory().getPITMember().deleteInactiveRecords(time);
  }
}
