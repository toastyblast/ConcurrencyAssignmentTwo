package Exercise2B;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that simulates the system administration of the AutoRAI of 2018, and how both Buyer and Viewer-type Threads
 * should behave.
 */
public class AutoRAI {
    private static final int VISITOR_LIMIT = 5;
    private static final int SUCCESSIVE_BUYER_LIMIT = 4;

    private Lock lock;
    //Conditions for viewers.
    private Condition viewerCanEnterLine, viewersAllowed;
    //Conditions for buyers.
    private Condition buyerAllowed;

    private int numberOfVisitorsInside = 0;
    private int successiveBuyers = 0;
    private int numberOfWaitingViewers = 0;
    private int numberOFWaitingBuyers = 0;
    private Buyer buyerInRoom;

    //Used to stop new users rushing into the line after 4 successive buyers have entered the AutoRAI.
    private boolean letNewViewersInLine = true;

    //Used to let viewers waiting in line know that they can enter. This boolean is used to stop new viewers from
    // rushing in even if a buyer came in line. Without this, a buyer has to wait until a viewer that was in the AutoRAI
    // while the buyer arrived reaches is end statement, letting more viewers in in the meantime before the buyer.
    private boolean viewerTurn = true;

    public AutoRAI() {
        lock = new ReentrantLock();
        viewerCanEnterLine = lock.newCondition();
        viewersAllowed = lock.newCondition();

        buyerAllowed = lock.newCondition();
    }

    private boolean noViewerSpaceAvailable() {
        return numberOfVisitorsInside == VISITOR_LIMIT;
    }

    private boolean noVisitorsInside() {
        return numberOfVisitorsInside == 0;
    }

    private boolean buyersAreWaiting() {
        return numberOFWaitingBuyers > 0;
    }

    private boolean viewersAreWaiting() {
        return numberOfWaitingViewers > 0;
    }

    private boolean noViewersWaiting() {
        return numberOfWaitingViewers == 0;
    }

    private boolean successiveBuyerLimitReached() {
        return successiveBuyers == SUCCESSIVE_BUYER_LIMIT;
    }

    /**
     * Method used by Viewer-type Threads. Simulates the entering of the line at AutoRAI by Viewers, after that their
     * entry into the AutoRAI.
     *
     * @throws InterruptedException is an exception thrown when the thread is interrupted.
     */
    public void viewerLoginAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            while (!letNewViewersInLine) {
                //Four successive buyers have gone into the AutoRAI. All Viewers in line should be let in, but newly
                // arrived viewers should be stopped from rushing into line and creating an endless stream of Viewers,
                // this is what makes sure that doesn't happen.
                System.out.println(Thread.currentThread().getName() + " - VIEWER - has been blocked out of the line.");
                viewerCanEnterLine.await();
            }

            while (buyerInRoom != null || !viewerTurn || noViewerSpaceAvailable()) {
                numberOfWaitingViewers++;

                //Debug messages to get more clarity on what a viewer is waiting for.
//                if (buyerInRoom != null) {
//                    System.out.println(Thread.currentThread().getName() + " - VIEWER - has to wait for the buyer(s).");
//                } else if (noViewerSpaceAvailable()) {
//                    System.out.println(Thread.currentThread().getName() + " - VIEWER - has to wait for space to become available.");
//                }

                //Wait, since there's either no space, or a buyer is in the room.
                viewersAllowed.await();

                numberOfWaitingViewers--;
            }

            numberOfVisitorsInside++;

            //Go to the Viewer class to see what it does...
        } finally {
            lock.unlock();
        }
    }

    /**
     * Once the Viewer-type Thread is done in the AutoRAI, this method is called to simulate them leaving and the system
     * updating who can come in after them.
     *
     * @throws InterruptedException is an exception thrown when the thread is interrupted.
     */
    public void viewerLogoutAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //Welcome back from the Viewer class!
            //The viewer has been in the Auto RAI and is now signalling that they are leaving.

            numberOfVisitorsInside--;

            if (!viewerTurn && buyersAreWaiting()) {
                //Buyers have priority over viewers, make sure to wake up a buyer if one travelled to the AutoRAI whilst
                // you were visiting.
                successiveBuyers = 0;

                buyerAllowed.signal();
            }
            else if (viewersAreWaiting()) {
                //If there are no buyers waiting or it is the turn for the viewers, let the viewers in line know they
                // can enter the AutoRAI.
                viewersAllowed.signalAll();
            }
            else if (noViewersWaiting()) {
                //Last viewer to go into the AutoRAI after 4 successive buyers had entered, has to make sure that buyers
                // are allowed in again as priority.
                viewerTurn = false;
                letNewViewersInLine = true;

                //Let any viewers waiting to enter the line know they can now go into the line (or even enter the AutoRAI
                // immediately if no buyers were in line).
                viewerCanEnterLine.signalAll();

                if (successiveBuyerLimitReached()) {

                    if (buyersAreWaiting()) {
                        //If there are buyers still waiting once, set successive buyers to 0 and let a buyer know they
                        // can come in.
                        successiveBuyers = 0;
                        buyerAllowed.signal();
                    }
                }
            }

            System.out.println(Thread.currentThread().getName() + " - VIEWER - Has left the AutoRAI");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Simulation method of a Buyer-type Thread going to the AutoRAI and trying to enter it, ending when the Buyer does
     * succeed in going into the AutoRAI.
     *
     * @throws InterruptedException is an exception thrown when the thread is interrupted.
     */
    public void buyerLoginAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            while (successiveBuyerLimitReached() || !noVisitorsInside()) {
                numberOFWaitingBuyers++;

                if (!noVisitorsInside() && successiveBuyers < SUCCESSIVE_BUYER_LIMIT) {
                    //If you should be prioritized, but there are still viewers inside, stop new viewers that are in line
                    // from entering before you.
                    viewerTurn = false;
                }

                //Wait until all currently present viewers have logged out again.
                System.out.println(Thread.currentThread().getName() + " - BUYER - has to wait for everybody to leave the AutoRAI.");
                buyerAllowed.await();

                numberOFWaitingBuyers--;
            }

            buyerInRoom = (Buyer) Thread.currentThread();

            numberOfVisitorsInside++;
            successiveBuyers++;

            //Go to the Buyer class to see what it does...
        } finally {
            lock.unlock();
        }
    }

    /**
     * Method called by Buyer-type Threads to simulate them leaving and the system of the AutoRAI notifying the right
     * people on who can enter after them.
     *
     * @throws InterruptedException is an exception thrown when the thread is interrupted.
     */
    public void buyerLogoutAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //Welcome back from the Buyer class!
            //The buyer has been in the Auto RAI and is now signalling that they are leaving.

            buyerInRoom = null;
            numberOfVisitorsInside--;

            if (buyersAreWaiting() && successiveBuyers < SUCCESSIVE_BUYER_LIMIT) {
                //Buyers still have priority...
                buyerAllowed.signal();
            }
            else if (successiveBuyerLimitReached()) {
                //4 buyers have gone in in succession, of which you're the last.

                if (viewersAreWaiting()) {
                    //And there are viewers waiting in line, so let them in first now.
                    viewerTurn = true;
                    //Make sure that no new Viewers quickly rush into line though.
                    letNewViewersInLine = false;

                    viewersAllowed.signalAll();
                }
                else {
                    //But there are no viewers waiting in line to go in, so just let another buyer in.
                    successiveBuyers--;

                    buyerAllowed.signal();
                }
            }
            else if (noViewersWaiting()) {
                //There are no viewers waiting anymore, but there are still buyers that want to go in, so let one in..
                buyerAllowed.signal();
            }
            else {
                //There are no buyers waiting anymore, but there are viewers waiting, so let them in then.
                viewerTurn = true;
                letNewViewersInLine = true;

                viewerCanEnterLine.signalAll();

                viewersAllowed.signalAll();
            }

            System.out.println(Thread.currentThread().getName() + " - BUYER - Has left the AutoRAI");
        } finally {
            lock.unlock();
        }
    }
}

