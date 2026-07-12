---
title: "Grouper custom template via GSH - V2"
space: Grouper
pageId: 28547883
version: 9
lastUpdated: 2026-07-01T05:45:57.881Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547883/Grouper+custom+template+via+GSH+-+V2
---

In Grouper 4.9.0+ there is a new optional format for GSH templates.

You can use the previous format without making changes, there are no plans to deprecate it. The new version involves only the source code (GSH) and the template version. All the inputs and validations etc work the same.

## Advantages

- You can have test cases and test from the UI
  
  - This facilitates easier changes
  - Quicker to test during upgrades
  - More reliable GSH template logic
- The template only executes in GSH once
  
  - The templates are a lot faster
  - Less resources used in UI/WS

## Write a new template

- Configure the template and inputs in UI
- Make a subclass of GshTemplateV2
- Implement the gshRunLogic() method
- Optionally override the decorateTemplateForUiDisplay() method for [dynamic dropdown templates](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555130/Grouper+custom+template+via+GSH+-+V2+-+dynamic+drop+downs+and+text)
- Declare variables like was generated for you in V1 templates
- Register the subclass with Grouper

For details see the example below...

## Convert from old to new

- Edit template
- Make a class with subclass
- Declare the built in variables yourself (see example)
- Convert any "GSH shortcuts" to the Grouper Java API. e.g.  
  FROM
  
  
  ```
  addMember("a:b:c", "jsmith");
  ```
  
  TO
  
  
  ```
  Group group = GroupFinder.findByName("a:b:c", true);
  group.addMember(SubjectFinder.findByIdOrIdentifier("jsmith", true), false);
  ```
- Edit the template config, and mark version as v2
- Add tests as needed
- Run the tests from the UI
- Change from GrouperUtil.gshReturn() to return

## Example without tests

Config

```
grouperGshTemplate.myTemplate.defaultRunButtonFolderUuidOrName = test
grouperGshTemplate.myTemplate.displayErrorOutput = true
grouperGshTemplate.myTemplate.folderShowType = allFolders
grouperGshTemplate.myTemplate.input.0.description = app name
grouperGshTemplate.myTemplate.input.0.label = App name
grouperGshTemplate.myTemplate.input.0.name = gsh_input_appName
grouperGshTemplate.myTemplate.input.0.validationBuiltin = alphaNumericUnderscore
grouperGshTemplate.myTemplate.input.0.validationType = builtin
grouperGshTemplate.myTemplate.input.1.description = input2
grouperGshTemplate.myTemplate.input.1.label = Input2
grouperGshTemplate.myTemplate.input.1.name = gsh_input_input2
grouperGshTemplate.myTemplate.input.1.validationType = none
grouperGshTemplate.myTemplate.moreActionsLabel = test
grouperGshTemplate.myTemplate.numberOfInputs = 2
grouperGshTemplate.myTemplate.runAsType = GrouperSystem
grouperGshTemplate.myTemplate.runButtonGroupOrFolder = folder
grouperGshTemplate.myTemplate.securityRunType = everyone
grouperGshTemplate.myTemplate.showInMoreActions = true
grouperGshTemplate.myTemplate.showOnFolders = true
grouperGshTemplate.myTemplate.templateDescription = test
grouperGshTemplate.myTemplate.templateName = test
grouperGshTemplate.myTemplate.templateVersion = V2

```

Config from UI

Template source

```
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateRuntime;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

//public class Test66testTemplate {

  // you need a class that extends GshTemplateV2
  public class MyGshTemplate extends GshTemplateV2 {
    
    // implement the gshRunLogic method, this is what is called when the template executes
    public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
        GshTemplateV2output gshTemplateV2output) {
      
      // declare all the variables and inputs you need (similar to what it generated for you in V1 templates)
      GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
      //GrouperSession gsh_builtin_grouperSession = gshTemplateV2input.getGsh_builtin_grouperSession();
      //GshTemplateRuntime gsh_builtin_gshTemplateRuntime = gshTemplateV2input.getGsh_builtin_gshTemplateRuntime();
      //Subject gsh_builtin_subject = gshTemplateV2input.getGsh_builtin_subject();
      //String gsh_builtin_subjectId = gshTemplateV2input.getGsh_builtin_subjectId();
      String gsh_builtin_ownerStemName = gshTemplateV2input.getGsh_builtin_ownerStemName();
      //String gsh_builtin_ownerGroupName = gshTemplateV2input.getGsh_builtin_ownerGroupName();
      //String templateConfigId = gsh_builtin_gshTemplateRuntime.getTemplateConfigId();
      String gsh_input_appName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_appName");
      String gsh_input_input2 = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_input2");
      //Integer gsh_input_someInt = gshTemplateV2input.getGsh_builtin_inputInteger("gsh_input_someInt");
      //Boolean gsh_input_someBool = gshTemplateV2input.getGsh_builtin_inputBoolean("gsh_input_someInt");

      String appStemName = gsh_builtin_ownerStemName + ":" + gsh_input_appName;
      
      Stem stem = StemFinder.findByName(appStemName, false);
      if (stem != null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_appName", String.format("Error: Folder exists '%s'!", appStemName));

      }
      
      if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
        gsh_builtin_gshTemplateOutput.assignIsError(true);
        return;
      }

      new StemSave().assignName(appStemName).assignDescription(String.format("Description: %s", gsh_input_input2)).save();

      gsh_builtin_gshTemplateOutput.addOutputLine("Stem created!");
            
    }

  }
  
  // register the template with grouper.  Note this is only needed before v4.10.0...
  gsh_builtin_gshTemplateRuntime.assignGshTemplateV2(new MyGshTemplate());
  
//}

```

## Example of template with tests

Note this uses the same config as above

Declare a method that starts with "test" that takes no inputs and returns a GshTemplateV2test implementation. You can define the classes below the main template to keep things organized. The main template can have static variables which can be shared with the tests (so things dont get out of sync). Note, the tests should not be all that destructive since you will be running these in a grouper env. (e.g. dont delete the whole registry!)

```
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExecTestOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2test;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2utils;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.util.GrouperUtil;

//public class Test65testTemplate {

  public class MyGshTemplate extends GshTemplateV2 {

    static String errorFolderExists = "Error: Folder exists '%s'!";
    static String descriptionFormat = "Description: %s";
    static String success = "Stem created!";
    
    public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
        GshTemplateV2output gshTemplateV2output) {
      
      GshTemplateOutput gsh_builtin_gshTemplateOutput = gshTemplateV2output.getGsh_builtin_gshTemplateOutput();
      String appName = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_appName");
      if (StringUtils.equals("wikiException", appName)) {
        throw new RuntimeException("expected run logic exception");
      }
      String input2 = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_input2");
      
      String stemName = gshTemplateV2input.getGsh_builtin_ownerStemName();
      String appStemName = stemName + ":" + appName;
      
      Stem stem = StemFinder.findByName(appStemName, false);
      if (stem != null) {
        gsh_builtin_gshTemplateOutput.addValidationLine("gsh_input_appName", String.format(errorFolderExists, appStemName));

      }
      
      if (GrouperUtil.length(gsh_builtin_gshTemplateOutput.getValidationLines()) > 0) {
        gsh_builtin_gshTemplateOutput.assignIsError(true);
        return;
      }

      new StemSave().assignName(appStemName).assignDescription(String.format(descriptionFormat, input2)).save();

      gsh_builtin_gshTemplateOutput.addOutputLine(success);
            
    }

    public GshTemplateV2test testCheckLogicException() {
      return new TestCheckLogicException();
    }
    
    public GshTemplateV2test testNotExistingExpectFailure() {
      return new TestNotExistingExpectFailure();
    }
    
    public GshTemplateV2test testTeardownException() {
      return new TestTeardownException();
    }
    
    public GshTemplateV2test testRunLogicException() {
      return new TestRunLogicException();
    }
    
    public GshTemplateV2test testSetupException() {
      return new TestSetupException();
    }
    
    public GshTemplateV2test testExistingDoesntWork() {
      return new TestExistingDoesntWork();
    }
    
    public GshTemplateV2test testNotExistingDoesWork() {
      return new TestNotExistingDoesWork();
    }
    
    public GshTemplateV2test testBuiltinValidationExpectFailure() {
      return new TestBuiltinValidationExpectFailure();
    }
    
    public GshTemplateV2test testBuiltinValidationExpectSuccess() {
      return new TestBuiltinValidationExpectSuccess();
    }
    
  }
  
  public class TestTeardownException extends GshTemplateV2test {
    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wiki");
      this.addGshInput("gsh_input_input2", "some string");
      // stem shouldnt exist
      new StemSave().assignName("test:wiki").assignSaveMode(SaveMode.DELETE).save();
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
            
      this.setGshStemName("test");
    }

    @Override
    public void gshCheckResult() {
    }

    protected void tearDown() {
      throw new RuntimeException("Expected teardown exception");
    }
      
  }
  
  public class TestCheckLogicException extends GshTemplateV2test {
    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wiki");
      this.addGshInput("gsh_input_input2", "some string");
      // stem shouldnt exist
      new StemSave().assignName("test:wiki").assignSaveMode(SaveMode.DELETE).save();
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
            
      this.setGshStemName("test");
    }

    @Override
    public void gshCheckResult() {
      throw new RuntimeException("Expected check result exception");
    }

    protected void tearDown() {
    }
      
  }
  
  public class TestSetupException extends GshTemplateV2test {

    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wikiException");
      this.addGshInput("gsh_input_input2", "some string");
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
      
      this.setGshStemName("test");

      throw new RuntimeException("expected exception");
    }

    @Override
    public void gshCheckResult() {
    }

    protected void tearDown() {
    }
      
  }

  public class TestRunLogicException extends GshTemplateV2test {
    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wikiException");
      this.addGshInput("gsh_input_input2", "some string");
      // stem shouldnt exist
      new StemSave().assignName("test:wiki").assignSaveMode(SaveMode.DELETE).save();
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
      
      this.setGshStemName("test");
    }

    @Override
    public void gshCheckResult() {
    }

    protected void tearDown() {
    }
      
  }

  public class TestExistingDoesntWork extends GshTemplateV2test {
    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wiki");
      this.addGshInput("gsh_input_input2", "some string");
      // stem exists
      new StemSave().assignName("test:wiki").assignDescription("desc").save();
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
      
      this.setGshExpectValidationError(true);
      
      this.setGshStemName("test");
    }

    @Override
    public void gshCheckResult() {
      // make sure description didnt change
      Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "test:wiki", true);
      assertEquals(stem.getDescription(), "desc");
      // validation error
      this.assertGshValidationContainsLine("gsh_input_appName", String.format(MyGshTemplate.errorFolderExists, stem.getName()));
    }

    protected void tearDown() {
      // delete the folder
      new StemSave().assignName("test:wiki").assignSaveMode(SaveMode.DELETE).save();
    }
      
  }

  public class TestBuiltinValidationExpectFailure extends GshTemplateV2test {
    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wiki&");
      this.addGshInput("gsh_input_input2", "some string");
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
            
      this.setGshStemName("test");
    }

    @Override
    public void gshCheckResult() {
      // validation error
      this.assertGshValidationContainsLine("gsh_input_appName", String.format(MyGshTemplate.errorFolderExists, "abc"));
    }

    protected void tearDown() {
    }
      
  }

  public class TestBuiltinValidationExpectSuccess extends GshTemplateV2test {
    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wiki&");
      this.addGshInput("gsh_input_input2", "some string");
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
      
      this.setGshExpectValidationError(true);
      
      this.setGshStemName("test");
    }

    @Override
    public void gshCheckResult() {
      // validation error
      this.assertGshValidationContainsLine("gsh_input_appName", String.format(MyGshTemplate.errorFolderExists, "abc"));
    }

    protected void tearDown() {
    }
      
  }

  public class TestNotExistingDoesWork extends GshTemplateV2test {
    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wiki");
      this.addGshInput("gsh_input_input2", "some string");
      // stem exists
      new StemSave().assignName("test:wiki").assignSaveMode(SaveMode.DELETE).save();
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
            
      this.setGshStemName("test");
      
      this.setGshShowOutputLinesOfTest(true);
    }

    @Override
    public void gshCheckResult() {
      // make sure description didnt change
      Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "test:wiki", true);
      assertEquals(stem.getDescription(), String.format(MyGshTemplate.descriptionFormat, "some string"));
      // output
      this.assertGshOutputContainsLine(MyGshTemplate.success);
    }

    protected void tearDown() {
      // delete the folder
      new StemSave().assignName("test:wiki").assignSaveMode(SaveMode.DELETE).save();
    }
      
  }
  
  public class TestNotExistingExpectFailure extends GshTemplateV2test {
    protected void setUp() {
      
      this.setGshTemplateConfigId("myTemplate");
      
      // inputs from screen
      this.addGshInput("gsh_input_appName", "wiki");
      this.addGshInput("gsh_input_input2", "some string");
      // stem exists
      new StemSave().assignName("test:wiki").assignSaveMode(SaveMode.DELETE).save();
      
      this.assignGshSubjectUsingApp("jdbc", "test.subject.1");
            
      this.setGshStemName("test");
    }

    @Override
    public void gshCheckResult() {
      // make sure description didnt change
      Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "test:wiki", true);
      assertEquals(stem.getDescription(), String.format(MyGshTemplate.descriptionFormat, "some string expect this wrong!!!"));
      // output
      this.assertGshOutputContainsLine(MyGshTemplate.success);
    }

    protected void tearDown() {
      // delete the folder
      new StemSave().assignName("test:wiki").assignSaveMode(SaveMode.DELETE).save();
    }
      
  }
  
   // register the template with grouper.  Note this is only needed before v4.10.0...
   gsh_builtin_gshTemplateRuntime.assignGshTemplateV2(new MyGshTemplate());
  
//  public static void main(String[] args) {
//
//    MyGshTemplate myGshTemplate = new Test65testTemplate().new MyGshTemplate();
//    GshTemplateExecTestOutput gshTemplateExecTestOutput = GshTemplateV2utils.gshRunTest(myGshTemplate, "testExistingDoesntWork");
//    System.out.println(gshTemplateExecTestOutput.toString());
//    
//  }
//  
//  
//}

```

## Run the tests

## Run a template from a main

```
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Subject subject = SubjectFinder.findByIdAndSource("10021368", "pennperson", true);

        Test67listservRequestCreate test67listservRequestCreate = new Test67listservRequestCreate();
        GshTemplateV2input gshTemplateV2input = new GshTemplateV2input();
        gshTemplateV2input.setGsh_builtin_subject(subject);
        GshTemplateRuntime gshTemplateRuntime = new GshTemplateRuntime();
        gshTemplateRuntime.setTemplateConfigId("pennNetMailingListRequestCreate");
        gshTemplateV2input.setGsh_builtin_gshTemplateRuntime(gshTemplateRuntime);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_action", "requestList");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_listName", "astt_idm");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_existingList", true);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_description", "Used in ASTT for registration of cloud services");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_requestedBy", "mchyzer");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_ownerListRequirement", "iscEmployee");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_sendersListRequirement", "iscEmployee");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_membersListRequirement", "iscEmployee");
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_automaticOrgs", null);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_automaticGroupNames", null);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_canOptout", false);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_optinsGroupName", null);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_managersGroupName", null);
        gshTemplateV2input.getGsh_builtin_inputs().put("gsh_input_enabled", false);
        

        GshTemplateV2output gshTemplateV2output = new GshTemplateV2output();
        
        test67listservRequestCreate.gshRunLogic(gshTemplateV2input, gshTemplateV2output);
        
        return null;
      }
    });
    System.exit(0);
 
  }

```

## Run a test from a main

```
  public static void main(String[] args) {

    Test68listservTeam myGshTemplate = new Test68listservTeam();
    GshTemplateExecTestOutput gshTemplateExecTestOutput = GshTemplateV2utils.gshRunTest(myGshTemplate, "testApproveRequest");
    System.out.println(gshTemplateExecTestOutput.toString());
    System.exit(0);
  }

```
