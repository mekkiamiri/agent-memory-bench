package io.github.mekkiamiri.memorybench;

/**
 * Un fait annoté dans le scénario : la valeur d'une clé est posée, ou la clé est à oublier.
 *
 * @param cle    nom du fait (ex. {@code budget})
 * @param valeur valeur posée, ou {@code null} pour un oubli
 */
public record Fait(String cle, String valeur) {

    public static Fait pose(String cle, String valeur) {
        return new Fait(cle, valeur);
    }

    public static Fait oubli(String cle) {
        return new Fait(cle, null);
    }

    public boolean estOubli() {
        return valeur == null;
    }
}
