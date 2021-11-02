package ryndinol.mymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(LivingEntity.class)
public abstract class ElytraTravelMixin {
	private boolean useVanillaMechanics  = false;

	//@Inject(method="travel", at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 6), locals = LocalCapture.PRINT)
	@Inject(method="travel", at=@At(value="INVOKE", target="Ljava/lang/Math;min(DD)D"), locals = LocalCapture.CAPTURE_FAILHARD)
	protected void customVelocityCalculation(Vec3d input, CallbackInfo ci, double dt, boolean $$4, FluidState $$5, Vec3d v0, Vec3d rotation, float pitch, double speed_horiz, double rotation_horiz, double rotation_length, float cos_pitch) {
		if (useVanillaMechanics) {
			return;
		}

		LivingEntity me = (LivingEntity)(Object)this;
		// pitch in radians.
		// speed_horiz is absolute value of velocity in the xz plane.
		// rotation_horiz is absolute value of the rotation vector in the xz plane.
		// rotation_length is absolute value of the rotation vector.
		//float j = cos_pitch;
		float j = (float)((double)cos_pitch * ((double)cos_pitch * Math.min(1.0, rotation_length / 0.4)));
		// j ranges from 0 if cos_pitch is 0 (flying straingt up or down) to 1 if pitch is 0 (horizontal flight);

		//Vec3d e = v0.add(0.0, dt * (-1.0 + (double)j * 0.75), 0.0);
		// Lift coefficient?
		double lift = 0.3; // default 0.75
		Vec3d e = v0.add(0.0, dt * (-1.0 + (double)j * lift), 0.0);
		double glide_factor;

		// Stalling
		if (e.y < 0.0 && speed_horiz > 0.0) {
			glide_factor = e.y * -0.1 * (double)j;
			e = e.add(rotation.x * glide_factor / speed_horiz, glide_factor, rotation.z * glide_factor / speed_horiz);
		}

		// Gliding down.
		if (pitch < 0.0f && speed_horiz > 0.0) {
			glide_factor = rotation_horiz * (double)(-MathHelper.sin(pitch)) * 0.04;
			e = e.add(-rotation.x * glide_factor / speed_horiz, glide_factor * 3.2, -rotation.z * glide_factor / speed_horiz);
		}

		// Gliding up.
		if (speed_horiz > 0.0) {
			e = e.add((rotation.x / speed_horiz * rotation_horiz - e.x) * 0.1, 0.0, (rotation.z / speed_horiz * rotation_horiz - e.z) * 0.1);
		}
		me.setVelocity(e.multiply(0.99f, 0.98f, 0.99f));
	}
	@Redirect(method="travel", at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 6))
	protected void overrideVanillaSetVelocity(LivingEntity me, Vec3d v) {
		if (useVanillaMechanics) {
			//setVelocity(v);
		}
	}

	/*
	@Redirect(method="travel", at=@At(value="INVOKE", target="Lnet/minecraft/util/math/Vec3d;multiply(DDD)Lnet/minecraft/util/math/Vec3d;", ordinal = 2))
	protected Vec3d attenuateVelocity(Vec3d v, double fx, double fy, double fz) {
		return v.multiply(0.97, fy, 0.97);
	}*/
}
