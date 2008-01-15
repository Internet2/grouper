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

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember8 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
        // TODO : Fill in the addMember8 here
        assertNotNull(stub.addMember(addMember8));
    }

    /**
     * Auto generated test method
     */
    public void testStartaddMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember8 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
        // TODO : Fill in the addMember8 here
        stub.startaddMember(addMember8, new tempCallbackN1000C());
    }

    /**
     * Auto generated test method
     */
    public void testaddMemberSimple() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple addMemberSimple10 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.class);
        // TODO : Fill in the addMemberSimple10 here
        assertNotNull(stub.addMemberSimple(addMemberSimple10));
    }

    /**
     * Auto generated test method
     */
    public void testStartaddMemberSimple() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple addMemberSimple10 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.class);
        // TODO : Fill in the addMemberSimple10 here
        stub.startaddMemberSimple(addMemberSimple10, new tempCallbackN10030());
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
}
