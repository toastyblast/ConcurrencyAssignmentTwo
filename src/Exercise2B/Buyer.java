package Exercise2B;

public class Buyer extends Thread {
    private AutoRAI autoRAI;

    public Buyer(String name, AutoRAI autoRAI) {
        super(name);
        this.autoRAI = autoRAI;
    }

    @Override
    public void run() {
        while (true) {

            try {
                justLive();
                autoRAI.buyAtAutoRAI();
                justLive();
                autoRAI.leaveBuyAutoRAI();
            } catch (InterruptedException ie) {
                //...
            }
        }
    }

    //Methods...

    private void justLive() {
        try {
//            System.out.println(getName() + " is travelling to the AutoRAI!");
            Thread.sleep((int) (Math.random() * 1000));
        } catch (InterruptedException e) {
            //...
        }
    }
}
