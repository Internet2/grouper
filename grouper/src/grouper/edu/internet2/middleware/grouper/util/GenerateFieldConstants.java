/*
 * @author mchyzer
 * $Id: GenerateFieldConstants.java,v 1.18 2009-07-03 21:15:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.app.loader.LoaderJobBean;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;


/**
 *
 */
public class GenerateFieldConstants {

  /**
   * @param args
   */
  public static void main(String[] args) {
    generateConstants(LoaderJobBean.class);
  }
  
  /**
   * <pre>
   * generate the Java for the constant fields
   * to ignore a field, annotate with GrouperIngoreFieldConstant
   * </pre>
   * @param theClass 
   *
   */
  private static void generateConstants(Class theClass) {
    Set<String> fieldNamesSet = null;
    //sort
    List<String> fieldNames = null;
    
    StringBuilder result = new StringBuilder();
    System.out.println("  //*****  START GENERATED WITH GenerateFieldConstants.java *****//\n");
    
    fieldNamesSet = GrouperUtil.fieldNames(theClass,
        Object.class, null, false, false, false, GrouperIgnoreFieldConstant.class);
    //sort
    fieldNames = new ArrayList<String>(fieldNamesSet);
    Collections.sort(fieldNames);
    
    for (String fieldName: fieldNames) {
      String caps = "FIELD_" + GrouperUtil.oracleStandardNameFromJava(fieldName);
      
      result.append("  /** constant for field name for: " + fieldName + " */\n");
      result.append("  public static final String " + caps + " = \"" + fieldName + "\";\n\n");
    }
    System.out.print(result.toString());

    if (theClass.getAnnotation(GrouperIgnoreDbVersion.class) == null) {
      fieldNamesSet = GrouperUtil.fieldNames(theClass,
          Object.class, null, false, false, false, GrouperIgnoreDbVersion.class);
      //sort
      fieldNames = new ArrayList<String>(fieldNamesSet);
      Collections.sort(fieldNames);
      
      System.out.println("  /**");
      System.out.println("   * fields which are included in db version");
      System.out.println("   */");
      System.out.print("  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(\n      ");
      
      for (int i=0;i<fieldNames.size();i++) {
        System.out.print("FIELD_" + GrouperUtil.oracleStandardNameFromJava(fieldNames.get(i)));
        if (i!=fieldNames.size()-1) {
          System.out.print(", ");

          //put a newline every once and a while
          if ((i+1) % 4 == 0) {
            System.out.print("\n      ");
          }
        } else {
          //else end it
          System.out.println(");");
        }
      }
      System.out.print("\n");
      
    }
    
    if (theClass.getAnnotation(GrouperIgnoreClone.class) == null) {
      fieldNamesSet = GrouperUtil.fieldNames(theClass,
          Object.class, null, false, false, false, GrouperIgnoreClone.class);
      //sort
      fieldNames = new ArrayList<String>(fieldNamesSet);
      Collections.sort(fieldNames);
      
      System.out.println("  /**");
      System.out.println("   * fields which are included in clone method");
      System.out.println("   */");
      System.out.print("  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(\n      ");
      
      for (int i=0;i<fieldNames.size();i++) {
        System.out.print("FIELD_" + GrouperUtil.oracleStandardNameFromJava(fieldNames.get(i)));
        if (i!=fieldNames.size()-1) {
          System.out.print(", ");

          //put a newline every once and a while
          if ((i+1) % 4 == 0) {
            System.out.print("\n      ");
          }
        } else {
          //else end it
          System.out.println(");");
        }
      }
      System.out.print("\n");
      
    }
    
    System.out.println("  //*****  END GENERATED WITH GenerateFieldConstants.java *****//");
  }

}
