package uk.co.compendiumdev.challenger.payloads;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "todo") // for RestAssuredXML Serialisation using Jaxb
public class Todo {

    public Integer id;
    public String title;
    public String description;
    public Boolean doneStatus;
}
