

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite282=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite282 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite282));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite282=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite282 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite282,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite hasMemberLite284=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite284 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite284));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite hasMemberLite284=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite284 here
                

                stub.starthasMemberLite(
                         hasMemberLite284,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite stemDeleteLite286=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite286 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite286));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite stemDeleteLite286=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite286 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite286,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions assignPermissions288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions288 here
                
                        assertNotNull(stub.assignPermissions(
                        assignPermissions288));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions assignPermissions288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions288 here
                

                stub.startassignPermissions(
                         assignPermissions288,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes assignAttributes290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes290 here
                
                        assertNotNull(stub.assignAttributes(
                        assignAttributes290));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributes() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes assignAttributes290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes290 here
                

                stub.startassignAttributes(
                         assignAttributes290,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember deleteMember292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember292 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember292));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember deleteMember292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember292 here
                

                stub.startdeleteMember(
                         deleteMember292,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite stemSaveLite294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite294 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite294));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite stemSaveLite294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite294 here
                

                stub.startstemSaveLite(
                         stemSaveLite294,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite groupSaveLite296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite296 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite296));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite groupSaveLite296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite296 here
                

                stub.startgroupSaveLite(
                         groupSaveLite296,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember addMember298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember.class);
                    // TODO : Fill in the addMember298 here
                
                        assertNotNull(stub.addMember(
                        addMember298));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember addMember298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMember.class);
                    // TODO : Fill in the addMember298 here
                

                stub.startaddMember(
                         addMember298,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups findGroups300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups300 here
                
                        assertNotNull(stub.findGroups(
                        findGroups300));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups findGroups300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups300 here
                

                stub.startfindGroups(
                         findGroups300,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember hasMember302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember.class);
                    // TODO : Fill in the hasMember302 here
                
                        assertNotNull(stub.hasMember(
                        hasMember302));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember hasMember302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.HasMember.class);
                    // TODO : Fill in the hasMember302 here
                

                stub.starthasMember(
                         hasMember302,
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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete stemDelete304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete304 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete304));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete stemDelete304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete304 here
                

                stub.startstemDelete(
                         stemDelete304,
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
        public  void testassignPermissionsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite assignPermissionsLite306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite306 here
                
                        assertNotNull(stub.assignPermissionsLite(
                        assignPermissionsLite306));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite assignPermissionsLite306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite306 here
                

                stub.startassignPermissionsLite(
                         assignPermissionsLite306,
                    new tempCallbackN101D4()
                );
              


        }

        private class tempCallbackN101D4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101D4(){ super(null);}

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
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite getMembershipsLite308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite308 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite308));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite getMembershipsLite308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite308 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite308,
                    new tempCallbackN101FA()
                );
              


        }

        private class tempCallbackN101FA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101FA(){ super(null);}

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
        public  void testgetPermissionAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite310 here
                
                        assertNotNull(stub.getPermissionAssignmentsLite(
                        getPermissionAssignmentsLite310));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite310 here
                

                stub.startgetPermissionAssignmentsLite(
                         getPermissionAssignmentsLite310,
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
        public  void testattributeDefNameSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite312=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite312 here
                
                        assertNotNull(stub.attributeDefNameSaveLite(
                        attributeDefNameSaveLite312));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite312=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite312 here
                

                stub.startattributeDefNameSaveLite(
                         attributeDefNameSaveLite312,
                    new tempCallbackN10246()
                );
              


        }

        private class tempCallbackN10246  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10246(){ super(null);}

            public void receiveResultattributeDefNameSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete attributeDefNameDelete314=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete314 here
                
                        assertNotNull(stub.attributeDefNameDelete(
                        attributeDefNameDelete314));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete attributeDefNameDelete314=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete314 here
                

                stub.startattributeDefNameDelete(
                         attributeDefNameDelete314,
                    new tempCallbackN1026C()
                );
              


        }

        private class tempCallbackN1026C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1026C(){ super(null);}

            public void receiveResultattributeDefNameDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite addMemberLite316=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite316 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite316));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite addMemberLite316=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite316 here
                

                stub.startaddMemberLite(
                         addMemberLite316,
                    new tempCallbackN10292()
                );
              


        }

        private class tempCallbackN10292  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10292(){ super(null);}

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
        public  void testattributeDefNameDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite318=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite318 here
                
                        assertNotNull(stub.attributeDefNameDeleteLite(
                        attributeDefNameDeleteLite318));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite318=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite318 here
                

                stub.startattributeDefNameDeleteLite(
                         attributeDefNameDeleteLite318,
                    new tempCallbackN102B8()
                );
              


        }

        private class tempCallbackN102B8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102B8(){ super(null);}

            public void receiveResultattributeDefNameDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave attributeDefNameSave320=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave320 here
                
                        assertNotNull(stub.attributeDefNameSave(
                        attributeDefNameSave320));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave attributeDefNameSave320=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave320 here
                

                stub.startattributeDefNameSave(
                         attributeDefNameSave320,
                    new tempCallbackN102DE()
                );
              


        }

        private class tempCallbackN102DE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102DE(){ super(null);}

            public void receiveResultattributeDefNameSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AttributeDefNameSaveResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments getAttributeAssignments322=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments322 here
                
                        assertNotNull(stub.getAttributeAssignments(
                        getAttributeAssignments322));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments getAttributeAssignments322=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments322 here
                

                stub.startgetAttributeAssignments(
                         getAttributeAssignments322,
                    new tempCallbackN10304()
                );
              


        }

        private class tempCallbackN10304  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10304(){ super(null);}

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
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave groupSave324=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave324 here
                
                        assertNotNull(stub.groupSave(
                        groupSave324));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave groupSave324=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave324 here
                

                stub.startgroupSave(
                         groupSave324,
                    new tempCallbackN1032A()
                );
              


        }

        private class tempCallbackN1032A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1032A(){ super(null);}

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
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite groupDeleteLite326=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite326 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite326));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite groupDeleteLite326=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite326 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite326,
                    new tempCallbackN10350()
                );
              


        }

        private class tempCallbackN10350  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10350(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite findStemsLite328=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite328 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite328));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite findStemsLite328=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite328 here
                

                stub.startfindStemsLite(
                         findStemsLite328,
                    new tempCallbackN10376()
                );
              


        }

        private class tempCallbackN10376  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10376(){ super(null);}

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
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite deleteMemberLite330=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite330 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite330));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite deleteMemberLite330=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite330 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite330,
                    new tempCallbackN1039C()
                );
              


        }

        private class tempCallbackN1039C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1039C(){ super(null);}

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
        public  void testfindAttributeDefNames() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames findAttributeDefNames332=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames332 here
                
                        assertNotNull(stub.findAttributeDefNames(
                        findAttributeDefNames332));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNames() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames findAttributeDefNames332=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames332 here
                

                stub.startfindAttributeDefNames(
                         findAttributeDefNames332,
                    new tempCallbackN103C2()
                );
              


        }

        private class tempCallbackN103C2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103C2(){ super(null);}

            public void receiveResultfindAttributeDefNames(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNames(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubject() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject memberChangeSubject334=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject334 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject334));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject memberChangeSubject334=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject334 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject334,
                    new tempCallbackN103E8()
                );
              


        }

        private class tempCallbackN103E8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN103E8(){ super(null);}

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
        public  void teststemSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave stemSave336=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave.class);
                    // TODO : Fill in the stemSave336 here
                
                        assertNotNull(stub.stemSave(
                        stemSave336));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave stemSave336=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.StemSave.class);
                    // TODO : Fill in the stemSave336 here
                

                stub.startstemSave(
                         stemSave336,
                    new tempCallbackN1040E()
                );
              


        }

        private class tempCallbackN1040E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1040E(){ super(null);}

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
        public  void testassignAttributeDefNameInheritanceLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite338=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite338 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritanceLite(
                        assignAttributeDefNameInheritanceLite338));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritanceLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite338=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite338 here
                

                stub.startassignAttributeDefNameInheritanceLite(
                         assignAttributeDefNameInheritanceLite338,
                    new tempCallbackN10434()
                );
              


        }

        private class tempCallbackN10434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10434(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritanceLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritanceLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefNameInheritance() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance340=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance340 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritance(
                        assignAttributeDefNameInheritance340));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritance() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance340=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance340 here
                

                stub.startassignAttributeDefNameInheritance(
                         assignAttributeDefNameInheritance340,
                    new tempCallbackN1045A()
                );
              


        }

        private class tempCallbackN1045A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1045A(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritance(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributeDefNameInheritanceResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritance(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite memberChangeSubjectLite342=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite342 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite342));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite memberChangeSubjectLite342=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite342 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite342,
                    new tempCallbackN10480()
                );
              


        }

        private class tempCallbackN10480  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10480(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers getMembers344=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers344 here
                
                        assertNotNull(stub.getMembers(
                        getMembers344));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers getMembers344=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers344 here
                

                stub.startgetMembers(
                         getMembers344,
                    new tempCallbackN104A6()
                );
              


        }

        private class tempCallbackN104A6  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104A6(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups getGroups346=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups346 here
                
                        assertNotNull(stub.getGroups(
                        getGroups346));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups getGroups346=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups346 here
                

                stub.startgetGroups(
                         getGroups346,
                    new tempCallbackN104CC()
                );
              


        }

        private class tempCallbackN104CC  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104CC(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems findStems348=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems.class);
                    // TODO : Fill in the findStems348 here
                
                        assertNotNull(stub.findStems(
                        findStems348));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems findStems348=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindStems.class);
                    // TODO : Fill in the findStems348 here
                

                stub.startfindStems(
                         findStems348,
                    new tempCallbackN104F2()
                );
              


        }

        private class tempCallbackN104F2  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN104F2(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite350=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite350 here
                
                        assertNotNull(stub.getAttributeAssignmentsLite(
                        getAttributeAssignmentsLite350));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite350=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite350 here
                

                stub.startgetAttributeAssignmentsLite(
                         getAttributeAssignmentsLite350,
                    new tempCallbackN10518()
                );
              


        }

        private class tempCallbackN10518  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10518(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete groupDelete352=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete352 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete352));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete groupDelete352=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete352 here
                

                stub.startgroupDelete(
                         groupDelete352,
                    new tempCallbackN1053E()
                );
              


        }

        private class tempCallbackN1053E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1053E(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite getSubjectsLite354=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite354 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite354));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite getSubjectsLite354=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite354 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite354,
                    new tempCallbackN10564()
                );
              


        }

        private class tempCallbackN10564  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10564(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects getSubjects356=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects356 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects356));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects getSubjects356=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects356 here
                

                stub.startgetSubjects(
                         getSubjects356,
                    new tempCallbackN1058A()
                );
              


        }

        private class tempCallbackN1058A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1058A(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite findGroupsLite358=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite358 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite358));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite findGroupsLite358=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite358 here
                

                stub.startfindGroupsLite(
                         findGroupsLite358,
                    new tempCallbackN105B0()
                );
              


        }

        private class tempCallbackN105B0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN105B0(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite getGroupsLite360=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite360 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite360));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite getGroupsLite360=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite360 here
                

                stub.startgetGroupsLite(
                         getGroupsLite360,
                    new tempCallbackN105D6()
                );
              


        }

        private class tempCallbackN105D6  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN105D6(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite362=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite362 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite362));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite362=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite362 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite362,
                    new tempCallbackN105FC()
                );
              


        }

        private class tempCallbackN105FC  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN105FC(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite assignAttributesLite364=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite364 here
                
                        assertNotNull(stub.assignAttributesLite(
                        assignAttributesLite364));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite assignAttributesLite364=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite364 here
                

                stub.startassignAttributesLite(
                         assignAttributesLite364,
                    new tempCallbackN10622()
                );
              


        }

        private class tempCallbackN10622  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10622(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges assignGrouperPrivileges366=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges366 here
                
                        assertNotNull(stub.assignGrouperPrivileges(
                        assignGrouperPrivileges366));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivileges() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges assignGrouperPrivileges366=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges366 here
                

                stub.startassignGrouperPrivileges(
                         assignGrouperPrivileges366,
                    new tempCallbackN10648()
                );
              


        }

        private class tempCallbackN10648  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10648(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments getPermissionAssignments368=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments368 here
                
                        assertNotNull(stub.getPermissionAssignments(
                        getPermissionAssignments368));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments getPermissionAssignments368=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments368 here
                

                stub.startgetPermissionAssignments(
                         getPermissionAssignments368,
                    new tempCallbackN1066E()
                );
              


        }

        private class tempCallbackN1066E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1066E(){ super(null);}

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
        public  void testfindAttributeDefNamesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite370=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite370 here
                
                        assertNotNull(stub.findAttributeDefNamesLite(
                        findAttributeDefNamesLite370));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNamesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite370=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite370 here
                

                stub.startfindAttributeDefNamesLite(
                         findAttributeDefNamesLite370,
                    new tempCallbackN10694()
                );
              


        }

        private class tempCallbackN10694  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10694(){ super(null);}

            public void receiveResultfindAttributeDefNamesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.FindAttributeDefNamesLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNamesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite getMembersLite372=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite372 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite372));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite getMembersLite372=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite372 here
                

                stub.startgetMembersLite(
                         getMembersLite372,
                    new tempCallbackN106BA()
                );
              


        }

        private class tempCallbackN106BA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN106BA(){ super(null);}

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

           edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships getMemberships374=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships374 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships374));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships getMemberships374=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships374 here
                

                stub.startgetMemberships(
                         getMemberships374,
                    new tempCallbackN106E0()
                );
              


        }

        private class tempCallbackN106E0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN106E0(){ super(null);}

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
    