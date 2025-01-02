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

