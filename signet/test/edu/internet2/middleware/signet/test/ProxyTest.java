/*--
$Id: ProxyTest.java,v 1.1 2005-12-02 18:36:53 acohen Exp $
$Date: 2005-12-02 18:36:53 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.subject.Subject;

import junit.framework.TestCase;

public class ProxyTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
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
    signet.beginTransaction();
    fixtures = new Fixtures(signet);
    signet.commit();
    signet.close();
    
    // Let's use a new Signet session, to make sure we're actually
    // pulling data from the database, and not just referring to in-memory
    // structures.
    signet = new Signet();
    signet.beginTransaction();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    signet.commit();
    signet.close();
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
      Subject subject
        = signet.getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));
      
      Proxy proxy = (Proxy)(proxiesReceived.toArray()[0]);
      
      PrivilegedSubject revoker;
      
      if (proxy.getProxy() == null)
      {
        revoker = proxy.getGrantor();
      }
      else
      {
        revoker = proxy.getProxy();
        revoker.setActingAs(proxy.getGrantor());
      }
      
      proxy.revoke(revoker);
      proxy.save();
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
      Subject subject
        = signet.getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
 
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      assertNotNull(proxiesReceived);
      
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));
      
      
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
        duplicateProxy.save();
        
        // At this point, there shoule be exactly one duplicate Proxy.
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
      Subject subject
        = signet.getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

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
        PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setEffectiveDate
          (grantor,
           Constants.DAY_BEFORE_YESTERDAY);
        proxy.save();
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
        PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setEffectiveDate
          (grantor,
           Constants.YESTERDAY);
        proxy.save();
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
      Subject subject
        = signet.getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

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
        PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setExpirationDate
          (grantor,
           Constants.DAY_AFTER_TOMORROW);
        proxy.save();
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
        PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setExpirationDate
          (grantor,
           Constants.TOMORROW);
        proxy.save();
      }
    }
  }
  
  public final void testEvaluate()
  throws
    ObjectNotFoundException,
    SignetAuthorityException
  {
    Proxy proxy = null;
    
    Subject subject0
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(0));
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    
    // Get any one of subject0's proxies - we don't care which one.
    Set proxiesReceived = pSubject0.getProxiesReceived();
    Iterator proxiesReceivedIterator = proxiesReceived.iterator();
    while (proxiesReceivedIterator.hasNext())
    {
      proxy = (Proxy)(proxiesReceivedIterator.next());
    }
    
    assertNotNull(proxy);
    
    Date lastWeek  = Common.getDate(-7);
    Date nextWeek = Common.getDate(7);
    
    PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
    
    proxy.setEffectiveDate(grantor, lastWeek);
    proxy.setExpirationDate(grantor, Constants.YESTERDAY);
    assertEquals(Status.INACTIVE, proxy.evaluate());
    
    proxy.setEffectiveDate(grantor, Constants.YESTERDAY);
    proxy.setExpirationDate(grantor, Constants.TOMORROW);
    assertEquals(Status.ACTIVE, proxy.evaluate());
    
    proxy.setEffectiveDate(grantor, Constants.TOMORROW);
    proxy.setExpirationDate(grantor, nextWeek);
    assertEquals(Status.PENDING, proxy.evaluate());
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
      Subject subject
        = signet.getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single "isExtensible" flag for every received Proxy.
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());

        boolean originalIsExtensible = proxy.canExtend();
        assertEquals(Constants.PROXY_CANEXTEND, originalIsExtensible);
        
        // Update the Proxy with the altered isExtensible flag.
        PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setCanExtend
          (grantor, !originalIsExtensible);
        proxy.save();
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
        PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setCanExtend
          (grantor,
           Constants.PROXY_CANEXTEND);
        proxy.save();
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
      Subject subject
        = signet.getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single "canUse" flag for every received Proxy.
      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());

        boolean originalCanUse = proxy.canUse();
        assertEquals(Constants.PROXY_CANUSE, originalCanUse);
        
        // Update the Proxy with the altered canUse flag.
        PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setCanUse
          (grantor, !originalCanUse);
        proxy.save();
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
        PrivilegedSubject grantor = Common.getOriginalGrantor(proxy);
        proxy.setCanUse
          (grantor,
           Constants.PROXY_CANUSE);
        proxy.save();
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
      Subject subject
        = signet.getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      Set proxiesReceived = pSubject.getProxiesReceived();
      proxiesReceived
        = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      proxiesReceived
        = Common.filterProxies
            (proxiesReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

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
      Subject subject
       = signet.getSubject
           (Signet.DEFAULT_SUBJECT_TYPE_ID,
            Common.makeSubjectId(subjectIndex));

      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);

      Set proxiesReceived = pSubject.getProxiesReceived();

      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());

        PrivilegedSubject grantee = proxy.getGrantee();
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
      Subject subject
       = signet.getSubject
           (Signet.DEFAULT_SUBJECT_TYPE_ID,
            Common.makeSubjectId(subjectIndex));

      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);

      Set proxiesGranted = pSubject.getProxiesGranted();

      Iterator proxiesGrantedIterator = proxiesGranted.iterator();
      while (proxiesGrantedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesGrantedIterator.next());

        PrivilegedSubject grantor = proxy.getGrantor();
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
    
    PrivilegedSubject expectedRevoker = Common.getPrivilegedSubject(signet, 0);
    PrivilegedSubject grantee = Common.getPrivilegedSubject(signet, 2);
    Set proxies = grantee.getProxiesReceived();
    proxies = Common.filterProxies(proxies, Status.INACTIVE);
    Proxy revokedProxy
      = (Proxy)(Common.getSingleSetMember(proxies));
    
    PrivilegedSubject actualRevoker = revokedProxy.getRevoker();
    assertNotNull(actualRevoker);
    assertEquals(expectedRevoker, actualRevoker);
  }
  
  private void correlateGranteeAndProxySubject
    (int    granteeIndex,
     int    proxySubjectIndex)
  throws ObjectNotFoundException
  {
    Subject grantee
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(granteeIndex));
  
    PrivilegedSubject pGrantee = signet.getPrivilegedSubject(grantee);

    Subject proxySubject
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(proxySubjectIndex));
  
    PrivilegedSubject pProxySubject = signet.getPrivilegedSubject(proxySubject);
    
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
