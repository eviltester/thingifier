package uk.co.compendiumdev.thingifier.api.http.headers.headerparser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Parses an HTTP Basic Authorization header.
 *
 * <p>The parser separates syntactic validity from credential matching so route-level auth can
 * reject malformed headers before application authenticators are called, while older code can keep
 * using {@link #matches(String, String)} for direct username/password checks.
 */
public class BasicAuthHeaderParser {
    private final String authHeader;
    private String password;
    private String basic;
    private String base64UserNamePass;
    private String username;
    private boolean valid;

    public BasicAuthHeaderParser(final String header) {
        if (header == null) {
            this.authHeader = "";
        } else {
            this.authHeader = header;
        }

        this.basic = "";
        this.base64UserNamePass = "";
        this.username = "";
        this.password = "";
        this.valid = false;

        splitParts(this.authHeader);
        decodeBase64();
    }

    private void decodeBase64() {
        if (this.base64UserNamePass.length() == 0) {
            return;
        }

        try {
            String usernamePassword =
                    new String(
                            Base64.getDecoder().decode(base64UserNamePass), StandardCharsets.UTF_8);
            final int separator = usernamePassword.indexOf(":");
            if (separator <= 0 || separator == usernamePassword.length() - 1) {
                return;
            }
            this.username = usernamePassword.substring(0, separator);
            this.password = usernamePassword.substring(separator + 1);
            this.valid = true;

        } catch (Exception e) {
            // ignore
        }
    }

    private void splitParts(final String authHeader) {

        List<String> parts = new ArrayList<>();

        String[] theparts = authHeader.split(" ");
        for (String aPart : theparts) {
            if (aPart.trim().length() > 0) {
                parts.add(aPart);
            }
        }
        if (parts.size() >= 1) {
            basic = parts.get(0).toLowerCase();
        }
        if (parts.size() == 2) {
            base64UserNamePass = parts.get(1);
        }
    }

    /**
     * Reports whether the header starts with the Basic auth scheme.
     *
     * @return true when the Authorization header uses Basic auth syntax
     */
    public boolean isBasicAuth() {
        return basic.equals("basic");
    }

    /**
     * Reports whether the header is syntactically valid Basic credentials.
     *
     * <p>Valid means the header uses the Basic scheme, has exactly one credential value, decodes as
     * Base64, and contains a non-empty username and password separated by the first colon.
     *
     * @return true when username and password can be safely read
     */
    public boolean isValid() {
        return isBasicAuth() && valid;
    }

    /**
     * Returns the parsed Basic username.
     *
     * @return username, or an empty string when the header is invalid
     */
    public String username() {
        return username;
    }

    /**
     * Returns the parsed Basic password.
     *
     * @return password, or an empty string when the header is invalid
     */
    public String password() {
        return password;
    }

    /**
     * Checks parsed Basic credentials against expected values.
     *
     * <p>This method remains for older callers. It now delegates through {@link #isValid()} so
     * malformed Basic headers are never treated as partial credential matches.
     *
     * @param username expected username
     * @param password expected password
     * @return true when the header is valid Basic auth and both values match
     */
    public boolean matches(final String username, final String password) {

        if (!isValid()) {
            return false;
        }

        if (username == null) {
            return false;
        }

        if (password == null) {
            return false;
        }

        return this.username.contentEquals(username) && this.password.contentEquals(password);
    }
}
