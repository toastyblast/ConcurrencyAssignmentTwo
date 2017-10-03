package Exercise2B;

public class Buyer extends Thread {
    private AutoRAI autoRAI;

    public Buyer(String name, AutoRAI autoRAI) {
        super(name);
        this.autoRAI = autoRAI;
    }

    @Override
    public void run() {
        try {
            justLive();
            autoRAI.buyerLoginAutoRAI();
            buyCar();
            autoRAI.buyerLogoutAutoRAI();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return;
        }

//        while (true) {
//
//            try {
//                justLive();
//                autoRAI.buyerLoginAutoRAI();
//                buyCar();
//                autoRAI.buyerLogoutAutoRAI();
//            } catch (InterruptedException ie) {
//                Thread.currentThread().interrupt();
//                return;
//            }
//        }
    }

    private void justLive() {
        try {
            System.out.println(getName() + " is travelling to the AutoRAI");
            Thread.sleep((int) (Math.random() * 1000));
        } catch (InterruptedException e) {
            //Do nothing...
        }
    }

    private void buyCar() {
        try {
            System.out.println(getName() + " is buying a car at the AutoRAI");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //Do nothing...
        }
    }
}
