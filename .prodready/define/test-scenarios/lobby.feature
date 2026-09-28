Feature: Private tables
  As a player
  I want to create private tables and invite friends by code or link
  So that we can play together

  Background:
    Given authenticated players "anna", "bartek", "celina" and "darek"

  Scenario Outline: [US-006] - Create a table for a supported player count
    When "anna" creates a table for <count> players
    Then a table with capacity <count> exists with "anna" seated as host
    And a join code and an invite link are returned

    Examples:
      | count |
      | 2     |
      | 3     |
      | 4     |

  Scenario: [US-006] - Unsupported player count is rejected
    When "anna" creates a table for 5 players
    Then the request is rejected with a validation error

  Scenario: [US-006] - Cannot create while seated elsewhere
    Given "anna" is seated at another open table
    When "anna" creates a table for 3 players
    Then the request is rejected

  Scenario: [US-007] - Join by code
    Given "anna" hosts an open 3-player table
    When "bartek" joins with its code
    Then "bartek" is seated
    And "anna" sees the updated seat list in real time

  Scenario: [US-007] - Invite link survives login
    Given "anna" hosts an open 3-player table
    And a logged-out visitor opens its invite link
    When the visitor logs in as "bartek"
    Then "bartek" is returned to that table's join flow

  Scenario: [US-007] - Full table rejects joins
    Given "anna" hosts a 2-player table where "bartek" is seated
    When "celina" joins with its code
    Then the join is rejected with "table full"

  Scenario: [US-007] - Unknown code
    When "bartek" joins with code "NOPE1234"
    Then the join is rejected with "table not found"

  Scenario: [US-008] - Host starts a full table
    Given "anna" hosts a 3-player table with "bartek" and "celina" seated
    When "anna" starts the match
    Then a 3-player match begins with a randomly chosen first dealer

  Scenario: [US-008] - Cannot start with empty seats
    Given "anna" hosts a 3-player table with only "bartek" seated
    When "anna" starts the match
    Then the start is rejected

  Scenario: [US-008] - Non-host cannot start
    Given "anna" hosts a full 2-player table with "bartek"
    When "bartek" starts the match
    Then the start is rejected as unauthorized

  Scenario: [US-009] - Host leaving closes the table
    Given "anna" hosts an unstarted table with "bartek" seated
    When "anna" leaves
    Then the table is closed
    And its join code no longer admits players

  Scenario: [US-010] - First-run empty state
    Given "darek" has no match history
    When he opens his home page
    Then he sees "Create table" and "Join with code" actions
