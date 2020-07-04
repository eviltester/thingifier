# Thingifier

An experiment in Model Based Development.

Based on my [Compendium-TA](https://www.compendiumdev.co.uk/page.php?title=compendiumta) tool from 2003/2004.

If you actually want an ER Based tool then check out:

- [Jeddict](https://jeddict.github.io/)
- [Evolutility](http://www.evolutility.org/index.aspx)

If you want to generate test data for an api, investigate:

- [next.json-generator.com](https://next.json-generator.com/)
- [Java-Faker](https://github.com/DiUS/java-faker)

---

- Alan Richardson
- https://www.eviltester.com
- https://www.compendiumdev.co.uk
- https://uk.linkedin.com/in/eviltester
- https://twitter.com/eviltester

---

This is a work in progress so it currently has a hard coded model of a small TODO application.

## Model

Entities:

- todo
    - Fields:
        - doneStatus
        - guid
        - description
        - title
- project
    - Fields:
        - guid
        - description
        - active
        - completed
        - title
- category
    - Fields:
        - guid
        - description
        - title

Relationships:

- projects : category => project
- categories : todo => category
- todos : category => todo
- tasks : project => todo

### Resulting REST API

This generates the following REST API automatically

- /todos
    - GET - all the todos
    - POST - create a todo - body JSON
    - OPTIONS
- /todos/_GUID_
    - GET - a specific todo
    - DELETE - a specific todo
    - POST - amend the todo
    - PUT - replace all the todo fields in the JSON body
    - OPTIONS
- /todos/_GUID_/categories
    - GET - all the categories for a todo
    - POST - create a todo - body JSON must contain a GUID for a category e.g. `{"guid":"1234-1234-1234-1234"}`
    
Similar for `/projects` and `/categories`

The JSON body for creating and amending is simply

`{"fieldname":"fieldvalue", "anotherFieldName":"anotherValue", etc.}`

The XML body for creating and amending is simply

`<entity><fieldname>fieldvalue></fieldname><anotherFieldName>anotherValue</anotherFieldName><entity>`

e.g.

`<todo><title>my title</title></todo>`

The fieldnames are shown in the list above for the model or the code below.

The REST API has some guards to check for 404s but very little error handling and no enforcement of mandatory fields etc.

But it is a workable API and all details are stored in memory.

The deployed app does have some sample data for testing purposes, this can easily be `DELETE`d using the API.
    
### Code for Model

~~~~~~~~
       Thing todo = todoManager.createThing("todo", "todos");

        todo.definition()
                .addFields(Field.is("title", STRING).
                                mandatory().
                                withValidation(
                                        VRule.notEmpty(),
                                        VRule.matchesType()),
                        Field.is("description", STRING),
                        Field.is("doneStatus", FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(
                                        VRule.matchesType()));
        
        Thing project = todoManager.createThing("project", "projects");

        project.definition()
                .addFields(
                        Field.is("title", STRING),
                        Field.is("description", STRING),
                        Field.is("completed", FieldType.BOOLEAN).
                                withDefaultValue("FALSE").
                                withValidation(VRule.matchesType()),
                        Field.is("active", FieldType.BOOLEAN).
                                withDefaultValue("TRUE").
                                withValidation(VRule.matchesType()));


        Thing category = todoManager.createThing("category", "categories");

        category.definition()
                .addFields(
                        Field.is("title", STRING).
                                mandatory().
                                withValidation(VRule.notEmpty()),
                        Field.is("description", STRING));

        todoManager.defineRelationship(Between.things(project, todo), AndCall.it("tasks"), WithCardinality.of("1", "*")).
                whenReversed(WithCardinality.of("1", "*"), AndCall.it("task-of"));

        todoManager.defineRelationship(Between.things(project, category), AndCall.it("categories"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, todo), AndCall.it("todos"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(category, project), AndCall.it("projects"), WithCardinality.of("1", "*"));
        todoManager.defineRelationship(Between.things(todo, category), AndCall.it("categories"), WithCardinality.of("1", "*"));
~~~~~~~~
    
Default model can be found in [src/main/java/uk/co/compendiumdev/thingifier/application/TodoManagerThingifier.java](https://github.com/eviltester/thingifier/blob/master/src/main/java/uk/co/compendiumdev/thingifier/application/TodoManagerThingifier.java)