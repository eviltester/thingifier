const state = {
    workspace: null,
    outline: {},
    expandedNodes: {},
    entityCounts: {},
    currentEntity: null,
    currentRows: [],
    selectedRow: null,
    relationshipContext: null,
    filters: {}
};

const els = {
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
    yamlFile: document.getElementById("yaml-file"),
    importFile: document.getElementById("import-file")
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
    els.title.textContent = state.workspace.model.title || "Thingifier";
    els.description.textContent = state.workspace.model.description || "";
    renderTree();
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
    els.message.textContent = text;
    els.message.className = error ? "message-bar error" : "message-bar";
    els.message.hidden = false;
}

function clearMessage() {
    els.message.textContent = "";
    els.message.hidden = true;
}

els.search.addEventListener("input", () => renderGrid(state.currentRows));
els.newButton.addEventListener("click", () => {
    state.selectedRow = null;
    state.relationshipContext = null;
    renderShell();
    renderGrid(state.currentRows);
    renderEditor(null);
});
els.refreshButton.addEventListener("click", async () => {
    if (state.currentEntity) {
        await loadCollection(state.currentEntity);
    } else {
        await loadWorkspace();
    }
});
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
els.yamlFile.addEventListener("change", async event => {
    await loadTextFile(event.target.files[0], async text => {
        const workspace = await requestJson("/ui/model/yaml", {method: "POST", body: text});
        state.workspace = workspace;
        await loadWorkspace();
        showMessage("YAML model loaded.");
    });
    event.target.value = "";
});
els.importFile.addEventListener("change", async event => {
    await loadTextFile(event.target.files[0], async text => {
        const workspace = await requestJson("/ui/import", {method: "POST", body: text});
        state.workspace = workspace;
        await loadWorkspace();
        showMessage("Workspace imported.");
    });
    event.target.value = "";
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

loadWorkspace().catch(error => showMessage(error.message, true));
