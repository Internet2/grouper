/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  junit.framework.*;
import  net.sourceforge.groboutils.junit.v1.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;

/**
 * TODO Right now this is just me playing around until I figure out a) how to
 * properly use GroboUtils and b) figure out what to test.
 * @author  blair christensen.
 * @version $Id: TestThread0.java,v 1.3 2006-08-30 19:31:02 blair Exp $
 * @since   1.0
 */
public class TestThread0 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestThread0.class);
  private String get;

  public TestThread0(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
    try {
      R   r = R.populateRegistry(1, 0, 0);
      get   = r.ns.getName();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  private class GetGet 
    extends TestRunnable
  {
    private String  name;
    private String  get;
    private GetGet(int n, String get) {
      this.name = "thread." + n;
      this.get  = get;
    }
    public void runTest() throws Throwable {
      try {
        StopWatch       sw  = new StopWatch();
        sw.start();
        GrouperSession  s   = SessionHelper.getRootSession();
        // Sleep between 0-5 seconds
        long            l   = Math.round(1000 * (Math.random() * 5));
        Stem            ns  = StemFinder.findByName(s, get);
        ns.getName();
        s.stop();
        sw.stop();
        System.out.println(
          "[" + this.name + "] delay=" + l  + " runtime=" + sw.getTime() 
        );
      }
      catch (Exception e) {
        T.e(e);
      }
    }
  }

  public void testExampleThread()
    throws Throwable 
  {
    // Instantiate the TestRunnable classes 
    int             size  = 25;
    TestRunnable[]  trs   = new TestRunnable[size];
    for (int i=0; i< size; i++) {
      trs[i] = new GetGet(i, get);
    }
    // Pass to the MTTR
    MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(trs);
    // And fire off the threads
    mttr.runTestRunnables();
  }

  /**
   * Standard main() and suite() methods
   */
  public static void main (String[] args) {
    String[] name = { TestThread0.class.getName() };
    junit.textui.TestRunner.main(name);
  }

  public static Test suite() {
    return new TestSuite(TestThread0.class);
  }
} // public class TestThread0

