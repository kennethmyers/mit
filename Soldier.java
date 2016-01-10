package team168;


import battlecode.common.MapLocation;
import battlecode.common.Signal;

public class Soldier extends RobotPlayer {

    protected static void playTurn() {

        Signal[] signals = getAlliedComplexSignalsOnly();
        for (int i = 0; i < signals.length; i++) {
            int[] message = signals[i].getMessage();
            if (message[0] == LEADER_COMMAND) {
                if (message[1] == MUSTER_AT_LOCATION) {
                    i++;
                    message = signals[i].getMessage();

                    MapLocation newLocation = new MapLocation(message[0], message[1]);

                    makeBestFirstMoveAndClearRubble(myLocation.directionTo(newLocation));
                }
            }
        }
    }
}
