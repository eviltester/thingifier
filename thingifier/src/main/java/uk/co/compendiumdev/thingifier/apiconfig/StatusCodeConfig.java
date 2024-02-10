package uk.co.compendiumdev.thingifier.apiconfig;

/* configurable because different apis return different codes under different situations
    e.g. some APIs are 400 for every client error

    //todo: have some high level methods e.g. set400ForAllClientErrors()
 */
public class StatusCodeConfig {

    // making setting public at the moment

    /* client side errors */

    public int acceptTypeNotSupportedValue; // 406
    public int contentTypeNotSupportedValue; // 415

    private int maxRequestBodyLengthBytes =-1; // by default no limit on request size, if set then generate 413 when exceeded
    private int maxRequestContentLengthBytes = -1; // by default no limit on content size, this includes headers and generates 413 when exceeded

    public StatusCodeConfig(){
        resetClientSideErrorStatusCodes();
    }

    public void resetClientSideErrorStatusCodes(){
        acceptTypeNotSupportedValue=406;
        contentTypeNotSupportedValue=415;
    }

    public int acceptTypeNotSupported() {
        return acceptTypeNotSupportedValue;
    }

    public void setFrom(final StatusCodeConfig statusCodes) {
        acceptTypeNotSupportedValue = statusCodes.acceptTypeNotSupported();
        contentTypeNotSupportedValue = statusCodes.contentTypeNotSupported();
    }

    public int contentTypeNotSupported() {
        return contentTypeNotSupportedValue;
    }

    public int getMaxRequestBodyLengthBytes() {
        return maxRequestBodyLengthBytes;
    }

    public void setMaxRequestBodyLengthBytes(int maxRequestLengthBytes) {
        this.maxRequestBodyLengthBytes = maxRequestLengthBytes;
    }

    public boolean hasMaxContentLength() {
        return maxRequestBodyLengthBytes>-1;
    }
}
