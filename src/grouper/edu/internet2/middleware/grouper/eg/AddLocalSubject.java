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
import  net.sf.hibernate.*;

/**
 * Example: Add a {@link JDBCSubject} to the local Groups Registry.
 * @author  blair christensen.
 * @version $Id: AddLocalSubject.java,v 1.1 2006-08-04 19:02:11 blair Exp $
 * @since   1.0.1
 */
public class AddLocalSubject {

  // MAIN //
  public static void main(String args[]) {
    try {
      GrouperSession  s     = GrouperSession.start(
        SubjectFinder.findById(
          "GrouperSystem", "application", InternalSourceAdapter.ID
        )
      );

      // We want to add a subject with this *subject id*
      String  subjectId   = "SD00125";
      // ... and of this *subject type*
      String  subjectType = "person";
      // ... and with this *name*
      String  subjectName = "John Doe";
      try {
        HibernateSubject hsubj = HibernateSubject.add(subjectId, subjectType, subjectName);
        EgLog.info(AddLocalSubject.class, "Added local Subject: " + subjectId);
      }
      catch (HibernateException eH) {
        EgLog.error(AddLocalSubject.class, "Failed to add local Subject: " + eH.getMessage());
        System.exit(1);
      } 
    
      s.stop();
    }
    catch (Exception e) {
      EgLog.error(AddLocalSubject.class, "UNEXPECTED ERROR: " + e.getMessage());
      System.exit(1);
    }
    System.exit(0);
  } // public static void main(args[])

} // public class AddLocalSubject

