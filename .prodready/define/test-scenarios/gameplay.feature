Feature: Game engine (Kurnik ruleset, 2/3/4 players)
  As a player
  I want every rule enforced by the server exactly as defined in ruleset.md
  So that matches are fair and faithful to Kurnik

  # Scenarios depending on CONFIRM items (C-01..C-13, R-085, R-103) are intentionally absent
  # until those rules are verified against Kurnik gameplay.

  Scenario: [US-011] - 3-player deal
    Given a 3-player match
    When a round is dealt
    Then each player holds 7 cards
    And the musik holds 3 cards
    And all 24 distinct cards are accounted for

  Scenario: [US-011] - 2-player deal
    Given a 2-player match
    When a round is dealt
    Then each player holds 10 cards
    And there are two musiks of 2 cards each

  Scenario: [US-011] - Hidden information
    Given a dealt 3-player round
    When "bartek" receives the game state
    Then it contains only bartek's own cards
    And it contains neither other hands nor musik contents

  Scenario: [US-012] - Automatic opening bid
    Given "anna" is the dealer in a 3-player round
    When bidding starts
    Then the player to anna's left holds an automatic bid of 100

  Scenario: [US-012] - Bid above 120 without a marriage is rejected
    Given "bartek" holds no king-queen pair of the same suit
    When "bartek" bids 130
    Then the server rejects the bid

  Scenario: [US-012] - Bid above 120 plus held marriages is rejected
    Given "bartek" holds only the spades marriage worth 40
    When "bartek" bids 170
    Then the server rejects the bid

  Scenario: [US-012] - Everyone passes after the automatic 100
    Given the opener holds the automatic 100
    When all other players pass
    Then the opener is the declarer with a bid of 100

  Scenario: [US-013] - 3-player musik is revealed and cards passed
    Given "bartek" is the declarer in a 3-player round
    When he takes the musik
    Then all players see the 3 musik cards
    When he gives one card to each opponent
    Then every player holds 8 cards

  Scenario: [US-013] - 2-player musik choice and discard
    Given "bartek" is the declarer in a 2-player round
    When he chooses the first musik
    Then only the chosen musik is revealed
    When he discards 2 cards face down
    Then each player holds 10 cards

  Scenario: [US-013] - Final contract cannot be lower than the bid
    Given "bartek" won the bidding at 120
    When he declares a final contract of 110
    Then the server rejects the declaration

  Scenario: [US-014] - Must follow suit
    Given hearts were led and "celina" holds a heart
    When "celina" plays a spade
    Then the server rejects the move
    And the game state is unchanged

  Scenario: [US-014] - Must beat when able
    Given the 10 of hearts is the highest heart in the trick and "celina" holds the ace and 9 of hearts
    When "celina" plays the 9 of hearts
    Then the server rejects the move

  Scenario: [US-014] - Trick winner leads next
    Given no trump is active and diamonds were led
    When the ace of diamonds wins the trick
    Then the player who played it leads the next trick

  Scenario: [US-014] - Off-suit non-trump card never wins
    Given no trump is active and the 9 of diamonds was led
    When another player discards the ace of clubs
    And no other diamond or trump is played
    Then the player who led the 9 of diamonds wins the trick

  Scenario: [US-015] - Declaring a marriage sets trump
    Given "anna" is on lead holding the king and queen of hearts
    When she leads the queen of hearts and declares a marriage
    Then 100 points are added to her round points
    And hearts become trump

  Scenario: [US-015] - Cannot declare a marriage without the pair
    Given "anna" is on lead holding the king of clubs but not the queen
    When she tries to declare a clubs marriage
    Then the server rejects the declaration

  Scenario: [US-016] - Declarer makes the contract
    Given "bartek" declared 120 and took 90 card points plus a 40 marriage
    When the round is scored
    Then 120 is added to bartek's score

  Scenario: [US-016] - Declarer fails the contract
    Given "bartek" declared 140 and took 70 card points plus a 60 marriage
    When the round is scored
    Then 140 is subtracted from bartek's score

  Scenario Outline: [US-016] - Defender rounding
    Given a defender took <raw> points in cards and marriages
    When the round is scored
    Then the defender scores <rounded>

    Examples:
      | raw | rounded |
      | 45  | 50      |
      | 44  | 40      |
      | 0   | 0       |

  Scenario: [US-016] - 2-player musik points go to the last-trick winner
    Given a 2-player round where the unchosen musik and the discards hold 21 card points
    When "anna" wins the last trick
    Then those 21 points count for "anna"

  Scenario: [US-016] - Barrel blocks defensive gains
    Given "celina" has 810 points and is a defender
    When the round is scored with celina taking 40 points
    Then celina's score remains 810

  Scenario: [US-016] - Failing on the barrel still subtracts
    Given "celina" has 820 points and fails a contract of 120 as declarer
    When the round is scored
    Then celina's score is 700

  Scenario: [US-017] - Bomb protects the declarer
    Given "bartek" is the declarer with a contract of 150
    When he throws a bomb
    Then bartek's score is unchanged

  Scenario: [US-018] - Redeal request without four nines is rejected
    Given "anna" holds three nines
    When she requests a redeal
    Then the server rejects the request

  Scenario: [US-019] - Declarer wins a simultaneous 1000
    Given "anna" as declarer and "bartek" as defender both pass 1000 in the same round
    When the round is scored
    Then "anna" wins the match
    And the match is recorded as COMPLETED with all final scores

  # R-111 tie-break is tested at the end-of-game rule level: two non-declarers crossing 1000 in the
  # same round is practically unreachable through normal play, but the rule must still be implemented.
  Scenario: [US-019] - Equal non-declarer totals draw
    Given the end-of-game rule receives two non-declarer players both at 1010 after a round
    When the winner is determined
    Then the match ends in a draw
