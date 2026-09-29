Feature: Delivery and operations
  As an operator
  I want automated quality checks, deployment and observability
  So that main stays deployable and production problems are visible

  Scenario: [US-027] - CI blocks a failing pull request
    Given a pull request whose tests fail
    When CI runs
    Then the merge is blocked

  Scenario: [US-027] - Domain coverage threshold
    Given the game module line coverage is 85%
    When CI reports coverage
    Then the build fails

  Scenario: [US-027] - Deploy from main
    When a change is merged to main
    Then the deploy workflow updates the VPS without manual SSH steps

  Scenario: [US-027] - HTTP redirects to HTTPS
    When the production domain is requested over HTTP
    Then it redirects to HTTPS with a valid certificate

  Scenario: [US-028] - Metrics and logs in Grafana
    Given the production deployment
    When the operator opens Grafana
    Then HTTP latency, active WebSocket sessions and active matches are visible
    And application logs are searchable
    And no log line contains a password, token or secret
