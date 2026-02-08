# instructions-capture-service

Spring Boot service that accepts trade “instruction” files (CSV or JSON), processes them, and publishes results to Kafka.

- Repo: `https://github.com/salukooti/instructions-capture-service.git`
- Branch: `dev`
- Sample files: `src/main/resources/samples`

---

## Prerequisites (Local)

Install these locally:

- **Java 21**
- **Maven**
- **Apache Kafka** (local install) running on **localhost:9092**

Kafka topics required:

- `instructions.inbound`
- `instructions.outbound`

Consumer group-id used by the application:

- `instructions-capture`

---

## 1) Checkout the project (dev branch)

```bash
git clone https://github.com/salukooti/instructions-capture-service.git
cd instructions-capture-service
git checkout dev
```

---

## 2) Start Kafka locally and create topics

### Start Zookeeper + Kafka (local install)

Kafka startup commands depend on your Kafka distribution, but commonly:

```bash
# Terminal 1 (Zookeeper)
bin/zookeeper-server-start.sh config/zookeeper.properties

# Terminal 2 (Kafka broker)
bin/kafka-server-start.sh config/server.properties
```

Kafka should be running at:

- `localhost:9092`

### Create topics

```bash
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic instructions.inbound --partitions 3 --replication-factor 1
bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic instructions.outbound --partitions 3 --replication-factor 1
bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

---

## 3) Build the application

You can build using **IntelliJ** or using terminal commands.

### Option A — IntelliJ
1. Open the project in IntelliJ as a Maven project
2. Run Maven goal: `clean install`

### Option B — Terminal

From the repo root:

```bash
mvn clean install
```

---

## 4) Run the application

### Option A — IntelliJ
1. Find the Spring Boot main class (annotated with `@SpringBootApplication`)
2. Click **Run**

### Option B — Terminal

From the repo root:

```bash
mvn spring-boot:run
```

The service should start on:

- `http://localhost:9090`

---

## 5) Test the upload API using Postman

Endpoint:

- `POST http://localhost:9090/api/v1/trades/upload`

### Step-by-step

1. Open **Postman**
2. Create a **New Request**
3. Set method to **POST**
4. URL: `http://localhost:9090/api/v1/trades/upload`
5. Go to **Body**
6. Select **form-data**
7. Add a key named: `file`
8. Change the key type dropdown from **Text** → **File**
9. Choose a file from your local workspace under:  
   `src/main/resources/samples`
   - `sample_instructions.csv`
   - `sample_instructions.json`
10. Click **Send**

---

## 6) Test the upload API using curl

> Update the file path to match where you cloned the repo locally.

### CSV sample

```bash
curl --location 'http://localhost:9090/api/v1/trades/upload' \
  --form 'file=@"/Users/shankar/Projects/Challenge3/src/main/resources/samples/sample_instructions.csv"'
```

### JSON sample

```bash
curl --location 'http://localhost:9090/api/v1/trades/upload' \
  --form 'file=@"/Users/shankar/Projects/Challenge3/src/main/resources/samples/sample_instructions.json"'
```

---

## Notes

- Start **Kafka first**, then run the Spring Boot service.
- Ensure both topics exist before testing uploads.
- The service expects Kafka on `localhost:9092` and the API on `localhost:9090`.
