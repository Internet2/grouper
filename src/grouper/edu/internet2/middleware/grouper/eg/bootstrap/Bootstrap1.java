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

package edu.internet2.middleware.grouper.eg.bootstrap;
import  edu.internet2.middleware.grouper.*; // Import Grouper API
import  edu.internet2.middleware.subject.*; // Import Subject API
import  org.apache.commons.logging.*;       // For logging

/**
 * Step 1: Find <i>GrouperSystem</i> subject.
 * <p>
 * This example will demonstrate how to use the Grouper API to find a
 * <code>Subject</code>.  As most API operations require a {@link GrouperSession} 
 * to operate within, you will typically begin by retrieving a subject that you
 * may then use to start a session.
 * </p>
 * <p>
 * The first step you will need to take is to import <b>both</b> the Grouper API
 * and the Subject API:
 * </p>
 * <pre class="eg">
 * import  edu.internet2.middleware.grouper.*; // Import Grouper API
 * import  edu.internet2.middleware.subject.*; // Import Subject API
 * </pre>
 * <p>
 * To bootstrap your Groups Registry you will need to act as the
 * <i>GrouperSystem</i> subject.  This is a subject provided by Grouper and that
 * has full privileges over the entire Groups Registry.  In addition, there are
 * a few operations wihin the API that can <b>only</b> be performed by either
 * <i>GrouperSystem</i> or members of the wheel group.
 * </p>
 * <p>
 * To find <i>GrouperSystem</i> we will use the {@link SubjectFinder} class.
 * While that class provides a number of different ways of retrieving subjects I
 * will use the simplest for this example by retrieving the subject by its
 * subject identifier.
 * </p>
 * <pre class="eg">
 * String  subjectId = "GrouperSystem";
 * Subject root      = SubjectFinder.findById(subjectId);
 * </pre>
 * <p>If Grouper is able to retrieve <i>GrouperSystem</i> {@link SubjectFinder}
 * will return a <code>Subject</code> object that you can use as a method
 * argument.  If the subject cannot be retrieved an exception will be thrown:  
 * </p>
 * <pre class="eg">
 * catch (SubjectNotFoundException   eSNF) {
 *   // No matching subject id found
 * }
 * catch (SubjectNotUniqueException  eSNU) {
 *   // More than one subject with this subject id was found
 * }
 * </pre>
 * <p>
 * <b>NOTE:</b> The Grouper 1.1 release will include a shortcut method for
 * retrieving <i>GrouperSystem</i>: {@link SubjectFinder#findRootSubject()}.
 * </p>
 * @author  blair christensen.
 * @version $Id: Bootstrap1.java,v 1.1 2006-09-08 19:17:59 blair Exp $
 * @see     <a href="http://viewvc.internet2.edu/viewvc.py/grouper/src/grouper/edu/internet2/middleware/grouper/eg/bootstrap/Bootstrap1.java?root=I2MI&view=markup">Source</a>
 * @since   1.1.0
 */
public class Bootstrap1 {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(Bootstrap1.class);


  // MAIN //
  public static void main(String args[]) {
    int exit_value = 1; // Indicate failure by default
    try {
      String  subjectId = "GrouperSystem";
      Subject root      = SubjectFinder.findById(subjectId);
      LOG.info("Found GrouperSystem: " + root.getId());
      exit_value = 0;
    }
    catch (SubjectNotFoundException   eSNF) {
      // No matching subject id found
      LOG.error(eSNF.getMessage());
    }
    catch (SubjectNotUniqueException  eSNU) {
      // More than one subject with this subject id was found
      LOG.error(eSNU.getMessage());
    }
    System.exit(exit_value);
  } // public static void main(args[])

} // public class Bootstrap1

