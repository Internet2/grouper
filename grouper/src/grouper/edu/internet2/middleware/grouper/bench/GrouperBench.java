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
/*
  Copyright (C) 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2006-2007 The University Of Chicago

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
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Run Grouper benchmarks.
 * @author  blair christensen.
 * @version $Id: GrouperBench.java,v 1.23 2009-03-15 06:37:22 mchyzer Exp $
 * @since   1.1.0
 */
public class GrouperBench {

  // PRIVATE CLASS CONSTANTS //
  private static final int    RUNSIZE_DEFAULT = 100;
  private static final String RUNSIZE_VAR     = "GROUPER_BENCH_RUNSIZE";
  private static final Log    LOG             = GrouperUtil.getLog(GrouperBench.class);


  // PRIVATE CLASS VARIABLES //
  private static int runSize = Integer.MIN_VALUE;


  // MAIN //

  // @since   1.1.0
  public static void main(String args[]) {
    int exit_value = 0;
    try {
      run( new FindGrouperSystem()            );
      run( new StartSession()                 );
      run( new StopSession()                  );
      run( new FindRootStem()                 );
      run( new AddRootStem()                  );
      run( new AddGroup()                     );
      run( new AddRegistrySubject()          );
      run( new AddImmMember()                 );
      run( new AddEffMember()                 );
      run( new Add10EffMembers()              );
      run( new Add100EffMembers()             );
      run( new AddUnionMember()               );
      run( new Add10UnionMembers()            );
      run( new Add100UnionMembers()           );
      run( new FindExistingMemberBySubject()  );
      run( new XmlExportEmpty()               );
      run( new XmlImportEmpty()               );
      run( new XmlUpdateEmpty()               );
      run( new MemberHasRead()                );
      run( new AddAsEffMemberToLotsOfGroups() );
      run( new AddLotsOfGroupsToStem()        );
    }
    catch (GrouperException eGRE) {
      LOG.fatal(eGRE.getMessage());
      exit_value = 1;
    }
    System.exit(exit_value);
  } // public static void main(args[])


  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static void run(GrouperBenchmark bm) {
    int                   cnt   = _getRunSize();
    DescriptiveStatistics stats = new DescriptiveStatistics();
    for (int i = 0; i < cnt; i++) {
      RegistryReset.reset();
      bm.init();
      StopWatch sw = new StopWatch();
      sw.start();
      bm.run();
      sw.stop();  
      stats.addValue(sw.getTime());
    }
    LOG.info(
        "runs: "        + runSize 
      + "  median: "    + _toLong(stats.getPercentile(50))
      + "  mean: "      + _toLong(stats.getMean()) 
      + "  deviation: " + _toLong(stats.getStandardDeviation()) 
      + "  min: "       + _toLong(stats.getMin()) 
      + "  max: "       + _toLong(stats.getMax())
      + "  " + bm.getClass().getName()
    );
  } // protected static void _run(bm)


  // PRIVATE CLASS METHODS //
  
  // @since   1.1.0
  private static int _getRunSize() {
    if (runSize == Integer.MIN_VALUE) {
      String size = System.getenv(RUNSIZE_VAR);
      if (size == null) {
        runSize = RUNSIZE_DEFAULT;
      }
      else {
        runSize = Integer.parseInt(size);
      }
    }
    return runSize;
  } // private static int _getRunSize()

  // @since   1.1.0
  private static long _toLong(double d) {
    return new Double(d).longValue();
  } // private static long _toLong(d)
  
} // public class GrouperBench
