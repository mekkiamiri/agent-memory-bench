package io.github.mekkiamiri.memorybench.scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * Question de rappel à réponse connue.
 *
 * @param cle      fait interrogé
 * @param attendu  valeur courante attendue, ou {@code null} si le fait a été oublié volontairement
 * @param perimees valeurs qui ne doivent plus ressortir : anciennes valeurs, ou valeurs oubliées
 */
public record Sonde(String cle, String attendu, List<String> perimees) {

    public Sonde {
        perimees = List.copyOf(perimees);
    }

    public boolean surUnOubli() {
        return attendu == null;
    }

    /** Toutes les valeurs connues de la clé à ce moment (périmées puis attendue). */
    public List<String> candidats() {
        List<String> c = new ArrayList<>(perimees);
        if (attendu != null) {
            c.add(attendu);
        }
        return c;
    }
}
