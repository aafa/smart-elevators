import scala.collection.mutable

object ElevatorControlSystem {
  sealed trait Direction
  case object Up extends Direction
  case object Down extends Direction

  case class Floor(value: Int) extends AnyVal
  case class ElevatorId(value: Int) extends AnyVal
  case class ElevatorState(current: Floor, target: Floor)
  case class PickupRequest(floor: Floor, d: Direction)
  type ElevatorStateMap = Map[ElevatorId, ElevatorState]
}

import ElevatorControlSystem._

trait ElevatorControlSystem {
  def status(): ElevatorStateMap
  def update(state: ElevatorStateMap): Unit
  def pickup(pr: PickupRequest): Unit
  def step(): Unit
}

class NaiveElevators extends ElevatorControlSystem {
  private[this] var stepCounter: Int = 0
  private[this] val pickupRequest = mutable.Queue.empty[PickupRequest]
  private[this] val state = mutable.HashMap[ElevatorId, ElevatorState] (
    ElevatorId(1) -> ElevatorState(Floor(1), Floor(2)),
    ElevatorId(2) -> ElevatorState(Floor(0), Floor(0))
  )

  override def status(): ElevatorStateMap = state.toMap

  override def update(updatedState: ElevatorStateMap): Unit =
    updatedState.foreach {
      case (id: ElevatorId, targetState: ElevatorState) =>
        if (state.contains(id)) {
          state(id) = targetState
        } else {
          state.put(id, targetState)
        }
    }

  override def pickup(pr: PickupRequest): Unit = pickupRequest.enqueue(pr)

  override def step(): Unit = {
    stepCounter += 1
    // do some step logic here, moving elevators etc
  }
}
