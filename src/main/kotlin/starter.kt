import java.lang.System.err
import java.util.*
import kotlin.math.abs

/**
 * Auto-generated code below aims at helping you parse
 * the standard input according to the problem statement.
 **/

data class Location(val x: Int, val y: Int) {
    fun getNeighbours(): List<Location> {
        return listOf(
                Location(x - 1, y),
                Location(x + 1, y),
                Location(x, y - 1),
                Location(x, y + 1)
        ).filter { it.x >= 0 && it.x <= 11 && it.y >= 0 && it.y <= 11 }
    }
}

data class Building(val owner: Int, val buildingType: Int, val x: Int, val y: Int)

data class Cell(val x: Int, val y: Int, val ownership: Int, var piece: Piece? = null) {
    // doesn't account for holes in the map!
    fun distance(other: Cell) = abs(x - other.x) + abs(y - other.y)

    fun distance(xx: Int, yy: Int) = abs(x - xx) + abs(y - yy)
}

data class Piece(val id: Int, val isFriendly: Boolean, val level: Int) {
    fun pieceCost(): Int {
        return StarterUtils.pieceCost(this.level)
    }
}

interface Action {
    val x: Int
    val y: Int
}

data class MoveAction(val id: Int, override val x: Int, override val y: Int) : Action {
    override fun toString() = "MOVE $id $x $y"
}

data class TrainAction(val level: Int, override val x: Int, override val y: Int) : Action {
    override fun toString() = "TRAIN $level $x $y"
}

data class BuildAction(val type: Int, override val x: Int, override val y: Int) : Action {
    override fun toString() = if (type == 1) "BUILD MINE $x $y" else "BUILD TOWER $x $y"
}

class StarterUtils {
    fun calculateCost(pieces: List<Piece>): Int {
        return pieces.map { it.pieceCost() }.sum()
    }

    fun printBoard(board: List<List<Cell>>) {
        board.forEach { y ->
            y.forEach {
                err.printf("%3d", it.ownership)
            }
            err.println()
        }
    }

    companion object {
        fun pieceCost(level: Int): Int {
            return if (level == 1) 1 else if (level == 2) 4 else 20
        }
    }
}

fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val mines = List(input.nextInt()) {
        val x = input.nextInt()
        val y = input.nextInt()
        Location(x, y)
    }

//     TODO calculate Location -> Enemy HQ distance on first move
//     TODO Possibly to mines as well

    val utils = StarterUtils()
    val voidCellValue = 99;
    // game loop
    while (true) {
        val gold = input.nextInt()
        val income = input.nextInt()
        val opponentGold = input.nextInt()
        val opponentIncome = input.nextInt()

        val board: List<List<Cell>> = List(12) { y ->
            input.next().mapIndexed { x: Int, ch ->
                val type = when (ch) {
                    '#' -> voidCellValue
                    '.' -> -1
                    'O' -> 2; 'o' -> 1
                    'X' -> -3; 'x' -> -2
                    else -> throw Exception("Unexpected")
                }
                Cell(x, y, type)
            }
        }

        // Gives us a flat list of the non-void cells
        val boardCells = board.flatten().filterNot { it.ownership == voidCellValue }

        lateinit var myHQ: Cell
        lateinit var enemyHQ: Cell

        val buildingCount = input.nextInt()
        val buildings = mutableListOf<Building>()

        for (i in 0 until buildingCount) {
            val owner = input.nextInt()
            val buildingType = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()
            if (buildingType == 0) {
                // HQ
                val hq = board[y][x]!!
                when (owner) {
                    0 -> myHQ = hq
                    1 -> enemyHQ = hq
                }
            }
            buildings += Building(owner, buildingType, x, y)
        }

        val builtMinesLocations = buildings.filter { it.buildingType == 1 }
                .map { Location(it.x, it.y) }

        repeat(input.nextInt()) {
            val owner = input.nextInt()
            val unitId = input.nextInt()
            val level = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()
            val cell = board[y][x]!!
            cell.piece = Piece(unitId, owner == 0, level)
        }

        val actions = mutableListOf<Action>()
        generateActions(
                boardCells,
                enemyHQ,
                actions,
                builtMinesLocations.toMutableList(),
                gold,
                income,
                mines,
                board
        )

        if (actions.any()) {
            println(actions.joinToString(";"))
        } else {
            println("WAIT")
        }
    }
}

fun generateActions(
        boardCells: List<Cell>,
        enemyHQ: Cell,
        actions: MutableList<Action>,
        builtMines: MutableList<Location>,
        gold: Int,
        income: Int,
        mines: List<Location>,
        board: List<List<Cell>>
) {
//        TODO fix conflict between move and train as well - apply actions to board state
    val myPiecesCells = boardCells.filter { it.piece?.isFriendly == true }
    myPiecesCells.map {
        Pair(it, bestValueMove(it, enemyHQ, boardCells))
    }.forEach {
        actions += MoveAction(it.first.piece!!.id, it.second.x, it.second.y)
        it.first.piece = null
    }

    actions.sortBy { - it.x - it.y }

    val trainingSpots = getAllTrainingSpots(
            boardCells,
            builtMines,
            actions.map { Location(it.x, it.y) }
    )
    useGold(gold, income, builtMines, mines, board, actions, trainingSpots.toMutableList())
}

fun useGold(
        gold: Int,
        income: Int,
        builtMines: MutableList<Location>,
        mines: List<Location>,
        board: List<List<Cell>>,
        actions: MutableList<Action>,
        trainingSpots: MutableList<Location>
) {
    var availableGold = gold;
    var availableIncome = income;
    var consumedTrainingSpots = mutableListOf<Location>()
    var canDoMoreActions = true
    var moveActions = actions.map { Location(it.x, it.y) }
    trainingSpots.sortBy { abs(it.x - it.y) }

    // TODO enable placing minions that can destroy lower level things
    trainingSpots.map { Pair(trainingSpots, board[it.y][it.x].piece) }

    while (availableGold > 0 && availableIncome > 0 && canDoMoreActions) {
        val mineCost = 20 + builtMines.count() * 4

        if (availableGold > mineCost) {
            val possibleMineLocation = mines.filterNot { builtMines.contains(it) }
                    .map { board[it.y][it.x] }
                    .filter { it.piece == null }
                    .filter { it.ownership == 1 || it.ownership == 2 }
                    .filterNot { it.piece != null }
                    .firstOrNull()

            if (possibleMineLocation != null) {
                availableGold -= mineCost
                builtMines.add(Location(possibleMineLocation.x, possibleMineLocation.y))
                actions.add(BuildAction(1, possibleMineLocation.x, possibleMineLocation.y))
            }
        }

        // Exclude spots we've just moved to
        val trainingSpot = trainingSpots.filterNot { consumedTrainingSpots.contains(it) }.firstOrNull()
        if (trainingSpot != null) {
            // 100 & 50 for rank ~100
            val unitLevel = if (availableIncome > 50) 3 else if (availableIncome > 20 ) 2 else 1
            availableIncome = availableIncome - StarterUtils.pieceCost(unitLevel)
            actions += TrainAction(unitLevel, trainingSpot.x, trainingSpot.y)
            consumedTrainingSpots.add(trainingSpot)
        } else {
            canDoMoreActions = false
        }
    }
}

fun bestValueMove(myPiece: Cell, enemyHQ: Cell, flatBoard: List<Cell>): Cell {
    val possibleMoves = flatBoard.filterNot {
        it.ownership > 10
    }.filter {
        (it.x == myPiece.x - 1 && it.y == myPiece.y && it.x > 0) ||
                (it.x == myPiece.x + 1 && it.y == myPiece.y && it.x < 12) ||
                (it.x == myPiece.x && it.y == myPiece.y - 1 && it.y > 0) ||
                (it.x == myPiece.x && it.y == myPiece.y + 1 && it.y < 12)
    }
    return possibleMoves.maxBy {
        val distanceToEnemyHQ = it.distance(enemyHQ)
        val distanceScore = if (distanceToEnemyHQ == 1) -100 else distanceToEnemyHQ
        100 - it.ownership - distanceScore
    }!!
}

fun getAllTrainingSpots(flatBoard: List<Cell>, buildingLocations: List<Location>, moveActions: List<Location>): List<Location> {
    val allNeighborsToOwningCells = flatBoard.filter {
                it.ownership == 1 || it.ownership == 2 || moveActions.contains(Location(it.x, it.y))
            }.map { Location(it.x, it.y) }
            .map { it.getNeighbours() }
            .flatten()

    return flatBoard.filter {
        allNeighborsToOwningCells.contains(Location(it.x, it.y))
    }.filterNot {
        it.ownership > 0 || moveActions.contains(Location(it.x, it.y))
    }.map {
        Location(it.x, it.y)
    }.filterNot { buildingLocations.contains(it) }
}