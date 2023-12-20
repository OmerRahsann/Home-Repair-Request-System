Feature: Customer Service Request Management
  @regression
  Scenario: Customer Creates a Service Request
    Given a logged-in customer is on the dashboard
    When the customer clicks on the "Create Service Request" button
    And the customer fills in the required details for the service request
    And the customer submits the service request form
    Then the service request should be created successfully

  @regression
  Scenario: Customer Edits Service Request
    Given a logged-in customer is on the dashboard
    When the customer clicks on the "Edit" button for the service request
    And the customer updates the details in the service request form
    And the customer submits the edited service request
    Then the service request should be updated successfully