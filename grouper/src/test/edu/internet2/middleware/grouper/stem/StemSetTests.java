/**
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
 */
package edu.internet2.middleware.grouper.stem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SyncStemSets;

/**
 * @author shilen
 * $Id$
 */
public class StemSetTests extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public StemSetTests(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    new SyncStemSets().showResults(false).fullSync();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  /**
   * 
   */
  public void testStemAdd() {
    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);

    Stem testStem = edu.addChildStem("test", "test");
    
    long stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount + 3, stemSetCountNew);
    
    List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(testStem.getUuid()));
    assertEquals(3, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(testStem.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(testStem.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(testStem, stemSets.get(0).getIfHasStem());
    assertEquals(testStem, stemSets.get(0).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(testStem.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(testStem, stemSets.get(1).getIfHasStem());
    assertEquals(edu, stemSets.get(1).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(testStem.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(testStem, stemSets.get(2).getIfHasStem());
    assertEquals(root, stemSets.get(2).getThenHasStem());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    assertEquals(0, new SyncStemSets().showResults(false).fullSync());
  }
  
  /**
   * 
   */
  public void testStemDelete() {
    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);

    Stem testStem = edu.addChildStem("test", "test");
    testStem.delete();
    
    long stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount, stemSetCountNew);
    
    List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(testStem.getUuid()));
    assertEquals(0, stemSets.size());
    
    assertEquals(0, new SyncStemSets().showResults(false).fullSync());
  }
  
  /**
   * 
   */
  public void testStemMoveNoChildren() {
    Stem testStem = edu.addChildStem("test", "test");
    
    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    testStem.move(root);
    
    long stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount - 1, stemSetCountNew);
    
    List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(testStem.getUuid()));
    assertEquals(2, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(testStem.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(testStem.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(testStem, stemSets.get(0).getIfHasStem());
    assertEquals(testStem, stemSets.get(0).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(testStem.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(testStem, stemSets.get(1).getIfHasStem());
    assertEquals(root, stemSets.get(1).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    assertEquals(0, new SyncStemSets().showResults(false).fullSync());
  }
  
  /**
   * 
   */
  public void testStemMoveWithChildren() {
    // root -> edu -> a -> b
    //                  -> c -> d
    //                       -> e
    //             -> 1 -> 2
    // moving "a" to "2"
    // root -> edu -> 1 -> 2 -> a -> b
    //                            -> c -> d
    //                                 -> e
    // then move it back
    Stem stemA = edu.addChildStem("a", "a");
    Stem stemB = stemA.addChildStem("b", "b");
    Stem stemC = stemA.addChildStem("c", "c");
    Stem stemD = stemC.addChildStem("d", "d");
    Stem stemE = stemC.addChildStem("e", "e");
    Stem stem1 = edu.addChildStem("1", "1");
    Stem stem2 = stem1.addChildStem("2", "2");

    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    stemA.move(stem2);
    
    long stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount + 10, stemSetCountNew);
    
    assertEquals(0, new SyncStemSets().showResults(false).fullSync());

    // 1.  verify edu (shouldn't have changed)
    List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(edu.getUuid()));
    assertEquals(2, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(edu.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(edu, stemSets.get(0).getIfHasStem());
    assertEquals(edu, stemSets.get(0).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(edu.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(edu, stemSets.get(1).getIfHasStem());
    assertEquals(root, stemSets.get(1).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // 2.  verify stem2 (shouldn't have changed)
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stem2.getUuid()));
    assertEquals(4, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stem2.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stem2.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stem2, stemSets.get(0).getIfHasStem());
    assertEquals(stem2, stemSets.get(0).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stem2.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stem1.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stem2, stemSets.get(1).getIfHasStem());
    assertEquals(stem1, stemSets.get(1).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stem2.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stem2, stemSets.get(2).getIfHasStem());
    assertEquals(edu, stemSets.get(2).getThenHasStem());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stem2.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stem2, stemSets.get(3).getIfHasStem());
    assertEquals(root, stemSets.get(3).getThenHasStem());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
    
    // 3.  verify stemA
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemA.getUuid()));
    assertEquals(5, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemA.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemA, stemSets.get(0).getIfHasStem());
    assertEquals(stemA, stemSets.get(0).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemA.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stem2.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemA, stemSets.get(1).getIfHasStem());
    assertEquals(stem2, stemSets.get(1).getThenHasStem());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to stem1
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemA.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(stem1.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemA, stemSets.get(2).getIfHasStem());
    assertEquals(stem1, stemSets.get(2).getThenHasStem());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stemA.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stemA, stemSets.get(3).getIfHasStem());
    assertEquals(edu, stemSets.get(3).getThenHasStem());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(4, stemSets.get(4).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(4).getType());
    assertEquals(stemA.getUuid(), stemSets.get(4).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(4).getThenHasStemId());
    assertEquals(stemSets.get(3).getId(), stemSets.get(4).getParentStemSetId());
    assertEquals(stemA, stemSets.get(4).getIfHasStem());
    assertEquals(root, stemSets.get(4).getThenHasStem());
    assertEquals(stemSets.get(3), stemSets.get(4).getParentStemSet());
    
    // 4.  verify stemB
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemB.getUuid()));
    assertEquals(6, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemB.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemB.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemB.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to stem2
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemB.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(stem2.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to stem1
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stemB.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(stem1.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(4, stemSets.get(4).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(4).getType());
    assertEquals(stemB.getUuid(), stemSets.get(4).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(4).getThenHasStemId());
    assertEquals(stemSets.get(3).getId(), stemSets.get(4).getParentStemSetId());
    assertEquals(stemSets.get(3), stemSets.get(4).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(5, stemSets.get(5).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(5).getType());
    assertEquals(stemB.getUuid(), stemSets.get(5).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(5).getThenHasStemId());
    assertEquals(stemSets.get(4).getId(), stemSets.get(5).getParentStemSetId());
    assertEquals(stemSets.get(4), stemSets.get(5).getParentStemSet());
    
    // 5.  verify stemC
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemC.getUuid()));
    assertEquals(6, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemC.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemC.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemC.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to stem2
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemC.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(stem2.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to stem1
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stemC.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(stem1.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(4, stemSets.get(4).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(4).getType());
    assertEquals(stemC.getUuid(), stemSets.get(4).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(4).getThenHasStemId());
    assertEquals(stemSets.get(3).getId(), stemSets.get(4).getParentStemSetId());
    assertEquals(stemSets.get(3), stemSets.get(4).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(5, stemSets.get(5).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(5).getType());
    assertEquals(stemC.getUuid(), stemSets.get(5).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(5).getThenHasStemId());
    assertEquals(stemSets.get(4).getId(), stemSets.get(5).getParentStemSetId());
    assertEquals(stemSets.get(4), stemSets.get(5).getParentStemSet());
    
    // 6.  verify stemD
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemD.getUuid()));
    assertEquals(7, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemD.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemD.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemD.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stemC.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to stemA
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemD.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to stem2
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stemD.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(stem2.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
    
    // verify effective stem set to stem1
    assertEquals(4, stemSets.get(4).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(4).getType());
    assertEquals(stemD.getUuid(), stemSets.get(4).getIfHasStemId());
    assertEquals(stem1.getUuid(), stemSets.get(4).getThenHasStemId());
    assertEquals(stemSets.get(3).getId(), stemSets.get(4).getParentStemSetId());
    assertEquals(stemSets.get(3), stemSets.get(4).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(5, stemSets.get(5).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(5).getType());
    assertEquals(stemD.getUuid(), stemSets.get(5).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(5).getThenHasStemId());
    assertEquals(stemSets.get(4).getId(), stemSets.get(5).getParentStemSetId());
    assertEquals(stemSets.get(4), stemSets.get(5).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(6, stemSets.get(6).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(6).getType());
    assertEquals(stemD.getUuid(), stemSets.get(6).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(6).getThenHasStemId());
    assertEquals(stemSets.get(5).getId(), stemSets.get(6).getParentStemSetId());
    assertEquals(stemSets.get(5), stemSets.get(6).getParentStemSet());
    
    // 7.  verify stemE
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemE.getUuid()));
    assertEquals(7, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemE.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemE.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemE.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stemC.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to stemA
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemE.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to stem2
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stemE.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(stem2.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
    
    // verify effective stem set to stem1
    assertEquals(4, stemSets.get(4).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(4).getType());
    assertEquals(stemE.getUuid(), stemSets.get(4).getIfHasStemId());
    assertEquals(stem1.getUuid(), stemSets.get(4).getThenHasStemId());
    assertEquals(stemSets.get(3).getId(), stemSets.get(4).getParentStemSetId());
    assertEquals(stemSets.get(3), stemSets.get(4).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(5, stemSets.get(5).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(5).getType());
    assertEquals(stemE.getUuid(), stemSets.get(5).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(5).getThenHasStemId());
    assertEquals(stemSets.get(4).getId(), stemSets.get(5).getParentStemSetId());
    assertEquals(stemSets.get(4), stemSets.get(5).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(6, stemSets.get(6).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(6).getType());
    assertEquals(stemE.getUuid(), stemSets.get(6).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(6).getThenHasStemId());
    assertEquals(stemSets.get(5).getId(), stemSets.get(6).getParentStemSetId());
    assertEquals(stemSets.get(5), stemSets.get(6).getParentStemSet());
    
    // move back now
    stemA.move(edu);
    stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount, stemSetCountNew);
    assertEquals(0, new SyncStemSets().showResults(false).fullSync());

    // 1. verify stemA
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemA.getUuid()));
    assertEquals(3, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemA.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemA.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemA.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // 2. verify stemE
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemE.getUuid()));
    assertEquals(5, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemE.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemE.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemE.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stemC.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to stemA
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemE.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stemE.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(4, stemSets.get(4).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(4).getType());
    assertEquals(stemE.getUuid(), stemSets.get(4).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(4).getThenHasStemId());
    assertEquals(stemSets.get(3).getId(), stemSets.get(4).getParentStemSetId());
    assertEquals(stemSets.get(3), stemSets.get(4).getParentStemSet());
  }
  
  /**
   * 
   */
  public void testStemSetFullRefresh() {
    // root -> edu -> a -> b
    //                  -> c -> d
    //                       -> e
    //             -> 1 -> 2
    // verify root and stemC
    Stem stemA = edu.addChildStem("a", "a");
    stemA.addChildStem("b", "b");
    Stem stemC = stemA.addChildStem("c", "c");
    stemC.addChildStem("d", "d");
    stemC.addChildStem("e", "e");
    Stem stem1 = edu.addChildStem("1", "1");
    stem1.addChildStem("2", "2");
    
    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    
    Set<Stem> allStems = GrouperDAOFactory.getFactory().getStem().getAllStems();
    for (Stem stem : allStems) {
      GrouperDAOFactory.getFactory().getStemSet().deleteByIfHasStemId(stem.getUuid());
    }
    
    long stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(0, stemSetCountNew);
    
    // check for issues
    assertEquals(allStems.size(), new SyncStemSets().saveUpdates(false).showResults(false).fullSync());
    stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(0, stemSetCountNew);
    
    // add it all back
    assertEquals(allStems.size(), new SyncStemSets().showResults(false).fullSync());
    stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount, stemSetCountNew);
    
    // 1. verify root
    List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(root.getUuid()));
    assertEquals(1, stemSets.size());

    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(root.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // 2. verify stemC
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemC.getUuid()));
    assertEquals(4, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemC.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemC.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemC.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemC.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stemC.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
  }
  
  /**
   * 
   */
  public void testStemSetMissingStems() {
    // root -> edu -> a -> b
    //                  -> c -> d
    //                       -> e
    //             -> 1 -> 2
    // delete stemA and stemE stemSets
    Stem stemA = edu.addChildStem("a", "a");
    stemA.addChildStem("b", "b");
    Stem stemC = stemA.addChildStem("c", "c");
    stemC.addChildStem("d", "d");
    Stem stemE = stemC.addChildStem("e", "e");
    Stem stem1 = edu.addChildStem("1", "1");
    stem1.addChildStem("2", "2");
    
    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    
    GrouperDAOFactory.getFactory().getStemSet().deleteByIfHasStemId(stemA.getUuid());
    GrouperDAOFactory.getFactory().getStemSet().deleteByIfHasStemId(stemE.getUuid());
    
    long stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount - 8, stemSetCountNew);
    
    // check for issues
    assertEquals(2, new SyncStemSets().saveUpdates(false).showResults(false).fullSync());
    stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount - 8, stemSetCountNew);
    
    // add it all back
    assertEquals(2, new SyncStemSets().showResults(false).fullSync());
    stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount, stemSetCountNew);
    
    // 1. verify stemA
    List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemA.getUuid()));
    assertEquals(3, stemSets.size());

    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemA.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemA.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemA.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
  }
  
  /**
   * 
   */
  public void testStemSetMissingEffectiveStemSets() {
    // root -> edu -> a -> b
    //                  -> c -> d
    //                       -> e
    //             -> 1 -> 2
    // delete stemSets:
    //   stemA -> root
    //   stemA -> edu
    //   stemC -> root
    Stem stemA = edu.addChildStem("a", "a");
    stemA.addChildStem("b", "b");
    Stem stemC = stemA.addChildStem("c", "c");
    stemC.addChildStem("d", "d");
    stemC.addChildStem("e", "e");
    Stem stem1 = edu.addChildStem("1", "1");
    stem1.addChildStem("2", "2");
    
    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);

    List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemA.getUuid()));

    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    stemSets.get(2).delete();
    stemSets.get(1).delete();

    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemC.getUuid()));

    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    stemSets.get(3).delete();

    long stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount - 3, stemSetCountNew);
    
    // check for issues
    assertEquals(2, new SyncStemSets().saveUpdates(false).showResults(false).fullSync());
    stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount - 3, stemSetCountNew);
    
    // add it all back
    assertEquals(2, new SyncStemSets().showResults(false).fullSync());
    stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount, stemSetCountNew);
    
    // 1. verify stemA
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemA.getUuid()));
    assertEquals(3, stemSets.size());

    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemA.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemA.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemA.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
  }
  
  /**
   * 
   */
  public void testStemSetWithBadStemSets() {
    // root -> edu -> a -> b
    //                  -> c -> d
    //                       -> e
    //             -> 1 -> 2
    // move "a" to "2" (That is, set things up to simulate that stemA was moved from stem2 to edu but stemSets weren't updated.)
    Stem stemA = edu.addChildStem("a", "a");
    stemA.addChildStem("b", "b");
    Stem stemC = stemA.addChildStem("c", "c");
    stemC.addChildStem("d", "d");
    Stem stemE = stemC.addChildStem("e", "e");
    Stem stem1 = edu.addChildStem("1", "1");
    Stem stem2 = stem1.addChildStem("2", "2");
    
    long stemSetCount = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    
    // move stemSets for stemA (and children) to stem2 to produce bad results
    Set<StemSet> ifHasStemSetsOfParentStem = GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stem2.getUuid());
    Set<StemSet> oldStemSets = GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemA.getUuid());
    GrouperDAOFactory.getFactory().getStem().moveStemSets(new LinkedList<StemSet>(ifHasStemSetsOfParentStem), new LinkedList<StemSet>(oldStemSets), stemA.getUuid(), 0);
    
    // check for issues
    assertEquals(5, new SyncStemSets().saveUpdates(false).showResults(false).fullSync());
    long stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount + 10, stemSetCountNew);
    
    // correct it all
    assertEquals(5, new SyncStemSets().showResults(false).fullSync());
    stemSetCountNew = HibernateSession.byHqlStatic().createQuery("select count(*) from StemSet").uniqueResult(Long.class);
    assertEquals(stemSetCount, stemSetCountNew);
    
    assertEquals(0, new SyncStemSets().saveUpdates(false).showResults(false).fullSync());

    // 1. verify stemA
    List<StemSet> stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemA.getUuid()));
    assertEquals(3, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemA.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemA.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemA.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // 2. verify stemE
    stemSets = new ArrayList<StemSet>(GrouperDAOFactory.getFactory().getStemSet().findByIfHasStemId(stemE.getUuid()));
    assertEquals(5, stemSets.size());
    
    // sort by depth
    Collections.sort(stemSets, new Comparator<StemSet>() {

      public int compare(StemSet o1, StemSet o2) {
        return ((Integer)o1.getDepth()).compareTo(o2.getDepth());
      }
    });
    
    // verify self stem set
    assertEquals(0, stemSets.get(0).getDepth());
    assertEquals(StemHierarchyType.self, stemSets.get(0).getType());
    assertEquals(stemE.getUuid(), stemSets.get(0).getIfHasStemId());
    assertEquals(stemE.getUuid(), stemSets.get(0).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(0).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(0).getParentStemSet());
    
    // verify immediate stem set
    assertEquals(1, stemSets.get(1).getDepth());
    assertEquals(StemHierarchyType.immediate, stemSets.get(1).getType());
    assertEquals(stemE.getUuid(), stemSets.get(1).getIfHasStemId());
    assertEquals(stemC.getUuid(), stemSets.get(1).getThenHasStemId());
    assertEquals(stemSets.get(0).getId(), stemSets.get(1).getParentStemSetId());
    assertEquals(stemSets.get(0), stemSets.get(1).getParentStemSet());
    
    // verify effective stem set to stemA
    assertEquals(2, stemSets.get(2).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(2).getType());
    assertEquals(stemE.getUuid(), stemSets.get(2).getIfHasStemId());
    assertEquals(stemA.getUuid(), stemSets.get(2).getThenHasStemId());
    assertEquals(stemSets.get(1).getId(), stemSets.get(2).getParentStemSetId());
    assertEquals(stemSets.get(1), stemSets.get(2).getParentStemSet());
    
    // verify effective stem set to edu
    assertEquals(3, stemSets.get(3).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(3).getType());
    assertEquals(stemE.getUuid(), stemSets.get(3).getIfHasStemId());
    assertEquals(edu.getUuid(), stemSets.get(3).getThenHasStemId());
    assertEquals(stemSets.get(2).getId(), stemSets.get(3).getParentStemSetId());
    assertEquals(stemSets.get(2), stemSets.get(3).getParentStemSet());
    
    // verify effective stem set to root
    assertEquals(4, stemSets.get(4).getDepth());
    assertEquals(StemHierarchyType.effective, stemSets.get(4).getType());
    assertEquals(stemE.getUuid(), stemSets.get(4).getIfHasStemId());
    assertEquals(root.getUuid(), stemSets.get(4).getThenHasStemId());
    assertEquals(stemSets.get(3).getId(), stemSets.get(4).getParentStemSetId());
    assertEquals(stemSets.get(3), stemSets.get(4).getParentStemSet());
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new StemSetTests("testStemSetMissingEffectiveStemSets"));
  }
}