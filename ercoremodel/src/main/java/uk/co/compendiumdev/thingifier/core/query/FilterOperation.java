package uk.co.compendiumdev.thingifier.core.query;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public enum FilterOperation {
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN_OR_EQUAL(">="),
    EXACT_MATCH("=="),
    NOT_EQUALS("!="),
    REGEX_MATCH("~="),
    WILDCARD_MATCH("*="),
    LITERAL_CONTAINS("%="),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    EQUALS("="),
    NOT("!");

    private static final List<FilterOperation> PREFIX_PARSE_ORDER =
            List.of(
                    LESS_THAN_OR_EQUAL,
                    GREATER_THAN_OR_EQUAL,
                    EXACT_MATCH,
                    NOT_EQUALS,
                    REGEX_MATCH,
                    WILDCARD_MATCH,
                    LITERAL_CONTAINS,
                    LESS_THAN,
                    GREATER_THAN,
                    EQUALS,
                    NOT);

    private final String token;

    FilterOperation(final String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    public static ParsedValue parseLeadingToken(final String value) {
        String parseValue = value == null ? "" : value;
        for (FilterOperation operation : PREFIX_PARSE_ORDER) {
            if (parseValue.startsWith(operation.token())) {
                return new ParsedValue(operation, parseValue.substring(operation.token().length()));
            }
        }
        return new ParsedValue(EQUALS, parseValue);
    }

    public static Optional<FilterOperation> firstIn(final String value) {
        if (value == null || value.isEmpty()) {
            return Optional.empty();
        }

        return PREFIX_PARSE_ORDER.stream()
                .map(operation -> new OperationIndex(operation, value.indexOf(operation.token())))
                .filter(candidate -> candidate.index() >= 0)
                .min(
                        Comparator.comparingInt(OperationIndex::index)
                                .thenComparing(
                                        candidate -> -candidate.operation().token().length()))
                .map(OperationIndex::operation);
    }

    public record ParsedValue(FilterOperation operation, String value) {}

    private record OperationIndex(FilterOperation operation, int index) {}
}
