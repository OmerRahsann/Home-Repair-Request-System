Feature: Service Provider Registration, Login and update info
  @regression
  Scenario: Provider Registration
    Given a user visits the provider registration page
    When the provider enters valid registration details
    When the provider submits the registration form
    Then the provider should be registered successfully
  @regression
  Scenario: Provider Login and update info with Valid Credentials
    Given a registered provider visits the login page
    When the provider enters valid login credentials
    Then the provider submits the login form and should be logged in successfully
    When the provider tries to update their info
    Then the provider should able to update their info successfully
