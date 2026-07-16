package uk.co.compendiumdev.serverstart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class ChallengerAutoConfig {

    public static final String PROPERTY_TARGET = "challenger.auto.target";
    public static final String PROPERTY_BASE_URL = "challenger.auto.baseUrl";
    public static final String PROPERTY_LOCAL_REPOSITORY = "challenger.auto.local.repository";
    public static final String PROPERTY_LOCAL_PLAYER_MODE = "challenger.auto.local.playerMode";
    public static final String PROPERTY_LOCAL_PORT = "challenger.auto.local.port";
    public static final String PROPERTY_LOCAL_EXTRA_ARGS = "challenger.auto.local.extraArgs";
    public static final String PROPERTY_EXTERNAL_FULL = "challenger.auto.external.full";

    public static final String DEFAULT_EXISTING_BASE_URL = "http://localhost:4567";
    public static final String DEFAULT_LIVE_BASE_URL = "https://apichallenges.eviltester.com";

    public enum Target {
        LOCAL,
        EXISTING,
        LIVE
    }

    public enum Repository {
        MEMORY,
        SQLITE_MEMORY,
        SQLITE_FILE
    }

    public enum PlayerMode {
        SINGLE,
        MULTI
    }

    private final Target target;
    private final String baseUrl;
    private final Repository localRepository;
    private final PlayerMode localPlayerMode;
    private final String localPort;
    private final List<String> localExtraArgs;
    private final boolean externalFull;

    private ChallengerAutoConfig(
            final Target target,
            final String baseUrl,
            final Repository localRepository,
            final PlayerMode localPlayerMode,
            final String localPort,
            final List<String> localExtraArgs,
            final boolean externalFull) {
        this.target = target;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.localRepository = localRepository;
        this.localPlayerMode = localPlayerMode;
        this.localPort = localPort;
        this.localExtraArgs = Collections.unmodifiableList(new ArrayList<>(localExtraArgs));
        this.externalFull = externalFull;
        validate();
    }

    public static ChallengerAutoConfig current() {
        return from(systemPropertiesAsMap(), System.getenv());
    }

    public static ChallengerAutoConfig from(
            final Map<String, String> properties, final Map<String, String> environment) {
        Map<String, String> props = properties == null ? Collections.emptyMap() : properties;
        Map<String, String> env = environment == null ? Collections.emptyMap() : environment;

        Target target = parseTarget(valueFor(props, env, PROPERTY_TARGET, "local"));
        Repository repository =
                parseRepository(valueFor(props, env, PROPERTY_LOCAL_REPOSITORY, "sqlite-memory"));
        PlayerMode playerMode =
                parsePlayerMode(valueFor(props, env, PROPERTY_LOCAL_PLAYER_MODE, "multi"));
        String port = normalizePort(valueFor(props, env, PROPERTY_LOCAL_PORT, "auto"));
        List<String> extraArgs =
                parseExtraArgs(valueFor(props, env, PROPERTY_LOCAL_EXTRA_ARGS, ""));
        boolean externalFull = parseBoolean(valueFor(props, env, PROPERTY_EXTERNAL_FULL, "false"));

        String baseUrl = configuredBaseUrlFor(target, props, env);

        return new ChallengerAutoConfig(
                target, baseUrl, repository, playerMode, port, extraArgs, externalFull);
    }

    public static ChallengerAutoConfig localProfile(
            final Repository repository, final PlayerMode playerMode) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(PROPERTY_TARGET, "local");
        properties.put(PROPERTY_LOCAL_REPOSITORY, repositoryName(repository));
        properties.put(PROPERTY_LOCAL_PLAYER_MODE, playerModeName(playerMode));
        return from(properties, System.getenv());
    }

    public static ChallengerAutoConfig existingLocal() {
        return from(
                Map.of(PROPERTY_TARGET, "existing", PROPERTY_BASE_URL, DEFAULT_EXISTING_BASE_URL),
                System.getenv());
    }

    public static ChallengerAutoConfig liveSmoke() {
        return from(Map.of(PROPERTY_TARGET, "live"), System.getenv());
    }

    public Target getTarget() {
        return target;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Repository getLocalRepository() {
        return localRepository;
    }

    public PlayerMode getLocalPlayerMode() {
        return localPlayerMode;
    }

    public String getLocalPort() {
        return localPort;
    }

    public boolean isAutoPort() {
        return "auto".equals(localPort);
    }

    public int fixedPort() {
        if (isAutoPort()) {
            throw new IllegalStateException("Local port is configured as auto");
        }
        return Integer.parseInt(localPort);
    }

    public List<String> getLocalExtraArgs() {
        return localExtraArgs;
    }

    public boolean isExternalFull() {
        return externalFull;
    }

    public boolean isExternalTarget() {
        return target == Target.LIVE;
    }

    public boolean startsOwnedLocalProcess() {
        return target == Target.LOCAL;
    }

    public boolean shouldRunFullSuite() {
        return !isExternalTarget() || externalFull;
    }

    public String fullSuiteSkipReason() {
        if (shouldRunFullSuite()) {
            return "";
        }
        return "External Challenger target is smoke-only by default; set -D"
                + PROPERTY_EXTERNAL_FULL
                + "=true to run the mutating suite.";
    }

    public List<String> challengeMainArgs(final int port) {
        List<String> args = new ArrayList<>();
        args.add("-port=" + port);

        switch (localRepository) {
            case MEMORY:
                args.add("-thingifier-repository=memory");
                break;
            case SQLITE_MEMORY:
                args.add("-sqlite-memory");
                break;
            case SQLITE_FILE:
                args.add("-thingifier-repository=sqlite-file");
                break;
            default:
                throw new IllegalStateException("Unsupported repository " + localRepository);
        }

        if (localPlayerMode == PlayerMode.MULTI) {
            args.add("-multiplayer");
        }

        args.addAll(localExtraArgs);
        return args;
    }

    @Override
    public String toString() {
        return "ChallengerAutoConfig{"
                + "target="
                + target
                + ", baseUrl='"
                + baseUrl
                + '\''
                + ", localRepository="
                + localRepository
                + ", localPlayerMode="
                + localPlayerMode
                + ", localPort='"
                + localPort
                + '\''
                + ", localExtraArgs="
                + localExtraArgs
                + ", externalFull="
                + externalFull
                + '}';
    }

    private void validate() {}

    private static Map<String, String> systemPropertiesAsMap() {
        Properties properties = System.getProperties();
        Map<String, String> values = new LinkedHashMap<>();
        for (String name : properties.stringPropertyNames()) {
            values.put(name, properties.getProperty(name));
        }
        return values;
    }

    private static String configuredBaseUrlFor(
            final Target target,
            final Map<String, String> properties,
            final Map<String, String> environment) {
        switch (target) {
            case LOCAL:
                return "";
            case EXISTING:
                return valueFor(
                        properties, environment, PROPERTY_BASE_URL, DEFAULT_EXISTING_BASE_URL);
            case LIVE:
                return valueFor(properties, environment, PROPERTY_BASE_URL, DEFAULT_LIVE_BASE_URL);
            default:
                throw new IllegalArgumentException("Unsupported target " + target);
        }
    }

    private static String valueFor(
            final Map<String, String> properties,
            final Map<String, String> environment,
            final String propertyName,
            final String defaultValue) {
        String propertyValue = trimToNull(properties.get(propertyName));
        if (propertyValue != null) {
            return propertyValue;
        }

        String environmentValue = trimToNull(environment.get(envName(propertyName)));
        if (environmentValue == null) {
            environmentValue = trimToNull(environment.get(legacyEnvName(propertyName)));
        }
        if (environmentValue != null) {
            return environmentValue;
        }

        return defaultValue;
    }

    private static Target parseTarget(final String value) {
        String normalized = normalize(value);
        switch (normalized) {
            case "local":
            case "owned-local":
                return Target.LOCAL;
            case "existing":
            case "existing-local":
            case "local-existing":
                return Target.EXISTING;
            case "live":
            case "prod":
            case "production":
                return Target.LIVE;
            default:
                throw new IllegalArgumentException(
                        "Unknown challenger auto target "
                                + value
                                + ". Expected local, existing, or live.");
        }
    }

    private static Repository parseRepository(final String value) {
        String normalized = normalize(value);
        switch (normalized) {
            case "memory":
            case "in-memory":
            case "custom-memory":
                return Repository.MEMORY;
            case "sqlite":
            case "sqlite-memory":
            case "sqlite-in-memory":
                return Repository.SQLITE_MEMORY;
            case "sqlite-file":
            case "sqlite-disk":
            case "file":
                return Repository.SQLITE_FILE;
            default:
                throw new IllegalArgumentException(
                        "Unknown local repository "
                                + value
                                + ". Expected memory, sqlite-memory, or sqlite-file.");
        }
    }

    private static PlayerMode parsePlayerMode(final String value) {
        String normalized = normalize(value);
        switch (normalized) {
            case "single":
            case "single-player":
            case "singleplayer":
                return PlayerMode.SINGLE;
            case "multi":
            case "multiplayer":
            case "multi-player":
            case "multiuser":
            case "multi-user":
                return PlayerMode.MULTI;
            default:
                throw new IllegalArgumentException(
                        "Unknown local player mode " + value + ". Expected single or multi.");
        }
    }

    private static String normalizePort(final String value) {
        String port = trimToEmpty(value).toLowerCase(Locale.ROOT);
        if (port.isEmpty() || "auto".equals(port)) {
            return "auto";
        }
        try {
            int parsed = Integer.parseInt(port);
            if (parsed < 1 || parsed > 65535) {
                throw new IllegalArgumentException("Local port out of range: " + value);
            }
            return String.valueOf(parsed);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid local port " + value + ". Expected auto or a port number.", e);
        }
    }

    private static List<String> parseExtraArgs(final String value) {
        List<String> args = new ArrayList<>();
        if (isBlank(value)) {
            return args;
        }
        for (String part : value.split(",")) {
            String arg = part.trim();
            if (!arg.isEmpty()) {
                args.add(arg);
            }
        }
        return args;
    }

    private static boolean parseBoolean(final String value) {
        String normalized = normalize(value);
        return "true".equals(normalized)
                || "yes".equals(normalized)
                || "y".equals(normalized)
                || "1".equals(normalized);
    }

    private static String repositoryName(final Repository repository) {
        switch (repository) {
            case MEMORY:
                return "memory";
            case SQLITE_MEMORY:
                return "sqlite-memory";
            case SQLITE_FILE:
                return "sqlite-file";
            default:
                throw new IllegalStateException("Unknown repository " + repository);
        }
    }

    private static String playerModeName(final PlayerMode playerMode) {
        switch (playerMode) {
            case SINGLE:
                return "single";
            case MULTI:
                return "multi";
            default:
                throw new IllegalStateException("Unknown player mode " + playerMode);
        }
    }

    private static String envName(final String propertyName) {
        return propertyName
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toUpperCase(Locale.ROOT)
                .replace('.', '_');
    }

    private static String legacyEnvName(final String propertyName) {
        return propertyName.toUpperCase(Locale.ROOT).replace('.', '_');
    }

    private static String normalize(final String value) {
        return trimToEmpty(value).toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
    }

    private static String normalizeBaseUrl(final String value) {
        String baseUrl = trimToEmpty(value);
        while (baseUrl.endsWith("/") && baseUrl.length() > "https://".length()) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private static String trimToEmpty(final String value) {
        return value == null ? "" : value.trim();
    }

    private static String trimToNull(final String value) {
        String trimmed = trimToEmpty(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }
}
