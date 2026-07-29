package com.chimera.item;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.chimera.entity.WebHookEntity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Byproduct economy work order Milestone 3b: crafted from Chitin Resin. Fires a WebHookEntity -
// see that class for the flight/reel behavior. Reusable (durability), consistent with the
// Adrenaline Draught correction earlier this work order.
public class WebSlingerItem extends Item {

    // Tracks each player's most recently fired hook, for left-click retract - mirrors the
    // per-player Map<UUID, ...> shape GeneEffectHandlers already uses for Raging Bull/Ramming
    // Charge cooldowns. Only one active hook per player is meaningful, so a new throw naturally
    // overwrites any prior entry; a stale/already-discarded reference is a harmless no-op.
    private static final Map<UUID, WebHookEntity> ACTIVE_HOOKS = new ConcurrentHashMap<>();

    public WebSlingerItem(Properties properties) {
        super(properties);
    }

    // One hook at a time per player, matching the fishing rod's own right-click-again-to-reel-in
    // convention - right-clicking with an active hook already out retracts it (no durability
    // cost, no throw sound) instead of firing another, which is what let a player previously
    // spam out an unbounded number of hooks at once.
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide) {
            WebHookEntity existing = ACTIVE_HOOKS.get(player.getUUID());
            if (existing != null && existing.isAlive()) {
                retractActiveHook(player);
                return InteractionResultHolder.success(stack);
            }

            WebHookEntity hook = new WebHookEntity(player, level);
            hook.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 1.0F);
            level.addFreshEntity(hook);
            ACTIVE_HOOKS.put(player.getUUID(), hook);

            EquipmentSlot slot = usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, slot);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // Byproduct economy work order Milestone 3b: server-side handler for RetractWebSlingerPayload.
    public static void retractActiveHook(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        WebHookEntity hook = ACTIVE_HOOKS.remove(player.getUUID());
        if (hook != null && hook.isAlive()) {
            hook.discard();
        }
    }
}
