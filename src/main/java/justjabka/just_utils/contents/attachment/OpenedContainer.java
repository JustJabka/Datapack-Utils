package justjabka.just_utils.contents.attachment;

import justjabka.just_utils.JustUtils;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class OpenedContainer {
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackWithSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ItemStackWithSlot::slot,
            ItemStack.STREAM_CODEC, ItemStackWithSlot::stack,
            ItemStackWithSlot::new
    );

    private static final AttachmentType<List<ItemStackWithSlot>> ITEMS = AttachmentRegistry.create(
            JustUtils.id("items"),
            builder -> builder
                    .initializer(ArrayList::new)
                    .persistent(ItemStackWithSlot.CODEC.listOf())
                    .syncWith(STREAM_CODEC.apply(ByteBufCodecs.list()), AttachmentSyncPredicate.all())
    );


    public static OpenedContainerData get(AttachmentTarget target) {
        return new OpenedContainerData(target);
    }

    public record OpenedContainerData(AttachmentTarget target) {
        public List<ItemStackWithSlot> get() {
            return this.target.getAttachedOrCreate(ITEMS);
        }

        public void set(List<ItemStackWithSlot> list) {
            this.target.setAttached(ITEMS, new ArrayList<>(list));
        }

        public void clear() {
            OpenedContainer.get(this.target).set(List.of());
        }
    }
}
