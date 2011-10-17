
/**
 * GetPermissionAssignments.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:41 LKT)
 */
            
                package edu.internet2.middleware.grouper.ws.soap_v2_1.xsd;
            

            /**
            *  GetPermissionAssignments bean class
            */
        
        public  class GetPermissionAssignments
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                "getPermissionAssignments",
                "ns1");

            

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        

                        /**
                        * field for ClientVersion
                        */

                        
                                    protected java.lang.String localClientVersion ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localClientVersionTracker = false ;
                           

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
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localClientVersionTracker = true;
                                       } else {
                                          localClientVersionTracker = true;
                                              
                                       }
                                   
                                            this.localClientVersion=param;
                                    

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

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localWsAttributeDefLookupsTracker = true;
                                          } else {
                                             localWsAttributeDefLookupsTracker = true;
                                                 
                                          }
                                      
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

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localWsAttributeDefNameLookupsTracker = true;
                                          } else {
                                             localWsAttributeDefNameLookupsTracker = true;
                                                 
                                          }
                                      
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
                        * field for RoleLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[] localRoleLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRoleLookupsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[] getRoleLookups(){
                               return localRoleLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for RoleLookups
                               */
                              protected void validateRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param RoleLookups
                              */
                              public void setRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[] param){
                              
                                   validateRoleLookups(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localRoleLookupsTracker = true;
                                          } else {
                                             localRoleLookupsTracker = true;
                                                 
                                          }
                                      
                                      this.localRoleLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup
                             */
                             public void addRoleLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup param){
                                   if (localRoleLookups == null){
                                   localRoleLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localRoleLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localRoleLookups);
                               list.add(param);
                               this.localRoleLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[list.size()]);

                             }
                             

                        /**
                        * field for WsSubjectLookups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] localWsSubjectLookups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsSubjectLookupsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] getWsSubjectLookups(){
                               return localWsSubjectLookups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsSubjectLookups
                               */
                              protected void validateWsSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsSubjectLookups
                              */
                              public void setWsSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[] param){
                              
                                   validateWsSubjectLookups(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localWsSubjectLookupsTracker = true;
                                          } else {
                                             localWsSubjectLookupsTracker = true;
                                                 
                                          }
                                      
                                      this.localWsSubjectLookups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup
                             */
                             public void addWsSubjectLookups(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup param){
                                   if (localWsSubjectLookups == null){
                                   localWsSubjectLookups = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsSubjectLookupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsSubjectLookups);
                               list.add(param);
                               this.localWsSubjectLookups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[list.size()]);

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

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localActionsTracker = true;
                                          } else {
                                             localActionsTracker = true;
                                                 
                                          }
                                      
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
                        * field for IncludePermissionAssignDetail
                        */

                        
                                    protected java.lang.String localIncludePermissionAssignDetail ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludePermissionAssignDetailTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludePermissionAssignDetail(){
                               return localIncludePermissionAssignDetail;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludePermissionAssignDetail
                               */
                               public void setIncludePermissionAssignDetail(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludePermissionAssignDetailTracker = true;
                                       } else {
                                          localIncludePermissionAssignDetailTracker = true;
                                              
                                       }
                                   
                                            this.localIncludePermissionAssignDetail=param;
                                    

                               }
                            

                        /**
                        * field for IncludeAttributeDefNames
                        */

                        
                                    protected java.lang.String localIncludeAttributeDefNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeAttributeDefNamesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeAttributeDefNames(){
                               return localIncludeAttributeDefNames;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeAttributeDefNames
                               */
                               public void setIncludeAttributeDefNames(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludeAttributeDefNamesTracker = true;
                                       } else {
                                          localIncludeAttributeDefNamesTracker = true;
                                              
                                       }
                                   
                                            this.localIncludeAttributeDefNames=param;
                                    

                               }
                            

                        /**
                        * field for IncludeAttributeAssignments
                        */

                        
                                    protected java.lang.String localIncludeAttributeAssignments ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeAttributeAssignmentsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeAttributeAssignments(){
                               return localIncludeAttributeAssignments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeAttributeAssignments
                               */
                               public void setIncludeAttributeAssignments(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludeAttributeAssignmentsTracker = true;
                                       } else {
                                          localIncludeAttributeAssignmentsTracker = true;
                                              
                                       }
                                   
                                            this.localIncludeAttributeAssignments=param;
                                    

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
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludeAssignmentsOnAssignmentsTracker = true;
                                       } else {
                                          localIncludeAssignmentsOnAssignmentsTracker = true;
                                              
                                       }
                                   
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
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localActAsSubjectLookupTracker = true;
                                       } else {
                                          localActAsSubjectLookupTracker = true;
                                              
                                       }
                                   
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
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludeSubjectDetailTracker = true;
                                       } else {
                                          localIncludeSubjectDetailTracker = true;
                                              
                                       }
                                   
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

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localSubjectAttributeNamesTracker = true;
                                          } else {
                                             localSubjectAttributeNamesTracker = true;
                                                 
                                          }
                                      
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
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludeGroupDetailTracker = true;
                                       } else {
                                          localIncludeGroupDetailTracker = true;
                                              
                                       }
                                   
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

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localParamsTracker = true;
                                          } else {
                                             localParamsTracker = true;
                                                 
                                          }
                                      
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
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localEnabledTracker = true;
                                       } else {
                                          localEnabledTracker = true;
                                              
                                       }
                                   
                                            this.localEnabled=param;
                                    

                               }
                            

                        /**
                        * field for PointInTimeFrom
                        */

                        
                                    protected java.lang.String localPointInTimeFrom ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPointInTimeFromTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPointInTimeFrom(){
                               return localPointInTimeFrom;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PointInTimeFrom
                               */
                               public void setPointInTimeFrom(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPointInTimeFromTracker = true;
                                       } else {
                                          localPointInTimeFromTracker = true;
                                              
                                       }
                                   
                                            this.localPointInTimeFrom=param;
                                    

                               }
                            

                        /**
                        * field for PointInTimeTo
                        */

                        
                                    protected java.lang.String localPointInTimeTo ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPointInTimeToTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPointInTimeTo(){
                               return localPointInTimeTo;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PointInTimeTo
                               */
                               public void setPointInTimeTo(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPointInTimeToTracker = true;
                                       } else {
                                          localPointInTimeToTracker = true;
                                              
                                       }
                                   
                                            this.localPointInTimeTo=param;
                                    

                               }
                            

                        /**
                        * field for ImmediateOnly
                        */

                        
                                    protected java.lang.String localImmediateOnly ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localImmediateOnlyTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getImmediateOnly(){
                               return localImmediateOnly;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ImmediateOnly
                               */
                               public void setImmediateOnly(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localImmediateOnlyTracker = true;
                                       } else {
                                          localImmediateOnlyTracker = true;
                                              
                                       }
                                   
                                            this.localImmediateOnly=param;
                                    

                               }
                            

                        /**
                        * field for PermissionType
                        */

                        
                                    protected java.lang.String localPermissionType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionTypeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPermissionType(){
                               return localPermissionType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionType
                               */
                               public void setPermissionType(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPermissionTypeTracker = true;
                                       } else {
                                          localPermissionTypeTracker = true;
                                              
                                       }
                                   
                                            this.localPermissionType=param;
                                    

                               }
                            

                        /**
                        * field for PermissionProcessor
                        */

                        
                                    protected java.lang.String localPermissionProcessor ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionProcessorTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPermissionProcessor(){
                               return localPermissionProcessor;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionProcessor
                               */
                               public void setPermissionProcessor(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPermissionProcessorTracker = true;
                                       } else {
                                          localPermissionProcessorTracker = true;
                                              
                                       }
                                   
                                            this.localPermissionProcessor=param;
                                    

                               }
                            

                        /**
                        * field for LimitEnvVars
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[] localLimitEnvVars ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLimitEnvVarsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[] getLimitEnvVars(){
                               return localLimitEnvVars;
                           }

                           
                        


                               
                              /**
                               * validate the array for LimitEnvVars
                               */
                              protected void validateLimitEnvVars(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param LimitEnvVars
                              */
                              public void setLimitEnvVars(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[] param){
                              
                                   validateLimitEnvVars(param);

                               
                                          if (param != null){
                                             //update the setting tracker
                                             localLimitEnvVarsTracker = true;
                                          } else {
                                             localLimitEnvVarsTracker = true;
                                                 
                                          }
                                      
                                      this.localLimitEnvVars=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar
                             */
                             public void addLimitEnvVars(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar param){
                                   if (localLimitEnvVars == null){
                                   localLimitEnvVars = new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[]{};
                                   }

                            
                                 //update the setting tracker
                                localLimitEnvVarsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localLimitEnvVars);
                               list.add(param);
                               this.localLimitEnvVars =
                             (edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[list.size()]);

                             }
                             

                        /**
                        * field for IncludeLimits
                        */

                        
                                    protected java.lang.String localIncludeLimits ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIncludeLimitsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getIncludeLimits(){
                               return localIncludeLimits;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param IncludeLimits
                               */
                               public void setIncludeLimits(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIncludeLimitsTracker = true;
                                       } else {
                                          localIncludeLimitsTracker = true;
                                              
                                       }
                                   
                                            this.localIncludeLimits=param;
                                    

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
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
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       GetPermissionAssignments.this.serialize(MY_QNAME,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       final org.apache.axiom.om.OMFactory factory,
                                       org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,factory,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               final org.apache.axiom.om.OMFactory factory,
                               org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();

                    if ((namespace != null) && (namespace.trim().length() > 0)) {
                        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                        if (writerPrefix != null) {
                            xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                        } else {
                            if (prefix == null) {
                                prefix = generatePrefix(namespace);
                            }

                            xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                            xmlWriter.writeNamespace(prefix, namespace);
                            xmlWriter.setPrefix(prefix, namespace);
                        }
                    } else {
                        xmlWriter.writeStartElement(parentQName.getLocalPart());
                    }
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":getPermissionAssignments",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "getPermissionAssignments",
                           xmlWriter);
                   }

               
                   }
                if (localClientVersionTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"clientVersion", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"clientVersion");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("clientVersion");
                                    }
                                

                                          if (localClientVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClientVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeDefLookupsTracker){
                                       if (localWsAttributeDefLookups!=null){
                                            for (int i = 0;i < localWsAttributeDefLookups.length;i++){
                                                if (localWsAttributeDefLookups[i] != null){
                                                 localWsAttributeDefLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"wsAttributeDefLookups", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"wsAttributeDefLookups");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("wsAttributeDefLookups");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"wsAttributeDefLookups", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"wsAttributeDefLookups");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("wsAttributeDefLookups");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsAttributeDefNameLookupsTracker){
                                       if (localWsAttributeDefNameLookups!=null){
                                            for (int i = 0;i < localWsAttributeDefNameLookups.length;i++){
                                                if (localWsAttributeDefNameLookups[i] != null){
                                                 localWsAttributeDefNameLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNameLookups"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"wsAttributeDefNameLookups", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"wsAttributeDefNameLookups");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("wsAttributeDefNameLookups");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"wsAttributeDefNameLookups", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"wsAttributeDefNameLookups");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("wsAttributeDefNameLookups");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localRoleLookupsTracker){
                                       if (localRoleLookups!=null){
                                            for (int i = 0;i < localRoleLookups.length;i++){
                                                if (localRoleLookups[i] != null){
                                                 localRoleLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","roleLookups"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"roleLookups", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"roleLookups");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("roleLookups");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"roleLookups", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"roleLookups");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("roleLookups");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsSubjectLookupsTracker){
                                       if (localWsSubjectLookups!=null){
                                            for (int i = 0;i < localWsSubjectLookups.length;i++){
                                                if (localWsSubjectLookups[i] != null){
                                                 localWsSubjectLookups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsSubjectLookups"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"wsSubjectLookups", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"wsSubjectLookups");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("wsSubjectLookups");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"wsSubjectLookups", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"wsSubjectLookups");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("wsSubjectLookups");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localActionsTracker){
                             if (localActions!=null) {
                                   namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localActions.length;i++){
                                        
                                            if (localActions[i] != null){
                                        
                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"actions", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"actions");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("actions");
                                                }

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActions[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace.equals("")) {
                                                                prefix = xmlWriter.getPrefix(namespace);

                                                                if (prefix == null) {
                                                                    prefix = generatePrefix(namespace);

                                                                    xmlWriter.writeStartElement(prefix,"actions", namespace);
                                                                    xmlWriter.writeNamespace(prefix, namespace);
                                                                    xmlWriter.setPrefix(prefix, namespace);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace,"actions");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("actions");
                                                            }
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                            if (! namespace2.equals("")) {
                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                if (prefix2 == null) {
                                                    prefix2 = generatePrefix(namespace2);

                                                    xmlWriter.writeStartElement(prefix2,"actions", namespace2);
                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                } else {
                                                    xmlWriter.writeStartElement(namespace2,"actions");
                                                }

                                            } else {
                                                xmlWriter.writeStartElement("actions");
                                            }

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localIncludePermissionAssignDetailTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includePermissionAssignDetail", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includePermissionAssignDetail");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includePermissionAssignDetail");
                                    }
                                

                                          if (localIncludePermissionAssignDetail==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludePermissionAssignDetail);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIncludeAttributeDefNamesTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includeAttributeDefNames", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includeAttributeDefNames");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includeAttributeDefNames");
                                    }
                                

                                          if (localIncludeAttributeDefNames==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeAttributeDefNames);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIncludeAttributeAssignmentsTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includeAttributeAssignments", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includeAttributeAssignments");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includeAttributeAssignments");
                                    }
                                

                                          if (localIncludeAttributeAssignments==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeAttributeAssignments);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIncludeAssignmentsOnAssignmentsTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includeAssignmentsOnAssignments", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includeAssignmentsOnAssignments");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includeAssignmentsOnAssignments");
                                    }
                                

                                          if (localIncludeAssignmentsOnAssignments==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeAssignmentsOnAssignments);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectLookupTracker){
                                    if (localActAsSubjectLookup==null){

                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";

                                        if (! namespace2.equals("")) {
                                            java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                            if (prefix2 == null) {
                                                prefix2 = generatePrefix(namespace2);

                                                xmlWriter.writeStartElement(prefix2,"actAsSubjectLookup", namespace2);
                                                xmlWriter.writeNamespace(prefix2, namespace2);
                                                xmlWriter.setPrefix(prefix2, namespace2);

                                            } else {
                                                xmlWriter.writeStartElement(namespace2,"actAsSubjectLookup");
                                            }

                                        } else {
                                            xmlWriter.writeStartElement("actAsSubjectLookup");
                                        }


                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localActAsSubjectLookup.serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectLookup"),
                                        factory,xmlWriter);
                                    }
                                } if (localIncludeSubjectDetailTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includeSubjectDetail", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includeSubjectDetail");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includeSubjectDetail");
                                    }
                                

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
                                   boolean emptyNamespace = namespace == null || namespace.length() == 0;
                                   prefix =  emptyNamespace ? null : xmlWriter.getPrefix(namespace);
                                   for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                        
                                            if (localSubjectAttributeNames[i] != null){
                                        
                                                if (!emptyNamespace) {
                                                    if (prefix == null) {
                                                        java.lang.String prefix2 = generatePrefix(namespace);

                                                        xmlWriter.writeStartElement(prefix2,"subjectAttributeNames", namespace);
                                                        xmlWriter.writeNamespace(prefix2, namespace);
                                                        xmlWriter.setPrefix(prefix2, namespace);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace,"subjectAttributeNames");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("subjectAttributeNames");
                                                }

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace.equals("")) {
                                                                prefix = xmlWriter.getPrefix(namespace);

                                                                if (prefix == null) {
                                                                    prefix = generatePrefix(namespace);

                                                                    xmlWriter.writeStartElement(prefix,"subjectAttributeNames", namespace);
                                                                    xmlWriter.writeNamespace(prefix, namespace);
                                                                    xmlWriter.setPrefix(prefix, namespace);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace,"subjectAttributeNames");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("subjectAttributeNames");
                                                            }
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                            if (! namespace2.equals("")) {
                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                if (prefix2 == null) {
                                                    prefix2 = generatePrefix(namespace2);

                                                    xmlWriter.writeStartElement(prefix2,"subjectAttributeNames", namespace2);
                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                } else {
                                                    xmlWriter.writeStartElement(namespace2,"subjectAttributeNames");
                                                }

                                            } else {
                                                xmlWriter.writeStartElement("subjectAttributeNames");
                                            }

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localIncludeGroupDetailTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includeGroupDetail", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includeGroupDetail");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includeGroupDetail");
                                    }
                                

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
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"params", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"params");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("params");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"params", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"params");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("params");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localEnabledTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"enabled", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"enabled");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("enabled");
                                    }
                                

                                          if (localEnabled==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localEnabled);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPointInTimeFromTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"pointInTimeFrom", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"pointInTimeFrom");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("pointInTimeFrom");
                                    }
                                

                                          if (localPointInTimeFrom==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPointInTimeFrom);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPointInTimeToTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"pointInTimeTo", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"pointInTimeTo");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("pointInTimeTo");
                                    }
                                

                                          if (localPointInTimeTo==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPointInTimeTo);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localImmediateOnlyTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"immediateOnly", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"immediateOnly");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("immediateOnly");
                                    }
                                

                                          if (localImmediateOnly==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localImmediateOnly);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPermissionTypeTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"permissionType", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"permissionType");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("permissionType");
                                    }
                                

                                          if (localPermissionType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPermissionProcessorTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"permissionProcessor", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"permissionProcessor");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("permissionProcessor");
                                    }
                                

                                          if (localPermissionProcessor==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionProcessor);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localLimitEnvVarsTracker){
                                       if (localLimitEnvVars!=null){
                                            for (int i = 0;i < localLimitEnvVars.length;i++){
                                                if (localLimitEnvVars[i] != null){
                                                 localLimitEnvVars[i].serialize(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","limitEnvVars"),
                                                           factory,xmlWriter);
                                                } else {
                                                   
                                                            // write null attribute
                                                            java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                            if (! namespace2.equals("")) {
                                                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                                if (prefix2 == null) {
                                                                    prefix2 = generatePrefix(namespace2);

                                                                    xmlWriter.writeStartElement(prefix2,"limitEnvVars", namespace2);
                                                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                                                    xmlWriter.setPrefix(prefix2, namespace2);

                                                                } else {
                                                                    xmlWriter.writeStartElement(namespace2,"limitEnvVars");
                                                                }

                                                            } else {
                                                                xmlWriter.writeStartElement("limitEnvVars");
                                                            }

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                // write null attribute
                                                java.lang.String namespace2 = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                                if (! namespace2.equals("")) {
                                                    java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                                    if (prefix2 == null) {
                                                        prefix2 = generatePrefix(namespace2);

                                                        xmlWriter.writeStartElement(prefix2,"limitEnvVars", namespace2);
                                                        xmlWriter.writeNamespace(prefix2, namespace2);
                                                        xmlWriter.setPrefix(prefix2, namespace2);

                                                    } else {
                                                        xmlWriter.writeStartElement(namespace2,"limitEnvVars");
                                                    }

                                                } else {
                                                    xmlWriter.writeStartElement("limitEnvVars");
                                                }

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localIncludeLimitsTracker){
                                    namespace = "http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"includeLimits", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"includeLimits");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("includeLimits");
                                    }
                                

                                          if (localIncludeLimits==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localIncludeLimits);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

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
                if (namespace.equals(""))
              {
                  xmlWriter.writeAttribute(attName,attValue);
              }
              else
              {
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

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
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

                        } if (localRoleLookupsTracker){
                             if (localRoleLookups!=null) {
                                 for (int i = 0;i < localRoleLookups.length;i++){

                                    if (localRoleLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                         elementList.add(localRoleLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "roleLookups"));
                                        elementList.add(localRoleLookups);
                                    
                             }

                        } if (localWsSubjectLookupsTracker){
                             if (localWsSubjectLookups!=null) {
                                 for (int i = 0;i < localWsSubjectLookups.length;i++){

                                    if (localWsSubjectLookups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsSubjectLookups"));
                                         elementList.add(localWsSubjectLookups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsSubjectLookups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsSubjectLookups"));
                                        elementList.add(localWsSubjectLookups);
                                    
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

                        } if (localIncludePermissionAssignDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includePermissionAssignDetail"));
                                 
                                         elementList.add(localIncludePermissionAssignDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludePermissionAssignDetail));
                                    } if (localIncludeAttributeDefNamesTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeAttributeDefNames"));
                                 
                                         elementList.add(localIncludeAttributeDefNames==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeAttributeDefNames));
                                    } if (localIncludeAttributeAssignmentsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeAttributeAssignments"));
                                 
                                         elementList.add(localIncludeAttributeAssignments==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeAttributeAssignments));
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
                                    } if (localPointInTimeFromTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pointInTimeFrom"));
                                 
                                         elementList.add(localPointInTimeFrom==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPointInTimeFrom));
                                    } if (localPointInTimeToTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "pointInTimeTo"));
                                 
                                         elementList.add(localPointInTimeTo==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPointInTimeTo));
                                    } if (localImmediateOnlyTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "immediateOnly"));
                                 
                                         elementList.add(localImmediateOnly==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localImmediateOnly));
                                    } if (localPermissionTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionType"));
                                 
                                         elementList.add(localPermissionType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionType));
                                    } if (localPermissionProcessorTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionProcessor"));
                                 
                                         elementList.add(localPermissionProcessor==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionProcessor));
                                    } if (localLimitEnvVarsTracker){
                             if (localLimitEnvVars!=null) {
                                 for (int i = 0;i < localLimitEnvVars.length;i++){

                                    if (localLimitEnvVars[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "limitEnvVars"));
                                         elementList.add(localLimitEnvVars[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "limitEnvVars"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "limitEnvVars"));
                                        elementList.add(localLimitEnvVars);
                                    
                             }

                        } if (localIncludeLimitsTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeLimits"));
                                 
                                         elementList.add(localIncludeLimits==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeLimits));
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
        public static GetPermissionAssignments parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetPermissionAssignments object =
                new GetPermissionAssignments();

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
                    
                            if (!"getPermissionAssignments".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (GetPermissionAssignments)edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                 
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                        java.util.ArrayList list3 = new java.util.ArrayList();
                    
                        java.util.ArrayList list4 = new java.util.ArrayList();
                    
                        java.util.ArrayList list5 = new java.util.ArrayList();
                    
                        java.util.ArrayList list6 = new java.util.ArrayList();
                    
                        java.util.ArrayList list13 = new java.util.ArrayList();
                    
                        java.util.ArrayList list15 = new java.util.ArrayList();
                    
                        java.util.ArrayList list22 = new java.util.ArrayList();
                    
                                    
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list2.add(null);
                                                              reader.next();
                                                          } else {
                                                        list2.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone2 = false;
                                                        while(!loopDone2){
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
                                                                loopDone2 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list2.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list2.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeDefLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefLookup.class,
                                                                list2));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNameLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                              reader.next();
                                                          } else {
                                                        list3.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNameLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list3.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list3.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone3 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeDefNameLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsAttributeDefNameLookup.class,
                                                                list3));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","roleLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list4.add(null);
                                                              reader.next();
                                                          } else {
                                                        list4.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","roleLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list4.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list4.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setRoleLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsGroupLookup.class,
                                                                list4));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsSubjectLookups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list5.add(null);
                                                              reader.next();
                                                          } else {
                                                        list5.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","wsSubjectLookups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list5.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list5.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone5 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsSubjectLookups((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsSubjectLookup.class,
                                                                list5));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actions").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list6.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list6.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone6 = false;
                                            while(!loopDone6){
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
                                                    loopDone6 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","actions").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list6.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list6.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone6 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setActions((java.lang.String[])
                                                        list6.toArray(new java.lang.String[list6.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includePermissionAssignDetail").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludePermissionAssignDetail(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeAttributeDefNames").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeAttributeDefNames(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeAttributeAssignments").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeAttributeAssignments(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
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
                                                  list13.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list13.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone13 = false;
                                            while(!loopDone13){
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
                                                    loopDone13 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list13.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list13.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone13 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setSubjectAttributeNames((java.lang.String[])
                                                        list13.toArray(new java.lang.String[list13.size()]));
                                                
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
                                                              list15.add(null);
                                                              reader.next();
                                                          } else {
                                                        list15.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone15 = false;
                                                        while(!loopDone15){
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
                                                                loopDone15 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","params").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list15.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list15.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone15 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setParams((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsParam.class,
                                                                list15));
                                                            
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","pointInTimeFrom").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPointInTimeFrom(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","pointInTimeTo").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPointInTimeTo(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","immediateOnly").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setImmediateOnly(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","permissionType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPermissionType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","permissionProcessor").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPermissionProcessor(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","limitEnvVars").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list22.add(null);
                                                              reader.next();
                                                          } else {
                                                        list22.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar.Factory.parse(reader));
                                                                }
                                                        //loop until we find a start element that is not part of this array
                                                        boolean loopDone22 = false;
                                                        while(!loopDone22){
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
                                                                loopDone22 = true;
                                                            } else {
                                                                if (new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","limitEnvVars").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list22.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list22.add(edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone22 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setLimitEnvVars((edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_1.xsd.WsPermissionEnvVar.class,
                                                                list22));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_1.ws.grouper.middleware.internet2.edu/xsd","includeLimits").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setIncludeLimits(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                  
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                            



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          