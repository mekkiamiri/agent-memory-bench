package io.github.mekkiamiri.memorybench.notation;

/**
 * Mesure d'un tour.
 *
 * @param tour                 numéro du tour
 * @param messages             nombre de messages envoyés (historique + message courant)
 * @param caracteres           taille du prompt en caractères
 * @param tokensEstimes        taille estimée en tokens (caractères / 4)
 * @param verdict              verdict si c'est une question de rappel, sinon {@code null}
 * @param attenduDansContexte  la bonne valeur était-elle dans le prompt ?
 * @param perimeDansContexte   une valeur périmée ou oubliée était-elle dans le prompt ?
 * @param reponse              réponse de l'assistant (scénarisée ou produite par le répondeur)
 */
public record MesureTour(int tour, int messages, int caracteres, int tokensEstimes, Verdict verdict,
                         boolean attenduDansContexte, boolean perimeDansContexte, String reponse) {
}
