# Challenger Auto Tests

`challengerAuto` runs the API Challenges REST tests against a configurable Challenger environment.

By default, the tests start a local Challenger app in a separate JVM, wait for `/heartbeat`, run the full suite, and then shut down only the process they started.

- local - startup the application locally prior to running the code
- existing - app is running locally already so test doesn't start it
- live - app is external so we need to set the baseurl to access it

You can choose between running 'smoke' tests for heartbeat or all tests for external `challenger.auto.external.full`

See usage combinations below.

## Local Runs

PowerShell users should quote Maven `-D` properties, e.g. `"-Dchallenger.auto.target=local"`.

Default local run:

```powershell
mvn -pl challengerAuto -am test
```

This starts a local Challenger process with:

- SQLite in-memory repository;
- multiplayer mode;
- automatically selected free port;
- full mutating test suite enabled.

Run local in-memory, single-player:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=local" `
  "-Dchallenger.auto.local.repository=memory" `
  "-Dchallenger.auto.local.playerMode=single"
```

Run local in-memory, multiplayer:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=local" `
  "-Dchallenger.auto.local.repository=memory" `
  "-Dchallenger.auto.local.playerMode=multi"
```

Run local SQLite in-memory, single-player:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=local" `
  "-Dchallenger.auto.local.repository=sqlite-memory" `
  "-Dchallenger.auto.local.playerMode=single"
```

Run local SQLite in-memory, multiplayer:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=local" `
  "-Dchallenger.auto.local.repository=sqlite-memory" `
  "-Dchallenger.auto.local.playerMode=multi"
```

Run local SQLite file-backed:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=local" `
  "-Dchallenger.auto.local.repository=sqlite-file" `
  "-Dchallenger.auto.local.extraArgs=-thingifier-sqlite-directory=target/challenger-auto-sqlite"
```

Use a fixed local port:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=local" `
  "-Dchallenger.auto.local.port=4567"
```

If the fixed port is already in use, the harness fails fast. Use `challenger.auto.target=existing` when you want to attach to an app you already started.

Pass extra Challenger args:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.local.extraArgs=-unlimitedtodos,-enableadminapi"
```

Extra args are comma-separated and appended to the generated `ChallengeMain` command line.

## Existing, Live, And Railway Runs

Run against an existing local app:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=existing" `
  "-Dchallenger.auto.baseUrl=http://localhost:4567"
```

`existing` never starts or stops Challenger. It only uses the configured base URL.

Run smoke checks against the live site:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=live" `
  "-Dtest=ChallengerAutoSmokeTest"
```

Run smoke checks against Railway or another hosted environment:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=live" `
  "-Dchallenger.auto.baseUrl=https://your-railway-app.up.railway.app" `
  "-Dtest=ChallengerAutoSmokeTest"
```

`live` defaults to `https://apichallenges.eviltester.com`. Override `challenger.auto.baseUrl` for Railway, staging, or any other remote Challenger deployment.

External targets are smoke-only by default. The mutating challenge and simple API suites are skipped unless explicitly enabled:

```powershell
mvn -pl challengerAuto -am test `
  "-Dchallenger.auto.target=live" `
  "-Dchallenger.auto.baseUrl=https://your-remote-app.example" `
  "-Dchallenger.auto.external.full=true"
```

Only use `challenger.auto.external.full=true` on environments where creating, updating, and deleting test data is acceptable.

## Configuration Properties

| Property | Values | Default | Purpose |
| --- | --- | --- | --- |
| `challenger.auto.target` | `local`, `existing`, `live` | `local` | Chooses whether the harness starts Challenger or attaches to a URL. |
| `challenger.auto.baseUrl` | URL | target-specific | Base URL for `existing` and remote `live` runs. |
| `challenger.auto.local.repository` | `memory`, `sqlite-memory`, `sqlite-file` | `sqlite-memory` | Repository mode for owned local Challenger. |
| `challenger.auto.local.playerMode` | `single`, `multi` | `multi` | Single-player or multiplayer mode for owned local Challenger. |
| `challenger.auto.local.port` | `auto` or port number | `auto` | Local port for owned local Challenger. |
| `challenger.auto.local.extraArgs` | comma-separated args | empty | Extra `ChallengeMain` command-line args. |
| `challenger.auto.external.full` | `true`, `false` | `false` | Allows full mutating suite for `live` targets. |

Environment variables use uppercase names with underscores, for example `CHALLENGER_AUTO_BASE_URL`.

## IDE Run Profiles

`uk.co.compendiumdev.sparkstart.ChallengerAutoRunProfiles` contains convenience methods that can be right-clicked in IntelliJ:

- `localMemorySinglePlayer()`
- `localMemoryMultiPlayer()`
- `localSqliteMemorySinglePlayer()`
- `localSqliteMemoryMultiPlayer()`
- `existingLocal()`
- `liveSmoke()`

The class name deliberately does not end in `Test`, `Tests`, or `TestCase`, so normal Surefire discovery does not run every profile automatically. Each method delegates to the same harness used by Maven property runs and performs smoke checks against `/heartbeat` and `/challenges`.

## Harness Tests

The harness has its own focused tests:

```powershell
mvn -pl challengerAuto -am `
  "-Dtest=ChallengerAutoConfigTest,ChallengerAutoLocalArgsTest,ChallengerAutoRuntimeHarnessCheck,ChallengerAutoSmokeTest" `
  "-DfailIfNoTests=false" test
```

These explicit harness checks cover:

- config parsing, defaults, aliases, invalid values, and property/env precedence;
- generated `ChallengeMain` args for repository and player-mode variants;
- owned local JVM startup for memory single-player and SQLite-memory multiplayer;
- smoke endpoints for the selected target;
- external target safety defaults.

Use the harness checks when changing `challengerAuto` startup/config behavior. `ChallengerAutoRuntimeHarnessCheck` is deliberately not part of normal Surefire discovery because it starts additional local Challenger modes. Use the full suite commands above when validating Challenger behavior in a specific environment.
