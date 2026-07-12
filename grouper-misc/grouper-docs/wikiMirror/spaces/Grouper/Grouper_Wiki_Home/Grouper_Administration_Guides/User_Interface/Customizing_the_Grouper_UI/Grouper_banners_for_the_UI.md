---
title: "Grouper banners for the UI"
space: Grouper
pageId: 28548820
version: 8
lastUpdated: 2026-07-01T05:43:25.515Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548820/Grouper+banners+for+the+UI
---

This is a new feature in Grouper v5.21.4+

## Environment banner

An environment banner will show in non-prod environments of the Grouper UI to indicate that the environment is not production. This will show in the UI, the simplified GSH UI, and Grouper custom UIs.

If you maintain the environment in grouper.properties (grouper.env.name) this will show automatically.

- If grouper.env.name is blank or "prod" or "production", then the environment label will not show
- If the value is something else, then it will show by default (with the env name in all caps)
- If you do not what this to show up in non-production, set this to false in grouper-ui.properties: uiV2.show.environment.header
- To customize the text, put the value in banner.environment.header.text
  
  - You can use these variables: ${grouperRequestContainer.indexContainer.environmentNameCap}, ${grouperRequestContainer.indexContainer.environmentName}

## Announcement banner

You can have an announcement banner in the Grouper UI to announce upgrades, or for other purposes. This will show in the UI, but not in the simplified GSH UI or custom UIs.

Configure an announcement by setting this in externalized text: banner.announce.header.text

## Custom banner in the UI

You can have a custom banner if you would like to have Java logic or custom JSP code in your banner. This will show in the UI, but not in the simplified GSH UI or custom UIs.

This assumes you are comfortable writing Java and JSP code. Note: this shows in the UI in the outer div, which does not redisplay as users click through the UI. It will redisplay if they click refresh.

Extend the class: edu.internet2.middleware.grouper.ui.customizeUi.JavaUiCustomizer

Override the method: public void indexMainLogic(IndexMainLogicInput indexMainLogicInput)

Put that classfile in a jar in WEB-INF/lib in your container or in a class file in WEB-INF/classes. Note, you can put your class in any package and it can have any name, do not use this package and name.

```
package your.package;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class YourClassName extends JavaUiCustomizer {

  @Override
  public void indexMainLogic(IndexMainLogicInput indexMainLogicInput) {

    // count the number of groups
    int myGroupCount = new GcDbAccess().sql("select count(1) from grouper_groups").select(int.class);
    
    // use a name that is unlikely to collide with others
    indexMainLogicInput.getRequest().setAttribute("myGroupCount", myGroupCount);
  }

}
```

Register the classfile in grouper-ui.properties:

```
uiV2.javaUiCustomizer.class = your.package.YourClassName
```

Override the custom JSP in your container: WEB-INF/grouperUi2/assetsJsp/customHeader.jsp

```
<%-- put this at the top of all jsp's --%>
<%@ include file="../assetsJsp/commonTaglib.jsp"%>

        <div class="grouper-env-announce-outer">
          <div class="grouper-env-announce-inner">
            <span>There are ${myGroupCount} groups in the registry</span>
          </div>
        </div>

```
