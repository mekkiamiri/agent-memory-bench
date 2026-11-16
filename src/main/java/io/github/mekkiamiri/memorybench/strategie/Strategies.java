package io.github.mekkiamiri.memorybench.strategie;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Les stratégies du banc, avec leurs réglages par défaut. Les mêmes réglages sont repris
 * dans la simulation de la page ({@code docs/index.html}).
 */
public final class Strategies {

    /** Fenêtre : 12 derniers messages, soit 6 tours. */
    public static final int FENETRE_MESSAGES = 12;
    /** Résumé : on résume au-delà de 12 messages, on en garde 6, résumé plafonné à 600 caractères. */
    public static final int RESUME_SEUIL = 12;
    public static final int RESUME_GARDES = 6;
    public static final int RESUME_BUDGET = 600;
    /** Journal : compaction au-delà de 10 tours visibles, on garde les 3 derniers tours. */
    public static final int JOURNAL_DECLENCHEUR = 10;
    public static final int JOURNAL_GARDES = 3;
    /** Récupération : 6 derniers messages + 4 anciens messages proches de la question. */
    public static final int RECUP_RECENTS = 6;
    public static final int RECUP_NOMBRE = 4;

    private Strategies() {
    }

    /** Les quatre stratégies maison, plus la référence « historique complet ». */
    public static Map<String, Supplier<StrategieMemoire>> maison() {
        Map<String, Supplier<StrategieMemoire>> s = new LinkedHashMap<>();
        s.put("historique-complet", HistoriqueComplet::new);
        s.put("fenetre", () -> new FenetreGlissante(FENETRE_MESSAGES));
        s.put("resume", () -> new Resume(RESUME_SEUIL, RESUME_GARDES, new ResumeurCumulatif(RESUME_BUDGET)));
        s.put("journal", () -> new JournalEvenements(JOURNAL_DECLENCHEUR, JOURNAL_GARDES));
        s.put("recuperation", () -> new Recuperation(RECUP_RECENTS, RECUP_NOMBRE));
        return s;
    }

    /** Toutes les stratégies, y compris la session de Spring AI 2.1 (mêmes réglages que le journal). */
    public static Map<String, Supplier<StrategieMemoire>> toutes() {
        Map<String, Supplier<StrategieMemoire>> s = maison();
        s.put("session-spring-ai", () -> new SessionSpringAi(JOURNAL_DECLENCHEUR, JOURNAL_GARDES));
        return s;
    }
}
