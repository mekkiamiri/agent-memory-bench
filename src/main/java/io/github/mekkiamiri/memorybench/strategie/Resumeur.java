package io.github.mekkiamiri.memorybench.strategie;

import io.github.mekkiamiri.memorybench.Message;

import java.util.List;

/** Produit un nouveau résumé à partir du résumé précédent et des messages qui sortent du contexte. */
@FunctionalInterface
public interface Resumeur {

    String resumer(String resumePrecedent, List<Message> sortants);
}
