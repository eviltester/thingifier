package uk.co.compendiumdev.challenge;

import java.util.ArrayList;
import java.util.List;

public class BearerAuthHeaderParser {
    private final String header;
    private String bearer;
    private String token;

    public BearerAuthHeaderParser(final String header) {

        if(header==null){
            this.header = "";
        }else{
            this.header= header;
        }

        this.bearer = "";
        this.token = "";

        splitParts(this.header);
    }


    public boolean isBearerToken() {
        return bearer.equalsIgnoreCase("bearer");
    }


    public boolean isValid() {
        return isBearerToken() && token.length()>0;
    }

    public String getToken() {
        return token;
    }

    private void splitParts(final String authHeader) {

        List<String> parts = new ArrayList<>();

        String[] theparts = authHeader.split(" ");
        for(String aPart : theparts){
            if(aPart.trim().length()>0){
                parts.add(aPart);
            }
        }
        if(parts.size()>=1){
            bearer = parts.get(0).toLowerCase();
        }
        if(parts.size()>=2){
            token = parts.get(1);
        }
    }
}
