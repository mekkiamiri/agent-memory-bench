package io.github.mekkiamiri.memorybench.notation;

import io.github.mekkiamiri.memorybench.Contexte;
import io.github.mekkiamiri.memorybench.Message;
import io.github.mekkiamiri.memorybench.scenario.Sonde;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Note une réponse et le contexte qui l'a produite. Une valeur n'est reconnue que comme mot
 * entier : « Java 2 » ne correspond pas à « Java 21 ».
 */
public final class Noteur {

    private Noteur() {
    }

    /**
     * La bonne valeur citée : correct, même si l'ancienne est citée comme ancienne
     * (« passé de 1 200 euros à 1 450 euros »). Sinon une valeur périmée ou oubliée : périmé.
     * Sinon : manqué. Pour un fait oublié, toute valeur citée est une fuite.
     */
    public static Verdict verdict(Sonde sonde, String reponse) {
        String r = reponse == null ? "" : reponse;
        if (!sonde.surUnOubli() && contient(r, sonde.attendu())) {
            return Verdict.CORRECT;
        }
        if (sonde.perimees().stream().anyMatch(v -> contient(r, v))) {
            return Verdict.PERIME;
        }
        return sonde.surUnOubli() ? Verdict.CORRECT : Verdict.MANQUE;
    }

    /** L'historique envoyé (question courante exclue) contient-il la bonne valeur ? (faux pour un fait oublié) */
    public static boolean attenduDansContexte(Sonde sonde, List<Message> prompt) {
        return !sonde.surUnOubli() && contient(historique(prompt), sonde.attendu());
    }

    /** L'historique envoyé (question courante exclue) contient-il une valeur périmée ou oubliée ? */
    public static boolean perimeDansContexte(Sonde sonde, List<Message> prompt) {
        String texte = historique(prompt);
        return sonde.perimees().stream().anyMatch(v -> contient(texte, v));
    }

    /** Le texte contient-il la valeur comme mot entier (ni lettre ni chiffre collé de part et d'autre) ? */
    public static boolean contient(String texte, String valeur) {
        return position(texte, valeur) >= 0;
    }

    /** Position de la dernière occurrence de la valeur comme mot entier, ou -1. */
    public static int position(String texte, String valeur) {
        var m = Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(valeur) + "(?![\\p{L}\\p{N}])").matcher(texte);
        int p = -1;
        while (m.find()) {
            p = m.start();
        }
        return p;
    }

    private static String historique(List<Message> prompt) {
        return Contexte.texte(prompt.subList(0, Math.max(0, prompt.size() - 1)));
    }
}
