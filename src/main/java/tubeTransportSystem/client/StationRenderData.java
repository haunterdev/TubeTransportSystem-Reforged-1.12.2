package tubeTransportSystem.client;

/**
 * Per-side render description for a station block (face s in standard MC numbering).
 * Carries what RenderStation.renderWorldBlock computed before drawing the inner box:
 * the inset bounds (which shift to flush or overlap where a tube or the partner block
 * joins) and the per-face uvRotate value.
 */
public class StationRenderData {
    public final String[] sprite = new String[6];

    /** Inner-box bounds; defaults are the plain 0.01 inset RenderStation starts from. */
    public double minX = 0.01;
    public double minY = 0.01;
    public double minZ = 0.01;
    public double maxX = 0.99;
    public double maxY = 0.99;
    public double maxZ = 0.99;

    /** uvRotate per face for the inner box; 1.7.10 only ever used 0 or 3 (= 180 degrees). */
    public final int[] uvRotation = new int[6];
}
