package Exercise2B;

/**
 * Viewer Thread class for the simulation of assignment 2B. Represents a viewer at the AutoRAI.
 * These two Thread classes look a lot like each other. The reason for this is explained in the duo's documentation.
 */
public class Viewer extends Thread {
    //Change this value to make the Thread perform multiple runs.
    private final static int AMOUNT_OF_RUNS = 1;

    private AutoRAI autoRAI;

    public Viewer(String name, AutoRAI autoRAI) {
        super(name);
        this.autoRAI = autoRAI;
    }

    @Override
    public void run() {
        for (int i = 0; i < AMOUNT_OF_RUNS; i++) {

            try {
                //Travel to the AutoRAI...
                System.out.println(getName() + " is travelling to the AutoRAI");
                justLive();
                System.out.println(getName() + " arrived at the AutoRAI");
                //Try to get into the AutoRAI.
                autoRAI.viewerLoginAutoRAI();
                //Now that you're inside, view the cars at your leisure!
                viewCars();
                //And then exit the AutoRAI once you're done.
                autoRAI.viewerLogoutAutoRAI();
                justLive();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();//preserve the message
                return;
            }
        }
    }

    /**
     * Method to simulate travel to the AutoRAI, or any action for that matter, actually making the Thread sleep.
     */
    private void justLive() {
        try {
            Thread.sleep((int) (Math.random() * 2000));
        } catch (InterruptedException e) {
            //Do nothing...
        }
    }

    private void viewCars() {
        try {
            System.out.println(getName() + " is viewing the cars at the AutoRAI");
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            //Do nothing...
        }
    }
}