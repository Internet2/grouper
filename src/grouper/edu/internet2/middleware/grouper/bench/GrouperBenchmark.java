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

package edu.internet2.middleware.grouper.bench;
import  edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;

/** 
 * Interface for writing Grouper benchmarks.
 * @author  blair christensen.
 * @version $Id: GrouperBenchmark.java,v 1.3 2008-07-21 04:43:58 mchyzer Exp $
 * @since   1.1.0
 */
public interface GrouperBenchmark {

  // PUBLIC INSTANCE METHODS //

  /**
   * Perform any initialization necessary for this benchmark.
   * @since 1.1.0
   */
  void init() throws GrouperRuntimeException;

  /**
   * Run benchmark.
   * @since 1.1.0
   */
  void run() throws GrouperRuntimeException;

} // public interface GrouperBenchmark

