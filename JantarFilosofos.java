public class JantarFilosofos {
    private static final int QUANTIDADE_FILOSOFOS = 5;

    private enum Estado {
        PENSANDO, COM_FOME, COMENDO
    }

    static class Filosofo implements Runnable {
        private final int id;
        private Estado estado;
        private final Object GARFO_ESQUERDO;
        private final Object GARFO_DIREITO;

        public Filosofo(int id, Object[] garfos) {
            this.id = id;
            this.estado = Estado.PENSANDO;

            int garfoEsquerdo = id;
            int garfoDireito = (id + 1) % QUANTIDADE_FILOSOFOS;

            int esquerda = Math.min(garfoEsquerdo, garfoDireito);
            int direita = Math.max(garfoEsquerdo, garfoDireito);

            this.GARFO_ESQUERDO = garfos[esquerda];
            this.GARFO_DIREITO = garfos[direita];
        }

        public void run() {
            try {
                while (true) {
                    estado = Estado.PENSANDO;
                    System.out.println("Filósofo " + id + " está PENSANDO");
                    Thread.sleep(1000);

                    estado = Estado.COM_FOME;
                    System.out.println("Filósofo " + id + " está COM FOME e está tentando pegar garfos");

                    synchronized (GARFO_ESQUERDO) {
                        System.out.println("Filósofo " + id + " pegou o garfo DA ESQUERDA");
                        synchronized (GARFO_DIREITO) {
                            System.out.println("Filósofo " + id + " pegou o garfo DA DIREITA");

                            estado = Estado.COMENDO;
                            System.out.println("Filósofo " + id + " está COMENDO");
                            Thread.sleep(1500);
                        }
                    }

                    System.out.println("Filósofo " + id + " terminou de comer, está limpando os garfos");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Filósofo " + id + " interrompido");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("O JANTAR COMEÇOU!!!");
        Object[] garfos = new Object[QUANTIDADE_FILOSOFOS];

        for (int i = 0; i < QUANTIDADE_FILOSOFOS; i++) {
            garfos[i] = new Object();
        }

        for (int i = 0; i < QUANTIDADE_FILOSOFOS; i++) {
            Filosofo filosofo = new Filosofo(i, garfos);
            Thread t = new Thread(filosofo);
            t.start();
        }
    }
}
