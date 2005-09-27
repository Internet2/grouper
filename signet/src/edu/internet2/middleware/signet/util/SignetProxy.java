/*
SignetProxy.java
Created on Sep 12, 2005

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/

package edu.internet2.middleware.signet.util;

import edu.internet2.middleware.signet.*;
import edu.internet2.middleware.signet.Proxy.*;
import edu.internet2.middleware.signet.Status;

import java.sql.SQLException;
import java.util.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

public class SignetProxy
{

   public SignetProxy()
   {
      // Nothing to do here
   }

   public static void main(String[] args) 
      {
      
      String action = "";
      String sysAdminIdentifier = "";
      PrivilegedSubject sysAdminSubject = null;
      
      try {

      SignetProxy signetProxy = new SignetProxy();

      boolean parsed = false;
      if (args.length == 2) {
         action = args[0];
         sysAdminIdentifier = args[1];
         if (action.equals("grant") || action.equals("revoke")) {
            parsed = true;
         }
      } else if (args.length == 1) {
         action = args[0];
         if (action.equals("list")) {
            parsed = true;
         }
      } 
      
      if (!parsed) {
         System.err.println("Usage: SignetProxy grant|revoke <subjectid>");
         System.err.println("   or: SignetProxy list");
         return;
      }

      Signet signet = new Signet();
      signet.beginTransaction();

      PrivilegedSubject signetSubject = signet.getSignetSubject();
      if (!sysAdminIdentifier.equals("")) {
         sysAdminSubject = signet.getPrivilegedSubjectByDisplayId("person", sysAdminIdentifier);
      }

      if (action.equals("grant")) {

         Set proxiesSet = sysAdminSubject.getProxiesReceived(Status.ACTIVE, null, signetSubject);

         if (proxiesSet.size() > 0)
         {
            System.out.println("Signet system administration proxy already exists for " + sysAdminIdentifier);
            return;
         }

         System.out.println("Granting Signet system administration proxy to " + sysAdminIdentifier);

         Date today = new Date();
         Calendar cal = Calendar.getInstance();  
         cal.setTime(today);
 
         Proxy sysAdminProxy = signetSubject.grantProxy(sysAdminSubject,null,false,true,today,null);
         sysAdminProxy.save();

      } else if (action.equals("revoke")) {

         Set proxiesSet = sysAdminSubject.getProxiesReceived(Status.ACTIVE, null, signetSubject);

         if (proxiesSet.size() != 1)
         {
            System.out.println("No current Signet proxy found for " + sysAdminIdentifier);
            return;
         }

         System.out.println("Revoking Signet system administration proxy from " + sysAdminIdentifier);


         Iterator proxiesIterator = proxiesSet.iterator();
         while (proxiesIterator.hasNext())
         {
            Proxy sysAdminProxy = (Proxy)(proxiesIterator.next());

            sysAdminProxy.revoke(signetSubject);
            sysAdminProxy.save();
         }

      } else if (action.equals("list")) {

         System.out.println("Current Signet system administration proxies:");

         Set proxiesSet = signetSubject.getProxiesGranted(Status.ACTIVE, null, null);

         if (proxiesSet.size() == 0)
         {
            System.out.println("No current Signet proxies found");
            return;
         }

         Iterator proxiesIterator = proxiesSet.iterator();
         while (proxiesIterator.hasNext())
         {
            Proxy sysAdminProxy = (Proxy)(proxiesIterator.next());
            PrivilegedSubject grantee = sysAdminProxy.getGrantee();
            System.out.println("   " + grantee.getName());
         }

      }

      signet.commit();

      } catch (Exception e) {
         e.printStackTrace();
      }
 
   }

}
