---
title: "Oregon State University Grouper Project Page"
space: Grouper
pageId: 28543632
version: 14
lastUpdated: 2026-07-01T05:49:20.976Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543632/Oregon+State+University+Grouper+Project+Page
---

Oregon State University deployed Grouper v2.2.1 to production in Spring 2015 and upgraded to v2.3.0 in Winter 2017.

See slides from the [IAM Online of June 2015,](https://www.incommon.org/docs/iamonline/20150610_IAMOnline.pdf) pages 3-9

#### Current

- Loader & PSP

- ChangeLogConsumer
- Hook
  
  - GroupName validation for configured stem (See **Service Groups** below.)
- Web Services

#### Possible Future Plans

- Deprovisioning (aging out services and sending out notifications)
- PSPNG
- Release permission groups to AWS management console via SAML entitlements (Shibboleth)
- Course groups to Canvas, AD/LDAP, Google (Loader/Messaging)
- Google Apps Grouper Provisioner
- Rules (notify certGroup admin when user is removed from employee’s group)

#### External Service Accounts

- Extend ChangeLogConsumerBase class
- Override processChangeLogEntries
- Check for changes in
  
  - WebEx: webex staff/students group
  - Kaltura: All Users group
  - Box: Box-eligible employee group
- Call API/web services to change user's account status whenever the following change types occur:
  
  - ChangeLogTypeBuiltin.MEMBERSHIP_ADD
  - ChangeLogTypeBuiltin.MEMBERSHIP_DELETE
- WebEx reference: [https://developer.cisco.com/media/webex-xml-api/311SetUser.html](https://developer.cisco.com/media/webex-xml-api/311SetUser.html)
- Kaltura reference: [http://www.kaltura.com/api_v3/testmeDoc/index.php?page=overview](http://www.kaltura.com/api_v3/testmeDoc/index.php?page=overview)
- Box reference: [https://docs.box.com/reference](https://docs.box.com/reference)

#### Service Groups

(Planned deployment Spring/Summer 2017)

Service Groups is a stem in Grouper that houses groups and memberships intended for external services such as Box. It's further sub-divided by IT service organization where the Grouper group management is delegated.

- Components
  
  - UI - Slightly modified [Unicon's provisioning target UI](https://github.com/Unicon/grouper-provisioning-target-ui) to add support for group provisioning with additional attributes.
  - Hook
    
    - GroupNameValidationHook
    - Configurable:
      
      - uniqueness
      - case sensitivity
      - reserved names
      - name and displayExtension length
      - parentStem where validation should be done. (Can configure multiple stems with their own set of config.)
  - Consumer
    
    - Provision/deprovision groups and their memberships to/from external services, eligibility-check can also be included.
    - Update attributes on external service groups, if configured.
- Supports
  
  - Box  
    
    
    - Group name uniqueness is ensured by the hook
    - Group name, description, invitability, visibility and membership are all managed through Grouper.
    - Box group ID is saved as an attribute on Grouper group.
    - Grouper group uuid is saved in Box group.
  - Support for other external services can be added by updating config files, attributes, and the consumer.
