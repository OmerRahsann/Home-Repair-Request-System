Feature: Customer should able register and log in the application

  @regression
  Scenario: Customer Registration
    Given a user visits the registration page
    When the user enters valid registration details
    And the user submits the registration form
    Then the user should be registered successfully
  @regression
  Scenario: Customer Login with Valid Credentials
    Given a registered user visits the login page
    When the user enters valid login credentials
    Then the user submits the login form and the user should be logged in successfully


