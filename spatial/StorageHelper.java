package appeng.spatial;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import appeng.api.AEApi;
import appeng.api.util.WorldCoord;
import appeng.block.solids.BlockMatrixFrame;

public class StorageHelper
{

	private static StorageHelper instance;

	public static StorageHelper getInstance()
	{
		if ( instance == null )
			instance = new StorageHelper();
		return instance;
	}

	class triggerUpdates implements ISpatialVisitor
	{

		World dst;

		public triggerUpdates(World dst2) {
			dst = dst2;
		}

		@Override
		public void visit(int x, int y, int z)
		{
			int blk = dst.getBlockId( x, y, z );
			if ( blk > 0 )
				Block.blocksList[blk].onNeighborBlockChange( dst, x, y, z, 0 );
		}
	};

	class WrapInMatrixFrame implements ISpatialVisitor
	{

		World dst;
		int blkID, Meta;

		public WrapInMatrixFrame(int blockID, int metaData, World dst2) {
			dst = dst2;
			blkID = blockID;
			Meta = metaData;
		}

		@Override
		public void visit(int x, int y, int z)
		{
			dst.setBlock( x, y, z, blkID, Meta, 3 );
		}
	};

	class TelDestination
	{

		TelDestination(World _dim, AxisAlignedBB srcBox, double _x, double _y, double _z) {
			dim = _dim;
			x = Math.min( srcBox.maxX - 0.5, Math.max( srcBox.minX + 0.5, _x ) );
			y = Math.min( srcBox.maxY - 0.5, Math.max( srcBox.minY + 0.5, _y ) );
			z = Math.min( srcBox.maxZ - 0.5, Math.max( srcBox.minZ + 0.5, _z ) );
		}

		final World dim;
		final double x;
		final double y;
		final double z;
	};

	class METeleporter extends Teleporter
	{

		TelDestination dest;

		public METeleporter(WorldServer par1WorldServer, TelDestination d) {
			super( par1WorldServer );
			dest = d;
		}

		@Override
		public void placeInPortal(Entity par1Entity, double par2, double par4, double par6, float par8)
		{
			par1Entity.setLocationAndAngles( dest.x, dest.y, dest.z, par1Entity.rotationYaw, 0.0F );
			par1Entity.motionX = par1Entity.motionY = par1Entity.motionZ = 0.0D;
		}

		@Override
		public boolean makePortal(Entity par1Entity)
		{
			return false;
		}

		@Override
		public boolean placeInExistingPortal(Entity par1Entity, double par2, double par4, double par6, float par8)
		{
			return false;
		}

		@Override
		public void removeStalePortalLocations(long par1)
		{

		}

	};

	/**
	 * Mostly from dimentional doors.. which mostly got it form X-Comp.
	 * 
	 * @param world
	 * @param entity
	 * @param link
	 * @return
	 */
	public Entity teleportEntity(Entity entity, TelDestination link)
	{
		WorldServer oldWorld, newWorld;
		EntityPlayerMP player;

		try
		{
			oldWorld = (WorldServer) entity.worldObj;
			newWorld = (WorldServer) link.dim;
			player = (entity instanceof EntityPlayerMP) ? (EntityPlayerMP) entity : null;
		}
		catch (Throwable e)
		{
			return entity;
		}

		if ( oldWorld == null )
			return entity;
		if ( newWorld == null )
			return entity;

		// Is something riding? Handle it first.
		if ( entity.riddenByEntity != null )
		{
			return teleportEntity( entity.riddenByEntity, link );
		}
		// Are we riding something? Dismount and tell the mount to go first.
		Entity cart = entity.ridingEntity;
		if ( cart != null )
		{
			entity.mountEntity( null );
			cart = teleportEntity( cart, link );
			// We keep track of both so we can remount them on the other side.
		}

		boolean difDest = newWorld != oldWorld;
		if ( difDest )
		{
			if ( player != null )
			{
				player.mcServer.getConfigurationManager().transferPlayerToDimension( player, link.dim.provider.dimensionId, new METeleporter( newWorld, link ) );
			}
			else
			{
				int entX = entity.chunkCoordX;
				int entZ = entity.chunkCoordZ;
				if ( (entity.addedToChunk) && (oldWorld.getChunkProvider().chunkExists( entX, entZ )) )
				{
					oldWorld.getChunkFromChunkCoords( entX, entZ ).removeEntity( entity );
					oldWorld.getChunkFromChunkCoords( entX, entZ ).isModified = true;
				}

				oldWorld.onEntityRemoved( entity );

				if ( player == null ) // Are we NOT working with a player?
				{
					NBTTagCompound entityNBT = new NBTTagCompound();
					entity.posX = link.x;
					entity.posY = link.y;
					entity.posZ = link.z;
					entity.prevPosX = link.x;
					entity.prevPosY = link.y;
					entity.prevPosZ = link.z;
					entity.isDead = false;
					entity.writeToNBTOptional( entityNBT );
					entity.isDead = true;
					entity = EntityList.createEntityFromNBT( entityNBT, newWorld );
				}

				if ( entity == null )
					return entity;

				newWorld.spawnEntityInWorld( entity );
				entity.setWorld( newWorld );
			}
		}

		entity.worldObj.updateEntityWithOptionalForce( entity, false );

		if ( cart != null )
		{
			if ( player != null )
				entity.worldObj.updateEntityWithOptionalForce( entity, true );

			entity.mountEntity( cart );
		}

		if ( player != null )
		{
			WorldServer.class.cast( newWorld ).getChunkProvider().loadChunk( MathHelper.floor_double( entity.posX ) >> 4, MathHelper.floor_double( entity.posZ ) >> 4 );
		}

		return entity;
	}

	public void transverseEdges(int minx, int miny, int minz, int maxx, int maxy, int maxz, ISpatialVisitor obj)
	{
		for (int y = miny; y < maxy; y++)
			for (int z = minz; z < maxz; z++)
			{
				obj.visit( minx, y, z );
				obj.visit( maxx, y, z );
			}

		for (int x = minx; x < maxx; x++)
			for (int z = minz; z < maxz; z++)
			{
				obj.visit( x, miny, z );
				obj.visit( x, maxy, z );
			}

		for (int x = minx; x < maxx; x++)
			for (int y = miny; y < maxy; y++)
			{
				obj.visit( x, y, minz );
				obj.visit( x, y, maxz );
			}

	}

	public void swapRegions(World src /** over world **/
	, World dst /** storage cell **/
	, int x, int y, int z, int i, int j, int k, int scaleX, int scaleY, int scaleZ)
	{
		BlockMatrixFrame blkMF = (BlockMatrixFrame) AEApi.instance().blocks().blockMatrixFrame.block();

		transverseEdges( i - 1, j - 1, k - 1, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1, new WrapInMatrixFrame( blkMF.blockID, 0, dst ) );

		AxisAlignedBB srcBox = AxisAlignedBB.getBoundingBox( x, y, z, x + scaleX + 1, y + scaleY + 1, z + scaleZ + 1 );

		AxisAlignedBB dstBox = AxisAlignedBB.getBoundingBox( i, j, k, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1 );

		CachedPlane cDst = new CachedPlane( dst, i, j, k, i + scaleX, j + scaleY, k + scaleZ );
		CachedPlane cSrc = new CachedPlane( src, x, y, z, x + scaleX, y + scaleY, z + scaleZ );

		// do nearly all the work... swaps blocks, tiles, and block ticks
		cSrc.Swap( cDst );

		List<Entity> srcE = src.getEntitiesWithinAABB( Entity.class, srcBox );
		List<Entity> dstE = dst.getEntitiesWithinAABB( Entity.class, dstBox );

		for (Entity e : dstE)
		{
			teleportEntity( e, new TelDestination( src, srcBox, e.posX - i + x, e.posY - j + y, e.posZ - k + z ) );
		}

		for (Entity e : srcE)
		{
			teleportEntity( e, new TelDestination( dst, dstBox, e.posX - x + i, e.posY - y + j, e.posZ - z + k ) );
		}

		for (WorldCoord wc : cDst.updates)
			cDst.wrld.notifyBlockOfNeighborChange( wc.x, wc.y, wc.z, 0 );

		for (WorldCoord wc : cSrc.updates)
			cSrc.wrld.notifyBlockOfNeighborChange( wc.x, wc.y, wc.z, 0 );

		transverseEdges( x - 1, y - 1, z - 1, x + scaleX + 1, y + scaleY + 1, z + scaleZ + 1, new triggerUpdates( src ) );
		transverseEdges( i - 1, j - 1, k - 1, i + scaleX + 1, j + scaleY + 1, k + scaleZ + 1, new triggerUpdates( dst ) );

		transverseEdges( x, y, z, x + scaleX, y + scaleY, z + scaleZ, new triggerUpdates( src ) );
		transverseEdges( i, j, k, i + scaleX, j + scaleY, k + scaleZ, new triggerUpdates( dst ) );

		/*
		 * IChunkProvider cp = dest.getChunkProvider(); if ( cp instanceof
		 * ChunkProviderServer ) { ChunkProviderServer srv =
		 * (ChunkProviderServer) cp; srv.unloadAllChunks(); }
		 * 
		 * cp.unloadQueuedChunks();
		 */

	}

}
