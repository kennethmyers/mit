package team168;


import battlecode.common.*;

import java.util.ArrayList;

public class Archon extends RobotPlayer {

    // Roles
    protected static final int NO_ROLE = 0;
    protected static final int LEADER = 1;
    protected static final int FOLLOWER = 2;




    protected static ArrayList<LocationReport> reports = new ArrayList<>();
    //protected static Stack<MapLocation> orders = new Stack<>();
    static MapLocation order = null;
    protected static int role = NO_ROLE;

    protected static void playTurn() {

        if (rc.getRoundNum() == 0) {
            // Hold election for lead Archon on first round
            holdElectionForArchonLeader();
            tryToCreateRobot(SCOUT);
            Clock.yield();
        }

        if (role == LEADER) {
            Signal[] signals = getAlliedComplexSignalsOnly();
            for (int i = 0; i < signals.length; i++) {
                int[] message = signals[i].getMessage();
                int reportType = message[REPORT_TYPE_INDEX];
                if (VALID_SIGNAL_TYPES.contains(reportType)) {
                    int reportData = message[REPORT_DATA_INDEX];

                    i++;
                    int[] locationMessage = signals[i].getMessage();
                    MapLocation reportLocation = new MapLocation(locationMessage[X_COORDINATE],
                            locationMessage[Y_COORDINATE]);

                    LocationReport report = new LocationReport(reportLocation, reportType, reportData, roundNumber);
                    if (! reports.contains(report)) {
                        reports.add(report);
                    }
                }
            }

            if (order == null) {
                for (LocationReport report : reports) {
                    if (report.getReportType() == ZOMBIE_DEN_SIGNAL) {
                        rc.setIndicatorString(1, "ZOMBIE DEN");
                        MapLocation location = report.getReportLocation();
                        order = location;
                    }
                }
            }

            if (order != null) {
                if (rc.getRoundNum() % 5 == 0) {
                    try {
                        rc.broadcastMessageSignal(LEADER_COMMAND, MUSTER_AT_LOCATION, TRANSMISSION_RANGE);
                        rc.broadcastMessageSignal(order.x, order.y, TRANSMISSION_RANGE);
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (order != null) {
                makeBestFirstMoveAndClearRubble(myLocation.directionTo(order));
            }

            rc.setIndicatorString(2, String.format("%d reports recieved form scouts: ", reports.size()));
        }

        if (role == FOLLOWER) {
            tryToCreateRobot(SOLDIER);
            Signal[] signals = getAlliedComplexSignalsOnly();
            for (int i = 0; i < signals.length; i++) {
                int[] message = signals[i].getMessage();
                if (message[0] == LEADER_COMMAND) {
                    if (message[1] == MUSTER_AT_LOCATION) {
                        rc.setIndicatorString(2, "Muster command received");
                        i++;
                        message = signals[i].getMessage();

                        MapLocation newLocation = new MapLocation(message[0], message[1]);

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
            rc.broadcastMessageSignal(ELECTION_SIGNAL_1, ELECTION_SIGNAL_2, TRANSMISSION_RANGE);
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
