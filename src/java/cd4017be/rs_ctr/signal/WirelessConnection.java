package cd4017be.rs_ctr.signal;

import cd4017be.lib.util.DimPos;
import cd4017be.lib.util.ItemFluidUtil;
import cd4017be.lib.util.Orientation;
import cd4017be.rs_ctr.Objects;
import cd4017be.rs_ctr.api.signal.IConnector;
import cd4017be.rs_ctr.api.signal.ISignalIO;
import cd4017be.rs_ctr.api.signal.ITagableConnector;
import cd4017be.rs_ctr.api.signal.MountedSignalPort;
import cd4017be.rs_ctr.api.signal.SignalPort;
import cd4017be.rs_ctr.render.WireRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;


/**
 * @author CD4017BE
 *
 */
public class WirelessConnection implements ITagableConnector {

	public static final String ID = "wireless";

	private DimPos linkPos;
	private int linkPin;
	private boolean dropsItem;
	private String tag;

	public WirelessConnection() {}

	public WirelessConnection(DimPos linkPos, int linkPin, boolean drop) {
		this.linkPos = linkPos;
		this.linkPin = linkPin;
		this.dropsItem = drop;
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("id", ID);
		nbt.setLong("pos", linkPos.toLong());
		nbt.setInteger("dim", linkPos.dimId);
		nbt.setInteger("pin", linkPin);
		nbt.setBoolean("drop", dropsItem);
		if (tag != null) nbt.setString("tag", tag);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		linkPos = new DimPos(BlockPos.fromLong(nbt.getLong("pos")), nbt.getInteger("dim"));
		linkPin = nbt.getInteger("pin");
		dropsItem = nbt.getBoolean("drop");
		tag = nbt.hasKey("tag", NBT.TAG_STRING) ? nbt.getString("tag") : null;
	}

	@Override
	public String displayInfo(MountedSignalPort port, int linkID) {
		try {
			return ITagableConnector.super.displayInfo(port, linkID)
				+ "\n[" + linkPos.getX() + ", " + linkPos.getY() + ", " + linkPos.getZ() + "]\n"
				+ DimensionManager.getProviderType(linkPos.dimId).getName();
		} catch (IllegalArgumentException e) {
			return "\n" + e.getMessage();
		}
	}

	@Override
	public void renderConnection(World world, BlockPos pos, MountedSignalPort port, double x, double y, double z, int light, BufferBuilder buffer) {
		WireRenderer.instance.drawModel(buffer, (float)x, (float)y, (float)z, Orientation.fromFacing(port.face), light, "plug.main(2)");
	}

	@Override
	public AxisAlignedBB renderSize(World world, BlockPos pos, MountedSignalPort port) {
		return null;
	}

	@Override
	public void onRemoved(MountedSignalPort port, EntityPlayer player) {
		ItemStack stack = new ItemStack(Objects.wireless);
		World world = port.getWorld();
		BlockPos pos = port.getPos();
		SignalPort p = ISignalIO.getPort(linkPos.getWorldServer(), linkPos, linkPin);
		if (p instanceof MountedSignalPort) {
			IConnector c = ((MountedSignalPort)p).getConnector();
			if (c instanceof WirelessConnection) {
				WirelessConnection wc = (WirelessConnection)c;
				if (wc.linkPos.equals(pos) && wc.linkPos.dimId == world.provider.getDimension() && wc.linkPin == port.pin) {
					if (wc.dropsItem && !this.dropsItem) {
						this.dropsItem = true; wc.dropsItem = false;
					}
					((MountedSignalPort)p).setConnector(null, player);
				}
			}
		}
		if (dropsItem) {
			if (player != null) ItemFluidUtil.dropStack(stack, player);
			else ItemFluidUtil.dropStack(stack, world, pos);
		}
		port.disconnect();
	}

	@Override
	public void setTag(MountedSignalPort port, String tag) {
		if (this.tag != null ? this.tag.equals(tag) : tag == null) return;
		this.tag = tag;
		port.owner.onPortModified(port, ISignalIO.E_CON_UPDATE);
		SignalPort p = ISignalIO.getPort(linkPos.getWorldServer(), linkPos, linkPin);
		if (p instanceof MountedSignalPort) {
			IConnector c = ((MountedSignalPort)p).getConnector();
			if (c instanceof WirelessConnection) {
				((WirelessConnection)c).tag = tag;
				p.owner.onPortModified(p, ISignalIO.E_CON_UPDATE);
			}
		}
	}

	@Override
	public String getTag() {
		return tag;
	}

}
