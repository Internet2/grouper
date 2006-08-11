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
 * Example: Find the Groups Registry's <i>root</i> {@link Subject}:
 * <i>GrouperSystem</i>.
 * @author  blair christensen.
 * @version $Id: FindGrouperSystem.java,v 1.2 2006-08-11 18:50:49 blair Exp $
 * @since   1.0.1
 */
public class FindGrouperSystem {

  // PRIVATE CLASS CONSTANTS //
  private static final Log LOG = LogFactory.getLog(FindGrouperSystem.class);


  // MAIN //
  public static void main(String args[]) {
    int exit_value = 0;
    try {
      // We are looking for a subject with this *subject id*
      String  subjectId   = "GrouperSystem";
      // ... and this *subject type*
      String  subjectType = "application";
      // ... and that can be found within this *source*
      String  source      = InternalSourceAdapter.ID;
      Subject subj        = SubjectFinder.findById(
        subjectId, subjectType, source
      );
      LOG.info("Found GrouperSystem");
    }
    catch (SourceUnavailableException eSU)  {
      LOG.error(eSU.getMessage());
      exit_value = 1;
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

} // public class FindGrouperSystem

