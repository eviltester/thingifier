package uk.co.compendiumdev.thingifier.core.query.urlparamsparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.co.compendiumdev.thingifier.core.query.FilterBy;
import uk.co.compendiumdev.thingifier.core.query.QueryFilterParams;
import uk.co.compendiumdev.thingifier.core.query.fromurl.UrlParamParser;

import java.util.List;

public class FilterableParamsTest {

    /*
    The default Spark parsing for url params is a split by & and then a split by =

    We want to be able to filter and sort e.g. ?id>=2&sortBy=-id&id<=16

    This would come through as (id>,2) and (sortBy,-id) so we would lose the >= and it is on the wrong side for us.

     */

    @ParameterizedTest
    @CsvSource({"id%3E%3D4,id>=4", "id%3E=4,id>=4"})
    public void canProcessUrlEncoded(String encoded, String decoded){

        UrlParamParser parser = new UrlParamParser();
        String decodedValue = parser.urlDecode(encoded);
        Assertions.assertEquals(decoded, decodedValue);
    }

    @Test
    public void canSplitGreaterThanEqual(){
        UrlParamParser parser = new UrlParamParser();
        QueryFilterParams values = parser.parse("id%3E%3D4");
        Assertions.assertEquals("id", values.get(0).fieldName);
        Assertions.assertEquals(">=", values.get(0).filterOperation);
        Assertions.assertEquals("4", values.get(0).fieldValue);
    }

    @Test
    public void canSplitIntoGreaterThanEqualAndLessThanEqual(){
        UrlParamParser parser = new UrlParamParser();
        QueryFilterParams values = parser.parse("id%3E%3D4&id<=7");
        Assertions.assertEquals(2, values.size());

        Assertions.assertEquals("id", values.get(0).fieldName);
        Assertions.assertEquals(">=", values.get(0).filterOperation);
        Assertions.assertEquals("4", values.get(0).fieldValue);

        Assertions.assertEquals("id", values.get(1).fieldName);
        Assertions.assertEquals("<=", values.get(1).filterOperation);
        Assertions.assertEquals("7", values.get(1).fieldValue);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id%3E%3D4&", "id%3E%3D4&     ", "id%3E%3D4&&&&", "id%3E%3D4&%20  && & "})
    public void canHandleNullParams(){
        UrlParamParser parser = new UrlParamParser();
        QueryFilterParams values = parser.parse("id%3E%3D4&");
        Assertions.assertEquals(1, values.size());

        Assertions.assertEquals("id", values.get(0).fieldName);
        Assertions.assertEquals(">=", values.get(0).filterOperation);
        Assertions.assertEquals("4", values.get(0).fieldValue);
    }

    @Test
    public void canHandleNullUrl(){
        UrlParamParser parser = new UrlParamParser();
        QueryFilterParams values = parser.parse(null);
        Assertions.assertEquals(0, values.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "     ", ""})
    public void canHandleEmptyStrings(String aString){
        UrlParamParser parser = new UrlParamParser();
        QueryFilterParams values = parser.parse(aString);
        Assertions.assertEquals(0, values.size());
    }

    @Test
    public void processPartialFilterWithoutOperator(){
        UrlParamParser parser = new UrlParamParser();
        QueryFilterParams values = parser.parse("fieldname4");
        Assertions.assertEquals(1, values.size());

        Assertions.assertEquals("fieldname4", values.get(0).fieldName);
        Assertions.assertEquals("=", values.get(0).filterOperation, "expected = by default");
        Assertions.assertEquals("", values.get(0).fieldValue);
    }

    @Test
    public void processPartialFilterWithoutValue(){
        UrlParamParser parser = new UrlParamParser();
        QueryFilterParams values = parser.parse("fieldname>=");
        Assertions.assertEquals(1, values.size());

        Assertions.assertEquals("fieldname", values.get(0).fieldName);
        Assertions.assertEquals(">=", values.get(0).filterOperation);
        Assertions.assertEquals("", values.get(0).fieldValue);
    }
}
