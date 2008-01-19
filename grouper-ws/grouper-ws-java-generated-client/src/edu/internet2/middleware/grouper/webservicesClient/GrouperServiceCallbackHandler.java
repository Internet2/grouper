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
