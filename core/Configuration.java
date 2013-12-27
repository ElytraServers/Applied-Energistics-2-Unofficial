package appeng.core;

import java.io.File;
import java.util.EnumSet;

import appeng.api.config.CondenserOuput;
import appeng.api.config.PowerUnits;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class Configuration extends net.minecraftforge.common.Configuration implements IConfigureableObject, IConfigManagerHost
{

	public static Configuration instance;

	public String latestVersion = VERSION;
	public long latestTimeStamp = 0;

	public static final String VERSION = "${version}";
	public static final String CHANNEL = "${channel}";

	public final static String PACKET_CHANNEL = "AE";

	public IConfigManager settings = new ConfigManager( this );
	public EnumSet<AEFeature> featureFlags = EnumSet.noneOf( AEFeature.class );

	public int oresPerCluster = 4;

	private double WirelessBaseCost = 8;
	private double WirelessCostMultiplier = 1;
	private double WirelessHighWirelessCount = 64;

	private double WirelessBaseRange = 16;
	private double WirelessBoosterRangeMultiplier = 1;
	private double WirelessBoosterExp = 1.5;

	public double wireless_getMaxRange(int boosters)
	{
		return WirelessBaseRange + WirelessBoosterRangeMultiplier * Math.pow( boosters, WirelessBoosterExp );
	}

	public double wireless_getPowerDrain(int boosters)
	{
		return WirelessBaseCost + WirelessCostMultiplier * Math.pow( boosters, 1 + boosters / WirelessHighWirelessCount );
	}

	public double spatialPowerScaler = 1.5;
	public double spatialPowerMultiplier = 1500.0;

	private int blkBaseNumber = 900;
	private int blkItemNumber = 3841;

	public String grinderOres[] = {
			// Vanilla Items
			"Obsidian", "EnderPearl", "Coal", "Iron", "Gold", "Charcoal",
			// Common Mod Ores
			"Copper", "Tin", "Silver", "Lead", "Bronze",
			// Other Mod Ores
			"Brass", "Platinum", "Nickel", "Invar", "Aluminium", "Electrum" };

	public double oreDoublePercentage = 90.0;

	public boolean enableEffects = true;
	public boolean enableNetworkProfiler = true;

	public Configuration(File f) {
		super( f );

		final double DEFAULT_BC_EXCHANGE = 5.0;
		final double DEFAULT_UE_EXCHANGE = 5.0;
		final double DEFAULT_IC2_EXCHANGE = 2.0;
		final double DEFAULT_RTC_EXCHANGE = 1.0 / 11256.0;
		final double DEFAULT_RF_EXCHANGE = 0.5;

		PowerUnits.MJ.conversionRatio = get( "PowerInputRatios", "BuildCraft", DEFAULT_BC_EXCHANGE ).getDouble( DEFAULT_BC_EXCHANGE );
		PowerUnits.KJ.conversionRatio = get( "PowerInputRatios", "UniversalElectricity", DEFAULT_UE_EXCHANGE ).getDouble( DEFAULT_UE_EXCHANGE );
		PowerUnits.EU.conversionRatio = get( "PowerInputRatios", "IC2", DEFAULT_IC2_EXCHANGE ).getDouble( DEFAULT_IC2_EXCHANGE );
		PowerUnits.WA.conversionRatio = get( "PowerInputRatios", "RotaryCraft", DEFAULT_RTC_EXCHANGE ).getDouble( DEFAULT_RTC_EXCHANGE );
		PowerUnits.RF.conversionRatio = get( "PowerInputRatios", "ThermalExpansion", DEFAULT_RF_EXCHANGE ).getDouble( DEFAULT_RF_EXCHANGE );

		CondenserOuput.MATTER_BALLS.requiredPower = get( "Condenser", "MatterBalls", 256 ).getInt( 256 );
		CondenserOuput.SINGULARITY.requiredPower = get( "Condenser", "Singularity", 256000 ).getInt( 256000 );

		grinderOres = get( "GrindStone", "grinderOres", grinderOres ).getStringList();
		oreDoublePercentage = get( "GrindStone", "oreDoublePercentage", oreDoublePercentage ).getDouble( oreDoublePercentage );
		enableEffects = get( "Client", "enableEffects", true ).getBoolean( true );
		enableNetworkProfiler = get( "Server", "enableNetworkProfiler", true ).getBoolean( true );

		// settings.registerSetting( Settings.SEARCH_MODS, YesNo.YES );
		settings.registerSetting( Settings.SEARCH_TOOLTIPS, YesNo.YES );
		settings.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		settings.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		for (AEFeature feature : AEFeature.values())
		{
			if ( feature.isVisible() )
			{
				if ( get( "Features." + feature.getCategory(), feature.name(), feature.defaultVaue() ).getBoolean( feature.defaultVaue() ) )
					featureFlags.add( feature );
			}
			else
				featureFlags.add( feature );
		}

		for (Enum e : settings.getSettings())
		{
			String Category = e.getClass().getSimpleName();
			this.get( Category, e.name(), settings.getSetting( e ).name() );
		}

		if ( isFeatureEnabled( AEFeature.VersionChecker ) )
		{
			try
			{
				latestVersion = get( "VersionChecker", "LatestVersion", "" ).getString();
				latestTimeStamp = Long.parseLong( get( "VersionChecker", "LatestTimeStamp", "" ).getString() );
			}
			catch (NumberFormatException err)
			{
				latestTimeStamp = 0;
			}
		}
	}

	@Override
	public void updateSetting(Enum setting, Enum newValue)
	{
		for (Enum e : settings.getSettings())
		{
			String Category = e.getClass().getSimpleName();
			this.get( Category, e.name(), settings.getSetting( e ).name() );
		}

		save();
	}

	@Override
	public void save()
	{
		if ( isFeatureEnabled( AEFeature.VersionChecker ) )
		{
			get( "VersionChecker", "LatestVersion", latestVersion ).set( latestVersion );
			get( "VersionChecker", "LatestTimeStamp", "" ).set( Long.toString( latestTimeStamp ) );
		}

		if ( hasChanged() )
			super.save();
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return settings;
	}

	public boolean isFeatureEnabled(AEFeature f)
	{
		return featureFlags.contains( f );
	}

	public int getBlockID(Class c, String subname)
	{
		return getBlock( AEFeatureHandler.getName( c, subname ), blkBaseNumber++ ).getInt();
	}

	public int getItemID(Class c, String subname)
	{
		return getItem( AEFeatureHandler.getName( c, subname ), blkItemNumber++ ).getInt();
	}

	public boolean useTerminalUseLargeFont()
	{
		return false;
	}

}
