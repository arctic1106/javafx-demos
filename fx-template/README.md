# JavaFX Starter
A simple JavaFX application template that demonstrates how to build a maintainable and flexible JavaFX app. This project focuses on dynamically switching views, keeping the UI and logic separate, and making it easy to extend.

## Features
- **Dynamic View Switching**:
  - Easily switch between different views at runtime.
- **Separation of UI and Logic**:
  - The UI code is separate from the business logic, making it easier to maintain, test, and scale.
  - Controllers are passed into views via constructor injection, allowing views to call controller methods for actions.
- **Pure Java UI Definition**:
  - All UI components are written in Java (no FXML), offering better control and reducing potential errors.
- **Scalable and Extensible**: 
  - Adding new views or features is simple, with no major changes needed to the core code.

## Requirements
- **Java 23** or later.
- **Maven** for building and running the project.

## Running directly with the javaFX plugin
```sh
mvn javafx:run
```

## Creating jar without dependencies (fat jar)
```sh
mvn clean package
```
Run normally:
```sh
java -jar <jarfile>
```