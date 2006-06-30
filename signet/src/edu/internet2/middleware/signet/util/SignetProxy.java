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

   public SignetProxy(String action, String adminIdentifier, String subsystemId)
   {
      try {

	      Signet signet = new Signet();
	      PrivilegedSubject signetSubject = signet.getSignetSubject();
	
	      if (action.equals("grant")) {
	
	         grant (signet, signetSubject, adminIdentifier, subsystemId);
	
	      } else if (action.equals("revoke")) {
	
	         revoke (signet, signetSubject, adminIdentifier, subsystemId);
	
	      } else if (action.equals("list")) {
	
	         list (signet, signetSubject, subsystemId);
	
	      }

      } catch (ObjectNotFoundException exc) {
         System.out.println("Error: " + exc.getMessage());
      } catch (Exception exc) {
         exc.printStackTrace();
      }

   }

   public static void main(String[] args) 
      {
      
      String action = "";
      String adminIdentifier = "";
      String subsystemId = "";
      
      boolean parsed = false;
      if (args.length > 0) {
         action = args[0];
         if (action.equalsIgnoreCase("grant") || action.equalsIgnoreCase("revoke")) {
            adminIdentifier = args[1];
            if (args.length == 3) {
               subsystemId = args[2];
            }
            parsed = true;
         } else if (action.equalsIgnoreCase("list")) {
            if (args.length == 2) {
               subsystemId = args[1];
            }
            parsed = true;
         } 
      }
 
      if (!parsed) {
         System.err.println("Usage: SignetProxy grant|revoke <subjectid> [<subsystemid>]");
         System.err.println("   or: SignetProxy list [<subsystemid>]");
         return;
      }

      new SignetProxy(action, adminIdentifier, subsystemId);

   }

   private void grant(Signet signet, PrivilegedSubject signetSubject, String adminIdentifier, String subsystemId)
      throws ObjectNotFoundException, SignetAuthorityException {

      PrivilegedSubject sysAdminSubject = signet.getSubjectSources().getPrivilegedSubjectByDisplayId(
    		  "person", adminIdentifier);
      
      Set proxiesSet = sysAdminSubject.getProxiesReceived();
      proxiesSet = filterProxies(proxiesSet, Status.ACTIVE);
      proxiesSet = filterProxiesByGrantor(proxiesSet, signetSubject);
   
      Date today = new Date();
      Calendar cal = Calendar.getInstance();  
      cal.setTime(today);
      
      if (subsystemId.equals("")) {
         proxiesSet = filterProxiesByNoSubsystem(proxiesSet);

         if (proxiesSet.size() > 0)
         {
            System.out.println("Signet system administration proxy already exists for " + adminIdentifier);
            return;
         }
   
         signet.getPersistentDB().beginTransaction();
   
         System.out.println("Granting Signet system administration proxy to " + adminIdentifier);
    
         Proxy sysAdminProxy = signetSubject.grantProxy(sysAdminSubject,null,false,true,today,null);
         sysAdminProxy.save();
   
         signet.getPersistentDB().commit();

      } else {
         Subsystem subsystem = signet.getPersistentDB().getSubsystem(subsystemId);
      
         proxiesSet = filterProxiesBySubsystem(proxiesSet, subsystem);
         if (proxiesSet.size() > 0)
         {
            System.out.println("Signet subsystem owner proxy already exists for " + adminIdentifier + " in subsystem " + subsystemId);
            return;
         }
   
         signet.getPersistentDB().beginTransaction();
   
         System.out.println("Granting Signet subsystem owner proxy to " + adminIdentifier + " for " + subsystemId);
    
         Proxy sysAdminProxy = signetSubject.grantProxy(sysAdminSubject,subsystem,false,true,today,null);
         sysAdminProxy.save();
   
         signet.getPersistentDB().commit();
      }
   }
      
   private void revoke(Signet signet, PrivilegedSubject signetSubject, String adminIdentifier, String subsystemId)
      throws ObjectNotFoundException, SignetAuthorityException {

      PrivilegedSubject sysAdminSubject = 
    	  signet.getSubjectSources().getPrivilegedSubjectByDisplayId("person", adminIdentifier);
      
      Set proxiesSet = sysAdminSubject.getProxiesReceived();
      proxiesSet = filterProxies(proxiesSet, Status.ACTIVE);
      proxiesSet = filterProxiesByGrantor(proxiesSet, signetSubject);

      if (subsystemId.equals("")) {
         proxiesSet = filterProxiesByNoSubsystem(proxiesSet);
   
         if (proxiesSet.size() == 0)         {
            System.out.println("No current Signet system administration proxy found for " + adminIdentifier);
            return;
         }
   
         signet.getPersistentDB().beginTransaction();
   
         System.out.println("Revoking Signet system administration proxy from " + adminIdentifier);
   
         // Should be only one Signet system administration proxy, but revoke all that exist
         Iterator proxiesIterator2 = proxiesSet.iterator();
         while (proxiesIterator2.hasNext())
         {
            Proxy sysAdminProxy = (Proxy)(proxiesIterator2.next());
            sysAdminProxy.revoke(signetSubject);
            sysAdminProxy.save();
         }
   
         signet.getPersistentDB().commit();

      } else {
         Subsystem subsystem = signet.getPersistentDB().getSubsystem(subsystemId);

         proxiesSet = filterProxiesBySubsystem(proxiesSet, subsystem);
   
         if (proxiesSet.size() == 0)
         {
            System.out.println("No current Signet subsystem owner proxy found for " + adminIdentifier + " for " + subsystemId);
            return;
         }
   
         signet.getPersistentDB().beginTransaction();
   
         System.out.println("Revoking Signet subsystem owner proxy from " + adminIdentifier + " for " + subsystemId);
   
         // Should be only one Signet subsystem owner proxy for a person, but revoke all that exist
         Iterator proxiesIterator2 = proxiesSet.iterator();
         while (proxiesIterator2.hasNext())
         {
            Proxy sysAdminProxy = (Proxy)(proxiesIterator2.next());
            sysAdminProxy.revoke(signetSubject);
            sysAdminProxy.save();
         }
   
      signet.getPersistentDB().commit();
      }
   }

   private void list (Signet signet, PrivilegedSubject signetSubject, String subsystemId)
      throws ObjectNotFoundException {

      Set proxiesSet = signetSubject.getProxiesGranted();
      proxiesSet = filterProxies(proxiesSet, Status.ACTIVE);

      if (subsystemId.equals("")) {
         proxiesSet = filterProxiesByNoSubsystem(proxiesSet);

         if (proxiesSet.size() == 0)
         {
            System.out.println("No current Signet system administrative proxies found");
            return;
         }

         System.out.println("Current Signet system administration proxies:");

         Iterator proxiesIterator = proxiesSet.iterator();
         while (proxiesIterator.hasNext())
         {
            Proxy sysAdminProxy = (Proxy)(proxiesIterator.next());
            PrivilegedSubject grantee = sysAdminProxy.getGrantee();
            System.out.println("   " + grantee.getName());
         }
      } else {
         Subsystem subsystem = signet.getPersistentDB().getSubsystem(subsystemId);
      
         proxiesSet = filterProxiesBySubsystem(proxiesSet, subsystem);

         if (proxiesSet.size() == 0)
         {
            System.out.println("No current Signet subsystem owner proxies found for " + subsystemId);
            return;
         }

         System.out.println("Current Signet subsystem owner proxies for " + subsystemId + ":");

         Iterator proxiesIterator = proxiesSet.iterator();
         while (proxiesIterator.hasNext())
         {
            Proxy sysAdminProxy = (Proxy)(proxiesIterator.next());
            PrivilegedSubject grantee = sysAdminProxy.getGrantee();
            System.out.println("   " + grantee.getName());
         }

      }

   }

   private Set filterProxies(Set all, Status status)
   {
     Set statusSet = new HashSet();
     statusSet.add(status);
     return filterProxies(all, statusSet);
   }

   private Set filterProxies(Set all, Set statusSet)
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
   
   private Set filterProxiesByGrantor (Set all, PrivilegedSubject grantor)
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
   
   private Set filterProxiesBySubsystem (Set all, Subsystem subsystem)
   {
     
     Set subset = new HashSet();
     Iterator iterator = all.iterator();
     while (iterator.hasNext())
     {
       Proxy candidate = (Proxy)(iterator.next());
       if (subsystem.equals(candidate.getSubsystem()))
       {
         subset.add(candidate);
       }
     }
     
     return subset;
   }
   
   private Set filterProxiesByNoSubsystem (Set all)
   {
     
     Set subset = new HashSet();
     Iterator iterator = all.iterator();
     while (iterator.hasNext())
     {
       Proxy candidate = (Proxy)(iterator.next());
       if (candidate.getSubsystem() == null)
       {
         subset.add(candidate);
       }
     }
     
     return subset;
   }

}
