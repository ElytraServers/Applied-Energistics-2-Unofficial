package appeng.core.features.registries.entries;

import net.minecraft.item.ItemStack;
import appeng.api.features.IGrinderEntry;

public class AppEngGrinderRecipe implements IGrinderEntry
{

	private ItemStack in;
	private ItemStack out;

	private float optionalChance;
	private ItemStack optionalOutput;

	private int energy;

	public AppEngGrinderRecipe(ItemStack a, ItemStack b, int cost) {
		in = a;
		out = b;
		energy = cost;
	}

	public AppEngGrinderRecipe(ItemStack a, ItemStack b, ItemStack c, float chance, int cost) {
		in = a;
		out = b;

		optionalOutput = c;
		optionalChance = chance;

		energy = cost;
	}

	@Override
	public ItemStack getInput()
	{
		return in;
	}

	@Override
	public void setInput(ItemStack i)
	{
		in = i.copy();
	}

	@Override
	public ItemStack getOutput()
	{
		return out;
	}

	@Override
	public void setOutput(ItemStack o)
	{
		out = o.copy();
	}

	@Override
	public int getEnergyCost()
	{
		return energy;
	}

	@Override
	public void setEnergyCost(int c)
	{
		energy = c;
	}

	@Override
	public ItemStack getOptionalOutput()
	{
		return optionalOutput;
	}

	@Override
	public void setOptionalOutput(ItemStack output, float chance)
	{
		optionalOutput = output;
		optionalChance = chance;
	}

	@Override
	public float getOptionalChance()
	{
		return optionalChance;
	}

}
