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

package edu.internet2.middleware.grouper.prof;
import  edu.internet2.middleware.grouper.*;      

/**
 * Base class for setting up a profiling run.
 * @author  blair christensen.
 * @version $Id: BaseGrouperProfSetup.java,v 1.3 2007-01-04 17:17:45 blair Exp $
 * @since   1.1.0
 */
public class BaseGrouperProfSetup {
  // MAIN //
  public static void main(String args[]) {
    run();
  } // public static void main(args[])


  // PROTECTED CLASS METHODS //

  /**
   * @since 1.1.0
   */
  protected static void run() {
    RegistryReset.reset();
    System.exit(0); 
  } // protected static void run()

} // public class BaseGrouperProfSetup

