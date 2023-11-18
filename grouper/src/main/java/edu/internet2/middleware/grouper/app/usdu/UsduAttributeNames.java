package edu.internet2.middleware.grouper.app.usdu;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
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
  
  /**
   * attribute def assigned to members
   * @return the attribute def
   */
  public static AttributeDef retrieveAttributeDefBaseDef() {
    
    AttributeDef attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return AttributeDefFinder.findByName(UsduSettings.usduStemName()+":"+SUBJECT_RESOLUTION_DEF, false, new QueryOptions().secondLevelCache(false));
      }
      
    });
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant subjectResolutionDef attribute def be found?");
    }
    
    return attributeDef;
  }
  
  /**
   * attribute def assigned to assignments on members
   * @return the attribute def
   */
  public static AttributeDef retrieveAttributeDefValueDef() {
    
    AttributeDef attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return AttributeDefFinder.findByName(UsduSettings.usduStemName()+":"+SUBJECT_RESOLUTION_VALUE_DEF, false, new QueryOptions().secondLevelCache(false));
      }
      
    });
  
    if (attributeDef == null) {
      throw new RuntimeException("Why cant subjectResolutionValueDef attribute def be found?");
    }
    
    return attributeDef;
  }
  

}
