package main;

import input.Mouse;
import input.Keyboard;
import entity.Mario;
import entity.Sky;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import physics.Input;
import physics.World;
import renderer.core.Light;
import renderer.core.Renderer;
import renderer.core.Shader;
import renderer.core.Time;
import static renderer.core.Renderer.MatrixMode.*;
import renderer.math.Vec2;
import renderer.math.Vec4;
import renderer.parser.wavefront.Obj;
import renderer.parser.wavefront.WavefrontParser;
import renderer.shader.GouraudShaderWithTexture;

/**
 *
 * @author leo
 */
public class ViewCanvas extends Canvas {
    
    private boolean running = false;
    private BufferStrategy bs;

    private Renderer renderer;
    private Thread thread;
    
    private Shader gouraudShader = new GouraudShaderWithTexture();
    
    private Light light = new Light();

    private List<Obj> objs;
    private List<Obj> objsPlayer;
    
    // physics
    private final World world = new World();
    
    private Mario mario;
    private Sky sky;
    
    public ViewCanvas() {
        addKeyListener(new KeyHandler());
        addKeyListener(new Input());
    }
    
    public void init() {
        createBufferStrategy(1);
        bs = getBufferStrategy();
        
        try {
            objs = new ArrayList<Obj>(WavefrontParser.load("/res/princess_peaches_castle_(outside).obj", 200));
            //objs = new ArrayList<Obj>(WavefrontParser.load("/res/level_mesh_collider.obj", 20));
            objsPlayer = new ArrayList<Obj>(WavefrontParser.load("/res/sphere.obj", 10));
        } catch (Exception ex) {
            Logger.getLogger(ViewCanvas.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        
        mario = new Mario(world.getPlayer(), objsPlayer);
        sky = new Sky(world.getPlayer());
        
        renderer = new Renderer(440, 330);
        //renderer = new Renderer(600, 450);
        //renderer = new Renderer(3200, 2400);
        
        // light
        light.diffuse.set(1, 1, 1, 1);
        renderer.addLight(light);

        // textures
        //Image texture = new Image("/res/brick.jpg");
        //Image texture = new Image("/res/corridor.png");
        //Image texture = new Image("/res/new_thin_zombie.png");
        //renderer.addTexture(texture);
        
        // shader
        renderer.setShader(gouraudShader);
        
        renderer.setMatrixMode(PROJECTION);
        renderer.setPerspectiveProjection(Math.toRadians(60));
        //double d = 400 / Math.tan(Math.toRadians(30));
        
        renderer.setClipZNear(-0.01);
        //renderer.setClipZfar(-15000);
        
        running = true;
        thread = new Thread(new MainLoop());
        thread.start();
        
        MouseHandler mouseHandler = new MouseHandler();
        addMouseMotionListener(mouseHandler);
    }
    
    private double angle = -9.999;
    private double[] uniform = new double[3];
    
    private double cameraDz = 360;
    private double cameraDx = 0;

    private double lightX = 100;
    private double lightZ = 100;
    
    private double cameraTargetAngleX = 0;
    private double cameraTargetAngleY = Math.toRadians(30);

    private double cameraAngleX = 0;
    private double cameraAngleY = Math.toRadians(30);
    private Vec4 cameraPosition = new Vec4(100, 150, 0, 50);
    
    private void cameraMove(double d, double dAngle) {
        double s = Math.sin(cameraAngleY + dAngle);
        double c = Math.cos(cameraAngleY + dAngle);
        cameraPosition.x += (c * d);
        cameraPosition.z += (s * d);
    }
    
    public void update() {
        world.update();
        mario.update();
        sky.update();
        
        boolean firstPerson = false;

        // third person        
        if (!firstPerson) {
            cameraTargetAngleX = Mouse.y;
            //cameraTargetAngleY = Mouse.x;
            cameraTargetAngleY = world.getPlayer().getAngle() + Math.toRadians(90);

            cameraAngleX += (Math.toRadians(cameraTargetAngleX * 0.5) - cameraAngleX) * 0.1;
            //cameraAngleY += (Math.toRadians(cameraTargetAngleY * 0.5) - cameraAngleY) * 0.1;
            cameraAngleY += (cameraTargetAngleY - cameraAngleY) * 0.05;

            //cameraAngleX = -0.5;
            //cameraAngleY = world.getPlayer().getAngle() + Math.toRadians(90);
        }
        
        // first person
        if (firstPerson) {
            cameraTargetAngleX = Mouse.y;
            cameraTargetAngleY = world.getPlayer().getAngle() + Math.toRadians(90);
            
            cameraAngleX += (Math.toRadians(cameraTargetAngleX * 0.5) - cameraAngleX) * 0.1;
            cameraAngleY += (cameraTargetAngleY - cameraAngleY) * 0.25;
        }
        
        
//        double velocity = 0.1;
//        
//        if (Keyboard.keyDown[37]) {
//            cameraMove(velocity, Math.toRadians(90));
//        }
//        else if (Keyboard.keyDown[39]) {
//            cameraMove(-velocity, Math.toRadians(90));
//        }
//        
//        if (Keyboard.keyDown[38]) {
//            cameraMove(-velocity, 0);
//        }
//        else if (Keyboard.keyDown[40]) {
//            cameraMove(velocity, 0);
//        }
//
//        if (Keyboard.keyDown[87]) {
//            cameraPosition.y -= velocity;
//        }
//        else if (Keyboard.keyDown[83]) {
//            cameraPosition.y += velocity;
//        }

// third person
cameraPosition.x = world.getPlayer().getCollider().getPosition().x + 200 * Math.cos(world.getPlayer().getAngle() + Math.toRadians(90));
cameraPosition.y = world.getPlayer().getCollider().getPosition().y + 180;
cameraPosition.z = world.getPlayer().getCollider().getPosition().z + 200 * Math.sin(world.getPlayer().getAngle() + Math.toRadians(90));

// first person
if (firstPerson) {
    cameraPosition.x = world.getPlayer().getCollider().getPosition().x + 0 * Math.cos(world.getPlayer().getAngle() + Math.toRadians(90));
    cameraPosition.y = world.getPlayer().getCollider().getPosition().y + 8;
    cameraPosition.z = world.getPlayer().getCollider().getPosition().z + 0 * Math.sin(world.getPlayer().getAngle() + Math.toRadians(90));
}

//        if (Keyboard.keyDown[65]) {
//            cameraTargetAngleY -= 5;
//        }
//        else if (Keyboard.keyDown[68]) {
//            cameraTargetAngleY += 5;
//        }
        
        //System.out.println("dx=" + cameraPosition.x + " dz=" + cameraPosition.y + " angle: " + cameraPosition.z);
        
//        renderer.setMatrixMode(PROJECTION);
//        double fov = Math.toRadians(dolly);
//        renderer.setPerspectiveProjection(fov);
//        
//        double dCamera = 400 / Math.tan(fov);
//        double dObjeto = 50 / Math.tan(fov);
//        cameraDz = dCamera - dObjeto;
        
        //light.position.translate(lightX, 100, lightZ);
        renderer.setBackfaceCullingEnabled(true);
        
        renderer.clearAllBuffers(); 
        //renderer.getDepthBuffer().clear();
        
        // drawTriangleManually();
        drawFace1();
        

        // draw mario        
        renderer.setMatrixMode(VIEW);
        renderer.setIdentity();
        renderer.rotateX(cameraAngleX + Math.PI * 2);
        renderer.rotateY(-cameraAngleY + Math.PI * 0.5);
        renderer.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        renderer.setMatrixMode(MODEL);
        renderer.setIdentity();
        mario.draw(renderer);

        renderer.setMatrixMode(VIEW);
        renderer.setIdentity();
        renderer.rotateX(cameraAngleX + Math.PI * 2);
        renderer.rotateY(-cameraAngleY + Math.PI * 0.5);
        renderer.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);
        renderer.setMatrixMode(MODEL);
        renderer.setIdentity();
        sky.draw(renderer);
    }

    

    private double mx;
    private double my;
    
    private void drawFace1() {
        renderer.setMatrixMode(VIEW);
        renderer.setIdentity();
        renderer.rotateX(cameraAngleX + Math.PI * 2);
        renderer.rotateY(-cameraAngleY + Math.PI * 0.5);
        renderer.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        renderer.setMatrixMode(MODEL);
        renderer.setIdentity();
        //renderer.translate(0, -400 + cameraPosition.y, 50);        
        //renderer.rotateX(Math.toRadians(-90));
        
        for (Obj obj : objs) {
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

    int[] color = new int[] { 255, 255, 0, 255 }; // rgba
    int count = 0;

    double crazySize = 200;
    double crazySizeY = 0;
    
    public void draw(Graphics2D g) {
        Graphics2D g2d = (Graphics2D) renderer.getColorBuffer().getColorBuffer().getGraphics();
        g2d.setColor(Color.WHITE);
        g2d.drawString("FPS: " + Time.fps, 10, 20);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);        
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 3200, 2400, 0, 0, 6400, 4800, null);
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 1600, 1200, 0, 0, 3200, 2400, null);
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 1200, 900, 0, 0, 1600, 1200, null);
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 800, 600, 0, 0, 1200, 900, null);
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 600, 450, 0, 0, 800, 600, null);
        
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 420, 315, 0, 0, 440, 330, null);
        g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 400, 300, 0, 0, 440, 330, null);
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 400, 300, 0, 0, 600, 450, null);
        
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 410, 300, 0, 0, 420, 310, null);
        //g2d.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 400, 290, 0, 0, 410, 300, null);
        //g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);        
        g.drawImage(renderer.getColorBuffer().getColorBuffer(), 0, 0, 800, 600, 0, 0, 400, 300, null);
    }
    
    private class MainLoop implements Runnable {

        @Override
        public void run() {
            while (running) {
                Time.update();
                update();
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                draw(g);
                g.dispose();
                bs.show();
            }
        }
        
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            Mouse.x = e.getX() - getWidth() * 0.5;
            Mouse.y = getHeight() * 0.5 - e.getY();
        }
        
    }

    private class KeyHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            Keyboard.keyDown[e.getKeyCode()] = true;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            Keyboard.keyDown[e.getKeyCode()] = false;
        }
        
    }
    
}
