package uk.co.compendiumdev.thingifier.application.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.NamedValue;

public final class BodyFieldValue {

    public enum SourceType {
        STRING("STRING"),
        BOOLEAN("BOOLEAN"),
        INTEGER("INTEGER"),
        NUMERIC("NUMERIC"),
        OBJECT("OBJECT"),
        ARRAY("ARRAY"),
        NULL("NULL"),
        SOMETHING_ELSE("Something Else");

        private final String displayName;

        SourceType(final String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private final String name;
    private final String value;
    private final SourceType sourceType;

    public BodyFieldValue(final String name, final String value, final SourceType sourceType) {
        this.name = name;
        this.value = value;
        this.sourceType = sourceType == null ? SourceType.SOMETHING_ELSE : sourceType;
    }

    public static List<BodyFieldValue> fromNamedValues(final List<NamedValue> values) {
        List<BodyFieldValue> bodyFields = new ArrayList<>();
        for (NamedValue value : values) {
            bodyFields.add(
                    new BodyFieldValue(value.getName(), value.asString(), SourceType.STRING));
        }
        return Collections.unmodifiableList(bodyFields);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String sourceTypeDisplayName() {
        return sourceType.displayName();
    }
}
