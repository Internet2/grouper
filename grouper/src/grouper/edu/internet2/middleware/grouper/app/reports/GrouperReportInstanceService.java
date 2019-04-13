package edu.internet2.middleware.grouper.app.reports;

import static edu.internet2.middleware.grouper.app.reports.GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID;
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

import java.util.HashSet;
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

public class GrouperReportInstanceService {
  
  public static GrouperReportInstance getReportInstance(String attributeAssignId) {
    
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    return buildReportInstance(attributeAssign);
  }
  
  public static Set<GrouperReportInstance> getReportInstances(GrouperObject grouperObject, String configMarkerAssignmentId) {
    
    Set<GrouperReportInstance> reportInstances = new HashSet<GrouperReportInstance>();
    
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
  
  public static GrouperReportInstance getMostRecentReportInstance(GrouperObject grouperObject, String configMarkerAssignmentId) {
   
    Set<GrouperReportInstance> reportInstances = getReportInstances(grouperObject, configMarkerAssignmentId);
    
    long mostRecent = -1;
    GrouperReportInstance mostRecentReportInstance = null;
    
    for (GrouperReportInstance reportInstance: reportInstances) {
      if (reportInstance.getReportInstanceMillisSince1970() > mostRecent) {
        mostRecentReportInstance = reportInstance;
        mostRecent = reportInstance.getReportInstanceMillisSince1970();
      }
    }
    
   return mostRecentReportInstance;
    
  }
  
  public static void saveReportInstanceAttributes(GrouperReportInstance reportInstance, GrouperObject grouperObject) {
    
//    Set<AttributeAssign> attributeAssigns = getAttributeAssigns(grouperObject);
//    
//    AttributeAssign attributeAssign = findAttributeAssignForReportConfigName(attributeAssigns, reportConfigBean.getReportConfigName());
    
    AttributeAssign attributeAssign = null;
    if (attributeAssign == null) {
      if (grouperObject instanceof Group) {
        attributeAssign = ((Group)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      } else {
        attributeAssign = ((Stem)grouperObject).getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
      }
    }
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_STATUS, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceStatus());
    
    if (reportInstance.getReportInstanceStatus().equals(GrouperReportInstance.STATUS_SUCCESS)) {

      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceConfigMarkerAssignmentId());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceEmailToSubjects());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_EMAIL_TO_SUBJECTS_ERROR, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceEmailToSubjectsError());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_ENCRYPTION_KEY, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceEncryptionKey());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_FILE_NAME, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportFileUnencrypted().getName());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_FILE_POINTER, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), reportInstance.getReportInstanceFilePointer());
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_MILLIS_ELAPSED, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportElapsedMillis()));
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_MILLIS_SINCE_1970, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportInstanceMillisSince1970()));
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_ROWS, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportInstanceRows()));
      
      attributeDefName = AttributeDefNameFinder.findByName(reportConfigStemName()+":"+GROUPER_REPORT_INSTANCE_SIZE_BYTES, true);
      attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), String.valueOf(reportInstance.getReportInstanceSizeBytes()));
      
    }
    attributeAssign.saveOrUpdate();
    
  }
  
  
  public static void deleteReportInstance(GrouperReportInstance reportInstance) {
    String attributeAssignId = reportInstance.getAttributeAssignId();
    AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, true);
    attributeAssign.delete();
  }
  
  private static Set<AttributeAssign> getAttributeAssigns(GrouperObject grouperObject) {
    
    if (grouperObject instanceof Group) {
      Group group = (Group)grouperObject;
      return group.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    }
    
    Stem stem = (Stem)grouperObject;
    return stem.getAttributeDelegate().retrieveAssignments(retrieveAttributeDefNameBase());
    
  }
  
  private static GrouperReportInstance buildReportInstance(AttributeAssign attributeAssign) {
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    GrouperReportInstance result = new GrouperReportInstance();
    
    result.setAttributeAssignId(attributeAssign.getId());
    
    AttributeAssignValue assignValue = attributeValueDelegate.retrieveAttributeAssignValue(reportConfigStemName()+":"+GrouperReportInstanceAttributeNames.GROUPER_REPORT_INSTANCE_CONFIG_MARKER_ASSIGNMENT_ID);
    result.setReportInstanceConfigMarkerAssignmentId(assignValue != null ? assignValue.getValueString(): null);
    
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
