public class DeadlockDemo {
                                    //monitor
    static final Object LOCK_A = new Object();
    static final Object LOCK_B = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            //trava
            synchronized (LOCK_A) {
                System.out.println("T1: Peguei LOCK_A, esperando LOCK_B...");
                dormir(50);
                synchronized (LOCK_B) {
                    System.out.println("T1 concluiu");
                }
            }
        });

        Thread t2 = new Thread(() -> {
            //trava
            synchronized (LOCK_B) {
                System.out.println("T2: Peguei LOCK_B, esperando LOCK_A...");
                dormir(50);
                synchronized (LOCK_A) {
                    System.out.println("T2 concluiu");
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println("Sistema iniciado. Aguardando conclusão...");
    }
    //causa o conflito de tempo
    static void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}