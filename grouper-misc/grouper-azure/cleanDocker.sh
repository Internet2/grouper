# need to run this once in while to keep things tidy
# otherwise the VM disk will fill up and Docker won't be happy 
docker rm -v $(docker ps -a -q -f status=exited)
docker rmi $(docker images -f "dangling=true" -q)
