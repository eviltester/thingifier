# How to Download And Run Challenges Single Player Mode

## Downloading

To download:

- Visit the releases page
     - https://github.com/eviltester/thingifier/releases
- Look for the `apichallenger.jar`
- Download `apichallenger.jar`

Alternative Route:

- Visit https://eviltester.com/apichallenges
- there will be a link to the latest release
- click on that
- Download `apichallenger.jar`

You can run this from anywhere, so copy the `.jar` file into a directory of your choosing.

## Running

We need to have java installed. And most computers should have this by default.

So we'll assume you have it, and if you don't then download and install it from either of...

- https://java.com
- https://adoptopenjdk.net

In a terminal window, or a command prompt then run the app with `java -jar apichallenger.jar`

We should see a bunch of information displayed in the command line, and one of the output lines is the URL that the application is running on.

### Running on Windows

- visit the folder you copied the `.jar` file in using Explorer
- in the path input field in explorer type `cmd`
- at the command line type `java -jar apichallenger.jar`

### Running on Mac

- open `terminal`
- use `cd` to navigate to the folder e.g. assuming you copied `apichallenger.jar` to a folder on your desktop
     - `cd Desktop`
     - `cd apichallenger`
- `java -jar apichallenger.jar`

### Double Click

If you double click the application, it will probably run but you won't see it running.

If you visit the GUI then you'll see if it is working or not.

### Visit The GUI

- Open a browser
- `http://localhost:4567`
- you should see the GUI

### Stopping the App

In single user mode you can stop the app by:

- `http://localhost:4567/shutdown`

Or, `cntrl+c` at the command line to stop the running app.

### Using the API

Make your api calls to `http://localhost:4567/{API}`

e.g.

- `http://localhost:4567/todos`
- `http://localhost:4567/challenges` 

### Tracking Challenges

Challenges are automatically tracked in single user mode.

And written to a file:

- ` rest-api-challenges-single-player.data.txt`

Stored in the same directory as the `apichallenger.jar`

This file will be automatically loaded when the application is started.

You don't need to `POST /challenger`.

### Example solving a challenge

- If I use Insomnia,
- to make a GET request to `http://localhost:4567`
- then visit the GUI `http://localhost:4567/gui/challenges`
- then we should see the challenge that I just completed
- and if I close the app and start it again
- then my challenge status has been tracked in the file 

 ---
 
## Video

> API Challenges - How to Run the Application locally

[youtu.be/FIYKZiTDoAI](https://youtu.be/FIYKZiTDoAI)

The API Challenges application has a cloud version, but because it is multi-user it can be useful to run locally so you don't compete with other people for data.

This video shows how to:

- download the jar file
- run the jar file
- stop the app

With instructions for both Mac and Windows.

Find the application links and more information at:

- https://eviltester.com/apichallenges
 
  
 
 Patreon ad free video: https://www.patreon.com/posts/39849331