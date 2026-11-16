package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;

/** Événements du journal. Le journal ne fait qu'ajouter : rien n'est jamais réécrit. */
public sealed interface Evenement {

    /** Un message de la conversation. */
    record MessageAjoute(Message message) implements Evenement {
    }

    /** Un fait posé ou mis à jour. */
    record FaitPose(String cle, String valeur) implements Evenement {
    }

    /** Un fait à oublier : il sort de l'état et les messages qui le citent sortent de la vue. */
    record FaitOublie(String cle) implements Evenement {
    }

    /**
     * Frontière de compaction : à partir d'ici, la vue ne montre plus que les messages
     * dont la position est supérieure ou égale à {@code premierMessageVisible}.
     */
    record Frontiere(int premierMessageVisible) implements Evenement {
    }
}
