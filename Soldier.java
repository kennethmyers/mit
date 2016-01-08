package team168;


import battlecode.common.*;

import java.util.Random;

public class Soldier extends RobotPlayer {

    public static void playTurn(){
        Random rand = new Random(rc.getID());
        int fate = rand.nextInt(1000); // FUCK THIS

        RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(attackRadius, enemyTeam);
        RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(attackRadius, Team.ZOMBIE);

        if (enemiesWithinRange.length > 0) {
            // Check if weapon is ready
            if (rc.isWeaponReady()) {
                try {
                    rc.attackLocation(getLocationOfRobotWithLowestHP(enemiesWithinRange));
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        } else if (zombiesWithinRange.length > 0) {
            // Check if weapon is ready
            if (rc.isWeaponReady()) {
                try {
                    rc.attackLocation(getLocationOfRobotWithLowestHP(zombiesWithinRange));
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }

        if (numberOfRobotsAdjacentToMe() > 2) {
            rc.setIndicatorString(0, "Too Crowded here, spread out");
            for (int i = 0; i < 8; i++) {
                Direction directionToMove = directions[fate % 8];
                rc.setIndicatorString(1, "Going to move to " + directionToMove);
                if (rc.canMove(directionToMove) && rc.isCoreReady()) {
                    try {
                        rc.move(directionToMove);
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
