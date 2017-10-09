package Exercise2B;

/**
 * Buyer Thread class for the simulation of assignment 2B. Represents a buyer at the AutoRAI.
 * These two Thread classes look a lot like each other. The reason for this is explained in the duo's documentation.
 */
public class Buyer extends Thread {
    //Change this value to make the Thread perform multiple runs.
    private final static int AMOUNT_OF_RUNS = 1;

    private AutoRAI autoRAI;

    public Buyer(String name, AutoRAI autoRAI) {
        super(name);
        this.autoRAI = autoRAI;
    }

    @Override
    public void run() {
        for (int i = 0; i < AMOUNT_OF_RUNS; i++) {

            try {
                //Travel to the AutoRAI...
                justLive();
                System.out.println(getName() + " arrived at the AutoRAI");
                //Try to get into the autoRAI to buy a car
                autoRAI.buyerLoginAutoRAI();
                //Now that you're inside alone, buy a car or two
                buyCar();
                //Then leave the AutoRAI again and signal the others.
                autoRAI.buyerLogoutAutoRAI();
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Method to simulate travel to the AutoRAI, or any action for that matter, actually making the Thread sleep.
     */
    private void justLive() {
        try {
            Thread.sleep((int) (Math.random() * 1000));
        }
        catch (InterruptedException e) {
            //Do nothing...
        }
    }

    /**
     * Method to simulate the buyer getting a car or two at the AutoRAI, making the Thread sleep for a moment in reality.
     */
    private void buyCar() {
        try {
            System.out.println(getName() + " is buying a car at the AutoRAI");
            Thread.sleep(2000);
        }
        catch (InterruptedException e) {
            //Do nothing...
        }
    }
}
