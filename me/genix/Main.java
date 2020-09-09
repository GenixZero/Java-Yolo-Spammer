package me.genix;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    private List<IDCount> list = new ArrayList<IDCount>();

    public Main() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the text? ");
        String toSend = scanner.nextLine();

        System.out.print("Enter the wording? ");
        String wording = scanner.nextLine();

        int threads = -1;
        while (threads < 1) {
            try {
                System.out.print("How many threads? ");
                threads = Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                System.out.println("Please respond with a number above 0.");
            }
        }

        while (list.isEmpty()) {
            try {
                System.out.print("Enter the ID(s) of the yolos (Separated by a space): ");
                String line = scanner.nextLine();
                String[] IDs = line.split(" ");
                if (IDs.length <= 0 || line.isEmpty()) {
                    throw new Exception();
                }

                for (String ID : IDs) {
                    this.list.add(new IDCount(ID));
                }
            } catch (Exception e) {}
            if (list.isEmpty()) {
                System.out.println("Please respond with at least one ID.");
                System.out.println();
            }
        }
        scanner.close();
        for (int i = 0; i < threads; i++) {
            runThread(toSend, wording, list.get(i % list.size()));
        }
    }

    public void runThread(String text, String wording, IDCount IDCounter) {
        new Thread(() -> {
            while (true) {
                try {
                    URL url = new URL("https://onyolo.com/" + IDCounter.ID + "/message");
                    URLConnection con = url.openConnection();
                    HttpURLConnection http = (HttpURLConnection)con;
                    http.setRequestMethod("POST");
                    http.setDoOutput(true);

                    byte[] out = ("{\"text\":\"" + text + " " + IDCounter.count + "\",\"cookie\":\"" + (randomString().substring(2, 15) + randomString().substring(2, 15)) + "\",\"wording\":\"" + wording + "\"}").getBytes(StandardCharsets.UTF_8);
                    http.setFixedLengthStreamingMode(out.length);
                    http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    http.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36");
                    http.connect();
                    try(OutputStream os = http.getOutputStream()) {
                        os.write(out);
                    }
                    if (http.getResponseCode() != 200) {
                        throw new IOException();
                    }
                    IDCounter.count++;
                    System.out.println("Sent | " + IDCounter.ID + " | " + IDCounter.count);
                } catch (IOException e) {
                    System.err.println("Failed to send | " + IDCounter.ID);
                }
            }
        }).start();
    }

    public String randomString() {
        String characters = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 23; i++) {
            sb.append(characters.charAt(ThreadLocalRandom.current().nextInt(characters.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        new Main();
    }
}

class IDCount {
    public int count;
    public final String ID;

    public IDCount(String ID) {
        this.ID = ID;
    }
}
