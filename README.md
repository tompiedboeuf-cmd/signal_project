# Cardio Data Simulator

The Cardio Data Simulator is a Java-based application designed to simulate real-time cardiovascular data for multiple patients. This tool is particularly useful for educational purposes, enabling students to interact with real-time data streams of ECG, blood pressure, blood saturation, and other cardiovascular signals.

## Features

- Simulate real-time ECG, blood pressure, blood saturation, and blood levels data.
- Supports multiple output strategies:
  - Console output for direct observation.
  - File output for data persistence.
  - WebSocket and TCP output for networked data streaming.
- Configurable patient count and data generation rate.
- Randomized patient ID assignment for simulated data diversity.

## UML Models

This project includes UML class diagrams for the CHMS system.
1. Alert Generation System
uml_models/alert_generation_system.png

This diagram models how the system detects when a patient's vitals go outside safe limits and sends out an alert. The main class here is AlertGenerator, which looks at each patient's data and checks whether any thresholds have been exceeded. If something is wrong, it creates an Alert object that stores the patient ID, what the condition is, and when it happened.

To keep things flexible, we used a Threshold interface so that different types of thresholds (like HeartRateThreshold with a limit of 130 bpm or TemperatureThreshold at 38.5°C) can be added without changing the AlertGenerator itself. Each patient can have one or more thresholds assigned to them, which means the system can handle different alert rules per patient.

The Patient class holds the actual PatientData (heart rate, temperature, timestamp), and once the AlertGenerator detects a problem it passes the alert to AlertManager, which is responsible for sending it to the right medical staff.

We designed it this way to keep each class focused on one job the generator checks conditions, the manager handles sending, and the threshold classes define the rules. This makes it easy to add new alert types in the future without breaking existing code.

2. Data Storage System
uml_models/data_storage_system.png

This diagram shows how patient data is stored, accessed, and cleaned up over time. The central class is DataStorage, which holds a list of PatientData records. Each record contains a patient ID, timestamp, heart rate, blood pressure, and a version number. The class provides methods to store new data, fetch data by patient ID, and delete old records.

To handle data cleanup, DataStorage has an optional DataRetentionPolicy object. This policy defines how long data should be kept and applies the deletion logic when called. We kept it separate from DataStorage so that the retention rules can be changed or turned off without touching the storage logic itself.

For retrieving data, we added a DataRetriever class that sits between the storage and whoever is requesting the data. Before returning anything, it checks with AccessControl whether the requesting User actually has permission to see that patient's records. This way, sensitive medical data is never handed out without an access check.

Overall, this design makes sure that storage, access control, and data lifecycle are all handled separately, which makes the system easier to maintain and more secure.

3. Patient Identification System
uml_models/patient_Identification.png

This diagram models how the system links incoming data from the simulator to real patient records in the hospital database. The main class is PatientIdentifier, which takes a patient ID from the data stream and tries to find the matching HospitalPatient. It also has an isRegistered() method to check whether a patient exists before doing a full lookup.

The patientRegistry stores all the actual hospital patient records, including name and medical history. It provides methods to find patients by ID and register new ones. We kept this separate from PatientIdentifier so that the registry can be updated independently.

One important thing we thought about was what happens when a patient ID doesn't match anything. Instead of crashing or silently ignoring the problem, the IdentityManager handles this by logging an IdentityAnomaly with the patient ID, the reason it failed, and a timestamp. This gives the system a clear record of any mismatches that happened.

On the simulator side, the Patient class only holds a numeric ID on purpose — it has no access to hospital details like names or history. Only PatientIdentifier can resolve an ID into a full HospitalPatient, which keeps sensitive information protected from the data generation side of the system.

4. Data Access Layer
uml_models/data_Access_Layer.png.png

This diagram shows how the system connects to different data sources — TCP, WebSocket, and file-based input — and turns the raw data into something the rest of the system can use. The idea was to make it so that the rest of the system doesn't need to know or care how the data is coming in.

We defined an abstract DataListener class with three subclasses: TCPDataListener, WebSocketDataListener, and FileDataListener. Each one handles connecting and receiving data in its own way, but they all follow the same interface. This means you can switch between data sources without changing anything else in the system.

Once raw data arrives, it gets passed to DataParser, which figures out the format, parses it, and validates it. The result is a clean ParsedData object that holds the patient ID, parameter type, value, and timestamp. This object is what the rest of the system works with it never sees the raw strings.

The DataSourceAdapter ties everything together. It holds a listener and a parser, and when data comes in it runs it through the parser and passes the result along to storage. This keeps each class focused on one responsibility and makes the layer easy to extend if a new data source needs to be added later.

See: /uml_models

## Getting Started

### Prerequisites

- Java JDK 11 or newer.
- Maven for managing dependencies and compiling the application.

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/tpepels/signal_project.git
   ```

2. Navigate to the project directory:

   ```sh
   cd signal_project
   ```

3. Compile and package the application using Maven:
   ```sh
   mvn clean package
   ```
   This step compiles the source code and packages the application into an executable JAR file located in the `target/` directory.

### Running the Simulator

After packaging, you can run the simulator directly from the executable JAR:

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar
```

To run with specific options (e.g., to set the patient count and choose an output strategy):

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --patient-count 100 --output file:./output
```

### Supported Output Options

- `console`: Directly prints the simulated data to the console.
- `file:<directory>`: Saves the simulated data to files within the specified directory.
- `websocket:<port>`: Streams the simulated data to WebSocket clients connected to the specified port.
- `tcp:<port>`: Streams the simulated data to TCP clients connected to the specified port.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


## Project Members
- Student ID: 6436890
- Student ID: 6414274
