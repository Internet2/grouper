
/**
 * WsMessageResults.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1  Built on : Aug 31, 2011 (12:23:23 CEST)
 */

            
                package edu.internet2.middleware.grouper.ws.soap_v2_4.xsd;
            

            /**
            *  WsMessageResults bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class WsMessageResults
        implements org.apache.axis2.databinding.ADBBean{
        /* This type was generated from the piece of schema that had
                name = WsMessageResults
                Namespace URI = http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd
                Namespace Prefix = ns1
                */
            

                        /**
                        * field for MessageSystemName
                        */

                        
                                    protected java.lang.String localMessageSystemName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMessageSystemNameTracker = false ;

                           public boolean isMessageSystemNameSpecified(){
                               return localMessageSystemNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getMessageSystemName(){
                               return localMessageSystemName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MessageSystemName
                               */
                               public void setMessageSystemName(java.lang.String param){
                            localMessageSystemNameTracker = true;
                                   
                                            this.localMessageSystemName=param;
                                    

                               }
                            

                        /**
                        * field for Messages
                        * This was an Array!
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[] localMessages ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMessagesTracker = false ;

                           public boolean isMessagesSpecified(){
                               return localMessagesTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[]
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[] getMessages(){
                               return localMessages;
                           }

                           
                        


                               
                              /**
                               * validate the array for Messages
                               */
                              protected void validateMessages(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[] param){
                             
                              }


                             /**
                              * Auto generated setter method
                              * @param param Messages
                              */
                              public void setMessages(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[] param){
                              
                                   validateMessages(param);

                               localMessagesTracker = true;
                                      
                                      this.localMessages=param;
                              }

                               
                             
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage
                             */
                             public void addMessages(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage param){
                                   if (localMessages == null){
                                   localMessages = new edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[]{};
                                   }

                            
                                 //update the setting tracker
                                localMessagesTracker = true;
                            

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(localMessages);
                               list.add(param);
                               this.localMessages =
                             (edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[])list.toArray(
                            new edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[list.size()]);

                             }
                             

                        /**
                        * field for QueueOrTopicName
                        */

                        
                                    protected java.lang.String localQueueOrTopicName ;
                                
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localQueueOrTopicNameTracker = false ;

                           public boolean isQueueOrTopicNameSpecified(){
                               return localQueueOrTopicNameTracker;
                           }

                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getQueueOrTopicName(){
                               return localQueueOrTopicName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param QueueOrTopicName
                               */
                               public void setQueueOrTopicName(java.lang.String param){
                            localQueueOrTopicNameTracker = true;
                                   
                                            this.localQueueOrTopicName=param;
                                    

                               }
                            

                        /**
                        * field for ResponseMetadata
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResponseMeta localResponseMetadata ;
                                
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
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResponseMeta
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResponseMeta getResponseMetadata(){
                               return localResponseMetadata;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ResponseMetadata
                               */
                               public void setResponseMetadata(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResponseMeta param){
                            localResponseMetadataTracker = true;
                                   
                                            this.localResponseMetadata=param;
                                    

                               }
                            

                        /**
                        * field for ResultMetadata
                        */

                        
                                    protected edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResultMeta localResultMetadata ;
                                
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
                           * @return edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResultMeta
                           */
                           public  edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResultMeta getResultMetadata(){
                               return localResultMetadata;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param ResultMetadata
                               */
                               public void setResultMetadata(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResultMeta param){
                            localResultMetadataTracker = true;
                                   
                                            this.localResultMetadata=param;
                                    

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
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":WsMessageResults",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "WsMessageResults",
                           xmlWriter);
                   }

               
                   }
                if (localMessageSystemNameTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "messageSystemName", xmlWriter);
                             

                                          if (localMessageSystemName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localMessageSystemName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMessagesTracker){
                                       if (localMessages!=null){
                                            for (int i = 0;i < localMessages.length;i++){
                                                if (localMessages[i] != null){
                                                 localMessages[i].serialize(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","messages"),
                                                           xmlWriter);
                                                } else {
                                                   
                                                            writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "messages", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    
                                                }

                                            }
                                     } else {
                                        
                                                writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "messages", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        
                                    }
                                 } if (localQueueOrTopicNameTracker){
                                    namespace = "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd";
                                    writeStartElement(null, namespace, "queueOrTopicName", xmlWriter);
                             

                                          if (localQueueOrTopicName==null){
                                              // write the nil attribute
                                              
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localQueueOrTopicName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localResponseMetadataTracker){
                                    if (localResponseMetadata==null){

                                        writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "responseMetadata", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localResponseMetadata.serialize(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","responseMetadata"),
                                        xmlWriter);
                                    }
                                } if (localResultMetadataTracker){
                                    if (localResultMetadata==null){

                                        writeStartElement(null, "http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd", "resultMetadata", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     localResultMetadata.serialize(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","resultMetadata"),
                                        xmlWriter);
                                    }
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

                 if (localMessageSystemNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "messageSystemName"));
                                 
                                         elementList.add(localMessageSystemName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMessageSystemName));
                                    } if (localMessagesTracker){
                             if (localMessages!=null) {
                                 for (int i = 0;i < localMessages.length;i++){

                                    if (localMessages[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "messages"));
                                         elementList.add(localMessages[i]);
                                    } else {
                                        
                                                elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "messages"));
                                                elementList.add(null);
                                            
                                    }

                                 }
                             } else {
                                 
                                        elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                          "messages"));
                                        elementList.add(localMessages);
                                    
                             }

                        } if (localQueueOrTopicNameTracker){
                                      elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "queueOrTopicName"));
                                 
                                         elementList.add(localQueueOrTopicName==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localQueueOrTopicName));
                                    } if (localResponseMetadataTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "responseMetadata"));
                            
                            
                                    elementList.add(localResponseMetadata==null?null:
                                    localResponseMetadata);
                                } if (localResultMetadataTracker){
                            elementList.add(new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd",
                                                                      "resultMetadata"));
                            
                            
                                    elementList.add(localResultMetadata==null?null:
                                    localResultMetadata);
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
        public static WsMessageResults parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            WsMessageResults object =
                new WsMessageResults();

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
                    
                            if (!"WsMessageResults".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (WsMessageResults)edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    
                    reader.next();
                
                        java.util.ArrayList list2 = new java.util.ArrayList();
                    
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","messageSystemName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMessageSystemName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","messages").equals(reader.getName())){
                                
                                    
                                    
                                    // Process the array and step past its final element's end.
                                    
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              list2.add(null);
                                                              reader.next();
                                                          } else {
                                                        list2.add(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage.Factory.parse(reader));
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
                                                                if (new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","messages").equals(reader.getName())){
                                                                    
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          list2.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    list2.add(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage.Factory.parse(reader));
                                                                        }
                                                                }else{
                                                                    loopDone2 = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        
                                                        object.setMessages((edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage[])
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsMessage.class,
                                                                list2));
                                                            
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","queueOrTopicName").equals(reader.getName())){
                                
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                    
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setQueueOrTopicName(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                            
                                       } else {
                                           
                                           
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                      
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","responseMetadata").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setResponseMetadata(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setResponseMetadata(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResponseMeta.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
                              }  // End of if for expected property start element
                                
                                    else {
                                        
                                    }
                                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://soap_v2_4.ws.grouper.middleware.internet2.edu/xsd","resultMetadata").equals(reader.getName())){
                                
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.setResultMetadata(null);
                                          reader.next();
                                            
                                            reader.next();
                                          
                                      }else{
                                    
                                                object.setResultMetadata(edu.internet2.middleware.grouper.ws.soap_v2_4.xsd.WsResultMeta.Factory.parse(reader));
                                              
                                        reader.next();
                                    }
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
           
    