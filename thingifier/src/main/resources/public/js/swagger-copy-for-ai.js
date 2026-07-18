(function () {
    "use strict";

    const BUTTON_TEXT = "Copy for AI";
    const FULL_BUTTON_TEXT = "Copy full API for AI";
    const COPIED_TEXT = "Copied";
    const FAILED_TEXT = "Copy failed";
    const DEFAULT_OPEN_API_URL = "/docs/openapi.json";
    const HTTP_METHODS = ["get", "put", "post", "delete", "patch", "head", "options", "trace"];

    const config = window.thingifierSwaggerCopyForAi || {};
    let openApiSpec;
    let openApiPromise;
    let operationIndex = {};

    window.addEventListener("load", function () {
        const swaggerRoot = document.querySelector("#swagger-ui");
        if (!swaggerRoot) {
            return;
        }
        addFullApiToolbar(swaggerRoot);
        loadOpenApi().then(enhanceOperationRows).catch(reportLoadFailure);
        const observer = new MutationObserver(function () {
            if (openApiSpec) {
                enhanceOperationRows();
            }
        });
        observer.observe(swaggerRoot, {childList: true, subtree: true});
    });

    function loadOpenApi() {
        if (!openApiPromise) {
            openApiPromise = fetch(config.openApiUrl || DEFAULT_OPEN_API_URL)
                    .then(function (response) {
                        if (!response.ok) {
                            throw new Error("Could not load OpenAPI JSON: " + response.status);
                        }
                        return response.json();
                    })
                    .then(function (spec) {
                        openApiSpec = spec;
                        operationIndex = indexOperations(spec);
                        return spec;
                    });
        }
        return openApiPromise;
    }

    function addFullApiToolbar(swaggerRoot) {
        if (document.querySelector("[data-testid='swagger-copy-full-ai']")) {
            return;
        }

        const toolbar = document.createElement("div");
        toolbar.className = "swagger-copy-ai-toolbar";
        toolbar.setAttribute("data-testid", "swagger-copy-ai-toolbar");

        const button = document.createElement("button");
        button.type = "button";
        button.className = "swagger-copy-ai-button";
        button.textContent = FULL_BUTTON_TEXT;
        button.setAttribute("data-testid", "swagger-copy-full-ai");
        button.addEventListener("click", function () {
            loadOpenApi()
                    .then(function (spec) {
                        return copyText(fullApiMarkdown(spec), button);
                    })
                    .catch(function () {
                        showTemporaryText(button, FAILED_TEXT);
                    });
        });

        const status = document.createElement("span");
        status.className = "swagger-copy-ai-status";
        status.textContent = "Copies clean Markdown for Codex, Claude, Cursor, or other AI tools.";

        toolbar.appendChild(button);
        toolbar.appendChild(status);
        swaggerRoot.parentNode.insertBefore(toolbar, swaggerRoot);
    }

    function enhanceOperationRows() {
        document.querySelectorAll(".swagger-ui .opblock").forEach(function (block) {
            if (block.querySelector("[data-testid='swagger-copy-operation-ai']")) {
                return;
            }

            const operation = operationFromBlock(block);
            if (!operation) {
                return;
            }

            const summary = block.querySelector(".opblock-summary");
            if (!summary) {
                return;
            }

            const button = document.createElement("button");
            button.type = "button";
            button.className = "swagger-copy-ai-button swagger-copy-ai-operation";
            button.textContent = BUTTON_TEXT;
            button.setAttribute("data-testid", "swagger-copy-operation-ai");
            button.addEventListener("click", function (event) {
                event.preventDefault();
                event.stopPropagation();
                copyText(operationMarkdown(operation.path, operation.method, operation.operation), button);
            });

            summary.appendChild(button);
        });
    }

    function operationFromBlock(block) {
        const methodElement = block.querySelector(".opblock-summary-method");
        const pathElement = block.querySelector(".opblock-summary-path");
        if (!methodElement || !pathElement) {
            return null;
        }

        const method = methodElement.textContent.trim().toLowerCase();
        const path = pathElement.getAttribute("data-path") || pathElement.textContent.trim();
        const normalizedPath = normalizePath(path);
        return operationIndex[operationKey(normalizedPath, method)] || null;
    }

    function indexOperations(spec) {
        const index = {};
        const paths = spec.paths || {};
        Object.keys(paths).forEach(function (path) {
            const pathItem = paths[path] || {};
            HTTP_METHODS.forEach(function (method) {
                if (pathItem[method]) {
                    index[operationKey(path, method)] = {
                        path: path,
                        method: method,
                        operation: pathItem[method]
                    };
                }
            });
        });
        return index;
    }

    function operationKey(path, method) {
        return method.toLowerCase() + " " + normalizePath(path);
    }

    function normalizePath(path) {
        if (!path) {
            return "/";
        }
        let normalized = path.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.replace(/\s+/g, "");
    }

    function fullApiMarkdown(spec) {
        const info = spec.info || {};
        const lines = [
            "# " + (info.title || "OpenAPI documentation"),
            "",
            "- Version: `" + (info.version || "") + "`",
            "- Base URL: `" + baseUrl() + "`",
            "- OpenAPI: `" + (spec.openapi || spec.swagger || "") + "`"
        ];

        if (info.description) {
            lines.push("", "## Description", "", info.description);
        }

        lines.push("", "## Operations");
        Object.keys(spec.paths || {}).sort().forEach(function (path) {
            const pathItem = spec.paths[path] || {};
            HTTP_METHODS.forEach(function (method) {
                if (pathItem[method]) {
                    lines.push("", operationMarkdown(path, method, pathItem[method]));
                }
            });
        });

        lines.push("", "## Component Schemas");
        const schemas = ((spec.components || {}).schemas) || {};
        if (Object.keys(schemas).length === 0) {
            lines.push("", "No component schemas documented.");
        } else {
            Object.keys(schemas).sort().forEach(function (name) {
                lines.push("", "### " + name, "", jsonBlock(schemas[name]));
            });
        }

        return lines.join("\n").trim();
    }

    function operationMarkdown(path, method, operation) {
        const fullUrl = fullOperationUrl(path);
        const lines = [
            "# " + method.toUpperCase() + " " + fullUrl,
            "",
            "- Method: `" + method.toUpperCase() + "`",
            "- OpenAPI path: `" + path + "`",
            "- Full URL: `" + fullUrl + "`"
        ];

        if (operation.summary) {
            lines.push("- Summary: " + operation.summary);
        }
        if (operation.tags && operation.tags.length > 0) {
            lines.push("- Tags: " + operation.tags.map(code).join(", "));
        }

        if (operation.description) {
            lines.push("", "## Description", "", operation.description);
        }

        appendParameters(lines, operation.parameters || []);
        appendRequestBody(lines, operation.requestBody);
        appendResponses(lines, operation.responses || {});
        appendSecurity(lines, operation.security);
        appendReferencedSchemas(lines, operation);

        return lines.join("\n").trim();
    }

    function appendParameters(lines, parameters) {
        lines.push("", "## Parameters");
        if (!parameters.length) {
            lines.push("", "No parameters documented.");
            return;
        }

        parameters.forEach(function (parameter) {
            lines.push(
                    "",
                    "- `" + (parameter.name || "") + "` in `" + (parameter.in || "") + "`"
                            + (parameter.required ? " required" : "")
                            + (parameter.description ? ": " + parameter.description : ""));
            if (parameter.schema) {
                lines.push("  - Schema: `" + compactJson(parameter.schema) + "`");
            }
            if (parameter.example !== undefined) {
                lines.push("  - Example: `" + String(parameter.example) + "`");
            }
        });
    }

    function appendRequestBody(lines, requestBody) {
        lines.push("", "## Request Body");
        if (!requestBody) {
            lines.push("", "No request body documented.");
            return;
        }

        if (requestBody.description) {
            lines.push("", requestBody.description);
        }
        lines.push("", jsonBlock(requestBody));
    }

    function appendResponses(lines, responses) {
        lines.push("", "## Responses");
        const codes = Object.keys(responses);
        if (!codes.length) {
            lines.push("", "No responses documented.");
            return;
        }

        codes.sort().forEach(function (status) {
            const response = responses[status] || {};
            lines.push("", "### " + status, "", response.description || "");
            if (response.content) {
                lines.push("", jsonBlock(response.content));
            }
        });
    }

    function appendSecurity(lines, security) {
        if (!security || !security.length) {
            return;
        }

        lines.push("", "## Security", "", jsonBlock(security));
    }

    function appendReferencedSchemas(lines, operation) {
        const refs = referencedSchemaNames(operation);
        if (!refs.length) {
            return;
        }

        lines.push("", "## Referenced Schemas");
        const schemas = ((openApiSpec.components || {}).schemas) || {};
        refs.forEach(function (name) {
            if (schemas[name]) {
                lines.push("", "### " + name, "", jsonBlock(schemas[name]));
            }
        });
    }

    function referencedSchemaNames(value) {
        const refs = [];
        collectRefs(value, refs);
        return refs.filter(function (name, index) {
            return refs.indexOf(name) === index;
        }).sort();
    }

    function collectRefs(value, refs) {
        if (!value || typeof value !== "object") {
            return;
        }
        if (typeof value.$ref === "string") {
            const match = value.$ref.match(/^#\/components\/schemas\/(.+)$/);
            if (match) {
                refs.push(match[1]);
            }
        }
        Object.keys(value).forEach(function (key) {
            collectRefs(value[key], refs);
        });
    }

    function baseUrl() {
        const serverUrl = firstServerUrl();
        let serverPath = "";
        try {
            serverPath = new URL(serverUrl || "/", window.location.origin).pathname;
        } catch (e) {
            serverPath = "";
        }
        return joinUrlParts(window.location.origin, serverPath === "/" ? "" : serverPath);
    }

    function firstServerUrl() {
        if (!openApiSpec || !openApiSpec.servers || !openApiSpec.servers.length) {
            return "";
        }
        return openApiSpec.servers[0].url || "";
    }

    function fullOperationUrl(path) {
        return joinUrlParts(baseUrl(), path);
    }

    function joinUrlParts(left, right) {
        const lhs = String(left || "").replace(/\/+$/, "");
        const rhs = String(right || "").replace(/^\/+/, "");
        if (!rhs) {
            return lhs || "/";
        }
        return lhs + "/" + rhs;
    }

    function copyText(text, button) {
        button.disabled = true;
        return copyToClipboard(text)
                .then(function () {
                    showTemporaryText(button, COPIED_TEXT);
                })
                .catch(function () {
                    showTemporaryText(button, FAILED_TEXT);
                });
    }

    function copyToClipboard(text) {
        if (navigator.clipboard && navigator.clipboard.writeText) {
            return navigator.clipboard.writeText(text);
        }
        return fallbackCopy(text);
    }

    function fallbackCopy(text) {
        return new Promise(function (resolve, reject) {
            const textarea = document.createElement("textarea");
            textarea.value = text;
            textarea.setAttribute("readonly", "readonly");
            textarea.style.position = "fixed";
            textarea.style.left = "-9999px";
            document.body.appendChild(textarea);
            textarea.select();
            try {
                if (document.execCommand("copy")) {
                    resolve();
                } else {
                    reject(new Error("Copy command failed"));
                }
            } finally {
                document.body.removeChild(textarea);
            }
        });
    }

    function showTemporaryText(button, text) {
        const original = button.getAttribute("data-original-text") || button.textContent;
        button.setAttribute("data-original-text", original);
        button.textContent = text;
        setTimeout(function () {
            button.disabled = false;
            button.textContent = original;
        }, 1400);
    }

    function reportLoadFailure() {
        const toolbar = document.querySelector("[data-testid='swagger-copy-ai-toolbar']");
        if (!toolbar || toolbar.querySelector(".swagger-copy-ai-error")) {
            return;
        }
        const error = document.createElement("span");
        error.className = "swagger-copy-ai-status swagger-copy-ai-error";
        error.textContent = "Copy for AI could not load the OpenAPI document.";
        toolbar.appendChild(error);
    }

    function jsonBlock(value) {
        return "```json\n" + JSON.stringify(value, null, 2) + "\n```";
    }

    function compactJson(value) {
        return JSON.stringify(value);
    }

    function code(value) {
        return "`" + String(value) + "`";
    }
})();
