package com.example.squadbuilderrepository.Utils;

public class PlayerUtil {
    private final static String GK = "GK";
    private final static String DEF = "DEF";
    private final static String MID = "MID";
    private final static String FW = "FW";

    public static String getTypeFromPosition (String position) {
        if (position == null) return null;

        if (position.equalsIgnoreCase("GK")) return GK;

        else if (position.equalsIgnoreCase("LB") ||
                position.equalsIgnoreCase("CB") ||
                position.equalsIgnoreCase("RB")) return DEF;

        else if (position.equalsIgnoreCase("CDM") ||
                position.equalsIgnoreCase("CM") ||
                position.equalsIgnoreCase("CAM") ||
                position.equalsIgnoreCase("RM") ||
                position.equalsIgnoreCase("LM")) return MID;

        else if (position.equalsIgnoreCase("LW") ||
                position.equalsIgnoreCase("FW") ||
                position.equalsIgnoreCase("ST") ||
                position.equalsIgnoreCase("RW")) return FW;

        else return "unspecified";
    }
}
