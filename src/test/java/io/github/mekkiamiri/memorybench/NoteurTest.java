package io.github.mekkiamiri.memorybench;

import io.github.mekkiamiri.memorybench.notation.LecteurNaif;
import io.github.mekkiamiri.memorybench.notation.Noteur;
import io.github.mekkiamiri.memorybench.notation.Verdict;
import io.github.mekkiamiri.memorybench.scenario.Sonde;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteurTest {

    private final Sonde ville = new Sonde("ville", "Grenoble", List.of("Lyon"));
    private final Sonde code = new Sonde("code", null, List.of("4242"));

    @Test
    void verdicts() {
        assertEquals(Verdict.CORRECT, Noteur.verdict(ville, "C'est Grenoble."));
        assertEquals(Verdict.PERIME, Noteur.verdict(ville, "C'est Lyon."));
        assertEquals(Verdict.CORRECT, Noteur.verdict(ville, "Avant Lyon, maintenant Grenoble."));
        assertEquals(Verdict.MANQUE, Noteur.verdict(ville, null));
        assertEquals(Verdict.CORRECT, Noteur.verdict(code, null));
        assertEquals(Verdict.MANQUE, Noteur.verdict(ville, "Je ne sais pas."));
        assertEquals(Verdict.CORRECT, Noteur.verdict(code, "Je ne l'ai plus."));
        assertEquals(Verdict.PERIME, Noteur.verdict(code, "C'est 4242."));
    }

    @Test
    void uneValeurNeCorrespondQuAUnMotEntier() {
        Sonde java = new Sonde("java", "Java 25", List.of("Java 2"));
        assertEquals(Verdict.MANQUE, Noteur.verdict(java, "On cible Java 21."));
        assertEquals(Verdict.PERIME, Noteur.verdict(java, "On cible Java 2."));
        Sonde budget = new Sonde("budget", "1 450 euros", List.of("1 200 euros"));
        assertEquals(Verdict.CORRECT, Noteur.verdict(budget, "Il est passé de 1 200 euros à 1 450 euros."));
        assertFalse(Noteur.contient("11 200 euros", "1 200 euros"));
    }

    @Test
    void laQuestionCouranteNeCompteParDansLeContexte() {
        List<Message> prompt = List.of(Message.utilisateur("Bonjour.", 1, List.of()),
                Message.utilisateur("C'est Grenoble ou Lyon ?", 2, List.of()));
        assertFalse(Noteur.attenduDansContexte(ville, prompt));
        assertFalse(Noteur.perimeDansContexte(ville, prompt));
    }

    @Test
    void contexte() {
        List<Message> prompt = List.of(Message.utilisateur("Je pars à Lyon.", 1, List.of()),
                Message.utilisateur("Finalement Grenoble.", 2, List.of()),
                Message.utilisateur("Où ?", 3, List.of()));
        assertTrue(Noteur.attenduDansContexte(ville, prompt));
        assertTrue(Noteur.perimeDansContexte(ville, prompt));
        assertFalse(Noteur.attenduDansContexte(code, prompt));
    }

    @Test
    void leLecteurNaifPrendLaDerniereValeurVueEtIgnoreLaQuestion() {
        LecteurNaif lecteur = new LecteurNaif();
        List<Message> prompt = List.of(Message.utilisateur("Je pars à Lyon.", 1, List.of()),
                Message.utilisateur("Finalement Grenoble.", 2, List.of()),
                Message.utilisateur("Lyon ?", 3, List.of()));
        assertEquals("C'est Grenoble.", lecteur.repondre(prompt, ville));
        assertEquals(LecteurNaif.NE_SAIT_PAS, lecteur.repondre(List.of(Message.utilisateur("?", 1, List.of())), ville));
    }

    @Test
    void tailleDuContexte() {
        List<Message> prompt = List.of(Message.utilisateur("abcd", 1, List.of()), Message.assistant("ef", 1));
        assertEquals("user: abcd\nassistant: ef".length(), Contexte.caracteres(prompt));
        assertEquals(6, Contexte.tokensEstimes(prompt)); // 24 caractères
    }
}
