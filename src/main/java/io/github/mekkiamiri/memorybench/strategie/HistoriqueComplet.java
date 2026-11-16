package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;

import java.util.ArrayList;
import java.util.List;

/** Référence : tout l'historique part à chaque tour. Sert d'étalon pour la taille du contexte. */
public final class HistoriqueComplet implements StrategieMemoire {

    private final List<Message> messages = new ArrayList<>();

    @Override
    public String nom() {
        return "historique-complet";
    }

    @Override
    public void enregistrer(Message message) {
        messages.add(message);
    }

    @Override
    public List<Message> contexte(String requete) {
        return List.copyOf(messages);
    }
}
