# Notification Service

## Overview

The **Notification Service** is an event-driven microservice designed to listen for events from a message broker (RabbitMQ) and send specific email notifications based on the events received. The service is responsible for processing events, determining which type of notification should be sent, and generating the appropriate email content. It follows the principles of Domain-Driven Design (DDD), ensuring that the logic for event handling and email generation is cleanly separated.

## Purpose

The **Notification Service** automatically triggers email notifications in response to specific events. These events are sent to the service via RabbitMQ, and the service processes each event to generate the appropriate email content and send it to the designated recipients. Depending on the event, different types of notifications will be generated, such as user registration emails, password reset emails, or account updates.

Each type of notification has its own logic for generating the email body, allowing the service to handle a variety of use cases. The emails are dynamically created using predefined templates and event-specific data.

## Key Features

- **Event-Driven**: The service listens to RabbitMQ for incoming events, such as user actions or system triggers, which prompt the sending of emails.
- **Custom Email Notifications**: For each type of event, there is a corresponding notification handler that generates the appropriate email content.
- **Dynamic Email Generation**: Emails are dynamically generated using templates that are populated with data from the event (e.g., user information, reset links, etc.).
- **Separation of Concerns**: The service is structured so that event processing, email content generation, and email sending are all clearly separated, following best practices of Domain-Driven Design.

## Architecture

The **Notification Service** is structured around the concept of listening to events and generating the corresponding email notifications. It consists of the following key components:

1. **Event Listener**: The service listens for events that are published to RabbitMQ. Each event corresponds to a specific action or trigger in the system, such as user registration or a password reset request.

2. **Notification Handlers**: Once an event is received, the appropriate notification handler is invoked. These handlers are responsible for determining what type of notification to send (e.g., welcome email, password reset email) and generating the HTML content for the email.

3. **HTML Email Generation**: The service uses HTML templates and data from the events to generate dynamic email content. Each notification type has a dedicated handler that knows how to populate the email template with the necessary data.

4. **Email Sending**: After the HTML content is generated, the service uses an email provider to send the email to the recipients specified in the event.

5. **Infrastructure**: The service integrates with RabbitMQ for event consumption and uses an email service for sending emails. The infrastructure layer handles these integrations and other external dependencies.

## Workflow

1. **Event Reception**: The service listens for events from RabbitMQ.
2. **Event Processing**: When an event is received, the service processes the event and triggers the appropriate notification handler.
3. **Email Content Generation**: The notification handler generates the email content based on the event data.
4. **Email Delivery**: The generated email content is passed to the email service, which sends the email to the recipients.

## Project Structure

The service is structured following Domain-Driven Design (DDD) principles, with clear separation of concerns between different parts of the application.

### Key Directories:
- **`application`**: Contains the logic for handling incoming events and triggering the corresponding notification handlers.
  - **`events`**: Defines the events that trigger notifications.
  - **`handlers`**: Contains the classes that process the events and generate the corresponding emails.

- **`domain`**: Contains domain models and logic for handling notifications and email content generation.
  - **`model`**: Defines the domain models for notifications, users, and other relevant data.
  - **`valueObject`**: Represents data objects such as email content or user information.

- **`infrastructure`**: Contains the integration with external services like RabbitMQ and email providers.
  - **`messaging`**: Manages communication with RabbitMQ for event consumption.
  - **`emailService`**: Handles the sending of emails via an email provider.

- **`templates`**: Stores HTML templates used by the notification handlers to generate dynamic email content.

## Summary

The **Notification Service** is a flexible and scalable microservice designed to handle email notifications in response to events in the system. By leveraging RabbitMQ for event-driven communication and following Domain-Driven Design principles, the service ensures that email notifications are processed in a maintainable and extendable manner. Each event type has its own logic for generating the corresponding email, and the service is easily extensible to accommodate additional notification types or new event-driven use cases in the future.
