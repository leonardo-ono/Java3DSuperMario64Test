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
public class PhongShaderWithTexture extends Shader {
    
    public PhongShaderWithTexture() {
        super(0, 0, 8);
    }
    
    private Vec4 vertexLightDirection = new Vec4();
    private Vec4 normal = new Vec4();
    private Vec4 p = new Vec4();

    @Override
    public void processVertex(Renderer renderer, Vertex vertex) {
        //renderer.doVertexMVPTransformation(vertex);
        
        // perspective correct texture mapping
        double zInv = 1 / vertex.p.z;
        vertex.vars[0] = zInv;
        vertex.vars[1] = vertex.st.x * zInv;
        vertex.vars[2] = vertex.st.y * zInv;
        
        // vertex normal interpolation
        vertex.vars[3] = vertex.normal.x;
        vertex.vars[4] = vertex.normal.y;
        vertex.vars[5] = vertex.normal.z;
        
        // pixel x, y coordinates interpolation
        vertex.vars[6] = vertex.p.x * zInv;
        vertex.vars[7] = vertex.p.y * zInv;
    }
    
    private int[] color = new int[] { 255, 255, 255 ,255};
    private int[] color2 = new int[] { 255, 255, 255 ,255};

    @Override
    public void processPixel(Renderer renderer, int xMin, int xMax, int x, int y, double[] vars) {
        double depth = vars[0];
        double pz = 1 / depth;
        double s = vars[1] * pz;
        double t = vars[2] * pz;
        
        Image texture = renderer.getTextures().get(0);
        int tx = (int) (s * (texture.getWidth() - 1));
        int textureHeight = texture.getHeight() - 1;
        int ty = textureHeight - (int) (t * textureHeight);
        texture.getPixel(tx, ty, color);
        
        // simple light
        double px = vars[6] * pz;
        double py = vars[7] * pz;
        p.set(px, py, pz, 1);
        normal.set(vars[3], vars[4], vars[5], 0);
        
        color2[1] = 0;
        color2[2] = 0;
        color2[3] = 0;
        
        for (Light light : renderer.getLights()) {
            vertexLightDirection.set(light.position);
            vertexLightDirection.sub(p);
            double colorp = normal.getRelativeCosBetween(vertexLightDirection);
            if (colorp < 0.25) {
                colorp = 0.25;
            }
            else if (colorp > 1) {
                colorp = 1;
            }

            color2[1] += (int) (color[1] * (light.diffuse.x * colorp));
            color2[2] += (int) (color[2] * (light.diffuse.y *colorp));
            color2[3] += (int) (color[3] * (light.diffuse.z *colorp));
        }
        color2[1] = color2[1] > 255 ? 255 : color2[1];
        color2[2] = color2[2] > 255 ? 255 : color2[2];
        color2[3] = color2[3] > 255 ? 255 : color2[3];
        
        renderer.setPixel(x, y, color2, depth);
    }
    
}
