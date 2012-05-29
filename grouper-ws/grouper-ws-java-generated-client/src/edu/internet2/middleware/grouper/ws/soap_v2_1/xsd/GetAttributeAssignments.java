/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * GetAttributeAssignments.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_1.xsd;
            

            /**
            *  GetAttributeAssignments bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class GetAttributeAssignments
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                "getAttributeAssignments",
                "ns1");

            

                        /**
                        * field for ClientVersion
                        */

                        
                                    protected java.lang.String localClientVersion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localClientVersionTracker = false ;

                           public boolean isClientVersionSpecified(){
                               return localClientVersionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getClientVersion(){
                               return localClientVersion;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ClientVersion
                               */
                               public void setClientVersion(java.lang.String param){
                            localClientVersionTracker = true;
                                   
                                            this.localClientVersion=param;
                                    

                               }
                            

                        /**
                        * field for AttributeAssignType
                        */

                        
                                    protected java.lang.String localAttributeAssignType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeAssignTypeTracker = false ;

                           public boolean isAttributeAssignTypeSpecified(){
                               return localAttributeAssignTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeAssignType(){
                               return localAttributeAssignType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeAssignType
                               */
                               public void setAttributeAssignType(java.lang.String param){
                            localAttributeAssignTypeTracker = true;
                                   
                                            this.localAttributeAssignType=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeAssignLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[] localWsAttributeAssignLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeAssignLookupsTracker = false ;

                           public boolean isWsAttributeAssignLookupsSpecified(){
                               return localWsAttributeAssignLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[] getWsAttributeAssignLookups(){
                               return localWsAttributeAssignLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAttributeAssignLookups
                               */
                              protected void validateWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAttributeAssignLookups
                              */
                              public void setWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[] param){
                              
                                   validateWsAttributeAssignLookups(param);

                               localWsAttributeAssignLookupsTracker = true;
                                      
                                      this.localWsAttributeAssignLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup
                             */
                             public void addWsAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup param){
                                   if (localWsAttributeAssignLookups == null){
                                   localWsAttributeAssignLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAttributeAssignLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAttributeAssignLookups);
                               list.add(param);
                               this.localWsAttributeAssignLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsAttributeDefLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] localWsAttributeDefLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefLookupsTracker = false ;

                           public boolean isWsAttributeDefLookupsSpecified(){
                               return localWsAttributeDefLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] getWsAttributeDefLookups(){
                               return localWsAttributeDefLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAttributeDefLookups
                               */
                              protected void validateWsAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAttributeDefLookups
                              */
                              public void setWsAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] param){
                              
                                   validateWsAttributeDefLookups(param);

                               localWsAttributeDefLookupsTracker = true;
                                      
                                      this.localWsAttributeDefLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup
                             */
                             public void addWsAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup param){
                                   if (localWsAttributeDefLookups == null){
                                   localWsAttributeDefLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAttributeDefLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAttributeDefLookups);
                               list.add(param);
                               this.localWsAttributeDefLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsAttributeDefNameLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[] localWsAttributeDefNameLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefNameLookupsTracker = false ;

                           public boolean isWsAttributeDefNameLookupsSpecified(){
                               return localWsAttributeDefNameLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[] getWsAttributeDefNameLookups(){
                               return localWsAttributeDefNameLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAttributeDefNameLookups
                               */
                              protected void validateWsAttributeDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAttributeDefNameLookups
                              */
                              public void setWsAttributeDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[] param){
                              
                                   validateWsAttributeDefNameLookups(param);

                               localWsAttributeDefNameLookupsTracker = true;
                                      
                                      this.localWsAttributeDefNameLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup
                             */
                             public void addWsAttributeDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup param){
                                   if (localWsAttributeDefNameLookups == null){
                                   localWsAttributeDefNameLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAttributeDefNameLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAttributeDefNameLookups);
                               list.add(param);
                               this.localWsAttributeDefNameLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsOwnerGroupLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[] localWsOwnerGroupLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsOwnerGroupLookupsTracker = false ;

                           public boolean isWsOwnerGroupLookupsSpecified(){
                               return localWsOwnerGroupLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[] getWsOwnerGroupLookups(){
                               return localWsOwnerGroupLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsOwnerGroupLookups
                               */
                              protected void validateWsOwnerGroupLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsOwnerGroupLookups
                              */
                              public void setWsOwnerGroupLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[] param){
                              
                                   validateWsOwnerGroupLookups(param);

                               localWsOwnerGroupLookupsTracker = true;
                                      
                                      this.localWsOwnerGroupLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup
                             */
                             public void addWsOwnerGroupLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup param){
                                   if (localWsOwnerGroupLookups == null){
                                   localWsOwnerGroupLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsOwnerGroupLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsOwnerGroupLookups);
                               list.add(param);
                               this.localWsOwnerGroupLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsOwnerStemLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[] localWsOwnerStemLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsOwnerStemLookupsTracker = false ;

                           public boolean isWsOwnerStemLookupsSpecified(){
                               return localWsOwnerStemLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[] getWsOwnerStemLookups(){
                               return localWsOwnerStemLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsOwnerStemLookups
                               */
                              protected void validateWsOwnerStemLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsOwnerStemLookups
                              */
                              public void setWsOwnerStemLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[] param){
                              
                                   validateWsOwnerStemLookups(param);

                               localWsOwnerStemLookupsTracker = true;
                                      
                                      this.localWsOwnerStemLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup
                             */
                             public void addWsOwnerStemLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup param){
                                   if (localWsOwnerStemLookups == null){
                                   localWsOwnerStemLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsOwnerStemLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsOwnerStemLookups);
                               list.add(param);
                               this.localWsOwnerStemLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsOwnerSubjectLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] localWsOwnerSubjectLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsOwnerSubjectLookupsTracker = false ;

                           public boolean isWsOwnerSubjectLookupsSpecified(){
                               return localWsOwnerSubjectLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] getWsOwnerSubjectLookups(){
                               return localWsOwnerSubjectLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsOwnerSubjectLookups
                               */
                              protected void validateWsOwnerSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsOwnerSubjectLookups
                              */
                              public void setWsOwnerSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] param){
                              
                                   validateWsOwnerSubjectLookups(param);

                               localWsOwnerSubjectLookupsTracker = true;
                                      
                                      this.localWsOwnerSubjectLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup
                             */
                             public void addWsOwnerSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup param){
                                   if (localWsOwnerSubjectLookups == null){
                                   localWsOwnerSubjectLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsOwnerSubjectLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsOwnerSubjectLookups);
                               list.add(param);
                               this.localWsOwnerSubjectLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsOwnerMembershipLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[] localWsOwnerMembershipLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsOwnerMembershipLookupsTracker = false ;

                           public boolean isWsOwnerMembershipLookupsSpecified(){
                               return localWsOwnerMembershipLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[] getWsOwnerMembershipLookups(){
                               return localWsOwnerMembershipLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsOwnerMembershipLookups
                               */
                              protected void validateWsOwnerMembershipLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsOwnerMembershipLookups
                              */
                              public void setWsOwnerMembershipLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[] param){
                              
                                   validateWsOwnerMembershipLookups(param);

                               localWsOwnerMembershipLookupsTracker = true;
                                      
                                      this.localWsOwnerMembershipLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup
                             */
                             public void addWsOwnerMembershipLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup param){
                                   if (localWsOwnerMembershipLookups == null){
                                   localWsOwnerMembershipLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsOwnerMembershipLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsOwnerMembershipLookups);
                               list.add(param);
                               this.localWsOwnerMembershipLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsOwnerMembershipAnyLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[] localWsOwnerMembershipAnyLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsOwnerMembershipAnyLookupsTracker = false ;

                           public boolean isWsOwnerMembershipAnyLookupsSpecified(){
                               return localWsOwnerMembershipAnyLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[] getWsOwnerMembershipAnyLookups(){
                               return localWsOwnerMembershipAnyLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsOwnerMembershipAnyLookups
                               */
                              protected void validateWsOwnerMembershipAnyLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsOwnerMembershipAnyLookups
                              */
                              public void setWsOwnerMembershipAnyLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[] param){
                              
                                   validateWsOwnerMembershipAnyLookups(param);

                               localWsOwnerMembershipAnyLookupsTracker = true;
                                      
                                      this.localWsOwnerMembershipAnyLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup
                             */
                             public void addWsOwnerMembershipAnyLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup param){
                                   if (localWsOwnerMembershipAnyLookups == null){
                                   localWsOwnerMembershipAnyLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsOwnerMembershipAnyLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsOwnerMembershipAnyLookups);
                               list.add(param);
                               this.localWsOwnerMembershipAnyLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsOwnerAttributeDefLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] localWsOwnerAttributeDefLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsOwnerAttributeDefLookupsTracker = false ;

                           public boolean isWsOwnerAttributeDefLookupsSpecified(){
                               return localWsOwnerAttributeDefLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] getWsOwnerAttributeDefLookups(){
                               return localWsOwnerAttributeDefLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsOwnerAttributeDefLookups
                               */
                              protected void validateWsOwnerAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsOwnerAttributeDefLookups
                              */
                              public void setWsOwnerAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] param){
                              
                                   validateWsOwnerAttributeDefLookups(param);

                               localWsOwnerAttributeDefLookupsTracker = true;
                                      
                                      this.localWsOwnerAttributeDefLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup
                             */
                             public void addWsOwnerAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup param){
                                   if (localWsOwnerAttributeDefLookups == null){
                                   localWsOwnerAttributeDefLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsOwnerAttributeDefLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsOwnerAttributeDefLookups);
                               list.add(param);
                               this.localWsOwnerAttributeDefLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[list.size()]);

                             }
                             

                        /**
                        * field for Actions
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localActions ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActionsTracker = false ;

                           public boolean isActionsSpecified(){
                               return localActionsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getActions(){
                               return localActions;
                           }

                           
                        


                               
                              /**
                               * validate the array for Actions
                               */
                              protected void validateActions(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Actions
                              */
                              public void setActions(java.lang.String[] param){
                              
                                   validateActions(param);

                               localActionsTracker = true;
                                      
                                      this.localActions=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addActions(java.lang.String param){
                                   if (localActions == null){
                                   localActions = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localActionsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localActions);
                               list.add(param);
                               this.localActions =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for IncludeAssignmentsOnAssignments
                        */

                        
                                    protected java.lang.String localIncludeAssignmentsOnAssignments ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeAssignmentsOnAssignmentsTracker = false ;

                           public boolean isIncludeAssignmentsOnAssignmentsSpecified(){
                               return localIncludeAssignmentsOnAssignmentsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeAssignmentsOnAssignments(){
                               return localIncludeAssignmentsOnAssignments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeAssignmentsOnAssignments
                               */
                               public void setIncludeAssignmentsOnAssignments(java.lang.String param){
                            localIncludeAssignmentsOnAssignmentsTracker = true;
                                   
                                            this.localIncludeAssignmentsOnAssignments=param;
                                    

                               }
                            

                        /**
                        * field for ActAsSubjectLookup
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup localActAsSubjectLookup ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectLookupTracker = false ;

                           public boolean isActAsSubjectLookupSpecified(){
                               return localActAsSubjectLookupTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup getActAsSubjectLookup(){
                               return localActAsSubjectLookup;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectLookup
                               */
                               public void setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup param){
                            localActAsSubjectLookupTracker = true;
                                   
                                            this.localActAsSubjectLookup=param;
                                    

                               }
                            

                        /**
                        * field for IncludeSubjectDetail
                        */

                        
                                    protected java.lang.String localIncludeSubjectDetail ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeSubjectDetailTracker = false ;

                           public boolean isIncludeSubjectDetailSpecified(){
                               return localIncludeSubjectDetailTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeSubjectDetail(){
                               return localIncludeSubjectDetail;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeSubjectDetail
                               */
                               public void setIncludeSubjectDetail(java.lang.String param){
                            localIncludeSubjectDetailTracker = true;
                                   
                                            this.localIncludeSubjectDetail=param;
                                    

                               }
                            

                        /**
                        * field for SubjectAttributeNames
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localSubjectAttributeNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectAttributeNamesTracker = false ;

                           public boolean isSubjectAttributeNamesSpecified(){
                               return localSubjectAttributeNamesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getSubjectAttributeNames(){
                               return localSubjectAttributeNames;
                           }

                           
                        


                               
                              /**
                               * validate the array for SubjectAttributeNames
                               */
                              protected void validateSubjectAttributeNames(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param SubjectAttributeNames
                              */
                              public void setSubjectAttributeNames(java.lang.String[] param){
                              
                                   validateSubjectAttributeNames(param);

                               localSubjectAttributeNamesTracker = true;
                                      
                                      this.localSubjectAttributeNames=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addSubjectAttributeNames(java.lang.String param){
                                   if (localSubjectAttributeNames == null){
                                   localSubjectAttributeNames = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localSubjectAttributeNamesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localSubjectAttributeNames);
                               list.add(param);
                               this.localSubjectAttributeNames =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

                        /**
                        * field for IncludeGroupDetail
                        */

                        
                                    protected java.lang.String localIncludeGroupDetail ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeGroupDetailTracker = false ;

                           public boolean isIncludeGroupDetailSpecified(){
                               return localIncludeGroupDetailTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeGroupDetail(){
                               return localIncludeGroupDetail;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeGroupDetail
                               */
                               public void setIncludeGroupDetail(java.lang.String param){
                            localIncludeGroupDetailTracker = true;
                                   
                                            this.localIncludeGroupDetail=param;
                                    

                               }
                            

                        /**
                        * field for Params
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[] localParams ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamsTracker = false ;

                           public boolean isParamsSpecified(){
                               return localParamsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[] getParams(){
                               return localParams;
                           }

                           
                        


                               
                              /**
                               * validate the array for Params
                               */
                              protected void validateParams(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Params
                              */
                              public void setParams(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[] param){
                              
                                   validateParams(param);

                               localParamsTracker = true;
                                      
                                      this.localParams=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam
                             */
                             public void addParams(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam param){
                                   if (localParams == null){
                                   localParams = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[]{};
                                   }

                            
                                 //update the setting tracker
                                localParamsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localParams);
                               list.add(param);
                               this.localParams =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[list.size()]);

                             }
                             

                        /**
                        * field for Enabled
                        */

                        
                                    protected java.lang.String localEnabled ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localEnabledTracker = false ;

                           public boolean isEnabledSpecified(){
                               return localEnabledTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getEnabled(){
                               return localEnabled;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Enabled
                               */
                               public void setEnabled(java.lang.String param){
                            localEnabledTracker = true;
                                   
                                            this.localEnabled=param;
                                    

                               }
                            

                        /**
                        * field for AttributeDefValueType
                        */

                        
                                    protected java.lang.String localAttributeDefValueType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefValueTypeTracker = false ;

                           public boolean isAttributeDefValueTypeSpecified(){
                               return localAttributeDefValueTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeDefValueType(){
                               return localAttributeDefValueType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeDefValueType
                               */
                               public void setAttributeDefValueType(java.lang.String param){
                            localAttributeDefValueTypeTracker = true;
                                   
                                            this.localAttributeDefValueType=param;
                                    

                               }
                            

                        /**
                        * field for TheValue
                        */

                        
                                    protected java.lang.String localTheValue ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTheValueTracker = false ;

                           public boolean isTheValueSpecified(){
                               return localTheValueTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getTheValue(){
                               return localTheValue;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param TheValue
                               */
                               public void setTheValue(java.lang.String param){
                            localTheValueTracker = true;
                                   
                                            this.localTheValue=param;
                                    

                               }
                            

                        /**
                        * field for IncludeAssignmentsFromAssignments
                        */

                        
                                    protected java.lang.String localIncludeAssignmentsFromAssignments ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeAssignmentsFromAssignmentsTracker = false ;

                           public boolean isIncludeAssignmentsFromAssignmentsSpecified(){
                               return localIncludeAssignmentsFromAssignmentsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeAssignmentsFromAssignments(){
                               return localIncludeAssignmentsFromAssignments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeAssignmentsFromAssignments
                               */
                               public void setIncludeAssignmentsFromAssignments(java.lang.String param){
                            localIncludeAssignmentsFromAssignmentsTracker = true;
                                   
                                            this.localIncludeAssignmentsFromAssignments=param;
                                    

                               }
                            

                        /**
                        * field for AttributeDefType
                        */

                        
                                    protected java.lang.String localAttributeDefType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefTypeTracker = false ;

                           public boolean isAttributeDefTypeSpecified(){
                               return localAttributeDefTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeDefType(){
                               return localAttributeDefType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeDefType
                               */
                               public void setAttributeDefType(java.lang.String param){
                            localAttributeDefTypeTracker = true;
                                   
                                            this.localAttributeDefType=param;
                                    

                               }
                            

                        /**
                        * field for WsAssignAssignOwnerAttributeAssignLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[] localWsAssignAssignOwnerAttributeAssignLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAssignAssignOwnerAttributeAssignLookupsTracker = false ;

                           public boolean isWsAssignAssignOwnerAttributeAssignLookupsSpecified(){
                               return localWsAssignAssignOwnerAttributeAssignLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[] getWsAssignAssignOwnerAttributeAssignLookups(){
                               return localWsAssignAssignOwnerAttributeAssignLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAssignAssignOwnerAttributeAssignLookups
                               */
                              protected void validateWsAssignAssignOwnerAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAssignAssignOwnerAttributeAssignLookups
                              */
                              public void setWsAssignAssignOwnerAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[] param){
                              
                                   validateWsAssignAssignOwnerAttributeAssignLookups(param);

                               localWsAssignAssignOwnerAttributeAssignLookupsTracker = true;
                                      
                                      this.localWsAssignAssignOwnerAttributeAssignLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup
                             */
                             public void addWsAssignAssignOwnerAttributeAssignLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup param){
                                   if (localWsAssignAssignOwnerAttributeAssignLookups == null){
                                   localWsAssignAssignOwnerAttributeAssignLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAssignAssignOwnerAttributeAssignLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAssignAssignOwnerAttributeAssignLookups);
                               list.add(param);
                               this.localWsAssignAssignOwnerAttributeAssignLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsAssignAssignOwnerAttributeDefLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] localWsAssignAssignOwnerAttributeDefLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAssignAssignOwnerAttributeDefLookupsTracker = false ;

                           public boolean isWsAssignAssignOwnerAttributeDefLookupsSpecified(){
                               return localWsAssignAssignOwnerAttributeDefLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] getWsAssignAssignOwnerAttributeDefLookups(){
                               return localWsAssignAssignOwnerAttributeDefLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAssignAssignOwnerAttributeDefLookups
                               */
                              protected void validateWsAssignAssignOwnerAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAssignAssignOwnerAttributeDefLookups
                              */
                              public void setWsAssignAssignOwnerAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[] param){
                              
                                   validateWsAssignAssignOwnerAttributeDefLookups(param);

                               localWsAssignAssignOwnerAttributeDefLookupsTracker = true;
                                      
                                      this.localWsAssignAssignOwnerAttributeDefLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup
                             */
                             public void addWsAssignAssignOwnerAttributeDefLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup param){
                                   if (localWsAssignAssignOwnerAttributeDefLookups == null){
                                   localWsAssignAssignOwnerAttributeDefLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAssignAssignOwnerAttributeDefLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAssignAssignOwnerAttributeDefLookups);
                               list.add(param);
                               this.localWsAssignAssignOwnerAttributeDefLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsAssignAssignOwnerAttributeDefNameLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[] localWsAssignAssignOwnerAttributeDefNameLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAssignAssignOwnerAttributeDefNameLookupsTracker = false ;

                           public boolean isWsAssignAssignOwnerAttributeDefNameLookupsSpecified(){
                               return localWsAssignAssignOwnerAttributeDefNameLookupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[] getWsAssignAssignOwnerAttributeDefNameLookups(){
                               return localWsAssignAssignOwnerAttributeDefNameLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAssignAssignOwnerAttributeDefNameLookups
                               */
                              protected void validateWsAssignAssignOwnerAttributeDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAssignAssignOwnerAttributeDefNameLookups
                              */
                              public void setWsAssignAssignOwnerAttributeDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[] param){
                              
                                   validateWsAssignAssignOwnerAttributeDefNameLookups(param);

                               localWsAssignAssignOwnerAttributeDefNameLookupsTracker = true;
                                      
                                      this.localWsAssignAssignOwnerAttributeDefNameLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup
                             */
                             public void addWsAssignAssignOwnerAttributeDefNameLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup param){
                                   if (localWsAssignAssignOwnerAttributeDefNameLookups == null){
                                   localWsAssignAssignOwnerAttributeDefNameLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAssignAssignOwnerAttributeDefNameLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAssignAssignOwnerAttributeDefNameLookups);
                               list.add(param);
                               this.localWsAssignAssignOwnerAttributeDefNameLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsAssignAssignOwnerActions
                        * This was an Array!
                        */

                        
                                    protected java.lang.String[] localWsAssignAssignOwnerActions ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAssignAssignOwnerActionsTracker = false ;

                           public boolean isWsAssignAssignOwnerActionsSpecified(){
                               return localWsAssignAssignOwnerActionsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String[]
                           */
                           public  java.lang.String[] getWsAssignAssignOwnerActions(){
                               return localWsAssignAssignOwnerActions;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAssignAssignOwnerActions
                               */
                              protected void validateWsAssignAssignOwnerActions(java.lang.String[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAssignAssignOwnerActions
                              */
                              public void setWsAssignAssignOwnerActions(java.lang.String[] param){
                              
                                   validateWsAssignAssignOwnerActions(param);

                               localWsAssignAssignOwnerActionsTracker = true;
                                      
                                      this.localWsAssignAssignOwnerActions=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param java.lang.String
                             */
                             public void addWsAssignAssignOwnerActions(java.lang.String param){
                                   if (localWsAssignAssignOwnerActions == null){
                                   localWsAssignAssignOwnerActions = new java.lang.String[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAssignAssignOwnerActionsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAssignAssignOwnerActions);
                               list.add(param);
                               this.localWsAssignAssignOwnerActions =
                             (java.lang.String[])list.toArray(
                            new java.lang.String[list.size()]);

                             }
                             

     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
               return factory.createOMElement(dataSource,MY_QNAME);
            
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":getAttributeAssignments",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "getAttributeAssignments",
                           xmlWriter);
                   }

               
                   }
                if (localClientVersionTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "clientVersion", xmlWriter);
                             

                                          if (localClientVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClientVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeAssignTypeTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeAssignType", xmlWriter);
                             

                                          if (localAttributeAssignType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeAssignType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeAssignLookupsTracker){
                                       if (localWsAttributeAssignLookups!=null){
                                            for (int i = 0;i < localWsAttributeAssignLookups.length;i++){
                                                if (localWsAttributeAssignLookups[i] != null){
                                                 localWsAttributeAssignLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeAssignLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeAssignLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsAttributeDefLookupsTracker){
                                       if (localWsAttributeDefLookups!=null){
                                            for (int i = 0;i < localWsAttributeDefLookups.length;i++){
                                                if (localWsAttributeDefLookups[i] != null){
                                                 localWsAttributeDefLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsAttributeDefNameLookupsTracker){
                                       if (localWsAttributeDefNameLookups!=null){
                                            for (int i = 0;i < localWsAttributeDefNameLookups.length;i++){
                                                if (localWsAttributeDefNameLookups[i] != null){
                                                 localWsAttributeDefNameLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNameLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefNameLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefNameLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsOwnerGroupLookupsTracker){
                                       if (localWsOwnerGroupLookups!=null){
                                            for (int i = 0;i < localWsOwnerGroupLookups.length;i++){
                                                if (localWsOwnerGroupLookups[i] != null){
                                                 localWsOwnerGroupLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerGroupLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerGroupLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerGroupLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsOwnerStemLookupsTracker){
                                       if (localWsOwnerStemLookups!=null){
                                            for (int i = 0;i < localWsOwnerStemLookups.length;i++){
                                                if (localWsOwnerStemLookups[i] != null){
                                                 localWsOwnerStemLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerStemLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerStemLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerStemLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsOwnerSubjectLookupsTracker){
                                       if (localWsOwnerSubjectLookups!=null){
                                            for (int i = 0;i < localWsOwnerSubjectLookups.length;i++){
                                                if (localWsOwnerSubjectLookups[i] != null){
                                                 localWsOwnerSubjectLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerSubjectLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerSubjectLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerSubjectLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsOwnerMembershipLookupsTracker){
                                       if (localWsOwnerMembershipLookups!=null){
                                            for (int i = 0;i < localWsOwnerMembershipLookups.length;i++){
                                                if (localWsOwnerMembershipLookups[i] != null){
                                                 localWsOwnerMembershipLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerMembershipLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerMembershipLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerMembershipLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsOwnerMembershipAnyLookupsTracker){
                                       if (localWsOwnerMembershipAnyLookups!=null){
                                            for (int i = 0;i < localWsOwnerMembershipAnyLookups.length;i++){
                                                if (localWsOwnerMembershipAnyLookups[i] != null){
                                                 localWsOwnerMembershipAnyLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerMembershipAnyLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerMembershipAnyLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerMembershipAnyLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsOwnerAttributeDefLookupsTracker){
                                       if (localWsOwnerAttributeDefLookups!=null){
                                            for (int i = 0;i < localWsOwnerAttributeDefLookups.length;i++){
                                                if (localWsOwnerAttributeDefLookups[i] != null){
                                                 localWsOwnerAttributeDefLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerAttributeDefLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerAttributeDefLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsOwnerAttributeDefLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localActionsTracker){
                             if (localActions!=null) {
                                   namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                   for (int i = 0;i < localActions.length;i++){
                                        
                                            if (localActions[i] != null){
                                        
                                                writeStartElement(null, namespace, "actions", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActions[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            writeStartElement(null, namespace, "actions", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "actions", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localIncludeAssignmentsOnAssignmentsTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "includeAssignmentsOnAssignments", xmlWriter);
                             

                                          if (localIncludeAssignmentsOnAssignments==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeAssignmentsOnAssignments);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectLookupTracker){
                                    if (localActAsSubjectLookup==null){

                                        writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "actAsSubjectLookup", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localActAsSubjectLookup.serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup"),
                                        xmlWriter);
                                    }
                                } if (localIncludeSubjectDetailTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "includeSubjectDetail", xmlWriter);
                             

                                          if (localIncludeSubjectDetail==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeSubjectDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectAttributeNamesTracker){
                             if (localSubjectAttributeNames!=null) {
                                   namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                   for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                        
                                            if (localSubjectAttributeNames[i] != null){
                                        
                                                writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "subjectAttributeNames", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localIncludeGroupDetailTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "includeGroupDetail", xmlWriter);
                             

                                          if (localIncludeGroupDetail==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeGroupDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamsTracker){
                                       if (localParams!=null){
                                            for (int i = 0;i < localParams.length;i++){
                                                if (localParams[i] != null){
                                                 localParams[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","params"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "params", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "params", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localEnabledTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "enabled", xmlWriter);
                             

                                          if (localEnabled==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localEnabled);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefValueTypeTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefValueType", xmlWriter);
                             

                                          if (localAttributeDefValueType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefValueType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localTheValueTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "theValue", xmlWriter);
                             

                                          if (localTheValue==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localTheValue);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIncludeAssignmentsFromAssignmentsTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "includeAssignmentsFromAssignments", xmlWriter);
                             

                                          if (localIncludeAssignmentsFromAssignments==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeAssignmentsFromAssignments);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefTypeTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefType", xmlWriter);
                             

                                          if (localAttributeDefType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAssignAssignOwnerAttributeAssignLookupsTracker){
                                       if (localWsAssignAssignOwnerAttributeAssignLookups!=null){
                                            for (int i = 0;i < localWsAssignAssignOwnerAttributeAssignLookups.length;i++){
                                                if (localWsAssignAssignOwnerAttributeAssignLookups[i] != null){
                                                 localWsAssignAssignOwnerAttributeAssignLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeAssignLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAssignAssignOwnerAttributeAssignLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAssignAssignOwnerAttributeAssignLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsAssignAssignOwnerAttributeDefLookupsTracker){
                                       if (localWsAssignAssignOwnerAttributeDefLookups!=null){
                                            for (int i = 0;i < localWsAssignAssignOwnerAttributeDefLookups.length;i++){
                                                if (localWsAssignAssignOwnerAttributeDefLookups[i] != null){
                                                 localWsAssignAssignOwnerAttributeDefLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeDefLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAssignAssignOwnerAttributeDefLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAssignAssignOwnerAttributeDefLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsAssignAssignOwnerAttributeDefNameLookupsTracker){
                                       if (localWsAssignAssignOwnerAttributeDefNameLookups!=null){
                                            for (int i = 0;i < localWsAssignAssignOwnerAttributeDefNameLookups.length;i++){
                                                if (localWsAssignAssignOwnerAttributeDefNameLookups[i] != null){
                                                 localWsAssignAssignOwnerAttributeDefNameLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeDefNameLookups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAssignAssignOwnerAttributeDefNameLookups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAssignAssignOwnerAttributeDefNameLookups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsAssignAssignOwnerActionsTracker){
                             if (localWsAssignAssignOwnerActions!=null) {
                                   namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                   for (int i = 0;i < localWsAssignAssignOwnerActions.length;i++){
                                        
                                            if (localWsAssignAssignOwnerActions[i] != null){
                                        
                                                writeStartElement(null, namespace, "wsAssignAssignOwnerActions", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsAssignAssignOwnerActions[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            writeStartElement(null, namespace, "wsAssignAssignOwnerActions", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd", "wsAssignAssignOwnerActions", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        
        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace,attName,attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace,attName,attValue);
            }
        }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                while (true) {
                    java.lang.String uri = nsContext.getNamespaceURI(prefix);
                    if (uri == null || uri.length() == 0) {
                        break;
                    }
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localClientVersionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "clientVersion"));
                                 
                                         elementList.add(localClientVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientVersion));
                                    } if (localAttributeAssignTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeAssignType"));
                                 
                                         elementList.add(localAttributeAssignType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeAssignType));
                                    } if (localWsAttributeAssignLookupsTracker){
                             if (localWsAttributeAssignLookups!=null) {
                                 for (int i = 0;i < localWsAttributeAssignLookups.length;i++){

                                    if (localWsAttributeAssignLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                         elementList.add(localWsAttributeAssignLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeAssignLookups"));
                                        elementList.add(localWsAttributeAssignLookups);
                                    
                             }

                        } if (localWsAttributeDefLookupsTracker){
                             if (localWsAttributeDefLookups!=null) {
                                 for (int i = 0;i < localWsAttributeDefLookups.length;i++){

                                    if (localWsAttributeDefLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefLookups"));
                                         elementList.add(localWsAttributeDefLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefLookups"));
                                        elementList.add(localWsAttributeDefLookups);
                                    
                             }

                        } if (localWsAttributeDefNameLookupsTracker){
                             if (localWsAttributeDefNameLookups!=null) {
                                 for (int i = 0;i < localWsAttributeDefNameLookups.length;i++){

                                    if (localWsAttributeDefNameLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefNameLookups"));
                                         elementList.add(localWsAttributeDefNameLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefNameLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefNameLookups"));
                                        elementList.add(localWsAttributeDefNameLookups);
                                    
                             }

                        } if (localWsOwnerGroupLookupsTracker){
                             if (localWsOwnerGroupLookups!=null) {
                                 for (int i = 0;i < localWsOwnerGroupLookups.length;i++){

                                    if (localWsOwnerGroupLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerGroupLookups"));
                                         elementList.add(localWsOwnerGroupLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerGroupLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerGroupLookups"));
                                        elementList.add(localWsOwnerGroupLookups);
                                    
                             }

                        } if (localWsOwnerStemLookupsTracker){
                             if (localWsOwnerStemLookups!=null) {
                                 for (int i = 0;i < localWsOwnerStemLookups.length;i++){

                                    if (localWsOwnerStemLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerStemLookups"));
                                         elementList.add(localWsOwnerStemLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerStemLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerStemLookups"));
                                        elementList.add(localWsOwnerStemLookups);
                                    
                             }

                        } if (localWsOwnerSubjectLookupsTracker){
                             if (localWsOwnerSubjectLookups!=null) {
                                 for (int i = 0;i < localWsOwnerSubjectLookups.length;i++){

                                    if (localWsOwnerSubjectLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerSubjectLookups"));
                                         elementList.add(localWsOwnerSubjectLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerSubjectLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerSubjectLookups"));
                                        elementList.add(localWsOwnerSubjectLookups);
                                    
                             }

                        } if (localWsOwnerMembershipLookupsTracker){
                             if (localWsOwnerMembershipLookups!=null) {
                                 for (int i = 0;i < localWsOwnerMembershipLookups.length;i++){

                                    if (localWsOwnerMembershipLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerMembershipLookups"));
                                         elementList.add(localWsOwnerMembershipLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerMembershipLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerMembershipLookups"));
                                        elementList.add(localWsOwnerMembershipLookups);
                                    
                             }

                        } if (localWsOwnerMembershipAnyLookupsTracker){
                             if (localWsOwnerMembershipAnyLookups!=null) {
                                 for (int i = 0;i < localWsOwnerMembershipAnyLookups.length;i++){

                                    if (localWsOwnerMembershipAnyLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerMembershipAnyLookups"));
                                         elementList.add(localWsOwnerMembershipAnyLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerMembershipAnyLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerMembershipAnyLookups"));
                                        elementList.add(localWsOwnerMembershipAnyLookups);
                                    
                             }

                        } if (localWsOwnerAttributeDefLookupsTracker){
                             if (localWsOwnerAttributeDefLookups!=null) {
                                 for (int i = 0;i < localWsOwnerAttributeDefLookups.length;i++){

                                    if (localWsOwnerAttributeDefLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerAttributeDefLookups"));
                                         elementList.add(localWsOwnerAttributeDefLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerAttributeDefLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsOwnerAttributeDefLookups"));
                                        elementList.add(localWsOwnerAttributeDefLookups);
                                    
                             }

                        } if (localActionsTracker){
                            if (localActions!=null){
                                  for (int i = 0;i < localActions.length;i++){
                                      
                                         if (localActions[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActions[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "actions"));
                                    elementList.add(null);
                                
                            }

                        } if (localIncludeAssignmentsOnAssignmentsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeAssignmentsOnAssignments"));
                                 
                                         elementList.add(localIncludeAssignmentsOnAssignments==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeAssignmentsOnAssignments));
                                    } if (localActAsSubjectLookupTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectLookup"));
                            
                            
                                    elementList.add(localActAsSubjectLookup==null?null:
                                    localActAsSubjectLookup);
                                } if (localIncludeSubjectDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeSubjectDetail"));
                                 
                                         elementList.add(localIncludeSubjectDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeSubjectDetail));
                                    } if (localSubjectAttributeNamesTracker){
                            if (localSubjectAttributeNames!=null){
                                  for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                      
                                         if (localSubjectAttributeNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                    elementList.add(null);
                                
                            }

                        } if (localIncludeGroupDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeGroupDetail"));
                                 
                                         elementList.add(localIncludeGroupDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeGroupDetail));
                                    } if (localParamsTracker){
                             if (localParams!=null) {
                                 for (int i = 0;i < localParams.length;i++){

                                    if (localParams[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                         elementList.add(localParams[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "params"));
                                        elementList.add(localParams);
                                    
                             }

                        } if (localEnabledTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "enabled"));
                                 
                                         elementList.add(localEnabled==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnabled));
                                    } if (localAttributeDefValueTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefValueType"));
                                 
                                         elementList.add(localAttributeDefValueType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefValueType));
                                    } if (localTheValueTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "theValue"));
                                 
                                         elementList.add(localTheValue==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTheValue));
                                    } if (localIncludeAssignmentsFromAssignmentsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeAssignmentsFromAssignments"));
                                 
                                         elementList.add(localIncludeAssignmentsFromAssignments==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeAssignmentsFromAssignments));
                                    } if (localAttributeDefTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefType"));
                                 
                                         elementList.add(localAttributeDefType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefType));
                                    } if (localWsAssignAssignOwnerAttributeAssignLookupsTracker){
                             if (localWsAssignAssignOwnerAttributeAssignLookups!=null) {
                                 for (int i = 0;i < localWsAssignAssignOwnerAttributeAssignLookups.length;i++){

                                    if (localWsAssignAssignOwnerAttributeAssignLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeAssignLookups"));
                                         elementList.add(localWsAssignAssignOwnerAttributeAssignLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeAssignLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeAssignLookups"));
                                        elementList.add(localWsAssignAssignOwnerAttributeAssignLookups);
                                    
                             }

                        } if (localWsAssignAssignOwnerAttributeDefLookupsTracker){
                             if (localWsAssignAssignOwnerAttributeDefLookups!=null) {
                                 for (int i = 0;i < localWsAssignAssignOwnerAttributeDefLookups.length;i++){

                                    if (localWsAssignAssignOwnerAttributeDefLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeDefLookups"));
                                         elementList.add(localWsAssignAssignOwnerAttributeDefLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeDefLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeDefLookups"));
                                        elementList.add(localWsAssignAssignOwnerAttributeDefLookups);
                                    
                             }

                        } if (localWsAssignAssignOwnerAttributeDefNameLookupsTracker){
                             if (localWsAssignAssignOwnerAttributeDefNameLookups!=null) {
                                 for (int i = 0;i < localWsAssignAssignOwnerAttributeDefNameLookups.length;i++){

                                    if (localWsAssignAssignOwnerAttributeDefNameLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeDefNameLookups"));
                                         elementList.add(localWsAssignAssignOwnerAttributeDefNameLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeDefNameLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignAssignOwnerAttributeDefNameLookups"));
                                        elementList.add(localWsAssignAssignOwnerAttributeDefNameLookups);
                                    
                             }

                        } if (localWsAssignAssignOwnerActionsTracker){
                            if (localWsAssignAssignOwnerActions!=null){
                                  for (int i = 0;i < localWsAssignAssignOwnerActions.length;i++){
                                      
                                         if (localWsAssignAssignOwnerActions[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "wsAssignAssignOwnerActions"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsAssignAssignOwnerActions[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "wsAssignAssignOwnerActions"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "wsAssignAssignOwnerActions"));
                                    elementList.add(null);
                                
                            }

                        }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static GetAttributeAssignments parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetAttributeAssignments object =
                new GetAttributeAssignments();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"getAttributeAssignments".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (GetAttributeAssignments)edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                        java.util.ArrayList list5 = new java.util.ArrayList();
                    
                        java.util.ArrayList list6 = new java.util.ArrayList();
                    
                        java.util.ArrayList list7 = new java.util.ArrayList();
                    
                        java.util.ArrayList list8 = new java.util.ArrayList();
                    
                        java.util.ArrayList list9 = new java.util.ArrayList();
                    
                        java.util.ArrayList list10 = new java.util.ArrayList();
                    
                        java.util.ArrayList list11 = new java.util.ArrayList();
                    
                        java.util.ArrayList list12 = new java.util.ArrayList();
                    
                        java.util.ArrayList list16 = new java.util.ArrayList();
                    
                        java.util.ArrayList list18 = new java.util.ArrayList();
                    
                        java.util.ArrayList list24 = new java.util.ArrayList();
                    
                        java.util.ArrayList list25 = new java.util.ArrayList();
                    
                        java.util.ArrayList list26 = new java.util.ArrayList();
                    
                        java.util.ArrayList list27 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","clientVersion").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setClientVersion(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","attributeAssignType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeAssignType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                              reader.next();
                                                          } else {
                                                        list3.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone3 = false;
                                                        while(!loopDone3){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone3 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list3.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list3.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeAssignLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list4.add(null);
                                                              reader.next();
                                                          } else {
                                                        list4.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone4 = false;
                                                        while(!loopDone4){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone4 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list4.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list4.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeDefLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.class,
                                                                list4));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNameLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list5.add(null);
                                                              reader.next();
                                                          } else {
                                                        list5.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone5 = false;
                                                        while(!loopDone5){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone5 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNameLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list5.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list5.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone5 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeDefNameLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.class,
                                                                list5));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerGroupLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list6.add(null);
                                                              reader.next();
                                                          } else {
                                                        list6.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone6 = false;
                                                        while(!loopDone6){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone6 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerGroupLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list6.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list6.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone6 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsOwnerGroupLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup.class,
                                                                list6));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerStemLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list7.add(null);
                                                              reader.next();
                                                          } else {
                                                        list7.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone7 = false;
                                                        while(!loopDone7){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone7 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerStemLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list7.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list7.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone7 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsOwnerStemLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsStemLookup.class,
                                                                list7));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerSubjectLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list8.add(null);
                                                              reader.next();
                                                          } else {
                                                        list8.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone8 = false;
                                                        while(!loopDone8){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone8 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerSubjectLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list8.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list8.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone8 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsOwnerSubjectLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.class,
                                                                list8));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerMembershipLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list9.add(null);
                                                              reader.next();
                                                          } else {
                                                        list9.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone9 = false;
                                                        while(!loopDone9){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone9 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerMembershipLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list9.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list9.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone9 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsOwnerMembershipLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipLookup.class,
                                                                list9));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerMembershipAnyLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list10.add(null);
                                                              reader.next();
                                                          } else {
                                                        list10.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone10 = false;
                                                        while(!loopDone10){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone10 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerMembershipAnyLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list10.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list10.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone10 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsOwnerMembershipAnyLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsMembershipAnyLookup.class,
                                                                list10));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerAttributeDefLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list11.add(null);
                                                              reader.next();
                                                          } else {
                                                        list11.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone11 = false;
                                                        while(!loopDone11){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone11 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsOwnerAttributeDefLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list11.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list11.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone11 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsOwnerAttributeDefLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.class,
                                                                list11));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actions").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list12.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list12.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone12 = false;
                                            while(!loopDone12){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone12 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actions").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list12.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list12.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone12 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setActions((java.lang.String[])
                                                        list12.toArray(new java.lang.String[list12.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeAssignmentsOnAssignments").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeAssignmentsOnAssignments(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setActAsSubjectLookup(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setActAsSubjectLookup(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeSubjectDetail").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeSubjectDetail(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list16.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list16.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone16 = false;
                                            while(!loopDone16){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone16 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list16.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list16.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone16 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setSubjectAttributeNames((java.lang.String[])
                                                        list16.toArray(new java.lang.String[list16.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeGroupDetail").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeGroupDetail(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list18.add(null);
                                                              reader.next();
                                                          } else {
                                                        list18.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone18 = false;
                                                        while(!loopDone18){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone18 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list18.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list18.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone18 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setParams((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.class,
                                                                list18));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","enabled").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setEnabled(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","attributeDefValueType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeDefValueType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","theValue").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTheValue(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeAssignmentsFromAssignments").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeAssignmentsFromAssignments(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","attributeDefType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeDefType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeAssignLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list24.add(null);
                                                              reader.next();
                                                          } else {
                                                        list24.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone24 = false;
                                                        while(!loopDone24){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone24 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeAssignLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list24.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list24.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone24 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAssignAssignOwnerAttributeAssignLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeAssignLookup.class,
                                                                list24));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeDefLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list25.add(null);
                                                              reader.next();
                                                          } else {
                                                        list25.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone25 = false;
                                                        while(!loopDone25){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone25 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeDefLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list25.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list25.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone25 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAssignAssignOwnerAttributeDefLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.class,
                                                                list25));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeDefNameLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list26.add(null);
                                                              reader.next();
                                                          } else {
                                                        list26.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone26 = false;
                                                        while(!loopDone26){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() && !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                loopDone26 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerAttributeDefNameLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list26.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list26.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone26 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAssignAssignOwnerAttributeDefNameLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.class,
                                                                list26));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerActions").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list27.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list27.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone27 = false;
                                            while(!loopDone27){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() && !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    loopDone27 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAssignAssignOwnerActions").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list27.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list27.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone27 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setWsAssignAssignOwnerActions((java.lang.String[])
                                                        list27.toArray(new java.lang.String[list27.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    
