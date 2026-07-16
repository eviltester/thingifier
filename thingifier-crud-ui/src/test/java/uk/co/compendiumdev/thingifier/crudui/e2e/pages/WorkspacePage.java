package uk.co.compendiumdev.thingifier.crudui.e2e.pages;

import com.microsoft.playwright.Page;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.DataGridComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.EditorPanelComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.MessageBarComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.OutlineTreeComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.ProjectDialogComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.RelationshipManagerComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.StorageControlsComponent;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.TestSelectors;
import uk.co.compendiumdev.thingifier.crudui.e2e.components.TopBarComponent;

public final class WorkspacePage {

    private final Page page;
    private final String baseUrl;
    private final TopBarComponent topBar;
    private final OutlineTreeComponent outline;
    private final DataGridComponent grid;
    private final EditorPanelComponent editor;
    private final RelationshipManagerComponent relationships;
    private final ProjectDialogComponent projectDialog;
    private final StorageControlsComponent storage;
    private final MessageBarComponent messages;

    public WorkspacePage(final Page page, final String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
        this.topBar = new TopBarComponent(page);
        this.outline = new OutlineTreeComponent(page);
        this.grid = new DataGridComponent(page);
        this.editor = new EditorPanelComponent(page);
        this.relationships = new RelationshipManagerComponent(page);
        this.projectDialog = new ProjectDialogComponent(page);
        this.storage = new StorageControlsComponent(page);
        this.messages = new MessageBarComponent(page);
    }

    public WorkspacePage open() {
        page.navigate(baseUrl + "/");
        TestSelectors.byTestId(page, "workspace-page").waitFor();
        TestSelectors.byTestId(page, "outline-tree").waitFor();
        return this;
    }

    public TopBarComponent topBar() {
        return topBar;
    }

    public OutlineTreeComponent outline() {
        return outline;
    }

    public DataGridComponent grid() {
        return grid;
    }

    public EditorPanelComponent editor() {
        return editor;
    }

    public RelationshipManagerComponent relationships() {
        return relationships;
    }

    public ProjectDialogComponent projectDialog() {
        return projectDialog;
    }

    public StorageControlsComponent storage() {
        return storage;
    }

    public MessageBarComponent messages() {
        return messages;
    }
}
