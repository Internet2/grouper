/*--
$Id: ProxyTest.java,v 1.7 2007-02-24 02:11:32 ddonn Exp $
$Date: 2007-02-24 02:11:32 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import junit.framework.TestCase;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.subjsrc.SignetAppSource;
import edu.internet2.middleware.signet.subjsrc.SignetSubject;

public class ProxyTest extends TestCase
{
  private Signet		signet;
// not used
//  private Fixtures	fixtures;
  protected HibernateDB hibr;
  protected Session hs;
  protected Transaction tx;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(ProxyTest.class);
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    
    signet = new Signet();
    hibr = signet.getPersistentDB();
    hs = hibr.openSession();
    tx = hs.beginTransaction();
    /* fixtures = */ new Fixtures(signet);
    tx.commit();
    hibr.closeSession(hs);
    
    // Let's use a new Signet session, to make sure we're actually
    // pulling data from the database, and not just referring to in-memory
    // structures.
    signet = new Signet();
    hibr = signet.getPersistentDB();
    hs = hibr.openSession();
    tx = hs.beginTransaction();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    tx.commit();
    hibr.closeSession(hs);
  }

  /**
   * Constructor for ProxyTest.
   * @param name
   */
  public ProxyTest(String name)
  {
    super(name);
  }
  
  public final void testRevoke()
  throws
    ObjectNotFoundException,
    SignetAuthorityException
  {
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
//      Subject subject
//        = signet.getSubjectSources().getSubject
//            (Signet.DEFAULT_SUBJECT_TYPE_ID,
//             Common.makeSubjectId(subjectIndex));
      
	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));
      
      Proxy proxy = (Proxy)(proxiesReceived.toArray()[0]);
      
      SignetSubject revoker;
      
      if (proxy.getProxy() == null)
      {
        revoker = proxy.getGrantor();
      }
      else
      {
        revoker = proxy.getProxy();
        revoker.setActingAs(proxy.getGrantor());
      }
      
      // We can't alter an already-inactive Proxy, but we can alter any others.
      if (!(proxy.getStatus().equals(Status.INACTIVE)))
      {
        proxy.revoke(revoker);
        hibr.save(hs, proxy);
      }
    }
  }
  
  public final void testFindDuplicates()
  throws
    SignetAuthorityException,
    ObjectNotFoundException
  {
    for (int subjectIndex = 0;
    subjectIndex < Constants.MAX_SUBJECTS;
    subjectIndex++)
    {
//      Subject subject
//        = signet.getSubjectSources().getSubject
//            (Signet.DEFAULT_SUBJECT_TYPE_ID,
//             Common.makeSubjectId(subjectIndex));
 
	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      assertNotNull(proxiesReceived);
      
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));
      
      
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxyReceived
          = (Proxy)(proxiesReceivedIterator.next());
        
        // At this point, there should be no duplicate Proxies.
        assertEquals(0, proxyReceived.findDuplicates().size());
        
        // Let's make a duplicate.
        Proxy duplicateProxy
          = Common.getOriginalGrantor(proxyReceived)
              .grantProxy
                (proxyReceived.getGrantee(),
                 proxyReceived.getSubsystem(),
                 proxyReceived.canUse(),
                 proxyReceived.canExtend(),
                 Constants.TODAY,  // EffectiveDate and expirationDate are not
                 Constants.TOMORROW); // considered when finding duplicates.
        hibr.save(hs, duplicateProxy);
        
        // At this point, there should be exactly one duplicate Proxy.
        assertEquals(1, proxyReceived.findDuplicates().size());
      }
    }
  }

  public final void testSetEffectiveDate()
  throws
    SignetAuthorityException,
    ObjectNotFoundException
  {    
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
//      Subject subject
//        = signet.getSubjectSources().getSubject
//            (Signet.DEFAULT_SUBJECT_TYPE_ID,
//             Common.makeSubjectId(subjectIndex));
      
	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single effectiveDate for every received Proxy.
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());

        Date originalEffectiveDate = proxy.getEffectiveDate();
        
        assertEquals
          (Constants.YESTERDAY, originalEffectiveDate);
        
        // Update the Proxy with the altered effectiveDate.
        SignetSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setEffectiveDate
          (grantor,
           Constants.DAY_BEFORE_YESTERDAY, true);
        hibr.save(hs, proxy);
      }
      
      // Examine every single altered EffectiveDate for every received
      // Proxy, and set them back to their original values.
      proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());
        
        Date alteredEffectiveDate = proxy.getEffectiveDate();
        
        assertEquals
          (Constants.DAY_BEFORE_YESTERDAY, alteredEffectiveDate);
        
        // Update the Proxy with the restored original effectiveDate.
        SignetSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setEffectiveDate
          (grantor,
           Constants.YESTERDAY, true);
        hibr.save(hs, proxy);
      }
    }
  }

  public final void testSetExpirationDate()
  throws
    SignetAuthorityException,
    ObjectNotFoundException
  {
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
//      Subject subject
//        = signet.getSubjectSources().getSubject
//            (Signet.DEFAULT_SUBJECT_TYPE_ID,
//             Common.makeSubjectId(subjectIndex));
      
	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single expirationDate for every received Proxy.
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());

        Date originalExpirationDate = proxy.getExpirationDate();
        
        assertEquals
          (Constants.TOMORROW, originalExpirationDate);
        
        // Update the Proxy with the altered expirationDate.
        SignetSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setExpirationDate
          (grantor,
           Constants.DAY_AFTER_TOMORROW, true);
        hibr.save(hs, proxy);
      }
      
      // Examine every single altered expirationDate for every received
      // Proxy, and set them back to their original values.
      proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());
        
        Date alteredExpirationDate = proxy.getExpirationDate();
        
        assertEquals
          (Constants.DAY_AFTER_TOMORROW, alteredExpirationDate);
        
        // Update the Proxy with the restored original expirationDate.
        SignetSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setExpirationDate
          (grantor,
           Constants.TOMORROW, true);
        hibr.save(hs, proxy);
      }
    }
  }
  
  public final void testEvaluate()
  throws
    ObjectNotFoundException,
    SignetAuthorityException
  {
    Proxy proxy = null;
    
//    Subject subject0
//      = signet.getSubjectSources().getSubject
//          (Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(0));

	SignetSubject pSubject0 = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(0));
//    PrivilegedSubject pSubject0 = signet.getSubjectSources().getPrivilegedSubject(subject0);
    
    // Get any one of subject0's proxies - we don't care which one.
    Set proxiesReceived = pSubject0.getProxiesReceived();
    Iterator proxiesReceivedIterator = proxiesReceived.iterator();
    while (proxiesReceivedIterator.hasNext())
    {
      proxy = (Proxy)(proxiesReceivedIterator.next());
    }
    
    assertNotNull(proxy);
    assertNotSame(Status.INACTIVE, proxy.getStatus());
    
    Date lastWeek  = Common.getDate(-7);
    Date nextWeek = Common.getDate(7);
    
    SignetSubject grantor = Common.getOriginalGrantor(proxy);

    proxy.checkEditAuthority(grantor); // throws on error
    proxy.setEffectiveDate(grantor, Constants.YESTERDAY, false);
    proxy.setExpirationDate(grantor, Constants.TOMORROW, false);
    proxy.evaluate();
    assertEquals(Status.ACTIVE, proxy.getStatus());
    
    proxy.setEffectiveDate(grantor, Constants.TOMORROW, false);
    proxy.setExpirationDate(grantor, nextWeek, false);
    proxy.evaluate();
    assertEquals(Status.PENDING, proxy.getStatus());
    
    proxy.setEffectiveDate(grantor, lastWeek, false);
    proxy.setExpirationDate(grantor, Constants.YESTERDAY, false);
    proxy.evaluate();
    assertEquals(Status.INACTIVE, proxy.getStatus());
  }

  public final void testSetGrantable()
  throws
    SignetAuthorityException,
    ObjectNotFoundException
  { 
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
//      Subject subject
//        = signet.getSubjectSources().getSubject
//            (Signet.DEFAULT_SUBJECT_TYPE_ID,
//             Common.makeSubjectId(subjectIndex));
      
	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single "isExtensible" flag for every received Proxy.
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());

        boolean originalIsExtensible = proxy.canExtend();
        assertEquals(Constants.PROXY_CANEXTEND, originalIsExtensible);
        
        // Update the Proxy with the altered isExtensible flag.
        SignetSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setCanExtend
          (grantor, !originalIsExtensible);
        hibr.save(hs, proxy);
      }
      
      // Examine every single altered "isExtensible" flag for every received
      // Proxy, and set them back to their original values.
      proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());
        
        boolean alteredCanExtend = proxy.canExtend();
        assertEquals(!Constants.PROXY_CANEXTEND, alteredCanExtend);
        
        // Update the Proxy with the restored original "canExtend" flag.
        SignetSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setCanExtend
          (grantor,
           Constants.PROXY_CANEXTEND);
        hibr.save(hs, proxy);
      }
    }
  }

  public final void testSetCanUse()
  throws
    SignetAuthorityException,
    ObjectNotFoundException
  { 
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
//      Subject subject
//        = signet.getSubjectSources().getSubject
//            (Signet.DEFAULT_SUBJECT_TYPE_ID,
//             Common.makeSubjectId(subjectIndex));
      
	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single "canUse" flag for every received Proxy.
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());

        boolean originalCanUse = proxy.canUse();
        assertEquals(Constants.PROXY_CANUSE, originalCanUse);
        
        // Update the Proxy with the altered canUse flag.
        SignetSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setCanUse
          (grantor, !originalCanUse);
        hibr.save(hs, proxy);
      }
      
      // Examine every single altered "canUse" flag for every received
      // Proxy, and set them back to their original values.
      proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());
        
        boolean alteredCanUse = proxy.canUse();
        assertEquals(!Constants.PROXY_CANUSE, alteredCanUse);
        
        // Update the Proxy with the restored original "canUse" flag.
        SignetSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setCanUse
          (grantor,
           Constants.PROXY_CANUSE);
        hibr.save(hs, proxy);
      }
    }
  }

  public final void testGetHistory()
  throws
    ObjectNotFoundException
  { 
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
//      Subject subject
//        = signet.getSubjectSources().getSubject
//            (Signet.DEFAULT_SUBJECT_TYPE_ID,
//             Common.makeSubjectId(subjectIndex));
      
	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      // Every single Proxy should have a single History record.
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());
        
        Set historySet = proxy.getHistory();
        assertNotNull(historySet);
        assertEquals(1, historySet.size());
      }
    }
  }
  
  public final void testGetGrantee()
  throws ObjectNotFoundException
  {
    for (int subjectIndex = 0;
    subjectIndex < Constants.MAX_SUBJECTS;
    subjectIndex++)
    {
//      Subject subject
//       = signet.getSubjectSources().getSubject
//           (Signet.DEFAULT_SUBJECT_TYPE_ID,
//            Common.makeSubjectId(subjectIndex));

	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);

      Set proxiesReceived = pSubject.getProxiesReceived();

      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());

        SignetSubject grantee = proxy.getGrantee();
        assertEquals(pSubject, grantee);
      }
    }
  }
  
  public final void testGetGrantor()
  throws ObjectNotFoundException
  {
    for (int subjectIndex = 0;
    subjectIndex < Constants.MAX_SUBJECTS;
    subjectIndex++)
    {
//      Subject subject
//       = signet.getSubjectSources().getSubject
//           (Signet.DEFAULT_SUBJECT_TYPE_ID,
//            Common.makeSubjectId(subjectIndex));

	SignetSubject pSubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(subjectIndex));
//      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);

      Set proxiesGranted = pSubject.getProxiesGranted();

      Iterator proxiesGrantedIterator = proxiesGranted.iterator();
      while (proxiesGrantedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesGrantedIterator.next());

        SignetSubject grantor = proxy.getGrantor();
        assertEquals(pSubject, grantor);
      }
    }
  }
  
  public final void testGetProxySubject()
  throws ObjectNotFoundException
  {
    // subject1001 was proxySubject for a Proxy granted to subject1000.
    correlateGranteeAndProxySubject(1000, 1001);
  }
  
  public final void testGetRevoker()
  throws ObjectNotFoundException
  {
    // Subject0 has revoked a Proxy held by subject2.
    
    SignetSubject expectedRevoker = Common.getPrivilegedSubject(signet, 0);
    SignetSubject grantee = Common.getPrivilegedSubject(signet, 2);
    Set proxies = grantee.getProxiesReceived();
    proxies = Common.filterProxies(proxies, Status.INACTIVE);
    Proxy revokedProxy
      = (Proxy)(Common.getSingleSetMember(proxies));
    
    SignetSubject actualRevoker = revokedProxy.getRevoker();
    assertNotNull(actualRevoker);
    assertEquals(expectedRevoker, actualRevoker);
  }
  
  private void correlateGranteeAndProxySubject
    (int    granteeIndex,
     int    proxySubjectIndex)
  throws ObjectNotFoundException
  {
//    Subject grantee
//      = signet.getSubjectSources().getSubject
//          (Signet.DEFAULT_SUBJECT_TYPE_ID,
//           Common.makeSubjectId(granteeIndex));
  
	SignetSubject pGrantee = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(granteeIndex));
//    PrivilegedSubject pGrantee = signet.getSubjectSources().getPrivilegedSubject(grantee);

//    Subject proxySubject
//      = signet.getSubjectSources().getSubject
//          (Signet.DEFAULT_SUBJECT_TYPE_ID,
//           Common.makeSubjectId(proxySubjectIndex));
  
	SignetSubject pProxySubject = signet.getSubject(SignetAppSource.SIGNET_SOURCE_ID, Common.makeSubjectId(proxySubjectIndex));
//    PrivilegedSubject pProxySubject = signet.getSubjectSources().getPrivilegedSubject(proxySubject);
    
    // Let's see if this grantee has at least one Proxy proxied by this
    // proxySubject.
    Proxy matchingProxy = null;
    Set proxies = pGrantee.getProxiesReceived();
    Iterator proxiesIterator = proxies.iterator();
    while (proxiesIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesIterator.next());
      if ((proxy.getProxy() != null)
          && proxy.getProxy().equals(pProxySubject))
      {
        matchingProxy = proxy;
      }
    }
    
    assertNotNull(matchingProxy);
  }
}
