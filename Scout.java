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
            for (int i = 0; i < mySensorRadius; i++) {
                try {
                    if (! rc.onTheMap(locationToCheck)) {
                        currentDirection = currentDirection.rotateLeft().rotateLeft();
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
        ArrayList<MapLocation> locationsWithParts = new ArrayList<MapLocation>();
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

        RobotInfo[] nuetralRobots = rc.senseNearbyRobots(mySensorRadius, Team.NEUTRAL);
        for (RobotInfo robotInfo : nuetralRobots) {
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

        RobotInfo[] enemyRobots = rc.senseNearbyRobots(mySensorRadius, enemyTeam);
        for (RobotInfo robotInfo : enemyRobots) {
            MapLocation location = robotInfo.location;
            if (! reportedLocations.contains(location) && robotInfo.type == RobotType.ZOMBIEDEN) {
                try{
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
