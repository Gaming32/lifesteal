package io.github.gaming32.lifesteal.mixin;

import com.mojang.authlib.GameProfile;
import io.github.gaming32.lifesteal.Lifesteal;
import io.github.gaming32.lifesteal.LifestealUtil;
import io.github.gaming32.lifesteal.ext.ServerPlayerExt;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements ServerPlayerExt {
    @Unique
    private int ls$livesGain;

    public MixinServerPlayer(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Override
    public int ls$getLivesGain() {
        return ls$livesGain;
    }

    @Override
    public void ls$setLivesGain(int gain) {
        ls$livesGain = LifestealUtil.clamp(gain, Lifesteal.CONFIG.getLives());
        final AttributeInstance attribute = getAttribute(Attributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(Lifesteal.HEALTH_MODIFIER_ID);
            if (ls$livesGain != 0) {
                attribute.addPermanentModifier(new AttributeModifier(
                    Lifesteal.HEALTH_MODIFIER_ID, "Lifesteal health",
                    2.0 * ls$livesGain, AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }
}
