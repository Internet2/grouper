package edu.internet2.middleware.grouper.app.browser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionableGroupSave;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.testing.GrouperTestBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperUiBrowserTest extends GrouperTestBase {

  public GrouperUiBrowserTest() {
    super();
  }

  /**
   * We can't delete the grouper database or the ui will get confused, so this does not extend GrouperTest.
   */
  public GrouperUiBrowserTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    new GrouperUiBrowserTest().testMembershipFinder();
    new GrouperUiBrowserTest().testDaemonErrors();
    new GrouperUiBrowserTest().testGroupCreate();
    new GrouperUiBrowserTest().testGroupDelete();
    new GrouperUiBrowserTest().testGroupEditWithChangedId();
    new GrouperUiBrowserTest().testGroupEditWithTheSameId();
    new GrouperUiBrowserTest().testGroupFinder();
    new GrouperUiBrowserTest().testMembershipAdd();
    new GrouperUiBrowserTest().testMembershipRemove();
    new GrouperUiBrowserTest().testVersion();
    new GrouperUiBrowserTest().testGshTemplateRunInMisc();
    new GrouperUiBrowserTest().testGshTemplateRunInStem();
    new GrouperUiBrowserTest().testGshTemplateRunInGroup();
    new GrouperUiBrowserTest().testCustomUiView();
    new GrouperUiBrowserTest().testProvisioningAssignGroup();
    new GrouperUiBrowserTest().testProvisioningRemoveGroup();
    System.exit(0);
  }

  public void cleanUpBeforeTest() {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Stem stem = StemFinder.findByName("test", false);
        if (stem == null) {
          stem = new StemSave().assignName("test").save();
        }
        stem.deleteGroups(false, false, Scope.ONE);
        return null;
      }
    });
    GrouperUtil.sleep(10000);
  }

  /**
   * We can't delete the grouper database or the ui will get confused, so this is not a real junit test.
   */
  public void testVersion() {
    GrouperPage grouperPage = new GrouperPage();
    try {
      grouperPage = grouperPage.initializePage();
      grouperPage.getPage().navigate("http://GrouperSystem:pass@localhost:8080/grouper");
      GrouperUiBrowserGeneralVerifyVersion grouperUiBrowserGeneralVerifyVersion = new GrouperUiBrowserGeneralVerifyVersion(
          grouperPage).browse();
      // get the current ui version
      String uiVersion = grouperUiBrowserGeneralVerifyVersion.getUiVersion().toString();

      // Confirm the current ui version
      new GrouperUiBrowserGeneralVerifyVersion(grouperPage).assignExpectedVersion("4.0.0")
          .browse();

      try {
        new GrouperUiBrowserGeneralVerifyVersion(grouperPage)
            .assignExpectedVersion("1.2.3")
            .browse();
        throw new RuntimeException("This should fail because it is the wrong version");
      } catch (Exception e) {
        // This is good
      }

    } finally {

      grouperPage.close();
    }
  }

  public void testDaemonErrors() {
    cleanUpBeforeTest();
    GrouperPage grouperPage = new GrouperPage();
    try {
      grouperPage.initializePage();
      grouperPage.getPage().navigate("http://GrouperSystem:pass@localhost:8080/grouper");
      GrouperUiBrowserDaemonViewErrors grouperUiBrowserDaemonViewErrors = null;

      grouperUiBrowserDaemonViewErrors = new GrouperUiBrowserDaemonViewErrors(grouperPage)
          .browse();
      // get the errors
      List<String> errors = grouperUiBrowserDaemonViewErrors
          .getGrouperUiBrowserDaemonErrors();
    } finally {
      grouperPage.close();
    }
  }

  public void testGroupCreate() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        new StemSave().assignName("test").save();
        new GroupSave().assignName("test:testGroup").assignSaveMode(SaveMode.DELETE)
            .save();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserGroupCreate grouperUiBrowserGroupCreate = new GrouperUiBrowserGroupCreate(
              grouperPage)
                  .assignGroupDisplayExtension("testGroupDisplay")
                  .assignGroupExtension("test").assignStemName("test")
                  .assignDescription("test group").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });
  }

  public void testGroupFinder() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        Group group = new GroupSave().assignName("test:test")
            .assignCreateParentStemsIfNotExist(true).save();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserGroupFinder grouperUiBrowserGroupFinder = new GrouperUiBrowserGroupFinder(
              grouperPage).assignGroupToFindName("test:test").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });

  }

  public void testGroupDelete() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        Group group = new GroupSave().assignName("test:test22")
            .assignDescription("initial description")
            .assignCreateParentStemsIfNotExist(true).save();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserGroupDelete grouperUiBrowserGroupDelete = new GrouperUiBrowserGroupDelete(
              grouperPage).assignGroupToDeleteName("test:test22").browse();
        } finally {
          grouperPage.close();
        }
        group = GroupFinder.findByUuid(group.getId(), false);
        assertNull(group);
        return null;
      }
    });

  }

  public void testGroupEditWithTheSameId() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        Group group = new GroupSave().assignName("test:test22")
            .assignDescription("initial description")
            .assignCreateParentStemsIfNotExist(true).save();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserGroupEdit grouperUiBrowserGroupEdit = new GrouperUiBrowserGroupEdit(
              grouperPage).assignGroupToEditName("test:test22")
                  .assignDescription("this is the edited description").browse();
        } finally {
          grouperPage.close();
        }
        group = GroupFinder.findByUuid(group.getId(), true);
        assertEquals("test22", group.getDisplayExtension());
        assertEquals("this is the edited description", group.getDescription());
        assertTrue(StringUtils.isBlank(group.getAlternateName()));
        return null;
      }
    });

  }

  public void testGroupEditWithChangedId() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        Group group = new GroupSave().assignName("test:test22")
            .assignDescription("initial description")
            .assignCreateParentStemsIfNotExist(true).save();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserGroupEdit grouperUiBrowserGroupEdit = new GrouperUiBrowserGroupEdit(
              grouperPage).assignGroupToEditName("test:test22")
                  .assignGroupExtension("testeditedagain")
                  .assignDescription("this is the edited description").browse();
        } finally {
          grouperPage.close();
        }
        group = GroupFinder.findByUuid(group.getId(), true);
        assertEquals("testeditedagain", group.getExtension());
        assertEquals("test22", group.getDisplayExtension());
        assertEquals("this is the edited description", group.getDescription());
        assertTrue(StringUtils.isBlank(group.getAlternateName()));
        assertEquals("test:testeditedagain", group.getName());
        return null;
      }
    });

  }

  public void testMembershipFinder() {

    cleanUpBeforeTest();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        Group group = new GroupSave().assignName("test:test")
            .assignCreateParentStemsIfNotExist(true).save();
        Subject subject = SubjectFinder.findById("test.subject.5", true);
        group.addMember(subject, false);
        for (int i = 0; i < 102; i++) {
          Group newgroup = new GroupSave().assignName("test:" + i + "test_subject_5")
              .assignDisplayExtension(i + "test.subject.5")
              .assignCreateParentStemsIfNotExist(true).save();
          group.addMember(newgroup.toSubject());
        }

        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserMembershipFinder grouperUiBrowserMembershipFinder = new GrouperUiBrowserMembershipFinder(
              grouperPage).assignGroupToLookInName("test:test")
                  .assignSubjectId("test.subject.5").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });

  }

  public void testMembershipRemove() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        Group group = new GroupSave().assignName("test:test")
            .assignCreateParentStemsIfNotExist(true).save();
        group.deleteAllMemberships();
        Subject subject = SubjectFinder.findById("test.subject.5", true);
        group.addMember(subject);
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserMembershipRemove grouperUiBrowserMembershipRemove = new GrouperUiBrowserMembershipRemove(
              grouperPage).assignGroupToRemoveFromName("test:test")
                  .assignSubject(subject).browse();
        } finally {
          grouperPage.close();
        }
        group = GroupFinder.findByUuid(group.getId(), true);
        assertEquals(group.getMembers().size(), 0);
        return null;
      }
    });
  }

  public void testMembershipAdd() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        Group group = new GroupSave().assignName("test:test")
            .assignCreateParentStemsIfNotExist(true).save();
        group.deleteAllMemberships();
        Subject subject = SubjectFinder.findById("test.subject.5", true);
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserMembershipAdd grouperUiBrowserMembershipAdd = new GrouperUiBrowserMembershipAdd(
              grouperPage).assignGroupToAddToName("test:test")
                  .assignSubject(subject).browse();
        } finally {
          grouperPage.close();
        }
        group = GroupFinder.findByUuid(group.getId(), true);
        return null;
      }
    });

  }

  public void testGshTemplateRunInMisc() {
    
    
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
            
        configureTestGshTemplate();
        
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserTemplateRun grouperUiBrowserTemplateRun = new GrouperUiBrowserTemplateRun(
              grouperPage).assignGshTemplateConfigId("validateGrouper")
                  .assignSecondsToWait(20)
                  .addInputValue("gsh_input_expectedVersion", "1.2.3")
                  .addInputValue("gsh_input_textarea", "textAreaInput")
                  .addInputValue("gsh_input_dropdown", "first")
                  .addInputValue("gsh_input_password", "passwordInput").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });

  }

  public void testGshTemplateRunInGroup() {
    
    

    
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        configureTestGshTemplate();
        Group group = new GroupSave().assignName("test:test")
            .assignCreateParentStemsIfNotExist(true).save();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserTemplateRun grouperUiBrowserTemplateRun = new GrouperUiBrowserTemplateRun(
              grouperPage).assignGroupToExecuteIn(group)
                  .assignGshTemplateConfigId("validateGrouper")
                  .addInputValue("gsh_input_expectedVersion", "1.2.3").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });

  }

  public void testGshTemplateRunInStem() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        configureTestGshTemplate();
        
        new StemSave().assignName("test").save();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserTemplateRun grouperUiBrowserTemplateRun = new GrouperUiBrowserTemplateRun(
              grouperPage).assignStemToExecuteInName("test")
                  .assignGshTemplateConfigId("validateGrouper")
                  .addInputValue("gsh_input_expectedVersion", "1.2.3").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });

  }

  public void testCustomUiView() {
    cleanUpBeforeTest();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {

        configureTestCustomUi();

        new GroupSave().assignName("test:test")
            .assignCreateParentStemsIfNotExist(true).save();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserCustomUiView grouperUiBrowserCustomUiView = new GrouperUiBrowserCustomUiView(
              grouperPage).assignCustomUiConfigId("atlassianJiraClaimLicense").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });

  }

  public void testProvisioningAssignGroup() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        
        configureTestProvisioner();
        
        Group group = new GroupSave().assignName("test:test")
            .assignCreateParentStemsIfNotExist(true).save();
        group.deleteAllMemberships();
        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserProvisioningAssignGroup grouperUiBrowserProvisioningAssignGroup = new GrouperUiBrowserProvisioningAssignGroup(
              grouperPage).assignGroupToAssign(group)
                  .assignProvisionerName("myProvisioner").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });

  }

  public void testProvisioningRemoveGroup() {
    cleanUpBeforeTest();
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        //        For provisioner to work, you need this table:
        //         CREATE TABLE public.provision_groups (
        //        id varchar NULL,
        //        "name" varchar NULL,
        //        CONSTRAINT provision_groups_unique UNIQUE (id));
            
        configureTestProvisioner();
        
        Group group = new GroupSave().assignName("test:test")
            .assignCreateParentStemsIfNotExist(true).save();
        group.deleteAllMemberships();

        new ProvisionableGroupSave().assignTargetName("myProvisioner").assignGroup(group)
            .save();

        GrouperPage grouperPage = new GrouperPage();
        try {
          grouperPage.initializePage();
          grouperPage.getPage()
              .navigate("http://GrouperSystem:pass@localhost:8080/grouper");
          GrouperUiBrowserProvisioningRemoveGroup grouperUiBrowserProvisioningRemoveGroup = new GrouperUiBrowserProvisioningRemoveGroup(
              grouperPage).assignGroupToRemove(group)
                  .assignProvisionerName("myProvisioner").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });

  }

  public void configureTestGshTemplate() {
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.defaultRunButtonFolderUuidOrName").value("etc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.displayErrorOutput").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.folderShowType").value("allFolders").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.groupShowType").value("allGroups").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.0.description").value("Enter the version that you are upgrading to").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.0.label").value("Expected version").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.0.name").value("gsh_input_expectedVersion").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.0.validationType").value("none").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.1.description").value("abc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.1.formElementType").value("textarea").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.1.label").value("text field").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.1.name").value("gsh_input_textarea").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.1.type").value("string").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.1.validationType").value("none").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.2.description").value("abc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.2.dropdownCsvValue").value("first, second, third").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.2.formElementType").value("dropdown").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.2.label").value("dropdown").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.2.name").value("gsh_input_dropdown").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.3.description").value("abc").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.3.formElementType").value("password").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.3.label").value("password").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.3.name").value("gsh_input_password").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.input.3.validationType").value("none").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.moreActionsLabel").value("Validate upgrade").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.numberOfInputs").value("4").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.runAsType").value("GrouperSystem").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.runButtonGroupOrFolder").value("folder").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.securityRunType").value("wheel").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.showInMoreActions").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.showOnFolders").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.showOnGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.templateDescription").value("Validate upgrade").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.templateName").value("Validate upgrade").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.templateVersion").value("V2").store();
    
    // This is the source of the template which you can compile in the default java package for debugging purposes.
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouperGshTemplate.validateGrouper.gshTemplate").value("""
        import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.browser.GrouperPage;
import edu.internet2.middleware.grouper.app.browser.GrouperUiBrowserGeneralVerifyVersion;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;

public class Test1ValidateUpgrade extends GshTemplateV2 {

 @Override
 public void gshRunLogic(GshTemplateV2input gshTemplateV2input,
   GshTemplateV2output gshTemplateV2output) {
  gshTemplateV2output.getGsh_builtin_gshTemplateOutput()
    .assignRedirectToGrouperOperation("NONE");
  String expectedVersion = gshTemplateV2input
    .getGsh_builtin_inputString("gsh_input_expectedVersion");
  String textAreaValue = gshTemplateV2input
    .getGsh_builtin_inputString("gsh_input_textarea");
  String dropdown = gshTemplateV2input
    .getGsh_builtin_inputString("gsh_input_dropdown");
  String password = gshTemplateV2input
    .getGsh_builtin_inputString("gsh_input_password");
  gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("error", 
    "Text area value: " + textAreaValue);
  gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("info", 
    "Expected version value: " + expectedVersion);
  gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("info", 
    "Dropdown value: " + dropdown);
  gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("Passswords value: " + password);
  
  GrouperPage grouperPage = new GrouperPage();
  try {
   grouperPage.initializePage();
   grouperPage.getPage().navigate("http://GrouperSystem:pass@localhost:8080/grouper");
   GrouperUiBrowserGeneralVerifyVersion grouperUiBrowserGeneralVerifyVersion = new GrouperUiBrowserGeneralVerifyVersion(
      grouperPage).browse();
   if (StringUtils.isBlank(expectedVersion)) {
    gshTemplateV2output.getGsh_builtin_gshTemplateOutput()
        .addOutputLine("The grouper version is: " +
            grouperUiBrowserGeneralVerifyVersion.getUiVersion());
   } else {
    if (StringUtils.equals(
        grouperUiBrowserGeneralVerifyVersion.getUiVersion().toString(),
        expectedVersion)) {
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput()
          .addOutputLine("Success: expected the correct version: " +
              grouperUiBrowserGeneralVerifyVersion.getUiVersion());
    } else {
      gshTemplateV2output.getGsh_builtin_gshTemplateOutput()
          .addOutputLine("Error: expect version '" + expectedVersion
              + "', but the grouper version is: '"
              + grouperUiBrowserGeneralVerifyVersion.getUiVersion() + "'");
    }
   }
  } finally {
   grouperPage.close();
  }
 }

}

        """).store();
  }

  public void configureTestProvisioner() {
    //        For provisioner to work, you need this table:
    //         CREATE TABLE public.provision_groups (
    //        id varchar NULL,
    //        "name" varchar NULL,
    //        CONSTRAINT provision_groups_unique UNIQUE (id));
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myProvisioner.class").value("edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myProvisioner.provisionerConfigId").value("myProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myProvisioner.publisher.class").value("edu.internet2.middleware.grouper.app.provisioning.ProvisioningConsumer").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myProvisioner.publisher.debug").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("changeLog.consumer.provisioner_incremental_myProvisioner.quartzCron").value("0 * * * * ?").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myProvisioner.class").value("edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningFullSyncJob").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myProvisioner.provisionerConfigId").value("myProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("otherJob.provisioner_full_myProvisioner.quartzCron").value("0 59 7 * * ?").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.addDisabledFullSyncDaemon").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.addDisabledIncrementalSyncDaemon").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.class").value("edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupAttributeValueCache0groupAttribute").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupAttributeValueCache0has").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupAttributeValueCache0source").value("target").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupAttributeValueCache0type").value("groupAttribute").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupAttributeValueCacheHas").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupMatchingAttribute0name").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupMatchingAttributeCount").value("1").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupTableIdColumn").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.groupTableName").value("provision_groups").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.hasTargetGroupLink").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.numberOfGroupAttributes").value("2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.showAdvanced").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.startWith").value("this is start with read only").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.targetGroupAttribute.0.name").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("id").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.targetGroupAttribute.1.name").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.targetGroupAttribute.1.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.myProvisioner.targetGroupAttribute.1.translateFromGrouperProvisioningGroupField").value("name").store();
  }

  public void configureTestCustomUi() {
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.index")
        .value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.textBoolean")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.textIsScript")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.0.textType")
        .value("canSeeScreenState").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.defaultText")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.index")
        .value("10").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.text")
        .value("<h1>ISC Jira account analysis</h1>").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.1.textType")
        .value("header").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.index")
        .value("30").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.script")
        .value("\u0024{true}").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.text")
        .value(
            "<font style=\"color\u003Abrown\"><b>Error\u003A</b></font> You are not an active Penn affiliate and need to ask your BA to check employment records")
        .store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.10.textType")
        .value("enrollmentLabel").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.defaultText")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.index")
        .value("10").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.text")
        .value("Claim Jira license").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.11.textType")
        .value("enrollButtonText").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.index")
        .value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.textBoolean")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.textIsScript")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.2.textType")
        .value("canSeeUserEnvironment").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.index")
        .value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName(
        "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.textBooleanScript")
        .value("\u0024{true}").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.textIsScript")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.3.textType")
        .value("canAssignVariables").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.index")
        .value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.text")
        .value("null").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.4.textType")
        .value("helpLink").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.defaultText")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.index")
        .value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.textBoolean")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.textIsScript")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.5.textType")
        .value("manageMembership").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.defaultText")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.endIfMatches")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.index")
        .value("-20").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.script")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.text")
        .value(
            "This page displays your account status for ISC cloud Jira.  If you do not have a license and are eligible to claim one, you can do that here.<br /><br /><ul>")
        .store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.6.textType")
        .value("instructions1").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.defaultText")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.index")
        .value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.textBoolean")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.textIsScript")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.7.textType")
        .value("unenrollButtonShow").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.defaultText")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.endIfMatches")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.index")
        .value("10").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.script")
        .value("\u0024{ true }").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.text")
        .value(
            "<li><b>Jira license provisioned\u003A</b> yes. Your Jira license was provisioned more than 5 minutes ago and is ready to use</li>")
        .store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.8.textType")
        .value("instructions1").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.defaultText")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.enabled")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.endIfMatches")
        .value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.index")
        .value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.textBoolean")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName(
        "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.textBooleanScript")
        .value("\u0024{true}").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.textIsScript")
        .value("false").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName(
            "grouperCustomUI.atlassianJiraClaimLicense.cuTextConfig.9.textType")
        .value("enrollButtonShow").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperCustomUI.atlassianJiraClaimLicense.groupUUIDOrName")
        .value("test\u003Atest").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperCustomUI.atlassianJiraClaimLicense.numberOfQueries")
        .value("0").store();
    new GrouperDbConfig().configFileName("grouper.properties")
        .propertyName("grouperCustomUI.atlassianJiraClaimLicense.numberOfTextConfigs")
        .value("12").store();
  }

}
