/**
 * GrouperServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
package edu.internet2.middleware.grouper.webservicesClient;


/*
 *  GrouperServiceTest Junit test case
 */
public class GrouperServiceTest extends junit.framework.TestCase {
    /**
     * Auto generated test method
     */
    public void testaddMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember12 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
        // TODO : Fill in the addMember12 here
        assertNotNull(stub.addMember(addMember12));
    }

    /**
     * Auto generated test method
     */
    public void testStartaddMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember12 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
        // TODO : Fill in the addMember12 here
        stub.startaddMember(addMember12, new tempCallbackN1000C());
    }

    /**
     * Auto generated test method
     */
    public void testaddMemberSimple() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple addMemberSimple14 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.class);
        // TODO : Fill in the addMemberSimple14 here
        assertNotNull(stub.addMemberSimple(addMemberSimple14));
    }

    /**
     * Auto generated test method
     */
    public void testStartaddMemberSimple() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple addMemberSimple14 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.class);
        // TODO : Fill in the addMemberSimple14 here
        stub.startaddMemberSimple(addMemberSimple14, new tempCallbackN10030());
    }

    /**
     * Auto generated test method
     */
    public void testfindGroups() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups16 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class);
        // TODO : Fill in the findGroups16 here
        assertNotNull(stub.findGroups(findGroups16));
    }

    /**
     * Auto generated test method
     */
    public void testStartfindGroups() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups16 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class);
        // TODO : Fill in the findGroups16 here
        stub.startfindGroups(findGroups16, new tempCallbackN10054());
    }

    //Create an ADBBean and provide it as the test object
    public org.apache.axis2.databinding.ADBBean getTestObject(
        java.lang.Class type) throws Exception {
        return (org.apache.axis2.databinding.ADBBean) type.newInstance();
    }

    private class tempCallbackN1000C extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN1000C() {
            super(null);
        }

        public void receiveResultaddMember(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse result) {
        }

        public void receiveErroraddMember(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10030 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10030() {
            super(null);
        }

        public void receiveResultaddMemberSimple(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse result) {
        }

        public void receiveErroraddMemberSimple(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10054 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10054() {
            super(null);
        }

        public void receiveResultfindGroups(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse result) {
        }

        public void receiveErrorfindGroups(java.lang.Exception e) {
            fail();
        }
    }
}
