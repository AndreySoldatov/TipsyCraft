package net.ansol.tipsycraft.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Fertilizable
import net.minecraft.block.ShapeContext
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.registry.tag.BlockTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.IntProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView

abstract class HopsVineBlockBase(settings: Settings) :
    Block(settings
        .noCollision()
        .nonOpaque()
        .breakInstantly()
        .sounds(BlockSoundGroup.CAVE_VINES)
        .pistonBehavior(PistonBehavior.DESTROY)
        .ticksRandomly()
    ), Fertilizable
{
    /**
     * Data class defining per-varietal behaviour
     */
    data class HopsVineVarietalSpec(
        val startMeanVigor: Int = 5,
        val vigorDispersion: Int = 2,
        val spreadChance: Double = 0.25,
        val ageTickChance: Double = 0.25,
        val verticalGrowthProbability: Double = 0.5,
        val seedLootProbability: Double = 0.2,
        val maxAdditionalFlowersLoot: Int = 2,
        val mutationWeights: Map<String, Int> = emptyMap(),
    )

    abstract val spec: HopsVineVarietalSpec
    abstract val seedItem: Item
    abstract val flowerItem: Item

    companion object {
        const val MAX_VIGOR = 16

        val AGE: IntProperty = Properties.AGE_3
        val VIGOR: IntProperty = IntProperty.of("vigor", 0, MAX_VIGOR)
        val FACING: EnumProperty<Direction> = Properties.HORIZONTAL_FACING

        const val MAX_AGE = 3

        val RIPE_OUTLINE_SHAPES: Map<Direction, VoxelShape> = mapOf(
            Direction.EAST to createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0),
            Direction.WEST to createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            Direction.SOUTH to createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0),
            Direction.NORTH to createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0)
        )

        val UNRIPE_OUTLINE_SHAPES: Map<Direction, VoxelShape> = mapOf(
            Direction.EAST to createCuboidShape(0.0, 0.0, 0.0, 2.0, 16.0, 16.0),
            Direction.WEST to createCuboidShape(14.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            Direction.SOUTH to createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 2.0),
            Direction.NORTH to createCuboidShape(0.0, 0.0, 14.0, 16.0, 16.0, 16.0)
        )

        private fun isSameVine(p1: BlockPos, p2: BlockPos, world: WorldView): Boolean {
            val b1 = world.getBlockState(p1)
            val b2 = world.getBlockState(p2)
            return b1.isOf(b2.block)
        }

        private fun isOnDirt(world: WorldView, pos: BlockPos): Boolean {
            return world.getBlockState(pos.down()).isIn(BlockTags.DIRT)
        }

        private fun diagonals(pos: BlockPos): List<BlockPos> {
            return listOf(
                pos.offset(Direction.EAST).offset(Direction.NORTH),
                pos.offset(Direction.EAST).offset(Direction.SOUTH),
                pos.offset(Direction.WEST).offset(Direction.NORTH),
                pos.offset(Direction.WEST).offset(Direction.SOUTH)
            )
        }

        const val MAX_NODES = 2048
        private fun hasOnDirtBlockInNet(world: WorldView, startPos: BlockPos): Boolean {
            val queue = ArrayDeque<BlockPos>()
            val visited = HashSet<BlockPos>()

            fun tryAdd(p: BlockPos) {
                if(visited.add(p)) queue.add(p)
            }

            tryAdd(startPos)
            while (queue.isNotEmpty()) {
                val p = queue.removeFirst()

                val s = world.getBlockState(p)

                if (isOnDirt(world, p)) return true

                val testPositions = listOf(
                    Direction.entries
                        .filter { direction -> direction != s[FACING] && direction != s[FACING].opposite }
                        .map { direction -> p.offset(direction) },
                    diagonals(p)
                ).flatten()
                for (np in testPositions) {
                    if (isSameVine(p, np, world)) tryAdd(np)
                }

                if(visited.size >= MAX_NODES) break
            }
            return false
        }

        private fun againstFullWall(world: WorldView, pos: BlockPos, direction: Direction): Boolean {
            val supportBlockPos = pos.offset(direction.opposite)
            return world.getBlockState(supportBlockPos).isSideSolidFullSquare(world, supportBlockPos, direction)
        }
    }

    init {
        defaultState = stateManager.defaultState
            .with(AGE, 0)
            .with(FACING, Direction.NORTH)
            .with(VIGOR, 0)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(AGE, FACING, VIGOR)
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return if (state[AGE] < MAX_AGE) UNRIPE_OUTLINE_SHAPES[state[FACING]]!! else RIPE_OUTLINE_SHAPES[state[FACING]]!!
    }

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        return isOnDirt(world, pos) && againstFullWall(world, pos, state[FACING])
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val side = ctx.side

        if (!side.axis.isHorizontal) return null

        val worldView = ctx.world
        val targetState = worldView.getBlockState(ctx.blockPos)
        if (!targetState.canReplace(ctx)) return null

        if(!isOnDirt(worldView, ctx.blockPos)) return null

        if(!againstFullWall(worldView, ctx.blockPos, side)) return null

        val startVigor = (spec.startMeanVigor + (worldView.random.nextInt(spec.vigorDispersion))).coerceAtMost(MAX_VIGOR)

        return stateManager.defaultState
            .with(AGE, 0)
            .with(FACING, ctx.side)
            .with(VIGOR, startVigor)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: WorldAccess,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        if (!world.isClient) {
            if (!hasOnDirtBlockInNet(world, pos)) {
                world.scheduleBlockTick(pos, this, 1)
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        for (diagonal in diagonals(pos)) {
            if (isSameVine(diagonal, pos, world)) {
                if (!hasOnDirtBlockInNet(world, diagonal)) {
                    world.scheduleBlockTick(diagonal, this, 1)
                }
            }
        }

        dropStacks(state, world, pos)
        world.breakBlock(pos, false)
    }

    override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (state[AGE] < MAX_AGE && random.nextFloat() < spec.ageTickChance) {
            world.setBlockState(pos, state.with(AGE, state[AGE] + 1), NOTIFY_LISTENERS)
        }

        if (state[VIGOR] > 0 && random.nextFloat() < spec.spreadChance) {
            trySpread(state, world, pos, random)
        }
    }

    private fun getPossibleSpreadPositions(state: BlockState, world: WorldView, pos: BlockPos): List<Pair<BlockPos, Direction>> {
        val adjacentPositions: List<Pair<BlockPos, Direction>> = Direction.entries
            .filter { direction ->
                direction != state[FACING] &&
                direction != state[FACING].opposite &&
                direction != Direction.UP
            }
            .map { direction -> Pair(pos.offset(direction), state[FACING]) }

        // diagonals
        val cw  = state[FACING].rotateYClockwise()
        val ccw = state[FACING].rotateYCounterclockwise()
        val cwp  = pos.offset(state[FACING].opposite).offset(cw)
        val ccwp = pos.offset(state[FACING].opposite).offset(ccw)

        val possiblePositions =
            (adjacentPositions + listOf(Pair(cwp, cw), Pair(ccwp, ccw)))
                .filter { (pos, direction) ->
                    againstFullWall(world, pos, direction) &&
                    world.getBlockState(pos).isAir
                }

        return possiblePositions
    }

    private fun trySpread(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        if (state[VIGOR] <= 0) return

        if (random.nextFloat() < spec.verticalGrowthProbability) {
            val targetPos = pos.offset(Direction.UP)
            if (world.getBlockState(targetPos).isAir && againstFullWall(world, targetPos, state[FACING])) {
                spread(state, world, pos, targetPos, state[FACING])
            }
        } else {
            val possiblePositions = getPossibleSpreadPositions(state, world, pos)

            if (!possiblePositions.isEmpty()) {
                val target = possiblePositions[random.nextInt(possiblePositions.size)]
                spread(state, world, pos, target.first, target.second)
            }
        }
    }

    private fun spread(state: BlockState, world: ServerWorld, pos: BlockPos, targetPos: BlockPos, targetFacing: Direction) {
        val child = defaultState
            .with(FACING, targetFacing)
            .with(AGE, 0)
            .with(VIGOR, state[VIGOR] - 1)
        world.setBlockState(targetPos, child, NOTIFY_ALL)

        world.setBlockState(pos,
            state.with(VIGOR, state[VIGOR] - 1))

        world.playSound(
            null, targetPos,
            net.minecraft.sound.SoundEvents.BLOCK_AZALEA_HIT,
            net.minecraft.sound.SoundCategory.BLOCKS,
            1.0f, 1.0f
        )
    }

    override fun isFertilizable(world: WorldView, pos: BlockPos, state: BlockState): Boolean {
        val canAge = state[AGE] < MAX_AGE
        val canSpread = state[VIGOR] > 0 && getPossibleSpreadPositions(state, world, pos).isNotEmpty()
        return canAge || canSpread
    }

    override fun canGrow(world: World, random: Random, pos: BlockPos, state: BlockState): Boolean = true

    override fun grow(world: ServerWorld, random: Random, pos: BlockPos, state: BlockState) {
        if (state[AGE] < MAX_AGE) {
            val newAge = (state[AGE] + 1 + random.nextInt(3)).coerceAtMost(MAX_AGE)
            world.setBlockState(pos, state.with(AGE, newAge), NOTIFY_LISTENERS)
        }

        if (state[VIGOR] > 0) {
            repeat(1 + random.nextInt(3)) {
                trySpread(state, world, pos, random)
            }
        }
    }
}