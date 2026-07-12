---
title: "Migrate a GSH provisioner to compiled Java"
space: Grouper
pageId: 28555834
version: 2
lastUpdated: 2026-07-01T05:37:21.245Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555834/Migrate+a+GSH+provisioner+to+compiled+Java
---

## Today vs compiled

A GSH provisioner is a provisioner whose logic is a GSH template:

provisioner.myProvisioner.class = edu.internet2.middleware.grouper.app.gshTemplateProvisioner.GshTemplateProvisionerFactory provisioner.myProvisioner.gshTemplateConfigId = myProvisionerTemplate Today the referenced template (`myProvisionerTemplate`) is `templateMode=interpreted`, `templateType=provisioner`, and its body extends `GshTemplateV2` and constructs the provisioner and attaches it with `gshTemplateOutput.assignGrouperProvisioner(...)`.

To convert, set that template to `templateMode=compiled` and rewrite the body so the template class **is** the provisioner — extend `GshTemplateProvisionerBase` directly. The `assignGrouperProvisioner` wrapper is dropped; the factory returns your class directly. The `provisioner.myProvisioner.*` config is unchanged.

## Before (interpreted: GshTemplateV2 wrapper that attaches the provisioner)

class MyProvisionerTemplate extends GshTemplateV2 { public void gshRunLogic(GshTemplateV2input in, GshTemplateV2output out) { GrouperProvisioner provisioner = new MyProvisioner(); // a subclass of GshTemplateProvisionerBase with a target DAO out.getGsh_builtin_gshTemplateOutput().assignGrouperProvisioner(provisioner); } } 

## After (compiled: the template class is the provisioner)

package edu.institution.grouper.provisioner; import edu.internet2.middleware.grouper.app.gshTemplateProvisioner.GshTemplateProvisionerBase; import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase; public class MyProvisioner extends GshTemplateProvisionerBase { @Override protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() { return MyProvisionerTargetDao.class; } // ... the rest of your provisioner (configuration class, translation, target DAO, etc.) ... }
