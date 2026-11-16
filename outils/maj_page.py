"""Recopie le scénario d'exemple et la trace de référence du banc Java dans docs/index.html.

Usage (depuis la racine du dépôt) :
  mvn -q compile exec:java -Dexec.args="--trace demenagement target/trace.json"
  python3 outils/maj_page.py

Le test PageTest échoue si la page n'est pas à jour.
"""

import re
from pathlib import Path

RACINE = Path(__file__).resolve().parent.parent
PAGE = RACINE / "docs" / "index.html"


def remplacer(html: str, ident: str, contenu: str) -> str:
    motif = re.compile(r'(id="' + re.escape(ident) + r'">)(.*?)(</script>)', re.S)
    if not motif.search(html):
        raise SystemExit(f"balise {ident} absente de {PAGE}")
    return motif.sub(lambda m: m.group(1) + "\n" + contenu.strip() + "\n" + m.group(3), html, count=1)


def main() -> None:
    scenario = (RACINE / "src/main/resources/scenarios/demenagement.txt").read_text(encoding="utf-8")
    trace = (RACINE / "target/trace.json").read_text(encoding="utf-8")
    html = PAGE.read_text(encoding="utf-8")
    html = remplacer(html, "scenario", scenario)
    html = remplacer(html, "trace-java", trace)
    PAGE.write_text(html, encoding="utf-8")
    print(f"mis à jour : {PAGE.relative_to(RACINE)}")


if __name__ == "__main__":
    main()
