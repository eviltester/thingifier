package uk.co.compendiumdev.thingifier.swaggerizer;

import com.google.gson.GsonBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import uk.co.compendiumdev.thingifier.Thingifier;

public class Swaggerizer {

    private final Thingifier thingifier;
    OpenAPI api;

    public Swaggerizer(Thingifier thingifier){
        this.thingifier = thingifier;
    }

    public OpenAPI swagger(){

        api = new OpenAPI();

        final Info info = new Info();
        info.setTitle(thingifier.getTitle());
        info.setDescription(thingifier.getInitialParagraph());
        api.setInfo(info);

        return api;
    }

    public String asJson(){
        if(api==null){
            swagger();
        }
        return new GsonBuilder().setPrettyPrinting().
                create().toJson(api);
    }
}
