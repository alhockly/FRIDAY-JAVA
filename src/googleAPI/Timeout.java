package googleAPI;

import java.util.concurrent.TimeUnit;

public class Timeout implements Runnable {
    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(20);
            System.out.println("...jar time out");
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
