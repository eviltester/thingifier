# Common issues

## URLs

- `/noun` is singular, or mixed, causing confusion
- `/nouns/` does not throw 404

## Query Params

- query params being used for sensitive data that might appear in server logs
- query params allowing access to content not normally accessible to user e.g. paging, or specific ids

## Status Codes

- status codes often use the most generic, when a more specific would capture semantics better, e.g. 400 used all the time, 200 used when 204 might be better

## Verbs

- post, put, patch - used without properly discriminating between them
- `404` returned instead of `405` when endpoint exists, but not configured for a verb