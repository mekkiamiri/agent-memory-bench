package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;

import java.util.ArrayList;
import java.util.List;

/** Stratégie 1 : seuls les N derniers messages partent (équivalent de MessageWindowChatMemory). */
public final class FenetreGlissante implements StrategieMemoire {

    private final int maxMessages;
    private final List<Message> messages = new ArrayList<>();

    public FenetreGlissante(int maxMessages) {
        if (maxMessages < 2 || maxMessages % 2 != 0) {
            throw new IllegalArgumentException("maxMessages doit être pair et au moins 2 (des tours complets)");
        }
        this.maxMessages = maxMessages;
    }

    @Override
    public String nom() {
        return "fenetre";
    }

    @Override
    public void enregistrer(Message message) {
        messages.add(message);
    }

    @Override
    public List<Message> contexte(String requete) {
        int debut = Math.max(0, messages.size() - maxMessages);
        return List.copyOf(messages.subList(debut, messages.size()));
    }
}
