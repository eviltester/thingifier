package uk.co.compendiumdev.thingifier.api.http.bodyparser.xml;

public class GenericXMLPrettyPrinter {

    /*
        a very basic and crude 'pretty printer
        for html or xml - assumes valid xml
        XMLParserFactory.create(someXml, new ArrayList<>()).prettyPrint();
     */
    public String prettyPrint(final String someXml) {

        int indentLevel = 0;
        StringBuilder sb = new StringBuilder();
        String process = someXml.trim();
        boolean endTag = false;
        for (int i = 0, length = process.length(); i < length; i++) {
            char c = process.charAt(i);
            switch (c) {
                case '<':
                    if(process.charAt(i+1)=='/'){
                        indentLevel--;
                        if(endTag){
                            // this is wrapping end tag
                            sb.append(String.format("%n"));
                            indent(sb, indentLevel);

                        }
                        endTag=true;
                    }else{
                        endTag=false;
                        if(i!=0){
                            indentLevel++;
                            sb.append(String.format("%n"));
                        }
                        indent(sb, indentLevel);
                    }
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '/':
                    if(process.charAt(i+1)=='>'){
                        // handle self closing empty tags
                        endTag=true;
                        indentLevel--;
                    }
                    sb.append("/");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public String asHtml(final String html) {
        return html.replace("<", "&lt;").replace(">", "&gt;");
    }

    private void indent(final StringBuilder sb, final int indentLevel) {
        String indentAs = "  ";
        for(int spaceCount=0;spaceCount<indentLevel;spaceCount++){
            sb.append(indentAs);
        }
    }

    public String prettyPrintHtml(final String singleObjectXml) {
        return asHtml(prettyPrint(singleObjectXml));
    }
}
