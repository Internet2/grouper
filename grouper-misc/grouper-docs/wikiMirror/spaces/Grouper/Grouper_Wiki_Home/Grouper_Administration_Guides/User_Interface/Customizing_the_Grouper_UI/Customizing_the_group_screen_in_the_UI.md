---
title: "Customizing the group screen in the UI"
space: Grouper
pageId: 28548858
version: 3
lastUpdated: 2026-07-01T05:43:21.486Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548858/Customizing+the+group+screen+in+the+UI
---

This is a new feature in Grouper v5.21.4+

If you would like to customize the group screen in the UI, you can do that with Java and a JSP.

Extend the class: edu.internet2.middleware.grouper.ui.customizeUi.JavaUiCustomizer

Override the method: public void indexMainLogic(IndexMainLogicInput indexMainLogicInput)

Put that classfile in a jar in WEB-INF/lib in your container or in a class file in WEB-INF/classes. Note, you can put your class in any package and it can have any name, do not use this package and name.

```
package your.package;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class YourClassName extends JavaUiCustomizer {

  @Override
  public void groupViewLogic(GroupViewLogicInput groupViewLogicInput) {
    // count the number of stem
    int myStemCount = new GcDbAccess().sql("select count(1) from grouper_stems").select(int.class);
    
    // use a name that is unlikely to collide with others
    groupViewLogicInput.getRequest().setAttribute("myStemCount", myStemCount);
  }

}

```

Register the classfile in grouper-ui.properties:

```
uiV2.javaUiCustomizer.class = your.package.YourClassName
```

Override the custom JSP in your container: WEB-INF/grouperUi2/group/groupSummaryCustom.jsp

```
<%-- put this at the top of all jsp's --%>
<%@ include file="../assetsJsp/commonTaglib.jsp"%>

        <tr>
            <td style="vertical-align: top"><strong>Registry stems count</strong></td>
            <td style="padding-left: 0px;">
              ${myStemCount}
            </td>
          </tr>
```
