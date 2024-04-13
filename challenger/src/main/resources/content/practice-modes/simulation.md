---
title: API Challenges Simulation Mode
description: A simulated API tutorial - follow the guided instructions and learn how to use your API Tool without any side-effects or risk.
---

# Simulation Mode

The API has a simulation mode, it uses hard coded data in responses, but tries to mimic some conditions.

{{<youtube-embed key="jlbLr2Ddo6s" title="How to use simulation mode">}}


[Patreon ad free video](https://www.patreon.com/posts/54383023)

e.g. it expects you to post a specific JSON payload or XML payload and responds 'as if' you sent it. But... it also checks if you sent valid json, or valid xml, and responds based on your headers e.g. returning XML if you ask for it.

The simulator is stateless and does not track your usage, making it deterministic for multiple users. Which means:

*   Entities created do not show in the 'entities' call, but can be retrieved by a 'GET'
*   Entities deleted do not show in the 'entities' and respond to a 404, but the delete for them will return a 200... you can only delete 'specific' entities, other entities will respond with a forbidden request.
*   etc. there are 'inconsistencies' but they are logical based on the needs of a stateless simulator. Use the actual API that underpins the challenges if you want a 'real' API.

## How to Use

Work through the requests in sequence to achieve a fairly logical interaction.

- try different tooling, the only difference then will be the tool because the API is fairly forgiving and no-one else can interfere with your practice. Use it to learn the tools.
- try different automated execution approaches. The API is simple, there are only a few requests and sequences, so use it to learn a new automated execution tool. It won't change as you are automating, if something goes wrong then it is most likely some nuance of the tool.

This simulator is designed to make starting with API testing as simple as possible.

## Suggested Request Sequence

Try the verbs and payloads listed below as a way of making sure your tooling is setup and you understand the absolute basics about API usage and Testing.

GET <span class="currenthost">CURRENTHOST</span>/sim/entities (200)

*   Entities 1-10
*   Get all the entities in the simulator

GET <span class="currenthost">CURRENTHOST</span>/sim/entities/1 (200)

*   Return entity number 1... try any of the entities listed
*   Entities 1-8 are suitable for getting, 9 and 10 are for deletes and amendments so you may not get the response you are expecting

GET <span class="currenthost">CURRENTHOST</span>/sim/entities/404 (404)

*   Entity does not exist, receive a 404 response

POST <span class="currenthost">CURRENTHOST</span>/sim/entities (201)

*   Create an entity...note we assume you are creating with the payload below, because that is what we return. Creates an entity that is not listed in the /entities list, but it will be returned by GET to keep consistent with the location header.
*   Will create Entity with ID 11
*   Entity 11 - Get will work for this but it will not show in the entities list. It will appear in the location header

    {"name": "bob"}

POST <span class="currenthost">CURRENTHOST</span>/sim/entities/10 (200)

*   Amend an entity...note we assume you are amending to the payload below, because that is what we return. Creates an entity that is not listed in the /entities list, but it will be returned by GET to keep consistent with the location header.
*   Will amend Entity with ID 10, once you amend you can GET this item and check it has amended
*   Entity 10 - Get will show the amendment, but the list view will show the original name "entity number 10"

    {"name": "eris"}

PUT <span class="currenthost">CURRENTHOST</span>/sim/entities/id (200)

*   Amend an entity...note we assume you are amending to the payload below, because that is what we return. Creates an entity that is not listed in the /entities list, but it will be returned by GET to keep consistent with the location header.
*   Can amend Entity with ID 10, once you amend you can GET this item and check it has amended
*   Entity 10 - Get will show the amendment, but the list view will show the original name "entity number 10"

    {"name": "eris"}

*   Can also create entity with id 11

    {"name": "bob"}

DELETE <span class="currenthost">CURRENTHOST</span>/sim/entities/id (204)

*   the only entity you can delete is id 9
*   if you GET id 9 then you will find it 404's

## Other things to try:

*   Try malformed XML and JSON for POST and PUT
*   Try amending the Content-Type to XML and passing in `<entity><name>bob</name></entity>`
*   Try accept `application/xml` and `application/json`
*   Delete an entity listed in the /entities call is forbidden id < 9
*   POST/PUT an entity listed in the /entities call is forbidden id < 10
*   PATCH and TRACE should be 501 Unimplemented for all end endpoints
*   other /sim/* endpoints should 404


    <script>
        let spans =document.querySelectorAll(".currenthost");
        spans.forEach(element =>{
            element.innerHTML = window.location.origin;
            }
        );
    </script>

## Swagger OpenAPI File

You can download a simple Swagger [OpenAPI File for simulation mode](/practice-modes/simulation/swagger).

## Simulation Mode Walkthrough - Insomnia

{{<youtube-embed key="CG3G5lpxE0Y" title="How to use Insomnia with simulation mode as example api">}}

[Patreon ad free video](https://www.patreon.com/posts/54383155)

## Simulation Mode Walkthrough - Postman

{{<youtube-embed key="CF3gVz9zc2s" title="How to use Postman with simulation mode as example api">}}

[Patreon ad free video](https://www.patreon.com/posts/54383110)

