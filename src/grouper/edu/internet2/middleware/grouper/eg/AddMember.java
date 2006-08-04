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
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;

/**
 * Example: Make a {@link Subject} a {@link Member} of a {@link Group}.
 * @author  blair christensen.
 * @version $Id: AddMember.java,v 1.1 2006-08-04 19:02:11 blair Exp $
 * @since   1.0.1
 */
public class AddMember {

  // MAIN //
  public static void main(String args[]) {
    try {
      GrouperSession  s     = GrouperSession.start(
        SubjectFinder.findById(
          "GrouperSystem", "application", InternalSourceAdapter.ID
        )
      );
      Group           g     = GroupFinder.findByName(s, "etc:wheel");
      Subject         subj  = SubjectFinder.findById("SD00125");

      try {
        g.addMember(subj);
        EgLog.info(AddMember.class, "Added Member: " + subj.getId()); 
      }
      catch (InsufficientPrivilegeException eIP) {
        EgLog.error(AddMember.class, "Failed to add Member: " + eIP.getMessage());
        System.exit(1);
      }
      catch (MemberAddException eMA) {
        EgLog.error(AddMember.class, "Failed to add Member: " + eMA.getMessage());
        System.exit(1);
      }

      s.stop();
    }
    catch (Exception e) {
      EgLog.error(AddMember.class, "UNEXPECTED ERROR: " + e.getMessage());
      System.exit(1);
    }
    System.exit(0);
  } // public static void main(args[])

} // public class AddMember

