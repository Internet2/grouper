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
import  org.apache.commons.logging.*;      

/**
 * Base {@link GrouperBenchark} implementation.
 * @author  blair christensen.
 * @version $Id: BaseGrouperBenchmark.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 * @since   1.1.0
 */
public class BaseGrouperBenchmark implements GrouperBenchmark {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(BaseGrouperBenchmark.class);


  // MAIN //
  public static void main(String args[]) {
    LOG.fatal("ERROR: No main() defined for benchmark!");
    System.exit(1);
  } // public static void main(args[])


  // CONSTRUCTORS

  /**
   * @since 1.1.0
   */
  protected BaseGrouperBenchmark() {
    super();
  } // protected BaseGrouperBenchmark()


  // PUBLIC INSTANCE METHODS //

  /**
   * @since 1.1.0
   */
  public void benchmark() {
    int exit_value = 0;
    try {
      GrouperBench.run( this );
    }
    catch (GrouperRuntimeException eGRE) {
      LOG.fatal(eGRE.getMessage());
      exit_value = 1;
    }
    System.exit(exit_value);
  } // public void benchmark()

  /**
   * @since 1.1.0
   */
  public void init() 
    throws GrouperRuntimeException 
  {
    // Nothing
  } // public void init()

  /**
   * @since 1.1.0
   */
  public void run() 
    throws GrouperRuntimeException 
  {
    // Nothing
  } // public void run()

} // public class BaseGrouperBenchmark

