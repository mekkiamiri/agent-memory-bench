package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Stratégie 2 : quand le tampon dépasse un seuil, les plus anciens messages sont résumés
 * et seuls les derniers sont gardés tels quels. Le résumé part en message système.
 * La coupe se fait toujours après une réponse de l'assistant (frontière de tour).
 */
public final class Resume implements StrategieMemoire {

    private final int seuilMessages;
    private final int messagesGardes;
    private final Resumeur resumeur;
    private final List<Message> tampon = new ArrayList<>();
    private String resume = "";

    public Resume(int seuilMessages, int messagesGardes, Resumeur resumeur) {
        if (messagesGardes < 2 || messagesGardes % 2 != 0) {
            throw new IllegalArgumentException("messagesGardes doit être pair et au moins 2 (des tours complets)");
        }
        if (messagesGardes >= seuilMessages) {
            throw new IllegalArgumentException("messagesGardes doit être inférieur à seuilMessages");
        }
        this.seuilMessages = seuilMessages;
        this.messagesGardes = messagesGardes;
        this.resumeur = resumeur;
    }

    @Override
    public String nom() {
        return "resume";
    }

    @Override
    public void enregistrer(Message message) {
        tampon.add(message);
        if (message.role() == Message.Role.ASSISTANT && tampon.size() > seuilMessages) {
            int coupe = tampon.size() - messagesGardes;
            List<Message> sortants = List.copyOf(tampon.subList(0, coupe));
            resume = resumeur.resumer(resume, sortants);
            tampon.subList(0, coupe).clear();
        }
    }

    @Override
    public List<Message> contexte(String requete) {
        List<Message> contexte = new ArrayList<>();
        if (!resume.isEmpty()) {
            contexte.add(Message.systeme("Résumé de la conversation : " + resume));
        }
        contexte.addAll(tampon);
        return contexte;
    }

    public String resumeCourant() {
        return resume;
    }
}
