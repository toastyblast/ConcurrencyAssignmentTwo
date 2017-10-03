package Exercise2B;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AutoRAI {
    //TODO: This doesn't seem to really have an effect, since viewers go in one after each other (since that's how monitors work) anyways.
    private static final int VISITOR_LIMIT = 5;

    private Lock lock;
    //Conditions for viewers.
    private Condition viewerSpaceAvailable, viewersAllowed;
    //Conditions for buyers.
    private Condition buyerAllowed;

    private int numberOfVisitorsInside = 0;
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
        return numberOfVisitorsInside == VISITOR_LIMIT;
    }

    private boolean noVisitorsInside() {
        return numberOfVisitorsInside == 0;
    }

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
            numberOfVisitorsInside++;

            //Go to the Viewer class to see what it does...
        } finally {
            lock.unlock();
        }
    }

    public void viewerLogoutAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //Welcome back from the Viewer class!
            //The viewer has been in the Auto RAI and is now signalling that they are leaving.

            numberOfVisitorsInside--;

            //Since the buyers have been in 4 times consecutively, or there are none waiting, let more viewers in.
            if (successiveBuyers == 4 || numberOFWaitingBuyers == 0) {
                //As this viewer has left, let a new viewer in.
                viewersAllowed.signal();
                //Let all currently waiting viewers move into the line to wait for space to clear.
                viewerSpaceAvailable.signalAll();

                if (numberOfWaitingViewers == 0) {
                    //Let a waiting buyer in, as there are no viewers waiting anymore.
                    buyerAllowed.signal();
                }
            } else {
                //Since buyers have priority, let a waiting buyer in.
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
                //Wait until all currently present viewers have logged out again.
                System.out.println(Thread.currentThread().getName() + " - BUYER - has to wait for everybody to leave the AutoRAI.");
                buyerAllowed.await();

                if (numberOFWaitingBuyers > 0) {
                    //This number can't go under 0, but since it's reset once in a while, you still have to check.
                    numberOFWaitingBuyers--;
                }
            }

            //TODO: Make this piece more safer, instead of this clunky cast.
            buyerInRoom = (Buyer) Thread.currentThread();

            numberOfVisitorsInside++;
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
            //The buyer has been in the Auto RAI and is now signalling that they are leaving.

            buyerInRoom = null;
            numberOfVisitorsInside--;

            if (successiveBuyers < 4 && numberOFWaitingBuyers > 0) {
                //The buyers still have priority...
                buyerAllowed.signal();
            } else {
                //Four consecutive buyers have gone into the Auto RAI, which is the limit.
                viewerSpaceAvailable.signalAll();

                if (numberOfWaitingViewers == 0) {
                    //There are no viewers waiting, so just let buyers continue to go in, until a new viewer comes into the line.
                    successiveBuyers--;
                    buyerAllowed.signal();
                } else {
                    //There are waiting viewers, so let all of them in first.
                    numberOFWaitingBuyers = 0;
                    successiveBuyers = 0;
                    viewersAllowed.signalAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
