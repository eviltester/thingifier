package uk.co.compendiumdev.thingifier.api.security;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.adapter.http.lifecycle.ThingifierApiLifecycleHookRegistry;
import uk.co.compendiumdev.thingifier.api.docgen.RoutingVerb;
import uk.co.compendiumdev.thingifier.api.docgen.ThingifierApiDocumentationDefn;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi;
import uk.co.compendiumdev.thingifier.api.http.bodyparser.BodyParser;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.api.spec.ThingifierApiRouteRule;
import uk.co.compendiumdev.thingifier.apiconfig.ThingifierApiConfig;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;

class ThingifierApiAuthPolicyTest {

    @Test
    void namedBearerSchemeIsDocumentedOnProtectedGeneratedRoute() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().security().bearer("cartToken");
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/todos")
                .secureWithBearerAuth("cartToken");
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);
        apiDefn.setPathPrefix("/api");

        final OpenAPI openApi =
                new uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer(apiDefn).swagger();

        final SecurityScheme scheme = openApi.getComponents().getSecuritySchemes().get("cartToken");
        Assertions.assertNotNull(scheme);
        Assertions.assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        Assertions.assertEquals("bearer", scheme.getScheme());
        Assertions.assertEquals(
                "cartToken",
                openApi.getPaths()
                        .get("/api/todos")
                        .getPost()
                        .getSecurity()
                        .get(0)
                        .keySet()
                        .iterator()
                        .next());
    }

    @Test
    void namedBasicSchemeIsDocumentedOnProtectedGeneratedRoute() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().security().basic("adminPassword");
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/todos")
                .secureWithBasicAuth("adminPassword");
        final ThingifierApiDocumentationDefn apiDefn = new ThingifierApiDocumentationDefn();
        apiDefn.setThingifier(thingifier);
        apiDefn.setPathPrefix("/api");

        final OpenAPI openApi =
                new uk.co.compendiumdev.thingifier.swaggerizer.Swaggerizer(apiDefn).swagger();

        final SecurityScheme scheme =
                openApi.getComponents().getSecuritySchemes().get("adminPassword");
        Assertions.assertNotNull(scheme);
        Assertions.assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        Assertions.assertEquals("basic", scheme.getScheme());
        Assertions.assertEquals(
                "adminPassword",
                openApi.getPaths()
                        .get("/api/todos")
                        .getPost()
                        .getSecurity()
                        .get(0)
                        .keySet()
                        .iterator()
                        .next());
    }

    @Test
    @SuppressWarnings("deprecation")
    void legacyBearerMarkerRemainsDocumentationOnly() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth();

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"created\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, todoCount(thingifier));
    }

    @Test
    void legacyBasicMarkerRemainsDocumentationOnly() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBasicAuth();

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"created\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, todoCount(thingifier));
    }

    @Test
    void directApiMissingBearerTokenReturns401WithoutMutation() {
        final Thingifier thingifier = todoModel();
        protectTodoPost(thingifier);

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals("Bearer", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void directApiMissingBasicCredentialsReturns401WithoutMutation() {
        final Thingifier thingifier = todoModel();
        protectTodoPostWithBasic(thingifier);

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(
                "Basic realm=\"Thingifier\"", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void directApiInvalidBearerTokenReturns401WithoutMutation() {
        final Thingifier thingifier = todoModel();
        protectTodoPost(thingifier);

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBearer("wrong"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals("Bearer", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void directApiInvalidBasicCredentialsReturns401WithoutMutation() {
        final Thingifier thingifier = todoModel();
        protectTodoPostWithBasic(thingifier);

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBasic("admin", "wrong"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(
                "Basic realm=\"Thingifier\"", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void malformedBearerHeaderReturns401BeforeAuthenticator() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<Boolean> authenticatorCalled = new AtomicReference<>(false);
        thingifier
                .apiSpec()
                .authenticator(
                        "cartToken",
                        context -> {
                            authenticatorCalled.set(true);
                            return ThingifierApiAuthenticationResult.authenticated();
                        });
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("cartToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithAuthorization("Basic valid-token"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertFalse(authenticatorCalled.get());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void bearerHeaderForBasicRouteReturns401BeforeAuthenticator() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<Boolean> authenticatorCalled = new AtomicReference<>(false);
        thingifier
                .apiSpec()
                .authenticator(
                        "adminPassword",
                        context -> {
                            authenticatorCalled.set(true);
                            return ThingifierApiAuthenticationResult.authenticated();
                        });
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBasicAuth("adminPassword");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(
                "Basic realm=\"Thingifier\"", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertFalse(authenticatorCalled.get());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void malformedBasicHeaderReturns401BeforeAuthenticator() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<Boolean> authenticatorCalled = new AtomicReference<>(false);
        thingifier
                .apiSpec()
                .authenticator(
                        "adminPassword",
                        context -> {
                            authenticatorCalled.set(true);
                            return ThingifierApiAuthenticationResult.authenticated();
                        });
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBasicAuth("adminPassword");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithAuthorization("Basic not-base64"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(
                "Basic realm=\"Thingifier\"", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertFalse(authenticatorCalled.get());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void directApiValidBearerTokenAllowsMutation() {
        final Thingifier thingifier = todoModel();
        protectTodoPost(thingifier);

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"created\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, todoCount(thingifier));
    }

    @Test
    void validBasicSyntaxCallsAuthenticatorWithUsernameAndPassword() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<ThingifierApiAuthenticationContext> seenContext =
                new AtomicReference<>();
        thingifier
                .apiSpec()
                .authenticator(
                        "adminPassword",
                        context -> {
                            seenContext.set(context);
                            return ThingifierApiAuthenticationResult.rejected(403, "checked");
                        });
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBasicAuth("adminPassword");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBasic("admin", "password"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertNotNull(seenContext.get());
        Assertions.assertEquals("adminPassword", seenContext.get().schemeName());
        Assertions.assertEquals("admin", seenContext.get().basicUsername());
        Assertions.assertEquals("password", seenContext.get().basicPassword());
        Assertions.assertEquals("", seenContext.get().bearerToken());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void directApiValidBasicCredentialsAllowMutation() {
        final Thingifier thingifier = todoModel();
        protectTodoPostWithBasic(thingifier);

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"created\"}"),
                                headersWithBasic("admin", "password"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, todoCount(thingifier));
    }

    @Test
    void directApiMissingBearerTokenReturns401ForProtectedRead() {
        final Thingifier thingifier = todoModel();
        createTodo(thingifier, "existing");
        thingifier.apiSpec().authenticator("cartToken", this::validTokenAuthenticator);
        thingifier.apiSpec().route(RoutingVerb.GET, "/todos").secureWithBearerAuth("cartToken");

        final ApiResponse response =
                thingifier.api().get("todos", new QueryFilterParams(), new HttpHeadersBlock());

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals("Bearer", response.getHeaders().get("WWW-Authenticate"));
    }

    @Test
    void basicChallengeUsesConfiguredRealm() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().security().basic("adminPassword", "User Visible Realm");
        protectTodoPostWithBasic(thingifier);

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                new HttpHeadersBlock());

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(
                "Basic realm=\"User Visible Realm\"",
                response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void authorizerDenialReturns403WithoutMutation() {
        final Thingifier thingifier = todoModel();
        protectTodoPost(thingifier)
                .authorizeWith(context -> ThingifierApiAuthorizationResult.forbidden());

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void authorizerReceivesAuthenticatedPrincipal() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<Object> seenPrincipal = new AtomicReference<>();
        protectTodoPost(thingifier)
                .authorizeWith(
                        context -> {
                            seenPrincipal.set(context.principal());
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"created\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("valid-principal", seenPrincipal.get());
    }

    @Test
    void basicAuthorizerReceivesAuthenticatedPrincipal() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<Object> seenPrincipal = new AtomicReference<>();
        protectTodoPostWithBasic(thingifier)
                .authorizeWith(
                        context -> {
                            seenPrincipal.set(context.principal());
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"created\"}"),
                                headersWithBasic("admin", "password"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("basic-principal", seenPrincipal.get());
    }

    @Test
    void basicAuthorizerCanReadParsedCredentials() {
        final Thingifier thingifier = todoModel();
        final AtomicReference<String> seenUsername = new AtomicReference<>();
        final AtomicReference<String> seenPassword = new AtomicReference<>();
        protectTodoPostWithBasic(thingifier)
                .authorizeWith(
                        context -> {
                            seenUsername.set(context.basicUsername());
                            seenPassword.set(context.basicPassword());
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"created\"}"),
                                headersWithBasic("admin", "password"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("admin", seenUsername.get());
        Assertions.assertEquals("password", seenPassword.get());
    }

    @Test
    void basicAuthenticatorCustom401CanOmitChallengeAndBody() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .authenticator(
                        "adminPassword",
                        context ->
                                ThingifierApiAuthenticationResult.rejected(new ApiResponse(401)));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBasicAuth("adminPassword");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBasic("admin", "password"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals("", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertFalse(response.hasABody());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void basicAuthenticatorCustom403PreservesHeadersAndBody() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .authenticator(
                        "adminPassword",
                        context -> {
                            final ApiResponse response =
                                    new ApiResponse(403).setHeader("X-Auth-Reason", "locked");
                            response.setBody("custom forbidden");
                            return ThingifierApiAuthenticationResult.rejected(response);
                        });
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBasicAuth("adminPassword");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBasic("admin", "password"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertEquals("locked", response.getHeaders().get("X-Auth-Reason"));
        Assertions.assertEquals("", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals("custom forbidden", response.getBody());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void httpApiMissingBearerTokenReturns401WithoutMutation() {
        final Thingifier thingifier = todoModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        protectPrefixedTodoPost(thingifier);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPostRequest("/api/todos", "{\"title\":\"blocked\"}"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals("Bearer", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void httpApiMissingBasicCredentialsReturns401WithoutMutation() {
        final Thingifier thingifier = todoModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        protectPrefixedTodoPostWithBasic(thingifier);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(jsonPostRequest("/api/todos", "{\"title\":\"blocked\"}"));

        Assertions.assertEquals(401, response.getStatusCode());
        Assertions.assertEquals(
                "Basic realm=\"Thingifier\"", response.getHeaders().get("WWW-Authenticate"));
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    @Test
    void httpApiValidBearerTokenAllowsMutation() {
        final Thingifier thingifier = todoModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        protectPrefixedTodoPost(thingifier);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(
                                jsonPostRequest("/api/todos", "{\"title\":\"created\"}")
                                        .addHeader("Authorization", "Bearer valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, todoCount(thingifier));
    }

    @Test
    void httpApiValidBasicCredentialsAllowMutation() {
        final Thingifier thingifier = todoModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        protectPrefixedTodoPostWithBasic(thingifier);

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .post(
                                jsonPostRequest("/api/todos", "{\"title\":\"created\"}")
                                        .addHeader(
                                                "Authorization",
                                                basicAuthorization("admin", "password")));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, todoCount(thingifier));
    }

    @Test
    void authorizerReceivesNamedPathParameters() {
        final Thingifier thingifier = cartModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        createCart(thingifier);
        final AtomicReference<String> seenCartId = new AtomicReference<>();
        thingifier.apiSpec().authenticator("cartToken", this::validTokenAuthenticator);
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/api/carts/{cartId}")
                .secureWithBearerAuth("cartToken")
                .authorizeWith(
                        context -> {
                            seenCartId.set(context.pathParameter("cartId"));
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(
                                new HttpApiRequest("/api/carts/1")
                                        .addHeader("Authorization", "Bearer valid-token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("1", seenCartId.get());
    }

    @Test
    void authorizerReceivesRouteIdentifiers() {
        final Thingifier thingifier = cartModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        createCart(thingifier);
        final AtomicReference<String> seenTargetIdentifier = new AtomicReference<>();
        thingifier.apiSpec().authenticator("cartToken", this::validTokenAuthenticator);
        thingifier
                .apiSpec()
                .route(RoutingVerb.GET, "/api/carts/{cartId}")
                .secureWithBearerAuth("cartToken")
                .authorizeWith(
                        context -> {
                            seenTargetIdentifier.set(context.targetIdentifier());
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final HttpApiResponse response =
                new ThingifierHttpApi(thingifier)
                        .get(
                                new HttpApiRequest("/api/carts/1")
                                        .addHeader("Authorization", "Bearer valid-token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("1", seenTargetIdentifier.get());
    }

    @Test
    void bearerAuthenticatorSelectsDataScopeForWrite() {
        final Thingifier thingifier = todoModel();
        ensureDataScopeExists(thingifier, "tenant-one");
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope("tenant-one"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                0, todoCountInScope(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(1, todoCountInScope(thingifier, "tenant-one"));
    }

    @Test
    void basicAuthenticatorSelectsDataScopeForWrite() {
        final Thingifier thingifier = todoModel();
        ensureDataScopeExists(thingifier, "tenant-one");
        thingifier
                .apiSpec()
                .authenticator(
                        "adminPassword",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("basic-principal")
                                        .useDataScope("tenant-one"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBasicAuth("adminPassword");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithBasic("admin", "password"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                0, todoCountInScope(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(1, todoCountInScope(thingifier, "tenant-one"));
    }

    @Test
    void authorizerReceivesAuthSelectedDataScope() {
        final Thingifier thingifier = todoModel();
        ensureDataScopeExists(thingifier, "tenant-one");
        final AtomicReference<String> seenDataScope = new AtomicReference<>();
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope("tenant-one"));
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .secureWithBearerAuth("tenantToken")
                .authorizeWith(
                        context -> {
                            seenDataScope.set(context.dataScopeName());
                            return ThingifierApiAuthorizationResult.authorized();
                        });

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("tenant-one", seenDataScope.get());
    }

    @Test
    void explicitDefaultDataScopeOverridesSessionHeader() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDefaultDataScope());
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"default todo\"}"),
                                headersWithSessionAndBearer("tenant-one", "valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                1, todoCountInScope(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(0, todoCountInScope(thingifier, "tenant-one"));
    }

    @Test
    void useExistingOnlyRejectsMissingDataScope() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope("missing-tenant"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(
                0, todoCountInScope(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertNull(thingifier.getStore("missing-tenant"));
    }

    @Test
    void useExistingOnlyRejectsHeaderCreatedDataScope() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope("header-tenant"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithSessionAndBearer("header-tenant", "valid-token"));

        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals(0, todoCountInScope(thingifier, "header-tenant"));
    }

    @Test
    void ensureExistsCreatesUnpopulatedDataScope() {
        final Thingifier thingifier = todoModel();
        setSeedDataGenerator(thingifier, "seed todo");
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope(
                                                "tenant-one",
                                                DataScopeCreationPolicy.ENSURE_EXISTS));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"created todo\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(1, todoCountInScope(thingifier, "tenant-one"));
        Assertions.assertEquals("created todo", firstTodoTitleInScope(thingifier, "tenant-one"));
    }

    @Test
    void ensureCreatedAndPopulatedCreatesSeededDataScope() {
        final Thingifier thingifier = todoModel();
        setSeedDataGenerator(thingifier, "seed todo");
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope(
                                                "tenant-one",
                                                DataScopeCreationPolicy
                                                        .ENSURE_CREATED_AND_POPULATED));
        thingifier.apiSpec().route(RoutingVerb.GET, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .get("todos", new QueryFilterParams(), headersWithBearer("valid-token"));

        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals(1, response.getReturnedInstanceCollection().size());
        Assertions.assertEquals(
                "seed todo",
                response.getReturnedInstanceCollection().get(0).getFieldValue("title").asString());
    }

    @Test
    void validatorReceivesAuthSelectedStore() {
        final Thingifier thingifier = todoModel();
        ensureDataScopeExists(thingifier, "tenant-one");
        final AtomicReference<ThingStore> seenStore = new AtomicReference<>();
        thingifier
                .getDefinitionNamed("todo")
                .withDomainValidation(
                        context -> {
                            seenStore.set(context.store());
                            return new ValidationReport();
                        });
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope("tenant-one"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertSame(thingifier.getStore("tenant-one"), seenStore.get());
    }

    @Test
    void bodyParsedHookReceivesAuthSelectedDataScope() {
        final Thingifier thingifier = todoModel();
        thingifier.apiConfig().setFrom(new ThingifierApiConfig("/api"));
        ensureDataScopeExists(thingifier, "tenant-one");
        final AtomicReference<String> seenDataScope = new AtomicReference<>();
        final ThingifierApiLifecycleHookRegistry hooks = new ThingifierApiLifecycleHookRegistry();
        hooks.registerBodyParsedHook(context -> seenDataScope.set(context.dataScopeName()));
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated("tenant-principal")
                                        .useDataScope("tenant-one"));
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/todos")
                .secureWithBearerAuth("tenantToken");

        final HttpApiResponse response =
                ThingifierHttpApi.withHookRegistries(thingifier, null, hooks)
                        .post(
                                jsonPostRequest("/api/todos", "{\"title\":\"tenant todo\"}")
                                        .addHeader("Authorization", "Bearer valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals("tenant-one", seenDataScope.get());
    }

    @Test
    void sessionHeaderScopeAppliesWhenAuthenticationDoesNotSelectScope() {
        final Thingifier thingifier = todoModel();
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context ->
                                ThingifierApiAuthenticationResult.authenticated(
                                        "tenant-principal"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"tenant todo\"}"),
                                headersWithSessionAndBearer("tenant-one", "valid-token"));

        Assertions.assertEquals(201, response.getStatusCode());
        Assertions.assertEquals(
                0, todoCountInScope(thingifier, EntityRelModel.DEFAULT_DATABASE_NAME));
        Assertions.assertEquals(1, todoCountInScope(thingifier, "tenant-one"));
    }

    @Test
    void rejectedAuthenticationLeavesTargetScopeUnused() {
        final Thingifier thingifier = todoModel();
        ensureDataScopeExists(thingifier, "tenant-one");
        thingifier
                .apiSpec()
                .authenticator(
                        "tenantToken",
                        context -> ThingifierApiAuthenticationResult.rejected(403, "Forbidden"));
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("tenantToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(403, response.getStatusCode());
        Assertions.assertEquals(0, todoCountInScope(thingifier, "tenant-one"));
    }

    @Test
    void missingAuthenticatorReturnsConfigurationError() {
        final Thingifier thingifier = todoModel();
        thingifier.apiSpec().route(RoutingVerb.POST, "/todos").secureWithBearerAuth("cartToken");

        final ApiResponse response =
                thingifier
                        .api()
                        .post(
                                "todos",
                                parser(thingifier, "{\"title\":\"blocked\"}"),
                                headersWithBearer("valid-token"));

        Assertions.assertEquals(500, response.getStatusCode());
        Assertions.assertEquals(0, todoCount(thingifier));
    }

    private ThingifierApiRouteRule protectTodoPost(final Thingifier thingifier) {
        thingifier.apiSpec().authenticator("cartToken", this::validTokenAuthenticator);
        return thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .secureWithBearerAuth("cartToken");
    }

    private ThingifierApiRouteRule protectTodoPostWithBasic(final Thingifier thingifier) {
        thingifier.apiSpec().authenticator("adminPassword", this::validBasicAuthenticator);
        return thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/todos")
                .secureWithBasicAuth("adminPassword");
    }

    private void protectPrefixedTodoPost(final Thingifier thingifier) {
        thingifier.apiSpec().authenticator("cartToken", this::validTokenAuthenticator);
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/todos")
                .secureWithBearerAuth("cartToken");
    }

    private void protectPrefixedTodoPostWithBasic(final Thingifier thingifier) {
        thingifier.apiSpec().authenticator("adminPassword", this::validBasicAuthenticator);
        thingifier
                .apiSpec()
                .route(RoutingVerb.POST, "/api/todos")
                .secureWithBasicAuth("adminPassword");
    }

    private ThingifierApiAuthenticationResult validTokenAuthenticator(
            final ThingifierApiAuthenticationContext context) {
        if ("valid-token".equals(context.bearerToken())) {
            return ThingifierApiAuthenticationResult.authenticated("valid-principal");
        }
        return ThingifierApiAuthenticationResult.rejected("Invalid bearer token");
    }

    private ThingifierApiAuthenticationResult validBasicAuthenticator(
            final ThingifierApiAuthenticationContext context) {
        if ("admin".equals(context.basicUsername()) && "password".equals(context.basicPassword())) {
            return ThingifierApiAuthenticationResult.authenticated("basic-principal");
        }
        return ThingifierApiAuthenticationResult.rejected("Invalid basic credentials");
    }

    private Thingifier todoModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition todo = thingifier.defineThing("todo", "todos", 5);
        todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        todo.addField(Field.is("title", FieldType.STRING).makeMandatory());
        return thingifier;
    }

    private Thingifier cartModel() {
        final Thingifier thingifier = new Thingifier();
        final EntityDefinition cart = thingifier.defineThing("cart", "carts", 5);
        cart.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
        cart.addField(Field.is("state", FieldType.STRING));
        return thingifier;
    }

    private EntityInstance createCart(final Thingifier thingifier) {
        final EntityDefinition cart = thingifier.getDefinitionNamed("cart");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(cart).withField("state", "open"));
    }

    private EntityInstance createTodo(final Thingifier thingifier, final String title) {
        final EntityDefinition todo = thingifier.getDefinitionNamed("todo");
        return thingifier
                .getStore(EntityRelModel.DEFAULT_DATABASE_NAME)
                .entities()
                .create(EntityInstanceDraft.forEntity(todo).withField("title", title));
    }

    private void ensureDataScopeExists(final Thingifier thingifier, final String dataScopeName) {
        thingifier.getERmodel().createInstanceDatabaseIfNotExisting(dataScopeName);
    }

    private void setSeedDataGenerator(final Thingifier thingifier, final String title) {
        thingifier.setDataGenerator(
                (schema, store) -> {
                    final EntityDefinition todo = schema.getEntityDefinitionNamed("todo");
                    store.entities()
                            .create(EntityInstanceDraft.forEntity(todo).withField("title", title));
                });
    }

    private int todoCountInScope(final Thingifier thingifier, final String dataScopeName) {
        return thingifier.listThingInstancesNamed("todos", dataScopeName).size();
    }

    private String firstTodoTitleInScope(final Thingifier thingifier, final String dataScopeName) {
        return thingifier
                .listThingInstancesNamed("todos", dataScopeName)
                .get(0)
                .getFieldValue("title")
                .asString();
    }

    private BodyParser parser(final Thingifier thingifier, final String body) {
        return new BodyParser(
                new HttpApiRequest("/request")
                        .addHeader("Content-Type", "application/json")
                        .setBody(body),
                thingifier.getThingNames());
    }

    private HttpHeadersBlock headersWithBearer(final String token) {
        return headersWithAuthorization("Bearer " + token);
    }

    private HttpHeadersBlock headersWithBasic(final String username, final String password) {
        return headersWithAuthorization(basicAuthorization(username, password));
    }

    private HttpHeadersBlock headersWithSessionAndBearer(
            final String dataScopeName, final String token) {
        final HttpHeadersBlock headers = headersWithBearer(token);
        headers.put(ThingifierHttpApi.HTTP_SESSION_HEADER_NAME, dataScopeName);
        return headers;
    }

    private String basicAuthorization(final String username, final String password) {
        final String credentials = username + ":" + password;
        return "Basic "
                + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private HttpHeadersBlock headersWithAuthorization(final String authorization) {
        final HttpHeadersBlock headers = new HttpHeadersBlock();
        headers.put("Authorization", authorization);
        return headers;
    }

    private HttpApiRequest jsonPostRequest(final String path, final String body) {
        return new HttpApiRequest(path)
                .setVerb("POST")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .setBody(body);
    }

    private int todoCount(final Thingifier thingifier) {
        return thingifier
                .api()
                .get("todos", new QueryFilterParams(), new HttpHeadersBlock())
                .getReturnedInstanceCollection()
                .size();
    }
}
