/*
AssignmentReconcile.java
Created on Sep 12, 2005

Copyright 2006 Internet2, Stanford University

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

package edu.internet2.middleware.signet.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import edu.internet2.middleware.signet.Signet;

public class AssignmentReconcile
{

   public AssignmentReconcile(String reconcileDate)
   {
      try {

         Signet signet = new Signet();
         signet.getPersistentDB().beginTransaction();

         processReconcile (signet, reconcileDate);
         signet.getPersistentDB().commit();

      } catch (java.text.ParseException exc) {
         System.out.println("Error: " + exc.getMessage());
      } catch (Exception exc) {
         exc.printStackTrace();
      }
   }

   public static void main(String[] args) 
   {
      String reconcileDate = "";

         boolean parsed = false;
         if (args.length == 0) {
            parsed = true;
         } else if (args.length == 1) {
            reconcileDate = args[0];
            parsed = true;
         }
      
         if (!parsed) {
            System.err.println("Usage: Reconcile [<date>]");
            return;
         }

         new AssignmentReconcile(reconcileDate);


   }

   private void processReconcile(Signet signet, String reconcileDate)
      throws java.text.ParseException {
      
      if (reconcileDate.equals("")) {
   
         Set reconciledSet = signet.reconcile();
         System.out.println("- Assignments affected: " + reconciledSet.size());

      } else {

         SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");
         Date theDate = dateFormatter.parse(reconcileDate);   

         Set reconciledSet = signet.reconcile(theDate);
         System.out.println("- Assignments affected: " + reconciledSet.size());
 
      }
   }

}
