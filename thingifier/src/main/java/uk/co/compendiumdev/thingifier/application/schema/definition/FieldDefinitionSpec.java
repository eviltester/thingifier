package uk.co.compendiumdev.thingifier.application.schema.definition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FieldDefinitionSpec {

    private final String name;
    private final String type;
    private final boolean required;
    private final boolean unique;
    private final String defaultValue;
    private final String description;
    private final List<String> examples;
    private final Integer truncateTo;
    private final String minValue;
    private final String maxValue;
    private final List<ValidationRuleSpec> validationRules;
    private final List<FieldDefinitionSpec> objectFields;

    private FieldDefinitionSpec(final Builder builder) {
        name = builder.name;
        type = builder.type;
        required = builder.required;
        unique = builder.unique;
        defaultValue = builder.defaultValue;
        description = builder.description;
        examples = Collections.unmodifiableList(new ArrayList<>(builder.examples));
        truncateTo = builder.truncateTo;
        minValue = builder.minValue;
        maxValue = builder.maxValue;
        validationRules = Collections.unmodifiableList(new ArrayList<>(builder.validationRules));
        objectFields = Collections.unmodifiableList(new ArrayList<>(builder.objectFields));
    }

    public static Builder named(final String name, final String type) {
        return new Builder(name, type);
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public boolean required() {
        return required;
    }

    public boolean unique() {
        return unique;
    }

    public String defaultValue() {
        return defaultValue;
    }

    public String description() {
        return description;
    }

    public List<String> examples() {
        return examples;
    }

    public Integer truncateTo() {
        return truncateTo;
    }

    public String minValue() {
        return minValue;
    }

    public String maxValue() {
        return maxValue;
    }

    public List<ValidationRuleSpec> validationRules() {
        return validationRules;
    }

    public List<FieldDefinitionSpec> objectFields() {
        return objectFields;
    }

    public boolean hasRange() {
        return minValue != null || maxValue != null;
    }

    public static final class Builder {

        private final String name;
        private final String type;
        private boolean required;
        private boolean unique;
        private String defaultValue;
        private String description;
        private final List<String> examples;
        private Integer truncateTo;
        private String minValue;
        private String maxValue;
        private final List<ValidationRuleSpec> validationRules;
        private final List<FieldDefinitionSpec> objectFields;

        private Builder(final String name, final String type) {
            this.name = name;
            this.type = type;
            examples = new ArrayList<>();
            validationRules = new ArrayList<>();
            objectFields = new ArrayList<>();
        }

        public Builder required(final boolean required) {
            this.required = required;
            return this;
        }

        public Builder unique(final boolean unique) {
            this.unique = unique;
            return this;
        }

        public Builder defaultValue(final String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        public Builder example(final String example) {
            examples.add(example);
            return this;
        }

        public Builder examples(final List<String> examples) {
            this.examples.addAll(examples);
            return this;
        }

        public Builder truncateTo(final Integer truncateTo) {
            this.truncateTo = truncateTo;
            return this;
        }

        public Builder range(final String minValue, final String maxValue) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            return this;
        }

        public Builder validationRule(final ValidationRuleSpec validationRule) {
            validationRules.add(validationRule);
            return this;
        }

        public Builder validationRules(final List<ValidationRuleSpec> validationRules) {
            this.validationRules.addAll(validationRules);
            return this;
        }

        public Builder objectField(final FieldDefinitionSpec objectField) {
            objectFields.add(objectField);
            return this;
        }

        public Builder objectFields(final List<FieldDefinitionSpec> objectFields) {
            this.objectFields.addAll(objectFields);
            return this;
        }

        public FieldDefinitionSpec build() {
            return new FieldDefinitionSpec(this);
        }
    }
}
