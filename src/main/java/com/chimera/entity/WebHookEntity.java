package com.chimera.entity;

import com.chimera.ChimeraEntityTypes;
import com.chimera.ChimeraItems;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

// Byproduct economy work order Milestone 3b: the Web Slinger's thrown hook, the mod's first
// custom entity. Flies normally (ThrowableProjectile's own tick() handles gravity/collision)
// until it hits something, then switches to a custom multi-tick "reel" instead of discarding -
// pulling the owner toward a hit block, or a hit entity toward the owner - by nudging velocity
// via Entity#push each tick.
//
// push() alone (hasImpulse) is enough for a mob: ServerEntity#sendChanges broadcasts the motion
// to every *observing* client, and mobs are server-authoritative anyway. It is NOT enough for
// the entity's own controlling player - confirmed the hard way (diagnostic logging showed
// velocity growing every tick while position never moved): sendChanges only sends the motion
// packet to the owning connection itself when Entity#hurtMarked is set (a separate flag from
// hasImpulse), via a distinct broadcastAndSend call at the end of that method. So every push()
// on a LivingEntity here is followed by hurtMarked = true as well, whether the pulled entity
// ends up being a mob or the owning player - harmless either way, required for the player case.
public class WebHookEntity extends ThrowableItemProjectile {

    private static final double PULL_STRENGTH = 0.2;
    private static final double ARRIVAL_THRESHOLD = 1.5;
    private static final int MAX_STUCK_TICKS = 100;
    private static final double MAX_RANGE = 25.0;

    private BlockPos stuckBlockPos;
    private Entity hookedEntity;
    private int stuckTicks;
    private Vec3 spawnPos;

    public WebHookEntity(EntityType<? extends WebHookEntity> type, Level level) {
        super(type, level);
    }

    public WebHookEntity(LivingEntity owner, Level level) {
        super(ChimeraEntityTypes.WEB_HOOK.get(), owner, level);
        this.spawnPos = this.position();
    }

    @Override
    protected Item getDefaultItem() {
        return ChimeraItems.WEB_SLINGER.get();
    }

    @Override
    public void tick() {
        if (stuckBlockPos != null || hookedEntity != null) {
            reelTick();
            return;
        }
        // Range cap is server-only (like reelTick()'s real logic) rather than checked on both
        // sides - spawnPos is only known on the side that actually threw the hook (the
        // (LivingEntity, Level) constructor; the client's own copy is built via the plain
        // (EntityType, Level) factory constructor with no spawnPos at all) and discard()
        // already syncs entity removal to clients automatically, so there's nothing the client
        // needs to compute here itself.
        if (!level().isClientSide && spawnPos != null && position().distanceTo(spawnPos) > MAX_RANGE) {
            discard();
            return;
        }
        super.tick();
    }

    // Deliberately NOT gated by level().isClientSide - tick() runs independently on both sides
    // (the client has its own local copy of this entity), and the client needs to recognize
    // "I'm stuck" itself too, or it just keeps simulating flight/gravity forever regardless of
    // what the server-side entity does, which is exactly the "hook falls after impact" bug
    // this fixes: the client-side copy was hitting the block every tick, never freezing, and
    // never stopping its own local gravity simulation.
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (stuckBlockPos == null && hookedEntity == null) {
            stuckBlockPos = result.getBlockPos();
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (stuckBlockPos == null && hookedEntity == null && result.getEntity() != getOwner()) {
            hookedEntity = result.getEntity();
            setDeltaMovement(Vec3.ZERO);
        }
    }

    // Not vanilla tick() flight/collision anymore once stuck - this drives the hook's whole
    // post-impact behavior instead. Discards on arrival, on a dead/gone target, on a missing
    // owner (e.g. disconnected), or after MAX_STUCK_TICKS as a failsafe against a hook that can
    // never actually reach its target (mirrors FishingHook's own max-age failsafe). The client
    // only needs to stay frozen (handled by tick() never calling super.tick() again once stuck)
    // - the actual pull is server-authoritative only.
    private void reelTick() {
        if (level().isClientSide) {
            return;
        }
        stuckTicks++;
        Entity owner = getOwner();
        if (owner == null || stuckTicks > MAX_STUCK_TICKS) {
            discard();
            return;
        }

        if (stuckBlockPos != null) {
            Vec3 toTarget = Vec3.atCenterOf(stuckBlockPos).subtract(owner.position());
            if (toTarget.length() < ARRIVAL_THRESHOLD) {
                discard();
                return;
            }
            Vec3 pull = toTarget.normalize().scale(PULL_STRENGTH);
            owner.push(pull.x, pull.y, pull.z);
            owner.hurtMarked = true;
            owner.resetFallDistance();
        } else if (hookedEntity != null) {
            if (!hookedEntity.isAlive()) {
                discard();
                return;
            }
            Vec3 toOwner = owner.position().subtract(hookedEntity.position());
            if (toOwner.length() < ARRIVAL_THRESHOLD) {
                discard();
                return;
            }
            Vec3 pull = toOwner.normalize().scale(PULL_STRENGTH);
            hookedEntity.push(pull.x, pull.y, pull.z);
            hookedEntity.hurtMarked = true;
        }
    }
}
