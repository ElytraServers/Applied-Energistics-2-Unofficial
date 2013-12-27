package appeng.core;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;
import appeng.block.AEBaseBlock;
import cpw.mods.fml.common.SidedProxy;

public abstract class CommonHelper
{

	@SidedProxy(clientSide = "appeng.client.ClientHelper", serverSide = "appeng.server.ServerHelper")
	public static CommonHelper proxy;

	public abstract void init();

	public abstract World getWorld();

	public abstract void bindTileEntitySpecialRenderer(Class tile, AEBaseBlock blk);

	public abstract List<EntityPlayer> getPlayers();

	public abstract void sendToAllNearExcept(EntityPlayer p, double x, double y, double z, double dist, World w, Packet packet);

}
