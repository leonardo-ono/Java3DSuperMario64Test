package renderer.shader;

import renderer.core.Renderer;
import renderer.core.Shader;
import renderer.rasterizer.Vertex;

/**
 *
 * @author leonardo
 */
public class ShadowMapShader extends Shader {

    private static final int UNIFORMS_SIZE = 3;
    private static final int VERTEX_EXTRA_DATA_SIZE = 0;
    private static final int VERTEX_VARS_SIZE = 1;
    
    public ShadowMapShader() {
        super(UNIFORMS_SIZE, VERTEX_EXTRA_DATA_SIZE, VERTEX_VARS_SIZE);
    }

    @Override
    public void processVertex(Renderer renderer, Vertex v) {
        //renderer.doVertexMVPTransformation(v);
        v.vars[0] = 1 / v.p.z;
    }
    
    @Override
    public void processPixel(Renderer renderer, int xMin, int xMax, int x, int y, double[] vars) {
        double zInv = vars[0];
        double depth = 1 / zInv;
        double p = depth / -2500;
        p = p < 0 ? 0 : p;
        p = p > 1 ? 1 : p;
        int c = (int) (255 * p);
        color[1] = c;
        color[2] = c;
        color[3] = c;
        depth = depth < 0 ? -depth : depth;

        color[1] = (int) (255 * uniforms[0]);
        color[2] = (int) (255 * uniforms[1]);
        color[3] = (int) (255 * uniforms[2]);
        
        //renderer.setPixel(x, y, color, depth);
        renderer.getDepthBuffer().set(x, y, zInv);
    }
    
}
