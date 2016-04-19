/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/

/**
 * WsPermissionAssign.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_3.xsd;
            

            /**
            *  WsPermissionAssign bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class WsPermissionAssign
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = WsPermissionAssign
                Namespace URI = http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for Action
                        */

                        
                                    protected java.lang.String localAction ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localActionTracker = false ;

                           public boolean isActionSpecified(){
                               return localActionTracker;
                           }

                           

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
                            localActionTracker = true;
                                   
                                            this.localAction=param;
                                    

                               }
                            

                        /**
                        * field for AllowedOverall
                        */

                        
                                    protected java.lang.String localAllowedOverall ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAllowedOverallTracker = false ;

                           public boolean isAllowedOverallSpecified(){
                               return localAllowedOverallTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAllowedOverall(){
                               return localAllowedOverall;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AllowedOverall
                               */
                               public void setAllowedOverall(java.lang.String param){
                            localAllowedOverallTracker = true;
                                   
                                            this.localAllowedOverall=param;
                                    

                               }
                            

                        /**
                        * field for AttributeAssignId
                        */

                        
                                    protected java.lang.String localAttributeAssignId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeAssignIdTracker = false ;

                           public boolean isAttributeAssignIdSpecified(){
                               return localAttributeAssignIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeAssignId(){
                               return localAttributeAssignId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeAssignId
                               */
                               public void setAttributeAssignId(java.lang.String param){
                            localAttributeAssignIdTracker = true;
                                   
                                            this.localAttributeAssignId=param;
                                    

                               }
                            

                        /**
                        * field for AttributeDefId
                        */

                        
                                    protected java.lang.String localAttributeDefId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefIdTracker = false ;

                           public boolean isAttributeDefIdSpecified(){
                               return localAttributeDefIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeDefId(){
                               return localAttributeDefId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeDefId
                               */
                               public void setAttributeDefId(java.lang.String param){
                            localAttributeDefIdTracker = true;
                                   
                                            this.localAttributeDefId=param;
                                    

                               }
                            

                        /**
                        * field for AttributeDefName
                        */

                        
                                    protected java.lang.String localAttributeDefName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefNameTracker = false ;

                           public boolean isAttributeDefNameSpecified(){
                               return localAttributeDefNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeDefName(){
                               return localAttributeDefName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeDefName
                               */
                               public void setAttributeDefName(java.lang.String param){
                            localAttributeDefNameTracker = true;
                                   
                                            this.localAttributeDefName=param;
                                    

                               }
                            

                        /**
                        * field for AttributeDefNameId
                        */

                        
                                    protected java.lang.String localAttributeDefNameId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefNameIdTracker = false ;

                           public boolean isAttributeDefNameIdSpecified(){
                               return localAttributeDefNameIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeDefNameId(){
                               return localAttributeDefNameId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeDefNameId
                               */
                               public void setAttributeDefNameId(java.lang.String param){
                            localAttributeDefNameIdTracker = true;
                                   
                                            this.localAttributeDefNameId=param;
                                    

                               }
                            

                        /**
                        * field for AttributeDefNameName
                        */

                        
                                    protected java.lang.String localAttributeDefNameName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAttributeDefNameNameTracker = false ;

                           public boolean isAttributeDefNameNameSpecified(){
                               return localAttributeDefNameNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAttributeDefNameName(){
                               return localAttributeDefNameName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param AttributeDefNameName
                               */
                               public void setAttributeDefNameName(java.lang.String param){
                            localAttributeDefNameNameTracker = true;
                                   
                                            this.localAttributeDefNameName=param;
                                    

                               }
                            

                        /**
                        * field for Detail
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionAssignDetail localDetail ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDetailTracker = false ;

                           public boolean isDetailSpecified(){
                               return localDetailTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionAssignDetail
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionAssignDetail getDetail(){
                               return localDetail;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Detail
                               */
                               public void setDetail(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionAssignDetail param){
                            localDetailTracker = true;
                                   
                                            this.localDetail=param;
                                    

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

                           public boolean isDisallowedSpecified(){
                               return localDisallowedTracker;
                           }

                           

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
                            localDisallowedTracker = true;
                                   
                                            this.localDisallowed=param;
                                    

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
                        * field for Limits
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[] localLimits ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLimitsTracker = false ;

                           public boolean isLimitsSpecified(){
                               return localLimitsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[] getLimits(){
                               return localLimits;
                           }

                           
                        


                               
                              /**
                               * validate the array for Limits
                               */
                              protected void validateLimits(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Limits
                              */
                              public void setLimits(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[] param){
                              
                                   validateLimits(param);

                               localLimitsTracker = true;
                                      
                                      this.localLimits=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit
                             */
                             public void addLimits(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit param){
                                   if (localLimits == null){
                                   localLimits = new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[]{};
                                   }

                            
                                 //update the setting tracker
                                localLimitsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localLimits);
                               list.add(param);
                               this.localLimits =
                             (edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[list.size()]);

                             }
                             

                        /**
                        * field for MembershipId
                        */

                        
                                    protected java.lang.String localMembershipId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMembershipIdTracker = false ;

                           public boolean isMembershipIdSpecified(){
                               return localMembershipIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getMembershipId(){
                               return localMembershipId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MembershipId
                               */
                               public void setMembershipId(java.lang.String param){
                            localMembershipIdTracker = true;
                                   
                                            this.localMembershipId=param;
                                    

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

                           public boolean isPermissionTypeSpecified(){
                               return localPermissionTypeTracker;
                           }

                           

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
                            localPermissionTypeTracker = true;
                                   
                                            this.localPermissionType=param;
                                    

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

                           public boolean isRoleIdSpecified(){
                               return localRoleIdTracker;
                           }

                           

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
                            localRoleIdTracker = true;
                                   
                                            this.localRoleId=param;
                                    

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

                           public boolean isRoleNameSpecified(){
                               return localRoleNameTracker;
                           }

                           

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
                            localRoleNameTracker = true;
                                   
                                            this.localRoleName=param;
                                    

                               }
                            

                        /**
                        * field for SourceId
                        */

                        
                                    protected java.lang.String localSourceId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSourceIdTracker = false ;

                           public boolean isSourceIdSpecified(){
                               return localSourceIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSourceId(){
                               return localSourceId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SourceId
                               */
                               public void setSourceId(java.lang.String param){
                            localSourceIdTracker = true;
                                   
                                            this.localSourceId=param;
                                    

                               }
                            

                        /**
                        * field for SubjectId
                        */

                        
                                    protected java.lang.String localSubjectId ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localSubjectIdTracker = false ;

                           public boolean isSubjectIdSpecified(){
                               return localSubjectIdTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getSubjectId(){
                               return localSubjectId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param SubjectId
                               */
                               public void setSubjectId(java.lang.String param){
                            localSubjectIdTracker = true;
                                   
                                            this.localSubjectId=param;
                                    

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
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName);
               return factory.createOMElement(dataSource,parentQName);
            
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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":WsPermissionAssign",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "WsPermissionAssign",
                           xmlWriter);
                   }

               
                   }
                if (localActionTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "action", xmlWriter);
                             

                                          if (localAction==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAction);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAllowedOverallTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "allowedOverall", xmlWriter);
                             

                                          if (localAllowedOverall==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAllowedOverall);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeAssignIdTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeAssignId", xmlWriter);
                             

                                          if (localAttributeAssignId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeAssignId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefIdTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefId", xmlWriter);
                             

                                          if (localAttributeDefId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefNameTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefName", xmlWriter);
                             

                                          if (localAttributeDefName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefNameIdTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefNameId", xmlWriter);
                             

                                          if (localAttributeDefNameId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefNameId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localAttributeDefNameNameTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "attributeDefNameName", xmlWriter);
                             

                                          if (localAttributeDefNameName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAttributeDefNameName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDetailTracker){
                                    if (localDetail==null){

                                        writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "detail", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localDetail.serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","detail"),
                                        xmlWriter);
                                    }
                                } if (localDisallowedTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "disallowed", xmlWriter);
                             

                                          if (localDisallowed==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDisallowed);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localEnabledTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "enabled", xmlWriter);
                             

                                          if (localEnabled==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localEnabled);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localLimitsTracker){
                                       if (localLimits!=null){
                                            for (int i = 0;i < localLimits.length;i++){
                                                if (localLimits[i] != null){
                                                 localLimits[i].serialize(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","limits"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "limits", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd", "limits", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localMembershipIdTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "membershipId", xmlWriter);
                             

                                          if (localMembershipId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localMembershipId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPermissionTypeTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "permissionType", xmlWriter);
                             

                                          if (localPermissionType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPermissionType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRoleIdTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "roleId", xmlWriter);
                             

                                          if (localRoleId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRoleId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRoleNameTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "roleName", xmlWriter);
                             

                                          if (localRoleName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRoleName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSourceIdTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "sourceId", xmlWriter);
                             

                                          if (localSourceId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSourceId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localSubjectIdTracker){
                                    namespace = "http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "subjectId", xmlWriter);
                             

                                          if (localSubjectId==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localSubjectId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd")){
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

                 if (localActionTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "action"));
                                 
                                         elementList.add(localAction==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAction));
                                    } if (localAllowedOverallTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "allowedOverall"));
                                 
                                         elementList.add(localAllowedOverall==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAllowedOverall));
                                    } if (localAttributeAssignIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeAssignId"));
                                 
                                         elementList.add(localAttributeAssignId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeAssignId));
                                    } if (localAttributeDefIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefId"));
                                 
                                         elementList.add(localAttributeDefId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefId));
                                    } if (localAttributeDefNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefName"));
                                 
                                         elementList.add(localAttributeDefName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefName));
                                    } if (localAttributeDefNameIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefNameId"));
                                 
                                         elementList.add(localAttributeDefNameId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefNameId));
                                    } if (localAttributeDefNameNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "attributeDefNameName"));
                                 
                                         elementList.add(localAttributeDefNameName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAttributeDefNameName));
                                    } if (localDetailTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "detail"));
                            
                            
                                    elementList.add(localDetail==null?null:
                                    localDetail);
                                } if (localDisallowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "disallowed"));
                                 
                                         elementList.add(localDisallowed==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDisallowed));
                                    } if (localEnabledTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "enabled"));
                                 
                                         elementList.add(localEnabled==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEnabled));
                                    } if (localLimitsTracker){
                             if (localLimits!=null) {
                                 for (int i = 0;i < localLimits.length;i++){

                                    if (localLimits[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "limits"));
                                         elementList.add(localLimits[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "limits"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "limits"));
                                        elementList.add(localLimits);
                                    
                             }

                        } if (localMembershipIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "membershipId"));
                                 
                                         elementList.add(localMembershipId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMembershipId));
                                    } if (localPermissionTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "permissionType"));
                                 
                                         elementList.add(localPermissionType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPermissionType));
                                    } if (localRoleIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "roleId"));
                                 
                                         elementList.add(localRoleId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRoleId));
                                    } if (localRoleNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "roleName"));
                                 
                                         elementList.add(localRoleName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRoleName));
                                    } if (localSourceIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "sourceId"));
                                 
                                         elementList.add(localSourceId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSourceId));
                                    } if (localSubjectIdTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "subjectId"));
                                 
                                         elementList.add(localSubjectId==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectId));
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
        public static WsPermissionAssign parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            WsPermissionAssign object =
                new WsPermissionAssign();

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
                    
                            if (!"WsPermissionAssign".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (WsPermissionAssign)edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list11 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","action").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","allowedOverall").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAllowedOverall(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","attributeAssignId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeAssignId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","attributeDefId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeDefId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","attributeDefName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeDefName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","attributeDefNameId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeDefNameId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","attributeDefNameName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAttributeDefNameName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","detail").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setDetail(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setDetail(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionAssignDetail.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","disallowed").equals(reader.getName())){
                                
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
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","enabled").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","limits").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list11.add(null);
                                                              reader.next();
                                                          } else {
                                                        list11.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","limits").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list11.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list11.add(edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone11 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setLimits((edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_3.xsd.WsPermissionLimit.class,
                                                                list11));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","membershipId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMembershipId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","permissionType").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","roleId").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","roleName").equals(reader.getName())){
                                
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
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","sourceId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSourceId(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_3.ws.grouper.middleware.internet2.edu/xsd","subjectId").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setSubjectId(
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
           
    