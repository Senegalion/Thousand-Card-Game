# User Stories

Conventions:
- `AC-N` numbering resets per story. Each `AC-N` has exactly one canonical acceptance test named `AC-N: <criterion>`.
- Rule references (`R-xxx`) point to `ruleset.md`. Every approved `R-xxx` also needs its own domain tests, whether or not a story AC names it (success metric 1).
- Rules marked `CONFIRM` in `ruleset.md` (C-01..C-13, R-085, R-103) are **not** specified here. Stories that depend on them say so and stay blocked until those rules are verified.
- Assumptions `A-xx` are listed in `discovery.md`.

---

## Epic 1: Identity and Account

### US-001: Register an account
**As a** visitor
**I want to** register with email, username and password
**So that** I can create and join private tables

**Acceptance Criteria**:
AC-1: Given a visitor with an unused email and username and a password that meets the policy (A-01), when they submit registration, then an account is created and the visitor is authenticated.
AC-2: Given an email that is already registered, when a visitor submits registration with it, then registration is rejected with an "email unavailable" error and no account is created.
AC-3: Given a username that is already taken, when a visitor submits registration with it, then registration is rejected with a "username unavailable" error and no account is created.
AC-4: Given a password shorter than the minimum length (A-01), when a visitor submits registration, then registration is rejected with a password-policy error.
AC-5: Given a successful registration, when the stored account record is inspected, then the password is stored only as an Argon2id hash and the only personal fields are email and username.

**Priority**: P0
**Estimate**: M

---

### US-002: Log in and log out
**As a** player
**I want to** log in with email and password and log out
**So that** only I can act as my account

**Acceptance Criteria**:
AC-1: Given a registered player, when they submit a correct email and password, then they are authenticated and can reach protected pages.
AC-2: Given a registered player, when they submit a wrong password, then login fails with a generic "invalid credentials" error that doesn't reveal whether the email exists.
AC-3: Given repeated failed login attempts exceeding the configured rate limit (A-02), when another attempt is made, then it is rejected with HTTP 429 without checking the credentials.
AC-4: Given an authenticated player, when they log out, then their session or token no longer authorizes protected REST or WebSocket requests.

**Priority**: P0
**Estimate**: M

---

### US-003: Reset a forgotten password
**As a** player who forgot their password
**I want to** reset it by email
**So that** I can recover my account

**Acceptance Criteria**:
AC-1: Given a registered email, when a reset is requested, then one email containing a single-use reset link is sent through the SMTP provider.
AC-2: Given an unregistered email, when a reset is requested, then the response is identical to the registered-email response and no email is sent.
AC-3: Given a valid, unexpired reset token, when the player submits a new password that meets the policy, then the new password works for login and the old one no longer does.
AC-4: Given a reset token that is already used or older than 30 minutes (A-03), when it is submitted, then the reset is rejected and the password is unchanged.
AC-5: Given a successful password reset, when a session issued before the reset is used, then it is rejected (A-03).

**Priority**: P0
**Estimate**: M

---

### US-004: Delete my account
**As a** player
**I want to** permanently delete my account
**So that** my personal data is removed

**Acceptance Criteria**:
AC-1: Given an authenticated player who confirms with their current password (A-14), when they delete their account, then their email, username and password hash no longer exist in the database.
AC-2: Given a deleted account, when its former credentials are used to log in, then login fails with the generic invalid-credentials error.
AC-3: Given a completed match that included the deleted player, when another participant views that match in their history, then the deleted player's seat shows "Deleted player" with the original score and status.
AC-4: Given a deleted account, when all remaining match history records are inspected, then none contains the deleted user's id, email or username.
AC-5: Given a player seated at an active table or match, when they request account deletion, then deletion is rejected until they have left or the match has ended (A-14).

**Priority**: P0
**Estimate**: M

---

### US-005: Read the privacy policy
**As a** visitor
**I want to** read the privacy policy before registering
**So that** I know what data is stored and how to exercise my rights

**Acceptance Criteria**:
AC-1: Given an unauthenticated visitor on the registration page, when they follow the privacy policy link, then the privacy policy page is displayed without logging in.
AC-2: Given the privacy policy page, when it is displayed, then it lists the stored data (email, username, password hash, match history), the deletion option, and a contact address for access requests (A-11).

**Priority**: P0
**Estimate**: S

---

## Epic 2: Private Tables (Lobby)

### US-006: Create a private table
**As a** player
**I want to** create a private table for 2, 3 or 4 players
**So that** I can invite friends to play

**Acceptance Criteria**:
AC-1: Given an authenticated player who is not seated elsewhere (A-10), when they create a table with a player count of 2, 3 or 4, then a table is created with that capacity, the creator seated as host, and a join code and invite link returned.
AC-2: Given a create request with a player count other than 2, 3 or 4, when it is submitted, then it is rejected with a validation error.
AC-3: Given a player already seated at another active table or match, when they try to create a table, then the request is rejected.

**Priority**: P0
**Estimate**: M

---

### US-007: Join a private table by code or link
**As a** player
**I want to** join a friend's table with a code or invite link
**So that** we can play together

**Acceptance Criteria**:
AC-1: Given an open table with a free seat, when an authenticated player submits its join code, then they are seated and all seated players see the updated seat list.
AC-2: Given an unauthenticated visitor opening an invite link, when they log in, then they are returned to that table's join flow.
AC-3: Given a table whose seats are all taken, when another player tries to join, then the join is rejected with a "table full" error.
AC-4: Given a code that doesn't match any open table, when a player submits it, then the join is rejected with a "table not found" error.

**Priority**: P0
**Estimate**: M

---

### US-008: Start the match
**As a** table host
**I want to** start the match once every seat is filled
**So that** the game begins with the correct player count

**Acceptance Criteria**:
AC-1: Given a table with all seats filled, when the host starts the match, then a match begins for that player count with a server-chosen random first dealer (R-010).
AC-2: Given a table with at least one empty seat, when the host tries to start, then the start is rejected.
AC-3: Given a seated non-host player, when they try to start the match, then the start is rejected as unauthorized.

**Priority**: P0
**Estimate**: S

---

### US-009: Leave a table before the match starts
**As a** player
**I want to** leave a table I joined before the match starts
**So that** I'm free to join another table

**Acceptance Criteria**:
AC-1: Given a seated non-host player at an unstarted table, when they leave, then their seat becomes free and the other players see the updated seat list.
AC-2: Given the host of an unstarted table, when they leave, then the table is closed and its join code no longer admits players (A-15).

**Priority**: P1
**Estimate**: S

---

### US-010: First-run empty states
**As a** newly registered player
**I want to** see clear next steps when I have no tables or matches
**So that** I know how to start playing

**Acceptance Criteria**:
AC-1: Given a player with no match history, when they open their home or history page, then an empty state is shown with "Create table" and "Join with code" actions.

**Priority**: P1
**Estimate**: S

---

## Epic 3: Game Engine (Kurnik rules, 2/3/4 players)

### US-011: Deal cards per player count
**As a** player
**I want** each round dealt according to the rules for our player count
**So that** the game matches Kurnik

**Acceptance Criteria**:
AC-1: Given a 3-player match, when a round is dealt, then each player holds 7 cards and the musik holds 3 cards from a 24-card deck (R-001, R-020).
AC-2: Given a 2-player match, when a round is dealt, then each player holds 10 cards and there are two musiks of 2 cards each (R-021).
AC-3: Given a round that ends, when the next round is dealt, then the dealer is the player to the left of the previous dealer (R-010).
AC-4: Given a dealt round, when a player's client receives the game state, then it contains only that player's own cards and not other hands or the musik contents (hidden information).

**Priority**: P0
**Estimate**: M
**Blocked in part by**: C-01, C-02 (4-player deal and the player sitting out). 4-player dealing ACs are added once these are verified.

---

### US-012: Bid for the contract
**As a** player
**I want to** bid under the Kurnik bidding rules
**So that** the highest bidder becomes declarer

**Acceptance Criteria**:
AC-1: Given a new round, when bidding starts, then the player to the dealer's left holds an automatic bid of 100 (R-030).
AC-2: Given a bidding turn, when a player bids a value that isn't a multiple of 10 or doesn't exceed the current bid, then the server rejects the bid (R-031).
AC-3: Given a player holding no marriage, when they bid above 120, then the server rejects the bid (R-032).
AC-4: Given a player holding marriages totalling M points, when they bid above 120 + M, then the server rejects the bid (R-033).
AC-5: Given every other player has passed after the automatic 100, when bidding ends, then the opener is the declarer at 100 (R-035).

**Priority**: P0
**Estimate**: M
**Blocked in part by**: C-04 (re-entry after passing, auction end).

---

### US-013: Take the musik and declare the final contract
**As a** declarer
**I want to** take the musik and declare my final contract
**So that** I can play with the best hand

**Acceptance Criteria**:
AC-1: Given a 3-player declarer, when they take the musik, then its 3 cards are revealed to all players and added to the declarer's hand (R-040).
AC-2: Given a 3-player declarer holding 10 cards, when they give one card to each opponent, then every player holds 8 cards (R-040).
AC-3a: Given a 2-player declarer, when they choose one of the two musiks, then only the chosen musik's cards are revealed to both players (R-041).
AC-3b: Given a 2-player declarer holding 12 cards after taking a musik, when they discard 2 cards face down, then each player holds 10 cards and the discards are visible to no one (R-041).
AC-4: Given a declarer declaring the final contract, when the declared value is below their winning bid, then the server rejects it (R-042).

**Priority**: P0
**Estimate**: M
**Blocked in part by**: C-05, C-06, C-07 (unchosen musik visibility, 4-player musik, cap on the final contract).

---

### US-014: Play tricks with enforced obligations
**As a** player
**I want** the server to accept only legal cards
**So that** nobody can break the rules

**Acceptance Criteria**:
AC-1: Given the first trick of a round, when play begins, then only the declarer may lead (R-060).
AC-2: Given a player holding a card of the led suit, when they play a card of another suit, then the server rejects the move (R-061).
AC-3: Given a player holding a card of the led suit that beats the current highest card of that suit, when they play a lower card of that suit, then the server rejects the move (R-061).
AC-4: Given a completed trick, when the winner is determined, then the strongest trump wins, or if there is no trump the strongest card of the led suit wins, and the winner leads the next trick (R-062, R-063, R-064).
AC-5: Given a player whose move is rejected, when the rejection is returned, then the game state is unchanged for all players.

**Priority**: P0
**Estimate**: L
**Blocked in part by**: C-12 (obligations when void in the led suit, and whether a player must beat the led suit after the trick has been trumped).

---

### US-015: Declare marriages and set trump
**As a** player on lead
**I want to** declare a marriage when I lead its Q or K
**So that** I score it and set trump

**Acceptance Criteria**:
AC-1: Given a player on lead holding the Q and K of one suit, when they lead one of them and declare, then the marriage value is added to their round points and that suit becomes trump (R-070, R-071, R-072).
AC-2: Given an active trump, when a new marriage is declared, then the new suit replaces the previous trump (R-071).
AC-3: Given a player who doesn't hold both the Q and K of a suit, when they try to declare a marriage in it, then the server rejects the declaration.
AC-4: Given a defender on lead holding a marriage, when they declare it, then the declaration is accepted and scored for that defender (R-073).

**Priority**: P0
**Estimate**: M
**Blocked in part by**: C-13 (marriage on the declarer's first lead).

---

### US-016: Score the round
**As a** player
**I want** round scores calculated exactly as on Kurnik
**So that** the standings are correct

**Acceptance Criteria**:
AC-1: Given a declarer whose card points plus declared marriages are at least the contract, when the round is scored, then the contract is added to their score (R-082, R-083).
AC-2: Given a declarer whose card points plus declared marriages are below the contract, when the round is scored, then the contract is subtracted from their score (R-082).
AC-3a: Given a defender whose card points plus marriages end in 5 to 9, when the round is scored, then the value is rounded up to the next multiple of 10 (R-084).
AC-3b: Given a defender whose card points plus marriages end in 1 to 4, when the round is scored, then the value is rounded down to the previous multiple of 10 (R-084).
AC-4: Given a 2-player round, when it is scored, then the card points in both musik piles (the unchosen musik and the declarer's discards) go to the winner of the last trick (R-081).
AC-5: Given a player with 800 or more points who is a defender, when the round is scored, then their score doesn't increase (R-100).
AC-6: Given a player with 800 or more points who fails as declarer, when the round is scored, then the contract is subtracted (R-102).

**Priority**: P0
**Estimate**: L
**Blocked in part by**: R-085 (negative scores), R-103 (4-player sitting-out player on the barrel), C-01/C-02 (4-player musik scoring context, R-091).

---

### US-017: Throw a bomb
**As a** declarer
**I want to** throw a bomb instead of playing the contract
**So that** I avoid losing the contract points

**Acceptance Criteria**:
AC-1: Given a declarer who throws a bomb, when the round is scored, then the declarer's score is unchanged (R-050).
AC-2: Given the first bomb that qualifies as free, when it is thrown, then no opponent receives points (R-051).
AC-3: Given a subsequent bomb in a 2- or 3-player match, when it is thrown, then each opponent receives 60 points (R-051).

**Priority**: P0
**Estimate**: M
**Blocked in part by**: C-08, C-09, C-10, C-11 (bomb counting, timing, the sitting-out player, barrel interaction). The 4-player penalty of 40 depends on C-10.

---

### US-018: Request a redeal with four nines
**As a** player dealt all four 9s
**I want to** request a redeal
**So that** I don't have to play a hopeless hand

**Acceptance Criteria**:
AC-1: Given a player holding all four 9s, when they request a redeal, then the round is redealt (R-022).
AC-2: Given a player not holding all four 9s, when they request a redeal, then the server rejects the request.

**Priority**: P0
**Estimate**: S
**Blocked in part by**: C-03 (timing, who redeals, the sitting-out player).

---

### US-019: Finish the game at 1000
**As a** player
**I want** the match to end when someone reaches 1000
**So that** there is a clear winner

**Acceptance Criteria**:
AC-1: Given a round after which exactly one player has 1000 or more points, when it is scored, then the match ends with that player as the winner (R-110).
AC-2: Given several players reaching 1000 in the same round, one of whom was the declarer, when it is scored, then the declarer wins (R-111).
AC-3a: Given several players reaching 1000 in the same round, none of whom was the declarer, with different totals, when the winner is determined, then the player with the higher total wins (R-111, R-112).
AC-3b: Given several players reaching 1000 in the same round, none of whom was the declarer, with equal totals, when the winner is determined, then the match ends in a draw (R-111).
AC-4: Given a match that ends at 1000, when it is recorded, then its status is `COMPLETED` with every participant's final score.

**Priority**: P0
**Estimate**: S

---

### US-020: Complete matches for every player count
**As a** player
**I want to** play a full match with 2, 3 or 4 players
**So that** every supported format works end to end

**Acceptance Criteria**:
AC-1: Given a 2-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETED` (Playwright).
AC-2: Given a 3-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETED` (Playwright).
AC-3: Given a 4-player table, when a full match is played through the web UI until someone reaches 1000, then the match ends as `COMPLETED` (Playwright).

**Priority**: P0
**Estimate**: L
**Blocked in part by**: AC-3 depends on verification of the 4-player items C-01, C-02, C-06 and C-10.

---

## Epic 4: Realtime and Resilience

### US-021: See other players' moves in real time
**As a** player
**I want** the table to update as soon as anyone acts
**So that** the game feels live

**Acceptance Criteria**:
AC-1: Given a match in progress, when a player makes a legal move, then every connected participant receives the resulting state update over WebSocket without refreshing.
AC-2: Given 50 concurrent simulated users in a load test, when typical game actions are submitted, then the p95 server processing time is at most 200 ms.

**Priority**: P0
**Estimate**: M

---

### US-022: Automatic move when my turn timer expires
**As a** player at the table
**I want** a minimal legal move made when someone's turn timer runs out
**So that** the game never stalls

**Acceptance Criteria**:
AC-1: Given a connected player whose 60-second bidding turn timer (A-09) expires and who is allowed to pass, when the timer elapses, then the server passes for them.
AC-2: Given a connected player whose 60-second trick-play turn timer (A-09) expires, when the timer elapses, then the server plays their lowest legal card.
AC-3: Given a player whose turn timer expired once, when the automatic move has been made, then the match continues and the player is still seated.

**Priority**: P0
**Estimate**: M
**Blocked in part by**: timeout defaults for the other decision types (automatic-100 opener, musik choice and discard, card passing, final contract, bomb, marriage, redeal) and the definition of "lowest legal card". These go in the Design game-flow spec (see the open questions in `discovery.md`).

---

### US-023: Reconnect to a match in progress
**As a** player who lost connection
**I want to** return to my match within 2 minutes
**So that** I don't lose the game

**Acceptance Criteria**:
AC-1: Given a player who disconnects mid-match, when they reconnect within 2 minutes, then they receive the full current game state for their seat and can continue playing.
AC-2: Given a player who reconnected within the grace period, when their state is compared with the state before the disconnect, then no cards, scores or turn information were lost.
AC-3: Given a player who disconnects, when the other participants view the table, then they see that player marked as disconnected with the remaining grace time.

**Priority**: P0
**Estimate**: L

---

### US-024: Forfeit after the grace period expires
**As a** player whose opponent vanished
**I want** the match to end by forfeit after 2 minutes
**So that** I'm not stuck in a dead match

**Acceptance Criteria**:
AC-1: Given a disconnected player who doesn't reconnect within 2 minutes, when the grace period expires, then the match ends with status `FORFEIT`, that player is recorded as forfeiting, and the remaining players are recorded as winners by forfeit.
AC-2: Given a match ended by forfeit, when it is recorded, then participants, status, timestamp and the scores at that moment are stored.
AC-3: Given all remaining participants disconnected beyond the grace period, when the last grace period expires, then the match ends with status `ABANDONED` (A-08).

**Priority**: P0
**Estimate**: M

---

## Epic 5: Match History

### US-025: View my match history
**As a** player
**I want to** see my past matches
**So that** I can review my results

**Acceptance Criteria**:
AC-1: Given a player with finished matches, when they open their history, then each match shows its date, player count, participants, status and final scores, newest first.
AC-2: Given a player's history, when it is requested, then it contains only matches that player took part in.
AC-3: Given a match with status `FORFEIT` or `ABANDONED`, when it appears in history, then its status is shown explicitly and distinguished from `COMPLETED`.

**Priority**: P0
**Estimate**: M

---

### US-026: Only completed matches count for statistics
**As a** player
**I want** forfeited and abandoned matches excluded from future statistics
**So that** future rankings stay fair

**Acceptance Criteria**:
AC-1: Given stored matches with statuses `COMPLETED`, `FORFEIT` and `ABANDONED`, when the statistics-eligible match query runs, then only `COMPLETED` matches are returned.

**Priority**: P1
**Estimate**: S

---

## Epic 6: Operations

### US-027: Deploy from CI to the VPS over HTTPS
**As an** operator
**I want to** deploy `main` to the VPS from CI
**So that** releases are repeatable and safe

**Acceptance Criteria**:
AC-1a: Given a pull request, when CI runs, then the workflow executes unit tests, integration tests, dependency and security scanning, and coverage reporting.
AC-1b: Given a pull request with any failing CI job, when a merge is attempted, then branch protection blocks it.
AC-2: Given the domain/game module, when CI reports coverage, then line coverage is at least 90% or the build fails.
AC-3: Given a merge to `main`, when the deploy workflow runs, then the new version is running on the VPS without manual SSH steps.
AC-4: Given the production domain, when it is requested over HTTP, then it redirects to HTTPS with a valid certificate.

**Priority**: P0
**Estimate**: L

---

### US-028: Observe the production system
**As an** operator
**I want** metrics and logs in Grafana
**So that** I can detect and diagnose problems

**Acceptance Criteria**:
AC-1: Given the production deployment, when the operator opens Grafana, then application metrics (HTTP latency, active WebSocket sessions, active matches) are visible from Prometheus.
AC-2: Given the production deployment, when the operator searches logs in Grafana, then application logs from Loki are available and contain no passwords, tokens or secrets.
AC-3: Given the tracing increment is delivered, when a request is handled, then its trace is viewable in Tempo.

**Priority**: P0 (AC-3 is delivered incrementally, as agreed)
**Estimate**: M
