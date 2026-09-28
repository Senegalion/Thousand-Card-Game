Feature: Match history
  As a player
  I want to see my finished matches with their outcome
  So that I can review results and future statistics stay fair

  Scenario: [US-025] - History lists my matches newest first
    Given "anna" finished two completed matches and one forfeited match
    When she opens her match history
    Then she sees 3 matches newest first with date, player count, participants, status and final scores

  Scenario: [US-025] - History contains only my matches
    Given "bartek" played a match without "anna"
    When "anna" opens her match history
    Then bartek's match is not listed

  Scenario: [US-025] - Non-completed statuses are explicit
    Given "anna" has a FORFEIT match in her history
    When she opens her match history
    Then the match is labelled FORFEIT and visually distinct from COMPLETED

  Scenario: [US-026] - Only completed matches are statistics-eligible
    Given stored matches with statuses COMPLETED, FORFEIT and ABANDONED
    When the statistics-eligible match query runs
    Then only the COMPLETED match is returned
