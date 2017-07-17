### Smart elevators

Design and implement an elevator control system.

Initial thought process\plans [brainstorm.md](brainstorm.md)

### Techs that was used
- Plain step-based control system implementation
- [Monocle](https://github.com/julien-truffaut/Monocle) lib to zoom into model properties
- [Scala.Js](https://www.scala-js.org/) UI for demo purposes

### See demo gh-page
See `Smart elevators` demo here https://aafa.github.io/smart-elevators/index.html

### Run locally

- `sbt test` to run tests covering basic scenarios 
- `sbt ~fastOptJS` to iterate over Scala.js UI

### Further plans
- Address minor todos\concerns mentioned through the code
- Step-based approach is very simple and straightforward however I'd like to see event-driven design for better interaction handling and more smooth UI flow (listen to events and update only parts that was changed etc.) 
