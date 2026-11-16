package io.github.mekkiamiri.memorybench;

import io.github.mekkiamiri.memorybench.scenario.LecteurScenario;
import io.github.mekkiamiri.memorybench.scenario.Scenario;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * La page rejoue le scénario en JavaScript. Ce test garantit qu'elle embarque le même scénario
 * et la trace de référence produite par le code Java ; la page compare sa simulation à cette trace
 * et affiche l'écart. Pour régénérer : {@code mvn -q compile exec:java -Dexec.args="--trace demenagement target/trace.json"}
 * puis recopier dans la balise {@code trace-java} de docs/index.html.
 */
class PageTest {

    private static final Path PAGE = Path.of("docs/index.html");

    private static String bloc(String html, String id) {
        String debut = "id=\"" + id + "\">";
        int i = html.indexOf(debut);
        int j = html.indexOf("</script>", i);
        if (i < 0 || j < 0) {
            throw new AssertionError("balise " + id + " absente de la page");
        }
        return html.substring(i + debut.length(), j).strip();
    }

    @Test
    void laPageEmbarqueLeScenarioEtLaTraceDuBanc() throws IOException {
        String html = Files.readString(PAGE, StandardCharsets.UTF_8);
        Scenario s = LecteurScenario.charger("demenagement");
        assertEquals(s.source().strip(), bloc(html, "scenario"), "scénario de la page désynchronisé");
        assertEquals(Banc.trace(s).strip(), bloc(html, "trace-java"), "trace de la page désynchronisée");
    }
}
