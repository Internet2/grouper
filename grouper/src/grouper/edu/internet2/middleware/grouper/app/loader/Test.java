/*
 * @author mchyzer
 * $Id: Test.java,v 1.1 2008-11-08 03:42:33 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.loader;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *
 */
public class Test {

  /**
   * @param args
   */
  public static void main(String[] args) {
    String weekday = new SimpleDateFormat("EEEE").format(new Date());
    System.out.println(weekday);
  }

}
