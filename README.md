# Lipa Terminal — TPE Android (POS)

Application Android native (Kotlin / Jetpack Compose) du **terminal de paiement Lipa**.
Elle gère **une seule** opération financière : **carte NFC client → paiement marchand**,
encaissé sur l'appareil par un caissier (opérateur marchand) authentifié.

Pas de cash-in, pas de cash-out, pas d'émission de carte, pas d'historique, pas de
profil — ces flux appartiennent aux apps client/agent. Le périmètre suit
`Terminal_Frontend_Specification.md` (v1.1), qui fait **foi** : on n'appelle et n'affiche
rien qui n'y figure pas.

> « KomoPay » est le nom interne du backend — jamais montré au caissier. La copie visible
> est sous la marque **Lipa**. La devise est le **KMF** (entier, unité mineure). Les clés
> de configuration internes (`KOMOPAY_API_BASE_URL`, noms de propriétés Gradle…) gardent
> le préfixe `komopay` : ce ne sont pas des libellés visibles.

## Stack

- **Kotlin** + **Jetpack Compose** (Material 3), `compileSdk` 35, `minSdk` 24, **JDK 21**
- **ViewModel** + **StateFlow** — une seule `TerminalViewModel` pilote une machine à états
- **OkHttp** — client HTTP
- **kotlinx.serialization** — JSON
- **DataStore Preferences** — préférences
- **NFC** (`android.nfc`, mode reader) — lecture réelle de l'UID de carte
- Polices : Bricolage Grotesque (UI) + DM Mono (chiffres/codes)

## Architecture

```
app/src/main/java/com/lipa/terminal/
  MainActivity.kt          héberge le NfcReader, branche les UID lus → ViewModel
  domain/
    TerminalViewModel.kt   machine à états (Screen) + logique de paiement
  data/
    api/                   TerminalApi (contrat) + HttpTerminalApi (OkHttp) + ApiResult
    model/                 requêtes / réponses / enums / ApiError (spec §6–§9)
    repository/            SessionRepository (sessions terminal + opérateur, StateFlow)
  nfc/
    NfcReader.kt           NfcAdapter en reader mode → flux d'UID hex
  ui/
    TerminalApp.kt         routeur Compose selon l'état
    screens/               DeviceLogin · OperatorLogin · Idle · TapCard ·
                           CustomerPin · Confirmation · Approved · Declined ·
                           TerminalLocked · ShiftMenu
    components/ theme/      composants partagés, couleurs, typographie, formatage
```

La `TerminalViewModel` détient tout l'état dans un `TerminalUiState` immuable et expose un
unique `StateFlow`. L'écran courant est un `sealed interface Screen` ; l'UI Compose ne fait
que rendre l'état et émettre des intentions.

## Authentification (deux niveaux)

1. **Device login** — `serial` + `apiKey` du terminal → token terminal. Les codes
   `TERMINAL_SUSPENDED` / `TERMINAL_REVOKED` / `TERMINAL_NOT_REGISTERED` verrouillent
   l'appareil (écran *Terminal verrouillé*).
2. **Operator login** — l'opérateur (caissier) s'identifie par téléphone + PIN sur le token
   terminal → token opérateur, avec lequel se font le challenge NFC et le paiement.

Fin de poste (*End shift*) déconnecte l'opérateur et revient à l'écran operator login ;
*Sign out* déconnecte le terminal entier.

## Flux de paiement

`Idle` (saisie du montant) → `TapCard` → carte présentée (UID lu par NFC) → soumission du
paiement. La réponse `202` porte un **outcome** de contrôle de transaction :

- **EXECUTED** → écran *Approuvé*
- **PENDING_PIN** → écran *PIN client* (4 chiffres) puis resoumission
- **PENDING_CONFIRMATION** → écran *Confirmation* (gros montant) puis resoumission

La resoumission réutilise le **même `Idempotency-Key`** (UUID généré au début de
l'encaissement) et ajoute `pin` ou `confirmationAcknowledged`. Un `X-Correlation-Id` est
joint pour le traçage.

### Méthode d'authentification carte

Par défaut, le terminal envoie l'**UID seul** (`UID_ONLY`). Les modes
`NFC_CHALLENGE_RESPONSE` / `CHALLENGE_RESPONSE` passent d'abord par
`POST /nfc/challenge` ; dans cette app la réponse cryptographique de la carte est
**simulée** (les SE applets réels ne sont pas intégrés), ce mode reste donc un outil de
dev/démo.

## Endpoints (spec §5 — 5 au total)

| Méthode | Chemin | Rôle |
|---|---|---|
| POST | `/api/v1/terminal/auth/login` | Login terminal (serial + apiKey) |
| POST | `/api/v1/terminal/auth/operator-login` | Login opérateur (téléphone + PIN) |
| POST | `/api/v1/terminal/auth/logout` | Déconnexion (terminal ou opérateur) |
| POST | `/api/v1/terminal/nfc/challenge` | Challenge NFC (modes challenge-response) |
| POST | `/api/v1/terminal/transactions/payment` | Paiement (boucle de contrôle 202) |

Toutes les réponses succès utilisent l'enveloppe `ApiResponse` ; les erreurs sont parsées
dans `ApiError` (avec repli sur le statut HTTP).

## Environnements (local / prod)

L'environnement est choisi au build via une propriété Gradle `-PENV=...` (défaut :
`local`). L'URL résolue est injectée dans `BuildConfig.KOMOPAY_API_BASE_URL`.

| ENV     | baseUrl par défaut       |
|---------|--------------------------|
| `local` | `http://localhost:8080`  |
| `prod`  | `https://api.lipa.km`    |

Surcharge directe : `-PKOMOPAY_API_BASE_URL=https://...` (prioritaire sur le défaut d'env).
La config réseau debug (`src/debug/res/xml/network_security_config.xml`) autorise le trafic
clair vers `localhost`, `127.0.0.1` et `10.0.2.2` pour le dev.

```powershell
# Build debug contre un backend local
.\gradlew assembleDebug

# Build debug contre la prod
.\gradlew assembleDebug -PENV=prod

# Installer sur un appareil/émulateur connecté
.\gradlew installDebug
```

## NFC

`NfcReader` utilise `NfcAdapter.enableReaderMode` (NFC-A/B/F/V, sans NDEF) ; l'UID du tag
est normalisé en hex majuscule sur 14 caractères et poussé vers la ViewModel. Si l'appareil
n'a pas de puce NFC ou qu'elle est désactivée, l'état le reflète (`nfcSupported` /
`nfcEnabled`) et l'écran *TapCard* l'indique. Il n'y a **pas** de saisie manuelle d'UID :
le terminal est NFC-only.
