package io.github.mekkiamiri.memorybench;

import java.util.List;

/**
 * Un message de la conversation.
 *
 * @param role     qui parle
 * @param texte    le texte envoyé au modèle
 * @param tour     numéro du tour (1 = premier message de l'utilisateur)
 * @param faits    faits annotés dans le scénario ({@code set} et {@code forget}) ;
 *                 ils simulent ce qu'un extracteur (outil « remember / forget » appelé par le modèle)
 *                 produirait. Les stratégies sont libres de les ignorer.
 */
public record Message(Role role, String texte, int tour, List<Fait> faits) {

    public enum Role {
        UTILISATEUR("user"), ASSISTANT("assistant"), SYSTEME("system");

        private final String libelle;

        Role(String libelle) {
            this.libelle = libelle;
        }

        public String libelle() {
            return libelle;
        }
    }

    public Message {
        faits = List.copyOf(faits);
    }

    public static Message utilisateur(String texte, int tour, List<Fait> faits) {
        return new Message(Role.UTILISATEUR, texte, tour, faits);
    }

    public static Message assistant(String texte, int tour) {
        return new Message(Role.ASSISTANT, texte, tour, List.of());
    }

    public static Message systeme(String texte) {
        return new Message(Role.SYSTEME, texte, 0, List.of());
    }

    /** Ligne telle qu'elle est comptée dans la taille du contexte : {@code role: texte}. */
    public String ligne() {
        return role.libelle() + ": " + texte;
    }
}
