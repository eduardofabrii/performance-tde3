# Relatório: Análises de Threads e Semáforos

## Condição de Corrida e Sincronização com Semaphroe em Java

**Aluno:** João Vitor Correa Oliveira e Eduardo Fabri
**Matéria:** Performance de Sistemas Ciberfisícos

---

## 1. Objetivo

O objetivo deste relatório é analisar e demonstrar o problema da **condição de corrida** (*race condition*) concorrência.

Para isso, foram implementados dois cenários:

1. **Cenário 1:** Múltiplas threads (8) tentam incrementar um contador compartilhado (250.000 vezes cada) sem qualquer tipo de sincronização.
2. **Cenário 2:** O mesmo processo é repetido, mas desta vez utilizando um **Semáforo binário (`Semaphore`)** para garantir a exclusão mútua e proteger o acesso ao contador.

---

## 2. Experimento 1: A Condição de Corrida (Sem Sincronização)

### Código: `CorridaSemControle.java`

```java
import java.util.concurrent.*;

public class CorridaSemControle {
   static int count = 0;

   public static void main(String[] args) throws Exception {
     int T = 8, M = 250_000;
     ExecutorService pool = Executors.newFixedThreadPool(T);
   
     Runnable r = () -> {
       for (int i = 0; i < M; i++) {
         count++;
       }
     };

     long t0 = System.nanoTime();
     for (int i = 0; i < T; i++) pool.submit(r);
   
     pool.shutdown();
     pool.awaitTermination(1, TimeUnit.MINUTES);
     long t1 = System.nanoTime();
   
     System.out.printf("Esperado=%d, Obtido=%d, Tempo=%.3fs%n",
         T * M, count, (t1 - t0) / 1e9);
   }
}

```

## Análise do Script

Nesta primeira implementação, um *pool* de 8 threads (`T=8`) foi criado. Cada thread executou um laço que incrementava a variável estática `count` 250.000 vezes (`M=250.000`). O valor final esperado para o contador era `T * M = 2.000.000`.

## Análise do problema

Foi observado que o valor obtido foi **consideravelmente menor** menor que o esperado. Baseado no contexto tão comentado na programação, a atomicidade, ela nos explica o motivo do resultado.

##### A patir de 3 execuções temos:

![1763208257175](../image/1763208257175.png) ![1763208276325](../image/1763208276325.png) ![1763208297991](../image/1763208297991.png)

A operação count que utilizamos no código, na realidade é um conjunto de outras três micros operações, **ler o valor atual do count, somar 1 ao valor, escrever o novo valor de volta em count.** E como as **threads** executam em paralelo, é possível que duas threads diferentes calculem o mesmo valor. Duas operações resultaram em apenas um aumento no contador.

***Apenas um adendo,  é possivel visualizar que o tempo de execução foi super rapido.***

# 3. Experimento 2: A Correção com Sicronização

### Código: `CorridaComSemaphore.java`

```java
c
import java.util.concurrent.*;

public class CorridaComSemaphore {
   static int count = 0;                         
              //imita um lock                           //modo justo
   static final Semaphore sem = new Semaphore(1, true); 

   public static void main(String[] args) throws Exception {
     int T = 8, M = 250_000;
    //lógica de inicialização igual a anterior
     ExecutorService pool = Executors.newFixedThreadPool(T);
   
     Runnable r = () -> {
       for (int i = 0; i < M; i++) {
         try {
           sem.acquire();
           count++;
         } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
         } finally {
           sem.release();
         }
       }
     };

     long t0 = System.nanoTime();
     for (int i = 0; i < T; i++) pool.submit(r);
   
     pool.shutdown();
     pool.awaitTermination(1, TimeUnit.MINUTES);
     long t1 = System.nanoTime();
   
     System.out.printf("Esperado=%d, Obtido=%d, Tempo=%.3fs%n",
         T * M, count, (t1 - t0) / 1e9);
   }
}
```

Para corrigir o problema da concorrência, utilizamos a biblioteca concurrent, e nele temos a Classe Semaphore, que por sua vez tem o método acquire. Com isso o mecanismo **lock** atua, ou seja **exclusão mútua**.

A váriavel **sem** do tipo **Semaphore** esta grantindo que uma thread seja executada. Com os parâmetros 1 e true, são o permitis e o fair.

```java
static final Semaphore sem = new Semaphore(1, true);
```

Aqui temos uma chamada do método acquire via a variável **Sem do tipo Semaphore (objeto).** Ele adquire a permissão necessária para acessar a thread, se tiver acessa, caso contrário há um bloqueio até a permissão ser liberada.

```Java
sem.acquire();
```

No final do tratamento de erro temos esse finally, sabemos que o finally é um processo dentro de um tratamento de erro, porém, não apenas isso. Nesta ocasiçao é ele que devolve a permissão, permitindo que a próxima thread possa adquiri-la.

Ou seja, por conta deste método **release**, não temos o famoso **deadlock.** Estamos **serializando** o acesso, fazendo com que as threads concorrentes sejam organizada em fila, não em paralelo.

```java
finally {  
 sem.release();   
 }
```

## 5. Discussão e Conclusão

* Após as analises conseguimos indentificar que, se seu objeto for **correção**, perdemos **velocidade.**
* Se seu objetivo for **velocidade**, perdemos **correção**, ou seja, inconsistência no valor final.

A partir dessa conclusão, conseguimos discutir sobre a Throughput(vazão), que no caso da utilização do Semaphore, a vazão cai, pois a threads ficam bloqueadas ao chamar Acquire. O acesso deixa de ser paralelo.

Há também um termo bastante comentando tratando-se de performance, **Overhead (custo adicional de recurso).** Pela lógica, não ser um simples count, a troca de contexto aumenta, junto com a atomicidade e o gerenciamento da fila.

| Abordagem    | Correção | Throughput | Motivo                                                                |
| ------------ | ---------- | ---------- | --------------------------------------------------------------------- |
| Sem controle | ❌ Baixa   | Alta       | Todas as threads incrementam ao mesmo tempo → mas perdem operações |
| Semaphore(1) | ✔️ Alta  | Baixa      | Acesso serializado + bloqueio →  comentado acima                   |


## Garantias **happens-before** preservadas

Ou seja, a visibilidade e ordem entre threads.

`sem.release()` da thread A → **happens-before** → `sem.acquire()` da thread B. Portanto, escritas de A são visualizadas por B após a adquirir o mesmo semaphore. 

* Esta relação é estabelecida no **JMM (Java Memory Model)** não importanto a CPU ou o Core. 

Entretanto, essa correção tem como custo uma possível redução de throughput, já que threads competindo pelo mesmo semáforo são serializadas, gerando bloqueio e reduzindo o paralelismo efetivo da aplicação.

O experimento demonstrou que ambos os scripts possuem os mesmo objetivos, porém com práticas e viez diferentes. Sem semáforo, sua **Throughput** (vazão) será maior, porém com resultados indeterminados e incorretos devido a condição de corrida. O uso da classe **Semaphore**, como mecanismo de **serialização** ou uma "formação de fila" mostrou-se consistente no resultado final dos incrementos, mas com menor vazão. Ele garante por outro lado a **atomicidade** e **integridade** dos dados.
