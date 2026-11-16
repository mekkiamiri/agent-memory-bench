package io.github.mekkiamiri.memorybench.notation;

import io.github.mekkiamiri.memorybench.Contexte;
import io.github.mekkiamiri.memorybench.Message;
import io.github.mekkiamiri.memorybench.scenario.Sonde;

import java.util.List;

/**
 * Faux modèle déterministe : parmi les valeurs connues du fait interrogé, il répond celle
 * qui apparaît en dernier dans le prompt. Il ne sait pas « oublier sur demande » : s'il voit
 * une valeur oubliée, il la ressort. Il vérifie la plomberie du banc ; il ne prédit pas
 * ce que ferait un vrai modèle.
 */
public final class LecteurNaif implements Repondeur {

    public static final String NE_SAIT_PAS = "Je ne sais pas.";

    @Override
    public String repondre(List<Message> prompt, Sonde sonde) {
        String texte = Contexte.texte(prompt.subList(0, Math.max(0, prompt.size() - 1)));
        String meilleure = null;
        int position = -1;
        for (String valeur : sonde.candidats()) {
            int p = Noteur.position(texte, valeur);
            if (p > position) {
                position = p;
                meilleure = valeur;
            }
        }
        return meilleure == null ? NE_SAIT_PAS : "C'est " + meilleure + ".";
    }
}
