# Protocole : mesurer avec un vrai modèle

La CI ne fait aucun appel à un vrai modèle. Elle vérifie la plomberie du banc avec deux faux :
`LecteurNaif` (répond la dernière valeur connue vue dans le prompt) et `ResumeurCumulatif`
(accumule les phrases porteuses de faits). Les chiffres qu'ils produisent ne disent rien
d'un vrai modèle. Ce document décrit la mesure réelle. **Aucun résultat n'est publié tant
qu'il n'a pas été mesuré de cette façon.**

## Ce qu'on mesure

Pour chaque stratégie, chaque scénario, chaque question de rappel :

1. **Rappel correct** : la réponse contient la valeur courante attendue et aucune valeur périmée
   (ou, pour un fait oublié volontairement, ne contient aucune valeur de ce fait).
2. **Fait périmé ou oublié ressorti** : la réponse contient une ancienne valeur, ou une valeur
   que l'utilisateur a demandé d'oublier.
3. **Fait manqué** : ni l'une ni l'autre.
4. **Taille du contexte envoyé à chaque tour** : caractères, tokens estimés (caractères / 4) et,
   avec un vrai modèle, tokens réels lus dans les métadonnées d'usage de la réponse.
5. **Coût de la mémoire elle-même** : appels de résumé, appels d'extraction de faits, appels
   d'embeddings, comptés à part.

Le classement (`Noteur`) est automatique (présence de chaînes). Chaque réponse classée
« manqué » ou « périmé » est relue à la main : un modèle peut reformuler une valeur
(« mille quatre cent cinquante euros ») ou refuser poliment de répondre.

## Brancher un vrai modèle

Trois points d'extension, tous des interfaces du dépôt :

| Interface | Faux en CI | Version réelle à écrire |
|---|---|---|
| `Repondeur` | `LecteurNaif` | envoie le prompt (`List<Message>`) à un `ChatClient` de Spring AI et renvoie le texte |
| `Resumeur` | `ResumeurCumulatif` | demande au modèle de résumer `resumePrecedent` + `sortants` |
| extraction des faits | annotations `{set}` / `{forget}` du scénario | un outil `remember(cle, valeur)` / `forget(cle)` exposé au modèle ; ses appels deviennent des `Fait` |

Pour la session Spring AI (`SessionSpringAi`), le résumé se branche avec
`WindowCompactionStrategy.builder().lastTurns(n).summarizer(ChatModelSummarizer.builder(chatModel).build())`.

Dépendances à ajouter dans un profil Maven séparé (jamais dans la CI) : `spring-ai-client-chat`
et un starter de modèle, par exemple `spring-ai-starter-model-ollama` (local),
`spring-ai-starter-model-anthropic` ou `spring-ai-starter-model-openai`, en version 2.1.0-RC2
(ou la 2.1.0 finale à sa sortie). Clés d'API en variables d'environnement, jamais dans un fichier.

## Déroulé

1. Figer le modèle (nom exact et version), la température (0 si possible) et la date.
2. Pour chaque stratégie et chaque scénario, rejouer la conversation tour par tour avec `Banc.jouer`
   et le `Repondeur` réel. Les réponses scénarisées de l'assistant restent celles du scénario ;
   seules les questions de rappel sont posées au modèle.
3. Répéter 3 fois chaque passage, garder les trois, rapporter l'écart entre passages.
4. Enregistrer pour chaque question : le prompt complet, la réponse brute, le verdict automatique,
   le verdict relu, les tokens réels.
5. Publier les fichiers bruts dans `results/<modele>-<date>/` avec le commit du banc utilisé.

## Biais connus, à dire pendant le talk

- Le journal d'événements reçoit les faits structurés (annotations) : c'est l'hypothèse d'un
  extracteur parfait. Sans extracteur réel, son avantage est surestimé.
- La récupération est lexicale (mots communs) ; des embeddings changeraient ses résultats.
- Les scénarios sont écrits à la main, en français, et les valeurs sont des chaînes exactes.
- Trois scénarios ne font pas une étude : c'est un banc pour comparer chez soi, sur ses propres
  conversations.
