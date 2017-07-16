package app

import core.ElevatorControlSystemModel._
import core.{ElevatorControlSystem, SimpleElevatorsController}
import monocle.macros.syntax.lens._
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom
import scalatags.JsDom.all._

object MainHall extends JSApp {

  val stepTimeMs = 400
  val floors     = 9

  @JSExport
  def main(b: html.Body): Unit = {
    val elevators = new SimpleElevatorsController
    elevators.genInitialState(elevatorsCount = 5)

    renderContent(b, elevators)
    dom.window.setInterval(() => {
      elevators.step()
      renderContent(b, elevators)
    }, stepTimeMs)
  }

  private def renderContent(body: html.Body, elevatorControlSystem: ElevatorControlSystem): Unit = {
    body.innerHTML = ""

    val columns = elevatorControlSystem.status().length + 1

    def cellDiv(string: JsDom.Modifier) = cellDivColumns(string, columns)
    def cellDivColumns(string: JsDom.Modifier, columnsP: Int) =
      div(`class` := s"pure-u-1-$columnsP l-box", p(string))

    type Cells = Vector[GridCell]
    type Tag   = JsDom.TypedTag[HTMLElement]

    sealed trait GridCell {
      def view(floor: Int): Tag
    }

    case class FirstColumn(floor: Int) extends GridCell {

      def view(floor: Int): Tag = {
        def selectionColor(d: Direction): String = {
          val maybeRequest = elevatorControlSystem.getPickupRequests.find(_.floor.value == floor)
          if (maybeRequest.exists(_.direction == d))
            "pure-button pure-button-primary"
          else
            "pure-button"
        }

        def pickup(direction: Direction) =
          elevatorControlSystem.pickup(PickupRequest(Floor(floor), direction))

        cellDiv(if (floor > 0) {
          div(
            floor.toString,
            button("up", onclick := { () =>
              pickup(Up)
            }, `class` := selectionColor(Up)),
            button("down", onclick := { () =>
              pickup(Down)
            }, `class` := selectionColor(Down))
          )
        } else {
          s"step: ${elevatorControlSystem.getStepCounter}"
        })
      }
    }

    case class ElevatorColumn(elevator: Elevator) extends GridCell {
      def view(floor: Int): Tag =
        cellDiv(if (elevator.state.floor.value == floor) {
          if (elevator.state.door == Opened) "[...]" else "[]"
        } else {
          ""
        })
    }

    case class ElevatorButtons(elevator: Elevator) extends GridCell {
      def view(floor: Int): Tag = {
        def selectionColor(f: Int): String = {
          if (elevator.state.target.exists(_.value == f))
            "pure-button button-xlarge pure-button-primary"
          else
            "pure-button button-xlarge"
        }

        def go(f: Int) =
          elevatorControlSystem.update(elevator.lens(_.state.target).modify(_ + Floor(f)))

        cellDiv(
          (1 to floors).iterator
            .map(i =>
              cellDivColumns(button(i.toString, onclick := { () =>
                go(i)
              }, `class` := selectionColor(i)), 3))
            .toList)
      }
    }

    def headerCells: Cells =
      FirstColumn(0) +: elevatorControlSystem.status().map(e => ElevatorButtons(e))

    def rowCells(floor: Int): Cells =
      FirstColumn(floor) +: elevatorControlSystem.status().map(e => ElevatorColumn(e))

    val filler: Vector[Tag] =
      headerCells.map(value => value.view(0)) ++
        (1 to floors).iterator
          .map(row => rowCells(row).map(cell => cell.view(row)))
          .flatten

    body.appendChild(
      div(
        `class` := "container",
        div(
          `class` := "pure-g",
          filler
        )
      ).render
    )
  }

  override def main(): Unit = {}
}
