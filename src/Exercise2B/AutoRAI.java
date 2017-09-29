package Exercise2B;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AutoRAI {
    //TODO: This doesn't seem to really have an effect, since viewers go in one after each other (since that's how monitors work) anyways.
    private static final int VISITOR_LIMIT = 4;

    private Lock lock;
    //Conditions for viewers.
    private Condition viewerSpaceAvailable, viewersAllowed;
    //Conditions for buyers.
    private Condition buyerAllowed;

    private int numberOfVisitors = 0;
    private int successiveBuyers = 0;
    private int numberOfWaitingViewers = 0;
    private int numberOFWaitingBuyers = 0;
    private Buyer buyerInRoom;

    public AutoRAI() {
        lock = new ReentrantLock();
        viewerSpaceAvailable = lock.newCondition();
        viewersAllowed = lock.newCondition();

        buyerAllowed = lock.newCondition();
    }

    private boolean noViewerSpaceAvailable() {
        return numberOfVisitors == VISITOR_LIMIT;
    }

    private boolean noVisitorsInside() {
        return numberOfVisitors == 0;
    }

    //TODO: Make the code work with a single run of threads as well. Endless loops function properly.

    public void viewerLoginAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            while (noViewerSpaceAvailable()) {
                //Wait, since the room is full and no new people can enter.
                System.out.println(Thread.currentThread().getName() + " - VIEWER - has to wait for space to become available.");
                viewerSpaceAvailable.await();
            }

            numberOfWaitingViewers++;

            while (buyerInRoom != null || numberOFWaitingBuyers > 0) {
                //Wait, since there's a buyer in the room.
                System.out.println(Thread.currentThread().getName() + " - VIEWER - has to wait for the buyer(s).");
                viewersAllowed.await();
            }

            numberOfWaitingViewers--;
            numberOfVisitors++;

            //Go to the Viewer class to see what it does...
        } finally {
            lock.unlock();
        }
    }

    public void viewerLogoutAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //Welcome back from the Viewer class!

            numberOfVisitors--;

            if (successiveBuyers == 4 || numberOFWaitingBuyers == 0) {
                //As a viewer has left, let a new viewer in [4 SUCCESSIVE BUYERS WERE LET IN BEFORE]
                viewersAllowed.signal();
                //Let all currently waiting viewers move into the line to wait for space to clear.
                viewerSpaceAvailable.signalAll();
            } else {
                //Let a waiting buyer in.
                buyerAllowed.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void buyerLoginAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            while (successiveBuyers == 4 || !noVisitorsInside()) {
                numberOFWaitingBuyers++;
                //Wait until all currently waiting viewers have logged out again.
                System.out.println(Thread.currentThread().getName() + " - BUYER - has to wait for everybody to leave the AutoRAI.");
                buyerAllowed.await();

                if (numberOFWaitingBuyers > 0) {
                    numberOFWaitingBuyers--;
                }
            }

            //TODO: Make this piece more safer, instead of this clunky cast.
            buyerInRoom = (Buyer) Thread.currentThread();
            numberOfVisitors++;
            successiveBuyers++;

            //Go to the Buyer class to see what it does...
        } finally {
            lock.unlock();
        }
    }

    public void buyerLogoutAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //Welcome back from the Buyer class!

            buyerInRoom = null;
            numberOfVisitors--;

            if (successiveBuyers < 4) {
                //The buyers still have priority...
                buyerAllowed.signal();
            } else if (numberOfWaitingViewers == 0) {
                //There are no viewers waiting, so just let buyers continue to go in, until a new viewer comes into the line.
                successiveBuyers--;
                buyerAllowed.signal();
            } else {
                //Now that 4 buyers have gone in after each other, you should allow all waiting visitors in.
                numberOFWaitingBuyers = 0;
                successiveBuyers = 0;
                viewersAllowed.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}
