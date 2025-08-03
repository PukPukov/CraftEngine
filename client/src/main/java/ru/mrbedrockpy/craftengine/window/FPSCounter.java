package ru.mrbedrockpy.craftengine.window;

public class FPSCounter {
    private long previousTime;
    private long secStart;
    private int frames;
    private int fps;

    public FPSCounter() {
        this.previousTime = System.currentTimeMillis();
        this.secStart = previousTime;
        this.frames = 0;
        this.fps = 0;
    }

    public void update() {
        frames++;
        long currentTime = System.currentTimeMillis();
        int frametime = (int) (currentTime-this.previousTime);
        if (currentTime - this.previousTime > 5) {
//            System.out.println("STUTTER: "+frametime);
        }
        if (currentTime - secStart >= 1000) {
            fps = frames;
            frames = 0;
            secStart = currentTime;
        }
        this.previousTime = currentTime;
    }

    public int fps() {
        return fps;
    }
}