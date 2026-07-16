package uk.co.compendiumdev.challenge.challengehooks;

import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.DELETE;
import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.GET;
import static uk.co.compendiumdev.thingifier.api.http.HttpApiRequest.VERB.POST;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.compendiumdev.challenge.CHALLENGE;
import uk.co.compendiumdev.challenge.ChallengerAuthData;
import uk.co.compendiumdev.challenge.challengers.Challengers;
import uk.co.compendiumdev.thingifier.Thingifier;
import uk.co.compendiumdev.thingifier.api.ermodelconversion.JsonThing;
import uk.co.compendiumdev.thingifier.api.http.HttpApiRequest;
import uk.co.compendiumdev.thingifier.api.http.HttpApiResponse;
import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.api.response.ApiResponse;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.Field;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.definition.FieldType;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstanceDraft;
import uk.co.compendiumdev.thingifier.core.repository.ThingStore;
import uk.co.compendiumdev.thingifier.core.repository.ThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.inmemory.InMemoryThingStoreProvider;
import uk.co.compendiumdev.thingifier.core.repository.sqlite.SqliteThingStoreProvider;

public class ChallengerApiResponseHookTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosChallengeCompletesWhenDoneAndNotDoneTodosExist(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");
            fixture.addTodo("not done", "false");

            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("doneStatus", "true")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void filteredTodosChallengeDoesNotCompleteWithoutMixedDoneStatusTodos(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            fixture.addTodo("done", "true");

            fixture.hook.run(
                    fixture.request("todos", GET).setQueryParams(Map.of("doneStatus", "true")),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.GET_TODOS_FILTERED));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void postMaxContentChallengeReadsCreatedTodoThroughRepository(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance todo =
                    fixture.addTodo(
                            "2*4*6*8*11*14*17*20*23*26*29*32*35*38*41*44*47*50*",
                            "true",
                            "*3*5*7*9*12*15*18*21*24*27*30*33*36*39*42*45*48*51*"
                                    + "54*57*60*63*66*69*72*75*78*81*84*87*90*93*96*100*"
                                    + "104*108*112*116*120*124*128*132*136*140*144*148*"
                                    + "152*156*160*164*168*172*176*180*184*188*192*196*200*");

            ApiResponse created =
                    new ApiResponse(201).setLocationHeader("/todos/" + todo.getPrimaryKeyValue());

            fixture.hook.run(
                    fixture.request("todos", POST),
                    fixture.apiResponse(created),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(
                    fixture.challenger.statusOfChallenge(
                            CHALLENGE.POST_MAX_OUT_TITLE_DESCRIPTION_LENGTH));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void deleteAllTodosChallengeCompletesWhenRepositoryIsEmpty(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance todo = fixture.addTodo("delete me", "false");
            fixture.repository.entities().delete(todo);

            fixture.hook.run(
                    fixture.request("todos/" + todo.getPrimaryKeyValue(), DELETE),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertTrue(fixture.challenger.statusOfChallenge(CHALLENGE.DELETE_ALL_TODOS));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("repositoryProviders")
    public void deleteAllTodosChallengeDoesNotCompleteWhenRepositoryStillHasTodos(
            final String repositoryName, final Supplier<ThingStoreProvider> providerFactory) {

        try (HookFixture fixture = new HookFixture(providerFactory.get())) {
            EntityInstance deletedTodo = fixture.addTodo("delete me", "false");
            fixture.addTodo("keep me", "false");
            fixture.repository.entities().delete(deletedTodo);

            fixture.hook.run(
                    fixture.request("todos/" + deletedTodo.getPrimaryKeyValue(), DELETE),
                    fixture.apiResponse(200),
                    fixture.thingifier.apiConfig());

            Assertions.assertFalse(
                    fixture.challenger.statusOfChallenge(CHALLENGE.DELETE_ALL_TODOS));
        }
    }

    private static Stream<Arguments> repositoryProviders() {
        return Stream.of(
                Arguments.of(
                        "in-memory",
                        (Supplier<ThingStoreProvider>) InMemoryThingStoreProvider::new),
                Arguments.of(
                        "sqlite-memory",
                        (Supplier<ThingStoreProvider>) SqliteThingStoreProvider::inMemory));
    }

    private static class HookFixture implements AutoCloseable {
        private final Thingifier thingifier;
        private final Challengers challengers;
        private final ChallengerAuthData challenger;
        private final ChallengerApiResponseHook hook;
        private final EntityDefinition todo;
        private final ThingStore repository;

        HookFixture(final ThingStoreProvider provider) {
            thingifier = new Thingifier(new EntityRelModel(provider));
            todo = thingifier.defineThing("todo", "todos");
            todo.addAsPrimaryKeyField(Field.is("id", FieldType.AUTO_INCREMENT));
            todo.addFields(
                    Field.is("title", FieldType.STRING),
                    Field.is("doneStatus", FieldType.BOOLEAN).withDefaultValue("false"),
                    Field.is("description", FieldType.STRING));

            challengers =
                    new Challengers(thingifier.getERmodel(), Arrays.asList(CHALLENGE.values()));
            challengers.setMultiPlayerMode();
            challenger = challengers.createNewChallenger();
            thingifier.ensureCreatedAndPopulatedInstanceDatabaseNamed(challenger.getXChallenger());
            repository = thingifier.getStore(challenger.getXChallenger());
            hook = new ChallengerApiResponseHook(challengers, thingifier);
        }

        EntityInstance addTodo(final String title, final String doneStatus) {
            return addTodo(title, doneStatus, "");
        }

        EntityInstance addTodo(
                final String title, final String doneStatus, final String description) {
            return repository
                    .entities()
                    .create(
                            EntityInstanceDraft.forEntity(todo)
                                    .withField("title", title)
                                    .withField("doneStatus", doneStatus)
                                    .withField("description", description));
        }

        HttpApiRequest request(final String path, final HttpApiRequest.VERB verb) {
            return new HttpApiRequest(path)
                    .setVerb(verb)
                    .addHeader("X-CHALLENGER", challenger.getXChallenger());
        }

        HttpApiResponse apiResponse(final int statusCode) {
            return apiResponse(new ApiResponse(statusCode));
        }

        HttpApiResponse apiResponse(final ApiResponse apiResponse) {
            return new HttpApiResponse(
                    new HttpHeadersBlock(),
                    apiResponse,
                    new JsonThing(thingifier.apiConfig().jsonOutput()),
                    thingifier.apiConfig());
        }

        @Override
        public void close() {
            thingifier.close();
        }
    }
}
