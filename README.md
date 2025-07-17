Course Search Application
This is a Spring Boot application that provides a course search API using Elasticsearch. It allows searching, filtering, and sorting courses based on various criteria.

Table of Contents
Prerequisites

Setup and Running

1. Launch Elasticsearch

2. Build and Run the Spring Boot Application

3. Data Loading

API Endpoints

Search Courses (GET /api/search)

Bonus Features (Autocomplete & Fuzzy Search)

Prerequisites
Before you begin, ensure you have the following installed:

Java Development Kit (JDK) 17 or higher

Apache Maven (or mvnd for faster builds)

Docker and Docker Compose (for running Elasticsearch)

Elasticsearch 8.13.4 (or compatible version, as configured in ElasticsearchConfig.java)

Setup and Running

1. Launch Elasticsearch
   The application connects to an Elasticsearch instance. You can easily run Elasticsearch using Docker Compose.

Create a docker-compose.yml file in your project's root directory (or a separate docker directory) with the following content:

version: '3.8'

services:
elasticsearch:
image: docker.elastic.co/elasticsearch/elasticsearch:8.13.4
container_name: elasticsearch
environment: - xpack.security.enabled=false # Disable security for simplicity in development - discovery.type=single-node - "ES_JAVA_OPTS=-Xms512m -Xmx512m" # Adjust memory as needed
ports: - "9200:9200" # HTTP port - "9300:9300" # Transport port
volumes: - esdata:/usr/share/elasticsearch/data # Persist data
ulimits:
memlock:
soft: -1
hard: -1
healthcheck:
test: ["CMD-SHELL", "curl -f http://localhost:9200/_cat/health?h=st || exit 1"]
interval: 10s
timeout: 10s
retries: 5

volumes:
esdata:
driver: local

Start Elasticsearch in detached mode:

docker-compose up -d

Wait a few moments for Elasticsearch to start up completely. You can check its health with curl http://localhost:9200/\_cat/health.

2. Build and Run the Spring Boot Application
   Navigate to the project root directory in your terminal:

cd /path/to/your/coursesearch/project

Clean and build the project: This will compile the code, run tests, and package the application into a JAR file.

mvnd clean install

Run the Spring Boot application:

mvnd spring-boot:run

The application will start on http://localhost:8080 by default.

3. Data Loading
   Upon successful startup, the CourseLoader component (defined in src/main/java/com/example/coursesearch/listener/CourseLoader.java) will automatically read the sample-courses.json file (located in src/main/resources/) and index the course data into your running Elasticsearch instance. You should see a message like "Courses indexed into Elasticsearch." in your application logs.

Ensure your sample-courses.json file has content similar to this example:

[
{
"id": "course-1",
"title": "Math Magic",
"description": "This is a detailed course about math magic for students interested in hands-on learning.",
"category": "Literature",
"type": "COURSE",
"gradeRange": "10th–12th",
"minAge": 14,
"maxAge": 16,
"price": 256.63,
"nextSessionDate": "2025-06-21T00:00:00Z"
}
]

Note: The nextSessionDate field must be in ISO 8601 format (e.g., "YYYY-MM-DDTHH:mm:ssZ").

API Endpoints
The application exposes a single REST endpoint for searching courses.

Search Courses (GET /api/search)
Base URL: http://localhost:8080/api/search

Query Parameters:

q (Optional, String): General search query for title and description.

minAge (Optional, Integer): Minimum age for the course.

maxAge (Optional, Integer): Maximum age for the course.

category (Optional, String): Filter by course category (case-sensitive, uses .keyword field in Elasticsearch).

type (Optional, String): Filter by course type (e.g., "COURSE", "ONE_TIME", "CLUB"; case-sensitive, uses .keyword field).

minPrice (Optional, Double): Minimum price for the course.

maxPrice (Optional, Double): Maximum price for the course.

startDate (Optional, Instant): Filter for courses starting on or after this date (ISO 8601 format, e.g., 2025-07-01T00:00:00Z).

sort (Optional, String): Sorting criteria.

upcoming (Default): Sorts by nextSessionDate ascending.

priceAsc: Sorts by price ascending.

priceDesc: Sorts by price descending.

page (Optional, Integer): Page number (default: 0).

size (Optional, Integer): Number of results per page (default: 10).

Example curl Requests:

Search for courses containing "math":

curl "http://localhost:8080/api/search?q=math"

Find courses for age 10-15 in "Science" category:

curl "http://localhost:8080/api/search?minAge=10&maxAge=15&category=Science"

Get all "CLUB" type courses, sorted by price descending:

curl "http://localhost:8080/api/search?type=CLUB&sort=priceDesc"

Find courses starting on or after July 1st, 2025:

curl "http://localhost:8080/api/search?startDate=2025-07-01T00:00:00Z"

Combine search, filters, and pagination:

curl "http://localhost:8080/api/search?q=creative&minPrice=50&maxPrice=100&category=Science&page=0&size=5"

Bonus Features (Autocomplete & Fuzzy Search)
Fuzzy Search
The current implementation of the q parameter uses a multi_match query in Elasticsearch across the title and description fields. Elasticsearch's multi_match query can inherently support a degree of "fuzziness" if configured. While not explicitly set with a fuzziness parameter in CourseSearchService.java, Elasticsearch's default analysis and tokenization often provide a basic level of tolerance for minor typos.

To enable explicit fuzzy search, you would modify the MultiMatchQuery in CourseSearchService.java to include a fuzziness option (e.g., .fuzziness("AUTO")).

Autocomplete
The current API does not have a dedicated autocomplete endpoint. To implement autocomplete functionality, you would typically:

Add a completion suggester field to your Elasticsearch courses index mapping for the fields you want to autocomplete (e.g., title).

Create a new API endpoint (e.g., /api/autocomplete) that accepts a partial query string.

Use Elasticsearch's completion suggester query in this new endpoint to return suggestions based on the partial input.

This would involve modifications to your CourseDocument.java (for mapping), a new method in CourseSearchService.java, and a new endpoint in CourseSearchController.java.
