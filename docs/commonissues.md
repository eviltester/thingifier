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

## Payloads

-Malformed payloads typically trigger errors in the surrounding frameworks, not the api, but if not handled properly can cause server errors

## Headers

- Duplicate headers can cause systems to only validate contents of first header, but use the second header, allowing bypass of validation
- Extra / Unrecognised headers can sometimes server error APIs


## Conversion

- various technologies can mean conversion issues slip through e.g. JSON Integers to floats, boolean to string. Check field types.