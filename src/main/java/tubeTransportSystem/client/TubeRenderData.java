package tubeTransportSystem.client;

/**
 * Immutable per-tube render description computed in BlockTube.getExtendedState
 * (which has world+pos) and consumed by TubeBakedModel. Keeps all neighbour
 * sampling out of the baked model. Face index s uses standard MC numbering:
 * 0=DOWN 1=UP 2=NORTH 3=SOUTH 4=WEST 5=EAST (EnumFacing.byIndex(s)).
 *
 * Face culling is NOT held here: the original culls both the outer skin and the
 * inner connector box through Block.shouldSideBeRendered, so BlockTube overrides
 * that and vanilla does the culling per side.
 */
public class TubeRenderData {
    public final int direction;
    /** Outer skin sprite name per face s. */
    public final String[] outerSprite = new String[6];
    /** Inner connector sprite name per face s. */
    public final String[] innerSprite = new String[6];
    /** canConnectTo by EnumFacing ordinal (D0,U1,N2,S3,W4,E5); drives inner-box inset. */
    public final boolean[] connByFacing = new boolean[6];

    public TubeRenderData(int direction) {
        this.direction = direction;
    }
}
