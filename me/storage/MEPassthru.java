package appeng.me.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public class MEPassthru<T extends IAEStack<T>> implements IMEInventoryHandler<T>
{

	private IMEInventory<T> internal;

	protected IMEInventory<T> getInternal()
	{
		return internal;
	}

	public MEPassthru(IMEInventory<T> i) {
		setInternal( i );
	}

	public void setInternal(IMEInventory<T> i)
	{
		internal = i;
	}

	@Override
	public T injectItems(T input, Actionable type)
	{
		return internal.injectItems( input, type );
	}

	@Override
	public T extractItems(T request, Actionable type)
	{
		return internal.extractItems( request, type );
	}

	@Override
	public IItemList<T> getAvailableItems(IItemList out)
	{
		return internal.getAvailableItems( out );
	}

	@Override
	public StorageChannel getChannel()
	{
		return internal.getChannel();
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public boolean isPrioritized(T input)
	{
		return false;
	}

	@Override
	public boolean canAccept(T input)
	{
		return true;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

}
