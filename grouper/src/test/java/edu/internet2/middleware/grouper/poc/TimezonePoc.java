/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
