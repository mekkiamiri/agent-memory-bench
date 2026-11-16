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
        assertEquals(Verdict.PERIME, Noteur.verdict(ville, "Grenoble, ou Lyon ?"));
        assertEquals(Verdict.MANQUE, Noteur.verdict(ville, "Je ne sais pas."));
        assertEquals(Verdict.CORRECT, Noteur.verdict(code, "Je ne l'ai plus."));
        assertEquals(Verdict.PERIME, Noteur.verdict(code, "C'est 4242."));
    }

    @Test
    void contexte() {
        List<Message> prompt = List.of(Message.utilisateur("Je pars à Lyon.", 1, List.of()),
                Message.utilisateur("Finalement Grenoble.", 2, List.of()));
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
