package team168;


import battlecode.common.*;

public class Soldier extends RobotPlayer {

    protected static MapLocation order = null;

    protected static void playTurn() {

        Signal[] signals = getAlliedComplexSignalsOnly();
        for (int i = 0; i < signals.length; i++) {
            int[] message = signals[i].getMessage();
            if (message[0] == LEADER_COMMAND) {
                if (message[1] == MUSTER_AT_LOCATION) {
                    i++;
                    message = signals[i].getMessage();

                    order = new MapLocation(message[0], message[1]);
                }
            }
        }
        if (order != null) {
            makeBestFirstMoveAndClearRubble(myLocation.directionTo(order));
        }



        // TODO TEST
        RobotInfo[] enemiesInRange = getAllHostilesWithinRange(attackRadius);
        if (enemiesInRange.length > 0) {
            if (rc.isWeaponReady()) {
                try {
                    rc.attackLocation(getLocationOfRobotWithLowestHP(enemiesInRange));
                    Clock.yield();
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }

        RobotInfo[] enemiesInSensorRange = getAllHostilesWithinRange(mySensorRadius);
        if (enemiesInSensorRange.length > 0) {
            rc.setIndicatorString(0, "Enemies detected " +enemiesInSensorRange.length );
            makeBestFirstMoveAndClearRubble(myLocation.directionTo(enemiesInSensorRange[0].location));
        }
    }
}
