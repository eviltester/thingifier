# The ER Model

This is intended to be the definition of the base for as much as follows.

It should enforce the definition rules for the model:

- cardinality
- relationships
- values in fields

Flagging warnings/errors for:

- fields that do not exist

It may need to have transactional updates/creation where given a set of fields, it either creates or rejects based on the full instance, rather than allowing creation of a partial instance.

In theory it could be 'backed' by a database, with the schema used to create the database. Or interact with an existing database that matches the schema. This would require swapping in a different implementation of the instances package.

It should know nothing of HTTP, REST. This is a schema and instantiation for that schema. In theory this could be split into ER Model, Schema, Persistence.