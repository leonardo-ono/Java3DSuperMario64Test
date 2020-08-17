package physics;


import renderer.math.Vec3;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import physics.partition.Box;

/**
 * Player class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Player {

    private Sphere collider = new Sphere();
    private double angle;
    private final Vec3 velocity = new Vec3();
    private final Box box = new Box();
    
    private PlayerListener listener;
    private boolean jumping;
    
    
    public Player() {
        
        //collider = new Sphere(0.5, 25.85, -92.81, 41.92);
        collider = new Sphere(10.0, 257.1, 71.6, -1064.1);
    }

    public void setListener(PlayerListener listener) {
        this.listener = listener;
    }

    public Sphere getCollider() {
        return collider;
    }

    public double getAngle() {
        return angle;
    }

    public Vec3 getVelocity() {
        return velocity;
    }

    public void update() {
        boolean turnLeftOfRight = false;
        if (Input.isKeyPressed(KeyEvent.VK_LEFT)) {
            //sphere.getPosition().x -= 0.03;
            angle -= 0.0075;
            turnLeftOfRight = true;
            if (listener != null) {
                listener.onPlayerRotateLeft();
            }
        }
        else if (Input.isKeyPressed(KeyEvent.VK_RIGHT)) {
            //sphere.getPosition().x += 0.03;
            angle += 0.0075;
            turnLeftOfRight = true;
            if (listener != null) {
                listener.onPlayerRotateRight();
            }
        }
        
        double speedAdd = 1;
        
        double speed = 0;
        if (turnLeftOfRight || Input.isKeyPressed(KeyEvent.VK_UP)) {
            speed = -speedAdd;
            //if (turnLeftOfRight) {
            //    speed *= 0.25;
            //}
            //sphere.getPosition().y += 0.03;
            if (listener != null) {
                listener.onPlayerFoward();
            }
        }
        else if (Input.isKeyPressed(KeyEvent.VK_DOWN)) {
            speed = speedAdd;
            //sphere.getPosition().y -= 0.03;
            if (listener != null) {
                listener.onPlayerBackward();
            }
        }
        velocity.x = 0;
        velocity.z = 0;
        //velocity.x *= 0.95;
        //velocity.z *= 0.95;
        
        if (speed != 0) {
            double dx = speed * Math.cos(angle + Math.toRadians(90));
            double dz = speed * Math.sin(angle + Math.toRadians(90));
            velocity.x = dx;
            velocity.z = dz;
        }
        else {
            if (listener != null) {
                listener.onPlayerStop();
            }
        }

        if (Input.isKeyPressed(KeyEvent.VK_A)) {
            speed = speedAdd;
            double dx = speed * Math.cos(angle - Math.toRadians(180));
            double dz = speed * Math.sin(angle - Math.toRadians(180));
            velocity.x = dx;
            velocity.z = dz;
        }
        else if (Input.isKeyPressed(KeyEvent.VK_D)) {
            speed = -speedAdd;
            double dx = speed * Math.cos(angle - Math.toRadians(180));
            double dz = speed * Math.sin(angle - Math.toRadians(180));
            velocity.x += dx;
            velocity.z += dz;
        }
        
        if (jumping) {
            System.out.println("velocity y = " + velocity.y);
        }
        
        if (jumping && velocity.y > 1) {
            jumping = false;
            if (listener != null) {
                listener.onPlayerJumpEnd();
            }
        }
        else if (Input.isKeyPressed(KeyEvent.VK_Z)) {
            jumping = true;
            velocity.y += 3;
            if (velocity.y > 3) {
                velocity.y = 3;
            }

            if (listener != null) {
                listener.onPlayerJumpStart();
            }
            //sphere.getPosition().y -= 0.03;
        }
        
        // gravity
        velocity.y -= 0.025;


        
//        if (Input.isKeyPressed(KeyEvent.VK_Q)) {
//            collider.getPosition().y -= 0.025;
//        }
//        else if (Input.isKeyPressed(KeyEvent.VK_A)) {
//            collider.getPosition().y += 0.025;
//        }
        
        //System.out.println("player position: " + collider.getPosition());
        
    }
    
    public void draw(Graphics2D g, double scale) {
        collider.draw(g, scale);
    }

    public void draw3D(Graphics2D g, double scale) {
        collider.draw3D(g, scale, angle, collider.getPosition());
    }

    public Box getBox() {
        box.getMin().set(collider.getPosition());
        box.getMin().x -= collider.getRadius();
        box.getMin().y -= collider.getRadius();
        box.getMin().z -= collider.getRadius();
        box.getMax().set(collider.getPosition());
        box.getMax().x += collider.getRadius();
        box.getMax().y += collider.getRadius();
        box.getMax().z += collider.getRadius();
        box.updateLength();
        return box;
    }
    
}
