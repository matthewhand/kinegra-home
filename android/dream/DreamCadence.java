package tv.metaxy.dream;

/**
 * Original deterministic and offline-only cadence model. Inspired by the
 * rotating-slide concept in the user's MIT-licensed Omarchy Cadence project,
 * not a port of its shell/Python terminal code.
 */
public final class DreamCadence {
    public static final long SLIDE_MS=20000L;
    public static final long FRAME_MS=125L; // 8 FPS; bounded paint work.
    public static final int SLIDE_COUNT=4;
    private static final char[] INK = {'.', ':', '+', '*', '0', '1', '/', '\\', '|', '#'};
    private DreamCadence(){}
    public static int slide(long ms){
        if(ms<0)return 0;
        return (int)((ms/SLIDE_MS)%SLIDE_COUNT);
    }
    public static long frame(long ms){return ms<0?0:ms/FRAME_MS;}
    public static int driftX(long ms){return (int)((frame(ms)/64L)%25L)-12;}
    public static int driftY(long ms){return (int)((frame(ms)/96L)%17L)-8;}
    public static int columns(int width){return clamp(width/35,36,116);}
    public static int rows(int height){return clamp(height/60,12,36);}
    public static int clamp(int x,int low,int high){return Math.max(low,Math.min(high,x));}
    public static char glyph(long frame,int x,int y){
        long v=(frame/4L + x*17L + y*31L + x*y*7L);
        int idx=(int)Math.floorMod(v,INK.length);
        return INK[idx];
    }
    public static String label(int slot){
        switch(slot){
            case 0:return "THE SPACE BETWEEN";
            case 1:return "SYNTHETIC SIGNAL";
            case 2:return "DEEP SYSTEM IDLE";
            case 3:return "TERMINAL CADENCE";
            default:return "SHIELDARCHY";
        }
    }
    public static String lowerText(int slot){
        switch(slot){
            case 0:return "ONE SCREEN  /  TWO WORLDS";
            case 1:return "0101  ::  NEON DATA / LOCAL ONLY";
            case 2:return "NO NETWORK  ::  NO MEDIA CAPTURE";
            case 3:return "A DREAM BY METAXY TV";
            default:return "LOCAL";
        }
    }
}
