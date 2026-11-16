package io.github.mekkiamiri.memorybench;

import io.github.mekkiamiri.memorybench.scenario.LecteurScenario;
import io.github.mekkiamiri.memorybench.scenario.Scenario;
import io.github.mekkiamiri.memorybench.scenario.Tour;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioTest {

    static List<Scenario> scenarios() {
        return LecteurScenario.tous();
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void conversationLongueDe30a80Tours(Scenario s) {
        assertTrue(s.tours().size() >= 30 && s.tours().size() <= 80, s.id() + " : " + s.tours().size());
        for (int i = 0; i < s.tours().size(); i++) {
            assertEquals(i + 1, s.tours().get(i).numero());
        }
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void contientUnFaitQuiChangeEtUnFaitOublie(Scenario s) {
        assertTrue(s.questions().stream().anyMatch(t -> !t.sonde().surUnOubli() && !t.sonde().perimees().isEmpty()),
                "il faut une question sur un fait qui a changé");
        assertTrue(s.questions().stream().anyMatch(t -> t.sonde().surUnOubli()),
                "il faut une question sur un fait oublié volontairement");
        assertTrue(s.questions().size() >= 6);
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void aucuneValeurDeFaitDansLeBavardage(Scenario s) {
        List<String> valeurs = s.questions().stream().flatMap(t -> t.sonde().candidats().stream()).distinct().toList();
        for (Tour t : s.tours()) {
            if (t.bavardage()) {
                for (String v : valeurs) {
                    assertFalse(t.utilisateur().contains(v) || t.reponse().contains(v), v + " dans le tour " + t.numero());
                }
            }
        }
    }

    @Test
    void lAttenduEcritEstVerifieContreLEtatCalcule() {
        String source = """
                U: Je pars à Lyon. {set ville=Lyon}
                A: Noté.
                U: Finalement Grenoble. {set ville=Grenoble}
                A: Noté.
                ? ville | Où ? | Lyon
                """;
        assertThrows(IllegalArgumentException.class, () -> LecteurScenario.lire("t", source));
    }

    @Test
    void unOubliAttendLeSymboleVide() {
        Scenario s = LecteurScenario.lire("t", """
                U: Mon code est 4242. {set code=4242}
                A: Noté.
                U: Oublie-le. {forget code}
                A: C'est fait.
                ? code | Mon code ? | ∅
                """);
        Tour q = s.questions().get(0);
        assertTrue(q.sonde().surUnOubli());
        assertEquals(List.of("4242"), q.sonde().perimees());
        assertEquals("Mon code est 4242.", s.tours().get(0).utilisateur());
    }

    @Test
    void laValeurDoitFigurerDansLeMessage() {
        assertThrows(IllegalArgumentException.class, () -> LecteurScenario.lire("t", """
                U: Je pars bientôt. {set ville=Lyon}
                A: Noté.
                """));
    }
}
