package ru.twistofthemyth.graves;

import org.bukkit.DyeColor;

public class Utils {
    public static long getDay(long time) {
        return time / 24000;
    }

    public static DyeColor parseColor(String color) {
        switch (color) {
            case "WHITE":
                return DyeColor.WHITE;
            case "ORANGE":
                return DyeColor.ORANGE;
            case "MAGENTA":
                return DyeColor.MAGENTA;
            case "LIGHT_BLUE":
                return DyeColor.LIGHT_BLUE;
            case "YELLOW":
                return DyeColor.YELLOW;
            case "LIME":
                return DyeColor.LIME;
            case "PINK":
                return DyeColor.PINK;
            case "LIGHT_GRAY":
                return DyeColor.LIGHT_GRAY;
            case "CYAN":
                return DyeColor.CYAN;
            case "PURPLE":
                return DyeColor.PURPLE;
            case "BLUE":
                return DyeColor.BLUE;
            case "BROWN":
                return DyeColor.BROWN;
            case "GREEN":
                return DyeColor.GREEN;
            case "RED":
                return DyeColor.RED;
            case "BLACK":
                return DyeColor.BLACK;
            default:
                return DyeColor.GRAY;
        }
    }
}
