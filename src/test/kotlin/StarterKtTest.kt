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

        assertThat(
                "There should be 2 trainig spots on an empty board",
                getAllTrainingSpots(testBoard.flatten(), emptyList()),
                hasSize(2)
        )

        assertThat(
                "There should be 1 trainig spot - 1,0 is a building location",
                getAllTrainingSpots(testBoard.flatten(), singletonList(Location(1,0))),
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
                "There should be 2 trainig spots - 1,0 is a building location, 2,0 is void",
                getAllTrainingSpots(testBoard.flatten(), emptyList()),
                containsInAnyOrder(
                        Location(0,1),
                        Location(1,1)
                )
        )
    }

    @Test
    internal fun test_nearby_location() {
        assertThat("There are two neighbours",
                Location(0, 0).getNeighbours(),
                containsInAnyOrder(
                        Location(1,0),
                        Location(0, 1)
                )
        )

        assertThat("There are two neighbours",
                Location(11, 11).getNeighbours(),
                containsInAnyOrder(
                        Location(11,10),
                        Location(10, 11)
                )
        )

        assertThat("There are two neighbours",
                Location(5, 5).getNeighbours(),
                containsInAnyOrder(
                        Location(5,4),
                        Location(5,6),
                        Location(4,5),
                        Location(6, 5)
                )
        )
    }

    @Test
    internal fun test_use_gold() {
        val testBoard = GameData.emptyBoard()
        val actions = mutableListOf<IAction>()
        useGold(
                7,
                0,
                mutableListOf(),
                emptyList(),
                testBoard,
                actions,
                listOf(Location(0, 1), Location(1,0))
        )
        assertThat(
                "There are two train actions",
                actions,
                anyOf(
                        hasItem(TrainAction(1, 1, 0)),
                        hasItem(TrainAction(1, 0, 1))
                )
        )

        useGold(
                20,
                1,
                mutableListOf(),
                emptyList(),
                testBoard,
                actions,
                listOf(Location(0, 1), Location(1,0))
        )
        assertThat(
                "There are two train actions",
                actions,
                anyOf(
                        hasItem(TrainAction(1, 1, 0)),
                        hasItem(TrainAction(1, 0, 1))
                )
        )
    }

    @Test
    fun thisShouldCompile() {
        val myList = Arrays.asList("a", "b", "c")
        assertThat("List doesn't contain unexpected elements", myList, not(anyOf(hasItem("d"), hasItem("e"), hasItem("f"))))
    }
}
