package io.github.mekkiamiri.memorybench;

import io.github.mekkiamiri.memorybench.strategie.Evenement;
import io.github.mekkiamiri.memorybench.strategie.FenetreGlissante;
import io.github.mekkiamiri.memorybench.strategie.JournalEvenements;
import io.github.mekkiamiri.memorybench.strategie.Recuperation;
import io.github.mekkiamiri.memorybench.strategie.Resume;
import io.github.mekkiamiri.memorybench.strategie.ResumeurCumulatif;
import io.github.mekkiamiri.memorybench.strategie.SessionSpringAi;
import io.github.mekkiamiri.memorybench.strategie.StrategieMemoire;
import io.github.mekkiamiri.memorybench.strategie.Strategies;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategiesTest {

    /** Joue {@code n} tours de bavardage numérotés à partir de {@code debut}. */
    private static void bavarder(StrategieMemoire s, int debut, int n) {
        for (int i = debut; i < debut + n; i++) {
            s.enregistrer(Message.utilisateur("question " + i, i, List.of()));
            s.enregistrer(Message.assistant("réponse " + i, i));
        }
    }

    private static void tour(StrategieMemoire s, int n, String texte, Fait... faits) {
        s.enregistrer(Message.utilisateur(texte, n, List.of(faits)));
        s.enregistrer(Message.assistant("Noté.", n));
    }

    private static String texte(StrategieMemoire s, String requete) {
        return Contexte.texte(s.contexte(requete));
    }

    @Test
    void laFenetreNeGardeQueLesDerniersMessages() {
        FenetreGlissante f = new FenetreGlissante(4);
        bavarder(f, 1, 5);
        List<Message> c = f.contexte("");
        assertEquals(4, c.size());
        assertEquals("question 4", c.get(0).texte());
    }

    @Test
    void leResumeCumulatifGardeLesFaitsMaisAussiLesValeursPerimees() {
        Resume r = new Resume(6, 2, new ResumeurCumulatif(600));
        tour(r, 1, "Je pars à Lyon.", Fait.pose("ville", "Lyon"));
        tour(r, 2, "Finalement Grenoble.", Fait.pose("ville", "Grenoble"));
        bavarder(r, 3, 4);
        assertEquals("Je pars à Lyon. | Finalement Grenoble.", r.resumeCourant());
        assertEquals(Message.Role.SYSTEME, r.contexte("").get(0).role());
        assertTrue(r.contexte("").size() <= 1 + 6);
    }

    @Test
    void leResumeCumulatifRespecteSonBudget() {
        ResumeurCumulatif resumeur = new ResumeurCumulatif(30);
        String resume = resumeur.resumer("", List.of(
                Message.utilisateur("Premier fait assez long.", 1, List.of(Fait.pose("a", "long"))),
                Message.utilisateur("Second fait.", 2, List.of(Fait.pose("b", "Second")))));
        assertEquals("Second fait.", resume);
    }

    @Test
    void leJournalNeFaitQuAjouterEtSaVueSuitLEtatCourant() {
        JournalEvenements j = new JournalEvenements(4, 2);
        tour(j, 1, "Je pars à Lyon.", Fait.pose("ville", "Lyon"));
        tour(j, 2, "Mon code est 4242.", Fait.pose("code", "4242"));
        tour(j, 3, "Finalement Grenoble.", Fait.pose("ville", "Grenoble"));
        tour(j, 4, "Oublie mon code.", Fait.oubli("code"));
        int avant = j.evenements().size();
        bavarder(j, 5, 3);
        assertTrue(j.evenements().size() > avant, "le journal grossit");
        assertTrue(j.evenements().stream().anyMatch(e -> e instanceof Evenement.Frontiere), "une compaction a eu lieu");
        assertEquals(Map.of("ville", "Grenoble"), j.etat());
        String vue = texte(j, "");
        assertTrue(vue.startsWith("system: Faits connus : ville = Grenoble"));
        assertFalse(vue.contains("4242"));
        assertFalse(vue.contains("Lyon"));
    }

    @Test
    void leJournalCacheUnFaitOublieMemeAvantCompaction() {
        JournalEvenements j = new JournalEvenements(10, 3);
        tour(j, 1, "Mon code est 4242.", Fait.pose("code", "4242"));
        tour(j, 2, "Oublie mon code.", Fait.oubli("code"));
        assertFalse(texte(j, "").contains("4242"));
        assertTrue(j.evenements().stream().anyMatch(e -> e instanceof Evenement.MessageAjoute m
                && m.message().texte().contains("4242")), "le journal, lui, n'est pas réécrit");
    }

    @Test
    void laRecuperationRameneUnVieuxMessageProcheDeLaQuestion() {
        Recuperation r = new Recuperation(4, 2);
        tour(r, 1, "Le nouvel appartement est au troisième étage.", Fait.pose("etage", "troisième étage"));
        bavarder(r, 2, 10);
        assertTrue(texte(r, "À quel étage est l'appartement ?").contains("troisième étage"));
        assertFalse(texte(r, "Quelle couleur pour le salon ?").contains("troisième"));
    }

    @Test
    void laSessionSpringAiCompacteSurUneFrontiereDeTour() {
        SessionSpringAi s = new SessionSpringAi(4, 2);
        bavarder(s, 1, 12);
        List<Message> vue = s.contexte("");
        assertTrue(vue.size() <= 2 * 5, "la vue reste bornée : " + vue.size());
        assertEquals(Message.Role.UTILISATEUR, vue.get(0).role(), "la vue commence par un tour complet");
        assertEquals("réponse 12", vue.get(vue.size() - 1).texte());
        assertTrue(s.nombreEvenements() >= 24, "le journal garde tout : " + s.nombreEvenements());
    }

    @Test
    void toutesLesStrategiesSontInterchangeables() {
        Strategies.toutes().forEach((nom, fabrique) -> {
            StrategieMemoire s = fabrique.get();
            assertEquals(nom, s.nom());
            assertTrue(s.contexte("").isEmpty(), nom + " démarre vide");
            tour(s, 1, "Je pars à Lyon.", Fait.pose("ville", "Lyon"));
            assertTrue(texte(s, "Où je pars ?").contains("Lyon"), nom + " se souvient du dernier tour");
        });
    }
}
