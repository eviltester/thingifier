---
date:  2025-01-01T15:26:00Z
title: API Challenges Solution For - Delete /todos/id (200) all
description: How to solve API challenges to Delete all the todos
showads: true
---

# How to complete the Delete All Todos Challenge

This challenge requires you to delete the last todo item. This actually means you need to delete all the todo items.

There are many ways to do this and some automated execution is probably required.


## Automated With Java RestAssured Example

I have an automated Java execution using Rest Assured to complete this:

- [C058DeleteAllTodosTest.java](https://github.com/eviltester/thingifier/blob/master/challengerAuto/src/test/java/uk/co/compendiumdev/challenger/restassured/_19_misc_challenges/C058DeleteAllTodosTest.java)

This uses a bunch of abstractions to keep the code simple but the basic process is:

- `GET` the `/todos` and create a list of all the ids
- Issue a `DELETE` request for each of the `/todos/id`

```
        TodosApi api = new TodosApi();

        List<Todo> todos = api.getTodos();

        for(Todo todo : todos) {
            api.deleteTodo(todo.id);
        }
```

## Completing Challenge Client Tools

It is possible to complete this challenge manually by issuing all the requests by hand.

Some tools have the ability to issue Data Driven requests, so if you can parse the GET response and create a list of ids then you could use the data driven feature of the tool.

- [Bruno Data Driven Testing](https://docs.usebruno.com/testing/tests/data-driven-testing)
- [Postman Data Driven Community Posts](https://community.postman.com/tag/data-driven)

Most of the API client tools also have the ability to create scripts to achieve this.

## Deleting All `todo`s using Command Line Tools

I'm going use standard Linux/Unix/Bash commands here.

I can use a combination of `curl`, `jq`, and `xargs` to achieve what I want.

- [cUrl](https://curl.se/) command line HTTP client
- [jq](https://jqlang.github.io/jq/) a command line JSON processor
- [xargs](https://pubs.opengroup.org/onlinepubs/9799919799/utilities/xargs.html) piping parameters into commands [examples](https://en.wikipedia.org/wiki/Xargs)

I can use all these linux commands on  Windows using a WSL ((Windows Subsystem for Linux)[https://learn.microsoft.com/en-us/windows/wsl/about])  which means I can use the same commands as a mac and make this solution portable.

I'm running Ubuntu in my WSL

First issue a curl request to get the todos in API challenges.

If I don't know how to create the `culr` command I can generate the `curl` command easily by using Bruno to generate the code (most REST Clients  also have this feature).

- create request in Bruno with the X-CHALLENGER header
- try it out
- right click on the item in the left side bar and `Generate Code` and then choose `Shell-Curl`

```
curl --request GET \
  --url https://apichallenges.eviltester.com/todos \
  --header 'X-CHALLENGER: 07466215-9bab-4bf4-9b7d-34b7ac765915'
```

Replace the `X-CHALLENGER` GUID with your GUID

I will actually save the response into a file to make it easier to work with.

```
curl --request GET \
  --url https://apichallenges.eviltester.com/todos \
  --header 'X-CHALLENGER: 07466215-9bab-4bf4-9b7d-34b7ac765915'
  > todos.json
```

The response is JSON, I want to parse that JSON to find all the ids, because I'm going to use that list of ids to delete them all.

I will use JQ for that.

The command below iterates over all the items in the todo array and extracts the id

```
jq '.todos[].id' todos.json
```

I can save this list to a file

```
jq '.todos[].id' todos.json > ids.txt
```

Then if I `cat ids.txt` I can see all the ids

```
> cat ids.txt
1
8
5
2
6
3
9
7
4
10
```

I then want to call curl for each of these values and I can use `xargs` to do that

```
cat ids.txt | xargs -I % curl --request DELETE \
  --url https://apichallenges.eviltester.com/todos/% \
  --header 'X-CHALLENGER: 07466215-9bab-4bf4-9b7d-34b7ac765915'
```

I can double check that I have deleted them by looking at the API Challenges progress page.

Or just call the `GET /todos` and see an empty array

```
> curl --request GET \
  --url https://apichallenges.eviltester.com/todos \
  --header 'X-CHALLENGER: 07466215-9bab-4bf4-9b7d-34b7ac765915'

{"todos":[]}
```


Or I could do it all in one command:

```
curl --request GET \
  --url https://apichallenges.eviltester.com/todos \
  --header 'X-CHALLENGER: 880c5857-dbff-4266-b419-701efa804679' |
jq '.todos[].id' |
xargs -I % curl --request DELETE \
  --url https://apichallenges.eviltester.com/todos/% \
  --header 'X-CHALLENGER: 880c5857-dbff-4266-b419-701efa804679'
```

## Delete All Todos Using Command Line Tools Video

{{<youtube-embed key="7Kz97rn7f3I" title="Solution to Delete all Todos in default format">}}

[Patreon ad free version](https://www.patreon.com/posts/119362209)