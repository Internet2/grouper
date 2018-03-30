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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.poc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSession;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class LdapPennPoc {

  /**
   * 
   * @param pennids
   */
  public static void runAllOneByOne(List<String> pennids) {
    
    Set<String> pennkeysSet = new HashSet<String>();
    
    //try 100 without timing
    for (int i=0; i<100; i++) {
      List<String> pennkeys = retrieveOnePennkey(pennids, i);
      
      pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));
      
//      for (int j=0;j<pennkeys.size();j++) {
//        
//        System.out.println("Pennkey: " + pennkeys.get(j));
//      }
    }
    
    long start = System.nanoTime();
    for (int i=100;i<pennids.size();i++) {
      List<String> pennkeys = retrieveOnePennkey(pennids, i);
      
      pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));
      
      if (i%1000 == 0) {
        System.out.println("Retrieved from ldap (" + i + "): " + ((System.nanoTime() - start) / 1000000) + "ms");
      }
    }
    
    System.out.println("Retrieved from ldap all: " + ((System.nanoTime() - start) / 1000000) + "ms");
    System.out.println("Found " + pennkeysSet.size() + " pennkeys");
  }


  /**
   * @param pennids
   * @param i
   * @return the list
   */
  private static List<String> retrieveOnePennkey(List<String> pennids, int i) {
    return LdapSessionUtils.ldapSession().list(String.class, "pennProd", "ou=pennnames", 
        LdapSearchScope.ONELEVEL_SCOPE, "(pennid=" + pennids.get(i) + ")", "pennname");
  }
  
  /**
   * @param pennids
   * @param startIndex
   * @return the list
   */
  private static List<String> retrieve100Pennkeys(List<String> pennids, int startIndex) {
    
    StringBuilder filter = new StringBuilder("  (| ");
    
    //(...K1...) (...K2...) (...K3...) (...K4...))");
    boolean foundOne = false;
    for (int i=startIndex*100;(i<startIndex*100 + 100) && (startIndex*100+99<pennids.size());i++) {
      
      filter.append("(pennid=").append(pennids.get(i)).append(") ");
      
      foundOne = true;
      
    }
    if (!foundOne) {
      return new ArrayList<String>();
    }
    
    filter.append(" )");
    
    //System.out.println(filter);
    
    return LdapSessionUtils.ldapSession().list(String.class, "pennProd", "ou=pennnames", 
        LdapSearchScope.ONELEVEL_SCOPE, filter.toString(), "pennname");
  }
  

  
    /**
   * 
   * @param pennids
   */
  public static void runAll100atTime(List<String> pennids) {
    
    Set<String> pennkeysSet = new HashSet<String>();

    List<String> pennkeys = retrieve100Pennkeys(pennids, 0);
    
    pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));
    
    long start = System.nanoTime();
    for (int i=1;i<pennids.size()/100;i++) {
      pennkeys = retrieve100Pennkeys(pennids, i);
      if (i%10 == 0) {
        System.out.println("Retrieved from ldap (" + (i*100)+ "): " + ((System.nanoTime() - start) / 1000000) + "ms");
      }
      pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));

    }
    
    System.out.println("Retrieved from ldap all: " + ((System.nanoTime() - start) / 1000000) + "ms");
    System.out.println("Found " + pennkeysSet.size() + " pennkeys");

  }

  
  /**
   * @param pennids
   * @param startIndex
   * @return the list
   */
  private static List<String> retrieve200Pennkeys(List<String> pennids, int startIndex) {
    
    StringBuilder filter = new StringBuilder("  (| ");
    
    //(...K1...) (...K2...) (...K3...) (...K4...))");
    boolean foundOne = false;
    for (int i=startIndex*200;(i<startIndex*200 + 200) && (startIndex*200+199<pennids.size());i++) {
      
      filter.append("(pennid=").append(pennids.get(i)).append(") ");
      
      foundOne = true;
      
    }
    if (!foundOne) {
      return new ArrayList<String>();
    }
    
    filter.append(" )");
    
    //System.out.println(filter);
    
    return LdapSessionUtils.ldapSession().list(String.class, "pennProd", "ou=pennnames", 
        LdapSearchScope.ONELEVEL_SCOPE, filter.toString(), "pennname");
  }
  

  
    /**
   * 
   * @param pennids
   */
  public static void runAll200atTime(List<String> pennids) {
    
    Set<String> pennkeysSet = new HashSet<String>();

    List<String> pennkeys = retrieve200Pennkeys(pennids, 0);
    
    pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));
    
    long start = System.nanoTime();
    for (int i=1;i<pennids.size()/200;i++) {
      pennkeys = retrieve200Pennkeys(pennids, i);
      if (i%5 == 0) {
        System.out.println("Retrieved from ldap (" + (i*200)+ "): " + ((System.nanoTime() - start) / 1000000) + "ms");
      }
      pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));

    }
    
    System.out.println("Retrieved from ldap all: " + ((System.nanoTime() - start) / 1000000) + "ms");
    System.out.println("Found " + pennkeysSet.size() + " pennkeys");

  }

  /**
   * @param pennids
   * @param startIndex
   * @return the list
   */
  private static List<String> retrieve50Pennkeys(List<String> pennids, int startIndex) {
    
    StringBuilder filter = new StringBuilder("  (| ");
    
    //(...K1...) (...K2...) (...K3...) (...K4...))");
    boolean foundOne = false;
    for (int i=startIndex*50;(i<startIndex*50 + 50) && (startIndex*50+49<pennids.size());i++) {
      
      filter.append("(pennid=").append(pennids.get(i)).append(") ");
      
      foundOne = true;
      
    }
    if (!foundOne) {
      return new ArrayList<String>();
    }
    
    filter.append(" )");
    
    //System.out.println(filter);
    
    return LdapSessionUtils.ldapSession().list(String.class, "pennProd", "ou=pennnames", 
        LdapSearchScope.ONELEVEL_SCOPE, filter.toString(), "pennname");
  }
  

  
    /**
   * 
   * @param pennids
   */
  public static void runAll50atTime(List<String> pennids) {
    
    Set<String> pennkeysSet = new HashSet<String>();

    List<String> pennkeys = retrieve50Pennkeys(pennids, 0);
    
    pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));
    
    pennkeys = retrieve50Pennkeys(pennids, 10);
    
    pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));

    long start = System.nanoTime();
    for (int i=2;i<pennids.size()/50;i++) {
      pennkeys = retrieve50Pennkeys(pennids, i);
      if (i%20 == 0) {
        System.out.println("Retrieved from ldap (" + (i*50)+ "): " + ((System.nanoTime() - start) / 1000000) + "ms");
      }
      pennkeysSet.addAll(GrouperUtil.nonNull(pennkeys));

    }
    
    System.out.println("Retrieved from ldap all: " + ((System.nanoTime() - start) / 1000000) + "ms");
    System.out.println("Found " + pennkeysSet.size() + " pennkeys");

  }
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.runDdlBootstrap = false;
    GrouperStartup.ignoreCheckConfig = true;
    
    long start = System.nanoTime();
    
    //get all pennid
    List<String> pennids = HibernateSession.bySqlStatic().listSelect(String.class, 
        "select distinct(GMLV.SUBJECT_ID) from grouper_memberships_lw_v gmlv "
        + " where GMLV.SUBJECT_SOURCE = 'pennperson' and gmlv.list_name = 'members' "
        + " and GMLV.GROUP_NAME = 'penn:community:employee' and rownum <= 20100 ", null);
    
    System.out.println("Retrieved pennids: " + ((System.nanoTime() - start) / 1000000) + "ms");
    
    start = System.nanoTime();
    
    Collections.shuffle(pennids);
    
    System.out.println("Shuffled pennids: " + ((System.nanoTime() - start) / 1000000) + "ms");
    
    //runAllOneByOne(pennids);
    
    //runAll100atTime(pennids);
    
    //runAll200atTime(pennids);
    
    runAll50atTime(pennids);
  }

}
