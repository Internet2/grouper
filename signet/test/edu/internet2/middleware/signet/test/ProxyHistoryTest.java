/*--
$Id: ProxyHistoryTest.java,v 1.1 2005-12-02 18:36:53 acohen Exp $
$Date: 2005-12-02 18:36:53 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.AssignmentHistory;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.ProxyHistory;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.subject.Subject;

import junit.framework.TestCase;

public class ProxyHistoryTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(ProxyHistoryTest.class);
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
   * Constructor for LimitTest.
   * @param name
   */
  public ProxyHistoryTest(String name)
  {
    super(name);
  }
  
  public final void testCanExtend()
  throws ObjectNotFoundException
  {
    int totalProxyHistoryRecords = 0;
    
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

      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());
        Set proxyHistorySet = proxy.getHistory();
        assertEquals(1, proxyHistorySet.size());
        
        Iterator proxyHistoryIterator = proxyHistorySet.iterator();
        while (proxyHistoryIterator.hasNext())
        {
          ProxyHistory proxyHistoryRecord
            = (ProxyHistory)(proxyHistoryIterator.next());
          totalProxyHistoryRecords++;
          
          boolean canExtend = proxyHistoryRecord.canExtend();
          assertEquals(Constants.PROXY_CANEXTEND, canExtend);
        }
      }
    }
    
    assertTrue
      ("At least one ProxyHistory record must be present",
       totalProxyHistoryRecords > 0);
  }
  
  public final void testCanUse()
  throws ObjectNotFoundException
  {
    int totalProxyHistoryRecords = 0;
    
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

      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());
        Set proxyHistorySet = proxy.getHistory();
        assertEquals(1, proxyHistorySet.size());
        
        Iterator proxyHistoryIterator = proxyHistorySet.iterator();
        while (proxyHistoryIterator.hasNext())
        {
          ProxyHistory proxyHistoryRecord
            = (ProxyHistory)(proxyHistoryIterator.next());
          totalProxyHistoryRecords++;
          
          boolean canUse = proxyHistoryRecord.canUse();
          assertEquals(Constants.PROXY_CANUSE, canUse);
        }
      }
    }
    
    assertTrue
      ("At least one ProxyHistory record must be present",
       totalProxyHistoryRecords > 0);
  }

  public final void testGetEffectiveDate()
  throws ObjectNotFoundException
  { 
    int totalProxyHistoryRecordsFound = 0;
    
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

      Iterator proxiesReceivedIterator = proxiesReceived.iterator();
      while (proxiesReceivedIterator.hasNext())
      {
        Proxy proxy
          = (Proxy)(proxiesReceivedIterator.next());
        
        Set proxyHistorySet = proxy.getHistory();
        Iterator proxyHistoryIterator = proxyHistorySet.iterator();
        while (proxyHistoryIterator.hasNext())
        {
          ProxyHistory proxyHistory
            = (ProxyHistory)(proxyHistoryIterator.next());
          
          totalProxyHistoryRecordsFound++;
          
          Date effectiveDate = proxyHistory.getEffectiveDate();

          assertNotNull(effectiveDate);
          assertEquals(Constants.YESTERDAY, effectiveDate);
        }
      }
    }
    
    assertTrue
      ("We expect to encounter at least one ProxyHistory record.",
       totalProxyHistoryRecordsFound > 0);
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
        
        Set proxyHistorySet = proxy.getHistory();
        Iterator proxyHistoryIterator = proxyHistorySet.iterator();
        while (proxyHistoryIterator.hasNext())
        {
          ProxyHistory proxyHistory
            = (ProxyHistory)(proxyHistoryIterator.next());

          PrivilegedSubject grantee = proxyHistory.getGrantee();
          assertEquals(pSubject, grantee);
        }
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
        
        Set proxyHistorySet = proxy.getHistory();
        Iterator proxyHistoryIterator = proxyHistorySet.iterator();
        while (proxyHistoryIterator.hasNext())
        {
          ProxyHistory proxyHistory
            = (ProxyHistory)(proxyHistoryIterator.next());

          PrivilegedSubject grantor = proxyHistory.getGrantor();
          assertEquals(pSubject, grantor);
        }
      }
    }
  }
  
  public final void testGetProxySubject()
  throws ObjectNotFoundException
  {
    // subject1001 id1 was proxySubject for a Proxy granted to subject1000 id2.
    correlateGranteeAndProxySubject(1000, 1001);
  }
  
  public final void testGetRevoker()
  throws ObjectNotFoundException
  {
    // Subject0 has revoked a Proxy held by subject2.
    boolean revokerFound = false;
    
    PrivilegedSubject expectedRevoker = Common.getPrivilegedSubject(signet, 0);
    PrivilegedSubject grantee = Common.getPrivilegedSubject(signet, 2);
    Set proxies = grantee.getProxiesReceived();
    proxies = Common.filterProxies(proxies, Status.INACTIVE);
    Proxy revokedProxy
      = (Proxy)(Common.getSingleSetMember(proxies));
    
    Set proxyHistorySet = revokedProxy.getHistory();
    Iterator proxyHistoryIterator = proxyHistorySet.iterator();
    while (proxyHistoryIterator.hasNext())
    {
      ProxyHistory proxyHistoryRecord
        = (ProxyHistory)(proxyHistoryIterator.next());
      
      PrivilegedSubject actualRevoker = proxyHistoryRecord.getRevoker();
      if ((actualRevoker != null) && (actualRevoker.equals(expectedRevoker)))
      {
        revokerFound = true;
      }
    }
    
    assertTrue(revokerFound);
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

    Subject expectedProxySubject
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(proxySubjectIndex));
  
    PrivilegedSubject pExpectedProxySubject = signet.getPrivilegedSubject(expectedProxySubject);
    
    // Let's see if this grantee has at least one Proxy proxied by this
    // proxySubject.
    ProxyHistory matchingProxyHistoryRecord = null;
    Set proxies = pGrantee.getProxiesReceived();
    Iterator proxiesIterator = proxies.iterator();
    while (proxiesIterator.hasNext())
    {
      Proxy proxy = (Proxy)(proxiesIterator.next());

      Set proxyHistorySet = proxy.getHistory();
      Iterator proxyHistoryIterator = proxyHistorySet.iterator();
      while (proxyHistoryIterator.hasNext())
      {
        ProxyHistory proxyHistoryRecord
          = (ProxyHistory)(proxyHistoryIterator.next());
        
        PrivilegedSubject actualProxySubject = proxyHistoryRecord.getProxySubject();
          
        if ((actualProxySubject != null)
            && actualProxySubject.equals(pExpectedProxySubject))
        {
          matchingProxyHistoryRecord = proxyHistoryRecord;
        }
      }
    }
    
    assertNotNull(matchingProxyHistoryRecord);
  }
}
