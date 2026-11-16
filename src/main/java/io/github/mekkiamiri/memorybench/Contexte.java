package io.github.mekkiamiri.memorybench;

import java.util.List;
import java.util.stream.Collectors;

/** Ce qui est envoyé au modèle à un tour donné, et sa taille. */
public final class Contexte {

    private Contexte() {
    }

    /** Texte du prompt : une ligne {@code role: texte} par message, séparées par un saut de ligne. */
    public static String texte(List<Message> messages) {
        return messages.stream().map(Message::ligne).collect(Collectors.joining("\n"));
    }

    public static int caracteres(List<Message> messages) {
        return texte(messages).length();
    }

    /** Estimation grossière et assumée : 1 token pour 4 caractères, arrondi au-dessus. */
    public static int tokensEstimes(List<Message> messages) {
        return (caracteres(messages) + 3) / 4;
    }
}
