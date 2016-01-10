package team168;


import battlecode.common.*;

import java.util.ArrayList;
import java.util.Stack;

public class Archon extends RobotPlayer {

    // Roles
    protected static final int NO_ROLE = 0;
    protected static final int LEADER = 1;
    protected static final int FOLLOWER = 2;




    protected static ArrayList<MapLocation> locationsOfInterest = new ArrayList<MapLocation>();
    protected static int role = NO_ROLE;

    protected static void playTurn() {

        if (rc.getRoundNum() == 0) {
            // Hold election for lead Archon on first round
            holdElectionForArchonLeader();
            tryToCreateRobot(SCOUT);
        }

        if (role == LEADER) {
            Signal[] signals = getAlliedComplexSignalsOnly();
            for (int i = 0; i < signals.length; i++) {
                int[] message = signals[i].getMessage();
                if (message[0] == 0 && message[0] == 0) {
                    int parts = message[1];
                    rc.setIndicatorString(1, "Incoming transmission from:" + signals[i].getRobotID());
                    i++;
                    message = signals[i].getMessage();
                    rc.setIndicatorString(2, String.format("Parts %d found at: %d %d", parts, message[0], message[1]));
                    locationsOfInterest.add(new MapLocation(message[0], message[1]));
                }
            }

            try {
                rc.broadcastMessageSignal(LEADER_COMMAND, MUSTER_AT_LOCATION, TRANSMISSION_RANGE);
                rc.broadcastMessageSignal(myLocation.x + 5, myLocation.y + 5, TRANSMISSION_RANGE);
            } catch (GameActionException e) {
                e.printStackTrace();
            }

            rc.setIndicatorString(2, String.format("%d reports recieved form scouts: ", locationsOfInterest.size()));


        }

        if (role == FOLLOWER) {
            tryToCreateRobot(SOLDIER);
            Signal[] signals = getAlliedComplexSignalsOnly();
            for (int i = 0; i < signals.length; i++) {
                int[] message = signals[i].getMessage();
                if (message[0] == LEADER_COMMAND) {
                    if (message[1] == MUSTER_AT_LOCATION) {
                        i++;
                        message = signals[i].getMessage();

                        MapLocation newLocation = new MapLocation(message[0], message[1]);

                        try {
                            // Rebroadcast
                            rc.broadcastMessageSignal(LEADER_COMMAND, MUSTER_AT_LOCATION, TRANSMISSION_RANGE);
                            rc.broadcastMessageSignal(message[0], message[1], TRANSMISSION_RANGE);
                        } catch (GameActionException e) {
                            e.printStackTrace();
                        }
                        makeBestFirstMoveAndClearRubble(myLocation.directionTo(newLocation));
                    }
                }
            }

        }
    }

    private static void collectParts() {

        MapLocation partsLocation = findLocationWithMostParts(mySensorRadius);
        if (partsLocation != null){
            rc.setIndicatorString(1, "Some parts at " + partsLocation);
            makeBestFirstMoveAndClearRubble(myLocation.directionTo(partsLocation));
        }
    }

    private static void holdElectionForArchonLeader() {
        try {
            rc.broadcastMessageSignal(0, 0, TRANSMISSION_RANGE);
        } catch (GameActionException e) {
            e.printStackTrace();
        }

        Signal[] electionSignals = getAlliedComplexSignalsOnly();
        if (electionSignals.length == 0) {
            // I am the leader!
            rc.setIndicatorString(0, "I am the leader!");
            role = LEADER;
        } else {
            role = FOLLOWER;
            rc.setIndicatorString(0, "At your service m'lord!");
        }
    }

    protected static void tryToCreateRobot(RobotType robotType) {
        if (rc.isCoreReady()) {
            if (rc.hasBuildRequirements(robotType)) {
                try {
                    for (Direction direction : directions) {
                        if (rc.canBuild(direction, robotType) && rc.isCoreReady()) {
                            rc.build(direction, robotType);
                        }
                    }
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
