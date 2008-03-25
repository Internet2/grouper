/**
 * GrouperServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
package edu.internet2.middleware.grouper.webservicesClient;


/**
 *  GrouperServiceTest Junit test case
 */
public class GrouperServiceTest extends junit.framework.TestCase {
    /**
     * Auto generated test method
     * @throws Exception
     */
    public void testfindStems() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems findStems112 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems.class);
        // TODO : Fill in the findStems112 here
        assertNotNull(stub.findStems(findStems112));
    }

    /**
     * Auto generated test method
     */
    public void testStartfindStems() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems findStems112 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems.class);
        // TODO : Fill in the findStems112 here
        stub.startfindStems(findStems112, new tempCallbackN1000C());
    }

    /**
     * Auto generated test method
     */
    public void testgetGroupsLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite getGroupsLite114 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite.class);
        // TODO : Fill in the getGroupsLite114 here
        assertNotNull(stub.getGroupsLite(getGroupsLite114));
    }

    /**
     * Auto generated test method
     */
    public void testStartgetGroupsLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite getGroupsLite114 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite.class);
        // TODO : Fill in the getGroupsLite114 here
        stub.startgetGroupsLite(getGroupsLite114, new tempCallbackN10030());
    }

    /**
     * Auto generated test method
     */
    public void testdeleteMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember deleteMember116 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember.class);
        // TODO : Fill in the deleteMember116 here
        assertNotNull(stub.deleteMember(deleteMember116));
    }

    /**
     * Auto generated test method
     */
    public void testStartdeleteMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember deleteMember116 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember.class);
        // TODO : Fill in the deleteMember116 here
        stub.startdeleteMember(deleteMember116, new tempCallbackN10054());
    }

    /**
     * Auto generated test method
     */
    public void testgroupDelete() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete groupDelete118 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete.class);
        // TODO : Fill in the groupDelete118 here
        assertNotNull(stub.groupDelete(groupDelete118));
    }

    /**
     * Auto generated test method
     */
    public void testStartgroupDelete() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete groupDelete118 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete.class);
        // TODO : Fill in the groupDelete118 here
        stub.startgroupDelete(groupDelete118, new tempCallbackN10078());
    }

    /**
     * Auto generated test method
     */
    public void teststemSave() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave stemSave120 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave.class);
        // TODO : Fill in the stemSave120 here
        assertNotNull(stub.stemSave(stemSave120));
    }

    /**
     * Auto generated test method
     */
    public void testStartstemSave() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave stemSave120 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave.class);
        // TODO : Fill in the stemSave120 here
        stub.startstemSave(stemSave120, new tempCallbackN1009C());
    }

    /**
     * Auto generated test method
     */
    public void testfindGroupsLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite findGroupsLite122 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite.class);
        // TODO : Fill in the findGroupsLite122 here
        assertNotNull(stub.findGroupsLite(findGroupsLite122));
    }

    /**
     * Auto generated test method
     */
    public void testStartfindGroupsLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite findGroupsLite122 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite.class);
        // TODO : Fill in the findGroupsLite122 here
        stub.startfindGroupsLite(findGroupsLite122, new tempCallbackN100C0());
    }

    /**
     * Auto generated test method
     */
    public void testdeleteMemberLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite deleteMemberLite124 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite.class);
        // TODO : Fill in the deleteMemberLite124 here
        assertNotNull(stub.deleteMemberLite(deleteMemberLite124));
    }

    /**
     * Auto generated test method
     */
    public void testStartdeleteMemberLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite deleteMemberLite124 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite.class);
        // TODO : Fill in the deleteMemberLite124 here
        stub.startdeleteMemberLite(deleteMemberLite124, new tempCallbackN100E4());
    }

    /**
     * Auto generated test method
     */
    public void testgroupSaveLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite groupSaveLite126 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite.class);
        // TODO : Fill in the groupSaveLite126 here
        assertNotNull(stub.groupSaveLite(groupSaveLite126));
    }

    /**
     * Auto generated test method
     */
    public void testStartgroupSaveLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite groupSaveLite126 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite.class);
        // TODO : Fill in the groupSaveLite126 here
        stub.startgroupSaveLite(groupSaveLite126, new tempCallbackN10108());
    }

    /**
     * Auto generated test method
     */
    public void testgroupSave() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave groupSave128 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave.class);
        // TODO : Fill in the groupSave128 here
        assertNotNull(stub.groupSave(groupSave128));
    }

    /**
     * Auto generated test method
     */
    public void testStartgroupSave() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave groupSave128 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave.class);
        // TODO : Fill in the groupSave128 here
        stub.startgroupSave(groupSave128, new tempCallbackN1012C());
    }

    /**
     * Auto generated test method
     */
    public void teststemDeleteLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite stemDeleteLite130 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite.class);
        // TODO : Fill in the stemDeleteLite130 here
        assertNotNull(stub.stemDeleteLite(stemDeleteLite130));
    }

    /**
     * Auto generated test method
     */
    public void testStartstemDeleteLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite stemDeleteLite130 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite.class);
        // TODO : Fill in the stemDeleteLite130 here
        stub.startstemDeleteLite(stemDeleteLite130, new tempCallbackN10150());
    }

    /**
     * Auto generated test method
     */
    public void testaddMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember132 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
        // TODO : Fill in the addMember132 here
        assertNotNull(stub.addMember(addMember132));
    }

    /**
     * Auto generated test method
     */
    public void testStartaddMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember132 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
        // TODO : Fill in the addMember132 here
        stub.startaddMember(addMember132, new tempCallbackN10174());
    }

    /**
     * Auto generated test method
     */
    public void testviewOrEditAttributes() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributes viewOrEditAttributes134 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributes) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributes.class);
        // TODO : Fill in the viewOrEditAttributes134 here
        assertNotNull(stub.viewOrEditAttributes(viewOrEditAttributes134));
    }

    /**
     * Auto generated test method
     */
    public void testStartviewOrEditAttributes() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributes viewOrEditAttributes134 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributes) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributes.class);
        // TODO : Fill in the viewOrEditAttributes134 here
        stub.startviewOrEditAttributes(viewOrEditAttributes134,
            new tempCallbackN10198());
    }

    /**
     * Auto generated test method
     */
    public void testgetMembershipsLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLite getMembershipsLite136 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLite.class);
        // TODO : Fill in the getMembershipsLite136 here
        assertNotNull(stub.getMembershipsLite(getMembershipsLite136));
    }

    /**
     * Auto generated test method
     */
    public void testStartgetMembershipsLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLite getMembershipsLite136 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLite.class);
        // TODO : Fill in the getMembershipsLite136 here
        stub.startgetMembershipsLite(getMembershipsLite136,
            new tempCallbackN101BC());
    }

    /**
     * Auto generated test method
     */
    public void teststemDelete() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete stemDelete138 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete.class);
        // TODO : Fill in the stemDelete138 here
        assertNotNull(stub.stemDelete(stemDelete138));
    }

    /**
     * Auto generated test method
     */
    public void testStartstemDelete() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete stemDelete138 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete.class);
        // TODO : Fill in the stemDelete138 here
        stub.startstemDelete(stemDelete138, new tempCallbackN101E0());
    }

    /**
     * Auto generated test method
     */
    public void testgetMembers() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers getMembers140 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers.class);
        // TODO : Fill in the getMembers140 here
        assertNotNull(stub.getMembers(getMembers140));
    }

    /**
     * Auto generated test method
     */
    public void testStartgetMembers() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers getMembers140 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers.class);
        // TODO : Fill in the getMembers140 here
        stub.startgetMembers(getMembers140, new tempCallbackN10204());
    }

    /**
     * Auto generated test method
     */
    public void testgroupDeleteLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite groupDeleteLite142 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite.class);
        // TODO : Fill in the groupDeleteLite142 here
        assertNotNull(stub.groupDeleteLite(groupDeleteLite142));
    }

    /**
     * Auto generated test method
     */
    public void testStartgroupDeleteLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite groupDeleteLite142 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite.class);
        // TODO : Fill in the groupDeleteLite142 here
        stub.startgroupDeleteLite(groupDeleteLite142, new tempCallbackN10228());
    }

    /**
     * Auto generated test method
     */
    public void testaddMemberLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite addMemberLite144 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite.class);
        // TODO : Fill in the addMemberLite144 here
        assertNotNull(stub.addMemberLite(addMemberLite144));
    }

    /**
     * Auto generated test method
     */
    public void testStartaddMemberLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite addMemberLite144 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite.class);
        // TODO : Fill in the addMemberLite144 here
        stub.startaddMemberLite(addMemberLite144, new tempCallbackN1024C());
    }

    /**
     * Auto generated test method
     */
    public void teststemSaveLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite stemSaveLite146 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite.class);
        // TODO : Fill in the stemSaveLite146 here
        assertNotNull(stub.stemSaveLite(stemSaveLite146));
    }

    /**
     * Auto generated test method
     */
    public void testStartstemSaveLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite stemSaveLite146 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite.class);
        // TODO : Fill in the stemSaveLite146 here
        stub.startstemSaveLite(stemSaveLite146, new tempCallbackN10270());
    }

    /**
     * Auto generated test method
     */
    public void testviewOrEditAttributesLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLite viewOrEditAttributesLite148 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLite.class);
        // TODO : Fill in the viewOrEditAttributesLite148 here
        assertNotNull(stub.viewOrEditAttributesLite(viewOrEditAttributesLite148));
    }

    /**
     * Auto generated test method
     */
    public void testStartviewOrEditAttributesLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLite viewOrEditAttributesLite148 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLite.class);
        // TODO : Fill in the viewOrEditAttributesLite148 here
        stub.startviewOrEditAttributesLite(viewOrEditAttributesLite148,
            new tempCallbackN10294());
    }

    /**
     * Auto generated test method
     */
    public void testfindStemsLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite findStemsLite150 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite.class);
        // TODO : Fill in the findStemsLite150 here
        assertNotNull(stub.findStemsLite(findStemsLite150));
    }

    /**
     * Auto generated test method
     */
    public void testStartfindStemsLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite findStemsLite150 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite.class);
        // TODO : Fill in the findStemsLite150 here
        stub.startfindStemsLite(findStemsLite150, new tempCallbackN102B8());
    }

    /**
     * Auto generated test method
     */
    public void testgetMemberships() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMemberships getMemberships152 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMemberships) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMemberships.class);
        // TODO : Fill in the getMemberships152 here
        assertNotNull(stub.getMemberships(getMemberships152));
    }

    /**
     * Auto generated test method
     */
    public void testStartgetMemberships() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMemberships getMemberships152 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMemberships) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMemberships.class);
        // TODO : Fill in the getMemberships152 here
        stub.startgetMemberships(getMemberships152, new tempCallbackN102DC());
    }

    /**
     * Auto generated test method
     */
    public void testhasMemberLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite hasMemberLite154 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite.class);
        // TODO : Fill in the hasMemberLite154 here
        assertNotNull(stub.hasMemberLite(hasMemberLite154));
    }

    /**
     * Auto generated test method
     */
    public void testStarthasMemberLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite hasMemberLite154 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite.class);
        // TODO : Fill in the hasMemberLite154 here
        stub.starthasMemberLite(hasMemberLite154, new tempCallbackN10300());
    }

    /**
     * Auto generated test method
     */
    public void testhasMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember hasMember156 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember.class);
        // TODO : Fill in the hasMember156 here
        assertNotNull(stub.hasMember(hasMember156));
    }

    /**
     * Auto generated test method
     */
    public void testStarthasMember() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember hasMember156 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember.class);
        // TODO : Fill in the hasMember156 here
        stub.starthasMember(hasMember156, new tempCallbackN10324());
    }

    /**
     * Auto generated test method
     */
    public void testviewOrEditPrivilegesLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLite viewOrEditPrivilegesLite158 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLite.class);
        // TODO : Fill in the viewOrEditPrivilegesLite158 here
        assertNotNull(stub.viewOrEditPrivilegesLite(viewOrEditPrivilegesLite158));
    }

    /**
     * Auto generated test method
     */
    public void testStartviewOrEditPrivilegesLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLite viewOrEditPrivilegesLite158 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLite.class);
        // TODO : Fill in the viewOrEditPrivilegesLite158 here
        stub.startviewOrEditPrivilegesLite(viewOrEditPrivilegesLite158,
            new tempCallbackN10348());
    }

    /**
     * Auto generated test method
     */
    public void testviewOrEditPrivileges() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivileges viewOrEditPrivileges160 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivileges) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivileges.class);
        // TODO : Fill in the viewOrEditPrivileges160 here
        assertNotNull(stub.viewOrEditPrivileges(viewOrEditPrivileges160));
    }

    /**
     * Auto generated test method
     */
    public void testStartviewOrEditPrivileges() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivileges viewOrEditPrivileges160 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivileges) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivileges.class);
        // TODO : Fill in the viewOrEditPrivileges160 here
        stub.startviewOrEditPrivileges(viewOrEditPrivileges160,
            new tempCallbackN1036C());
    }

    /**
     * Auto generated test method
     */
    public void testgetGroups() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups getGroups162 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups.class);
        // TODO : Fill in the getGroups162 here
        assertNotNull(stub.getGroups(getGroups162));
    }

    /**
     * Auto generated test method
     */
    public void testStartgetGroups() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups getGroups162 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups.class);
        // TODO : Fill in the getGroups162 here
        stub.startgetGroups(getGroups162, new tempCallbackN10390());
    }

    /**
     * Auto generated test method
     */
    public void testgetMembersLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite getMembersLite164 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite.class);
        // TODO : Fill in the getMembersLite164 here
        assertNotNull(stub.getMembersLite(getMembersLite164));
    }

    /**
     * Auto generated test method
     */
    public void testStartgetMembersLite() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite getMembersLite164 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite.class);
        // TODO : Fill in the getMembersLite164 here
        stub.startgetMembersLite(getMembersLite164, new tempCallbackN103B4());
    }

    /**
     * Auto generated test method
     */
    public void testfindGroups() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub(); //the default implementation should point to the right endpoint

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups166 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class);
        // TODO : Fill in the findGroups166 here
        assertNotNull(stub.findGroups(findGroups166));
    }

    /**
     * Auto generated test method
     */
    public void testStartfindGroups() throws java.lang.Exception {
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
            new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups166 =
            (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups) getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class);
        // TODO : Fill in the findGroups166 here
        stub.startfindGroups(findGroups166, new tempCallbackN103D8());
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

        public void receiveResultfindStems(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsResponse result) {
        }

        public void receiveErrorfindStems(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10030 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10030() {
            super(null);
        }

        public void receiveResultgetGroupsLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLiteResponse result) {
        }

        public void receiveErrorgetGroupsLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10054 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10054() {
            super(null);
        }

        public void receiveResultdeleteMember(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberResponse result) {
        }

        public void receiveErrordeleteMember(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10078 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10078() {
            super(null);
        }

        public void receiveResultgroupDelete(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteResponse result) {
        }

        public void receiveErrorgroupDelete(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN1009C extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN1009C() {
            super(null);
        }

        public void receiveResultstemSave(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveResponse result) {
        }

        public void receiveErrorstemSave(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN100C0 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN100C0() {
            super(null);
        }

        public void receiveResultfindGroupsLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLiteResponse result) {
        }

        public void receiveErrorfindGroupsLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN100E4 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN100E4() {
            super(null);
        }

        public void receiveResultdeleteMemberLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLiteResponse result) {
        }

        public void receiveErrordeleteMemberLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10108 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10108() {
            super(null);
        }

        public void receiveResultgroupSaveLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLiteResponse result) {
        }

        public void receiveErrorgroupSaveLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN1012C extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN1012C() {
            super(null);
        }

        public void receiveResultgroupSave(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveResponse result) {
        }

        public void receiveErrorgroupSave(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10150 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10150() {
            super(null);
        }

        public void receiveResultstemDeleteLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLiteResponse result) {
        }

        public void receiveErrorstemDeleteLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10174 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10174() {
            super(null);
        }

        public void receiveResultaddMember(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse result) {
        }

        public void receiveErroraddMember(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10198 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10198() {
            super(null);
        }

        public void receiveResultviewOrEditAttributes(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesResponse result) {
        }

        public void receiveErrorviewOrEditAttributes(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN101BC extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN101BC() {
            super(null);
        }

        public void receiveResultgetMembershipsLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsLiteResponse result) {
        }

        public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN101E0 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN101E0() {
            super(null);
        }

        public void receiveResultstemDelete(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteResponse result) {
        }

        public void receiveErrorstemDelete(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10204 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10204() {
            super(null);
        }

        public void receiveResultgetMembers(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersResponse result) {
        }

        public void receiveErrorgetMembers(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10228 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10228() {
            super(null);
        }

        public void receiveResultgroupDeleteLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLiteResponse result) {
        }

        public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN1024C extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN1024C() {
            super(null);
        }

        public void receiveResultaddMemberLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLiteResponse result) {
        }

        public void receiveErroraddMemberLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10270 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10270() {
            super(null);
        }

        public void receiveResultstemSaveLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLiteResponse result) {
        }

        public void receiveErrorstemSaveLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10294 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10294() {
            super(null);
        }

        public void receiveResultviewOrEditAttributesLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditAttributesLiteResponse result) {
        }

        public void receiveErrorviewOrEditAttributesLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN102B8 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN102B8() {
            super(null);
        }

        public void receiveResultfindStemsLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLiteResponse result) {
        }

        public void receiveErrorfindStemsLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN102DC extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN102DC() {
            super(null);
        }

        public void receiveResultgetMemberships(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembershipsResponse result) {
        }

        public void receiveErrorgetMemberships(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10300 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10300() {
            super(null);
        }

        public void receiveResulthasMemberLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLiteResponse result) {
        }

        public void receiveErrorhasMemberLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10324 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10324() {
            super(null);
        }

        public void receiveResulthasMember(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberResponse result) {
        }

        public void receiveErrorhasMember(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10348 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10348() {
            super(null);
        }

        public void receiveResultviewOrEditPrivilegesLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesLiteResponse result) {
        }

        public void receiveErrorviewOrEditPrivilegesLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN1036C extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN1036C() {
            super(null);
        }

        public void receiveResultviewOrEditPrivileges(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.ViewOrEditPrivilegesResponse result) {
        }

        public void receiveErrorviewOrEditPrivileges(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN10390 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN10390() {
            super(null);
        }

        public void receiveResultgetGroups(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsResponse result) {
        }

        public void receiveErrorgetGroups(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN103B4 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN103B4() {
            super(null);
        }

        public void receiveResultgetMembersLite(
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLiteResponse result) {
        }

        public void receiveErrorgetMembersLite(java.lang.Exception e) {
            fail();
        }
    }

    private class tempCallbackN103D8 extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler {
        public tempCallbackN103D8() {
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
