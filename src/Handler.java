import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Handler extends Thread {
    Socket m_s;

    String[][] TYPES = {
        {"txt", "text/plain"},
        {"md",  "text/markdown"},
        {"csv", "text/csv"},
        {"html", "text/html"},
    };

    public static final Map<Integer, String> STATUS_CODES = Map.of(
        400, "Bad Request",
        501, "Not Implemented",
        505, "Version Not Supported"
    );

    public Handler(Socket s) {
        m_s = s;
    }

    public void run() {
        try (
            PrintWriter out = new PrintWriter(m_s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(m_s.getInputStream()));
        ) {
            List<String> lines = new ArrayList<>();
            String line;

            
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                lines.add(line);
            }

            Request r = new Request(lines);
            //Log.log(r.toString());
            String t = r.optionals.get("User-Agent");
            if (t != null) {
                Log.log("Request UA: " + t);
            }
            out.print("HTTP/1.0 ");
            if (r.eCode != 200) {
                out.println(r.eCode + " " + STATUS_CODES.get(r.eCode) + "\r");
                return;
            }

            String acceptHeader = r.optionals.get("Accept");
            File dir = new File("www");

    
            Map<String, File> available = new LinkedHashMap<>();
            for (String[] ext : TYPES) {
                File file = new File(dir, r.path + "." + ext[0]);
                if (file.isFile()) {
                    available.put(ext[1], file);
                }
            }

            if (available.isEmpty()) {
                out.println("404 Not Found\r");
                return;
            }

            File chosen = null;
            String chosenType = null;

            if (acceptHeader == null || acceptHeader.equals("*/*")) {
                Map.Entry<String, File> first = available.entrySet().iterator().next();
                chosen = first.getValue();
                chosenType = first.getKey();
            } else {
                for (String rawChoice : acceptHeader.split(",|;")) {
                    String choice = rawChoice.trim();
                    
                    if (choice.equals("*/*")) {
                        Map.Entry<String, File> first = available.entrySet().iterator().next();
                        chosen = first.getValue();
                        chosenType = first.getKey();
                        break;
                    }
                    
                    if (available.containsKey(choice)) {
                        chosen = available.get(choice);
                        chosenType = choice;
                        break;
                    }
                }
                
                if (chosen == null) {
                    out.println("406 Not Acceptable\r");
                    return;
                }
            }

            try (BufferedReader br = new BufferedReader(new FileReader(chosen))) {
                byte[] dati = Files.readAllBytes(chosen.toPath());
                String contenuto = new String(dati);
                out.println("200 OK\r");
                out.println("Content-Type: " + chosenType + "\r");
                out.println("Content-Length: " + dati.length + "\r");
                out.println("\r");
                out.print(contenuto);
                out.flush(); 
                
            } catch (IOException io) {
                out.println("500 Internal Server Error\r");
            }

        } catch (IOException e) {
            Log.err(e.getMessage());
        }
        
        try {
            m_s.close();
        } catch (IOException e) { Log.err(e.getMessage()); }
    }
}
