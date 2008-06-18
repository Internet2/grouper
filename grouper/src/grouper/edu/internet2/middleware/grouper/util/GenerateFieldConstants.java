/*
 * @author mchyzer
 * $Id: GenerateFieldConstants.java,v 1.1.2.1 2008-06-17 17:00:23 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIngoreFieldConstant;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupDAO;


/**
 *
 */
public class GenerateFieldConstants {
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    generateConstants(Hib3GroupDAO.class);
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
    Set<String> fieldNamesSet = GrouperUtil.fieldNames(theClass,Object.class, null, false, false, false, GrouperIngoreFieldConstant.class);
    //sort
    List<String> fieldNames = new ArrayList<String>(fieldNamesSet);
    Collections.sort(fieldNames);
    StringBuilder result = new StringBuilder();
    System.out.println("  //*****  START GENERATED WITH GenerateFieldConstants.java *****//\n");
    for (String fieldName: fieldNames) {
      String caps = "FIELD_" + GrouperUtil.oracleStandardNameFromJava(fieldName);
      
      result.append("  /** constant for field name for: " + fieldName + " */\n");
      result.append("  public static final String " + caps + " = \"" + fieldName + "\";\n\n");
    }
    System.out.print(result.toString());
    System.out.println("  //*****  END GENERATED WITH GenerateFieldConstants.java *****//");
  }

}
