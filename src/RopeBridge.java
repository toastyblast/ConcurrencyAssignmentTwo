import java.util.concurrent.Semaphore;

public class RopeBridge {
    /**
     * your s(hared) data structures to guarantee correct behaviour of the people
     * in passing the rope bridge
     **/
    private static final int NR_OF_PEOPLE = 50;
    private static final int BRIDGE_CAPACITY = 3;
    private int peopleRemaining = NR_OF_PEOPLE;
    private int leftPool = 0;
    private int rightPool = 0;
    private int waitingPeople = 0;
    private int switchCounter = 0;
    //false = GOING left to right
    //true = GOING right to left
    private boolean directionOfBridge;

    private Semaphore mutex = new Semaphore(1);

    private Person[] person = new Person[NR_OF_PEOPLE];

    private Semaphore passingTheBridgeQueue;
    private Semaphore waitingTheDirectionToChangeQueue;

    public RopeBridge() {

        passingTheBridgeQueue = new Semaphore(BRIDGE_CAPACITY, true);
        waitingTheDirectionToChangeQueue = new Semaphore(0, true);

        for (int i = 0; i < NR_OF_PEOPLE; i++) {
            if (i % 2 == 0 /*i >= NR_OF_PEOPLE/2*/ ) {
                person[i] = new Person("P" + i, false); /* argument list can be extended */
                leftPool++;
            } else {
                person[i] = new Person("P" + i, true); /* argument list can be extended */
                rightPool++;

            }
//            person[i] = new Person("P" + i, true); /* argument list can be extended */
//            rightPool++;
        }

        //The side that has more people on it becomes the first direction of the bridge.
        directionOfBridge = leftPool <= rightPool;

        for (int i = 0 ; i < NR_OF_PEOPLE; i++){
            person[i].start();
        }
    }

    /**
     * Inner person class.
     */

    class Person extends Thread {
        boolean direction;
        boolean stop = false;

        public Person(String name, boolean direction) {
            super(name);
            this.direction = direction;
            //Anything else you want to do in the constructor...
        }

        public void run() {

            //false = GOING left to right
            //true = GOING right to left

            while (!stop) {
                justLive();
                try {

                    mutex.acquire();
                    if(directionOfBridge != direction){
                        waitingPeople++;
                        mutex.release();
                        waitingTheDirectionToChangeQueue.acquire();
                    } else {
                        mutex.release();
                    }


                    mutex.acquire();
                    //If the direction of the person doesn't match that of the bridge, wait.
                    if (directionOfBridge == direction && switchCounter < 8){
                        switchCounter++;
                        mutex.release();

                        passingTheBridgeQueue.acquire();
                        if (!direction) {
                            System.out.println(getName() + " is on the bridge! " + "left to right " +  direction
                                    + " Current direction: " + directionOfBridge + " " + (switchCounter) + " / 8");
                        } else if (direction) {
                            System.out.println(getName() + " is on the bridge! " + "right to left " +  direction
                                    + " Current direction: " + directionOfBridge + " " + (switchCounter) + " / 8");
                        }
                        //When 8 people from one direction have crossed, switch the directions.
                        mutex.acquire();
                        if (switchCounter == 8) {
                            if (leftPool == 0 || rightPool == 0){
                                System.out.println("WE ARE NOT SWITCHING BECAUSE THERE ARE NO PEOPLE FROM THE " +
                                        "OTHER SIDE.");
                            } else {
                                directionOfBridge = !directionOfBridge;
                                assert waitingPeople == waitingTheDirectionToChangeQueue.getQueueLength();
                                assert switchCounter == 8;
                                waitingTheDirectionToChangeQueue.release(waitingPeople);
                                waitingPeople = 0;
                                System.out.println("WE SWITCHED DIRECTION BECAUSE PEOPLE FROM THE OTHER SIDE GOT " +
                                        "TIRED OF WAITING");
                            }
                            switchCounter = 0;
                        }
                        mutex.release();
                        //If there are no people left in either sides of the bridge change directions accordingly.
                        mutex.acquire();
                        if (direction) {
                            rightPool--;
                        } else if (!direction) {
                            leftPool--;
                        }
                        if (!direction && leftPool == 0) {
                            directionOfBridge = true;
                            assert leftPool == 0;
                            assert waitingPeople == waitingTheDirectionToChangeQueue.getQueueLength();
                            waitingTheDirectionToChangeQueue.release(waitingPeople);
                            waitingPeople = 0;
                            switchCounter = 0;
                            System.out.println("WE SWITCHED DIRECTIONS BECAUSE I WAS THE LAST PERSON FROM MY SIDE");
                        } else if (direction && rightPool == 0) {
                            directionOfBridge = false;
                            assert rightPool == 0;
                            assert waitingPeople == waitingTheDirectionToChangeQueue.getQueueLength();
                            waitingTheDirectionToChangeQueue.release(waitingPeople);
                            waitingPeople = 0;
                            switchCounter = 0;
                            System.out.println("WE SWITCHED DIRECTIONS BECAUSE I WAS THE LAST PERSON FROM MY SIDE");
                        }
                        //Check if someone is not left to wait forever.
                        peopleRemaining--;
                        if (peopleRemaining == 0){
                            assert waitingPeople == 0;
                        }
                        mutex.release();

                        stop = true;

                        passingTheBridgeQueue.release();
                    }
//                    else {
////                        waitingPeople++;
////                        mutex.release();
////                        waitingTheDirectionToChangeQueue.acquire();
//                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void justLive() {
            try {
                System.out.println(getName() + " is working/getting education " + direction);
                Thread.sleep((int) (Math.random() * 1000));
            } catch (InterruptedException e) {
                //...
            }
        }

        private void addPeople(){
            for (int i = 0; i < NR_OF_PEOPLE; i++) {
                person[i] = new Person("P" + i, true); /* argument list can be extended */
                rightPool++;
            }
            for (int i = 0 ; i < NR_OF_PEOPLE; i++){
                person[i].start();
            }
        }
    }
}
