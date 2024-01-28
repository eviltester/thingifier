package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.core.EntityRelModel;

import java.util.Map;

import static uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi.HTTP_SESSION_HEADER_NAME;

public class SessionHeaderParser {

    public static String getDatabaseNameFromHeaderValue(final Map<String, String> requestHeaders){
        String sessionHeaderValue = requestHeaders.get(HTTP_SESSION_HEADER_NAME.toLowerCase());
        String instanceDatabaseName = EntityRelModel.DEFAULT_DATABASE_NAME;
        if(sessionHeaderValue!=null){
            instanceDatabaseName = sessionHeaderValue;
        }

        return instanceDatabaseName;
    }
}
