package uk.co.compendiumdev.thingifier.swaggerizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OpenApi32FinalizerTest {

    @Test
    void promotesQueryExtensionToPathItemQueryOperation() {
        final String source =
                """
                {
                  "openapi": "3.1.0",
                  "paths": {
                    "/todos": {
                      "get": {
                        "responses": {
                          "200": {
                            "description": "OK"
                          }
                        }
                      },
                      "x-query-operation": {
                        "summary": "query todos",
                        "x-http-method": "QUERY",
                        "x-query-content-types": [
                          "application/x-www-form-urlencoded",
                          "application/jsonpath",
                          "application/vnd.apichallenges.todo-query+json"
                        ],
                        "responses": {
                          "200": {
                            "description": "OK"
                          }
                        }
                      }
                    }
                  }
                }
                """;

        final JsonObject document =
                JsonParser.parseString(new OpenApi32Finalizer().finalizeJson(source))
                        .getAsJsonObject();
        final JsonObject todos = document.getAsJsonObject("paths").getAsJsonObject("/todos");
        final JsonObject query = todos.getAsJsonObject("query");
        final JsonObject formMediaType =
                query.getAsJsonObject("requestBody")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/x-www-form-urlencoded");
        final JsonObject jsonPathMediaType =
                query.getAsJsonObject("requestBody")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/jsonpath");
        final JsonObject structuredMediaType =
                query.getAsJsonObject("requestBody")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/vnd.apichallenges.todo-query+json");

        Assertions.assertEquals("3.2.0", document.get("openapi").getAsString());
        Assertions.assertTrue(todos.has("get"));
        Assertions.assertTrue(todos.has("query"));
        Assertions.assertFalse(todos.has("x-query-operation"));
        Assertions.assertEquals("query todos", query.get("summary").getAsString());
        Assertions.assertFalse(query.has("x-http-method"));
        Assertions.assertFalse(query.has("x-query-content-types"));
        Assertions.assertFalse(query.getAsJsonObject("requestBody").get("required").getAsBoolean());
        Assertions.assertEquals(
                "object", formMediaType.getAsJsonObject("schema").get("type").getAsString());
        Assertions.assertEquals(
                "string",
                formMediaType
                        .getAsJsonObject("schema")
                        .getAsJsonObject("additionalProperties")
                        .get("type")
                        .getAsString());
        Assertions.assertEquals(
                "string", jsonPathMediaType.getAsJsonObject("schema").get("type").getAsString());
        Assertions.assertEquals(
                "object", structuredMediaType.getAsJsonObject("schema").get("type").getAsString());
        Assertions.assertEquals(
                "integer",
                structuredMediaType
                        .getAsJsonObject("schema")
                        .getAsJsonObject("properties")
                        .getAsJsonObject("limit")
                        .get("type")
                        .getAsString());
    }

    @Test
    void preservesExistingFormRequestBodySchema() {
        final String source =
                """
                {
                  "openapi": "3.1.0",
                  "paths": {
                    "/todos": {
                      "x-query-operation": {
                        "requestBody": {
                          "required": true,
                          "content": {
                            "application/x-www-form-urlencoded": {
                              "schema": {
                                "type": "object",
                                "properties": {
                                  "title": {
                                    "type": "string"
                                  }
                                }
                              }
                            }
                          }
                        },
                        "responses": {
                          "200": {
                            "description": "OK"
                          }
                        }
                      }
                    }
                  }
                }
                """;

        final JsonObject query =
                JsonParser.parseString(new OpenApi32Finalizer().finalizeJson(source))
                        .getAsJsonObject()
                        .getAsJsonObject("paths")
                        .getAsJsonObject("/todos")
                        .getAsJsonObject("query");

        Assertions.assertTrue(query.getAsJsonObject("requestBody").get("required").getAsBoolean());
        Assertions.assertTrue(
                query.getAsJsonObject("requestBody")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/x-www-form-urlencoded")
                        .getAsJsonObject("schema")
                        .getAsJsonObject("properties")
                        .has("title"));
        Assertions.assertEquals(
                "string",
                query.getAsJsonObject("requestBody")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/jsonpath")
                        .getAsJsonObject("schema")
                        .get("type")
                        .getAsString());
        Assertions.assertEquals(
                "object",
                query.getAsJsonObject("requestBody")
                        .getAsJsonObject("content")
                        .getAsJsonObject("application/vnd.apichallenges.todo-query+json")
                        .getAsJsonObject("schema")
                        .get("type")
                        .getAsString());
    }
}
