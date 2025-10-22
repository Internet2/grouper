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

  @Override
  public void groupViewLogic(GroupViewLogicInput groupViewLogicInput) {
    // count the number of stem
    int myStemCount = new GcDbAccess().sql("select count(1) from grouper_stems").select(int.class);
    
    // use a name that is unlikely to collide with others
    groupViewLogicInput.getRequest().setAttribute("myStemCount", myStemCount);
  }


}
