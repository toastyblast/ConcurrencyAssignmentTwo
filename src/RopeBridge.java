import java.util.concurrent.Semaphore;

public class RopeBridge {
    /**
     * your s(hared) data structures to guarantee correct behaviour of the people
     * in passing the rope bridge
     **/
    private static final int NR_OF_PEOPLE = 20;
    private static final int BRIDGE_CAPACITY = 3;
    private int leftPool = 20;
    private int rightPool = 0;
    //false = left
    //true = right
    private boolean leftToRight = false;

    private Person[] person = new Person[NR_OF_PEOPLE];

    private Semaphore leftFreePass, rightFreePass;

    public RopeBridge() {

        leftFreePass = new Semaphore(BRIDGE_CAPACITY, true);
        rightFreePass = new Semaphore(0, true);

        for (int i = 0; i < NR_OF_PEOPLE; i++) {
            person[i] = new Person("P" + i); /* argument list can be extended */
            person[i].start();
        }
    }

    /**
     * Inner person class.
     */
    class Person extends Thread {
        boolean direction  = false;

        public Person(String name) {
            super(name);
            //Anything else you want to do in the constructor...
        }

        public void run() {

            //false = left
            //true = right

            while (true) {
                justLive();
                try {

                    if (leftToRight == direction){

                        leftFreePass.acquire();
                        System.out.println(getName() + " is on the bridge! " + direction);

                        //Wait for a moment...
                        Thread.sleep((2000));

                        //Here it all has to happen...

                        leftFreePass.release();

                        if (direction){
                            //Update the number of people on each side.
                            rightPool--;
                            leftPool++;
                        } else if (!direction){
                            //Update the number of people on each side.
                            leftPool--;
                            rightPool++;
                        }

                        //Change your direction
                        direction = !direction;

                    } else {
                        rightFreePass.acquire();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (leftPool == 0){
                    leftToRight = true;
                } else if (rightPool == 0){
                    leftToRight = false;
                }
//                System.out.println(getName() + " has passed the bridge!");
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
