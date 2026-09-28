# UI Components

The frontend is organized by feature (`frontend/CLAUDE.md`). The server is authoritative: components render `PlayerView` and send intentions, and they never apply moves locally.

## Primitives
- [ ] Button (primary, secondary, ghost, danger)
- [ ] Input (text, email, password with show/hide)
- [ ] FormField (label, error text, `aria-describedby`)
- [ ] Spinner
- [ ] Badge (status: COMPLETED / FORFEIT / ABANDONED, "Na beczce", "Rozłączony")

## Composite
- [ ] Form (client-side UX validation only)
- [ ] Card (panel)
- [ ] Modal / ConfirmDialog (account deletion with password confirmation)
- [ ] Toast (action rejected, reconnecting, and similar)
- [ ] Table (match history)
- [ ] EmptyState (US-010: "Utwórz stół" / "Dołącz kodem")
- [ ] ConnectionBanner ("Łączenie ponownie…" during a STOMP reconnect)

## Game (feature `game`)
- [ ] PlayingCard (face, back, selected, illegal-dimmed, trump-highlighted)
- [ ] Hand (own cards; select, play and multi-select for discard or give)
- [ ] OpponentSeat (name, card count, game score, barrel badge, dealer or sitting-out marker, connection state and grace countdown)
- [ ] TrickArea (cards played to the current trick, by seat)
- [ ] MusikArea (face-down, or revealed; 2 musiks in 2p)
- [ ] BiddingPanel (current bid, Bid +10 / custom value, Pass; buttons disabled according to `legalActions`)
- [ ] ContractPanel (declare the final contract ≥ the bid; bomb button once C-09 is verified)
- [ ] GiveCardsPanel (3p/4p: assign one card to each opponent)
- [ ] MarriageToggle (shown when leading with the Q or K of a held pair)
- [ ] TurnTimer (60 s countdown, warning below 15 s)
- [ ] ScoreBoard (per-round and game totals, barrel status)
- [ ] RoundSummaryDialog (card points, marriages, contract made or failed)
- [ ] GameOverDialog (winner or draw, final scores)
- [ ] EventLine (the last action, including "Ruch automatyczny (czas minął)")

## Lobby (feature `lobby`)
- [ ] CreateTableForm (choice of 2, 3 or 4 players; unavailable counts disabled with an explanation, ADR-006)
- [ ] JoinByCodeForm
- [ ] TableRoom (seat list, invite link with copy button, Start for the host, Leave)

## Account (features `auth`, `profile`)
- [ ] LoginForm, RegisterForm, ForgotPasswordForm, ResetPasswordForm
- [ ] MatchHistoryList (newest first, status badges, "Gracz usunięty" for a deleted account)
- [ ] DeleteAccountSection

## Layout
- [ ] AppShell (header with username and logout)
- [ ] Header
- [ ] Footer (privacy policy link, "Polityka prywatności")
- [ ] Container
- [ ] GameLayout (full-screen table, seats arranged by player count: 2p opposite; 3p/4p around the table with the sitting-out seat greyed out)

## Pages / routes (React Router)
| Route | Page | Auth |
|---|---|---|
| `/` | Home: current activity or EmptyState | yes |
| `/logowanie` | Login | no |
| `/rejestracja` | Register | no |
| `/reset-hasla`, `/reset-hasla/:token` | Password reset request / confirm | no |
| `/polityka-prywatnosci` | Privacy policy | no |
| `/stol/:joinCode` | TableRoom (the invite link; unauthenticated users go to login and come back, US-007 AC-2) | yes |
| `/mecz/:matchId` | GameLayout | yes (participant) |
| `/historia` | Match history | yes |
| `/konto` | Account and deletion | yes |
