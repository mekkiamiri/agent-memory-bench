package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.session.CreateSessionRequest;
import org.springframework.ai.session.DefaultSessionService;
import org.springframework.ai.session.InMemorySessionRepository;
import org.springframework.ai.session.SessionService;
import org.springframework.ai.session.compaction.CompactionStrategy;
import org.springframework.ai.session.compaction.TurnCountTrigger;
import org.springframework.ai.session.compaction.WindowCompactionStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Le journal d'événements de Spring AI 2.1 (session), branché sur le même banc.
 *
 * <p>Utilise les vraies classes de {@code spring-ai-session} 2.1.0-RC2 : {@link DefaultSessionService},
 * {@link InMemorySessionRepository}, {@link TurnCountTrigger}, {@link WindowCompactionStrategy}.
 * Sans résumé ({@code ChatModelSummarizer} demanderait un vrai modèle) et sans faits structurés :
 * seuls les messages sont ajoutés à la session.
 */
public final class SessionSpringAi implements StrategieMemoire {

    private static final String ID = "banc";

    private final SessionService sessions;
    private final TurnCountTrigger declencheur;
    private final CompactionStrategy compaction;

    public SessionSpringAi(int declencheurTours, int toursGardes) {
        this.sessions = DefaultSessionService.builder()
                .sessionRepository(InMemorySessionRepository.builder().build())
                .build();
        this.sessions.create(CreateSessionRequest.builder().id(ID).build());
        this.declencheur = new TurnCountTrigger(declencheurTours);
        this.compaction = WindowCompactionStrategy.builder().lastTurns(toursGardes).build();
    }

    @Override
    public String nom() {
        return "session-spring-ai";
    }

    @Override
    public void enregistrer(Message message) {
        switch (message.role()) {
            case UTILISATEUR -> sessions.appendMessage(ID, new UserMessage(message.texte()));
            case ASSISTANT -> {
                sessions.appendMessage(ID, new AssistantMessage(message.texte()));
                sessions.compact(ID, declencheur, compaction);
            }
            case SYSTEME -> throw new IllegalArgumentException("le banc n'ajoute pas de message système");
        }
    }

    @Override
    public List<Message> contexte(String requete) {
        List<Message> vue = new ArrayList<>();
        for (org.springframework.ai.chat.messages.Message m : sessions.getActiveMessages(ID)) {
            String texte = m.getText() == null ? "" : m.getText();
            vue.add(switch (m.getMessageType()) {
                case USER -> Message.utilisateur(texte, 0, List.of());
                case ASSISTANT -> Message.assistant(texte, 0);
                default -> Message.systeme(texte);
            });
        }
        return vue;
    }

    /** Nombre d'événements dans la session (le journal ne fait que grossir). */
    public int nombreEvenements() {
        return sessions.getEvents(ID).size();
    }
}
