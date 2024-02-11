package uk.co.compendiumdev.thingifier.api.restapihandlers;

import uk.co.compendiumdev.thingifier.api.http.headers.HttpHeadersBlock;
import uk.co.compendiumdev.thingifier.core.EntityRelModel;

import java.util.Map;

import static uk.co.compendiumdev.thingifier.api.http.ThingifierHttpApi.HTTP_SESSION_HEADER_NAME;

public class SessionHeaderParser {

    public static String getDatabaseNameFromHeaderValue(final HttpHeadersBlock requestHeaders){
        String sessionHeaderValue = requestHeaders.get(HTTP_SESSION_HEADER_NAME);
        String instanceDatabaseName = EntityRelModel.DEFAULT_DATABASE_NAME;
        if(!sessionHeaderValue.isEmpty()){
            instanceDatabaseName = sessionHeaderValue;
        }

        return instanceDatabaseName;
    }
}
