package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Stratégie 4 : récupération sur l'historique. Les derniers messages partent toujours ;
 * on y ajoute les anciens messages les plus proches de la question.
 *
 * <p>La proximité est lexicale (mots communs), pour rester déterministe et sans modèle
 * d'embedding en CI. Remplacer {@link #score} par une similarité d'embeddings (un
 * {@code VectorStore} de Spring AI par exemple) est l'étape suivante.
 */
public final class Recuperation implements StrategieMemoire {

    static final Set<String> MOTS_VIDES = Set.of(
            "pour", "dans", "avec", "mais", "plus", "tout", "bien", "fait", "faire", "cette", "sont",
            "nous", "vous", "elle", "quoi", "quel", "quelle", "quels", "quelles", "comme", "alors",
            "donc", "aussi", "encore", "juste", "très", "moins", "peux", "veux");

    private final int messagesRecents;
    private final int nombreRecuperes;
    private final List<Message> messages = new ArrayList<>();

    public Recuperation(int messagesRecents, int nombreRecuperes) {
        this.messagesRecents = messagesRecents;
        this.nombreRecuperes = nombreRecuperes;
    }

    @Override
    public String nom() {
        return "recuperation";
    }

    @Override
    public void enregistrer(Message message) {
        messages.add(message);
    }

    @Override
    public List<Message> contexte(String requete) {
        int debutRecents = Math.max(0, messages.size() - messagesRecents);
        Set<String> motsRequete = mots(requete);
        record Candidat(int position, int score) {
        }
        List<Candidat> candidats = new ArrayList<>();
        for (int i = 0; i < debutRecents; i++) {
            int s = score(motsRequete, messages.get(i).texte());
            if (s > 0) {
                candidats.add(new Candidat(i, s));
            }
        }
        candidats.sort(Comparator.comparingInt(Candidat::score).reversed()
                .thenComparing(Comparator.comparingInt(Candidat::position).reversed()));
        List<Integer> retenus = new ArrayList<>();
        for (int i = 0; i < Math.min(nombreRecuperes, candidats.size()); i++) {
            retenus.add(candidats.get(i).position());
        }
        retenus.sort(Comparator.naturalOrder());
        List<Message> contexte = new ArrayList<>();
        for (int p : retenus) {
            contexte.add(messages.get(p));
        }
        contexte.addAll(messages.subList(debutRecents, messages.size()));
        return contexte;
    }

    /** Nombre de mots distincts de la requête présents dans le texte. */
    static int score(Set<String> motsRequete, String texte) {
        Set<String> motsTexte = mots(texte);
        int n = 0;
        for (String m : motsRequete) {
            if (motsTexte.contains(m)) {
                n++;
            }
        }
        return n;
    }

    /** Mots de 4 lettres ou plus, en minuscules, hors mots vides. */
    static Set<String> mots(String texte) {
        Set<String> mots = new HashSet<>();
        for (String m : texte.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (m.length() >= 4 && !MOTS_VIDES.contains(m)) {
                mots.add(m);
            }
        }
        return mots;
    }
}
