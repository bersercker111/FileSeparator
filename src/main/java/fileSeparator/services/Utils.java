package fileSeparator.services;

public class Utils {
    public static String getGroup(String line) {
        int commaIndex = line.indexOf(",");
        if (commaIndex > -1) return line.substring(0, commaIndex);
        else return "";
    }
}
