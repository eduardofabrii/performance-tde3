
# Relatório: Jantar dos Filósofos

**Aluno:** João Vitor Correa Oliveira e Eduardo Fabri  
**Matéria:** Performance de Sistemas Ciberfísicos

## O Problema

O Jantar dos Filósofos modela cinco filósofos sentados numa mesa circular, alternando entre **pensar** e **comer**. Para comer, cada filósofo precisa dos dois garfos (esquerda e direita), que são compartilhados com os vizinhos.

## Por que ocorre Deadlock?

O protocolo ingênuo "pegar primeiro um garfo, depois o outro" pode causar impasse se todos pegarem simultaneamente o garfo da esquerda e aguardarem o da direita - ninguém progride e todos esperam indefinidamente.

## As Quatro Condições de Coffman para Deadlock

Para deadlock ocorrer, **todas** estas condições devem estar presentes:

1. **Exclusão Mútua**: Recursos não podem ser compartilhados (garfo só para um filósofo)
2. **Manter e Esperar**: Processos seguram recursos enquanto esperam outros (filósofo segura um garfo esperando outro)
3. **Não Preempção**: Recursos não podem ser forçadamente removidos (não pode tirar garfo de outro filósofo)
4. **Espera Circular**: Existe um ciclo de dependência (filósofo A espera recurso do B, B espera do C, ..., que espera do A)

## Nossa Solução: Hierarquia de Recursos

**Estratégia:** Atribuir um índice único a cada garfo e forçar todos os filósofos a requisitarem primeiro o garfo de **menor índice**, depois o de **maior índice**.

### Como Elimina a Espera Circular

A hierarquia impõe uma **ordem global** na aquisição de recursos, tornando impossível formar um ciclo de dependência:

- **Filósofo 0**: Garfos (0,1) → pega garfo 0 primeiro, depois 1
- **Filósofo 1**: Garfos (1,2) → pega garfo 1 primeiro, depois 2  
- **Filósofo 2**: Garfos (2,3) → pega garfo 2 primeiro, depois 3
- **Filósofo 3**: Garfos (3,4) → pega garfo 3 primeiro, depois 4
- **Filósofo 4**: Garfos (4,0) → pega garfo **0 primeiro** (menor), depois 4

**Resultado:** Filósofos 0 e 4 competem pelo garfo 0. Apenas um consegue, o outro espera sem segurar nenhum recurso. **Não há ciclo** → **Não há deadlock**

## Pseudocódigo da Solução

```
Dados: N = 5 filósofos, Garfos 0..4

Para cada filósofo p (0 ≤ p < 5):
    garfo_esquerda = p
    garfo_direita = (p + 1) mod 5
    
    left = min(garfo_esquerda, garfo_direita)
    right = max(garfo_esquerda, garfo_direita)
    
    Loop infinito:
        pensar()
        estado[p] ← "com fome"
        
        adquirir(left)    // Sempre menor índice primeiro
        adquirir(right)   // Depois maior índice
        
        estado[p] ← "comendo"
        comer()
        
        liberar(right)    // Libera na ordem reversa
        liberar(left)
        
        estado[p] ← "pensando"
```

## Por que a Solução Funciona

### Condição de Coffman Eliminada: **Espera Circular**

A hierarquia de recursos quebra especificamente a **4ª condição de Coffman** (espera circular) ao impor uma ordem global fixa. As outras três condições permanecem:

- ✅ **Exclusão Mútua**: Mantida (garfos não compartilháveis)
- ✅ **Manter-e-Esperar**: Mantida (filósofo pode segurar um garfo esperando outro)  
- ✅ **Não Preempção**: Mantida (garfos não são forçadamente removidos)
- ❌ **Espera Circular**: **ELIMINADA** pela ordem global (menor → maior)

### Garantias da Solução

- **Ausência de deadlock** por design
- **Progresso garantido** para todos os filósofos  
- **Justiça preservada** pela ordem determinística
- **Implementação simples** sem árbitros externos

## Aplicabilidade

Esta técnica pode ser aplicada em sistemas concorrentes onde múltiplos processos competem por recursos: sistemas operacionais, bancos de dados, aplicações distribuídas, etc.