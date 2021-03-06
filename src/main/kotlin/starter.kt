import java.lang.System.err
import java.util.*
import kotlin.math.abs

data class Location(val x: Int, val y: Int) {
    fun getNeighbours(): List<Location> {
        return listOf(
                Location(x - 1, y),
                Location(x + 1, y),
                Location(x, y - 1),
                Location(x, y + 1)
        ).filter { it.x in 0..11 && it.y in 0..11 }
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
        return pieceCost(this.level)
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
    override fun toString() = when (type) {
        1 -> "BUILD MINE $x $y"
        2 -> "BUILD TOWER $x $y"
        else -> throw Exception("Unexpected")
    }
}

fun printBoard(board: List<List<Cell>>) {
    board.forEach { y ->
        y.forEach {
            err.printf("%3d", it.ownership)
        }
        err.println()
    }
}

fun calculateCost(pieces: List<Piece>) = pieces.map { it.pieceCost() }.sum()

fun pieceCost(level: Int) = if (level == 1) 1 else if (level == 2) 4 else 20

private const val VOID_CELL_VALUE = 99

interface ActionGenerator {
    fun generateActions(
            board: List<List<Cell>>,
            mines: List<Location>,
            buildings: List<Building>,
            myGold: Int,
            myIncome: Int,
            opponentGold: Int,
            opponentIncome: Int
    ): List<Action>
}

class BronzeLeagueActionGenerator: ActionGenerator {
    override fun generateActions(
            board: List<List<Cell>>,
            mines: List<Location>,
            buildings: List<Building>,
            myGold: Int,
            myIncome: Int,
            opponentGold: Int,
            opponentIncome: Int
    ): List<Action> {
        val myHQ = buildings.filter { it.buildingType == 0 && it.owner == 0 }.map { board[it.y][it.x] }.first()
        val opponentHQ = buildings.filter { it.buildingType == 0 && it.owner == 1 }.map { board[it.y][it.x] }.first()
        val builtMinesLocations = buildings.filter { it.buildingType == 1 }
                .map { Location(it.x, it.y) }
        val boardCells = board.flatten().filterNot { it.ownership == VOID_CELL_VALUE }
        val actions = mutableListOf<Action>()

//        TODO fix conflict between move and train as well - apply actions to board state
        boardCells.filter { it.piece?.isFriendly == true }
                .map {
                    Pair(it, bestValueMove(it, opponentHQ, boardCells))
                }.forEach {
                    //        TODO remove actions where the current piece cannot kill opponent
                    actions += MoveAction(it.first.piece!!.id, it.second.x, it.second.y)
                    it.first.piece = null
                }

        actions.sortBy { -it.x - it.y }

        val trainingSpots = getAllTrainingSpots(
                boardCells,
                builtMinesLocations,
                actions.map { Location(it.x, it.y) }
        )
        actions += useGold(myGold, myIncome, builtMinesLocations, mines, board, trainingSpots.toMutableList())
        return actions
    }

    fun useGold(
            gold: Int,
            income: Int,
            builtMines: List<Location>,
            mines: List<Location>,
            board: List<List<Cell>>,
            trainingSpots: MutableList<Location>
    ): List<Action> {
        var availableGold = gold;
        var availableIncome = income;
        val consumedTrainingSpots = mutableListOf<Location>()
        val actions = mutableListOf<Action>()
        val mineLocation = builtMines.toMutableList()
        var canDoMoreActions = true

        trainingSpots.sortBy { abs(it.x - it.y) }

        val trainingSpotWithPieces = trainingSpots.map { Pair(it, board[it.y][it.x].piece) }
                .sortedBy { it.second != null }
                .reversed()

        while (availableGold > 0 && availableIncome > 0 && canDoMoreActions) {
            val mineCost = 20 + mineLocation.count() * 4

            if (availableGold > mineCost) {
                val possibleMineLocation = mines.filterNot { mineLocation.contains(it) }
                        .map { board[it.y][it.x] }
                        .filter { it.piece == null }
                        .filter { it.ownership == 1 || it.ownership == 2 }
                        .filterNot { it.piece != null }
                        .firstOrNull()

                if (possibleMineLocation != null) {
                    availableGold -= mineCost
                    mineLocation += Location(possibleMineLocation.x, possibleMineLocation.y)
                    actions += BuildAction(1, possibleMineLocation.x, possibleMineLocation.y)
                }
            }

            // Exclude spots we've just moved to
            val trainingSpot = trainingSpotWithPieces.filterNot { consumedTrainingSpots.contains(it.first) }.firstOrNull()
            if (trainingSpot != null) {
                // 100 & 50 for rank ~100
                if (trainingSpot.second == null) {
                    val unitLevel = if (availableIncome > 50) 3 else if (availableIncome > 20) 2 else 1
                    availableIncome -= pieceCost(unitLevel)
                    actions += TrainAction(unitLevel, trainingSpot.first.x, trainingSpot.first.y)
                } else {
                    if (trainingSpot.second!!.level == 1) {
                        availableIncome -= pieceCost(2)
                        actions += TrainAction(2, trainingSpot.first.x, trainingSpot.first.y)
                    } else {
                        availableIncome -= pieceCost(3)
                        actions += TrainAction(3, trainingSpot.first.x, trainingSpot.first.y)
                    }
                }
                consumedTrainingSpots.add(trainingSpot.first)
            } else {
                canDoMoreActions = false
            }
        }
        return actions
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

    // game loop
    while (true) {
        val startTime = System.currentTimeMillis();

        val gold = input.nextInt()
        val income = input.nextInt()
        val opponentGold = input.nextInt()
        val opponentIncome = input.nextInt()

        val board: List<List<Cell>> = List(12) { y ->
            input.next().mapIndexed { x: Int, ch ->
                val type = when (ch) {
                    '#' -> VOID_CELL_VALUE
                    '.' -> -1
                    'O' -> 2; 'o' -> 1
                    'X' -> -3; 'x' -> -2
                    else -> throw Exception("Unexpected")
                }
                Cell(x, y, type)
            }
        }

        val buildingCount = input.nextInt()
        val buildings = mutableListOf<Building>()

        for (i in 0 until buildingCount) {
            val owner = input.nextInt()
            val buildingType = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()

            buildings += Building(owner, buildingType, x, y)
        }

        repeat(input.nextInt()) {
            val owner = input.nextInt()
            val unitId = input.nextInt()
            val level = input.nextInt()
            val x = input.nextInt()
            val y = input.nextInt()
            val cell = board[y][x]!!
            cell.piece = Piece(unitId, owner == 0, level)
        }

        val actionGenerator = BronzeLeagueActionGenerator()
        val actions = actionGenerator.generateActions(
                board,
                mines,
                buildings.toList(),
                gold,
                income,
                opponentGold,
                opponentIncome
        )

        val endTime = System.currentTimeMillis()
        if (actions.any()) {
            print(actions.joinToString(";"))
            val timeTaken = endTime - startTime
            println(";MSG $timeTaken")
        } else {
            println("WAIT;")
        }

    }
}

