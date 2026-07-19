---
title: "Grouper custom template via GSH used by daemon for CSV report"
space: Grouper
pageId: 28548277
version: 2
lastUpdated: 2026-01-15T17:44:47.434Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548277/Grouper+custom+template+via+GSH+used+by+daemon+for+CSV+report
---

This is a GSH template which is re-used for a few GSH script daemons to send out a CSV report attachment to emails

## Configuration

```
grouperGshTemplate.emailCsvReport.defaultRunButtonFolderUuidOrName = penn\u003Aetc\u003Atemplates\u003AemailCsvReport
grouperGshTemplate.emailCsvReport.folderShowOnDescendants = certainFoldersAndDescendants
grouperGshTemplate.emailCsvReport.folderShowType = certainFolders
grouperGshTemplate.emailCsvReport.folderUuidToShow = penn\u003Aetc\u003Atemplates\u003AemailCsvReport
grouperGshTemplate.emailCsvReport.gshTemplate = //
grouperGshTemplate.emailCsvReport.input.0.description = Comma separated list of email address
grouperGshTemplate.emailCsvReport.input.0.formElementType = textarea
grouperGshTemplate.emailCsvReport.input.0.label = Emails
grouperGshTemplate.emailCsvReport.input.0.maxLength = 2000
grouperGshTemplate.emailCsvReport.input.0.name = gsh_input_emails
grouperGshTemplate.emailCsvReport.input.0.required = true
grouperGshTemplate.emailCsvReport.input.0.validationType = none
grouperGshTemplate.emailCsvReport.input.1.description = Enter the subject of the email
grouperGshTemplate.emailCsvReport.input.1.label = Email subject
grouperGshTemplate.emailCsvReport.input.1.name = gsh_input_subject
grouperGshTemplate.emailCsvReport.input.1.required = true
grouperGshTemplate.emailCsvReport.input.1.validationType = none
grouperGshTemplate.emailCsvReport.input.2.description = Enter the email body describing the attachment and where to go for support
grouperGshTemplate.emailCsvReport.input.2.formElementType = textarea
grouperGshTemplate.emailCsvReport.input.2.label = Email body
grouperGshTemplate.emailCsvReport.input.2.maxLength = 2000
grouperGshTemplate.emailCsvReport.input.2.name = gsh_input_body
grouperGshTemplate.emailCsvReport.input.2.required = true
grouperGshTemplate.emailCsvReport.input.2.validationType = none
grouperGshTemplate.emailCsvReport.input.3.description = Enter the SQL external system config ID
grouperGshTemplate.emailCsvReport.input.3.label = Database config ID
grouperGshTemplate.emailCsvReport.input.3.name = gsh_input_databaseConfigId
grouperGshTemplate.emailCsvReport.input.3.required = true
grouperGshTemplate.emailCsvReport.input.3.validationType = none
grouperGshTemplate.emailCsvReport.input.4.description = Enter the full database query.  Should also include all column names
grouperGshTemplate.emailCsvReport.input.4.formElementType = textarea
grouperGshTemplate.emailCsvReport.input.4.label = SQL query
grouperGshTemplate.emailCsvReport.input.4.maxLength = 4000
grouperGshTemplate.emailCsvReport.input.4.name = gsh_input_query
grouperGshTemplate.emailCsvReport.input.4.required = true
grouperGshTemplate.emailCsvReport.input.4.validationType = none
grouperGshTemplate.emailCsvReport.input.5.description = Enter the CSV file name, should probably end in .csv.  Can use \u0024date_time\u0024 and will substitute in a timestamp
grouperGshTemplate.emailCsvReport.input.5.label = File name
grouperGshTemplate.emailCsvReport.input.5.name = gsh_input_fileName
grouperGshTemplate.emailCsvReport.input.5.validationType = none
grouperGshTemplate.emailCsvReport.input.6.description = BCC email addresses
grouperGshTemplate.emailCsvReport.input.6.label = BCC's
grouperGshTemplate.emailCsvReport.input.6.name = gsh_input_bccs
grouperGshTemplate.emailCsvReport.input.6.validationType = none
grouperGshTemplate.emailCsvReport.moreActionsLabel = Email CSV report
grouperGshTemplate.emailCsvReport.numberOfInputs = 7
grouperGshTemplate.emailCsvReport.runAsType = GrouperSystem
grouperGshTemplate.emailCsvReport.runButtonGroupOrFolder = folder
grouperGshTemplate.emailCsvReport.securityRunType = wheel
grouperGshTemplate.emailCsvReport.showInMoreActions = true
grouperGshTemplate.emailCsvReport.showOnFolders = true
grouperGshTemplate.emailCsvReport.templateDescription = Email a CSV as an attachment to some email addresses
grouperGshTemplate.emailCsvReport.templateName = Email CSV report
grouperGshTemplate.emailCsvReport.templateVersion = V2

```

GSH code

```
import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExec;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateInput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOwnerType;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.gsh.template.GshValidationLine;
import edu.internet2.middleware.grouper.app.reports.GrouperCsvReportJob;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class Test69emailCsvReport extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
      GshTemplateV2output gshTemplateV2output) {
    
    String gsh_input_query = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_query");
    String gsh_input_databaseConfigId = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_databaseConfigId");
    String gsh_input_fileName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_fileName");
    String gsh_input_subject = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_subject");
    String gsh_input_body = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_body");
    String gsh_input_emails = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_emails");
    String gsh_input_bccs = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_bccs");
    
    List<String> headers = GrouperCsvReportJob.retrieveHeaders(gsh_input_query, true);
    
    List<String[]> data = GrouperCsvReportJob.retrieveData(gsh_input_databaseConfigId, gsh_input_query);
    
    String fileName = GrouperUtil.tmpDir(true) + StringUtils.replace(gsh_input_fileName, "#date_time#", GrouperUtil.timestampToFileString(new Date()));

    File file = GrouperCsvReportJob.createCsv(fileName, headers, data);

    GrouperEmail grouperEmail = new GrouperEmail().addAttachment(file).setSubject(gsh_input_subject).setBody(gsh_input_body).setTo(gsh_input_emails);
    if (!StringUtils.isBlank(gsh_input_bccs)) {
      grouperEmail.setBcc(gsh_input_bccs);
    }
    grouperEmail.send();

  }

}
```

## Script daemon

Config

GSH script daemon source

```
String emails = "support@example.com"; 
    String bccs = "someone@example.com"; 
    String subject = "Penn O365 report for Annenberg"; 
    String body = "Attached is the Penn O365 report for Annenberg.  Email help@example.com for support.";
    String databaseConfigId = "pennCommunity";
    String fileName = "o365_Annenberg_report_#date_time#.csv";
    String query = "select PENN_ID, PENNKEY, NAME, INSTITUTIONAL_PREFERRED_EMAIL, PHONE, ACTIVE_CODE, O365_PRIMARY_AFFILIATION, CENTER, CENTER_NAME, HOME_ORG, SRS_DIVISION, SRS_SCHOOL, ALL_ACTIVE_AFFILIATIONS, O365_READY, EXPIRES_IN_DAYS, HAS_MAILBOX, O365_CENTER, JOB_ORG_CODE, JOB_ORG_DISPLAY_NAME, JOB_ORG_CENTER_CODE, FIRST_NAME, LAST_NAME, SCHEDULED_DELETION_DATE, JOB_CLASS_TITLE, PERSON_DESCRIPTION, DN, DECEASED_FLAG from pcdadmin.project_o365_Annenberg_v";
    
    GshTemplateExec gshTemplateExec = new GshTemplateExec();
    gshTemplateExec.assignConfigId("emailCsvReport");
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_emails").assignValueString(emails));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_subject").assignValueString(subject));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_body").assignValueString(body));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_databaseConfigId").assignValueString(databaseConfigId));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_fileName").assignValueString(fileName));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_query").assignValueString(query));
    gshTemplateExec.addGshTemplateInput(new GshTemplateInput().assignName("gsh_input_bccs").assignValueString(bccs));
    gshTemplateExec.assignOwnerStemName("penn:etc:templates:emailCsvReport");
    gshTemplateExec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    gshTemplateExec.assignCurrentUser(SubjectFinder.findRootSubject());
    GshTemplateExecOutput gshTemplateExecOutput = gshTemplateExec.execute();
    List<GshValidationLine> validationLines = gshTemplateExecOutput.getGshTemplateOutput().getValidationLines();
    if (GrouperUtil.length(validationLines) > 0) {
      throw new RuntimeException(GrouperUtil.toStringForLog(validationLines));
    }
    if (gshTemplateExecOutput.getException() != null) {
      throw gshTemplateExecOutput.getException();
    }
```
