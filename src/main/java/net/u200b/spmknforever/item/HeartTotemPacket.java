package net.u200b.spmknforever.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.u200b.spmknforever.SpMknForever;

public class HeartTotemPacket {
    public static void sendTotemAnimation(PlayerEntity player) {
        if (player.getWorld().isClient()) {
            // Отправляем пакет на клиент для отображения анимации
            triggerTotemAnimation(player, new ItemStack(SpMknForever.HEART_TOTEM));
        }
    }

    @Environment(EnvType.CLIENT)
    public static void triggerTotemAnimation(PlayerEntity player, ItemStack itemStack) {
        // Клиентская логика для анимации
        MinecraftClient.getInstance().gameRenderer.showFloatingItem(itemStack); // Анимация вращения предмета
        for (int i = 0; i < 64; ++i) {
            player.getWorld().addParticle(ParticleTypes.TOTEM_OF_UNDYING, player.getX() + (player.getWorld().getRandom().nextDouble() - 0.5D) * 2.0D, player.getY() + 0.5D + player.getWorld().getRandom().nextDouble(), player.getZ() + (player.getWorld().getRandom().nextDouble() - 0.5D) * 2.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}

