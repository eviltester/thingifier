package uk.co.compendiumdev.thingifier.crudui.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.compendiumdev.thingifier.crudui.e2e.pages.SwaggerPage;

public class SwaggerCopyForAiUiIT extends BrowserTestBase {

    private SwaggerPage swagger;

    @BeforeEach
    void openSwagger() {
        resetToProjectTasks();
        swagger = new SwaggerPage(page, server().baseUrl());
        swagger.copy().installClipboardStub();
        swagger.open();
    }

    @Test
    public void operationCopyForAiCopiesMarkdownWithCurrentOriginAndApiPrefix() {
        assertThat(swagger.copy().fullApiButton()).isVisible();
        assertThat(swagger.copy().operationButton("POST", "/projects")).isVisible();

        swagger.copy().operationButton("POST", "/projects").click();
        swagger.copy().waitForCopiedTextContaining("POST " + server().baseUrl() + "/api/projects");

        final String copied = swagger.copy().copiedText();
        Assertions.assertTrue(copied.contains("# POST " + server().baseUrl() + "/api/projects"));
        Assertions.assertTrue(copied.contains("- OpenAPI path: `/projects`"));
        Assertions.assertTrue(copied.contains("## Request Body"));
        Assertions.assertTrue(copied.contains("## Responses"));
        Assertions.assertTrue(copied.contains("## Referenced Schemas"));
    }

    @Test
    public void fullApiCopyForAiCopiesAllOperationsAndSchemas() {
        assertThat(swagger.copy().firstOperationButton()).isVisible();

        swagger.copy().fullApiButton().click();
        swagger.copy().waitForCopiedTextContaining("## Component Schemas");

        final String copied = swagger.copy().copiedText();
        Assertions.assertTrue(copied.contains("# Project Tasks"));
        Assertions.assertTrue(copied.contains("- Base URL: `" + server().baseUrl() + "/api`"));
        Assertions.assertTrue(copied.contains("GET " + server().baseUrl() + "/api/projects"));
        Assertions.assertTrue(copied.contains("POST " + server().baseUrl() + "/api/todos"));
        Assertions.assertTrue(copied.contains("## Component Schemas"));
    }
}
