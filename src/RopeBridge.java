import java.util.concurrent.Semaphore;

public class RopeBridge {
    /**
     * your s(hared) data structures to guarantee correct behaviour of the people
     * in passing the rope bridge
     **/
    private static final int NR_OF_PEOPLE = 40;
    private static final int BRIDGE_CAPACITY = 3;
    private int remaining = NR_OF_PEOPLE;
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
            if (i % 2 == 0 /*i <= 2*/ ) {
                person[i] = new Person("P" + i, false); /* argument list can be extended */
                leftPool++;
            } else {
                person[i] = new Person("P" + i, true); /* argument list can be extended */
                rightPool++;
            }
        }

        //The side that has more people on it becomes the first direction of the bridge.
        directionOfBridge = leftPool <= rightPool;

        System.out.println("****Left: "+leftPool+"*****************************Right: "+rightPool+"***********************************Total: " + NR_OF_PEOPLE);
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

                    //Check if the direction of the person matches that of the bridge.
                    //If it doesn't wait till direction is changed.
                    mutex.acquire();
                    if (directionOfBridge != direction){
                        //Increase number of people waiting for the direction to change.
                        waitingPeople++;
                        mutex.release();
                        //Queue.
                        waitingTheDirectionToChangeQueue.acquire();
                        mutex.acquire();
                        waitingPeople--;
                        mutex.release();
                    } else {
                        mutex.release();
                    }


                    mutex.acquire();
                    //If the direction the person matches that of the bridge and the consecutive counter is lower than
                    //8 go on the bridge.
                    if (directionOfBridge == direction && switchCounter < 8){
                        switchCounter++;
                        mutex.release();

                        //Enter the bridge.
                        passingTheBridgeQueue.acquire();
                        System.out.println(getName() + "Has entered the bridge. Current direction: "
                                + directionOfBridge + " Person direction: " + direction);

                        mutex.acquire();
                        //Reduce the number of remaining people.
                        remaining--;
                        //Reduce the number of people, depending on, from which side they passed.
                        if (direction) {
                            rightPool--;
                        } else if (!direction) {
                            leftPool--;
                        }

                        if (!direction && leftPool == 0) {
                            //If you are the last person on your side, change the directions so other people can pass.
                            int temp = waitingPeople;
                            //Change the direction of the bridge.
                            directionOfBridge = true;
                            switchCounter = 0;
                            System.out.println("WE SWITCHED DIRECTIONS BECAUSE I WAS THE LAST PERSON FROM MY SIDE "
                                    + remaining + " left " + leftPool + " right " + rightPool);
                            //Release the waiting people.
                            waitingTheDirectionToChangeQueue.release(temp);

                        } else if (direction && rightPool == 0) {
                            //If you are the last person on your side, change the directions so other people can pass.
                            int temp = waitingPeople;
                            //Change the direction of the bridge.
                            directionOfBridge = false;
                            switchCounter = 0;
                            System.out.println("WE SWITCHED DIRECTIONS BECAUSE I WAS THE LAST PERSON FROM MY SIDE "
                                    + remaining + " left " + leftPool + " right " + rightPool);
                            //Release the waiting people.
                            waitingTheDirectionToChangeQueue.release(temp);

                        } else if (switchCounter == 8) {
                            //If 8 people have passed from one side, it is time to change directions.
                            if (leftPool == 0 || rightPool == 0){
                                //If there are no people left on either side, do not change the direction.
                                System.out.println("WE ARE NOT SWITCHING BECAUSE THERE ARE NO PEOPLE FROM THE " +
                                        "OTHER SIDE." + remaining + " left " + leftPool + " right " + rightPool);
                                switchCounter = 0;
                            } else {
                                int temp = waitingPeople;
                                //Change the direction of the bridge to the opposite.
                                directionOfBridge = !directionOfBridge;
                                switchCounter = 0;
                                System.out.println("WE SWITCHED DIRECTION BECAUSE PEOPLE FROM THE OTHER SIDE GOT TIRED " +
                                        remaining + " left " + leftPool + " right " + rightPool);
                                //Release all of the waiting people.
                                waitingTheDirectionToChangeQueue.release(temp);
                            }
                        }

                        mutex.release();
                        //Stop the lifecycle of the person.
                        stop = true;
                        //Leave the bridge.
                        passingTheBridgeQueue.release();

                    } else {
                        mutex.release();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void justLive() {
            try {
                System.out.println(getName() + " is working/getting education.");
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
