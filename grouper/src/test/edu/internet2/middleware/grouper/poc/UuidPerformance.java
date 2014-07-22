/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: UuidPerformance.java,v 1.2 2009-08-11 20:18:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.poc;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import org.safehaus.uuid.UUIDGenerator;

import java.util.UUID;

/**
 *
 */
public class UuidPerformance {

  /**
   * @param args
   */
  public static void main(String[] args) {
    UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
    long now = System.nanoTime();
    String uuid = null;
    for (int i=0;i<100000;i++) {
      uuid = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
      if (i<5) {
        System.out.println(uuid);
      }
      
    }
    System.out.println("Current took " + ((System.nanoTime() - now)/1000000d) + "millis");
    now = System.nanoTime();
    UUID.randomUUID().toString();

    for (int i=0;i<100000;i++) {
      uuid = UUID.randomUUID().toString();
      if (i<5) {
        System.out.println(uuid);
      }
    }
    System.out.println("New took " + ((System.nanoTime() - now)/1000000d) + "millis");
    
    now = System.nanoTime();
    GrouperUuid.getUuid();

    for (int i=0;i<100000;i++) {
      uuid = GrouperUuid.getUuid();
      if (i<5) {
        System.out.println(uuid);
      }
    }
    System.out.println("New2 took " + ((System.nanoTime() - now)/1000000d) + "millis");

    now = System.nanoTime();
    GrouperUuid.getUuid();

    for (int i=0;i<100000;i++) {
      uuid = GrouperUuid.getUuid();
      if (i<5) {
        System.out.println(uuid);
      }
    }
    System.out.println("New3 took " + ((System.nanoTime() - now)/1000000d) + "millis");

    now = System.nanoTime();
    GrouperUuid.getUuid();

    for (int i=0;i<100000;i++) {
      uuid = GrouperUuid.getUuid();
      if (i<5) {
        System.out.println(uuid);
      }
    }
    System.out.println("New4 took " + ((System.nanoTime() - now)/1000000d) + "millis");

  }

}
