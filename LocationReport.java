package team168;

import battlecode.common.MapLocation;

import java.util.Map;


public class LocationReport {
    private MapLocation reportLocation;
    private int reportType;
    private int reportData; // Contextual, meaning depends on ReportType
    private int roundNumber; //When this report was filed.

    public LocationReport(MapLocation reportLocation, int reportType, int reportData, int roundNumber) {
        this.reportLocation = reportLocation;
        this.reportType = reportType;
        this.reportData = reportData;
        this.roundNumber = roundNumber;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public  MapLocation getReportLocation() {
        return reportLocation;
    }

    public  int getReportType() {
        return reportType;
    }

    public  int getReportData() {
        return reportData;
    }

    @Override
    public boolean equals(Object object) {
        if (! (object instanceof LocationReport)) {
            return false;
        }

        LocationReport otherReport = (LocationReport) object;
        if (this.reportLocation.equals(otherReport.getReportLocation())){
            return true;
        }
        return false;
    }
}
