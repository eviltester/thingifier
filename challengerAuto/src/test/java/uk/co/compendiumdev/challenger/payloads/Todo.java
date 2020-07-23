package uk.co.compendiumdev.challenger.payloads;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "todo") // for RestAssuredXML Serialisation using Jaxb
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Todo {

    public Integer id;
    public String title;
    public String description;
    public Boolean doneStatus;
}
