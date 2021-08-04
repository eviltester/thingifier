//package uk.co.compendiumdev.thingifier.api.http.bodyparser.xml;
//
//import com.google.gson.Gson;
//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.converters.Converter;
//import com.thoughtworks.xstream.converters.MarshallingContext;
//import com.thoughtworks.xstream.converters.UnmarshallingContext;
//import com.thoughtworks.xstream.io.HierarchicalStreamReader;
//import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
//import com.thoughtworks.xstream.io.StreamException;
//import com.thoughtworks.xstream.io.xml.DomDriver;
//import org.json.JSONObject;
//import org.json.XML;
//
//import java.util.*;
//
//public class XMLParserUsingXstream implements XMLParserAbstraction {
//    private final String xml;
//    private final List<String> thingNames;
//
//    // TODO: push all XML json usage in here to build an abstraction
//    //       and see what the interface is
//    //       then create implementations that wrap other XML libraries
//    //       to see what works best for our purpose
//
//    public XMLParserUsingXstream(String xml, List<String> thingNames){
//        this.xml = xml;
//        this.thingNames = thingNames;
//    }
//
//    public String validateXML(){
//        try{
//            XStream xStream = new XStream(new DomDriver());
//            xStream.alias("map", java.util.Map.class);
//            Map<String,Object> map = (Map<String,Object>) xStream.fromXML(this.xml);
//        }catch(Exception e){
//            if(e instanceof StreamException) {
//                return e.getCause().toString();
//            }else{
//                return e.getMessage();
//            }
//        }
//
//        return "";
//    }
//
//    // XStream does not like root elements it does not know
//    public Map<String, Object> xmlAsMap(){
//        XStream xStream = new XStream(new DomDriver());
//        xStream.registerConverter(new MapEntryConverter());
//        Map<String,Object> map = (Map<String,Object>) xStream.fromXML(this.xml);
//        if (map.keySet().size() == 1) {
//            // if the key is an entity type then we just want the body
//            ArrayList<String> keys = new ArrayList<String>(map.keySet());
//
//            if (thingNames.contains(keys.get(0))) {
//                // just the body
//                Map<String,Object> bodyMap = new HashMap<String, Object>();
//                for (Map.Entry<String, Object> entry: bodyMap.entrySet()) {
//                    bodyMap.put(entry.getKey(), entry.getValue());
//                }
//                return bodyMap;
//            }
//        }
//        return map;
//    }
//
//
//    // https://stackoverflow.com/Questions/1537207/how-to-convert-xml-to-java-util-map-and-vice-versa
//    private class MapEntryConverter implements Converter {
//
//        public boolean canConvert(Class clazz) {
//            return AbstractMap.class.isAssignableFrom(clazz);
//        }
//
//        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
//
//            AbstractMap map = (AbstractMap) value;
//            for (Object obj : map.entrySet()) {
//                Map.Entry entry = (Map.Entry) obj;
//                writer.startNode(entry.getKey().toString());
//                Object val = entry.getValue();
//                if ( null != val ) {
//                    writer.setValue(val.toString());
//                }
//                writer.endNode();
//            }
//
//        }
//
//        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
//
//            Map<String, String> map = new HashMap<String, String>();
//
//            while(reader.hasMoreChildren()) {
//                reader.moveDown();
//
//                String key = reader.getNodeName(); // nodeName aka element's name
//                String value = reader.getValue();
//                map.put(key, value);
//
//                reader.moveUp();
//            }
//
//            return map;
//        }
//
//    }
//
//}
