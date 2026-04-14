package uk.co.compendiumdev.challenge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;

import java.util.function.BiConsumer;

public class IndexNowRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(IndexNowRouteHandler.class);

    static final String INDEX_NOW_KEY_LOCATION = "INDEX_NOW_KEY_LOCATION";
    static final String INDEX_NOW_KEY = "INDEX_NOW_KEY";

    private final String keyLocation;
    private final String key;
    private final BiConsumer<String, Route> getRouteRegistrar;

    public IndexNowRouteHandler() {
        this(System.getenv(INDEX_NOW_KEY_LOCATION),
                System.getenv(INDEX_NOW_KEY),
                Spark::get);
    }

    IndexNowRouteHandler(final String keyLocation,
                         final String key,
                         final BiConsumer<String, Route> getRouteRegistrar) {
        this.keyLocation = normalize(keyLocation);
        this.key = normalize(key);
        this.getRouteRegistrar = getRouteRegistrar;
    }

    public boolean configureRoutes() {

        if (!isConfigurationValid()) {
            logger.info("IndexNow endpoint not enabled because environment variables are missing or invalid");
            return false;
        }

        getRouteRegistrar.accept(keyLocation, (request, response) -> {
            if (response != null) {
                response.type("text/plain; charset=utf-8");
            }
            return key;
        });

        logger.info("IndexNow endpoint enabled at {}", keyLocation);
        return true;
    }

    private boolean isConfigurationValid() {
        return key != null && keyLocation != null && isValidLocation(keyLocation);
    }

    static boolean isValidLocation(final String location) {
        if (location == null || location.isBlank()) {
            return false;
        }

        if (!location.startsWith("/") || !location.endsWith(".txt")) {
            return false;
        }

        return !location.contains("*") &&
                !location.contains("?") &&
                !location.contains("#") &&
                !location.contains(" ") &&
                !location.contains("\\");
    }

    private static String normalize(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
