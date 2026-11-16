package io.github.mekkiamiri.memorybench.scenario;

import java.util.List;

/** Tours de remplissage, sans fait à retenir, distribués dans un ordre fixe. */
final class Bavardage {

    static final List<String[]> PAIRES = List.of(
            new String[] {"Au fait, quelle différence entre un café allongé et un americano ?",
                    "L'allongé fait passer plus d'eau dans la mouture ; l'americano ajoute de l'eau chaude à un espresso."},
            new String[] {"Tu connais une astuce pour mieux dormir ?",
                    "Des horaires réguliers, une chambre fraîche et moins d'écrans le soir aident souvent."},
            new String[] {"Comment on dit « bon courage » en italien ?",
                    "On dit « in bocca al lupo », et on répond « crepi »."},
            new String[] {"Une idée de lecture courte pour le train ?",
                    "Une nouvelle de Maupassant ou un recueil de chroniques se lit bien par morceaux."},
            new String[] {"Pourquoi le ciel est bleu, déjà ?",
                    "La lumière bleue est davantage diffusée par l'atmosphère que la lumière rouge."},
            new String[] {"Tu peux m'expliquer ce qu'est un haïku ?",
                    "Un poème japonais très bref, traditionnellement en trois segments, souvent sur une saison."},
            new String[] {"Comment garder du basilic frais plus longtemps ?",
                    "Dans un verre d'eau comme un bouquet, à température ambiante, plutôt qu'au réfrigérateur."},
            new String[] {"C'est quoi la règle du hors-jeu, en résumé ?",
                    "Un attaquant est hors-jeu s'il est plus près du but adverse que l'avant-dernier défenseur au moment de la passe."},
            new String[] {"Quelle est la meilleure façon d'apprendre un raccourci clavier ?",
                    "L'utiliser volontairement chaque jour pendant une semaine, jusqu'à ce qu'il devienne automatique."},
            new String[] {"Tu as une recette rapide avec des pois chiches ?",
                    "Un houmous : pois chiches, tahini, citron, ail, un filet d'huile, et on mixe."},
            new String[] {"Pourquoi les chats ronronnent ?",
                    "Souvent par bien-être, mais aussi pour s'apaiser quand ils sont stressés."},
            new String[] {"Comment retirer une tache de café sur un tee-shirt ?",
                    "Rincer à l'eau froide par l'envers, puis un peu de savon avant le lavage."});

    private Bavardage() {
    }

    static String[] paire(int rang) {
        return PAIRES.get(rang % PAIRES.size());
    }
}
