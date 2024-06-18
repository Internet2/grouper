package edu.internet2.middleware.grouper.app.browser;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.mysql.cj.exceptions.AssertionFailedException;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import junit.textui.TestRunner;

public class GrouperUiBrowserTest extends GrouperTest {

  public GrouperUiBrowserTest() {
    super();
  }

  public GrouperUiBrowserTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    new GrouperUiBrowserTest().runTestGroupFinder();
    System.exit(0);
  }

  /**
   * We can't delete the grouper database or the ui will get confused, so this is not a real junit test.
   */
  public void runTestVersion() {
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

  public void runTestDaemonErrors() {
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

  public void runTestGroupCreate() {
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
          GrouperUiBrowserGroupCreate grouperUiBrowserGroupCreate = null;

          grouperUiBrowserGroupCreate = new GrouperUiBrowserGroupCreate(grouperPage)
              .assignGroupDisplayExtension("testGroupDisplay")
              .assignGroupExtension("testGroup").assignStemName("test")
              .assignDescription("test group").browse();
        } finally {
          grouperPage.close();
        }
        return null;
      }
    });
  }

  public void runTestGroupFinder() {
    GrouperPage grouperPage = new GrouperPage();
    try {
      grouperPage.initializePage();
      grouperPage.getPage().navigate("http://GrouperSystem:pass@localhost:8080/grouper");
      GrouperUiBrowserGroupFinder grouperUiBrowserGroupFinder = new GrouperUiBrowserGroupFinder(
          grouperPage).assignGroupToFindName("etc:test").browse();
    } finally {
      grouperPage.close();
    }
  }
}
