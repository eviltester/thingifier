# API Self Teaching Challenges

From the commandline, run the app:
 
- `java -jar apichallenges.jar`

This will start an API and GUI running on:

- http://localhost:4567

If you visit the URL in a browser then you will see the GUI where you can:
 
- read the API reference documentation
- browse the items in the application
- see the API Challenges

You can run it in multi-user mode locally by using the command line parameter:

`-multiplayer`

e.g.

- `java -jar apichallenges.jar -multiplayer`

This could be useful for team workshops and learning sessions.

## More information

More information can be found at [eviltester.com/apichallenges](https://eviltester.com/apichallenges)

## Challenges

The challenges can be completed by issuing API requests to the API.

e.g. `GET http://localhost:4567/todos` would complete the challenge to "GET the list of todos"

You can also `GET http://localhost:4567/challenges` to get the list of challenges and their status as an API call.

## Version Tracking

v1.0 - 20200727

- ready for initial public release
- fix bug to allow tracking of x-challenger session as a challenge

v0.1

- initial development of challenges
- s3 storage of multi-user cloud challenge sessions
- file persistence for local release

## How Tos

- [About the application](https://github.com/eviltester/thingifier/blob/master/challenger/info/00_welcome.md)
- [Creating a Multi-User Session](https://github.com/eviltester/thingifier/blob/master/challenger/info/10_how_to_register_cloud_challenger.md)

