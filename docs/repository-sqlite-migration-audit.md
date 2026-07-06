# Repository SQLite Migration Audit

This note classifies the current `getInstanceData()` and `getThingInstancesNamed()` usage during the SQLite repository experiment.

## Normal API Request Paths

Simple entity `GET` requests already use `ThingRepository` for `/entity`, `/entities`, `/entity/{id}`, and `/entities/{id}` when the path does not traverse a relationship.

Simple entity `POST`, `PUT`, and `DELETE` requests now resolve entity definitions and entity instances through `ThingRepository` for root collection and root instance paths. These paths should not call `getInstanceData()` for SQLite unless a payload uses unsupported legacy behavior.

Relationship URL paths still use the legacy `SimpleQuery` fallback. This includes relationship traversal and relationship create/delete paths such as `/projects/{id}/tasks` and `/projects/{id}/tasks/{id}`.

## Relationship Payload Helpers

Relationship references in request bodies now resolve target instances through `ThingRepository` rather than `EntityInstanceCollection` scans. This keeps normal create/amend payloads from requiring a full SQLite compatibility snapshot merely to find a related target by id, guid, or other supported field.

## Compatibility Snapshot Paths

`EntityRelModel.getInstanceData()` and `ThingRepository.getInstanceData()` are compatibility/export paths. For SQLite, `getInstanceData()` explicitly hydrates the legacy in-memory snapshot.

Known compatibility users:

- data populator and JSON import paths;
- admin/debug/export views and reporting;
- Challenger persistence/session restore;
- GUI data explorer pages;
- relationship URL fallback paths.

These are acceptable for this phase, but they are the next places to reduce if memory efficiency needs to extend beyond simple entity API operations.

## Test Helpers

Most `getInstanceData()` and `getThingInstancesNamed()` usage is in `src/test`. These are test fixture and assertion helpers, not runtime leaks, unless a test is specifically exercising SQLite memory efficiency.

## Runtime Lifecycle

SQLite in-memory repositories use named database URLs scoped to a single `SqliteThingRepositoryProvider` lifetime. Each logical database key receives its own SQLite in-memory database name inside that provider namespace.

The repository/provider owns the SQLite connection. `Thingifier.close()`, `EntityRelModel.close()`, `MainImplementation.close()`, and the shutdown route close repository providers so the in-memory database lifetime is explicit and connection-bound.
