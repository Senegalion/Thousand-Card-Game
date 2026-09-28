Feature: Identity and account
  As a player
  I want a secure account with password recovery and deletion
  So that I can play under my own identity and control my data

  Scenario: [US-001] - Successful registration
    Given no account exists for "anna@example.com" or username "anna"
    When a visitor registers with email "anna@example.com", username "anna" and a valid password
    Then an account is created
    And the visitor is authenticated
    And the stored password is an Argon2id hash

  Scenario: [US-001] - Duplicate email is rejected
    Given an account exists for "anna@example.com"
    When a visitor registers with email "ANNA@example.com"
    Then registration is rejected with "email unavailable"
    And no new account is created

  Scenario: [US-001] - Password below minimum length is rejected
    When a visitor registers with a 7-character password
    Then registration is rejected with a password-policy error

  Scenario: [US-002] - Login with wrong password gives a generic error
    Given a registered player "anna@example.com"
    When she logs in with a wrong password
    Then login fails with "invalid credentials"
    And the response does not reveal whether the email exists

  Scenario: [US-002] - Login is rate limited
    Given the failed-login limit for "anna@example.com" has been reached
    When another login attempt is made
    Then the response status is 429

  Scenario: [US-002] - Logout invalidates the session
    Given an authenticated player
    When she logs out
    Then her previous session is rejected by REST and WebSocket endpoints

  Scenario: [US-003] - Password reset for a registered email
    Given a registered player "anna@example.com"
    When a password reset is requested for "anna@example.com"
    Then exactly one reset email with a single-use link is sent

  Scenario: [US-003] - Password reset does not enumerate accounts
    When a password reset is requested for "nobody@example.com"
    Then the response is identical to a registered-email request
    And no email is sent

  Scenario: [US-003] - Expired or used reset token is rejected
    Given a reset token that expired 31 minutes after issue
    When the player submits a new password with that token
    Then the reset is rejected
    And the password is unchanged

  Scenario: [US-003] - Successful reset invalidates old sessions
    Given a player with an active session
    When she completes a password reset
    Then the old session is rejected
    And she can log in with the new password only

  Scenario: [US-004] - Account deletion anonymizes shared history
    Given "anna" and "bartek" played a completed match
    When "anna" deletes her account confirming her password
    Then no record contains anna's id, email or username
    And bartek's history shows that match with "Deleted player", original scores and status

  Scenario: [US-004] - Deletion is blocked while seated
    Given "anna" is seated in an in-progress match
    When she requests account deletion
    Then the deletion is rejected

  Scenario: [US-005] - Privacy policy is public
    Given an unauthenticated visitor on the registration page
    When she opens the privacy policy link
    Then the privacy policy lists stored data, deletion and an access-request contact
