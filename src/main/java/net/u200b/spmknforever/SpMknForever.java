package net.u200b.spmknforever;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.particle.TotemParticle;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.u200b.spmknforever.item.HeartTotemItem;
import net.u200b.spmknforever.item.HeartTotemPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpMknForever implements ModInitializer {
	public static final String MOD_ID = "sp-mkn-forever";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Item HEART_TOTEM = new HeartTotemItem(new Item.Settings().maxCount(1));

	@Override
	public void onInitialize() {
		registerItems();

		// Подписываемся на событие смерти существа
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
			if (entity instanceof PlayerEntity player) {
				LOGGER.info("Player death event triggered for: " + player.getName().getString());

				ItemStack totemStack = findTotemInInventory(player);

				if (totemStack != null) {
					LOGGER.info("Totem found in inventory.");
					// Активируем тотем
					activateTotem(player, damageSource);
					totemStack.decrement(1); // Убираем предмет из инвентаря после использования
					return false; // Блокируем смерть
				}
			}
			return true; // Позволяем смерти произойти, если тотем не найден
		});
	}

	public static void registerItems() {
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "heart_totem"), HEART_TOTEM);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
				.register((itemGroup) -> itemGroup.add(HEART_TOTEM));
	}

	private static ItemStack findTotemInInventory(PlayerEntity player) {
		for (ItemStack stack : player.getInventory().main) {
			LOGGER.info("Checking item: " + stack.getItem().toString());
			if (stack.getItem() == HEART_TOTEM) {
				LOGGER.info("Heart Totem found.");
				return stack;
			}
		}
		LOGGER.info("Heart Totem not found.");
		return null;
	}

	private static void activateTotem(PlayerEntity player, DamageSource damageSource) {
		if (!player.getWorld().isClient()) {
			// Серверная логика
			player.setHealth(1.0F);
			player.clearStatusEffects();
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 900, 1));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 800, 0));

			// Посылаем пакет на клиент
			HeartTotemPacket.sendTotemAnimation(player);

			// Эффекты и звуки
			((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1, player.getZ(), 10, 0.0, 0.5, 0.0, 0.1);
			((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 1.5, player.getZ(), 10, 0.0, 0.5, 0.0, 0.1);
			player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
		}
	}
}
