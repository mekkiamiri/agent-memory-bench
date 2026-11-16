package io.github.mekkiamiri.memorybench.scenario;

import java.util.List;

/** Une conversation scénarisée longue, avec ses faits et ses questions de rappel. */
public record Scenario(String id, String titre, String source, List<Tour> tours) {

    public Scenario {
        tours = List.copyOf(tours);
    }

    public List<Tour> questions() {
        return tours.stream().filter(Tour::estQuestion).toList();
    }
}
