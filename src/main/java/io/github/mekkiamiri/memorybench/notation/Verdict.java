package io.github.mekkiamiri.memorybench.notation;

/** Verdict d'une réponse à une question de rappel. */
public enum Verdict {
    /** La bonne valeur, sans valeur périmée (ou, pour un fait oublié, aucune valeur). */
    CORRECT,
    /** Une valeur périmée ou oubliée est ressortie. */
    PERIME,
    /** Ni la bonne valeur, ni une valeur périmée : le fait a été perdu. */
    MANQUE
}
