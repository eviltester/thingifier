package uk.co.compendiumdev.challenge.challengesrouting;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

public class ChallengerIpAddressTrackerTest {

    @Test
    public void canPurgeOldIps(){

        ChallengerIpAddressTracker tracker = new ChallengerIpAddressTracker(2, true);

        tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());
        tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());
        tracker.trackAgainstThisIp("123.123.123.124", UUID.randomUUID().toString());
        tracker.trackAgainstThisIp("123.123.123.124", UUID.randomUUID().toString());

        tracker.purgeEmptyIpAddresses(new HashSet<>());

        Assertions.assertFalse(tracker.isTrackingIp("123.123.123.123"));
        Assertions.assertFalse(tracker.isTrackingIp("123.123.123.124"));
    }

    @Test
    public void canPurgeOldChallengers(){

        ChallengerIpAddressTracker tracker = new ChallengerIpAddressTracker(2, true);

        String keepChallenger = UUID.randomUUID().toString();
        tracker.trackAgainstThisIp("123.123.123.123", keepChallenger);
        tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());

        HashSet<String> challengers = new HashSet<>();
        challengers.add(keepChallenger);

        tracker.purgeEmptyIpAddresses(challengers);

        Assertions.assertTrue(tracker.isTrackingIp("123.123.123.123"));
        Assertions.assertEquals(1, tracker.countFor("123.123.123.123"));
    }

    @Test
    public void willOnlyTrackACertainNumber(){

        ChallengerIpAddressTracker tracker = new ChallengerIpAddressTracker(2, true);

        tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());
        tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());
        boolean added = tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());

        Assertions.assertTrue(tracker.isTrackingIp("123.123.123.123"));
        Assertions.assertEquals(2, tracker.countFor("123.123.123.123"));
        Assertions.assertFalse(added);
    }

    @Test
    public void canSwitchOffIPAddressTracking(){

        ChallengerIpAddressTracker tracker = new ChallengerIpAddressTracker(2, false);

        tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());
        tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());
        boolean added = tracker.trackAgainstThisIp("123.123.123.123", UUID.randomUUID().toString());

        HashSet<String> challengers = new HashSet<>();
        challengers.add(UUID.randomUUID().toString());
        challengers.add(UUID.randomUUID().toString());
        tracker.purgeEmptyIpAddresses(challengers);

        Assertions.assertFalse(tracker.isTrackingIp("123.123.123.123"));
        Assertions.assertEquals(0, tracker.countFor("123.123.123.123"));
        Assertions.assertFalse(added);
        Assertions.assertFalse(tracker.hasLimitBeenReachedFor("123.123.123.123"));
    }
}
