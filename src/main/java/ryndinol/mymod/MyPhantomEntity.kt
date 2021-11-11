package ryndinol.mymod

import net.minecraft.client.model.*
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.PhantomEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelPartNames
import net.minecraft.client.render.entity.model.PhantomEntityModel
import net.minecraft.entity.EntityType
import net.minecraft.entity.ai.goal.ActiveTargetGoal
import net.minecraft.entity.ai.goal.LookAtEntityGoal
import net.minecraft.entity.ai.control.MoveControl
import net.minecraft.entity.mob.PhantomEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import io.github.ladysnake.pal.AbilitySource
import io.github.ladysnake.pal.Pal
import net.adriantodt.fallflyinglib.FallFlyingLib

class MyPhantomEntity(entityType: EntityType<out PhantomEntity?>?, world: World?) : PhantomEntity(entityType, world) {
    companion object {
        val TAME_PHANTOM: AbilitySource = Pal.getAbilitySource(MyMod.id("tame_phantom"))
    }
    override fun initGoals() {
        //goalSelector.add(1, StartAttackGoal())
        //goalSelector.add(2, SwoopMovementGoal())
        //goalSelector.add(3, CircleMovementGoal())
        goalSelector.add(6, LookAtEntityGoal(this, PlayerEntity::class.java, 8.0f))
        targetSelector.add(1, ActiveTargetGoal(this, PlayerEntity::class.java, true))
        //targetSelector.add(1, FindTargetGoal())
    }


    override fun getMoveControl(): MoveControl {
        (getVehicle() as? PlayerEntity)?.let {

        }
        return super.getMoveControl()
    }

    val isRidingPlayer: Boolean get() = this.getVehicle() is PlayerEntity

    override fun dismountVehicle() {

        if (!world.isClient) {
            setAiDisabled(false)
            (firstPassenger as? PlayerEntity)?.let {
                Pal.revokeAbility(it, FallFlyingLib.ABILITY, TAME_PHANTOM)
            }
        }
        super.dismountVehicle()
    }

    override fun tickRiding() {
        super.tickRiding()
    }

    override fun tickMovement() {
        if (isRidingPlayer) {
            if (isAffectedByDaylight) {
                dismountVehicle()
                return
            }
        } else {
            super.tickMovement()
        }
    }

    override fun interactMob(player: PlayerEntity?, hand: Hand?): ActionResult {
        if (player != null && hand != null && player.getStackInHand(hand).isEmpty) {
            //if (!hasPassengers()) {
            if (world.isClient) {
            //if (!world.isClient) {

                // Ride phantom
//                player.yaw = yaw
//                player.pitch = pitch
//                if (player.startRiding(this)) {
//                    return ActionResult.CONSUME
//                } else {
//                    return ActionResult.SUCCESS
//                }
                // Phantom rides you!
                yaw = player.yaw
                pitch = player.pitch
            }

            if (this.startRiding(player, true)) {
                if (!world.isClient) {
                    setAiDisabled(true)
                    Pal.grantAbility(player, FallFlyingLib.ABILITY, TAME_PHANTOM)
                }
                return ActionResult.SUCCESS
            }
            //return ActionResult.success(!world.isClient)
            return ActionResult.CONSUME
        }
        return super.interactMob(player, hand)
    }

    fun stickToPlayer(player: PlayerEntity) {
        this.yaw = player.yaw
        if (player.isFallFlying) {
            this.pitch = -player.pitch
        } else {
            this.pitch = 90.0f //-(Math.PI/4.0).toFloat();
        }
    }

}

class MyPhantomEntityRenderer(context: EntityRendererFactory.Context?) : PhantomEntityRenderer(context)

class MyPhantomEntityModel(modelPart: ModelPart?) : PhantomEntityModel<MyPhantomEntity>(modelPart) {
    companion object {
        private val TAIL_BASE = "tail_base"
        private val TAIL_TIP = "tail_tip"
        fun getTexturedModelData(): TexturedModelData {
            val modelData = ModelData()
            val modelPartData = modelData.root
            val modelPartData2 = modelPartData.addChild(
                EntityModelPartNames.BODY,
                ModelPartBuilder.create().uv(0, 8).cuboid(-3.0f, -2.0f, -8.0f, 5.0f, 3.0f, 9.0f),
                ModelTransform.rotation(-0.1f, 0.0f, 0.0f)
            )
            val modelPartData3 = modelPartData2.addChild(
                TAIL_BASE,
                ModelPartBuilder.create().uv(3, 20).cuboid(-2.0f, 0.0f, 0.0f, 3.0f, 2.0f, 6.0f),
                ModelTransform.pivot(0.0f, -2.0f, 1.0f)
            )
            modelPartData3.addChild(
                TAIL_TIP,
                ModelPartBuilder.create().uv(4, 29).cuboid(-1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 6.0f),
                ModelTransform.pivot(0.0f, 0.5f, 6.0f)
            )
            val modelPartData4 = modelPartData2.addChild(
                EntityModelPartNames.LEFT_WING_BASE,
                ModelPartBuilder.create().uv(23, 12).cuboid(0.0f, 0.0f, 0.0f, 6.0f, 2.0f, 9.0f),
                ModelTransform.of(2.0f, -2.0f, -8.0f, 0.0f, 0.0f, 0.1f)
            )
            modelPartData4.addChild(
                EntityModelPartNames.LEFT_WING_TIP,
                ModelPartBuilder.create().uv(16, 24).cuboid(0.0f, 0.0f, 0.0f, 13.0f, 1.0f, 9.0f),
                ModelTransform.of(6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.1f)
            )
            val modelPartData5 = modelPartData2.addChild(
                EntityModelPartNames.RIGHT_WING_BASE,
                ModelPartBuilder.create().uv(23, 12).mirrored().cuboid(-6.0f, 0.0f, 0.0f, 6.0f, 2.0f, 9.0f),
                ModelTransform.of(-3.0f, -2.0f, -8.0f, 0.0f, 0.0f, -0.1f)
            )
            modelPartData5.addChild(
                EntityModelPartNames.RIGHT_WING_TIP,
                ModelPartBuilder.create().uv(16, 24).mirrored().cuboid(-13.0f, 0.0f, 0.0f, 13.0f, 1.0f, 9.0f),
                ModelTransform.of(-6.0f, 0.0f, 0.0f, 0.0f, 0.0f, -0.1f)
            )
            modelPartData2.addChild(
                EntityModelPartNames.HEAD,
                ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -2.0f, -5.0f, 7.0f, 3.0f, 5.0f),
                ModelTransform.of(0.0f, 1.0f, -7.0f, 0.2f, 0.0f, 0.0f)
            )
            return TexturedModelData.of(modelData, 64, 64)
        }
    }
}