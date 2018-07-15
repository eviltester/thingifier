package uk.co.compendiumdev.thingifier.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidationReport {


    private boolean validity;
    private List<String> errorMessages;

    public ValidationReport(){
        validity=true;
        errorMessages = new ArrayList<>();
    }

    public void setValid(boolean validity) {
        this.validity=validity;
    }

    public void combine(ValidationReport report) {
        if(!report.isValid()){
            setValid(false);
        }

        errorMessages.addAll(report.getErrorMessages());
    }

    public Collection<String> getErrorMessages() {
        return errorMessages;
    }

    public boolean isValid() {
        return validity;
    }

    public void addErrorMessage(String errorMessage) {
        errorMessages.add(errorMessage);
    }
}
