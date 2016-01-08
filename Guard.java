package team168;

import battlecode.common.*;

import java.util.Random;


public class Guard extends RobotPlayer {

    public static void playTurn() {

        Random rand = new Random(rc.getID());

        // This is a loop to prevent the run() method from returning. Because of the Clock.yield()
        // at the end of it, the loop will iterate once per game round.
        try {
            int fate = rand.nextInt(1000);

            if (fate % 5 == 3) {
                // Send a normal signal
                rc.broadcastSignal(80);
            }

            boolean shouldAttack = false;

            // If this robot type can attack, check for enemies within range and attack one
            if (attackRadius > 0) {
                RobotInfo[] enemiesWithinRange = rc.senseNearbyRobots(attackRadius, enemyTeam);
                RobotInfo[] zombiesWithinRange = rc.senseNearbyRobots(attackRadius, Team.ZOMBIE);
                if (enemiesWithinRange.length > 0) {
                    shouldAttack = true;
                    // Check if weapon is ready
                    if (rc.isWeaponReady()) {
                        rc.attackLocation(enemiesWithinRange[rand.nextInt(enemiesWithinRange.length)].location);
                    }
                } else if (zombiesWithinRange.length > 0) {
                    shouldAttack = true;
                    // Check if weapon is ready
                    if (rc.isWeaponReady()) {
                        rc.attackLocation(zombiesWithinRange[rand.nextInt(zombiesWithinRange.length)].location);
                    }
                }
            }

            if (!shouldAttack) {
                if (numberOfRobotsAdjacentToMe() > 2) {
                    if (fate < 600) {
                        // Choose a random direction to try to move in
                        Direction dirToMove = directions[fate % 8];
                        // Check the rubble in that direction
                        if (rc.senseRubble(rc.getLocation().add(dirToMove)) >= GameConstants.RUBBLE_OBSTRUCTION_THRESH) {
                            // Too much rubble, so I should clear it
                            rc.clearRubble(dirToMove);
                            // Check if I can move in this direction
                        } else if (rc.canMove(dirToMove)) {
                            // Move
                            rc.move(dirToMove);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    protected static int numberOfRobotsAdjacentToMe() {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(1, myTeam);
        return nearbyRobots.length;
    }
}
