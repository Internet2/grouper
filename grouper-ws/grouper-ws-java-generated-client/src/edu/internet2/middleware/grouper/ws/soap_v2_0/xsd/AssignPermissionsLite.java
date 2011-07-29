
/**
 * AssignPermissionsLite.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:41 LKT)
 */
            
                package edu.internet2.middleware.grouper.ws.soap_v2_0.xsd;
            

            /**
            *  AssignPermissionsLite bean class
            */
        
        public  class AssignPermissionsLite
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                "assignPermissionsLite",
                "ns1");

            

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd")){
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
                        * field for PermissionDefNameName
                        */

                        
                                    protected java.lang.String localPermissionDefNameName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionDefNameNameTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPermissionDefNameName(){
                               return localPermissionDefNameName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionDefNameName
                               */
                               public void setPermissionDefNameName(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPermissionDefNameNameTracker = true;
                                       } else {
                                          localPermissionDefNameNameTracker = true;
                                              
                                       }
                                   
                                            this.localPermissionDefNameName=param;
                                    

                               }
                            

                        /**
                        * field for PermissionDefNameId
                        */

                        
                                    protected java.lang.String localPermissionDefNameId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionDefNameIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPermissionDefNameId(){
                               return localPermissionDefNameId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionDefNameId
                               */
                               public void setPermissionDefNameId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPermissionDefNameIdTracker = true;
                                       } else {
                                          localPermissionDefNameIdTracker = true;
                                              
                                       }
                                   
                                            this.localPermissionDefNameId=param;
                                    

                               }
                            

                        /**
                        * field for PermissionAssignOperation
                        */

                        
                                    protected java.lang.String localPermissionAssignOperation ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPermissionAssignOperationTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPermissionAssignOperation(){
                               return localPermissionAssignOperation;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PermissionAssignOperation
                               */
                               public void setPermissionAssignOperation(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPermissionAssignOperationTracker = true;
                                       } else {
                                          localPermissionAssignOperationTracker = true;
                                              
                                       }
                                   
                                            this.localPermissionAssignOperation=param;
                                    

                               }
                            

                        /**
                        * field for AssignmentNotes
                        */

                        
                                    protected java.lang.String localAssignmentNotes ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignmentNotesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignmentNotes(){
                               return localAssignmentNotes;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignmentNotes
                               */
                               public void setAssignmentNotes(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localAssignmentNotesTracker = true;
                                       } else {
                                          localAssignmentNotesTracker = true;
                                              
                                       }
                                   
                                            this.localAssignmentNotes=param;
                                    

                               }
                            

                        /**
                        * field for AssignmentEnabledTime
                        */

                        
                                    protected java.lang.String localAssignmentEnabledTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignmentEnabledTimeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignmentEnabledTime(){
                               return localAssignmentEnabledTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignmentEnabledTime
                               */
                               public void setAssignmentEnabledTime(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localAssignmentEnabledTimeTracker = true;
                                       } else {
                                          localAssignmentEnabledTimeTracker = true;
                                              
                                       }
                                   
                                            this.localAssignmentEnabledTime=param;
                                    

                               }
                            

                        /**
                        * field for AssignmentDisabledTime
                        */

                        
                                    protected java.lang.String localAssignmentDisabledTime ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignmentDisabledTimeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignmentDisabledTime(){
                               return localAssignmentDisabledTime;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignmentDisabledTime
                               */
                               public void setAssignmentDisabledTime(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localAssignmentDisabledTimeTracker = true;
                                       } else {
                                          localAssignmentDisabledTimeTracker = true;
                                              
                                       }
                                   
                                            this.localAssignmentDisabledTime=param;
                                    

                               }
                            

                        /**
                        * field for Delegatable
                        */

                        
                                    protected java.lang.String localDelegatable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDelegatableTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDelegatable(){
                               return localDelegatable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Delegatable
                               */
                               public void setDelegatable(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDelegatableTracker = true;
                                       } else {
                                          localDelegatableTracker = true;
                                              
                                       }
                                   
                                            this.localDelegatable=param;
                                    

                               }
                            

                        /**
                        * field for WsAttributeAssignId
                        */

                        
                                    protected java.lang.String localWsAttributeAssignId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeAssignIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getWsAttributeAssignId(){
                               return localWsAttributeAssignId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsAttributeAssignId
                               */
                               public void setWsAttributeAssignId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localWsAttributeAssignIdTracker = true;
                                       } else {
                                          localWsAttributeAssignIdTracker = true;
                                              
                                       }
                                   
                                            this.localWsAttributeAssignId=param;
                                    

                               }
                            

                        /**
                        * field for RoleName
                        */

                        
                                    protected java.lang.String localRoleName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRoleNameTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRoleName(){
                               return localRoleName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RoleName
                               */
                               public void setRoleName(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localRoleNameTracker = true;
                                       } else {
                                          localRoleNameTracker = true;
                                              
                                       }
                                   
                                            this.localRoleName=param;
                                    

                               }
                            

                        /**
                        * field for RoleId
                        */

                        
                                    protected java.lang.String localRoleId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRoleIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRoleId(){
                               return localRoleId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RoleId
                               */
                               public void setRoleId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localRoleIdTracker = true;
                                       } else {
                                          localRoleIdTracker = true;
                                              
                                       }
                                   
                                            this.localRoleId=param;
                                    

                               }
                            

                        /**
                        * field for SubjectRoleName
                        */

                        
                                    protected java.lang.String localSubjectRoleName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectRoleNameTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectRoleName(){
                               return localSubjectRoleName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectRoleName
                               */
                               public void setSubjectRoleName(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSubjectRoleNameTracker = true;
                                       } else {
                                          localSubjectRoleNameTracker = true;
                                              
                                       }
                                   
                                            this.localSubjectRoleName=param;
                                    

                               }
                            

                        /**
                        * field for SubjectRoleId
                        */

                        
                                    protected java.lang.String localSubjectRoleId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectRoleIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectRoleId(){
                               return localSubjectRoleId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectRoleId
                               */
                               public void setSubjectRoleId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSubjectRoleIdTracker = true;
                                       } else {
                                          localSubjectRoleIdTracker = true;
                                              
                                       }
                                   
                                            this.localSubjectRoleId=param;
                                    

                               }
                            

                        /**
                        * field for SubjectRoleSubjectId
                        */

                        
                                    protected java.lang.String localSubjectRoleSubjectId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectRoleSubjectIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectRoleSubjectId(){
                               return localSubjectRoleSubjectId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectRoleSubjectId
                               */
                               public void setSubjectRoleSubjectId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSubjectRoleSubjectIdTracker = true;
                                       } else {
                                          localSubjectRoleSubjectIdTracker = true;
                                              
                                       }
                                   
                                            this.localSubjectRoleSubjectId=param;
                                    

                               }
                            

                        /**
                        * field for SubjectRoleSubjectSourceId
                        */

                        
                                    protected java.lang.String localSubjectRoleSubjectSourceId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectRoleSubjectSourceIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectRoleSubjectSourceId(){
                               return localSubjectRoleSubjectSourceId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectRoleSubjectSourceId
                               */
                               public void setSubjectRoleSubjectSourceId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSubjectRoleSubjectSourceIdTracker = true;
                                       } else {
                                          localSubjectRoleSubjectSourceIdTracker = true;
                                              
                                       }
                                   
                                            this.localSubjectRoleSubjectSourceId=param;
                                    

                               }
                            

                        /**
                        * field for SubjectRoleSubjectIdentifier
                        */

                        
                                    protected java.lang.String localSubjectRoleSubjectIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectRoleSubjectIdentifierTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectRoleSubjectIdentifier(){
                               return localSubjectRoleSubjectIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectRoleSubjectIdentifier
                               */
                               public void setSubjectRoleSubjectIdentifier(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSubjectRoleSubjectIdentifierTracker = true;
                                       } else {
                                          localSubjectRoleSubjectIdentifierTracker = true;
                                              
                                       }
                                   
                                            this.localSubjectRoleSubjectIdentifier=param;
                                    

                               }
                            

                        /**
                        * field for Action
                        */

                        
                                    protected java.lang.String localAction ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActionTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAction(){
                               return localAction;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Action
                               */
                               public void setAction(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localActionTracker = true;
                                       } else {
                                          localActionTracker = true;
                                              
                                       }
                                   
                                            this.localAction=param;
                                    

                               }
                            

                        /**
                        * field for ActAsSubjectId
                        */

                        
                                    protected java.lang.String localActAsSubjectId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActAsSubjectId(){
                               return localActAsSubjectId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectId
                               */
                               public void setActAsSubjectId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localActAsSubjectIdTracker = true;
                                       } else {
                                          localActAsSubjectIdTracker = true;
                                              
                                       }
                                   
                                            this.localActAsSubjectId=param;
                                    

                               }
                            

                        /**
                        * field for ActAsSubjectSourceId
                        */

                        
                                    protected java.lang.String localActAsSubjectSourceId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectSourceIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActAsSubjectSourceId(){
                               return localActAsSubjectSourceId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectSourceId
                               */
                               public void setActAsSubjectSourceId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localActAsSubjectSourceIdTracker = true;
                                       } else {
                                          localActAsSubjectSourceIdTracker = true;
                                              
                                       }
                                   
                                            this.localActAsSubjectSourceId=param;
                                    

                               }
                            

                        /**
                        * field for ActAsSubjectIdentifier
                        */

                        
                                    protected java.lang.String localActAsSubjectIdentifier ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActAsSubjectIdentifierTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getActAsSubjectIdentifier(){
                               return localActAsSubjectIdentifier;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ActAsSubjectIdentifier
                               */
                               public void setActAsSubjectIdentifier(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localActAsSubjectIdentifierTracker = true;
                                       } else {
                                          localActAsSubjectIdentifierTracker = true;
                                              
                                       }
                                   
                                            this.localActAsSubjectIdentifier=param;
                                    

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
                        */

                        
                                    protected java.lang.String localSubjectAttributeNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectAttributeNamesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectAttributeNames(){
                               return localSubjectAttributeNames;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectAttributeNames
                               */
                               public void setSubjectAttributeNames(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localSubjectAttributeNamesTracker = true;
                                       } else {
                                          localSubjectAttributeNamesTracker = true;
                                              
                                       }
                                   
                                            this.localSubjectAttributeNames=param;
                                    

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
                        * field for ParamName0
                        */

                        
                                    protected java.lang.String localParamName0 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamName0Tracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParamName0(){
                               return localParamName0;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParamName0
                               */
                               public void setParamName0(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localParamName0Tracker = true;
                                       } else {
                                          localParamName0Tracker = true;
                                              
                                       }
                                   
                                            this.localParamName0=param;
                                    

                               }
                            

                        /**
                        * field for ParamValue0
                        */

                        
                                    protected java.lang.String localParamValue0 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamValue0Tracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParamValue0(){
                               return localParamValue0;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParamValue0
                               */
                               public void setParamValue0(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localParamValue0Tracker = true;
                                       } else {
                                          localParamValue0Tracker = true;
                                              
                                       }
                                   
                                            this.localParamValue0=param;
                                    

                               }
                            

                        /**
                        * field for ParamName1
                        */

                        
                                    protected java.lang.String localParamName1 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamName1Tracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParamName1(){
                               return localParamName1;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParamName1
                               */
                               public void setParamName1(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localParamName1Tracker = true;
                                       } else {
                                          localParamName1Tracker = true;
                                              
                                       }
                                   
                                            this.localParamName1=param;
                                    

                               }
                            

                        /**
                        * field for ParamValue1
                        */

                        
                                    protected java.lang.String localParamValue1 ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localParamValue1Tracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getParamValue1(){
                               return localParamValue1;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ParamValue1
                               */
                               public void setParamValue1(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localParamValue1Tracker = true;
                                       } else {
                                          localParamValue1Tracker = true;
                                              
                                       }
                                   
                                            this.localParamValue1=param;
                                    

                               }
                            

                        /**
                        * field for Disallowed
                        */

                        
                                    protected java.lang.String localDisallowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDisallowedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDisallowed(){
                               return localDisallowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Disallowed
                               */
                               public void setDisallowed(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDisallowedTracker = true;
                                       } else {
                                          localDisallowedTracker = true;
                                              
                                       }
                                   
                                            this.localDisallowed=param;
                                    

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
                       AssignPermissionsLite.this.serialize(MY_QNAME,factory,xmlWriter);
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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":assignPermissionsLite",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "assignPermissionsLite",
                           xmlWriter);
                   }

               
                   }
                if (localClientVersionTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
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
                             } if (localPermissionTypeTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
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
                             } if (localPermissionDefNameNameTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"permissionDefNameName", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"permissionDefNameName");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("permissionDefNameName");
                                    }
                                

                                          if (localPermissionDefNameName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionDefNameName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPermissionDefNameIdTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"permissionDefNameId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"permissionDefNameId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("permissionDefNameId");
                                    }
                                

                                          if (localPermissionDefNameId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionDefNameId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPermissionAssignOperationTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"permissionAssignOperation", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"permissionAssignOperation");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("permissionAssignOperation");
                                    }
                                

                                          if (localPermissionAssignOperation==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionAssignOperation);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentNotesTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"assignmentNotes", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"assignmentNotes");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("assignmentNotes");
                                    }
                                

                                          if (localAssignmentNotes==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentNotes);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentEnabledTimeTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"assignmentEnabledTime", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"assignmentEnabledTime");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("assignmentEnabledTime");
                                    }
                                

                                          if (localAssignmentEnabledTime==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentEnabledTime);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignmentDisabledTimeTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"assignmentDisabledTime", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"assignmentDisabledTime");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("assignmentDisabledTime");
                                    }
                                

                                          if (localAssignmentDisabledTime==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignmentDisabledTime);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDelegatableTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"delegatable", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"delegatable");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("delegatable");
                                    }
                                

                                          if (localDelegatable==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDelegatable);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsAttributeAssignIdTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"wsAttributeAssignId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"wsAttributeAssignId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("wsAttributeAssignId");
                                    }
                                

                                          if (localWsAttributeAssignId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localWsAttributeAssignId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRoleNameTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"roleName", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"roleName");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("roleName");
                                    }
                                

                                          if (localRoleName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRoleName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRoleIdTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"roleId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"roleId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("roleId");
                                    }
                                

                                          if (localRoleId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRoleId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectRoleNameTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"subjectRoleName", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"subjectRoleName");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("subjectRoleName");
                                    }
                                

                                          if (localSubjectRoleName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectRoleName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectRoleIdTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"subjectRoleId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"subjectRoleId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("subjectRoleId");
                                    }
                                

                                          if (localSubjectRoleId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectRoleId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectRoleSubjectIdTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"subjectRoleSubjectId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"subjectRoleSubjectId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("subjectRoleSubjectId");
                                    }
                                

                                          if (localSubjectRoleSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectRoleSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectRoleSubjectSourceIdTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"subjectRoleSubjectSourceId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"subjectRoleSubjectSourceId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("subjectRoleSubjectSourceId");
                                    }
                                

                                          if (localSubjectRoleSubjectSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectRoleSubjectSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectRoleSubjectIdentifierTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"subjectRoleSubjectIdentifier", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"subjectRoleSubjectIdentifier");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("subjectRoleSubjectIdentifier");
                                    }
                                

                                          if (localSubjectRoleSubjectIdentifier==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectRoleSubjectIdentifier);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActionTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"action", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"action");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("action");
                                    }
                                

                                          if (localAction==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAction);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectIdTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"actAsSubjectId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"actAsSubjectId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("actAsSubjectId");
                                    }
                                

                                          if (localActAsSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectSourceIdTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"actAsSubjectSourceId", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"actAsSubjectSourceId");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("actAsSubjectSourceId");
                                    }
                                

                                          if (localActAsSubjectSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectIdentifierTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"actAsSubjectIdentifier", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"actAsSubjectIdentifier");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("actAsSubjectIdentifier");
                                    }
                                

                                          if (localActAsSubjectIdentifier==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectIdentifier);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIncludeSubjectDetailTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
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
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
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
                                

                                          if (localSubjectAttributeNames==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectAttributeNames);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIncludeGroupDetailTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
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
                             } if (localParamName0Tracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"paramName0", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"paramName0");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("paramName0");
                                    }
                                

                                          if (localParamName0==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamName0);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamValue0Tracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"paramValue0", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"paramValue0");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("paramValue0");
                                    }
                                

                                          if (localParamValue0==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamValue0);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamName1Tracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"paramName1", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"paramName1");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("paramName1");
                                    }
                                

                                          if (localParamName1==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamName1);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamValue1Tracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"paramValue1", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"paramValue1");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("paramValue1");
                                    }
                                

                                          if (localParamValue1==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamValue1);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDisallowedTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"disallowed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"disallowed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("disallowed");
                                    }
                                

                                          if (localDisallowed==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDisallowed);
                                            
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
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "clientVersion"));
                                 
                                         elementList.add(localClientVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientVersion));
                                    } if (localPermissionTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionType"));
                                 
                                         elementList.add(localPermissionType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionType));
                                    } if (localPermissionDefNameNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionDefNameName"));
                                 
                                         elementList.add(localPermissionDefNameName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionDefNameName));
                                    } if (localPermissionDefNameIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionDefNameId"));
                                 
                                         elementList.add(localPermissionDefNameId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionDefNameId));
                                    } if (localPermissionAssignOperationTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionAssignOperation"));
                                 
                                         elementList.add(localPermissionAssignOperation==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionAssignOperation));
                                    } if (localAssignmentNotesTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentNotes"));
                                 
                                         elementList.add(localAssignmentNotes==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentNotes));
                                    } if (localAssignmentEnabledTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentEnabledTime"));
                                 
                                         elementList.add(localAssignmentEnabledTime==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentEnabledTime));
                                    } if (localAssignmentDisabledTimeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignmentDisabledTime"));
                                 
                                         elementList.add(localAssignmentDisabledTime==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignmentDisabledTime));
                                    } if (localDelegatableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "delegatable"));
                                 
                                         elementList.add(localDelegatable==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDelegatable));
                                    } if (localWsAttributeAssignIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsAttributeAssignId"));
                                 
                                         elementList.add(localWsAttributeAssignId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localWsAttributeAssignId));
                                    } if (localRoleNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "roleName"));
                                 
                                         elementList.add(localRoleName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRoleName));
                                    } if (localRoleIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "roleId"));
                                 
                                         elementList.add(localRoleId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRoleId));
                                    } if (localSubjectRoleNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectRoleName"));
                                 
                                         elementList.add(localSubjectRoleName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectRoleName));
                                    } if (localSubjectRoleIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectRoleId"));
                                 
                                         elementList.add(localSubjectRoleId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectRoleId));
                                    } if (localSubjectRoleSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectRoleSubjectId"));
                                 
                                         elementList.add(localSubjectRoleSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectRoleSubjectId));
                                    } if (localSubjectRoleSubjectSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectRoleSubjectSourceId"));
                                 
                                         elementList.add(localSubjectRoleSubjectSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectRoleSubjectSourceId));
                                    } if (localSubjectRoleSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectRoleSubjectIdentifier"));
                                 
                                         elementList.add(localSubjectRoleSubjectIdentifier==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectRoleSubjectIdentifier));
                                    } if (localActionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "action"));
                                 
                                         elementList.add(localAction==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAction));
                                    } if (localActAsSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectId"));
                                 
                                         elementList.add(localActAsSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectId));
                                    } if (localActAsSubjectSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectSourceId"));
                                 
                                         elementList.add(localActAsSubjectSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectSourceId));
                                    } if (localActAsSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectIdentifier"));
                                 
                                         elementList.add(localActAsSubjectIdentifier==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectIdentifier));
                                    } if (localIncludeSubjectDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeSubjectDetail"));
                                 
                                         elementList.add(localIncludeSubjectDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeSubjectDetail));
                                    } if (localSubjectAttributeNamesTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectAttributeNames"));
                                 
                                         elementList.add(localSubjectAttributeNames==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames));
                                    } if (localIncludeGroupDetailTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "includeGroupDetail"));
                                 
                                         elementList.add(localIncludeGroupDetail==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localIncludeGroupDetail));
                                    } if (localParamName0Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramName0"));
                                 
                                         elementList.add(localParamName0==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName0));
                                    } if (localParamValue0Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramValue0"));
                                 
                                         elementList.add(localParamValue0==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue0));
                                    } if (localParamName1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramName1"));
                                 
                                         elementList.add(localParamName1==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName1));
                                    } if (localParamValue1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramValue1"));
                                 
                                         elementList.add(localParamValue1==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue1));
                                    } if (localDisallowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "disallowed"));
                                 
                                         elementList.add(localDisallowed==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDisallowed));
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
        public static AssignPermissionsLite parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            AssignPermissionsLite object =
                new AssignPermissionsLite();

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
                    
                            if (!"assignPermissionsLite".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (AssignPermissionsLite)edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                 
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","clientVersion").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","permissionType").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","permissionDefNameName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPermissionDefNameName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","permissionDefNameId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPermissionDefNameId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","permissionAssignOperation").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPermissionAssignOperation(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","assignmentNotes").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignmentNotes(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","assignmentEnabledTime").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignmentEnabledTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","assignmentDisabledTime").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignmentDisabledTime(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","delegatable").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDelegatable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","wsAttributeAssignId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setWsAttributeAssignId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","roleName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRoleName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","roleId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRoleId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","subjectRoleName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectRoleName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","subjectRoleId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectRoleId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","subjectRoleSubjectId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectRoleSubjectId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","subjectRoleSubjectSourceId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectRoleSubjectSourceId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","subjectRoleSubjectIdentifier").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectRoleSubjectIdentifier(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","action").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAction(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActAsSubjectId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectSourceId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActAsSubjectSourceId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectIdentifier").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setActAsSubjectIdentifier(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","includeSubjectDetail").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectAttributeNames(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","includeGroupDetail").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","paramName0").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParamName0(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","paramValue0").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParamValue0(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","paramName1").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParamName1(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","paramValue1").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setParamValue1(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","disallowed").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDisallowed(
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
           
          