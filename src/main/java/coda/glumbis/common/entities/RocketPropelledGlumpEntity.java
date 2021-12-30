package coda.glumbis.common.entities;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class RocketPropelledGlumpEntity extends Projectile implements IAnimatable, IAnimationTickable {
    private final AnimationFactory factory = new AnimationFactory(this);

    public RocketPropelledGlumpEntity(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void onHitEntity(EntityHitResult p_37259_) {
        super.onHitEntity(p_37259_);
        this.level.explode(this, this.getX(), this.getY(), this.getZ(), 1, Explosion.BlockInteraction.NONE);
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult p_36755_) {
        super.onHitBlock(p_36755_);
        Vec3 vec3 = p_36755_.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.onGround = true;
        this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));

    }

    @Override
    public void tick() {
        super.tick();
        if (onGround && tickCount % 30 == 0) {
            this.level.explode(this, this.getX(), this.getY(), this.getZ(), 1, Explosion.BlockInteraction.NONE);
            this.discard();
        }

        if (!isOnGround() && getBlockStateOn().isAir() && getTarget() != null) {

            setDeltaMovement(getX() - getTarget().getX(), getY() - getTarget().getY(), getZ() - getTarget().getZ());
        }
    }

    public LivingEntity getTarget() {
        return level.getNearestEntity(Chicken.class, TargetingConditions.forCombat(), null, 1.0, 1.0, 1.0, getBoundingBox().inflate(50));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 2, this::predicate));
    }

    @Override
    public int tickTimer() {
        return tickCount;
    }
}
