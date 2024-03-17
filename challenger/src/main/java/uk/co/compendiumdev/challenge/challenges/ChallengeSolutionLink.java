package uk.co.compendiumdev.challenge.challenges;

public class ChallengeSolutionLink {

    public final String linkText;
    public final String linkType;
    public final String linkData;

    public ChallengeSolutionLink(final String linkText, final String linkType, final String linkUrl) {
        this.linkText = linkText.trim();

        if(linkType==null){
            this.linkType = "";
        }else{
            this.linkType = linkType.trim().toUpperCase();
        }

        if(linkUrl==null) {
            this.linkData = "";
        }else{
            this.linkData = linkUrl.trim();
        }
    }

    public String asHtmlAHref() {
        if(linkType.equals("YOUTUBE")){
            return String.format("<a href='https://youtu.be/%s' target='_blank'>%s</a>",linkData, linkText);
        }
        if(linkData.isEmpty()){
            return linkText;
        }

        String target="target='_blank'";
        if(!linkData.startsWith("http")){
            target="";
        }
        return String.format("<a href='%s' %s>%s</a>",linkData, target, linkText);
    }
}
