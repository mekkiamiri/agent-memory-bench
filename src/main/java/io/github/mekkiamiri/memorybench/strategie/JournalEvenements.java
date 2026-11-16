package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Fait;
import io.github.mekkiamiri.memorybench.Message;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stratégie 3 : journal d'événements avec compaction (implémentation maison, même principe
 * que la session de Spring AI 2.1, voir {@link SessionSpringAi}).
 *
 * <ul>
 *   <li>Tout est ajouté au journal : messages, faits posés, faits oubliés.</li>
 *   <li>Quand le nombre de tours visibles dépasse {@code declencheurTours}, une {@link Evenement.Frontiere}
 *       est ajoutée : la vue ne garde que les {@code toursGardes} derniers tours. Le journal n'est pas réécrit.</li>
 *   <li>La vue envoyée au modèle = l'état courant des faits (rejoué depuis tout le journal, comme une
 *       projection en event sourcing) + les messages visibles, moins ceux qui citent une valeur oubliée.</li>
 * </ul>
 *
 * <p>Avantage assumé : le journal exploite les faits annotés (ils simulent un outil
 * « remember / forget » appelé par le modèle). Le coût et les erreurs d'un vrai extracteur sont à mesurer.
 */
public final class JournalEvenements implements StrategieMemoire {

    private final int declencheurTours;
    private final int toursGardes;
    private final List<Evenement> journal = new ArrayList<>();
    private final List<Message> messages = new ArrayList<>();
    private int premierVisible = 0;

    public JournalEvenements(int declencheurTours, int toursGardes) {
        if (toursGardes < 1 || toursGardes >= declencheurTours) {
            throw new IllegalArgumentException("il faut 1 <= toursGardes < declencheurTours");
        }
        this.declencheurTours = declencheurTours;
        this.toursGardes = toursGardes;
    }

    @Override
    public String nom() {
        return "journal";
    }

    @Override
    public void enregistrer(Message message) {
        journal.add(new Evenement.MessageAjoute(message));
        messages.add(message);
        for (Fait f : message.faits()) {
            journal.add(f.estOubli() ? new Evenement.FaitOublie(f.cle()) : new Evenement.FaitPose(f.cle(), f.valeur()));
        }
        if (message.role() == Message.Role.ASSISTANT && toursVisibles() > declencheurTours) {
            premierVisible = debutDesDerniersTours(toursGardes);
            journal.add(new Evenement.Frontiere(premierVisible));
        }
    }

    @Override
    public List<Message> contexte(String requete) {
        Map<String, String> etat = etat();
        Set<String> oubliees = valeursOubliees();
        List<Message> vue = new ArrayList<>();
        if (!etat.isEmpty()) {
            String faits = etat.entrySet().stream()
                    .map(e -> e.getKey() + " = " + e.getValue())
                    .collect(Collectors.joining(" ; "));
            vue.add(Message.systeme("Faits connus : " + faits));
        }
        for (Message m : messages.subList(premierVisible, messages.size())) {
            if (oubliees.stream().noneMatch(m.texte()::contains)) {
                vue.add(m);
            }
        }
        return vue;
    }

    /** Le journal complet, en lecture seule. */
    public List<Evenement> evenements() {
        return List.copyOf(journal);
    }

    /** Projection : état courant des faits, dans l'ordre de leur dernière mise à jour. */
    public Map<String, String> etat() {
        Map<String, String> etat = new LinkedHashMap<>();
        for (Evenement e : journal) {
            if (e instanceof Evenement.FaitPose p) {
                etat.remove(p.cle());
                etat.put(p.cle(), p.valeur());
            } else if (e instanceof Evenement.FaitOublie o) {
                etat.remove(o.cle());
            }
        }
        return etat;
    }

    /** Valeurs déjà posées pour les clés dont le dernier événement est un oubli. */
    Set<String> valeursOubliees() {
        Map<String, List<String>> valeurs = new LinkedHashMap<>();
        Set<String> oubliees = new HashSet<>();
        for (Evenement e : journal) {
            if (e instanceof Evenement.FaitPose p) {
                valeurs.computeIfAbsent(p.cle(), k -> new ArrayList<>()).add(p.valeur());
                oubliees.remove(p.cle());
            } else if (e instanceof Evenement.FaitOublie o) {
                oubliees.add(o.cle());
            }
        }
        Set<String> resultat = new HashSet<>();
        for (String cle : oubliees) {
            resultat.addAll(valeurs.getOrDefault(cle, List.of()));
        }
        return resultat;
    }

    private int toursVisibles() {
        int n = 0;
        for (int i = premierVisible; i < messages.size(); i++) {
            if (messages.get(i).role() == Message.Role.UTILISATEUR) {
                n++;
            }
        }
        return n;
    }

    /** Position du premier message des {@code tours} derniers tours (un tour commence par un message utilisateur). */
    private int debutDesDerniersTours(int tours) {
        int vus = 0;
        for (int i = messages.size() - 1; i >= premierVisible; i--) {
            if (messages.get(i).role() == Message.Role.UTILISATEUR && ++vus == tours) {
                return i;
            }
        }
        return premierVisible;
    }
}
