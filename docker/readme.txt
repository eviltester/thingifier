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

