package entity;

import java.awt.Image;
import java.util.List;
import javax.imageio.ImageIO;
import md2.MD2Obj;
import physics.Player;
import physics.PlayerListener;
import renderer.math.Vec3;
import renderer.core.Material;
import renderer.core.Renderer;
import renderer.parser.wavefront.Obj;

/**
 *
 * @author admin
 */
public class Mario extends Entity implements PlayerListener {
    
    private Player player;
    private List<Obj> objsPlayer;
    
    private MD2Obj md2Run;
    private MD2Obj md2Idle;
    private MD2Obj md2Jump;
    private MD2Obj md2Current;
    
    private Material marioMd2Material = new Material("mario");
    
    public Mario(Player player, List<Obj> objsPlayer) {
        this.player = player;
        player.setListener(this);
        
        this.objsPlayer = objsPlayer;
        
        try {
            md2Idle = new MD2Obj("/res/mario_idle.md2", 20);
            md2Run = new MD2Obj("/res/mario_run2.md2", 20);
            md2Jump = new MD2Obj("/res/mario_jump.md2", 20);
            
            md2Current = md2Jump;
            
//            for (String animationName : md2Run.getAnimationNames()) {
//                System.out.println(animationName);
//            }
            
            md2Idle.setAnimation("frame");
            md2Run.setAnimation("frame");
            md2Jump.setAnimation("frame");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        marioMd2Material.map_kd = new renderer.core.Image("/res/mario_all.png");
    }

    @Override
    public void update() {
        player.update();
        

        md2Current.nextFrame(0.5);        
    }

    @Override
    public void draw(Renderer renderer) {
        //renderer.translate(0, -50, 50);        
        
        Vec3 playerPosition = player.getCollider().getPosition();
        renderer.translate(playerPosition.x, playerPosition.y - 9, playerPosition.z);        
        
        renderer.rotateY(player.getAngle() + Math.toRadians(180));
        
        // draw collider
//        for (Obj obj : objsPlayer) {
//            renderer.setMaterial(obj.material);
//            renderer.begin();
//            for (WavefrontParser.Face face : obj.faces) {
//                //uniform[0] = 1;
//                //uniform[1] = 1;
//                //uniform[2] = 1;
//                //renderer.setUniforms(uniform);
//                for (int f=0; f<3; f++) {
//                    Vec4 v = face.vertex[f];
//                    Vec4 n = face.normal[f];
//                    Vec2 t = face.texture[f];
//                    renderer.setTextureCoordinates(t.x, t.y);
//                    renderer.setNormal(n.x, n.y, n.z);
//                    renderer.setVertex(v.x, v.y, v.z);
//                }
//            }
//            renderer.end();            
//        }
        
        // draw md2 animation
        for (int t = 0; t < md2Current.getTriangles().length; t++) {
            renderer.setMaterial(marioMd2Material);
            renderer.begin();
                for (int p=0; p<3; p++) {
                    double[] tv = md2Current.getTriangleVertex(t, p);
                    renderer.setTextureCoordinates(tv[6], 1 - tv[7]);
                    renderer.setNormal(tv[4], tv[5], tv[3]);
                    renderer.setVertex(tv[1], tv[2], tv[0]);
                }
            renderer.end();            
        }
            
    }

    @Override
    public void onPlayerJumpStart() {
        System.out.println("mario jumped !");
    }

    @Override
    public void onPlayerJumpTop() {
        System.out.println("mario jump top arrived !");
    }

    @Override
    public void onPlayerJumpEnd() {
        System.out.println("mario jump end !");
    }

    @Override
    public void onPlayerFoward() {
        md2Current = md2Run;            
    }

    @Override
    public void onPlayerBackward() {
        md2Current = md2Run;
    }

    @Override
    public void onPlayerRotateLeft() {
        md2Current = md2Run;
    }

    @Override
    public void onPlayerRotateRight() {
        md2Current = md2Run;
    }

    @Override
    public void onPlayerStop() {
        md2Current = md2Idle;
    }
    
}
