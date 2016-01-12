package team168;


import battlecode.common.*;

import java.util.ArrayList;

public class Scout extends RobotPlayer {
    static ArrayList<MapLocation> reportedLocations = new ArrayList<MapLocation>();
    static ArrayList<MapLocation> previousLocaions = new ArrayList<MapLocation>();
    static Direction currentDirection = null;

    protected static void playTurn() {

        if (previousLocaions.size() == 0) {
            currentDirection = directions[(rc.getRoundNum() + rc.getID()) % 8];
            makeBestFirstMove(currentDirection);
            previousLocaions.add(myLocation);
        } else {
            MapLocation locationToCheck = myLocation.add(currentDirection);
            for (int i = 0; i < mySensorRadius - 1; i++) {
                try {
                    if (! rc.onTheMap(locationToCheck)) {
                        if (roundNumber % 2 == 0) {
                            currentDirection = currentDirection.rotateLeft().rotateLeft();
                        } else {
                            currentDirection = currentDirection.rotateRight().rotateRight();
                        }
                    } else {
                        locationToCheck = locationToCheck.add(currentDirection);
                    }
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
            makeBestFirstMove(currentDirection);
        }

        MapLocation[] locationsToScan = myLocation.getAllMapLocationsWithinRadiusSq(myLocation, mySensorRadius);
        for (MapLocation location : locationsToScan) {
            if (rc.senseParts(location) > 0 && ! reportedLocations.contains(location)) {
                try {
                    rc.broadcastMessageSignal(PARTS_SIGNAL, (int) rc.senseParts(location), TRANSMISSION_RANGE);
                    rc.broadcastMessageSignal(location.x, location.y, TRANSMISSION_RANGE);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
                reportedLocations.add(location);
            }
        }

        RobotInfo[] neutralRobots = rc.senseNearbyRobots(mySensorRadius, Team.NEUTRAL);
        for (RobotInfo robotInfo : neutralRobots) {
            MapLocation location = robotInfo.location;
            if (! reportedLocations.contains(location)) {
                try{
                    rc.broadcastMessageSignal(NUETRAL_BOT_SIGNAL, robotInfo.type.ordinal(), TRANSMISSION_RANGE);
                    rc.broadcastMessageSignal(location.x, location.y, TRANSMISSION_RANGE);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
                reportedLocations.add(location);
            }
        }

        RobotInfo[] enemyRobots = rc.senseNearbyRobots(mySensorRadius, Team.ZOMBIE);
        for (RobotInfo robotInfo : enemyRobots) {
            MapLocation location = robotInfo.location;
            if (! reportedLocations.contains(location) && robotInfo.type == RobotType.ZOMBIEDEN) {
                try{
                    rc.setIndicatorString(0, String.format("ZOMBIE DEN FOUND at %d,%d", location.x, location.y));
                    rc.broadcastMessageSignal(ZOMBIE_DEN_SIGNAL, robotInfo.ID, TRANSMISSION_RANGE);
                    rc.broadcastMessageSignal(location.x, location.y, TRANSMISSION_RANGE);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
                reportedLocations.add(location);
            }
        }
    }
}
