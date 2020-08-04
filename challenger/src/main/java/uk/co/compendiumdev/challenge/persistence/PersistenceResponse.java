package uk.co.compendiumdev.challenge.persistence;

import uk.co.compendiumdev.challenge.ChallengerAuthData;

public class PersistenceResponse {

    private ChallengerAuthData authData;
    private boolean success;
    private String errorMessage;

    public PersistenceResponse(){
        this.errorMessage="";
        this.success=true;
        this.authData=null;
    }
    public PersistenceResponse withSuccess(final boolean successStatus) {
        this.success=successStatus;
        return this;
    }

    public PersistenceResponse withErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    public PersistenceResponse withChallengerAuthData(final ChallengerAuthData authData) {
        this.authData=authData;
        return this;
    }

    public ChallengerAuthData getAuthData() {
        return this.authData;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
