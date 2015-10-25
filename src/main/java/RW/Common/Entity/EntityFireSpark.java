/**
 * This Class Created By Lord_Crystalyx.
 */
package RW.Common.Entity;

import java.util.List;

import RW.Core.RogueWorldCore;
import RW.Utils.MathUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityFireSpark extends Entity implements IProjectile
{
	private int field_145795_e = -1;
	private int field_145793_f = -1;
	private int field_145794_g = -1;
	private Block field_145796_h;
	private boolean inGround;
	public EntityLivingBase shootingEntity;
	private int ticksAlive;
	private int ticksInAir;
	public double accelerationX;
	public double accelerationY;
	public double accelerationZ;
	public float damage = 2.0F;

	public EntityFireSpark(World p_i1759_1_)
	{
		super(p_i1759_1_);
		this.setSize(0.5F, 0.5F);
	}

	@Override
	protected void entityInit()
	{
	}

	/**
	 * Checks if the entity is in range to render by using the past in distance
	 * and comparing it to its average edge length * 64 * renderDistanceWeight
	 * Args: distance
	 */
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.boundingBox.getAverageEdgeLength() * 4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1 * d1;
	}

	public EntityFireSpark(World w, double x, double y, double z, double p_i1760_8_, double p_i1760_10_, double p_i1760_12_)
	{
		super(w);
		this.setSize(1.0F, 1.0F);
		this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
		this.setPosition(x, y, z);
		double d6 = (double) MathHelper.sqrt_double(p_i1760_8_ * p_i1760_8_ + p_i1760_10_ * p_i1760_10_ + p_i1760_12_ * p_i1760_12_);
		this.accelerationX = p_i1760_8_ / d6 * 0.1D;
		this.accelerationY = p_i1760_10_ / d6 * 0.1D;
		this.accelerationZ = p_i1760_12_ / d6 * 0.1D;
	}

	public EntityFireSpark(World w, EntityPlayer p, double px, double py, double pz)
	{
		super(w);
		this.shootingEntity = p;
		this.setSize(1.0F, 1.0F);
		this.setLocationAndAngles(p.posX, p.posY, p.posZ, p.rotationYawHead, p.rotationPitch);
		this.setPosition(this.posX, this.posY, this.posZ);
		this.yOffset = 0.0F;
		this.motionX = this.motionY = this.motionZ = 0.0D;
		double x = px + this.rand.nextGaussian() * 0.4D;
		double y = py + this.rand.nextGaussian() * 0.4D;
		double z = pz + this.rand.nextGaussian() * 0.4D;
		double f = 1;
		MathUtils.getMotionsByEntity(1, this);

		if (p.getCurrentEquippedItem() != null)
		{
			this.damage = 10;
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate()
	{
		if (!this.worldObj.isRemote && (this.shootingEntity != null && this.shootingEntity.isDead || !this.worldObj.blockExists((int) this.posX, (int) this.posY, (int) this.posZ)))
		{
			this.setDead();
		} else
		{
			super.onUpdate();
			List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(this.posX - 2.0F, this.posY - 2.0F, this.posZ - 2.0F, this.posX + 2.0F, this.posY + 2.0F, this.posZ + 2.0F));
			for (int l = 0; l < entities.size(); l++)
			{
				entities.get(l).attackEntityFrom(DamageSource.generic.setDamageBypassesArmor(), this.damage);
				if(entities.get(l) instanceof EntityDragonPart)
				{
					((EntityDragonPart)entities.get(l)).entityDragonObj.attackEntityFromPart((EntityDragonPart) entities.get(l), DamageSource.generic.setDamageBypassesArmor(), this.damage);
				}
			}

			if (this.inGround)
			{
				if (this.worldObj.getBlock(this.field_145795_e, this.field_145793_f, this.field_145794_g) == this.field_145796_h)
				{
					++this.ticksAlive;

					if (this.ticksAlive == 600)
					{
						this.setDead();
					}

					return;
				}

				this.inGround = false;
				this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
				this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
				this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
				this.ticksAlive = 0;
				this.ticksInAir = 0;
			} else
			{
				++this.ticksInAir;
			}

			Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			Vec3 vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec3, vec31);
			vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			vec31 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			if (movingobjectposition != null)
			{
				vec31 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
			}

			Entity entity = null;
			List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = (Entity) list.get(i);

				if (entity1.canBeCollidedWith() && (!entity1.isEntityEqual(this.shootingEntity) || this.ticksInAir >= 25))
				{
					float f = 0.3F;
					AxisAlignedBB axisalignedbb = entity1.boundingBox.expand((double) f, (double) f, (double) f);
					MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec3, vec31);

					if (movingobjectposition1 != null)
					{
						double d1 = vec3.distanceTo(movingobjectposition1.hitVec);

						if (d1 < d0 || d0 == 0.0D)
						{
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null)
			{
				movingobjectposition = new MovingObjectPosition(entity);
			}

			if (movingobjectposition != null)
			{
				this.onImpact(movingobjectposition);
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			float f1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (Math.atan2(this.motionZ, this.motionX) * 180.0D / Math.PI) + 90.0F;

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			float f2 = this.getMotionFactor();

			if (this.isInWater())
			{
				for (int j = 0; j < 4; ++j)
				{
					float f3 = 0.25F;
					this.worldObj.spawnParticle("bubble", this.posX - this.motionX * (double) f3, this.posY - this.motionY * (double) f3, this.posZ - this.motionZ * (double) f3, this.motionX, this.motionY, this.motionZ);
				}

				f2 = 0.8F;
			}

			this.motionX += this.accelerationX;
			this.motionY += this.accelerationY;
			this.motionZ += this.accelerationZ;
			this.motionX *= (double) f2;
			this.motionY *= (double) f2;
			this.motionZ *= (double) f2;
			if (this.worldObj.isRemote)
			{
				this.worldObj.spawnParticle("smoke", this.posX, this.posY  - 1.5D, this.posZ, 0.0D, 0.0D, 0.0D);
				RogueWorldCore.proxy.spawnParticle("spark", this.posX, this.posY -1.5D, this.posZ, 0.0D, 0.0D, 0.0D);
			}
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	/**
	 * Return the motion factor for this projectile. The factor is multiplied by
	 * the original motion.
	 */
	protected float getMotionFactor()
	{
		return 1F;
	}

	/**
	 * Called when this EntityFireball hits a block or entity.
	 */
	protected void onImpact(MovingObjectPosition mop)
	{

		if (!this.worldObj.isRemote)
		{
			if (mop.entityHit instanceof EntityLivingBase)
			{
				mop.entityHit.hurtResistantTime = 0;
				mop.entityHit.attackEntityFrom(DamageSource.generic.setDamageBypassesArmor(), this.damage);
			}
			this.setDead();
		}
	}

	/**
	 * (abstract) Protected helper method to write subclass entity data to NBT.
	 */
	public void writeEntityToNBT(NBTTagCompound p_70014_1_)
	{
		p_70014_1_.setShort("xTile", (short) this.field_145795_e);
		p_70014_1_.setShort("yTile", (short) this.field_145793_f);
		p_70014_1_.setShort("zTile", (short) this.field_145794_g);
		p_70014_1_.setByte("inTile", (byte) Block.getIdFromBlock(this.field_145796_h));
		p_70014_1_.setByte("inGround", (byte) (this.inGround ? 1 : 0));
		p_70014_1_.setTag("direction", this.newDoubleNBTList(new double[]
		{ this.motionX, this.motionY, this.motionZ }));
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readEntityFromNBT(NBTTagCompound p_70037_1_)
	{
		this.field_145795_e = p_70037_1_.getShort("xTile");
		this.field_145793_f = p_70037_1_.getShort("yTile");
		this.field_145794_g = p_70037_1_.getShort("zTile");
		this.field_145796_h = Block.getBlockById(p_70037_1_.getByte("inTile") & 255);
		this.inGround = p_70037_1_.getByte("inGround") == 1;

		if (p_70037_1_.hasKey("direction", 9))
		{
			NBTTagList nbttaglist = p_70037_1_.getTagList("direction", 6);
			this.motionX = nbttaglist.func_150309_d(0);
			this.motionY = nbttaglist.func_150309_d(1);
			this.motionZ = nbttaglist.func_150309_d(2);
		} else
		{
			this.setDead();
		}
	}

	/**
	 * Returns true if other Entities should be prevented from moving through
	 * this Entity.
	 */
	public boolean canBeCollidedWith()
	{
		return true;
	}

	public float getCollisionBorderSize()
	{
		return 1.0F;
	}

	/**
	 * Called when the entity is attacked.
	 */
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		if (this.isEntityInvulnerable())
		{
			return false;
		} else
		{
			this.setBeenAttacked();

			if (p_70097_1_.getEntity() != null)
			{
				Vec3 vec3 = p_70097_1_.getEntity().getLookVec();

				if (vec3 != null)
				{
					this.motionX = vec3.xCoord;
					this.motionY = vec3.yCoord;
					this.motionZ = vec3.zCoord;
					this.accelerationX = this.motionX * 0.1D;
					this.accelerationY = this.motionY * 0.1D;
					this.accelerationZ = this.motionZ * 0.1D;
				}

				if (p_70097_1_.getEntity() instanceof EntityLivingBase)
				{
					this.shootingEntity = (EntityLivingBase) p_70097_1_.getEntity();
				}

				return true;
			} else
			{
				return false;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public float getShadowSize()
	{
		return 10.0F;
	}

	/**
	 * Gets how bright this entity is.
	 */
	public float getBrightness(float p_70013_1_)
	{
		return 1.0F;
	}

	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float p_70070_1_)
	{
		return 15728880;
	}

	/**
	 * Gets the amount of gravity to apply to the thrown entity with each tick.
	 */
	protected float getGravityVelocity()
	{
		return 0.00F;
	}

	@Override
	public void setThrowableHeading(double p_70186_1_, double p_70186_3_, double p_70186_5_, float p_70186_7_, float p_70186_8_)
	{
		this.onUpdate();

	}
}
