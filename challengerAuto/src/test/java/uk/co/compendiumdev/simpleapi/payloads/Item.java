package uk.co.compendiumdev.simpleapi.payloads;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

@XmlRootElement(name = "item") // for RestAssuredXML Serialisation using Jaxb
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item {

    public Integer id;
    public String type;
    public String isbn13;
    public BigDecimal price;
    public Integer numberinstock;
}
