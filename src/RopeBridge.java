import java.util.concurrent.Semaphore;

public class RopeBridge {
    /**
     * your s(hared) data structures to guarantee correct behaviour of the people
     * in passing the rope bridge
     **/
    private static final int NR_OF_PEOPLE = 5;
    private static final int BRIDGE_CAPACITY = 3;
    private int leftPool = NR_OF_PEOPLE;
    private int rightPool = 0;
    private int waitingPeople = 0;
    //false = left
    //true = right
    private boolean leftToRight = false;
    private Semaphore mutex = new Semaphore(1);

    private Person[] person = new Person[NR_OF_PEOPLE];

    private Semaphore currentDirection, waitingQueue;

    public RopeBridge() {

        currentDirection = new Semaphore(BRIDGE_CAPACITY, true);
        waitingQueue = new Semaphore(0, true);

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

                    //If this is the direction of the person proceed.
                    //If this is not the direction of the person, queue on the "waitingPeople line" until the direction is
                    //changed.

                    if (leftToRight == direction){
                        //The direction of the person matches that of the bridge.
                        currentDirection.acquire();

                        //Print from where to where the person is going.
                        if (!direction){
                            System.out.println(getName() + " is on the bridge! " + "left to right");
                        }else if (direction){
                            System.out.println(getName() + " is on the bridge! " + "right to left");
                        }


                        //Wait for a moment...
                        Thread.sleep((2000));

                        //Here it all has to happen...

                        //Move people from one pool to another.

                        if (direction){
                            //Update the number of people on each side.
                            mutex.acquire();
                            rightPool--;
                            leftPool++;
                            mutex.release();
                        } else if (!direction){
                            //Update the number of people on each side.
                            mutex.acquire();
                            leftPool--;
                            rightPool++;
                            mutex.release();
                        }

                        //Change your direction
                        direction = !direction;

                        currentDirection.release();

                    } else {

                        //This code is executed if the person's direction doesn't mach that of the bridge.


                        //Update the number of people that are waiting.
                        mutex.acquire();
                        waitingPeople++;
                        mutex.release();

                        System.out.println(getName() + " has queued." + waitingPeople);

                        //If the number of waitingPeople people is 5(aka everyone has passed the bridge and now are
                        // waiting for direction change), this means it is time for a change...
                        if (waitingPeople == 5){
                            mutex.acquire();
                            //First, "release" all other people from the waitingPeople queue.
                            waitingQueue.release(4);

                            //Change the direction depending on the current direction of the bridge.
                            if (leftPool == 0){
                                leftToRight = true;
                            } else if (rightPool == 0){
                                leftToRight = false;
                            }

                            //Resets values so the process can be repeated.
                            //Make a new semaphore so people can wait for the direction to change.
                            waitingQueue = new Semaphore(0, true);
                            waitingPeople = 0;
                            mutex.release();

                        } else {

                            //Wait for the direction to change.
                            waitingQueue.acquire();
                            System.out.println(getName() + "stopped waitingPeople");
                        }

                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
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
