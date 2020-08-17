package renderer.shader;

import renderer.core.Image;
import renderer.core.Light;
import renderer.core.Renderer;
import renderer.core.Shader;
import renderer.math.Vec4;
import renderer.rasterizer.Vertex;

/**
 *
 * @author leonardo
 */
public class GouraudShader extends Shader {
    
    public GouraudShader() {
        super(0, 0, 4);
    }
    
    private Vec4 vertexLightDirection = new Vec4();

    @Override
    public void processVertex(Renderer renderer, Vertex vertex) {
        
        // perspective correct texture mapping
        double zInv = 1 / vertex.p.z;
        vertex.vars[0] = zInv;
        vertex.vars[1] = vertex.st.x * zInv;
        vertex.vars[2] = vertex.st.y * zInv;
        
        // simple light
        Light light = renderer.getLights().get(0);
        vertexLightDirection.set(light.position);
        vertexLightDirection.sub(vertex.p);
        double p = vertex.normal.getRelativeCosBetween(vertexLightDirection);
        if (p < 0.15) {
            p = 0.15;
        }
        else if (p > 1) {
            p = 1;
        }
        vertex.vars[3] = p;
    }
    
    @Override
    public void processPixel(Renderer renderer, int xMin, int xMax, int x, int y, double[] vars) {
        double depth = vars[0];
        double z = 1 / depth;
        double s = vars[1] * z;
        double t = vars[2] * z;
        double colorp = vars[3];

        color[1] = (int) (255 * colorp);
        color[2] = (int) (255 * colorp);
        color[3] = (int) (255 * colorp);
        
        renderer.setPixel(x, y, color, depth);
    }
    
}
