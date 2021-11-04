package ryndinol.mymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ryndinol.mymod.MyElytra;

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

		if (true) {
			// Airplane :D
			double cl0 = 0.1;
			double cl_alpha = 6;
			//double cd0 = 0.03;
			double cd0 = 0.01;

			// Small hang glider aspect ratio and wing area.
			double ar = 5.5;
			double wing_area = 10;

			// Effect of gravity.
			Vec3d v = v0.add(0.0, -dt, 0.0);
			MyElytra.logger.warn("rotation", rotation);
			MyElytra.logger.warn("velocity", v);

			//double airspeed = v.length();

			// Transform velocity from world to local frame.
			// Local frame: only pitch and yaw, no roll.
			//v = v.rotateY(-(float)rotation.y);
			float yaw = (float)(me.getYaw()*Math.PI/180.0);
			v = v.rotateY(yaw);
			v = v.rotateZ(-pitch);
			// NOTE: Not the standard aircraft cartesian frame of reference.
			// Z is forward, Y is upward, X is left.
			MyElytra.logger.warn("velocity rotated", v);

			double alpha = MathHelper.atan2(v.y, v.z);

			// TODO: better stall?
			//double cl = MathHelper.clamp(cl0 + cl_alpha*alpha, -1.0, 1.0);
			double cl = cl0 + cl_alpha*alpha; // No stall/max for testing only.
			double cd = cd0 + cl*cl/(Math.PI*ar);

			double half_rho_v_squared_s = .5*1.225*v.z*v.z*wing_area;
			//double weight = 100.0 * 9.81;
			double weight = 20.0 * 9.81;

			// Scale force to gravity. From later in the code, adding -1.0*dt to vy is equivalent to the acceleration due to gravity.
			// So force / 9.81 * dt would be its delta_v effect.
			double lift = half_rho_v_squared_s*cl / weight * dt;
			double drag = half_rho_v_squared_s*cd / weight * dt;
			// Lift and drag act in the 'rotation' frame. Need to rotate into the world coordinate frame.
			
			// Fake yaw equation.
			double yaw_force = -0.1 * v.x;

			//v = v.add(-drag, lift, yaw_force);
			v = v.add(yaw_force, lift, -drag);

			// Convert back to world coordinates.
			v = v.rotateZ(pitch);
			v = v.rotateY(-yaw);

			//me.setVelocity(e.multiply(0.99f, 0.98f, 0.99f));
			me.setVelocity(v);

			return;
		}


        // Below is vanilla style Elytra math.

		// pitch in radians.
		// speed_horiz is absolute value of velocity in the xz plane.
		// rotation_horiz is absolute value of the rotation vector in the xz plane.
		// rotation_length is absolute value of the rotation vector.
		//float j = cos_pitch;
		float j = (float)((double)cos_pitch * ((double)cos_pitch * Math.min(1.0, rotation_length / 0.4)));
		// j ranges from 0 if cos_pitch is 0 (flying straingt up or down) to 1 if pitch is 0 (horizontal flight);

		// Fake antigravity style lift. Does not depend on speed.
		// When looking forward horizontally, gravity is reduced by 75%.
		double antigravity = 0.75;
		Vec3d e = v0.add(0.0, dt * (-1.0 + (double)j * antigravity), 0.0);
		double glide_factor;

		if (rotation_horiz > 0.0) {
			// Descending.
			if (e.y < 0.0) {
				glide_factor = e.y * -0.1 * (double)j;
				// Increases with higher descent speed, and with horizontal orientation.
				e = e.add((rotation.x / rotation_horiz) * glide_factor, glide_factor, (rotation.z / rotation_horiz) * glide_factor );
				// Resist downward descent, and increase horinontal speed in the direction facing.
			}

			// Pitched down.
			if (pitch < 0.0f) {
				glide_factor = speed_horiz * (double)(-MathHelper.sin(pitch)) * 0.04;
				// Larger when horizontal speed large, but multiplied by sin(pitch) which grows when close to vertical?
				// So maximum at a downward diagonal?

				e = e.add(-(rotation.x / rotation_horiz) * glide_factor , glide_factor * 3.2, -(rotation.z / rotation_horiz) * glide_factor );
				// Big resistance to downward y motion compared to above.
				// Opposite sign to x/y components compared to above.
			}

			// Oppose horizontal sideslip motion for yaw control.
			// Add speed in direction facing minus component of velocity in that direction.
			e = e.add(((rotation.x / rotation_horiz) * speed_horiz - e.x) * 0.1, 0.0, ((rotation.z / rotation_horiz) * speed_horiz - e.z) * 0.1);
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
