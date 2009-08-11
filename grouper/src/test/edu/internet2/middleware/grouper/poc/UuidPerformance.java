/*
 * @author mchyzer
 * $Id: UuidPerformance.java,v 1.2 2009-08-11 20:18:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.poc;

import java.util.UUID;

import org.doomdark.uuid.UUIDGenerator;

import edu.internet2.middleware.grouper.internal.util.GrouperUuid;

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
