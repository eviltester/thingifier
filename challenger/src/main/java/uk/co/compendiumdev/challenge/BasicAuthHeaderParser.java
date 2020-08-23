package uk.co.compendiumdev.challenge;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BasicAuthHeaderParser {
    private final String authHeader;
    private String password;
    private String basic;
    private String base64UserNamePass;
    private String username;

    public BasicAuthHeaderParser(final String header) {
        if(header==null){
            this.authHeader = "";
        }else{
            this.authHeader= header;
        }

        this.basic = "";
        this.base64UserNamePass = "";
        this.username="";
        this.password="";

        splitParts(this.authHeader);
        decodeBase64();


    }

    private void decodeBase64() {
        if(this.base64UserNamePass.length()==0)
            return;

        try {
            String usernamePassword = new String(Base64.getDecoder().decode(base64UserNamePass));
            final String[] up = usernamePassword.split(":");
            if(up.length>=1){
                this.username = up[0];
            }
            if(up.length>=2){
                this.password = up[1];
            }

        }catch(Exception e){
            // ignore
        }

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
            basic = parts.get(0).toLowerCase();
        }
        if(parts.size()>=2){
            base64UserNamePass = parts.get(1);
        }
    }


    public boolean matches(final String username, final String password) {

        if(!basic.equals("basic")){
            return false;
        }

        if(username==null){
            return false;
        }

        if(password==null){
            return false;
        }

        return this.username.contentEquals(username) &&
                this.password.contentEquals(password);
    }
}
