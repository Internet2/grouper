
/**
 * WsGrouperPrivilegeResult.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:41 LKT)
 */
            
                package edu.internet2.middleware.grouper.ws.soap_v2_0.xsd;
            

            /**
            *  WsGrouperPrivilegeResult bean class
            */
        
        public  class WsGrouperPrivilegeResult
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = WsGrouperPrivilegeResult
                Namespace URI = http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd
                Namespace Prefix = ns1
                */
            

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        

                        /**
                        * field for Allowed
                        */

                        
                                    protected java.lang.String localAllowed ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localAllowedTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getAllowed(){
                               return localAllowed;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Allowed
                               */
                               public void setAllowed(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localAllowedTracker = true;
                                       } else {
                                          localAllowedTracker = true;
                                              
                                       }
                                   
                                            this.localAllowed=param;
                                    

                               }
                            

                        /**
                        * field for OwnerSubject
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject localOwnerSubject ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localOwnerSubjectTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject getOwnerSubject(){
                               return localOwnerSubject;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param OwnerSubject
                               */
                               public void setOwnerSubject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localOwnerSubjectTracker = true;
                                       } else {
                                          localOwnerSubjectTracker = true;
                                              
                                       }
                                   
                                            this.localOwnerSubject=param;
                                    

                               }
                            

                        /**
                        * field for PrivilegeName
                        */

                        
                                    protected java.lang.String localPrivilegeName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPrivilegeNameTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPrivilegeName(){
                               return localPrivilegeName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PrivilegeName
                               */
                               public void setPrivilegeName(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPrivilegeNameTracker = true;
                                       } else {
                                          localPrivilegeNameTracker = true;
                                              
                                       }
                                   
                                            this.localPrivilegeName=param;
                                    

                               }
                            

                        /**
                        * field for PrivilegeType
                        */

                        
                                    protected java.lang.String localPrivilegeType ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localPrivilegeTypeTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPrivilegeType(){
                               return localPrivilegeType;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PrivilegeType
                               */
                               public void setPrivilegeType(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localPrivilegeTypeTracker = true;
                                       } else {
                                          localPrivilegeTypeTracker = true;
                                              
                                       }
                                   
                                            this.localPrivilegeType=param;
                                    

                               }
                            

                        /**
                        * field for Revokable
                        */

                        
                                    protected java.lang.String localRevokable ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRevokableTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRevokable(){
                               return localRevokable;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Revokable
                               */
                               public void setRevokable(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localRevokableTracker = true;
                                       } else {
                                          localRevokableTracker = true;
                                              
                                       }
                                   
                                            this.localRevokable=param;
                                    

                               }
                            

                        /**
                        * field for WsGroup
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroup localWsGroup ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsGroupTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroup
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroup getWsGroup(){
                               return localWsGroup;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsGroup
                               */
                               public void setWsGroup(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroup param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localWsGroupTracker = true;
                                       } else {
                                          localWsGroupTracker = true;
                                              
                                       }
                                   
                                            this.localWsGroup=param;
                                    

                               }
                            

                        /**
                        * field for WsStem
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsStem localWsStem ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsStemTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsStem
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsStem getWsStem(){
                               return localWsStem;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsStem
                               */
                               public void setWsStem(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsStem param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localWsStemTracker = true;
                                       } else {
                                          localWsStemTracker = true;
                                              
                                       }
                                   
                                            this.localWsStem=param;
                                    

                               }
                            

                        /**
                        * field for WsSubject
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject localWsSubject ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localWsSubjectTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject getWsSubject(){
                               return localWsSubject;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param WsSubject
                               */
                               public void setWsSubject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localWsSubjectTracker = true;
                                       } else {
                                          localWsSubjectTracker = true;
                                              
                                       }
                                   
                                            this.localWsSubject=param;
                                    

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
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

                 public void serialize(org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
                       WsGrouperPrivilegeResult.this.serialize(parentQName,factory,xmlWriter);
                 }
               };
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               parentQName,factory,dataSource);
            
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
                           namespacePrefix+":WsGrouperPrivilegeResult",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "WsGrouperPrivilegeResult",
                           xmlWriter);
                   }

               
                   }
                if (localAllowedTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"allowed", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"allowed");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("allowed");
                                    }
                                

                                          if (localAllowed==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localAllowed);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localOwnerSubjectTracker){
                                    if (localOwnerSubject==null){

                                            java.lang.String namespace2 = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";

                                        if (! namespace2.equals("")) {
                                            java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                            if (prefix2 == null) {
                                                prefix2 = generatePrefix(namespace2);

                                                xmlWriter.writeStartElement(prefix2,"ownerSubject", namespace2);
                                                xmlWriter.writeNamespace(prefix2, namespace2);
                                                xmlWriter.setPrefix(prefix2, namespace2);

                                            } else {
                                                xmlWriter.writeStartElement(namespace2,"ownerSubject");
                                            }

                                        } else {
                                            xmlWriter.writeStartElement("ownerSubject");
                                        }


                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localOwnerSubject.serialize(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","ownerSubject"),
                                        factory,xmlWriter);
                                    }
                                } if (localPrivilegeNameTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"privilegeName", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"privilegeName");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("privilegeName");
                                    }
                                

                                          if (localPrivilegeName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPrivilegeName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localPrivilegeTypeTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"privilegeType", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"privilegeType");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("privilegeType");
                                    }
                                

                                          if (localPrivilegeType==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localPrivilegeType);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localRevokableTracker){
                                    namespace = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = generatePrefix(namespace);

                                            xmlWriter.writeStartElement(prefix,"revokable", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"revokable");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("revokable");
                                    }
                                

                                          if (localRevokable==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRevokable);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localWsGroupTracker){
                                    if (localWsGroup==null){

                                            java.lang.String namespace2 = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";

                                        if (! namespace2.equals("")) {
                                            java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                            if (prefix2 == null) {
                                                prefix2 = generatePrefix(namespace2);

                                                xmlWriter.writeStartElement(prefix2,"wsGroup", namespace2);
                                                xmlWriter.writeNamespace(prefix2, namespace2);
                                                xmlWriter.setPrefix(prefix2, namespace2);

                                            } else {
                                                xmlWriter.writeStartElement(namespace2,"wsGroup");
                                            }

                                        } else {
                                            xmlWriter.writeStartElement("wsGroup");
                                        }


                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localWsGroup.serialize(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","wsGroup"),
                                        factory,xmlWriter);
                                    }
                                } if (localWsStemTracker){
                                    if (localWsStem==null){

                                            java.lang.String namespace2 = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";

                                        if (! namespace2.equals("")) {
                                            java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                            if (prefix2 == null) {
                                                prefix2 = generatePrefix(namespace2);

                                                xmlWriter.writeStartElement(prefix2,"wsStem", namespace2);
                                                xmlWriter.writeNamespace(prefix2, namespace2);
                                                xmlWriter.setPrefix(prefix2, namespace2);

                                            } else {
                                                xmlWriter.writeStartElement(namespace2,"wsStem");
                                            }

                                        } else {
                                            xmlWriter.writeStartElement("wsStem");
                                        }


                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localWsStem.serialize(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","wsStem"),
                                        factory,xmlWriter);
                                    }
                                } if (localWsSubjectTracker){
                                    if (localWsSubject==null){

                                            java.lang.String namespace2 = "http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd";

                                        if (! namespace2.equals("")) {
                                            java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                            if (prefix2 == null) {
                                                prefix2 = generatePrefix(namespace2);

                                                xmlWriter.writeStartElement(prefix2,"wsSubject", namespace2);
                                                xmlWriter.writeNamespace(prefix2, namespace2);
                                                xmlWriter.setPrefix(prefix2, namespace2);

                                            } else {
                                                xmlWriter.writeStartElement(namespace2,"wsSubject");
                                            }

                                        } else {
                                            xmlWriter.writeStartElement("wsSubject");
                                        }


                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localWsSubject.serialize(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","wsSubject"),
                                        factory,xmlWriter);
                                    }
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

                 if (localAllowedTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "allowed"));
                                 
                                         elementList.add(localAllowed==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAllowed));
                                    } if (localOwnerSubjectTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "ownerSubject"));
                            
                            
                                    elementList.add(localOwnerSubject==null?null:
                                    localOwnerSubject);
                                } if (localPrivilegeNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "privilegeName"));
                                 
                                         elementList.add(localPrivilegeName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPrivilegeName));
                                    } if (localPrivilegeTypeTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "privilegeType"));
                                 
                                         elementList.add(localPrivilegeType==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPrivilegeType));
                                    } if (localRevokableTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "revokable"));
                                 
                                         elementList.add(localRevokable==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRevokable));
                                    } if (localWsGroupTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsGroup"));
                            
                            
                                    elementList.add(localWsGroup==null?null:
                                    localWsGroup);
                                } if (localWsStemTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsStem"));
                            
                            
                                    elementList.add(localWsStem==null?null:
                                    localWsStem);
                                } if (localWsSubjectTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "wsSubject"));
                            
                            
                                    elementList.add(localWsSubject==null?null:
                                    localWsSubject);
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
        public static WsGrouperPrivilegeResult parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            WsGrouperPrivilegeResult object =
                new WsGrouperPrivilegeResult();

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
                    
                            if (!"WsGrouperPrivilegeResult".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (WsGrouperPrivilegeResult)edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                 
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","allowed").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setAllowed(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","ownerSubject").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setOwnerSubject(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setOwnerSubject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","privilegeName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPrivilegeName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","privilegeType").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setPrivilegeType(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","revokable").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRevokable(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","wsGroup").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setWsGroup(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setWsGroup(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsGroup.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","wsStem").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setWsStem(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setWsStem(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsStem.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_0.ws.grouper.middleware.internet2.edu/xsd","wsSubject").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setWsSubject(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setWsSubject(edu.internet2.middleware.grouper.ws.soap_v2_0.xsd.WsSubject.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
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
           
          