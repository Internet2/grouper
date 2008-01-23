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
    public void testfindGroupsSimple() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimple findGroupsSimple16 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimple) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimple.class);
        // TODO : Fill in the findGroupsSimple16 here
        assertNotNull(stub.findGroupsSimple(findGroupsSimple16));
    }

    /**
     * Auto generated test method
     */
    public void testStartfindGroupsSimple() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimple findGroupsSimple16 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimple) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimple.class);
        // TODO : Fill in the findGroupsSimple16 here
        stub.startfindGroupsSimple(findGroupsSimple16, new tempCallbackN1000C());
    }

    /**
     * Auto generated test method
     */
    public void testaddMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember18 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
        // TODO : Fill in the addMember18 here
        assertNotNull(stub.addMember(addMember18));
    }

    /**
     * Auto generated test method
     */
    public void testStartaddMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember18 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
        // TODO : Fill in the addMember18 here
        stub.startaddMember(addMember18, new tempCallbackN10030());
    }

    /**
     * Auto generated test method
     */
    public void testaddMemberSimple() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple addMemberSimple20 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.class);
        // TODO : Fill in the addMemberSimple20 here
        assertNotNull(stub.addMemberSimple(addMemberSimple20));
    }

    /**
     * Auto generated test method
     */
    public void testStartaddMemberSimple() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple addMemberSimple20 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.class);
        // TODO : Fill in the addMemberSimple20 here
        stub.startaddMemberSimple(addMemberSimple20, new tempCallbackN10054());
    }

    /**
     * Auto generated test method
     */
    public void testfindGroups() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups22 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class);
        // TODO : Fill in the findGroups22 here
        assertNotNull(stub.findGroups(findGroups22));
    }

    /**
     * Auto generated test method
     */
    public void testStartfindGroups() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups22 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class);
        // TODO : Fill in the findGroups22 here
        stub.startfindGroups(findGroups22, new tempCallbackN10078());
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

        public void receiveResultfindGroupsSimple(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimpleResponse result) {
        }

        public void receiveErrorfindGroupsSimple(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10030 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10030() {
            super(null);
        }

        public void receiveResultaddMember(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse result) {
        }

        public void receiveErroraddMember(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10054 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10054() {
            super(null);
        }

        public void receiveResultaddMemberSimple(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse result) {
        }

        public void receiveErroraddMemberSimple(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10078 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10078() {
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
