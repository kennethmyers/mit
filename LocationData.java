package team168;


import battlecode.common.MapLocation;

public class LocationData {
    private static MapLocation location;
    private static int parts;

    public static MapLocation getLocation() {
        return location;
    }

    public static void setLocation(MapLocation location) {
        LocationData.location = location;
    }

    public static int getParts() {
        return parts;
    }

    public static void setParts(int parts) {
        LocationData.parts = parts;
    }
}
