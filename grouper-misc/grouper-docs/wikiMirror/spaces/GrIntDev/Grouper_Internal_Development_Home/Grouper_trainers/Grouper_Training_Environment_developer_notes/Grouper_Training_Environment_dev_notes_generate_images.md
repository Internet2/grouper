---
title: "Grouper Training Environment dev notes - generate images"
space: GrIntDev
pageId: 48794130
version: 13
lastUpdated: 2026-07-12T06:46:31.219Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794130/Grouper+Training+Environment+dev+notes+-+generate+images
---

## New way

1. [aws.internet2.edu](http://aws.internet2.edu/) (login as mcg)
2. ec2 and select launch templates.. and the modify the gte on demand one and bump the version
3. api call from grouper is set to use the $latest version

Store stuff in: [https://github.internet2.edu/ICP-OPs/training-vm-automation.git](https://github.internet2.edu/ICP-OPs/training-vm-automation.git) → [mkstudent-onDemand.sh](https://github.internet2.edu/ICP-OPs/training-vm-automation/blob/main/mkstudent-onDemand.sh)

## Old way

Generate student images

1. Make sure "gte" command has right branch in it at top
2. Make sure the [mkstudent.sh](https://github.internet2.edu/docker/grouper_training/blob/GROUPER_BUILD_CLOUD_FORMATION/internal/mkstudent.sh) file points to the right build, and has the right docker containers to pull for exercises, and the right commands to download
  
  
  ```
  export GROUPER_GTE_BRANCH=GROUPER_BUILD_CLOUD_FORMATION
  #export GROUPER_GTE_DOCKER_BRANCH=GROUPER_BUILD_CLOUD_FORMATION
  export GROUPER_GTE_DOCKER_BRANCH=202006
  ```
3. Make sure TARGET_BRANCH is updated in Jenkinsfile
4. Commit to a branch in [https://github.internet2.edu/docker/grouper_training](https://github.internet2.edu/docker/grouper_training)
5. Make sure the [Docker images are available in dockerhub](https://hub.docker.com/r/tier/gte) after [CI/CD](https://jenkins.testbed.tier.internet2.edu/job/docker/job/grouper_training/)
6. Tag the commit that you want to build EC-2's for with GROUPER_BUILD_CLOUD_FORMATION. Note this uses a tag since that is hardcoded in the AWS launch configuration in the user data script, and that cannot be edited (immutable).
7. Go to [https://aws.internet2.edu](https://aws.internet2.edu/), 626413038627, us-east-2
8. Ec-2 - Autoscaling groups, Grouper Training, adjust the number of hosts
9. Wait 15 minutes, then run this to generate passwords
  
  
  ```
  [mchyzer@i2midev6 ~]$ wget https://github.internet2.edu/docker/grouper_training/raw/GROUPER_BUILD_CLOUD_FORMATION/internal/passwordsToSpreadsheet.sh
  [mchyzer@i2midev6 ~]$ chmod +x passwordsToSpreadsheet.sh 
  [mchyzer@i2midev6 ~]$ ./passwordsToSpreadsheet.sh 
  ```

## How this was setup

1. All scripts in git in "[internal](https://github.internet2.edu/docker/grouper_training/tree/GROUPER_BUILD_CLOUD_FORMATION/internal)"
2. Note the "[user data](https://github.internet2.edu/docker/grouper_training/blob/GROUPER_BUILD_CLOUD_FORMATION/internal/userdata.sh)" script in cloud formation is in git. This should be as thin as possible (e.g. download and run the real script)
  
  
  ```
  #!/bin/bash
  
  yum -y install wget
  cd /root
  wget "https://github.internet2.edu/docker/grouper_training/raw/GROUPER_BUILD_CLOUD_FORMATION/internal/mkstudent.sh"
  chmod +x mkstudent.sh
  /root/mkstudent.sh
  
  ```
3. "GTE Env" launch configuration (doesn't need to change for each training)  
  
  
  1. Standard Amazon linux AMI: amzn2-ami-hvm-2.0.20190508-x86_64-gp2 - ami-0ebbf2179e615c338
  2. Has the user data script from git immutable
  3. t3a.large, 30gb storage, public ip's, gte-training security group (exposes only port 22)
4. "Grouper Training" Autoscaling Group uses the "GTE Env" launch configuration (doesn't need to change for each training)
  
  1. Has all subnets
  2. Cloudwatch monitoring=true
5. [mkstudent.sh](https://github.internet2.edu/docker/grouper_training/blob/GROUPER_BUILD_CLOUD_FORMATION/internal/mkstudent.sh) script is also fairly thin
  
  1. installs docker and editors and tools for training
  2. pulls docker images
  3. downloads commands, sets executable, adds to path
6. [GTE commands](https://github.internet2.edu/docker/grouper_training/blob/GROUPER_BUILD_CLOUD_FORMATION/) allow [easy docker operations](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543607/Grouper+training+VM+documentation)

## Passwords to spreadsheet

First off, this runs on i2midev6 as mchyzer

To replicate

1. Make a user here: [https://console.aws.amazon.com/iam/home?region=us-east-2#/users](https://console.aws.amazon.com/iam/home?region=us-east-2#/users)
2. Install aws CLI
3. mkdir ~/.aws
4. ~/.aws/credentials
  
  
  ```
  [default]
  aws_access_key_id = **********
  aws_secret_access_key = **********
  ```
5. ~/.aws/config
  
  
  ```
  [default]
  region = us-east-2
  ```

## Previous method of generating images

1. ssh to master AMI image
  
  1. ```
     ssh -i gte-training-master.pem ec2-user@3.136.154.33
    ```
2. Clean out old images
  
  1. ```
    docker rmi -f $(docker images -aq)
    ```
3. Pull new images for class (this is for the 202006 class)
  
  1. e.g.
    
    
    ```
    docker pull tier/gte:401.4.end-202006
    docker pull tier/gte:401.4.1-202006
    docker pull tier/gte:401.3.end-202006
    docker pull tier/gte:401.3.1-202006
    docker pull tier/gte:401.2.end-202006
    docker pull tier/gte:401.2.1-202006
    docker pull tier/gte:401.1.end-202006
    docker pull tier/gte:401.1.1-202006
    docker pull tier/gte:301.4.1-202006
    docker pull tier/gte:211.1.1-202006
    docker pull tier/gte:201.5.end-202006
    docker pull tier/gte:201.5.1-202006
    docker pull tier/gte:201.4.end-202006
    docker pull tier/gte:201.4.1-202006
    docker pull tier/gte:201.3.end-202006
    docker pull tier/gte:201.3.1-202006
    docker pull tier/gte:201.2.end-202006
    docker pull tier/gte:201.2.1-202006
    docker pull tier/gte:201.1.end-202006
    docker pull tier/gte:201.1.1-202006
    docker pull tier/gte:101.1.1-202006
    docker pull tier/gte:full_demo-202006
    docker pull tier/gte:base-202006
    docker pull i2incommon/grouper:2.5.28
    docker pull tier/shib-idp:3.4.3_20190201
    docker pull rabbitmq:management
    ```
4. Navigate to [https://aws.internet2.edu](https://aws.internet2.edu/), select the **Administrator** role for account **internet2-training (626413038627)**
5. Change to the **Ohio (us-east-2) region**
6. Navigate to **Instances** under the **EC2 Console**
7. Right click on the instance named **gte-master** and select **Image → Create Image** 
  
  1. Input a name for the image like gte-202006
  2. Click **Create Image**
8. A window will appear that says **Create Image Request Received**, and will have a link to view the pending image, click the link
9. **Copy the AMI ID** of the AMI (e.g. ami-045d1df0ec4b860e0)
10. When the status for the image says **Available**, click on the link for **Auto Scaling → Launch Configurations**
11. Right click on the Launch configuration for the **GTE Env** with the newest creation time and select **Copy Launch Configuration**
12. Under the **AMI Details** section, click **Edit AMI**
13. Paste the new AMI ID into the box that has the current AMI and hit enter
14. The newly created AMI with the name you provided above should appear
15. Click **Select AMI**
16. A window will appear that says, "You selected a different AMI." Click **Yes, I want to continue with this AMI**
17. The current GTE instance is a **t3a.large**, select a different instance size if necessary
18. Click **Next: Configure Details**
19. Give the Launch Config a meaningful name, like **GTE Env 202006**
20. If the **User Data** needs to be changed (i.e. the script that runs on spin up), expand the **Advanced Details** section and make changes
21. Click **Next: Add Storage**
22. The current root filesystem is **40 GiB**, change this is necessary
23. Click **Next: Configure Security Groups**
24. Select the radio button next to **Select an existing security** group, and click the one named **gte-training**
25. Click **Review** and look at the summary to make sure everything is correct
26. Click **Create Launch Configuration**
27. Select **gte-training-master** as the key (or create a new key if you're testing)
28. Click **View Your Scaling Groups**
29. Right click on **gte-test** and click **edit**
30. In the **Launch Configuration** dropdown, select your newly created **LauchConfig**
31. Adjust the **Desired Capacity**, **Min** and **Max** values to be the number of VMs you need for the class
32. Click **Save**
33. The Autoscaling service will now being to spin up the new instances
34. Wait about 10 minutes or until the number of the instances in the EC2 console matches the desired number for the autoscaling group
35. The User Data script creates the password for the student user during the spinup process and outputs it to a cloudwatch log group
36. Use a set of aws credentials for the **internet2-training (626413038627)** account that has an IAM policy that allows for reading EC2 and Cloudwatch services
37. Run the following script:
  
  
  
  
  ```
  aws ec2 describe-instances | jq '[.Reservations | .[] | .Instances | .[] | select(.State.Name!="terminated") | select((.Tags[]|select(.Key=="env")
  |.Value) =="training")] | .[] | .InstanceId' | xargs -n 1 -I{}  aws  ec2 get-console-output  --instance-id {} | fmt | grep student | awk -F\\ '{print $1
  }' | awk -F , '{print $1 "\t" $2 "\t" $3 "\t\t\tssh -L 8443:localhost:8443 -l student"$1}'
  ```
  
  
  
  ****
38. Paste that into Erin's google sheet for the class

> Person performing the actions above are assumed to have a copy of the gte-training-master.pem file and API credentials for that account.

babb note:

```
I think it was just docker and enable SSH in /etc/cloud/cloud.cfg so things could get cloned correctly. I forwarded you an email of what we did to clone it and make it the student account. I had a script that would make the student account, set a random password for it, add it to the docker group, etc and print it out.  We originally had student/student on the master and the master got pwned unsurprisingly...  was in /root/mkstudent.sh
```

```
Once the gte-master is prepared with whatever updates are needed, clone it a bunch of times. I don’t remember if I had to select the ssh key each time or if it just cloned over.
Then for each VM do something like this:
ssh -i "gte-training-master.pem" ec2-user@3.14.11.71 "sudo /root/mkstudent.sh"
*do not* run mkstudent on the master. It will create the student account on each individual VM with a separate password. Our previous GTE got pwned because it was all student/password. The master should have no password accounts at all on it.
The mkstudent script will print:   IP of the VM, “student”, password set
I grabbed the IPs from the AWS console of my cloned EC2 instances and then added them all to a script. Used tee  to grab the output from my script to make the user accounts on all the VMs. Then pasted the output in to a google sheet and assigned each trainee one of the VMs. We shared out the google sheet to the whole class instead of individually emailing each person.
```
