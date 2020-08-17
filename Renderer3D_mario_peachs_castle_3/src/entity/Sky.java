package entity;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import md2.MD2Obj;
import physics.Player;
import physics.PlayerListener;
import renderer.math.Vec3;
import renderer.core.Material;
import renderer.core.Renderer;
import renderer.math.Vec2;
import renderer.math.Vec4;
import renderer.parser.wavefront.Obj;
import renderer.parser.wavefront.WavefrontParser;

/**
 *
 * @author admin
 */
public class Sky extends Entity {
    
    private Player player;
    private List<Obj> objsSky;
    
    
    public Sky(Player player) {
        this.player = player;
        try {
            objsSky = new ArrayList<Obj>(WavefrontParser.load("/res/sky.obj", 100));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Renderer renderer) {
        //renderer.translate(0, -50, 50);        
        
        Vec3 playerPosition = player.getCollider().getPosition();
        renderer.translate(playerPosition.x, playerPosition.y - 2800, playerPosition.z);        
        
        //renderer.rotateY(-player.getAngle() + Math.toRadians(180));
        for (Obj obj : objsSky) {
            renderer.setMaterial(obj.material);
            renderer.begin();
            for (WavefrontParser.Face face : obj.faces) {
                //uniform[0] = 1;
                //uniform[1] = 1;
                //uniform[2] = 1;
                //renderer.setUniforms(uniform);
                for (int f=0; f<3; f++) {
                    Vec4 v = face.vertex[f];
                    Vec4 n = face.normal[f];
                    Vec2 t = face.texture[f];
                    renderer.setTextureCoordinates(t.x, t.y);
                    renderer.setNormal(n.x, n.y, n.z);
                    renderer.setVertex(v.x, v.y, v.z);
                }
            }
            renderer.end();            
        }        
            
    }

}
