import java.util.concurrent.Semaphore;

public class RopeBridge {
    /**
     * your s(hared) data structures to guarantee correct behaviour of the people
     * in passing the rope bridge
     **/
    private static final int NR_OF_PEOPLE = 20;
    private static final int BRIDGE_CAPACITY = 3;

    private Person[] person = new Person[NR_OF_PEOPLE];

    private Semaphore leftFreePass;

    public RopeBridge() {
        leftFreePass = new Semaphore(BRIDGE_CAPACITY, true);

        for (int i = 0; i < NR_OF_PEOPLE; i++) {
            person[i] = new Person("P" + i); /* argument list can be extended */
            person[i].start();
        }
    }

    /**
     * Inner person class.
     */
    class Person extends Thread {
        public Person(String name) {
            super(name);
            //Anything else you want to do in the constructor...
        }

        public void run() {
            while (true) {
                justLive();

                try {
                    leftFreePass.acquire();
                    System.out.println(getName() + " is on the bridge!");

                    //Wait for a moment...
                    Thread.sleep((2000));

                    //Here it all has to happen...

                    leftFreePass.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println(getName() + " has passed the bridge!");
            }
        }

        private void justLive() {
            try {
                System.out.println(getName() + " is working/getting education");
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                //...
            }
        }
    }
}
