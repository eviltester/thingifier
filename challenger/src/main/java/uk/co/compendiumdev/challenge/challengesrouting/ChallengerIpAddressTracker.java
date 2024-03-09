package uk.co.compendiumdev.challenge.challengesrouting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChallengerIpAddressTracker {
    private final int maxChallengers;
    private final Map<String, ConcurrentSkipListSet<String>> ipaddresses;
    private final boolean addressLimitingOn;

    // todo: add a banned ip address list
    // todo: add a no-limit ip address list
    public ChallengerIpAddressTracker(int maxChallengersPerIp, boolean addressLimitingOn) {
        maxChallengers = maxChallengersPerIp;
        ipaddresses = new ConcurrentHashMap<>();
        this.addressLimitingOn=addressLimitingOn;
    }

    public void purgeEmptyIpAddresses(Set<String> existingChallengerGuids) {

        for(String anIp : ipaddresses.keySet()) {
            ConcurrentSkipListSet<String> challengerGuids = ipaddresses.get(anIp);
            List<String> challengersToRemove = new ArrayList<>();

            // find any challengers that have been purged
            for (String aChallengerGuid : challengerGuids) {
                if (!existingChallengerGuids.contains(aChallengerGuid)) {
                    challengersToRemove.add(aChallengerGuid);
                }
            }

            challengerGuids.removeAll(challengersToRemove);
        }

        // purge any empty ip addresses
        for(String anIp : ipaddresses.keySet()){
            if(ipaddresses.get(anIp).isEmpty()){
                ipaddresses.remove(anIp);
            }
        }
    }

    public boolean hasLimitBeenReachedFor(String ip) {

        if(!addressLimitingOn) return false;

        if(countFor(ip)<maxChallengers){
            return false;
        }

        return true;
    }

    public boolean trackAgainstThisIp(String ip, String xChallengerGuid) {

        if(!addressLimitingOn){
            return false;
        }

        if(hasLimitBeenReachedFor(ip)){
            return false;
        }

        if(isTrackingIp(ip)){
            ipaddresses.get(ip).add(xChallengerGuid);
        }else{
            ConcurrentSkipListSet<String> guids = new ConcurrentSkipListSet<>();
            guids.add(xChallengerGuid);
            ipaddresses.put(ip, guids);
        }
        return true;
    }

    public boolean isTrackingIp(String ip) {
        return ipaddresses.containsKey(ip);
    }

    public int countFor(String ip) {
        if(isTrackingIp(ip)){
            return ipaddresses.get(ip).size();
        }
        return 0;
    }
}
