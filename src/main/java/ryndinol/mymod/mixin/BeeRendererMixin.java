package ryndinol.mymod.mixin;

import net.minecraft.client.render.entity.BeeEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeeEntityRenderer.class)
public abstract class BeeRendererMixin {
	protected void scale(LivingEntity entity, MatrixStack matrices, float amount) {
		float scale = 0.25f;
		matrices.scale(scale, scale, scale);
	}
}
