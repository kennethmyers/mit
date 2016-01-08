package team168;

import battlecode.common.*;

public class RobotPlayer {

    protected static final Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};

    // Pre-define these for better readability
    protected static Direction EAST = Direction.EAST;
    protected static Direction WEST = Direction.WEST;
    protected static Direction SOUTH = Direction.SOUTH;
    protected static Direction NORTH = Direction.EAST;
    protected static Direction NORTH_EAST = Direction.NORTH_EAST;
    protected static Direction NORTH_WEST = Direction.NORTH_EAST;
    protected static Direction SOTH_EAST = Direction.SOUTH_EAST;
    protected static Direction SOUTH_WEST = Direction.SOUTH_WEST;

    protected static RobotController rc = null;
    protected static Team myTeam = null;
    protected static Team enemyTeam = null;
    protected static RobotType myType = null;
    protected static int attackRadius = 0;
    protected static int sensorRadius = 0;


    // Switch into difference strategy after this cutoff.
    protected final static int roundNumberCutoff = 200;

    public static void run(RobotController robotController) {

        // Set any data that won't ever change as the game progresses
        rc = robotController;
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        myType = rc.getType();
        attackRadius = myType.attackRadiusSquared;
        sensorRadius = myType.sensorRadiusSquared;

        try {
            if (myType == RobotType.ARCHON) {
                while (true) {
                    Archon.playTurn();
                    Clock.yield();
                }
            } else if (myType == RobotType.GUARD) {
                while (true) {
                    Guard.playTurn();
                    Clock.yield();
                }
            } else if (myType == RobotType.SOLDIER) {
                while (true) {
                    Guard.playTurn();
                    Clock.yield();
                }
            } else if (myType == RobotType.SCOUT) {
                while (true) {
                    Soldier.playTurn();
                    Clock.yield();
                }
            } else if (myType == RobotType.TURRET) {
                while (true) {
                    Soldier.playTurn();
                    Clock.yield();
                }
            } else if (myType == RobotType.TTM) {
                while (true) {
                    Soldier.playTurn();
                    Clock.yield();
                }
            } else if (myType == RobotType.VIPER) {
                while (true) {
                    Soldier.playTurn();
                    Clock.yield();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static boolean afterEarlyGame(){
        if (rc.getRoundNum() > 200){
            return true;
        }
        return false;
    }
}
