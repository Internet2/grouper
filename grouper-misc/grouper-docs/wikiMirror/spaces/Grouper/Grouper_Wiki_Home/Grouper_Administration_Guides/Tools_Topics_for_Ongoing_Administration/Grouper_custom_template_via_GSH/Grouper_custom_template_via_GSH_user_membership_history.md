---
title: "Grouper custom template via GSH user membership history"
space: Grouper
pageId: 28548962
version: 5
lastUpdated: 2026-07-01T05:43:05.685Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548962/Grouper+custom+template+via+GSH+user+membership+history
---

This shows groups that a user was a member of at a certain point in time, either in a folder or all groups.

## Config

## Config from export

```
grouperGshTemplate.userPointInTime.defaultRunButtonFolderUuidOrName = test
grouperGshTemplate.userPointInTime.folderShowType = allFolders
grouperGshTemplate.userPointInTime.gshTemplate = //
grouperGshTemplate.userPointInTime.input.0.description = Enter the user PennID or PennKey that you are looking for historical memberships for
grouperGshTemplate.userPointInTime.input.0.label = Enter PennKey or PennID
grouperGshTemplate.userPointInTime.input.0.maxLength = 8
grouperGshTemplate.userPointInTime.input.0.name = gsh_input_userId
grouperGshTemplate.userPointInTime.input.0.required = true
grouperGshTemplate.userPointInTime.input.0.validationBuiltin = alphaNumeric
grouperGshTemplate.userPointInTime.input.0.validationType = builtin
grouperGshTemplate.userPointInTime.input.1.description = Enter a timestamp to search for a user.  e.g. 2024/03/01 15\u003A12\u003A45
grouperGshTemplate.userPointInTime.input.1.label = Timestamp
grouperGshTemplate.userPointInTime.input.1.name = gsh_input_timestamp
grouperGshTemplate.userPointInTime.input.1.required = true
grouperGshTemplate.userPointInTime.input.1.validationType = none
grouperGshTemplate.userPointInTime.input.2.description = Show groups for all groups in Grouper you are allowed to READ or only in this folder?
grouperGshTemplate.userPointInTime.input.2.dropdownCsvValue = All groups, Only groups in this folder
grouperGshTemplate.userPointInTime.input.2.dropdownValueFormat = csv
grouperGshTemplate.userPointInTime.input.2.formElementType = dropdown
grouperGshTemplate.userPointInTime.input.2.label = All memberships?
grouperGshTemplate.userPointInTime.input.2.name = gsh_input_allObjects
grouperGshTemplate.userPointInTime.input.2.required = true
grouperGshTemplate.userPointInTime.moreActionsLabel = User memberships in the past
grouperGshTemplate.userPointInTime.numberOfInputs = 3
grouperGshTemplate.userPointInTime.runAsType = currentUser
grouperGshTemplate.userPointInTime.runButtonGroupOrFolder = folder
grouperGshTemplate.userPointInTime.runGshInTransaction = false
grouperGshTemplate.userPointInTime.securityRunType = everyone
grouperGshTemplate.userPointInTime.showInMoreActions = true
grouperGshTemplate.userPointInTime.showOnFolders = true
grouperGshTemplate.userPointInTime.templateDescription = Enter a user, timestamp, and a regex and see the group names for memberships of the user at a certain time in the past.  Note\u003A you will only see groups that you are allowed to READ
grouperGshTemplate.userPointInTime.templateName = User memberships in the past
grouperGshTemplate.userPointInTime.templateVersion = V2

```

## GSH script

```
import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.gsh.template.GshOutputLine;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.gsh.template.GshValidationLine;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

public class Test87userPointInTime extends GshTemplateV2 {

  @Override
  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {

    String ownerStemNameWithColon = gshTemplateV2input.getGsh_builtin_gshTemplateRuntime().getOwnerStemName() + ":";
    Subject currentUserSubject = gshTemplateV2input.getGsh_builtin_subject();
    Member currentUserMember = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), currentUserSubject, true);
    
    // action
    String gsh_input_userId = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_userId");
    String gsh_input_timestamp = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_timestamp");
    String gsh_input_allObjects = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_allObjects");

    Subject subjectSearchingFor = SubjectFinder.findByIdOrIdentifier(gsh_input_userId, false);

    if (subjectSearchingFor == null) {

      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addValidationLine("gsh_input_userId",
          "Error: user not found!");
      return;
      
    }

    Member memberSearchingFor = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subjectSearchingFor, true);
    
    Timestamp timestamp = null;
    
    try {
      timestamp = GrouperUtil.stringToTimestamp(gsh_input_timestamp);
    } catch (Exception e) {
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addValidationLine("gsh_input_timestamp",
          "Error: invalid timestamp!  Enter in the currect format: e.g. 2024/03/01 08:12:18");
      return;
      
    }
    
    boolean allObjects = false;
    
    if (StringUtils.equals("All groups", gsh_input_allObjects)) {
      allObjects = false;
    } else if (!StringUtils.equals("Only groups in this folder", gsh_input_allObjects)) {
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addValidationLine("gsh_input_allObjects",
          "Error: invalid drop down.  Shouldnt happen...");
      return;
      
    }
      
    
    
    GcDbAccess gcDbAccess = new GcDbAccess();
    if (PrivilegeHelper.isWheelOrRootOrReadonlyRoot(currentUserSubject)) {
      gcDbAccess.sql("select gpmglv.group_name from grouper_pit_mship_group_lw_v gpmglv "
          + " where gpmglv.field_name = 'members' "
          + " and gpmglv.member_id = ? and gpmglv.the_start_time < ? and (gpmglv.the_end_time is null "
          + " or gpmglv.the_end_time > ?)");
    } else {
      gcDbAccess.sql("select gpmglv.group_name from grouper_pit_mship_group_lw_v gpmglv, "
          + " grouper_memberships_lw_v gmlv where gpmglv.group_id = gmlv.group_id "
          + " and gmlv.list_name in ('admins', 'readers') and gmlv.member_id = ? "
          + " and gpmglv.field_name = 'members' and gpmglv.member_id = ? and gpmglv.the_start_time < ? and (gpmglv.the_end_time is null "
          + " or gpmglv.the_end_time > ?)").addBindVar(currentUserMember.getId());
    }
    List<String> groupNamesList = gcDbAccess.addBindVar(memberSearchingFor.getId()).
      addBindVar(timestamp.getTime()*1000).addBindVar(timestamp.getTime()*1000).selectList(String.class);
    
    Collections.sort(groupNamesList);
    Set<String> groupNamesSet = new LinkedHashSet<>();
    
    for (String groupName : groupNamesList) {
      if (allObjects || groupName.startsWith(ownerStemNameWithColon)) {
        groupNamesSet.add(groupName);
      }
    }
    
    if (GrouperUtil.length(groupNamesSet) == 0) {
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("The user was in no groups that you are allowed to READ at that time.");
      return;
    }
    
    
    GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();

    gsh_builtin_gshTemplateOutput.addOutputLine("The user was in the following groups.  These are ID paths (system name):");

    for (String groupName : groupNamesSet) {
      gsh_builtin_gshTemplateOutput.addOutputLine(groupName);
    }
    
  }
  
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Subject subject = SubjectFinder.findByIdAndSource("10021368", "pennperson", true);

        Test87userPointInTime test81smartSheet = new Test87userPointInTime();
        GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();
        gshTemplateV2input.setGsh_builtin_subject(subject);
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
        gshTemplateRuntime.setOwnerStemName("test");
        gshTemplateRuntime.setTemplateConfigId("userPointInTime");
        gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(gshTemplateRuntime);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_userId", "mchyzer");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_timestamp", "2024/03/01 08:12:18");
        // "Only groups in this folder", "All groups"
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_allObjects", "All groups");

        
        GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();
        
        test81smartSheet.gshRunLogic(gshTemplateV2input, gshTemplateV2output);

        for (GshValidationLine gshValidationLine : gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getValidationLines()) {
          System.out.println(gshValidationLine.getText());
        }

        for (GshOutputLine gshOutputLine : gshTemplateV2output.getGsh_builtin_gshTemplateOutput().getOutputLines()) {
          System.out.println(gshOutputLine.getText());
        }
        
        return null;
      }
    });
    System.exit(0);

  }

}
  
```
