
/**
 * WsAssignPermissionsResults.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_5.xsd;
            

            /**
            *  WsAssignPermissionsResults bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class WsAssignPermissionsResults
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = WsAssignPermissionsResults
                Namespace URI = http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for ResponseMetadata
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResponseMeta localResponseMetadata ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localResponseMetadataTracker = false ;

                           public boolean isResponseMetadataSpecified(){
                               return localResponseMetadataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResponseMeta
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResponseMeta getResponseMetadata(){
                               return localResponseMetadata;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ResponseMetadata
                               */
                               public void setResponseMetadata(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResponseMeta param){
                            localResponseMetadataTracker = true;
                                   
                                            this.localResponseMetadata=param;
                                    

                               }
                            

                        /**
                        * field for ResultMetadata
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResultMeta localResultMetadata ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localResultMetadataTracker = false ;

                           public boolean isResultMetadataSpecified(){
                               return localResultMetadataTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResultMeta
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResultMeta getResultMetadata(){
                               return localResultMetadata;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ResultMetadata
                               */
                               public void setResultMetadata(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResultMeta param){
                            localResultMetadataTracker = true;
                                   
                                            this.localResultMetadata=param;
                                    

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
                        * field for WsAssignPermissionResults
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[] localWsAssignPermissionResults ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAssignPermissionResultsTracker = false ;

                           public boolean isWsAssignPermissionResultsSpecified(){
                               return localWsAssignPermissionResultsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[] getWsAssignPermissionResults(){
                               return localWsAssignPermissionResults;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAssignPermissionResults
                               */
                              protected void validateWsAssignPermissionResults(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAssignPermissionResults
                              */
                              public void setWsAssignPermissionResults(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[] param){
                              
                                   validateWsAssignPermissionResults(param);

                               localWsAssignPermissionResultsTracker = true;
                                      
                                      this.localWsAssignPermissionResults=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult
                             */
                             public void addWsAssignPermissionResults(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult param){
                                   if (localWsAssignPermissionResults == null){
                                   localWsAssignPermissionResults = new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAssignPermissionResultsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAssignPermissionResults);
                               list.add(param);
                               this.localWsAssignPermissionResults =
                             (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[list.size()]);

                             }
                             

                        /**
                        * field for WsAttributeDefNames
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[] localWsAttributeDefNames ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefNamesTracker = false ;

                           public boolean isWsAttributeDefNamesSpecified(){
                               return localWsAttributeDefNamesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[] getWsAttributeDefNames(){
                               return localWsAttributeDefNames;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAttributeDefNames
                               */
                              protected void validateWsAttributeDefNames(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAttributeDefNames
                              */
                              public void setWsAttributeDefNames(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[] param){
                              
                                   validateWsAttributeDefNames(param);

                               localWsAttributeDefNamesTracker = true;
                                      
                                      this.localWsAttributeDefNames=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName
                             */
                             public void addWsAttributeDefNames(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName param){
                                   if (localWsAttributeDefNames == null){
                                   localWsAttributeDefNames = new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAttributeDefNamesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAttributeDefNames);
                               list.add(param);
                               this.localWsAttributeDefNames =
                             (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[list.size()]);

                             }
                             

                        /**
                        * field for WsAttributeDefs
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[] localWsAttributeDefs ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsAttributeDefsTracker = false ;

                           public boolean isWsAttributeDefsSpecified(){
                               return localWsAttributeDefsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[] getWsAttributeDefs(){
                               return localWsAttributeDefs;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsAttributeDefs
                               */
                              protected void validateWsAttributeDefs(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsAttributeDefs
                              */
                              public void setWsAttributeDefs(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[] param){
                              
                                   validateWsAttributeDefs(param);

                               localWsAttributeDefsTracker = true;
                                      
                                      this.localWsAttributeDefs=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef
                             */
                             public void addWsAttributeDefs(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef param){
                                   if (localWsAttributeDefs == null){
                                   localWsAttributeDefs = new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsAttributeDefsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsAttributeDefs);
                               list.add(param);
                               this.localWsAttributeDefs =
                             (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[list.size()]);

                             }
                             

                        /**
                        * field for WsGroups
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[] localWsGroups ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsGroupsTracker = false ;

                           public boolean isWsGroupsSpecified(){
                               return localWsGroupsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[] getWsGroups(){
                               return localWsGroups;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsGroups
                               */
                              protected void validateWsGroups(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsGroups
                              */
                              public void setWsGroups(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[] param){
                              
                                   validateWsGroups(param);

                               localWsGroupsTracker = true;
                                      
                                      this.localWsGroups=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup
                             */
                             public void addWsGroups(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup param){
                                   if (localWsGroups == null){
                                   localWsGroups = new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsGroupsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsGroups);
                               list.add(param);
                               this.localWsGroups =
                             (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[list.size()]);

                             }
                             

                        /**
                        * field for WsSubjects
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[] localWsSubjects ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsSubjectsTracker = false ;

                           public boolean isWsSubjectsSpecified(){
                               return localWsSubjectsTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[] getWsSubjects(){
                               return localWsSubjects;
                           }

                           
                        


                               
                              /**
                               * validate the array for WsSubjects
                               */
                              protected void validateWsSubjects(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param WsSubjects
                              */
                              public void setWsSubjects(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[] param){
                              
                                   validateWsSubjects(param);

                               localWsSubjectsTracker = true;
                                      
                                      this.localWsSubjects=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject
                             */
                             public void addWsSubjects(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject param){
                                   if (localWsSubjects == null){
                                   localWsSubjects = new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[]{};
                                   }

                            
                                 //update the setting tracker
                                localWsSubjectsTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localWsSubjects);
                               list.add(param);
                               this.localWsSubjects =
                             (edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[list.size()]);

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":WsAssignPermissionsResults",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "WsAssignPermissionsResults",
                           xmlWriter);
                   }

               
                   }
                if (localResponseMetadataTracker){
                                    if (localResponseMetadata==null){

                                        writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "responseMetadata", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localResponseMetadata.serialize(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","responseMetadata"),
                                        xmlWriter);
                                    }
                                } if (localResultMetadataTracker){
                                    if (localResultMetadata==null){

                                        writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "resultMetadata", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localResultMetadata.serialize(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","resultMetadata"),
                                        xmlWriter);
                                    }
                                } if (localSubjectAttributeNamesTracker){
                             if (localSubjectAttributeNames!=null) {
                                   namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                   for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                        
                                            if (localSubjectAttributeNames[i] != null){
                                        
                                                writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);

                                            
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                                    
                                                xmlWriter.writeEndElement();
                                              
                                                } else {
                                                   
                                                           // write null attribute
                                                            namespace = "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd";
                                                            writeStartElement(null, namespace, "subjectAttributeNames", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       
                                                }

                                   }
                             } else {
                                 
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "subjectAttributeNames", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    
                             }

                        } if (localWsAssignPermissionResultsTracker){
                                       if (localWsAssignPermissionResults!=null){
                                            for (int i = 0;i < localWsAssignPermissionResults.length;i++){
                                                if (localWsAssignPermissionResults[i] != null){
                                                 localWsAssignPermissionResults[i].serialize(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAssignPermissionResults"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsAssignPermissionResults", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsAssignPermissionResults", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsAttributeDefNamesTracker){
                                       if (localWsAttributeDefNames!=null){
                                            for (int i = 0;i < localWsAttributeDefNames.length;i++){
                                                if (localWsAttributeDefNames[i] != null){
                                                 localWsAttributeDefNames[i].serialize(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNames"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefNames", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefNames", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsAttributeDefsTracker){
                                       if (localWsAttributeDefs!=null){
                                            for (int i = 0;i < localWsAttributeDefs.length;i++){
                                                if (localWsAttributeDefs[i] != null){
                                                 localWsAttributeDefs[i].serialize(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefs"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefs", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsAttributeDefs", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsGroupsTracker){
                                       if (localWsGroups!=null){
                                            for (int i = 0;i < localWsGroups.length;i++){
                                                if (localWsGroups[i] != null){
                                                 localWsGroups[i].serialize(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsGroups"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsGroups", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsGroups", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localWsSubjectsTracker){
                                       if (localWsSubjects!=null){
                                            for (int i = 0;i < localWsSubjects.length;i++){
                                                if (localWsSubjects[i] != null){
                                                 localWsSubjects[i].serialize(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsSubjects"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsSubjects", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd", "wsSubjects", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 }
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd")){
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

                 if (localResponseMetadataTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "responseMetadata"));
                            
                            
                                    elementList.add(localResponseMetadata==null?null:
                                    localResponseMetadata);
                                } if (localResultMetadataTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "resultMetadata"));
                            
                            
                                    elementList.add(localResultMetadata==null?null:
                                    localResultMetadata);
                                } if (localSubjectAttributeNamesTracker){
                            if (localSubjectAttributeNames!=null){
                                  for (int i = 0;i < localSubjectAttributeNames.length;i++){
                                      
                                         if (localSubjectAttributeNames[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSubjectAttributeNames[i]));
                                          } else {
                                             
                                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                                    elementList.add(null);
                                                
                                          }
                                      

                                  }
                            } else {
                              
                                    elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                              "subjectAttributeNames"));
                                    elementList.add(null);
                                
                            }

                        } if (localWsAssignPermissionResultsTracker){
                             if (localWsAssignPermissionResults!=null) {
                                 for (int i = 0;i < localWsAssignPermissionResults.length;i++){

                                    if (localWsAssignPermissionResults[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignPermissionResults"));
                                         elementList.add(localWsAssignPermissionResults[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignPermissionResults"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAssignPermissionResults"));
                                        elementList.add(localWsAssignPermissionResults);
                                    
                             }

                        } if (localWsAttributeDefNamesTracker){
                             if (localWsAttributeDefNames!=null) {
                                 for (int i = 0;i < localWsAttributeDefNames.length;i++){

                                    if (localWsAttributeDefNames[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefNames"));
                                         elementList.add(localWsAttributeDefNames[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefNames"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefNames"));
                                        elementList.add(localWsAttributeDefNames);
                                    
                             }

                        } if (localWsAttributeDefsTracker){
                             if (localWsAttributeDefs!=null) {
                                 for (int i = 0;i < localWsAttributeDefs.length;i++){

                                    if (localWsAttributeDefs[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefs"));
                                         elementList.add(localWsAttributeDefs[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefs"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsAttributeDefs"));
                                        elementList.add(localWsAttributeDefs);
                                    
                             }

                        } if (localWsGroupsTracker){
                             if (localWsGroups!=null) {
                                 for (int i = 0;i < localWsGroups.length;i++){

                                    if (localWsGroups[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsGroups"));
                                         elementList.add(localWsGroups[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsGroups"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsGroups"));
                                        elementList.add(localWsGroups);
                                    
                             }

                        } if (localWsSubjectsTracker){
                             if (localWsSubjects!=null) {
                                 for (int i = 0;i < localWsSubjects.length;i++){

                                    if (localWsSubjects[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsSubjects"));
                                         elementList.add(localWsSubjects[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsSubjects"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "wsSubjects"));
                                        elementList.add(localWsSubjects);
                                    
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
        public static WsAssignPermissionsResults parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            WsAssignPermissionsResults object =
                new WsAssignPermissionsResults();

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
                    
                            if (!"WsAssignPermissionsResults".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (WsAssignPermissionsResults)edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.ExtensionMapper.getTypeObject(
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
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","responseMetadata").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setResponseMetadata(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setResponseMetadata(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResponseMeta.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","resultMetadata").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setResultMetadata(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setResultMetadata(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsResultMeta.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  list3.add(null);
                                                       
                                                  reader.next();
                                              } else {
                                            list3.add(reader.getElementText());
                                            }
                                            //loop until we find a start element that is not part of this array
                                            boolean loopDone3 = false;
                                            while(!loopDone3){
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
                                                    loopDone3 = true;
                                                } else {
                                                    if (new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","subjectAttributeNames").equals(reader.getName())){
                                                         
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list3.add(null);
                                                                   
                                                              reader.next();
                                                          } else {
                                                        list3.add(reader.getElementText());
                                                        }
                                                    }else{
                                                        loopDone3 = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            
                                                    object.setSubjectAttributeNames((java.lang.String[])
                                                        list3.toArray(new java.lang.String[list3.size()]));
                                                
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAssignPermissionResults").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list4.add(null);
                                                              reader.next();
                                                          } else {
                                                        list4.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAssignPermissionResults").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list4.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list4.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone4 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAssignPermissionResults((edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAssignPermissionResult.class,
                                                                list4));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNames").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list5.add(null);
                                                              reader.next();
                                                          } else {
                                                        list5.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefNames").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list5.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list5.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone5 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeDefNames((edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDefName.class,
                                                                list5));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefs").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list6.add(null);
                                                              reader.next();
                                                          } else {
                                                        list6.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsAttributeDefs").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list6.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list6.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone6 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsAttributeDefs((edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsAttributeDef.class,
                                                                list6));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsGroups").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list7.add(null);
                                                              reader.next();
                                                          } else {
                                                        list7.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsGroups").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list7.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list7.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone7 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsGroups((edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsGroup.class,
                                                                list7));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsSubjects").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list8.add(null);
                                                              reader.next();
                                                          } else {
                                                        list8.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_5.ws.grouper.middleware.internet2.edu/xsd","wsSubjects").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list8.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list8.add(edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone8 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setWsSubjects((edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_5.xsd.WsSubject.class,
                                                                list8));
                                                            
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
           
    