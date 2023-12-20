Feature: Provider creates Appointment
  @regression
  Scenario: Provider creates Appointment
    Given a logged-inn provider is on the dashboard
    When Provider sees the notification and creates an appointment
    When the customer checks the notification and accept the appointment.
    Then the appointment on provider calendar should updated as approved.
    Then the provider will cancel the appointment and customer will get notified