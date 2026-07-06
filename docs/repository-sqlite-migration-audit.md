# Repository SQLite Migration Audit

This note classifies the current `getInstanceData()` and `getThingInstancesNamed()` usage during the SQLite repository experiment.

## Normal API Request Paths

Simple entity `GET` requests use `ThingRepository` for `/entity`, `/entities`, `/entity/{id}`, and `/entities/{id}`.

Simple relationship `GET` traversal, such as `/projects/{id}/tasks`, now uses `ThingRepository.listRelatedInstances(...)`. SQLite executes the relationship lookup against relationship tables and materializes only the returned rows.

Simple entity `POST`, `PUT`, and `DELETE` requests resolve entity definitions and entity instances through `ThingRepository` for root collection and root instance paths. These paths should not call `getInstanceData()` for SQLite unless a payload uses unsupported legacy behavior.

Relationship create/delete URL paths still use the legacy `SimpleQuery` fallback. This includes write paths such as `POST /projects/{id}/tasks` and `DELETE /projects/{id}/tasks/{id}`.

## Relationship Payload Helpers

Relationship references in request bodies now resolve target instances through `ThingRepository` rather than `EntityInstanceCollection` scans. This keeps normal create/amend payloads from requiring a full SQLite compatibility snapshot merely to find a related target by id, guid, or other supported field.

## Compatibility Snapshot Paths

`EntityRelModel.getInstanceData()` and `ThingRepository.getInstanceData()` are compatibility/export paths. For SQLite, `getInstanceData()` explicitly hydrates the legacy in-memory snapshot.

Known compatibility users:

- data populator and JSON import paths;
- admin/debug/export views and reporting;
- legacy data populators that have not implemented `RepositoryDataPopulator`;
- relationship write URL fallback paths.

The default Todo API and Todo Manager seed populators now implement `RepositoryDataPopulator`, so SQLite-backed Todo apps can seed through repository writes without first hydrating `ERInstanceData`.

The base GUI data explorer list/detail pages now read via `ThingRepository` and render relationship sections via `ThingRepository.listRelatedInstances(...)`.

These remaining compatibility paths are acceptable for this phase, but they are the next places to reduce if memory efficiency needs to extend beyond simple entity reads and Todo-style population.

## Test Helpers

Most `getInstanceData()` and `getThingInstancesNamed()` usage is in `src/test`. These are test fixture and assertion helpers, not runtime leaks, unless a test is specifically exercising SQLite memory efficiency.

## Runtime Lifecycle

SQLite in-memory repositories use named database URLs scoped to a single `SqliteThingRepositoryProvider` lifetime. Each logical database key receives its own SQLite in-memory database name inside that provider namespace.

The repository/provider owns the SQLite connection. `Thingifier.close()`, `EntityRelModel.close()`, `MainImplementation.close()`, and the shutdown route close repository providers so the in-memory database lifetime is explicit and connection-bound.
