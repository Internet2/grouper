package edu.internet2.middleware.grouper.poc;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * timezone poc
 * @author mchyzer
 *
 */
public class TimezonePoc {

  /**
   * @param args
   */
  public static void main(String[] args) {

    Set<String> timezones = new TreeSet<String>();
    
    for (String id : TimeZone.getAvailableIDs()) {
      timezones.add(id);
    }
    
    for (String id : timezones) {
      System.out.println(id);
    }
    
    Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Eastern"));
    
    System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
    
    calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Pacific"));
    
    System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
    
  }

}
