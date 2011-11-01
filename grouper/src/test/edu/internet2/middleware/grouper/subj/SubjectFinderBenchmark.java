/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;


/**
 * run this to benchmark subject findings... note, this should NOT extend GrouperTest!
 */
public class SubjectFinderBenchmark {

  /**
   * @param args
   */
  public static void main(String[] args) {

    GrouperSession.startRootSession();
    
    //SubjectFinder.findPage("ach");
    //SubjectFinder.findPage("mch");
    
    
//    SubjectFinder.findAll("abcsd");
//    SubjectFinder.findById("sdf", false);
//    
    runTest();
  }

  /**
   * 
   */
  private static void runTest() {
    
    
    long startNanos = System.nanoTime();
    long overallStartNanos = startNanos;
    
//    for (char i = 'a'; i<='b'; i++) {
//      for (char j = 'a'; j<='z'; j++) {
//
//        SubjectFinder.findAll(i + "" + j);
//        
//      }
//      System.out.print(".");
//    }
//    System.out.println("");
//
//    System.out.println((2*26) + " findAlls took " + ((System.nanoTime() - startNanos)/1000000) + "ms");
    startNanos = System.nanoTime();

    for (char i = 'c'; i<='d'; i++) {
      for (char j = 'a'; j<='z'; j++) {

        SubjectFinder.findPage(i + "" + j);
        
      }
      System.out.print(".");
    }
    System.out.println("");

    System.out.println((2*26) + " findPages took " + ((System.nanoTime() - startNanos)/1000000) + "ms");
    
    startNanos = System.nanoTime();

    for (char i = 'e'; i<='f'; i++) {
      for (char j = 'a'; j<='z'; j++) {

        SubjectFinder.findById(i + "" + j, false);
        
      }
      System.out.print(".");
    }
    System.out.println("");

    System.out.println((2 * 26) + " findByIds took " + ((System.nanoTime() - startNanos)/1000000) + "ms");
    
    startNanos = System.nanoTime();

    for (char i = 'g'; i<='h'; i++) {
      for (char j = 'a'; j<='z'; j++) {

        SubjectFinder.findByIdentifier("abd" + i + "" + j, false);
        
      }
      System.out.print(".");
    }
    System.out.println("");

    System.out.println((2*26) + " findByIdentifiers took " + ((System.nanoTime() - startNanos)/1000000) + "ms");
    
    startNanos = System.nanoTime();

    for (char i = 'i'; i<='j'; i++) {
      for (char j = 'a'; j<='z'; j++) {

        SubjectFinder.findByIdOrIdentifier("abe" + i + "" + j, false);
        
      }
      System.out.print(".");
    }
    System.out.println("");

    System.out.println((2*26) + " findByIdOrIdentifiers took " + ((System.nanoTime() - startNanos)/1000000) + "ms");
    
    System.out.println("Overall took: " + ((System.nanoTime() - overallStartNanos)/1000000) + "ms");
    
  }

  
  
}
