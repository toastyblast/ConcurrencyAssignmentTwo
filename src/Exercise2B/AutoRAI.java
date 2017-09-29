package Exercise2B;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AutoRAI {
    private static final int VISITOR_LIMIT = 10;

    private Lock lock;
    //Conditions for viewers.
    private Condition viewerSpaceAvailable, viewersAllowed;
    //Conditions for buyers.
    private Condition buyerSpaceAvailable, buyerAllowed;

    private int numberOfVisitors = 0;
    private int successiveBuyers = 0;
    private Buyer buyerInRoom;

    public AutoRAI() {
        lock = new ReentrantLock();
        viewerSpaceAvailable = lock.newCondition();
        viewersAllowed = lock.newCondition();

        buyerSpaceAvailable = lock.newCondition();
        buyerAllowed = lock.newCondition();
    }

    private boolean noViewerSpaceAvailable() {
        return numberOfVisitors == VISITOR_LIMIT;
    }

    private boolean noVisitorsInside() {
        return numberOfVisitors == 0;
    }

    //Methods...

    public void viewerLoginAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            while (noViewerSpaceAvailable()) {
                //Wait, since the room is full and no new people can enter.
                viewerSpaceAvailable.await();
            }

            while (buyerInRoom != null) {
                //Wait, since there's a buyer in the room.
                viewersAllowed.await();
            }

            numberOfVisitors++;

            //...
        } finally {
            lock.unlock();
        }
    }

    public void viewerLogoutAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //...

            numberOfVisitors--;
            //TODO: First check if there are buyers waiting, if not, allow more viewers in.
            viewersAllowed.signal();
            buyerSpaceAvailable.signal();
        } finally {
            lock.unlock();
        }
    }

    public void buyerLoginAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            while (successiveBuyers == 4) {
                buyerAllowed.await();
            }

            while (!noVisitorsInside()) {
                buyerSpaceAvailable.await();
            }

            buyerInRoom = (Buyer) Thread.currentThread();
            numberOfVisitors++;
            successiveBuyers++;

            //...
        } finally {
            lock.unlock();
        }
    }

    public void buyerLogoutAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //...

            buyerInRoom = null;
            numberOfVisitors--;

            if (successiveBuyers < 4) {
                buyerAllowed.signal();
                buyerSpaceAvailable.signal();
                //TODO: If there's no buyer, the buyerInRoom == null. But I'm not sure
//                viewersAllowed.signalAll();
            } else {
                //Now that 4 buyers have gone in after each other, you should allow all waiting visitors in.
                viewersAllowed.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}
