package uk.co.compendiumdev.challenge;

import java.util.Base64;

public class BasicAuthHeader {
    private final String authHeader;
    private String password;
    private String[] parts;
    private String basic;
    private String base64UserNamePass;
    private String username;

    public BasicAuthHeader(final String header) {
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
        this.parts = authHeader.split(" ");
        if(parts.length>=1){
            basic = parts[0];
        }
        if(parts.length>=2){
            base64UserNamePass = parts[1];
        }
    }


    public boolean matches(final String username, final String password) {
        return this.username.contentEquals(username) &&
                this.password.contentEquals(password);
    }
}
