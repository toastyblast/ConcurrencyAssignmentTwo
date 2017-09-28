package Exercise2B;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AutoRAI {
    private static final int VISITOR_LIMIT = 10;

    private Lock lock;
    //Conditions for viewers.
    private Condition viewerSpaceAvailable;
    //Conditions for buyers.
    private Condition buyerAllowed;

    private int numberOfVisitors = 0;
    private int successiveBuyers = 0;
    private Buyer buyerInRoom;

    public AutoRAI() {
        lock = new ReentrantLock();
        viewerSpaceAvailable = lock.newCondition();
        buyerAllowed = lock.newCondition();
    }

    private boolean noViewerSpaceAvailable() {
        return numberOfVisitors == VISITOR_LIMIT;
    }

    private boolean noViewers() {
        return numberOfVisitors == 0;
    }

    public void viewAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            while (noViewerSpaceAvailable() || buyerInRoom != null) {
                System.out.println(Thread.currentThread().getName() + " has to wait to go into the AutoRAI.");
                viewerSpaceAvailable.await();
            }

            numberOfVisitors++;

            //Do something more here, as you're now in the AutoRAI.
            System.out.println(Thread.currentThread().getName() + " is in the AutoRAI!");

            //...
        } finally {
            lock.unlock();
        }
    }

    public void leaveViewAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //...

            System.out.println(Thread.currentThread().getName() + " is leaving the AutoRAI!");

            numberOfVisitors--;
            viewerSpaceAvailable.signal();
        } finally {
            lock.unlock();
        }
    }

    public void buyAtAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //...

            while (successiveBuyers == 4) {
                System.out.println(Thread.currentThread().getName() + " has to wait to buy at the AutoRAI.");
                buyerAllowed.await();
            }

            numberOfVisitors++;
            successiveBuyers++;
            buyerInRoom = (Buyer) Thread.currentThread();

            //...
        } finally {
            lock.unlock();
        }
    }

    public void leaveBuyAutoRAI() throws InterruptedException {
        lock.lock();

        try {
            //...

            buyerInRoom = null;
            numberOfVisitors--;

            if (successiveBuyers < 4) {
                buyerAllowed.signal();
            } else {
                successiveBuyers = 0;
                viewerSpaceAvailable.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    //Methods...
}
