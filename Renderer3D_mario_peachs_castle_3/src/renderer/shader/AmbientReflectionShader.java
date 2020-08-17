package renderer.shader;

import renderer.core.Image;
import renderer.core.Renderer;
import renderer.core.Shader;
import renderer.math.Vec4;
import renderer.rasterizer.Vertex;

/**
 *
 * @author leonardo
 */
public class AmbientReflectionShader extends Shader {

    public AmbientReflectionShader() {
        this(3, 0, 6);
    }
    
    private AmbientReflectionShader(int uniformsSize, int vertexExtraDatasSize, int vertexVarsSize) {
        super(uniformsSize, vertexExtraDatasSize, vertexVarsSize);
    }

    @Override
    public void processVertex(Renderer renderer, Vertex v) {
        //renderer.doVertexMVPTransformation(v);
        
        noriginal.set(v.normal.x, v.normal.y, v.normal.z, 0);
        
        double zInv = 1 / v.p.z;
        v.vars[0] = v.p.x * zInv;
        v.vars[1] = v.p.y * zInv;
        v.vars[2] = zInv;

        v.vars[3] = v.normal.x * zInv;
        v.vars[4] = v.normal.y * zInv;
        v.vars[5] = v.normal.z * zInv;
    }
    
    private Vec4 noriginal = new Vec4();
    private Vec4 normal = new Vec4();
    private Vec4 i = new Vec4();
    private Vec4 r = new Vec4();
    
    @Override
    public void processPixel(Renderer renderer, int xMin, int xMax, int x, int y, double[] vars) {
        
        double pz = 1 / vars[2];
        double px = vars[0] * pz;
        double py = vars[1] * pz;
        
        double nx = vars[3] * pz;
        double ny = vars[4] * pz;
        double nz = vars[5] * pz;
        
        // smooth normals
        normal.set(nx, ny, nz, 0);
        
        // flat normals
        //normal.set(noriginal);
        
        // calculate reflection: i = d - 2 * (d.n) * n
        i.set(px, py, pz, 0);
        normal.multiply(2 * i.dot(normal));
        Vec4.sub(i, normal, r);
        //System.out.println(r);
        
        Image posx = renderer.getTextures().get(0);
        Image negx = renderer.getTextures().get(1);
        Image posy = renderer.getTextures().get(2);
        Image negy = renderer.getTextures().get(3);
        Image posz = renderer.getTextures().get(4);
        Image negz = renderer.getTextures().get(5);
        
        int textureHalfSize = posx.getWidth() / 2;
        
        double tx = Math.abs(r.x);
        double ty = Math.abs(r.y);
        double tz = Math.abs(r.z);
        double f = 0;
        
        int textX = 0;
        int textY = 0;
        
        if (tz >= tx && tz >= ty) {
            f = textureHalfSize / tz;
            r.multiply(f);
            if (r.z >= 0) {
                textX = (int) r.x + textureHalfSize;
                textY = (int) (textureHalfSize - r.y);
                posz.getPixel(textX, textY, color);
            }
            else {
                textX = (int) (textureHalfSize - r.x);
                textY = (int) (textureHalfSize - r.y);
                negz.getPixel(textX, textY, color);
            }
        }
        else if (tx >= ty && tx >= tz) {
            f = textureHalfSize / tx;
            r.multiply(f);
            if (r.x >= 0) {
                textX = (int) (textureHalfSize - r.z);
                textY = (int) (textureHalfSize - r.y);
                posx.getPixel(textX, textY, color);
            }
            else {
                textX = (int) r.z + textureHalfSize;
                textY = (int) (textureHalfSize - r.y);
                negx.getPixel(textX, textY, color);
            }
        }
        else if (ty >= tx && ty >= tz) {
            f = textureHalfSize / ty;
            r.multiply(f);
            if (r.y >= 0) {
                textX = (int) r.x + textureHalfSize;
                textY = (int) r.z + textureHalfSize;
                posy.getPixel(textX, textY, color);
            }
            else {
                textX = (int) r.x + textureHalfSize;
                textY = (int) (textureHalfSize - r.z);
                negy.getPixel(textX, textY, color);
            }
        }
        
//        color[1] = 255;
//        color[2] = 255;
//        color[3] = 255;
        color[1] -= 100;
        color[2] -= 100;
        color[3] -= 100;

        //color[1] *= 0.75;
        //color[2] *= 0.75;
        //color[3] *= 0.75;
        
        color[1] = color[1] > 255 ? 255 : color[1];
        color[2] = color[2] > 255 ? 255 : color[2];
        color[3] = color[3] > 255 ? 255 : color[3];

        color[1] = color[1] < 0 ? 0 : color[1];
        color[2] = color[2] < 0 ? 0 : color[2];
        color[3] = color[3] < 0 ? 0 : color[3];
        
        renderer.setPixel(x, y, color, vars[2]);
    }
    
}
