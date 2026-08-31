---
title: "Grouper container institutional images"
space: Grouper
pageId: 28554290
version: 8
lastUpdated: 2026-07-12T15:27:07.047Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554290/Grouper+container+institutional+images
---

It is recommended if you have derived images of the Grouper image (incommon group image) to have one institutional image and

- deploy that to each env (dev/test/prod) and module. (UI / WS / daemon / GSH)
- or make a further subimage of your institutional image and deploy that to each env/module

This document shows how Penn previously organized derived images, and what it moved to.

## Current/new design

- If you can deploy one image and have the orchestration inject the secrets and envrionment variables then you do not need derived images for each environment / module, you can just deploy the institutional image
- 1 copy of files and directory structures in the institutional image
- For the files which are intended to be different, the swiss-army knife institutional image will dynamically see what it is via environment variables (e.g. a test-ws), and rearrange the files appropriately
  
  
  
  - e.g. there are some static HTML files to accommodate eforms
- You need a CI/CD on your institutional image. You can decide the specifics of this, but one idea is what we did at Penn.
  
  - To build the institutional image, there is one branch “main”, and each commit/push will automatically build an immutably versioned image and publish it to AWS ECR (Elastic container registry). The version is just an incrementing integer
    
    
    
    - The fact that the image versions are immutable is crucial for this design. We NEVER want an errant (or intentional  ) commit/push to change what version 79 means for example.
    - Any changes needed to a institutional image will just create a new image versioned with the next integer
    - Environment specific images (e.g. ui/ws/etc in the test env) will generally be on the same version of the institutional image, but do not need to be
- Each module (ui/ws/etc) had its own git folder (you can design this however you want)
- The environments are different branches (prod is “prod” branch, test is “test” branch) (you can design this however you want)
- Each commit/push makes a new image for that env/module (e.g. test-ui) and deploys to AWS ECS (Elastic container service runs the appropriate number of nodes for that module in that environment)
  
  
  
  - e.g. there are two daemons in prod
  - e.g. there are three web services containers in prod
  - e.g. there is one command line container in test
- The environment images generally just consist of a Dockerfile which specify which institutional image to use and some environment variables (e.g. its the “test” env, its the “ws” module, Java should have 3g of memory, etc)
  
  
  
  - There is still an opportunity to temporarily “decorate/patch” a module image with fixes, which could be removed once those are incorporated into the Grouper product
- Advantages
  
  
  
  - An upgrade that involves 5 changes to the image, requires 5 changes to the institutional image, and bumping the institutional image version in the Dockerfile for each module
  - There is no drift between which files are included for each env or module
  - If we rollback an upgrade, we just decrease the institutional image version number in the Dockerfile for that module
- Here is what the institutional image looks like (everything we need for any environment/module)

- Here is what a module image looks like (very thin)
- Note the build progression is generally linear, unless we are upgrading a major version of Grouper (every year or two), we are just working forward with a similar version in test and prod

## Upgrading the institutional image

In the Dockerfile of the Institutional image

1. Change the version of the Grouper version (note any [upgrade instructions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549165/v5+Upgrade+instructions+from+v5))
2. Make any other changes necessary in the file structure
3. Commit and push the changes to institutions Gitlab
4. See the Jenkins cloudbees output
5. The image is in Penn’s AWS ECR (Elastic container registry)
6. By design, this does not change any running grouper containers until those are built
7. This is handled by the AWS systems team, but the Jenkinsfile for the Penn image is here
  
  
  ```
  #!groovy
  @Library('defaultlibrary')_
  
  def targetaccountassumedrole = ':role/CrossAccountFargate'
  def awsaccountnumber = '123'
  def project_token = 'abc'
  def myslackchannel = '#slack' // Slack notification channel
  def myslacktokenCredentialId = 'user' // Credentials that the shared library uses to post slack messages
  def myslackteamDomain = 'domain'
  
  
  pipeline {
      //agent any
      //agent {
          //label 'TerraForm'
      //}
      agent {
          //label 'terrafrom12-26-grouper'
          label 'amzLinux2023-docker-terraform-python3-aws-cli-jq'
      }
  
      triggers {
          gitlab(
              triggerOnPush: true, 
              triggerOnMergeRequest: true,
              triggerOnApprovedMergeRequest: true,
              branchFilterType: 'NameBasedFilter', 
              includeBranchesSpec: "main",
              //excludeBranchesSpec: "master",
              secretToken: project_token
              )
      }
  
      stages {
          stage('image deploy') {
              steps {
                  withCredentials([[$class: "AmazonWebServicesCredentialsBinding", accessKeyVariable: "AWS_ACCESS_KEY_ID", credentialsId: "grouper-account", secretKeyVariable: "AWS_SECRET_ACCESS_KEY"]]){
                  checkout([$class: 'GitSCM',
                  branches: [[name: "$gitlabBranch" ]],
                  extensions: scm.extensions,
                  userRemoteConfigs: [[
                      url: 'https://gitlab.com/isc-penn/cloudapps/aws/upenn-isc-grouper/penn-grouper-subimage.git',
                      credentialsId: 'cone-prod-cm-aws-deployed-apps-jenkinsfiles'
                  ]]
              ])    
              script {
              sendNotifications.buildstartnotify ("${myslackchannel}", "${myslacktokenCredentialId}", "${myslackteamDomain}")
                  }        
                      sh 'ls'
                      sh '''
                      echo $BUILD_NUMBER
                      #$(aws ecr get-login-password --region us-east-1)
                      aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 123.dkr.ecr.us-east-1.amazonaws.com
                      docker build -t 123.dkr.ecr.us-east-1.amazonaws.com/penn-grouper-image:$BUILD_NUMBER .
                      docker tag 123.dkr.ecr.us-east-1.amazonaws.com/penn-grouper-image:$BUILD_NUMBER 056999794094.dkr.ecr.us-east-1.amazonaws.com/penn-grouper-image:latest
                      docker push 056999794094.dkr.ecr.us-east-1.amazonaws.com/penn-grouper-image:$BUILD_NUMBER
                      #printenv
                      '''
                  }
              }
          }
      }
          post {
             always {  
                 sendNotifications ("${myslackchannel}", "${myslacktokenCredentialId}", 'My Extra Message', "${myslackteamDomain}")
             }
         }
  
  }
  
  
  ```

## Upgrading a module

After the institutional image is ready:

1. Adjust the institutional image version in the Dockerfile of a module
2. Remove previous unneeded patches, or add new ones, and commit/push to git
3. See the Jenkins cloudbees output
4. See the new container in AWS ECS (Elastic container service) or whatever orchestration you have. Note the health check should pass and previous containers will be automatically phased out and removed
5. Note if there are any DDL changes, the GSH container can be deployed first and the DDL can be carefully run. Though if any container builds it will adjust the DDL as needed
6. Grouper is now running that new version for that module!

## Handling differences among the modules/environments

1. ECS knows if there is a test or prod and injects the DB (and some other external systems) credentials as needed. At Penn this is done through AWS secrets manager and environment variables injected at runtime (NOT baked into the image, so it’s not in ECR, which would be a security issue!)
2. The Dockerfile for the env/module specifies some differences. In this case is specifies the memory, the fact that tomcat should run (it doesn't run in the command line container), that it’s a test env, and that the daemon should run (as opposed to ui/ws)
3. For more sophisticated things, there is a bash script (in the institutional container only) that runs on container startup (grouperScriptHooks.sh) that can make adjustments. In this case on line 22, you see there is a different health check script that should run for GSH command line containers. In other containers the health check sees if tomcat is running. For GSH it just sees if the container is there.
4. Another example is the eforms endpoint is different in a static file in test vs prod.
  
  
  
  1. In the static file in the institutional image we replace the endpoint with a variable
  2. We set the environment in the module Dockerfile
  3. Then substitute in the institutional image grouperScriptHooks.sh file at runtime

## Previous container design at Penn

The Grouper image is the InCommon Internet2 DockerHub Grouper image.

- Each module (ui/ws/etc) had its own git folder
- The environments are different branches (prod is “prod” branch, test is “test” branch)
- Each commit/push makes a new image for that env/module (e.g. test-ui) and deploys to AWS ECS (Elastic container service runs the appropriate number of nodes for that module in that environment)
  
  
  
  - e.g. there are two daemons in prod
  - e.g. there are three web services containers in prod
  - e.g. there is one command line container in test
- 8 copies of files and directory structures
- Some files are intended to be different for different modules
  
  
  
  - e.g. the UI authentication config is not deployed with the WS/daemon/command line
- Disadvantages:
  
  
  
  - An upgrade that involved 5 changes to the image, requires 40 changes to git
  - Sometimes there is drift between test/prod or test-ui and prod-ui since changes have to be made multiple times
  - We do not need to roll back a lot, but it would be difficult since would need to find the commit that was previously there, and compare and resurrect those files.
- Here is what a module image looked like (there are 8 of these!)
