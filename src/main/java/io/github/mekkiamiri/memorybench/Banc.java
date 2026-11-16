package io.github.mekkiamiri.memorybench;

import io.github.mekkiamiri.memorybench.notation.LecteurNaif;
import io.github.mekkiamiri.memorybench.notation.MesureTour;
import io.github.mekkiamiri.memorybench.notation.Noteur;
import io.github.mekkiamiri.memorybench.notation.Repondeur;
import io.github.mekkiamiri.memorybench.notation.Resultat;
import io.github.mekkiamiri.memorybench.notation.Verdict;
import io.github.mekkiamiri.memorybench.scenario.LecteurScenario;
import io.github.mekkiamiri.memorybench.scenario.Scenario;
import io.github.mekkiamiri.memorybench.scenario.Tour;
import io.github.mekkiamiri.memorybench.strategie.StrategieMemoire;
import io.github.mekkiamiri.memorybench.strategie.Strategies;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/** Rejoue un scénario tour par tour avec une stratégie, et mesure chaque tour. */
public final class Banc {

    /**
     * Réponse enregistrée dans la mémoire après une question de rappel, à la place de la réponse
     * du modèle : une réponse fausse ne doit pas contaminer les tours suivants, et toutes les
     * stratégies doivent voir le même historique.
     */
    public static final String REPONSE_QUESTION = "Je vérifie et je reviens vers vous.";

    private Banc() {
    }

    public static Resultat jouer(Scenario scenario, StrategieMemoire strategie, Repondeur repondeur) {
        List<MesureTour> mesures = new ArrayList<>();
        for (Tour tour : scenario.tours()) {
            Message question = Message.utilisateur(tour.utilisateur(), tour.numero(), tour.faits());
            List<Message> prompt = new ArrayList<>(strategie.contexte(tour.utilisateur()));
            prompt.add(question);
            String reponse;
            Verdict verdict = null;
            boolean attendu = false;
            boolean perime = false;
            if (tour.estQuestion()) {
                reponse = repondeur.repondre(prompt, tour.sonde());
                verdict = Noteur.verdict(tour.sonde(), reponse);
                attendu = Noteur.attenduDansContexte(tour.sonde(), prompt);
                perime = Noteur.perimeDansContexte(tour.sonde(), prompt);
            } else {
                reponse = tour.reponse();
            }
            mesures.add(new MesureTour(tour.numero(), prompt.size(), Contexte.caracteres(prompt),
                    Contexte.tokensEstimes(prompt), verdict, attendu, perime, reponse));
            strategie.enregistrer(question);
            String enregistree = tour.estQuestion() ? REPONSE_QUESTION : reponse;
            strategie.enregistrer(Message.assistant(enregistree, tour.numero()));
        }
        return new Resultat(scenario.id(), strategie.nom(), mesures);
    }

    /** Rapport Markdown de toutes les stratégies sur tous les scénarios, avec le faux modèle. */
    public static String rapport() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Rapport du banc (faux modèle déterministe)\n\n")
                .append("> Ces chiffres viennent du LecteurNaif et du ResumeurCumulatif : ils vérifient la plomberie\n")
                .append("> du banc, ils ne mesurent pas un vrai modèle. Voir PROTOCOLE.md.\n\n");
        for (Scenario s : LecteurScenario.tous()) {
            sb.append("## ").append(s.titre()).append(" (").append(s.tours().size()).append(" tours, ")
                    .append(s.questions().size()).append(" questions)\n\n")
                    .append("| Stratégie | Correct | Périmé | Manqué | Périmé dans le contexte | Caractères max | Caractères moyens | Tokens estimés (total) |\n")
                    .append("|---|---|---|---|---|---|---|---|\n");
            for (Map.Entry<String, Supplier<StrategieMemoire>> e : Strategies.toutes().entrySet()) {
                Resultat r = jouer(s, e.getValue().get(), new LecteurNaif());
                sb.append(String.format(Locale.ROOT, "| %s | %d | %d | %d | %d | %d | %.0f | %d |%n", e.getKey(),
                        r.compte(Verdict.CORRECT), r.compte(Verdict.PERIME), r.compte(Verdict.MANQUE),
                        r.perimesDansContexte(), r.caracteresMax(), r.caracteresMoyens(), r.tokensTotal()));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Trace de référence pour la page : pour chaque stratégie maison, la taille du prompt (caractères)
     * et le verdict à chaque tour. La page la compare à sa propre simulation en JavaScript.
     */
    public static String trace(Scenario scenario) {
        StringBuilder sb = new StringBuilder("{\"scenario\":\"").append(scenario.id()).append("\",\"strategies\":{");
        boolean premiere = true;
        for (Map.Entry<String, Supplier<StrategieMemoire>> e : Strategies.maison().entrySet()) {
            Resultat r = jouer(scenario, e.getValue().get(), new LecteurNaif());
            sb.append(premiere ? "" : ",").append("\n\"").append(e.getKey()).append("\":[");
            premiere = false;
            for (int i = 0; i < r.tours().size(); i++) {
                MesureTour t = r.tours().get(i);
                sb.append(i == 0 ? "" : ",").append('[').append(t.caracteres()).append(',')
                        .append(t.verdict() == null ? "null" : "\"" + t.verdict() + "\"").append(']');
            }
            sb.append(']');
        }
        return sb.append("\n}}").toString();
    }

    /**
     * {@code mvn -q compile exec:java} : imprime le rapport.
     * {@code -Dexec.args="--trace demenagement docs/trace.json"} : écrit la trace de référence.
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 3 && args[0].equals("--trace")) {
            Files.writeString(Path.of(args[2]), trace(LecteurScenario.charger(args[1])) + "\n", StandardCharsets.UTF_8);
            System.out.println("écrit " + args[2]);
            return;
        }
        System.out.print(rapport());
    }
}
