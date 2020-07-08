package uk.co.compendiumdev.thingifier.generic.instances;

import java.util.*;

public class InstanceFields {

    private Map<String, String> values = new HashMap<String, String>();

    public void addValue(String fieldName, String value) {
        values.put(fieldName.toLowerCase(), value);
    }

    public String getValue(String fieldName) {
        return values.get(fieldName.toLowerCase());
    }

    public List<String> getFields() {
        List<String> fields = new ArrayList<String>(values.keySet());
        return fields;
    }

    public String toString() {

        StringBuilder output = new StringBuilder();

        for (Map.Entry<String, String> entry : values.entrySet()) {
            output.append("\n\t\t\t\t" + entry.getKey() + " : " + entry.getValue() + "\n");
        }

        return output.toString();
    }


    public void deleteAllFieldsExcept(final String... fieldNamesToIgnore) {

        Set<String> ignorekeys = new HashSet<>(Arrays.asList(fieldNamesToIgnore));
        Set<String> keys = new HashSet(values.keySet());

        for (String key : keys) {
            if (!ignorekeys.contains(key)) {
                values.remove(key);
            }
        }
    }

    public Map<String, String> asMap() {
        HashMap<String, String> aMap = new HashMap<String, String>();
        aMap.putAll(values);
        return aMap;
    }

    public boolean hasFieldNamed(String fieldName) {
        return values.keySet().contains(fieldName);
    }
}
