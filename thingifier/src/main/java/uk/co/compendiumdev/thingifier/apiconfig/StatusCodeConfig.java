package uk.co.compendiumdev.thingifier.apiconfig;

/* configurable because different apis return different codes under different situations
    e.g. some APIs are 400 for every client error
 */
public class StatusCodeConfig {

    // making setting public at the moment
    public int acceptTypeNotSupportedValue;

    public StatusCodeConfig(){
        acceptTypeNotSupportedValue=406;
    }

    public int acceptTypeNotSupported() {
        return acceptTypeNotSupportedValue;
    }

}
