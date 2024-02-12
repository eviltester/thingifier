package uk.co.compendiumdev.challenger.payloads;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "errors") // for RestAssuredXML Serialisation using Jaxb
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessages {
    public List<String> errorMessages;
}
