package techguns.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import techguns.Techguns;

public class BlockTGLamp<T extends Enum<T> & IStringSerializable> extends GenericBlock {
	
	public PropertyEnum<T> LAMP_TYPE;
	protected Class<T> clazz;
	protected BlockStateContainer blockStateOverride;
	protected GenericItemBlockMeta itemblock;
	
	public static final PropertyBool CONNECT_DOWN = PropertyBool.create("down");
	public static final PropertyBool CONNECT_UP = PropertyBool.create("up");
	public static final PropertyBool CONNECT_N = PropertyBool.create("north");
	public static final PropertyBool CONNECT_S = PropertyBool.create("south");
	public static final PropertyBool CONNECT_W = PropertyBool.create("west");
	public static final PropertyBool CONNECT_E = PropertyBool.create("east");
    
	
	public BlockTGLamp(String name,  Class<T> clazz) {
		super(name, Material.IRON, MapColor.YELLOW);
		this.clazz=clazz;
		this.LAMP_TYPE = PropertyEnum.create("lamp_type", clazz);
		this.blockStateOverride = new BlockStateContainer.Builder(this).add(LAMP_TYPE).add(FACING_ALL).add(CONNECT_DOWN)
				.add(CONNECT_UP).add(CONNECT_N).add(CONNECT_S).add(CONNECT_W).add(CONNECT_E).build();
		this.setDefaultState(this.getBlockState().getBaseState());
		
		setHardness(0.25f);
		this.setSoundType(SoundType.GLASS);
		setLightLevel(1.0f);
		setLightOpacity(0);
	}
	
	public BlockStateContainer getBlockState() {
		return this.blockStateOverride;
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return this.getMetaFromState(getDefaultState().withProperty(LAMP_TYPE, state.getValue(LAMP_TYPE)));
	}
	
	  /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
    
	@Override
	public ItemBlock createItemBlock() {
		this.itemblock =  new GenericItemBlockMeta(this);
		return itemblock;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState s = this.getDefaultState();
		if (meta <6) {
			s = s.withProperty(LAMP_TYPE, this.clazz.getEnumConstants()[0]).withProperty(FACING_ALL,EnumFacing.getFront(meta));
		} else if (meta <12) {
			s = s.withProperty(LAMP_TYPE, this.clazz.getEnumConstants()[1]).withProperty(FACING_ALL,EnumFacing.getFront(meta-6));
		} else if (meta == 12) {
			s = s.withProperty(LAMP_TYPE, this.clazz.getEnumConstants()[2]);
		} else if (meta == 13) {
			s = s.withProperty(LAMP_TYPE, this.clazz.getEnumConstants()[3]);
		}
		return s;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		T t = state.getValue(LAMP_TYPE);
		if (t.ordinal() == 0) {
			return state.getValue(FACING_ALL).getIndex();
		} else if (t.ordinal() == 1) {
			return 6+state.getValue(FACING_ALL).getIndex();
		} else if (t.ordinal() == 2) {
			return 12;
		} else if (t.ordinal() == 3) {
			return 13;
		}
		return 0;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		int type = state.getValue(LAMP_TYPE).ordinal();
		if (type==0 || type==1) {
			return state.withProperty(CONNECT_UP, false).withProperty(CONNECT_DOWN,false).withProperty(CONNECT_E,false)
					.withProperty(CONNECT_W, false).withProperty(CONNECT_N, false).withProperty(CONNECT_S, false);
		} else {
			
			boolean u = this.canPlaceAt(worldIn, pos, EnumFacing.DOWN);
			boolean d = this.canPlaceAt(worldIn, pos, EnumFacing.UP);
			boolean n = this.canPlaceAt(worldIn, pos, EnumFacing.SOUTH);
			boolean s = this.canPlaceAt(worldIn, pos, EnumFacing.NORTH);
			boolean w = this.canPlaceAt(worldIn, pos, EnumFacing.EAST);
			boolean e = this.canPlaceAt(worldIn, pos, EnumFacing.WEST);
			
			return state.withProperty(CONNECT_UP, u).withProperty(CONNECT_DOWN,d).withProperty(CONNECT_E,e)
					.withProperty(CONNECT_W, w).withProperty(CONNECT_N, n).withProperty(CONNECT_S, s);
			
		}
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		IBlockState state = this.getStateFromMeta(meta);
		return state.withProperty(FACING_ALL, facing.getOpposite());
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
		if(tab == this.getCreativeTabToDisplayOn()){
			for (T t : clazz.getEnumConstants()) {
				items.add(new ItemStack(this,1,this.getMetaFromState(getDefaultState().withProperty(LAMP_TYPE, t))));
			}
		}
	}

	public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        for (EnumFacing enumfacing : FACING_ALL.getAllowedValues())
        {
            if (this.canPlaceAt(worldIn, pos, enumfacing))
            {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceAt(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        BlockPos blockpos = pos.offset(facing.getOpposite());
        IBlockState iblockstate = worldIn.getBlockState(blockpos);
        Block block = iblockstate.getBlock();
        BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, blockpos, facing);

        if ((facing.equals(EnumFacing.UP)||facing.equals(EnumFacing.DOWN)) && this.canPlaceOn(worldIn, blockpos))
        {
            return true;
        }
        else if (facing != EnumFacing.UP && facing != EnumFacing.DOWN)
        {
            return !isExceptBlockForAttachWithPiston(block) && blockfaceshape == BlockFaceShape.SOLID;
        }
        else
        {
            return false;
        }
    }
    
    private boolean canPlaceOn(IBlockAccess worldIn, BlockPos pos)
    {
        IBlockState state = worldIn.getBlockState(pos);
        return state.getBlock().canPlaceTorchOnTop(state, worldIn, pos);
    }
    
	@SideOnly(Side.CLIENT)
	@Override
	public void registerItemBlockModels() {
		for(int i = 0; i< clazz.getEnumConstants().length;i++) {
			IBlockState state = getDefaultState().withProperty(LAMP_TYPE, clazz.getEnumConstants()[i]);
			ModelLoader.setCustomModelResourceLocation(itemblock, this.getMetaFromState(state), new ModelResourceLocation(new ResourceLocation(Techguns.MODID,"lamp_inventory_"+clazz.getEnumConstants()[i].getName()),"inventory"));
		}
	}

}
