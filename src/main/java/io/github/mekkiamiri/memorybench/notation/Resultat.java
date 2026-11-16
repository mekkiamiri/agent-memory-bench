package io.github.mekkiamiri.memorybench.notation;

import java.util.List;

/** Résultat d'une stratégie sur un scénario. */
public record Resultat(String scenario, String strategie, List<MesureTour> tours) {

    public Resultat {
        tours = List.copyOf(tours);
    }

    public List<MesureTour> questions() {
        return tours.stream().filter(t -> t.verdict() != null).toList();
    }

    public long compte(Verdict v) {
        return questions().stream().map(MesureTour::verdict).filter(v::equals).count();
    }

    public long perimesDansContexte() {
        return questions().stream().filter(MesureTour::perimeDansContexte).count();
    }

    public int caracteresMax() {
        return tours.stream().mapToInt(MesureTour::caracteres).max().orElse(0);
    }

    public double caracteresMoyens() {
        return tours.stream().mapToInt(MesureTour::caracteres).average().orElse(0);
    }

    public int tokensTotal() {
        return tours.stream().mapToInt(MesureTour::tokensEstimes).sum();
    }

    public MesureTour tour(int numero) {
        return tours.stream().filter(t -> t.tour() == numero).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("tour " + numero));
    }
}
