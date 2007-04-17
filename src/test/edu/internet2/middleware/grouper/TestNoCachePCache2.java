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

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.grouper.internal.cache.BasePrivilegeCache;
import  edu.internet2.middleware.grouper.internal.cache.NoCachePrivilegeCache;
import  edu.internet2.middleware.grouper.internal.cache.PrivilegeCache;
import  edu.internet2.middleware.grouper.internal.cache.PrivilegeCacheElement;
import  edu.internet2.middleware.subject.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestNoCachePCache2.java,v 1.6 2007-04-17 17:13:27 blair Exp $
 * @since   1.1.0
 */
public class TestNoCachePCache2 extends GrouperTest {

  private static final Log LOG = LogFactory.getLog(TestNoCachePCache2.class);

  public TestNoCachePCache2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testPutOk() {
    LOG.info("testPutOk");
    try {
      Class                 klass = NoCachePrivilegeCache.class;
      PrivilegeCache        pc    = BasePrivilegeCache.getCache( klass.getName() );
      GrouperSession        s     = GrouperSession.start( SubjectFinder.findRootSubject() );
      Stem                  ns    = StemFinder.findRootStem(s);
      Subject               all   = SubjectFinder.findAllSubject();
      Privilege             p     = NamingPrivilege.STEM;

      // Put
      pc.put(ns, all, p, true);
      assertTrue(true);
      // Get
      PrivilegeCacheElement el    = pc.get(ns, all, p);
      assertFalse( el.getIsCached() );

      s.stop();
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testPutOk()

} // public class TestNoCachePCache2

