package uk.co.compendiumdev.thingifier.api.spec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.ApiRoutingDefinitionDocGenerator;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingDefinition;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.response.ApiResponseAsJson;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.Cardinality;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

class ThingifierApiDefaultEntityViewsTest {

    @Test
    void defaultEntityViewConfiguresRequestAndResponseDefaults() {
        final Thingifier thingifier = shop();

        final ThingifierApiEntityRule rule =
                thingifier.apiSpec().entity("cart").defaultEntityView("PublicCart");

        Assertions.assertEquals("PublicCart", rule.defaultRequestView());
        Assertions.assertEquals("PublicCart", rule.defaultResponseView());
    }

    @Test
    void defaultResponseViewHidesFieldsForCollectionGet() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultResponseView("PublicCart");
        createCart(thingifier, "basket", "secret-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("carts"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("basket"));
        Assertions.assertFalse(response.getBody().contains("token"));
        Assertions.assertFalse(response.getBody().contains("secret-token"));
    }

    @Test
    void defaultResponseViewHidesFieldsForInstanceGet() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultResponseView("PublicCart");
        final EntityInstance cart = createCart(thingifier, "basket", "secret-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(jsonRequest("carts/" + cart.getPrimaryKeyValue()));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("basket"));
        Assertions.assertFalse(response.getBody().contains("token"));
        Assertions.assertFalse(response.getBody().contains("secret-token"));
    }

    @Test
    void defaultResponseViewUsesPluralEntityRuleNames() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("carts").defaultResponseView("PublicCart");
        createCart(thingifier, "basket", "secret-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier).get(jsonRequest("carts"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertFalse(response.getBody().contains("token"));
    }

    @Test
    void routeResponseViewOverridesEntityDefaultResponseView() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultResponseView("PublicCart");
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/carts/{cartId}")
                .responseEntityView(200, "InternalCart");
        final EntityInstance cart = createCart(thingifier, "basket", "secret-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(jsonRequest("carts/" + cart.getPrimaryKeyValue()));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("token"));
        Assertions.assertTrue(response.getBody().contains("secret-token"));
    }

    @Test
    void defaultResponseViewHidesFieldsForRelationshipCollectionGet() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultResponseView("PublicCart");
        final EntityInstance user = createUser(thingifier, "Alice");
        final EntityInstance cart = createCart(thingifier, "basket", "secret-token");
        storeFor(thingifier).relationships().connect(user, "carts", cart);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(jsonRequest("users/" + user.getPrimaryKeyValue() + "/carts"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains("basket"));
        Assertions.assertFalse(response.getBody().contains("token"));
        Assertions.assertFalse(response.getBody().contains("secret-token"));
    }

    @Test
    void defaultResponseViewHidesFieldsForDirectApiResponses() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultResponseView("PublicCart");
        createCart(thingifier, "basket", "secret-token");

        final ApiResponse response =
                thingifier.api().get("carts", new QueryFilterParams(), new HttpHeadersBlock());
        final String json =
                new ApiResponseAsJson(response, new JsonThing(thingifier.apiConfig().jsonOutput()))
                        .getJson();

        Assertions.assertFalse(json.contains("token"));
        Assertions.assertFalse(json.contains("secret-token"));
    }

    @Test
    void defaultRequestViewRejectsDisallowedInputFields() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultRequestView("PublicCart");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPost("carts", "{\"name\":\"basket\",\"token\":\"blocked\"}"));

        Assertions.assertEquals(422, response.getStatusCode());
        Assertions.assertEquals(0, cartCount(thingifier));
    }

    @Test
    void routeRequestViewOverridesEntityDefaultRequestView() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultRequestView("PublicCart");
        thingifier.apiSpec().route(RoutingVerb.POST, "/carts").requestEntityView("InternalCart");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPost("carts", "{\"name\":\"basket\",\"token\":\"allowed\"}"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, cartCount(thingifier));
    }

    @Test
    void jsonPathQueryUsesDefaultResponseViewBeforeFiltering() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultResponseView("PublicCart");
        createCart(thingifier, "basket", "secret-token");

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .queryRequest(
                                jsonPathQuery("carts", "$.carts[?(@.token == 'secret-token')]"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(0, response.apiResponse().getReturnedInstanceCollection().size());
    }

    @Test
    void defaultRequestViewUpdatesGeneratedRequestPayloadDocumentation() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultRequestView("PublicCart");

        final RoutingDefinition route =
                route(
                        new ApiRoutingDefinitionDocGenerator(thingifier).generate(""),
                        RoutingVerb.POST,
                        "carts");

        Assertions.assertTrue(route.hasRequestEntityView());
        Assertions.assertEquals("create_PublicCart", route.getRequestPayload());
    }

    @Test
    void defaultResponseViewUpdatesGeneratedResponsePayloadDocumentation() {
        final Thingifier thingifier = shop();
        thingifier.apiSpec().entity("cart").defaultResponseView("PublicCart");

        final RoutingDefinition route =
                route(
                        new ApiRoutingDefinitionDocGenerator(thingifier).generate(""),
                        RoutingVerb.GET,
                        "carts");

        Assertions.assertEquals("PublicCart", route.getResponseEntityViewFor(200));
        Assertions.assertEquals("PublicCart", route.getReturnPayloadFor(200));
    }

    private Thingifier shop() {
        final Thingifier thingifier = new Thingifier();

        final EntityDefinition user = thingifier.defineThing("user", "users");
        user.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        user.addField(Field.is("name", FieldType.STRING));

        final EntityDefinition cart = thingifier.defineThing("cart", "carts");
        cart.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        cart.addField(Field.is("name", FieldType.STRING));
        cart.addField(Field.is("token", FieldType.STRING));
        cart.defineView("PublicCart")
                .hideRequestFields("token")
                .hideResponseFields("token")
                .disallowInputFields("token");
        cart.defineView("InternalCart");

        thingifier
                .defineRelationship(user, cart, "carts", Cardinality.ONE_TO_MANY())
                .whenReversed(Cardinality.ONE_TO_ONE(), "owner");

        return thingifier;
    }

    private EntityInstance createUser(final Thingifier thingifier, final String name) {
        final EntityDefinition user = thingifier.getDefinitionNamed("user");
        return storeFor(thingifier)
                .entities()
                .create(EntityInstanceDraft.forEntity(user).withField("name", name));
    }

    private EntityInstance createCart(
            final Thingifier thingifier, final String name, final String token) {
        final EntityDefinition cart = thingifier.getDefinitionNamed("cart");
        return storeFor(thingifier)
                .entities()
                .create(
                        EntityInstanceDraft.forEntity(cart)
                                .withField("name", name)
                                .withField("token", token));
    }

    private int cartCount(final Thingifier thingifier) {
        return storeFor(thingifier)
                .entityQueries()
                .list(thingifier.getDefinitionNamed("cart"))
                .size();
    }

    private ThingStore storeFor(final Thingifier thingifier) {
        return thingifier.getStore(EntityRelModel.DEFAULT_DATABASE_NAME);
    }

    private HttpApiRequest jsonRequest(final String path) {
        return new HttpApiRequest(path).addHeader("Accept", "application/json");
    }

    private HttpApiRequest jsonPost(final String path, final String body) {
        return new HttpApiRequest(path)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private HttpApiRequest jsonPathQuery(final String path, final String body) {
        return new HttpApiRequest(path)
                .addHeader("Content-Type", ThingifierHttpApi.JSONPATH_QUERY_CONTENT_TYPE)
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private RoutingDefinition route(
            final ApiRoutingDefinition definition, final RoutingVerb verb, final String url) {
        return definition.definitions().stream()
                .filter(route -> route.verb() == verb)
                .filter(route -> route.url().equals(url))
                .findFirst()
                .orElseThrow();
    }
}
