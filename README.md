# README
Thanks again for your patience

In order to run the project you will need to build the following four commands
These build the mysql, flyway and spring containers and only need to be run once

    docker build -t allane/mysql-image:latest -f Dockerfile-mysql .
    docker build -t allane/flyway-image:latest -f Dockerfile-flyway .
    docker build -t allane/allane-app:latest -f Dockerfile-spring .
    docker network create kestlernet

The following starts the mysql container

    docker run -d --name mysql-container --network kestlernet  -p 3306:3306  allane/mysql-image:latest

Wait for MySQL: Can connect with docker exec -it mysql-container mysql -uroot -p

    docker run --network kestlernet allane/flyway-image:latest migrate
    docker run -d -p 8080:8080 --name allane-app --network kestlernet allane/allane-app:latest

After that it should be findable at localhost:8080. There is not default data but I wil

Most of the code is fairly well commented.
Most of the POJOs can be found in src/java/de.sixt.allane.kestler/model
The rest API can be found in src/java/de.sixt.allane.kestler/control/DataController
The Hibernate Service can be found in src/java/de.sixt.allane.kestler/control/RentalService

I went a bit overboard with the frontend but that can be found in resources/allane.js and index.html
There is also schemas in resources/db/migration

It should be noted that there was little in the way of race condition prevention or data validation as there were no requirements specified for it. 

Let me know if there are any issues running the code
