

/**
 * GrouperServiceTest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:22:40 CEST)
 */
    package edu.internet2.middleware.grouper.webservicesClient;

    /*
     *  GrouperServiceTest Junit test case
    */

    public class GrouperServiceTest extends junit.framework.TestCase{

     
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignments getPermissionAssignments288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments288 here
                
                        assertNotNull(stub.getPermissionAssignments(
                        getPermissionAssignments288));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignments getPermissionAssignments288=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments288 here
                

                stub.startgetPermissionAssignments(
                         getPermissionAssignments288,
                    new tempCallbackN65548()
                );
              


        }

        private class tempCallbackN65548  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65548(){ super(null);}

            public void receiveResultgetPermissionAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStems findStems290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStems.class);
                    // TODO : Fill in the findStems290 here
                
                        assertNotNull(stub.findStems(
                        findStems290));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStems findStems290=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStems.class);
                    // TODO : Fill in the findStems290 here
                

                stub.startfindStems(
                         findStems290,
                    new tempCallbackN65589()
                );
              


        }

        private class tempCallbackN65589  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65589(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStemsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLite findStemsLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite292 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite292));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLite findStemsLite292=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite292 here
                

                stub.startfindStemsLite(
                         findStemsLite292,
                    new tempCallbackN65630()
                );
              


        }

        private class tempCallbackN65630  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65630(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindStemsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindStemsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMember addMember294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMember.class);
                    // TODO : Fill in the addMember294 here
                
                        assertNotNull(stub.addMember(
                        addMember294));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMember addMember294=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMember.class);
                    // TODO : Fill in the addMember294 here
                

                stub.startaddMember(
                         addMember294,
                    new tempCallbackN65671()
                );
              


        }

        private class tempCallbackN65671  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65671(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberResponse result
                            ) {
                
            }

            public void receiveErroraddMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMemberships() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMemberships getMemberships296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships296 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships296));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMemberships getMemberships296=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships296 here
                

                stub.startgetMemberships(
                         getMemberships296,
                    new tempCallbackN65712()
                );
              


        }

        private class tempCallbackN65712  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65712(){ super(null);}

            public void receiveResultgetMemberships(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsResponse result
                            ) {
                
            }

            public void receiveErrorgetMemberships(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDelete stemDelete298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete298 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete298));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDelete stemDelete298=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete298 here
                

                stub.startstemDelete(
                         stemDelete298,
                    new tempCallbackN65753()
                );
              


        }

        private class tempCallbackN65753  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65753(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteResponse result
                            ) {
                
            }

            public void receiveErrorstemDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite300 here
                
                        assertNotNull(stub.getPermissionAssignmentsLite(
                        getPermissionAssignmentsLite300));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite300=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite300 here
                

                stub.startgetPermissionAssignmentsLite(
                         getPermissionAssignmentsLite300,
                    new tempCallbackN65794()
                );
              


        }

        private class tempCallbackN65794  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65794(){ super(null);}

            public void receiveResultgetPermissionAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetPermissionAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissions assignPermissions302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions302 here
                
                        assertNotNull(stub.assignPermissions(
                        assignPermissions302));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissions assignPermissions302=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions302 here
                

                stub.startassignPermissions(
                         assignPermissions302,
                    new tempCallbackN65835()
                );
              


        }

        private class tempCallbackN65835  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65835(){ super(null);}

            public void receiveResultassignPermissions(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefNameInheritanceLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite304 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritanceLite(
                        assignAttributeDefNameInheritanceLite304));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritanceLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite304=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite304 here
                

                stub.startassignAttributeDefNameInheritanceLite(
                         assignAttributeDefNameInheritanceLite304,
                    new tempCallbackN65876()
                );
              


        }

        private class tempCallbackN65876  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65876(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritanceLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritanceLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite306 here
                
                        assertNotNull(stub.getAttributeAssignmentsLite(
                        getAttributeAssignmentsLite306));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite306=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite306 here
                

                stub.startgetAttributeAssignmentsLite(
                         getAttributeAssignmentsLite306,
                    new tempCallbackN65917()
                );
              


        }

        private class tempCallbackN65917  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65917(){ super(null);}

            public void receiveResultgetAttributeAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLite getMembershipsLite308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite308 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite308));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLite getMembershipsLite308=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite308 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite308,
                    new tempCallbackN65958()
                );
              


        }

        private class tempCallbackN65958  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65958(){ super(null);}

            public void receiveResultgetMembershipsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembershipsLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite assignPermissionsLite310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite310 here
                
                        assertNotNull(stub.assignPermissionsLite(
                        assignPermissionsLite310));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite assignPermissionsLite310=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite310 here
                

                stub.startassignPermissionsLite(
                         assignPermissionsLite310,
                    new tempCallbackN65999()
                );
              


        }

        private class tempCallbackN65999  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65999(){ super(null);}

            public void receiveResultassignPermissionsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignPermissionsLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissionsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLite stemSaveLite312=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite312 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite312));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLite stemSaveLite312=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite312 here
                

                stub.startstemSaveLite(
                         stemSaveLite312,
                    new tempCallbackN66040()
                );
              


        }

        private class tempCallbackN66040  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66040(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLite findGroupsLite314=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite314 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite314));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLite findGroupsLite314=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite314 here
                

                stub.startfindGroupsLite(
                         findGroupsLite314,
                    new tempCallbackN66081()
                );
              


        }

        private class tempCallbackN66081  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66081(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefNameInheritance() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance316=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance316 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritance(
                        assignAttributeDefNameInheritance316));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritance() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance316=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance316 here
                

                stub.startassignAttributeDefNameInheritance(
                         assignAttributeDefNameInheritance316,
                    new tempCallbackN66122()
                );
              


        }

        private class tempCallbackN66122  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66122(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritance(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributeDefNameInheritanceResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritance(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLite addMemberLite318=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite318 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite318));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLite addMemberLite318=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite318 here
                

                stub.startaddMemberLite(
                         addMemberLite318,
                    new tempCallbackN66163()
                );
              


        }

        private class tempCallbackN66163  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66163(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMember hasMember320=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMember.class);
                    // TODO : Fill in the hasMember320 here
                
                        assertNotNull(stub.hasMember(
                        hasMember320));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMember hasMember320=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMember.class);
                    // TODO : Fill in the hasMember320 here
                

                stub.starthasMember(
                         hasMember320,
                    new tempCallbackN66204()
                );
              


        }

        private class tempCallbackN66204  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66204(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberResponse result
                            ) {
                
            }

            public void receiveErrorhasMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups getGroups322=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups322 here
                
                        assertNotNull(stub.getGroups(
                        getGroups322));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups getGroups322=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups322 here
                

                stub.startgetGroups(
                         getGroups322,
                    new tempCallbackN66245()
                );
              


        }

        private class tempCallbackN66245  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66245(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsResponse result
                            ) {
                
            }

            public void receiveErrorgetGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLite deleteMemberLite324=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite324 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite324));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLite deleteMemberLite324=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite324 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite324,
                    new tempCallbackN66286()
                );
              


        }

        private class tempCallbackN66286  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66286(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrordeleteMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubject() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubject memberChangeSubject326=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject326 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject326));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubject memberChangeSubject326=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject326 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject326,
                    new tempCallbackN66327()
                );
              


        }

        private class tempCallbackN66327  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66327(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLite stemDeleteLite328=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite328 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite328));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLite stemDeleteLite328=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite328 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite328,
                    new tempCallbackN66368()
                );
              


        }

        private class tempCallbackN66368  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66368(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesBatch() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatch assignAttributesBatch330=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatch)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatch.class);
                    // TODO : Fill in the assignAttributesBatch330 here
                
                        assertNotNull(stub.assignAttributesBatch(
                        assignAttributesBatch330));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesBatch() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatch assignAttributesBatch330=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatch)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatch.class);
                    // TODO : Fill in the assignAttributesBatch330 here
                

                stub.startassignAttributesBatch(
                         assignAttributesBatch330,
                    new tempCallbackN66409()
                );
              


        }

        private class tempCallbackN66409  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66409(){ super(null);}

            public void receiveResultassignAttributesBatch(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesBatchResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesBatch(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDelete attributeDefNameDelete332=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete332 here
                
                        assertNotNull(stub.attributeDefNameDelete(
                        attributeDefNameDelete332));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDelete attributeDefNameDelete332=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete332 here
                

                stub.startattributeDefNameDelete(
                         attributeDefNameDelete332,
                    new tempCallbackN66450()
                );
              


        }

        private class tempCallbackN66450  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66450(){ super(null);}

            public void receiveResultattributeDefNameDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributes() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributes assignAttributes334=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes334 here
                
                        assertNotNull(stub.assignAttributes(
                        assignAttributes334));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributes() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributes assignAttributes334=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes334 here
                

                stub.startassignAttributes(
                         assignAttributes334,
                    new tempCallbackN66491()
                );
              


        }

        private class tempCallbackN66491  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66491(){ super(null);}

            public void receiveResultassignAttributes(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributes(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLite groupDeleteLite336=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite336 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite336));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLite groupDeleteLite336=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite336 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite336,
                    new tempCallbackN66532()
                );
              


        }

        private class tempCallbackN66532  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66532(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects getSubjects338=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects338 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects338));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects getSubjects338=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects338 here
                

                stub.startgetSubjects(
                         getSubjects338,
                    new tempCallbackN66573()
                );
              


        }

        private class tempCallbackN66573  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66573(){ super(null);}

            public void receiveResultgetSubjects(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite340=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite340 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite340));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite340=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite340 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite340,
                    new tempCallbackN66614()
                );
              


        }

        private class tempCallbackN66614  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66614(){ super(null);}

            public void receiveResultassignGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLite hasMemberLite342=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite342 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite342));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLite hasMemberLite342=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite342 here
                

                stub.starthasMemberLite(
                         hasMemberLite342,
                    new tempCallbackN66655()
                );
              


        }

        private class tempCallbackN66655  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66655(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSave stemSave344=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSave.class);
                    // TODO : Fill in the stemSave344 here
                
                        assertNotNull(stub.stemSave(
                        stemSave344));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSave stemSave344=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSave.class);
                    // TODO : Fill in the stemSave344 here
                

                stub.startstemSave(
                         stemSave344,
                    new tempCallbackN66696()
                );
              


        }

        private class tempCallbackN66696  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66696(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.StemSaveResponse result
                            ) {
                
            }

            public void receiveErrorstemSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSave attributeDefNameSave346=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave346 here
                
                        assertNotNull(stub.attributeDefNameSave(
                        attributeDefNameSave346));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSave attributeDefNameSave346=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave346 here
                

                stub.startattributeDefNameSave(
                         attributeDefNameSave346,
                    new tempCallbackN66737()
                );
              


        }

        private class tempCallbackN66737  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66737(){ super(null);}

            public void receiveResultattributeDefNameSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite348=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite348 here
                
                        assertNotNull(stub.attributeDefNameSaveLite(
                        attributeDefNameSaveLite348));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite348=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite348 here
                

                stub.startattributeDefNameSaveLite(
                         attributeDefNameSaveLite348,
                    new tempCallbackN66778()
                );
              


        }

        private class tempCallbackN66778  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66778(){ super(null);}

            public void receiveResultattributeDefNameSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLite getGroupsLite350=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite350 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite350));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLite getGroupsLite350=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite350 here
                

                stub.startgetGroupsLite(
                         getGroupsLite350,
                    new tempCallbackN66819()
                );
              


        }

        private class tempCallbackN66819  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66819(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite352=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite352 here
                
                        assertNotNull(stub.attributeDefNameDeleteLite(
                        attributeDefNameDeleteLite352));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite352=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite352 here
                

                stub.startattributeDefNameDeleteLite(
                         attributeDefNameDeleteLite352,
                    new tempCallbackN66860()
                );
              


        }

        private class tempCallbackN66860  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66860(){ super(null);}

            public void receiveResultattributeDefNameDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AttributeDefNameDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefNamesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite354=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite354 here
                
                        assertNotNull(stub.findAttributeDefNamesLite(
                        findAttributeDefNamesLite354));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNamesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite354=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite354 here
                

                stub.startfindAttributeDefNamesLite(
                         findAttributeDefNamesLite354,
                    new tempCallbackN66901()
                );
              


        }

        private class tempCallbackN66901  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66901(){ super(null);}

            public void receiveResultfindAttributeDefNamesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNamesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroups findGroups356=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups356 here
                
                        assertNotNull(stub.findGroups(
                        findGroups356));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroups findGroups356=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups356 here
                

                stub.startfindGroups(
                         findGroups356,
                    new tempCallbackN66942()
                );
              


        }

        private class tempCallbackN66942  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66942(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjectsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLite getSubjectsLite358=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite358 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite358));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLite getSubjectsLite358=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite358 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite358,
                    new tempCallbackN66983()
                );
              


        }

        private class tempCallbackN66983  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66983(){ super(null);}

            public void receiveResultgetSubjectsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetSubjectsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjectsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDelete groupDelete360=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete360 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete360));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDelete groupDelete360=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete360 here
                

                stub.startgroupDelete(
                         groupDelete360,
                    new tempCallbackN67024()
                );
              


        }

        private class tempCallbackN67024  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67024(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLite assignAttributesLite362=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite362 here
                
                        assertNotNull(stub.assignAttributesLite(
                        assignAttributesLite362));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLite assignAttributesLite362=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite362 here
                

                stub.startassignAttributesLite(
                         assignAttributesLite362,
                    new tempCallbackN67065()
                );
              


        }

        private class tempCallbackN67065  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67065(){ super(null);}

            public void receiveResultassignAttributesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignAttributesLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivileges assignGrouperPrivileges364=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges364 here
                
                        assertNotNull(stub.assignGrouperPrivileges(
                        assignGrouperPrivileges364));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivileges() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivileges assignGrouperPrivileges364=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges364 here
                

                stub.startassignGrouperPrivileges(
                         assignGrouperPrivileges364,
                    new tempCallbackN67106()
                );
              


        }

        private class tempCallbackN67106  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67106(){ super(null);}

            public void receiveResultassignGrouperPrivileges(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.AssignGrouperPrivilegesResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivileges(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefNames() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNames findAttributeDefNames366=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames366 here
                
                        assertNotNull(stub.findAttributeDefNames(
                        findAttributeDefNames366));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNames() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNames findAttributeDefNames366=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames366 here
                

                stub.startfindAttributeDefNames(
                         findAttributeDefNames366,
                    new tempCallbackN67147()
                );
              


        }

        private class tempCallbackN67147  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67147(){ super(null);}

            public void receiveResultfindAttributeDefNames(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.FindAttributeDefNamesResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNames(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSave groupSave368=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave368 here
                
                        assertNotNull(stub.groupSave(
                        groupSave368));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSave groupSave368=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave368 here
                

                stub.startgroupSave(
                         groupSave368,
                    new tempCallbackN67188()
                );
              


        }

        private class tempCallbackN67188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67188(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveResponse result
                            ) {
                
            }

            public void receiveErrorgroupSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite370=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite370 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite370));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite370=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite370 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite370,
                    new tempCallbackN67229()
                );
              


        }

        private class tempCallbackN67229  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67229(){ super(null);}

            public void receiveResultgetGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMember deleteMember372=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember372 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember372));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMember deleteMember372=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember372 here
                

                stub.startdeleteMember(
                         deleteMember372,
                    new tempCallbackN67270()
                );
              


        }

        private class tempCallbackN67270  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67270(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignments getAttributeAssignments374=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments374 here
                
                        assertNotNull(stub.getAttributeAssignments(
                        getAttributeAssignments374));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignments getAttributeAssignments374=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments374 here
                

                stub.startgetAttributeAssignments(
                         getAttributeAssignments374,
                    new tempCallbackN67311()
                );
              


        }

        private class tempCallbackN67311  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67311(){ super(null);}

            public void receiveResultgetAttributeAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetAttributeAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLite groupSaveLite376=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite376 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite376));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLite groupSaveLite376=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite376 here
                

                stub.startgroupSaveLite(
                         groupSaveLite376,
                    new tempCallbackN67352()
                );
              


        }

        private class tempCallbackN67352  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67352(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GroupSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLite memberChangeSubjectLite378=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite378 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite378));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLite memberChangeSubjectLite378=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite378 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite378,
                    new tempCallbackN67393()
                );
              


        }

        private class tempCallbackN67393  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67393(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLite getMembersLite380=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite380 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite380));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLite getMembersLite380=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite380 here
                

                stub.startgetMembersLite(
                         getMembersLite380,
                    new tempCallbackN67434()
                );
              


        }

        private class tempCallbackN67434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67434(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembers getMembers382=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers382 here
                
                        assertNotNull(stub.getMembers(
                        getMembers382));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembers getMembers382=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers382 here
                

                stub.startgetMembers(
                         getMembers382,
                    new tempCallbackN67475()
                );
              


        }

        private class tempCallbackN67475  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67475(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.ws.soap_v2_2.xsd.GetMembersResponse result
                            ) {
                
            }

            public void receiveErrorgetMembers(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    