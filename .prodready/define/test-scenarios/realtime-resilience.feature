Feature: Realtime play, turn timers and reconnection
  As a player
  I want live updates, no stalled turns and protection against dropped connections
  So that matches are reliable

  Background:
    Given a 3-player match in progress between "anna", "bartek" and "celina"

  Scenario: [US-021] - Moves are pushed in real time
    When "anna" plays a legal card
    Then "bartek" and "celina" receive the updated state over WebSocket without refreshing

  Scenario: [US-022] - Bidding timer expiry passes
    Given it is bartek's bidding turn and he is allowed to pass
    When his turn timer expires
    Then the server passes for "bartek"
    And "bartek" is still seated

  Scenario: [US-022] - Trick timer expiry plays the lowest legal card
    Given it is celina's turn in trick play
    When her turn timer expires
    Then the server plays celina's lowest legal card

  Scenario: [US-023] - Reconnect within the grace period
    Given "bartek" disconnects
    When he reconnects after 90 seconds
    Then he receives the full current state for his seat
    And no cards, scores or turn information were lost

  Scenario: [US-023] - Others see the disconnect
    When "bartek" disconnects
    Then "anna" and "celina" see bartek marked as disconnected with the remaining grace time

  Scenario: [US-024] - Forfeit after the grace period
    Given "bartek" disconnects
    When 2 minutes pass without him reconnecting
    Then the match ends with status FORFEIT
    And "bartek" is recorded as FORFEITED
    And "anna" and "celina" are recorded as WIN_BY_FORFEIT with their current scores

  Scenario: [US-024] - Everyone gone means abandoned
    Given "anna", "bartek" and "celina" all disconnect
    When the last grace period expires
    Then the match ends with status ABANDONED
