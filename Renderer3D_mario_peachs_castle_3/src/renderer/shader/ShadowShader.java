package renderer.shader;

import renderer.buffer.ColorBuffer;
import renderer.buffer.DepthBuffer;
import renderer.core.Light;
import renderer.core.Renderer;
import renderer.core.Shader;
import renderer.math.Vec4;
import renderer.rasterizer.Vertex;

/**
 *
 * @author leonardo
 */
public class ShadowShader extends Shader {

    private ColorBuffer colorBuffer;
    private DepthBuffer shadowMap;
    
    private static final int UNIFORMS_SIZE = 3;
    private static final int VERTEX_EXTRA_DATA_SIZE = 0;
    private static final int VERTEX_VARS_SIZE = 3;
    
    public ShadowShader(int width, int height) {
        super(UNIFORMS_SIZE, VERTEX_EXTRA_DATA_SIZE, VERTEX_VARS_SIZE);
        shadowMap = new DepthBuffer(width, height);
        //shadowMap.setClearValue(Double.MAX_VALUE);
        colorBuffer = new ColorBuffer(width, height);
    }

    public DepthBuffer getShadowMap() {
        return shadowMap;
    }

    public ColorBuffer getColorBuffer() {
        return colorBuffer;
    }

    @Override
    public void processVertex(Renderer renderer, Vertex v) {
        //renderer.doVertexMVPTransformation(v);
        
        double zInv = 1 / v.p.z;
        v.vars[0] = v.p.x * zInv;
        v.vars[1] = v.p.y * zInv;
        v.vars[2] = zInv;

        normal.set(v.normal);
        //v.vars[3] = normal.x * zInv;
        //v.vars[4] = normal.y * zInv;
        //v.vars[5] = normal.z * zInv;
    }
    
    private Vec4 normal = new Vec4();
    private Vec4 pointToLight = new Vec4();
    
    @Override
    public void processPixel(Renderer renderer, int xMin, int xMax, int x, int y, double[] vars) {
        double zInv = vars[2];
        double pz = 1 / zInv;
        double px = vars[0] * pz;
        double py = vars[1] * pz;
        
        //double nx = vars[3] * pz;
        //double ny = vars[4] * pz;
        //double nz = vars[5] * pz;
        //normal.set(nx, ny, nz, 0);
        
        Light light = renderer.getLights().get(0);
        
        pointToLight.set(px, py, pz, 1);
        pointToLight.sub(light.position);
        
        double pointToLightDistance = -1 / pointToLight.y;
        double p = -965.685424949238 / pointToLight.y; // precalculated
        pointToLight.multiply(p);
        
        int zx = (int) (pointToLight.x);
        int zy = (int) (-pointToLight.z);
        
        //colorBuffer.getPixel(zx, zy, color);
        
        double shadowMapDistance = -shadowMap.get(zx, zy);
        
        //pointToLight.multiply(-1);
        //double cosTheta = normal.getRelativeAngleBetween(pointToLight);
        //double bias = 0.00007525 * Math.tan(cosTheta); // cosTheta is dot( n,l ), clamped between 0 and 1
        //bias = bias < 0 ? 0 : bias;
        //bias = bias > 0.000015 ? 0.000015 : bias;
        
        double bias = 0.002;
        if (Math.abs(pointToLightDistance*pz - shadowMapDistance*pz) > bias) {
            //double dif = shadowMapDistance - pointToLightDistance;
            double q = 0;
            //if (dif <= 0.0003) {
            //    q = 1 - dif / 0.0003;
            //}
            
            color[1] = (int) (255 * (uniforms[0] * (0.5 + 0.5 * q)));
            color[2] = (int) (255 * (uniforms[1] * (0.5 + 0.5 * q)));
            color[3] = (int) (255 * (uniforms[2] * (0.5 + 0.5 * q)));
        }
        else {
            color[1] = (int) (255 * uniforms[0]);
            color[2] = (int) (255 * uniforms[1]);
            color[3] = (int) (255 * uniforms[2]);
        }
        
//        if (x == xMin || x == xMax) {
//            color[1] = 0;
//            color[2] = 0;
//            color[3] = 0;
//        }
        
        renderer.setPixel(x, y, color, zInv);
    }
    
}
