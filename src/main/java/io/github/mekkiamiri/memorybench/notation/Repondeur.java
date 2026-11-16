package io.github.mekkiamiri.memorybench.notation;

import io.github.mekkiamiri.memorybench.Message;
import io.github.mekkiamiri.memorybench.scenario.Sonde;

import java.util.List;

/** Le modèle qui répond aux questions de rappel. En CI : {@link LecteurNaif}, jamais un vrai modèle. */
@FunctionalInterface
public interface Repondeur {

    /**
     * @param prompt historique choisi par la stratégie, suivi de la question
     * @param sonde  la question de rappel (seul le faux modèle s'en sert, pour connaître les valeurs candidates)
     */
    String repondre(List<Message> prompt, Sonde sonde);
}
