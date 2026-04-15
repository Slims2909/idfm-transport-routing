# Dijkstra Routing Engine — Paris Public Transport Network (IDFM)

## Description

This project implements **Dijkstra's shortest-path algorithm** to compute optimal routes between stations in the Paris Île-de-France Mobilités (IDFM) public transport network. The optimization criterion can be either **travel time** or **distance**.

Data is sourced from the IDFM open API, parsed from CSV files, and modeled as a weighted graph. The system is exposed both as a command-line interface (CLI) and a JavaFX graphical interface.

---

## Architecture

The project follows a five-layer modular architecture:

- **Presentation** — CLI (`RouteDemo`) and GUI (`RouteFinderApp`, `RouteFinderController`)
- **Application** — Entry point and orchestration (`App`)
- **Domain** — Network modeling (`TransportGraph`, `Ligne`, `Station`, `Segment`, `Trajet`)
- **Infrastructure** — Data ingestion and CSV I/O (`IDFMDataDownloader`, `CSVParser`, `CSVTools`)
- **Algorithm** — Graph and pathfinding (`Node`, `Dijkstra`)

---

## Main Classes

### `Station`
Represents a transit stop with an `id`, `name`, `latitude`, and `longitude`.

### `Segment`
Represents a connection between two stations with `departure`, `arrival`, `duration`, and `distance`.

### `GrapheTransport`
Models the transport network as a weighted undirected graph of stations and segments.

### `Dijkstra`
Implements Dijkstra's algorithm to compute the shortest path between two stations. Supports transfer penalties (5-minute connection cost) between different lines.

### `CSVParser` and `CSVStreamProvider`
Handle loading and streaming network data from structured CSV files.

### `IDFMDataDownloader`
Automatically downloads raw data from the IDFM API and formats it into `network.csv`.

### `ScheduleGenerator`
Generates theoretical timetables (`schedules.csv`) with departures every 30 minutes between 7:00 and 9:00 AM.

---

## Features

- Shortest-path computation between any two stations
- Optimization by travel time or distance
- Multi-line support with automatic transfer handling
- Graph statistics (isolated stations, density, connectivity)
- Timetable generation from network data
- JavaFX graphical interface with line colors and step-by-step itinerary display

---

## Usage Example

```java
GrapheTransport graphe = new GrapheTransport();
graphe.loadFromCSVProvider(provider);

Station depart  = new Station("S1", "Station 1", 48.8566, 2.3522);
Station arrivee = new Station("S2", "Station 2", 48.8600, 2.3500);

Map<Station, Double> distances = Dijkstra.calculerChemin(graphe, depart, arrivee, true);
```

---

## Build & Run

**Compile:**
```bash
mvn clean compile
```

**Run the JavaFX interface:**
```bash
mvn javafx:run
```

**Run the CLI:**
```bash
java -cp target/project.jar fr.u_paris.gla.project.idfm.RouteDemo
```

**Download and format IDFM data:**
```bash
java -cp target/project.jar fr.u_paris.gla.project.idfm.IDFMDataDownloader
```

---

## Technologies

- **Language:** Java 17
- **Build tool:** Maven
- **GUI:** JavaFX
- **Data source:** IDFM Open API
- **Algorithms:** Dijkstra, Haversine formula

---

## Project Context

M1 university project — solo — 4 months.
Developed as part of a graduate software engineering course at Université Paris.

---

## Author

**Slims** — [github.com/Slims2909](https://github.com/Slims2909)

---

*Academic project, open-source for educational purposes. © 2025 — Slims2909*
