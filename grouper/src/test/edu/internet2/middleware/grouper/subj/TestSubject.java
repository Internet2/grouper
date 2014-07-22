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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.subj;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestSubject.java,v 1.2 2009-09-02 05:57:26 mchyzer Exp $
 */
public class TestSubject extends GrouperTest {

  /** Private Static Class Constants */
  private static final Log LOG = GrouperUtil.getLog(TestSubject.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestSubject("testFindAllInternal"));
  }
  
  public TestSubject(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testFindAllInternal() {
    GrouperSession.startRootSession();
    //grouper sys admin
    Subject grouperSystem = SubjectFinder.findById("GrouperSystem", true);
    Subject everyEntity = SubjectFinder.findById("GrouperAll", true);
    
    Set<Subject> subjects = SubjectFinder.findAll("groupers");
    
    assertEquals(1, subjects.size());
    
    assertTrue(SubjectHelper.inList(subjects, grouperSystem));
    
    subjects = SubjectFinder.findAll("rsyste");
    
    assertEquals(1, subjects.size());
    
    assertTrue(SubjectHelper.inList(subjects, grouperSystem));
    
    subjects = SubjectFinder.findAll("rsysa");
    
    assertEquals(1, subjects.size());
    
    assertTrue(SubjectHelper.inList(subjects, grouperSystem));    
    
    subjects = SubjectFinder.findAll("groupera");
    
    assertEquals(1, subjects.size());
    
    assertTrue(SubjectHelper.inList(subjects, everyEntity));  
    
    subjects = SubjectFinder.findAll("erall");
    
    assertEquals(1, subjects.size());
    
    assertTrue(SubjectHelper.inList(subjects, everyEntity));  
    
    subjects = SubjectFinder.findAll("eryen");
    
    assertEquals(1, subjects.size());
    
    assertTrue(SubjectHelper.inList(subjects, everyEntity));  
    
  
  }
  
  public void testGetSources() {
    LOG.info("testGetSources");
    Set sources = SubjectFinder.getSources();
    assertTrue(sources.size() >= 3);
  } // public void testGetSources()

  public void testFailToFindAllBySource() {
    LOG.info("testFailToFindAllBySource");
    try {
      R       r     = R.populateRegistry(0, 0, 0);
      T.amount("group subjects", SubjectFinder.findAll("i2").size(), 0);
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFailToFindAllBySource()

  public void testFailToFindByIdAndTypeAndSource() {
    LOG.info("testFailToFindByIdAndTypeAndSource");
    try {
      R       r     = R.populateRegistry(0, 0, 0);
      String  id    = "a";
      String  type  = "person";
      String  sa    = "jdbc";
      Subject subj  = SubjectFinder.findById(id, type, sa, true);
      Assert.fail("found non-existent subject: " + subj);
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.assertTrue("OK: failed to find non-existent subject", true);
    }
  } // public void testFailToFindByIdAndTypeAndSource()

  public void testFailToFindByIdentifierAndTypeAndSource() {
    LOG.info("testFailToFindByIdentifierAndTypeAndSource");
    try {
      R       r     = R.populateRegistry(0, 0, 0);
      String  id    = "i2:a";
      String  type  = "group";
      String  sa    = "g:gsa";
      Subject subj  = SubjectFinder.findByIdentifier(id, type, sa, true);
      Assert.fail("found non-existent subject: " + subj);
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.assertTrue("OK: failed to find non-existent subject", true);
    }
  } // public void testFailToFindByIdentifierAndTypeAndSource()

  public void testFindAllBySource() {
    LOG.info("testFindAllBySource");
    try {
      R       r     = R.populateRegistry(1, 2, 0);
      T.amount("group subjects", SubjectFinder.findAll("i2").size(), 2);
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFindAllBySource()

  public void testFindByIdAndTypeAndSource() {
    LOG.info("testFindByIdAndTypeAndSource");
    try {
      R       r     = R.populateRegistry(0, 0, 1);
      String  id    = "a";
      String  type  = "person";
      String  sa    = "jdbc";
      Subject subj  = SubjectFinder.findById(id, type, sa, true);
      T.string("subject id"     , subj.getId()              , id    );
      T.string("subject type"   , subj.getType().getName()  , type  );
      T.string("subject source" , subj.getSource().getId()  , sa    );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFindByIdAndTypeAndSource()

  public void testFindByIdentifierAndTypeAndSource() {
    LOG.info("testFindByIdentifierAndTypeAndSource");
    try {
      R       r     = R.populateRegistry(1, 1, 0);
      String  id    = "i2:a:a";
      String  type  = "group";
      String  sa    = "g:gsa";
      Subject subj  = SubjectFinder.findByIdentifier(id, type, sa, true);
      T.string("subject name"   , subj.getName()            , id    );
      T.string("subject type"   , subj.getType().getName()  , type  );
      T.string("subject source" , subj.getSource().getId()  , sa    );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFindByIdentifierAndTypeAndSource()

  public void testFindGrouperAllByIdentifier() {
    LOG.info("testFindGrouperAllByIdentifier");
    try {
      Subject subj = SubjectFinder.findByIdentifier(GrouperConfig.ALL, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj instanceof Subject", subj instanceof Subject);
      T.string("subj id"      , GrouperConfig.ALL       , subj.getId()              );
      T.string("subj type"    , GrouperConfig.IST       , subj.getType().getName()  );
      T.string("subj source"  , InternalSourceAdapter.ID, subj.getSource().getId()  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperAllByIdentifier()

  public void testFindGrouperAllByIdentifierAndType() {
    LOG.info("testFindGrouperAllByIdentifierAndType");
    try {
      Subject subj = SubjectFinder.findByIdentifier(GrouperConfig.ALL, GrouperConfig.IST, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj instanceof Subject", subj instanceof Subject);
      T.string("subj id"      , GrouperConfig.ALL       , subj.getId()              );
      T.string("subj type"    , GrouperConfig.IST       , subj.getType().getName()  );
      T.string("subj source"  , InternalSourceAdapter.ID, subj.getSource().getId()  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperAllByIdentifierAndType()

  public void testFindGrouperAllByIdentifierAndTypeAndSource() {
    LOG.info("testFindGrouperAllByIdentifierAndTypeAndSource");
    try {
      Subject subj = SubjectFinder.findByIdentifier(
        GrouperConfig.ALL, GrouperConfig.IST, InternalSourceAdapter.ID, true
      );
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue("subj instanceof Subject", subj instanceof Subject);
      T.string("subj id"      , GrouperConfig.ALL       , subj.getId()              );
      T.string("subj type"    , GrouperConfig.IST       , subj.getType().getName()  );
      T.string("subj source"  , InternalSourceAdapter.ID, subj.getSource().getId()  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindGrouperAllByIdentifierAndTypeAndSource()

  public void testFindRootSubject() {
    LOG.info("testFindRootSubject");
    try {
      Subject root = SubjectFinder.findRootSubject();
      Assert.assertNotNull("root not null", root);
      Assert.assertTrue("root instanceof Subject", root instanceof Subject);
      T.string("root id"      , GrouperConfig.ROOT      , root.getId()              );
      T.string("root type"    , GrouperConfig.IST       , root.getType().getName()  );
      T.string("root source"  , InternalSourceAdapter.ID, root.getSource().getId()  );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFindRootSubject()

  public void testGetSource2() {
    LOG.info("testGetSource");
    T.getSource("g:gsa");
    T.getSource("g:isa");
    T.getSource("jdbc");
  } // public void testGetSource()

  public void testGetSource() {
    LOG.info("testGetSource");
    try {
      String  id  = "no such source";
      SubjectFinder.getSource(id);
      Assert.fail("found invalid source: " + id);
    }
    catch (SourceUnavailableException eSU) {
      Assert.assertTrue("OK: failed to find invalid source", true);
    }
  } // public void testGetSource()

  public void testGrouperSubjectEqual() {
    LOG.info("testGrouperSubjectEqual");
    try {
      R               r       = R.populateRegistry(1, 2, 1);
      Group           gA      = r.getGroup("a", "a");
      Group           gB      = r.getGroup("a", "b");
      Subject         subjA   = r.getSubject("a");
      GrouperSubject  subjGA  = new GrouperSubject( gA);
      Assert.assertTrue(
        "gA == gA"    , subjGA.equals(gA.toSubject())
      );
      Assert.assertFalse(
        "gA != gB"    , subjGA.equals(gB.toSubject())
      );
      Assert.assertFalse(
        "gA != subjA" , subjGA.equals(subjA)
      );
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGrouperSubjectEqual()

  public void testAddRegistrySubjectAsRoot() {
    LOG.info("testAddRegistrySubjectAsRoot");
    try {
      RegistrySubject.add(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        "subj id", "subj type", "subj name"
      );
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testAddRegistrySubjectAsRoot()

  public void testFailToAddAlreadyExistingSubject() {
    LOG.info("testFailToAddAlreadyExistingSubject");
    try {
      RegistrySubject.add(
        GrouperSession.start( SubjectFinder.findRootSubject() ),
        "subj id", "subj type", "subj name"
      );
      // Now add it again
      try {
        RegistrySubject.add(
          GrouperSession.start( SubjectFinder.findRootSubject() ),
          "subj id", "subj type", "subj name"
        );
        fail("added already existing RegistrySubject");
      }
      catch (GrouperException eG) {
        assertTrue(true);
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToAddAlreadyExistingSubject()

  public void testFailToAddRegistrySubjectAsNonRoot() {
    LOG.info("testFailToAddRegistrySubjectAsNonRoot");
    try {
      RegistrySubject.add(
        GrouperSession.start( SubjectFinder.findAllSubject() ),
        "subj id", "subj type", "subj name"
      );
      fail("added hibernate subject as !root");
    }
    catch (InsufficientPrivilegeException eIP) {
      assertTrue(true);
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFailToAddRegistrySubjectAsNonRoot()

}

