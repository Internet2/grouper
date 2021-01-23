package edu.internet2.middleware.grouper.app.loader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperEmailUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.subject.Subject;

public class NotificationDaemon extends OtherJobBase {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(NotificationDaemon.class);


  public NotificationDaemon() {
  }

  /**
   * 
   * @return the stem name
   */
  public static String attributeAutoCreateStemName() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects", "etc") + ":attribute:notifications";
  }

  public static final String GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT_DEF = "grouperNotificationLastSentDef";

  public static final String GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT = "grouperNotificationLastSent";

  private OtherJobInput otherJobInput = null;

  private Map<String, Object> debugMap = null;

  private GrouperSession grouperSession = null;
  
  private String jobName = null;
  
  private List<Subject> emailSummaryToSubjects = null;
  
  private String emailTypeString = null;
  
  private boolean isNotification = false;
  private boolean isSummary = false;
    
  private String emailSubjectTemplate = null;

  private String emailBodyTemplate = null;

  // select penn_id, needed_by_date from authz_ngss_ferpa_needed_v
  private String emailListQuery = null;

  private String subjectSourceId = null;

  // pennCommunity
  private String emailListDbConnection = null;
  
  private String lastSentGroupName = null;

  private String bccsCommaSeparated = null;

  private Group lastSentGroup = null;
  
  private Set<String> eligibilitySubjectIds = null;
  
  private List<Object[]> results = null;
  
  private int subjectIdIndex = -1;
  
  private boolean sendToBccOnly = false;
  
  private boolean emailSummaryOnlyIfRecordsExist = false;
  
  private String date = null;
  
  @Override
  public OtherJobOutput run(OtherJobInput theOtherJobInput) {
    
    this.otherJobInput = theOtherJobInput;
    
    debugMap = new LinkedHashMap<String, Object>();
    
    grouperSession = GrouperSession.startRootSession();
    
    jobName = otherJobInput.getJobName();
    
    // jobName = OTHER_JOB_csvSync
    jobName = jobName.substring("OTHER_JOB_".length(), jobName.length());

    // notification, summary
    emailTypeString = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".emailType");
    debugMap.put("emailType", emailTypeString);
    
    isNotification = "notification".equals(emailTypeString);
    isSummary = "summary".equals(emailTypeString);
    if (!isNotification && !isSummary) {
      throw new RuntimeException("Invalid email type: '" + emailTypeString + "'" );
    }
    emailSummaryToSubjects = null;
    if (isSummary) {
      
      emailSummaryToSubjects = new ArrayList<Subject>();
      
      String emailSummaryToGroupName = GrouperLoaderConfig
          .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".emailSummaryToGroupName");
      
      Group emailSummaryToGroup = GroupFinder.findByName(grouperSession, emailSummaryToGroupName, true);
      
      for (Member member : emailSummaryToGroup.getMembers()) {
        Subject subject = member.getSubject();
        if (subject == null) {
          continue;
        }
        String email = GrouperEmailUtils.getEmail(subject);
        if (StringUtils.isBlank(email)) {
          continue;
        }
        emailSummaryToSubjects.add(subject);
      }
      
      // sort for testing
      Collections.sort(emailSummaryToSubjects, new Comparator<Subject>() {

        @Override
        public int compare(Subject o1, Subject o2) {
          return o1.getId().compareTo(o2.getId());
        }
      });
      
      emailSummaryOnlyIfRecordsExist = GrouperLoaderConfig
          .retrieveConfig().propertyValueBooleanRequired("otherJob." + jobName + ".emailSummaryOnlyIfRecordsExist");
    }
    
    emailSubjectTemplate = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".emailSubjectTemplate");

    emailBodyTemplate = GrouperLoaderConfig
        .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".emailBodyTemplate");

    // select penn_id, needed_by_date from authz_ngss_ferpa_needed_v
    emailListQuery = GrouperLoaderConfig
        .retrieveConfig().propertyValueString("otherJob." + jobName + ".emailListQuery");

    subjectSourceId = GrouperLoaderConfig
        .retrieveConfig().propertyValueString("otherJob." + jobName + ".subjectSourceId");

    // pennCommunity
    emailListDbConnection = GrouperLoaderConfig
        .retrieveConfig().propertyValueString("otherJob." + jobName + ".emailListDbConnection");

    if (StringUtils.isBlank(emailListQuery)) {
      String emailListGroupName = GrouperLoaderConfig
          .retrieveConfig().propertyValueString("otherJob." + jobName + ".emailListGroupName");
      GrouperUtil.assertion(StringUtils.isNotBlank(emailListGroupName), "emailListQuery or emailListGroupName is required");

      emailListDbConnection = "grouper";
      GrouperUtil.assertion(StringUtils.isNotBlank(emailListGroupName), "emailListQuery or emailListGroupName is required");
      GrouperUtil.assertion(!emailListGroupName.contains("'"), "emailListGroupName '" + emailListGroupName + "' cannot contain a single quote!");
      emailListQuery = "select subject_id from grouper_memberships_lw_v where group_name = '" + emailListGroupName + "' and list_name = 'members' order by subject_id ";
      if (!StringUtils.isBlank(subjectSourceId)) {
        
        GrouperUtil.assertion(!subjectSourceId.contains("'"), "subjectSourceId '" + subjectSourceId + "' cannot contain a single quote!");
        emailListQuery += " and subject_source = '" + subjectSourceId + "'";
        
      }

    } else {
      GrouperUtil.assertion(StringUtils.isNotBlank(emailListQuery), "emailListQuery or emailListGroupName is required");
    }
    
    // penn:isc:ait:apps:ngss:team:ngssTeamNotificationsLastSent
    lastSentGroupName = GrouperLoaderConfig
        .retrieveConfig().propertyValueString("otherJob." + jobName + ".lastSentGroupName");

    bccsCommaSeparated = GrouperLoaderConfig
        .retrieveConfig().propertyValueString("otherJob." + jobName + ".bccsCommaSeparated");

    sendToBccOnly = GrouperLoaderConfig
        .retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".sendToBccOnly", false);
    
    lastSentGroup = null;
    
    if (!StringUtils.isBlank(lastSentGroupName)) {
      lastSentGroup = GroupFinder.findByName(grouperSession, lastSentGroupName, true);
  
//      lastSentNameOfAttributeDefName = GrouperLoaderConfig
//          .retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".lastSentAttributeDefName");
//      AttributeDefName lastSentAttributeDefName = null;
//      
//      lastSentAttributeDefName = AttributeDefNameFinder.findByName(lastSentNameOfAttributeDefName, true);
//  
//      AttributeDef lastSentAttributeDef = lastSentAttributeDefName.getAttributeDef();
//      
//      GrouperUtil.assertion(!lastSentAttributeDef.isMultiValued(), 
//          lastSentAttributeDef.getName() + " must not be multi valued");
//      GrouperUtil.assertion(!lastSentAttributeDef.isMultiAssignable(), 
//          lastSentAttributeDef.getName() + " must not be multi assign");
//      GrouperUtil.assertion(lastSentAttributeDef.getValueType() == AttributeDefValueType.string, 
//          lastSentAttributeDef.getName() + " must be value type string");
//      GrouperUtil.assertion(lastSentAttributeDef.isAssignToImmMembership(), 
//          lastSentAttributeDef.getName() + " must be able to be assign to immediate membership");
    }
    
    eligibilitySubjectIds = null;
    
    {
      Group eligibilityGroup = null;
      
      String eligibilityGroupName = GrouperLoaderConfig
          .retrieveConfig().propertyValueString("otherJob." + jobName + ".eligibilityGroupName");
      if (!StringUtils.isBlank(eligibilityGroupName)) {
        eligibilityGroup = GroupFinder.findByName(grouperSession, eligibilityGroupName, true);
        GcDbAccess gcDbAccess = new GcDbAccess();
        gcDbAccess.addBindVar(eligibilityGroup.getName());
        
        String membershipQuery = "select subject_id from grouper_memberships_lw_v where group_name = ? and list_name = 'members' ";
        if (!StringUtils.isBlank(subjectSourceId)) {
          membershipQuery += " and subject_source = ?";
          gcDbAccess.addBindVar(subjectSourceId);
        }
        
        eligibilitySubjectIds = new HashSet<String>(
            gcDbAccess.sql(membershipQuery).selectList(String.class));
      }

    }

    date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
    debugMap.put("date", date);
    
    if (lastSentGroup != null) {
      subjectIdsSentToday = new HashSet<String>(new GcDbAccess()
          .sql("select subject_id from grouper_aval_asn_mship_v gaaev "
              + "where group_name = ? "
              + "and attribute_def_name_name = ? and value_string = ?")
          .addBindVar(lastSentGroupName).addBindVar(
              NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT )
          .addBindVar(date).selectList(String.class));
    }
    
    results = new GcDbAccess().connectionName(emailListDbConnection)
        .sql(emailListQuery)
        .selectList(Object[].class);

    gcTableSyncTableMetadata = GcTableSyncTableMetadata.retrieveQueryMetadataFromDatabase(emailListDbConnection, emailListQuery);

    for (int i=0;i<gcTableSyncTableMetadata.getColumnMetadata().size();i++) {
      GcTableSyncColumnMetadata gcTableSyncColumnMetadata = gcTableSyncTableMetadata.getColumnMetadata().get(i);
      if ("subject_id".equals(gcTableSyncColumnMetadata.getColumnName().toLowerCase())) {
        subjectIdIndex = i;
        break;
      }
    }
    
    GrouperUtil.assertion(!isNotification || subjectIdIndex >= 0, "Cannot find a column named: subject_id: " + emailListQuery);
    
    
    for (int i=0;i<gcTableSyncTableMetadata.getColumnMetadata().size();i++) {
      GcTableSyncColumnMetadata gcTableSyncColumnMetadata = gcTableSyncTableMetadata.getColumnMetadata().get(i);
      if ("email_address_to_send_to".equals(gcTableSyncColumnMetadata.getColumnName().toLowerCase())) {
        emailAddressIndex = i;
        break;
      }
    }
    
    otherJobInput.getHib3GrouperLoaderLog().addTotalCount(GrouperUtil.length(results));

    if (isNotification) {
      sendNotifications();
    } else if (isSummary) {
      sendSummary();
    }
    
    return null;
  }

  public void sendSummary() {

    RuntimeException re = null;

    int resultCount = GrouperUtil.length(results);
    int eligibleSubjectCount = GrouperUtil.length(eligibilitySubjectIds);
    int subjectIdsNullCount = 0;
    int ineligibleSubjectCount = 0;
    int subjectsNotFoundCount = 0;
    int subjectsAlreadySentToday = 0;

    List<Map<String, Object>> listOfRecordMaps = new ArrayList<Map<String, Object>>();
    for (Object[] result : (List<Object[]>)results) {
      
      String subjectId = (String)result[subjectIdIndex];

      if (StringUtils.isBlank(subjectId)) {
        subjectIdsNullCount++;
        continue;
      }
      
      String email = null;

      if (eligibilitySubjectIds != null && !eligibilitySubjectIds.contains(subjectId)) {
        ineligibleSubjectCount++;
        continue;
      }

      Subject subject = StringUtils.isBlank(subjectSourceId) ? SubjectFinder.findById(subjectId, false) 
          : SubjectFinder.findByIdAndSource(subjectId, subjectSourceId, false);
      if (subject == null) {
        subjectsNotFoundCount++;
        continue;
      }
      if (emailAddressIndex >= 0) {
        email = (String)result[emailAddressIndex];
      }
      if (GrouperUtil.isBlank(email)) {
        email = GrouperEmailUtils.getEmail(subject);
      }
      
      Map<String, Object> recordMap = new HashMap<String, Object>();
      recordMap.put("email", email);
      recordMap.put("subject", subject);
      recordMap.put("subject_name", subject.getName());
      recordMap.put("subject_id", subject.getId());
      recordMap.put("subject_description", subject.getDescription());
      for (String attributeName : GrouperUtil.nonNull(subject.getAttributes()).keySet()) {
        
        String attributeValueOrCommaSeparated = subject.getAttributeValueOrCommaSeparated(attributeName);
        recordMap.put("subject_attribute_" + attributeName.toLowerCase(), attributeValueOrCommaSeparated);
        
      }

      int i=0;
      for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : gcTableSyncTableMetadata.getColumnMetadata()) {

        recordMap.put(
            "column_" + gcTableSyncColumnMetadata.getColumnName().toLowerCase(), 
            result[i]);

        i++;
      }
      listOfRecordMaps.add(recordMap);
    }

    debugMap.put("resultCount", resultCount);
    debugMap.put("eligibleSubjectCount", eligibleSubjectCount);
    debugMap.put("subjectIdsNullCount", subjectIdsNullCount);
    debugMap.put("ineligibleSubjectCount", ineligibleSubjectCount);
    debugMap.put("subjectsNotFoundCount", subjectsNotFoundCount);
    debugMap.put("listOfRecordMapsCount", listOfRecordMaps);

    if (!emailSummaryOnlyIfRecordsExist || GrouperUtil.length(listOfRecordMaps) > 0) {

      int emailToIndex = 0;
      debugMap.put("emailSummaryToEmailAddressesCount", GrouperUtil.length(this.emailSummaryToSubjects));

      for (Subject emailSummaryToSubject : this.emailSummaryToSubjects) {

        if (subjectIdsSentToday != null && subjectIdsSentToday.contains(emailSummaryToSubject.getId())) {
          subjectsAlreadySentToday++;
          continue;
        }

        Map<String, Object> variableMap = new HashMap<String, Object>();
        String email = GrouperEmailUtils.getEmail(emailSummaryToSubject);
        variableMap.put("email", email);
        variableMap.put("subject", emailSummaryToSubject);
        variableMap.put("subject_name", emailSummaryToSubject.getName());
        variableMap.put("subject_id", emailSummaryToSubject.getId());
        variableMap.put("subject_description", emailSummaryToSubject.getDescription());
        for (String attributeName : GrouperUtil.nonNull(emailSummaryToSubject.getAttributes()).keySet()) {
          
          String attributeValueOrCommaSeparated = emailSummaryToSubject.getAttributeValueOrCommaSeparated(attributeName);
          variableMap.put("subject_attribute_" + attributeName.toLowerCase(), attributeValueOrCommaSeparated);
          
        }

        variableMap.put("listOfRecordMaps", listOfRecordMaps);
        variableMap.put("listOfRecordMapsCount", GrouperUtil.length(listOfRecordMaps));
        variableMap.put("resultCount", resultCount);
        variableMap.put("eligibleSubjectCount", eligibleSubjectCount);
        variableMap.put("subjectIdsNullCount", subjectIdsNullCount);
        variableMap.put("ineligibleSubjectCount", ineligibleSubjectCount);
        variableMap.put("subjectsNotFoundCount", subjectsNotFoundCount);
        
        String subjectText = GrouperUtil.substituteExpressionLanguageTemplate(emailSubjectTemplate, variableMap, true, false, true);
        emailBodyTemplate = GrouperUtil.replace(emailBodyTemplate, "__NEWLINE__", "\n");
        String bodyText = GrouperUtil.substituteExpressionLanguageTemplate(emailBodyTemplate, variableMap, true, false, true);

        GrouperEmail grouperEmail = new GrouperEmail().setSubject(subjectText);

        if (emailToIndex < 20) {
          debugMap.put("emailTo_" + emailToIndex, email);
        }
        
        if (!sendToBccOnly) {
          grouperEmail.setTo(email);
          if (!StringUtils.isBlank(bccsCommaSeparated)) {
            grouperEmail.setBcc(bccsCommaSeparated);
          }
        } else {
          GrouperUtil.assertion(StringUtils.isNotBlank(bccsCommaSeparated), "If you are sending to bcc only, then you need to list some bccs");
          grouperEmail.setTo(bccsCommaSeparated);
          bodyText = "To: " + email + " (Note: this line and following whitespace not included in real email)\n\n" + bodyText;
        }
        grouperEmail.setBody(bodyText);
        
        // send this out if we are sending emails to recipients, or bcc up to 20
        grouperEmail.send();

        if (!sendToBccOnly) {
          if (lastSentGroup != null) {
            lastSentGroup.addMember(emailSummaryToSubject, false);
            Member member = MemberFinder.findBySubject(grouperSession, emailSummaryToSubject, true);
            lastSentGroup.getAttributeValueDelegateMembership(member).assignValue(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT, date);
          }
        }

        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);

      }
      
    }
    
    debugMap.put("subjectsAlreadySentToday", subjectsAlreadySentToday);

    otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
  }
  
  private GcTableSyncTableMetadata gcTableSyncTableMetadata;
  
  private Set<String> subjectIdsSentToday = null;

  private int emailAddressIndex = -1;
  
  public void sendNotifications() {

    RuntimeException re = null;

    int resultCount = GrouperUtil.length(results);
    int eligibleSubjectIds = GrouperUtil.length(eligibilitySubjectIds);
    int subjectIdsNull = 0;
    int ineligibleSubjects = 0;
    int subjectsAlreadySentToday = 0;
    int subjectsNotFound = 0;
    int subjectsWithBlankEmailAddress = 0;
    
    int emailCount = 0;
    for (Object[] result : (List<Object[]>)results) {
      
      String subjectId = (String)result[subjectIdIndex];
      
      if (StringUtils.isBlank(subjectId)) {
        subjectIdsNull++;
        continue;
      }
      String email = null;
      try {

        if (eligibilitySubjectIds != null && !eligibilitySubjectIds.contains(subjectId)) {
          ineligibleSubjects++;
          continue;
        }

        if (subjectIdsSentToday != null && subjectIdsSentToday.contains(subjectId)) {
          subjectsAlreadySentToday++;
          continue;
        }
        Subject subject = StringUtils.isBlank(subjectSourceId) ? SubjectFinder.findById(subjectId, false) 
            : SubjectFinder.findByIdAndSource(subjectId, subjectSourceId, false);
        if (subject == null) {
          subjectsNotFound++;
          continue;
        }
        if (emailAddressIndex >= 0) {
          email = (String)result[emailAddressIndex];
        }
        if (GrouperUtil.isBlank(email)) {
          email = GrouperEmailUtils.getEmail(subject);
        }
        if (GrouperUtil.isBlank(email)) {          
          subjectsWithBlankEmailAddress++;
          continue;
        }
        
        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("email", email);
        variableMap.put("subject_name", subject.getName());
        variableMap.put("subject_id", subject.getId());
        variableMap.put("subject_description", subject.getDescription());
        for (String attributeName : GrouperUtil.nonNull(subject.getAttributes()).keySet()) {
          
          String attributeValueOrCommaSeparated = subject.getAttributeValueOrCommaSeparated(attributeName);
          variableMap.put("subject_attribute_" + attributeName.toLowerCase(), attributeValueOrCommaSeparated);
          
        }

        int i=0;
        for (GcTableSyncColumnMetadata gcTableSyncColumnMetadata : gcTableSyncTableMetadata.getColumnMetadata()) {

          variableMap.put(
              "column_" + gcTableSyncColumnMetadata.getColumnName().toLowerCase(), 
              result[i]);

          i++;
        }
        
        String subjectText = GrouperUtil.substituteExpressionLanguageTemplate(emailSubjectTemplate, variableMap, true, false, true);
        emailBodyTemplate = GrouperUtil.replace(emailBodyTemplate, "__NEWLINE__", "\n");
        String bodyText = GrouperUtil.substituteExpressionLanguageTemplate(emailBodyTemplate, variableMap, true, false, true);

        GrouperEmail grouperEmail = new GrouperEmail()
          .setSubject(GrouperUtil.trim(subjectText));
        
        if (!sendToBccOnly) {
          grouperEmail.setTo(email);
          if (!StringUtils.isBlank(bccsCommaSeparated)) {
            grouperEmail.setBcc(bccsCommaSeparated);
          }
        } else {
          GrouperUtil.assertion(StringUtils.isNotBlank(bccsCommaSeparated), "If you are sending to bcc only, then you need to list some bccs");
          grouperEmail.setTo(bccsCommaSeparated);
          bodyText = "To: " + email + " (Note: this line and following whitespace not included in real email)\n\n" + bodyText;
        }
        grouperEmail.setBody(StringUtils.trim(bodyText));
        
        // send this out if we are sending emails to recipients, or bcc up to 20
        if (!sendToBccOnly || emailCount < 20) {
          grouperEmail.send();
          emailCount++;
        }

        if (!sendToBccOnly) {
          if (lastSentGroup != null) {
            lastSentGroup.addMember(subject, false);
            Member member = MemberFinder.findBySubject(grouperSession, subject, true);
            lastSentGroup.getAttributeValueDelegateMembership(member).assignValue(NotificationDaemon.attributeAutoCreateStemName()  + ":" + NotificationDaemon.GROUPER_ATTRIBUTE_NOTIFICATION_LAST_SENT, date);
          }
        }

        otherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);

      } catch (RuntimeException e) {

        LOG.error("Error sending email to: " + subjectId + ", '" + email + "'");
        re = e;

        otherJobInput.getHib3GrouperLoaderLog().incrementUnresolvableSubjectCount();
      }
    }
    debugMap.put("resultCount", resultCount);
    debugMap.put("eligibleSubjectIds", eligibleSubjectIds);
    debugMap.put("subjectIdsNull", subjectIdsNull);
    debugMap.put("ineligibleSubjects", ineligibleSubjects);
    debugMap.put("subjectsAlreadySentToday", subjectsAlreadySentToday);
    debugMap.put("subjectsNotFound", subjectsNotFound);
    debugMap.put("subjectsWithBlankEmailAddress", subjectsWithBlankEmailAddress);
    otherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
    if (re != null) {
      throw re;
    }
  }

  

}
