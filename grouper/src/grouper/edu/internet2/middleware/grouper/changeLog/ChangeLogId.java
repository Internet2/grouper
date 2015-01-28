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
/*
 * @author mchyzer
 * $Id: ChangeLogId.java,v 1.1 2009-05-23 06:00:29 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

/**
 * we want the millis since 1970, though we also want to go down to microsecond, not millis.
 * we do this by seeing if we get repeated millis, and if so, then check nanos, and if same, 
 * just increment
 */
public class ChangeLogId {

  /** last millis number */
  private static long lastMillis = -1;
  
  /** nanos when the last millis was taken (since nanos are diffs) */
  private static long millisNanos = -1;
  
  /** last id generated */
  private static long lastResult = -1;
  /**
   * get a change log id
   * @return a change log id value
   */
  public static synchronized long changeLogId() {
    long currentMillis = System.currentTimeMillis();
    long currentNanos = System.nanoTime();
    int currentThousandthsMicros = 0;
    //see if a milli has gone by since the last check
    if (currentMillis > lastMillis) {
      lastMillis = currentMillis;
      millisNanos = currentNanos;
    } else {
      
      //if less, then must have incremented
      currentMillis = lastMillis;
      
      //see if the micros are more.  if the number is 123456789, we want to get the 123456 number
      //note, this might add millis too, thats ok
      currentThousandthsMicros = (int)((currentNanos - millisNanos) / 1000);
    }
    
    //calculate and return
    long result = (currentMillis * 1000) + currentThousandthsMicros;
    
    //make sure greater
    if (result <= lastResult) {
      result = lastResult + 1;
    }
    lastResult = result;
    return result;
    
  }
  
  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    
//    long nanos = 123456789;
//    
//    long nanosMill = (nanos) / 1000;
//    System.out.println((int)(nanosMill) % 1000);
    
//    stressTest();
    
//    for (int i=0;i<100;i++) {
//      System.out.println(changeLogId());
//      Thread.sleep(1);
//    }
    
  }

  
}
