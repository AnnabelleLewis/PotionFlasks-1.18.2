package com.annabelle.potionflasks.potionflask;

import com.annabelle.potionflasks.ItemRegistry;
import com.annabelle.potionflasks.config.PotionFlasksCommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PotionFlaskItem extends PotionItem {
    private static final int DRINK_DURATION = 16;


    public PotionFlaskItem(Properties p_42979_) {
        super(p_42979_);
    }

    public static int getMaxFillLevel(){return PotionFlasksCommonConfig.FLASK_MAX_FILL_LEVEL.get();}

    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
        Player player = pEntityLiving instanceof Player ? (Player)pEntityLiving : null;
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, pStack);
        }

        if (!pLevel.isClientSide) {
            for(MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(pStack)) {
                if (mobeffectinstance.getEffect().isInstantenous()) {
                    mobeffectinstance.getEffect().applyInstantenousEffect(player, player, pEntityLiving, mobeffectinstance.getAmplifier(), 1.0D);
                } else {
                    pEntityLiving.addEffect(new MobEffectInstance(mobeffectinstance));
                }
            }
        }

        // TODO: Reduce flask fill level

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                if(pStack.getTag().getInt("potionflasks:fill_level") != 0){
                    pStack.getTag().putInt("potionflasks:fill_level",
                            pStack.getTag().getInt("potionflasks:fill_level") - 1);
                } else {
                    pStack.getTag().putInt("potionflasks:fill_level", PotionFlasksCommonConfig.FLASK_MAX_FILL_LEVEL.get() - 1);
                }
            }
        }


        if (player == null || !player.getAbilities().instabuild) {

            if (pStack.getTag().getInt("potionflasks:fill_level") == 0) {
                return new ItemStack(ItemRegistry.EMPTY_POTION_FLASK.get());
            }
        }

        pLevel.gameEvent(pEntityLiving, GameEvent.DRINKING_FINISH, pEntityLiving.eyeBlockPosition());
        return pStack;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if(!Screen.hasShiftDown()){return;}
        pTooltipComponents.add(new TranslatableComponent("tooltip.potionflasks.potion_flask").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return pStack.getTag().getInt("potionflasks:fill_level") != 0;
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        return (int)(((float)pStack.getTag().getInt("potionflasks:fill_level")/PotionFlasksCommonConfig.FLASK_MAX_FILL_LEVEL.get()) * 13);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        float stackMaxDamage = this.getMaxDamage(pStack);
        float f = Math.max(0.0F, (float)pStack.getTag().getInt("potionflasks:fill_level") / (float)PotionFlasksCommonConfig.FLASK_MAX_FILL_LEVEL.get());
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }


}
