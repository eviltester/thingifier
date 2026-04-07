---
date:  2025-01-01T15:26:00Z
lastmod: 2026-02-18
title: API Challenges Solution For - POST /todos (201) all
seo_title: Solution: POST /todos (201) all | API Challenges
description: How to solve API challenges to Create maximum number of todos
seo_description: Use this walkthrough to solve POST /todos (201) all with request setup, key headers, and expected status codes so you can complete the challenge.
schema_howto_steps: Create todos repeatedly with POST /todos until the maximum limit is reached||Use valid JSON payloads and include X-CHALLENGER for every create request||Track successful creations so you can confirm the max-count boundary||Verify the final allowed create response and behavior at the limit||Check challenge status and confirm maximum todo creation is complete
showads: true
---

# How to complete the Create Maximum Number of Todos

This challenge requires you to max out the number of todos in the system.

The API Documentation says that `A maximum of 20 todos is allowed.`

There are many ways to do this and some automated execution is probably required but since there are only 20 allowed, you could do this easily in a REST Client.


## Automated With Java RestAssured Example

I have an automated Java execution using Rest Assured to complete this:

- [C058DeleteAllTodosTest.java](https://github.com/eviltester/thingifier/blob/master/challengerAuto/src/test/java/uk/co/compendiumdev/challenger/restassured/_19_misc_challenges/C059AddMaximumNumberOfTodosTest.java)

This uses a bunch of abstractions to keep the code simple but the basic process is:

- `GET` the `/todos` and find out how many there are already
- Issue as many `POST` requests to create a todo as necessary to max it out

```

TodosApi todos = new TodosApi();
List<Todo> currentTodos = todos.getTodos();

int todosToCreate = 20 - currentTodos.size();

while( todosToCreate > 0 ){
    Todo aTodo = todos.createTodo("my title " + 
       todosToCreate, "description", true
       );
    idsToDelete.add(aTodo.id);
    todosToCreate--;
};

// create a to do to throw it over the edge
Todo createMe = new Todo();
createMe.title = "my title";
createMe.description = "my description";
```

## Completing Challenge Using Client Tools

It is possible to complete this challenge manually by issuing all the requests by hand. This is easy to do because a TODO can be created with a single `POST`

`POST` to `/todos` with a simple payload `{"title":"not unique"}`

Resending this request would eventually result in:

```json
{
  "errorMessages": [
    "ERROR: Cannot add instance, maximum limit of 20 reached"
  ]
}
```

## Completing Challenge Using Client Tool Features

Some tools have the ability to issue Data Driven requests, so if you can parse the GET response and create a list of ids then you could use the data driven feature of the tool.

- [Bruno Data Driven Testing](https://docs.usebruno.com/testing/tests/data-driven-testing)
- [Postman Data Driven Community Posts](https://community.postman.com/tag/data-driven)

Most of the API client tools also have the ability to create scripts to achieve this.

