Chris,

Checking in on this thread.  Do you have what you need from me to do some experiments?  Do you still have time for it?

             Regards,   —Keith  

From: Keith Hazelton
Date: Wednesday, August 13, 2014 at 15:55 
To: Chris Hyzer
Cc: Keith Hazelton
Subject: Re: Custom RESTful subject adapter revisited

Chris,

Not sure if you need this, but the urls I sent you a month ago don't include display name values, 
and that probably won't get fixed in the near term.  For development purposes, 
if you want an endpoint that returns a display name, you could use the following "contacts" endpoint:

 GET http://somehost.whatever.org/bsp/contacts/urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7

  [...]
    <contacts:contactId>urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7</contacts:contactId>
    <contacts:contactNote>Bamboo Person One Contact</contacts:contactNote>
    <contacts:emails>
        <email>PersonOne@example1.com</email>
        <contacts:email>PersonOne@example1.com</contacts:email>
    </contacts:emails>
    <contacts:displayName>Mr. John Doe</contacts:displayName>
  […]

That will work for the other uids below as well, I believe.   Let me know if you have any questions.     --Keith

From: Keith Hazelton <hazelton@wisc.edu>
Date: Friday, July 11, 2014 14:04 
To: Chris Hyzer
Cc: Keith Hazelton
Subject: Re: Custom RESTful subject adapter revisited

That’s encouraging!

So in the test source, there are at least these three subjects; the get subject by id equivalent call would be:

John Doe:
GET http://somehost.whatever.org/bsp/persons/urn:uuid:441408b6-5208-42dc-8af8-5fc6f112dc67

Melissa Smith:
GET http://somehost.whatever.org/bsp/persons/urn:uuid:d4bdf7b7-15ca-45d0-ba1d-925696cb5294

Fred Jones:
GET http://somehost.whatever.org/bsp/persons/urn:uuid:f64a32a5-ef22-4797-a852-f3eed6101860

At this stage, there is no authentication and there are only a handful of attributes 
that we would care about: dcterms:subject, person:bambooPersonId, contacts:displayName
(which seems empty on these test subjects) and partNameTypes of given and family paternal.

I’ve cleared us using these endpoints with the project lead, Bridget Almas.

Here’s a curl example:

dyn-128-104-18-225:_notesPlus khazelton$ curl http://somehost.whatever.org/bsp/persons/urn:uuid:441408b6-5208-42dc-8af8-5fc6f112dc67
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<person:bambooPerson xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:bsp="http://projectbamboo.org/bsp/resource" xmlns:contacts="http://projectbamboo.org/bsp/services/core/contact" xmlns:person="http://projectbamboo.org/bsp/BambooPerson" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <dcterms:subject>Mr. John Doe</dcterms:subject>
    <dcterms:creator xsi:type="dcterms:URI">urn:uuid:99999999-9999-9999-9999-999999999999</dcterms:creator>
    <dcterms:created xsi:type="dcterms:W3CDTF">2014-03-17T21:15:09.438Z</dcterms:created>
    <bsp:modifier>urn:uuid:99999999-9999-9999-9999-999999999999</bsp:modifier>
    <dcterms:modified xsi:type="dcterms:W3CDTF">2014-03-17T21:15:09.446Z</dcterms:modified>
    <person:bambooPersonId>urn:uuid:441408b6-5208-42dc-8af8-5fc6f112dc67</person:bambooPersonId>
    <person:sourcedId>
        <dcterms:subject/>
        <dcterms:creator xsi:type="dcterms:URI">urn:uuid:441408b6-5208-42dc-8af8-5fc6f112dc67</dcterms:creator>
        <bsp:modifier>urn:uuid:99999999-9999-9999-9999-999999999999</bsp:modifier>
        <person:sourcedIdId>urn:uuid:c1a1c918-b199-45bd-82e7-206f4f403ef2</person:sourcedIdId>
        <person:sourcedIdName>Person One SourcedId</person:sourcedIdName>
        <person:bambooPersonId>urn:uuid:441408b6-5208-42dc-8af8-5fc6f112dc67</person:bambooPersonId>
        <person:sourcedIdKey>
            <idPId>http://example1.com</idPId>
            <userId>ad77ebe8c7c6d4ca569a18ad0483df1245ffd2d734cc065b9328b9c121ac8adc</userId>
            <person:idPId>http://example1.com</person:idPId>
            <person:userId>ad77ebe8c7c6d4ca569a18ad0483df1245ffd2d734cc065b9328b9c121ac8adc</person:userId>
        </person:sourcedIdKey>
        <person:accountNonExpired>true</person:accountNonExpired>
        <person:accountNonLocked>true</person:accountNonLocked>
        <person:credentialsNonExpired>true</person:credentialsNonExpired>
        <person:enabled>true</person:enabled>
    </person:sourcedId>
    <person:bambooProfile person:confidential="false" person:primary="true">
        <dcterms:subject/>
        <dcterms:creator xsi:type="dcterms:URI">urn:uuid:441408b6-5208-42dc-8af8-5fc6f112dc67</dcterms:creator>
        <dcterms:created xsi:type="dcterms:W3CDTF">2014-03-17T21:15:09.446Z</dcterms:created>
        <bsp:modifier>urn:uuid:99999999-9999-9999-9999-999999999999</bsp:modifier>
        <dcterms:modified xsi:type="dcterms:W3CDTF">2014-03-17T21:15:09.446Z</dcterms:modified>
        <person:profileId>urn:uuid:0d3b9033-3588-4261-8bff-f66974c23862</person:profileId>
        <person:profileInformation>PersonOne profile</person:profileInformation>
        <person:bambooPersonId>urn:uuid:441408b6-5208-42dc-8af8-5fc6f112dc67</person:bambooPersonId>
        <contacts:bambooContact>
            <dcterms:subject/>
            <dcterms:creator xsi:type="dcterms:URI">urn:uuid:99999999-9999-9999-9999-999999999999</dcterms:creator>
            <dcterms:created xsi:type="dcterms:W3CDTF">2014-03-17T21:25:36.487Z</dcterms:created>
            <bsp:modifier>urn:uuid:99999999-9999-9999-9999-999999999999</bsp:modifier>
            <dcterms:modified xsi:type="dcterms:W3CDTF">2014-03-17T21:25:36.488Z</dcterms:modified>
            <contacts:contactId>urn:uuid:61c8922b-2d23-4fba-9bce-5f1dde6decf7</contacts:contactId>
            <contacts:contactNote>Bamboo Person One Contact</contacts:contactNote>
            <contacts:emails>
                <email>PersonOne@example1.com</email>
                <contacts:email>PersonOne@example1.com</contacts:email>
            </contacts:emails>
            <contacts:displayName></contacts:displayName>
            <contacts:partNames>
                <contacts:partNameType>HONORIFIC_PREFIX</contacts:partNameType>
                <contacts:partNameContent>Mr.</contacts:partNameContent>
                <contacts:partNameLang>eng</contacts:partNameLang>
            </contacts:partNames>
            <contacts:partNames>
                <contacts:partNameType>NAME_GIVEN</contacts:partNameType>
                <contacts:partNameContent>John</contacts:partNameContent>
                <contacts:partNameLang>spa</contacts:partNameLang>
            </contacts:partNames>
            <contacts:partNames>
                <contacts:partNameType>NAME_FAMILY_PATERNAL</contacts:partNameType>
                <contacts:partNameContent>Doe</contacts:partNameContent>
                <contacts:partNameLang>spa</contacts:partNameLang>
            </contacts:partNames>
            <contacts:telephones>
                <contacts:telephoneType>VOICE</contacts:telephoneType>
                <contacts:telephoneNumber>212-555-1212</contacts:telephoneNumber>
                <contacts:locationType>HOME</contacts:locationType>
            </contacts:telephones>
            <contacts:telephones>
                <contacts:telephoneType>SMS</contacts:telephoneType>
                <contacts:telephoneNumber>999-555-1212</contacts:telephoneNumber>
                <contacts:locationType>SABBATICAL</contacts:locationType>
            </contacts:telephones>
            <contacts:iMs>
                <contacts:instantMessagingType>SKYPE</contacts:instantMessagingType>
                <contacts:account>PersonOneSkype</contacts:account>
                <contacts:locationType>WORK</contacts:locationType>
            </contacts:iMs>
            <contacts:addresses>
                <contacts:streetAddress1>123 Main St.</contacts:streetAddress1>
                <contacts:streetAddress2>2nd Fl</contacts:streetAddress2>
                <contacts:locality>New York</contacts:locality>
                <contacts:region>NY</contacts:region>
                <contacts:postalCode>10001</contacts:postalCode>
                <contacts:country>USA</contacts:country>
                <contacts:locationType>WORK</contacts:locationType>
            </contacts:addresses>
        </contacts:bambooContact>
        <person:interests person:confidential="false">
            <person:interest>PersonOne Interest</person:interest>
        </person:interests>
        <person:expertises person:confidential="false">
            <person:expertise>PersonOne expertise</person:expertise>
        </person:expertises>
        <person:externalAffiliations>http://harvard.edu</person:externalAffiliations>
        <person:preferredLanguage>eng</person:preferredLanguage>
        <person:languageUsedInScholarships>spa</person:languageUsedInScholarships>
        <person:otherProfiles person:confidential="false">
            <person:profileName>PersonOneOtherProfile</person:profileName>
            <person:profileUrl>http://PersonOne.org</person:profileUrl>
        </person:otherProfiles>
        <person:authorizedPublisher>true</person:authorizedPublisher>
    </person:bambooProfile>
</person:bambooPerson>
dyn-128-104-18-225:_notesPlus khazelton$ 

From: Chris Hyzer <mchyzer@isc.upenn.edu>
Date: Friday, July 11, 2014 at 13:44 
To: Keith Hazelton <hazelton@wisc.edu>
Subject: RE: Custom RESTful subject adapter revisited

I do plan on making one of these for my new SCIM/CIFER service 
 
Lets start with get subject by id… what service is available, what 
attributes do you want to expose, etc?  What authentication does it use?
 
Thanks,
Chris
 
From: Keith Hazelton
Sent: Friday, July 11, 2014 2:42 PM
To: Chris Hyzer
Subject: Custom RESTful subject adapter revisited
 
Chris,
 
A project at Tufts is using some of the components of the Bamboo project 
from way back.  One thing I’m trying to help with is a custom subject adapter 
that does searches against a RESTful person service rather than the more 
traditional LDAP or JDBC approach.  I mentioned this probably over a year 
ago now, but I’m coming back to it.
 
Would you have any time and/or interest in helping me with this?  This could take a few
different forms. The minimal form would be helping me when I bump into challenges
developing the custom subject adapter.  I’ve started down this path and could use 
some input/advice. On the other hand, maybe it would be more efficient all around 
if you put together a basic custom RESTful subject adapter based on my providing
info on the RESTful person service endpoints that are available.  What do you think?
 
           Regards,   —Keith
