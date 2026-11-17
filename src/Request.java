import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    public int eCode;
    public String action;
    public String path;

    public Map<String, String> optionals = new HashMap<>();

    public Request(List<String> lines) {
        if (lines == null || lines.size() <= 1) {
            eCode = 400;
            return;
        }

        String[] parts = lines.get(0).split("\\s+");
        if (parts.length != 3 || !isValid(parts[1]) || !parts[2].startsWith("HTTP/")) {
            eCode = 400;
            return;
        }

        path = parts[1].substring(1);
        String v = parts[2].substring(5);

        if (!(v.equals("1.1") || v.equals("1.0"))) {
            eCode = 505;
            return;
        }

        if (!parts[0].equals("GET")) {
            eCode = 501;
            return;
        }

        action = parts[0];

        for (int i = 1; i < lines.size(); ++i) {
            String[] c = lines.get(i).split(":", 2);
            optionals.put(c[0].trim(),c[1].trim());
        }

        eCode = 200;
    }

    private static boolean isValid(String p) {
        if (!p.startsWith("/")) return false;
        String path = p.substring(1);
        return !path.contains("/") && !path.contains("\\") && !path.contains("..");
    }

    @Override
    public String toString() {
        return "Request [eCode=" + eCode + ", action=" + action + ", path=" + path + ", optionals=" + optionals + "]";
    }

    
}
