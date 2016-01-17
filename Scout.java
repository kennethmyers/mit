package team168;


import battlecode.common.*;

import java.util.ArrayList;

public class Scout extends RobotPlayer {

    static boolean firstTurn = true;
    static MapLocation spawnLocation = null;
    ArrayList[][] table = new ArrayList[10][10];
    static ArrayList<MapLocation> rubbleLocations = new ArrayList<>();

    protected static void playTurn() {
        if (firstTurn) {
            firstTurn = false;
            spawnLocation = myLocation;

        }

        MapLocation[] locations = myLocation.getAllMapLocationsWithinRadiusSq(myLocation, 10);
        for (MapLocation location : locations) {
            try {
                if (! rubbleLocations.contains(location) && rc.onTheMap(location)) {
                    rubbleLocations.add(location);
                    if (rc.senseRubble(location) > 0) {
                        Message message = new Message(Message.Command.RUBBLE, location);
                        try {
                            rc.broadcastMessageSignal(message.toSignalPayload()[0], message.toSignalPayload()[1], 200);
                        } catch (GameActionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        rc.setIndicatorString(0, String.format("%d", rubbleLocations.size()));
        rc.setIndicatorString(1, String.format("%d", GameConstants.MAP_MAX_HEIGHT));
        rc.setIndicatorString(2, String.format("%1$,.2f", GameConstants.RUBBLE_OBSTRUCTION_THRESH));
    }

}
