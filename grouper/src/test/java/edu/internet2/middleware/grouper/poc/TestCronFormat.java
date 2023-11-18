/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import java.text.ParseException;

import net.redhogs.cronparser.CronExpressionDescriptor;


/**
 *
 */
public class TestCronFormat {

  /**
   * 
   */
  public TestCronFormat() {
  }

  /**
   * @param args
   * @throws ParseException 
   */
  public static void main(String[] args) throws ParseException {
      System.out.println(CronExpressionDescriptor.getDescription("* * * * * *"));
  }

}
