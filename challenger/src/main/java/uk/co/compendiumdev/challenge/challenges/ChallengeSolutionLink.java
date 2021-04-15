package uk.co.compendiumdev.challenge.challenges;

public class ChallengeSolutionLink {

    public final String linkText;
    public final String linkType;
    public final String linkData;

    public ChallengeSolutionLink(final String linkText, final String linkType, final String linkData) {
        this.linkText = linkText;
        this.linkType = linkType.toUpperCase();
        this.linkData = linkData;
    }

    public String asHtmlAHref() {
        if(linkType.equals("YOUTUBE")){
            return String.format("<a href='https://youtu.be/%s' target='_blank'>%s</a>",linkData, linkText);
        }
        return String.format("<a href='%s' target='_blank'>%s</a>",linkData, linkText);
    }
}
