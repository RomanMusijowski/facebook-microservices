# Facebook
Backend REST API for Facebook app created with [Spring Boot](http://projects.spring.io/spring-boot/).  
Service [Architecture](ARCHITECTURE.MD)  
You can find addional information in the [Wiki](WIKI.md)  
For running that project you you need:

``` 
- java 13
- maven 3.6 or higher
```

### Docker-compose
You can run program with Docker-compose. During that program will create containers for next services:
```
- eureka-service
- configuration-service
- auth-service
- mail-service
- zuul-service
- auth-db
- rabbitmq
```


If you wanna run this program in docker, run next command in root project directory:
```
- mvn clean package
```
After that go to docker directory and run next command:
```
-  sudo docker-compose up --build
```
After running that command docker will retrieve all necessary containers.
    


## Eureka-service

Eureka is Client-side service discovery allows services to find and 
communicate with each other without hard-coding hostname and port.
With Netflix Eureka each client can simultaneously act as a server,
to replicate its status to a connected peer.
To be informed about the presence of a client, they have to
send a heartbeat signal to the registry.

- http://localhost:8761/ - Eureka dashboard

## Configuration-service  

Each service pulls its configuration from the central repository on startup.

## Auth-service  
[Swagger](http://localhost:5000/swagger-ui.html)  

Anyone can register and log in.  
Only logged in users can invite to friends.  
Only friends can break their relationship.

- http://localhost:5000/api/auth - authentication service (register, log in, activate)
- http://localhost:5000/api/user - user service (friends management)

### PostgresDB    

Auth service uses PostgresDB for persistence.
Before running the application, ensure that you have a Postgres instance running on localhost port 6551.
         
Database creation:

    docker pull postgres
    docker run -d --name postgres-user -p 6551:5432 postgres
            
    docker exec -ti <4 first letters from ID container> psql -U postgres
    crete database facebook;
              
For connection to db:
                   
    url: "jdbc:postgresql://localhost:6551/facebook"
    username: "postgres"
    
## Post-service  
[Swagger](http://localhost:5001/swagger-ui.html)  

Only logged in users can create posts and comments.  
There is possibility to add post with photo.    
Only post/comment owner can edit/delete it.  

- http://localhost:5001/api/post - post service (post management)

### PostgresDB    

Post service uses PostgresDB for persistence.
Before running the application, ensure that you have a Postgres instance running on localhost port 6552.
       
Database creation:

    docker pull postgres
    docker run -d --name postgres-post -p 6552:5432 postgres
            
    docker exec -ti <4 first letters from ID container> psql -U postgres
    crete database facebook;
              
For connection to db:
                   
    url: "jdbc:postgresql://localhost:6552/facebook"
    username: "postgres"

### Redis
We use Redis for store data in cache.

    docker pull redis
    docker run -d --name redis1 -p 6379:6379 redis
    
## Mail-service   
Mail service takes messages from queue and sends it to provided email.

### RabbitMq    

Mail service uses RabbitMq for queueing messages.
Before running the application, ensure that you have a RabbitMq instance running on localhost.
       
RabbitMq creation:

    docker run -d --hostname facebook --name mail-queue -p 15672:15672 -p 5672:5672 rabbitmq:3.6-management-alpine
              
After this we need to go to this site:  
 - http://localhost:15672  
 
Username and password: guest  
Next enter queues tab and create new queue with name: mail-queue

## Event-service
* [Swagger](http://localhost:5003/swagger-ui.html)
    
Events are visible to anyone on or off Facebook. 
There is possibility to add event with photo.
Only active users can join or invite to events.
Only people who created event can delete it.
    
### PostgresDB
Event service uses PostgresDB for persistence.
Before running the application, ensure that you have a Postgres instance running on localhost port 6553.
        
Database creation:
            
    docker pull postgres
    docker run -d --name postgres-evet -p 6553:5432 postgres
            
    docker exec -ti <4 first letters from ID container> psql -U postgres
    crete database facebook;

For connection to db:
                   
    url: "jdbc:postgresql://localhost:6553/facebook"
    username: "postgres"

For creating you have to login.
- http://localhost:5003/api/event - create event;

## Chat-service
Any person can send messages to any channel.
The number of channels is not limited.
To start chat enter username and channel name.

### MongoDB

    docker pull mongo
    docker run --name mongo-chat -d -p 27017:27017 mongo --noauth --bind_ip=0.0.0.0
    
Navigate to chat: 
- http://localhost:5005/


    
## News-service
* [Swagger](http://localhost:5004/swagger-ui.html)
    
Only logged in users can view news.  
Only admin can add news.      
Only admin can edit/delete news it.   
News from the last 14 days are returned by default.  
Users can specify the number of days they want to receive messages.  
 
 - http://localhost:5004/api/news - news service
    
News service uses DynamoDB for persistence.  
Before running app you must create table "News" in amazon DynamoDB.  
There is also need to have AWS credentials configured on your PC.
        
## Zuul-service
Zuul is an edge service that proxies requests to multiple backing services.     

        
    
