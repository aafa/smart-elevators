### Smart elevators

Design and implement an elevator control system.

Initial thought process\plans [brainstorm.md]

### Techs that was used
- Plain step-based control system implementation
- Scala.Js UI for demo purposes

### See demo gh-page
.. insert gh-page url here

### Run locally

- `sbt test` to run tests covering basic scenarios 
- `sbt ~fastOptJS` to iterate over Scala.js UI

### Further plans
- Address minor todos\concerns mentioned through the code
- Step-based approach is very simple and straightforward however I'd like to see event-driven design for better interaction handling and more smooth UI flow (listen to events and update only parts that was changed etc.) 