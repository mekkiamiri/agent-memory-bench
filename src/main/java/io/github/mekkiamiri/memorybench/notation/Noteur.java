package io.github.mekkiamiri.memorybench.notation;

import io.github.mekkiamiri.memorybench.Contexte;
import io.github.mekkiamiri.memorybench.Message;
import io.github.mekkiamiri.memorybench.scenario.Sonde;

import java.util.List;

/** Note une réponse et le contexte qui l'a produite. */
public final class Noteur {

    private Noteur() {
    }

    public static Verdict verdict(Sonde sonde, String reponse) {
        boolean perime = sonde.perimees().stream().anyMatch(reponse::contains);
        if (perime) {
            return Verdict.PERIME;
        }
        if (sonde.surUnOubli()) {
            return Verdict.CORRECT;
        }
        return reponse.contains(sonde.attendu()) ? Verdict.CORRECT : Verdict.MANQUE;
    }

    /** Le contexte envoyé contient-il la bonne valeur ? (faux pour un fait oublié) */
    public static boolean attenduDansContexte(Sonde sonde, List<Message> prompt) {
        return !sonde.surUnOubli() && Contexte.texte(prompt).contains(sonde.attendu());
    }

    /** Le contexte envoyé contient-il une valeur périmée ou oubliée ? */
    public static boolean perimeDansContexte(Sonde sonde, List<Message> prompt) {
        String texte = Contexte.texte(prompt);
        return sonde.perimees().stream().anyMatch(texte::contains);
    }
}
