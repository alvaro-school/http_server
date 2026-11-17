import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    private Log() {}

    private static String time() {
        LocalDateTime oraCorrente = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return oraCorrente.format(formatter);
    }
    public static void log(String s) {
        System.out.println("[INFO " + time() + "] " + s);
    }

    public static void err(String s) {
        System.out.println("[INFO " + time() + "] " + s);
    }
}
