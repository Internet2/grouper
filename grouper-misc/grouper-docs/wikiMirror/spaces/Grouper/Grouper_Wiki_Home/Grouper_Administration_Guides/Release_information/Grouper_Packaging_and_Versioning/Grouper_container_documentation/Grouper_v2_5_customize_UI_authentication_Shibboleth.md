---
title: "Grouper v2.5 customize UI authentication Shibboleth"
space: Grouper
pageId: 28554265
version: 4
lastUpdated: 2026-07-01T05:40:49.359Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554265/Grouper+v2.5+customize+UI+authentication+Shibboleth
---

To allow authentication using Shibboleth:

1. Add /etc/shibboleth/shibboleth2.xml. This can be copied using your Dockerfile if you have one. Or you can overlay it by adding the file to slashRoot/etc/shibboleth/shibboleth2.xml.
  
  1. Make sure you've set your entityID correctly in the file.
  2. Add the entityID for your IdP or configure discovery.
  3. Update the MetadataProvider section to point to InCommon, another federation, a local file, etc. If you're doing signature validation, you'll need to add that certificate as well. e.g. slashRoot/etc/shibboleth/fedsigner.pem
  4. Of course you may have other changes per your environment.
2. Add your signing and encryption keys to /etc/shibboleth/. The default file names are sp-signing-key.pem, sp-signing-cert.pem, sp-encrypt-key.pem, sp-encrypt-cert.pem. Again this can be overlaid using slashRoot.
  
  1. Note that sp-signing-key.pem and sp-encrypt-key.pem are private keys (secrets).
  2. Make sure shibd has read access to these files in the container.
