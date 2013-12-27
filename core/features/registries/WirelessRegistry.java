package appeng.core.features.registries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistery;
import appeng.util.Platform;
import cpw.mods.fml.common.network.Player;

public class WirelessRegistry implements IWirelessTermRegistery
{

	List<IWirelessTermHandler> handlers;

	public WirelessRegistry() {
		handlers = new ArrayList();
	}

	@Override
	public void registerWirelessHandler(IWirelessTermHandler handler)
	{
		handlers.add( handler );
	}

	@Override
	public boolean isWirelessTerminal(ItemStack is)
	{
		for (IWirelessTermHandler h : handlers)
		{
			if ( h.canHandle( is ) )
				return true;
		}
		return false;
	}

	@Override
	public IWirelessTermHandler getWirelessTerminalHandler(ItemStack is)
	{
		for (IWirelessTermHandler h : handlers)
		{
			if ( h.canHandle( is ) )
				return h;
		}
		return null;
	}

	@Override
	public void OpenWirelessTermainlGui(ItemStack item, World w, EntityPlayer player)
	{
		if ( Platform.isClient() )
			return;

		IWirelessTermHandler handler = getWirelessTerminalHandler( item );
		if ( handler == null )
		{
			player.addChatMessage( "Item is not a wireless terminal." );
			return;
		}

		if ( handler.usePower( (Player) player, 10.0f, item ) )
		{

		}

	}

}
