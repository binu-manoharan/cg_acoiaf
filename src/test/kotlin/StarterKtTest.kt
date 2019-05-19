import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import java.util.Collections.singletonList
import java.util.Arrays

internal class StarterKtTest {
    private val starterUtils = StarterUtils()

    @Test
    internal fun piece_cost_should_be_1() {
        val level1 = starterUtils.calculateCost(singletonList(Piece(1, true, 1)));
        val level2 = starterUtils.calculateCost(singletonList(Piece(1, true, 2)));
        val level3 = starterUtils.calculateCost(singletonList(Piece(1, true, 3)));
        assertThat("Piece cost is 1", level1, `is`(1))
        assertThat("Piece cost is 4", level2, `is`(4))
        assertThat("Piece cost is 20", level3, `is`(20))

        val allThree = starterUtils.calculateCost(
                Arrays.asList(
                        Piece(1, true, 1),
                        Piece(1, true, 2),
                        Piece(1, true, 3)
                )
        )
        assertThat("Total piece cost is 25", allThree, `is`(25))
    }

    @Test
    internal fun test_bit_count() {
        val startTime = System.currentTimeMillis()

        val count = (2047..4192256).filter { Integer.bitCount(it) == 11 }.count()
        println(count)
        val endTime = System.currentTimeMillis()
        println(endTime - startTime)
    }

    @Test
    internal fun print_empty_board() {
        starterUtils.printBoard(GameData.emptyBoard())
    }

    @Test
    internal fun move_value() {
        val testBoard = GameData.emptyBoard()
        val myPiece = testBoard.get(10).get(10)
        val enemyHQ = testBoard.get(11).get(11)

        starterUtils.printBoard(testBoard)
        val flatBoard = testBoard.flatten()

        val bestValueMove = bestValueMove(myPiece, enemyHQ, flatBoard)
        println("best move $bestValueMove")
    }

    @Test
    internal fun all_training_spots() {
        val testBoard = GameData.emptyBoard().toMutableList()
        val moveActions = mutableListOf<Location>()
        assertThat(
                "There should be 2 training spots on an empty board",
                getAllTrainingSpots(testBoard.flatten(), emptyList(), moveActions),
                hasSize(2)
        )

        assertThat(
                "There should be 1 training spot - 1,0 is a building location",
                getAllTrainingSpots(testBoard.flatten(), singletonList(Location(1, 0)), moveActions),
                hasSize(1)
        )

        testBoard[0] = listOf(
                Cell(0, 0, 2, null),
                Cell(1, 0, 2, null),
                Cell(2, 0, 99, null),
                Cell(3, 0, 0, null),
                Cell(4, 0, 0, null),
                Cell(5, 0, 0, null),
                Cell(6, 0, 0, null),
                Cell(7, 0, 0, null),
                Cell(8, 0, 0, null),
                Cell(9, 0, 0, null),
                Cell(10, 0, 0, null),
                Cell(11, 0, 0, null)
        )
        assertThat(
                "There should be 2 training spots - 1,0 is a building location, 2,0 is void",
                getAllTrainingSpots(testBoard.flatten(), emptyList(), moveActions),
                containsInAnyOrder(
                        Location(0, 1),
                        Location(1, 1)
                )
        )
    }

    @Test
    internal fun all_training_spots_with_move_action() {
        val testBoard = GameData.emptyBoard().toMutableList()
        val moveActions = mutableListOf<Location>()
        moveActions += Location(1, 0)
        val allTrainingSpots10 = getAllTrainingSpots(testBoard.flatten(), emptyList(), moveActions)
        assertThat(
                "The training spots should be 2,0 0,1 and 1,1",
                allTrainingSpots10,
                allOf(
                        hasItem(Location(2, 0)),
                        hasItem(Location(1, 1)),
                        hasItem(Location(0, 1))
                )
        )
        assertThat(allTrainingSpots10, hasSize(3))

//        assertThat(
//                "There should be 1 training spot - 1,0 is a building location",
//                getAllTrainingSpots(testBoard.flatten(), singletonList(Location(1, 0)), moveActions),
//                hasSize(1)
//        )
//
//        testBoard[0] = listOf(
//                Cell(0, 0, 2, null),
//                Cell(1, 0, 2, null),
//                Cell(2, 0, 99, null),
//                Cell(3, 0, 0, null),
//                Cell(4, 0, 0, null),
//                Cell(5, 0, 0, null),
//                Cell(6, 0, 0, null),
//                Cell(7, 0, 0, null),
//                Cell(8, 0, 0, null),
//                Cell(9, 0, 0, null),
//                Cell(10, 0, 0, null),
//                Cell(11, 0, 0, null)
//        )
//        assertThat(
//                "There should be 2 training spots - 1,0 is a building location, 2,0 is void",
//                getAllTrainingSpots(testBoard.flatten(), emptyList(), moveActions),
//                containsInAnyOrder(
//                        Location(0, 1),
//                        Location(1, 1)
//                )
//        )
    }

    @Test
    internal fun test_nearby_location() {
        assertThat("There are two neighbours",
                Location(0, 0).getNeighbours(),
                containsInAnyOrder(
                        Location(1, 0),
                        Location(0, 1)
                )
        )

        assertThat("There are two neighbours",
                Location(11, 11).getNeighbours(),
                containsInAnyOrder(
                        Location(11, 10),
                        Location(10, 11)
                )
        )

        assertThat("There are four neighbours",
                Location(5, 5).getNeighbours(),
                containsInAnyOrder(
                        Location(5, 4),
                        Location(5, 6),
                        Location(4, 5),
                        Location(6, 5)
                )
        )
    }

    @Test
    internal fun test_use_gold_creates_troops() {
        val testBoard = GameData.emptyBoard()
        val actions = mutableListOf<Action>()
        useGold(
                7,
                2,
                mutableListOf(),
                emptyList(),
                testBoard,
                actions,
                mutableListOf(Location(0, 1), Location(1, 0))
        )
        assertThat(
                actions,
                anyOf(
                        hasItem(TrainAction(1, 1, 0)),
                        hasItem(TrainAction(1, 0, 1))
                )
        )
        assertThat(actions, hasSize(2))
        actions.clear()
        useGold(
                20,
                2,
                mutableListOf(),
                emptyList(),
                testBoard,
                actions,
                mutableListOf(Location(0, 1), Location(1, 0))
        )
        assertThat(
                actions,
                anyOf(
                        hasItem(TrainAction(1, 1, 0)),
                        hasItem(TrainAction(1, 0, 1))
                )
        )
        assertThat(actions, hasSize(2))
    }

    @Test
    internal fun test_use_gold_creates_mines() {
        val testBoard = GameData.emptyBoard().toMutableList()
        testBoard[0] = listOf(
                Cell(0, 0, 2, null),
                Cell(1, 0, 1, null),
                Cell(2, 0, 99, null),
                Cell(3, 0, 0, null),
                Cell(4, 0, 0, null),
                Cell(5, 0, 0, null),
                Cell(6, 0, 0, null),
                Cell(7, 0, 0, null),
                Cell(8, 0, 0, null),
                Cell(9, 0, 0, null),
                Cell(10, 0, 0, null),
                Cell(11, 0, 0, null)
        )
        val actions = mutableListOf<Action>()
        useGold(
                30,
                2,
                mutableListOf(),
                mutableListOf(Location(1, 0)),
                testBoard,
                actions,
                mutableListOf(Location(0, 1), Location(1, 1))
        )
        assertThat(
                actions,
                allOf(
                        hasItem(BuildAction(1, 1, 0)),
                        hasItem(TrainAction(1, 0, 1)),
                        hasItem(TrainAction(1, 1, 1))
                )
        )
        assertThat(actions, hasSize(3))

        actions.clear()
        useGold(
                161,
                100,
                mutableListOf(),
                mutableListOf(Location(1, 0)),
                testBoard,
                actions,
                mutableListOf(Location(0, 1), Location(1, 1))
        )
        assertThat(
                actions,
                allOf(
                        hasItem(BuildAction(1, 1, 0)),
                        hasItem(TrainAction(3, 0, 1)),
                        hasItem(TrainAction(3, 1, 1))
                )
        )
        assertThat(actions, hasSize(3))
    }

    @Test
    internal fun should_not_add_actions_to_same_location() {
        val testBoard = GameData.emptyBoard().toMutableList()
        testBoard[0] = listOf(
                Cell(0, 0, 2, null),
                Cell(1, 0, 1, null),
                Cell(2, 0, 0, null),
                Cell(3, 0, 0, null),
                Cell(4, 0, 0, null),
                Cell(5, 0, 0, null),
                Cell(6, 0, 0, null),
                Cell(7, 0, 0, null),
                Cell(8, 0, 0, null),
                Cell(9, 0, 0, null),
                Cell(10, 0, 0, null),
                Cell(11, 0, 0, null)
        )
        val actions = mutableListOf<Action>()
        actions += MoveAction(1, 2, 0)
        useGold(
                30,
                2,
                mutableListOf(),
                mutableListOf(Location(1, 0)),
                testBoard,
                actions,
                mutableListOf(Location(0, 1), Location(1, 1))
        )
        assertThat(
                actions,
                allOf(
                        hasItem(MoveAction(1, 2, 0)),
                        hasItem(BuildAction(1, 1, 0)),
                        hasItem(TrainAction(1, 0, 1)),
                        hasItem(TrainAction(1, 1, 1))
                )
        )
        assertThat(actions, hasSize(4))
    }

    @Test
    internal fun generate_sequential_move_actions() {
        val testBoard = GameData.emptyBoard().toMutableList()
        testBoard[0] = listOf(
                Cell(0, 0, 2, null),
                Cell(1, 0, 1, Piece(1, true, 1)),
                Cell(2, 0, 1, Piece(2, true, 1)),
                Cell(3, 0, 1, Piece(3, true, 1)),
                Cell(4, 0, 0, null),
                Cell(5, 0, 0, null),
                Cell(6, 0, 0, null),
                Cell(7, 0, 0, null),
                Cell(8, 0, 0, null),
                Cell(9, 0, 0, null),
                Cell(10, 0, 0, null),
                Cell(11, 0, 0, null)
        )
        testBoard[1] = listOf(
                Cell(0, 0, 99, null),
                Cell(1, 0, 99, null),
                Cell(2, 0, 99, null),
                Cell(3, 0, 99, null),
                Cell(4, 0, 99, null),
                Cell(5, 0, 99, null),
                Cell(6, 0, 99, null),
                Cell(7, 0, 99, null),
                Cell(8, 0, 99, null),
                Cell(9, 0, 99, null),
                Cell(10, 0, 99, null),
                Cell(11, 0, 0, null)
        )

        var actions = mutableListOf<Action>()
        generateActions(
                testBoard.flatten(),
                testBoard[11][11],
                actions,
                mutableListOf(),
                0,
                0,
                emptyList(),
                testBoard
        )
        assertThat(actions.get(0) as MoveAction, `is`(MoveAction(3, 4, 0)))
        assertThat(actions.get(1) as MoveAction, `is`(MoveAction(2, 3, 0)))
        assertThat(actions.get(2) as MoveAction, `is`(MoveAction(1, 2, 0)))
    }
}
