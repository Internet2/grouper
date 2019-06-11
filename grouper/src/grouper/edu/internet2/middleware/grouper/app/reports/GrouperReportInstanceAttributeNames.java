package edu.internet2.middleware.grouper.app.reports;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

/**
 * 
 */
public class GrouperReportInstanceAttributeNames {
  
  /**
   * main attribute definition assigned to groups, folders
   */
  public static final String GROUPER_REPORT_INSTANCE_DEF = "reportInstanceDef";
  
  /**
   * main attribute name assigned to reportInstanceDef
   */
  public static final String GROUPER_REPORT_INSTANCE_ATTRIBUTE_NAME = "reportInstanceMarker";
  
  /**
   * attribute definition for name value pairs assigned to assignment on groups or folders
   */
  public static final String GROUPER_REPORT_INSTANCE_VALUE_DEF = "reportInstanceValueDef";

  
  /**
   * SUCCESS means link to the report from screen, ERROR means didnt execute successfully
   */
  public static final String GROUPER_REPORT_INSTANCE_STATUS = "reportInstanceStatus";
  
  /**
   * number of millis it took to generate this report
   */
  public static final String GROUPER_REPORT_INSTANCE_MILLIS_ELAPSED = "reportElapsedMillis";
  
  /**
   * Attribute assign ID of the marker attribute of the config (same owner as this attribute, but there could be many reports configured on one owner)
   */
  public static final String GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID = "reportInstanceConfigMarkerAssignmentId";
  
  /**
   * millis since 1970 that this report was run. This must match the timestamp in the report name and storage
   */
  public static final String GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970 = "reportInstanceMillisSince1970";
  
  /**
   * number of bytes of the unencrypted report
   */
  public static final String GROUPER_REPORT_INSTANCE_SIZE_BYTES = "reportInstanceSizeBytes";
  
  /**
   * filename of report
   */
  public static final String GROUPER_REPORT_INSTANCE_FILE_NAME = "reportInstanceFilename";
  
  /**
   * depending on storage type, this is a pointer to the report in storage, e.g. the S3 address.
   *  note the S3 address is .csv suffix, but change to __metadata.json for instance metadata
   */
  public static final String GROUPER_REPORT_INSTANCE_FILE_POINTER = "reportInstanceFilePointer";
  
  /**
   * number of times this report was downloaded (note update this in try/catch and a for loop so concurrency doesnt cause problems)
   */
  public static final String GROUPER_REPORT_INSTANCE_DOWNLOAD_COUNT = "reportInstanceDownloadCount";
  
  /**
   * randomly generated 16 char alphanumeric encryption key (never allow display or edit of this)
   */
  public static final String GROUPER_REPORT_INSTANCE_ENCRYPTION_KEY = "reportInstanceEncryptionKey";
  
  /**
   * number of rows returned in report
   */
  public static final String GROUPER_REPORT_INSTANCE_ROWS = "reportInstanceRows";
  
  /**
   * source::::subjectId1, source2::::subjectId2 list for subjects who were were emailed successfully (cant be more than 4k chars)
   */
  public static final String GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS = "reportInstanceEmailToSubjects";
  
  /**
   * source::::subjectId1, source2::::subjectId2 list for subjects who were were NOT emailed successfully, dont include g:gsa groups (cant be more than 4k chars)
   */
  public static final String GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS_ERROR = "reportInstanceEmailToSubjectsError";
  
  /**
   * marker attribute def assigned to stem or group
   * @return the attribute def name
   */
  public static AttributeDefName retrieveAttributeDefNameBase() {
    
    AttributeDefName attributeDefName = (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        return AttributeDefNameFinder.findByName(GrouperReportSettings.reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_ATTRIBUTE_NAME, false, new QueryOptions().secondLevelCache(false));
        
      }
      
    });
  
    if (attributeDefName == null) {
      throw new RuntimeException("Why cant reportInstanceMarker attribute def name be found?");
    }
    
    return attributeDefName;
  }
  
  
    /**
     * attribute value def assigned to stem or group
     * @return the attribute def name
     */
    public static AttributeDef retrieveAttributeDefBaseDef() {
      
      AttributeDef attributeDef = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        @Override
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          
          return AttributeDefFinder.findByName(GrouperReportSettings.reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_DEF, false, new QueryOptions().secondLevelCache(false));
          
        }
        
      });
    
      if (attributeDef == null) {
        throw new RuntimeException("Why cant reportInstanceDef attribute def be found?");
      }
      
      return attributeDef;
    }
}
