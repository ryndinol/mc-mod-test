package ryndinol.mymod.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import ryndinol.mymod.MyPhantomEntity;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void updatePassengerPosition(Entity passenger) {
        if (passenger instanceof MyPhantomEntity phantom) {
            phantom.stickToPlayer((PlayerEntity)(Object)this);
        }
        super.updatePassengerPosition(passenger);
    }

}
