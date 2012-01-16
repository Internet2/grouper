/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class SubjectPerformance {

  /**
   * @param args
   */
  public static void main(String[] args) {

    //lets get 5k pennids
    List<String> pennids = HibernateSession.bySqlStatic().listSelect(String.class, 
        "select penn_id from person_source_v where rownum < 6000", null);
    
    int index = 0;
    
    {
      //lets do a batch and a single one to prime the pump
      List<String> fiveHundred = new ArrayList<String>();
      
      for (int i=0;i<500;i++) {
        fiveHundred.add(pennids.get(index));
        index++;
      }
      
      Map<String, Subject> result = SubjectFinder.findByIds(fiveHundred, "pennperson");
      
      if (result.size() < 490) {
        throw new RuntimeException("Problem! " + result.size());
      }
      
      fiveHundred = new ArrayList<String>();
      
      result = new HashMap<String, Subject>();
      
      for (int i=0;i<500;i++) {
        fiveHundred.add(pennids.get(index));
        index++;
      }
      
      for (String pennid : fiveHundred) {
        Subject subject = SubjectFinder.findByIdAndSource(pennid, "pennperson", false);
        if (subject != null) {
          result.put(pennid, subject);
        }
      }
      
      if (result.size() < 490) {
        throw new RuntimeException("Problem! " + result.size());
      }
    }    

    long start = System.nanoTime();
    
    //############### lets try batched
    //lets do a batch and a single one to prime the pump
    List<String> twoThousand = new ArrayList<String>();
    
    for (int i=0;i<2000;i++) {
      twoThousand.add(pennids.get(index));
      index++;
    }
    
    Map<String, Subject> result = SubjectFinder.findByIds(twoThousand, "pennperson");
    
    if (result.size() < 1900) {
      throw new RuntimeException("Problem! " + result.size());
    }
    
    System.out.println("Resolving 2000 subjects by batch took: " + ((System.nanoTime() - start) / 1000000) + "ms");
    
    //############### lets try not batched
    start = System.nanoTime();
    
    twoThousand = new ArrayList<String>();
    
    result = new HashMap<String, Subject>();
    
    for (int i=0;i<2000;i++) {
      twoThousand.add(pennids.get(index));
      index++;
    }
    
    for (String pennid : twoThousand) {
      Subject subject = SubjectFinder.findByIdAndSource(pennid, "pennperson", false);
      if (subject != null) {
        result.put(pennid, subject);
      }
    }
    
    if (result.size() < 1900) {
      throw new RuntimeException("Problem! " + result.size());
    }

    System.out.println("Resolving 2000 subjects individually took: " + ((System.nanoTime() - start) / 1000000) + "ms");
    
  
  }

}
