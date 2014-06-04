package edu.internet2.middleware.grouper.poc;


public class AutoCreateObjects {

  public AutoCreateObjects() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    
    System.out.println("grouperSession = GrouperSession.startRootSession();");
    
    for (int i=0;i<39;i++) {
      System.out.println("new StemSave(grouperSession).assignName(\"test2:testStem" + i + "\").assignCreateParentStemsIfNotExist(true).save();");
    }

    for (int i=0;i<139;i++) {
      System.out.println("group = new GroupSave(grouperSession).assignName(\"test2:testGroup" + i + "\").assignCreateParentStemsIfNotExist(true).save();");
    }

    System.out.println("attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.attr).assignName(\"test2:testAttrName\").assignToStem(true).save();");

    for (int i=0;i<39;i++) {
      System.out.println("new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.attr).assignName(\"test2:testAttrName" + i + "\").assignToStem(true).save();");
    }

    for (int i=0;i<39;i++) {
      System.out.println("new AttributeDefNameSave(grouperSession, attributeDef).assignCreateParentStemsIfNotExist(true).assignName(\"test2:testAttrDefName" + i + "\").save();");
    }
  }

}
