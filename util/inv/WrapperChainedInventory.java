package appeng.util.inv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class WrapperChainedInventory implements IInventory
{

	class InvOffset
	{

		int offset;
		int size;
		IInventory i;
	};

	int fullSize = 0;

	private List<IInventory> l;
	private HashMap<Integer, InvOffset> offsets;

	public WrapperChainedInventory(IInventory ilist) {
		setInventory( ilist );
	}

	public WrapperChainedInventory(List<IInventory> ilist) {
		setInventory( ilist );
	}

	public void calculateSizes()
	{
		offsets = new HashMap<Integer, WrapperChainedInventory.InvOffset>();

		int offset = 0;
		for (IInventory in : l)
		{
			InvOffset io = new InvOffset();
			io.offset = offset;
			io.size = in.getSizeInventory();
			io.i = in;

			for (int y = 0; y < io.size; y++)
			{
				offsets.put( y + io.offset, io );
			}

			offset += io.size;
		}

		fullSize = offset;
	}

	public void setInventory(IInventory a)
	{
		l = new ArrayList();
		l.add( a );
		calculateSizes();
	}

	public void setInventory(List<IInventory> a)
	{
		l = a;
		calculateSizes();
	}

	public IInventory getInv(int idx)
	{
		InvOffset io = offsets.get( idx );
		if ( io != null )
		{
			return io.i;
		}
		return null;
	}

	public int getInvSlot(int idx)
	{
		InvOffset io = offsets.get( idx );
		if ( io != null )
		{
			return idx - io.offset;
		}
		return 0;
	}

	@Override
	public int getSizeInventory()
	{
		return fullSize;
	}

	@Override
	public ItemStack getStackInSlot(int idx)
	{
		InvOffset io = offsets.get( idx );
		if ( io != null )
		{
			return io.i.getStackInSlot( idx - io.offset );
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int idx, int var2)
	{
		InvOffset io = offsets.get( idx );
		if ( io != null )
		{
			return io.i.decrStackSize( idx - io.offset, var2 );
		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int idx)
	{
		InvOffset io = offsets.get( idx );
		if ( io != null )
		{
			return io.i.getStackInSlotOnClosing( idx - io.offset );
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int idx, ItemStack var2)
	{
		InvOffset io = offsets.get( idx );
		if ( io != null )
		{
			io.i.setInventorySlotContents( idx - io.offset, var2 );
		}
	}

	@Override
	public String getInvName()
	{
		return "ChainedInv";
	}

	@Override
	public int getInventoryStackLimit()
	{
		int smallest = 64;

		for (IInventory i : l)
			smallest = Math.min( smallest, i.getInventoryStackLimit() );

		return smallest;
	}

	@Override
	public void onInventoryChanged()
	{
		for (IInventory i : l)
		{
			i.onInventoryChanged();
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1)
	{
		return false;
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int idx, ItemStack itemstack)
	{
		InvOffset io = offsets.get( idx );
		if ( io != null )
		{
			return io.i.isItemValidForSlot( idx - io.offset, itemstack );
		}
		return false;
	}
}
