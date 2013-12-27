package appeng.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class InWorldToolOperationResult
{

	public ItemStack BlockItem;
	public List<ItemStack> Drops;

	public static InWorldToolOperationResult getBlockOperationResult(ItemStack[] items)
	{
		List<ItemStack> temp = new ArrayList<ItemStack>();
		ItemStack b = null;

		for (ItemStack l : items)
		{
			if ( b == null )
			{
				if ( Block.blocksList.length > l.itemID )
				{
					Block bl = Block.blocksList[l.itemID];

					if ( bl.blockID > 0 )
					{
						b = l;
						continue;
					}
				}
			}

			temp.add( l );
		}

		return new InWorldToolOperationResult( b, temp );
	}

	public InWorldToolOperationResult() {
		BlockItem = null;
		Drops = null;
	}

	public InWorldToolOperationResult(ItemStack block, List<ItemStack> drops) {
		BlockItem = block;
		Drops = drops;
	}

	public InWorldToolOperationResult(ItemStack block) {
		BlockItem = block;
		Drops = null;
	}
}
