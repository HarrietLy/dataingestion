# Data Ingestion via Spring Batch

### Below is the prerequisite installed software to run the project in local computer
1. Java 21
2. IntelliJ (optional for viewing the code)

### 💡 Description
This is a CLI application that can be used to ingest large files in NEM12 format. The command line can then be run by a job orchestrator platform.

### 🛠️ Setup Instructions
#### ✅ Step 1. Git Clone the Project to your local machine.
#### ✅ Step 2. Execute the following on your terminal
- cd <repo-folder-in-your-local>
- ./mvnw clean install 
- java -jar target/data-0.0.1-SNAPSHOT.jar classpath:nem12example
#### ✅ Step 3. To release to production, adding in path to the file as argument to the command line like below
- ./mvnw clean install in deploy script to build the jar in the production server
- java -jar target/data-0.0.1-SNAPSHOT.jar **put in filepath here**

### 🛠️ Answers to Questions
1. Q1. What is the rationale for the technologies you have decided to use?
Since the nem12 meter reating file potentially contain millions of rows or more (tracking daily meter readings of many mater readers). It can be a very big file, hence we need a tool that
can help perform data ingestion of a large file without sacrificing ingestion speed. Furthermore, large data is bound to have some dirty records
that need to be handled, without impacting the rest of non-dirty data.
Spring Batch is a mature Spring library that offers both speed and resilence to a data ingestion job:
- First, Spring Batch processes the file line by line, instead of loading the entire file to memory, keeping the memory
usage low.
- Second, Spring Batch saves its execuation context in its various execution tables, hence if a job fails halfway, 
it can be restarted and start right where it left off instead of from the beginning, saving time and computing power.
- Third, data is written in chunks. If a record insertion fails, only current chunk is rolled back, not the whole job,
keeping previous chunks of data that were successfully inserted.

Please note that for development purpose, H2 in-memory database is used. For production, another database should be used such as Postgres, MySQL, etc.
2. Q2. What would you have done differently if you had more time?
With more time, I would write automated tests to ensure core functionality is preserved with future enhancements.
I would also log out bad records that cause failure in read/process or write into a separate table. This table would be helpful for
debugging ingestion failure and detect any file corruption.

3. Q3. What is the rationale for the design choices that you have made?
To extend on points in Q1, Spring Batch logic for read/process/write is below:
- read: read line by line. For line that starts with 300, if required, we will stitch multiple lines together 
till we encounter quality flag which signals end of the readings of that 300 record. The reason is that as observed from
example file, 300 record can be split into multiple lines
process: since we are only inserting into meter_readings table, hence we ignore all non-300 and non-200 records.
200record get the current nmi, following 300 records will be of the same nmi. Within each 300 record, nem12 format specifies that all readings 
will come in chronological order, hence we can use the index of the reading to calculate the timestamp of the reading.
One 300 records is expected to churn out a list of 48 meter reading objects
- writer: we use JdbcBatchItemWriter to do efficient batch insertion of list of meter reading objects into database