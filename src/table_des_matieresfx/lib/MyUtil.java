package table_des_matieresfx.lib;

public class MyUtil {

    public static String toUpperCaseExceptµ(String s) {
        StringBuilder str = new StringBuilder("");
        for (int i = 0; i < s.length(); i++) {
            Character c = s.charAt(i);
            if (c != 'µ') {
                str.append(Character.toUpperCase(c));
            } else {
                str.append(c);
            }
        }
        return str.toString();
    }
}
