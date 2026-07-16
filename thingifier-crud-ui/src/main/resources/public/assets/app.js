const state = {
    workspace: null,
    outline: {},
    expandedNodes: {},
    entityCounts: {},
    currentEntity: null,
    currentRows: [],
    selectedRow: null,
    relationshipContext: null,
    filters: {},
    mode: document.body.dataset.page === "schema" ? "schema" : "workspace",
    schemaDraft: null,
    schemaPreview: null,
    schemaPreviewTimer: null,
    schemaSelection: {type: "model"},
    schemaDiagramVisible: true,
    schemaYamlVisible: false,
    schemaExportsVisible: false,
    schemaDirty: false,
    schemaDiagramZoom: 1,
    schemaDiagramDirection: "LR",
    schemaDiagramHeight: 280,
    schemaDiagramResizeStart: null,
    schemaUpgradeDialogOpen: false,
    projectDialogAction: null,
    schemaUpgradePreview: null,
    schemaUpgradeMappings: {
        entityMappings: {},
        fieldMappings: {},
        relationshipMappings: []
    }
};

const els = {
    workspaceMain: document.querySelector("main.workspace"),
    schemaEditor: document.getElementById("schema-editor"),
    title: document.getElementById("model-title"),
    description: document.getElementById("model-description"),
    tree: document.getElementById("outline-tree"),
    gridTitle: document.getElementById("grid-title"),
    gridContext: document.getElementById("grid-context"),
    gridHost: document.getElementById("grid-host"),
    editor: document.getElementById("editor-panel"),
    message: document.getElementById("message-bar"),
    search: document.getElementById("search-input"),
    newButton: document.getElementById("new-button"),
    refreshButton: document.getElementById("refresh-button"),
    exportButton: document.getElementById("export-button"),
    saveProjectButton: document.getElementById("save-project-button"),
    loadProjectButton: document.getElementById("load-project-button"),
    yamlFile: document.getElementById("yaml-file"),
    importFile: document.getElementById("import-file"),
    projectDialog: document.getElementById("project-dialog"),
    projectDialogTitle: document.getElementById("project-dialog-title"),
    projectDialogDescription: document.getElementById("project-dialog-description"),
    projectDialogWarning: document.getElementById("project-dialog-warning"),
    projectPathInput: document.getElementById("project-path-input"),
    projectDialogConfirm: document.getElementById("project-dialog-confirm"),
    projectDialogCancel: document.getElementById("project-dialog-cancel"),
    schemaWorkspaceLink: document.getElementById("schema-workspace-link"),
    schemaDirtyStatus: document.getElementById("schema-dirty-status"),
    schemaReset: document.getElementById("schema-reset-button"),
    schemaSaveYaml: document.getElementById("schema-save-yaml-button"),
    schemaToggleYaml: document.getElementById("schema-toggle-yaml-button"),
    schemaToggleExports: document.getElementById("schema-toggle-exports-button"),
    schemaToggleDiagram: document.getElementById("schema-toggle-diagram-button"),
    schemaZoomOut: document.getElementById("schema-zoom-out-button"),
    schemaZoomReset: document.getElementById("schema-zoom-reset-button"),
    schemaZoomIn: document.getElementById("schema-zoom-in-button"),
    schemaLayoutToggle: document.getElementById("schema-layout-toggle-button"),
    schemaDiagramContent: document.getElementById("schema-diagram-content"),
    schemaDiagramResizer: document.getElementById("schema-diagram-resizer"),
    schemaFormHost: document.getElementById("schema-form-host"),
    schemaTree: document.getElementById("schema-tree"),
    schemaAddEntity: document.getElementById("schema-add-entity-button"),
    schemaDetailTitle: document.getElementById("schema-detail-title"),
    schemaDetailHost: document.getElementById("schema-detail-host"),
    schemaYamlSection: document.getElementById("schema-yaml-section"),
    schemaExportsSection: document.getElementById("schema-exports-section"),
    schemaYamlInput: document.getElementById("schema-yaml-input"),
    schemaParseYaml: document.getElementById("schema-parse-yaml-button"),
    schemaValidate: document.getElementById("schema-validate-button"),
    schemaUpgradeDialog: document.getElementById("schema-upgrade-dialog"),
    schemaUpgradePreview: document.getElementById("schema-upgrade-preview-button"),
    schemaUpgradeConfirm: document.getElementById("schema-upgrade-confirm-button"),
    schemaUpgradeCancel: document.getElementById("schema-upgrade-cancel-button"),
    schemaApplyWorkspace: document.getElementById("schema-apply-workspace-button"),
    schemaUpgradeHost: document.getElementById("schema-upgrade-host"),
    schemaValidation: document.getElementById("schema-validation"),
    schemaDownloadYaml: document.getElementById("schema-download-yaml"),
    schemaDownloadMermaid: document.getElementById("schema-download-mermaid"),
    schemaDownloadGraphviz: document.getElementById("schema-download-graphviz"),
    schemaCopyYaml: document.getElementById("schema-copy-yaml"),
    schemaCopyMermaid: document.getElementById("schema-copy-mermaid"),
    schemaCopyGraphviz: document.getElementById("schema-copy-graphviz"),
    schemaCanonicalYaml: document.getElementById("schema-canonical-yaml"),
    schemaMermaidOutput: document.getElementById("schema-mermaid-output"),
    schemaGraphvizOutput: document.getElementById("schema-graphviz-output"),
    schemaMermaidDiagram: document.getElementById("schema-mermaid-diagram")
};

async function requestJson(path, options = {}) {
    const response = await fetch(path, {
        headers: {
            "Accept": "application/json",
            ...(options.body ? {"Content-Type": "application/json"} : {})
        },
        ...options
    });
    const text = await response.text();
    let body = {};
    if (text.trim().length > 0) {
        try {
            body = JSON.parse(text);
        } catch (error) {
            body = {errorMessages: [text]};
        }
    }
    if (!response.ok) {
        throw new Error(errorText(body, response.status));
    }
    return body;
}

function errorText(body, status) {
    if (body && Array.isArray(body.errorMessages)) {
        return body.errorMessages.join("\n");
    }
    return `Request failed with status ${status}`;
}

async function loadWorkspace() {
    clearMessage();
    state.workspace = await requestJson("/ui/workspace");
    renderHeader();
    if (!els.tree) {
        return;
    }
    const workspaceVersion = activeWorkspaceVersion();
    state.outline = {};
    state.expandedNodes = {};
    state.currentEntity = null;
    state.selectedRow = null;
    state.relationshipContext = null;
    if (!await refreshOutlineData(workspaceVersion)) {
        return;
    }
    renderShell();
    renderGrid([]);
    renderEditor();
}

async function refreshOutlineData(expectedWorkspaceVersion = activeWorkspaceVersion()) {
    state.entityCounts = {};
    const outline = {};
    await Promise.all(state.workspace.entities.map(async entity => {
        try {
            const body = await requestJson(`/api/${entity.plural}`);
            const instances = sortedRows(collectionFrom(body, entity), entity);
            state.entityCounts[entity.name] = instances.length;
            outline[entity.name] = {
                entity,
                instances: await relationshipNodesFor(entity, instances)
            };
        } catch (error) {
            state.entityCounts[entity.name] = "?";
            outline[entity.name] = {entity, instances: []};
        }
    }));
    if (workspaceChangedSince(expectedWorkspaceVersion)) {
        return false;
    }
    state.outline = outline;
    return true;
}

async function relationshipNodesFor(entity, instances) {
    return Promise.all(instances.map(async row => {
        return {
            row,
            relationships: await relationshipsForInstance(entity, row)
        };
    }));
}

async function relationshipsForInstance(entity, row) {
    const relationships = [];
    await Promise.all((entity.relationships || []).map(async relationship => {
        const target = entityByName(relationship.toEntity);
        let rows = [];
        try {
            const id = encodeURIComponent(primaryValue(entity, row));
            const body = await requestJson(`/api/${entity.plural}/${id}/${relationship.name}`);
            rows = sortedRows(collectionFrom(body, target), target);
        } catch (error) {
            rows = [];
        }
        relationships.push({relationship, target, rows});
    }));
    relationships.sort((left, right) => left.relationship.name.localeCompare(right.relationship.name));
    return relationships;
}

function renderShell() {
    renderHeader();
    if (els.tree) {
        renderTree();
    }
}

function renderHeader() {
    if (!state.workspace) {
        return;
    }
    if (state.mode === "schema") {
        els.title.textContent = "Schema Edit";
        els.description.textContent = `Draft for ${state.workspace.model.title || "Thingifier"}`;
        return;
    }
    els.title.textContent = state.workspace.model.title || "Thingifier";
    const description = state.workspace.model.description || "";
    const projectPath = state.workspace.project && state.workspace.project.path
        ? `Project: ${state.workspace.project.path}`
        : "";
    els.description.textContent = [description, projectPath].filter(Boolean).join(" | ");
}

function renderTree() {
    els.tree.innerHTML = "";
    state.workspace.entities.forEach(entity => {
        const entityKey = entityNodeKey(entity);
        const node = state.outline[entity.name] || {instances: []};
        const button = treeButton(
            `tree-item tree-entity ${isCurrentEntity(entity) ? "active" : ""}`,
            entity.plural,
            state.entityCounts[entity.name] ?? "",
            isExpanded(entityKey));
        button.addEventListener("click", event => {
            if (clickedCaret(event)) {
                toggleExpanded(entityKey);
                renderShell();
                return;
            }
            selectEntity(entity);
        });
        els.tree.appendChild(button);

        if (isExpanded(entityKey)) {
            const group = document.createElement("div");
            group.className = "tree-children entity-children";
            node.instances.forEach(instance => renderInstanceNode(group, entity, instance));
            if (node.instances.length === 0) {
                group.appendChild(emptyTreeNode("No instances"));
            }
            els.tree.appendChild(group);
        }
    });
}

function renderInstanceNode(container, entity, instanceNode) {
    const row = instanceNode.row;
    const instanceKey = instanceNodeKey(entity, row);
    const button = treeButton(
        `tree-item tree-instance ${isSelectedRow(entity, row) ? "active" : ""}`,
        instanceLabel(entity, row),
        "",
        isExpanded(instanceKey));
    button.addEventListener("click", event => {
        if (clickedCaret(event)) {
            toggleExpanded(instanceKey);
            renderShell();
            return;
        }
        selectOutlineInstance(entity, row);
    });
    container.appendChild(button);

    if (!isExpanded(instanceKey)) {
        return;
    }

    const relationshipGroup = document.createElement("div");
    relationshipGroup.className = "tree-children relationship-group";
    instanceNode.relationships.forEach(relationshipNode => {
        renderRelationshipNode(relationshipGroup, entity, row, relationshipNode);
    });
    if (instanceNode.relationships.length === 0) {
        relationshipGroup.appendChild(emptyTreeNode("No relationships"));
    }
    container.appendChild(relationshipGroup);
}

function renderRelationshipNode(container, entity, sourceRow, relationshipNode) {
    const relationship = relationshipNode.relationship;
    const relationshipKey = relationshipNodeKey(entity, sourceRow, relationship);
    const button = treeButton(
        `relationship-item ${isCurrentRelationship(entity, sourceRow, relationship) ? "active" : ""}`,
        relationship.name,
        relationshipNode.rows.length,
        isExpanded(relationshipKey));
    button.addEventListener("click", event => {
        if (clickedCaret(event)) {
            toggleExpanded(relationshipKey);
            renderShell();
            return;
        }
        selectRelationshipForRow(entity, sourceRow, relationship);
    });
    container.appendChild(button);

    if (!isExpanded(relationshipKey)) {
        return;
    }

    const relatedGroup = document.createElement("div");
    relatedGroup.className = "tree-children related-instance-group";
    relationshipNode.rows.forEach(row => {
        const relatedButton = treeButton(
            `relationship-item relationship-instance ${isSelectedRow(relationshipNode.target, row) ? "active" : ""}`,
            instanceLabel(relationshipNode.target, row),
            "",
            null);
        relatedButton.addEventListener(
            "click",
            () => selectRelatedInstance(relationshipNode.target, relationshipNode.rows, row));
        relatedGroup.appendChild(relatedButton);
    });
    if (relationshipNode.rows.length === 0) {
        relatedGroup.appendChild(emptyTreeNode("No related instances"));
    }
    container.appendChild(relatedGroup);
}

function treeButton(className, label, meta, expanded) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = className;

    const labelWrap = document.createElement("span");
    labelWrap.className = "tree-node-label";
    if (expanded !== null) {
        const caret = document.createElement("span");
        caret.className = "tree-caret";
        caret.textContent = expanded ? "v" : ">";
        labelWrap.appendChild(caret);
    }
    const text = document.createElement("span");
    text.textContent = label;
    labelWrap.appendChild(text);
    button.appendChild(labelWrap);

    const metaWrap = document.createElement("span");
    metaWrap.className = "tree-count";
    metaWrap.textContent = meta;
    button.appendChild(metaWrap);
    return button;
}

function emptyTreeNode(text) {
    const node = document.createElement("div");
    node.className = "tree-empty";
    node.textContent = text;
    return node;
}

function clickedCaret(event) {
    return event.target.classList.contains("tree-caret");
}

function isCurrentEntity(entity) {
    return state.currentEntity && state.currentEntity.name === entity.name;
}

function isCurrentRelationship(entity, sourceRow, relationship) {
    if (!state.relationshipContext) {
        return false;
    }
    return state.relationshipContext.entity.name === entity.name
        && state.relationshipContext.relationship.name === relationship.name
        && primaryValue(entity, state.relationshipContext.sourceRow) === primaryValue(entity, sourceRow);
}

function isSelectedRow(entity, row) {
    if (!entity || !state.currentEntity || !state.selectedRow) {
        return false;
    }
    return state.currentEntity.name === entity.name
        && primaryValue(entity, state.selectedRow) === primaryValue(entity, row);
}

async function selectEntity(entity) {
    clearMessage();
    state.currentEntity = entity;
    state.selectedRow = null;
    state.relationshipContext = null;
    state.filters = {};
    await loadCollection(entity);
}

async function loadCollection(entity) {
    if (!await refreshOutlineData()) {
        return;
    }
    state.currentRows = rowsForEntity(entity);
    renderShell();
    renderGrid(state.currentRows);
    renderEditor();
}

async function selectRelationship(entity, relationship) {
    if (!state.selectedRow) {
        return;
    }
    await selectRelationshipForRow(entity, state.selectedRow, relationship);
}

async function selectRelationshipForRow(entity, sourceRow, relationship) {
    clearMessage();
    const id = encodeURIComponent(primaryValue(entity, sourceRow));
    const body = await requestJson(`/api/${entity.plural}/${id}/${relationship.name}`);
    const target = entityByName(relationship.toEntity);
    state.currentEntity = target;
    state.selectedRow = null;
    state.relationshipContext = {entity, relationship, sourceRow, target};
    state.currentRows = collectionFrom(body, target);
    if (!await refreshOutlineData()) {
        return;
    }
    expandRelationshipContext();
    renderShell();
    renderGrid(state.currentRows, target);
    renderEditor();
}

function selectOutlineInstance(entity, row) {
    clearMessage();
    state.currentEntity = entity;
    state.currentRows = rowsForEntity(entity);
    state.selectedRow = row;
    state.relationshipContext = null;
    renderShell();
    renderGrid(state.currentRows, entity);
    renderEditor(row);
}

function selectRelatedInstance(entity, rows, row) {
    clearMessage();
    state.currentEntity = entity;
    state.currentRows = rows;
    state.selectedRow = row;
    renderShell();
    renderGrid(rows, entity);
    renderEditor(row);
}

function collectionFrom(body, entity) {
    if (!body || !entity) {
        return [];
    }
    if (Array.isArray(body[entity.plural])) {
        return body[entity.plural];
    }
    if (Array.isArray(body[entity.name])) {
        return body[entity.name];
    }
    return [];
}

function renderGrid(rows, gridEntity = state.currentEntity) {
    if (!gridEntity) {
        els.gridTitle.textContent = "Select an entity";
        els.gridContext.textContent = "";
        els.gridHost.innerHTML = `<div class="empty-state">Choose an entity from the outline.</div>`;
        return;
    }

    els.gridTitle.textContent = state.relationshipContext
        ? `${state.relationshipContext.relationship.name} related ${gridEntity.plural}`
        : gridEntity.plural;
    els.gridContext.textContent = `${filteredRows(rows, gridEntity).length} visible of ${rows.length}`;

    const fields = gridEntity.fields;
    const table = document.createElement("table");
    table.className = "data-grid";
    table.appendChild(gridHead(fields, gridEntity, isRelationshipGrid(gridEntity)));
    table.appendChild(gridBody(filteredRows(rows, gridEntity), fields, gridEntity));
    els.gridHost.innerHTML = "";
    if (isRelationshipGrid(gridEntity)) {
        els.gridHost.appendChild(relationshipManagementPanel(gridEntity, rows));
    }
    els.gridHost.appendChild(table);
}

function isRelationshipGrid(entity) {
    return state.relationshipContext
        && state.relationshipContext.target
        && entity
        && state.relationshipContext.target.name === entity.name;
}

function gridHead(fields, entity, hasRelationshipActions = false) {
    const thead = document.createElement("thead");
    const row = document.createElement("tr");
    fields.forEach(field => {
        const th = document.createElement("th");
        th.textContent = field.name;
        const input = document.createElement("input");
        input.className = "filter-input";
        input.placeholder = "Filter";
        input.value = state.filters[field.name] || "";
        input.addEventListener("input", event => {
            state.filters[field.name] = event.target.value;
            renderGrid(state.currentRows, entity);
        });
        th.appendChild(input);
        row.appendChild(th);
    });
    if (hasRelationshipActions) {
        const actions = document.createElement("th");
        actions.textContent = "Relationship";
        row.appendChild(actions);
    }
    thead.appendChild(row);
    return thead;
}

function gridBody(rows, fields, entity) {
    const tbody = document.createElement("tbody");
    rows.forEach(rowData => {
        const row = document.createElement("tr");
        if (isSelectedRow(entity, rowData)) {
            row.className = "selected";
        }
        row.addEventListener("click", () => selectRow(entity, rowData));
        fields.forEach(field => {
            const td = document.createElement("td");
            td.textContent = valueText(rowData[field.name]);
            row.appendChild(td);
        });
        if (isRelationshipGrid(entity)) {
            const td = document.createElement("td");
            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "danger compact-button";
            remove.textContent = "Remove";
            remove.title = "Remove from relationship";
            remove.addEventListener("click", event => {
                event.stopPropagation();
                removeRelationship(rowData);
            });
            td.appendChild(remove);
            row.appendChild(td);
        }
        tbody.appendChild(row);
    });
    if (rows.length === 0) {
        const row = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = Math.max(fields.length + (isRelationshipGrid(entity) ? 1 : 0), 1);
        td.className = "empty-state";
        td.textContent = "No rows match.";
        row.appendChild(td);
        tbody.appendChild(row);
    }
    return tbody;
}

function relationshipManagementPanel(targetEntity, relatedRows) {
    const context = state.relationshipContext;
    const panel = document.createElement("section");
    panel.className = "relationship-manager";

    const heading = document.createElement("div");
    heading.className = "relationship-manager-heading";
    const title = document.createElement("h3");
    title.textContent = `Manage ${context.relationship.name}`;
    const detail = document.createElement("p");
    detail.textContent =
        `${context.relationship.name} from ${instanceLabel(context.entity, context.sourceRow)} to ${targetEntity.plural}`;
    heading.appendChild(title);
    heading.appendChild(detail);
    panel.appendChild(heading);

    const layout = document.createElement("div");
    layout.className = "relationship-manager-layout";
    layout.appendChild(connectExistingPanel(targetEntity, relatedRows));
    layout.appendChild(createAndConnectPanel(targetEntity));
    panel.appendChild(layout);
    return panel;
}

function connectExistingPanel(targetEntity, relatedRows) {
    const card = document.createElement("form");
    card.className = "relationship-manager-card";
    const title = document.createElement("h4");
    title.textContent = "Connect existing";
    card.appendChild(title);

    const availableRows = unrelatedRowsFor(targetEntity, relatedRows);
    const select = document.createElement("select");
    select.name = "target";
    select.disabled = availableRows.length === 0;
    availableRows.forEach(row => {
        const option = document.createElement("option");
        option.value = primaryValue(targetEntity, row);
        option.textContent = instanceLabel(targetEntity, row);
        select.appendChild(option);
    });
    card.appendChild(select);

    if (availableRows.length === 0) {
        const note = document.createElement("p");
        note.className = "relationship-note";
        note.textContent = `No unconnected ${targetEntity.plural} available.`;
        card.appendChild(note);
    }

    const button = document.createElement("button");
    button.type = "submit";
    button.className = "primary";
    button.textContent = "Connect existing";
    button.disabled = availableRows.length === 0;
    card.appendChild(button);
    card.addEventListener("submit", event => {
        event.preventDefault();
        connectExistingRelationship(targetEntity, select);
    });
    return card;
}

function createAndConnectPanel(targetEntity) {
    const card = document.createElement("form");
    card.className = "relationship-manager-card relationship-create-form";
    const title = document.createElement("h4");
    title.textContent = "Create and connect";
    card.appendChild(title);

    const editableFields = targetEntity.fields.filter(field => !field.auto);
    editableFields.forEach(field => {
        card.appendChild(fieldControl(field, null, true, "relationship-create"));
    });
    if (editableFields.length === 0) {
        const note = document.createElement("p");
        note.className = "relationship-note";
        note.textContent = `No editable fields are defined for ${targetEntity.name}.`;
        card.appendChild(note);
    }

    const button = document.createElement("button");
    button.type = "submit";
    button.className = "primary";
    button.textContent = "Create and connect";
    card.appendChild(button);
    card.addEventListener("submit", event => {
        event.preventDefault();
        createAndConnectRelationship(targetEntity, card);
    });
    return card;
}

function filteredRows(rows, entity) {
    const global = els.search.value.trim().toLowerCase();
    return rows.filter(row => {
        const globalMatch = !global || JSON.stringify(row).toLowerCase().includes(global);
        const columnMatch = entity.fields.every(field => {
            const filter = (state.filters[field.name] || "").trim().toLowerCase();
            return !filter || valueText(row[field.name]).toLowerCase().includes(filter);
        });
        return globalMatch && columnMatch;
    });
}

function selectRow(entity, row) {
    state.currentEntity = entity;
    state.selectedRow = row;
    renderShell();
    renderGrid(state.currentRows, entity);
    renderEditor(row);
}

function renderEditor(row = state.selectedRow) {
    const entity = state.currentEntity;
    if (!entity) {
        els.editor.innerHTML = `<div class="empty-state">No editor available until an entity is selected.</div>`;
        return;
    }
    if (state.relationshipContext && !row) {
        els.editor.innerHTML =
            `<div class="empty-state">Select a related instance to edit it, or use the relationship controls to connect one.</div>`;
        return;
    }
    const isCreate = !row;
    els.editor.innerHTML = "";
    const title = document.createElement("h3");
    title.textContent = isCreate ? `New ${displayName(entity.name)}` : `Edit ${displayName(entity.name)}`;
    els.editor.appendChild(title);

    const form = document.createElement("form");
    form.className = "editor-form";
    entity.fields.forEach(field => form.appendChild(fieldControl(field, row, isCreate)));
    els.editor.appendChild(form);

    const actions = document.createElement("div");
    actions.className = "editor-actions";
    const save = document.createElement("button");
    save.type = "button";
    save.className = "primary";
    save.textContent = "Save";
    save.addEventListener("click", () => saveEntity(entity, form, isCreate));
    actions.appendChild(save);
    if (!isCreate) {
        if (state.relationshipContext) {
            const disconnect = document.createElement("button");
            disconnect.type = "button";
            disconnect.className = "danger";
            disconnect.textContent = "Remove from relationship";
            disconnect.addEventListener("click", () => removeRelationship(row));
            actions.appendChild(disconnect);
        }
        const remove = document.createElement("button");
        remove.type = "button";
        remove.className = "danger";
        remove.textContent = "Delete";
        remove.addEventListener("click", () => deleteEntity(entity, row));
        actions.appendChild(remove);
    }
    els.editor.appendChild(actions);
}

function fieldControl(field, row, isCreate, idPrefix = "editor") {
    const wrap = document.createElement("div");
    wrap.className = "field-control";
    const readOnly = field.auto || (!isCreate && field.primary);
    const autoAssigned = isCreate && field.auto;
    if (readOnly) {
        wrap.classList.add("field-control-readonly");
    }

    const label = document.createElement("label");
    label.textContent = field.required ? `${field.name} required` : field.name;

    const value = autoAssigned ? "<auto-assigned>" : row ? row[field.name] : field.defaultValue || "";
    const input = inputFor(field, value, autoAssigned);
    input.name = field.name;
    input.id = `${idPrefix}-${field.name}`;
    label.htmlFor = input.id;
    wrap.appendChild(label);
    if (readOnly) {
        input.disabled = true;
        input.setAttribute("aria-readonly", "true");
    }
    wrap.appendChild(input);

    if (autoAssigned || readOnly) {
        const status = document.createElement("span");
        status.className = "field-status";
        status.textContent = autoAssigned ? "Auto-assigned on save" : "Read-only";
        wrap.appendChild(status);
    }
    if (field.description) {
        const note = document.createElement("span");
        note.className = "field-note";
        note.textContent = field.description;
        wrap.appendChild(note);
    }
    return wrap;
}

function inputFor(field, value, forceText = false) {
    if (forceText) {
        const input = document.createElement("input");
        input.type = "text";
        input.value = value ?? "";
        return input;
    }
    if (field.type === "boolean") {
        const input = document.createElement("input");
        input.type = "checkbox";
        input.checked = String(value) === "true" || value === true;
        return input;
    }
    if (field.type === "integer" || field.type === "float") {
        const input = document.createElement("input");
        input.type = "number";
        input.value = value ?? "";
        if (field.type === "integer") {
            input.step = "1";
        }
        return input;
    }
    if (field.type === "enum" && Array.isArray(field.examples) && field.examples.length > 0) {
        const select = document.createElement("select");
        field.examples.forEach(example => {
            const option = document.createElement("option");
            option.value = example;
            option.textContent = example;
            option.selected = example === value;
            select.appendChild(option);
        });
        return select;
    }
    if (field.type === "object") {
        const textarea = document.createElement("textarea");
        textarea.value = value ? JSON.stringify(value, null, 2) : "";
        return textarea;
    }
    const input = document.createElement("input");
    input.type = field.type === "date" ? "date" : "text";
    input.value = value ?? "";
    return input;
}

async function saveEntity(entity, form, isCreate) {
    clearMessage();
    const body = bodyFromForm(entity, form);
    try {
        const path = isCreate
            ? `/api/${entity.plural}`
            : `/api/${entity.plural}/${encodeURIComponent(primaryValue(entity, state.selectedRow))}`;
        const method = isCreate ? "POST" : "PUT";
        await requestJson(path, {method, body: JSON.stringify(body)});
        showMessage(`${entity.name} saved.`);
        if (state.relationshipContext && !isCreate) {
            await refreshRelationshipView(primaryValue(entity, state.selectedRow));
        } else {
            await loadCollection(entity);
        }
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function deleteEntity(entity, row) {
    clearMessage();
    try {
        await requestJson(`/api/${entity.plural}/${encodeURIComponent(primaryValue(entity, row))}`, {
            method: "DELETE"
        });
        state.selectedRow = null;
        state.relationshipContext = null;
        showMessage(`${entity.name} deleted.`);
        await loadCollection(entity);
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function connectExistingRelationship(targetEntity, select) {
    if (!select.value) {
        return;
    }
    const body = {};
    body[targetEntity.primaryKey] = select.value;
    try {
        await requestJson(relationshipPostPath(), {method: "POST", body: JSON.stringify(body)});
        showMessage(`${targetEntity.name} connected.`);
        await refreshRelationshipView(select.value);
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function createAndConnectRelationship(targetEntity, form) {
    try {
        const created = await requestJson(
            relationshipPostPath(),
            {method: "POST", body: JSON.stringify(bodyFromForm(targetEntity, form))});
        showMessage(`${targetEntity.name} created and connected.`);
        await refreshRelationshipView(valueText(created[targetEntity.primaryKey]));
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function removeRelationship(targetRow) {
    const targetEntity = state.relationshipContext.target;
    const targetId = primaryValue(targetEntity, targetRow);
    try {
        await requestJson(relationshipDeletePath(targetId), {method: "DELETE"});
        state.selectedRow = null;
        showMessage(`${targetEntity.name} removed from relationship.`);
        await refreshRelationshipView();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function refreshRelationshipView(selectedTargetId = "") {
    const context = state.relationshipContext;
    if (!context) {
        return;
    }
    const body = await requestJson(relationshipCollectionPath());
    const rows = sortedRows(collectionFrom(body, context.target), context.target);
    state.currentEntity = context.target;
    state.currentRows = rows;
    state.selectedRow = selectedTargetId
        ? rows.find(row => String(primaryValue(context.target, row)) === String(selectedTargetId)) || null
        : null;
    if (!await refreshOutlineData()) {
        return;
    }
    expandRelationshipContext();
    renderShell();
    renderGrid(state.currentRows, context.target);
    renderEditor(state.selectedRow);
}

function bodyFromForm(entity, form) {
    const body = {};
    entity.fields.forEach(field => {
        const input = form.elements[field.name];
        if (!input || input.disabled) {
            return;
        }
        body[field.name] = valueFromInput(field, input);
    });
    return body;
}

function valueFromInput(field, input) {
    if (field.type === "boolean") {
        return input.checked ? "true" : "false";
    }
    if (field.type === "object") {
        try {
            return input.value.trim() ? JSON.parse(input.value) : {};
        } catch (error) {
            return input.value;
        }
    }
    return input.value;
}

function primaryValue(entity, row) {
    return row ? row[entity.primaryKey] : "";
}

function unrelatedRowsFor(targetEntity, relatedRows) {
    const relatedIds = new Set(relatedRows.map(row => String(primaryValue(targetEntity, row))));
    return rowsForEntity(targetEntity).filter(row =>
        !relatedIds.has(String(primaryValue(targetEntity, row))));
}

function relationshipCollectionPath() {
    const context = state.relationshipContext;
    return `/api/${context.entity.plural}/${encodeURIComponent(primaryValue(context.entity, context.sourceRow))}/${encodeURIComponent(context.relationship.name)}`;
}

function relationshipPostPath() {
    return relationshipCollectionPath();
}

function relationshipDeletePath(targetId) {
    return `${relationshipCollectionPath()}/${encodeURIComponent(targetId)}`;
}

function displayName(value) {
    if (!value) {
        return "";
    }
    return `${value.charAt(0).toUpperCase()}${value.slice(1)}`;
}

function rowsForEntity(entity) {
    const node = state.outline[entity.name];
    return node ? node.instances.map(instance => instance.row) : [];
}

function sortedRows(rows, entity) {
    return [...rows].sort((left, right) => comparePrimaryValues(entity, left, right));
}

function comparePrimaryValues(entity, left, right) {
    const leftValue = primaryValue(entity, left);
    const rightValue = primaryValue(entity, right);
    const leftNumber = Number(leftValue);
    const rightNumber = Number(rightValue);
    if (!Number.isNaN(leftNumber) && !Number.isNaN(rightNumber)) {
        return leftNumber - rightNumber;
    }
    return String(leftValue).localeCompare(String(rightValue));
}

function instanceLabel(entity, row) {
    const primary = primaryValue(entity, row);
    const display = displayValue(entity, row);
    if (primary && display) {
        return `${primary} - ${display}`;
    }
    return primary || display || "(unnamed)";
}

function displayValue(entity, row) {
    const preferred = entity.fields.find(field =>
        field.name !== entity.primaryKey && ["title", "name"].includes(field.name.toLowerCase()));
    const fallback = entity.fields.find(field =>
        field.name !== entity.primaryKey && valueText(row[field.name]).length > 0);
    const field = preferred || fallback;
    return field ? valueText(row[field.name]) : "";
}

function entityNodeKey(entity) {
    return `entity:${entity.name}`;
}

function instanceNodeKey(entity, row) {
    return `instance:${entity.name}:${primaryValue(entity, row)}`;
}

function relationshipNodeKey(entity, row, relationship) {
    return `relationship:${entity.name}:${primaryValue(entity, row)}:${relationship.name}`;
}

function expandRelationshipContext() {
    const context = state.relationshipContext;
    if (!context) {
        return;
    }
    state.expandedNodes[entityNodeKey(context.entity)] = true;
    state.expandedNodes[instanceNodeKey(context.entity, context.sourceRow)] = true;
    state.expandedNodes[relationshipNodeKey(context.entity, context.sourceRow, context.relationship)] = true;
}

function activeWorkspaceVersion() {
    return state.workspace ? state.workspace.workspaceVersion : null;
}

function workspaceChangedSince(version) {
    return version !== null
        && state.workspace
        && state.workspace.workspaceVersion !== version;
}

function isExpanded(key) {
    return state.expandedNodes[key] === true;
}

function toggleExpanded(key) {
    state.expandedNodes[key] = !isExpanded(key);
}

function valueText(value) {
    if (value === undefined || value === null) {
        return "";
    }
    if (typeof value === "object") {
        return JSON.stringify(value);
    }
    return String(value);
}

function entityByName(name) {
    return state.workspace.entities.find(entity => entity.name === name);
}

function showMessage(text, error = false) {
    if (!els.message) {
        return;
    }
    els.message.textContent = text;
    els.message.className = error ? "message-bar error" : "message-bar";
    els.message.hidden = false;
}

function clearMessage() {
    if (!els.message) {
        return;
    }
    els.message.textContent = "";
    els.message.hidden = true;
}

function openProjectDialog(action) {
    if (!els.projectDialog) {
        return;
    }
    state.projectDialogAction = action;
    const isSave = action === "save";
    const currentPath = state.workspace && state.workspace.project
        ? state.workspace.project.path || ""
        : "";
    els.projectDialogTitle.textContent = isSave ? "Save Project" : "Load Project";
    els.projectDialogDescription.textContent = isSave
        ? "Save the active schema and data to a server-side project folder."
        : "Load schema and data from a server-side project folder or projectfile.erproj.";
    els.projectDialogWarning.textContent = isSave
        ? "Saving overwrites projectfile.erproj, schema.yaml, and data.json in the selected folder. Other files are left alone."
        : "Loading a project replaces the current workspace schema and data.";
    els.projectDialogConfirm.textContent = isSave ? "Save Project" : "Load Project";
    els.projectPathInput.value = currentPath;
    els.projectDialog.hidden = false;
    els.projectPathInput.focus();
}

function closeProjectDialog() {
    if (!els.projectDialog) {
        return;
    }
    state.projectDialogAction = null;
    els.projectDialog.hidden = true;
}

async function submitProjectDialog() {
    if (!state.projectDialogAction || !els.projectPathInput) {
        return;
    }
    const path = els.projectPathInput.value.trim();
    if (!path) {
        showMessage("Enter a project folder path.", true);
        return;
    }
    const action = state.projectDialogAction;
    const endpoint = action === "save" ? "/ui/project/save" : "/ui/project/load";
    const workspace = await requestJson(endpoint, {
        method: "POST",
        body: JSON.stringify({path})
    });
    state.workspace = workspace;
    state.schemaDraft = null;
    state.schemaPreview = null;
    closeProjectDialog();
    await loadWorkspace();
    showMessage(action === "save" ? "Project saved." : "Project loaded.");
}

function markSchemaDirty() {
    if (state.mode !== "schema") {
        return;
    }
    state.schemaDirty = true;
    state.schemaUpgradePreview = null;
    renderSchemaDirtyStatus();
    renderSchemaUpgradePanel();
}

function markSchemaClean() {
    state.schemaDirty = false;
    renderSchemaDirtyStatus();
}

function renderSchemaDirtyStatus() {
    if (!els.schemaDirtyStatus) {
        return;
    }
    els.schemaDirtyStatus.hidden = !state.schemaDirty;
}

function confirmSchemaSaveBefore(actionText) {
    if (!state.schemaDirty) {
        return true;
    }
    if (!state.schemaPreview || state.schemaPreview.valid !== true) {
        const discard = window.confirm(
            `Schema draft has unsaved changes but is not valid enough to save. Continue ${actionText} and discard them?`);
        if (discard) {
            markSchemaClean();
        }
        return discard;
    }
    if (!window.confirm(`Schema draft has unsaved changes. Save as YAML before ${actionText}?`)) {
        return false;
    }
    if (downloadSchemaOutput("yaml")) {
        markSchemaClean();
        return true;
    }
    return false;
}

async function switchMode(mode) {
    window.location.href = mode === "schema" ? "/schema" : "/";
}

async function initializeSchemaEditor(force) {
    if (!state.workspace) {
        await loadWorkspace();
    }
    if (force || !state.schemaDraft) {
        await parseSchemaYaml(state.workspace.schemaYaml || "", true);
    } else {
        renderSchemaEditor();
        renderSchemaPreview();
    }
}

async function parseSchemaYaml(yamlText, updateInput) {
    const preview = await requestJson("/ui/schema/from-yaml", {method: "POST", body: yamlText});
    state.schemaSelection = {type: "model"};
    state.schemaUpgradePreview = null;
    state.schemaUpgradeMappings = {entityMappings: {}, fieldMappings: {}, relationshipMappings: []};
    applySchemaPreview(preview);
    if (updateInput) {
        els.schemaYamlInput.value = preview.yaml || yamlText;
        markSchemaClean();
    } else {
        markSchemaDirty();
    }
    renderSchemaEditor();
}

async function previewSchemaDraft() {
    if (!state.schemaDraft) {
        return;
    }
    const preview = await requestJson("/ui/schema/preview", {
        method: "POST",
        body: JSON.stringify(state.schemaDraft)
    });
    applySchemaPreview(preview);
}

function scheduleSchemaPreview() {
    markSchemaDirty();
    clearTimeout(state.schemaPreviewTimer);
    state.schemaPreviewTimer = setTimeout(() => {
        previewSchemaDraft().catch(error => showMessage(error.message, true));
    }, 250);
}

function applySchemaPreview(preview) {
    state.schemaPreview = preview;
    state.schemaDraft = preview.draft || state.schemaDraft;
    renderSchemaPreview();
    renderSchemaUpgradePanel();
}

function renderSchemaEditor() {
    renderSchemaDirtyStatus();
    if (!state.schemaDraft) {
        els.schemaTree.innerHTML = "";
        els.schemaDetailHost.innerHTML = `<div class="empty-state">No schema draft loaded.</div>`;
        return;
    }
    state.schemaDraft.entities = state.schemaDraft.entities || [];
    state.schemaDraft.relationships = state.schemaDraft.relationships || [];
    ensureSchemaSelection();
    renderSchemaTree();
    renderSchemaDetail();
    renderSchemaPanelVisibility();
    renderSchemaUpgradePanel();
    initializeSchemaHelp();
}

function ensureSchemaSelection() {
    if (!state.schemaSelection) {
        state.schemaSelection = {type: "model"};
    }
    const selection = state.schemaSelection;
    if (selection.type === "entity" && !state.schemaDraft.entities[selection.entityIndex]) {
        state.schemaSelection = {type: "model"};
    }
    if (selection.type === "field") {
        const entity = state.schemaDraft.entities[selection.entityIndex];
        if (!entity || !fieldAt(entity, selection.path)) {
            state.schemaSelection = entity ? {type: "entity", entityIndex: selection.entityIndex} : {type: "model"};
        }
    }
    if (selection.type === "relationship" && !state.schemaDraft.relationships[selection.relationshipIndex]) {
        state.schemaSelection = {type: "model"};
    }
}

function renderSchemaTree() {
    els.schemaTree.innerHTML = "";
    els.schemaTree.appendChild(schemaTreeNode("Model", {type: "model"}, "title"));
    const entities = schemaTreeSection("Entities");
    state.schemaDraft.entities.forEach((entity, entityIndex) => {
        entities.appendChild(schemaTreeNode(entity.name || `entity ${entityIndex + 1}`, {type: "entity", entityIndex}, entity.plural || ""));
        const fieldGroup = document.createElement("div");
        fieldGroup.className = "schema-tree-children";
        renderSchemaFieldTree(fieldGroup, entityIndex, entity.fields || [], []);
        entities.appendChild(fieldGroup);
    });
    els.schemaTree.appendChild(entities);

    const relationships = schemaTreeSection("Relationships", () => {
        const relationship = newRelationshipDraft();
        state.schemaDraft.relationships.push(relationship);
        state.schemaSelection = {type: "relationship", relationshipIndex: state.schemaDraft.relationships.length - 1};
        renderSchemaEditor();
        scheduleSchemaPreview();
    });
    state.schemaDraft.relationships.forEach((relationship, relationshipIndex) => {
        relationships.appendChild(schemaTreeNode(
            relationship.name || `relationship ${relationshipIndex + 1}`,
            {type: "relationship", relationshipIndex},
            `${relationship.from || "?"} -> ${relationship.to || "?"}`));
    });
    els.schemaTree.appendChild(relationships);
}

function renderSchemaFieldTree(container, entityIndex, fields, parentPath) {
    fields.forEach((field, fieldIndex) => {
        const path = [...parentPath, fieldIndex];
        container.appendChild(schemaTreeNode(
            field.name || `field ${fieldIndex + 1}`,
            {type: "field", entityIndex, path},
            field.type || ""));
        if (field.type === "object" && Array.isArray(field.objectFields) && field.objectFields.length > 0) {
            const children = document.createElement("div");
            children.className = "schema-tree-children";
            renderSchemaFieldTree(children, entityIndex, field.objectFields, path);
            container.appendChild(children);
        }
    });
}

function schemaTreeSection(title, addAction) {
    const section = document.createElement("section");
    section.className = "schema-tree-section";
    const heading = document.createElement("div");
    heading.className = "schema-tree-heading";
    const text = document.createElement("span");
    text.textContent = title;
    heading.appendChild(text);
    if (addAction) {
        heading.appendChild(schemaActionButton("Add", addAction, "compact-button", `Add a ${title.toLowerCase().slice(0, -1)} definition.`));
    }
    section.appendChild(heading);
    return section;
}

function schemaTreeNode(label, selection, meta) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `schema-tree-node ${schemaSelectionMatches(selection) ? "active" : ""}`;
    const labelSpan = document.createElement("span");
    labelSpan.className = "tree-node-label";
    labelSpan.textContent = label;
    button.appendChild(labelSpan);
    const metaSpan = document.createElement("span");
    metaSpan.className = "tree-count";
    metaSpan.textContent = meta || "";
    button.appendChild(metaSpan);
    button.addEventListener("click", () => {
        state.schemaSelection = selection;
        renderSchemaEditor();
    });
    return button;
}

function schemaSelectionMatches(selection) {
    return schemaSelectionKey(selection) === schemaSelectionKey(state.schemaSelection);
}

function schemaSelectionKey(selection) {
    if (!selection) {
        return "";
    }
    if (selection.type === "field") {
        return `${selection.type}:${selection.entityIndex}:${selection.path.join(".")}`;
    }
    if (selection.type === "entity") {
        return `${selection.type}:${selection.entityIndex}`;
    }
    if (selection.type === "relationship") {
        return `${selection.type}:${selection.relationshipIndex}`;
    }
    return selection.type;
}

function renderSchemaDetail() {
    els.schemaDetailHost.innerHTML = "";
    const selection = state.schemaSelection;
    if (selection.type === "entity") {
        renderEntityDetail(selection.entityIndex);
    } else if (selection.type === "field") {
        renderFieldDetail(selection.entityIndex, selection.path);
    } else if (selection.type === "relationship") {
        renderRelationshipDetail(selection.relationshipIndex);
    } else {
        renderModelDetail();
    }
    initializeSchemaHelp();
}

function renderModelDetail() {
    els.schemaDetailTitle.textContent = "Model";
    const model = state.schemaDraft.model || {title: "", description: ""};
    state.schemaDraft.model = model;
    const card = schemaCard("Model Details");
    card.appendChild(schemaTextControl("Title", model.title, value => {
        model.title = value;
        scheduleSchemaPreview();
    }, "text", "The display name exported in YAML and shown by the workspace."));
    card.appendChild(schemaTextControl("Description", model.description, value => {
        model.description = value;
        scheduleSchemaPreview();
    }, "textarea", "A short explanation of what this model represents."));
    els.schemaDetailHost.appendChild(wrapSchemaDetail(card, true));
}

function renderEntityDetail(entityIndex) {
    const entity = state.schemaDraft.entities[entityIndex];
    els.schemaDetailTitle.textContent = `Entity: ${entity.name || entityIndex + 1}`;
    const details = schemaCard("Entity Details");
    const actions = schemaInlineActions();
    actions.appendChild(schemaActionButton("Delete Entity", () => {
        state.schemaDraft.entities.splice(entityIndex, 1);
        state.schemaSelection = {type: "model"};
        renderSchemaEditor();
        scheduleSchemaPreview();
    }, "compact-button danger", "Remove this entity from the draft. Relationships that used it will become invalid until updated."));
    details.appendChild(actions);
    details.appendChild(schemaTextControl("Name", entity.name, value => {
        entity.name = value;
        scheduleSchemaPreview();
        renderSchemaTree();
    }, "text", "The singular entity name used in schema definitions."));
    details.appendChild(schemaTextControl("Plural", entity.plural, value => {
        entity.plural = value;
        scheduleSchemaPreview();
        renderSchemaTree();
    }, "text", "The collection name used by the API path and generated schema."));
    details.appendChild(schemaTextControl("Max Instances", entity.maxInstances, value => {
        entity.maxInstances = numberOrDefault(value, -1);
        scheduleSchemaPreview();
    }, "number", "Maximum allowed instances. Use -1 for unlimited."));
    details.appendChild(schemaTextControl("Primary Key", entity.primaryKey, value => {
        entity.primaryKey = value;
        scheduleSchemaPreview();
    }, "text", "The field used to identify instances in API routes."));

    const fields = schemaCard("Fields");
    const fieldActions = schemaInlineActions();
    fieldActions.appendChild(schemaActionButton("Add Field", () => addFieldTo(entityIndex, []), "compact-button", "Add a field to this entity."));
    fields.appendChild(fieldActions);
    fields.appendChild(fieldSummaryTable(entityIndex, [], entity.fields || []));
    els.schemaDetailHost.appendChild(wrapSchemaDetail(details, false, fields));
}

function fieldSummaryTable(entityIndex, parentPath, fields) {
    const table = document.createElement("table");
    table.className = "schema-list-table";
    table.innerHTML = "<thead><tr><th>Name</th><th>Type</th><th>Required</th><th>Actions</th></tr></thead>";
    const body = document.createElement("tbody");
    fields.forEach((field, fieldIndex) => {
        const path = [...parentPath, fieldIndex];
        const row = document.createElement("tr");
        row.appendChild(schemaTableCell(field.name || "(unnamed)"));
        row.appendChild(schemaTableCell(field.type || ""));
        row.appendChild(schemaTableCell(field.required ? "yes" : "no"));
        const actions = document.createElement("td");
        actions.appendChild(schemaActionButton("Edit", () => {
            state.schemaSelection = {type: "field", entityIndex, path};
            renderSchemaEditor();
        }, "compact-button", "Edit this field in the detail panel."));
        actions.appendChild(schemaActionButton("Delete", () => deleteField(entityIndex, path), "compact-button danger", "Remove this field from the draft."));
        row.appendChild(actions);
        body.appendChild(row);
    });
    if (fields.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");
        cell.colSpan = 4;
        cell.className = "empty-state";
        cell.textContent = "No fields yet.";
        row.appendChild(cell);
        body.appendChild(row);
    }
    table.appendChild(body);
    return table;
}

function schemaTableCell(text) {
    const cell = document.createElement("td");
    cell.textContent = text;
    return cell;
}

function renderFieldDetail(entityIndex, path) {
    const entity = state.schemaDraft.entities[entityIndex];
    const field = fieldAt(entity, path);
    els.schemaDetailTitle.textContent = `Field: ${field.name || path.join(".")}`;
    const details = schemaCard("Field Details");
    const actions = schemaInlineActions();
    actions.appendChild(schemaActionButton("Back to Entity", () => {
        state.schemaSelection = {type: "entity", entityIndex};
        renderSchemaEditor();
    }, "compact-button", "Return to the entity field list."));
    actions.appendChild(schemaActionButton("Delete Field", () => deleteField(entityIndex, path), "compact-button danger", "Remove this field from the draft."));
    details.appendChild(actions);
    details.appendChild(schemaTextControl("Name", field.name, value => {
        field.name = value;
        scheduleSchemaPreview();
        renderSchemaTree();
    }, "text", "The field name used in YAML and API payloads."));
    details.appendChild(schemaSelectControl("Type", field.type, fieldTypes(), value => {
        field.type = value;
        if (value === "object") {
            field.objectFields = field.objectFields || [];
        }
        renderSchemaEditor();
        scheduleSchemaPreview();
    }, "The field data type."));
    details.appendChild(schemaCheckboxControl("Required", field.required, value => {
        field.required = value;
        scheduleSchemaPreview();
    }, "Whether API clients must provide this field."));
    details.appendChild(schemaCheckboxControl("Unique", field.unique, value => {
        field.unique = value;
        scheduleSchemaPreview();
    }, "Whether values must be unique across instances."));
    details.appendChild(schemaTextControl("Default", field.defaultValue, value => {
        field.defaultValue = emptyToNull(value);
        scheduleSchemaPreview();
    }, "text", "Default value applied when the field is not supplied."));
    details.appendChild(schemaTextControl("Description", field.description, value => {
        field.description = emptyToNull(value);
        scheduleSchemaPreview();
    }, "textarea", "Documentation text for this field."));
    details.appendChild(schemaTextControl("Examples", (field.examples || []).join(", "), value => {
        field.examples = value.split(",").map(item => item.trim()).filter(item => item.length > 0);
        scheduleSchemaPreview();
    }, "text", "Comma-separated example values."));
    details.appendChild(schemaTextControl("Truncate To", field.truncateTo, value => {
        field.truncateTo = numberOrNull(value);
        scheduleSchemaPreview();
    }, "number", "Maximum stored length after truncation."));
    details.appendChild(schemaTextControl("Min", field.min, value => {
        field.min = emptyToNull(value);
        scheduleSchemaPreview();
    }, "text", "Minimum value or length, depending on field type."));
    details.appendChild(schemaTextControl("Max", field.max, value => {
        field.max = emptyToNull(value);
        scheduleSchemaPreview();
    }, "text", "Maximum value or length, depending on field type."));

    const supporting = schemaCard("Validation And Object Fields");
    supporting.appendChild(validationListEditor(field.validations || (field.validations = [])));
    if (field.type === "object") {
        const childPath = path;
        const childFields = field.objectFields || (field.objectFields = []);
        const childActions = schemaInlineActions();
        childActions.appendChild(schemaActionButton("Add Object Field", () => addFieldTo(entityIndex, childPath), "compact-button", "Add a child field to this object field."));
        supporting.appendChild(childActions);
        supporting.appendChild(fieldSummaryTable(entityIndex, childPath, childFields));
    }
    els.schemaDetailHost.appendChild(wrapSchemaDetail(details, false, supporting));
}

function validationListEditor(validations) {
    const section = document.createElement("div");
    section.className = "schema-list";
    const heading = document.createElement("div");
    heading.className = "schema-list-heading";
    heading.textContent = "Validations";
    section.appendChild(heading);
    section.appendChild(schemaActionButton("Add Validation", () => {
        validations.push({type: "notEmpty", value: null});
        renderSchemaEditor();
        scheduleSchemaPreview();
    }, "compact-button", "Add a validation rule to the selected field."));
    validations.forEach((validation, index) => {
        const row = document.createElement("div");
        row.className = "schema-validation-row";
        row.appendChild(schemaSelectControl("", validation.type, validationTypes(), value => {
            validation.type = value;
            validation.value = value === "notEmpty" ? null : (validation.value || "");
            renderSchemaEditor();
            scheduleSchemaPreview();
        }, "The validation rule type."));
        row.appendChild(schemaTextControl("", validation.value, value => {
            validation.value = emptyToNull(value);
            scheduleSchemaPreview();
        }, "text", "Rule value, such as maximum length or regex text."));
        row.appendChild(schemaActionButton("Remove", () => {
            validations.splice(index, 1);
            renderSchemaEditor();
            scheduleSchemaPreview();
        }, "compact-button danger", "Remove this validation rule."));
        section.appendChild(row);
    });
    return section;
}

function renderRelationshipDetail(relationshipIndex) {
    const relationship = state.schemaDraft.relationships[relationshipIndex];
    els.schemaDetailTitle.textContent = `Relationship: ${relationship.name || relationshipIndex + 1}`;
    const details = schemaCard("Relationship Details");
    const entityOptions = state.schemaDraft.entities.map(entity => entity.name);
    const actions = schemaInlineActions();
    actions.appendChild(schemaActionButton("Delete Relationship", () => {
        state.schemaDraft.relationships.splice(relationshipIndex, 1);
        state.schemaSelection = {type: "model"};
        renderSchemaEditor();
        scheduleSchemaPreview();
    }, "compact-button danger", "Remove this relationship from the draft."));
    details.appendChild(actions);
    details.appendChild(schemaSelectControl("From", relationship.from, entityOptions, value => {
        relationship.from = value;
        scheduleSchemaPreview();
        renderSchemaTree();
    }, "The source entity for this relationship."));
    details.appendChild(schemaTextControl("Name", relationship.name, value => {
        relationship.name = value;
        scheduleSchemaPreview();
        renderSchemaTree();
    }, "text", "The relationship name exposed under the source entity."));
    details.appendChild(schemaSelectControl("To", relationship.to, entityOptions, value => {
        relationship.to = value;
        scheduleSchemaPreview();
        renderSchemaTree();
    }, "The target entity for this relationship."));
    details.appendChild(schemaSelectControl("Cardinality", relationship.cardinality, cardinalities(), value => {
        relationship.cardinality = value;
        scheduleSchemaPreview();
    }, "How many target instances can be related."));
    details.appendChild(schemaSelectControl("Optionality", relationship.optionality, optionalities(), value => {
        relationship.optionality = value;
        scheduleSchemaPreview();
    }, "Whether this relationship is optional or mandatory."));
    details.appendChild(schemaCheckboxControl("Reverse", Boolean(relationship.reverse), value => {
        relationship.reverse = value ? {name: "", cardinality: "one-to-one", optionality: "optional"} : null;
        renderSchemaEditor();
        scheduleSchemaPreview();
    }, "Generate a reverse relationship from target back to source."));
    if (relationship.reverse) {
        details.appendChild(schemaTextControl("Reverse Name", relationship.reverse.name, value => {
            relationship.reverse.name = value;
            scheduleSchemaPreview();
        }, "text", "The reverse relationship name."));
        details.appendChild(schemaSelectControl("Reverse Cardinality", relationship.reverse.cardinality, cardinalities(), value => {
            relationship.reverse.cardinality = value;
            scheduleSchemaPreview();
        }, "Cardinality for the reverse relationship."));
        details.appendChild(schemaSelectControl("Reverse Optionality", relationship.reverse.optionality, optionalities(), value => {
            relationship.reverse.optionality = value;
            scheduleSchemaPreview();
        }, "Optionality for the reverse relationship."));
    }
    els.schemaDetailHost.appendChild(wrapSchemaDetail(details, true));
}

function wrapSchemaDetail(primary, single, secondary) {
    const layout = document.createElement("div");
    layout.className = `schema-detail-grid ${single ? "single" : ""}`;
    layout.appendChild(primary);
    if (secondary) {
        layout.appendChild(secondary);
    }
    return layout;
}

function schemaCard(title) {
    const card = document.createElement("section");
    card.className = "schema-card";
    const heading = document.createElement("h3");
    heading.textContent = title;
    card.appendChild(heading);
    return card;
}

function addFieldTo(entityIndex, parentPath) {
    const entity = state.schemaDraft.entities[entityIndex];
    const fields = fieldsAt(entity, parentPath);
    fields.push(newFieldDraft());
    state.schemaSelection = {type: "field", entityIndex, path: [...parentPath, fields.length - 1]};
    renderSchemaEditor();
    scheduleSchemaPreview();
}

function deleteField(entityIndex, path) {
    const entity = state.schemaDraft.entities[entityIndex];
    const parentPath = path.slice(0, -1);
    const fields = fieldsAt(entity, parentPath);
    fields.splice(path[path.length - 1], 1);
    state.schemaSelection = {type: "entity", entityIndex};
    renderSchemaEditor();
    scheduleSchemaPreview();
}

function fieldsAt(entity, parentPath) {
    if (parentPath.length === 0) {
        entity.fields = entity.fields || [];
        return entity.fields;
    }
    const parent = fieldAt(entity, parentPath);
    parent.objectFields = parent.objectFields || [];
    return parent.objectFields;
}

function fieldAt(entity, path) {
    let fields = entity.fields || [];
    let field = null;
    for (const index of path) {
        field = fields[index];
        if (!field) {
            return null;
        }
        fields = field.objectFields || [];
    }
    return field;
}

function renderSchemaPreview() {
    const preview = state.schemaPreview;
    if (!preview) {
        return;
    }
    const mermaidSource = mermaidSourceForCurrentLayout(preview.mermaid || "");
    renderSchemaValidation(preview);
    if (els.schemaCanonicalYaml) {
        els.schemaCanonicalYaml.value = preview.yaml || "";
    }
    if (els.schemaMermaidOutput) {
        els.schemaMermaidOutput.value = mermaidSource;
    }
    if (els.schemaGraphvizOutput) {
        els.schemaGraphvizOutput.value = preview.graphviz || "";
    }
    const valid = preview.valid === true;
    [els.schemaSaveYaml, els.schemaDownloadYaml, els.schemaDownloadMermaid, els.schemaDownloadGraphviz,
        els.schemaCopyYaml, els.schemaCopyMermaid, els.schemaCopyGraphviz]
        .filter(Boolean)
        .forEach(button => {
            button.disabled = !valid;
        });
    renderSchemaDiagramControls();
    renderMermaidDiagram(mermaidSource);
}

function renderSchemaValidation(preview) {
    if (!els.schemaValidation) {
        return;
    }
    els.schemaValidation.innerHTML = "";
    const status = document.createElement("div");
    status.className = preview.valid ? "schema-valid" : "schema-invalid";
    status.textContent = preview.valid ? "Schema is valid." : "Schema has validation errors.";
    els.schemaValidation.appendChild(status);
    if (Array.isArray(preview.errors) && preview.errors.length > 0) {
        const list = document.createElement("ul");
        preview.errors.forEach(error => {
            const item = document.createElement("li");
            item.textContent = `${error.path || "schema"}: ${error.message}`;
            list.appendChild(item);
        });
        els.schemaValidation.appendChild(list);
    }
}

async function previewSchemaUpgrade() {
    if (!state.schemaDraft) {
        return;
    }
    if (!state.schemaUpgradeDialogOpen) {
        openSchemaUpgradeDialog();
    }
    const preview = await requestJson("/ui/schema/upgrade/preview", {
        method: "POST",
        body: JSON.stringify(schemaUpgradeRequest(true))
    });
    state.schemaUpgradePreview = preview;
    if (preview.mappings) {
        mergeEffectiveUpgradeMappings(preview.mappings);
    }
    renderSchemaUpgradePanel();
}

async function startSchemaUpgradeWorkflow() {
    if (!state.schemaDraft || !state.workspace) {
        showMessage("Load a schema draft before applying it to the workspace.", true);
        return;
    }
    if (!state.schemaPreview || state.schemaPreview.valid !== true) {
        await previewSchemaDraft();
    }
    if (!state.schemaPreview || state.schemaPreview.valid !== true) {
        showMessage("Fix schema validation errors before applying to the workspace.", true);
        return;
    }
    openSchemaUpgradeDialog();
    await previewSchemaUpgrade();
}

function openSchemaUpgradeDialog() {
    state.schemaUpgradeDialogOpen = true;
    if (els.schemaUpgradeDialog) {
        els.schemaUpgradeDialog.hidden = false;
    }
    renderSchemaUpgradePanel();
}

function closeSchemaUpgradeDialog() {
    state.schemaUpgradeDialogOpen = false;
    if (els.schemaUpgradeDialog) {
        els.schemaUpgradeDialog.hidden = true;
    }
    renderSchemaUpgradePanel();
}

async function applySchemaUpgrade() {
    if (!state.schemaUpgradePreview || !state.schemaUpgradePreview.canApply) {
        await previewSchemaUpgrade();
    }
    if (!state.schemaUpgradePreview || !state.schemaUpgradePreview.canApply) {
        showMessage("Schema upgrade preview has blocking errors.", true);
        return;
    }
    const response = await requestJson("/ui/schema/upgrade/apply", {
        method: "POST",
        body: JSON.stringify(schemaUpgradeRequest(true))
    });
    state.schemaUpgradePreview = response;
    if (response.workspace) {
        state.workspace = response.workspace;
    } else {
        await loadWorkspace();
    }
    markSchemaClean();
    renderHeader();
    renderSchemaUpgradePanel();
    closeSchemaUpgradeDialog();
    showMessage("Schema upgrade applied to workspace.");
}

function schemaUpgradeRequest(includeExpectedVersion) {
    const request = {
        draft: state.schemaDraft,
        mappings: state.schemaUpgradeMappings
    };
    if (includeExpectedVersion && state.workspace) {
        request.expectedWorkspaceVersion = state.workspace.workspaceVersion;
    }
    return request;
}

function mergeEffectiveUpgradeMappings(mappings) {
    state.schemaUpgradeMappings.entityMappings = {
        ...(mappings.entityMappings || {}),
        ...(state.schemaUpgradeMappings.entityMappings || {})
    };
    state.schemaUpgradeMappings.fieldMappings = {
        ...(mappings.fieldMappings || {}),
        ...(state.schemaUpgradeMappings.fieldMappings || {})
    };
    state.schemaUpgradeMappings.relationshipMappings =
        state.schemaUpgradeMappings.relationshipMappings.length > 0
            ? state.schemaUpgradeMappings.relationshipMappings
            : (mappings.relationshipMappings || []);
}

function renderSchemaUpgradePanel() {
    setApplyWorkspaceEnabled(Boolean(state.schemaDraft && state.workspace
        && state.schemaPreview && state.schemaPreview.valid === true));
    if (!els.schemaUpgradeHost || !state.schemaUpgradeDialogOpen) {
        setConfirmWorkspaceEnabled(false);
        return;
    }
    els.schemaUpgradeHost.innerHTML = "";
    if (!state.schemaDraft || !state.workspace) {
        els.schemaUpgradeHost.innerHTML = `<div class="empty-state">Load a schema draft before previewing an upgrade.</div>`;
        setConfirmWorkspaceEnabled(false);
        return;
    }
    const layout = document.createElement("div");
    layout.className = "schema-upgrade-layout";
    layout.appendChild(upgradeMappingPanel());
    layout.appendChild(upgradeReportPanel());
    els.schemaUpgradeHost.appendChild(layout);
    const canApply = Boolean(state.schemaUpgradePreview && state.schemaUpgradePreview.canApply)
        && state.schemaUpgradePreview.workspaceVersion === state.workspace.workspaceVersion;
    setConfirmWorkspaceEnabled(canApply);
    initializeSchemaHelp();
}

function upgradeMappingPanel() {
    const panel = document.createElement("div");
    panel.className = "schema-upgrade-card";
    panel.appendChild(sectionHeading("Migration Mappings"));
    panel.appendChild(upgradeEntityMappings());
    panel.appendChild(upgradeFieldMappings());
    panel.appendChild(upgradeRelationshipMappings());
    return panel;
}

function upgradeReportPanel() {
    const panel = document.createElement("div");
    panel.className = "schema-upgrade-card";
    panel.appendChild(sectionHeading("Upgrade Preview"));
    const preview = state.schemaUpgradePreview;
    if (!preview) {
        const empty = document.createElement("div");
        empty.className = "empty-state";
        empty.textContent = "Refresh Preview to see added, dropped, mapped, and coerced data.";
        panel.appendChild(empty);
        return panel;
    }
    const status = document.createElement("div");
    status.className = preview.canApply ? "schema-valid" : "schema-invalid";
    status.textContent = preview.canApply
        ? "Upgrade can be applied."
        : "Upgrade has blocking errors or stale workspace data.";
    panel.appendChild(status);
    panel.appendChild(upgradeSummary(preview.summary || {}));
    panel.appendChild(upgradeList("Warnings", preview.warnings || [], "warning"));
    panel.appendChild(upgradeObjectList("Errors", preview.errors || [], item => `${item.path || "migration"}: ${item.message}`));
    panel.appendChild(upgradeObjectList("Value assignments", preview.valueAssignments || [], item =>
        `${item.entity || ""} ${item.identifier || ""} ${item.field || ""}: ${item.to || ""} (${item.reason || ""})`));
    panel.appendChild(upgradeObjectList("Coercions", preview.coercions || [], item =>
        `${item.entity || ""} ${item.identifier || ""} ${item.field || ""}: ${item.from || ""} -> ${item.to || ""} (${item.reason || ""})`));
    return panel;
}

function upgradeEntityMappings() {
    const group = document.createElement("div");
    group.className = "schema-upgrade-group";
    group.appendChild(sectionHeading("Entities", "Map a target entity to an existing source entity when it was renamed."));
    (state.schemaDraft.entities || []).forEach(targetEntity => {
        group.appendChild(upgradeSelectRow(
            targetEntity.name,
            currentEntityMapping(targetEntity.name),
            ["", ...state.workspace.entities.map(entity => entity.name)],
            "(new entity)",
            value => {
                if (value) {
                    state.schemaUpgradeMappings.entityMappings[targetEntity.name] = value;
                } else {
                    delete state.schemaUpgradeMappings.entityMappings[targetEntity.name];
                }
                state.schemaUpgradePreview = null;
                renderSchemaUpgradePanel();
            }));
    });
    return group;
}

function upgradeFieldMappings() {
    const group = document.createElement("div");
    group.className = "schema-upgrade-group";
    group.appendChild(sectionHeading("Fields", "Top-level field mappings preserve data when a field was renamed."));
    (state.schemaDraft.entities || []).forEach(targetEntity => {
        const sourceEntity = workspaceEntityByName(currentEntityMapping(targetEntity.name));
        if (!sourceEntity) {
            return;
        }
        const title = document.createElement("h4");
        title.textContent = targetEntity.name;
        group.appendChild(title);
        (targetEntity.fields || []).forEach(targetField => {
            const fieldMappings = state.schemaUpgradeMappings.fieldMappings[targetEntity.name] || {};
            const exact = sourceEntity.fields.some(field => field.name === targetField.name) ? targetField.name : "";
            const current = fieldMappings[targetField.name] || exact;
            group.appendChild(upgradeSelectRow(
                targetField.name,
                current,
                ["", ...sourceEntity.fields.map(field => field.name)],
                "(new field: use default/fallback)",
                value => {
                    if (!state.schemaUpgradeMappings.fieldMappings[targetEntity.name]) {
                        state.schemaUpgradeMappings.fieldMappings[targetEntity.name] = {};
                    }
                    if (value) {
                        state.schemaUpgradeMappings.fieldMappings[targetEntity.name][targetField.name] = value;
                    } else {
                        delete state.schemaUpgradeMappings.fieldMappings[targetEntity.name][targetField.name];
                    }
                    state.schemaUpgradePreview = null;
                    renderSchemaUpgradePanel();
                }));
        });
    });
    return group;
}

function upgradeRelationshipMappings() {
    const group = document.createElement("div");
    group.className = "schema-upgrade-group";
    group.appendChild(sectionHeading("Relationships", "Map target relationships to existing relationship edges."));
    const sourceRelationships = state.workspace.relationships || [];
    (state.schemaDraft.relationships || []).forEach(targetRelationship => {
        const current = currentRelationshipMapping(targetRelationship);
        group.appendChild(upgradeSelectRow(
            `${targetRelationship.from}.${targetRelationship.name}`,
            current ? `${current.sourceFromEntity}|${current.sourceName}` : "",
            ["", ...sourceRelationships.map(relationship => `${relationship.fromEntity}|${relationship.name}`)],
            "(new relationship)",
            value => {
                setRelationshipMapping(targetRelationship, value);
                state.schemaUpgradePreview = null;
                renderSchemaUpgradePanel();
            }));
    });
    return group;
}

function upgradeSelectRow(labelText, value, options, emptyLabel, onChange) {
    const row = document.createElement("label");
    row.className = "schema-upgrade-row";
    const label = document.createElement("span");
    label.textContent = labelText;
    row.appendChild(label);
    const select = document.createElement("select");
    options.forEach(optionValue => {
        const option = document.createElement("option");
        option.value = optionValue;
        option.textContent = optionValue || emptyLabel;
        option.selected = optionValue === value;
        select.appendChild(option);
    });
    select.addEventListener("change", () => onChange(select.value));
    row.appendChild(select);
    return row;
}

function upgradeSummary(summary) {
    const list = document.createElement("dl");
    list.className = "schema-upgrade-summary";
    ["sourceEntities", "targetEntities", "migratedInstances", "preservedEdges", "droppedEdges"].forEach(key => {
        const term = document.createElement("dt");
        term.textContent = key;
        const value = document.createElement("dd");
        value.textContent = valueText(summary[key] || 0);
        list.appendChild(term);
        list.appendChild(value);
    });
    return list;
}

function upgradeList(title, items) {
    const section = document.createElement("div");
    section.className = "schema-upgrade-list";
    section.appendChild(sectionHeading(title));
    if (items.length === 0) {
        const empty = document.createElement("p");
        empty.textContent = "None.";
        section.appendChild(empty);
        return section;
    }
    const list = document.createElement("ul");
    items.forEach(text => {
        const item = document.createElement("li");
        item.textContent = text;
        list.appendChild(item);
    });
    section.appendChild(list);
    return section;
}

function upgradeObjectList(title, items, renderer) {
    return upgradeList(title, items.map(renderer));
}

function sectionHeading(text, helpText = "") {
    const heading = document.createElement("h3");
    heading.textContent = text;
    if (helpText) {
        heading.appendChild(helpIcon(helpText));
    }
    return heading;
}

function currentEntityMapping(targetEntityName) {
    if (state.schemaUpgradeMappings.entityMappings[targetEntityName]) {
        return state.schemaUpgradeMappings.entityMappings[targetEntityName];
    }
    return workspaceEntityByName(targetEntityName) ? targetEntityName : "";
}

function currentRelationshipMapping(targetRelationship) {
    const manual = state.schemaUpgradeMappings.relationshipMappings.find(relationship =>
        relationship.targetFromEntity === targetRelationship.from
        && relationship.targetName === targetRelationship.name);
    if (manual) {
        return manual;
    }
    const sourceFrom = currentEntityMapping(targetRelationship.from);
    const exact = (state.workspace.relationships || []).find(relationship =>
        relationship.fromEntity === sourceFrom && relationship.name === targetRelationship.name);
    if (!exact) {
        return null;
    }
    return {
        targetFromEntity: targetRelationship.from,
        targetName: targetRelationship.name,
        sourceFromEntity: sourceFrom,
        sourceName: exact.name
    };
}

function setRelationshipMapping(targetRelationship, value) {
    state.schemaUpgradeMappings.relationshipMappings =
        state.schemaUpgradeMappings.relationshipMappings.filter(relationship =>
            relationship.targetFromEntity !== targetRelationship.from
            || relationship.targetName !== targetRelationship.name);
    if (!value) {
        return;
    }
    const [sourceFromEntity, sourceName] = value.split("|");
    state.schemaUpgradeMappings.relationshipMappings.push({
        targetFromEntity: targetRelationship.from,
        targetName: targetRelationship.name,
        sourceFromEntity,
        sourceName
    });
}

function workspaceEntityByName(name) {
    if (!state.workspace) {
        return null;
    }
    return state.workspace.entities.find(entity => entity.name === name);
}

function setApplyWorkspaceEnabled(enabled) {
    if (els.schemaApplyWorkspace) {
        els.schemaApplyWorkspace.disabled = !enabled;
    }
}

function setConfirmWorkspaceEnabled(enabled) {
    if (els.schemaUpgradeConfirm) {
        els.schemaUpgradeConfirm.disabled = !enabled;
    }
}

async function renderMermaidDiagram(source) {
    if (!els.schemaMermaidDiagram) {
        return;
    }
    if (!source.trim()) {
        els.schemaMermaidDiagram.textContent = "";
        applySchemaDiagramSizing();
        return;
    }
    if (!window.mermaid) {
        els.schemaMermaidDiagram.textContent = source;
        applySchemaDiagramSizing();
        return;
    }
    try {
        window.mermaid.initialize({startOnLoad: false, securityLevel: "strict"});
        const result = await window.mermaid.render(`schema-diagram-${Date.now()}`, source);
        els.schemaMermaidDiagram.innerHTML = result.svg || result;
    } catch (error) {
        els.schemaMermaidDiagram.textContent = source;
    }
    applySchemaDiagramSizing();
}

function renderSchemaPanelVisibility() {
    if (els.schemaDiagramContent) {
        els.schemaDiagramContent.hidden = !state.schemaDiagramVisible;
    }
    if (els.schemaYamlSection) {
        els.schemaYamlSection.hidden = !state.schemaYamlVisible;
    }
    if (els.schemaExportsSection) {
        els.schemaExportsSection.hidden = !state.schemaExportsVisible;
    }
    if (els.schemaDiagramResizer) {
        els.schemaDiagramResizer.hidden = !state.schemaDiagramVisible;
    }
    if (els.schemaToggleDiagram) {
        els.schemaToggleDiagram.textContent = state.schemaDiagramVisible ? "Hide Diagram" : "Show Diagram";
    }
    if (els.schemaToggleYaml) {
        els.schemaToggleYaml.textContent = state.schemaYamlVisible ? "Hide YAML Draft" : "YAML Draft";
    }
    if (els.schemaToggleExports) {
        els.schemaToggleExports.textContent = state.schemaExportsVisible ? "Hide Exports" : "Exports";
    }
    renderSchemaDiagramControls();
}

function renderSchemaDiagramControls() {
    if (els.schemaZoomReset) {
        els.schemaZoomReset.textContent = `${Math.round(state.schemaDiagramZoom * 100)}%`;
    }
    if (els.schemaLayoutToggle) {
        els.schemaLayoutToggle.textContent =
            state.schemaDiagramDirection === "TB" ? "Horizontal Layout" : "Vertical Layout";
    }
    if (els.schemaZoomOut) {
        els.schemaZoomOut.disabled = state.schemaDiagramZoom <= 0.5;
    }
    if (els.schemaZoomIn) {
        els.schemaZoomIn.disabled = state.schemaDiagramZoom >= 2;
    }
    applySchemaDiagramSizing();
}

function setSchemaDiagramZoom(value) {
    state.schemaDiagramZoom = Math.max(0.5, Math.min(2, Number(value.toFixed(2))));
    renderSchemaDiagramControls();
}

function toggleSchemaDiagramDirection() {
    state.schemaDiagramDirection = state.schemaDiagramDirection === "TB" ? "LR" : "TB";
    renderSchemaPreview();
}

function setSchemaDiagramHeight(value) {
    state.schemaDiagramHeight = Math.max(160, Math.min(640, Math.round(value)));
    applySchemaDiagramSizing();
}

function applySchemaDiagramSizing() {
    if (!els.schemaMermaidDiagram) {
        return;
    }
    els.schemaMermaidDiagram.style.setProperty("--schema-diagram-height", `${state.schemaDiagramHeight}px`);
    const svg = els.schemaMermaidDiagram.querySelector("svg");
    if (svg) {
        svg.style.width = `${Math.round(state.schemaDiagramZoom * 100)}%`;
        svg.style.maxWidth = "none";
        svg.style.height = "auto";
    }
}

function mermaidSourceForCurrentLayout(source) {
    if (!source || !source.trim()) {
        return "";
    }
    const lines = source.split(/\r?\n/);
    const output = [];
    let inserted = false;
    lines.forEach(line => {
        const trimmed = line.trim();
        if (trimmed.startsWith("direction ")) {
            return;
        }
        output.push(line);
        if (!inserted && trimmed === "erDiagram") {
            output.push(`    direction ${state.schemaDiagramDirection}`);
            inserted = true;
        }
    });
    return output.join("\n");
}

function beginSchemaDiagramResize(event) {
    event.preventDefault();
    state.schemaDiagramResizeStart = {
        y: event.clientY,
        height: state.schemaDiagramHeight
    };
    document.addEventListener("pointermove", resizeSchemaDiagram);
    document.addEventListener("pointerup", endSchemaDiagramResize, {once: true});
}

function resizeSchemaDiagram(event) {
    if (!state.schemaDiagramResizeStart) {
        return;
    }
    const delta = event.clientY - state.schemaDiagramResizeStart.y;
    setSchemaDiagramHeight(state.schemaDiagramResizeStart.height + delta);
}

function endSchemaDiagramResize() {
    state.schemaDiagramResizeStart = null;
    document.removeEventListener("pointermove", resizeSchemaDiagram);
}

function schemaTextControl(labelText, value, onInput, type = "text", helpText = "") {
    const label = schemaControlLabel(labelText, helpText);
    const input = type === "textarea" ? document.createElement("textarea") : document.createElement("input");
    if (type !== "textarea") {
        input.type = type;
    }
    input.value = value === undefined || value === null ? "" : value;
    input.addEventListener("input", () => onInput(input.value));
    label.appendChild(input);
    return label;
}

function schemaSelectControl(labelText, value, options, onChange, helpText = "") {
    const label = schemaControlLabel(labelText, helpText);
    const select = document.createElement("select");
    const selectedValue = value === undefined || value === null ? "" : String(value);
    if (!options.includes(selectedValue)) {
        options = [selectedValue, ...options].filter((item, index, list) => item || index === list.indexOf(item));
    }
    options.forEach(optionValue => {
        const option = document.createElement("option");
        option.value = optionValue;
        option.textContent = optionValue || "(blank)";
        option.selected = optionValue === selectedValue;
        select.appendChild(option);
    });
    select.addEventListener("change", () => onChange(select.value));
    label.appendChild(select);
    return label;
}

function schemaCheckboxControl(labelText, value, onChange, helpText = "") {
    const label = schemaControlLabel(labelText, helpText);
    const input = document.createElement("input");
    input.type = "checkbox";
    input.checked = Boolean(value);
    input.addEventListener("change", () => onChange(input.checked));
    label.appendChild(input);
    return label;
}

function schemaControlLabel(labelText, helpText = "") {
    const label = document.createElement("label");
    label.className = "schema-control";
    if (labelText) {
        const span = document.createElement("span");
        span.textContent = labelText;
        if (helpText) {
            span.appendChild(helpIcon(helpText));
        }
        label.appendChild(span);
    }
    return label;
}

function schemaInlineActions() {
    const actions = document.createElement("div");
    actions.className = "schema-inline-actions";
    return actions;
}

function schemaActionButton(text, onClick, className = "compact-button", helpText = "") {
    const button = document.createElement("button");
    button.type = "button";
    button.className = className;
    button.textContent = text;
    if (helpText) {
        button.setAttribute("data-tippy-content", helpText);
    }
    button.addEventListener("click", onClick);
    return button;
}

function helpIcon(helpText) {
    const icon = document.createElement("span");
    icon.className = "help-icon";
    icon.textContent = "?";
    icon.setAttribute("data-tippy-content", helpText);
    return icon;
}

function initializeSchemaHelp() {
    if (!window.tippy) {
        return;
    }
    document.querySelectorAll("[data-tippy-content]:not([data-tippy-initialized])").forEach(element => {
        window.tippy(element);
        element.setAttribute("data-tippy-initialized", "true");
    });
}

function newEntityDraft() {
    const index = state.schemaDraft.entities.length + 1;
    return {
        name: `entity${index}`,
        plural: `entities${index}`,
        maxInstances: -1,
        primaryKey: "",
        fields: []
    };
}

function newFieldDraft() {
    return {
        name: "field",
        type: "string",
        required: false,
        unique: false,
        defaultValue: null,
        description: null,
        examples: [],
        truncateTo: null,
        min: null,
        max: null,
        validations: [],
        objectFields: []
    };
}

function newRelationshipDraft() {
    const first = state.schemaDraft.entities[0] ? state.schemaDraft.entities[0].name : "";
    const second = state.schemaDraft.entities[1] ? state.schemaDraft.entities[1].name : first;
    return {
        from: first,
        name: "relationship",
        to: second,
        cardinality: "one-to-many",
        optionality: "optional",
        reverse: null
    };
}

function fieldTypes() {
    return ["string", "integer", "float", "boolean", "enum", "object", "date", "auto-increment", "auto-guid"];
}

function validationTypes() {
    return ["notEmpty", "maximumLength", "matchesRegex", "satisfiesRegex"];
}

function cardinalities() {
    return ["one-to-many", "one-to-one", "zero-to-one", "zero-to-many"];
}

function optionalities() {
    return ["optional", "mandatory"];
}

function downloadSchemaOutput(kind) {
    if (!state.schemaPreview || !state.schemaPreview.valid) {
        showMessage("Schema must be valid before export.", true);
        return false;
    }
    const base = filenameBase();
    if (kind === "yaml") {
        downloadText(`${base}.yaml`, schemaOutputText("yaml"), "text/yaml");
    } else if (kind === "mermaid") {
        downloadText(`${base}.mmd`, schemaOutputText("mermaid"), "text/plain");
    } else {
        downloadText(`${base}.dot`, schemaOutputText("graphviz"), "text/vnd.graphviz");
    }
    return true;
}

function downloadText(filename, text, type) {
    const blob = new Blob([text || ""], {type});
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
}

async function copySchemaOutput(kind) {
    if (!state.schemaPreview || !state.schemaPreview.valid) {
        showMessage("Schema must be valid before copying.", true);
        return;
    }
    const text = schemaOutputText(kind);
    try {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            await navigator.clipboard.writeText(text);
        } else {
            fallbackCopyText(text);
        }
        showMessage(`${kindDisplayName(kind)} copied to clipboard.`);
    } catch (error) {
        fallbackCopyText(text);
        showMessage(`${kindDisplayName(kind)} copied to clipboard.`);
    }
}

function schemaOutputText(kind) {
    if (kind === "yaml") {
        return state.schemaPreview.yaml || "";
    }
    if (kind === "mermaid") {
        return mermaidSourceForCurrentLayout(state.schemaPreview.mermaid || "");
    }
    return state.schemaPreview.graphviz || "";
}

function fallbackCopyText(text) {
    const area = document.createElement("textarea");
    area.value = text;
    area.style.position = "fixed";
    area.style.left = "-9999px";
    document.body.appendChild(area);
    area.focus();
    area.select();
    document.execCommand("copy");
    document.body.removeChild(area);
}

function kindDisplayName(kind) {
    if (kind === "yaml") {
        return "YAML";
    }
    if (kind === "mermaid") {
        return "Mermaid";
    }
    return "Graphviz";
}

function filenameBase() {
    const title = state.schemaDraft && state.schemaDraft.model ? state.schemaDraft.model.title : "";
    const cleaned = (title || "thingifier-schema").toLowerCase().replace(/[^a-z0-9]+/g, "-");
    return cleaned.replace(/^-+|-+$/g, "") || "thingifier-schema";
}

function numberOrNull(value) {
    return value === "" ? null : Number(value);
}

function numberOrDefault(value, defaultValue) {
    return value === "" ? defaultValue : Number(value);
}

function emptyToNull(value) {
    return value === "" ? null : value;
}

if (els.schemaWorkspaceLink) {
    els.schemaWorkspaceLink.addEventListener("click", event => {
        if (!confirmSchemaSaveBefore("switching to Workspace")) {
            event.preventDefault();
        }
    });
}
if (els.schemaReset) {
    els.schemaReset.setAttribute("data-tippy-content", "Reload the draft from the current workspace schema YAML.");
    els.schemaReset.addEventListener("click", () => {
        if (!confirmSchemaSaveBefore("resetting the draft")) {
            return;
        }
        initializeSchemaEditor(true).catch(error => showMessage(error.message, true));
    });
}
if (els.schemaAddEntity) {
    els.schemaAddEntity.setAttribute("data-tippy-content", "Add a new entity to the schema draft.");
    els.schemaAddEntity.addEventListener("click", () => {
        state.schemaDraft.entities.push(newEntityDraft());
        state.schemaSelection = {type: "entity", entityIndex: state.schemaDraft.entities.length - 1};
        renderSchemaEditor();
        scheduleSchemaPreview();
    });
}
if (els.schemaSaveYaml) {
    els.schemaSaveYaml.setAttribute("data-tippy-content", "Save the current valid draft as canonical YAML.");
    els.schemaSaveYaml.addEventListener("click", () => {
        if (downloadSchemaOutput("yaml")) {
            markSchemaClean();
        }
    });
}
if (els.schemaToggleDiagram) {
    els.schemaToggleDiagram.setAttribute("data-tippy-content", "Show or hide the rendered ER diagram.");
    els.schemaToggleDiagram.addEventListener("click", () => {
        state.schemaDiagramVisible = !state.schemaDiagramVisible;
        renderSchemaPanelVisibility();
    });
}
if (els.schemaZoomOut) {
    els.schemaZoomOut.setAttribute("data-tippy-content", "Zoom out of the ER diagram.");
    els.schemaZoomOut.addEventListener("click", () => setSchemaDiagramZoom(state.schemaDiagramZoom - 0.1));
}
if (els.schemaZoomReset) {
    els.schemaZoomReset.setAttribute("data-tippy-content", "Reset ER diagram zoom.");
    els.schemaZoomReset.addEventListener("click", () => setSchemaDiagramZoom(1));
}
if (els.schemaZoomIn) {
    els.schemaZoomIn.setAttribute("data-tippy-content", "Zoom in to the ER diagram.");
    els.schemaZoomIn.addEventListener("click", () => setSchemaDiagramZoom(state.schemaDiagramZoom + 0.1));
}
if (els.schemaLayoutToggle) {
    els.schemaLayoutToggle.setAttribute(
        "data-tippy-content",
        "Toggle the ER diagram between vertical and horizontal layout.");
    els.schemaLayoutToggle.addEventListener("click", toggleSchemaDiagramDirection);
}
if (els.schemaDiagramResizer) {
    els.schemaDiagramResizer.addEventListener("pointerdown", beginSchemaDiagramResize);
}
if (els.schemaToggleYaml) {
    els.schemaToggleYaml.setAttribute("data-tippy-content", "Show or hide the raw YAML draft editor.");
    els.schemaToggleYaml.addEventListener("click", () => {
        state.schemaYamlVisible = !state.schemaYamlVisible;
        renderSchemaPanelVisibility();
    });
}
if (els.schemaToggleExports) {
    els.schemaToggleExports.setAttribute("data-tippy-content", "Show or hide generated export formats.");
    els.schemaToggleExports.addEventListener("click", () => {
        state.schemaExportsVisible = !state.schemaExportsVisible;
        renderSchemaPanelVisibility();
    });
}
if (els.schemaParseYaml) {
    els.schemaParseYaml.setAttribute("data-tippy-content", "Parse the raw YAML text into the draft editor.");
    els.schemaParseYaml.addEventListener("click", () => {
        parseSchemaYaml(els.schemaYamlInput.value, false).catch(error => showMessage(error.message, true));
    });
}
if (els.schemaYamlInput) {
    els.schemaYamlInput.addEventListener("input", markSchemaDirty);
}
if (els.schemaValidate) {
    els.schemaValidate.setAttribute("data-tippy-content", "Validate the current draft and refresh generated previews.");
    els.schemaValidate.addEventListener("click", () => previewSchemaDraft().catch(error => showMessage(error.message, true)));
}
if (els.schemaUpgradePreview) {
    els.schemaUpgradePreview.setAttribute("data-tippy-content", "Refresh the schema upgrade migration preview.");
    els.schemaUpgradePreview.addEventListener("click", () => previewSchemaUpgrade().catch(error => showMessage(error.message, true)));
}
if (els.schemaUpgradeConfirm) {
    els.schemaUpgradeConfirm.setAttribute("data-tippy-content", "Confirm and replace the live workspace with the migrated schema and data.");
    els.schemaUpgradeConfirm.addEventListener("click", () => applySchemaUpgrade().catch(error => showMessage(error.message, true)));
}
if (els.schemaUpgradeCancel) {
    els.schemaUpgradeCancel.setAttribute("data-tippy-content", "Cancel applying the schema draft and return to editing.");
    els.schemaUpgradeCancel.addEventListener("click", closeSchemaUpgradeDialog);
}
if (els.schemaApplyWorkspace) {
    els.schemaApplyWorkspace.setAttribute("data-tippy-content", "Preview and confirm applying the draft schema to the live workspace.");
    els.schemaApplyWorkspace.addEventListener("click", () => startSchemaUpgradeWorkflow().catch(error => showMessage(error.message, true)));
}
if (els.schemaDownloadYaml) {
    els.schemaDownloadYaml.addEventListener("click", () => {
        if (downloadSchemaOutput("yaml")) {
            markSchemaClean();
        }
    });
}
if (els.schemaDownloadMermaid) {
    els.schemaDownloadMermaid.setAttribute("data-tippy-content", "Save the generated Mermaid diagram source.");
    els.schemaDownloadMermaid.addEventListener("click", () => downloadSchemaOutput("mermaid"));
}
if (els.schemaDownloadGraphviz) {
    els.schemaDownloadGraphviz.setAttribute("data-tippy-content", "Save the generated Graphviz DOT source.");
    els.schemaDownloadGraphviz.addEventListener("click", () => downloadSchemaOutput("graphviz"));
}
if (els.schemaCopyYaml) {
    els.schemaCopyYaml.setAttribute("data-tippy-content", "Copy canonical YAML to the clipboard.");
    els.schemaCopyYaml.addEventListener("click", () => copySchemaOutput("yaml"));
}
if (els.schemaCopyMermaid) {
    els.schemaCopyMermaid.setAttribute("data-tippy-content", "Copy Mermaid ER diagram source to the clipboard.");
    els.schemaCopyMermaid.addEventListener("click", () => copySchemaOutput("mermaid"));
}
if (els.schemaCopyGraphviz) {
    els.schemaCopyGraphviz.setAttribute("data-tippy-content", "Copy Graphviz DOT source to the clipboard.");
    els.schemaCopyGraphviz.addEventListener("click", () => copySchemaOutput("graphviz"));
}

if (els.search) {
    els.search.addEventListener("input", () => renderGrid(state.currentRows));
}
if (els.newButton) {
    els.newButton.addEventListener("click", () => {
        state.selectedRow = null;
        state.relationshipContext = null;
        renderShell();
        renderGrid(state.currentRows);
        renderEditor(null);
    });
}
if (els.refreshButton) {
    els.refreshButton.addEventListener("click", async () => {
        if (state.currentEntity) {
            await loadCollection(state.currentEntity);
        } else {
            await loadWorkspace();
        }
    });
}
if (els.exportButton) {
    els.exportButton.addEventListener("click", async () => {
        try {
            const response = await fetch("/ui/export", {headers: {"Accept": "application/json"}});
            const blob = await response.blob();
            if (!response.ok) {
                throw new Error(await blob.text());
            }
            const url = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = "thingifier-workspace.json";
            link.click();
            URL.revokeObjectURL(url);
        } catch (error) {
            showMessage(error.message, true);
        }
    });
}
if (els.saveProjectButton) {
    els.saveProjectButton.addEventListener("click", () => openProjectDialog("save"));
}
if (els.loadProjectButton) {
    els.loadProjectButton.addEventListener("click", () => openProjectDialog("load"));
}
if (els.projectDialogConfirm) {
    els.projectDialogConfirm.addEventListener("click", () => {
        submitProjectDialog().catch(error => showMessage(error.message, true));
    });
}
if (els.projectDialogCancel) {
    els.projectDialogCancel.addEventListener("click", closeProjectDialog);
}
if (els.projectDialog) {
    els.projectDialog.addEventListener("click", event => {
        if (event.target === els.projectDialog) {
            closeProjectDialog();
        }
    });
}
if (els.projectPathInput) {
    els.projectPathInput.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            event.preventDefault();
            submitProjectDialog().catch(error => showMessage(error.message, true));
        }
        if (event.key === "Escape") {
            closeProjectDialog();
        }
    });
}
if (els.yamlFile) {
    els.yamlFile.addEventListener("change", async event => {
        await loadTextFile(event.target.files[0], async text => {
            const workspace = await requestJson("/ui/model/yaml", {method: "POST", body: text});
            state.workspace = workspace;
            state.schemaDraft = null;
            state.schemaPreview = null;
            await loadWorkspace();
            showMessage("YAML model loaded.");
        });
        event.target.value = "";
    });
}
if (els.importFile) {
    els.importFile.addEventListener("change", async event => {
        await loadTextFile(event.target.files[0], async text => {
            const workspace = await requestJson("/ui/import", {method: "POST", body: text});
            state.workspace = workspace;
            state.schemaDraft = null;
            state.schemaPreview = null;
            await loadWorkspace();
            showMessage("Workspace imported.");
        });
        event.target.value = "";
    });
}

window.addEventListener("beforeunload", event => {
    if (state.mode !== "schema" || !state.schemaDirty) {
        return;
    }
    event.preventDefault();
    event.returnValue = "";
});

async function loadTextFile(file, action) {
    if (!file) {
        return;
    }
    try {
        await action(await file.text());
    } catch (error) {
        showMessage(error.message, true);
    }
}

if (state.mode === "schema") {
    initializeSchemaEditor(true).catch(error => showMessage(error.message, true));
} else {
    loadWorkspace().catch(error => showMessage(error.message, true));
}
