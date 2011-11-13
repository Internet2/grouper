

/**
 * GrouperServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */
    package edu.internet2.middleware.grouper.webservicesClient;

    /*
     *  GrouperServiceTest Junit test case
    */

    public class GrouperServiceTest extends junit.framework.TestCase{

     
        /**
         * Auto generated test method
         */
        public  void testgetGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite234=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite234 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite234));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite234=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite234 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite234,
                    new tempCallbackN1000C()
                );
              


        }

        private class tempCallbackN1000C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1000C(){ super(null);}

            public void receiveResultgetGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite hasMemberLite236=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite236 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite236));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite hasMemberLite236=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite236 here
                

                stub.starthasMemberLite(
                         hasMemberLite236,
                    new tempCallbackN10032()
                );
              


        }

        private class tempCallbackN10032  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10032(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite stemDeleteLite238=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite238 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite238));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite stemDeleteLite238=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite238 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite238,
                    new tempCallbackN10058()
                );
              


        }

        private class tempCallbackN10058  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10058(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions assignPermissions240=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions240 here
                
                        assertNotNull(stub.assignPermissions(
                        assignPermissions240));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions assignPermissions240=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions240 here
                

                stub.startassignPermissions(
                         assignPermissions240,
                    new tempCallbackN1007E()
                );
              


        }

        private class tempCallbackN1007E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1007E(){ super(null);}

            public void receiveResultassignPermissions(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributes() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes assignAttributes242=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes242 here
                
                        assertNotNull(stub.assignAttributes(
                        assignAttributes242));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributes() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes assignAttributes242=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes242 here
                

                stub.startassignAttributes(
                         assignAttributes242,
                    new tempCallbackN100A4()
                );
              


        }

        private class tempCallbackN100A4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100A4(){ super(null);}

            public void receiveResultassignAttributes(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributes(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember deleteMember244=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember244 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember244));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember deleteMember244=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember244 here
                

                stub.startdeleteMember(
                         deleteMember244,
                    new tempCallbackN100CA()
                );
              


        }

        private class tempCallbackN100CA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100CA(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite stemSaveLite246=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite246 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite246));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite stemSaveLite246=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite246 here
                

                stub.startstemSaveLite(
                         stemSaveLite246,
                    new tempCallbackN100F0()
                );
              


        }

        private class tempCallbackN100F0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100F0(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite groupSaveLite248=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite248 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite248));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite groupSaveLite248=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite248 here
                

                stub.startgroupSaveLite(
                         groupSaveLite248,
                    new tempCallbackN10116()
                );
              


        }

        private class tempCallbackN10116  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10116(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember addMember250=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember.class);
                    // TODO : Fill in the addMember250 here
                
                        assertNotNull(stub.addMember(
                        addMember250));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember addMember250=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember.class);
                    // TODO : Fill in the addMember250 here
                

                stub.startaddMember(
                         addMember250,
                    new tempCallbackN1013C()
                );
              


        }

        private class tempCallbackN1013C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1013C(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberResponse result
                            ) {
                
            }

            public void receiveErroraddMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups findGroups252=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups252 here
                
                        assertNotNull(stub.findGroups(
                        findGroups252));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups findGroups252=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups252 here
                

                stub.startfindGroups(
                         findGroups252,
                    new tempCallbackN10162()
                );
              


        }

        private class tempCallbackN10162  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10162(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember hasMember254=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember.class);
                    // TODO : Fill in the hasMember254 here
                
                        assertNotNull(stub.hasMember(
                        hasMember254));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember hasMember254=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember.class);
                    // TODO : Fill in the hasMember254 here
                

                stub.starthasMember(
                         hasMember254,
                    new tempCallbackN10188()
                );
              


        }

        private class tempCallbackN10188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10188(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberResponse result
                            ) {
                
            }

            public void receiveErrorhasMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete stemDelete256=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete256 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete256));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete stemDelete256=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete256 here
                

                stub.startstemDelete(
                         stemDelete256,
                    new tempCallbackN101AE()
                );
              


        }

        private class tempCallbackN101AE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101AE(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteResponse result
                            ) {
                
            }

            public void receiveErrorstemDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite getMembershipsLite258=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite258 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite258));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite getMembershipsLite258=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite258 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite258,
                    new tempCallbackN101D4()
                );
              


        }

        private class tempCallbackN101D4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101D4(){ super(null);}

            public void receiveResultgetMembershipsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissionsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite assignPermissionsLite260=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite260 here
                
                        assertNotNull(stub.assignPermissionsLite(
                        assignPermissionsLite260));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite assignPermissionsLite260=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite260 here
                

                stub.startassignPermissionsLite(
                         assignPermissionsLite260,
                    new tempCallbackN101FA()
                );
              


        }

        private class tempCallbackN101FA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101FA(){ super(null);}

            public void receiveResultassignPermissionsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissionsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite262=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite262 here
                
                        assertNotNull(stub.getPermissionAssignmentsLite(
                        getPermissionAssignmentsLite262));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite262=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite262 here
                

                stub.startgetPermissionAssignmentsLite(
                         getPermissionAssignmentsLite262,
                    new tempCallbackN10220()
                );
              


        }

        private class tempCallbackN10220  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10220(){ super(null);}

            public void receiveResultgetPermissionAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite addMemberLite264=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite264 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite264));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite addMemberLite264=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite264 here
                

                stub.startaddMemberLite(
                         addMemberLite264,
                    new tempCallbackN10246()
                );
              


        }

        private class tempCallbackN10246  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10246(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave groupSave266=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave266 here
                
                        assertNotNull(stub.groupSave(
                        groupSave266));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave groupSave266=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave266 here
                

                stub.startgroupSave(
                         groupSave266,
                    new tempCallbackN1026C()
                );
              


        }

        private class tempCallbackN1026C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1026C(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveResponse result
                            ) {
                
            }

            public void receiveErrorgroupSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments getAttributeAssignments268=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments268 here
                
                        assertNotNull(stub.getAttributeAssignments(
                        getAttributeAssignments268));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments getAttributeAssignments268=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments268 here
                

                stub.startgetAttributeAssignments(
                         getAttributeAssignments268,
                    new tempCallbackN10292()
                );
              


        }

        private class tempCallbackN10292  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10292(){ super(null);}

            public void receiveResultgetAttributeAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite groupDeleteLite270=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite270 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite270));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite groupDeleteLite270=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite270 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite270,
                    new tempCallbackN102B8()
                );
              


        }

        private class tempCallbackN102B8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102B8(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStemsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite findStemsLite272=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite272 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite272));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite findStemsLite272=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite272 here
                

                stub.startfindStemsLite(
                         findStemsLite272,
                    new tempCallbackN102DE()
                );
              


        }

        private class tempCallbackN102DE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102DE(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindStemsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubject() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject memberChangeSubject274=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject274 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject274));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject memberChangeSubject274=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject274 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject274,
                    new tempCallbackN10304()
                );
              


        }

        private class tempCallbackN10304  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10304(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite deleteMemberLite276=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite276 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite276));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite deleteMemberLite276=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite276 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite276,
                    new tempCallbackN1032A()
                );
              


        }

        private class tempCallbackN1032A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1032A(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrordeleteMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave stemSave278=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave.class);
                    // TODO : Fill in the stemSave278 here
                
                        assertNotNull(stub.stemSave(
                        stemSave278));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave stemSave278=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave.class);
                    // TODO : Fill in the stemSave278 here
                

                stub.startstemSave(
                         stemSave278,
                    new tempCallbackN10350()
                );
              


        }

        private class tempCallbackN10350  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10350(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveResponse result
                            ) {
                
            }

            public void receiveErrorstemSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite memberChangeSubjectLite280=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite280 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite280));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite memberChangeSubjectLite280=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite280 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite280,
                    new tempCallbackN10376()
                );
              


        }

        private class tempCallbackN10376  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10376(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers getMembers282=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers282 here
                
                        assertNotNull(stub.getMembers(
                        getMembers282));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers getMembers282=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers282 here
                

                stub.startgetMembers(
                         getMembers282,
                    new tempCallbackN1039C()
                );
              


        }

        private class tempCallbackN1039C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1039C(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersResponse result
                            ) {
                
            }

            public void receiveErrorgetMembers(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups getGroups284=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups284 here
                
                        assertNotNull(stub.getGroups(
                        getGroups284));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups getGroups284=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups284 here
                

                stub.startgetGroups(
                         getGroups284,
                    new tempCallbackN103C2()
                );
              


        }

        private class tempCallbackN103C2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103C2(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsResponse result
                            ) {
                
            }

            public void receiveErrorgetGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems findStems286=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems.class);
                    // TODO : Fill in the findStems286 here
                
                        assertNotNull(stub.findStems(
                        findStems286));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems findStems286=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems.class);
                    // TODO : Fill in the findStems286 here
                

                stub.startfindStems(
                         findStems286,
                    new tempCallbackN103E8()
                );
              


        }

        private class tempCallbackN103E8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103E8(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite288 here
                
                        assertNotNull(stub.getAttributeAssignmentsLite(
                        getAttributeAssignmentsLite288));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite288 here
                

                stub.startgetAttributeAssignmentsLite(
                         getAttributeAssignmentsLite288,
                    new tempCallbackN1040E()
                );
              


        }

        private class tempCallbackN1040E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1040E(){ super(null);}

            public void receiveResultgetAttributeAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete groupDelete290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete290 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete290));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete groupDelete290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete290 here
                

                stub.startgroupDelete(
                         groupDelete290,
                    new tempCallbackN10434()
                );
              


        }

        private class tempCallbackN10434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10434(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjectsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite getSubjectsLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite292 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite292));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite getSubjectsLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite292 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite292,
                    new tempCallbackN1045A()
                );
              


        }

        private class tempCallbackN1045A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1045A(){ super(null);}

            public void receiveResultgetSubjectsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjectsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects getSubjects294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects294 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects294));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects getSubjects294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects294 here
                

                stub.startgetSubjects(
                         getSubjects294,
                    new tempCallbackN10480()
                );
              


        }

        private class tempCallbackN10480  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10480(){ super(null);}

            public void receiveResultgetSubjects(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite findGroupsLite296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite296 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite296));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite findGroupsLite296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite296 here
                

                stub.startfindGroupsLite(
                         findGroupsLite296,
                    new tempCallbackN104A6()
                );
              


        }

        private class tempCallbackN104A6  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104A6(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite getGroupsLite298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite298 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite298));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite getGroupsLite298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite298 here
                

                stub.startgetGroupsLite(
                         getGroupsLite298,
                    new tempCallbackN104CC()
                );
              


        }

        private class tempCallbackN104CC  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104CC(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite300 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite300));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite300 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite300,
                    new tempCallbackN104F2()
                );
              


        }

        private class tempCallbackN104F2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104F2(){ super(null);}

            public void receiveResultassignGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite assignAttributesLite302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite302 here
                
                        assertNotNull(stub.assignAttributesLite(
                        assignAttributesLite302));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite assignAttributesLite302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite302 here
                

                stub.startassignAttributesLite(
                         assignAttributesLite302,
                    new tempCallbackN10518()
                );
              


        }

        private class tempCallbackN10518  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10518(){ super(null);}

            public void receiveResultassignAttributesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivileges() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges assignGrouperPrivileges304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges304 here
                
                        assertNotNull(stub.assignGrouperPrivileges(
                        assignGrouperPrivileges304));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivileges() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges assignGrouperPrivileges304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges304 here
                

                stub.startassignGrouperPrivileges(
                         assignGrouperPrivileges304,
                    new tempCallbackN1053E()
                );
              


        }

        private class tempCallbackN1053E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1053E(){ super(null);}

            public void receiveResultassignGrouperPrivileges(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivileges(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments getPermissionAssignments306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments306 here
                
                        assertNotNull(stub.getPermissionAssignments(
                        getPermissionAssignments306));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments getPermissionAssignments306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments306 here
                

                stub.startgetPermissionAssignments(
                         getPermissionAssignments306,
                    new tempCallbackN10564()
                );
              


        }

        private class tempCallbackN10564  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10564(){ super(null);}

            public void receiveResultgetPermissionAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite getMembersLite308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite308 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite308));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite getMembersLite308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite308 here
                

                stub.startgetMembersLite(
                         getMembersLite308,
                    new tempCallbackN1058A()
                );
              


        }

        private class tempCallbackN1058A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1058A(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMemberships() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships getMemberships310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships310 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships310));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships getMemberships310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships310 here
                

                stub.startgetMemberships(
                         getMemberships310,
                    new tempCallbackN105B0()
                );
              


        }

        private class tempCallbackN105B0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN105B0(){ super(null);}

            public void receiveResultgetMemberships(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsResponse result
                            ) {
                
            }

            public void receiveErrorgetMemberships(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    