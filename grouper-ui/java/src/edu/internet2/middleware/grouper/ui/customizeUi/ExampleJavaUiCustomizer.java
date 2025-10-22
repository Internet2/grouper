package edu.internet2.middleware.grouper.ui.customizeUi;

import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class ExampleJavaUiCustomizer extends JavaUiCustomizer {

  @Override
  public void indexMainLogic(IndexMainLogicInput indexMainLogicInput) {

    // count the number of groups
    int myGroupCount = new GcDbAccess().sql("select count(1) from grouper_groups").select(int.class);
    
    // use a name that is unlikely to collide with others
    indexMainLogicInput.getRequest().setAttribute("myGroupCount", myGroupCount);
  }


}
