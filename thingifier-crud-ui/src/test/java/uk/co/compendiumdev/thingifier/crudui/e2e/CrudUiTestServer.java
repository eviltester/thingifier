package uk.co.compendiumdev.thingifier.crudui.e2e;

import java.io.IOException;
import java.net.ServerSocket;
import uk.co.compendiumdev.thingifier.crudui.ActiveThingifierWorkspace;
import uk.co.compendiumdev.thingifier.crudui.adapter.javalin.CrudUiApplication;

final class CrudUiTestServer implements AutoCloseable {

    private final int port;
    private final ActiveThingifierWorkspace workspace;
    private final CrudUiApplication application;
    private final CrudUiApiClient api;

    private CrudUiTestServer(final int port) {
        this.port = port;
        this.workspace = ActiveThingifierWorkspace.defaultTodoManagerWorkspace();
        this.application = new CrudUiApplication(workspace, port);
        this.api = new CrudUiApiClient(baseUrl());
    }

    static CrudUiTestServer start() {
        CrudUiTestServer server = new CrudUiTestServer(availablePort());
        server.application.start();
        return server;
    }

    String baseUrl() {
        return "http://localhost:" + port;
    }

    CrudUiApiClient api() {
        return api;
    }

    void resetToYaml(final String resourcePath) {
        api.postJson("/ui/storage/switch", "{\"mode\":\"memory\"}");
        CrudUiApiClient.ApiResult result =
                api.postText("/ui/model/yaml", E2eResource.text(resourcePath));
        if (result.statusCode() != 200) {
            throw new IllegalStateException(result.body());
        }
    }

    private static int availablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        application.close();
    }
}
