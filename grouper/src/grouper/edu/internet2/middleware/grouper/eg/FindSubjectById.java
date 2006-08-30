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

package edu.internet2.middleware.grouper.eg;
import  edu.internet2.middleware.grouper.*; // Import Grouper API
import  edu.internet2.middleware.subject.*; // Import Subject API
import  org.apache.commons.logging.*;       // For logging

/**
 * Example: Find {@link Subject} by <i>subject id</i>.
 * </p>
 * @author  blair christensen.
 * @version $Id: FindSubjectById.java,v 1.4 2006-08-30 19:31:02 blair Exp $
 * @since   1.1.0
 */
public class FindSubjectById {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(FindSubjectById.class);


  // MAIN //
  public static void main(String args[]) {
    int exit_value = 0;
    try {
      // We are looking for a subject with this *subject id*
      String  subjectId   = "SD00125";
      SubjectFinder.findById(subjectId);
      LOG.info("Found Subject by id: " + subjectId);
    }
    catch (SubjectNotFoundException   eSNF) {
      LOG.error(eSNF.getMessage());
      exit_value = 1;
    }
    catch (SubjectNotUniqueException  eSNU) {
      LOG.error(eSNU.getMessage());
      exit_value = 1;
    }
    System.exit(exit_value);
  } // public static void main(args[])

} // public class FindSubjectById

