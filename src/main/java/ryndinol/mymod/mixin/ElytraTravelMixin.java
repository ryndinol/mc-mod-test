package ryndinol.mymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ryndinol.mymod.MyElytra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(LivingEntity.class)
public abstract class ElytraTravelMixin extends Entity {
	public ElytraTravelMixin(EntityType<?> entityType, World world) {
		super(entityType, world);
		//TODO Auto-generated constructor stub
	}
	private boolean useVanillaMechanics  = false;
	private boolean wasFallFlying = false;

	@Inject(method="travel", at=@At(value="TAIL"))
	protected void resetFallFlying(CallbackInfo ci) {
		wasFallFlying = getFlag(FALL_FLYING_FLAG_INDEX);
	}

	private Vec3d eval(Vec3d v0, double gravity, float pitch, boolean debug) {
			// Airplane :D
			double cl0 = 0.1;
			//double cl_alpha = 6;
			double cd0 = 0.03;
			//double cd0 = 0.01;

			// Small hang glider aspect ratio and wing area.
			//double ar = 5.5;
			//double wing_area = 10;

			// Big glider
			double ar = 10;
			double wing_area = 20;

			float yaw = (float)(this.getYaw()*Math.PI/180.0);
			// NOTE: Roll isn't actually roll, it's used as a counter for number of ticks while fallflying.

		// Transform velocity from world to local frame.
		// NOTE: Not the standard aircraft cartesian frame of reference.
		// Z is forward, Y is upward, X is left. Pitch is about X-axis, and is opposite sign of what is expected.
		// Local frame: only pitch and yaw, no roll.
		//v = v.rotateY(-(float)rotation.y);
		Vec3d v = v0.rotateY(yaw);
		// Rotate in pitch such that frame lines up with airflow direction, since lift acts perpendicular to airflow, not perpendicular to wing.
		float airflow_pitch = -(float)MathHelper.atan2(v.y, v.z);
		//double alpha = -MathHelper.atan2(v.y, v.z);
		double alpha = airflow_pitch - pitch;
		v = v.rotateX(airflow_pitch);

		// TODO: better stall?
		//double cl = MathHelper.clamp(cl0 + cl_alpha*alpha, -1.0, 1.0);
		double cl_max = 1.5;
		double alpha_max = 0.3;
		double cl = cl0 + cl_max*MathHelper.sin((float)(MathHelper.clamp(2*alpha/alpha_max, -2.0, 2.0)*Math.PI/2.0));
		double cd = cd0 + cl*cl/(Math.PI*ar);


		// Units of kg*block/tick^2
		double half_rho_v_squared_s = .5*1.225*v.z*v.z*wing_area;
		double mass = 100.0;

		double lift = half_rho_v_squared_s*cl;
		double drag = half_rho_v_squared_s*cd;
		lift /= mass;
		drag /= mass;
		if (debug) MyElytra.logger.warn("L/D: "+cl/cd+"; alpha: "+alpha+"; cl: "+cl+"; lift: "+lift+"; drag: "+drag);

		// Lift and drag act in the 'rotation' frame. Need to rotate into the world coordinate frame.
		
		//v = v.add(-drag, lift, yaw_force);

		// Fake yaw equation.
		double yaw_force = -0.1 * v.x;

		Vec3d dv = v.z >= 0 ? new Vec3d(yaw_force, lift, -drag) : new Vec3d(yaw_force, 0, drag);

		// Convert back to world coordinates.
		dv = dv.rotateX(-airflow_pitch);
		dv = dv.rotateY(-yaw);

		// Effect of gravity.
		// Gravity is 0.08 blocks/tick^2.
		dv = dv.add(0.0, -gravity, 0.0);

		// Vanilla terminal velocity calc.
		//v_out = v_out.multiply(0.99f, 0.98f, 0.99f);
		return dv;
	}

	//@Inject(method="travel", at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 6), locals = LocalCapture.PRINT)
	@Inject(method="travel", at=@At(value="INVOKE", target="Ljava/lang/Math;min(DD)D"), locals = LocalCapture.CAPTURE_FAILHARD)
	protected void customVelocityCalculation(Vec3d input, CallbackInfo ci, double gravity, boolean $$4, FluidState $$5, Vec3d v0, Vec3d rotation, float pitch, double speed_horiz, double rotation_horiz, double rotation_length, float cos_pitch) {
		if (useVanillaMechanics) {
			return;
		}

		//LivingEntity me = (LivingEntity)(Object)this;

		if (true) {
			if (!wasFallFlying) {
				// initialize any state here?
				// TODO: Remove wasFallFlying if unused.
			}

			//double airspeed = v.length();

			// classic rk4
			// Vec3d k1 = eval(v0);
			// Vec3d k2 = eval(v0.add(k1.multiply(0.5)));
			// Vec3d k3 = eval(v0.add(k2.multiply(0.5)));
			// Vec3d k4 = eval(v0.add(k3));
			// Vec3d v_next = v0.add(k1.add(k2.multiply(2)).add(k3.multiply(2)).add(k4).multiply(1.0/6));

			// 3/8 rule
			Vec3d k1 = eval(v0, gravity, pitch, false);
			Vec3d k2 = eval(v0.add(k1.multiply(1.0/3)), gravity, pitch, true);
			Vec3d k3 = eval(v0.add(k1.multiply(-1.0/3)).add(k2.multiply(1.0)), gravity, pitch, false);
			Vec3d k4 = eval(v0.add(k1).subtract(k2).add(k3), gravity, pitch, false);
			Vec3d v_next = v0.add(k1.add(k2.multiply(3)).add(k3.multiply(3)).add(k4).multiply(1.0/8));


				//v = v.add(yaw_force, 0, -0.1*v.z);
				//me.isFallFlying()

				// TODO:
				//this.setFlag(FALL_FLYING_FLAG_INDEX, false);
				//v_out = v0;

			MyElytra.logger.warn("v_next: "+v_next);

			this.setVelocity(v_next);
			//this.setVelocity(v_out);
			//this.setVelocity(v);

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
		Vec3d e = v0.add(0.0, gravity * (-1.0 + (double)j * antigravity), 0.0);
		double glide_factor;

		if (rotation_horiz > 0.0) {
			// Descending.
			if (e.y < 0.0) {
				glide_factor = e.y * -0.1 * (double)j;
				// Increases with higher descent speed, and with horizontal orientation.
				e = e.add((rotation.x / rotation_horiz) * glide_factor, glide_factor, (rotation.z / rotation_horiz) * glide_factor );
				// Resist downward descent, and increase horinontal speed in the direction facing.
			}

			// Pitched up. Coordinate system is weird so negative pitch is up.
			if (pitch < 0.0f) {
				glide_factor = speed_horiz * (double)(-MathHelper.sin(pitch)) * 0.04;
				// Larger when horizontal speed large, but multiplied by sin(pitch) which grows when close to vertical?
				// So maximum at an upward diagonal?

				e = e.add(-(rotation.x / rotation_horiz) * glide_factor , glide_factor * 3.2, -(rotation.z / rotation_horiz) * glide_factor );
				// Big resistance to downward y motion compared to above.
				// Opposite sign to x/y components compared to above.
			}

			// Oppose horizontal sideslip motion for yaw control.
			// Add speed in direction facing minus component of velocity in that direction.
			e = e.add(((rotation.x / rotation_horiz) * speed_horiz - e.x) * 0.1, 0.0, ((rotation.z / rotation_horiz) * speed_horiz - e.z) * 0.1);
		}
		this.setVelocity(e.multiply(0.99f, 0.98f, 0.99f));
	}
	@Redirect(method="travel", at=@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V", ordinal = 6))
	protected void overrideVanillaSetVelocity(LivingEntity me, Vec3d v) {
		if (useVanillaMechanics) {
			//setVelocity(v);
		}
	}
}
