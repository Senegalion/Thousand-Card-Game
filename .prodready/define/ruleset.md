# Tysiąc Ruleset Specification

- Status: APPROVED except 15 `CONFIRM` items (C-01..C-13, R-085, R-103) pending verification in real Kurnik gameplay. They block implementation of the affected rules, not Design.
- Target: a faithful implementation of standard Polish Tysiąc **as played on Kurnik**. This is not a new or house-rule variant.
- Authoritative source: Kurnik, "Tysiąc – zasady", https://www.kurnik.pl/tysiac/zasady.phtml (retrieved 2026-09-28, licensed CC BY-NC). The full text is quoted in the Appendix. Where this document and the Kurnik text disagree, the Kurnik text wins and this document is a bug.
- Once approved, the `game` domain module implements exactly this document. Nothing outside it gets implemented.

## Status legend

| Status | Meaning |
|---|---|
| `KURNIK` | Stated explicitly in the Kurnik text. Accepted. |
| `DERIVED — APPROVED` | Not stated word for word, but follows directly from Kurnik text (the reasoning is given). The owner has signed off. |
| `CONFIRM` | Kurnik is silent or ambiguous. **No default is chosen.** Must be verified against actual Kurnik gameplay (owner answered "check"). |

Player-count tags: `[2p]`, `[3p]`, `[4p]`. Untagged rules apply to all counts.

Variant model: the engine has a fixed core (cards, bidding, tricks, marriages, scoring) plus a per-player-count variant configuration that covers deal layout, musik handling, the player sitting out, and the bomb penalty. Each player-count difference below is a field of that configuration.

---

## 1. Cards

- R-001 `KURNIK`: The deck has 24 cards, 9 to A in four suits. Quote: "mała talia (od 9 do asa)".
- R-002 `KURNIK`: Card strength from low to high is 9, J, Q, K, 10, A.
- R-003 `KURNIK`: Card values are A 11, 10 10, K 4, Q 3, J 2, 9 0, for a deck total of 120.
- R-004 `KURNIK`: A marriage (meldunek) is a Q and K of one suit. Values are ♥ 100, ♦ 80, ♣ 60, ♠ 40.
- R-005 `KURNIK` (by absence): There is no four-aces meld or any other meld.

## 2. Dealer and direction

- R-010 `KURNIK`: The first dealer is random. The next dealer is the player to the left of the previous one.
- R-011 `KURNIK`: Bidding and play proceed to the left.

## 3. Deal

- R-020 `KURNIK` [3p]: Each player gets 7 cards and 3 go to the musik.
- R-021 `KURNIK` [2p]: Each player gets 10 cards. The remaining 4 form two musiks of 2 cards each.
- C-01 `CONFIRM` [4p]: **Which player sits out ("na musie")?** Kurnik says only "jeden w każdym rozdaniu jest 'na musie'". It does not say that player is the dealer.
- C-02 `CONFIRM` [4p]: **The 4-player deal layout.** Kurnik does not state it. The implied layout is 7 cards to each of the 3 active players plus a 3-card musik, the same as 3p. This needs confirming.
- R-022 `KURNIK`: A player dealt all four 9s may ask for a redeal.
- C-03 `CONFIRM`: **How the four-9s redeal works.**
  - (i) When may it be requested (before bidding only)?
  - (ii) Does the same dealer redeal?
  - (iii) In 4p, does it apply to the player sitting out?

## 4. Bidding

- R-030 `KURNIK`: Bidding starts with the player to the dealer's left, who automatically bids 100.
- R-031 `KURNIK`: The following players, going left, may raise or pass. Bids are multiples of 10.
- R-032 `KURNIK`: A bid above 120 is allowed only if the bidder holds a marriage.
- R-033 `KURNIK`: The bid cap is 120 plus the total value of the marriages in the bidder's hand. The server checks this against the hidden hand.
- R-034 `KURNIK`: The bidding winner is the declarer ("grający").
- C-04 `CONFIRM`: **Can a player who has passed bid again later in the same auction?** Kurnik does not say. It also does not say when the auction ends; the assumed end is when all but one player have passed.
- R-035 `DERIVED — APPROVED 2026-09-28`: If everyone else passes, the automatic-100 opener is the declarer at 100. This follows from the automatic 100 in R-030.

## 5. Musik and final contract

- R-040 `KURNIK` [3p]: The declarer reveals the musik, takes it, and gives one card from their hand to each opponent.
- R-041 `KURNIK` [2p]: The declarer reveals one of the two musiks, takes it, and puts two chosen cards face down in place of the chosen musik.
- C-05 `CONFIRM` [2p]: **The unchosen musik.** Is it revealed to anyone during or after the round?
- C-06 `CONFIRM` [4p]: **Musik handling.** The implied handling is the 3p procedure (reveal, take, one card to each of the 2 active opponents). Kurnik does not state it.
- R-042 `KURNIK`: The declarer then states the final contract, which may not be less than the bid.
- C-07 `CONFIRM`: **The cap on the final contract.** Two readings are possible:
  - (i) the same 120 + marriages-in-hand rule, now counting marriages completed by the musik;
  - (ii) no cap.

  Kurnik does not say.

## 6. Bomb

- R-050 `KURNIK`: The declarer may withdraw by throwing a bomb. The declarer then loses no points.
- R-051 `KURNIK`: The first bomb carries no penalty. For each later bomb, every opponent gets +60, or +40 in a 4-player game.
- C-08 `CONFIRM`: **What does "first bomb" count?** It could be the first bomb per player in the game or the first bomb by anyone in the game.
- C-09 `CONFIRM`: **When can a bomb be thrown?**
  - The only point is after taking the musik and before the first lead?
  - Can it be thrown before or after passing cards (3p/4p)?
- C-10 `CONFIRM` [4p]: **Bomb and the player sitting out.** Does that player receive the +40, and do they still score musik points (R-091)?
- C-11 `CONFIRM`: **Bomb and opponents on the barrel.** Do they receive the +60/+40, given that R-100 says they score only as declarer?

## 7. Trick play

- R-060 `KURNIK`: The declarer leads to the first trick. The others follow in turn to the left.
- R-061 `KURNIK`: Players must follow the led suit, and must beat with a stronger card. Following suit takes priority over beating.
- R-062 `KURNIK`: The trick goes to whoever played the strongest card. Cards of the trump suit beat cards of other suits. Quotes: "Zagrane karty (tzw. lewa) zdobywa gracz, który zagrał najsilniejszą kartę" and "Kolor meldunku staje się atutowym - karty w tym kolorze przebijają inne".
- R-063 `DERIVED — APPROVED 2026-09-28`: The trick winner leads next. This is the standard trick-taking convention, and Kurnik's text implies it.
- R-064 `DERIVED — APPROVED 2026-09-28`: A card from another non-trump suit (neither the led suit nor trump) never wins a trick. Among non-trump cards, only cards of the led suit compete, and the strongest of them wins when no trump was played. Kurnik's "najsilniejszą kartę" doesn't say this explicitly; it is the universal trick-taking convention, and the owner has approved it.
- C-12 `CONFIRM`: **Obligations when void in the led suit, and when the trick has been trumped.**
  - (i) Kurnik's "obowiązek przebijania" most naturally reads as: the player must play a trump if they hold one and a trump is active. Otherwise any card may be played.
  - (ii) The same reading also requires over-trumping a trump already in the trick, if able.
  - (iii) The trick has already been trumped and the player can follow the led suit. Does the obligation to beat ("przebijanie") still require a higher card of the led suit than the highest led-suit card in the trick, even though no led-suit card can win it now? Or is any card of the led suit allowed?
  - The text does not spell out (i)–(iii). They must be verified against Kurnik's actual behavior.

## 8. Marriages and trump

- R-070 `KURNIK`: To declare a marriage, the player leads the Q or K of a pair they hold and says "melduję".
- R-071 `KURNIK`: The marriage's suit becomes trump until the round ends or a new marriage is declared.
- R-072 `KURNIK`: A declared marriage adds its value to that player's points in the round.
- R-073 `DERIVED — APPROVED 2026-09-28`: Any player who leads may declare, including defenders. Kurnik's scoring counts defenders' marriages ("Pozostali … plus meldunki"), and the text puts no restriction on who declares.
- C-13 `CONFIRM`: **Can the declarer declare a marriage on the very first lead of the round?** Kurnik does not restrict it. Some traditional rules require winning a trick first.

## 9. Round scoring

- R-080 `KURNIK`: Card points are counted from won tricks.
- R-081 `KURNIK` [2p]: The points of the cards in the musiks go to the winner of the last trick. Quote: "Punkty za karty z musików zdobywa zwycięzca ostatniej lewy". This covers the unchosen musik and the declarer's 2 face-down cards.
- R-082 `KURNIK`: If the declarer reaches at least the contract, they add the contract to their score. Otherwise they subtract it.
- R-083 `DERIVED — APPROVED 2026-09-28`: The declarer's total for meeting the contract is card points plus declared marriages. Defenders' marriages count, so the declarer's must count as well.
- R-084 `KURNIK`: Each defender scores card points plus declared marriages, rounded to a multiple of 10 (5 and up rounds up, below 5 rounds down).
- R-085 `CONFIRM` (moved from DERIVED to the verification list 2026-09-28; the text below is a hypothesis, not a rule): Scores may go negative. Kurnik says the contract "odpisuje" with no floor.
- R-086 `KURNIK` (by absence): There is no penalty for consecutive zero rounds.

### 9.1 The player sitting out [4p]

- R-091 `KURNIK` [4p]: The player sitting out scores the musik cards: 50 per ace, plus the value of any marriage in the musik.

## 10. Barrel (800)

- R-100 `KURNIK`: A player with 800 or more points gains further points only as declarer.
- R-101 `KURNIK` (by absence): There is no limit on rounds spent on the barrel. There is no falling off and no knock-off between players on the barrel. Several players may be at 800+ at once.
- R-102 `DERIVED — APPROVED 2026-09-28`: A player on the barrel who fails as declarer still loses the contract (R-082). R-100 only restricts gains.
- R-103 `CONFIRM` (moved from DERIVED to the verification list 2026-09-28; the text below is a hypothesis, not a rule) [4p]: A player on the barrel who is sitting out gets no musik points, because R-100 allows gains only as declarer.

## 11. End of game

- R-110 `KURNIK`: The game ends when someone reaches 1000 or more.
- R-111 `KURNIK`: If several players reach 1000 in the same round, the declarer wins. If none of them was the declarer, the higher score wins. Equal scores are a draw.
- R-112 `DERIVED — APPROVED 2026-09-28`: A player below 800 can reach 1000 as a defender. R-111 explicitly covers the case where none of the players who reached 1000 was the declarer.

## 12. Player-count differences

| Aspect | 2p | 3p | 4p |
|---|---|---|---|
| Players in the round | 2 | 3 | 3 active + 1 sitting out (C-01) |
| Deal | 10 each + 2 musiks × 2 | 7 each + 3 musik | C-02 (7 each + 3 implied) |
| Musik | reveal 1 of 2, take, discard 2 face down | reveal, take, give 1 to each opponent | C-06 (as 3p implied) |
| Cards in play per player | 10 | 8 | 8 (implied) |
| Leftover musik points | last-trick winner (R-081) | none left over | none left over (implied) |
| Player sitting out | — | — | aces 50 + marriages from the musik (R-091) |
| Bomb penalty per opponent | 60 | 60 | 40 |

## 13. Mapping from the previous draft (D-01 … D-20)

| Old | Topic | Old default | Kurnik result | Status |
|---|---|---|---|---|
| D-01 | Ace marriage | none | none (R-005) | KURNIK: default was right |
| D-02 | 4p sit-out scoring | marriages only | **aces 50 + marriages** (R-091) | KURNIK: default was **wrong** |
| D-03 | 4p sit-out on barrel | no points | **unclear** (R-103) | CONFIRM |
| D-04 | 2p musik | declarer keeps discards; unchosen musik goes to nobody | **both go to the last-trick winner** (R-081) | KURNIK: default was **wrong** |
| D-05 | Redeal | none | **four 9s allowed** (R-022); details C-03 | KURNIK: default was **wrong** |
| D-06 | >120 needs marriage | yes | yes (R-032) | KURNIK: default was right |
| D-07 | Max bid | 360 | **120 + marriages in hand** (R-033) | KURNIK: default was **wrong** |
| D-08 | Musik visible | yes | yes, 3p; 2p chosen musik only (R-040/041); 4p C-06 | KURNIK, per player count |
| D-09 | Bomb | not allowed | **allowed; first free, then 60/40** (R-050/051); details C-08..C-11 | KURNIK: default was **wrong** |
| D-10 | Automatic-100 declarer | plays normally | plays normally | DERIVED: no special rule in Kurnik |
| D-11 | Must beat | yes | yes (R-061) | KURNIK: default was right |
| D-12 | Must over-trump | yes | **unclear** (C-12) | CONFIRM |
| D-13 | Marriage timing | on lead, first lead OK | on lead (R-070); first lead **unclear** (C-13) | CONFIRM |
| D-14 | Defender marriages | yes | yes (R-073) | DERIVED: default was right |
| D-15 | Rounding | nearest 10, 5 up | same (R-084) | KURNIK: default was right |
| D-16 | Three-zero penalty | none | none (R-086) | KURNIK: default was right |
| D-17 | Barrel limit | 3 rounds then −120 | **no limit** (R-101) | KURNIK: default was **wrong** (house rule) |
| D-18 | Multiple on barrel | allowed | allowed (R-101) | KURNIK: default was right |
| D-19 | Reaching 1000 | declarer only | **any scoring** (R-112) | DERIVED: default was **wrong** |
| D-20 | Negative scores | allowed | **unclear** (R-085) | CONFIRM |

## 14. Open decisions — pending Kurnik gameplay verification

Owner response (2026-09-28): all C-01..C-13 marked **check**; R-085 and R-103 moved here as well — the owner does not know the Kurnik behavior and none may be assumed. Each must be verified by observing actual Kurnik gameplay (and recorded with evidence, e.g. screenshot or game log) before the rules it affects are implemented. The variant configuration must expose each as an explicit switch with **no default value** until verified.

| ID | Question | Player count |
|---|---|---|
| C-01 | Who sits out: the dealer, or someone else? | 4p |
| C-02 | The deal layout | 4p |
| C-03 | Four-9s redeal: timing, who redeals, and does it apply to the player sitting out? | all |
| C-04 | Can a player who passed bid again? When does the auction end? | all |
| C-05 | Is the unchosen musik revealed? | 2p |
| C-06 | Musik handling | 4p |
| C-07 | The cap on the final contract after taking the musik | all |
| C-08 | Is the free first bomb counted per player or per game? | all |
| C-09 | When a bomb can be thrown | all |
| C-10 | Bomb and the player sitting out | 4p |
| C-11 | Bomb bonus for opponents on the barrel | all |
| C-12 | (i) Must a player void in the led suit trump? (ii) Must they over-trump? (iii) After the trick is trumped, must a player following suit still beat the led suit? | all |
| C-13 | A marriage on the declarer's first lead | all |
| R-085 | Can scores go negative, or does Kurnik clamp them at 0? | all |
| R-103 | Does a player sitting out while on the barrel (800 or more) still score musik points (aces 50 + marriages)? | 4p |

## Out of scope

- Any rule not listed above: house rules, the three-zero-rounds penalty, falling off the barrel, the ace meld, stakes.
- Partnerships in 4p. The game is individual play.

## Appendix: Kurnik rule text (verbatim, CC BY-NC, © kurnik.pl)

```
Gra dla 2-4 graczy z użyciem małej talii (od 9 do asa).
Starszeństwo kart: 9, walet, dama, król, 10, as.
Pierwszy rozdający losowy, później kolejny na lewo od ostatniego.
Liczba rozdawanych kart przy grze w 3 osoby - po 7 kart, pozostałe 3
do musika; gra w dwie osoby - po 10 kart, reszta do dwóch musików po dwie karty.
LICYTACJA
Licytacja startuje od siedzącego po lewej rozdającego, który z automatu
gra 100.
Kolejni w kierunku na lewo mogą podnieść stawkę lub spasować.
Licytowane wartości muszą być wielokrotnościami 10.
Licytacja więcej niż 120 możliwa, tylko jeśli ma się jakiś meldunek.
Nie można licytować więcej niż 120 + wartość w meldunkach.
Zwycięzca licytacji to tzw. grający.
Grający przy grze w trójkę odkrywa musik, bierze karty i daje przeciwnikom po jednej karcie ze swoich; przy grze w dwie osoby - odkrywa jeden z dwóch musików, bierze karty i odkłada dwie wybrane zakryte w miejsce wybranego musika.
Grający deklaruje, ile ostatecznie gra - nie może to być mniej, niż licytował.
ROZGRYWKA
Grający wykłada kartę jako pierwszy, pozostali dokładają w kierunku
na lewo.
Istnieje obowiązek dokładania do koloru i przebijania silniejszą kartą
(pierwszy obowiązek ważniejszy).
Zagrane karty (tzw. lewa) zdobywa gracz, który zagrał najsilniejszą kartę.
Meldunek to para dama-król jednego koloru - jedna z nich musi być
zagrana jako pierwsza i zagrywający deklaruje "melduję".
Kolor meldunku staje się atutowym - karty w tym kolorze przebijają inne.
Atu obowiązuje do końca rozdania lub nowego meldunku.
Wartości meldunków - kier (czerwień) 100, karo (dzwonek) 80, trefl (żołądź) 60, pik (wino) 40.
PUNKTACJA
Po zakończeniu rozdania podlicza się punkty za wzięte lewy.
Wartości karty przy podliczaniu - as 11, dziesiątka 10, król 4, dama 3, walet 2, dziewiątka 0.
Punkty za karty z musików zdobywa zwycięzca ostatniej lewy.
Jeśli grający zdobędzie co najmniej tyle punktów, ile deklarował,
to dopisuje sobie deklarowaną wartość do swojego wyniku, a jeśli nie,
to ją odpisuje.
Pozostali gracze zdobywają tyle, ile wzięli w kartach plus meldunki,
zaokrąglone do wielokrotności 10 (od 5 w górę, poniżej - w dół).
Koniec następuje, gdy ktoś zdobędzie 1000 pkt. lub więcej.
Jeśli kilku zdobędzie jednocześnie, to wygrywa ten z nich, który
był grającym, a jeśli żaden nie był, to ten, kto zdobył więcej
(a jeśli zdobyli po równo, to jest remis).
INNE
Grający może wycofać się z grania, rzucając tzw. bombę.
Nie odpisuje mu się wtedy wylicytowanych punktów.
Pierwsza bomba jest bezkarna, przy kolejnych przeciwnikom dopisuje się po 60 pkt. (40 pkt. przy grze w czterech).
Mając 800 pkt. lub więcej, kolejne punkty zdobywa się, tylko będąc grającym.
Jeśli ktoś dostanie 4 dziewiątki, może poprosić o ponowne rozdanie.
Gdy gra jest w czterech, jeden w każdym rozdaniu jest "na musie" i
nie bierze udziału we właściwej grze, ale otrzymuje punkty za karty z musiku - za asa 50 pkt., za meldunki wartość meldunku.
```
