#!/bin/bash
#
# Elastic Beanstalk prebuild hook
# Installiert die fuer das serverseitige SVG->PNG Rendering (Apache Batik)
# benoetigten Schriftarten und konfiguriert fontconfig so, dass die in den
# nomnoml-SVGs verwendeten Schriftnamen (Helvetica/Arial/sans-serif) auf den
# frei verfuegbaren, metrisch kompatiblen Ersatz "Liberation Sans" gemappt
# werden. Dadurch passen die zur Helvetica-Metrik berechneten (fest
# eingefrorenen) Box-Breiten der nomnoml-Diagramme auch beim Batik-Rendering.
#
# Laeuft auf Amazon Linux 2 (yum) und Amazon Linux 2023 (dnf).
#
set -euo pipefail

echo "[install_fonts] Starte Font-Installation ..."

# --- Paketmanager bestimmen (AL2023 = dnf, AL2 = yum) ----------------------
if command -v dnf >/dev/null 2>&1; then
    PKG="dnf"
elif command -v yum >/dev/null 2>&1; then
    PKG="yum"
else
    echo "[install_fonts] FEHLER: weder dnf noch yum gefunden." >&2
    exit 1
fi
echo "[install_fonts] Verwende Paketmanager: ${PKG}"

# --- Pakete installieren ---------------------------------------------------
# fontconfig : Aufloesung generischer Familien + Aliase
# freetype   : Glyph-Rendering (von AWT/Batik benoetigt)
# liberation : metrisch kompatibler Arial/Helvetica/Times/Courier-Ersatz
# dejavu     : breiter Unicode-Fallback
#
# Hinweis: Paketnamen koennen je nach AL-Version leicht abweichen. Wir
# versuchen die gaengigen Namen und tolerieren, wenn einzelne fehlen.
install_pkg() {
    local pkg="$1"
    if ${PKG} install -y "${pkg}" >/dev/null 2>&1; then
        echo "[install_fonts]   installiert: ${pkg}"
    else
        echo "[install_fonts]   uebersprungen (nicht verfuegbar): ${pkg}"
    fi
}

echo "[install_fonts] Installiere Pakete ..."
install_pkg fontconfig
install_pkg freetype

# Liberation: mal als Sammelpaket, mal aufgeteilt -> beide Varianten versuchen
install_pkg liberation-fonts
install_pkg liberation-sans-fonts
install_pkg liberation-serif-fonts
install_pkg liberation-mono-fonts

# DejaVu als breiter Fallback
install_pkg dejavu-sans-fonts
install_pkg dejavu-sans-mono-fonts
install_pkg dejavu-fonts-common

# --- fontconfig-Alias schreiben -------------------------------------------
# Mappt die in den SVGs vorkommenden Namen auf Liberation (metrisch == Arial).
# Liberation Sans ist sehr nah an Helvetica -> die mit Helvetica vermessenen
# Box-Breiten passen.
echo "[install_fonts] Schreibe /etc/fonts/local.conf ..."
mkdir -p /etc/fonts
cat > /etc/fonts/local.conf <<'EOF'
<?xml version="1.0"?>
<!DOCTYPE fontconfig SYSTEM "fonts.dtd">
<fontconfig>
  <!-- Helvetica (nomnoml #font:helvetica) -> Liberation Sans -->
  <alias>
    <family>Helvetica</family>
    <prefer><family>Liberation Sans</family></prefer>
  </alias>
  <alias>
    <family>helvetica</family>
    <prefer><family>Liberation Sans</family></prefer>
  </alias>

  <!-- Arial -> Liberation Sans (metrisch identisch) -->
  <alias>
    <family>Arial</family>
    <prefer><family>Liberation Sans</family></prefer>
  </alias>

  <!-- generische Familien deterministisch festlegen -->
  <alias>
    <family>sans-serif</family>
    <prefer><family>Liberation Sans</family></prefer>
  </alias>
  <alias>
    <family>serif</family>
    <prefer><family>Liberation Serif</family></prefer>
  </alias>
  <alias>
    <family>monospace</family>
    <prefer><family>DejaVu Sans Mono</family></prefer>
  </alias>
</fontconfig>
EOF

# --- Font-Cache neu aufbauen ----------------------------------------------
echo "[install_fonts] Aktualisiere Font-Cache ..."
fc-cache -f >/dev/null 2>&1 || true

# --- Diagnose-Ausgabe (landet im EB-Deploy-Log) ---------------------------
echo "[install_fonts] Verfuegbare Schriften (Auszug):"
fc-list 2>/dev/null | grep -i -E 'liberation|dejavu' || echo "[install_fonts]   (keine gefunden!)"

echo "[install_fonts] Aufloesung der relevanten Familien:"
for fam in Helvetica Arial sans-serif serif monospace; do
    printf '[install_fonts]   %-12s -> ' "${fam}"
    fc-match "${fam}" 2>/dev/null || echo "(fc-match fehlgeschlagen)"
done

echo "[install_fonts] Fertig."
