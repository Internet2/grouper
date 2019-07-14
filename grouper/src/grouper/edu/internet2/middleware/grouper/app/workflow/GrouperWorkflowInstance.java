package edu.internet2.middleware.grouper.app.workflow;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.logging.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class GrouperWorkflowInstance {
  
  public static void main(String[] args) {
    System.out.println("hi");
    
    Document document = Jsoup.parse("Fill out this form to be added to this group.<br /><br />\n" + 
        "Several approvals will take place which usually take less than 2 business days<br /><br />\n" + 
        "State the reason you would like this access: <input type=\"text\" name=\"reason\" id=\"reasonId\" /><br /><br >\n" + 
        "<input type=\"checkbox\" name=\"agreeToTerms\" id=\"agreeToTermsId\" /> I agree this this institutions' <a href=\"https://whatever.whatever/whatever\">terms and conditions</a><br /><br />\n" + 
        "Notes: <textarea rows=\"4\" cols=\"50\" name=\"notes\" id=\"notesId\"></textarea><br /><br />\n" + 
        "Notes for approvers: <textarea rows=\"4\" cols=\"50\" name=\"notesForApprovers\" id=\"notesForApproversId\"></textarea><br /><br />");
    
//    Elements allElements = document.getAllElements();
//    for (Element e: allElements) {
//      System.out.println("element is "+e);
//    }
    
    Elements textAreas = document.getElementsByTag("textarea");
    for (Element e: textAreas) {
      System.out.println("one field is "+e.toString());
      System.out.println("name of field is "+e.attr("name"));
      System.out.println("type of field is "+e.attr("type"));
    }
    
    Elements elements = document.getElementsByAttributeValue("type", "textarea");
    for (Element e: elements) {
      System.out.println("text element is "+e.toString());
    }
    
    Element element1 = document.selectFirst("[name=notes]");
    element1.val("these are the notes");
    element1.attr("disabled", "disabled");
    
    Element element2 = document.selectFirst("[name=agreeToTerms]");
    element2.attr("disabled", "disabled");
    
    String changedHtml = document.html();
    System.out.println(changedHtml);
    
    
  }
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(GrouperWorkflowInstance.class);
  
  private String attributeAssignId;
  
  private String workflowInstanceState;
  
  private Long workflowInstanceLastUpdatedMillisSince1970;
  
  private String workflowInstanceConfigMarkerAssignmentId;
  
  private Long workflowInstanceInitiatedMillisSince1970;
  
  private String workflowInstanceUuid;
  
  private String workflowInstanceFileInfoString;
  
  private GrouperWorkflowInstanceFilesInfo grouperWorkflowInstanceFilesInfo;
  
  private String workflowInstanceLogEntriesString;
  
  private GrouperWorkflowInstanceLogEntries grouperWorkflowInstanceLogEntries;
  
  private String workflowInstanceEncryptionKey;
  
  private String workflowInstanceLastEmailedDate;
  
  private String workflowInstanceLastEmailedState;
    
  private String workflowInstanceError;
  
  private String workflowInstanceParamValue0String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue0;
  
  private String workflowInstanceParamValue1String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue1;
  
  private String workflowInstanceParamValue2String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue2;
  
  private String workflowInstanceParamValue3String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue3;
  
  private String workflowInstanceParamValue4String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue4;
  
  private String workflowInstanceParamValue5String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue5;
  
  private String workflowInstanceParamValue6String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue6;
  
  private String workflowInstanceParamValue7String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue7;
  
  private String workflowInstanceParamValue8String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue8;
  
  private String workflowInstanceParamValue9String;
  
  private GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue9;
  
  private GrouperWorkflowConfig grouperWorkflowConfig;
  
  private GrouperObject ownerGrouperObject;
  

  public GrouperWorkflowConfig getGrouperWorkflowConfig() {
    return grouperWorkflowConfig;
  }


  
  public void setGrouperWorkflowConfig(GrouperWorkflowConfig grouperWorkflowConfig) {
    this.grouperWorkflowConfig = grouperWorkflowConfig;
  }


  public String getAttributeAssignId() {
    return attributeAssignId;
  }

  
  public void setAttributeAssignId(String attributeAssignId) {
    this.attributeAssignId = attributeAssignId;
  }

  
  public String getWorkflowInstanceState() {
    return workflowInstanceState;
  }

  
  public void setWorkflowInstanceState(String workflowInstanceState) {
    this.workflowInstanceState = workflowInstanceState;
  }

  
  public Long getWorkflowInstanceLastUpdatedMillisSince1970() {
    return workflowInstanceLastUpdatedMillisSince1970;
  }

  
  public void setWorkflowInstanceLastUpdatedMillisSince1970(
      Long workflowInstanceLastUpdatedMillisSince1970) {
    this.workflowInstanceLastUpdatedMillisSince1970 = workflowInstanceLastUpdatedMillisSince1970;
  }
  
  public String getWorkflowInstanceLastUpdatedDate() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    String date = dateFormat.format(new Date(workflowInstanceLastUpdatedMillisSince1970));
    return date;
  }

  
  public String getWorkflowInstanceConfigMarkerAssignmentId() {
    return workflowInstanceConfigMarkerAssignmentId;
  }

  
  public void setWorkflowInstanceConfigMarkerAssignmentId(
      String workflowInstanceConfigMarkerAssignmentId) {
    this.workflowInstanceConfigMarkerAssignmentId = workflowInstanceConfigMarkerAssignmentId;
  }

  
  public Long getWorkflowInstanceInitiatedMillisSince1970() {
    return workflowInstanceInitiatedMillisSince1970;
  }

  
  public void setWorkflowInstanceInitiatedMillisSince1970(
      Long workflowInstanceInitiatedMillisSince1970) {
    this.workflowInstanceInitiatedMillisSince1970 = workflowInstanceInitiatedMillisSince1970;
  }

  
  public String getWorkflowInstanceUuid() {
    return workflowInstanceUuid;
  }
  
  public void setWorkflowInstanceUuid(String workflowInstanceUuid) {
    this.workflowInstanceUuid = workflowInstanceUuid;
  }

  public String getWorkflowInstanceFileInfoString() {
    return workflowInstanceFileInfoString;
  }

  public void setWorkflowInstanceFileInfoString(String workflowInstanceFileInfoString) {
    this.workflowInstanceFileInfoString = workflowInstanceFileInfoString;
  }

  public GrouperWorkflowInstanceFilesInfo getGrouperWorkflowInstanceFilesInfo() {
    return grouperWorkflowInstanceFilesInfo;
  }
  
  public void setGrouperWorkflowInstanceFilesInfo(GrouperWorkflowInstanceFilesInfo grouperWorkflowInstanceFilesInfo) {
    this.grouperWorkflowInstanceFilesInfo = grouperWorkflowInstanceFilesInfo;
  }
  
  
  public String getWorkflowInstanceLogEntriesString() {
    return workflowInstanceLogEntriesString;
  }

  public void setWorkflowInstanceLogEntriesString(String workflowInstanceLogEntriesString) {
    this.workflowInstanceLogEntriesString = workflowInstanceLogEntriesString;
  }

  public GrouperWorkflowInstanceLogEntries getGrouperWorkflowInstanceLogEntries() {
    return grouperWorkflowInstanceLogEntries;
  }

  public void setGrouperWorkflowInstanceLogEntries(GrouperWorkflowInstanceLogEntries grouperWorkflowInstanceLogEntries) {
    this.grouperWorkflowInstanceLogEntries = grouperWorkflowInstanceLogEntries;
  }

  public void setWorkflowInstanceEncryptionKey(String workflowInstanceEncryptionKey) {
    this.workflowInstanceEncryptionKey = workflowInstanceEncryptionKey;
  }

  
  public String getWorkflowInstanceEncryptionKey() {
    return workflowInstanceEncryptionKey;
  }



  public String getWorkflowInstanceLastEmailedDate() {
    return workflowInstanceLastEmailedDate;
  }

  
  public void setWorkflowInstanceLastEmailedDate(String workflowInstanceLastEmailedDate) {
    this.workflowInstanceLastEmailedDate = workflowInstanceLastEmailedDate;
  }

  
  public String getWorkflowInstanceLastEmailedState() {
    return workflowInstanceLastEmailedState;
  }

  
  public void setWorkflowInstanceLastEmailedState(String workflowInstanceLastEmailedState) {
    this.workflowInstanceLastEmailedState = workflowInstanceLastEmailedState;
  }
  
  public String getWorkflowInstanceError() {
    return workflowInstanceError;
  }

  
  public void setWorkflowInstanceError(String workflowInstanceError) {
    this.workflowInstanceError = workflowInstanceError;
  }

  
  public String getWorkflowInstanceParamValue0String() {
    return workflowInstanceParamValue0String;
  }


  public void setWorkflowInstanceParamValue0String(
      String workflowInstanceParamValue0String) {
    this.workflowInstanceParamValue0String = workflowInstanceParamValue0String;
  }

  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue0() {
    return grouperWorkflowInstanceParamValue0;
  }

  public void setGrouperWorkflowInstanceParamValue0(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue0) {
    this.grouperWorkflowInstanceParamValue0 = grouperWorkflowInstanceParamValue0;
  }


  public String getWorkflowInstanceParamValue1String() {
    return workflowInstanceParamValue1String;
  }

  
  public void setWorkflowInstanceParamValue1String(
      String workflowInstanceParamValue1String) {
    this.workflowInstanceParamValue1String = workflowInstanceParamValue1String;
  }



  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue1() {
    return grouperWorkflowInstanceParamValue1;
  }



  
  public void setGrouperWorkflowInstanceParamValue1(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue1) {
    this.grouperWorkflowInstanceParamValue1 = grouperWorkflowInstanceParamValue1;
  }



  
  public String getWorkflowInstanceParamValue2String() {
    return workflowInstanceParamValue2String;
  }



  
  public void setWorkflowInstanceParamValue2String(
      String workflowInstanceParamValue2String) {
    this.workflowInstanceParamValue2String = workflowInstanceParamValue2String;
  }



  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue2() {
    return grouperWorkflowInstanceParamValue2;
  }



  
  public void setGrouperWorkflowInstanceParamValue2(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue2) {
    this.grouperWorkflowInstanceParamValue2 = grouperWorkflowInstanceParamValue2;
  }



  
  public String getWorkflowInstanceParamValue3String() {
    return workflowInstanceParamValue3String;
  }



  
  public void setWorkflowInstanceParamValue3String(
      String workflowInstanceParamValue3String) {
    this.workflowInstanceParamValue3String = workflowInstanceParamValue3String;
  }



  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue3() {
    return grouperWorkflowInstanceParamValue3;
  }



  
  public void setGrouperWorkflowInstanceParamValue3(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue3) {
    this.grouperWorkflowInstanceParamValue3 = grouperWorkflowInstanceParamValue3;
  }



  
  public String getWorkflowInstanceParamValue4String() {
    return workflowInstanceParamValue4String;
  }



  
  public void setWorkflowInstanceParamValue4String(
      String workflowInstanceParamValue4String) {
    this.workflowInstanceParamValue4String = workflowInstanceParamValue4String;
  }



  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue4() {
    return grouperWorkflowInstanceParamValue4;
  }



  
  public void setGrouperWorkflowInstanceParamValue4(
      GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue4) {
    this.grouperWorkflowInstanceParamValue4 = grouperWorkflowInstanceParamValue4;
  }



  
  public String getWorkflowInstanceParamValue5String() {
    return workflowInstanceParamValue5String;
  }



  
  public void setWorkflowInstanceParamValue5String(
      String workflowInstanceParamValue5String) {
    this.workflowInstanceParamValue5String = workflowInstanceParamValue5String;
  }



  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue5() {
    return grouperWorkflowInstanceParamValue5;
  }


  public void setGrouperWorkflowInstanceParamValue5(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue5) {
    this.grouperWorkflowInstanceParamValue5 = grouperWorkflowInstanceParamValue5;
  }

  
  public String getWorkflowInstanceParamValue6String() {
    return workflowInstanceParamValue6String;
  }
  
  public void setWorkflowInstanceParamValue6String(String workflowInstanceParamValue6String) {
    this.workflowInstanceParamValue6String = workflowInstanceParamValue6String;
  }

  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue6() {
    return grouperWorkflowInstanceParamValue6;
  }



  
  public void setGrouperWorkflowInstanceParamValue6(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue6) {
    this.grouperWorkflowInstanceParamValue6 = grouperWorkflowInstanceParamValue6;
  }



  
  public String getWorkflowInstanceParamValue7String() {
    return workflowInstanceParamValue7String;
  }



  
  public void setWorkflowInstanceParamValue7String(String workflowInstanceParamValue7String) {
    this.workflowInstanceParamValue7String = workflowInstanceParamValue7String;
  }



  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue7() {
    return grouperWorkflowInstanceParamValue7;
  }

  
  public void setGrouperWorkflowInstanceParamValue7(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue7) {
    this.grouperWorkflowInstanceParamValue7 = grouperWorkflowInstanceParamValue7;
  }



  
  public String getWorkflowInstanceParamValue8String() {
    return workflowInstanceParamValue8String;
  }



  
  public void setWorkflowInstanceParamValue8String(String workflowInstanceParamValue8String) {
    this.workflowInstanceParamValue8String = workflowInstanceParamValue8String;
  }



  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue8() {
    return grouperWorkflowInstanceParamValue8;
  }



  
  public void setGrouperWorkflowInstanceParamValue8(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue8) {
    this.grouperWorkflowInstanceParamValue8 = grouperWorkflowInstanceParamValue8;
  }



  
  public String getWorkflowInstanceParamValue9String() {
    return workflowInstanceParamValue9String;
  }



  
  public void setWorkflowInstanceParamValue9String(String workflowInstanceParamValue9String) {
    this.workflowInstanceParamValue9String = workflowInstanceParamValue9String;
  }

  
  public GrouperWorkflowInstanceParamValue getGrouperWorkflowInstanceParamValue9() {
    return grouperWorkflowInstanceParamValue9;
  }

  
  public void setGrouperWorkflowInstanceParamValue9(GrouperWorkflowInstanceParamValue grouperWorkflowInstanceParamValue9) {
    this.grouperWorkflowInstanceParamValue9 = grouperWorkflowInstanceParamValue9;
  }

  
  public GrouperObject getOwnerGrouperObject() {
    return ownerGrouperObject;
  }

  
  public void setOwnerGrouperObject(GrouperObject ownerGrouperObject) {
    this.ownerGrouperObject = ownerGrouperObject;
  }


  public static GrouperWorkflowInstanceFilesInfo buildInstanceFileInfoFromJsonString(String jsonString){
    
    try {      
      GrouperWorkflowInstanceFilesInfo configParams = GrouperWorkflowSettings.objectMapper.readValue(jsonString, GrouperWorkflowInstanceFilesInfo.class);
      return configParams;
    } catch (Exception e) {
      LOG.error("could not convert: "+jsonString+" to GrouperWorkflowInstanceFilesInfo object");
      throw new RuntimeException("could not convert json string to GrouperWorkflowInstanceFilesInfo object", e);
    }
    
  }
  
  public static GrouperWorkflowInstanceLogEntries buildInstanceLogEntriesFromJsonString(String jsonString) {
    try {      
      GrouperWorkflowInstanceLogEntries logEntries = GrouperWorkflowSettings.objectMapper.readValue(jsonString, GrouperWorkflowInstanceLogEntries.class);
      return logEntries;
    } catch (Exception e) {
      LOG.error("could not convert: "+jsonString+" to GrouperWorkflowInstanceLogEntries object");
      throw new RuntimeException("could not convert json string to GrouperWorkflowInstanceLogEntries object", e);
    }
  }
  
  public static GrouperWorkflowInstanceParamValue buildParamValueFromJsonString(String jsonString) {
    try {      
      GrouperWorkflowInstanceParamValue paramValue = GrouperWorkflowSettings.objectMapper.readValue(jsonString, GrouperWorkflowInstanceParamValue.class);
      return paramValue;
    } catch (Exception e) {
      LOG.error("could not convert: "+jsonString+" to GrouperWorkflowInstanceParamValue object");
      throw new RuntimeException("could not convert json string to GrouperWorkflowInstanceParamValue object", e);
    }
  }
  
  
  public String htmlFormWithValues() {
    
    String htmlForm = null;
    if (StringUtils.isBlank(grouperWorkflowConfig.getWorkflowConfigForm())) {
      //String htmlForm = grouperWorkflowConfig.buildHtmlFromParams(true, workflowInstanceState);   
      htmlForm = grouperWorkflowConfig.buildHtmlFromParams(false, workflowInstanceState);
    } else {
      htmlForm = grouperWorkflowConfig.buildHtmlFromConfigForm(workflowInstanceState);
    }
    
    Document document = Jsoup.parse(htmlForm);
    
    GrouperWorkflowConfigParams configParams = grouperWorkflowConfig.getConfigParams();
    
    for (int i =0; i<configParams.getParams().size(); i++) {
      GrouperWorkflowConfigParam workflowConfigParam = configParams.getParams().get(i);
      String paramName = workflowConfigParam.getParamName();
      try {
        Method method = this.getClass().getMethod("getGrouperWorkflowInstanceParamValue"+String.valueOf(i));
        GrouperWorkflowInstanceParamValue paramValueObject = (GrouperWorkflowInstanceParamValue) method.invoke(this);
        
        String value = paramValueObject.getParamValue();
        
        Element element = document.selectFirst("[name="+workflowConfigParam.getParamName()+"]");
        if (workflowConfigParam.getType().equals("checkbox")) {
          if (StringUtils.isNotBlank(value) && value.equals("on")) {
            element.attr("checked", "checked");
            // htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", "checked");
          }
        } else {
          element.val(value);
          // htmlForm = htmlForm.replaceAll("~~"+paramName+"~~", value);
        }
      } catch (Exception e) {
        throw new RuntimeException("Error occurred setting param values.");
      }
    }
    
    return document.html();
    
  }



  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(attributeAssignId)
        .append(workflowInstanceUuid)
        .toHashCode();
  }



  @Override
  public boolean equals(Object other) {
    
    if (this == other) {
      return true;
    }
    if (!(other instanceof GrouperWorkflowInstance)) {
      return false;
    }
    return new EqualsBuilder()
      .append( attributeAssignId, ( (GrouperWorkflowInstance) other ).getAttributeAssignId() )
      .append( workflowInstanceUuid, ( (GrouperWorkflowInstance) other ).getWorkflowInstanceUuid())
      .isEquals();
    
  }
  
  
  
}
