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
