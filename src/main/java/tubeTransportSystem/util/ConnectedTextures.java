package tubeTransportSystem.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import tubeTransportSystem.block.BlockTube;

/**
 * Port of the 1.7.10 connected-texture selector. Instead of returning IIcon it
 * returns the stitched sprite NAME ("tts:block/tube0/5"); the baked model resolves the
 * actual TextureAtlasSprite. The connection-to-index table and per-side neighbour
 * sampling are copied verbatim for visual parity.
 *
 * <p>"Meta" is now the tube's facing index, so a neighbour connects when it is a tube
 * pointing the same way, exactly as the metadata comparison did.
 */
public class ConnectedTextures {
    protected final BlockTube block;
    protected final int blockMeta;
    protected final int subMeta;
    protected final String textureLoc;
    private static final short[] connectionToIndex = new short[]{0, 15, 13, 11, 12, 5, 3, 9, 14, 4, 2, 10, 8, 7, 6, 1};

    public ConnectedTextures(String textureLocation, BlockTube b, int meta) {
        this(textureLocation, b, meta, -1);
    }

    public ConnectedTextures(String textureLocation, BlockTube b, int meta, int meta2) {
        this.textureLoc = textureLocation;
        this.block = b;
        this.blockMeta = meta;
        this.subMeta = meta2;
    }

    protected boolean canConnectTo(BlockGetter level, BlockPos pos) {
        BlockState st = level.getBlockState(pos);
        if (st.is(this.block)) {
            if (this.blockMeta == -1) {
                return true;
            }
            int meta = BlockTube.metaOf(st);
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

    public int getIndexForSide(BlockGetter level, BlockPos pos, int side) {
        boolean[] c = sampleNeighbours(level, pos, side);
        return connectionToIndex[(c[0] ? 8 : 0) | (c[1] ? 4 : 0) | (c[2] ? 2 : 0) | (c[3] ? 1 : 0)];
    }

    public int getIndexForSideForInternal(BlockGetter level, BlockPos pos, int side) {
        boolean[] c = sampleNeighbours(level, pos, side);
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

    private boolean[] sampleNeighbours(BlockGetter b, BlockPos p, int side) {
        boolean[] c = new boolean[4];
        if (side == 0 || side == 1) {
            c[0] = canConnectTo(b, p.offset(-1, 0, 0));
            c[1] = canConnectTo(b, p.offset(1, 0, 0));
            c[2] = canConnectTo(b, p.offset(0, 0, 1));
            c[3] = canConnectTo(b, p.offset(0, 0, -1));
        } else if (side == 2) {
            c[0] = canConnectTo(b, p.offset(1, 0, 0));
            c[1] = canConnectTo(b, p.offset(-1, 0, 0));
            c[2] = canConnectTo(b, p.offset(0, -1, 0));
            c[3] = canConnectTo(b, p.offset(0, 1, 0));
        } else if (side == 3) {
            c[0] = canConnectTo(b, p.offset(-1, 0, 0));
            c[1] = canConnectTo(b, p.offset(1, 0, 0));
            c[2] = canConnectTo(b, p.offset(0, -1, 0));
            c[3] = canConnectTo(b, p.offset(0, 1, 0));
        } else if (side == 4) {
            c[0] = canConnectTo(b, p.offset(0, 0, -1));
            c[1] = canConnectTo(b, p.offset(0, 0, 1));
            c[2] = canConnectTo(b, p.offset(0, -1, 0));
            c[3] = canConnectTo(b, p.offset(0, 1, 0));
        } else {
            c[0] = canConnectTo(b, p.offset(0, 0, 1));
            c[1] = canConnectTo(b, p.offset(0, 0, -1));
            c[2] = canConnectTo(b, p.offset(0, -1, 0));
            c[3] = canConnectTo(b, p.offset(0, 1, 0));
        }
        return c;
    }
}
