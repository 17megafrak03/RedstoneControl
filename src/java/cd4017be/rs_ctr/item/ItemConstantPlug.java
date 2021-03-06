package cd4017be.rs_ctr.item;

import java.io.IOException;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.BlockGuiHandler.ClientItemPacketReceiver;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.IGuiItem;
import cd4017be.lib.Gui.ItemGuiData;
import cd4017be.lib.Gui.ModularGui;
import cd4017be.lib.Gui.comp.GuiFrame;
import cd4017be.lib.Gui.comp.TextField;
import cd4017be.lib.item.BaseItem;
import cd4017be.lib.util.TooltipUtil;
import cd4017be.rs_ctr.Main;
import cd4017be.rs_ctr.api.signal.IConnector.IConnectorItem;
import cd4017be.rs_ctr.api.signal.MountedSignalPort;
import cd4017be.rs_ctr.signal.Constant;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/**
 * @author CD4017BE
 *
 */
public class ItemConstantPlug extends BaseItem implements IConnectorItem, IGuiItem, ClientItemPacketReceiver {

	/**
	 * @param id
	 */
	public ItemConstantPlug(String id) {
		super(id);
	}

	@Override
	public void doAttach(ItemStack stack, MountedSignalPort port, EntityPlayer player) {
		if (port.isSource) {
			player.sendMessage(new TextComponentString(TooltipUtil.translate("msg.rs_ctr.const")));
			return;
		}
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) return;
		Constant c = new Constant();
		c.deserializeNBT(nbt);
		port.setConnector(c, player);
		if (!player.isCreative()) stack.shrink(1);
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {
		String s = super.getItemStackDisplayName(item);
		if (item.hasTagCompound()) s += " \u00a79" + item.getTagCompound().getInteger("val");
		return s;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (player.isSneaking()) stack.setTagCompound(null);
		else BlockGuiHandler.openItemGui(player, hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public Container getContainer(ItemStack item, EntityPlayer player, World world, BlockPos pos, int slot) {
		return new DataContainer(new ItemGuiData(this), player);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public GuiContainer getGui(ItemStack item, EntityPlayer player, World world, BlockPos pos, int slot) {
		ModularGui gui = new ModularGui(new DataContainer(new ItemGuiData(this), player));
		GuiFrame frame = new GuiFrame(gui, 80, 31, 1)
				.title("gui.rs_ctr.constant.name", 0.5F)
				.background(new ResourceLocation(Main.ID, "textures/gui/small.png"), 0, 0);
		frame.add(new TextField(frame, 64, 7, 8, 16, 12, ()-> {
			NBTTagCompound nbt = player.inventory.getStackInSlot(slot).getTagCompound();
			return nbt == null ? "0" : Integer.toString(nbt.getInteger("val"));
		}, (t)-> {
			try {
				PacketBuffer data = BlockGuiHandler.getPacketForItem(slot);
				data.writeInt(Integer.parseInt(t));
				BlockGuiHandler.sendPacketToServer(data);
			} catch (NumberFormatException e) {}
		}));
		gui.compGroup = frame;
		return gui;
	}

	@Override
	public void onPacketFromClient(PacketBuffer data, EntityPlayer sender, ItemStack item, int slot) throws IOException {
		NBTTagCompound nbt = item.getTagCompound();
		if (nbt == null) item.setTagCompound(nbt = new NBTTagCompound());
		nbt.setInteger("val", data.readInt());
	}

}
