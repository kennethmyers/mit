package team168;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import battlecode.common.Team;

/**
 * @author james
 */
public class Message {

    static private final int MESSAGE_TYPE_START = 0;
    static private final int MESSAGE_TYPE_LENGTH = 5;

    static private final int TARGET_X_START = MESSAGE_TYPE_START + MESSAGE_TYPE_LENGTH;
    static private final int TARGET_X_LENGTH = 10;

    static private final int TARGET_Y_START = TARGET_X_START + TARGET_X_LENGTH;
    static private final int TARGET_Y_LENGTH = 10;

    static private final int TEAM_START = TARGET_Y_START + TARGET_Y_LENGTH;
    static private final int TEAM_LENGTH = 2;

    static private final int ROBOT_TYPE_START = TEAM_START + TEAM_LENGTH;
    static private final int ROBOT_TYPE_LENGTH = 2;

    static private final int ROBOT_HP_START = ROBOT_TYPE_START + ROBOT_TYPE_LENGTH;
    static private final int ROBOT_HP_LENGTH = 4;

    static private final int RUBBLE_START = ROBOT_HP_START + ROBOT_HP_LENGTH;
    static private final int RUBBLE_LENGTH = 11;

    static private final int PARTS_START = RUBBLE_START + RUBBLE_LENGTH;
    static private final int PARTS_LENGTH = 10;

    public enum MessageType {
        COMPACT_RUBBLE_LOCATION_DATA,
        RUBBLE_LOCATION_DATA,
        ROBOT_LOCATION_DATA,
        PARTS_LOCATION_DATA,
        OTHER
    }


    private MessageType messageType;
    private MapLocation location;
    private Team team;
    private RobotType robotType;
    private int robotHP;
    private int rubble;
    private int parts;

    private long bitArray = 0;

    public Message(MessageType messageType, MapLocation location, int rubble) {
        this.messageType = messageType;
        this.location = location;
        this.rubble = rubble;
    }

    public Message(int[] signalPayload) {
        long array = getBitArrayFromTwoInts(signalPayload);

        this.messageType = MessageType.values()[(int) getIntFromBitArray(array, MESSAGE_TYPE_START, MESSAGE_TYPE_LENGTH)];
        if (MessageType.RUBBLE_LOCATION_DATA == MessageType.values()[(int) getIntFromBitArray(array, MESSAGE_TYPE_START, MESSAGE_TYPE_LENGTH)]){
            this.location = new MapLocation(
                    (int) getIntFromBitArray(array, TARGET_X_START, TARGET_X_LENGTH),
                    (int) getIntFromBitArray(array, TARGET_Y_START, TARGET_Y_LENGTH));

            this.rubble = (int) getIntFromBitArray(array, RUBBLE_START, RUBBLE_LENGTH);
        }

        if (MessageType.PARTS_LOCATION_DATA == MessageType.values()[(int) getIntFromBitArray(array, MESSAGE_TYPE_START, MESSAGE_TYPE_LENGTH)]){
            this.location = new MapLocation(
                    (int) getIntFromBitArray(array, TARGET_X_START, TARGET_X_LENGTH),
                    (int) getIntFromBitArray(array, TARGET_Y_START, TARGET_Y_LENGTH));

            this.parts = (int) getIntFromBitArray(array, PARTS_START, PARTS_LENGTH);
        }

     /*

        this.team = Team.values()[(int) getIntFromBitArray(array, TEAM_START, TEAM_LENGTH)];

        this.robotType = RobotType.values()[(int) getIntFromBitArray(array, ROBOT_TYPE_START, ROBOT_TYPE_LENGTH)];

        this.robotHP = (int) getIntFromBitArray(array, ROBOT_HP_START, ROBOT_HP_LENGTH);*/


    }

    /**
     * @return an int[] suitable to be broadcast
     */
    public int[] toSignalPayload() {
        long array = 0;

        if (this.messageType == MessageType.RUBBLE_LOCATION_DATA) {
            array = setBitsAtLocation(array, MESSAGE_TYPE_START, MESSAGE_TYPE_LENGTH, this.messageType.ordinal());
            array = setBitsAtLocation(array, TARGET_X_START, TARGET_X_LENGTH, this.location.x);
            array = setBitsAtLocation(array, TARGET_Y_START, TARGET_Y_LENGTH, this.location.y);
            array = setBitsAtLocation(array, RUBBLE_START, RUBBLE_LENGTH, this.rubble);
        } else if (this.messageType == MessageType.PARTS_LOCATION_DATA) {
            array = setBitsAtLocation(array, MESSAGE_TYPE_START, MESSAGE_TYPE_LENGTH, this.messageType.ordinal());
            array = setBitsAtLocation(array, TARGET_X_START, TARGET_X_LENGTH, this.location.x);
            array = setBitsAtLocation(array, TARGET_Y_START, TARGET_Y_LENGTH, this.location.y);
            array = setBitsAtLocation(array, PARTS_START, PARTS_LENGTH, this.parts);
        }

        /*array = setBitsAtLocation(array, TEAM_START, TEAM_LENGTH, this.team.ordinal());
        array = setBitsAtLocation(array, ROBOT_TYPE_START, ROBOT_TYPE_LENGTH, this.robotType.ordinal());
        array = setBitsAtLocation(array, ROBOT_HP_START, ROBOT_HP_LENGTH, this.robotHP);*/

        return getTwoIntsFromLong(array);
    }

    /**
     * @param array the long to extract the flag from
     * @param index the index of the flag in the array
     * @return the value of the flag
     */
    public static boolean getFlag(long array, int index) {
        // shift the array so the bit is in the 0 index
        long shiftedArray = array >>> index;

        // mask the array so that the only possible values
        // are 0 and 1
        long masked = shiftedArray & 0b1;

        // if the value is 1, the flag is true
        return masked == 1;
    }

    /**
     * @param array the array to modify
     * @param index the index of the flag to set
     * @param value the value of the flag
     * @return a modified array with the flag set
     */
    public static long setFlag(long array, int index, boolean value) {
        if (value) {
            return array | (1 << index);
        } else {
            return array & ~(1 << index);
        }
    }

    /**
     * @param array      the array to read
     * @param startIndex the start index of the integer to read
     * @param length     the length of the integer to read
     * @return an integer extracted from the array
     */
    public static long getIntFromBitArray(long array, int startIndex, int length) {
        // get rid of lower bits in the array
        long removedLowedBits = array >>> startIndex;

        // get rid of upper bits in the array
        long removedUpperBits = removedLowedBits << 64 - length;

        // shift the array into the correct place
        return removedUpperBits >>> 64 - length;
    }

    /**
     * @param array      the array to modify
     * @param startIndex the start index of the integer to write
     * @param length     the length of the integer to write
     * @param value      the value to set the output array to
     * @return a modified version of the array
     */
    public static long setBitsAtLocation(long array, int startIndex, int length, long value) {
        // this is a slow implementation. Can you make it use less bytecode?

        // read the current value in the chunk of the array
        long currentValue = getIntFromBitArray(array, startIndex, length);

        // zero the chunk of the array
        long zeroedArray = array ^ (currentValue << startIndex);

        // write the value to the array
        return zeroedArray | (value << startIndex);
    }

    /**
     * @param array the array to convert
     * @return a long representation of the array
     */
    public static long getBitArrayFromTwoInts(int[] array) {
        return ((long) array[0] << 32) | array[1] & 0xFFFFFFFFL;
    }

    /**
     * @param array the long to convert
     * @return an int[] representation of the array
     */
    public static int[] getTwoIntsFromLong(long array) {
        return new int[]{
                (int) (array >> 32),
                (int) array
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (messageType != message.messageType) return false;
        return location != null ? location.equals(message.location) : message.location == null;

    }

    @Override
    public int hashCode() {
        int result = messageType != null ? messageType.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public MapLocation getLocation() {
        return location;
    }

    public void setLocation(MapLocation location) {
        this.location = location;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public RobotType getRobotType() {
        return robotType;
    }

    public void setRobotType(RobotType robotType) {
        this.robotType = robotType;
    }

    public int getRobotHP() {
        return robotHP;
    }

    public void setRobotHP(int robotHP) {
        this.robotHP = robotHP;
    }

    public int getRubble() {
        return rubble;
    }

    public void setRubble(int rubble) {
        this.rubble = rubble;
    }

}
