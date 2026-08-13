package uk.co.compendiumdev.thingifier.core.query;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;

public class EntityListFilterParamParser {

    private final List<FilterBy> filterByConditions;

    public EntityListFilterParamParser(final QueryFilterParams queryParams) {
        // because  a map is used to set this up we can't handle multiple conditions
        // TODO: the combo field would need to be configurable to allow entities
        // to have a field called comoband
        // need a different representation for combinations e.g. comboand=[id<1,id>10]
        this.filterByConditions = queryParams.toList();
    }

    public EntityListFilterParamParser(final List<FilterBy> queryParams) {
        this.filterByConditions = queryParams;
    }

    public boolean matches(final EntityInstance instance) {
        for (FilterBy filterByCondition : filterByConditions) {

            final EntityDefinition defn = instance.getEntity();

            String fieldName = filterByCondition.fieldName;
            if (SortByFieldName.isSortByParam(fieldName)
                    || PaginationParams.isPaginationParam(fieldName)) {
                continue;
            }

            // TODO: handle - ranges, like, or etc.
            // currently all conditions are treated as an AND clause e.g. ?ID=<10&ID=>5  would be is
            // 6, 7, 8, 9
            if (defn.hasFieldNameDefined(fieldName)) {
                String value = instance.getFieldValue(fieldName).asString();
                // get the actual value
                final ComparableFieldValue actualValue =
                        new ComparableFieldValue(
                                defn.getField(fieldName), instance.getFieldValue(fieldName));
                // create a comparison value
                final ComparableFieldValue filterConditionValue =
                        new ComparableFieldValue(
                                defn.getField(fieldName),
                                defn.getField(fieldName).valueFor(filterByCondition.fieldValue));

                switch (filterByCondition.filterOperation) {
                    case EXACT_MATCH:
                    case EQUALS:
                        if (!(actualValue.compareTo(filterConditionValue) == 0)) {
                            return false;
                        }
                        break;
                    case LESS_THAN:
                        if (!(actualValue.compareTo(filterConditionValue) < 0)) {
                            return false;
                        }
                        break;
                    case GREATER_THAN:
                        if (!(actualValue.compareTo(filterConditionValue) > 0)) {
                            return false;
                        }
                        break;
                    case LESS_THAN_OR_EQUAL:
                        if (!(actualValue.compareTo(filterConditionValue) <= 0)) {
                            return false;
                        }
                        break;
                    case GREATER_THAN_OR_EQUAL:
                        if (!(actualValue.compareTo(filterConditionValue) >= 0)) {
                            return false;
                        }
                        break;
                    case NOT_EQUALS:
                    case NOT:
                        if (!(actualValue.compareTo(filterConditionValue) != 0)) {
                            return false;
                        }
                        break;
                    case REGEX_MATCH:
                        { // regex match
                            Pattern pattern = Pattern.compile(filterByCondition.fieldValue);
                            Matcher matcher = pattern.matcher(actualValue.getValue().asString());
                            return matcher.matches();
                        }
                    case WILDCARD_MATCH:
                        { // wildcard match so * matches any multiple and ? matches one
                            String actualFilter = filterByCondition.fieldValue.replace("*", ".*");
                            actualFilter = actualFilter.replace("?", ".");
                            Pattern pattern = Pattern.compile(actualFilter);
                            Matcher matcher = pattern.matcher(actualValue.getValue().asString());
                            return matcher.matches();
                        }
                    case LITERAL_CONTAINS:
                        if (!actualValue
                                .getValue()
                                .asString()
                                .contains(filterByCondition.fieldValue)) {
                            return false;
                        }
                        break;
                    default:
                        System.out.println(
                                String.format(
                                        "Unhandled filterby condition %s%s%s",
                                        fieldName, filterByCondition.operationToken(), value));
                }
            }
        }

        return true;
    }

    public List<FilterBy> filterBys() {
        return filterByConditions;
    }
}
