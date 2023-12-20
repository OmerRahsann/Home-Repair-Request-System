Feature: Provider Filters and Views Service Requests and Send email Requests
  @regression
  Scenario: Provider Filters Service Requests
    Given a logged-in provider is on the dashboard
    When the provider applies filters to view specific service requests
    When a logged-in provider is viewing a service request
    When the provider clicks on the "Request Email" button
    Then the email should be sent successfully

  @regression
  Scenario: Customer Accepts Email Request from Service Provider
    Given a loggedin customer is on the dashboard
    And a customer clicks and open the  email request notification from a service provider
    Then the customer should able to accepts or decline the request