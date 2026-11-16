package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Faux résumeur déterministe, pour les tests et la CI (aucun appel de modèle).
 *
 * <p>Il garde le texte des messages de l'utilisateur porteurs d'un fait annoté, un par ligne,
 * sans résoudre les contradictions, et retire les plus anciens quand le budget de caractères
 * est dépassé. C'est volontairement le comportement d'un résumé « qui accumule » : un vrai
 * modèle peut faire mieux (ou pire), et c'est à mesurer. Le séparateur est le saut de ligne :
 * un message du scénario n'en contient jamais.
 */
public final class ResumeurCumulatif implements Resumeur {

    static final String SEPARATEUR = "\n";

    private final int budgetCaracteres;

    public ResumeurCumulatif(int budgetCaracteres) {
        if (budgetCaracteres < 1) {
            throw new IllegalArgumentException("budgetCaracteres doit être positif");
        }
        this.budgetCaracteres = budgetCaracteres;
    }

    @Override
    public String resumer(String resumePrecedent, List<Message> sortants) {
        List<String> entrees = new ArrayList<>();
        if (!resumePrecedent.isEmpty()) {
            entrees.addAll(Arrays.asList(resumePrecedent.split(SEPARATEUR)));
        }
        for (Message m : sortants) {
            if (m.role() == Message.Role.UTILISATEUR && !m.faits().isEmpty()) {
                entrees.add(m.texte());
            }
        }
        while (entrees.size() > 1 && String.join(SEPARATEUR, entrees).length() > budgetCaracteres) {
            entrees.remove(0);
        }
        return String.join(SEPARATEUR, entrees);
    }
}
