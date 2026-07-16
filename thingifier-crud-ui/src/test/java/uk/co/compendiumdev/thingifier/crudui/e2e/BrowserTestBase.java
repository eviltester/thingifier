package uk.co.compendiumdev.thingifier.crudui.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

abstract class BrowserTestBase {

    private static final Path ARTIFACT_DIR =
            Path.of("thingifier-crud-ui", "target", "playwright-artifacts");
    private static CrudUiTestServer sharedServer;
    private static final ThreadLocal<BrowserTestBase> CURRENT = new ThreadLocal<>();

    @RegisterExtension final BrowserFailureArtifacts artifacts = new BrowserFailureArtifacts();

    protected Playwright playwright;
    protected Browser browser;
    protected BrowserContext context;
    protected Page page;
    protected final List<String> consoleMessages = new ArrayList<>();
    protected final List<String> requestFailures = new ArrayList<>();

    @BeforeAll
    static void startCrudUi() {
        if (sharedServer == null) {
            sharedServer = CrudUiTestServer.start();
        }
    }

    @AfterAll
    static void stopCrudUi() {
        if (sharedServer != null) {
            sharedServer.close();
            sharedServer = null;
        }
    }

    @BeforeEach
    void launchBrowser() {
        CURRENT.set(this);
        playwright = Playwright.create();
        browser =
                playwright
                        .chromium()
                        .launch(
                                new BrowserType.LaunchOptions()
                                        .setHeadless(
                                                Boolean.parseBoolean(
                                                        System.getProperty(
                                                                "crud.ui.headless", "true"))));
        context = browser.newContext(new Browser.NewContextOptions().setAcceptDownloads(true));
        page = context.newPage();
        page.onConsoleMessage(
                message -> consoleMessages.add(message.type() + ": " + message.text()));
        page.onRequestFailed(
                request -> requestFailures.add(request.method() + " " + request.url()));
    }

    @AfterEach
    void closeBrowser() {
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        CURRENT.remove();
    }

    protected CrudUiTestServer server() {
        return sharedServer;
    }

    protected CrudUiApiClient api() {
        return sharedServer.api();
    }

    protected void resetToProjectTasks() {
        server().resetToYaml("/models/project-tasks.yaml");
    }

    protected void resetToMinimalTodo() {
        server().resetToYaml("/models/minimal-todo.yaml");
    }

    protected void captureArtifacts(final String displayName) {
        try {
            Files.createDirectories(ARTIFACT_DIR);
            String safeName =
                    displayName.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            if (page != null) {
                page.screenshot(
                        new Page.ScreenshotOptions()
                                .setPath(ARTIFACT_DIR.resolve(safeName + ".png"))
                                .setFullPage(true));
                Files.writeString(ARTIFACT_DIR.resolve(safeName + ".html"), page.content());
            }
            Files.writeString(
                    ARTIFACT_DIR.resolve(safeName + "-console.txt"),
                    String.join(System.lineSeparator(), consoleMessages));
            Files.writeString(
                    ARTIFACT_DIR.resolve(safeName + "-network.txt"),
                    String.join(System.lineSeparator(), requestFailures));
        } catch (IOException ignored) {
            // Failure artifacts are best-effort only.
        }
    }

    private static final class BrowserFailureArtifacts implements TestExecutionExceptionHandler {

        @Override
        public void handleTestExecutionException(
                final ExtensionContext context, final Throwable throwable) throws Throwable {
            BrowserTestBase base = CURRENT.get();
            if (base != null) {
                base.captureArtifacts(context.getDisplayName());
            }
            throw throwable;
        }
    }
}
