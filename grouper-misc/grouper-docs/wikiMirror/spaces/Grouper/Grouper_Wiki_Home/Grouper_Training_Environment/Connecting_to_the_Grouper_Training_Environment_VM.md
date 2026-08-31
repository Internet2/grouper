---
title: "Connecting to the Grouper Training Environment VM"
space: Grouper
pageId: 28543735
version: 28
lastUpdated: 2026-07-01T05:49:07.751Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM
---

To start your VM:

1. Visit the  [InCommon Training VM manager.](https://grouper.at.internet2.edu/grouper/grouperUi/app/UiV2Main.indexGshSimplifiedUi?operation=UiV2Template.newTemplateSimplifiedUi&templateType=GTEManageAWSInstance&stemId=d0fcbfb223e74a288de5a388d42e6fca)
2. View status to see how many VM hours you have remaining
3. Start your VM
  
  1. Enter the number of hours you want the VM to run until it is terminated automatically
  2. You can terminate it manually from the [VM manager - https://incommon.org/VM-manager](https://grouper.at.internet2.edu/grouper/grouperUi/app/UiV2Main.indexGshSimplifiedUi?operation=UiV2Template.newTemplateSimplifiedUi&templateType=GTEManageAWSInstance&stemId=d0fcbfb223e74a288de5a388d42e6fca)
  3. If it is terminated automatically, it will run for a few more minutes than you specified
4. Wait a few minutes for it to start
5. Note the ssh command and password
6. Note: before connecting to your VM, your workstation must not have an application listening on ports 8443, 8432, or 8389
  
  1. This is not common, but if you see errors it could be the reason
7. Connect to the VM using ssh or an SSH client
  
  1. Most people can open a terminal / powershell
  2. On a Mac, click the search magnifying glass in the upper right and type "terminal". Click on the result
  3. On Windows, search for "powershell". Click on the result
8. If you are not using the SSH command in terminal / powershell, be sure to port forward: 
  
  1. workstation port 8443 to VM localhost port 8443
  2. workstation port 8432 to VM localhost port 5432
  3. workstation port 8389 to VM localhost port 389
9. Paste the command from the VM manager into the terminal / powershell, then press <return/enter>
  
  1. You can then paste your password.
  2. If it does not work, try again and carefully type your password and press <return/enter>.
  3. Note: you will not see the password as you type or paste it.
  4. When it asks if you are sure you want to continue connecting, that is expected, and answer "yes". 
    
    1. This happens the first time you connect to a new SSH host.
10. Example screen
11. If the SSH connection hangs:
  
  1. Maybe your VM is not started yet and you need to wait another minute
  2. Maybe you are connected to a VPN and need to disconnect.
  3. Maybe your VM is stopped. Go the  [InCommon Training VM manager](https://incommon.org/VM-manager) and check the status
12. Wait a minute for Grouper to start. If it does not start or you want to reset the state of the VM to the starting state (delete all work), type in the SSH console: gte 101.1.1
13. Go to the VM jump page:  [https://localhost:8443](https://localhost:8443/) (and accept any SSL security warnings)
14. If you want to do SQL queries in your Grouper database, have a SQL browser handy for postgres, or install the free DBeaver application
15. If you want to browse the LDAP database in your Grouper database, have an LDAP browser handy, or install the free Apache Directory Studio application

The virtual machine (VM) is a tool that will allow you to access a secure and private instance of Grouper where you can practice your new skills. Each time you create a VM it will have a different IP address, but you will use the same password. You have VM hours for all of the trainings that you have registered for. You can use hours allotted for any training for any other training or for any other purpose. Once all your total VM hours have been used, you will lose access to the InCommon training VM, though you can run the container locally on your own workstation/server. If the password is not accepted, trying carefully typing it in instead of copy/paste. When a VM is destroyed, all state in the container, database, and LDAP will be lost. You should complete each hands-on lesson that you start while the VM is running or expect to start over when you return. If your workstation terminal/powershell does not have the "ssh" command, then you will need to install that command, or SSH and port-forward with another SSH tool such as Putty, SecureCRT, etc. Keep these instructions handy as you take Grouper trainings so you can start your VM as needed.

Read further if you are having issues connecting to your VM

## OpenSSH

OpenSSH provides the command line **ssh**client found on most UNIX/Linux/Mac systems. This also works in windows powershell.

On Mac, open Finder → Utilties → Terminal

Copy and paste the SSH command from the [InCommon Training VM Manager](https://incommon.org/VM-manager). e.g. (a.b.c.d is your IP address)

```
$ ssh -L 8443:localhost:8443 -L 8432:localhost:5432 -L 8389:localhost:389 -l student a.b.c.d

```

If you have Mac/Unix and want to use public key (note: this is not all that useful since when the VM is destroyed, the public key will not be on the server until you upload again)

```
mkdir -p ~/.ssh
chmod 700 .ssh
ssh-keygen -o
```

Make a config file

```
vi ~/.ssh/config

Host gte
   HostName 1.2.3.4
   User student
   LocalForward 8443 localhost:8443
   LocalForward 8432 localhost:5432
   LocalForward 8389 localhost:389
   IdentityFile /Users/myusername/.ssh/id_rsa
   ServerAliveInterval 240
   ServerAliveCountMax 2

```

Save public key on server

```
mkdir .ssh
chmod 700 .ssh
cd .ssh
vi authorized_keys   (press i to edit) 
<paste the public key from ~/.ssh/id_rsa>
ESC : w q 
chmod 400 authorized_keys
```

Connect to server

```
mchyzer@Chriss-MacBook-Pro-2 ~ % ssh gte
Last login: Wed Sep 28 06:10:06 2022 from pool-2-3-4-5.phlapa.fios.verizon.net

       __|  __|_  )
       _|  (     /   Amazon Linux 2 AMI
      ___|\___|___|

https://aws.amazon.com/amazon-linux-2/
[student@ip-1-2-3-4 ~]$ 

```

## Windows PuTTY

PuTTY is SSH client/terminal software frequently used on Windows operating systems, but also available on Linux systems. Configuration of connections and tunnels is configured using a GUI. See the screen captures below.

Download [putty](https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html)

Start a session, to the IP address at AWS

Click on SSH, tunnels

Add a port forward for:

- L8443 → localhost:8443
- L8432 → localhost:5432
- L8389 → localhost:389

## Windows SecureCRT

This is a windows program that might be on your computer from your work. It is not free. So if you dont have SecureCRT already, putty is preferred (above)

Open SecureCRT

File → Connect in Tab/Tile

Click the plus sign to make a new session (if you havent connected before)

Protocol SSH2

Enter the IP address and username from the google doc of passwords next to your name (note, this is not your IP address

Enter a name for the connection so you can find it later

Right click and go to properties on that connection

Add a port forward for:

- L8443 → localhost:8443
- L8432 → localhost:5432
- L8389 → localhost:389

Connect, save password (from google doc of passwords)

# Troubleshooting

If you are having issues with the ssh session timing out after a short period of inactivity, try adding this to your ssh command line:

> `-o TCPKeepAlive=yes -o ServerAliveCountMax=20 -o ServerAliveInterval=15`
