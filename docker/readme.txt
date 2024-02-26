# Docker

This testing app can be run using Docker.

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
