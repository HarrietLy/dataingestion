# Data Ingestion via Spring Batch

### Below is the prerequisite installed software to run the project in local computer
1. Java 21
2. IntelliJ (optional for viewing the code)

### 💡 Description
Ingestion of large text file via a CLI application. The command can then be run to ingestion
by a job orchestrator platform.

### 💡 Design Consideration
- Use database to persist and manage a FIFO queue, and a scheduled cron to process queued transaction
- Use a locked flag for each transaction in the database to make sure only one thread can execute trade at a time

### 🛠️ Setup Instructions

#### ✅ Step 1. Git Clone the Project to your local machine.
#### ✅ Step 2. Execute the following on your terminal
- cd <repo-folder-in-your-local>
- ./mvnw clean install 
- java -jar target/data-0.0.1-SNAPSHOT.jar classpath:nem12example
#### ✅ Step 3. To release to production, adding in path to the file as argument to the command line like below
- ./mvnw clean install in deploy script to build the jar in the production server
- java -jar target/data-0.0.1-SNAPSHOT.jar <filepath>

### 🛠️ Answers to Questions
Q1. What is the rationale for the technologies you have decided to use?


Q2. What would you have done differently if you had more time?


Q3. What is the rationale for the design choices that you have made?