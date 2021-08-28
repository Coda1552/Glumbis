package coda.glumbis.common.items;

import coda.glumbis.Glumbis;
import coda.glumbis.common.init.GlumbisItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.UUID;

public class GlumbisItem extends Item {
    public static final String DATA_CREATURE = "CreatureData";

    public GlumbisItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level;
        if (containsEntity(stack)) return InteractionResult.PASS;

        if (!target.getPassengers().isEmpty()) target.ejectPassengers();
        if (target instanceof Cat) {
            if (!level.isClientSide) {
                CompoundTag tag = stack.getOrCreateTag();
                CompoundTag targetTag = target.serializeNBT();
                targetTag.putString("OwnerName", player.getName().getString());
                tag.put(DATA_CREATURE, targetTag);
                stack.setTag(tag);
                target.discard();
                player.setItemInHand(hand, stack);
                level.playSound(null, player.blockPosition(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.AMBIENT, 1, 1);
            }
        }

        return InteractionResult.sidedSuccess(true);
    }

    private static boolean containsEntity(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(DATA_CREATURE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult rt = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        ItemStack stack = player.getItemInHand(hand);
        if (rt.getType() == HitResult.Type.MISS) return InteractionResultHolder.pass(stack);
        BlockPos pos = rt.getBlockPos();
        if (!(level.getBlockState(pos).getBlock() instanceof LiquidBlock)) return InteractionResultHolder.success(stack);
        return new InteractionResultHolder<>(releaseEntity(level, player, stack, pos, rt.getDirection()), stack);
    }

    private static InteractionResult releaseEntity(Level level, Player player, ItemStack stack, BlockPos pos, Direction direction) {
        if (!containsEntity(stack)) return InteractionResult.PASS;

        CompoundTag tag = stack.getTag().getCompound(DATA_CREATURE);
        EntityType<?> type = EntityType.byString(tag.getString("id")).orElse(null);
        LivingEntity entity;

        if (type == null || (entity = (LivingEntity) type.create(level)) == null) {
            Glumbis.LOGGER.error("Something went wrong releasing a cat!");
            return InteractionResult.FAIL;
        }

        EntityDimensions size = entity.getDimensions(entity.getPose());
        if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
            pos = pos.relative(direction, (int) (direction.getAxis().isHorizontal() ? size.width : 1));

        entity.absMoveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        AABB aabb = entity.getBoundingBox();

        if (!level.noCollision(entity, new AABB(aabb.minX, entity.getEyeY() - 0.35, aabb.minZ, aabb.maxX, entity.getEyeY() + 1.0, aabb.maxZ))) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            UUID id = entity.getUUID();
            entity.deserializeNBT(tag);
            entity.setUUID(id);
            entity.moveTo(pos.getX(), pos.getY() + direction.getStepY() + 1.0, pos.getZ(), player.yRotO, 0f);

            if (stack.hasCustomHoverName()) entity.setCustomName(stack.getHoverName());
            stack.removeTagKey(DATA_CREATURE);
            level.addFreshEntity(entity);
            level.playSound(null, entity.blockPosition(), SoundEvents.BARREL_OPEN, SoundSource  .AMBIENT, 1, 1);
        }

        return InteractionResult.SUCCESS;
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!(world instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else if (context.getItemInHand().hasTag()) {
            ItemStack itemstack = context.getItemInHand();
            BlockPos blockpos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            BlockState blockstate = world.getBlockState(blockpos);

            BlockPos blockpos1;
            if (blockstate.getCollisionShape(world, blockpos).isEmpty()) {
                blockpos1 = blockpos;
            } else {
                blockpos1 = blockpos.relative(direction);
            }

            ItemStack stack = context.getItemInHand();
            CompoundTag tag = stack.getTag().getCompound(DATA_CREATURE);
            EntityType<?> type = EntityType.byString(tag.getString("id")).orElse(null);
            LivingEntity entity = (LivingEntity) type.create(context.getLevel());
            if (entity == null) return InteractionResult.FAIL;

            UUID id = entity.getUUID();
            entity.deserializeNBT(tag);
            entity.setUUID(id);

            entity.moveTo(blockpos1.getX() + 0.5, blockpos1.getY(), blockpos1.getZ() + 0.5, context.getPlayer().yRotO, 0f);

            if (stack.hasCustomHoverName()) entity.setCustomName(stack.getHoverName());
            stack.removeTagKey(DATA_CREATURE);

            if (context.getLevel().addFreshEntity(entity)) {
                itemstack.shrink(1);
            }
            context.getLevel().playSound(null, entity.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.AMBIENT, 1, 1);
            context.getPlayer().setItemInHand(context.getHand(), new ItemStack(GlumbisItems.SOCK.get()));

            return InteractionResult.CONSUME;
        }
        else {
            return super.useOn(context);
        }
    }
}
