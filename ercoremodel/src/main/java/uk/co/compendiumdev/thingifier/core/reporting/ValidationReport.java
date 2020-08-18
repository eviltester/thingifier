package uk.co.compendiumdev.thingifier.core.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidationReport {


    private boolean validity;
    private List<String> errorMessages;

    public ValidationReport() {
        validity = true;
        errorMessages = new ArrayList<>();
    }

    public ValidationReport setValid(boolean validity) {
        this.validity = validity;
        return this;
    }

    public void combine(ValidationReport report) {
        if (!report.isValid()) {
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

    public ValidationReport addErrorMessage(String errorMessage) {
        errorMessages.add(errorMessage);
        return this;
    }

    public String getCombinedErrorMessages() {
        StringBuilder reasons = new StringBuilder();
        reasons.append("Failed Validation: ");
        String delimiter ="";
        for(String reason : errorMessages){
            reasons.append( delimiter + reason);
            delimiter = ", ";
        }
        return reasons.toString();
    }
}
