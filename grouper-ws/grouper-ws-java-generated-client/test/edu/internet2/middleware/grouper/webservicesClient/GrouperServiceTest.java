

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
        public  void testaddMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite addMemberLite96=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite96 here
                
                        assertNotNull(stub.addMemberLite(
                        addMemberLite96));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite addMemberLite96=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLite.class);
                    // TODO : Fill in the addMemberLite96 here
                

                stub.startaddMemberLite(
                         addMemberLite96,
                    new tempCallbackN1000C()
                );
              


        }

        private class tempCallbackN1000C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1000C(){ super(null);}

            public void receiveResultaddMemberLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberLiteResponse result
                            ) {
                
            }

            public void receiveErroraddMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testmemberChangeSubjectLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectLite memberChangeSubjectLite98=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite98 here
                
                        assertNotNull(stub.memberChangeSubjectLite(
                        memberChangeSubjectLite98));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubjectLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectLite memberChangeSubjectLite98=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectLite.class);
                    // TODO : Fill in the memberChangeSubjectLite98 here
                

                stub.startmemberChangeSubjectLite(
                         memberChangeSubjectLite98,
                    new tempCallbackN10032()
                );
              


        }

        private class tempCallbackN10032  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10032(){ super(null);}

            public void receiveResultmemberChangeSubjectLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectLiteResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubjectLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testaddMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember100=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
                    // TODO : Fill in the addMember100 here
                
                        assertNotNull(stub.addMember(
                        addMember100));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartaddMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember100=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class);
                    // TODO : Fill in the addMember100 here
                

                stub.startaddMember(
                         addMember100,
                    new tempCallbackN10058()
                );
              


        }

        private class tempCallbackN10058  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10058(){ super(null);}

            public void receiveResultaddMember(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse result
                            ) {
                
            }

            public void receiveErroraddMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite stemDeleteLite102=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite102 here
                
                        assertNotNull(stub.stemDeleteLite(
                        stemDeleteLite102));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite stemDeleteLite102=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLite.class);
                    // TODO : Fill in the stemDeleteLite102 here
                

                stub.startstemDeleteLite(
                         stemDeleteLite102,
                    new tempCallbackN1007E()
                );
              


        }

        private class tempCallbackN1007E  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1007E(){ super(null);}

            public void receiveResultstemDeleteLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStemsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite findStemsLite104=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite104 here
                
                        assertNotNull(stub.findStemsLite(
                        findStemsLite104));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStemsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite findStemsLite104=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLite.class);
                    // TODO : Fill in the findStemsLite104 here
                

                stub.startfindStemsLite(
                         findStemsLite104,
                    new tempCallbackN100A4()
                );
              


        }

        private class tempCallbackN100A4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100A4(){ super(null);}

            public void receiveResultfindStemsLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindStemsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite findGroupsLite106=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite106 here
                
                        assertNotNull(stub.findGroupsLite(
                        findGroupsLite106));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite findGroupsLite106=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLite.class);
                    // TODO : Fill in the findGroupsLite106 here
                

                stub.startfindGroupsLite(
                         findGroupsLite106,
                    new tempCallbackN100CA()
                );
              


        }

        private class tempCallbackN100CA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100CA(){ super(null);}

            public void receiveResultfindGroupsLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorfindGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite stemSaveLite108=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite108 here
                
                        assertNotNull(stub.stemSaveLite(
                        stemSaveLite108));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite stemSaveLite108=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLite.class);
                    // TODO : Fill in the stemSaveLite108 here
                

                stub.startstemSaveLite(
                         stemSaveLite108,
                    new tempCallbackN100F0()
                );
              


        }

        private class tempCallbackN100F0  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN100F0(){ super(null);}

            public void receiveResultstemSaveLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorstemSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete stemDelete110=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete.class);
                    // TODO : Fill in the stemDelete110 here
                
                        assertNotNull(stub.stemDelete(
                        stemDelete110));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete stemDelete110=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDelete.class);
                    // TODO : Fill in the stemDelete110 here
                

                stub.startstemDelete(
                         stemDelete110,
                    new tempCallbackN10116()
                );
              


        }

        private class tempCallbackN10116  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10116(){ super(null);}

            public void receiveResultstemDelete(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemDeleteResponse result
                            ) {
                
            }

            public void receiveErrorstemDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDeleteLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite groupDeleteLite112=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite112 here
                
                        assertNotNull(stub.groupDeleteLite(
                        groupDeleteLite112));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDeleteLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite groupDeleteLite112=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLite.class);
                    // TODO : Fill in the groupDeleteLite112 here
                

                stub.startgroupDeleteLite(
                         groupDeleteLite112,
                    new tempCallbackN1013C()
                );
              


        }

        private class tempCallbackN1013C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1013C(){ super(null);}

            public void receiveResultgroupDeleteLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDeleteLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMemberLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite hasMemberLite114=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite114 here
                
                        assertNotNull(stub.hasMemberLite(
                        hasMemberLite114));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite hasMemberLite114=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLite.class);
                    // TODO : Fill in the hasMemberLite114 here
                

                stub.starthasMemberLite(
                         hasMemberLite114,
                    new tempCallbackN10162()
                );
              


        }

        private class tempCallbackN10162  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10162(){ super(null);}

            public void receiveResulthasMemberLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberLiteResponse result
                            ) {
                
            }

            public void receiveErrorhasMemberLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetGroupsLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite getGroupsLite116=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite116 here
                
                        assertNotNull(stub.getGroupsLite(
                        getGroupsLite116));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroupsLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite getGroupsLite116=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLite.class);
                    // TODO : Fill in the getGroupsLite116 here
                

                stub.startgetGroupsLite(
                         getGroupsLite116,
                    new tempCallbackN10188()
                );
              


        }

        private class tempCallbackN10188  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10188(){ super(null);}

            public void receiveResultgetGroupsLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetGroupsLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupDelete() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete groupDelete118=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete.class);
                    // TODO : Fill in the groupDelete118 here
                
                        assertNotNull(stub.groupDelete(
                        groupDelete118));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupDelete() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete groupDelete118=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDelete.class);
                    // TODO : Fill in the groupDelete118 here
                

                stub.startgroupDelete(
                         groupDelete118,
                    new tempCallbackN101AE()
                );
              


        }

        private class tempCallbackN101AE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101AE(){ super(null);}

            public void receiveResultgroupDelete(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupDeleteResponse result
                            ) {
                
            }

            public void receiveErrorgroupDelete(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testdeleteMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember deleteMember120=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember.class);
                    // TODO : Fill in the deleteMember120 here
                
                        assertNotNull(stub.deleteMember(
                        deleteMember120));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember deleteMember120=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMember.class);
                    // TODO : Fill in the deleteMember120 here
                

                stub.startdeleteMember(
                         deleteMember120,
                    new tempCallbackN101D4()
                );
              


        }

        private class tempCallbackN101D4  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101D4(){ super(null);}

            public void receiveResultdeleteMember(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberResponse result
                            ) {
                
            }

            public void receiveErrordeleteMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testhasMember() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember hasMember122=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember.class);
                    // TODO : Fill in the hasMember122 here
                
                        assertNotNull(stub.hasMember(
                        hasMember122));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStarthasMember() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember hasMember122=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMember.class);
                    // TODO : Fill in the hasMember122 here
                

                stub.starthasMember(
                         hasMember122,
                    new tempCallbackN101FA()
                );
              


        }

        private class tempCallbackN101FA  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN101FA(){ super(null);}

            public void receiveResulthasMember(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.HasMemberResponse result
                            ) {
                
            }

            public void receiveErrorhasMember(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSaveLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite groupSaveLite124=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite124 here
                
                        assertNotNull(stub.groupSaveLite(
                        groupSaveLite124));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSaveLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite groupSaveLite124=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLite.class);
                    // TODO : Fill in the groupSaveLite124 here
                

                stub.startgroupSaveLite(
                         groupSaveLite124,
                    new tempCallbackN10220()
                );
              


        }

        private class tempCallbackN10220  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10220(){ super(null);}

            public void receiveResultgroupSaveLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveLiteResponse result
                            ) {
                
            }

            public void receiveErrorgroupSaveLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void teststemSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave stemSave126=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave.class);
                    // TODO : Fill in the stemSave126 here
                
                        assertNotNull(stub.stemSave(
                        stemSave126));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartstemSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave stemSave126=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSave.class);
                    // TODO : Fill in the stemSave126 here
                

                stub.startstemSave(
                         stemSave126,
                    new tempCallbackN10246()
                );
              


        }

        private class tempCallbackN10246  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10246(){ super(null);}

            public void receiveResultstemSave(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.StemSaveResponse result
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

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups getGroups128=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups.class);
                    // TODO : Fill in the getGroups128 here
                
                        assertNotNull(stub.getGroups(
                        getGroups128));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups getGroups128=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroups.class);
                    // TODO : Fill in the getGroups128 here
                

                stub.startgetGroups(
                         getGroups128,
                    new tempCallbackN1026C()
                );
              


        }

        private class tempCallbackN1026C  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1026C(){ super(null);}

            public void receiveResultgetGroups(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetGroupsResponse result
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

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite deleteMemberLite130=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite130 here
                
                        assertNotNull(stub.deleteMemberLite(
                        deleteMemberLite130));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartdeleteMemberLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite deleteMemberLite130=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLite.class);
                    // TODO : Fill in the deleteMemberLite130 here
                

                stub.startdeleteMemberLite(
                         deleteMemberLite130,
                    new tempCallbackN10292()
                );
              


        }

        private class tempCallbackN10292  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10292(){ super(null);}

            public void receiveResultdeleteMemberLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.DeleteMemberLiteResponse result
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

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubject memberChangeSubject132=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject132 here
                
                        assertNotNull(stub.memberChangeSubject(
                        memberChangeSubject132));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartmemberChangeSubject() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubject memberChangeSubject132=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubject)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubject.class);
                    // TODO : Fill in the memberChangeSubject132 here
                

                stub.startmemberChangeSubject(
                         memberChangeSubject132,
                    new tempCallbackN102B8()
                );
              


        }

        private class tempCallbackN102B8  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102B8(){ super(null);}

            public void receiveResultmemberChangeSubject(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.MemberChangeSubjectResponse result
                            ) {
                
            }

            public void receiveErrormemberChangeSubject(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembers() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers getMembers134=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers.class);
                    // TODO : Fill in the getMembers134 here
                
                        assertNotNull(stub.getMembers(
                        getMembers134));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembers() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers getMembers134=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembers.class);
                    // TODO : Fill in the getMembers134 here
                

                stub.startgetMembers(
                         getMembers134,
                    new tempCallbackN102DE()
                );
              


        }

        private class tempCallbackN102DE  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN102DE(){ super(null);}

            public void receiveResultgetMembers(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersResponse result
                            ) {
                
            }

            public void receiveErrorgetMembers(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindGroups() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups136=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class);
                    // TODO : Fill in the findGroups136 here
                
                        assertNotNull(stub.findGroups(
                        findGroups136));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindGroups() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups136=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class);
                    // TODO : Fill in the findGroups136 here
                

                stub.startfindGroups(
                         findGroups136,
                    new tempCallbackN10304()
                );
              


        }

        private class tempCallbackN10304  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10304(){ super(null);}

            public void receiveResultfindGroups(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse result
                            ) {
                
            }

            public void receiveErrorfindGroups(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgroupSave() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave groupSave138=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave.class);
                    // TODO : Fill in the groupSave138 here
                
                        assertNotNull(stub.groupSave(
                        groupSave138));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgroupSave() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave groupSave138=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSave.class);
                    // TODO : Fill in the groupSave138 here
                

                stub.startgroupSave(
                         groupSave138,
                    new tempCallbackN1032A()
                );
              


        }

        private class tempCallbackN1032A  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN1032A(){ super(null);}

            public void receiveResultgroupSave(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GroupSaveResponse result
                            ) {
                
            }

            public void receiveErrorgroupSave(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testgetMembersLite() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite getMembersLite140=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite140 here
                
                        assertNotNull(stub.getMembersLite(
                        getMembersLite140));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartgetMembersLite() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite getMembersLite140=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLite.class);
                    // TODO : Fill in the getMembersLite140 here
                

                stub.startgetMembersLite(
                         getMembersLite140,
                    new tempCallbackN10350()
                );
              


        }

        private class tempCallbackN10350  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10350(){ super(null);}

            public void receiveResultgetMembersLite(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.GetMembersLiteResponse result
                            ) {
                
            }

            public void receiveErrorgetMembersLite(java.lang.Exception e) {
                fail();
            }

        }
      
        /**
         * Auto generated test method
         */
        public  void testfindStems() throws java.lang.Exception{

        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub =
                    new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();//the default implementation should point to the right endpoint

           edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems findStems142=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems.class);
                    // TODO : Fill in the findStems142 here
                
                        assertNotNull(stub.findStems(
                        findStems142));
                  



        }
        
         /**
         * Auto generated test method
         */
        public  void testStartfindStems() throws java.lang.Exception{
            edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub stub = new edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub();
             edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems findStems142=
                                                        (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems)getTestObject(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStems.class);
                    // TODO : Fill in the findStems142 here
                

                stub.startfindStems(
                         findStems142,
                    new tempCallbackN10376()
                );
              


        }

        private class tempCallbackN10376  extends edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler{
            public tempCallbackN10376(){ super(null);}

            public void receiveResultfindStems(
                         edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindStemsResponse result
                            ) {
                
            }

            public void receiveErrorfindStems(java.lang.Exception e) {
                fail();
            }

        }
      
        //Create an ADBBean and provide it as the test object
        public org.apache.axis2.databinding.ADBBean getTestObject(java.lang.Class type) throws java.lang.Exception{
           return (org.apache.axis2.databinding.ADBBean) type.newInstance();
        }

        
        

    }
    