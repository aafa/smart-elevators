package core

import core.ElevatorControlSystemModel._

trait ElevatorControlSystem {
  def status(): Vector[Elevator]
  def update(state: ElevatorControlState): Unit
  def update(elevator: Elevator): Unit
  def pickup(pr: PickupRequest): Unit
  def step(): Unit
  def getStepCounter: Int
  def getPickupRequests: Set[PickupRequest]
}

class SimpleElevatorsController extends ElevatorControlSystem {
  import monocle.macros.syntax.lens._
  val groundFloor = 1

  @volatile var model = Model()

  override def status(): Vector[Elevator] =
    model.elevatorsSystemState.values.toVector

  override def update(updatedState: ElevatorControlState): Unit =
    updatedState.foreach { case (_, elevator) => update(elevator) }

  override def pickup(pr: PickupRequest): Unit = {
    val processedPr = processNewPR(pr)
    updateModel(_.lens(_.pickupRequests).modify(_ + processedPr))

    val maybeElevatorToPickup = processedPr.pendingFor flatMap (model.elevatorById(_).get)
    maybeElevatorToPickup.foreach(
      elevator =>
        updateModel(
          _.elevatorById(elevator.id).set(
            Some(startServingPickupRequest(elevator, pr))
          )))
  }

  override def step(): Unit = {
    updateModel(_.lens(_.stepCounter).modify(_ + 1))

    updateModel(
      _.lens(_.elevatorsSystemState)
        .modify(_.map {
          case (id, elevator) => (id, stepAction(elevator))
        }))

    updateModel(_.lens(_.pickupRequests).modify(filterOutPrs))
  }

  def getStepCounter: Int = model.stepCounter

  def getPickupRequests: Set[PickupRequest] = model.pickupRequests

  def genInitialState(elevatorsCount: Int): Unit =
    (1 to elevatorsCount).iterator.foreach(i => {
      this.update(Elevator(i, groundFloor))
    })

  def filterOutPrs(pr: Set[PickupRequest]): Set[PickupRequest] = {
    val opened = model.elevatorsSystemState.values.toList.filter(_.state.door == Opened)
    pr.filterNot(v => opened.map(_.state.floor).contains(v.floor))
  }

  override def update(elevator: Elevator): Unit = {
    updateModel(
      _.elevatorById(elevator.id)
        .set(Some(elevator))
    )
  }

  private def stepAction(elevator: Elevator): Elevator = {
    val currentFloor = elevator.lens(_.state.floor)
    val targetFloors = elevator.getTargets

    elevator.state.door match {
      case Opened =>
        elevator
          .lens(_.state.door)
          .set(Closed)
          .modifyTargets(_ - currentFloor.get)
      case Closed =>
        if (targetFloors.contains(currentFloor.get)) {
          elevator.lens(_.state.door).set(Opened)
        } else if (targetFloors.isEmpty) {
          elevator.lens(_.state.direction).set(Idle)
        } else
          elevator.state.direction match {
            case Up =>
              currentFloor.modify(v => Floor(v.value + 1))
            case Down =>
              currentFloor.modify(v => Floor(v.value - 1))
            case Idle =>
              findTargets(elevator)
          }
    }
  }

  // assign elevator to a pr
  // set elevator's target
  private def startServingPickupRequest(elevator: Elevator, pr: PickupRequest): Elevator = {

    println(s"startServingPickupRequest $pr with $elevator")
    elevator
      .modifyTargets(_ + pr.floor)
      .lens(_.state.direction)
      .set(elevator.calculateDirection(pr.floor))
  }

  private def processNewPR(pr: PickupRequest): PickupRequest = {
    val elevators = model.elevatorsSystemState
    val sameDirection =
      elevators.values
        .filter(d => d.state.direction == pr.direction)
        .filter(
          elevator =>
            model.pickupRequests
              .filter(_.pendingFor.exists(_ == elevator.id))
              .exists(_.direction == pr.direction))
        .toList

    // todo backup plan / enqueue !!
    def process(closestOne: Option[Elevator]) = {
      val anyone = closestOne orElse elevators.values
        .filter(_.isIdle)
        .toVector
        .sortBy(v => Math.abs(pr.floor.value - v.state.floor.value))
        .headOption
      pr.lens(_.pendingFor).set(anyone.map(_.id))
    }

    pr.direction match {
      case Up =>
        val closestOne = sameDirection
          .filter(_.state.floor.value <= pr.floor.value)
          .sortBy(pr.floor.value - _.state.floor.value)
          .headOption
        process(closestOne)
      case Down =>
        val closestOne = sameDirection
          .filter(_.state.floor.value >= pr.floor.value)
          .sortBy(_.state.floor.value - pr.floor.value)
          .headOption
        process(closestOne)
      case Idle => pr
    }
  }

  private def findTargets(elevator: Elevator): Elevator = {
    if (elevator.getTargets.nonEmpty) {
      val destination = elevator.getTargets.head // todo better destination strategy
      elevator.lens(_.state.direction).set(elevator.calculateDirection(destination))
    } else {
      model.pickupRequests
        .find(_.pendingFor.isEmpty)
        .map(pr => elevator.lens(_.state.target).modify(_ + pr.floor))
        .getOrElse(elevator)
      // todo inform pr?
    }
  }

  private def updateModel(updatedModel: Model => Model): Unit =
    model = updatedModel(model)
}
