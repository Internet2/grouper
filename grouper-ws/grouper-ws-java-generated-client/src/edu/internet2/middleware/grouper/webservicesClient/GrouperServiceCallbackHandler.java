/**
 * GrouperServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
package edu.internet2.middleware.grouper.webservicesClient;


/**
 *  GrouperServiceCallbackHandler Callback class, Users can extend this class and implement
 *  their own receiveResult and receiveError methods.
 */
public abstract class GrouperServiceCallbackHandler {
    protected Object clientData;

    /**
     * User can pass in any object that needs to be accessed once the NonBlocking
     * Web service call is finished and appropriate method of this CallBack is called.
     * @param clientData Object mechanism by which the user can pass in user data
     * that will be avilable at the time this callback is called.
     */
    public GrouperServiceCallbackHandler(Object clientData) {
        this.clientData = clientData;
    }

    /**
     * Please use this constructor if you don't want to set any clientData
     */
    public GrouperServiceCallbackHandler() {
        this.clientData = null;
    }

    /**
     * Get the client data
     */
    public Object getClientData() {
        return clientData;
    }

    /**
     * auto generated Axis2 call back method for getMembershipsSimple method
     * override this method for handling normal response from getMembershipsSimple operation
     */
    public void receiveResultgetMembershipsSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMembershipsSimple operation
     */
    public void receiveErrorgetMembershipsSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getMembers method
     * override this method for handling normal response from getMembers operation
     */
    public void receiveResultgetMembers(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMembers operation
     */
    public void receiveErrorgetMembers(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for groupDelete method
     * override this method for handling normal response from groupDelete operation
     */
    public void receiveResultgroupDelete(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from groupDelete operation
     */
    public void receiveErrorgroupDelete(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for deleteMember method
     * override this method for handling normal response from deleteMember operation
     */
    public void receiveResultdeleteMember(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from deleteMember operation
     */
    public void receiveErrordeleteMember(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for groupSaveSimple method
     * override this method for handling normal response from groupSaveSimple operation
     */
    public void receiveResultgroupSaveSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from groupSaveSimple operation
     */
    public void receiveErrorgroupSaveSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getGroupsSimple method
     * override this method for handling normal response from getGroupsSimple operation
     */
    public void receiveResultgetGroupsSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getGroupsSimple operation
     */
    public void receiveErrorgetGroupsSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for findGroupsSimple method
     * override this method for handling normal response from findGroupsSimple operation
     */
    public void receiveResultfindGroupsSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from findGroupsSimple operation
     */
    public void receiveErrorfindGroupsSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getMemberships method
     * override this method for handling normal response from getMemberships operation
     */
    public void receiveResultgetMemberships(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMemberships operation
     */
    public void receiveErrorgetMemberships(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for hasMember method
     * override this method for handling normal response from hasMember operation
     */
    public void receiveResulthasMember(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from hasMember operation
     */
    public void receiveErrorhasMember(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for groupDeleteSimple method
     * override this method for handling normal response from groupDeleteSimple operation
     */
    public void receiveResultgroupDeleteSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from groupDeleteSimple operation
     */
    public void receiveErrorgroupDeleteSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for deleteMemberSimple method
     * override this method for handling normal response from deleteMemberSimple operation
     */
    public void receiveResultdeleteMemberSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from deleteMemberSimple operation
     */
    public void receiveErrordeleteMemberSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for groupSave method
     * override this method for handling normal response from groupSave operation
     */
    public void receiveResultgroupSave(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from groupSave operation
     */
    public void receiveErrorgroupSave(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getGroups method
     * override this method for handling normal response from getGroups operation
     */
    public void receiveResultgetGroups(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getGroups operation
     */
    public void receiveErrorgetGroups(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for getMembersSimple method
     * override this method for handling normal response from getMembersSimple operation
     */
    public void receiveResultgetMembersSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from getMembersSimple operation
     */
    public void receiveErrorgetMembersSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for addMember method
     * override this method for handling normal response from addMember operation
     */
    public void receiveResultaddMember(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from addMember operation
     */
    public void receiveErroraddMember(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for addMemberSimple method
     * override this method for handling normal response from addMemberSimple operation
     */
    public void receiveResultaddMemberSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from addMemberSimple operation
     */
    public void receiveErroraddMemberSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for hasMemberSimple method
     * override this method for handling normal response from hasMemberSimple operation
     */
    public void receiveResulthasMemberSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberSimpleResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from hasMemberSimple operation
     */
    public void receiveErrorhasMemberSimple(java.lang.Exception e) {
    }

    /**
     * auto generated Axis2 call back method for findGroups method
     * override this method for handling normal response from findGroups operation
     */
    public void receiveResultfindGroups(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse result) {
    }

    /**
     * auto generated Axis2 Error handler
     * override this method for handling error response from findGroups operation
     */
    public void receiveErrorfindGroups(java.lang.Exception e) {
    }
}
