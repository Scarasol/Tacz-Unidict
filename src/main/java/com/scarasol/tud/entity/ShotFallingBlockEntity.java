package com.scarasol.tud.entity;

import com.scarasol.tud.init.TudEntities;
import com.scarasol.tud.mixin.accessor.FallingBlockEntityAccessor;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Scarasol
 */
public class ShotFallingBlockEntity extends FallingBlockEntity {

    private static final int HURT_COOLDOWN_TICKS = 5;

    /**
     * 将“速度”换算成“等效下落距离”的倍率
     */
    private static final float SPEED_TO_FALL_DISTANCE = 2.0F;

    /**
     * 速度低于这个值不做实体扫描
     */
    private static final float MIN_SPEED_FOR_ENTITY_SCAN = 0.15F;

    /**
     * 扫掠判定膨胀，避免高速穿模漏判
     */
    private static final double SWEEP_INFLATE = 0.10D;

    /**
     * 高速时分段移动，避免一次移动太长导致碰撞解不稳定
     */
    private static final double MAX_STEP_LEN = 2.0D;

    /**
     * 用于“取样撞到的方块”的微小偏移
     */
    private static final double IMPACT_EPS = 1.0E-4D;

    /**
     * 是否发生过方块碰撞
     */
    private boolean collidedBlockThisTick = false;

    /**
     * 是否记录到有效撞击面
     */
    private boolean impactThisTick = false;

    @Nullable
    private BlockPos impactBlockPos = null;

    @Nullable
    private Direction impactFace = null;

    private final List<VoxelShape> blockCollisionShapes = new ArrayList<>(16);

    private final Int2LongOpenHashMap hurtCooldown = new Int2LongOpenHashMap();

    private boolean hurtsEntitiesFlag = false;
    private int fallDamageMaxLocal = 40;
    private float fallDamagePerDistanceLocal = 0.0F;

    public ShotFallingBlockEntity(EntityType<? extends FallingBlockEntity> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.hurtCooldown.defaultReturnValue(Long.MIN_VALUE);
    }

    public static ShotFallingBlockEntity create(Level level, BlockState state) {
        ShotFallingBlockEntity e = new ShotFallingBlockEntity(TudEntities.SHOT_FALLING_BLOCK.get(), level);

        BlockState fixed = state.hasProperty(BlockStateProperties.WATERLOGGED)
                ? state.setValue(BlockStateProperties.WATERLOGGED, false)
                : state;

        ((FallingBlockEntityAccessor) e).tud$setBlockState(fixed);
        e.blocksBuilding = true;

        e.setDeltaMovement(Vec3.ZERO);
        return e;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void setHurtsEntities(float amount, int max) {
        super.setHurtsEntities(amount, max);
        this.hurtsEntitiesFlag = true;
        this.fallDamagePerDistanceLocal = amount;
        this.fallDamageMaxLocal = max;
    }

    /**
     * 只计算方块碰撞，忽略实体碰撞箱（实体不会挡路）
     */
    private Vec3 collideBlocksOnly(Vec3 movement) {
        AABB aabb = this.getBoundingBox();

        this.blockCollisionShapes.clear();
        for (VoxelShape s : this.level().getBlockCollisions(this, aabb.expandTowards(movement))) {
            this.blockCollisionShapes.add(s);
        }

        if (movement.lengthSqr() == 0.0D) {
            return movement;
        }

        return collideBoundingBox(this, movement, aabb, this.level(), this.blockCollisionShapes);
    }

    private void recordImpactFromCollision(Vec3 stepMove, Vec3 allowedMove, AABB oldBB) {
        boolean colX = !Mth.equal(stepMove.x, allowedMove.x);
        boolean colY = !Mth.equal(stepMove.y, allowedMove.y);
        boolean colZ = !Mth.equal(stepMove.z, allowedMove.z);

        if (!(colX || colY || colZ)) {
            return;
        }

        double cutX = colX ? Math.abs(stepMove.x - allowedMove.x) : -1.0D;
        double cutY = colY ? Math.abs(stepMove.y - allowedMove.y) : -1.0D;
        double cutZ = colZ ? Math.abs(stepMove.z - allowedMove.z) : -1.0D;

        Direction face;
        if (cutY >= cutX && cutY >= cutZ) {
            face = stepMove.y < 0.0D ? Direction.UP : Direction.DOWN;
        } else if (cutX >= cutZ) {

            face = stepMove.x > 0.0D ? Direction.WEST : Direction.EAST;
        } else {
            face = stepMove.z > 0.0D ? Direction.NORTH : Direction.SOUTH;
        }

        AABB newBB = oldBB.move(allowedMove);

        double cx = (newBB.minX + newBB.maxX) * 0.5D;
        double cy = (newBB.minY + newBB.maxY) * 0.5D;
        double cz = (newBB.minZ + newBB.maxZ) * 0.5D;

        switch (face) {
            case UP -> cy = newBB.minY - IMPACT_EPS;
            case DOWN -> cy = newBB.maxY + IMPACT_EPS;
            case WEST -> cx = newBB.maxX + IMPACT_EPS;
            case EAST -> cx = newBB.minX - IMPACT_EPS;
            case NORTH -> cz = newBB.maxZ + IMPACT_EPS;
            case SOUTH -> cz = newBB.minZ - IMPACT_EPS;
        }

        this.impactBlockPos = BlockPos.containing(cx, cy, cz);
        this.impactFace = face;
        this.impactThisTick = true;
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        if (type != MoverType.SELF || this.noPhysics || movement.lengthSqr() == 0.0D) {
            super.move(type, movement);
            return;
        }

        Vec3 remain = movement;
        boolean anyColX = false;
        boolean anyColY = false;
        boolean anyColZ = false;

        while (remain.lengthSqr() > 1.0E-12D) {
            double len = remain.length();
            Vec3 step = len > MAX_STEP_LEN ? remain.scale(MAX_STEP_LEN / len) : remain;

            AABB oldBB = this.getBoundingBox();

            Vec3 allowed = this.collideBlocksOnly(step);

            boolean colX = !Mth.equal(step.x, allowed.x);
            boolean colY = !Mth.equal(step.y, allowed.y);
            boolean colZ = !Mth.equal(step.z, allowed.z);

            if (colX || colY || colZ) {
                this.collidedBlockThisTick = true;
                this.recordImpactFromCollision(step, allowed, oldBB);
            }

            this.setPos(this.getX() + allowed.x, this.getY() + allowed.y, this.getZ() + allowed.z);


            if (allowed.y < 0.0D && !(colY && step.y < 0.0D)) {
                this.fallDistance += (float) (-allowed.y);
            }

            anyColX = colX;
            anyColY = colY;
            anyColZ = colZ;

            if (colX || colY || colZ) {
                break;
            }

            remain = remain.subtract(step);
        }

        this.horizontalCollision = anyColX || anyColZ;
        this.verticalCollision = anyColY;
        this.setOnGround(anyColY && movement.y < 0.0D);

        if (anyColX || anyColY || anyColZ) {
            Vec3 dm = this.getDeltaMovement();
            this.setDeltaMovement(
                    anyColX ? 0.0D : dm.x,
                    anyColY ? 0.0D : dm.y,
                    anyColZ ? 0.0D : dm.z
            );
        }

        if (this.onGround()) {
            this.fallDistance = 0.0F;
        }
    }


    private void hurtEntitiesAlongPath(AABB oldBB, Vec3 actualDelta) {
        if (!this.hurtsEntitiesFlag) {
            return;
        }

        float speed = (float) actualDelta.length();
        if (speed < MIN_SPEED_FOR_ENTITY_SCAN && this.fallDistance < 1.0F) {
            return;
        }

        AABB swept = oldBB.expandTowards(actualDelta).inflate(SWEEP_INFLATE);

        Predicate<Entity> predicate = EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE);
        List<Entity> targets = this.level().getEntities(this, swept, predicate);
        if (targets.isEmpty()) {
            return;
        }

        float effectiveDistance = Math.max(this.fallDistance, speed * SPEED_TO_FALL_DISTANCE);

        int i = Mth.ceil(effectiveDistance - 1.0F);
        if (i <= 0) {
            return;
        }

        float damage = (float) Math.min(
                Mth.floor((float) i * this.fallDamagePerDistanceLocal),
                this.fallDamageMaxLocal
        );
        if (damage <= 0.0F) {
            return;
        }

        Block block = this.getBlockState().getBlock();
        DamageSource source = (block instanceof Fallable fallable)
                ? fallable.getFallDamageSource(this)
                : this.damageSources().fallingBlock(this);

        long now = this.level().getGameTime();
        boolean didHurt = false;

        for (Entity e : targets) {
            if (!e.getBoundingBox().intersects(swept)) {
                continue;
            }

            int id = e.getId();
            long last = this.hurtCooldown.get(id);
            if (now - last < HURT_COOLDOWN_TICKS) {
                continue;
            }

            this.hurtCooldown.put(id, now);
            e.hurt(source, damage);
            didHurt = true;
        }

        if (this.hurtCooldown.size() > 256) {
            this.hurtCooldown.clear();
        }

        if (didHurt && this.getBlockState().is(BlockTags.ANVIL)) {
            if (this.random.nextFloat() < 0.05F + (float) i * 0.05F) {
                BlockState damaged = AnvilBlock.damage(this.getBlockState());
                if (damaged == null) {
                    this.disableDrop();
                } else {
                    ((FallingBlockEntityAccessor) (Object) this).tud$setBlockState(damaged);
                }
            }
        }
    }

    @Override
    public void tick() {
        this.collidedBlockThisTick = false;
        this.impactThisTick = false;
        this.impactBlockPos = null;
        this.impactFace = null;

        BlockState currentState = this.getBlockState();
        if (currentState.isAir()) {
            this.discard();
            return;
        }

        Block block = currentState.getBlock();
        ++this.time;

        AABB oldBB = this.getBoundingBox();
        Vec3 oldPos = this.position();

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
        }

        Vec3 preMoveDelta = this.getDeltaMovement();
        this.move(MoverType.SELF, preMoveDelta);

        if (!this.level().isClientSide) {
            Vec3 actualDelta = this.position().subtract(oldPos);

            this.hurtEntitiesAlongPath(oldBB, actualDelta);

            BlockPos blockpos = this.blockPosition();

            boolean isConcretePowder = this.getBlockState().getBlock() instanceof ConcretePowderBlock;
            boolean hydrated = isConcretePowder && this.getBlockState().canBeHydrated(
                    this.level(), blockpos, this.level().getFluidState(blockpos), blockpos
            );

            double speedSqr = preMoveDelta.lengthSqr();
            if (isConcretePowder && speedSqr > 1.0D) {
                BlockHitResult waterCheck = this.level().clip(new ClipContext(
                        oldPos,
                        oldPos.add(preMoveDelta),
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.SOURCE_ONLY,
                        this
                ));
                if (waterCheck.getType() != HitResult.Type.MISS &&
                        this.getBlockState().canBeHydrated(this.level(), blockpos,
                                this.level().getFluidState(waterCheck.getBlockPos()), waterCheck.getBlockPos())) {
                    blockpos = waterCheck.getBlockPos();
                    hydrated = true;
                }
            }

            boolean collidedBlock = hydrated
                    || this.onGround()
                    || this.horizontalCollision
                    || this.verticalCollision
                    || this.collidedBlockThisTick;

            if (!collidedBlock) {
                if (this.time > 100) {
                    if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        this.spawnAtLocation(block);
                    }
                    this.discard();
                }
            } else {
                BlockPos placePos;

                if (hydrated) {
                    placePos = blockpos;
                }
                else if (this.impactThisTick && this.impactBlockPos != null && this.impactFace != null) {
                    placePos = this.impactBlockPos.relative(this.impactFace);
                }
                else {
                    BlockHitResult hit = this.level().clip(new ClipContext(
                            oldPos,
                            oldPos.add(preMoveDelta),
                            ClipContext.Block.COLLIDER,
                            ClipContext.Fluid.NONE,
                            this
                    ));
                    if (hit.getType() != HitResult.Type.MISS) {
                        placePos = hit.getBlockPos().relative(hit.getDirection());
                    } else {
                        placePos = blockpos;
                    }
                }

                BlockState stateAtPlace = this.level().getBlockState(placePos);

                this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));

                if (!stateAtPlace.is(net.minecraft.world.level.block.Blocks.MOVING_PISTON)) {
                    boolean cancelDrop = ((FallingBlockEntityAccessor) (Object) this).tud$getCancelDrop();

                    if (!cancelDrop) {
                        boolean canReplace = stateAtPlace.canBeReplaced(
                                new DirectionalPlaceContext(this.level(), placePos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
                        );

                        if (canReplace) {
                            BlockState falling = this.getBlockState();

                            if (falling.hasProperty(BlockStateProperties.WATERLOGGED)
                                    && this.level().getFluidState(placePos).getType() == Fluids.WATER) {
                                falling = falling.setValue(BlockStateProperties.WATERLOGGED, true);
                                ((FallingBlockEntityAccessor) (Object) this).tud$setBlockState(falling);
                            }

                            if (this.level().setBlock(placePos, falling, 3)) {
                                ((ServerLevel) this.level()).getChunkSource().chunkMap.broadcast(
                                        this, new ClientboundBlockUpdatePacket(placePos, this.level().getBlockState(placePos))
                                );

                                this.discard();

                                if (block instanceof Fallable fallable) {
                                    fallable.onLand(this.level(), placePos, this.getBlockState(), stateAtPlace, this);
                                }

                                if (this.blockData != null && this.getBlockState().hasBlockEntity()) {
                                    BlockEntity be = this.level().getBlockEntity(placePos);
                                    if (be != null) {
                                        CompoundTag merged = be.saveWithoutMetadata();
                                        for (String k : this.blockData.getAllKeys()) {
                                            merged.put(k, this.blockData.get(k).copy());
                                        }
                                        try {
                                            be.load(merged);
                                        } catch (Exception ignored) {
                                        }
                                        be.setChanged();
                                    }
                                }
                            } else {
                                if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                    this.discard();
                                    this.callOnBrokenAfterFall(block, placePos);
                                    this.spawnAtLocation(block);
                                } else {
                                    this.discard();
                                }
                            }
                        } else {
                            this.discard();
                            if (this.dropItem && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                                this.callOnBrokenAfterFall(block, placePos);
                                this.spawnAtLocation(block);
                            }
                        }
                    } else {
                        this.discard();
                        this.callOnBrokenAfterFall(block, placePos);
                    }
                }
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
    }
}
