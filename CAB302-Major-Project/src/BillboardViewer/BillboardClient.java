package BillboardViewer;

import java.io.IOException;
import java.util.Timer;

public class BillboardClient {

    private Timer timer;
    //New billboard creation
    public static void main(String[] args) {
        try {
            BillboardClient bc = new BillboardClient();
        } catch (IOException e) {
            System.out.println("Couldn't create billboardClient object");
        }
    }

    /**
     * Runs RemindTask.java every 15 seconds to receive & display billboards
     * @throws IOException
     */
    public BillboardClient() throws IOException {
        timer = new Timer();
        timer.schedule(new RemindTask(), 0, (long) (0.15*60000));
    }

}