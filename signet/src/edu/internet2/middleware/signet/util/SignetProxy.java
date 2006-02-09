/*
SignetProxy.java
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

import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
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
      PrivilegedSubject signetSubject = signet.getSignetSubject();

      if (action.equals("grant")) {

         grant(signet, signetSubject, sysAdminIdentifier);

      } else if (action.equals("revoke")) {

         revoke(signet, signetSubject, sysAdminIdentifier);

      } else if (action.equals("list")) {

         list(signetSubject);

      }

      } catch (ObjectNotFoundException exc) {
         System.out.println("Error: " + exc.getMessage());
      } catch (Exception exc) {
         exc.printStackTrace();
      }

   }

   private static void grant(Signet signet, PrivilegedSubject signetSubject, String sysAdminIdentifier)
      throws ObjectNotFoundException, SignetAuthorityException {

      PrivilegedSubject sysAdminSubject = signet.getPrivilegedSubjectByDisplayId("person", sysAdminIdentifier);
      
      Set proxiesSet = sysAdminSubject.getProxiesReceived();
      proxiesSet = filterProxies(proxiesSet, Status.ACTIVE);
      proxiesSet = filterProxiesByGrantor(proxiesSet, signetSubject);
      
      if (proxiesSet.size() > 0)
      {
         System.out.println("Signet system administration proxy already exists for " + sysAdminIdentifier);
         return;
      }

      signet.beginTransaction();

      System.out.println("Granting Signet system administration proxy to " + sysAdminIdentifier);

      Date today = new Date();
      Calendar cal = Calendar.getInstance();  
      cal.setTime(today);
 
      Proxy sysAdminProxy = signetSubject.grantProxy(sysAdminSubject,null,false,true,today,null);
      sysAdminProxy.save();

      signet.commit();

   }
      
   private static void revoke(Signet signet, PrivilegedSubject signetSubject, String sysAdminIdentifier)
      throws ObjectNotFoundException, SignetAuthorityException {

      PrivilegedSubject sysAdminSubject = signet.getPrivilegedSubjectByDisplayId("person", sysAdminIdentifier);
      
      Set proxiesSet = sysAdminSubject.getProxiesReceived();
      proxiesSet = filterProxies(proxiesSet, Status.ACTIVE);
      proxiesSet = filterProxiesByGrantor(proxiesSet, signetSubject);

      int proxyCount = 0;
      Iterator proxiesIterator = proxiesSet.iterator();
      while (proxiesIterator.hasNext())
      {
         Proxy sysAdminProxy = (Proxy)(proxiesIterator.next());
         if (sysAdminProxy.getSubsystem() == null) {
            proxyCount++;
         }
      }

      if (proxyCount == 0)
      {
         System.out.println("No current Signet proxy found for " + sysAdminIdentifier);
         return;
      }

      signet.beginTransaction();

      System.out.println("Revoking Signet system administration proxy from " + sysAdminIdentifier);

      // Should be only one Signet system administration proxy, but revoke all that exist
      Iterator proxiesIterator2 = proxiesSet.iterator();
      while (proxiesIterator2.hasNext())
      {
         Proxy sysAdminProxy = (Proxy)(proxiesIterator2.next());
         if (sysAdminProxy.getSubsystem() == null) {
            sysAdminProxy.revoke(signetSubject);
            sysAdminProxy.save();
         }
      }

      signet.commit();
   }

   private static void list(PrivilegedSubject signetSubject)
   {
      System.out.println("Current Signet system administration proxies:");

      Set proxiesSet = signetSubject.getProxiesGranted();
      proxiesSet = filterProxies(proxiesSet, Status.ACTIVE);

      if (proxiesSet.size() == 0)
      {
         System.out.println("No current Signet proxies found");
         return;
      }

      Iterator proxiesIterator = proxiesSet.iterator();
      while (proxiesIterator.hasNext())
      {
         Proxy sysAdminProxy = (Proxy)(proxiesIterator.next());
         if (sysAdminProxy.getSubsystem() == null) {
            PrivilegedSubject grantee = sysAdminProxy.getGrantee();
            System.out.println("   " + grantee.getName());
         }
      }
   }

   private static Set filterProxies(Set all, Status status)
   {
     Set statusSet = new HashSet();
     statusSet.add(status);
     return filterProxies(all, statusSet);
   }

   private static Set filterProxies(Set all, Set statusSet)
   {
     if (statusSet == null)
     {
       return all;
     }

     Set subset = new HashSet();
     Iterator iterator = all.iterator();
     while (iterator.hasNext())
     {
       Proxy candidate = (Proxy) (iterator.next());
       if (statusSet.contains(candidate.getStatus()))
       {
         subset.add(candidate);
       }
     }

     return subset;
   }
   
   private static Set filterProxiesByGrantor
     (Set                all,
      PrivilegedSubject  grantor)
   {
     if (grantor == null)
     {
       return all;
     }
     
     Set subset = new HashSet();
     Iterator iterator = all.iterator();
     while (iterator.hasNext())
     {
       Proxy candidate = (Proxy)(iterator.next());
       if (candidate.getGrantor().equals(grantor))
       {
         subset.add(candidate);
       }
     }
     
     return subset;
   }
}
