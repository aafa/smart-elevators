import ElevatorControlSystemModel._
import org.scalatest._
import monocle.macros.syntax.lens._

class ElevatorSystemSpec extends FreeSpecLike with Matchers {
  "Should work in a simple scenarios" - {
    val elevators = new SimpleElevatorsController
    elevators.update(Elevator(1, 0, 0))
    elevators.update(Elevator(2, 0))

    def elevatorStates = elevators.status().map(_.state)

    def steps(count: Int) = {
      (1 to count).iterator.foreach(_ => elevators.step())
//      Thread.sleep(100) // if async effects
    }

    "should report their status" in {
      elevators.status().map(_.id) should contain(ElevatorId(1))
      elevators.status().map(_.id) should contain(ElevatorId(2))
      elevators.status().map(_.id) should not contain (ElevatorId(4))
    }

    "should step 10 steps forward" in {
      elevators.getStepCounter should be(0)
      steps(10)
      elevators.getStepCounter should be(10)
    }

    "should update duplicated elevator" in {
      println(elevators.status())

      elevators.update(Elevator(1, 1))

      elevators.status().size should be(2)
      val maybeElevator = elevators.status().find(_.id == ElevatorId(1))
      println(maybeElevator)
      assert(maybeElevator.exists(_.state.floor == Floor(1)))
    }

    "should pickup on simple request" in {
      assert(elevators.status().map(_.state).forall(_.direction == Idle))
      elevators.pickup(PickupRequest(Floor(5), direction = Down))
      steps(1)

      info("elevators.status " + elevators.status())
      elevators.getPickupRequests.map(_.pendingFor) should contain(Some(ElevatorId(1)))
      elevators.status().map(_.state.direction) should contain(Up)

      steps(5)
      info("elevators.status " + elevators.status())
      info("pickupRequests " + elevators.getPickupRequests)
      assert(elevators.status().map(_.state.floor).exists(_.value == 5),
             "should be at the requested floor")

      steps(1)
      info("pickupRequests " + elevators.getPickupRequests)
      val elevators1 = elevators.status().find(_.state.floor.value == 5)

      assert(elevators1.isDefined, "someone should stop at 5")
      assert(elevators1.get.state.target.isEmpty, "someone should stop at 5 No targets")
      assert(elevators1.get.state.direction == Idle, "someone should stop at 5 Idle")
      assert(elevators1.get.state.door == Closed, "someone should stop at 5 and Closed")
      assert(elevators.getPickupRequests.isEmpty, "PR should be satisfied")
    }

    "should serve pickup in the same direction" in {
      elevators.update(Elevator(1, 5, 1)) // going from 5 to 1
      steps(1)                            // give elevator time to react

      elevators.pickup(PickupRequest(Floor(3), direction = Down)) // pickup
      info("elevators.status " + elevatorStates)
      steps(1)

      elevatorStates.map(_.floor.value) should contain(4)

      steps(1)
      elevatorStates.map(_.floor.value) should contain(3)

      steps(1)
      elevatorStates.map(_.floor.value) should contain(3)
      elevatorStates.map(_.door) should contain(Opened)

      steps(1)
      elevatorStates.map(_.floor.value) should contain(3)
      elevatorStates.map(_.door) should not contain (Opened)
      elevators.getPickupRequests should be(empty)

      steps(3) // 2 floors + open doors
      elevatorStates.map(_.floor.value) should contain(1)
      elevatorStates.map(_.door) should contain(Opened)

      steps(1)
      elevatorStates.map(_.door) should not contain (Opened)
      info("elevators.status " + elevatorStates)
    }

    "should not serve pickup in the wrong direction" in {
      elevators.update(Elevator(1, 5, 1)) // going from 5 to 1
      elevators.update(Elevator(2, 0))    // this one should be triggered for PR
      steps(1)

      elevators.pickup(PickupRequest(Floor(3), direction = Up)) // pickup

      steps(1)
      elevatorStates.map(_.direction) should be(List(Down, Up))

      steps(2)
      elevatorStates.map(_.floor.value) should be(List(2, 3))

      steps(1)
      elevatorStates.map(_.door).last should be(Opened) // picking up with el #2

      steps(1)
      info("elevators.status " + elevatorStates)
      elevatorStates.map(_.door).last should be(Closed) // el #2
      elevatorStates.map(_.door).head should be(Opened) // open el #1

      steps(1)
      info("elevators.status " + elevatorStates)
      elevatorStates.map(_.door) should be(List(Closed, Closed))
      elevatorStates.map(_.direction) should be(List(Idle, Idle))

    }
  }
}
