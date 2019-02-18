package edu.internet2.middleware.grouper.app.usdu;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

public class UsduAttributeNames {
  
  /**
   * main attribute definition assigned to members
   */
  public static final String SUBJECT_RESOLUTION_DEF = "subjectResolutionDef";
  
  
  /**
   * main attribute name assigned to subjectResolutionDef
   */
  public static final String SUBJECT_RESOLUTION_NAME = "subjectResolutionMarker";
  
  
  /**
   * attribute definition for name value pairs assigned to assignment on members
   */
  public static final String SUBJECT_RESOLUTION_VALUE_DEF = "subjectResolutionValueDef";

  /**
   * false if this subject is currently unresolvable (as of last check). If the subject is resolvable,
   *  remove subjectResolutionMarker and metadata
   */
  public static final String SUBJECT_RESOLUTION_RESOLVABLE = "subjectResolutionResolvable";
  
  /**
   * date subject was last resolved
   */
  public static final String SUBJECT_RESOLUTION_DATE_LAST_RESOLVED = "subjectResolutionDateLastResolved";
  
  /**
   * the number of days from current date minus dateLastResolved.
   */
  public static final String SUBJECT_RESOLUTION_DAYS_UNRESOLVED = "subjectResolutionDaysUnresolved";

  /**
   * yyyy/mm/dd the date this subject was last checked. When the USDU runs, if this subject is current unresolvable, then set to currentDate
   */
  public static final String SUBJECT_RESOLUTION_LAST_CHECKED = "subjectResolutionLastChecked";
  
  /**
   * set to true when member is removed from all the groups/stems/etc
   */
  public static final String SUBJECT_RESOLUTION_DELETED = "subjectResolutionDeleted";
  
  /**
   * yyyy/mm/dd the date this subject was removed from all the groups/stems/etc
   */
  public static final String SUBJECT_RESOLUTION_DELETE_DATE = "subjectResolutionDeleteDate";
  
  /**
   * marker attribute def assigned to member
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameBase() {
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return AttributeDefNameFinder.findByName(UsduSettings.usduStemName()+":"+SUBJECT_RESOLUTION_NAME, false, new QueryOptions().secondLevelCache(false));
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant subjectResolutionMarker attribute def name be found?");
    }
    
    return attributeDefName;
  }
  

}
