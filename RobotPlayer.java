package team168;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class RobotPlayer {

    static final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    static final int[] possibleDirections = new int[]{0, 1, -1, 2, -2, 3, -3, 4};

    static final int ELECTION_SIGNAL_1 = 100;
    static final int ELECTION_SIGNAL_2 = 100;


    static final int PARTS_SIGNAL = 0;
    static final int PARTS_AMOUNT = 1;
    static final int NUETRAL_BOT_SIGNAL = 1;
    static final int ENEMY_ARCHON_SIGNAL = 2;
    static final int ZOMBIE_DEN_SIGNAL = 3;


    static HashSet<Integer> VALID_SIGNAL_TYPES =
            new HashSet<Integer>(Arrays.asList(new Integer[]{PARTS_SIGNAL, NUETRAL_BOT_SIGNAL, ZOMBIE_DEN_SIGNAL}));

    static int REPORT_TYPE_INDEX = 0;
    static int REPORT_DATA_INDEX = 1;

    static int X_COORDINATE = 0;
    static int Y_COORDINATE = 1;

    // Lead archon command signals
    static final int LEADER_COMMAND = 10;
    static final int MUSTER_AT_LOCATION = 0;



    static final int TRANSMISSION_RANGE = 1000;

    // Pre-define these for better readability
    static final Direction EAST = Direction.EAST;
    static final Direction WEST = Direction.WEST;
    static final Direction SOUTH = Direction.SOUTH;
    static final Direction NORTH = Direction.EAST;
    static final Direction NORTH_EAST = Direction.NORTH_EAST;
    static final Direction NORTH_WEST = Direction.NORTH_EAST;
    static final Direction SOTH_EAST = Direction.SOUTH_EAST;
    static final Direction SOUTH_WEST = Direction.SOUTH_WEST;

    static final RobotType ARCHON = RobotType.ARCHON;
    static final RobotType SOLDIER = RobotType.SOLDIER;
    static final RobotType TURRET = RobotType.TURRET;
    static final RobotType GUARD = RobotType.GUARD;
    static final RobotType VIPER = RobotType.VIPER;
    static final RobotType TTM = RobotType.TTM;
    static final RobotType SCOUT = RobotType.SCOUT;

    static MapLocation spawnLocation;

    static RobotController rc = null;
    static Team myTeam = null;
    static Team enemyTeam = null;
    static RobotType myType = null;
    static int attackRadius = 0;
    static int mySensorRadius = 0;

    static int roundNumber;

    static ArrayList<MapLocation> pastLocations = new ArrayList<MapLocation>();

    // Things that will change and need to be re-updated every round.
    static MapLocation myLocation;

    public static void run(RobotController robotController) {

        // Set any data that won't ever change as the game progresses
        rc = robotController;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myType = rc.getType();
        attackRadius = myType.attackRadiusSquared;
        mySensorRadius = myType.sensorRadiusSquared;
        spawnLocation = rc.getLocation();

        try {
            while (true) {
                setCommonInfoForCurrentRound();
                if (myType == ARCHON) {
                    Archon.playTurn();
                } else if (myType == SCOUT) {
                    Scout.playTurn();
                } else if (myType == SOLDIER) {
                    Guard.playTurn();
                } else if (myType == TURRET || myType == TTM) {
                    Turret.playTurn();
                } else if (myType == VIPER) {
                    Soldier.playTurn();
                } else if (myType == GUARD) {
                    Guard.playTurn();
                }

                Clock.yield();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void makeBestFirstMove(Direction desiredDirection) {
        for (int i : possibleDirections) {
            Direction candidateDirection = directions[(desiredDirection.ordinal() + i + 8) % 8];
            if (rc.canMove(candidateDirection)){
                try {
                    rc.move(candidateDirection);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    static void makeBestFirstMoveAndClearRubble(Direction desiredDirection){
        for (int i : possibleDirections) {
            Direction candidateDirection = directions[(desiredDirection.ordinal() + i + 8) % 8];
            if (rc.canMove(candidateDirection) && ! pastLocations.contains(myLocation.add(candidateDirection))){
                try {
                    rc.move(candidateDirection);
                    pastLocations.add(myLocation);
                    if (pastLocations.size() > 10) {
                        pastLocations.remove(0);
                    }

                } catch (GameActionException e) {
                    e.printStackTrace();
                }
                break;
            }
           else if (rc.senseRubble(myLocation.add(candidateDirection)) > 0){
                try {
                    rc.clearRubble(candidateDirection);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    static MapLocation getLocationPercentageOfRobotWithLowestHP(RobotInfo[] robotInfos) {
        MapLocation locationWithLowestHealthRobot = null;
        double lowestHP = 999999.0;
        for (RobotInfo robotInfo : robotInfos) {
            if ((robotInfo.health / robotInfo.maxHealth) < lowestHP) {
                lowestHP = robotInfo.health;
                locationWithLowestHealthRobot = robotInfo.location;
            }
        }
        return locationWithLowestHealthRobot;
    }

    static int numberOfRobotsAdjacentToMe() {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1, myTeam);
        return nearbyRobots.length;
    }

    static boolean isAfterEarlyGame(){
        if (rc.getRoundNum() > 200){
            return true;
        }
        return false;
    }

    static void setCommonInfoForCurrentRound() {
        myLocation = rc.getLocation();
        roundNumber = rc.getRoundNum();
    }

    static MapLocation findLocationWithMostParts(int radiusToSearchForParts) {
        MapLocation[] locationsToCheckForParts = myLocation.getAllMapLocationsWithinRadiusSq(myLocation,
                                                                                            radiusToSearchForParts);

        ArrayList<MapLocation> locationWithParts = new ArrayList<>();
        for (MapLocation location : locationsToCheckForParts) {
            if (rc.senseParts(location) > 0) {
                locationWithParts.add(location);
            }
        }

        double largestNumberOfParts = 0.0;
        MapLocation locationWithMostParts = null;
        for (MapLocation location: locationWithParts) {
            double partsAtLocation = rc.senseParts(location);
            if (partsAtLocation > largestNumberOfParts) {
                largestNumberOfParts = partsAtLocation;
                locationWithMostParts = location;
            }
        }
        return locationWithMostParts;
    }

    static Signal[] getAlliedSimpleSignalsOnly() {
        Signal[] signals = rc.emptySignalQueue();
        ArrayList<Signal> alliedSignals = new ArrayList<Signal>();
        for (Signal signal : signals) {
            if (signal.getTeam() == myTeam && signal.getMessage() == null) {
                alliedSignals.add(signal);
            }
        }
        return alliedSignals.toArray(new Signal[alliedSignals.size()]);
    }

    static Signal[] getAlliedComplexSignalsOnly(Signal[] signals) {
        ArrayList<Signal> alliedSignals = new ArrayList<Signal>();
        for (Signal signal : signals) {
            if (signal.getTeam() == myTeam && signal.getMessage() != null) {
                alliedSignals.add(signal);
            }
        }
        return alliedSignals.toArray(new Signal[alliedSignals.size()]);
    }

    static RobotInfo[] getAllHostilesWithinRange(int radius) {
        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(attackRadius, enemyTeam);
        RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(attackRadius, Team.ZOMBIE);
        ArrayList<RobotInfo> allEnemiesWithinRange = new ArrayList<RobotInfo>();
        allEnemiesWithinRange.addAll(Arrays.asList(enemiesWithinRange));
        allEnemiesWithinRange.addAll(Arrays.asList(zombiesWithinRange));

        return allEnemiesWithinRange.toArray(new RobotInfo[allEnemiesWithinRange.size()]);
    }
}
