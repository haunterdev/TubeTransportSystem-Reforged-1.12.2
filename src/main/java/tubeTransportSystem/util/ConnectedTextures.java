package tubeTransportSystem.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Port of the 1.7.10 connected-texture selector. Instead of returning IIcon it
 * returns the stitched sprite NAME ("tts:tube0/5"); the baked model resolves the
 * actual TextureAtlasSprite. The connection->index table and per-side neighbour
 * sampling are copied verbatim for visual parity.
 */
public class ConnectedTextures {
    protected final Block block;
    protected final int blockMeta;
    protected final int subMeta;
    protected final String textureLoc;
    private static final short[] connectionToIndex = new short[]{0, 15, 13, 11, 12, 5, 3, 9, 14, 4, 2, 10, 8, 7, 6, 1};

    public ConnectedTextures(String textureLocation, Block b, int meta) {
        this(textureLocation, b, meta, -1);
    }

    public ConnectedTextures(String textureLocation, Block b, int meta, int meta2) {
        this.textureLoc = textureLocation;
        this.block = b;
        this.blockMeta = meta;
        this.subMeta = meta2;
    }

    protected boolean canConnectTo(IBlockAccess blockAccess, BlockPos pos) {
        IBlockState st = blockAccess.getBlockState(pos);
        if (this.block == st.getBlock()) {
            if (this.blockMeta == -1) {
                return true;
            }
            int meta = st.getBlock().getMetaFromState(st);
            if (this.blockMeta == meta) {
                return true;
            }
            return this.subMeta != -1 && meta == this.subMeta;
        }
        return false;
    }

    public String spriteName(int index) {
        return String.format(this.textureLoc, index);
    }

    public String getBaseSpriteName() {
        return spriteName(0);
    }

    public int getIndexForSide(IBlockAccess blockAccess, BlockPos pos, int side) {
        boolean[] c = sampleNeighbours(blockAccess, pos, side);
        return connectionToIndex[(c[0] ? 8 : 0) | (c[1] ? 4 : 0) | (c[2] ? 2 : 0) | (c[3] ? 1 : 0)];
    }

    public int getIndexForSideForInternal(IBlockAccess blockAccess, BlockPos pos, int side) {
        boolean[] c = sampleNeighbours(blockAccess, pos, side);
        int indx = connectionToIndex[(c[0] ? 8 : 0) | (c[1] ? 4 : 0) | (c[2] ? 2 : 0) | (c[3] ? 1 : 0)];
        if (side == 0 || side == 1) {
            if (indx == 2) {
                indx = 3;
            } else if (indx == 3) {
                indx = 2;
            } else if (indx == 4) {
                indx = 5;
            } else if (indx == 5) {
                indx = 4;
            } else if (indx == 10) {
                indx = 9;
            } else if (indx == 9) {
                indx = 10;
            } else if (indx == 12) {
                indx = 14;
            } else if (indx == 14) {
                indx = 12;
            }
        }
        return indx;
    }

    private boolean[] sampleNeighbours(IBlockAccess b, BlockPos p, int side) {
        boolean[] c = new boolean[4];
        if (side == 0 || side == 1) {
            c[0] = canConnectTo(b, p.add(-1, 0, 0));
            c[1] = canConnectTo(b, p.add(1, 0, 0));
            c[2] = canConnectTo(b, p.add(0, 0, 1));
            c[3] = canConnectTo(b, p.add(0, 0, -1));
        } else if (side == 2) {
            c[0] = canConnectTo(b, p.add(1, 0, 0));
            c[1] = canConnectTo(b, p.add(-1, 0, 0));
            c[2] = canConnectTo(b, p.add(0, -1, 0));
            c[3] = canConnectTo(b, p.add(0, 1, 0));
        } else if (side == 3) {
            c[0] = canConnectTo(b, p.add(-1, 0, 0));
            c[1] = canConnectTo(b, p.add(1, 0, 0));
            c[2] = canConnectTo(b, p.add(0, -1, 0));
            c[3] = canConnectTo(b, p.add(0, 1, 0));
        } else if (side == 4) {
            c[0] = canConnectTo(b, p.add(0, 0, -1));
            c[1] = canConnectTo(b, p.add(0, 0, 1));
            c[2] = canConnectTo(b, p.add(0, -1, 0));
            c[3] = canConnectTo(b, p.add(0, 1, 0));
        } else {
            c[0] = canConnectTo(b, p.add(0, 0, 1));
            c[1] = canConnectTo(b, p.add(0, 0, -1));
            c[2] = canConnectTo(b, p.add(0, -1, 0));
            c[3] = canConnectTo(b, p.add(0, 1, 0));
        }
        return c;
    }
}
