# JavaBlockchainForEXP

```
git clone https://github.com/Houston613/JavaBlockchainForEXP.git
```

### Docker build

```
docker build -t image-for-blockchain:1.0 .
```

### Docker compose run

```
docker-compose up
```

After this command you will see that 3 nodes are running and start to send ports to each other
![some text](src/main/resources/Screenshot_1.png?raw=true)

if you use in current time ports 8080, 8081, 8082, change ports in `docker-compose.yml` but you need to use 3 consecutive ports.
Mining strategies hardcoded to that ports, if you want to use other ports, only Fibonacci will be available
#### Block Mining
When node finds a new block, in console we can see information about this block
After that it will broadcasted to another blocks

![some text](/src/main/resources/Screenshot_2.png?raw=true)
#### Block broadcasting
When a node recieives block - we will see information about that in console - it replace last block with recievied and restart mining process

![some text](/src/main/resources/Screenshot_3.png?raw=true)
