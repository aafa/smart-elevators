import monocle.macros.Lenses
import scala.collection.immutable
import monocle.macros.syntax.lens._
import monocle.function.At.at
import monocle.std.map._
import monocle.syntax.ApplyLens

object ElevatorControlSystemModel {

  @Lenses
  case class Model(
      stepCounter: Int = 0,
      pickupRequests: Set[PickupRequest] = Set.empty,
      elevatorsSystemState: ElevatorControlState = immutable.Map.empty
  ) {

    def elevatorById(id: ElevatorId): ApplyLens[Model, Model, Option[Elevator], Option[Elevator]] = {
      this.lens(_.elevatorsSystemState) composeLens at(id)
    }
  }

  sealed trait Direction
  case object Up   extends Direction
  case object Down extends Direction
  case object Idle extends Direction

  sealed trait DoorsState
  case object Opened extends DoorsState
  case object Closed extends DoorsState

  case class Floor(value: Int)      extends AnyVal
  case class ElevatorId(value: Int) extends AnyVal

  // elevators tend to keep direction, we want to reflect that
  case class ElevatorState(floor: Floor,
                           target: Set[Floor],
                           door: DoorsState = Closed,
                           direction: Direction = Idle) {
    def updateFloor(increment: Int): ElevatorState = {
      val targetFloor = Floor(floor.value + increment)
      require(targetFloor.value >= 0) // assume non-negative floors for now
      this.copy(floor = targetFloor)
    }
    def addTarget(floor: Floor): ElevatorState =
      this.copy(target = target + floor)
    def removeTarget(floor: Floor): ElevatorState =
      this.copy(target = target - floor)
  }

  // todo we prob want to restrict out Idle direction here
  case class PickupRequest(floor: Floor,
                           direction: Direction,
                           pendingFor: Option[ElevatorId] = None)
  type ElevatorControlState = Map[ElevatorId, Elevator]

  @Lenses
  case class Elevator(id: ElevatorId, state: ElevatorState) {
    def isIdle: Boolean = state.direction == Idle && state.target.isEmpty

    def getTargets: Set[Floor] = this.state.target
    def modifyTargets(update: Set[Floor] => Set[Floor]): Elevator = {
      val upd = this.lens(_.state.target).modify(update)

      if (upd.state.target.isEmpty) {
        upd.lens(_.state.direction).set(Idle)
      } else {
        upd
          .lens(_.state.direction)
          .set(calculateDirection(upd.state.target.head)) // todo better strategy
      }
    }

    def calculateDirection(f: Floor): ElevatorControlSystemModel.Direction =
      if (f.value < this.state.floor.value) {
        Down
      } else
        Up
  }

  object Elevator {
    def apply(tuple: (ElevatorId, ElevatorState)): Elevator =
      Elevator(tuple._1, tuple._2)
    def apply(id: Int, floor: Int, target: Int*): Elevator =
      Elevator(ElevatorId(id), elState(floor, target: _*))
  }

  implicit val floorOrdering: Ordering[Floor]       = Ordering.by(_.value)
  implicit val prOrdering: Ordering[PickupRequest]  = Ordering.by(_.floor)
  implicit val elevatorOrdering: Ordering[Elevator] = Ordering.by(_.id.value)

  def elState(floor: Int, target: Int*): ElevatorState =
    ElevatorState(Floor(floor), target.map(Floor).to[Set])

}
