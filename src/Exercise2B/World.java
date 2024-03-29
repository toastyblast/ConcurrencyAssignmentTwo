package Exercise2B;

/**
 * Application class. Run this to perform Exercise2B.
 */
public class World {
    private static final int NR_OF_VIEWERS = 11;
    private static final int NR_OF_BUYERS = 9;

    public static void main(String[] args) {
        AutoRAI autoRAI = new AutoRAI();
        Thread[] viewers = new Thread[NR_OF_VIEWERS];
        Thread[] buyers = new Thread[NR_OF_BUYERS];

        //Create and start all of the viewers
        for (int i = 0; i < NR_OF_VIEWERS; i++) {
            viewers[i] = new Viewer("V" + i, autoRAI);
            viewers[i].start();
        }

        //Then also create and start all of the buyers.
        for (int i = 0; i < NR_OF_BUYERS; i++) {
            buyers[i] = new Buyer("B" + i, autoRAI);
            buyers[i].start();
        }
    }
}
