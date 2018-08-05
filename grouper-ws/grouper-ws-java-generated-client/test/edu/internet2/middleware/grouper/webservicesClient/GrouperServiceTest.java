

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
        public  void testgetAttributeAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite378=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite378 here
                
                        assertNotNull(stub.getAttributeAssignmentsLite(
                        getAttributeAssignmentsLite378));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignmentsLite getAttributeAssignmentsLite378=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignmentsLite.class);
                    // TODO : Fill in the getAttributeAssignmentsLite378 here
                

                stub.startgetAttributeAssignmentsLite(
                         getAttributeAssignmentsLite378,
                    new tempCallbackN65548()
                );
              


        }

        private class tempCallbackN65548  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65548(){ super(null);}

            public void receiveResultgetAttributeAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite380=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite380 here
                
                        assertNotNull(stub.attributeDefNameDeleteLite(
                        attributeDefNameDeleteLite380));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDeleteLite attributeDefNameDeleteLite380=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDeleteLite.class);
                    // TODO : Fill in the attributeDefNameDeleteLite380 here
                

                stub.startattributeDefNameDeleteLite(
                         attributeDefNameDeleteLite380,
                    new tempCallbackN65589()
                );
              


        }

        private class tempCallbackN65589  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65589(){ super(null);}

            public void receiveResultattributeDefNameDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDelete groupDelete382=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete382 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete382));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDelete groupDelete382=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDelete.class);
                    // TODO : Fill in the groupDelete382 here
                

                stub.startgroupDelete(
                         groupDelete382,
                    new tempCallbackN65630()
                );
              


        }

        private class tempCallbackN65630  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65630(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefNameInheritanceLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite384=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite384 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritanceLite(
                        assignAttributeDefNameInheritanceLite384));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritanceLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritanceLite assignAttributeDefNameInheritanceLite384=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritanceLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritanceLite.class);
                    // TODO : Fill in the assignAttributeDefNameInheritanceLite384 here
                

                stub.startassignAttributeDefNameInheritanceLite(
                         assignAttributeDefNameInheritanceLite384,
                    new tempCallbackN65671()
                );
              


        }

        private class tempCallbackN65671  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65671(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritanceLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritanceLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritanceLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindExternalSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindExternalSubjects findExternalSubjects386=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindExternalSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindExternalSubjects.class);
                    // TODO : Fill in the findExternalSubjects386 here
                
                        assertNotNull(stub.findExternalSubjects(
                        findExternalSubjects386));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindExternalSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindExternalSubjects findExternalSubjects386=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindExternalSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindExternalSubjects.class);
                    // TODO : Fill in the findExternalSubjects386 here
                

                stub.startfindExternalSubjects(
                         findExternalSubjects386,
                    new tempCallbackN65712()
                );
              


        }

        private class tempCallbackN65712  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65712(){ super(null);}

            public void receiveResultfindExternalSubjects(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindExternalSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorfindExternalSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMemberLite addMemberLite388=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite388 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite388));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMemberLite addMemberLite388=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite388 here
                

                stub.startaddMemberLite(
                         addMemberLite388,
                    new tempCallbackN65753()
                );
              


        }

        private class tempCallbackN65753  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65753(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributes() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributes assignAttributes390=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes390 here
                
                        assertNotNull(stub.assignAttributes(
                        assignAttributes390));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributes() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributes assignAttributes390=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributes)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributes.class);
                    // TODO : Fill in the assignAttributes390 here
                

                stub.startassignAttributes(
                         assignAttributes390,
                    new tempCallbackN65794()
                );
              


        }

        private class tempCallbackN65794  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65794(){ super(null);}

            public void receiveResultassignAttributes(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributes(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDelete attributeDefDelete392=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDelete.class);
                    // TODO : Fill in the attributeDefDelete392 here
                
                        assertNotNull(stub.attributeDefDelete(
                        attributeDefDelete392));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDelete attributeDefDelete392=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDelete.class);
                    // TODO : Fill in the attributeDefDelete392 here
                

                stub.startattributeDefDelete(
                         attributeDefDelete392,
                    new tempCallbackN65835()
                );
              


        }

        private class tempCallbackN65835  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65835(){ super(null);}

            public void receiveResultattributeDefDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDeleteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDeleteLite stemDeleteLite394=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite394 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite394));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDeleteLite stemDeleteLite394=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite394 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite394,
                    new tempCallbackN65876()
                );
              


        }

        private class tempCallbackN65876  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65876(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testexternalSubjectSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectSave externalSubjectSave396=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectSave.class);
                    // TODO : Fill in the externalSubjectSave396 here
                
                        assertNotNull(stub.externalSubjectSave(
                        externalSubjectSave396));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartexternalSubjectSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectSave externalSubjectSave396=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectSave.class);
                    // TODO : Fill in the externalSubjectSave396 here
                

                stub.startexternalSubjectSave(
                         externalSubjectSave396,
                    new tempCallbackN65917()
                );
              


        }

        private class tempCallbackN65917  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65917(){ super(null);}

            public void receiveResultexternalSubjectSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectSaveResponse result
                            ) {
                
            }

            public void receiveErrorexternalSubjectSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStemsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLite findStemsLite398=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite398 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite398));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLite findStemsLite398=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite398 here
                

                stub.startfindStemsLite(
                         findStemsLite398,
                    new tempCallbackN65958()
                );
              


        }

        private class tempCallbackN65958  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65958(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindStemsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDeleteLite groupDeleteLite400=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite400 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite400));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDeleteLite groupDeleteLite400=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite400 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite400,
                    new tempCallbackN65999()
                );
              


        }

        private class tempCallbackN65999  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN65999(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMemberships() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMemberships getMemberships402=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships402 here
                
                        assertNotNull(stub.getMemberships(
                        getMemberships402));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMemberships() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMemberships getMemberships402=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMemberships)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMemberships.class);
                    // TODO : Fill in the getMemberships402 here
                

                stub.startgetMemberships(
                         getMemberships402,
                    new tempCallbackN66040()
                );
              


        }

        private class tempCallbackN66040  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66040(){ super(null);}

            public void receiveResultgetMemberships(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembershipsResponse result
                            ) {
                
            }

            public void receiveErrorgetMemberships(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDelete attributeDefNameDelete404=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete404 here
                
                        assertNotNull(stub.attributeDefNameDelete(
                        attributeDefNameDelete404));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDelete attributeDefNameDelete404=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDelete.class);
                    // TODO : Fill in the attributeDefNameDelete404 here
                

                stub.startattributeDefNameDelete(
                         attributeDefNameDelete404,
                    new tempCallbackN66081()
                );
              


        }

        private class tempCallbackN66081  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66081(){ super(null);}

            public void receiveResultattributeDefNameDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameDeleteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroups findGroups406=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups406 here
                
                        assertNotNull(stub.findGroups(
                        findGroups406));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroups findGroups406=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroups.class);
                    // TODO : Fill in the findGroups406 here
                

                stub.startfindGroups(
                         findGroups406,
                    new tempCallbackN66122()
                );
              


        }

        private class tempCallbackN66122  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66122(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSave attributeDefSave408=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSave.class);
                    // TODO : Fill in the attributeDefSave408 here
                
                        assertNotNull(stub.attributeDefSave(
                        attributeDefSave408));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSave attributeDefSave408=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSave.class);
                    // TODO : Fill in the attributeDefSave408 here
                

                stub.startattributeDefSave(
                         attributeDefSave408,
                    new tempCallbackN66163()
                );
              


        }

        private class tempCallbackN66163  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66163(){ super(null);}

            public void receiveResultattributeDefSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSaveResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjects() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjects getSubjects410=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects410 here
                
                        assertNotNull(stub.getSubjects(
                        getSubjects410));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjects() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjects getSubjects410=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjects)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjects.class);
                    // TODO : Fill in the getSubjects410 here
                

                stub.startgetSubjects(
                         getSubjects410,
                    new tempCallbackN66204()
                );
              


        }

        private class tempCallbackN66204  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66204(){ super(null);}

            public void receiveResultgetSubjects(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjectsResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjects(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDelete stemDelete412=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete412 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete412));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDelete stemDelete412=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDelete.class);
                    // TODO : Fill in the stemDelete412 here
                

                stub.startstemDelete(
                         stemDelete412,
                    new tempCallbackN66245()
                );
              


        }

        private class tempCallbackN66245  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66245(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemDeleteResponse result
                            ) {
                
            }

            public void receiveErrorstemDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefNameInheritance() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance414=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance414 here
                
                        assertNotNull(stub.assignAttributeDefNameInheritance(
                        assignAttributeDefNameInheritance414));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefNameInheritance() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritance assignAttributeDefNameInheritance414=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritance)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritance.class);
                    // TODO : Fill in the assignAttributeDefNameInheritance414 here
                

                stub.startassignAttributeDefNameInheritance(
                         assignAttributeDefNameInheritance414,
                    new tempCallbackN66286()
                );
              


        }

        private class tempCallbackN66286  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66286(){ super(null);}

            public void receiveResultassignAttributeDefNameInheritance(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefNameInheritanceResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefNameInheritance(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignActionsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActionsLite getAttributeAssignActionsLite416=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActionsLite.class);
                    // TODO : Fill in the getAttributeAssignActionsLite416 here
                
                        assertNotNull(stub.getAttributeAssignActionsLite(
                        getAttributeAssignActionsLite416));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignActionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActionsLite getAttributeAssignActionsLite416=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActionsLite.class);
                    // TODO : Fill in the getAttributeAssignActionsLite416 here
                

                stub.startgetAttributeAssignActionsLite(
                         getAttributeAssignActionsLite416,
                    new tempCallbackN66327()
                );
              


        }

        private class tempCallbackN66327  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66327(){ super(null);}

            public void receiveResultgetAttributeAssignActionsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActionsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignActionsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefsLite findAttributeDefsLite418=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefsLite.class);
                    // TODO : Fill in the findAttributeDefsLite418 here
                
                        assertNotNull(stub.findAttributeDefsLite(
                        findAttributeDefsLite418));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefsLite findAttributeDefsLite418=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefsLite.class);
                    // TODO : Fill in the findAttributeDefsLite418 here
                

                stub.startfindAttributeDefsLite(
                         findAttributeDefsLite418,
                    new tempCallbackN66368()
                );
              


        }

        private class tempCallbackN66368  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66368(){ super(null);}

            public void receiveResultfindAttributeDefsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testacknowledge() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.Acknowledge acknowledge420=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.Acknowledge)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.Acknowledge.class);
                    // TODO : Fill in the acknowledge420 here
                
                        assertNotNull(stub.acknowledge(
                        acknowledge420));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartacknowledge() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.Acknowledge acknowledge420=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.Acknowledge)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.Acknowledge.class);
                    // TODO : Fill in the acknowledge420 here
                

                stub.startacknowledge(
                         acknowledge420,
                    new tempCallbackN66409()
                );
              


        }

        private class tempCallbackN66409  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66409(){ super(null);}

            public void receiveResultacknowledge(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AcknowledgeResponse result
                            ) {
                
            }

            public void receiveErroracknowledge(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesBatch() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesBatch assignAttributesBatch422=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesBatch)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesBatch.class);
                    // TODO : Fill in the assignAttributesBatch422 here
                
                        assertNotNull(stub.assignAttributesBatch(
                        assignAttributesBatch422));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesBatch() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesBatch assignAttributesBatch422=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesBatch)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesBatch.class);
                    // TODO : Fill in the assignAttributesBatch422 here
                

                stub.startassignAttributesBatch(
                         assignAttributesBatch422,
                    new tempCallbackN66450()
                );
              


        }

        private class tempCallbackN66450  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66450(){ super(null);}

            public void receiveResultassignAttributesBatch(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesBatchResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesBatch(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembersLite getMembersLite424=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite424 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite424));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembersLite getMembersLite424=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite424 here
                

                stub.startgetMembersLite(
                         getMembersLite424,
                    new tempCallbackN66491()
                );
              


        }

        private class tempCallbackN66491  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66491(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSaveLite stemSaveLite426=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite426 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite426));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSaveLite stemSaveLite426=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite426 here
                

                stub.startstemSaveLite(
                         stemSaveLite426,
                    new tempCallbackN66532()
                );
              


        }

        private class tempCallbackN66532  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66532(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testsendMessage() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.SendMessage sendMessage428=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.SendMessage)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.SendMessage.class);
                    // TODO : Fill in the sendMessage428 here
                
                        assertNotNull(stub.sendMessage(
                        sendMessage428));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartsendMessage() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.SendMessage sendMessage428=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.SendMessage)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.SendMessage.class);
                    // TODO : Fill in the sendMessage428 here
                

                stub.startsendMessage(
                         sendMessage428,
                    new tempCallbackN66573()
                );
              


        }

        private class tempCallbackN66573  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66573(){ super(null);}

            public void receiveResultsendMessage(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.SendMessageResponse result
                            ) {
                
            }

            public void receiveErrorsendMessage(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStems findStems430=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStems.class);
                    // TODO : Fill in the findStems430 here
                
                        assertNotNull(stub.findStems(
                        findStems430));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStems findStems430=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStems)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStems.class);
                    // TODO : Fill in the findStems430 here
                

                stub.startfindStems(
                         findStems430,
                    new tempCallbackN66614()
                );
              


        }

        private class tempCallbackN66614  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66614(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignmentsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite432=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite432 here
                
                        assertNotNull(stub.getPermissionAssignmentsLite(
                        getPermissionAssignmentsLite432));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignmentsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignmentsLite getPermissionAssignmentsLite432=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignmentsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignmentsLite.class);
                    // TODO : Fill in the getPermissionAssignmentsLite432 here
                

                stub.startgetPermissionAssignmentsLite(
                         getPermissionAssignmentsLite432,
                    new tempCallbackN66655()
                );
              


        }

        private class tempCallbackN66655  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66655(){ super(null);}

            public void receiveResultgetPermissionAssignmentsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignmentsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignmentsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignActions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActions getAttributeAssignActions434=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActions.class);
                    // TODO : Fill in the getAttributeAssignActions434 here
                
                        assertNotNull(stub.getAttributeAssignActions(
                        getAttributeAssignActions434));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignActions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActions getAttributeAssignActions434=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActions.class);
                    // TODO : Fill in the getAttributeAssignActions434 here
                

                stub.startgetAttributeAssignActions(
                         getAttributeAssignActions434,
                    new tempCallbackN66696()
                );
              


        }

        private class tempCallbackN66696  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66696(){ super(null);}

            public void receiveResultgetAttributeAssignActions(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignActionsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignActions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubject() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubject memberChangeSubject436=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject436 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject436));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubject memberChangeSubject436=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject436 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject436,
                    new tempCallbackN66737()
                );
              


        }

        private class tempCallbackN66737  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66737(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetAttributeAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignments getAttributeAssignments438=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments438 here
                
                        assertNotNull(stub.getAttributeAssignments(
                        getAttributeAssignments438));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetAttributeAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignments getAttributeAssignments438=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignments.class);
                    // TODO : Fill in the getAttributeAssignments438 here
                

                stub.startgetAttributeAssignments(
                         getAttributeAssignments438,
                    new tempCallbackN66778()
                );
              


        }

        private class tempCallbackN66778  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66778(){ super(null);}

            public void receiveResultgetAttributeAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetAttributeAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetAttributeAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMember hasMember440=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMember.class);
                    // TODO : Fill in the hasMember440 here
                
                        assertNotNull(stub.hasMember(
                        hasMember440));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMember hasMember440=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMember.class);
                    // TODO : Fill in the hasMember440 here
                

                stub.starthasMember(
                         hasMember440,
                    new tempCallbackN66819()
                );
              


        }

        private class tempCallbackN66819  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66819(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMemberResponse result
                            ) {
                
            }

            public void receiveErrorhasMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefNamesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite442=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite442 here
                
                        assertNotNull(stub.findAttributeDefNamesLite(
                        findAttributeDefNamesLite442));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNamesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNamesLite findAttributeDefNamesLite442=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNamesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNamesLite.class);
                    // TODO : Fill in the findAttributeDefNamesLite442 here
                

                stub.startfindAttributeDefNamesLite(
                         findAttributeDefNamesLite442,
                    new tempCallbackN66860()
                );
              


        }

        private class tempCallbackN66860  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66860(){ super(null);}

            public void receiveResultfindAttributeDefNamesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNamesLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNamesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSave attributeDefNameSave444=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave444 here
                
                        assertNotNull(stub.attributeDefNameSave(
                        attributeDefNameSave444));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSave attributeDefNameSave444=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSave.class);
                    // TODO : Fill in the attributeDefNameSave444 here
                

                stub.startattributeDefNameSave(
                         attributeDefNameSave444,
                    new tempCallbackN66901()
                );
              


        }

        private class tempCallbackN66901  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66901(){ super(null);}

            public void receiveResultattributeDefNameSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSaveResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMember addMember446=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMember.class);
                    // TODO : Fill in the addMember446 here
                
                        assertNotNull(stub.addMember(
                        addMember446));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMember addMember446=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMember.class);
                    // TODO : Fill in the addMember446 here
                

                stub.startaddMember(
                         addMember446,
                    new tempCallbackN66942()
                );
              


        }

        private class tempCallbackN66942  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66942(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AddMemberResponse result
                            ) {
                
            }

            public void receiveErroraddMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefNames() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNames findAttributeDefNames448=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames448 here
                
                        assertNotNull(stub.findAttributeDefNames(
                        findAttributeDefNames448));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefNames() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNames findAttributeDefNames448=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNames)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNames.class);
                    // TODO : Fill in the findAttributeDefNames448 here
                

                stub.startfindAttributeDefNames(
                         findAttributeDefNames448,
                    new tempCallbackN66983()
                );
              


        }

        private class tempCallbackN66983  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN66983(){ super(null);}

            public void receiveResultfindAttributeDefNames(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefNamesResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefNames(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissions assignPermissions450=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions450 here
                
                        assertNotNull(stub.assignPermissions(
                        assignPermissions450));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissions assignPermissions450=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissions.class);
                    // TODO : Fill in the assignPermissions450 here
                

                stub.startassignPermissions(
                         assignPermissions450,
                    new tempCallbackN67024()
                );
              


        }

        private class tempCallbackN67024  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67024(){ super(null);}

            public void receiveResultassignPermissions(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissionsResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite452=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite452 here
                
                        assertNotNull(stub.assignGrouperPrivilegesLite(
                        assignGrouperPrivilegesLite452));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivilegesLite assignGrouperPrivilegesLite452=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivilegesLite.class);
                    // TODO : Fill in the assignGrouperPrivilegesLite452 here
                

                stub.startassignGrouperPrivilegesLite(
                         assignGrouperPrivilegesLite452,
                    new tempCallbackN67065()
                );
              


        }

        private class tempCallbackN67065  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67065(){ super(null);}

            public void receiveResultassignGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignPermissionsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissionsLite assignPermissionsLite454=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite454 here
                
                        assertNotNull(stub.assignPermissionsLite(
                        assignPermissionsLite454));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignPermissionsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissionsLite assignPermissionsLite454=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissionsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissionsLite.class);
                    // TODO : Fill in the assignPermissionsLite454 here
                

                stub.startassignPermissionsLite(
                         assignPermissionsLite454,
                    new tempCallbackN67106()
                );
              


        }

        private class tempCallbackN67106  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67106(){ super(null);}

            public void receiveResultassignPermissionsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignPermissionsLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignPermissionsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSave stemSave456=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSave.class);
                    // TODO : Fill in the stemSave456 here
                
                        assertNotNull(stub.stemSave(
                        stemSave456));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSave stemSave456=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSave.class);
                    // TODO : Fill in the stemSave456 here
                

                stub.startstemSave(
                         stemSave456,
                    new tempCallbackN67147()
                );
              


        }

        private class tempCallbackN67147  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67147(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.StemSaveResponse result
                            ) {
                
            }

            public void receiveErrorstemSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroups getGroups458=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups458 here
                
                        assertNotNull(stub.getGroups(
                        getGroups458));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroups getGroups458=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroups)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroups.class);
                    // TODO : Fill in the getGroups458 here
                

                stub.startgetGroups(
                         getGroups458,
                    new tempCallbackN67188()
                );
              


        }

        private class tempCallbackN67188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67188(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroupsResponse result
                            ) {
                
            }

            public void receiveErrorgetGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGrouperPrivilegesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite460=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite460 here
                
                        assertNotNull(stub.getGrouperPrivilegesLite(
                        getGrouperPrivilegesLite460));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGrouperPrivilegesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGrouperPrivilegesLite getGrouperPrivilegesLite460=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGrouperPrivilegesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGrouperPrivilegesLite.class);
                    // TODO : Fill in the getGrouperPrivilegesLite460 here
                

                stub.startgetGrouperPrivilegesLite(
                         getGrouperPrivilegesLite460,
                    new tempCallbackN67229()
                );
              


        }

        private class tempCallbackN67229  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67229(){ super(null);}

            public void receiveResultgetGrouperPrivilegesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGrouperPrivilegesLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGrouperPrivilegesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributesLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesLite assignAttributesLite462=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite462 here
                
                        assertNotNull(stub.assignAttributesLite(
                        assignAttributesLite462));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributesLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesLite assignAttributesLite462=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesLite.class);
                    // TODO : Fill in the assignAttributesLite462 here
                

                stub.startassignAttributesLite(
                         assignAttributesLite462,
                    new tempCallbackN67270()
                );
              


        }

        private class tempCallbackN67270  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67270(){ super(null);}

            public void receiveResultassignAttributesLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributesLiteResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributesLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMemberLite deleteMemberLite464=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite464 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite464));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMemberLite deleteMemberLite464=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite464 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite464,
                    new tempCallbackN67311()
                );
              


        }

        private class tempCallbackN67311  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67311(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrordeleteMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetSubjectsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjectsLite getSubjectsLite466=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite466 here
                
                        assertNotNull(stub.getSubjectsLite(
                        getSubjectsLite466));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetSubjectsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjectsLite getSubjectsLite466=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjectsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjectsLite.class);
                    // TODO : Fill in the getSubjectsLite466 here
                

                stub.startgetSubjectsLite(
                         getSubjectsLite466,
                    new tempCallbackN67352()
                );
              


        }

        private class tempCallbackN67352  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67352(){ super(null);}

            public void receiveResultgetSubjectsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetSubjectsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetSubjectsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSave groupSave468=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave468 here
                
                        assertNotNull(stub.groupSave(
                        groupSave468));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSave groupSave468=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSave)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSave.class);
                    // TODO : Fill in the groupSave468 here
                

                stub.startgroupSave(
                         groupSave468,
                    new tempCallbackN67393()
                );
              


        }

        private class tempCallbackN67393  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67393(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSaveResponse result
                            ) {
                
            }

            public void receiveErrorgroupSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMemberLite hasMemberLite470=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite470 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite470));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMemberLite hasMemberLite470=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite470 here
                

                stub.starthasMemberLite(
                         hasMemberLite470,
                    new tempCallbackN67434()
                );
              


        }

        private class tempCallbackN67434  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67434(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignGrouperPrivileges() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivileges assignGrouperPrivileges472=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges472 here
                
                        assertNotNull(stub.assignGrouperPrivileges(
                        assignGrouperPrivileges472));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignGrouperPrivileges() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivileges assignGrouperPrivileges472=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivileges)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivileges.class);
                    // TODO : Fill in the assignGrouperPrivileges472 here
                

                stub.startassignGrouperPrivileges(
                         assignGrouperPrivileges472,
                    new tempCallbackN67475()
                );
              


        }

        private class tempCallbackN67475  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67475(){ super(null);}

            public void receiveResultassignGrouperPrivileges(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignGrouperPrivilegesResponse result
                            ) {
                
            }

            public void receiveErrorassignGrouperPrivileges(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSaveLite attributeDefSaveLite474=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSaveLite.class);
                    // TODO : Fill in the attributeDefSaveLite474 here
                
                        assertNotNull(stub.attributeDefSaveLite(
                        attributeDefSaveLite474));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSaveLite attributeDefSaveLite474=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSaveLite.class);
                    // TODO : Fill in the attributeDefSaveLite474 here
                

                stub.startattributeDefSaveLite(
                         attributeDefSaveLite474,
                    new tempCallbackN67516()
                );
              


        }

        private class tempCallbackN67516  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67516(){ super(null);}

            public void receiveResultattributeDefSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDeleteLite attributeDefDeleteLite476=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDeleteLite.class);
                    // TODO : Fill in the attributeDefDeleteLite476 here
                
                        assertNotNull(stub.attributeDefDeleteLite(
                        attributeDefDeleteLite476));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDeleteLite attributeDefDeleteLite476=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDeleteLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDeleteLite.class);
                    // TODO : Fill in the attributeDefDeleteLite476 here
                

                stub.startattributeDefDeleteLite(
                         attributeDefDeleteLite476,
                    new tempCallbackN67557()
                );
              


        }

        private class tempCallbackN67557  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67557(){ super(null);}

            public void receiveResultattributeDefDeleteLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetPermissionAssignments() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignments getPermissionAssignments478=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments478 here
                
                        assertNotNull(stub.getPermissionAssignments(
                        getPermissionAssignments478));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetPermissionAssignments() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignments getPermissionAssignments478=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignments)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignments.class);
                    // TODO : Fill in the getPermissionAssignments478 here
                

                stub.startgetPermissionAssignments(
                         getPermissionAssignments478,
                    new tempCallbackN67598()
                );
              


        }

        private class tempCallbackN67598  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67598(){ super(null);}

            public void receiveResultgetPermissionAssignments(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetPermissionAssignmentsResponse result
                            ) {
                
            }

            public void receiveErrorgetPermissionAssignments(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testexternalSubjectDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectDelete externalSubjectDelete480=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectDelete.class);
                    // TODO : Fill in the externalSubjectDelete480 here
                
                        assertNotNull(stub.externalSubjectDelete(
                        externalSubjectDelete480));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartexternalSubjectDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectDelete externalSubjectDelete480=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectDelete)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectDelete.class);
                    // TODO : Fill in the externalSubjectDelete480 here
                

                stub.startexternalSubjectDelete(
                         externalSubjectDelete480,
                    new tempCallbackN67639()
                );
              


        }

        private class tempCallbackN67639  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67639(){ super(null);}

            public void receiveResultexternalSubjectDelete(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExternalSubjectDeleteResponse result
                            ) {
                
            }

            public void receiveErrorexternalSubjectDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembers getMembers482=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers482 here
                
                        assertNotNull(stub.getMembers(
                        getMembers482));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembers getMembers482=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembers)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembers.class);
                    // TODO : Fill in the getMembers482 here
                

                stub.startgetMembers(
                         getMembers482,
                    new tempCallbackN67680()
                );
              


        }

        private class tempCallbackN67680  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67680(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembersResponse result
                            ) {
                
            }

            public void receiveErrorgetMembers(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubjectLite memberChangeSubjectLite484=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite484 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite484));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubjectLite memberChangeSubjectLite484=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite484 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite484,
                    new tempCallbackN67721()
                );
              


        }

        private class tempCallbackN67721  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67721(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMember deleteMember486=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember486 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember486));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMember deleteMember486=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMember)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMember.class);
                    // TODO : Fill in the deleteMember486 here
                

                stub.startdeleteMember(
                         deleteMember486,
                    new tempCallbackN67762()
                );
              


        }

        private class tempCallbackN67762  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67762(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindAttributeDefs() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefs findAttributeDefs488=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefs)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefs.class);
                    // TODO : Fill in the findAttributeDefs488 here
                
                        assertNotNull(stub.findAttributeDefs(
                        findAttributeDefs488));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindAttributeDefs() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefs findAttributeDefs488=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefs)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefs.class);
                    // TODO : Fill in the findAttributeDefs488 here
                

                stub.startfindAttributeDefs(
                         findAttributeDefs488,
                    new tempCallbackN67803()
                );
              


        }

        private class tempCallbackN67803  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67803(){ super(null);}

            public void receiveResultfindAttributeDefs(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindAttributeDefsResponse result
                            ) {
                
            }

            public void receiveErrorfindAttributeDefs(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testattributeDefNameSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite490=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite490 here
                
                        assertNotNull(stub.attributeDefNameSaveLite(
                        attributeDefNameSaveLite490));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartattributeDefNameSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSaveLite attributeDefNameSaveLite490=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSaveLite.class);
                    // TODO : Fill in the attributeDefNameSaveLite490 here
                

                stub.startattributeDefNameSaveLite(
                         attributeDefNameSaveLite490,
                    new tempCallbackN67844()
                );
              


        }

        private class tempCallbackN67844  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67844(){ super(null);}

            public void receiveResultattributeDefNameSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AttributeDefNameSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorattributeDefNameSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testassignAttributeDefActions() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefActions assignAttributeDefActions492=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefActions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefActions.class);
                    // TODO : Fill in the assignAttributeDefActions492 here
                
                        assertNotNull(stub.assignAttributeDefActions(
                        assignAttributeDefActions492));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartassignAttributeDefActions() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefActions assignAttributeDefActions492=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefActions)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefActions.class);
                    // TODO : Fill in the assignAttributeDefActions492 here
                

                stub.startassignAttributeDefActions(
                         assignAttributeDefActions492,
                    new tempCallbackN67885()
                );
              


        }

        private class tempCallbackN67885  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67885(){ super(null);}

            public void receiveResultassignAttributeDefActions(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.AssignAttributeDefActionsResponse result
                            ) {
                
            }

            public void receiveErrorassignAttributeDefActions(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembershipsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembershipsLite getMembershipsLite494=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite494 here
                
                        assertNotNull(stub.getMembershipsLite(
                        getMembershipsLite494));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembershipsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembershipsLite getMembershipsLite494=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembershipsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembershipsLite.class);
                    // TODO : Fill in the getMembershipsLite494 here
                

                stub.startgetMembershipsLite(
                         getMembershipsLite494,
                    new tempCallbackN67926()
                );
              


        }

        private class tempCallbackN67926  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67926(){ super(null);}

            public void receiveResultgetMembershipsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetMembershipsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembershipsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroupsLite findGroupsLite496=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite496 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite496));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroupsLite findGroupsLite496=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite496 here
                

                stub.startfindGroupsLite(
                         findGroupsLite496,
                    new tempCallbackN67967()
                );
              


        }

        private class tempCallbackN67967  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN67967(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.FindGroupsLiteResponse result
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

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroupsLite getGroupsLite498=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite498 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite498));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroupsLite getGroupsLite498=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite498 here
                

                stub.startgetGroupsLite(
                         getGroupsLite498,
                    new tempCallbackN68008()
                );
              


        }

        private class tempCallbackN68008  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN68008(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSaveLite groupSaveLite500=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite500 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite500));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSaveLite groupSaveLite500=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite500 here
                

                stub.startgroupSaveLite(
                         groupSaveLite500,
                    new tempCallbackN68049()
                );
              


        }

        private class tempCallbackN68049  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN68049(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.GroupSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testreceiveMessage() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ReceiveMessage receiveMessage502=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ReceiveMessage)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ReceiveMessage.class);
                    // TODO : Fill in the receiveMessage502 here
                
                        assertNotNull(stub.receiveMessage(
                        receiveMessage502));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartreceiveMessage() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ReceiveMessage receiveMessage502=
                                                        (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ReceiveMessage)getTestObject(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ReceiveMessage.class);
                    // TODO : Fill in the receiveMessage502 here
                

                stub.startreceiveMessage(
                         receiveMessage502,
                    new tempCallbackN68090()
                );
              


        }

        private class tempCallbackN68090  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN68090(){ super(null);}

            public void receiveResultreceiveMessage(
                         edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ReceiveMessageResponse result
                            ) {
                
            }

            public void receiveErrorreceiveMessage(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    