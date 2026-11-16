package io.github.mekkiamiri.memorybench.scenario;

import io.github.mekkiamiri.memorybench.Fait;

import java.util.List;

/**
 * Un tour : un message de l'utilisateur, puis la réponse de l'assistant.
 *
 * @param numero      numéro du tour, à partir de 1
 * @param utilisateur texte du message de l'utilisateur (annotations retirées)
 * @param faits       faits annotés dans ce message
 * @param reponse     réponse scénarisée de l'assistant, ou {@code null} pour une question de rappel
 *                    (la réponse vient alors du modèle)
 * @param sonde       question de rappel, ou {@code null}
 * @param bavardage   vrai pour un tour de remplissage sans fait
 */
public record Tour(int numero, String utilisateur, List<Fait> faits, String reponse, Sonde sonde, boolean bavardage) {

    public Tour {
        faits = List.copyOf(faits);
    }

    public boolean estQuestion() {
        return sonde != null;
    }
}
