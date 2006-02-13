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

import edu.internet2.middleware.signet.*;
import edu.internet2.middleware.signet.Status;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AssignmentReconcile
{

   public AssignmentReconcile()
   {
      // Nothing to do here
   }

   public static void main(String[] args) 
      {
      
      String reconcileDate = "";
      
      try {

         AssignmentReconcile reconcile = new AssignmentReconcile();

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

         Signet signet = new Signet();

         processReconcile (signet, reconcileDate);

      } catch (java.text.ParseException exc) {
         System.out.println("Error: " + exc.getMessage());
      } catch (Exception exc) {
         exc.printStackTrace();
      }

   }

   private static void processReconcile(Signet signet, String reconcileDate)
      throws java.text.ParseException {
      
      if (reconcileDate.equals("")) {
   
         signet.reconcile();

      } else {

         SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
         Date theDate = dateFormatter.parse(reconcileDate);   

         signet.reconcile(theDate);
 
      }
   }

}
