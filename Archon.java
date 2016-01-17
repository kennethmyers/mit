package team168;


import battlecode.common.*;
import scala.Array;
import scala.Int;

import java.util.*;

public class Archon extends RobotPlayer {

    static ArrayList<MapLocation> maps = new ArrayList<>();

    static final MapLocation testTargetLocation = new MapLocation(435, 154);
    static Stack<MapLocation> path = new Stack<>();


    private static int numberOfAlliedArchons;

    static void playTurn() {
        if (roundNumber == 0) {
            tryToCreateRobot(SCOUT);
            MapLocation[] alliedArchonLocations = rc.getInitialArchonLocations(myTeam);
            numberOfAlliedArchons = alliedArchonLocations.length;

            MapLocation[] enemyArchonLocations = rc.getInitialArchonLocations(enemyTeam);

            ZombieSpawnSchedule zombieSpawnSchedule = rc.getZombieSpawnSchedule();
        }

        Signal[] signals = rc.emptySignalQueue();
        Signal[] stuff = getAlliedComplexSignalsOnly(signals);
        for (Signal signal : stuff) {
            Message message = new Message(signal.getMessage());
            if (!maps.contains(message.target)) {
                maps.add(message.target);
            }

        }


        /*for next in graph.neighbors(current):
        new_cost = cost_so_far[current] + graph.cost(current, next)
        if next not in cost_so_far or new_cost < cost_so_far[next]:
        cost_so_far[next] = new_cost
        priority = new_cost + heuristic(goal, next)
        frontier.put(next, priority)
        came_from[next] = current
        */

        // A*
        if (roundNumber > 35 && path.size() == 0) {
            HashMap<MapLocation, Integer> frontier = new HashMap<MapLocation, Integer>();
            frontier.put(myLocation, 0);
            HashMap<MapLocation, MapLocation> cameFrom = new HashMap<MapLocation, MapLocation>();
            HashMap<MapLocation, Integer> costSoFar = new HashMap<MapLocation, Integer>();
            cameFrom.put(myLocation, null);
            costSoFar.put(myLocation, new Integer(0));
            while (! frontier.isEmpty()) {
                rc.setIndicatorString(0, String.format("%d", cameFrom.size()));

                MapLocation current = getMaxValue(frontier);
                frontier.remove(current);
                System.out.println(current);
                if (current.equals(testTargetLocation)) {
                    System.out.println("Found target!");
                    System.out.println("Bytecode used: " + Clock.getBytecodeNum());
                    System.out.println("Came from " + cameFrom);
                    MapLocation test = cameFrom.get(testTargetLocation);

                    while (test != null) {
                        System.out.println("Backtracking: " + test);
                        if (cameFrom.get(test).equals(myLocation)) {
                            break;
                        }
                        path.push(test);
                        test = cameFrom.get(test);
                    }

                    break;
                }

                ArrayList<MapLocation> adjacent = new ArrayList<MapLocation>(Arrays.asList(MapLocation.getAllMapLocationsWithinRadiusSq(current, 1)));
                adjacent.removeAll(maps);
                adjacent.remove(current);
                rc.setIndicatorString(1, String.format("%d", adjacent.size()));

                for (MapLocation next : adjacent) {
                    //rc.setIndicatorString(2, "" + next);
                    //if (!maps.contains(next)) {
                    System.out.println("Checkout out this location " + next);
                    Integer newCost = costSoFar.get(current) - 1; // Rubble clear penalty here
                    System.out.println("new Cost " + newCost);
                    rc.setIndicatorString(2, String.format("%d", newCost ));
                    if (! costSoFar.containsKey(next) || newCost > costSoFar.get(next)) {
                        costSoFar.put(next, newCost);
                        Integer priority = newCost + getHeuristicScore(testTargetLocation, next);
                        System.out.println("New Priority: " + priority);
                        frontier.put(next, priority);
                        cameFrom.put(next, current);
                    }
                    //}
                }
            }


        }

        if (rc.isCoreReady() && path.size() > 0) {
            try {
                rc.move(myLocation.directionTo(path.pop()));
            } catch (GameActionException e) {
                e.printStackTrace();
            }

        }

        rc.setIndicatorString(1, String.format("Dank locations %d", maps.size()));
    }

    protected static MapLocation getMaxValue(HashMap<MapLocation, Integer> hashMap) {
        HashMap.Entry<MapLocation, Integer> maxEntry = null;

        for (HashMap.Entry<MapLocation, Integer> entry : hashMap.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        return maxEntry.getKey();
    }


    protected static void tryToCreateRobot(RobotType robotType) {
        if (rc.isCoreReady()) {
            if (rc.hasBuildRequirements(robotType)) {
                try {
                    for (Direction direction : directions) {
                        if (rc.canBuild(direction, robotType) && rc.isCoreReady()) {
                            rc.build(direction, robotType);
                        }
                    }
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static Signal[] getAlliedComplexSignalsOnly(Signal[] signals) {
        ArrayList<Signal> alliedSignals = new ArrayList<Signal>();
        for (Signal signal : signals) {
            if (signal.getTeam() == myTeam && signal.getMessage() != null) {
                alliedSignals.add(signal);
            }
        }
        return alliedSignals.toArray(new Signal[alliedSignals.size()]);
    }

    private static int getHeuristicScore(MapLocation target, MapLocation next) {
        int distanceTo = 100 - next.distanceSquaredTo(target);
        return distanceTo;

    }
}
