package team168;

import battlecode.common.*;
import scala.Array;

import java.util.ArrayList;
import java.util.Arrays;

public class RobotPlayer {

    protected static final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    protected static final int[] possibleDirections = new int[]{0, 1, -1, 2, -2, 3, -3, 4};

    protected static final int ELECTION_SIGNAL_1 = 100;
    protected static final int ELECTION_SIGNAL_2 = 100;


    protected static final int PARTS_SIGNAL = 0;
    protected static final int PARTS_AMOUNT = 1;

    protected static final int NUETRAL_BOT_SIGNAL = 1;
    protected static final int ENEMY_ARCHON_SIGNAL = 2;
    protected static final int ZOMBIE_DEN_SIGNAL = 3;

    // Lead archon command signals
    protected static final int LEADER_COMMAND = 10;
    protected static final int MUSTER_AT_LOCATION = 0;

    protected static final int TRANSMISSION_RANGE = 1000;

    // Pre-define these for better readability
    protected static final Direction EAST = Direction.EAST;
    protected static final Direction WEST = Direction.WEST;
    protected static final Direction SOUTH = Direction.SOUTH;
    protected static final Direction NORTH = Direction.EAST;
    protected static final Direction NORTH_EAST = Direction.NORTH_EAST;
    protected static final Direction NORTH_WEST = Direction.NORTH_EAST;
    protected static final Direction SOTH_EAST = Direction.SOUTH_EAST;
    protected static final Direction SOUTH_WEST = Direction.SOUTH_WEST;

    protected static final RobotType ARCHON = RobotType.ARCHON;
    protected static final RobotType SOLDIER = RobotType.SOLDIER;
    protected static final RobotType TURRET = RobotType.TURRET;
    protected static final RobotType GUARD = RobotType.GUARD;
    protected static final RobotType VIPER = RobotType.VIPER;
    protected static final RobotType TTM = RobotType.TTM;
    protected static final RobotType SCOUT = RobotType.SCOUT;

    protected static RobotController rc = null;
    protected static Team myTeam = null;
    protected static Team enemyTeam = null;
    protected static RobotType myType = null;
    protected static int attackRadius = 0;
    protected static int mySensorRadius = 0;

    protected static ArrayList<MapLocation> pastLocations = new ArrayList<MapLocation>();

    // Things that will change and need to be re-updated every round.
    protected static MapLocation myLocation;

    public static void run(RobotController robotController) {

        // Set any data that won't ever change as the game progresses
        rc = robotController;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myType = rc.getType();
        attackRadius = myType.attackRadiusSquared;
        mySensorRadius = myType.sensorRadiusSquared;

        try {
            while (true) {
                setCommonInfoForCurrentRound();
                if (myType == ARCHON) {
                    Archon.playTurn();
                } else if (myType == SCOUT) {
                    Scout.playTurn();
              } else if (myType == RobotType.SOLDIER) {
                    Soldier.playTurn();}/*
                } else if (myType == RobotType.SCOUT) {
                    Scout.playTurn();
                } else if (myType == RobotType.TURRET) {
                    Soldier.playTurn();
                } else if (myType == RobotType.TTM) {
                    Soldier.playTurn();
                } else if (myType == RobotType.VIPER) {
                    Soldier.playTurn();
                }*/
                Clock.yield();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void makeBestFirstMove(Direction desiredDirection) {
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

    protected static void makeBestFirstMoveAndClearRubble(Direction desiredDirection){
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

    protected static MapLocation getLocationOfRobotWithLowestHP(RobotInfo[] robotInfos) {
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

    protected static int numberOfRobotsAdjacentToMe() {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1, myTeam);
        return nearbyRobots.length;
    }

    protected static boolean isAfterEarlyGame(){
        if (rc.getRoundNum() > 200){
            return true;
        }
        return false;
    }

    protected static void setCommonInfoForCurrentRound() {
        myLocation = rc.getLocation();
    }

    protected static MapLocation findLocationWithMostParts(int radiusToSearchForParts) {
        MapLocation[] locationsToCheckForParts = myLocation.getAllMapLocationsWithinRadiusSq(myLocation,
                                                                                            radiusToSearchForParts);

        ArrayList<MapLocation> locationWithParts = new ArrayList<MapLocation>();
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

    protected static Signal[] getAlliedSimpleSignalsOnly() {
        Signal[] signals = rc.emptySignalQueue();
        ArrayList<Signal> alliedSignals = new ArrayList<Signal>();
        for (Signal signal : signals) {
            if (signal.getTeam() == myTeam && signal.getMessage() == null) {
                alliedSignals.add(signal);
            }
        }
        return alliedSignals.toArray(new Signal[alliedSignals.size()]);
    }

    protected static Signal[] getAlliedComplexSignalsOnly() {
        Signal[] signals = rc.emptySignalQueue();
        ArrayList<Signal> alliedSignals = new ArrayList<Signal>();
        for (Signal signal : signals) {
            if (signal.getTeam() == myTeam && signal.getMessage() != null) {
                alliedSignals.add(signal);
            }
        }
        return alliedSignals.toArray(new Signal[alliedSignals.size()]);
    }

    protected static RobotInfo[] getAllHostilesWithinRange(int radius) {
        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(attackRadius, enemyTeam);
        RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(attackRadius, Team.ZOMBIE);
        ArrayList<RobotInfo> allEnemiesWithinRange = new ArrayList<RobotInfo>();
        allEnemiesWithinRange.addAll(Arrays.asList(enemiesWithinRange));
        allEnemiesWithinRange.addAll(Arrays.asList(zombiesWithinRange));

        return allEnemiesWithinRange.toArray(new RobotInfo[allEnemiesWithinRange.size()]);
    }
}
