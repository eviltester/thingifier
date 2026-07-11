# Repository SQLite Migration Audit

This note captures the current repository/store migration state after the broad
`ThingRepository` API was split into focused `ThingStore` ports.

## Runtime Request Paths

Simple entity `GET`, `POST`, `PUT`, `PATCH`, and `DELETE` requests now resolve
entities and persisted instances through `ThingStore`:

- `store.entityQueries()` handles lookup, filtering, sorting, and counts.
- `store.entities()` handles create, patch, replace, and delete.
- `store.relationships()` handles relationship connect, traversal, removal, and
  validation.
- `store.administration()` handles schema initialization, clearing, and counter
  maintenance.

Relationship URL paths such as `POST /projects/{id}/tasks`,
`DELETE /projects/{id}/tasks/{id}`, and `GET /projects/{id}/tasks` use
repository-backed relationship resolution and traversal. SQLite keeps
relationship rows in SQL tables and materializes only requested entity rows.

## Removed Compatibility Paths

The old public snapshot escape hatches have been removed from normal runtime
APIs. Runtime code should not call `getInstanceData()`,
`getThingInstancesNamed()`, `EntityInstanceCollection`, or `SimpleQuery`.

Import/population, reporting, GUI, API handlers, Challenger hooks, and
standalone apps now use repository-native store ports.

## SQLite Lifecycle

SQLite in-memory stores use named database URLs scoped to a single
`SqliteThingStoreProvider` lifetime. Each logical database key receives its own
SQLite in-memory database name inside that provider namespace.

The store/provider owns the SQLite connection. `Thingifier.close()`,
`EntityRelModel.close()`, `MainImplementation.close()`, and the shutdown route
close providers so the in-memory database lifetime is explicit and
connection-bound.

## Guardrails

Project Checkstyle guards now fail production code that reintroduces the removed
broad repository types, hidden test-support helpers, mutable `EntityInstance`
escape hatches, `SimpleQuery`, or direct in-memory collection access outside the
approved in-memory implementation packages.
