package team168;


import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;

public class Turret extends RobotPlayer {

     static void playTurn() {

        // Just attack!
        RobotInfo[] enemiesInRange = getAllHostilesWithinRange(attackRadius);
        if (enemiesInRange.length > 0) {
            if (rc.isWeaponReady()) {
                try {
                    rc.attackLocation(getLocationPercentageOfRobotWithLowestHP(enemiesInRange));
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
