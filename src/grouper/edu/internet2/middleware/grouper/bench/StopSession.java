/*
  Copyright 2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006 The University Of Chicago

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

package edu.internet2.middleware.grouper.bench;
import  edu.internet2.middleware.grouper.*; 
import  edu.internet2.middleware.subject.*; 
import  org.apache.commons.logging.*;      

/**
 * Benchmark starting a {@link GrouperSession}.
 * @author  blair christensen.
 * @version $Id: StopSession.java,v 1.1 2006-08-15 15:32:21 blair Exp $
 * @since   1.1.0
 */
public class StopSession extends BaseGrouperBenchmark {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(StopSession.class);


  // PRIVATE INSTANCE VARIABLES
  GrouperSession s;


  // MAIN //
  public static void main(String args[]) {
    BaseGrouperBenchmark gb = new StopSession();
    gb.benchmark();
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected StopSession() {
    super();
  } // protected StopSession()

  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperRuntimeException 
  {
    try {
      this.s = GrouperSession.start(
        SubjectFinder.findById("GrouperSystem")
      );
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e.getMessage());
    }
  } // public void init()

  /**
   * @since 1.1.0
   */
  public void run() 
    throws GrouperRuntimeException 
  {
    try {
      this.s.stop();
    }
    catch (Exception e) {
      throw new GrouperRuntimeException(e);
    }
  } // public void run()

} // public class StopSession

