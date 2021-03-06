package com.annabelle.potionflasks.regeneratingflask;

import com.annabelle.potionflasks.ItemRegistry;
import com.annabelle.potionflasks.config.PotionFlasksCommonConfig;
import com.annabelle.potionflasks.potionflask.PotionFlaskItem;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RegeneratingPotionFlaskItem extends PotionFlaskItem {
    public RegeneratingPotionFlaskItem(Properties p_42979_) {
        super(p_42979_);
    }

    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {

        if(pStack.getTag().getBoolean("potionflask:empty")){
            return pStack;
        }

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
                    if (!pStack.getTag().getBoolean("potionflask:empty")) {
                        pStack.getTag().putBoolean("potionflask:has_been_used", true);
                        pStack.getTag().putInt("potionflasks:fill_level", PotionFlasksCommonConfig.FLASK_MAX_FILL_LEVEL.get() - 1);
                    }
                }
                pStack.getTag().putInt("potionflasks:refill_countdown",0);
            }
        }


        if(!pLevel.isClientSide){


        }

        if (player == null || !player.getAbilities().instabuild) {

            if (pStack.getTag().getInt("potionflasks:fill_level") == 0) {
                pStack.getTag().putBoolean("potionflask:empty", true);
            }
        }


        pLevel.gameEvent(pEntityLiving, GameEvent.DRINKING_FINISH, pEntityLiving.eyeBlockPosition());
        return pStack;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);

        if(pLevel.isClientSide){return;}
        if(pStack.getTag().getInt("potionflasks:fill_level") < PotionFlasksCommonConfig.FLASK_MAX_FILL_LEVEL.get() ||
                pStack.getTag().getBoolean("potionflask:empty")){
            pStack.getTag().putInt("potionflasks:refill_countdown",
                    pStack.getTag().getInt("potionflasks:refill_countdown") + 1);

            if(pStack.getTag().getInt("potionflasks:refill_countdown") >= PotionFlasksCommonConfig.FLASK_REGEN_TIME.get()){
                pStack.getTag().putInt("potionflasks:fill_level",
                        pStack.getTag().getInt("potionflasks:fill_level") + 1);
                pStack.getTag().putInt("potionflasks:refill_countdown",0);
                pStack.getTag().putBoolean("potionflask:empty", false);
            }
        }


    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        //super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        if(!Screen.hasShiftDown()){return;}
        pTooltipComponents.add(new TranslatableComponent("tooltip.potionflasks.regenerating_potion_flask").withStyle(ChatFormatting.GRAY));
    }
}
