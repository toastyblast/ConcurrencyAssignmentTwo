package Exercise2B;

public class Viewer extends Thread {
    private AutoRAI autoRAI;

    public Viewer(String name, AutoRAI autoRAI) {
        super(name);
        this.autoRAI = autoRAI;
    }

    @Override
    public void run() {
        while (true) {

            try {
                justLive();
                autoRAI.viewerLoginAutoRAI();
                viewCars();
                autoRAI.viewerLogoutAutoRAI();
            } catch (InterruptedException ie) {
                //Do nothing...
            }
        }
    }

    private void justLive() {
        try {
            System.out.println(getName() + " is travelling to the AutoRAI");
            Thread.sleep((int) (Math.random() * 1000));
        } catch (InterruptedException e) {
            //Do nothing...
        }
    }

    private void viewCars() {
        try {
            System.out.println(getName() + " is viewing the cars at the AutoRAI");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //Do nothing...
        }
    }
}
