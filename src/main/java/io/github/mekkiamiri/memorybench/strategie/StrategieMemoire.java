package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;

import java.util.List;

/**
 * Une stratégie de mémoire : elle reçoit chaque message de la conversation
 * et décide de ce qui part dans le prompt au tour suivant.
 */
public interface StrategieMemoire {

    /** Nom court, stable (utilisé dans les rapports et la page). */
    String nom();

    /** Enregistre un message (utilisateur ou assistant) une fois le tour joué. */
    void enregistrer(Message message);

    /**
     * Messages d'historique à envoyer au modèle avant le message courant.
     *
     * @param requete texte du message courant de l'utilisateur (utile à la récupération)
     */
    List<Message> contexte(String requete);
}
