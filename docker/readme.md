# Docker

The API Challenges testing app can be run using Docker.

To build, run from the root directory

```
mvn package
```

Each project will be built standalone.


## Dockerised API Challenges

The API Challenges (challenger) project has been packaged as a docker image so can be run via docker.

After packaging the app with `mvn package` above.

From the root directory containing the `/docker` folder run:

```
docker build -t apichallenges -f ./docker/apichallenges/Dockerfile .
```

Then:

```
docker run -p 4567:4567 apichallenges
```

This will make the api challenges app accessible on `localhost:4567`

To save your progress you need to mount a directory e.g. on

Official Docker docks about mounting volumes https://docs.docker.com/storage/bind-mounts/

- create a folder called `sessions` and save the user session files from the docker application in this folder.

```
docker run -p 4567:4567 apichallenges -v ".\sessions:/opt/app/challengersessions"
```

on linux/mac

```
docker run -p 4567:4567 apichallenges -v  "$(pwd)"/sessions:/opt/app/challengersessions"
```

Make a note of the container name and if you don't delete the container then you can restart it with:

```
docker container start name_of_container_here
```

Alternatively you could just restart the container from the docker desktop.


## Other APIs

I've created Dockerfile for additional test APIs. I'll add more as I find them.

### FX Trader API

Stuart du Casse has created an API to support practicing testing.

- https://www.linkedin.com/in/stuart-du-casse-90139775/

The GitHub repo for the application is:

https://github.com/sd576/FX-TradeHub-API

The application is written for node. You can use the command below to create a docker image:

From the root directory containing the `/docker` folder run:


```
docker build -t fxtradehub -f ./docker/fxtradehub/Dockerfile .
```

Then you can run the image:

```
docker run -it -p 3000:3000 fxtradehub
```

And visit http://localhost:3000/api-docs to see the Swagger Docs UI.


### Device Registry API

Andy Knight has created an API to support practicing testing and support his book "The Way To Test Software".

- https://github.com/AutomationPanda
- https://automationpanda.com/

The GitHub repo for the application is:

https://github.com/AutomationPanda/device-registry-fastapi

The application is written for python. You can use the command below to create a docker image:

From the root directory containing the `/docker` folder run:

```
docker build -t devregfastapi -f ./docker/device-registry/Dockerfile .
```

Then you can run the image:

```
docker run -it -p 8000:8000 devregfastapi
```

The authentication information is in https://github.com/AutomationPanda/device-registry-fastapi/blob/main/config.json

e.g. `user` : `password`

- `engineer` : `Muh5devices`
- `pythonista` : `I<3testing`,


And visit http://localhost:8000/docs to see the Swagger Docs UI.

And visit http://localhost:8000/redoc to see the Redoc Documentation UI.

## RestListicator

The RestListicator is the first Testing API I created, with some deliberate bugs.

It is part of the TestingApp project which as multiple testing practice apps.

- https://github.com/eviltester/TestingApp
- specifically https://github.com/eviltester/TestingApp/tree/master/java/testingapps/restlisticator

There is a Swagger file in the docs folder.

It has a automated coverage using Java and RestAssured:

- https://github.com/eviltester/automating-rest-api/tree/master/rest-listicator-automating-examples

Can be run using Docker as either 'normal' or 'buggy'.

Normal:

```
docker build -t restlisticator -f ./docker/restlisticator/Dockerfile .

docker run -it -p 4567:4567 restlisticator  
```

Buggy:

```
docker build -t restlisticatorbuggy -f ./docker/restlisticator/Dockerfile-with-bugs .

docker run -it -p 4567:4567 restlisticatorbuggy 
```

The images have been pushed to Docker hub:

```
docker run -it -p 4567:4567 eviltester/restlisticator:latest
docker run -it -p 4567:4567 eviltester/restlisticator:latest-buggy
```

Then visit:

```
http://localhost:4567/listicator/
```

There are 3 default Users:

Basic authentication to use these users.

```
username: superadmin, password: password
username: admin, password: password
username: user, password: password
```

There are two entities:

- lists
- users

---

## Publishing To Docker

The instructions for deployment to docker hub are quite easy to follow.

https://docs.docker.com/guides/workshop/04_sharing_app/

- create a repository on docker hub itself e.g. `myapp`

- tag an existing image to match the repository

- `docker tag myapp eviltester/myapp`

- then push this to docker hub

- `docker push eviltester/myapp`

I needed to log into the Docker Desktop app to give docker permissions to push to the repository.

I also tagged it with a version `eviltester/myapp:2.3.0` to match the version of the app that it is running.