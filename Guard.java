package team168;

import battlecode.common.*;

import java.util.Random;


public class Guard extends RobotPlayer {

    static boolean firstRound = true;
    static int idOfArchonThatCreatedMe;
    static MapLocation archonLastKnownPosition;

    public static void playTurn() {
        if (firstRound) {
            firstRound = false;
            RobotInfo[] adjacentRobots = rc.senseNearbyRobots(2, myTeam);
            for (RobotInfo robot : adjacentRobots) {
                if (robot.type == ARCHON) {
                    idOfArchonThatCreatedMe = robot.ID;
                    archonLastKnownPosition = robot.location;
                    break;
                }
            }
        }



        // Keep track of our Archon
        RobotInfo[] robotsInSensorRange = rc.senseNearbyRobots(mySensorRadius, myTeam);
        //try {
            //if (! (rc.senseRobotAtLocation(archonLastKnownPosition).ID == idOfArchonThatCreatedMe)) {
                for (RobotInfo robot : robotsInSensorRange) {
                    if (robot.ID == idOfArchonThatCreatedMe) {
                        archonLastKnownPosition = robot.location;
                        break;
                    }
                }
            //}
       // } catch (GameActionException e) {
        //    e.printStackTrace();
        //}

        System.out.println("Byte code used 'Keep track of our Archon' : " + Clock.getBytecodeNum());
        System.out.println("Archon location last knonw" + archonLastKnownPosition);
        // Stay within sensor range of Archon
        if (! (myLocation.distanceSquaredTo(archonLastKnownPosition) < mySensorRadius) ) {
            if (rc.isCoreReady()) {
                try {
                    rc.move(myLocation.directionTo(archonLastKnownPosition));
                } catch (GameActionException e) {
                    e.printStackTrace();
                }

            }
        }

        System.out.println("Byte code used 'Stay within sensor range of Archon' : " + Clock.getBytecodeNum());

        RobotInfo[] enemyRobots = rc.senseHostileRobots(myLocation, mySensorRadius - 1);
        for (RobotInfo robot : enemyRobots) {
            if (rc.canAttackLocation(robot.location)) {
                try {
                    rc.attackLocation(robot.location);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            } else{
                try {
                    rc.move(myLocation.directionTo(robot.location));
                } catch (GameActionException e) {
                    e.printStackTrace();
                }

            }
        }

        // Spread out a bit
        for (int i = 0; i < 8; i++) {
            Direction directionToMove = directions[(rc.getRoundNum() + i) % 8];
            rc.setIndicatorString(1, "Going to move to " + directionToMove);
            if (rc.canMove(directionToMove) &&
                    rc.isCoreReady() &&
                    ! pastLocations.contains(myLocation.add((directionToMove)))) {
                try {
                    rc.move(directionToMove);
                    pastLocations.add(myLocation);
                    if (pastLocations.size() > 10) {
                        pastLocations.remove(0);
                    }

                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }




    protected static int numberOfRobotsAdjacentToMe() {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1, myTeam);
        return nearbyRobots.length;
    }
}
