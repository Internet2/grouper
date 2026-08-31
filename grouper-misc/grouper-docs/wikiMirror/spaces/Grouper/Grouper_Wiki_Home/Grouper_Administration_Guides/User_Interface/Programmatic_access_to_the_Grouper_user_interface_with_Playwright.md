---
title: "Programmatic access to the Grouper user interface with Playwright"
space: Grouper
pageId: 28545497
version: 20
lastUpdated: 2026-07-01T05:47:08.918Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545497/Programmatic+access+to+the+Grouper+user+interface+with+Playwright
---

In v4.14+ Grouper has better attributes in the UI HTML DOM to navigate programmatically. The playwright library will be available in the container by setting an environment variable

Playwright will be an option to run inside of Grouper to make scripts that can validate the UI.

Methods will be available to perform certain actions in the UI in a way that is easy to script and will survive upgrades.

These methods are idempotent, so if the state is already set, then it is still a success. If there is a problem then

[Demo movie 1](https://www.youtube.com/watch?v=u_g-zQXelWk) (new), [Demo movie 2](https://youtu.be/HeK1pMYEMSE) (previous)

[Use the Playwright recorder to simulate a log in or other purposes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549074/Using+the+Playwright+script+recorder)

| Category | Action | Class |
| --- | --- | --- |
| Daemon | View errors | [GrouperUiBrowserDaemonViewErrors](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549572/Programmatic+access+to+Grouper+-+view+daemon+errors) |
| General | Verify version | [GrouperUiBrowserGeneralVerifyVersion](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549586/Programmatic+access+to+Grouper+-+verify+version) |
| Group | Find group | [GrouperUiBrowserGroupFinder](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549598/Programmatic+access+to+Grouper+-+find+group) |
| Create group | [GrouperUiBrowserGroupCreate](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549592/Programmatic+access+to+Grouper+-+create+a+group) |
| Edit group | [GrouperUiBrowserGroupEdit](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549631/Programmatic+access+to+Grouper+-+edit+a+group) |
| Delete group | [GrouperUiBrowserGroupDelete](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549604/Programmatic+access+to+Grouper+-+delete+a+group) |
| GSH template | Run template | [GrouperUiBrowserTemplateRun](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549660/Programmatic+access+to+Grouper+-+run+gsh+template) |
| Custom UI | View custom UI | [GrouperUiBrowserCustomUiView](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549637/Programmatic+access+to+Grouper+-+view+custom+ui) |
| Membership | Find membership | [GrouperUiBrowserMembershipFinder](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549666/Programmatic+access+to+Grouper+-+find+membership) |
| Add membership | [GrouperUiBrowserMembershipAdd](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549619/Programmatic+access+to+Grouper+-+add+membership) |
| Remove membership | [GrouperUiBrowserMembershipRemove](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549625/Programmatic+access+to+Grouper+-+remove+membership) |
| Provisioning | Assign group provisionable | [GrouperUiBrowserProvisioningAssignGroup](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549654/Programmatic+access+to+Grouper+-+assign+group+provisioner) |
| Remove group provisionable | [GrouperUiBrowserProvisioningRemoveGroup](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549672/Programmatic+access+to+Grouper+-+remove+group+provisioner) |

## DOM attributes and standards

1. For these methods there will be strong HTML attributes to use for CSS selectors
2. Selectors are unique in a page
3. Externalized text will not be used
4. Prefer to use the "id" of the HTML element
5. Custom grouper attributes are added to find html elements. (start with "data-gr-" )
  
  
  ```
  <a href="" data-gr-browse-folder-name="some:folder:name">...</a>
  ```
6. Full paths will be used
7. Display names will not be used
8. Values will be escaped
9. Methods will start by navigating to the main page
10. Each click will validate that the UI changed
11. Hidden HTML elements can be used
12. These operations will run as GrouperSystem

## Using these methods

1. Setup some Playwright objects
  
  
  ```
  import com.microsoft.playwright.Browser;
  import com.microsoft.playwright.BrowserType;
  import com.microsoft.playwright.Page;
  import com.microsoft.playwright.Playwright;
  import com.microsoft.playwright.options.AriaRole;
  import edu.internet2.middleware.grouper.app.browser.*;
  
      Playwright playwright = Playwright.create();
      BrowserType chromium = playwright.chromium();
      Browser browser = chromium.launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      GrouperPage grouperPage = new GrouperPage();
      grouperPage.setPage(page);
    
  ```
2. Navigate to the Grouper being tested
  
  
  ```
  // either use the built in config for the Grouper UI
  String uiUrl = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.ui.url");
  
  // or hard code a url or use another config
  String uiUrl = "https://grouper.institution.edu";
  
  // note this defaults to grouper.properties grouper.ui.url 
  // so you do not need to set this if testing this env
  grouperPage.assignUrl(uiUrl);
  
  // note this is the default so you probably do not need to do this
  grouperPage.assignContext("/grouper");
  
  page.navigate(grouperPage.getUrl());
  ```
3. Authenticate to the UI (e.g. with a test account). Note you need to record this with selenium (and set the language to Java), or look at the DOM of your login screens
  
  
  ```
      page.getByLabel("Username").click();
      page.getByLabel("Username").fill("someTestUserName");
      page.getByLabel("Username").press("Tab");
      page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).click();
      page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).fill(GrouperConfig.retrieveConfig().propertyValueStringRequired("someTestUserName.pass"));
      page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).press("Enter");
      
      // does your test account have duo?  accept the push
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("No, other people use this")).click();
  
  
  ```
4. Call the API methods above
  
  
  ```
  import edu.internet2.middleware.grouper.grouperUi.browser.*;
  
  // add a user to a group
  new GrouperUiBrowserMembershipAdd().assignGrouperPage(grouperPage).
     assignGroupName("a:b:c").assignSubjectId("10021368").assignSubjectSourceId("schoolPerson").browse();
  
  // make a group provisionable
  new GrouperUiBrowserProvisioningAssignGroup().assignGrouperPage(grouperPage).
     assignGroupName("a:b:c").assignProvisionerConfigId("myLdapProvisioner").browse();
  
  // etc
  ```
