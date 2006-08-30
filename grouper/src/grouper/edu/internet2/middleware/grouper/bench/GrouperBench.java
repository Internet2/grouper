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
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;      

/**
 * Run Grouper benchmarks.
 * @author  blair christensen.
 * @version $Id: GrouperBench.java,v 1.6 2006-08-30 18:35:38 blair Exp $
 * @since   1.1.0
 */
public class GrouperBench {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(GrouperBench.class);


  // MAIN //
  public static void main(String args[]) {
    int exit_value = 0;
    try {
      run(  new FindGrouperSystem()   );
      run(  new StartSession()        );
      run(  new StopSession()         );
      run(  new FindRootStem()        );
      run(  new AddRootStem()         );
      run(  new AddGroup()            );
      run(  new AddHibernateSubject() );
      run(  new AddImmMember()        );
      run(  new AddEffMember()        );
      run(  new Add10EffMembers()     );
      run(  new Add100EffMembers()    );
      run(  new AddUnionMember()      );
    }
    catch (GrouperRuntimeException eGRE) {
      LOG.fatal(eGRE.getMessage());
      exit_value = 1;
    }
    System.exit(exit_value);
  } // public static void main(args[])


  // PROTECTED CLASS METHODS //
  protected static void run(GrouperBenchmark bm) {
    int   cnt   = 1000;  // TODO Make this runtime configurable
    long  max   = Long.MIN_VALUE;
    long  min   = Long.MAX_VALUE;
    long  total = 0;
    for (int i = 0; i < cnt; i++) {
      RegistryReset.reset();
      bm.init();
      // TODO Why am I not reusing the StopWatch for the entire run and just
      //      calling `suspend()` and `resume()` at appropriate times?
      StopWatch sw = new StopWatch();
      sw.start();
      bm.run();
      sw.stop();  
      total += sw.getTime();
      if      (sw.getTime() > max) {
        max = sw.getTime(); 
      }
      else if (sw.getTime() < min) {
        min = sw.getTime();
      }
    }
    if (min < 0) {
      min = 0;
    }
    LOG.info(
        (total / cnt) + "\t" + bm.getClass().getName() + " (" + min + "/" + max + ")"
    );
  } // protected static void _run(bm)

} // public class GrouperBench
