# agent-memory-bench

**Fenêtre, résumé, journal d'événements ou récupération : un banc d'essai pour choisir la mémoire de votre agent Java sur des conversations longues, avec des faits qui changent et des faits à oublier.**

Dépôt de démo du talk « La mémoire de vos agents Java : fenêtre, résumé ou journal d'événements ? » (proposé à Devoxx France 2027).

- Page de démo (rejouer une conversation tour par tour) : https://mekkiamiri.github.io/agent-memory-bench/
- Slides : https://mekkiamiri.github.io/agent-memory-bench/slides/

## Ce qui tourne

- **4 stratégies de mémoire** derrière une même interface `StrategieMemoire` :
  `FenetreGlissante`, `Resume`, `JournalEvenements` (journal append-only, compaction sur frontière
  de tour, état des faits rejoué depuis le journal), `Recuperation` (derniers messages + anciens
  messages proches de la question). Plus une référence, `HistoriqueComplet`.
- **La session de Spring AI 2.1** (`spring-ai-session` 2.1.0-RC2 : `DefaultSessionService`,
  `TurnCountTrigger`, `WindowCompactionStrategy`) branchée sur le même banc (`SessionSpringAi`).
- **3 conversations scénarisées** de 34, 53 et 71 tours (`src/main/resources/scenarios/`), avec des
  faits qui changent en cours de route, des faits à oublier volontairement, et des questions de
  rappel à réponse connue. La réponse attendue est recalculée et vérifiée à la lecture.
- **Un noteur** : rappel correct, fait périmé ou oublié ressorti, fait manqué, et taille du prompt
  à chaque tour (caractères et tokens estimés). Les valeurs sont reconnues comme mots entiers ; la réponse
  du modèle à une question de rappel n'entre pas dans l'historique (réponse fixe à la place).
- **37 tests JUnit**, une CI Maven (Java 21) sans aucun appel à un vrai modèle.
- **Une page** qui rejoue la conversation d'exemple en JavaScript, avec les mêmes stratégies, et qui
  vérifie elle-même qu'elle obtient exactement la trace du banc Java.

## Lancer

```bash
mvn -B verify                 # les tests
mvn -q compile exec:java      # le rapport du banc, avec le faux modèle
```

Le rapport sort des chiffres : ils viennent d'un **faux modèle déterministe** et vérifient la
plomberie. Ils ne disent pas quelle stratégie est la meilleure. Pour mesurer avec un vrai modèle :
[PROTOCOLE.md](PROTOCOLE.md).

## État

| Élément | État |
|---|---|
| 4 stratégies + référence, interface commune | fait, testé |
| Session Spring AI 2.1 (RC2) sur le même banc | fait, sans résumé ni faits structurés |
| 3 scénarios longs (faits qui changent, faits à oublier) | fait |
| Noteur (rappel, périmé, taille du contexte) | fait, testé |
| Page : rejouer tour par tour, conformité avec le banc Java | fait |
| Résultats avec un vrai modèle (rappel, faits périmés, tokens réels) | **à mesurer** |
| Résumé par un vrai modèle (`Resumeur`, `ChatModelSummarizer`) | à faire |
| Extraction réelle des faits (outil `remember` / `forget`) au lieu des annotations | à faire |
| Récupération par embeddings (`VectorStore`) au lieu de la proximité lexicale | à faire |

Biais connus : le journal d'événements reçoit les faits annotés du scénario (extracteur supposé
parfait) ; la récupération est lexicale. Détails dans [PROTOCOLE.md](PROTOCOLE.md).

## Structure

```
src/main/java/.../strategie/   les stratégies de mémoire
src/main/java/.../scenario/    lecture des scénarios
src/main/java/.../notation/    faux modèle, noteur, mesures
src/main/resources/scenarios/  les conversations scénarisées (format texte)
docs/index.html                la page de démo
slides/slides.html             la source des slides (docs/slides/index.html est généré)
outils/maj_page.py             recopie le scénario et la trace Java dans la page
```

## Licence

Apache-2.0, voir [LICENSE](LICENSE).
