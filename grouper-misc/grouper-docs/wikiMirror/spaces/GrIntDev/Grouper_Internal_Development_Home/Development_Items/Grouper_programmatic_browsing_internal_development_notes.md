---
title: "Grouper programmatic browsing internal development notes"
space: GrIntDev
pageId: 48792609
version: 5
lastUpdated: 2026-07-12T17:02:39.214Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792609/Grouper+programmatic+browsing+internal+development+notes
---

## Checking Grouper version

1. Look at [wiki standards](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792966/Grouper+style+guide)
2. Look at [existing GSH APIs for style](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh)
3. See the [user facing wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545497/Programmatic+access+to+the+Grouper+user+interface+with+Playwright)
4. Read [this about builder pattern](https://www.digitalocean.com/community/tutorials/builder-design-pattern-in-java)
  
  1. Note we dont use setters with builder pattern since it breaks [the Javabean contract](https://www.geeksforgeeks.org/javabean-class-java/), so we use assignWhatever(...
5. Read this about [CSS selectors](https://www.w3schools.com/cssref/css_selectors.php), might want to watch a CSS selector vid: [https://www.youtube.com/watch?v=KVmeQUsvbiQ](https://www.youtube.com/watch?v=KVmeQUsvbiQ)
6. Make this package in the grouper project: edu.internet2.middleware.grouper.app.browser
7. Make GrouperPage class which has a playwright member field Page object, with assigner and getter
  
  
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
  ```
  
  
  
  
  
  1. Has a method to navigate to grouper home: void navigateToGrouperHome()
  2. Validate that the home page has some element
  3. Make constructor with Page as param
8. Make an abstract class: GrouperUiBrowser
  
  1. Has field of GrouperPage with assigner and getter
9. Make this class: GrouperUiBrowserGeneralVerifyVersion extends the abstract
  
  1. Add a field: expectedVersion with assigner and getter
  2. Add a field: uiVersion with getter only
  3. After browse() method, if there is an expected version, then compare and if different then throw descriptive exception
  4. After browse() method, set the uiVersion field to what was in the UI
10. See the GSH existing APIs above where it allows various types of inputs (e.g. a Stem, a stemName, a stemUuid)
11. Assume these APIs will run as GrouperSystem and do not need a session
12. Put an "id" attribute on Miscellaneous, might as well add to the other 5 links under quick links. Use camelcase with lower case first letter as standard. Might want to do something unique, e.g. "leftMenuMiscellaneousLink"
13. Put an "id" attribute on Configure, might as well add to the other dozens of links in Miscellaneous and Administration. Use camelcase with lower case first letter as standard, e.g. "mainPanelConfigurationLink"
14. Put a span tag with an "id" around the version in the UI and read that from Playwright. e.g. "configureHeaderVersion"
15. Put Javadoc on the class, fields, all the getters and assigners/setters
16. Javadoc on class should have example calls (one with expected version one not), similar to the GSH api methods
17. Make a wiki under [this wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545497/Programmatic+access+to+the+Grouper+user+interface+with+Playwright)
  
  1. Copy the example from the javadoc and paste into the wiki, just like the other API methods
18. Link that new wiki to the [action here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545497/Programmatic+access+to+the+Grouper+user+interface+with+Playwright)
