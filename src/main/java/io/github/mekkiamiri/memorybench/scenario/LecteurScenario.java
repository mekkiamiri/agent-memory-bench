package io.github.mekkiamiri.memorybench.scenario;

import io.github.mekkiamiri.memorybench.Fait;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lit un scénario au format texte (voir {@code src/main/resources/scenarios/}).
 *
 * <pre>
 * # titre: Le déménagement
 * U: Je déménage à Lyon. {set ville=Lyon}     message de l'utilisateur, faits annotés
 * A: Noté, Lyon.                                réponse scénarisée de l'assistant
 * ~ 4                                           4 tours de bavardage sans fait
 * ? ville | Dans quelle ville je déménage ? | Lyon     question de rappel (∅ = fait oublié)
 * </pre>
 *
 * <p>La valeur attendue écrite dans le fichier est vérifiée contre l'état calculé des faits :
 * une incohérence fait échouer la lecture.
 */
public final class LecteurScenario {

    public static final String OUBLIE = "∅";
    private static final Pattern ANNOTATION = Pattern.compile("\\{(set|forget) ([^}=]+?)(?:=([^}]*))?\\}");
    public static final List<String> SCENARIOS = List.of("demenagement", "projet-java", "voyage");

    private LecteurScenario() {
    }

    public static Scenario charger(String id) {
        String chemin = "/scenarios/" + id + ".txt";
        try (InputStream in = LecteurScenario.class.getResourceAsStream(chemin)) {
            if (in == null) {
                throw new IllegalArgumentException("scénario introuvable : " + chemin);
            }
            return lire(id, new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static List<Scenario> tous() {
        return SCENARIOS.stream().map(LecteurScenario::charger).toList();
    }

    public static Scenario lire(String id, String source) {
        String titre = id;
        List<Tour> tours = new ArrayList<>();
        Map<String, List<String>> historique = new LinkedHashMap<>();
        Map<String, Boolean> oublie = new LinkedHashMap<>();
        int rangBavardage = 0;
        String[] lignes = source.split("\n", -1);
        for (int i = 0; i < lignes.length; i++) {
            String ligne = lignes[i].strip();
            int numero = tours.size() + 1;
            if (ligne.isEmpty()) {
                continue;
            }
            if (ligne.startsWith("#")) {
                if (ligne.startsWith("# titre:")) {
                    titre = ligne.substring("# titre:".length()).strip();
                }
            } else if (ligne.startsWith("U:")) {
                String brut = ligne.substring(2).strip();
                List<Fait> faits = new ArrayList<>();
                Matcher m = ANNOTATION.matcher(brut);
                while (m.find()) {
                    String cle = m.group(2).strip();
                    if (m.group(1).equals("set")) {
                        if (m.group(3) == null || m.group(3).isBlank()) {
                            throw erreur(id, i, "{set " + cle + "} sans valeur");
                        }
                        String valeur = m.group(3).strip();
                        if (historique.getOrDefault(cle, List.of()).contains(valeur)) {
                            throw erreur(id, i, "la valeur « " + valeur + " » a déjà été posée pour " + cle);
                        }
                        faits.add(Fait.pose(cle, valeur));
                        historique.computeIfAbsent(cle, k -> new ArrayList<>()).add(valeur);
                        oublie.put(cle, false);
                    } else {
                        faits.add(Fait.oubli(cle));
                        oublie.put(cle, true);
                    }
                }
                String texte = ANNOTATION.matcher(brut).replaceAll("").strip().replaceAll(" {2,}", " ");
                for (Fait f : faits) {
                    if (!f.estOubli() && !texte.contains(f.valeur())) {
                        throw erreur(id, i, "la valeur « " + f.valeur() + " » doit figurer dans le message");
                    }
                }
                if (i + 1 >= lignes.length || !lignes[i + 1].strip().startsWith("A:")) {
                    throw erreur(id, i, "une ligne U: doit être suivie d'une ligne A:");
                }
                String reponse = lignes[++i].strip().substring(2).strip();
                tours.add(new Tour(numero, texte, faits, reponse, null, false));
            } else if (ligne.startsWith("~")) {
                int n = Integer.parseInt(ligne.substring(1).strip());
                for (int k = 0; k < n; k++) {
                    String[] p = Bavardage.paire(rangBavardage++);
                    tours.add(new Tour(tours.size() + 1, p[0], List.of(), p[1], null, true));
                }
            } else if (ligne.startsWith("?")) {
                String[] parties = ligne.substring(1).split("\\|");
                if (parties.length != 3) {
                    throw erreur(id, i, "format attendu : ? cle | question | attendu");
                }
                String cle = parties[0].strip();
                String question = parties[1].strip();
                String ecrit = parties[2].strip();
                List<String> valeurs = historique.get(cle);
                if (valeurs == null) {
                    throw erreur(id, i, "question sur un fait jamais posé : " + cle);
                }
                boolean estOublie = oublie.get(cle);
                String attendu = estOublie ? null : valeurs.get(valeurs.size() - 1);
                List<String> perimees = estOublie ? valeurs : valeurs.subList(0, valeurs.size() - 1);
                String calcule = attendu == null ? OUBLIE : attendu;
                if (!calcule.equals(ecrit)) {
                    throw erreur(id, i, "attendu écrit « " + ecrit + " », état calculé « " + calcule + " »");
                }
                tours.add(new Tour(numero, question, List.of(), null, new Sonde(cle, attendu, perimees), false));
            } else {
                throw erreur(id, i, "ligne non reconnue : " + ligne);
            }
        }
        return new Scenario(id, titre, source, tours);
    }

    private static IllegalArgumentException erreur(String id, int ligne, String message) {
        return new IllegalArgumentException(id + ", ligne " + (ligne + 1) + " : " + message);
    }
}
