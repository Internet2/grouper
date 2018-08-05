
/**
 * AttributeDefSaveLite.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_4.xsd;
            

            /**
            *  AttributeDefSaveLite bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class AttributeDefSaveLite
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                "attributeDefSaveLite",
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
                        * field for AttributeDefLookupUuid
                        */

                        
                                    protected java.lang.String localAttributeDefLookupUuid ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefLookupUuidTracker = false ;

                           public boolean isAttributeDefLookupUuidSpecified(){
                               return localAttributeDefLookupUuidTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeDefLookupUuid(){
                               return localAttributeDefLookupUuid;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeDefLookupUuid
                               */
                               public void setAttributeDefLookupUuid(java.lang.String param){
                            localAttributeDefLookupUuidTracker = true;
                                   
                                            this.localAttributeDefLookupUuid=param;
                                    

                               }
                            

                        /**
                        * field for AttributeDefLookupName
                        */

                        
                                    protected java.lang.String localAttributeDefLookupName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefLookupNameTracker = false ;

                           public boolean isAttributeDefLookupNameSpecified(){
                               return localAttributeDefLookupNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeDefLookupName(){
                               return localAttributeDefLookupName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeDefLookupName
                               */
                               public void setAttributeDefLookupName(java.lang.String param){
                            localAttributeDefLookupNameTracker = true;
                                   
                                            this.localAttributeDefLookupName=param;
                                    

                               }
                            

                        /**
                        * field for UuidOfAttributeDef
                        */

                        
                                    protected java.lang.String localUuidOfAttributeDef ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUuidOfAttributeDefTracker = false ;

                           public boolean isUuidOfAttributeDefSpecified(){
                               return localUuidOfAttributeDefTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUuidOfAttributeDef(){
                               return localUuidOfAttributeDef;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UuidOfAttributeDef
                               */
                               public void setUuidOfAttributeDef(java.lang.String param){
                            localUuidOfAttributeDefTracker = true;
                                   
                                            this.localUuidOfAttributeDef=param;
                                    

                               }
                            

                        /**
                        * field for NameOfAttributeDef
                        */

                        
                                    protected java.lang.String localNameOfAttributeDef ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNameOfAttributeDefTracker = false ;

                           public boolean isNameOfAttributeDefSpecified(){
                               return localNameOfAttributeDefTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getNameOfAttributeDef(){
                               return localNameOfAttributeDef;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param NameOfAttributeDef
                               */
                               public void setNameOfAttributeDef(java.lang.String param){
                            localNameOfAttributeDefTracker = true;
                                   
                                            this.localNameOfAttributeDef=param;
                                    

                               }
                            

                        /**
                        * field for AssignToAttributeDef
                        */

                        
                                    protected java.lang.String localAssignToAttributeDef ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToAttributeDefTracker = false ;

                           public boolean isAssignToAttributeDefSpecified(){
                               return localAssignToAttributeDefTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToAttributeDef(){
                               return localAssignToAttributeDef;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToAttributeDef
                               */
                               public void setAssignToAttributeDef(java.lang.String param){
                            localAssignToAttributeDefTracker = true;
                                   
                                            this.localAssignToAttributeDef=param;
                                    

                               }
                            

                        /**
                        * field for AssignToAttributeDefAssignment
                        */

                        
                                    protected java.lang.String localAssignToAttributeDefAssignment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToAttributeDefAssignmentTracker = false ;

                           public boolean isAssignToAttributeDefAssignmentSpecified(){
                               return localAssignToAttributeDefAssignmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToAttributeDefAssignment(){
                               return localAssignToAttributeDefAssignment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToAttributeDefAssignment
                               */
                               public void setAssignToAttributeDefAssignment(java.lang.String param){
                            localAssignToAttributeDefAssignmentTracker = true;
                                   
                                            this.localAssignToAttributeDefAssignment=param;
                                    

                               }
                            

                        /**
                        * field for AssignToEffectiveMembership
                        */

                        
                                    protected java.lang.String localAssignToEffectiveMembership ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToEffectiveMembershipTracker = false ;

                           public boolean isAssignToEffectiveMembershipSpecified(){
                               return localAssignToEffectiveMembershipTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToEffectiveMembership(){
                               return localAssignToEffectiveMembership;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToEffectiveMembership
                               */
                               public void setAssignToEffectiveMembership(java.lang.String param){
                            localAssignToEffectiveMembershipTracker = true;
                                   
                                            this.localAssignToEffectiveMembership=param;
                                    

                               }
                            

                        /**
                        * field for AssignToEffectiveMembershipAssignment
                        */

                        
                                    protected java.lang.String localAssignToEffectiveMembershipAssignment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToEffectiveMembershipAssignmentTracker = false ;

                           public boolean isAssignToEffectiveMembershipAssignmentSpecified(){
                               return localAssignToEffectiveMembershipAssignmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToEffectiveMembershipAssignment(){
                               return localAssignToEffectiveMembershipAssignment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToEffectiveMembershipAssignment
                               */
                               public void setAssignToEffectiveMembershipAssignment(java.lang.String param){
                            localAssignToEffectiveMembershipAssignmentTracker = true;
                                   
                                            this.localAssignToEffectiveMembershipAssignment=param;
                                    

                               }
                            

                        /**
                        * field for AssignToGroup
                        */

                        
                                    protected java.lang.String localAssignToGroup ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToGroupTracker = false ;

                           public boolean isAssignToGroupSpecified(){
                               return localAssignToGroupTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToGroup(){
                               return localAssignToGroup;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToGroup
                               */
                               public void setAssignToGroup(java.lang.String param){
                            localAssignToGroupTracker = true;
                                   
                                            this.localAssignToGroup=param;
                                    

                               }
                            

                        /**
                        * field for AssignToGroupAssignment
                        */

                        
                                    protected java.lang.String localAssignToGroupAssignment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToGroupAssignmentTracker = false ;

                           public boolean isAssignToGroupAssignmentSpecified(){
                               return localAssignToGroupAssignmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToGroupAssignment(){
                               return localAssignToGroupAssignment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToGroupAssignment
                               */
                               public void setAssignToGroupAssignment(java.lang.String param){
                            localAssignToGroupAssignmentTracker = true;
                                   
                                            this.localAssignToGroupAssignment=param;
                                    

                               }
                            

                        /**
                        * field for AssignToImmediateMembership
                        */

                        
                                    protected java.lang.String localAssignToImmediateMembership ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToImmediateMembershipTracker = false ;

                           public boolean isAssignToImmediateMembershipSpecified(){
                               return localAssignToImmediateMembershipTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToImmediateMembership(){
                               return localAssignToImmediateMembership;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToImmediateMembership
                               */
                               public void setAssignToImmediateMembership(java.lang.String param){
                            localAssignToImmediateMembershipTracker = true;
                                   
                                            this.localAssignToImmediateMembership=param;
                                    

                               }
                            

                        /**
                        * field for AssignToImmediateMembershipAssignment
                        */

                        
                                    protected java.lang.String localAssignToImmediateMembershipAssignment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToImmediateMembershipAssignmentTracker = false ;

                           public boolean isAssignToImmediateMembershipAssignmentSpecified(){
                               return localAssignToImmediateMembershipAssignmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToImmediateMembershipAssignment(){
                               return localAssignToImmediateMembershipAssignment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToImmediateMembershipAssignment
                               */
                               public void setAssignToImmediateMembershipAssignment(java.lang.String param){
                            localAssignToImmediateMembershipAssignmentTracker = true;
                                   
                                            this.localAssignToImmediateMembershipAssignment=param;
                                    

                               }
                            

                        /**
                        * field for AssignToMember
                        */

                        
                                    protected java.lang.String localAssignToMember ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToMemberTracker = false ;

                           public boolean isAssignToMemberSpecified(){
                               return localAssignToMemberTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToMember(){
                               return localAssignToMember;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToMember
                               */
                               public void setAssignToMember(java.lang.String param){
                            localAssignToMemberTracker = true;
                                   
                                            this.localAssignToMember=param;
                                    

                               }
                            

                        /**
                        * field for AssignToMemberAssignment
                        */

                        
                                    protected java.lang.String localAssignToMemberAssignment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToMemberAssignmentTracker = false ;

                           public boolean isAssignToMemberAssignmentSpecified(){
                               return localAssignToMemberAssignmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToMemberAssignment(){
                               return localAssignToMemberAssignment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToMemberAssignment
                               */
                               public void setAssignToMemberAssignment(java.lang.String param){
                            localAssignToMemberAssignmentTracker = true;
                                   
                                            this.localAssignToMemberAssignment=param;
                                    

                               }
                            

                        /**
                        * field for AssignToStem
                        */

                        
                                    protected java.lang.String localAssignToStem ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToStemTracker = false ;

                           public boolean isAssignToStemSpecified(){
                               return localAssignToStemTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToStem(){
                               return localAssignToStem;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToStem
                               */
                               public void setAssignToStem(java.lang.String param){
                            localAssignToStemTracker = true;
                                   
                                            this.localAssignToStem=param;
                                    

                               }
                            

                        /**
                        * field for AssignToStemAssignment
                        */

                        
                                    protected java.lang.String localAssignToStemAssignment ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAssignToStemAssignmentTracker = false ;

                           public boolean isAssignToStemAssignmentSpecified(){
                               return localAssignToStemAssignmentTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAssignToStemAssignment(){
                               return localAssignToStemAssignment;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AssignToStemAssignment
                               */
                               public void setAssignToStemAssignment(java.lang.String param){
                            localAssignToStemAssignmentTracker = true;
                                   
                                            this.localAssignToStemAssignment=param;
                                    

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
                        * field for MultiAssignable
                        */

                        
                                    protected java.lang.String localMultiAssignable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMultiAssignableTracker = false ;

                           public boolean isMultiAssignableSpecified(){
                               return localMultiAssignableTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getMultiAssignable(){
                               return localMultiAssignable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MultiAssignable
                               */
                               public void setMultiAssignable(java.lang.String param){
                            localMultiAssignableTracker = true;
                                   
                                            this.localMultiAssignable=param;
                                    

                               }
                            

                        /**
                        * field for MultiValued
                        */

                        
                                    protected java.lang.String localMultiValued ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMultiValuedTracker = false ;

                           public boolean isMultiValuedSpecified(){
                               return localMultiValuedTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getMultiValued(){
                               return localMultiValued;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MultiValued
                               */
                               public void setMultiValued(java.lang.String param){
                            localMultiValuedTracker = true;
                                   
                                            this.localMultiValued=param;
                                    

                               }
                            

                        /**
                        * field for ValueType
                        */

                        
                                    protected java.lang.String localValueType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localValueTypeTracker = false ;

                           public boolean isValueTypeSpecified(){
                               return localValueTypeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getValueType(){
                               return localValueType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ValueType
                               */
                               public void setValueType(java.lang.String param){
                            localValueTypeTracker = true;
                                   
                                            this.localValueType=param;
                                    

                               }
                            

                        /**
                        * field for Description
                        */

                        
                                    protected java.lang.String localDescription ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDescriptionTracker = false ;

                           public boolean isDescriptionSpecified(){
                               return localDescriptionTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDescription(){
                               return localDescription;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Description
                               */
                               public void setDescription(java.lang.String param){
                            localDescriptionTracker = true;
                                   
                                            this.localDescription=param;
                                    

                               }
                            

                        /**
                        * field for SaveMode
                        */

                        
                                    protected java.lang.String localSaveMode ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSaveModeTracker = false ;

                           public boolean isSaveModeSpecified(){
                               return localSaveModeTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSaveMode(){
                               return localSaveMode;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SaveMode
                               */
                               public void setSaveMode(java.lang.String param){
                            localSaveModeTracker = true;
                                   
                                            this.localSaveMode=param;
                                    

                               }
                            

                        /**
                        * field for CreateParentStemsIfNotExist
                        */

                        
                                    protected java.lang.String localCreateParentStemsIfNotExist ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCreateParentStemsIfNotExistTracker = false ;

                           public boolean isCreateParentStemsIfNotExistSpecified(){
                               return localCreateParentStemsIfNotExistTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCreateParentStemsIfNotExist(){
                               return localCreateParentStemsIfNotExist;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CreateParentStemsIfNotExist
                               */
                               public void setCreateParentStemsIfNotExist(java.lang.String param){
                            localCreateParentStemsIfNotExistTracker = true;
                                   
                                            this.localCreateParentStemsIfNotExist=param;
                                    

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

                           public boolean isActAsSubjectIdSpecified(){
                               return localActAsSubjectIdTracker;
                           }

                           

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
                            localActAsSubjectIdTracker = true;
                                   
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

                           public boolean isActAsSubjectSourceIdSpecified(){
                               return localActAsSubjectSourceIdTracker;
                           }

                           

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
                            localActAsSubjectSourceIdTracker = true;
                                   
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

                           public boolean isActAsSubjectIdentifierSpecified(){
                               return localActAsSubjectIdentifierTracker;
                           }

                           

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
                            localActAsSubjectIdentifierTracker = true;
                                   
                                            this.localActAsSubjectIdentifier=param;
                                    

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

                           public boolean isParamName0Specified(){
                               return localParamName0Tracker;
                           }

                           

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
                            localParamName0Tracker = true;
                                   
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

                           public boolean isParamValue0Specified(){
                               return localParamValue0Tracker;
                           }

                           

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
                            localParamValue0Tracker = true;
                                   
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

                           public boolean isParamName1Specified(){
                               return localParamName1Tracker;
                           }

                           

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
                            localParamName1Tracker = true;
                                   
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

                           public boolean isParamValue1Specified(){
                               return localParamValue1Tracker;
                           }

                           

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
                            localParamValue1Tracker = true;
                                   
                                            this.localParamValue1=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":attributeDefSaveLite",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "attributeDefSaveLite",
                           xmlWriter);
                   }

               
                   }
                if (localClientVersionTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "clientVersion", xmlWriter);
                             

                                          if (localClientVersion==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localClientVersion);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefLookupUuidTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefLookupUuid", xmlWriter);
                             

                                          if (localAttributeDefLookupUuid==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefLookupUuid);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefLookupNameTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefLookupName", xmlWriter);
                             

                                          if (localAttributeDefLookupName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefLookupName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUuidOfAttributeDefTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "uuidOfAttributeDef", xmlWriter);
                             

                                          if (localUuidOfAttributeDef==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUuidOfAttributeDef);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localNameOfAttributeDefTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "nameOfAttributeDef", xmlWriter);
                             

                                          if (localNameOfAttributeDef==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localNameOfAttributeDef);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToAttributeDefTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToAttributeDef", xmlWriter);
                             

                                          if (localAssignToAttributeDef==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToAttributeDef);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToAttributeDefAssignmentTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToAttributeDefAssignment", xmlWriter);
                             

                                          if (localAssignToAttributeDefAssignment==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToAttributeDefAssignment);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToEffectiveMembershipTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToEffectiveMembership", xmlWriter);
                             

                                          if (localAssignToEffectiveMembership==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToEffectiveMembership);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToEffectiveMembershipAssignmentTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToEffectiveMembershipAssignment", xmlWriter);
                             

                                          if (localAssignToEffectiveMembershipAssignment==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToEffectiveMembershipAssignment);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToGroupTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToGroup", xmlWriter);
                             

                                          if (localAssignToGroup==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToGroup);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToGroupAssignmentTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToGroupAssignment", xmlWriter);
                             

                                          if (localAssignToGroupAssignment==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToGroupAssignment);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToImmediateMembershipTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToImmediateMembership", xmlWriter);
                             

                                          if (localAssignToImmediateMembership==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToImmediateMembership);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToImmediateMembershipAssignmentTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToImmediateMembershipAssignment", xmlWriter);
                             

                                          if (localAssignToImmediateMembershipAssignment==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToImmediateMembershipAssignment);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToMemberTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToMember", xmlWriter);
                             

                                          if (localAssignToMember==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToMember);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToMemberAssignmentTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToMemberAssignment", xmlWriter);
                             

                                          if (localAssignToMemberAssignment==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToMemberAssignment);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToStemTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToStem", xmlWriter);
                             

                                          if (localAssignToStem==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToStem);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAssignToStemAssignmentTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "assignToStemAssignment", xmlWriter);
                             

                                          if (localAssignToStemAssignment==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAssignToStemAssignment);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefTypeTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefType", xmlWriter);
                             

                                          if (localAttributeDefType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMultiAssignableTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "multiAssignable", xmlWriter);
                             

                                          if (localMultiAssignable==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localMultiAssignable);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMultiValuedTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "multiValued", xmlWriter);
                             

                                          if (localMultiValued==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localMultiValued);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localValueTypeTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "valueType", xmlWriter);
                             

                                          if (localValueType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localValueType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDescriptionTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "description", xmlWriter);
                             

                                          if (localDescription==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDescription);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSaveModeTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "saveMode", xmlWriter);
                             

                                          if (localSaveMode==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSaveMode);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localCreateParentStemsIfNotExistTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "createParentStemsIfNotExist", xmlWriter);
                             

                                          if (localCreateParentStemsIfNotExist==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCreateParentStemsIfNotExist);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectIdTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectId", xmlWriter);
                             

                                          if (localActAsSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectSourceIdTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectSourceId", xmlWriter);
                             

                                          if (localActAsSubjectSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localActAsSubjectIdentifierTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "actAsSubjectIdentifier", xmlWriter);
                             

                                          if (localActAsSubjectIdentifier==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localActAsSubjectIdentifier);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamName0Tracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramName0", xmlWriter);
                             

                                          if (localParamName0==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamName0);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamValue0Tracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramValue0", xmlWriter);
                             

                                          if (localParamValue0==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamValue0);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamName1Tracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramName1", xmlWriter);
                             

                                          if (localParamName1==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamName1);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localParamValue1Tracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "paramValue1", xmlWriter);
                             

                                          if (localParamValue1==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localParamValue1);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd")){
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
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "clientVersion"));
                                 
                                         elementList.add(localClientVersion==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localClientVersion));
                                    } if (localAttributeDefLookupUuidTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefLookupUuid"));
                                 
                                         elementList.add(localAttributeDefLookupUuid==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefLookupUuid));
                                    } if (localAttributeDefLookupNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefLookupName"));
                                 
                                         elementList.add(localAttributeDefLookupName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefLookupName));
                                    } if (localUuidOfAttributeDefTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "uuidOfAttributeDef"));
                                 
                                         elementList.add(localUuidOfAttributeDef==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUuidOfAttributeDef));
                                    } if (localNameOfAttributeDefTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "nameOfAttributeDef"));
                                 
                                         elementList.add(localNameOfAttributeDef==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localNameOfAttributeDef));
                                    } if (localAssignToAttributeDefTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToAttributeDef"));
                                 
                                         elementList.add(localAssignToAttributeDef==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToAttributeDef));
                                    } if (localAssignToAttributeDefAssignmentTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToAttributeDefAssignment"));
                                 
                                         elementList.add(localAssignToAttributeDefAssignment==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToAttributeDefAssignment));
                                    } if (localAssignToEffectiveMembershipTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToEffectiveMembership"));
                                 
                                         elementList.add(localAssignToEffectiveMembership==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToEffectiveMembership));
                                    } if (localAssignToEffectiveMembershipAssignmentTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToEffectiveMembershipAssignment"));
                                 
                                         elementList.add(localAssignToEffectiveMembershipAssignment==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToEffectiveMembershipAssignment));
                                    } if (localAssignToGroupTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToGroup"));
                                 
                                         elementList.add(localAssignToGroup==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToGroup));
                                    } if (localAssignToGroupAssignmentTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToGroupAssignment"));
                                 
                                         elementList.add(localAssignToGroupAssignment==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToGroupAssignment));
                                    } if (localAssignToImmediateMembershipTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToImmediateMembership"));
                                 
                                         elementList.add(localAssignToImmediateMembership==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToImmediateMembership));
                                    } if (localAssignToImmediateMembershipAssignmentTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToImmediateMembershipAssignment"));
                                 
                                         elementList.add(localAssignToImmediateMembershipAssignment==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToImmediateMembershipAssignment));
                                    } if (localAssignToMemberTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToMember"));
                                 
                                         elementList.add(localAssignToMember==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToMember));
                                    } if (localAssignToMemberAssignmentTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToMemberAssignment"));
                                 
                                         elementList.add(localAssignToMemberAssignment==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToMemberAssignment));
                                    } if (localAssignToStemTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToStem"));
                                 
                                         elementList.add(localAssignToStem==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToStem));
                                    } if (localAssignToStemAssignmentTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "assignToStemAssignment"));
                                 
                                         elementList.add(localAssignToStemAssignment==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAssignToStemAssignment));
                                    } if (localAttributeDefTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefType"));
                                 
                                         elementList.add(localAttributeDefType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefType));
                                    } if (localMultiAssignableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "multiAssignable"));
                                 
                                         elementList.add(localMultiAssignable==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMultiAssignable));
                                    } if (localMultiValuedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "multiValued"));
                                 
                                         elementList.add(localMultiValued==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMultiValued));
                                    } if (localValueTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "valueType"));
                                 
                                         elementList.add(localValueType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localValueType));
                                    } if (localDescriptionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "description"));
                                 
                                         elementList.add(localDescription==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDescription));
                                    } if (localSaveModeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "saveMode"));
                                 
                                         elementList.add(localSaveMode==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSaveMode));
                                    } if (localCreateParentStemsIfNotExistTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "createParentStemsIfNotExist"));
                                 
                                         elementList.add(localCreateParentStemsIfNotExist==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreateParentStemsIfNotExist));
                                    } if (localActAsSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectId"));
                                 
                                         elementList.add(localActAsSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectId));
                                    } if (localActAsSubjectSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectSourceId"));
                                 
                                         elementList.add(localActAsSubjectSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectSourceId));
                                    } if (localActAsSubjectIdentifierTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "actAsSubjectIdentifier"));
                                 
                                         elementList.add(localActAsSubjectIdentifier==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localActAsSubjectIdentifier));
                                    } if (localParamName0Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramName0"));
                                 
                                         elementList.add(localParamName0==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName0));
                                    } if (localParamValue0Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramValue0"));
                                 
                                         elementList.add(localParamValue0==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue0));
                                    } if (localParamName1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramName1"));
                                 
                                         elementList.add(localParamName1==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamName1));
                                    } if (localParamValue1Tracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "paramValue1"));
                                 
                                         elementList.add(localParamValue1==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localParamValue1));
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
        public static AttributeDefSaveLite parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            AttributeDefSaveLite object =
                new AttributeDefSaveLite();

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
                    
                            if (!"attributeDefSaveLite".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (AttributeDefSaveLite)edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","clientVersion").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","attributeDefLookupUuid").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeDefLookupUuid(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","attributeDefLookupName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeDefLookupName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","uuidOfAttributeDef").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUuidOfAttributeDef(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","nameOfAttributeDef").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setNameOfAttributeDef(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToAttributeDef").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToAttributeDef(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToAttributeDefAssignment").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToAttributeDefAssignment(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToEffectiveMembership").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToEffectiveMembership(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToEffectiveMembershipAssignment").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToEffectiveMembershipAssignment(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToGroup").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToGroup(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToGroupAssignment").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToGroupAssignment(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToImmediateMembership").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToImmediateMembership(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToImmediateMembershipAssignment").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToImmediateMembershipAssignment(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToMember").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToMember(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToMemberAssignment").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToMemberAssignment(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToStem").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToStem(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","assignToStemAssignment").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAssignToStemAssignment(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","attributeDefType").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","multiAssignable").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMultiAssignable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","multiValued").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMultiValued(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","valueType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setValueType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","description").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDescription(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","saveMode").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSaveMode(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","createParentStemsIfNotExist").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCreateParentStemsIfNotExist(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectId").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectSourceId").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","actAsSubjectIdentifier").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","paramName0").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","paramValue0").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","paramName1").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","paramValue1").equals(reader.getName())){
                                
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
           
    