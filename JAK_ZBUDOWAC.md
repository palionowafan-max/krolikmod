# Królik Mod – Forge 1.20.1

## Jak zrobić z tego plik .jar (tylko przeglądarka)

### Sposób A – GitHub Actions (najprościej)
1. Wejdź na github.com → **New repository** → nazwa np. `krolikmod` → Create.
2. Kliknij **uploading an existing file** i przeciągnij WSZYSTKIE pliki i foldery z rozpakowanego zipa
   (razem z ukrytym folderem `.github`!) → **Commit changes**.
3. Wejdź w zakładkę **Actions** → "Zbuduj moda" → poczekaj ok. 5 minut aż będzie zielony ✔.
4. Kliknij w ten build → na dole **Artifacts** → pobierz `krolikmod-jar`.
5. W środku jest `krolikmod-1.0.0.jar` → wrzuć do `.minecraft/mods` (Forge 1.20.1).

### Sposób B – GitHub Codespaces
1. Na stronie repo: **Code → Codespaces → Create codespace**.
2. W terminalu na dole wpisz:
   ```
   chmod +x gradlew
   ./gradlew build
   ```
3. Plik pojawi się w `build/libs/` → prawy przycisk → **Download**.

## Jak grać
- Weź **Tablet** (zakładka "Królik Mod" w kreatywnym) → PPM → kup **Królika**.
- Paczka spada 5 bloków przed tobą → PPM na paczkę → PPM pudełkiem z królikiem na ziemię.
- W survivalu płacisz szmaragdami, w kreatywnym wszystko za darmo.
- Rzeczy do założenia (okulary, buty, ochrona, dźwiękoinator, miłośnik): PPM na królika.
- Shift + PPM pustą ręką na królika = zdejmij wszystko.
- Komendy (z Miłośnikiem): /obrot /skok /idz /gryzon /miksturowiec
