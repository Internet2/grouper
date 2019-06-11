package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_DOWNLOAD_COUNT;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS_ERROR;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_ENCRYPTION_KEY;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_NAME;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_POINTER;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_MILLIS_ELAPSED;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_ROWS;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_SIZE_BYTES;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_STATUS;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.reports.GrouperReportSettings.reportConfigStemName;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.attr.value.AttributeValueDelegate;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

/**
 * @author vsachdeva
 */
public class GrouperReportInstanceService {
  
  /**
   * get report instance by attribute assign id
   * @param attributeAssignId
   * @return
   */
  public static GrouperReportInstance getReportInstance(String attributeAssignId) {
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    return buildReportInstance(attributeAssign);
  }
  
  /**
   * get all report instances for a given grouper object and report config id 
   * @param grouperObject
   * @param configMarkerAssignmentId
   * @return
   */
  public static List<GrouperReportInstance> getReportInstances(GrouperObject grouperObject, String configMarkerAssignmentId) {
    
    List<GrouperReportInstance> reportInstances = new ArrayList<GrouperReportInstance>();
    
    Set<AttributeAssign> attributeAssigns = getAttributeAssigns(grouperObject);
    
    for (AttributeAssign attributeAssign: attributeAssigns) {
      AttributeAssignValue attributeAssignValue = attributeAssign.getAttributeValueDelegate().retrieveAttributeAssignValue(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID);
      
      if (attributeAssignValue != null && StringUtils.isNotBlank(attributeAssignValue.getValueString()) 
          && attributeAssignValue.getValueString().equals(configMarkerAssignmentId)) {
        GrouperReportInstance instance = buildReportInstance(attributeAssign);
        reportInstances.add(instance);
      }
    }
    
    return reportInstances;
    
  }
  
  /**
   * get most recent report instance for a given grouper object and report config id
   * @param grouperObject
   * @param configMarkerAssignmentId
   * @return
   */
  public static GrouperReportInstance getMostRecentReportInstance(GrouperObject grouperObject, String configMarkerAssignmentId) {
   
    List<GrouperReportInstance> reportInstances = getReportInstances(grouperObject, configMarkerAssignmentId);
    
    long mostRecent = -1L;
    GrouperReportInstance mostRecentReportInstance = null;
    
    for (GrouperReportInstance reportInstance: reportInstances) {
      if (reportInstance.getReportInstanceMillisSince1970().longValue() > mostRecent) {
        mostRecentReportInstance = reportInstance;
        mostRecent = reportInstance.getReportInstanceMillisSince1970();
      }
    }
    
   return mostRecentReportInstance;
    
  }
  
  /**
   * save report instance attributes
   * @param reportInstance
   * @param grouperObject
   */
  public static void saveReportInstanceAttributes(GrouperReportInstance reportInstance, GrouperObject grouperObject) {
    
    AttributeAssign attributeAssign = null;
    
    if (StringUtils.isNotBlank(reportInstance.getAttributeAssignId())) {
      attributeAssign = AttributeAssignFinder.findById(reportInstance.getAttributeAssignId(), true);
    } else {
      if (grouperObject instanceof Group) {
        attributeAssign = ((Group)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else {
        attributeAssign = ((Stem)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      }
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_STATUS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceStatus());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceConfigMarkerAssignmentId());
    
    attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportInstanceMillisSince1970()));
    
    if (reportInstance.getReportInstanceStatus().equals(GrouperReportInstance.STATUS_SUCCESS)) {

      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceEmailToSubjects());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS_ERROR, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceEmailToSubjectsError());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_ENCRYPTION_KEY, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceEncryptionKey());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_FILE_NAME, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceFileName());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_FILE_POINTER, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceFilePointer());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_MILLIS_ELAPSED, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportElapsedMillis()));
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_ROWS, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportInstanceRows()));
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_SIZE_BYTES, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportInstanceSizeBytes()));
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_DOWNLOAD_COUNT, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportInstanceDownloadCount()));
      
    }
    attributeAssign.saveOrUpdate();
    reportInstance.setAttributeAssignId(attributeAssign.getId());
    
  }
  
  /**
   * delete given report instances
   * @param instancesToBeDeleted
   */
  public static void deleteReportInstances(List<GrouperReportInstance> instancesToBeDeleted) {
    
    for (GrouperReportInstance instance: instancesToBeDeleted) {
      
      if (instance.getReportInstanceStatus().equals(GrouperReportInstance.STATUS_SUCCESS)) {
        if (instance.isReportStoredInS3()) {
          GrouperReportLogic.deleteFileFromS3(instance);
        } else {
          GrouperReportLogic.deleteFromFileSystem(instance);
        }
      }
      
      String attributeAssignId = instance.getAttributeAssignId();
      AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
      attributeAssign.delete();
    }
  }
  
  /**
   * get attribute assigns for a given grouper object
   * @param grouperObject
   * @return
   */
  private static Set<AttributeAssign> getAttributeAssigns(GrouperObject grouperObject) {
    
    if (grouperObject instanceof Group) {
      Group group = (Group)grouperObject;
      return group.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    }
    
    Stem stem = (Stem)grouperObject;
    return stem.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    
  }
  
  /**
   * build report instance from attribute assign values
   * @param attributeAssign
   * @return
   */
  private static GrouperReportInstance buildReportInstance(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperReportInstance result = new GrouperReportInstance();
    
    result.setAttributeAssignId(attributeAssign.getId());
    
    AttributeAssignValue assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID);
    result.setReportInstanceConfigMarkerAssignmentId(assignValue != null ? assignValue.getValueString(): null);
    if (assignValue != null) {
      GrouperReportConfigurationBean reportConfigBean = GrouperReportConfigService.getGrouperReportConfigBean(assignValue.getValueString());
      result.setGrouperReportConfigurationBean(reportConfigBean);
    }
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_DOWNLOAD_COUNT);
    result.setReportInstanceDownloadCount(assignValue != null ? Long.valueOf(assignValue.getValueString()): 0L);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS);
    result.setReportInstanceEmailToSubjects(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS_ERROR);
    result.setReportInstanceEmailToSubjectsError(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_NAME);
    result.setReportInstanceFileName(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_FILE_POINTER);
    result.setReportInstanceFilePointer(assignValue != null ? assignValue.getValueString(): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_MILLIS_ELAPSED);
    result.setReportElapsedMillis(assignValue != null ? Long.valueOf(assignValue.getValueString()): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970);
    result.setReportInstanceMillisSince1970(assignValue != null ? Long.valueOf(assignValue.getValueString()): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_ROWS);
    result.setReportInstanceRows(assignValue != null ? Long.valueOf(assignValue.getValueString()): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_SIZE_BYTES);
    result.setReportInstanceSizeBytes(assignValue != null ? Long.valueOf(assignValue.getValueString()): null);
    
    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_STATUS);
    result.setReportInstanceStatus(assignValue != null ? assignValue.getValueString(): null);

    assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_ENCRYPTION_KEY);
    result.setReportInstanceEncryptionKey(assignValue != null ? assignValue.getValueString(): null);
    
    return result;
    
  }

}
