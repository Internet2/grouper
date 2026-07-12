---
title: "GTE on demand design ideas"
space: GrIntDev
pageId: 48793132
version: 3
lastUpdated: 2026-07-12T06:46:08.301Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793132/GTE+on+demand+design+ideas
---

1. After someone pays for courses, someone manually goes to a screen and associates a person with having paid for certain courses.
  
  1. This system knows which courses earn a certain number of VM hours
2. If we can, Stova or whichever system and feed in which users have paid for which courses on which date
3. A user can use the GSH template and if they have hours to their name, they can
  
  1. See the status of their VM and connection details if there is a running VM to their name
  2. Turn on a new VM for a certain number of hours (max might be 96 hours)
    
    1. They get back the IP, user, pass, connection string
    2. The VM ID, IP, user, pass are registered in the database
  3. Extend their running vm for more hours
  4. Destroy a VM (before it would automatically get destroyed)
4. The user GSH template will communicate to AWS when a VM is started or stopped
5. GSH daemon will check every minute to see if there is a VM which was started for a certain amount of time that has passed.
6. The GSH daemon will destroy those VMs and register that in the database
