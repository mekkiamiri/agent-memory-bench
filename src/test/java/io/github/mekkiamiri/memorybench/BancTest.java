package io.github.mekkiamiri.memorybench;

import io.github.mekkiamiri.memorybench.notation.LecteurNaif;
import io.github.mekkiamiri.memorybench.notation.MesureTour;
import io.github.mekkiamiri.memorybench.notation.Resultat;
import io.github.mekkiamiri.memorybench.notation.Verdict;
import io.github.mekkiamiri.memorybench.scenario.LecteurScenario;
import io.github.mekkiamiri.memorybench.scenario.Scenario;
import io.github.mekkiamiri.memorybench.strategie.HistoriqueComplet;
import io.github.mekkiamiri.memorybench.strategie.Strategies;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Propriétés de plomberie du banc, avec le faux modèle. Aucun de ces tests ne prétend
 * classer les stratégies : ce classement demande un vrai modèle (PROTOCOLE.md).
 */
class BancTest {

    @Test
    void chaqueStrategieJoueChaqueScenarioEtNoteChaqueQuestion() {
        for (Scenario s : LecteurScenario.tous()) {
            Strategies.toutes().forEach((nom, fabrique) -> {
                Resultat r = Banc.jouer(s, fabrique.get(), new LecteurNaif());
                assertEquals(s.tours().size(), r.tours().size());
                assertEquals(s.questions().size(), r.questions().size(), nom);
                long total = r.compte(Verdict.CORRECT) + r.compte(Verdict.PERIME) + r.compte(Verdict.MANQUE);
                assertEquals(s.questions().size(), total);
            });
        }
    }

    @Test
    void lHistoriqueCompletGrossitAChaqueTourEtNePerdRien() {
        for (Scenario s : LecteurScenario.tous()) {
            Resultat r = Banc.jouer(s, new HistoriqueComplet(), new LecteurNaif());
            List<MesureTour> t = r.tours();
            for (int i = 1; i < t.size(); i++) {
                assertTrue(t.get(i).caracteres() > t.get(i - 1).caracteres());
            }
            assertEquals(0, r.compte(Verdict.MANQUE), s.id());
            r.questions().stream().filter(q -> q.verdict() != null)
                    .forEach(q -> assertTrue(q.attenduDansContexte() || q.perimeDansContexte()));
        }
    }

    @Test
    void lesStrategiesBorneesRestentSousLHistoriqueCompletEnFinDeConversation() {
        for (Scenario s : LecteurScenario.tous()) {
            int reference = Banc.jouer(s, new HistoriqueComplet(), new LecteurNaif()).caracteresMax();
            Strategies.toutes().forEach((nom, fabrique) -> {
                if (!nom.equals("historique-complet")) {
                    int max = Banc.jouer(s, fabrique.get(), new LecteurNaif()).caracteresMax();
                    assertTrue(max < reference, nom + " sur " + s.id() + " : " + max + " >= " + reference);
                }
            });
        }
    }

    @Test
    void laReponseDuModeleNeContaminePasLesToursSuivants() {
        Scenario s = LecteurScenario.charger("demenagement");
        Strategies.toutes().forEach((nom, fabrique) -> {
            var strategie = fabrique.get();
            Banc.jouer(s, strategie, (prompt, sonde) -> "POISON-" + sonde.cle());
            assertTrue(!Contexte.texte(strategie.contexte("POISON budget ville")).contains("POISON"), nom);
        });
    }

    @Test
    void leBancEstDeterministe() {
        Scenario s = LecteurScenario.charger("voyage");
        Strategies.toutes().forEach((nom, fabrique) ->
                assertEquals(Banc.jouer(s, fabrique.get(), new LecteurNaif()),
                        Banc.jouer(s, fabrique.get(), new LecteurNaif()), nom));
    }

    @Test
    void leRapportAfficheSonAvertissement() {
        String rapport = Banc.rapport();
        assertTrue(rapport.contains("ne mesurent pas un vrai modèle"));
        assertTrue(rapport.contains("| session-spring-ai |"));
    }
}
