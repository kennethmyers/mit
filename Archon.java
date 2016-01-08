package team168;

import battlecode.common.*;


public class Archon extends RobotPlayer {
    public static void playTurn(){

        MapLocation myLocation = rc.getLocation();

        // Activate any adjacent neutral robots
        RobotInfo[] adjacentNeutralRobots = rc.senseNearbyRobots(1, Team.NEUTRAL);
        if (adjacentNeutralRobots.length > 0){
            for (RobotInfo robot : adjacentNeutralRobots) {
                try {
                    rc.activate(robot.location);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }

        // Create another bot if able.
        if (rc.isCoreReady()){
            if (rc.hasBuildRequirements(RobotType.SOLDIER))  {
                try {
                    rc.build(EAST, RobotType.SOLDIER);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }

        MapLocation closets_nuetral_robot = null;
        int closests_distance = 99999;
        RobotInfo[] nuetral_robots = rc.senseNearbyRobots(myType.sensorRadiusSquared, Team.NEUTRAL);
        if (nuetral_robots.length > 0) {
            for (RobotInfo robot : nuetral_robots){
                MapLocation target_location = robot.location;
                int distance = myLocation.distanceSquaredTo(target_location);
                if (distance < closests_distance){
                    closests_distance = distance;
                    closets_nuetral_robot = target_location;
                }

            }
            Direction move_direction = myLocation.directionTo(closets_nuetral_robot);
            if (rc.canMove(move_direction)){
                try {
                    rc.move(move_direction);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
