/**
 * GrouperServiceStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.3  Built on : Aug 10, 2007 (04:45:47 LKT)
 */
package edu.internet2.middleware.grouper.webservicesClient;


/*
 *  GrouperServiceStub java implementation
 */
public class GrouperServiceStub extends org.apache.axis2.client.Stub {
    protected org.apache.axis2.description.AxisOperation[] _operations;

    //hashmaps to keep the fault mapping
    private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
    private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
    private java.util.HashMap faultMessageMap = new java.util.HashMap();
    private javax.xml.namespace.QName[] opNameArray = null;

    /**
     *Constructor that takes in a configContext
     */
    public GrouperServiceStub(
        org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(configurationContext, targetEndpoint, false);
    }

    /**
     * Constructor that takes in a configContext  and useseperate listner
     */
    public GrouperServiceStub(
        org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint, boolean useSeparateListener)
        throws org.apache.axis2.AxisFault {
        //To populate AxisService
        populateAxisService();
        populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,
                _service);

        configurationContext = _serviceClient.getServiceContext()
                                             .getConfigurationContext();

        _serviceClient.getOptions()
                      .setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);

        //Set the soap version
        _serviceClient.getOptions()
                      .setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
    }

    /**
     * Default Constructor
     */
    public GrouperServiceStub(
        org.apache.axis2.context.ConfigurationContext configurationContext)
        throws org.apache.axis2.AxisFault {
        this(configurationContext,
            "http://localhost:8090/grouper-ws/services/GrouperService");
    }

    /**
     * Default Constructor
     */
    public GrouperServiceStub() throws org.apache.axis2.AxisFault {
        this("http://localhost:8090/grouper-ws/services/GrouperService");
    }

    /**
     * Constructor taking the target endpoint
     */
    public GrouperServiceStub(java.lang.String targetEndpoint)
        throws org.apache.axis2.AxisFault {
        this(null, targetEndpoint);
    }

    private void populateAxisService() throws org.apache.axis2.AxisFault {
        //creating the Service with a unique name
        _service = new org.apache.axis2.description.AxisService(
                "GrouperService" + this.hashCode());

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;

        _operations = new org.apache.axis2.description.AxisOperation[3];

        __operation = new org.apache.axis2.description.OutInAxisOperation();

        __operation.setName(new javax.xml.namespace.QName(
                "http://webservices.grouper.middleware.internet2.edu/",
                "addMember"));
        _service.addOperation(__operation);

        _operations[0] = __operation;

        __operation = new org.apache.axis2.description.OutInAxisOperation();

        __operation.setName(new javax.xml.namespace.QName(
                "http://webservices.grouper.middleware.internet2.edu/",
                "addMemberSimple"));
        _service.addOperation(__operation);

        _operations[1] = __operation;

        __operation = new org.apache.axis2.description.OutInAxisOperation();

        __operation.setName(new javax.xml.namespace.QName(
                "http://webservices.grouper.middleware.internet2.edu/",
                "findGroups"));
        _service.addOperation(__operation);

        _operations[2] = __operation;
    }

    //populates the faults
    private void populateFaults() {
    }

    /**
     * Auto generated method signature
     * @see edu.internet2.middleware.grouper.webservicesClient.GrouperService#addMember
     * @param addMember0
     */
    public edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse addMember(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember0)
        throws java.rmi.RemoteException {
        try {
            org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
            _operationClient.getOptions().setAction("urn:addMember");
            _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

            addPropertyToOperationClient(_operationClient,
                org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                "&");

            // create a message context
            org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

            // create SOAP envelope with that payload
            org.apache.axiom.soap.SOAPEnvelope env = null;

            env = toEnvelope(getFactory(_operationClient.getOptions()
                                                        .getSoapVersionURI()),
                    addMember0,
                    optimizeContent(
                        new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/",
                            "addMember")));

            //adding SOAP soap_headers
            _serviceClient.addHeadersToEnvelope(env);
            // set the message context with that soap envelope
            _messageContext.setEnvelope(env);

            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);

            //execute the operation client
            _operationClient.execute(true);

            org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

            java.lang.Object object = fromOM(_returnEnv.getBody()
                                                       .getFirstElement(),
                    edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse.class,
                    getEnvelopeNamespaces(_returnEnv));
            _messageContext.getTransportOut().getSender()
                           .cleanup(_messageContext);

            return (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse) object;
        } catch (org.apache.axis2.AxisFault f) {
            org.apache.axiom.om.OMElement faultElt = f.getDetail();

            if (faultElt != null) {
                if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
                    //make the fault by reflection
                    try {
                        java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();

                        //message class
                        java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,
                                messageClass, null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                new java.lang.Class[] { messageClass });
                        m.invoke(ex, new java.lang.Object[] { messageObject });

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    } catch (java.lang.ClassCastException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                } else {
                    throw f;
                }
            } else {
                throw f;
            }
        }
    }

    /**
     * Auto generated method signature for Asynchronous Invocations
     * @see edu.internet2.middleware.grouper.webservicesClient.GrouperService#startaddMember
     * @param addMember0
     */
    public void startaddMember(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember addMember0,
        final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)
        throws java.rmi.RemoteException {
        org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
        _operationClient.getOptions().setAction("urn:addMember");
        _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

        addPropertyToOperationClient(_operationClient,
            org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
            "&");

        // create SOAP envelope with that payload
        org.apache.axiom.soap.SOAPEnvelope env = null;
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

        //Style is Doc.
        env = toEnvelope(getFactory(_operationClient.getOptions()
                                                    .getSoapVersionURI()),
                addMember0,
                optimizeContent(
                    new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/",
                        "addMember")));

        // adding SOAP soap_headers
        _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);

        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                public void onMessage(
                    org.apache.axis2.context.MessageContext resultContext) {
                    try {
                        org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();

                        java.lang.Object object = fromOM(resultEnv.getBody()
                                                                  .getFirstElement(),
                                edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse.class,
                                getEnvelopeNamespaces(resultEnv));
                        callback.receiveResultaddMember((edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse) object);
                    } catch (org.apache.axis2.AxisFault e) {
                        callback.receiveErroraddMember(e);
                    }
                }

                public void onError(java.lang.Exception error) {
                    if (error instanceof org.apache.axis2.AxisFault) {
                        org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
                        org.apache.axiom.om.OMElement faultElt = f.getDetail();

                        if (faultElt != null) {
                            if (faultExceptionNameMap.containsKey(
                                        faultElt.getQName())) {
                                //make the fault by reflection
                                try {
                                    java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
                                    java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                                    java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();

                                    //message class
                                    java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                                    java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                                    java.lang.Object messageObject = fromOM(faultElt,
                                            messageClass, null);
                                    java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                            new java.lang.Class[] { messageClass });
                                    m.invoke(ex,
                                        new java.lang.Object[] { messageObject });

                                    callback.receiveErroraddMember(new java.rmi.RemoteException(
                                            ex.getMessage(), ex));
                                } catch (java.lang.ClassCastException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMember(f);
                                } catch (java.lang.ClassNotFoundException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMember(f);
                                } catch (java.lang.NoSuchMethodException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMember(f);
                                } catch (java.lang.reflect.InvocationTargetException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMember(f);
                                } catch (java.lang.IllegalAccessException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMember(f);
                                } catch (java.lang.InstantiationException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMember(f);
                                } catch (org.apache.axis2.AxisFault e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMember(f);
                                }
                            } else {
                                callback.receiveErroraddMember(f);
                            }
                        } else {
                            callback.receiveErroraddMember(f);
                        }
                    } else {
                        callback.receiveErroraddMember(error);
                    }
                }

                public void onFault(
                    org.apache.axis2.context.MessageContext faultContext) {
                    org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                    onError(fault);
                }

                public void onComplete() {
                    // Do nothing by default
                }
            });

        org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;

        if ((_operations[0].getMessageReceiver() == null) &&
                _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
            _operations[0].setMessageReceiver(_callbackReceiver);
        }

        //execute the operation client
        _operationClient.execute(false);
    }

    /**
     * Auto generated method signature
     * @see edu.internet2.middleware.grouper.webservicesClient.GrouperService#addMemberSimple
     * @param addMemberSimple2
     */
    public edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse addMemberSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple addMemberSimple2)
        throws java.rmi.RemoteException {
        try {
            org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
            _operationClient.getOptions().setAction("urn:addMemberSimple");
            _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

            addPropertyToOperationClient(_operationClient,
                org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                "&");

            // create a message context
            org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

            // create SOAP envelope with that payload
            org.apache.axiom.soap.SOAPEnvelope env = null;

            env = toEnvelope(getFactory(_operationClient.getOptions()
                                                        .getSoapVersionURI()),
                    addMemberSimple2,
                    optimizeContent(
                        new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/",
                            "addMemberSimple")));

            //adding SOAP soap_headers
            _serviceClient.addHeadersToEnvelope(env);
            // set the message context with that soap envelope
            _messageContext.setEnvelope(env);

            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);

            //execute the operation client
            _operationClient.execute(true);

            org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

            java.lang.Object object = fromOM(_returnEnv.getBody()
                                                       .getFirstElement(),
                    edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse.class,
                    getEnvelopeNamespaces(_returnEnv));
            _messageContext.getTransportOut().getSender()
                           .cleanup(_messageContext);

            return (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse) object;
        } catch (org.apache.axis2.AxisFault f) {
            org.apache.axiom.om.OMElement faultElt = f.getDetail();

            if (faultElt != null) {
                if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
                    //make the fault by reflection
                    try {
                        java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();

                        //message class
                        java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,
                                messageClass, null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                new java.lang.Class[] { messageClass });
                        m.invoke(ex, new java.lang.Object[] { messageObject });

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    } catch (java.lang.ClassCastException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                } else {
                    throw f;
                }
            } else {
                throw f;
            }
        }
    }

    /**
     * Auto generated method signature for Asynchronous Invocations
     * @see edu.internet2.middleware.grouper.webservicesClient.GrouperService#startaddMemberSimple
     * @param addMemberSimple2
     */
    public void startaddMemberSimple(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple addMemberSimple2,
        final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)
        throws java.rmi.RemoteException {
        org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
        _operationClient.getOptions().setAction("urn:addMemberSimple");
        _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

        addPropertyToOperationClient(_operationClient,
            org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
            "&");

        // create SOAP envelope with that payload
        org.apache.axiom.soap.SOAPEnvelope env = null;
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

        //Style is Doc.
        env = toEnvelope(getFactory(_operationClient.getOptions()
                                                    .getSoapVersionURI()),
                addMemberSimple2,
                optimizeContent(
                    new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/",
                        "addMemberSimple")));

        // adding SOAP soap_headers
        _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);

        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                public void onMessage(
                    org.apache.axis2.context.MessageContext resultContext) {
                    try {
                        org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();

                        java.lang.Object object = fromOM(resultEnv.getBody()
                                                                  .getFirstElement(),
                                edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse.class,
                                getEnvelopeNamespaces(resultEnv));
                        callback.receiveResultaddMemberSimple((edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse) object);
                    } catch (org.apache.axis2.AxisFault e) {
                        callback.receiveErroraddMemberSimple(e);
                    }
                }

                public void onError(java.lang.Exception error) {
                    if (error instanceof org.apache.axis2.AxisFault) {
                        org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
                        org.apache.axiom.om.OMElement faultElt = f.getDetail();

                        if (faultElt != null) {
                            if (faultExceptionNameMap.containsKey(
                                        faultElt.getQName())) {
                                //make the fault by reflection
                                try {
                                    java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
                                    java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                                    java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();

                                    //message class
                                    java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                                    java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                                    java.lang.Object messageObject = fromOM(faultElt,
                                            messageClass, null);
                                    java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                            new java.lang.Class[] { messageClass });
                                    m.invoke(ex,
                                        new java.lang.Object[] { messageObject });

                                    callback.receiveErroraddMemberSimple(new java.rmi.RemoteException(
                                            ex.getMessage(), ex));
                                } catch (java.lang.ClassCastException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMemberSimple(f);
                                } catch (java.lang.ClassNotFoundException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMemberSimple(f);
                                } catch (java.lang.NoSuchMethodException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMemberSimple(f);
                                } catch (java.lang.reflect.InvocationTargetException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMemberSimple(f);
                                } catch (java.lang.IllegalAccessException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMemberSimple(f);
                                } catch (java.lang.InstantiationException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMemberSimple(f);
                                } catch (org.apache.axis2.AxisFault e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErroraddMemberSimple(f);
                                }
                            } else {
                                callback.receiveErroraddMemberSimple(f);
                            }
                        } else {
                            callback.receiveErroraddMemberSimple(f);
                        }
                    } else {
                        callback.receiveErroraddMemberSimple(error);
                    }
                }

                public void onFault(
                    org.apache.axis2.context.MessageContext faultContext) {
                    org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                    onError(fault);
                }

                public void onComplete() {
                    // Do nothing by default
                }
            });

        org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;

        if ((_operations[1].getMessageReceiver() == null) &&
                _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
            _operations[1].setMessageReceiver(_callbackReceiver);
        }

        //execute the operation client
        _operationClient.execute(false);
    }

    /**
     * Auto generated method signature
     * @see edu.internet2.middleware.grouper.webservicesClient.GrouperService#findGroups
     * @param findGroups4
     */
    public edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse findGroups(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups4)
        throws java.rmi.RemoteException {
        try {
            org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
            _operationClient.getOptions().setAction("urn:findGroups");
            _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

            addPropertyToOperationClient(_operationClient,
                org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                "&");

            // create a message context
            org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

            // create SOAP envelope with that payload
            org.apache.axiom.soap.SOAPEnvelope env = null;

            env = toEnvelope(getFactory(_operationClient.getOptions()
                                                        .getSoapVersionURI()),
                    findGroups4,
                    optimizeContent(
                        new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/",
                            "findGroups")));

            //adding SOAP soap_headers
            _serviceClient.addHeadersToEnvelope(env);
            // set the message context with that soap envelope
            _messageContext.setEnvelope(env);

            // add the message contxt to the operation client
            _operationClient.addMessageContext(_messageContext);

            //execute the operation client
            _operationClient.execute(true);

            org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();

            java.lang.Object object = fromOM(_returnEnv.getBody()
                                                       .getFirstElement(),
                    edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse.class,
                    getEnvelopeNamespaces(_returnEnv));
            _messageContext.getTransportOut().getSender()
                           .cleanup(_messageContext);

            return (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse) object;
        } catch (org.apache.axis2.AxisFault f) {
            org.apache.axiom.om.OMElement faultElt = f.getDetail();

            if (faultElt != null) {
                if (faultExceptionNameMap.containsKey(faultElt.getQName())) {
                    //make the fault by reflection
                    try {
                        java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();

                        //message class
                        java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,
                                messageClass, null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                new java.lang.Class[] { messageClass });
                        m.invoke(ex, new java.lang.Object[] { messageObject });

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    } catch (java.lang.ClassCastException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                } else {
                    throw f;
                }
            } else {
                throw f;
            }
        }
    }

    /**
     * Auto generated method signature for Asynchronous Invocations
     * @see edu.internet2.middleware.grouper.webservicesClient.GrouperService#startfindGroups
     * @param findGroups4
     */
    public void startfindGroups(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups findGroups4,
        final edu.internet2.middleware.grouper.webservicesClient.GrouperServiceCallbackHandler callback)
        throws java.rmi.RemoteException {
        org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
        _operationClient.getOptions().setAction("urn:findGroups");
        _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

        addPropertyToOperationClient(_operationClient,
            org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
            "&");

        // create SOAP envelope with that payload
        org.apache.axiom.soap.SOAPEnvelope env = null;
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();

        //Style is Doc.
        env = toEnvelope(getFactory(_operationClient.getOptions()
                                                    .getSoapVersionURI()),
                findGroups4,
                optimizeContent(
                    new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/",
                        "findGroups")));

        // adding SOAP soap_headers
        _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        _messageContext.setEnvelope(env);

        // add the message context to the operation client
        _operationClient.addMessageContext(_messageContext);

        _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                public void onMessage(
                    org.apache.axis2.context.MessageContext resultContext) {
                    try {
                        org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();

                        java.lang.Object object = fromOM(resultEnv.getBody()
                                                                  .getFirstElement(),
                                edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse.class,
                                getEnvelopeNamespaces(resultEnv));
                        callback.receiveResultfindGroups((edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse) object);
                    } catch (org.apache.axis2.AxisFault e) {
                        callback.receiveErrorfindGroups(e);
                    }
                }

                public void onError(java.lang.Exception error) {
                    if (error instanceof org.apache.axis2.AxisFault) {
                        org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
                        org.apache.axiom.om.OMElement faultElt = f.getDetail();

                        if (faultElt != null) {
                            if (faultExceptionNameMap.containsKey(
                                        faultElt.getQName())) {
                                //make the fault by reflection
                                try {
                                    java.lang.String exceptionClassName = (java.lang.String) faultExceptionClassNameMap.get(faultElt.getQName());
                                    java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                                    java.lang.Exception ex = (java.lang.Exception) exceptionClass.newInstance();

                                    //message class
                                    java.lang.String messageClassName = (java.lang.String) faultMessageMap.get(faultElt.getQName());
                                    java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                                    java.lang.Object messageObject = fromOM(faultElt,
                                            messageClass, null);
                                    java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                            new java.lang.Class[] { messageClass });
                                    m.invoke(ex,
                                        new java.lang.Object[] { messageObject });

                                    callback.receiveErrorfindGroups(new java.rmi.RemoteException(
                                            ex.getMessage(), ex));
                                } catch (java.lang.ClassCastException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErrorfindGroups(f);
                                } catch (java.lang.ClassNotFoundException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErrorfindGroups(f);
                                } catch (java.lang.NoSuchMethodException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErrorfindGroups(f);
                                } catch (java.lang.reflect.InvocationTargetException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErrorfindGroups(f);
                                } catch (java.lang.IllegalAccessException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErrorfindGroups(f);
                                } catch (java.lang.InstantiationException e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErrorfindGroups(f);
                                } catch (org.apache.axis2.AxisFault e) {
                                    // we cannot intantiate the class - throw the original Axis fault
                                    callback.receiveErrorfindGroups(f);
                                }
                            } else {
                                callback.receiveErrorfindGroups(f);
                            }
                        } else {
                            callback.receiveErrorfindGroups(f);
                        }
                    } else {
                        callback.receiveErrorfindGroups(error);
                    }
                }

                public void onFault(
                    org.apache.axis2.context.MessageContext faultContext) {
                    org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                    onError(fault);
                }

                public void onComplete() {
                    // Do nothing by default
                }
            });

        org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;

        if ((_operations[2].getMessageReceiver() == null) &&
                _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
            _operations[2].setMessageReceiver(_callbackReceiver);
        }

        //execute the operation client
        _operationClient.execute(false);
    }

    /**
     *  A utility method that copies the namepaces from the SOAPEnvelope
     */
    private java.util.Map getEnvelopeNamespaces(
        org.apache.axiom.soap.SOAPEnvelope env) {
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();

        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }

        return returnMap;
    }

    private boolean optimizeContent(javax.xml.namespace.QName opName) {
        if (opNameArray == null) {
            return false;
        }

        for (int i = 0; i < opNameArray.length; i++) {
            if (opName.equals(opNameArray[i])) {
                return true;
            }
        }

        return false;
    }

    private org.apache.axiom.om.OMElement toOM(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.MY_QNAME,
                org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.om.OMElement toOM(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse.MY_QNAME,
                org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.om.OMElement toOM(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.MY_QNAME,
                org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.om.OMElement toOM(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse.MY_QNAME,
                org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.om.OMElement toOM(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.MY_QNAME,
                org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.om.OMElement toOM(
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            return param.getOMElement(edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse.MY_QNAME,
                org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
        org.apache.axiom.soap.SOAPFactory factory,
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
            emptyEnvelope.getBody()
                         .addChild(param.getOMElement(
                    edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.MY_QNAME,
                    factory));

            return emptyEnvelope;
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    /* methods to provide back word compatibility */
    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
        org.apache.axiom.soap.SOAPFactory factory,
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
            emptyEnvelope.getBody()
                         .addChild(param.getOMElement(
                    edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.MY_QNAME,
                    factory));

            return emptyEnvelope;
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    /* methods to provide back word compatibility */
    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
        org.apache.axiom.soap.SOAPFactory factory,
        edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember param,
        boolean optimizeContent) throws org.apache.axis2.AxisFault {
        try {
            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
            emptyEnvelope.getBody()
                         .addChild(param.getOMElement(
                    edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.MY_QNAME,
                    factory));

            return emptyEnvelope;
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    /* methods to provide back word compatibility */

    /**
     *  get the default envelope
     */
    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(
        org.apache.axiom.soap.SOAPFactory factory) {
        return factory.getDefaultEnvelope();
    }

    private java.lang.Object fromOM(org.apache.axiom.om.OMElement param,
        java.lang.Class type, java.util.Map extraNamespaces)
        throws org.apache.axis2.AxisFault {
        try {
            if (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.class.equals(
                        type)) {
                return edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimple.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }

            if (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse.class.equals(
                        type)) {
                return edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberSimpleResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }

            if (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.class.equals(
                        type)) {
                return edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroups.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }

            if (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse.class.equals(
                        type)) {
                return edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.FindGroupsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }

            if (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.class.equals(
                        type)) {
                return edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMember.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }

            if (edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse.class.equals(
                        type)) {
                return edu.internet2.middleware.grouper.webservicesClient.GrouperServiceStub.AddMemberResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
            }
        } catch (java.lang.Exception e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }

        return null;
    }

    //http://localhost:8090/grouper-ws/services/GrouperService
    public static class WsAddMemberResult extends WsResult implements org.apache.axis2.databinding.ADBBean {
        /**
         * field for SubjectId
         */
        protected java.lang.String localSubjectId;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectIdTracker = false;

        /**
         * field for SubjectIdentifier
         */
        protected java.lang.String localSubjectIdentifier;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectIdentifierTracker = false;

        /* This type was generated from the piece of schema that had
           name = WsAddMemberResult
           Namespace URI = http://webservices.grouper.middleware.internet2.edu/xsd
           Namespace Prefix = ns1
         */
        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSubjectId() {
            return localSubjectId;
        }

        /**
         * Auto generated setter method
         * @param param SubjectId
         */
        public void setSubjectId(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSubjectIdTracker = true;
            } else {
                localSubjectIdTracker = true;
            }

            this.localSubjectId = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSubjectIdentifier() {
            return localSubjectIdentifier;
        }

        /**
         * Auto generated setter method
         * @param param SubjectIdentifier
         */
        public void setSubjectIdentifier(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSubjectIdentifierTracker = true;
            } else {
                localSubjectIdentifierTracker = true;
            }

            this.localSubjectIdentifier = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    parentQName) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        WsAddMemberResult.this.serialize(parentQName, factory,
                            xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            java.lang.String namespacePrefix = registerPrefix(xmlWriter,
                    "http://webservices.grouper.middleware.internet2.edu/xsd");

            if ((namespacePrefix != null) &&
                    (namespacePrefix.trim().length() > 0)) {
                writeAttribute("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance", "type",
                    namespacePrefix + ":WsAddMemberResult", xmlWriter);
            } else {
                writeAttribute("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance", "type",
                    "WsAddMemberResult", xmlWriter);
            }

            if (localResultCodeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultCode",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultCode");
                    }
                } else {
                    xmlWriter.writeStartElement("resultCode");
                }

                if (localResultCode == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultCode);
                }

                xmlWriter.writeEndElement();
            }

            if (localResultMessageTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultMessage",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultMessage");
                    }
                } else {
                    xmlWriter.writeStartElement("resultMessage");
                }

                if (localResultMessage == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultMessage);
                }

                xmlWriter.writeEndElement();
            }

            if (localSuccessTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "success", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "success");
                    }
                } else {
                    xmlWriter.writeStartElement("success");
                }

                if (localSuccess == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSuccess);
                }

                xmlWriter.writeEndElement();
            }

            if (localSubjectIdTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "subjectId",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "subjectId");
                    }
                } else {
                    xmlWriter.writeStartElement("subjectId");
                }

                if (localSubjectId == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSubjectId);
                }

                xmlWriter.writeEndElement();
            }

            if (localSubjectIdentifierTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix,
                            "subjectIdentifier", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace,
                            "subjectIdentifier");
                    }
                } else {
                    xmlWriter.writeStartElement("subjectIdentifier");
                }

                if (localSubjectIdentifier == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSubjectIdentifier);
                }

                xmlWriter.writeEndElement();
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            attribList.add(new javax.xml.namespace.QName(
                    "http://www.w3.org/2001/XMLSchema-instance", "type"));
            attribList.add(new javax.xml.namespace.QName(
                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                    "WsAddMemberResult"));

            if (localResultCodeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultCode"));

                elementList.add((localResultCode == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultCode));
            }

            if (localResultMessageTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultMessage"));

                elementList.add((localResultMessage == null) ? null
                                                             : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultMessage));
            }

            if (localSuccessTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "success"));

                elementList.add((localSuccess == null) ? null
                                                       : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSuccess));
            }

            if (localSubjectIdTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "subjectId"));

                elementList.add((localSubjectId == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSubjectId));
            }

            if (localSubjectIdentifierTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "subjectIdentifier"));

                elementList.add((localSubjectIdentifier == null) ? null
                                                                 : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSubjectIdentifier));
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static WsAddMemberResult parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                WsAddMemberResult object = new WsAddMemberResult();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"WsAddMemberResult".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (WsAddMemberResult) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultCode").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultCode(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultMessage").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultMessage(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "success").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSuccess(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectId").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSubjectId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectIdentifier").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSubjectIdentifier(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class AddMemberResponse implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://webservices.grouper.middleware.internet2.edu/xsd",
                "addMemberResponse", "ns1");

        /**
         * field for _return
         */
        protected WsAddMemberResults local_return;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean local_returnTracker = false;

        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return WsAddMemberResults
         */
        public WsAddMemberResults get_return() {
            return local_return;
        }

        /**
         * Auto generated setter method
         * @param param _return
         */
        public void set_return(WsAddMemberResults param) {
            if (param != null) {
                //update the setting tracker
                local_returnTracker = true;
            } else {
                local_returnTracker = true;
            }

            this.local_return = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    MY_QNAME) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        AddMemberResponse.this.serialize(MY_QNAME, factory,
                            xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (local_returnTracker) {
                if (local_return == null) {
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "return",
                                namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2, "return");
                        }
                    } else {
                        xmlWriter.writeStartElement("return");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                } else {
                    local_return.serialize(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "return"), factory, xmlWriter);
                }
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (local_returnTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "return"));

                elementList.add((local_return == null) ? null : local_return);
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static AddMemberResponse parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                AddMemberResponse object = new AddMemberResponse();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"addMemberResponse".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (AddMemberResponse) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "return").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            object.set_return(null);
                            reader.next();

                            reader.next();
                        } else {
                            object.set_return(WsAddMemberResults.Factory.parse(
                                    reader));

                            reader.next();
                        }
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class WsSubjectLookup implements org.apache.axis2.databinding.ADBBean {
        /**
         * field for Blank
         */
        protected boolean localBlank;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localBlankTracker = false;

        /**
         * field for SubjectId
         */
        protected java.lang.String localSubjectId;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectIdTracker = false;

        /**
         * field for SubjectIdentifier
         */
        protected java.lang.String localSubjectIdentifier;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectIdentifierTracker = false;

        /**
         * field for SubjectSource
         */
        protected java.lang.String localSubjectSource;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectSourceTracker = false;

        /**
         * field for SubjectType
         */
        protected java.lang.String localSubjectType;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectTypeTracker = false;

        /* This type was generated from the piece of schema that had
           name = WsSubjectLookup
           Namespace URI = http://webservices.grouper.middleware.internet2.edu/xsd
           Namespace Prefix = ns1
         */
        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return boolean
         */
        public boolean getBlank() {
            return localBlank;
        }

        /**
         * Auto generated setter method
         * @param param Blank
         */
        public void setBlank(boolean param) {
            // setting primitive attribute tracker to true
            if (false) {
                localBlankTracker = false;
            } else {
                localBlankTracker = true;
            }

            this.localBlank = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSubjectId() {
            return localSubjectId;
        }

        /**
         * Auto generated setter method
         * @param param SubjectId
         */
        public void setSubjectId(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSubjectIdTracker = true;
            } else {
                localSubjectIdTracker = true;
            }

            this.localSubjectId = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSubjectIdentifier() {
            return localSubjectIdentifier;
        }

        /**
         * Auto generated setter method
         * @param param SubjectIdentifier
         */
        public void setSubjectIdentifier(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSubjectIdentifierTracker = true;
            } else {
                localSubjectIdentifierTracker = true;
            }

            this.localSubjectIdentifier = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSubjectSource() {
            return localSubjectSource;
        }

        /**
         * Auto generated setter method
         * @param param SubjectSource
         */
        public void setSubjectSource(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSubjectSourceTracker = true;
            } else {
                localSubjectSourceTracker = true;
            }

            this.localSubjectSource = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSubjectType() {
            return localSubjectType;
        }

        /**
         * Auto generated setter method
         * @param param SubjectType
         */
        public void setSubjectType(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSubjectTypeTracker = true;
            } else {
                localSubjectTypeTracker = true;
            }

            this.localSubjectType = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    parentQName) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        WsSubjectLookup.this.serialize(parentQName, factory,
                            xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (localBlankTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "blank", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "blank");
                    }
                } else {
                    xmlWriter.writeStartElement("blank");
                }

                if (false) {
                    throw new org.apache.axis2.databinding.ADBException(
                        "blank cannot be null!!");
                } else {
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            localBlank));
                }

                xmlWriter.writeEndElement();
            }

            if (localSubjectIdTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "subjectId",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "subjectId");
                    }
                } else {
                    xmlWriter.writeStartElement("subjectId");
                }

                if (localSubjectId == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSubjectId);
                }

                xmlWriter.writeEndElement();
            }

            if (localSubjectIdentifierTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix,
                            "subjectIdentifier", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace,
                            "subjectIdentifier");
                    }
                } else {
                    xmlWriter.writeStartElement("subjectIdentifier");
                }

                if (localSubjectIdentifier == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSubjectIdentifier);
                }

                xmlWriter.writeEndElement();
            }

            if (localSubjectSourceTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "subjectSource",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "subjectSource");
                    }
                } else {
                    xmlWriter.writeStartElement("subjectSource");
                }

                if (localSubjectSource == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSubjectSource);
                }

                xmlWriter.writeEndElement();
            }

            if (localSubjectTypeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "subjectType",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "subjectType");
                    }
                } else {
                    xmlWriter.writeStartElement("subjectType");
                }

                if (localSubjectType == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSubjectType);
                }

                xmlWriter.writeEndElement();
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (localBlankTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "blank"));

                elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localBlank));
            }

            if (localSubjectIdTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "subjectId"));

                elementList.add((localSubjectId == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSubjectId));
            }

            if (localSubjectIdentifierTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "subjectIdentifier"));

                elementList.add((localSubjectIdentifier == null) ? null
                                                                 : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSubjectIdentifier));
            }

            if (localSubjectSourceTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "subjectSource"));

                elementList.add((localSubjectSource == null) ? null
                                                             : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSubjectSource));
            }

            if (localSubjectTypeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "subjectType"));

                elementList.add((localSubjectType == null) ? null
                                                           : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSubjectType));
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static WsSubjectLookup parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                WsSubjectLookup object = new WsSubjectLookup();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"WsSubjectLookup".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (WsSubjectLookup) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "blank").equals(reader.getName())) {
                        java.lang.String content = reader.getElementText();

                        object.setBlank(org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(
                                content));

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectId").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSubjectId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectIdentifier").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSubjectIdentifier(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectSource").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSubjectSource(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectType").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSubjectType(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class WsAddMemberResults extends WsResult implements org.apache.axis2.databinding.ADBBean {
        /**
         * field for ResultMessage0
         */
        protected java.lang.String localResultMessage0;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localResultMessage0Tracker = false;

        /**
         * field for Results
         * This was an Array!
         */
        protected WsAddMemberResult[] localResults;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localResultsTracker = false;

        /* This type was generated from the piece of schema that had
           name = WsAddMemberResults
           Namespace URI = http://webservices.grouper.middleware.internet2.edu/xsd
           Namespace Prefix = ns1
         */
        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getResultMessage0() {
            return localResultMessage0;
        }

        /**
         * Auto generated setter method
         * @param param ResultMessage0
         */
        public void setResultMessage0(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localResultMessage0Tracker = true;
            } else {
                localResultMessage0Tracker = true;
            }

            this.localResultMessage0 = param;
        }

        /**
         * Auto generated getter method
         * @return WsAddMemberResult[]
         */
        public WsAddMemberResult[] getResults() {
            return localResults;
        }

        /**
         * validate the array for Results
         */
        protected void validateResults(WsAddMemberResult[] param) {
        }

        /**
         * Auto generated setter method
         * @param param Results
         */
        public void setResults(WsAddMemberResult[] param) {
            validateResults(param);

            if (param != null) {
                //update the setting tracker
                localResultsTracker = true;
            } else {
                localResultsTracker = true;
            }

            this.localResults = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * @param param WsAddMemberResult
         */
        public void addResults(WsAddMemberResult param) {
            if (localResults == null) {
                localResults = new WsAddMemberResult[] {  };
            }

            //update the setting tracker
            localResultsTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localResults);
            list.add(param);
            this.localResults = (WsAddMemberResult[]) list.toArray(new WsAddMemberResult[list.size()]);
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    parentQName) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        WsAddMemberResults.this.serialize(parentQName, factory,
                            xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            java.lang.String namespacePrefix = registerPrefix(xmlWriter,
                    "http://webservices.grouper.middleware.internet2.edu/xsd");

            if ((namespacePrefix != null) &&
                    (namespacePrefix.trim().length() > 0)) {
                writeAttribute("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance", "type",
                    namespacePrefix + ":WsAddMemberResults", xmlWriter);
            } else {
                writeAttribute("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance", "type",
                    "WsAddMemberResults", xmlWriter);
            }

            if (localResultCodeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultCode",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultCode");
                    }
                } else {
                    xmlWriter.writeStartElement("resultCode");
                }

                if (localResultCode == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultCode);
                }

                xmlWriter.writeEndElement();
            }

            if (localResultMessageTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultMessage",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultMessage");
                    }
                } else {
                    xmlWriter.writeStartElement("resultMessage");
                }

                if (localResultMessage == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultMessage);
                }

                xmlWriter.writeEndElement();
            }

            if (localSuccessTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "success", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "success");
                    }
                } else {
                    xmlWriter.writeStartElement("success");
                }

                if (localSuccess == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSuccess);
                }

                xmlWriter.writeEndElement();
            }

            if (localResultMessage0Tracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultMessage",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultMessage");
                    }
                } else {
                    xmlWriter.writeStartElement("resultMessage");
                }

                if (localResultMessage0 == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultMessage0);
                }

                xmlWriter.writeEndElement();
            }

            if (localResultsTracker) {
                if (localResults != null) {
                    for (int i = 0; i < localResults.length; i++) {
                        if (localResults[i] != null) {
                            localResults[i].serialize(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "results"), factory, xmlWriter);
                        } else {
                            // write null attribute
                            java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                            if (!namespace2.equals("")) {
                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                if (prefix2 == null) {
                                    prefix2 = generatePrefix(namespace2);

                                    xmlWriter.writeStartElement(prefix2,
                                        "results", namespace2);
                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                    xmlWriter.setPrefix(prefix2, namespace2);
                                } else {
                                    xmlWriter.writeStartElement(namespace2,
                                        "results");
                                }
                            } else {
                                xmlWriter.writeStartElement("results");
                            }

                            // write the nil attribute
                            writeAttribute("xsi",
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();
                        }
                    }
                } else {
                    // write null attribute
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "results",
                                namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2, "results");
                        }
                    } else {
                        xmlWriter.writeStartElement("results");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                }
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            attribList.add(new javax.xml.namespace.QName(
                    "http://www.w3.org/2001/XMLSchema-instance", "type"));
            attribList.add(new javax.xml.namespace.QName(
                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                    "WsAddMemberResults"));

            if (localResultCodeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultCode"));

                elementList.add((localResultCode == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultCode));
            }

            if (localResultMessageTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultMessage"));

                elementList.add((localResultMessage == null) ? null
                                                             : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultMessage));
            }

            if (localSuccessTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "success"));

                elementList.add((localSuccess == null) ? null
                                                       : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSuccess));
            }

            if (localResultMessage0Tracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultMessage"));

                elementList.add((localResultMessage0 == null) ? null
                                                              : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultMessage0));
            }

            if (localResultsTracker) {
                if (localResults != null) {
                    for (int i = 0; i < localResults.length; i++) {
                        if (localResults[i] != null) {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "results"));
                            elementList.add(localResults[i]);
                        } else {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "results"));
                            elementList.add(null);
                        }
                    }
                } else {
                    elementList.add(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "results"));
                    elementList.add(localResults);
                }
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static WsAddMemberResults parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                WsAddMemberResults object = new WsAddMemberResults();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"WsAddMemberResults".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (WsAddMemberResults) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    java.util.ArrayList list5 = new java.util.ArrayList();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultCode").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultCode(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultMessage").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultMessage(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "success").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSuccess(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultMessage").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultMessage0(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "results").equals(reader.getName())) {
                        // Process the array and step past its final element's end.
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            list5.add(null);
                            reader.next();
                        } else {
                            list5.add(WsAddMemberResult.Factory.parse(reader));
                        }

                        //loop until we find a start element that is not part of this array
                        boolean loopDone5 = false;

                        while (!loopDone5) {
                            // We should be at the end element, but make sure
                            while (!reader.isEndElement())
                                reader.next();

                            // Step out of this element
                            reader.next();

                            // Step to next element event.
                            while (!reader.isStartElement() &&
                                    !reader.isEndElement())
                                reader.next();

                            if (reader.isEndElement()) {
                                //two continuous end elements means we are exiting the xml structure
                                loopDone5 = true;
                            } else {
                                if (new javax.xml.namespace.QName(
                                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                                            "results").equals(reader.getName())) {
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                            "nil");

                                    if ("true".equals(nillableValue) ||
                                            "1".equals(nillableValue)) {
                                        list5.add(null);
                                        reader.next();
                                    } else {
                                        list5.add(WsAddMemberResult.Factory.parse(
                                                reader));
                                    }
                                } else {
                                    loopDone5 = true;
                                }
                            }
                        }

                        // call the converter utility  to convert and set the array
                        object.setResults((WsAddMemberResult[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                WsAddMemberResult.class, list5));
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class FindGroupsResponse implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://webservices.grouper.middleware.internet2.edu/xsd",
                "findGroupsResponse", "ns1");

        /**
         * field for _return
         */
        protected WsFindGroupsResults local_return;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean local_returnTracker = false;

        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return WsFindGroupsResults
         */
        public WsFindGroupsResults get_return() {
            return local_return;
        }

        /**
         * Auto generated setter method
         * @param param _return
         */
        public void set_return(WsFindGroupsResults param) {
            if (param != null) {
                //update the setting tracker
                local_returnTracker = true;
            } else {
                local_returnTracker = true;
            }

            this.local_return = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    MY_QNAME) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        FindGroupsResponse.this.serialize(MY_QNAME, factory,
                            xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (local_returnTracker) {
                if (local_return == null) {
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "return",
                                namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2, "return");
                        }
                    } else {
                        xmlWriter.writeStartElement("return");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                } else {
                    local_return.serialize(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "return"), factory, xmlWriter);
                }
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (local_returnTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "return"));

                elementList.add((local_return == null) ? null : local_return);
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static FindGroupsResponse parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                FindGroupsResponse object = new FindGroupsResponse();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"findGroupsResponse".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (FindGroupsResponse) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "return").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            object.set_return(null);
                            reader.next();

                            reader.next();
                        } else {
                            object.set_return(WsFindGroupsResults.Factory.parse(
                                    reader));

                            reader.next();
                        }
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class AddMemberSimple implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://webservices.grouper.middleware.internet2.edu/xsd",
                "addMemberSimple", "ns1");

        /**
         * field for GroupName
         */
        protected java.lang.String localGroupName;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localGroupNameTracker = false;

        /**
         * field for GroupUuid
         */
        protected java.lang.String localGroupUuid;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localGroupUuidTracker = false;

        /**
         * field for SubjectId
         */
        protected java.lang.String localSubjectId;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectIdTracker = false;

        /**
         * field for SubjectIdentifier
         */
        protected java.lang.String localSubjectIdentifier;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectIdentifierTracker = false;

        /**
         * field for ActAsSubjectId
         */
        protected java.lang.String localActAsSubjectId;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localActAsSubjectIdTracker = false;

        /**
         * field for ActAsSubjectIdentifier
         */
        protected java.lang.String localActAsSubjectIdentifier;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localActAsSubjectIdentifierTracker = false;

        /**
         * field for ParamName0
         */
        protected java.lang.String localParamName0;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParamName0Tracker = false;

        /**
         * field for ParamValue0
         */
        protected java.lang.String localParamValue0;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParamValue0Tracker = false;

        /**
         * field for ParamName1
         */
        protected java.lang.String localParamName1;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParamName1Tracker = false;

        /**
         * field for ParamValue1
         */
        protected java.lang.String localParamValue1;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParamValue1Tracker = false;

        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getGroupName() {
            return localGroupName;
        }

        /**
         * Auto generated setter method
         * @param param GroupName
         */
        public void setGroupName(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localGroupNameTracker = true;
            } else {
                localGroupNameTracker = true;
            }

            this.localGroupName = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getGroupUuid() {
            return localGroupUuid;
        }

        /**
         * Auto generated setter method
         * @param param GroupUuid
         */
        public void setGroupUuid(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localGroupUuidTracker = true;
            } else {
                localGroupUuidTracker = true;
            }

            this.localGroupUuid = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSubjectId() {
            return localSubjectId;
        }

        /**
         * Auto generated setter method
         * @param param SubjectId
         */
        public void setSubjectId(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSubjectIdTracker = true;
            } else {
                localSubjectIdTracker = true;
            }

            this.localSubjectId = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSubjectIdentifier() {
            return localSubjectIdentifier;
        }

        /**
         * Auto generated setter method
         * @param param SubjectIdentifier
         */
        public void setSubjectIdentifier(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSubjectIdentifierTracker = true;
            } else {
                localSubjectIdentifierTracker = true;
            }

            this.localSubjectIdentifier = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getActAsSubjectId() {
            return localActAsSubjectId;
        }

        /**
         * Auto generated setter method
         * @param param ActAsSubjectId
         */
        public void setActAsSubjectId(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localActAsSubjectIdTracker = true;
            } else {
                localActAsSubjectIdTracker = true;
            }

            this.localActAsSubjectId = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getActAsSubjectIdentifier() {
            return localActAsSubjectIdentifier;
        }

        /**
         * Auto generated setter method
         * @param param ActAsSubjectIdentifier
         */
        public void setActAsSubjectIdentifier(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localActAsSubjectIdentifierTracker = true;
            } else {
                localActAsSubjectIdentifierTracker = true;
            }

            this.localActAsSubjectIdentifier = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getParamName0() {
            return localParamName0;
        }

        /**
         * Auto generated setter method
         * @param param ParamName0
         */
        public void setParamName0(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localParamName0Tracker = true;
            } else {
                localParamName0Tracker = true;
            }

            this.localParamName0 = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getParamValue0() {
            return localParamValue0;
        }

        /**
         * Auto generated setter method
         * @param param ParamValue0
         */
        public void setParamValue0(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localParamValue0Tracker = true;
            } else {
                localParamValue0Tracker = true;
            }

            this.localParamValue0 = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getParamName1() {
            return localParamName1;
        }

        /**
         * Auto generated setter method
         * @param param ParamName1
         */
        public void setParamName1(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localParamName1Tracker = true;
            } else {
                localParamName1Tracker = true;
            }

            this.localParamName1 = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getParamValue1() {
            return localParamValue1;
        }

        /**
         * Auto generated setter method
         * @param param ParamValue1
         */
        public void setParamValue1(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localParamValue1Tracker = true;
            } else {
                localParamValue1Tracker = true;
            }

            this.localParamValue1 = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    MY_QNAME) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        AddMemberSimple.this.serialize(MY_QNAME, factory,
                            xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (localGroupNameTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "groupName",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "groupName");
                    }
                } else {
                    xmlWriter.writeStartElement("groupName");
                }

                if (localGroupName == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localGroupName);
                }

                xmlWriter.writeEndElement();
            }

            if (localGroupUuidTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "groupUuid",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "groupUuid");
                    }
                } else {
                    xmlWriter.writeStartElement("groupUuid");
                }

                if (localGroupUuid == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localGroupUuid);
                }

                xmlWriter.writeEndElement();
            }

            if (localSubjectIdTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "subjectId",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "subjectId");
                    }
                } else {
                    xmlWriter.writeStartElement("subjectId");
                }

                if (localSubjectId == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSubjectId);
                }

                xmlWriter.writeEndElement();
            }

            if (localSubjectIdentifierTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix,
                            "subjectIdentifier", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace,
                            "subjectIdentifier");
                    }
                } else {
                    xmlWriter.writeStartElement("subjectIdentifier");
                }

                if (localSubjectIdentifier == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSubjectIdentifier);
                }

                xmlWriter.writeEndElement();
            }

            if (localActAsSubjectIdTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "actAsSubjectId",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "actAsSubjectId");
                    }
                } else {
                    xmlWriter.writeStartElement("actAsSubjectId");
                }

                if (localActAsSubjectId == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localActAsSubjectId);
                }

                xmlWriter.writeEndElement();
            }

            if (localActAsSubjectIdentifierTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix,
                            "actAsSubjectIdentifier", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace,
                            "actAsSubjectIdentifier");
                    }
                } else {
                    xmlWriter.writeStartElement("actAsSubjectIdentifier");
                }

                if (localActAsSubjectIdentifier == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localActAsSubjectIdentifier);
                }

                xmlWriter.writeEndElement();
            }

            if (localParamName0Tracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "paramName0",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "paramName0");
                    }
                } else {
                    xmlWriter.writeStartElement("paramName0");
                }

                if (localParamName0 == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localParamName0);
                }

                xmlWriter.writeEndElement();
            }

            if (localParamValue0Tracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "paramValue0",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "paramValue0");
                    }
                } else {
                    xmlWriter.writeStartElement("paramValue0");
                }

                if (localParamValue0 == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localParamValue0);
                }

                xmlWriter.writeEndElement();
            }

            if (localParamName1Tracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "paramName1",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "paramName1");
                    }
                } else {
                    xmlWriter.writeStartElement("paramName1");
                }

                if (localParamName1 == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localParamName1);
                }

                xmlWriter.writeEndElement();
            }

            if (localParamValue1Tracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "paramValue1",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "paramValue1");
                    }
                } else {
                    xmlWriter.writeStartElement("paramValue1");
                }

                if (localParamValue1 == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localParamValue1);
                }

                xmlWriter.writeEndElement();
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (localGroupNameTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "groupName"));

                elementList.add((localGroupName == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localGroupName));
            }

            if (localGroupUuidTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "groupUuid"));

                elementList.add((localGroupUuid == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localGroupUuid));
            }

            if (localSubjectIdTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "subjectId"));

                elementList.add((localSubjectId == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSubjectId));
            }

            if (localSubjectIdentifierTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "subjectIdentifier"));

                elementList.add((localSubjectIdentifier == null) ? null
                                                                 : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSubjectIdentifier));
            }

            if (localActAsSubjectIdTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "actAsSubjectId"));

                elementList.add((localActAsSubjectId == null) ? null
                                                              : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localActAsSubjectId));
            }

            if (localActAsSubjectIdentifierTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "actAsSubjectIdentifier"));

                elementList.add((localActAsSubjectIdentifier == null) ? null
                                                                      : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localActAsSubjectIdentifier));
            }

            if (localParamName0Tracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "paramName0"));

                elementList.add((localParamName0 == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localParamName0));
            }

            if (localParamValue0Tracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "paramValue0"));

                elementList.add((localParamValue0 == null) ? null
                                                           : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localParamValue0));
            }

            if (localParamName1Tracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "paramName1"));

                elementList.add((localParamName1 == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localParamName1));
            }

            if (localParamValue1Tracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "paramValue1"));

                elementList.add((localParamValue1 == null) ? null
                                                           : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localParamValue1));
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static AddMemberSimple parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                AddMemberSimple object = new AddMemberSimple();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"addMemberSimple".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (AddMemberSimple) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "groupName").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setGroupName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "groupUuid").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setGroupUuid(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectId").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSubjectId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectIdentifier").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSubjectIdentifier(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "actAsSubjectId").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setActAsSubjectId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "actAsSubjectIdentifier").equals(
                                reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setActAsSubjectIdentifier(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "paramName0").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setParamName0(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "paramValue0").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setParamValue0(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "paramName1").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setParamName1(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "paramValue1").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setParamValue1(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class WsFindGroupsResults extends WsResult implements org.apache.axis2.databinding.ADBBean {
        /**
         * field for GroupResults
         * This was an Array!
         */
        protected WsGroupResult[] localGroupResults;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localGroupResultsTracker = false;

        /* This type was generated from the piece of schema that had
           name = WsFindGroupsResults
           Namespace URI = http://webservices.grouper.middleware.internet2.edu/xsd
           Namespace Prefix = ns1
         */
        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return WsGroupResult[]
         */
        public WsGroupResult[] getGroupResults() {
            return localGroupResults;
        }

        /**
         * validate the array for GroupResults
         */
        protected void validateGroupResults(WsGroupResult[] param) {
        }

        /**
         * Auto generated setter method
         * @param param GroupResults
         */
        public void setGroupResults(WsGroupResult[] param) {
            validateGroupResults(param);

            if (param != null) {
                //update the setting tracker
                localGroupResultsTracker = true;
            } else {
                localGroupResultsTracker = true;
            }

            this.localGroupResults = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * @param param WsGroupResult
         */
        public void addGroupResults(WsGroupResult param) {
            if (localGroupResults == null) {
                localGroupResults = new WsGroupResult[] {  };
            }

            //update the setting tracker
            localGroupResultsTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localGroupResults);
            list.add(param);
            this.localGroupResults = (WsGroupResult[]) list.toArray(new WsGroupResult[list.size()]);
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    parentQName) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        WsFindGroupsResults.this.serialize(parentQName,
                            factory, xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            java.lang.String namespacePrefix = registerPrefix(xmlWriter,
                    "http://webservices.grouper.middleware.internet2.edu/xsd");

            if ((namespacePrefix != null) &&
                    (namespacePrefix.trim().length() > 0)) {
                writeAttribute("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance", "type",
                    namespacePrefix + ":WsFindGroupsResults", xmlWriter);
            } else {
                writeAttribute("xsi",
                    "http://www.w3.org/2001/XMLSchema-instance", "type",
                    "WsFindGroupsResults", xmlWriter);
            }

            if (localResultCodeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultCode",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultCode");
                    }
                } else {
                    xmlWriter.writeStartElement("resultCode");
                }

                if (localResultCode == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultCode);
                }

                xmlWriter.writeEndElement();
            }

            if (localResultMessageTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultMessage",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultMessage");
                    }
                } else {
                    xmlWriter.writeStartElement("resultMessage");
                }

                if (localResultMessage == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultMessage);
                }

                xmlWriter.writeEndElement();
            }

            if (localSuccessTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "success", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "success");
                    }
                } else {
                    xmlWriter.writeStartElement("success");
                }

                if (localSuccess == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSuccess);
                }

                xmlWriter.writeEndElement();
            }

            if (localGroupResultsTracker) {
                if (localGroupResults != null) {
                    for (int i = 0; i < localGroupResults.length; i++) {
                        if (localGroupResults[i] != null) {
                            localGroupResults[i].serialize(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "groupResults"), factory, xmlWriter);
                        } else {
                            // write null attribute
                            java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                            if (!namespace2.equals("")) {
                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                if (prefix2 == null) {
                                    prefix2 = generatePrefix(namespace2);

                                    xmlWriter.writeStartElement(prefix2,
                                        "groupResults", namespace2);
                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                    xmlWriter.setPrefix(prefix2, namespace2);
                                } else {
                                    xmlWriter.writeStartElement(namespace2,
                                        "groupResults");
                                }
                            } else {
                                xmlWriter.writeStartElement("groupResults");
                            }

                            // write the nil attribute
                            writeAttribute("xsi",
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();
                        }
                    }
                } else {
                    // write null attribute
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2,
                                "groupResults", namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2,
                                "groupResults");
                        }
                    } else {
                        xmlWriter.writeStartElement("groupResults");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                }
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            attribList.add(new javax.xml.namespace.QName(
                    "http://www.w3.org/2001/XMLSchema-instance", "type"));
            attribList.add(new javax.xml.namespace.QName(
                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                    "WsFindGroupsResults"));

            if (localResultCodeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultCode"));

                elementList.add((localResultCode == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultCode));
            }

            if (localResultMessageTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultMessage"));

                elementList.add((localResultMessage == null) ? null
                                                             : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultMessage));
            }

            if (localSuccessTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "success"));

                elementList.add((localSuccess == null) ? null
                                                       : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSuccess));
            }

            if (localGroupResultsTracker) {
                if (localGroupResults != null) {
                    for (int i = 0; i < localGroupResults.length; i++) {
                        if (localGroupResults[i] != null) {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "groupResults"));
                            elementList.add(localGroupResults[i]);
                        } else {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "groupResults"));
                            elementList.add(null);
                        }
                    }
                } else {
                    elementList.add(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "groupResults"));
                    elementList.add(localGroupResults);
                }
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static WsFindGroupsResults parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                WsFindGroupsResults object = new WsFindGroupsResults();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"WsFindGroupsResults".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (WsFindGroupsResults) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    java.util.ArrayList list4 = new java.util.ArrayList();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultCode").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultCode(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultMessage").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultMessage(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "success").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSuccess(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "groupResults").equals(reader.getName())) {
                        // Process the array and step past its final element's end.
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            list4.add(null);
                            reader.next();
                        } else {
                            list4.add(WsGroupResult.Factory.parse(reader));
                        }

                        //loop until we find a start element that is not part of this array
                        boolean loopDone4 = false;

                        while (!loopDone4) {
                            // We should be at the end element, but make sure
                            while (!reader.isEndElement())
                                reader.next();

                            // Step out of this element
                            reader.next();

                            // Step to next element event.
                            while (!reader.isStartElement() &&
                                    !reader.isEndElement())
                                reader.next();

                            if (reader.isEndElement()) {
                                //two continuous end elements means we are exiting the xml structure
                                loopDone4 = true;
                            } else {
                                if (new javax.xml.namespace.QName(
                                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                                            "groupResults").equals(
                                            reader.getName())) {
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                            "nil");

                                    if ("true".equals(nillableValue) ||
                                            "1".equals(nillableValue)) {
                                        list4.add(null);
                                        reader.next();
                                    } else {
                                        list4.add(WsGroupResult.Factory.parse(
                                                reader));
                                    }
                                } else {
                                    loopDone4 = true;
                                }
                            }
                        }

                        // call the converter utility  to convert and set the array
                        object.setGroupResults((WsGroupResult[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                WsGroupResult.class, list4));
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class FindGroups implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://webservices.grouper.middleware.internet2.edu/xsd",
                "findGroups", "ns1");

        /**
         * field for GroupName
         */
        protected java.lang.String localGroupName;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localGroupNameTracker = false;

        /**
         * field for StemName
         */
        protected java.lang.String localStemName;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localStemNameTracker = false;

        /**
         * field for StemNameScope
         */
        protected java.lang.String localStemNameScope;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localStemNameScopeTracker = false;

        /**
         * field for GroupUuid
         */
        protected java.lang.String localGroupUuid;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localGroupUuidTracker = false;

        /**
         * field for QueryTerm
         */
        protected java.lang.String localQueryTerm;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localQueryTermTracker = false;

        /**
         * field for QuerySearchFromStemName
         */
        protected java.lang.String localQuerySearchFromStemName;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localQuerySearchFromStemNameTracker = false;

        /**
         * field for QueryScope
         */
        protected java.lang.String localQueryScope;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localQueryScopeTracker = false;

        /**
         * field for ActAsSubjectLookup
         */
        protected WsSubjectLookup localActAsSubjectLookup;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localActAsSubjectLookupTracker = false;

        /**
         * field for ParamNames
         * This was an Array!
         */
        protected java.lang.String[] localParamNames;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParamNamesTracker = false;

        /**
         * field for ParamValues
         * This was an Array!
         */
        protected java.lang.String[] localParamValues;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParamValuesTracker = false;

        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getGroupName() {
            return localGroupName;
        }

        /**
         * Auto generated setter method
         * @param param GroupName
         */
        public void setGroupName(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localGroupNameTracker = true;
            } else {
                localGroupNameTracker = true;
            }

            this.localGroupName = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getStemName() {
            return localStemName;
        }

        /**
         * Auto generated setter method
         * @param param StemName
         */
        public void setStemName(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localStemNameTracker = true;
            } else {
                localStemNameTracker = true;
            }

            this.localStemName = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getStemNameScope() {
            return localStemNameScope;
        }

        /**
         * Auto generated setter method
         * @param param StemNameScope
         */
        public void setStemNameScope(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localStemNameScopeTracker = true;
            } else {
                localStemNameScopeTracker = true;
            }

            this.localStemNameScope = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getGroupUuid() {
            return localGroupUuid;
        }

        /**
         * Auto generated setter method
         * @param param GroupUuid
         */
        public void setGroupUuid(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localGroupUuidTracker = true;
            } else {
                localGroupUuidTracker = true;
            }

            this.localGroupUuid = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getQueryTerm() {
            return localQueryTerm;
        }

        /**
         * Auto generated setter method
         * @param param QueryTerm
         */
        public void setQueryTerm(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localQueryTermTracker = true;
            } else {
                localQueryTermTracker = true;
            }

            this.localQueryTerm = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getQuerySearchFromStemName() {
            return localQuerySearchFromStemName;
        }

        /**
         * Auto generated setter method
         * @param param QuerySearchFromStemName
         */
        public void setQuerySearchFromStemName(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localQuerySearchFromStemNameTracker = true;
            } else {
                localQuerySearchFromStemNameTracker = true;
            }

            this.localQuerySearchFromStemName = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getQueryScope() {
            return localQueryScope;
        }

        /**
         * Auto generated setter method
         * @param param QueryScope
         */
        public void setQueryScope(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localQueryScopeTracker = true;
            } else {
                localQueryScopeTracker = true;
            }

            this.localQueryScope = param;
        }

        /**
         * Auto generated getter method
         * @return WsSubjectLookup
         */
        public WsSubjectLookup getActAsSubjectLookup() {
            return localActAsSubjectLookup;
        }

        /**
         * Auto generated setter method
         * @param param ActAsSubjectLookup
         */
        public void setActAsSubjectLookup(WsSubjectLookup param) {
            if (param != null) {
                //update the setting tracker
                localActAsSubjectLookupTracker = true;
            } else {
                localActAsSubjectLookupTracker = true;
            }

            this.localActAsSubjectLookup = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String[]
         */
        public java.lang.String[] getParamNames() {
            return localParamNames;
        }

        /**
         * validate the array for ParamNames
         */
        protected void validateParamNames(java.lang.String[] param) {
        }

        /**
         * Auto generated setter method
         * @param param ParamNames
         */
        public void setParamNames(java.lang.String[] param) {
            validateParamNames(param);

            if (param != null) {
                //update the setting tracker
                localParamNamesTracker = true;
            } else {
                localParamNamesTracker = true;
            }

            this.localParamNames = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * @param param java.lang.String
         */
        public void addParamNames(java.lang.String param) {
            if (localParamNames == null) {
                localParamNames = new java.lang.String[] {  };
            }

            //update the setting tracker
            localParamNamesTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localParamNames);
            list.add(param);
            this.localParamNames = (java.lang.String[]) list.toArray(new java.lang.String[list.size()]);
        }

        /**
         * Auto generated getter method
         * @return java.lang.String[]
         */
        public java.lang.String[] getParamValues() {
            return localParamValues;
        }

        /**
         * validate the array for ParamValues
         */
        protected void validateParamValues(java.lang.String[] param) {
        }

        /**
         * Auto generated setter method
         * @param param ParamValues
         */
        public void setParamValues(java.lang.String[] param) {
            validateParamValues(param);

            if (param != null) {
                //update the setting tracker
                localParamValuesTracker = true;
            } else {
                localParamValuesTracker = true;
            }

            this.localParamValues = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * @param param java.lang.String
         */
        public void addParamValues(java.lang.String param) {
            if (localParamValues == null) {
                localParamValues = new java.lang.String[] {  };
            }

            //update the setting tracker
            localParamValuesTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localParamValues);
            list.add(param);
            this.localParamValues = (java.lang.String[]) list.toArray(new java.lang.String[list.size()]);
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    MY_QNAME) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        FindGroups.this.serialize(MY_QNAME, factory, xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (localGroupNameTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "groupName",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "groupName");
                    }
                } else {
                    xmlWriter.writeStartElement("groupName");
                }

                if (localGroupName == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localGroupName);
                }

                xmlWriter.writeEndElement();
            }

            if (localStemNameTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "stemName",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "stemName");
                    }
                } else {
                    xmlWriter.writeStartElement("stemName");
                }

                if (localStemName == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localStemName);
                }

                xmlWriter.writeEndElement();
            }

            if (localStemNameScopeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "stemNameScope",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "stemNameScope");
                    }
                } else {
                    xmlWriter.writeStartElement("stemNameScope");
                }

                if (localStemNameScope == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localStemNameScope);
                }

                xmlWriter.writeEndElement();
            }

            if (localGroupUuidTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "groupUuid",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "groupUuid");
                    }
                } else {
                    xmlWriter.writeStartElement("groupUuid");
                }

                if (localGroupUuid == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localGroupUuid);
                }

                xmlWriter.writeEndElement();
            }

            if (localQueryTermTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "queryTerm",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "queryTerm");
                    }
                } else {
                    xmlWriter.writeStartElement("queryTerm");
                }

                if (localQueryTerm == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localQueryTerm);
                }

                xmlWriter.writeEndElement();
            }

            if (localQuerySearchFromStemNameTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix,
                            "querySearchFromStemName", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace,
                            "querySearchFromStemName");
                    }
                } else {
                    xmlWriter.writeStartElement("querySearchFromStemName");
                }

                if (localQuerySearchFromStemName == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localQuerySearchFromStemName);
                }

                xmlWriter.writeEndElement();
            }

            if (localQueryScopeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "queryScope",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "queryScope");
                    }
                } else {
                    xmlWriter.writeStartElement("queryScope");
                }

                if (localQueryScope == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localQueryScope);
                }

                xmlWriter.writeEndElement();
            }

            if (localActAsSubjectLookupTracker) {
                if (localActAsSubjectLookup == null) {
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2,
                                "actAsSubjectLookup", namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2,
                                "actAsSubjectLookup");
                        }
                    } else {
                        xmlWriter.writeStartElement("actAsSubjectLookup");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                } else {
                    localActAsSubjectLookup.serialize(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "actAsSubjectLookup"), factory, xmlWriter);
                }
            }

            if (localParamNamesTracker) {
                if (localParamNames != null) {
                    namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    boolean emptyNamespace = (namespace == null) ||
                        (namespace.length() == 0);
                    prefix = emptyNamespace ? null
                                            : xmlWriter.getPrefix(namespace);

                    for (int i = 0; i < localParamNames.length; i++) {
                        if (localParamNames[i] != null) {
                            if (!emptyNamespace) {
                                if (prefix == null) {
                                    java.lang.String prefix2 = generatePrefix(namespace);

                                    xmlWriter.writeStartElement(prefix2,
                                        "paramNames", namespace);
                                    xmlWriter.writeNamespace(prefix2, namespace);
                                    xmlWriter.setPrefix(prefix2, namespace);
                                } else {
                                    xmlWriter.writeStartElement(namespace,
                                        "paramNames");
                                }
                            } else {
                                xmlWriter.writeStartElement("paramNames");
                            }

                            xmlWriter.writeCharacters(localParamNames[i]);

                            xmlWriter.writeEndElement();
                        } else {
                            // write null attribute
                            namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                            if (!namespace.equals("")) {
                                prefix = xmlWriter.getPrefix(namespace);

                                if (prefix == null) {
                                    prefix = generatePrefix(namespace);

                                    xmlWriter.writeStartElement(prefix,
                                        "paramNames", namespace);
                                    xmlWriter.writeNamespace(prefix, namespace);
                                    xmlWriter.setPrefix(prefix, namespace);
                                } else {
                                    xmlWriter.writeStartElement(namespace,
                                        "paramNames");
                                }
                            } else {
                                xmlWriter.writeStartElement("paramNames");
                            }

                            writeAttribute("xsi",
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();
                        }
                    }
                } else {
                    // write the null attribute
                    // write null attribute
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "paramNames",
                                namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2, "paramNames");
                        }
                    } else {
                        xmlWriter.writeStartElement("paramNames");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                }
            }

            if (localParamValuesTracker) {
                if (localParamValues != null) {
                    namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    boolean emptyNamespace = (namespace == null) ||
                        (namespace.length() == 0);
                    prefix = emptyNamespace ? null
                                            : xmlWriter.getPrefix(namespace);

                    for (int i = 0; i < localParamValues.length; i++) {
                        if (localParamValues[i] != null) {
                            if (!emptyNamespace) {
                                if (prefix == null) {
                                    java.lang.String prefix2 = generatePrefix(namespace);

                                    xmlWriter.writeStartElement(prefix2,
                                        "paramValues", namespace);
                                    xmlWriter.writeNamespace(prefix2, namespace);
                                    xmlWriter.setPrefix(prefix2, namespace);
                                } else {
                                    xmlWriter.writeStartElement(namespace,
                                        "paramValues");
                                }
                            } else {
                                xmlWriter.writeStartElement("paramValues");
                            }

                            xmlWriter.writeCharacters(localParamValues[i]);

                            xmlWriter.writeEndElement();
                        } else {
                            // write null attribute
                            namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                            if (!namespace.equals("")) {
                                prefix = xmlWriter.getPrefix(namespace);

                                if (prefix == null) {
                                    prefix = generatePrefix(namespace);

                                    xmlWriter.writeStartElement(prefix,
                                        "paramValues", namespace);
                                    xmlWriter.writeNamespace(prefix, namespace);
                                    xmlWriter.setPrefix(prefix, namespace);
                                } else {
                                    xmlWriter.writeStartElement(namespace,
                                        "paramValues");
                                }
                            } else {
                                xmlWriter.writeStartElement("paramValues");
                            }

                            writeAttribute("xsi",
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();
                        }
                    }
                } else {
                    // write the null attribute
                    // write null attribute
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "paramValues",
                                namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2,
                                "paramValues");
                        }
                    } else {
                        xmlWriter.writeStartElement("paramValues");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                }
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (localGroupNameTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "groupName"));

                elementList.add((localGroupName == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localGroupName));
            }

            if (localStemNameTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "stemName"));

                elementList.add((localStemName == null) ? null
                                                        : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localStemName));
            }

            if (localStemNameScopeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "stemNameScope"));

                elementList.add((localStemNameScope == null) ? null
                                                             : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localStemNameScope));
            }

            if (localGroupUuidTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "groupUuid"));

                elementList.add((localGroupUuid == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localGroupUuid));
            }

            if (localQueryTermTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "queryTerm"));

                elementList.add((localQueryTerm == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localQueryTerm));
            }

            if (localQuerySearchFromStemNameTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "querySearchFromStemName"));

                elementList.add((localQuerySearchFromStemName == null) ? null
                                                                       : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localQuerySearchFromStemName));
            }

            if (localQueryScopeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "queryScope"));

                elementList.add((localQueryScope == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localQueryScope));
            }

            if (localActAsSubjectLookupTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "actAsSubjectLookup"));

                elementList.add((localActAsSubjectLookup == null) ? null
                                                                  : localActAsSubjectLookup);
            }

            if (localParamNamesTracker) {
                if (localParamNames != null) {
                    for (int i = 0; i < localParamNames.length; i++) {
                        if (localParamNames[i] != null) {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "paramNames"));
                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    localParamNames[i]));
                        } else {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "paramNames"));
                            elementList.add(null);
                        }
                    }
                } else {
                    elementList.add(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "paramNames"));
                    elementList.add(null);
                }
            }

            if (localParamValuesTracker) {
                if (localParamValues != null) {
                    for (int i = 0; i < localParamValues.length; i++) {
                        if (localParamValues[i] != null) {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "paramValues"));
                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    localParamValues[i]));
                        } else {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "paramValues"));
                            elementList.add(null);
                        }
                    }
                } else {
                    elementList.add(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "paramValues"));
                    elementList.add(null);
                }
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static FindGroups parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                FindGroups object = new FindGroups();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"findGroups".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (FindGroups) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    java.util.ArrayList list9 = new java.util.ArrayList();

                    java.util.ArrayList list10 = new java.util.ArrayList();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "groupName").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setGroupName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "stemName").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setStemName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "stemNameScope").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setStemNameScope(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "groupUuid").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setGroupUuid(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "queryTerm").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setQueryTerm(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "querySearchFromStemName").equals(
                                reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setQuerySearchFromStemName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "queryScope").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setQueryScope(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "actAsSubjectLookup").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            object.setActAsSubjectLookup(null);
                            reader.next();

                            reader.next();
                        } else {
                            object.setActAsSubjectLookup(WsSubjectLookup.Factory.parse(
                                    reader));

                            reader.next();
                        }
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "paramNames").equals(reader.getName())) {
                        // Process the array and step past its final element's end.
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            list9.add(null);

                            reader.next();
                        } else {
                            list9.add(reader.getElementText());
                        }

                        //loop until we find a start element that is not part of this array
                        boolean loopDone9 = false;

                        while (!loopDone9) {
                            // Ensure we are at the EndElement
                            while (!reader.isEndElement()) {
                                reader.next();
                            }

                            // Step out of this element
                            reader.next();

                            // Step to next element event.
                            while (!reader.isStartElement() &&
                                    !reader.isEndElement())
                                reader.next();

                            if (reader.isEndElement()) {
                                //two continuous end elements means we are exiting the xml structure
                                loopDone9 = true;
                            } else {
                                if (new javax.xml.namespace.QName(
                                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                                            "paramNames").equals(
                                            reader.getName())) {
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                            "nil");

                                    if ("true".equals(nillableValue) ||
                                            "1".equals(nillableValue)) {
                                        list9.add(null);

                                        reader.next();
                                    } else {
                                        list9.add(reader.getElementText());
                                    }
                                } else {
                                    loopDone9 = true;
                                }
                            }
                        }

                        // call the converter utility  to convert and set the array
                        object.setParamNames((java.lang.String[]) list9.toArray(
                                new java.lang.String[list9.size()]));
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "paramValues").equals(reader.getName())) {
                        // Process the array and step past its final element's end.
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            list10.add(null);

                            reader.next();
                        } else {
                            list10.add(reader.getElementText());
                        }

                        //loop until we find a start element that is not part of this array
                        boolean loopDone10 = false;

                        while (!loopDone10) {
                            // Ensure we are at the EndElement
                            while (!reader.isEndElement()) {
                                reader.next();
                            }

                            // Step out of this element
                            reader.next();

                            // Step to next element event.
                            while (!reader.isStartElement() &&
                                    !reader.isEndElement())
                                reader.next();

                            if (reader.isEndElement()) {
                                //two continuous end elements means we are exiting the xml structure
                                loopDone10 = true;
                            } else {
                                if (new javax.xml.namespace.QName(
                                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                                            "paramValues").equals(
                                            reader.getName())) {
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                            "nil");

                                    if ("true".equals(nillableValue) ||
                                            "1".equals(nillableValue)) {
                                        list10.add(null);

                                        reader.next();
                                    } else {
                                        list10.add(reader.getElementText());
                                    }
                                } else {
                                    loopDone10 = true;
                                }
                            }
                        }

                        // call the converter utility  to convert and set the array
                        object.setParamValues((java.lang.String[]) list10.toArray(
                                new java.lang.String[list10.size()]));
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class ExtensionMapper {
        public static java.lang.Object getTypeObject(
            java.lang.String namespaceURI, java.lang.String typeName,
            javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
            if ("http://webservices.grouper.middleware.internet2.edu/xsd".equals(
                        namespaceURI) && "WsResult".equals(typeName)) {
                return WsResult.Factory.parse(reader);
            }

            if ("http://webservices.grouper.middleware.internet2.edu/xsd".equals(
                        namespaceURI) && "WsAddMemberResult".equals(typeName)) {
                return WsAddMemberResult.Factory.parse(reader);
            }

            if ("http://webservices.grouper.middleware.internet2.edu/xsd".equals(
                        namespaceURI) && "WsAddMemberResults".equals(typeName)) {
                return WsAddMemberResults.Factory.parse(reader);
            }

            if ("http://webservices.grouper.middleware.internet2.edu/xsd".equals(
                        namespaceURI) && "WsSubjectLookup".equals(typeName)) {
                return WsSubjectLookup.Factory.parse(reader);
            }

            if ("http://webservices.grouper.middleware.internet2.edu/xsd".equals(
                        namespaceURI) && "WsGroupLookup".equals(typeName)) {
                return WsGroupLookup.Factory.parse(reader);
            }

            if ("http://webservices.grouper.middleware.internet2.edu/xsd".equals(
                        namespaceURI) &&
                    "WsFindGroupsResults".equals(typeName)) {
                return WsFindGroupsResults.Factory.parse(reader);
            }

            if ("http://webservices.grouper.middleware.internet2.edu/xsd".equals(
                        namespaceURI) && "WsGroupResult".equals(typeName)) {
                return WsGroupResult.Factory.parse(reader);
            }

            throw new org.apache.axis2.databinding.ADBException(
                "Unsupported type " + namespaceURI + " " + typeName);
        }
    }

    public static class WsResult implements org.apache.axis2.databinding.ADBBean {
        /**
         * field for ResultCode
         */
        protected java.lang.String localResultCode;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localResultCodeTracker = false;

        /**
         * field for ResultMessage
         */
        protected java.lang.String localResultMessage;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localResultMessageTracker = false;

        /**
         * field for Success
         */
        protected java.lang.String localSuccess;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSuccessTracker = false;

        /* This type was generated from the piece of schema that had
           name = WsResult
           Namespace URI = http://webservices.grouper.middleware.internet2.edu/xsd
           Namespace Prefix = ns1
         */
        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getResultCode() {
            return localResultCode;
        }

        /**
         * Auto generated setter method
         * @param param ResultCode
         */
        public void setResultCode(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localResultCodeTracker = true;
            } else {
                localResultCodeTracker = true;
            }

            this.localResultCode = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getResultMessage() {
            return localResultMessage;
        }

        /**
         * Auto generated setter method
         * @param param ResultMessage
         */
        public void setResultMessage(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localResultMessageTracker = true;
            } else {
                localResultMessageTracker = true;
            }

            this.localResultMessage = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getSuccess() {
            return localSuccess;
        }

        /**
         * Auto generated setter method
         * @param param Success
         */
        public void setSuccess(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localSuccessTracker = true;
            } else {
                localSuccessTracker = true;
            }

            this.localSuccess = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    parentQName) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        WsResult.this.serialize(parentQName, factory, xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (localResultCodeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultCode",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultCode");
                    }
                } else {
                    xmlWriter.writeStartElement("resultCode");
                }

                if (localResultCode == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultCode);
                }

                xmlWriter.writeEndElement();
            }

            if (localResultMessageTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "resultMessage",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "resultMessage");
                    }
                } else {
                    xmlWriter.writeStartElement("resultMessage");
                }

                if (localResultMessage == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localResultMessage);
                }

                xmlWriter.writeEndElement();
            }

            if (localSuccessTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "success", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "success");
                    }
                } else {
                    xmlWriter.writeStartElement("success");
                }

                if (localSuccess == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localSuccess);
                }

                xmlWriter.writeEndElement();
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (localResultCodeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultCode"));

                elementList.add((localResultCode == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultCode));
            }

            if (localResultMessageTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "resultMessage"));

                elementList.add((localResultMessage == null) ? null
                                                             : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localResultMessage));
            }

            if (localSuccessTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "success"));

                elementList.add((localSuccess == null) ? null
                                                       : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localSuccess));
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static WsResult parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                WsResult object = new WsResult();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"WsResult".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (WsResult) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultCode").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultCode(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "resultMessage").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setResultMessage(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "success").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setSuccess(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class AddMemberSimpleResponse implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://webservices.grouper.middleware.internet2.edu/xsd",
                "addMemberSimpleResponse", "ns1");

        /**
         * field for _return
         */
        protected WsAddMemberResult local_return;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean local_returnTracker = false;

        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return WsAddMemberResult
         */
        public WsAddMemberResult get_return() {
            return local_return;
        }

        /**
         * Auto generated setter method
         * @param param _return
         */
        public void set_return(WsAddMemberResult param) {
            if (param != null) {
                //update the setting tracker
                local_returnTracker = true;
            } else {
                local_returnTracker = true;
            }

            this.local_return = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    MY_QNAME) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        AddMemberSimpleResponse.this.serialize(MY_QNAME,
                            factory, xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (local_returnTracker) {
                if (local_return == null) {
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "return",
                                namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2, "return");
                        }
                    } else {
                        xmlWriter.writeStartElement("return");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                } else {
                    local_return.serialize(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "return"), factory, xmlWriter);
                }
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (local_returnTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "return"));

                elementList.add((local_return == null) ? null : local_return);
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static AddMemberSimpleResponse parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                AddMemberSimpleResponse object = new AddMemberSimpleResponse();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"addMemberSimpleResponse".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (AddMemberSimpleResponse) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "return").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            object.set_return(null);
                            reader.next();

                            reader.next();
                        } else {
                            object.set_return(WsAddMemberResult.Factory.parse(
                                    reader));

                            reader.next();
                        }
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class WsGroupResult implements org.apache.axis2.databinding.ADBBean {
        /**
         * field for CreateSource
         */
        protected java.lang.String localCreateSource;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localCreateSourceTracker = false;

        /**
         * field for CreateSubjectId
         */
        protected java.lang.String localCreateSubjectId;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localCreateSubjectIdTracker = false;

        /**
         * field for CreateTime
         */
        protected java.lang.String localCreateTime;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localCreateTimeTracker = false;

        /**
         * field for Description
         */
        protected java.lang.String localDescription;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localDescriptionTracker = false;

        /**
         * field for DisplayExtension
         */
        protected java.lang.String localDisplayExtension;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localDisplayExtensionTracker = false;

        /**
         * field for DisplayName
         */
        protected java.lang.String localDisplayName;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localDisplayNameTracker = false;

        /**
         * field for Extension
         */
        protected java.lang.String localExtension;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localExtensionTracker = false;

        /**
         * field for IsComposite
         */
        protected java.lang.String localIsComposite;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localIsCompositeTracker = false;

        /**
         * field for ModifySource
         */
        protected java.lang.String localModifySource;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localModifySourceTracker = false;

        /**
         * field for ModifySubjectId
         */
        protected java.lang.String localModifySubjectId;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localModifySubjectIdTracker = false;

        /**
         * field for ModifyTime
         */
        protected java.lang.String localModifyTime;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localModifyTimeTracker = false;

        /**
         * field for Name
         */
        protected java.lang.String localName;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localNameTracker = false;

        /**
         * field for ParentStemName
         */
        protected java.lang.String localParentStemName;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParentStemNameTracker = false;

        /**
         * field for ParentStemUuid
         */
        protected java.lang.String localParentStemUuid;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParentStemUuidTracker = false;

        /**
         * field for Uuid
         */
        protected java.lang.String localUuid;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localUuidTracker = false;

        /* This type was generated from the piece of schema that had
           name = WsGroupResult
           Namespace URI = http://webservices.grouper.middleware.internet2.edu/xsd
           Namespace Prefix = ns1
         */
        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getCreateSource() {
            return localCreateSource;
        }

        /**
         * Auto generated setter method
         * @param param CreateSource
         */
        public void setCreateSource(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localCreateSourceTracker = true;
            } else {
                localCreateSourceTracker = true;
            }

            this.localCreateSource = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getCreateSubjectId() {
            return localCreateSubjectId;
        }

        /**
         * Auto generated setter method
         * @param param CreateSubjectId
         */
        public void setCreateSubjectId(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localCreateSubjectIdTracker = true;
            } else {
                localCreateSubjectIdTracker = true;
            }

            this.localCreateSubjectId = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getCreateTime() {
            return localCreateTime;
        }

        /**
         * Auto generated setter method
         * @param param CreateTime
         */
        public void setCreateTime(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localCreateTimeTracker = true;
            } else {
                localCreateTimeTracker = true;
            }

            this.localCreateTime = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getDescription() {
            return localDescription;
        }

        /**
         * Auto generated setter method
         * @param param Description
         */
        public void setDescription(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localDescriptionTracker = true;
            } else {
                localDescriptionTracker = true;
            }

            this.localDescription = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getDisplayExtension() {
            return localDisplayExtension;
        }

        /**
         * Auto generated setter method
         * @param param DisplayExtension
         */
        public void setDisplayExtension(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localDisplayExtensionTracker = true;
            } else {
                localDisplayExtensionTracker = true;
            }

            this.localDisplayExtension = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getDisplayName() {
            return localDisplayName;
        }

        /**
         * Auto generated setter method
         * @param param DisplayName
         */
        public void setDisplayName(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localDisplayNameTracker = true;
            } else {
                localDisplayNameTracker = true;
            }

            this.localDisplayName = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getExtension() {
            return localExtension;
        }

        /**
         * Auto generated setter method
         * @param param Extension
         */
        public void setExtension(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localExtensionTracker = true;
            } else {
                localExtensionTracker = true;
            }

            this.localExtension = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getIsComposite() {
            return localIsComposite;
        }

        /**
         * Auto generated setter method
         * @param param IsComposite
         */
        public void setIsComposite(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localIsCompositeTracker = true;
            } else {
                localIsCompositeTracker = true;
            }

            this.localIsComposite = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getModifySource() {
            return localModifySource;
        }

        /**
         * Auto generated setter method
         * @param param ModifySource
         */
        public void setModifySource(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localModifySourceTracker = true;
            } else {
                localModifySourceTracker = true;
            }

            this.localModifySource = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getModifySubjectId() {
            return localModifySubjectId;
        }

        /**
         * Auto generated setter method
         * @param param ModifySubjectId
         */
        public void setModifySubjectId(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localModifySubjectIdTracker = true;
            } else {
                localModifySubjectIdTracker = true;
            }

            this.localModifySubjectId = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getModifyTime() {
            return localModifyTime;
        }

        /**
         * Auto generated setter method
         * @param param ModifyTime
         */
        public void setModifyTime(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localModifyTimeTracker = true;
            } else {
                localModifyTimeTracker = true;
            }

            this.localModifyTime = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getName() {
            return localName;
        }

        /**
         * Auto generated setter method
         * @param param Name
         */
        public void setName(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localNameTracker = true;
            } else {
                localNameTracker = true;
            }

            this.localName = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getParentStemName() {
            return localParentStemName;
        }

        /**
         * Auto generated setter method
         * @param param ParentStemName
         */
        public void setParentStemName(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localParentStemNameTracker = true;
            } else {
                localParentStemNameTracker = true;
            }

            this.localParentStemName = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getParentStemUuid() {
            return localParentStemUuid;
        }

        /**
         * Auto generated setter method
         * @param param ParentStemUuid
         */
        public void setParentStemUuid(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localParentStemUuidTracker = true;
            } else {
                localParentStemUuidTracker = true;
            }

            this.localParentStemUuid = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getUuid() {
            return localUuid;
        }

        /**
         * Auto generated setter method
         * @param param Uuid
         */
        public void setUuid(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localUuidTracker = true;
            } else {
                localUuidTracker = true;
            }

            this.localUuid = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    parentQName) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        WsGroupResult.this.serialize(parentQName, factory,
                            xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (localCreateSourceTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "createSource",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "createSource");
                    }
                } else {
                    xmlWriter.writeStartElement("createSource");
                }

                if (localCreateSource == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localCreateSource);
                }

                xmlWriter.writeEndElement();
            }

            if (localCreateSubjectIdTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "createSubjectId",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "createSubjectId");
                    }
                } else {
                    xmlWriter.writeStartElement("createSubjectId");
                }

                if (localCreateSubjectId == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localCreateSubjectId);
                }

                xmlWriter.writeEndElement();
            }

            if (localCreateTimeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "createTime",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "createTime");
                    }
                } else {
                    xmlWriter.writeStartElement("createTime");
                }

                if (localCreateTime == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localCreateTime);
                }

                xmlWriter.writeEndElement();
            }

            if (localDescriptionTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "description",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "description");
                    }
                } else {
                    xmlWriter.writeStartElement("description");
                }

                if (localDescription == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localDescription);
                }

                xmlWriter.writeEndElement();
            }

            if (localDisplayExtensionTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "displayExtension",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace,
                            "displayExtension");
                    }
                } else {
                    xmlWriter.writeStartElement("displayExtension");
                }

                if (localDisplayExtension == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localDisplayExtension);
                }

                xmlWriter.writeEndElement();
            }

            if (localDisplayNameTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "displayName",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "displayName");
                    }
                } else {
                    xmlWriter.writeStartElement("displayName");
                }

                if (localDisplayName == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localDisplayName);
                }

                xmlWriter.writeEndElement();
            }

            if (localExtensionTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "extension",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "extension");
                    }
                } else {
                    xmlWriter.writeStartElement("extension");
                }

                if (localExtension == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localExtension);
                }

                xmlWriter.writeEndElement();
            }

            if (localIsCompositeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "isComposite",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "isComposite");
                    }
                } else {
                    xmlWriter.writeStartElement("isComposite");
                }

                if (localIsComposite == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localIsComposite);
                }

                xmlWriter.writeEndElement();
            }

            if (localModifySourceTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "modifySource",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "modifySource");
                    }
                } else {
                    xmlWriter.writeStartElement("modifySource");
                }

                if (localModifySource == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localModifySource);
                }

                xmlWriter.writeEndElement();
            }

            if (localModifySubjectIdTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "modifySubjectId",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "modifySubjectId");
                    }
                } else {
                    xmlWriter.writeStartElement("modifySubjectId");
                }

                if (localModifySubjectId == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localModifySubjectId);
                }

                xmlWriter.writeEndElement();
            }

            if (localModifyTimeTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "modifyTime",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "modifyTime");
                    }
                } else {
                    xmlWriter.writeStartElement("modifyTime");
                }

                if (localModifyTime == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localModifyTime);
                }

                xmlWriter.writeEndElement();
            }

            if (localNameTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "name", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "name");
                    }
                } else {
                    xmlWriter.writeStartElement("name");
                }

                if (localName == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localName);
                }

                xmlWriter.writeEndElement();
            }

            if (localParentStemNameTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "parentStemName",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "parentStemName");
                    }
                } else {
                    xmlWriter.writeStartElement("parentStemName");
                }

                if (localParentStemName == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localParentStemName);
                }

                xmlWriter.writeEndElement();
            }

            if (localParentStemUuidTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "parentStemUuid",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "parentStemUuid");
                    }
                } else {
                    xmlWriter.writeStartElement("parentStemUuid");
                }

                if (localParentStemUuid == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localParentStemUuid);
                }

                xmlWriter.writeEndElement();
            }

            if (localUuidTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "uuid", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "uuid");
                    }
                } else {
                    xmlWriter.writeStartElement("uuid");
                }

                if (localUuid == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localUuid);
                }

                xmlWriter.writeEndElement();
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (localCreateSourceTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "createSource"));

                elementList.add((localCreateSource == null) ? null
                                                            : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localCreateSource));
            }

            if (localCreateSubjectIdTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "createSubjectId"));

                elementList.add((localCreateSubjectId == null) ? null
                                                               : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localCreateSubjectId));
            }

            if (localCreateTimeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "createTime"));

                elementList.add((localCreateTime == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localCreateTime));
            }

            if (localDescriptionTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "description"));

                elementList.add((localDescription == null) ? null
                                                           : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localDescription));
            }

            if (localDisplayExtensionTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "displayExtension"));

                elementList.add((localDisplayExtension == null) ? null
                                                                : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localDisplayExtension));
            }

            if (localDisplayNameTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "displayName"));

                elementList.add((localDisplayName == null) ? null
                                                           : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localDisplayName));
            }

            if (localExtensionTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "extension"));

                elementList.add((localExtension == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localExtension));
            }

            if (localIsCompositeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "isComposite"));

                elementList.add((localIsComposite == null) ? null
                                                           : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localIsComposite));
            }

            if (localModifySourceTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "modifySource"));

                elementList.add((localModifySource == null) ? null
                                                            : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localModifySource));
            }

            if (localModifySubjectIdTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "modifySubjectId"));

                elementList.add((localModifySubjectId == null) ? null
                                                               : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localModifySubjectId));
            }

            if (localModifyTimeTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "modifyTime"));

                elementList.add((localModifyTime == null) ? null
                                                          : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localModifyTime));
            }

            if (localNameTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "name"));

                elementList.add((localName == null) ? null
                                                    : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localName));
            }

            if (localParentStemNameTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "parentStemName"));

                elementList.add((localParentStemName == null) ? null
                                                              : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localParentStemName));
            }

            if (localParentStemUuidTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "parentStemUuid"));

                elementList.add((localParentStemUuid == null) ? null
                                                              : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localParentStemUuid));
            }

            if (localUuidTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "uuid"));

                elementList.add((localUuid == null) ? null
                                                    : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localUuid));
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static WsGroupResult parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                WsGroupResult object = new WsGroupResult();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"WsGroupResult".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (WsGroupResult) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "createSource").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setCreateSource(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "createSubjectId").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setCreateSubjectId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "createTime").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setCreateTime(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "description").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setDescription(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "displayExtension").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setDisplayExtension(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "displayName").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setDisplayName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "extension").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setExtension(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "isComposite").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setIsComposite(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "modifySource").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setModifySource(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "modifySubjectId").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setModifySubjectId(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "modifyTime").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setModifyTime(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "name").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "parentStemName").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setParentStemName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "parentStemUuid").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setParentStemUuid(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "uuid").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setUuid(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class WsGroupLookup implements org.apache.axis2.databinding.ADBBean {
        /**
         * field for GroupName
         */
        protected java.lang.String localGroupName;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localGroupNameTracker = false;

        /**
         * field for Uuid
         */
        protected java.lang.String localUuid;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localUuidTracker = false;

        /* This type was generated from the piece of schema that had
           name = WsGroupLookup
           Namespace URI = http://webservices.grouper.middleware.internet2.edu/xsd
           Namespace Prefix = ns1
         */
        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getGroupName() {
            return localGroupName;
        }

        /**
         * Auto generated setter method
         * @param param GroupName
         */
        public void setGroupName(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localGroupNameTracker = true;
            } else {
                localGroupNameTracker = true;
            }

            this.localGroupName = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getUuid() {
            return localUuid;
        }

        /**
         * Auto generated setter method
         * @param param Uuid
         */
        public void setUuid(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localUuidTracker = true;
            } else {
                localUuidTracker = true;
            }

            this.localUuid = param;
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    parentQName) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        WsGroupLookup.this.serialize(parentQName, factory,
                            xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(parentQName,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (localGroupNameTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "groupName",
                            namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "groupName");
                    }
                } else {
                    xmlWriter.writeStartElement("groupName");
                }

                if (localGroupName == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localGroupName);
                }

                xmlWriter.writeEndElement();
            }

            if (localUuidTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix, "uuid", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace, "uuid");
                    }
                } else {
                    xmlWriter.writeStartElement("uuid");
                }

                if (localUuid == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localUuid);
                }

                xmlWriter.writeEndElement();
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (localGroupNameTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "groupName"));

                elementList.add((localGroupName == null) ? null
                                                         : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localGroupName));
            }

            if (localUuidTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "uuid"));

                elementList.add((localUuid == null) ? null
                                                    : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localUuid));
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static WsGroupLookup parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                WsGroupLookup object = new WsGroupLookup();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"WsGroupLookup".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (WsGroupLookup) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "groupName").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setGroupName(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "uuid").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setUuid(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }

    public static class AddMember implements org.apache.axis2.databinding.ADBBean {
        public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName("http://webservices.grouper.middleware.internet2.edu/xsd",
                "addMember", "ns1");

        /**
         * field for WsGroupLookup
         */
        protected WsGroupLookup localWsGroupLookup;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localWsGroupLookupTracker = false;

        /**
         * field for SubjectLookups
         * This was an Array!
         */
        protected WsSubjectLookup[] localSubjectLookups;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localSubjectLookupsTracker = false;

        /**
         * field for ReplaceAllExisting
         */
        protected java.lang.String localReplaceAllExisting;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localReplaceAllExistingTracker = false;

        /**
         * field for ActAsSubjectLookup
         */
        protected WsSubjectLookup localActAsSubjectLookup;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localActAsSubjectLookupTracker = false;

        /**
         * field for ParamNames
         * This was an Array!
         */
        protected java.lang.String[] localParamNames;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParamNamesTracker = false;

        /**
         * field for ParamValues
         * This was an Array!
         */
        protected java.lang.String[] localParamValues;

        /*  This tracker boolean wil be used to detect whether the user called the set method
         *   for this attribute. It will be used to determine whether to include this field
         *   in the serialized XML
         */
        protected boolean localParamValuesTracker = false;

        private static java.lang.String generatePrefix(
            java.lang.String namespace) {
            if (namespace.equals(
                        "http://webservices.grouper.middleware.internet2.edu/xsd")) {
                return "ns1";
            }

            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Auto generated getter method
         * @return WsGroupLookup
         */
        public WsGroupLookup getWsGroupLookup() {
            return localWsGroupLookup;
        }

        /**
         * Auto generated setter method
         * @param param WsGroupLookup
         */
        public void setWsGroupLookup(WsGroupLookup param) {
            if (param != null) {
                //update the setting tracker
                localWsGroupLookupTracker = true;
            } else {
                localWsGroupLookupTracker = true;
            }

            this.localWsGroupLookup = param;
        }

        /**
         * Auto generated getter method
         * @return WsSubjectLookup[]
         */
        public WsSubjectLookup[] getSubjectLookups() {
            return localSubjectLookups;
        }

        /**
         * validate the array for SubjectLookups
         */
        protected void validateSubjectLookups(WsSubjectLookup[] param) {
        }

        /**
         * Auto generated setter method
         * @param param SubjectLookups
         */
        public void setSubjectLookups(WsSubjectLookup[] param) {
            validateSubjectLookups(param);

            if (param != null) {
                //update the setting tracker
                localSubjectLookupsTracker = true;
            } else {
                localSubjectLookupsTracker = true;
            }

            this.localSubjectLookups = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * @param param WsSubjectLookup
         */
        public void addSubjectLookups(WsSubjectLookup param) {
            if (localSubjectLookups == null) {
                localSubjectLookups = new WsSubjectLookup[] {  };
            }

            //update the setting tracker
            localSubjectLookupsTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localSubjectLookups);
            list.add(param);
            this.localSubjectLookups = (WsSubjectLookup[]) list.toArray(new WsSubjectLookup[list.size()]);
        }

        /**
         * Auto generated getter method
         * @return java.lang.String
         */
        public java.lang.String getReplaceAllExisting() {
            return localReplaceAllExisting;
        }

        /**
         * Auto generated setter method
         * @param param ReplaceAllExisting
         */
        public void setReplaceAllExisting(java.lang.String param) {
            if (param != null) {
                //update the setting tracker
                localReplaceAllExistingTracker = true;
            } else {
                localReplaceAllExistingTracker = true;
            }

            this.localReplaceAllExisting = param;
        }

        /**
         * Auto generated getter method
         * @return WsSubjectLookup
         */
        public WsSubjectLookup getActAsSubjectLookup() {
            return localActAsSubjectLookup;
        }

        /**
         * Auto generated setter method
         * @param param ActAsSubjectLookup
         */
        public void setActAsSubjectLookup(WsSubjectLookup param) {
            if (param != null) {
                //update the setting tracker
                localActAsSubjectLookupTracker = true;
            } else {
                localActAsSubjectLookupTracker = true;
            }

            this.localActAsSubjectLookup = param;
        }

        /**
         * Auto generated getter method
         * @return java.lang.String[]
         */
        public java.lang.String[] getParamNames() {
            return localParamNames;
        }

        /**
         * validate the array for ParamNames
         */
        protected void validateParamNames(java.lang.String[] param) {
        }

        /**
         * Auto generated setter method
         * @param param ParamNames
         */
        public void setParamNames(java.lang.String[] param) {
            validateParamNames(param);

            if (param != null) {
                //update the setting tracker
                localParamNamesTracker = true;
            } else {
                localParamNamesTracker = true;
            }

            this.localParamNames = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * @param param java.lang.String
         */
        public void addParamNames(java.lang.String param) {
            if (localParamNames == null) {
                localParamNames = new java.lang.String[] {  };
            }

            //update the setting tracker
            localParamNamesTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localParamNames);
            list.add(param);
            this.localParamNames = (java.lang.String[]) list.toArray(new java.lang.String[list.size()]);
        }

        /**
         * Auto generated getter method
         * @return java.lang.String[]
         */
        public java.lang.String[] getParamValues() {
            return localParamValues;
        }

        /**
         * validate the array for ParamValues
         */
        protected void validateParamValues(java.lang.String[] param) {
        }

        /**
         * Auto generated setter method
         * @param param ParamValues
         */
        public void setParamValues(java.lang.String[] param) {
            validateParamValues(param);

            if (param != null) {
                //update the setting tracker
                localParamValuesTracker = true;
            } else {
                localParamValuesTracker = true;
            }

            this.localParamValues = param;
        }

        /**
         * Auto generated add method for the array for convenience
         * @param param java.lang.String
         */
        public void addParamValues(java.lang.String param) {
            if (localParamValues == null) {
                localParamValues = new java.lang.String[] {  };
            }

            //update the setting tracker
            localParamValuesTracker = true;

            java.util.List list = org.apache.axis2.databinding.utils.ConverterUtil.toList(localParamValues);
            list.add(param);
            this.localParamValues = (java.lang.String[]) list.toArray(new java.lang.String[list.size()]);
        }

        /**
         * isReaderMTOMAware
         * @return true if the reader supports MTOM
         */
        public static boolean isReaderMTOMAware(
            javax.xml.stream.XMLStreamReader reader) {
            boolean isReaderMTOMAware = false;

            try {
                isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(
                            org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
            } catch (java.lang.IllegalArgumentException e) {
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
        public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory)
            throws org.apache.axis2.databinding.ADBException {
            org.apache.axiom.om.OMDataSource dataSource = new org.apache.axis2.databinding.ADBDataSource(this,
                    MY_QNAME) {
                    public void serialize(
                        org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
                        throws javax.xml.stream.XMLStreamException {
                        AddMember.this.serialize(MY_QNAME, factory, xmlWriter);
                    }
                };

            return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(MY_QNAME,
                factory, dataSource);
        }

        public void serialize(final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory,
            org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException,
                org.apache.axis2.databinding.ADBException {
            java.lang.String prefix = null;
            java.lang.String namespace = null;

            prefix = parentQName.getPrefix();
            namespace = parentQName.getNamespaceURI();

            if (namespace != null) {
                java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);

                if (writerPrefix != null) {
                    xmlWriter.writeStartElement(namespace,
                        parentQName.getLocalPart());
                } else {
                    if (prefix == null) {
                        prefix = generatePrefix(namespace);
                    }

                    xmlWriter.writeStartElement(prefix,
                        parentQName.getLocalPart(), namespace);
                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }
            } else {
                xmlWriter.writeStartElement(parentQName.getLocalPart());
            }

            if (localWsGroupLookupTracker) {
                if (localWsGroupLookup == null) {
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2,
                                "wsGroupLookup", namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2,
                                "wsGroupLookup");
                        }
                    } else {
                        xmlWriter.writeStartElement("wsGroupLookup");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                } else {
                    localWsGroupLookup.serialize(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "wsGroupLookup"), factory, xmlWriter);
                }
            }

            if (localSubjectLookupsTracker) {
                if (localSubjectLookups != null) {
                    for (int i = 0; i < localSubjectLookups.length; i++) {
                        if (localSubjectLookups[i] != null) {
                            localSubjectLookups[i].serialize(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "subjectLookups"), factory, xmlWriter);
                        } else {
                            // write null attribute
                            java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                            if (!namespace2.equals("")) {
                                java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                                if (prefix2 == null) {
                                    prefix2 = generatePrefix(namespace2);

                                    xmlWriter.writeStartElement(prefix2,
                                        "subjectLookups", namespace2);
                                    xmlWriter.writeNamespace(prefix2, namespace2);
                                    xmlWriter.setPrefix(prefix2, namespace2);
                                } else {
                                    xmlWriter.writeStartElement(namespace2,
                                        "subjectLookups");
                                }
                            } else {
                                xmlWriter.writeStartElement("subjectLookups");
                            }

                            // write the nil attribute
                            writeAttribute("xsi",
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();
                        }
                    }
                } else {
                    // write null attribute
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2,
                                "subjectLookups", namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2,
                                "subjectLookups");
                        }
                    } else {
                        xmlWriter.writeStartElement("subjectLookups");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                }
            }

            if (localReplaceAllExistingTracker) {
                namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                if (!namespace.equals("")) {
                    prefix = xmlWriter.getPrefix(namespace);

                    if (prefix == null) {
                        prefix = generatePrefix(namespace);

                        xmlWriter.writeStartElement(prefix,
                            "replaceAllExisting", namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    } else {
                        xmlWriter.writeStartElement(namespace,
                            "replaceAllExisting");
                    }
                } else {
                    xmlWriter.writeStartElement("replaceAllExisting");
                }

                if (localReplaceAllExisting == null) {
                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                } else {
                    xmlWriter.writeCharacters(localReplaceAllExisting);
                }

                xmlWriter.writeEndElement();
            }

            if (localActAsSubjectLookupTracker) {
                if (localActAsSubjectLookup == null) {
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2,
                                "actAsSubjectLookup", namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2,
                                "actAsSubjectLookup");
                        }
                    } else {
                        xmlWriter.writeStartElement("actAsSubjectLookup");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                } else {
                    localActAsSubjectLookup.serialize(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "actAsSubjectLookup"), factory, xmlWriter);
                }
            }

            if (localParamNamesTracker) {
                if (localParamNames != null) {
                    namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    boolean emptyNamespace = (namespace == null) ||
                        (namespace.length() == 0);
                    prefix = emptyNamespace ? null
                                            : xmlWriter.getPrefix(namespace);

                    for (int i = 0; i < localParamNames.length; i++) {
                        if (localParamNames[i] != null) {
                            if (!emptyNamespace) {
                                if (prefix == null) {
                                    java.lang.String prefix2 = generatePrefix(namespace);

                                    xmlWriter.writeStartElement(prefix2,
                                        "paramNames", namespace);
                                    xmlWriter.writeNamespace(prefix2, namespace);
                                    xmlWriter.setPrefix(prefix2, namespace);
                                } else {
                                    xmlWriter.writeStartElement(namespace,
                                        "paramNames");
                                }
                            } else {
                                xmlWriter.writeStartElement("paramNames");
                            }

                            xmlWriter.writeCharacters(localParamNames[i]);

                            xmlWriter.writeEndElement();
                        } else {
                            // write null attribute
                            namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                            if (!namespace.equals("")) {
                                prefix = xmlWriter.getPrefix(namespace);

                                if (prefix == null) {
                                    prefix = generatePrefix(namespace);

                                    xmlWriter.writeStartElement(prefix,
                                        "paramNames", namespace);
                                    xmlWriter.writeNamespace(prefix, namespace);
                                    xmlWriter.setPrefix(prefix, namespace);
                                } else {
                                    xmlWriter.writeStartElement(namespace,
                                        "paramNames");
                                }
                            } else {
                                xmlWriter.writeStartElement("paramNames");
                            }

                            writeAttribute("xsi",
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();
                        }
                    }
                } else {
                    // write the null attribute
                    // write null attribute
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "paramNames",
                                namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2, "paramNames");
                        }
                    } else {
                        xmlWriter.writeStartElement("paramNames");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                }
            }

            if (localParamValuesTracker) {
                if (localParamValues != null) {
                    namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    boolean emptyNamespace = (namespace == null) ||
                        (namespace.length() == 0);
                    prefix = emptyNamespace ? null
                                            : xmlWriter.getPrefix(namespace);

                    for (int i = 0; i < localParamValues.length; i++) {
                        if (localParamValues[i] != null) {
                            if (!emptyNamespace) {
                                if (prefix == null) {
                                    java.lang.String prefix2 = generatePrefix(namespace);

                                    xmlWriter.writeStartElement(prefix2,
                                        "paramValues", namespace);
                                    xmlWriter.writeNamespace(prefix2, namespace);
                                    xmlWriter.setPrefix(prefix2, namespace);
                                } else {
                                    xmlWriter.writeStartElement(namespace,
                                        "paramValues");
                                }
                            } else {
                                xmlWriter.writeStartElement("paramValues");
                            }

                            xmlWriter.writeCharacters(localParamValues[i]);

                            xmlWriter.writeEndElement();
                        } else {
                            // write null attribute
                            namespace = "http://webservices.grouper.middleware.internet2.edu/xsd";

                            if (!namespace.equals("")) {
                                prefix = xmlWriter.getPrefix(namespace);

                                if (prefix == null) {
                                    prefix = generatePrefix(namespace);

                                    xmlWriter.writeStartElement(prefix,
                                        "paramValues", namespace);
                                    xmlWriter.writeNamespace(prefix, namespace);
                                    xmlWriter.setPrefix(prefix, namespace);
                                } else {
                                    xmlWriter.writeStartElement(namespace,
                                        "paramValues");
                                }
                            } else {
                                xmlWriter.writeStartElement("paramValues");
                            }

                            writeAttribute("xsi",
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "nil", "1", xmlWriter);
                            xmlWriter.writeEndElement();
                        }
                    }
                } else {
                    // write the null attribute
                    // write null attribute
                    java.lang.String namespace2 = "http://webservices.grouper.middleware.internet2.edu/xsd";

                    if (!namespace2.equals("")) {
                        java.lang.String prefix2 = xmlWriter.getPrefix(namespace2);

                        if (prefix2 == null) {
                            prefix2 = generatePrefix(namespace2);

                            xmlWriter.writeStartElement(prefix2, "paramValues",
                                namespace2);
                            xmlWriter.writeNamespace(prefix2, namespace2);
                            xmlWriter.setPrefix(prefix2, namespace2);
                        } else {
                            xmlWriter.writeStartElement(namespace2,
                                "paramValues");
                        }
                    } else {
                        xmlWriter.writeStartElement("paramValues");
                    }

                    // write the nil attribute
                    writeAttribute("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance", "nil",
                        "1", xmlWriter);
                    xmlWriter.writeEndElement();
                }
            }

            xmlWriter.writeEndElement();
        }

        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,
            java.lang.String namespace, java.lang.String attName,
            java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }

            xmlWriter.writeAttribute(namespace, attName, attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,
            java.lang.String attName, java.lang.String attValue,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName, attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace, attName, attValue);
            }
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeQNameAttribute(java.lang.String namespace,
            java.lang.String attName, javax.xml.namespace.QName qname,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();

            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);

                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix, namespaceURI);
                }

                if (prefix.trim().length() > 0) {
                    xmlWriter.writeCharacters(prefix + ":" +
                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                            qname));
                }
            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
            javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException {
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
                            xmlWriter.setPrefix(prefix, namespaceURI);
                        }

                        if (prefix.trim().length() > 0) {
                            stringToWrite.append(prefix).append(":")
                                         .append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                qnames[i]));
                    }
                }

                xmlWriter.writeCharacters(stringToWrite.toString());
            }
        }

        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(
            javax.xml.stream.XMLStreamWriter xmlWriter,
            java.lang.String namespace)
            throws javax.xml.stream.XMLStreamException {
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
        public javax.xml.stream.XMLStreamReader getPullParser(
            javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {
            java.util.ArrayList elementList = new java.util.ArrayList();
            java.util.ArrayList attribList = new java.util.ArrayList();

            if (localWsGroupLookupTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "wsGroupLookup"));

                elementList.add((localWsGroupLookup == null) ? null
                                                             : localWsGroupLookup);
            }

            if (localSubjectLookupsTracker) {
                if (localSubjectLookups != null) {
                    for (int i = 0; i < localSubjectLookups.length; i++) {
                        if (localSubjectLookups[i] != null) {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "subjectLookups"));
                            elementList.add(localSubjectLookups[i]);
                        } else {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "subjectLookups"));
                            elementList.add(null);
                        }
                    }
                } else {
                    elementList.add(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "subjectLookups"));
                    elementList.add(localSubjectLookups);
                }
            }

            if (localReplaceAllExistingTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "replaceAllExisting"));

                elementList.add((localReplaceAllExisting == null) ? null
                                                                  : org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                        localReplaceAllExisting));
            }

            if (localActAsSubjectLookupTracker) {
                elementList.add(new javax.xml.namespace.QName(
                        "http://webservices.grouper.middleware.internet2.edu/xsd",
                        "actAsSubjectLookup"));

                elementList.add((localActAsSubjectLookup == null) ? null
                                                                  : localActAsSubjectLookup);
            }

            if (localParamNamesTracker) {
                if (localParamNames != null) {
                    for (int i = 0; i < localParamNames.length; i++) {
                        if (localParamNames[i] != null) {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "paramNames"));
                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    localParamNames[i]));
                        } else {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "paramNames"));
                            elementList.add(null);
                        }
                    }
                } else {
                    elementList.add(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "paramNames"));
                    elementList.add(null);
                }
            }

            if (localParamValuesTracker) {
                if (localParamValues != null) {
                    for (int i = 0; i < localParamValues.length; i++) {
                        if (localParamValues[i] != null) {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "paramValues"));
                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    localParamValues[i]));
                        } else {
                            elementList.add(new javax.xml.namespace.QName(
                                    "http://webservices.grouper.middleware.internet2.edu/xsd",
                                    "paramValues"));
                            elementList.add(null);
                        }
                    }
                } else {
                    elementList.add(new javax.xml.namespace.QName(
                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                            "paramValues"));
                    elementList.add(null);
                }
            }

            return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName,
                elementList.toArray(), attribList.toArray());
        }

        /**
         *  Factory class that keeps the parse method
         */
        public static class Factory {
            /**
             * static method to create the object
             * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
             *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
             * Postcondition: If this object is an element, the reader is positioned at its end element
             *                If this object is a complex type, the reader is positioned at the end element of its outer element
             */
            public static AddMember parse(
                javax.xml.stream.XMLStreamReader reader)
                throws java.lang.Exception {
                AddMember object = new AddMember();

                int event;
                java.lang.String nillableValue = null;
                java.lang.String prefix = "";
                java.lang.String namespaceuri = "";

                try {
                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.getAttributeValue(
                                "http://www.w3.org/2001/XMLSchema-instance",
                                "type") != null) {
                        java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "type");

                        if (fullTypeName != null) {
                            java.lang.String nsPrefix = null;

                            if (fullTypeName.indexOf(":") > -1) {
                                nsPrefix = fullTypeName.substring(0,
                                        fullTypeName.indexOf(":"));
                            }

                            nsPrefix = (nsPrefix == null) ? "" : nsPrefix;

                            java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(
                                        ":") + 1);

                            if (!"addMember".equals(type)) {
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext()
                                                               .getNamespaceURI(nsPrefix);

                                return (AddMember) ExtensionMapper.getTypeObject(nsUri,
                                    type, reader);
                            }
                        }
                    }

                    // Note all attributes that were handled. Used to differ normal attributes
                    // from anyAttributes.
                    java.util.Vector handledAttributes = new java.util.Vector();

                    reader.next();

                    java.util.ArrayList list2 = new java.util.ArrayList();

                    java.util.ArrayList list5 = new java.util.ArrayList();

                    java.util.ArrayList list6 = new java.util.ArrayList();

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "wsGroupLookup").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            object.setWsGroupLookup(null);
                            reader.next();

                            reader.next();
                        } else {
                            object.setWsGroupLookup(WsGroupLookup.Factory.parse(
                                    reader));

                            reader.next();
                        }
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "subjectLookups").equals(reader.getName())) {
                        // Process the array and step past its final element's end.
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            list2.add(null);
                            reader.next();
                        } else {
                            list2.add(WsSubjectLookup.Factory.parse(reader));
                        }

                        //loop until we find a start element that is not part of this array
                        boolean loopDone2 = false;

                        while (!loopDone2) {
                            // We should be at the end element, but make sure
                            while (!reader.isEndElement())
                                reader.next();

                            // Step out of this element
                            reader.next();

                            // Step to next element event.
                            while (!reader.isStartElement() &&
                                    !reader.isEndElement())
                                reader.next();

                            if (reader.isEndElement()) {
                                //two continuous end elements means we are exiting the xml structure
                                loopDone2 = true;
                            } else {
                                if (new javax.xml.namespace.QName(
                                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                                            "subjectLookups").equals(
                                            reader.getName())) {
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                            "nil");

                                    if ("true".equals(nillableValue) ||
                                            "1".equals(nillableValue)) {
                                        list2.add(null);
                                        reader.next();
                                    } else {
                                        list2.add(WsSubjectLookup.Factory.parse(
                                                reader));
                                    }
                                } else {
                                    loopDone2 = true;
                                }
                            }
                        }

                        // call the converter utility  to convert and set the array
                        object.setSubjectLookups((WsSubjectLookup[]) org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                WsSubjectLookup.class, list2));
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "replaceAllExisting").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if (!"true".equals(nillableValue) &&
                                !"1".equals(nillableValue)) {
                            java.lang.String content = reader.getElementText();

                            object.setReplaceAllExisting(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(
                                    content));
                        } else {
                            reader.getElementText(); // throw away text nodes if any.
                        }

                        reader.next();
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "actAsSubjectLookup").equals(reader.getName())) {
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            object.setActAsSubjectLookup(null);
                            reader.next();

                            reader.next();
                        } else {
                            object.setActAsSubjectLookup(WsSubjectLookup.Factory.parse(
                                    reader));

                            reader.next();
                        }
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "paramNames").equals(reader.getName())) {
                        // Process the array and step past its final element's end.
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            list5.add(null);

                            reader.next();
                        } else {
                            list5.add(reader.getElementText());
                        }

                        //loop until we find a start element that is not part of this array
                        boolean loopDone5 = false;

                        while (!loopDone5) {
                            // Ensure we are at the EndElement
                            while (!reader.isEndElement()) {
                                reader.next();
                            }

                            // Step out of this element
                            reader.next();

                            // Step to next element event.
                            while (!reader.isStartElement() &&
                                    !reader.isEndElement())
                                reader.next();

                            if (reader.isEndElement()) {
                                //two continuous end elements means we are exiting the xml structure
                                loopDone5 = true;
                            } else {
                                if (new javax.xml.namespace.QName(
                                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                                            "paramNames").equals(
                                            reader.getName())) {
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                            "nil");

                                    if ("true".equals(nillableValue) ||
                                            "1".equals(nillableValue)) {
                                        list5.add(null);

                                        reader.next();
                                    } else {
                                        list5.add(reader.getElementText());
                                    }
                                } else {
                                    loopDone5 = true;
                                }
                            }
                        }

                        // call the converter utility  to convert and set the array
                        object.setParamNames((java.lang.String[]) list5.toArray(
                                new java.lang.String[list5.size()]));
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement() &&
                            new javax.xml.namespace.QName(
                                "http://webservices.grouper.middleware.internet2.edu/xsd",
                                "paramValues").equals(reader.getName())) {
                        // Process the array and step past its final element's end.
                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                "nil");

                        if ("true".equals(nillableValue) ||
                                "1".equals(nillableValue)) {
                            list6.add(null);

                            reader.next();
                        } else {
                            list6.add(reader.getElementText());
                        }

                        //loop until we find a start element that is not part of this array
                        boolean loopDone6 = false;

                        while (!loopDone6) {
                            // Ensure we are at the EndElement
                            while (!reader.isEndElement()) {
                                reader.next();
                            }

                            // Step out of this element
                            reader.next();

                            // Step to next element event.
                            while (!reader.isStartElement() &&
                                    !reader.isEndElement())
                                reader.next();

                            if (reader.isEndElement()) {
                                //two continuous end elements means we are exiting the xml structure
                                loopDone6 = true;
                            } else {
                                if (new javax.xml.namespace.QName(
                                            "http://webservices.grouper.middleware.internet2.edu/xsd",
                                            "paramValues").equals(
                                            reader.getName())) {
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                                            "nil");

                                    if ("true".equals(nillableValue) ||
                                            "1".equals(nillableValue)) {
                                        list6.add(null);

                                        reader.next();
                                    } else {
                                        list6.add(reader.getElementText());
                                    }
                                } else {
                                    loopDone6 = true;
                                }
                            }
                        }

                        // call the converter utility  to convert and set the array
                        object.setParamValues((java.lang.String[]) list6.toArray(
                                new java.lang.String[list6.size()]));
                    } // End of if for expected property start element

                    else {
                    }

                    while (!reader.isStartElement() && !reader.isEndElement())
                        reader.next();

                    if (reader.isStartElement()) {
                        // A start element we are not expecting indicates a trailing invalid property
                        throw new org.apache.axis2.databinding.ADBException(
                            "Unexpected subelement " + reader.getLocalName());
                    }
                } catch (javax.xml.stream.XMLStreamException e) {
                    throw new java.lang.Exception(e);
                }

                return object;
            }
        } //end of factory class
    }
}
