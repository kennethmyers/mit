package team168;


import battlecode.common.*;

import java.util.ArrayList;

public class Archon extends RobotPlayer {

    // Roles
    static final int NO_ROLE = 0;
    static final int LEADER = 1;
    static final int FOLLOWER = 2;

    static ArrayList<LocationReport> reports = new ArrayList<>();

    //protected static Stack<MapLocation> orders = new Stack<>();
    static MapLocation orderLocation = null;
    static LocationReport orderReport = null;
    static int role = NO_ROLE;

    static int leaderID = 0;
    static int turnsSinceLastLeaderHeartbeat = 0;
    static final int lastHeartBeatThreshold = 25;
    static final int NEW_LEADER_SIGNAL = 11;

    static void playTurn() {

        if (rc.getRoundNum() == 0) {
            // Hold election for lead Archon on first round
            holdElectionForArchonLeader();
            tryToCreateRobot(SCOUT);
            Clock.yield();
        }

        activateAdjacentNuetralBots();

        if (role == LEADER) {

            // Get any reports from scouts and save them
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
                        //rc.setIndicatorString(1, "New report filed " + report);
                        reports.add(report);
                    }
                }
            }

            // If there are no outstanding orders, issue a new one
            if (orderLocation == null && (reports.size() > 10 || roundNumber > 50)) {
                rc.setIndicatorString(2, "Setting new orders....");
                orderReport = getBestOrder();
                if (orderReport != null) {
                    orderLocation = orderReport.getReportLocation();
                    try {
                        //rc.setIndicatorString(1,  String.format("ZOMBIE DEN at %d %d ", orderLocation.x, orderLocation.y));
                        rc.broadcastMessageSignal(LEADER_COMMAND, MUSTER_AT_LOCATION, TRANSMISSION_RANGE);
                        rc.broadcastMessageSignal(orderLocation.x, orderLocation.y, TRANSMISSION_RANGE);
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }

            }

            // There is an active order issued
            if (orderLocation != null) {
                rc.setIndicatorString(0, "Current orders: " + orderReport);
                // Rebroadcast orders every 5 rounds for new bots.
                if (rc.getRoundNum() % 5 == 0) {
                    try {
                        rc.broadcastMessageSignal(LEADER_COMMAND, MUSTER_AT_LOCATION, TRANSMISSION_RANGE);
                        rc.broadcastMessageSignal(orderLocation.x, orderLocation.y, TRANSMISSION_RANGE);
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }


                if (rc.canSenseLocation(orderLocation)) {
                    boolean isOrderComplete = false;
                    if (orderReport.getReportType() == PARTS_SIGNAL) {
                        if (rc.senseParts(orderReport.getReportLocation()) == 0) {
                            isOrderComplete = true;
                        }
                    } else if (orderReport.getReportType() == NUETRAL_BOT_SIGNAL) {
                        try {
                            if (rc.senseRobotAtLocation(orderLocation).team == myTeam) {
                                isOrderComplete = true;
                            }
                        } catch (GameActionException e) {
                            e.printStackTrace();
                        }
                    } else if (orderReport.getReportType() == ZOMBIE_DEN_SIGNAL) {
                        try {
                            if (rc.senseRobotAtLocation(orderLocation).type != RobotType.ZOMBIEDEN) {
                                isOrderComplete = true;
                            }
                        } catch (GameActionException e) {
                            e.printStackTrace();
                        }
                    }

                    if (isOrderComplete) {
                        orderReport.setValid(false);
                        reports.remove(orderReport);
                        reports.add(orderReport);
                        orderReport = null;
                        orderLocation = null;
                        rc.setIndicatorString(1, "Orders complete.");
                    }
                }
            } else {
                rc.setIndicatorString(0, "No orders...");
            }


            if (orderLocation != null) {
                makeBestFirstMoveAndClearRubble(myLocation.directionTo(orderLocation));
            }

            //collectParts();

            rc.setIndicatorString(0, String.format("%d reports received from scouts: ", reports.size()));
        }

        if (role == FOLLOWER) {

            if (roundNumber > 100) {
                Signal[] leaderSignals = getAlliedComplexSignalsOnlyFromRobotWithId(leaderID);
                if (leaderSignals.length == 0) {
                    turnsSinceLastLeaderHeartbeat++;
                    if (turnsSinceLastLeaderHeartbeat > lastHeartBeatThreshold) {
                        // The King is dead!
                        role = LEADER;
                        try {
                            // Long live the King!
                            rc.broadcastMessageSignal(LEADER_COMMAND, NEW_LEADER_SIGNAL, 300);
                        } catch (GameActionException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    turnsSinceLastLeaderHeartbeat = 0;
                }
            }
            activateAdjacentNuetralBots();


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
                    } else if(message[1] == NEW_LEADER_SIGNAL){
                        leaderID = signals[i].getID();
                    }
                }
            }
            healNearbyAllies();
            collectParts();
        }
    }

    private static void activateAdjacentNuetralBots() {
        // Activate any adjacent neutral robots
        RobotInfo[] adjacentNeutralRobots = rc.senseNearbyRobots(1, Team.NEUTRAL);
        if (adjacentNeutralRobots.length > 0) {
            for (RobotInfo robot : adjacentNeutralRobots) {
                try {
                    rc.activate(robot.location);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static LocationReport getBestOrder() {
        int highestScoreFound = -99999;
        LocationReport mostPromisingReport = null;
        for (LocationReport report : reports) {
            if (report.isValid()) {
                int score = 0;
                int distance = myLocation.distanceSquaredTo(report.getReportLocation());
                if (report.getReportType() == ZOMBIE_DEN_SIGNAL) {
                    score = 100 - distance;
                } else if (report.getReportType() == PARTS_SIGNAL) {
                    score = report.getReportData() - distance;
                } else if (report.getReportType() == NUETRAL_BOT_SIGNAL) {
                    score = +RobotType.values()[report.getReportData()].partCost - distance;
                }

                if (score > highestScoreFound) {
                    highestScoreFound = score;
                    mostPromisingReport = report;
                }
            }
        }
        rc.setIndicatorString(2, "Most promising report is.... " + mostPromisingReport);
        return mostPromisingReport;
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
            leaderID = electionSignals[0].getID();
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

    protected static Signal[] getAlliedComplexSignalsOnlyFromRobotWithId(int id) {
        Signal[] signals = rc.emptySignalQueue();
        ArrayList<Signal> leaderSignals = new ArrayList<Signal>();
        for (Signal signal : signals) {
            if (signal.getTeam() == myTeam && signal.getMessage() != null && signal.getID() == leaderID) {
                leaderSignals.add(signal);
            }
        }
        return leaderSignals.toArray(new Signal[leaderSignals.size()]);
    }

    protected static void healNearbyAllies() {
        // Repair any wounded ally robots
        if (rc.isCoreReady()) {
            RobotInfo[] nearbyAlliedRobots = rc.senseNearbyRobots(attackRadius, myTeam);
            if (nearbyAlliedRobots.length > 0) {
                try {
                    rc.repair(getLocationPercentageOfRobotWithLowestHP(nearbyAlliedRobots));
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
